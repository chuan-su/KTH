package pagerank;

import ir.PostingsEntry;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PageRankSparse {

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

       
    /* --------------------------------------------- */


    public PageRankSparse( String filename ) {
			int noOfDocs = readDocs( filename );
			iterate( noOfDocs, 1000 );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
			int fileIndex = 0;
			try {
				System.err.print( "Reading file... " );
				BufferedReader in = new BufferedReader( new FileReader( filename ));
				String line;
				while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
					int index = line.indexOf( ";" );
					String title = line.substring( 0, index );
					Integer fromdoc = docNumber.get( title );
					//  Have we seen this document before?
					if ( fromdoc == null ) {
						// This is a previously unseen doc, so add it to the table.
						fromdoc = fileIndex++;
						docNumber.put( title, fromdoc );
						docName[fromdoc] = title;
					}
					// Check all outlinks.
					StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
					while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
						String otherTitle = tok.nextToken();
						Integer otherDoc = docNumber.get( otherTitle );
						if ( otherDoc == null ) {
							// This is a previousy unseen doc, so add it to the table.
							otherDoc = fileIndex++;
							docNumber.put( otherTitle, otherDoc );
							docName[otherDoc] = otherTitle;
						}
						// Set the probability to 0 for now, to indicate that there is
						// a link from fromdoc to otherDoc.
						if ( link.get(fromdoc) == null ) {
							link.put(fromdoc, new HashMap<Integer,Boolean>());
						}
						if ( link.get(fromdoc).get(otherDoc) == null ) {
							link.get(fromdoc).put( otherDoc, true );
							out[fromdoc]++;
						}
					}
				}
				if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
					System.err.print( "stopped reading since documents table is full. " );
				}
				else {
					System.err.print( "done. " );
				}
			}
			catch ( FileNotFoundException e ) {
				System.err.println( "File " + filename + " not found!" );
			}
			catch ( IOException e ) {
				System.err.println( "Error reading file " + filename );
			}
			System.err.println( "Read " + fileIndex + " number of documents" );
			return fileIndex;
		}


    /* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
    void iterate( int numberOfDocs, int maxIterations ) {

			// YOUR CODE HERE
			double[] pv = initialProbabilityVector(numberOfDocs);

			int iteration = 0;
			while (iteration <= maxIterations) {
				double[] npv = new double[numberOfDocs];

				for (int i = 0; i < numberOfDocs; i ++) {
					int numberOfOutDegrees = out[i];
					if (numberOfOutDegrees == 0) {
						for (int j = 0; j < numberOfDocs; j ++) {
							npv[j] += (pv[i] * (1. / numberOfDocs));
						}
						continue;
					}
					Map<Integer, Boolean> row = link.get(i);
					for (int j = 0; j < numberOfDocs; j++) {
						double probability = 0;
						if (row.containsKey(j) && row.get(j)) {
							probability += ((1 - BORED) * (1. / numberOfOutDegrees));
						}
						probability += (BORED * (1. / numberOfDocs));
						npv[j] += (pv[i] * probability);
					}
				}
				if (arrDelta(pv, npv) <= EPSILON) {
					break;
				}

				pv = Arrays.copyOf(npv, npv.length);
				iteration ++;
			}
			//System.out.println(Arrays.stream(pv).boxed().map(String::valueOf).collect(Collectors.joining(",")));
			writePageRankScoreToFile(pv);
		}

	private double[] initialProbabilityVector(int numberOfDocs) {
		double[] pv = new double[numberOfDocs];

		Arrays.fill(pv, 1. / numberOfDocs);
		return pv;
	}

	private double arrDelta(double[] a, double[] b) {
		return IntStream.range(0, a.length)
			.mapToDouble(i -> Math.abs(a[i] - b[i]))
			.reduce(0, Double::sum);
	}

	/* --------------------------------------------- */
	private void writePageRankScoreToFile(double[] pv) {

		try {
			BufferedOutputStream outputStream = new BufferedOutputStream( new FileOutputStream(new File("files/davis_score.txt")));
			PrintStream out = new PrintStream( outputStream, true, "UTF-8" );

			IntStream.range(0, pv.length)
				.mapToObj(i -> {
					int docId = Integer.parseInt(docName[i]);
					double score = pv[i];
					//double score = BigDecimal.valueOf(pv[i]).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
					return new PostingsEntry(docId, score);
				})
				.sorted((e1, e2) -> Double.compare(e2.score, e1.score))
				.forEach(e -> out.printf("%d: %.5f\n",e.docID, e.score));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) {
		if ( args.length != 1 ) {
			System.err.println( "Please give the name of the link file" );
		}
		else {
			new PageRankSparse( args[0] );
		}
	}
}