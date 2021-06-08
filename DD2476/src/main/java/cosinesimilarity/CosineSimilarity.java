package cosinesimilarity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CosineSimilarity {

  private Doc a, b;

  public CosineSimilarity(Doc a, Doc b) {
    this.a = a;
    this.b = b;
  }

  public double get(Space space, Norm norm) {
    List<String> terms = Stream.concat(a.terms().stream(), b.terms().stream()).distinct()
      .collect(Collectors.toList());

    double cosineSim;

    if (Space.TF == space) {
      int[] va = terms.stream().mapToInt(t -> Optional.ofNullable(a.getTF_IDF(t)).map(tfIdf -> tfIdf.tf).orElse(0)).toArray();
      int[] vb = terms.stream().mapToInt(t -> Optional.ofNullable(b.getTF_IDF(t)).map(tfIdf -> tfIdf.tf).orElse(0)).toArray();
      int dotProduct = dotProduct(va, vb);
      double length = norm == Norm.Euclidean ? a.euclideanLength(space) * b.euclideanLength(space) : a.manhattanLength(space) * b.manhattanLength(space);

      cosineSim = dotProduct * 1.0 / length;

    } else {
      double[] va = terms.stream().mapToDouble(t -> Optional.ofNullable(a.getTF_IDF(t)).map(TfIdf::value).orElse(0.0)).toArray();
      double[] vb = terms.stream().mapToDouble(t -> Optional.ofNullable(b.getTF_IDF(t)).map(TfIdf::value).orElse(0.0)).toArray();

      double dotProduct = dotProduct(va, vb);
      double length = norm == Norm.Euclidean ? a.euclideanLength(space) * b.euclideanLength(space) : a.manhattanLength(space) * b.manhattanLength(space);

      cosineSim = dotProduct * 1.0 / length;
    }

    return BigDecimal.valueOf(cosineSim).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  private int dotProduct(int[] a, int[] b) {
    assert a.length == b.length;

    return IntStream.range(0, a.length)
      .map(i -> a[i] * b[i])
      .reduce(0, (result, v) -> result + v);
  }

  private double dotProduct(double[] a, double[] b) {
    assert a.length == b.length;

    return IntStream.range(0, a.length)
      .mapToDouble(i -> a[i] * b[i])
      .reduce(0.0, (result, v) -> result + v);
  }
}
