// ApplyMultinomialNA (also the binarized version) and ApplyBernoulliNA combined
// reads the output from IR5A, NctTcts.txt
// reads the test file labeledBowTest.feat (or labeledBow.feat as training set self-testing)
// outputs a 2x2 contingnecy table for the four counts tp, fp, fn, and tn.
// Usage: java IR5B NctTcts.txt labeledBowTest.feat

import java.io.*;
import java.util.*;

public class IR5B{

  static final int numberOfTerms = 89527;
  static final int numberOfDocs = 25000;
  static final int numberOfClasses = 2;
  int[] Nc = new int[numberOfClasses];
  int[][] Nct = new int[numberOfTerms][numberOfClasses];// this retreived from the previous programme
  int[] sumOfNcts = new int[numberOfClasses];// this retreived from the previous programme
  int[][] Tct = new int[numberOfTerms][numberOfClasses];// this retreived from the previous programme
  int[] sumOfTcts = new int[numberOfClasses];// this retreived from the previous programme


  void readModel(String filename)
  {
	Scanner in = null;
	try {
		in = new Scanner(new File(filename));
	}catch (FileNotFoundException e){
		System.err.println(filename + " not found");
		System.exit(1);
	}
	String[] terms = in.nextLine().split(" ");
	for (int j = 0; j < numberOfClasses; j++) sumOfNcts[j] = 0;
	for (int j = 0; j < numberOfClasses; j++) Nc[j] = Integer.parseInt(terms[j]);
	for (int i = 0; i < numberOfTerms; i++){
		terms = in.nextLine().split(" ");
		for (int j = 0; j < numberOfClasses; j++){ 
			Nct[i][j] = Integer.parseInt(terms[j]); // here they are arranged in such a way that (class's no of documents a term occurs in, class's no of documents with their sum of frequencies of all documets belong to that class)
			sumOfNcts[j] += Nct[i][j];// adding all the nct's that belong to the same class 
			Tct[i][j] = Integer.parseInt(terms[numberOfClasses + j]);// so aw we mentioned earlier( class's no of docs and sum of frequencies of each term are side by side  )
			sumOfTcts[j] += Tct[i][j];// sum of all tct's
		}
	}
             /*    MULTINOMIAL NAIVE BAYES
                   prior[c] ? Nc/N 6 textc ? CONCATENATETEXTOFALLDOCSINCLASS(D,c) 7
                    for each t ? V 8 do Tct ? COUNTTOKENSOFTERM(textc,t) 
                     9 for each t ? V do condprob[t][c] ? Tct+1 /?t'(Tct'+1)// for tct's of all the terms in all in documents of a class are summed and divided by one particular terms TCT and that is the conditional probababilty of that term in that class
             */
              /*
                for each t = V  do 
                Nct ? COUNTDOCSINCLASSCONTAININGTERM(D,c,t) 
                condprob[t][c] ? (Nct +1)/(Nc +2) // nc is total no of documents containing in a particu,ar class 
              */
	in.close();
  }

  void apply(String filename){
	int[][] MNB = new int[numberOfClasses][2]; // contingency table for MNB
	int[][] MNBB = new int[numberOfClasses][2]; // binarized multinomial naive Bayes = ir is the ix of mutinomila with replacing tct bu nct
	int[][] BNB = new int[numberOfClasses][2]; // Bernoulli NB
	Scanner in = null;
	try {
		in = new Scanner(new File(filename));
	}catch (FileNotFoundException e){
		System.err.println(filename + " not found");
		System.exit(1);
	}
	for (int i = 0; i < numberOfClasses; i++) for (int j = 0; j < 2; j++)
		MNB[i][j] = MNBB[i][j] = BNB[i][j] = 0;
	double[] priors = new double[numberOfClasses];
	double[] MNBscores = new double[numberOfClasses];
	double[] MNBBscores = new double[numberOfClasses];
	 double[] BNBscores = new double[numberOfClasses];
	double[] MNBdenominators = new double[numberOfClasses];
	double[] MNBBdenominators = new double[numberOfClasses];
	double[] BNBdenominators = new double[numberOfClasses];
	for (int i = 0; i < numberOfClasses; i++){//                here we are adding no of terms  to every class denominators,instead of adding no of terms in the documnets of class c , becuae the the rest of their value is already zero in that particular class so it does not matter
		priors[i] = Math.log((double)(Nc[i]));
		MNBdenominators[i] = Math.log((double)(sumOfTcts[i] + numberOfTerms));// it is denominator =  it is sum all the tct's of terms of a in a purticular  class and +1 for every term in the documne t so there is that no of terms  beside + this denotes 1 is added no of terms times
		MNBBdenominators[i] = Math.log((double)(sumOfNcts[i] + numberOfTerms));// so here we are swapping tct's by nct it is the only difference
		BNBdenominators[i] = -Math.log((double)(Nc[i] + 2)) * numberOfTerms;// for every term denominator is nc +2 for that particular class so it is multipled, no of terms times 
		for (int j = 0; j < numberOfTerms; j++) 
			BNBdenominators[i] += Math.log((double)(Nc[i] - Nct[j][i] + 1));// here we are adding all the numerators because we are applying log, you may confuse that denominator should be substracted but it is already given - sign in the above formula
	}
	for (int i = 0; i < numberOfDocs; i++)
        {
		String line = in.nextLine();  
		String[] terms = line.split("[ :]");
		int c = line.charAt(0) > '5' ? 0 : 1;
		int len = terms.length / 2;
		for (int j = 0; j < numberOfClasses; j++) {
			MNBscores[j] = priors[j];// here we are just assinging the prior probability to the MNB Scores
			MNBBscores[j] = priors[j] - (MNBBdenominators[j] * len);// here we are multiplying denominator by the no of temrs as we did in the BNBDENOMINATOR
			BNBscores[j] = priors[j] + (BNBdenominators[j]- Math.log((double)(Nc[j] + 2)) *len); // here we are multiplting len because denominator is same for all the terms in the doc
			//	- Math.log((double)(Nc[j] + 2)) * len;? we are multiplying nc+2 no of terms times in a doc because for everyterm we have already thought that it isnot present in the doc and mulipled nc-nct+1/nc+2 so to compensate it we have to  multiply nc+2 again for no of ters in doc

		}
		for (int j = 0; j < len; j++){
			int termID = Integer.parseInt(terms[j * 2 + 1]);
			int tf = Integer.parseInt(terms[j * 2 + 2]);
			for (int k = 0; k < numberOfClasses; k++){
				MNBscores[k] += (Math.log(Tct[termID][k] + 1.0) 
					- MNBdenominators[k]) * tf;// so here we ar completing the formula of the MNB by adding log of prior probability to the tct of a purticular term minus th denominator which we calculated additionly we are multiplying the no of term frequency
				MNBBscores[k] += Math.log(Nct[termID][k] + 1.0);// so here we are adding (Nct +1) term
				BNBscores[k] +=  Math.log(Nct[termID][k] + 1.0);// so as we converted the TCT TO NCT ibn MNB and called it MNBB 
			}
		}
		if (MNBscores[c] >= MNBscores[1 - c]) MNB[c][c]++; else MNB[1-c][c]++;
		if (MNBBscores[c] >= MNBBscores[1 - c]) MNBB[c][c]++; else MNBB[1-c][c]++;
		if (BNBscores[c] >= BNBscores[1 - c]) BNB[c][c]++; else BNB[1-c][c]++;
	}
	in.close();
	System.out.println("MNB");
	System.out.println(MNB[0][0] + "\t" + MNB[0][1]);
	System.out.println(MNB[1][0] + "\t" + MNB[1][1]);
	System.out.println("\r\nMNBB");
	System.out.println(MNBB[0][0] + "\t" + MNBB[0][1]);
	System.out.println(MNBB[1][0] + "\t" + MNBB[1][1]);
	System.out.println("\r\nBNB");
	System.out.println(BNB[0][0] + "\t" + BNB[0][1]);
	System.out.println(BNB[1][0] + "\t" + BNB[1][1]);
        System.out.println("Accuracy of  the MNB")
        double nume = MNB[0][0]+MNB[1][1];
        double denome = MNB[0][0]+MNB[1][0]+MNB[0][1]+MNB[1][1];
        double accuracy = nume/denome;
        Sytem.out.println(accuracy);
        System.out.println("Accuracy of  the BNB")
        double nume1 = BNB[0][0]+BNB[1][1];
        double denome1 = BNB[0][0]+BNB[1][0]+BNB[0][1]+BNB[1][1];
        double accuracy1 = nume1/denome1;
        Sytem.out.println(accuracy1);
        System.out.println("Accuracy of  the MNBB")
        double nume2 = MNBB[0][0]+MNBB[1][1];
        double denome2 = MNBB[0][0]+MNBB[1][0]+MNBB[0][1]+MNBB[1][1];
        double accuracy2 = nume1/denome2;
        Sytem.out.println(accuracy2);
  }

 public static void main(String[] args){
	if (args.length < 2){
		System.err.println("Usage: java IR5B NctTcts.txt labeledBowTest.feat");
		return;
	}
	IR5B ir5 = new IR5B();
	ir5.readModel(args[0]);
	ir5.apply(args[1]);
 }
}
                  