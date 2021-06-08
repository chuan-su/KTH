package ir;

import java.util.ArrayList;
import java.util.List;

public class PositionalPostingsEntry {
  final int phrasePosition;
  final PostingsEntry postingsEntry;
  List<Integer> positions;

  public PositionalPostingsEntry(int phrasePosition, PostingsEntry postingsEntry) {
    this.phrasePosition = phrasePosition;
    this.postingsEntry = postingsEntry;
    this.positions = new ArrayList<>(postingsEntry.positions);
  }

  public int getDocId() {
    return postingsEntry.docID;
  }
}
