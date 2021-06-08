/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */

package ir;

import cosinesimilarity.*;

import java.util.*;
import java.io.File;

/**
 *  This is the main class for the search engine.
 */
public class Engine {

    /** The inverted index. */
    //Index index = new PersistentHashedIndex();
    public Index index = new HashedIndex();

    /** The indexer creating the search index. */
    public Indexer indexer;

    /** K-gram index */
    KGramIndex kgIndex = new KGramIndex(2);

    /** The searcher used to search the index. */
    public Searcher searcher;

    /** Spell checker */
    SpellChecker speller = new SpellChecker(index, kgIndex);

    /** The engine GUI. */
    SearchGUI gui;

    /** Directories that should be indexed. */
    ArrayList<String> dirNames = new ArrayList<String>();

    /** Lock to prevent simultaneous access to the index. */
    Object indexLock = new Object();

    /** The patterns matching non-standard words (e-mail addresses, etc.) */
    String patterns_file = null;

    /** The file containing the logo. */
    String pic_file = "";

    /** The file containing the pageranks. */
    String rank_file = "";

    /** For persistent indexes, we might not need to do any indexing. */
    boolean is_indexing = true;


    /* ----------------------------------------------- */


    /**  
     *   Constructor. 
     *   Indexes all chosen directories and files
     */
    public Engine( String[] args ) {
        decodeArgs( args );
        indexer = new Indexer( index, kgIndex, patterns_file );
        searcher = new Searcher( index, kgIndex );
        gui = new SearchGUI( this );
        gui.init();
        /* 
         *   Calls the indexer to index the chosen directory structure.
         *   Access to the index is synchronized since we don't want to 
         *   search at the same time we're indexing new files (this might 
         *   corrupt the index).
         */
        if (is_indexing) {
            synchronized ( indexLock ) {
                gui.displayInfoText( "Indexing, please wait..." );
                long startTime = System.currentTimeMillis();
                for ( int i=0; i<dirNames.size(); i++ ) {
                    File dokDir = new File( dirNames.get( i ));
                    indexer.processFiles( dokDir, is_indexing );
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                gui.displayInfoText( String.format( "Indexing done in %.1f seconds.", elapsedTime/1000.0 ));
                index.cleanup();
            }
        } else {
            gui.displayInfoText( "Index is loaded from disk" );
        }
//        final int N = index.docLengths.keySet().size();
//
//        Doc queryDoc = Doc.newDoc("food residence", Arrays.asList("food", "residence"), index, N);
//        Doc doc1 = Doc.newDoc("Davis_Food_Coop.f", indexer.davisTerms, index, N);
//        Doc doc2 = Doc.newDoc("Resource_Recovery_Drive.f", indexer.resourceTerms, index, N);
//
//        Doc specificQueryDoc = new Doc(queryDoc.name);
//        queryDoc.terms().forEach(term -> specificQueryDoc.add(term, new TfIdf(1,1)));
//
//        System.out.format("|%-5s|%-10s|%5s|\n", "Term", "Doc", "TF");
//        queryDoc.terms().forEach(term -> {
//            System.out.format("|%-5s|%-10s|%5d|\n", term, doc1.name, doc1.getTF(term));
//            System.out.format("|%-5s|%-10s|%5d|\n", term, doc2.name, doc2.getTF(term));
//        });
//
//        System.out.println("Table 2 Doc Length");
//        System.out.format("|%-20s|%-10s|%-10s|%-10s|\n", "", queryDoc.name, doc1.name, doc2.name);
//        System.out.format("|%-20s|%-4f|%-4f|%-4f|\n", "Euclidean, tf",       queryDoc.euclideanLength(Space.TF),     doc1.euclideanLength(Space.TF),     doc2.euclideanLength(Space.TF));
//        System.out.format("|%-20s|%-4f|%-4f|%-4f|\n", "Manhattan, tf",       queryDoc.manhattanLength(Space.TF),     doc1.manhattanLength(Space.TF),     doc2.manhattanLength(Space.TF));
//        System.out.format("|%-20s|%-4f|%-4f|%-4f|\n", "Euclidean, tf x idf", queryDoc.euclideanLength(Space.TF_IDF), doc1.euclideanLength(Space.TF_IDF), doc2.euclideanLength(Space.TF_IDF));
//        System.out.format("|%-20s|%-4f|%-4f|%-4f|\n", "Manhattan, tf x idf", queryDoc.manhattanLength(Space.TF_IDF), doc1.manhattanLength(Space.TF_IDF), doc2.manhattanLength(Space.TF_IDF));
//
//
//        System.out.println("Table 3 Cosine Similarity");
//        CosineSimilarity cs = new CosineSimilarity(queryDoc, doc1);
//        CosineSimilarity cs1 = new CosineSimilarity(queryDoc, doc2);
//        System.out.format("|%-20s|%-10s|%-10s|\n", "",                  doc1.name, doc2.name);
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Euclidean, tf",       cs.get(Space.TF, Norm.Euclidean),     cs1.get(Space.TF, Norm.Euclidean));
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Manhattan, tf",       cs.get(Space.TF, Norm.Manhattan),     cs1.get(Space.TF, Norm.Manhattan));
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Euclidean, tf x idf", cs.get(Space.TF_IDF, Norm.Euclidean), cs1.get(Space.TF_IDF, Norm.Euclidean));
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Manhattan, tf x idf", cs.get(Space.TF_IDF, Norm.Manhattan), cs1.get(Space.TF_IDF, Norm.Manhattan));
//
//
//
//
//        System.out.println("Table 4 Cosine Similarity");
//        CosineSimilarity cs2 = new CosineSimilarity(specificQueryDoc, doc1);
//        CosineSimilarity cs3 = new CosineSimilarity(specificQueryDoc, doc2);
//
//        System.out.format("|%-20s|%-10s|%-10s|\n", "",                    doc1.name, doc2.name);
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Euclidean, tf",       cs2.get(Space.TF, Norm.Euclidean),     cs3.get(Space.TF, Norm.Euclidean));
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Manhattan, tf",       cs2.get(Space.TF, Norm.Manhattan),     cs3.get(Space.TF, Norm.Manhattan));
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Euclidean, tf x idf", cs2.get(Space.TF_IDF, Norm.Euclidean), cs3.get(Space.TF_IDF, Norm.Euclidean));
//        System.out.format("|%-20s|%-4f|%-4f|\n", "Manhattan, tf x idf", cs2.get(Space.TF_IDF, Norm.Manhattan), cs3.get(Space.TF_IDF, Norm.Manhattan));
    }

    /* ----------------------------------------------- */

    /**
     *   Decodes the command line arguments.
     */
    private void decodeArgs( String[] args ) {
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-d".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    dirNames.add( args[i++] );
                }
            } else if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    patterns_file = args[i++];
                }
            } else if ( "-l".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    pic_file = args[i++];
                }
            } else if ( "-r".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    rank_file = args[i++];
                }
            } else if ( "-ni".equals( args[i] )) {
                i++;
                is_indexing = false;
            } else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }                   
    }


    /* ----------------------------------------------- */


    public static void main( String[] args ) {
        Engine e = new Engine( args );
    }

}

