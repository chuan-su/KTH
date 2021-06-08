/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.*;
import java.util.stream.Stream;

public class PostingsList {

    /** The postings list */
    private List<PostingsEntry> list = new ArrayList<>();

    /** Number of postings in this list. */
    public int size() {
        return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
        return list.get( i );
    }

    public void add(int docId, int position) {
        Optional<PostingsEntry> existingEntry = Optional.empty();

        int lastIdx = size() - 1;

        if (lastIdx >= 0) {
            existingEntry = Optional.of(list.get(lastIdx)).filter(e -> e.docID == docId);
        }

        if (existingEntry.isPresent()) {
            existingEntry.get().positions.add(position);
        } else {
            PostingsEntry newEntry = new PostingsEntry(docId);
            newEntry.positions.add(position);
            list.add(newEntry);
        }
    }


    public void add(PostingsEntry postingsEntry) {
        list.add(postingsEntry);
    }

    public void addAll(List<PostingsEntry> postingsEntries) {
        list.addAll(postingsEntries);
    }

    public Stream<PostingsEntry> stream() {
        return list.stream();
    }
}
