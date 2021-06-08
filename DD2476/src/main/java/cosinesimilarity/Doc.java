package cosinesimilarity;

import ir.Index;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class Doc {

  public String name;
  private Map<String, TfIdf> tfIdfMap = new HashMap<>();

  public Doc(String name) {
    this.name = name;
  }

  public void add(String term, TfIdf tfIdf) {
    tfIdfMap.put(term, tfIdf);
  }

  public TfIdf getTF_IDF(String term) {
    return tfIdfMap.get(term);
  }

  public int getTF(String term) {
    return Optional.ofNullable(tfIdfMap.get(term)).map(tfIdf -> tfIdf.tf).orElse(0);
  }

  public double getIDF(String term) {
    return Optional.ofNullable(tfIdfMap.get(term)).map(tfIdf -> tfIdf.idf).orElse(0.0);
  }

  public Set<String> terms() {
    return tfIdfMap.keySet();
  }

  public double euclideanLength(Space space) {
    double length;
    switch (space) {
      case TF:
        length = tfIdfMap.values().stream().mapToDouble(tfIdf -> tfIdf.tf).reduce(0.0, (result, tf) -> result + Math.pow(tf, 2));
        break;
      case TF_IDF:
        length = tfIdfMap.values().stream().map(TfIdf::value).reduce(0.0, (result, tfidf) -> result + Math.pow(tfidf, 2));
        break;
      default:
        throw new UnsupportedOperationException("unsupported space type");
    }
    length = Math.sqrt(length);
    return BigDecimal.valueOf(length).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  public double manhattanLength(Space space) {
    double length;
    switch (space) {
      case TF:
        length = tfIdfMap.values().stream().mapToDouble(tfIdf -> tfIdf.tf).reduce(0.0, (result, tf) -> result + Math.abs(tf));
        break;
      case TF_IDF:
        length = tfIdfMap.values().stream().map(TfIdf::value).reduce(0.0, (result, tfidf) -> result + Math.abs(tfidf));
        break;
      default:
        throw new UnsupportedOperationException("unsupported space type");
    }
    return BigDecimal.valueOf(length).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  public static Doc newDoc(String name, List<String> terms, Index index, int totalNrOfDocs) {
    Doc doc = new Doc(name);

    Map<String, Long> frequency = terms.stream().collect(groupingBy(Function.identity(), counting()));

    for (Map.Entry<String, Long> e : frequency.entrySet()) {
      String term = e.getKey();

      int tf = e.getValue().intValue();
      int df = index.getPostings(term).size();
      doc.add(term, new TfIdf(tf, df, totalNrOfDocs));
    }
    return doc;
  }
}
