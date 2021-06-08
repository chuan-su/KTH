package pagerank;

import ir.PostingsEntry;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MonteCarlo {
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

  double[] monteCarloAlgo1(int numberOfPages, int N) {
    Random rand = new Random();
    double[] pv = new double[numberOfPages];

    for (int run = 0; run < N; run ++) {
      int page = rand.nextInt(numberOfPages);

      while (rand.nextDouble() > BORED) {
        int outDegrees = out[page];
        if (outDegrees == 0) {
          page = rand.nextInt(numberOfPages);
        } else {
          List<Integer> a = new ArrayList<>(link.get(page).keySet());
          page = a.get(rand.nextInt(outDegrees));
        }
      }
      pv[page] += 1.0 / N;
    }
    return pv;
  }

  double[] monteCarloAlgo2(int numberOfPages, int M) {
    Random rand = new Random();
    double[] pv = new double[numberOfPages];

    for (int m = 0; m < M; m ++) {
      for (int n = 0; n < numberOfPages; n++ ) {
        int page = n;

        while (rand.nextDouble() > BORED) {
          int outDegrees = out[page];
          if (outDegrees == 0) {
            page = rand.nextInt(numberOfPages);
          } else {
            List<Integer> a = new ArrayList<>(link.get(page).keySet());
            page = a.get(rand.nextInt(outDegrees));
          }
        }
        pv[page] += 1.0 / (M * numberOfPages);
      }
    }
    return pv;
  }

  double[] monteCarloAlgo4(int numberOfPages, int M) {
    Random rand = new Random();
    double[] visits = new double[numberOfPages];
    int totalVisits = 0;

    for (int m = 0; m < M; m ++) {
      for (int n = 0; n < numberOfPages; n++ ) {
        int page = n;
        totalVisits ++;
        visits[page] ++;
        while (rand.nextDouble() > BORED) {
          int outDegrees = out[page];
          if (outDegrees == 0) {
            break;
          } else {
            List<Integer> a = new ArrayList<>(link.get(page).keySet());
            page = a.get(rand.nextInt(outDegrees));
            totalVisits ++;
            visits[page] ++;
          }
        }
      }
    }

    for (int i = 0; i < visits.length; i ++) {
      visits[i] = visits[i] * 1.0 / totalVisits;
    }

    return visits;
  }

  double[] monteCarloAlgo5(int numberOfPages, int N) {
    Random rand = new Random();
    double[] visits = new double[numberOfPages];
    int totalVisits = 0;
    for (int run = 0; run < N; run ++) {
      int page = rand.nextInt(numberOfPages);
      visits[page] ++;
      totalVisits ++;
      while (rand.nextDouble() > BORED) {
        int outDegrees = out[page];
        if (outDegrees == 0) {
          break;
        }
        List<Integer> a = new ArrayList<>(link.get(page).keySet());
        page = a.get(rand.nextInt(outDegrees));
        visits[page] ++;
        totalVisits ++;
      }
    }

    for (int i = 0; i < visits.length; i ++) {
      visits[i] = visits[i] * 1.0 / totalVisits;
    }

    return visits;
  }

  int randomWalk(int numberOfPages, Optional<Integer> startPage) {
    Random rand = new Random();

    int page = startPage.orElse(rand.nextInt(numberOfPages));

    while (rand.nextDouble() > BORED) {
      int outDegrees = out[page];
      if (outDegrees == 0) {
        page = rand.nextInt(numberOfPages);
      } else {
        List<Integer> a = new ArrayList<>(link.get(page).keySet());
        page = a.get(rand.nextInt(outDegrees));
      }
    }
    return page;
  }

  /*
   *   Chooses a probability vector a, and repeatedly computes
   *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
   */
  double[] iterate( int numberOfDocs, int maxIterations ) {

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
    return pv;
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
  private void writePageRankScoreToFile(double[] pv,  int k) {

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
        .limit(k)
        .forEach(e -> out.printf("%d: %.5f\n",e.docID, e.score));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<Page> topKPages(double[] pv, Optional<Integer> k) {
    return IntStream.range(0, pv.length)
      .mapToObj(i -> {
        int docId = Integer.parseInt(docName[i]);
        double score = pv[i];
        return new Page(docId, score);
      })
      .sorted((e1, e2) -> Double.compare(e2.score, e1.score))
      .limit(k.orElse(pv.length))
      .collect(Collectors.toList());
  }

  private static double squareSum(List<Page> a, List<Page> b) {
    assert a.size() == b.size();

    double squareSum = 0.0;

    for (int i = 0; i < a.size(); i ++) {
      squareSum += Math.pow((a.get(i).score - b.get(i).score), 2);
    }
    return squareSum;
  }

  private static void printArr(double[] arr) {
    System.out.println(Arrays.stream(arr).boxed().map(String::valueOf).collect(Collectors.joining(",")));
  }

  public static void main( String[] args ) {
    if ( args.length != 1 ) {
      System.err.println( "Please give the name of the link file" );
    } else if (args[0].equals("files/linksSvwiki.txt")) {
      System.out.println("links");
      MonteCarlo mc = new MonteCarlo();
      int noOfDocs = mc.readDocs(args[0]);

      double[] pageRankSimScore5 = mc.monteCarloAlgo5(noOfDocs, 2 * noOfDocs);
      List<Page> pages5 = mc.topKPages(pageRankSimScore5, Optional.of(30));
      try {
        BufferedOutputStream outputStream = new BufferedOutputStream( new FileOutputStream(new File("files/montecarlo5_score.txt")));
        PrintStream out = new PrintStream( outputStream, true, "UTF-8" );
        pages5.forEach(e -> out.printf("%d: %.5f\n",e.docId, e.score));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      MonteCarlo mc = new MonteCarlo();
      int noOfDocs = mc.readDocs(args[0]);

      double[] pageRankScores = mc.iterate( noOfDocs, 1000 );
      List<Page> pages = mc.topKPages(pageRankScores, Optional.of(30));
//      String s = pages.stream().map(p -> "(" + p.docId + ", " + p.score + ")").collect(Collectors.joining(","));
//      System.out.println("PageRank: " + s);
//      System.out.println();
//
      List<Double> result1 = new ArrayList<>();
      List<Double> result2 = new ArrayList<>();
      List<Double> result4 = new ArrayList<>();
      List<Double> result5 = new ArrayList<>();

      List<Integer> runs = new ArrayList<>();

      for (int i = 2; i <= 40; i = i + 2) {
        runs.add(i * noOfDocs);

        double[] pageRankSimScore1 = mc.monteCarloAlgo1(noOfDocs, i * noOfDocs);

        double[] pageRankSimScore2 = mc.monteCarloAlgo2(noOfDocs, i);
        double[] pageRankSimScore4 = mc.monteCarloAlgo4(noOfDocs, i);
        double[] pageRankSimScore5 = mc.monteCarloAlgo5(noOfDocs, i * noOfDocs);

        List<Page> pages1 = mc.topKPages(pageRankSimScore1, Optional.empty());
        List<Page> pages2 = mc.topKPages(pageRankSimScore2, Optional.empty());
        List<Page> pages4 = mc.topKPages(pageRankSimScore4, Optional.empty());
        List<Page> pages5 = mc.topKPages(pageRankSimScore5, Optional.empty());

        List<Page> p1 = pages
          .stream()
          .map(p -> pages1.stream().filter(pm -> pm.docId == p.docId).findFirst().get())
          .collect(Collectors.toList());

        List<Page> p2 = pages
          .stream()
          .map(p -> pages2.stream().filter(pm -> pm.docId == p.docId).findFirst().get())
          .collect(Collectors.toList());

        List<Page> p4 = pages
          .stream()
          .map(p -> pages4.stream().filter(pm -> pm.docId == p.docId).findFirst().get())
          .collect(Collectors.toList());

        List<Page> p5 = pages
          .stream()
          .map(p -> pages5.stream().filter(pm -> pm.docId == p.docId).findFirst().get())
          .collect(Collectors.toList());

//        String s1 = p1.stream().map(p -> "(" + p.docId + ", " + p.score + ")").collect(Collectors.joining(","));
//        String s2 = p2.stream().map(p -> "(" + p.docId + ", " + p.score + ")").collect(Collectors.joining(","));
//        String s4 = p4.stream().map(p -> "(" + p.docId + ", " + p.score + ")").collect(Collectors.joining(","));
//        String s5 = p5.stream().map(p -> "(" + p.docId + ", " + p.score + ")").collect(Collectors.joining(","));
//
//        System.out.println("MC1 : " + s1);
//        System.out.println();
//        System.out.println("MC2 : " + s2);
//        System.out.println();
//        System.out.println("MC4 : " + s4);
//        System.out.println();
//        System.out.println("MC5 : " + s5);
//        System.out.println();

        double d1 = squareSum(p1, pages);
        double d2 = squareSum(p2, pages);
        double d4 = squareSum(p4, pages);
        double d5 = squareSum(p5, pages);

        result1.add(d1);
        result2.add(d2);
        result4.add(d4);
        result5.add(d5);

        System.out.println(d1 + "    " + d2 + "    " + d4 + "    " + d5);
      }

      String rs = runs.stream().map(String::valueOf).collect(Collectors.joining(","));
      String s1 = result1.stream().map(String::valueOf).collect(Collectors.joining(","));
      String s2 = result2.stream().map(String::valueOf).collect(Collectors.joining(","));
      String s4 = result4.stream().map(String::valueOf).collect(Collectors.joining(","));
      String s5 = result5.stream().map(String::valueOf).collect(Collectors.joining(","));

      System.out.println("MC1 : [" + s1 + "]");
      System.out.println();
      System.out.println("MC2 : [" + s2 + "]");
      System.out.println();
      System.out.println("MC4 : [" + s4 + "]");
      System.out.println();
      System.out.println("MC5 : [" + s5 + "]");
      System.out.println();
      System.out.println("RUNS: [" + rs + "]");

    }
  }
}
