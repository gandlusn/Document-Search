// Tf-idf ranking of documents for queries
import java.io.*;
import java.util.*;

public class IR3B
{
 static final int numberOfPrecomputedLogTfs = 100;
 static final int tops = 5;
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null;  // read in
 String[] titles = null; // read in
 int[] postingsLists = null; // read in
 int[] postings = null; // read in
 int[] tfs = null;  // read in
 double[] precomputedLogTfs = new double[numberOfPrecomputedLogTfs];
 double[] idfSquares = null; // precomputed
 double[] docLengths = null; // precomputed
 double[] scores = null;

 void readInvertedIndex(String filename){
    Scanner in = null;  
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    String[] tokens = in.nextLine().split(" ");
    numberOfTerms = Integer.parseInt(tokens[0]);// take it from the IR3AN programme
    numberOfDocs = Integer.parseInt(tokens[1]);// take it from the IR3AN programme
    numberOfIncidences = Integer.parseInt(tokens[2]);// take it from the IR3AN programme
    dictionary = new String[numberOfTerms];
    idfSquares = new double[numberOfTerms];
    postingsLists = new int[numberOfTerms + 1];
    postings = new int[numberOfIncidences];
    tfs = new int[numberOfIncidences];
    double logN = Math.log10((double)numberOfDocs);
    int n = 0;
    for (int i = 0; i < numberOfTerms; i++)
    {
       postingsLists[i] = n;// setting the first point for the docs which contain the particular term in the dictionary
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];// the term
       int df = tokens.length / 2;// because half the document is term frequency in document
       double idf = logN - Math.log10((double)df);// calculating idf
       idfSquares[i] = idf * idf;
       for (int j = 0; j < df; j++)
       {
         postings[n] = Integer.parseInt(tokens[2 * j + 1]);// in document it self we have [term docid frequency docid frequency .... docid frequency]
         tfs[n] = Integer.parseInt(tokens[2 * j + 2]);
         n++;
       }
    }
    postingsLists[numberOfTerms] = n;
    in.close();
 }

 void readTitles(String filename){
    Scanner in = null;  
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    titles = new String[numberOfDocs];
    for (int i = 0; i < numberOfDocs; i++){
      titles[i] = in.nextLine();
      in.nextLine(); in.nextLine();
    }
    in.close();
  }

void precompute()
{
    for (int i = 1; i < numberOfPrecomputedLogTfs; i++)
    {
         precomputedLogTfs[i] = 1.0 + Math.log10((double)i);// we are precomputing the frequency till 100 times in a particula document for a term
    }
    docLengths = new double[numberOfDocs];
    for (int i = 0; i < numberOfDocs; i++) docLengths[i] = 0;
    for (int termID = 0; termID < numberOfTerms; termID++)
       for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++)
    {
          double w = tfs[k] < numberOfPrecomputedLogTfs ?
          precomputedLogTfs[tfs[k]] : 1.0 + Math.log10((double)(tfs[k]));
          docLengths[postings[k]] += w * w * idfSquares[termID];// the aggregation of all the tf*idf of all terms is equal to the doclenght
    }
    for (int i = 0; i < numberOfDocs; i++) 
       docLengths[i] = Math.sqrt(docLengths[i]);// we are doing sqares to remove -ve
 }

// binary search
 int find(String key, String[] array){
   int lo = 0; int hi = array.length - 1;
   while (lo <= hi){
     int mid = (lo + hi) / 2;
     int diff = key.compareTo(array[mid]);
     if (diff == 0) return mid;
     if (diff < 0) hi = mid - 1; else lo = mid + 1;
   }
   return -1;
 }


 void answerQueries(){  // precondition: readInvertedIndex() done
   scores = new double[numberOfDocs];
   Scanner in = new Scanner(System.in);
   System.out.println("Enter a query (a number of words).");
   while (in.hasNextLine()){
     String query = in.nextLine();
     if (query.length() == 0) break;
     cosineScore(query);
     retrieveByRanking();
     System.out.println("Enter a query or empty line for end.");
   }
 }

 void cosineScore(String query){  //ltc.ntc
 // precondition: readInvertIndex() done, uses binary search function find()
   for (int i = 0; i < numberOfDocs; i++) scores[i] = 0;// for every document we will check every term in query ad compare them and retrive the value
   String[] terms = query.split(" ");
   int len = terms.length;
   double queryLength = 0;
   for (int j = 0; j < len; j++){
     int termID = find(terms[j], dictionary);// getting term id
     if (termID >= 0){  
       queryLength += idfSquares[termID];// idf is the no of documents a term has occured. same everytime document leanght is the aggregation of all term in document (tfs)*(idfs) 
       for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++)
         if (tfs[k] < numberOfPrecomputedLogTfs)
            scores[postings[k]] += 
              precomputedLogTfs[tfs[k]] * idfSquares[termID];// so here we are aggregating all the tfs[termids found in document and query]*idfs[termids found in document and query]
         else scores[postings[k]] += 
               (1.0 + Math.log10((double)(tfs[k]))) * idfSquares[termID];
     }
   }
   queryLength = Math.sqrt(queryLength);
   for (int i = 0; i < numberOfDocs; i++) 
      scores[i] /= (queryLength * docLengths[i]);//doc lenght is sum of tf*idf values of all the terms in the doc 
 }

 void retrieveByRanking(){  // precondition: cosineScore(query) done
     int[] results = new int[tops + 1];
     double[] topScores = new double[tops + 1];
     int numberOfResults = 0;
     for (int i = 0; i < numberOfDocs; i++) if (scores[i] > 0){
      int k = numberOfResults - 1; for (; k >= 0; k--)
         if (scores[i] > topScores[k]){  // insertion sort
            results[k + 1] = results[k];
            topScores[k + 1] = topScores[k];
         }else break;
      if (k < tops - 1){ results[k + 1] = i; topScores[k + 1] = scores[i]; }
      if (numberOfResults < tops) numberOfResults++;
     }
     for (int i = 0; i < numberOfResults; i++)
      System.out.println(titles[results[i]] + " " + topScores[i]);
 }

 public static void main(String[] args){
   IR3B ir3 = new IR3B();
   ir3.readInvertedIndex(args[0]);
   ir3.readTitles(args[1]);
   ir3.precompute();
   ir3.answerQueries();
 }
}