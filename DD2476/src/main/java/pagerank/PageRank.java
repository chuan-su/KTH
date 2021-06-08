package pagerank;

import ir.PostingsEntry;

import java.util.*;
import java.io.*;
import java.util.stream.IntStream;

public class PageRank {

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory;
     */
    final static int MAX_NUMBER_OF_DOCS = 1000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   The adjacency matrix. p[i][j] = -1 if page i links to page j
     */
    double[][] p = new double[MAX_NUMBER_OF_DOCS][MAX_NUMBER_OF_DOCS];

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

  	/**
		 *   The transition matrix. p[i][j] = the probability that the
		 *   random surfer clicks from page i to page j.
		 */
		double[][] tm;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   In the initializaton phase, we use a negative number to represent 
     *   that there is a direct link from a document to another.
     */
    final static double LINK = -1.0;
    
    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
			int noOfDocs = readDocs( filename );
			initiateProbabilityMatrix( noOfDocs);
			iterate(noOfDocs, 100 );
    }

    /* --------------------------------------------- */

    /**
     *   Reads the documents and fills the data structures. When this method 
     *   finishes executing, <code>p[i][j] = LINK</code> if there is a direct
     *   link from i to j, and <code>p[i][j] = 0</code> otherwise.
     *   <p>
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
						// Set the probability to LINK for now, to indicate that there is
						// a link from d to otherDoc.
						if ( p[fromdoc][otherDoc] >= 0 ) {
							p[fromdoc][otherDoc] = LINK;
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
     *   Initiates the probability matrix. 
     */
		void initiateProbabilityMatrix( int numberOfDocs ) {
			tm = new double[numberOfDocs][numberOfDocs];
			for (int i = 0; i < numberOfDocs; i++) {
				int numberOfOutDegrees = out[i];
				if (numberOfOutDegrees == 0) {
					Arrays.fill(tm[i], 1. / numberOfDocs);
					continue;
				}
				for (int j = 0; j < numberOfDocs; j++) {
					double probability = 0;
					if (p[i][j] == LINK) {
						probability = (1 - BORED) * (1. / numberOfOutDegrees);
					}
					tm[i][j] = probability + BORED * (1. / numberOfDocs);
				}
			}
    }

    /* --------------------------------------------- */

    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
    void iterate( int numberOfDocs , int maxIterations ) {
			assert tm != null : "transition matrix must not be null, initiateProbabilityMatrix must be called first";
			assert tm.length == numberOfDocs : "transition matrix must be square matrix with length = numberOfDocs";

    	double[] pv = initialProbabilityVector(numberOfDocs);

			int iteration = 0;
			while (iteration <= maxIterations) {
				double[] npv = new double[numberOfDocs];

				for (int i = 0; i < numberOfDocs; i ++) {
					for (int j = 0; j < numberOfDocs; j ++) {
						npv[j] += tm[i][j] * pv[i];
					}
				}

				if (arrDelta(pv, npv) <= EPSILON) {
					break;
				}

				pv = Arrays.copyOf(npv, npv.length);
				iteration ++;
			}

			writePageRankScoreToFile(pv, 30);
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

		private void writePageRankScoreToFile(double[] pv, int k) {

			try {
				BufferedOutputStream outputStream = new BufferedOutputStream( new FileOutputStream(new File("files/svwiki1000_top_30_result.txt")));
				PrintStream out = new PrintStream( outputStream, true, "UTF-8" );

				IntStream.range(0, pv.length)
					.mapToObj(i -> {
						int docId = Integer.parseInt(docName[i]);
						double score = pv[i];
						//double score = BigDecimal.valueOf(pv[i]).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
						return new PostingsEntry(docId, score);
					})
					.sorted((e1, e2) -> Double.compare(e2.score, e1.score))
					.limit(k)
					.forEach(e -> out.printf("%d: %.5f\n",e.docID, e.score));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    /* --------------------------------------------- */


    public static void main( String[] args ) {
			if ( args.length != 1 ) {
				System.err.println( "Please give the name of the link file" );
			}
			else {
				new PageRank( args[0] );
			}
		}
}