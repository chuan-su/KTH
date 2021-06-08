/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;

    Map<Integer, Double> rankScores;
    Map<String, Integer> docNumber;
    
    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search(Query query, QueryType queryType, RankingType rankingType ) {
        switch (queryType) {
            case INTERSECTION_QUERY:
                return intersect(query);
            case PHRASE_QUERY:
                return positionalIntersect(query);
            case RANKED_QUERY:
                if (rankScores == null) {
                    readRankScores();
                }
                return rankedQuery(query, rankingType);
            default:
                throw new IllegalArgumentException("error");
        }
    }

    public PostingsList intersect(Query query) {

        return query.queryterm.stream()
          .flatMap(queryTerm -> {
              int wildcardIndex = queryTerm.term.indexOf("*");
              if (wildcardIndex == -1) {
                  return Optional.ofNullable(index.getPostings(queryTerm.term)).orElse(new PostingsList()).stream();
              }
              Map<Integer, PostingsEntry> map = wildcardMatchTerms(queryTerm.term)
                .stream()
                .flatMap(term -> index.getPostings(term).stream())
                .collect(Collector.of(
                  HashMap::new,
                  (result, a) -> result.putIfAbsent(a.docID, a),
                  (m1, m2) -> {
                      m2.forEach(m1::putIfAbsent);
                      return m2;
                  }
                ));
              return map.values().stream();
          })
          .collect(new PostingsIntersection(query.queryterm.size()));

    }

    public PostingsList positionalIntersect(Query query) {
        return IntStream.range(0, query.queryterm.size())
          .boxed()
          .flatMap(phrasePosition -> {
              Query.QueryTerm queryTerm = query.queryterm.get(phrasePosition);
              int wildcardIndex = queryTerm.term.indexOf("*");
              if (wildcardIndex == -1) {
                  return index.getPostings(queryTerm.term)
                    .stream()
                    .map(postingsEntry -> new PositionalPostingsEntry(phrasePosition, postingsEntry));
              }

              Map<Integer, PostingsEntry> map = wildcardMatchTerms(queryTerm.term)
                .stream()
                .flatMap(term -> index.getPostings(term).stream())
                .collect(Collector.of(
                  HashMap::new,
                  (result, a) -> {
                      result.compute(a.docID, (k, v) -> {
                          PostingsEntry pe = Optional.ofNullable(v).orElse(new PostingsEntry(a.docID));
                          pe.positions.addAll(a.positions);
                          return pe;
                      });
                  },
                  (m1, m2) -> {
                      m2.forEach(m1::putIfAbsent);
                      return m2;
                  }
                ));
              return map.values()
                .stream()
                .map(postingsEntry -> new PositionalPostingsEntry(phrasePosition, postingsEntry));
          })
          .collect(new PostingsPositionalIntersection(query.queryterm.size()));
    }

    public PostingsList rankedQuery(Query query, RankingType rankingType) {
        Map<Integer, PostingsEntry> result = new HashMap<>();

        final int N = index.docLengths.keySet().size();
        query.queryterm.stream()
          .flatMap(queryTerm -> {
              int wildcardIndex = queryTerm.term.indexOf("*");
              if (wildcardIndex == -1) {
                  return Stream.of(queryTerm);
              }

              return wildcardMatchTerms(queryTerm.term)
                .stream()
                .map(term -> query.new QueryTerm(term, 1.0));
          })
          .forEach(queryTerm -> {

            PostingsList postingsList = index.getPostings(queryTerm.term);

            int df = postingsList.size();
            double idf = Math.log10(N / df);

            postingsList.stream().forEach(postingsEntry -> {
                int docId = postingsEntry.docID;

                int docLength = index.docLengths.get(docId);
                int termFrequency = postingsEntry.positions.size();

                double tf_idf = termFrequency * idf / docLength;
                double cosineSim = queryTerm.weight * tf_idf;

                String docName = index.docNames.get(docId).split("/")[1];
                int did = docNumber.getOrDefault(docName, 0);
                double pageRankScore = rankScores.getOrDefault(did,0.);

                PostingsEntry pe;

                switch (rankingType) {
                    case TF_IDF:
                        pe = new PostingsEntry(docId, cosineSim);
                        break;
                    case PAGERANK:
                        pe = new PostingsEntry(docId, pageRankScore);
                        break;
                    case COMBINATION:
                        pe = new PostingsEntry(docId, 100 * pageRankScore * 0.25 + 0.75 * cosineSim);
                        break;
                    default:
                        throw new IllegalArgumentException("error");
                }

                result.merge(docId, pe, (o1, o2) -> {
                    o1.score += o2.score;
                    return o1;
                });
            });
        });

        PostingsList postingsList = new PostingsList();
        postingsList.addAll(result.values().stream().sorted().collect(Collectors.toList()));

        return postingsList;
    }

    private Set<String> wildcardMatchTerms(String term) {

        String[] kgrams = term.split("\\*");

        if (kgrams.length == 1) { // * appears in the end of string
            String gram = kgrams[0];
            return kgIndex.getMatchedVocabularyTerms("$".concat(gram))
              .stream()
              .filter(t -> t.startsWith(gram))
              .collect(toSet());
        } else if (kgrams[0].isEmpty()) { // appears in the beginning of string
            String gram = kgrams[1];
            return kgIndex.getMatchedVocabularyTerms(gram.concat("$"))
              .stream()
              .filter(t -> t.endsWith(gram))
              .collect(toSet());

        } else { // in the middle of the string
            return Stream.of("$".concat(kgrams[0]), kgrams[1].concat("$"))
              .flatMap(gram -> kgIndex.getMatchedVocabularyTerms(gram).stream())
              .filter(t -> t.startsWith(kgrams[0]) && t.endsWith(kgrams[1]) && t.length() >= (kgrams[0].length() + kgrams[1].length()))
              .collect(toSet());
        }
    }

    private void readRankScores() {
        rankScores = new HashMap<>();

        File file = new File( "files/davis_score.txt" );

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(":");
                rankScores.put(Integer.parseInt(data[0].trim()), Double.parseDouble(data[1].trim()));
            }
            fileReader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        docNumber = new HashMap<>();
        File file2 = new File( "files/davisTitles.txt" );
        try {
            FileReader fileReader = new FileReader(file2);
            BufferedReader br = new BufferedReader(new FileReader(file2));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNumber.put(data[1], Integer.parseInt(data[0]));
            }
            fileReader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}