/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.util.HashMap;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<>();


    /**
     *  Inserts this token in the hashtable.
     */
    @Override
    public void insert( String token, int docID, int offset ) {
        index.compute(token, (k, postingList) -> {
            if (postingList == null) {
                PostingsList result = new PostingsList();
                result.add(docID, offset);
                return result;
            } else {
                postingList.add(docID, offset);
                return postingList;
            }
        });
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    @Override
    public PostingsList getPostings( String token ) {
        return index.get(token);
    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    @Override
    public void cleanup() {
    }
}
