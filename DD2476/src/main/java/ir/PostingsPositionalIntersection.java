package ir;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostingsPositionalIntersection implements Collector<PositionalPostingsEntry, List<PositionalPostingsEntry>, PostingsList> {
  private int frequency;
  private Map<Integer, List<PositionalPostingsEntry>> frequencyMap = new HashMap<>();

  PostingsPositionalIntersection(int frequency) {
    this.frequency = frequency;
  }

  @Override
  public Supplier<List<PositionalPostingsEntry>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<PositionalPostingsEntry>, PositionalPostingsEntry> accumulator() {
    return (result, positionalPostingsEntry) -> {
      List<PositionalPostingsEntry> entries = frequencyMap.compute(positionalPostingsEntry.getDocId(),
        (k, v) -> {
          List<PositionalPostingsEntry> list = Optional.ofNullable(v).orElse(new ArrayList<>());
          list.add(positionalPostingsEntry);
          return list;
        });

      if (entries.size() == frequency) {
        Map<Integer, Long> a = entries.stream()
          .flatMap(e -> {
            if (e.phrasePosition == 0) {
              return e.postingsEntry.positions.stream();
            }
            return e.postingsEntry.positions.stream().map(position -> position - e.phrasePosition);
          })
          .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (a.values().stream().anyMatch(v -> v == frequency)) {
          result.add(positionalPostingsEntry);
        }
      }
    };
  }

  @Override
  public BinaryOperator<List<PositionalPostingsEntry>> combiner() {
    return (left, right) -> Stream.of(left, right).flatMap(List::stream).collect(Collectors.toList());
  }

  @Override
  public Function<List<PositionalPostingsEntry>, PostingsList> finisher() {
    return result -> {
      PostingsList postingsList = new PostingsList();
      postingsList.addAll(result.stream().map(positionalPostingsEntry -> positionalPostingsEntry.postingsEntry).collect(Collectors.toList()));
      return postingsList;
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }
}
