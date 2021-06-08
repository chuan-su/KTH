package ir;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostingsIntersection implements Collector<PostingsEntry, List<PostingsEntry>, PostingsList> {
  private int frequency;
  private Map<Integer, Integer> frequencyMap = new HashMap<>();

  PostingsIntersection(int frequency) {
    this.frequency = frequency;
  }

  @Override
  public Supplier<List<PostingsEntry>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<PostingsEntry>, PostingsEntry> accumulator() {
    return (result, postingEntry) -> {
      int f = frequencyMap.merge(postingEntry.docID, 1, (oldV, newV) -> oldV + newV);
      if (f >= frequency) {
        result.add(postingEntry);
      }
    };
  }

  @Override
  public BinaryOperator<List<PostingsEntry>> combiner() {
    return (left, right) -> Stream.of(left, right).flatMap(List::stream).collect(Collectors.toList());
  }

  @Override
  public Function<List<PostingsEntry>, PostingsList> finisher() {
    return result -> {
      PostingsList postingsList = new PostingsList();
      postingsList.addAll(result);
      return postingsList;
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }
}
