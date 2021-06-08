/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.*;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;
    public List<Integer> positions = new ArrayList<>();

    public PostingsEntry(int docID) {
        this.docID = docID;
    }

    public PostingsEntry(int docID, double score) {
        this(docID);
        this.score = score;
    }

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in
     *  descending order.
     */
    @Override
    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, score );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostingsEntry that = (PostingsEntry) o;
        return docID == that.docID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(docID);
    }
}
