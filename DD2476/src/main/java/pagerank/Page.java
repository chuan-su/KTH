package pagerank;

public class Page {

  public int docId;
  public double score;

  public Page(int docId, double score) {
    this.docId = docId;
    this.score = score;
  }
}
