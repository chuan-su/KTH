/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public class SpellChecker {
    /** The regular inverted index to be used by the spell checker */
    Index index;

    /** K-gram index to be used by the spell checker */
    KGramIndex kgIndex;

    /** The auxiliary class for containing the value of your ranking function for a token */
    class KGramStat implements Comparable {
        double score;
        String token;

        KGramStat(String token, double score) {
            this.token = token;
            this.score = score;
        }

        public String getToken() {
            return token;
        }

        public int compareTo(Object other) {
            if (this.score == ((KGramStat)other).score) return 0;
            return this.score < ((KGramStat)other).score ? -1 : 1;
        }

        public String toString() {
            return token + ";" + score;
        }
    }

    /**
     * The threshold for Jaccard coefficient; a candidate spelling
     * correction should pass the threshold in order to be accepted
     */
    private static final double JACCARD_THRESHOLD = 0.4;


    /**
      * The threshold for edit distance for a candidate spelling
      * correction to be accepted.
      */
    private static final int MAX_EDIT_DISTANCE = 2;


    public SpellChecker(Index index, KGramIndex kgIndex) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Computes the Jaccard coefficient for two sets A and B, where the size of set A is 
     *  <code>szA</code>, the size of set B is <code>szB</code> and the intersection 
     *  of the two sets contains <code>intersection</code> elements.
     */
    private double jaccard(int szA, int szB, int intersection) {
        return intersection / ((szA + szB - intersection) * 1.0);
    }

    /**
     * Computing Levenshtein edit distance using dynamic programming.
     * Allowed operations are:
     *      => insert (cost 1)
     *      => delete (cost 1)
     *      => substitute (cost 2)
     */
    private int editDistance(String s1, String s2) {
        int[][] minCosts = new int[s1.length()][s2.length()];

        if (s1.charAt(s1.length() -1) != s2.charAt(s2.length() -1)) {
            minCosts[s1.length() - 1][s2.length() -1] = 1;
        } else {
            minCosts[s1.length() - 1][s2.length() -1] = 0;
        }

        for (int j = s2.length() - 2; j >= 0; j--) {
            minCosts[s1.length() - 1][j] = 1 + minCosts[s1.length() - 1][j + 1];
        }

        for (int i = s1.length() - 2; i >= 0; i--) {
            minCosts[i][s2.length() - 1] = 1 + minCosts[i + 1][s2.length() - 1];
        }

        for (int i = s1.length() - 2; i >= 0; i--) {
            for (int j = s2.length() - 2; j >= 0; j--) {
                int replace = minCosts[i + 1][j + 1];
                if (s1.charAt(i) != s2.charAt(j)) {
                    replace = replace + 2;
                }

                int delete = 1 + minCosts[i + 1][j];
                int insert = 1 + minCosts[i][j + 1];
                minCosts[i][j] = Stream.of(replace, delete, insert).min(Integer::compareTo).get();
            }
        }
        return minCosts[0][0];
    }

    /**
     *  Checks spelling of all terms in <code>query</code> and returns up to
     *  <code>limit</code> ranked suggestions for spelling correction.
     */
    public String[] check(Query query, int limit) {
        Set<String> qgrams = kgIndex.kGrams("$" +query.queryterm.get(0).term + "$");

        String[] candidates = qgrams
          .stream()
          .flatMap(gram -> kgIndex.getPostings(gram).stream().map(e -> kgIndex.id2term.get(e.tokenID)))
          .distinct()
          .filter(word -> {
              String nword = "$" + word + "$";

              int intersection = (int) qgrams.stream()
                .filter(nword::contains)
                .count();
              int kgramsCount = kgIndex.term2gramcounts.get(word);

              return jaccard(qgrams.size(), kgramsCount, intersection) >= JACCARD_THRESHOLD;
          })
          .filter(word -> editDistance(query.queryterm.get(0).term, word) <= MAX_EDIT_DISTANCE)
          .sorted((a, b) -> index.getPostings(b).size() - index.getPostings(a).size())
          .limit(limit)
          .toArray(String[]::new);

        return candidates;
    }

    /**
     *  Merging ranked candidate spelling corrections for all query terms available in
     *  <code>qCorrections</code> into one final merging of query phrases. Returns up
     *  to <code>limit</code> corrected phrases.
     */
    private List<KGramStat> mergeCorrections(List<List<KGramStat>> qCorrections, int limit) {
        //
        // YOUR CODE HERE
        //
        return null;
    }
}
