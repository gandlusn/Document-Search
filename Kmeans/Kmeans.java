import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class IR8A 
{
	int N = 1;  // number of data points
	int M = 0;  // number of features
	int numberOfPopulations = 0;
	static final int K = 8;  // number of clusters
	ArrayList<HashSet<Integer>> data = new ArrayList<HashSet<Integer>>();
	ArrayList<String> sampleIDs = new ArrayList<String>();
	String[] populationNames = null;
	ArrayList<HashMap<Integer, Double>> centroids 
		= new ArrayList<HashMap<Integer, Double>>();
	int[] membership= null;
	int[] population = null;
	int[] populationSize = null;
	int[] clusterSize = new int[K];
	int[][] confusion = null;

	class d
	{
		double class2;
		double cluster1;
	};
 void readData()
 {
	Scanner in = null;
	try
	{
		in = new Scanner(new File("sample2population.txt"));// tis data contains populatopn names onaly alon wit some number 
	} 
	catch (FileNotFoundException e)
	{
		System.err.println("not found");
		return;
	}
	TreeSet<String> tset = new TreeSet<String>();
	while (in.hasNextLine())
	{	
		String line = in.nextLine();
		sampleIDs.add(line);// ere we add wole line to te sample IDS
		tset.add(line.substring(8));//  we only add population names uniquely
		data.add(new HashSet<Integer>());//  ere we add tose many data to use after wards  
	}
	in.close();
	numberOfPopulations = tset.size();
	populationNames = new String[numberOfPopulations];
	populationSize = new int[numberOfPopulations];
	for (int i = 0; i < numberOfPopulations; i++) populationSize[i] = 0;
	int n = 0; for (String s: tset) populationNames[n++] = s;// ere we enter all te population names
	N = sampleIDs.size();
	membership = new int[N];
	for (int i = 0; i < N; i++) membership[i] = -1;
	population = new int[N];
	for (int i = 0; i < N; i++)
	{ 
		String pop = sampleIDs.get(i).substring(8);
		int k = 0; for (; k < numberOfPopulations; k++)
			if (populationNames[k].equals(pop)) break;// ere we find te index of tat purticular population name
		
		population[i] = k; populationSize[k]++;// ere we are addin population names to te population multiplr times and countin it and storin it in anoter 
	}
	try { in = new Scanner(new File("Ygenotypes.txt"));
	} catch (FileNotFoundException e){
		System.err.println("not found");
		return;
	}
	String line = null;
	do {
		line = in.nextLine();
	} while (line.indexOf("#CHROM") < 0);// we do tis because teir is some unwanted text in te beinin of file
	String[] terms = line.split("\t");  // checking sampleIDs
	for (int i = 0; i < N; i++) if (sampleIDs.get(i).indexOf(terms[i + 9]) != 0)
	{
		System.err.println("wrong sample ID " + terms[i + 9]); System.exit(1); // dont know wy we are doin tis
	}
	while (in.hasNextLine())
	{
		line = in.nextLine(); 
		terms = line.split("\t");
		for (int j = 0; j < N; j++)
			if (terms[j + 9].charAt(0) != '0')
				data.get(j).add(M); // ere we are addin M values wic are line values wic ave Sample IDS wit 1  
		// tis means in every line teir is a 0 or 1 for every one in te population so we ave to et tat j from data we added above wit asset inteers and add tat line number wic as 1 at its respective index   
		M++;
	}
	in.close();
  }

  void selectSeeds()
  {
	Random random = new Random();
	HashSet<Integer> seeds = new HashSet<Integer>();
	int seed = -1;
	for (int k = 0; k < K; k++) 
	{
		do {
			seed = random.nextInt(N);
		}while (seeds.contains(seed));
		seeds.add(seed);// ere we are randomly addin a new seed wic is not present ere in tis seeds before  
		HashMap<Integer, Double> centroid = new HashMap<Integer, Double>();
		System.out.println(seed);
		for (int j: data.get(seed)) centroid.put(j, 1.0);// takin tat seed data  = wic is wic sample iD and all te ilnes wic it ot 1. we are foin tis in line 92 
		centroids.add(centroid);
	}
  }

  double distance(HashSet<Integer> dataPoint, HashMap<Integer, Double> centroid){
	double sum = 0;
	Set<Integer> centroidFeatures= centroid.keySet(); // et all te line numbers wic ave 1 fopr tat particular sample id 
	for (int j: dataPoint) if (centroidFeatures.contains(j))// ceks if sample id we took as 1 in te same line as cetroid sample id as 1
	{
		double d = 1.0 - centroid.get(j);// after wards it will decrease because we ave decreased its value by divin it by cluster size 
		// it is literally 0 for te first time because cetroid.et(j) is 1 if you can see line 111  
		sum += d * d; 
	}
	else sum += 1.0; // if not add 1
	for (int j: centroidFeatures) if (!dataPoint.contains(j))// we are doin tis because in forst loop we calculated ow many are similar ow many are different, for tat we pass trou te wole centroid and ceck for a line number wic is not present in datapoint  
	{// we od tis because we exactly calculate ow many extra lines does tis particular centroid ave 1 
		double d = centroid.get(j);
		sum += d * d;
	}
	return sum;
  }

  int applyRocchio()
  {
	int updates = 0;
	double RSS = 0;
	for (int i = 0; i < N; i++)
	{
		double minD = (double)M; // ere M i ste no of line we went trou in te top read data second part 
		int nearest = -1;
		for (int k = 0; k < K; k++)
		{
			double d = distance(data.get(i), centroids.get(k));
			if (d < minD){ minD = d; nearest = k; }// ere we find te minimu k and minimum distance
	    }
		RSS += minD;// as we determine before rss will elp us know ow muc ood our model is so tat we can compare RSS in every iteration and track our proress  
		if (membership[i] != nearest)// we ave specified it as -1 in te beinnin 62 line so dont worry.  
		{// so for te first time teir will be many updates
			membership[i] = nearest; // it specifies te nearest cluster center number
			updates++;
		}
	}
	System.out.println("RSS = " + RSS);
	return updates;
  }

  void trainRocchio()
  {
	for (int k = 0; k < K; k++){ centroids.get(k).clear(); clusterSize[k] = 0; }
      
	for (int i = 0; i < N; i++)
	{
		clusterSize[membership[i]]++;// ere we ar eincressin te cluseter size every time we find it in te sample id 
		 HashMap<Integer, Double> c  = centroids.get(membership[i]);// we wiil et tat purticular cetroid
		for (int j: data.get(i)) // oin tro all te lines of tat sample id in data and et all te values of lines wic it ad 1 for 
			if (c.containsKey(j)) c.put(j, c.get(j) + 1.0);// in te startin it was made 0 and later it is increased by 1 everytime tat line is encountered in datapoints of its cluster
			else c.put(j, 1.0); // if it encountered for te first time te ive tat line index 1
	}
	for (int k = 0; k < K; k++) if (clusterSize[k] > 0)
	{
		HashMap<Integer, Double> c = centroids.get(k);
		for (int j: c.keySet()) c.put(j, c.get(j) / clusterSize[k]);// ere we are decreasiin te et(j) size by sluster size and if cluster sixe is mor ten value will be muc smaller 
	}
	// so ere we ae updatin te value for pulticular line in cluseter cetroid every time we enter ere unless ten untill we ave 0 updatin values) 
  }

  void kMeans()
  {
	selectSeeds();// ere we select first seeds
	boolean updating = true;
	while (updating)
	{
		int n = applyRocchio(); 
		System.out.println(n);
		updating = n > 0;
		if (updating)
			{
			System.out.println("Updates : " + n);
			trainRocchio();
			}
		for (int k = 0; k < K; k++) System.out.print(clusterSize[k] + " ");
		System.out.println();
	}
  }

  void simplerKMeans(){
	selectSeeds();
	while(applyRocchio() > 0) trainRocchio();
  }

  void confusionMatrix()
  {
	confusion = new int[K][numberOfPopulations];
	for (int i = 0; i < K; i++)
		for (int j = 0; j < numberOfPopulations; j++) confusion[i][j] = 0;
	for (int i = 0; i < N; i++) confusion[membership[i]][population[i]]++;// membership is te one we calculated and population is te one ter ave specified
	for (int i = 0; i < numberOfPopulations; i++) 
		System.out.print(populationNames[i] + "\t");
	System.out.println();
	for (int k = 0; k < K; k++)
	{
		for (int i = 0; i < numberOfPopulations; i++) 
		{
			
			System.out.print(confusion[k][i] + "\t");
		}
		System.out.println();
	}
  }

void computeNMI()
{
	double[] row = new double[8];
	double[] col = new double[numberOfPopulations];
	for(int  i =0 ; i<K;i++)
	{
	                	row[i]=0;
				for(int j=0;j<numberOfPopulations;j++)
				{
				row[i] = row[i]+confusion[i][j];	
				}
	}
	for(int  i =0 ; i<numberOfPopulations;i++)
	{
		col[i]=0;
				for(int j=0;j<K;j++)
				{
				col[i] = col[i]+confusion[j][i];	
				}
	}
	
	double Factor = (double)1/Math.log(2.0)/N;
	double MI = 0;
	double a=0,b=0,c=0,d=0,e=0;
	for(int i=0;i<K;i++)
	{
		for(int j=0;j<numberOfPopulations;j++)
		{
                    if(confusion[i][j]>0)
                    {
			a =(double)N*(double)confusion[i][j]/(double)row[i]/(double)col[j];
			b= confusion[i][j]*Math.log(a);
			c = c+ b;
			a=0;
			b=0;
                    }
		}
	}
	c = c*Factor;
        System.out.println("c : " +c);

	double p,q,r,u = 0;
	for(int i=0;i<K;i++)
	{
		p = (double)row[i]/(double)N;
		q = Math.log(p);
		r = row[i]*q;
		u = u+r;
	}
	u = u*Factor;
	double u1 =0;
	for(int i=0;i<numberOfPopulations;i++)
	{
		p = ((double)col[i]/(double)N);
		q = Math.log(p);
		r = col[i]*q;
		u1 = u1+r;
   
	}
	u1 = u1*Factor;
	double NMI = (double)((double)c/(double)((u+u1)/2));
	System.out.println("NMI : " + NMI);
}
public static void main(String[] args)
{
	IR8A ir8 = new IR8A();
	ir8.readData();
	ir8.kMeans();
	ir8.confusionMatrix();
	ir8.computeNMI();
}
}