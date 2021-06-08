/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class KGramIndex {

    /** Mapping from term ids to actual term strings */
    HashMap<Integer,String> id2term = new HashMap<Integer,String>();

    /** Mapping from term strings to term ids */
    HashMap<String,Integer> term2id = new HashMap<String,Integer>();

    /** Index from k-grams to list of term ids that contain the k-gram */
    HashMap<String,List<KGramPostingsEntry>> index = new HashMap<String,List<KGramPostingsEntry>>();
    HashMap<String, Integer> term2gramcounts = new HashMap<>();
    /** The ID of the last processed term */
    int lastTermID = -1;

    /** Number of symbols to form a K-gram */
    int K;

    public KGramIndex(int k) {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can't be constructed for a negative K value");
            System.exit(1);
        }
    }

    /** Generate the ID for an unknown term */
    private int generateTermID() {
        return ++lastTermID;
    }

    public int getK() {
        return K;
    }


    /**
     *  Get intersection of two postings lists
     */
    private List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
      if (Optional.ofNullable(p1).filter(p -> p.size() == 0).isPresent() || Optional.ofNullable(p2).filter(p -> p.size() == 0).isPresent()) {
        return new ArrayList<>();
      }

      Map<KGramPostingsEntry, Long> map = Stream.concat(p1.stream().distinct(), p2.stream().distinct())
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

      return map.entrySet().stream()
        .filter(entry -> entry.getValue() == 2)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    }

    public List<KGramPostingsEntry> getPostings(Set<String> kgrams) {
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != K) {
                throw new IllegalArgumentException("each gram length must be " + K);
            }

            if (postings == null) {
                postings = getPostings(kgram);
                if (postings == null) break;
            } else {
                postings = intersect(postings, getPostings(kgram));
            }
        }
        return postings;
    }


    /** Inserts all k-grams from a token into the index. */
    public void insert( String token ) {
        if (Optional.ofNullable(term2id.get(token)).isPresent()) {
            return;
        }

        int tokenId = generateTermID();
        term2id.put(token, tokenId);
        id2term.put(tokenId, token);

        String term = "$"+token+"$";
        Set<String> kgrams = kGrams(term);
        term2gramcounts.put(token, kgrams.size());

        for (String kgram : kgrams) {
            index.compute(kgram, (k, v) -> {
                List<KGramPostingsEntry> result = Optional.ofNullable(v).orElse(new ArrayList<>());
                result.add(new KGramPostingsEntry(tokenId));
                return result;
            });
        }
    }

    public Set<String> kGrams(String token) {
        Set<String> kgrams = new HashSet<>();

        for (int startIndex = 0, endIndex = startIndex + K; endIndex <= token.length(); startIndex ++, endIndex++) {
            String kgram = token.substring(startIndex, endIndex);
            kgrams.add(kgram);
        }
        return kgrams;
    }


    public List<String> getMatchedVocabularyTerms(String token) {
        Set<String> kgrams = kGrams(token);
        return getPostings(kgrams)
          .stream()
          .map(pe -> Optional.ofNullable(id2term.get(pe.tokenID)))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
    }

    /** Get postings for the given k-gram */
    public List<KGramPostingsEntry> getPostings(String kgram) {
        return index.get(kgram);
    }

    /** Get id of a term */
    public Integer getIDByTerm(String term) {
        return term2id.get(term);
    }

    /** Get a term by the given id */
    public String getTermByID(Integer id) {
        return id2term.get(id);
    }

    private static HashMap<String,String> decodeArgs( String[] args ) {
        HashMap<String,String> decodedArgs = new HashMap<String,String>();
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("patterns_file", args[i++]);
                }
            } else if ( "-f".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("file", args[i++]);
                }
            } else if ( "-k".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("k", args[i++]);
                }
            } else if ( "-kg".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("kgram", args[i++]);
                }
            } else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }
        return decodedArgs;
    }

    public static void main(String[] arguments) throws FileNotFoundException, IOException {
        HashMap<String,String> args = decodeArgs(arguments);

        int k = Integer.parseInt(args.getOrDefault("k", "3"));
        KGramIndex kgIndex = new KGramIndex(k);

        File f = new File(args.get("file"));
        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
        Tokenizer tok = new Tokenizer( reader, true, false, true, args.get("patterns_file") );
        while ( tok.hasMoreTokens() ) {
            String token = tok.nextToken();
            kgIndex.insert(token);
        }

        Set<String> kgrams = Pattern.compile(" ").splitAsStream(args.get("kgram")).collect(Collectors.toSet());
        List<KGramPostingsEntry> postings = kgIndex.getPostings(kgrams);
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 10) {
                System.err.println("The first 10 of them are:");
                resNum = 10;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println(kgIndex.getTermByID(postings.get(i).tokenID));
            }
        }
    }
}
