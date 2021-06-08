/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import cosinesimilarity.TfIdf;

import java.util.*;
import java.nio.charset.*;
import java.io.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm {
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.2;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
        }    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryterm ) {
            len += t.weight; 
        }
        return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryterm ) {
            queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
        }
        return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
        final int N = engine.index.docLengths.keySet().size();

        int[] relevantDocIndexes = IntStream.range(0, docIsRelevant.length)
          .filter(j -> docIsRelevant[j])
          .toArray();

        if (relevantDocIndexes.length == 0) {
            return;
        }

        for (QueryTerm term : queryterm) {
            int df = engine.index.getPostings(term.term).size();
            double idf = Math.log10(N / df);
            if (term.weight == 1) {
                term.weight = idf / length();
            }
            term.weight *= alpha;
        }

        Arrays.stream(relevantDocIndexes)
          .boxed()
          .map(results::get)
          .map(pe -> pe.docID)
          .forEach(docId -> {
              int docLength = engine.index.docLengths.get(docId);
              Map<String, TfIdf> map = engine.indexer.getTermsByDocId(docId);
              map.forEach((token, tfidf) -> {
                  Optional<QueryTerm> qt = queryterm.stream().filter(term -> Objects.equals(term, token)).findFirst();
                  double w = (beta / relevantDocIndexes.length * tfidf.value() / docLength);
                  if (qt.isPresent()) {
                      qt.get().weight += w;
                  } else {
                      queryterm.add(new QueryTerm(token, w));
                  }
              });
          });
    }
}


