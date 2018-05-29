// TrainingMultinomialNA and TrainingBernoulliNA combined
// reads the imdb bag-of-words file train/labeledBow.feat
// outputs Ncs for the two classes in the first row and then
// outputs Ncts and Tcts for the two classes (pos and neg) one row for each term.

import java.io.*;
import java.util.*;

public class IR5A{

  static final int numberOfTerms = 89527;
  static final int numberOfDocs = 25000;
  static final int numberOfClasses = 2;
  int[] Nc = new int[numberOfClasses];
  int[][] Nct = new int[numberOfTerms][numberOfClasses];
  int[][] Tct = new int[numberOfTerms][numberOfClasses];

  void train(String filename){
	Scanner in = null;
	try {
		in = new Scanner(new File(filename));
	}catch (FileNotFoundException e){
		System.err.println(filename + " not found");
		System.exit(1);
	}
	for (int j = 0; j < numberOfClasses; j++) Nc[j] = 0;
	for (int i = 0; i < numberOfTerms; i++)
		for (int j = 0; j < numberOfClasses; j++) Tct[i][j] = Nct[i][j] = 0;                                           
	for (int i = 0; i < numberOfDocs; i++){
		String line = in.nextLine();  
		String[] terms = line.split("[ :]");
		int c = line.charAt(0) > '5' ? 0 : 1;
		Nc[c]++; 
		int len = terms.length / 2;
		for (int j = 0; j < len; j++){
			int termID = Integer.parseInt(terms[j * 2 + 1]); termid, noof repetetions termid, noof repetetions termid, noof repetetions
			Nct[termID][c]++;// no of documents a word is present in a subset of a class
			Tct[termID][c] += Integer.parseInt(terms[j * 2 + 2]);// no of times it is present in that class
		}
	}
	in.close();
	System.out.println(Nc[0] + " " + Nc[1]);//here we display how many documents are there in both the classes 
	for (int i = 0; i < numberOfTerms; i++) System.out.println(
		Nct[i][0] + " " + Nct[i][1] + " " + Tct[i][0] + " " + Tct[i][1]);
  }

 public static void main(String[] args){
	if (args.length < 1){
		System.err.println("Usage: java IR5A labeledBow.feat");
		return;
	}
	IR5A ir5 = new IR5A();
	ir5.train(args[0]);
 }
}
