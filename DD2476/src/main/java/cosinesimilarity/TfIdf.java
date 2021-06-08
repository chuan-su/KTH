package cosinesimilarity;

import java.math.BigDecimal;

public class TfIdf {
  public int tf;
  public double idf;

  public TfIdf(int tf, int idf) {
    this.tf = tf;
    this.idf = idf;
  }

  public TfIdf(int tf, int df, int n) {
    double idf = Math.log(n * 1.0 / df);
    double round_idf = BigDecimal.valueOf(idf).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();

    this.tf = tf;
    this.idf = round_idf;
  }

  public double value() {
    return tf * idf;
  }
}
