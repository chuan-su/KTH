package ndcg;

import ir.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class NDCG {

  Engine engine;

  Map<String, Integer> averageScores;

  /**  The results of a search query. */
  PostingsList results;

  /**  The query posed by the user. */
  private Query query;

  public NDCG(Engine engine, Map<String, Integer> averageScores) {
    this.engine = engine;
    this.averageScores = averageScores;
  }

  public void search(String term, boolean[] relevant) {
    String queryString = term.toLowerCase().trim();
    query = new Query( queryString );

    if (relevant.length > 0) {
      query.relevanceFeedback( results, relevant, engine );
    }

    results = engine.searcher.search( query, QueryType.RANKED_QUERY, RankingType.TF_IDF );
  }

  public double evaluate(int k, Optional<String> docToExclude) {
    if (docToExclude.isPresent()) {
      List<PostingsEntry> a = results.stream()
        .filter(postingsEntry -> {
          String docName = engine.index.docNames.get(postingsEntry.docID).split("/")[1];
          return !docToExclude.get().equals(docName);
        })
        .collect(Collectors.toList());

      averageScores.remove(docToExclude.get());

      results = new PostingsList();
      results.addAll(a);
    }

    double dcg = IntStream.rangeClosed(1, k)
      .mapToDouble(i -> {
        PostingsEntry postingsEntry = results.get(i-1);

        String docName = engine.index.docNames.get(postingsEntry.docID).split("/")[1];
        int rel = averageScores.getOrDefault(docName, 0);
        // double log2 = rel / (Math.log(i + 1) / Math.log(2.0));
        return rel / Math.log(i + 1);
      })
      .reduce(0.0, (a, b) -> a + b);

    List<Integer> scores = averageScores.values().stream()
      .sorted(Collections.reverseOrder())
      .collect(toList());

    double idcg = IntStream.rangeClosed(1, k)
      .mapToDouble(i -> {
        int rel = scores.get(i - 1);
        return rel / Math.log(i + 1);
      })
      .reduce(0.0, (a, b) -> a + b);

    double ndcg = dcg / idcg;
    return ndcg;
  }

  public static void main( String[] args ) {
    Map<String, Integer> averageScore = new HashMap<>();

    try (BufferedReader br = Files.newBufferedReader(Paths.get("files/average_relevance.txt"))){
      br.lines()
        .forEach(line -> {
          String[] pair = line.split(" ");
          averageScore.put(pair[0], Integer.valueOf(pair[1]));
        });
    } catch (IOException e) {
      System.out.println("IO exception");
      System.exit(1);
    }

    Engine e = new Engine( args );
    NDCG ndcg = new NDCG(e, averageScore);

    String term = "graduate program mathematics";
    // first evaluation
    ndcg.search(term, new boolean[0]);
    double ndcg1 = ndcg.evaluate(50, Optional.empty());

    boolean[] relevance = new boolean[ndcg.results.size()];
    IntStream.range(0, ndcg.results.size())
      .filter(i -> {
        int docId = ndcg.results.get(i).docID;
        String docName = e.index.docNames.get(docId).split("/")[1];

        return docName.equals("Mathematics.f");
      })
      .findFirst()
      .ifPresent(idx -> relevance[idx] = true);


    // second evaluation after relevance feedback
    ndcg.search(term, relevance);
    double ndcg2 = ndcg.evaluate(50, Optional.of("Mathematics.f"));

    System.out.println("ndcg1: " + ndcg1);
    System.out.println("ndcg2: " + ndcg2);

    /*
       idx = 385 doc: Mathematics.f docID: 16027
       ndcg1: 0.15537038602288047
       ndcg2: 0.3112081032361729

     */
  }
}
