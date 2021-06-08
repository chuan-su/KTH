/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The dictionary file name */
    public static final String DATA_FNAME = "data";

    /** The dictionary entry length in bytes */
    public static final int DICTIONARY_ENTRY_LENGTH = 16;

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    Function<String, Integer> firstHashFunc = (term) -> Math.abs(2 * (term.hashCode()) + 5);
    Function<String, Integer> secondHashFunc = (term) -> Math.abs(3 * (term.hashCode()) + 7);
    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */ 
    public class Entry {
        long dataOffset;
        int  dataLength;
        int checksum;

        Entry(long dataOffset, int dataLength, int checksum) {
            this.dataOffset = dataOffset;
            this.dataLength = dataLength;
            this.checksum = checksum;
        }

        byte[] toBytes() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            byteBuffer.putLong(0, dataOffset);
            byteBuffer.putInt(8, dataLength);
            byteBuffer.putInt(12, checksum);

            return byteBuffer.array();
        }
    }
    // ==================================================================

    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created. 
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
        } catch ( FileNotFoundException e ) {
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek( ptr ); 
            byte[] data = dataString.getBytes();
            dataFile.write(data);
            return data.length;
        } catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file. 
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
        byte[] bytes = entry.toBytes();
        try {
            dictionaryFile.seek(ptr);
            dictionaryFile.write(bytes);
        }catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr ) {
        try {
            dictionaryFile.seek( ptr );
            byte[] data = new byte[DICTIONARY_ENTRY_LENGTH];
            dictionaryFile.readFully( data );

            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            int checksum = byteBuffer.getInt(12);
            if (checksum == 0) {
                return null;
            }
            long offset = byteBuffer.getLong(0);
            int length = byteBuffer.getInt(8);
            return new Entry(offset, length, checksum);

        } catch ( IOException e ) {
            //e.printStackTrace();
            return null;
        }
    }

    Entry readEntry(String token) {

        int hashValue = firstHashFunc.apply(token);
        int checksum = secondHashFunc.apply(token);

        Entry e;

        do {
            long ptr = (hashValue % TABLESIZE) * DICTIONARY_ENTRY_LENGTH;
            e = readEntry(ptr);
            hashValue ++;
        } while (e != null && e.checksum != checksum);

        return e;
    }

    int writeEntry(String token, Entry entry) {
        int collisions = -1;
        int hashValue = firstHashFunc.apply(token);

        Entry e;
        long ptr;

        do {
            ptr = (hashValue % TABLESIZE) * DICTIONARY_ENTRY_LENGTH;
            e = readEntry(ptr);

            hashValue ++;
            collisions ++;
        } while (e != null);

        writeEntry(entry, ptr);
        return collisions;
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for (Map.Entry<Integer,String> entry : docNames.entrySet()) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write(docInfoEntry.getBytes());
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put(new Integer(data[0]), data[1]);
                docLengths.put(new Integer(data[0]), new Integer(data[2]));
            }
        }
        freader.close();
    }


    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings list
            int dataPtr = 0;

            for (Map.Entry<String, PostingsList> entry: index.entrySet()) {
                // postingsList string representation
                String postingsListStr = entry.getValue().stream().map(this::encodePostingsEntry).collect(joining(";"));
                // write postingsList to data file.
                int dataLength = writeData(postingsListStr, dataPtr);

                // compute token checksum
                int checksum = secondHashFunc.apply(entry.getKey());
                // write to dictionary file
                int writeCollisions = writeEntry(entry.getKey(), new Entry(dataPtr, dataLength, checksum));

                // increase data file pointer to point to next available space
                dataPtr = dataPtr + dataLength;

                collisions = collisions + writeCollisions;
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }
    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    @Override
    public PostingsList getPostings( String token ) {
        Entry e = readEntry(token);

        if (e == null) {
            return null;
        }
        // read postingsList string from data file
        String postingListStr = readData(e.dataOffset, e.dataLength);

        // unmarshalling string to postingsList
        List<PostingsEntry> entries = Pattern.compile(";").splitAsStream(postingListStr)
          .map(this::decodePostingsEntry)
          .collect(toList());

        PostingsList postingsList = new PostingsList();
        postingsList.addAll(entries);
        return postingsList;
    }


    /**
     *  Inserts this token in the main-memory hashtable.
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
     *  Write index to file after indexing is done.
     */
    @Override
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        writeIndex();
        System.err.println( "done!" );
    }

    private String encodePostingsEntry(PostingsEntry postingsEntry) {
        return postingsEntry.docID + "," + postingsEntry.positions.size() + "," + postingsEntry.positions.stream().map(String::valueOf).collect(joining(","));
    }

    private PostingsEntry decodePostingsEntry(String str) {
        List<Integer> a = Pattern.compile(",").splitAsStream(str).map(Integer::parseInt).collect(Collectors.toList());

        PostingsEntry postingsEntry = new PostingsEntry(a.get(0));
        postingsEntry.positions.addAll(a.subList(2, a.size()));

        return postingsEntry;
    }
}
