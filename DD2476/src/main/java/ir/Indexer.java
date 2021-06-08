/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import cosinesimilarity.TfIdf;

import java.io.*;
import java.util.*;
import java.nio.charset.*;
import java.util.stream.Collectors;


/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {

    /** The index to be built up by this Indexer. */
    Index index;

    /** K-gram index to be built up by this Indexer */
    KGramIndex kgIndex;

    /** The next docID to be generated. */
    private int lastDocID = 0;

    /** The patterns matching non-standard words (e-mail addresses, etc.) */
    String patterns_file;

    List<String> resourceTerms = new ArrayList<>();
    List<String> davisTerms = new ArrayList<>();
    /* ----------------------------------------------- */


    /** Constructor */
    public Indexer( Index index, KGramIndex kgIndex, String patterns_file ) {
        this.index = index;
        this.kgIndex = kgIndex;
        this.patterns_file = patterns_file;
    }


    /** Generates a new document identifier as an integer. */
    private int generateDocID() {
        return lastDocID++;
    }



    /**
     *  Tokenizes and indexes the file @code{f}. If <code>f</code> is a directory,
     *  all its files and subdirectories are recursively processed.
     */
    public void processFiles( File f, boolean is_indexing ) {
        // do not try to index fs that cannot be read
        if (is_indexing) {
            if ( f.canRead() ) {
                if ( f.isDirectory() ) {
                    String[] fs = f.list();
                    // an IO error could occur
                    if ( fs != null ) {
                        for ( int i=0; i<fs.length; i++ ) {
                            processFiles( new File( f, fs[i] ), is_indexing );
                        }
                    }
                } else {
                    // First register the document and get a docID
                    int docID = generateDocID();
                    if ( docID % 1000 == 0 ) System.err.println( "Indexed " + docID + " files" );
                    try {
                        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                        Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );
                        int offset = 0;
                        while ( tok.hasMoreTokens() ) {
                            String token = tok.nextToken();
                            insertIntoIndex( docID, token, offset++ );
                            if (f.getName().equals("Davis_Food_Coop.f")) {
                                davisTerms.add(token);
                            }
                            if (f.getName().equals("Resource_Recovery_Drive.f")) {
                                resourceTerms.add(token);
                            }
                        }
                        index.docNames.put( docID, f.getPath() );
                        index.docLengths.put( docID, offset );
                        reader.close();
                    } catch ( IOException e ) {
                        System.err.println( "Warning: IOException during indexing." );
                    }
                }
            }
        }
    }


    /* ----------------------------------------------- */


    /**
     *  Indexes one token.
     */
    public void insertIntoIndex( int docID, String token, int offset ) {
        index.insert( token, docID, offset );
        if (kgIndex != null)
            kgIndex.insert(token);
    }

    public Map<String, TfIdf> getTermsByDocId(int docID) {
        Map<String, TfIdf> result = new HashMap<>();

        final int N = index.docLengths.keySet().size();
        String f = index.docNames.get(docID);

        try {
            Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
            Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );
            while ( tok.hasMoreTokens() ) {
                String token = tok.nextToken();
                result.compute(token, (k, v) -> {
                    int df = index.getPostings(token).size();
                    TfIdf tfIdf = Optional.ofNullable(v).orElse(new TfIdf(0, df, N));
                    tfIdf.tf = tfIdf.tf + 1;
                    return tfIdf;
                });
            }
            reader.close();
        } catch ( IOException e ) {
            System.err.println( "Warning: IOException during indexing." );
        } finally {
            return result;
        }
    }
}

