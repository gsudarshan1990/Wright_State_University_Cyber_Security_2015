import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.*;

public class BitStringSPNCipher {

	static int blocksize = 16;
	static int samplesize = 10000; // number of rows in sample data
	
	//List to store all the subkeys.
	static List<String> subkey=new ArrayList<String>();

	static int[] permutation =    {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
	
	static int counter1=0;
	static String[] keylist;
	
	//Stores the probability bias values for all the 256 subkeys
	static int[] counterlist=new int[256];
	//Calculate the deviation value from 0.03125
	static double[] deviationlist=new double[256];
		
	static HashMap<String, String> sub = new HashMap<>();
	static {
		sub.put("0000", "1110");
		sub.put("0001", "0100");
		sub.put("0010", "1101");
		sub.put("0011", "0001");
		sub.put("0100", "0010");
		sub.put("0101", "1111");
		sub.put("0110", "1011");
		sub.put("0111", "1000");
		sub.put("1000", "0011");
		sub.put("1001", "1010");
		sub.put("1010", "0110");
		sub.put("1011", "1100");
		sub.put("1100", "0101");
		sub.put("1101", "1001");
		sub.put("1110", "0000");
		sub.put("1111", "0111");
	}
	
	static HashMap<String, String> desub = new HashMap<>();
	static {
		desub.put("0000", "1110");
		desub.put("0001", "0011");
		desub.put("0010", "0100");
		desub.put("0011", "1000");
		desub.put("0100", "0001");
		desub.put("0101", "1100");
		desub.put("0110", "1010");
		desub.put("0111", "1111");
		desub.put("1000", "0111");
		desub.put("1001", "1101");
		desub.put("1010", "1001");
		desub.put("1011", "0110");
		desub.put("1100", "1011");
		desub.put("1101", "0010");
		desub.put("1110", "0000");
		desub.put("1111", "0101");
	}

	// note, these are not the keys used to create the test data
	static String sk1 = "0110110001101010";
	static String sk2 = "1001110111011111";
	static String sk3 = "1011111011010001";
	static String sk4 = "1010101010000010";
	static String sk5 = "0000001111111001";

	
	public static String encryptBlock(String plaintextBlock) {

		// Round 1
		String mixed1 = subkeyMixing(plaintextBlock, sk1); 
		String subbed1 = substitute(mixed1, true);
		String permuted1 = permute(subbed1);

		// Round 2
		String mixed2 = subkeyMixing(permuted1, sk2); 
		String subbed2 = substitute(mixed2, true);
		String permuted2 = permute(subbed2);

		// Round 3
		String mixed3 = subkeyMixing(permuted2, sk3); 
		String subbed3 = substitute(mixed3, true);
		String permuted3 = permute(subbed3);

		// Round 4
		String mixed4 = subkeyMixing(permuted3, sk4);
		String subbed4 = substitute(mixed4, true);
		String mixed5 = subkeyMixing(subbed4, sk5); 

		return mixed5;
	}

	
	public static String decryptBlock(String ciphertextBlock) {

		// Round 4 reversed
		String mixed5 = subkeyMixing(ciphertextBlock, sk5); 
		String substituted4 = substitute(mixed5, false);
		String mixed4 = subkeyMixing(substituted4, sk4); 

		// Round 3 reversed
		String permuted3 = permute(mixed4);
		String substituted3 = substitute(permuted3, false);
		String mixed3 = subkeyMixing(substituted3, sk3); 

		// Round 2 reversed
		String permuted2 = permute(mixed3);
		String substituted2 = substitute(permuted2, false);
		String mixed2 = subkeyMixing(substituted2, sk2); 

		// Round 1 reversed
		String permuted1 = permute(mixed2);
		String substituted1 = substitute(permuted1, false);
		String mixed1 = subkeyMixing(substituted1, sk1); 

		return mixed1;
	}

	
	private static String subkeyMixing(String input, String subkey) {	
		String result = "";

		for (int i=0; i<input.length(); i++) {
			
			if (input.charAt(i) == '1' && subkey.charAt(i) == '0' || 
					input.charAt(i) == '0' && subkey.charAt(i) == '1') {
				
				result += "1";
				
			} else {
				result += "0";
			}
		}

		return result;
	}

	
	private static String substitute(String input, boolean forwards) {
		String result = "";
		
		String nibble1 = input.substring(0, 4);
		String nibble2 = input.substring(4, 8);
		String nibble3 = input.substring(8, 12);
		String nibble4 = input.substring(12, 16);
		
		if (forwards) {
			result += sub.get(nibble1);
			result += sub.get(nibble2);
			result += sub.get(nibble3);
			result += sub.get(nibble4);
		} else {
			result += desub.get(nibble1);
			result += desub.get(nibble2);
			result += desub.get(nibble3);
			result += desub.get(nibble4);
		}
		
		return result;
	}

	
	private static String permute(String input) {	
		StringBuffer permuted = new StringBuffer(input);
		
		for (int j=0; j<input.length(); j++) {
			permuted.setCharAt(j, input.charAt(permutation[j]));
		}
		
		return permuted.toString();
	}


	public static void main(String[] args) throws Exception {
		String plaintext = "0100111101001011";
		String ciphertext = encryptBlock(plaintext);
		System.out.println(plaintext);
		System.out.println(ciphertext);
		KeyGeneration kg=new KeyGeneration();
	// calling the function sampleKeyGenerator  to store the values in the subkey list	
		subkey=kg.sampleKeyGenerator();
		keylist=subkey.toArray(new String[subkey.size()]);
	
	
	// Calculation of the last subkey 
		readCipherFile();
		
	}
	
	static String output="";
	private static void readCipherFile() {
		// TODO Auto-generated method stub
		String filename="ciphertextSample.txt";
		String filename2="plaintextSample.txt";
		String ciphertext=null;
		String plaintext=null;
		int i=0;
		int j;
		
		try
		{
			//Reading of the file "Ciphertext"
			FileReader fileReader=new FileReader(filename);
			BufferedReader bufferedReader=new BufferedReader(fileReader);
			//Reading of the file "Plaintext"
			FileReader fileReader1=new FileReader(filename2);
			BufferedReader bufferedReader2=new BufferedReader(fileReader1);
		
			
			for(i=0;i<10001;i++)
			{
				ciphertext=bufferedReader.readLine();
				plaintext=bufferedReader2.readLine();
				//System.out.println("line is:"+line);
				//System.out.println(sk1);
				

				for(j=0;j<keylist.length;j++)
				{
				
					//Computing the linear approximation
					compute(plaintext,ciphertext,keylist[j],j);
					
								
				}
				
			}
			
		}
		catch(Exception e)
		{
			
		}
		/*
		for(j=0;j<counterlist.length;j++)
		{
			System.out.println(counterlist[j]);
		}
		*/
	//Calculating the bias values for all the subkeys	
		deviationcalculation();
	/*	Printing all the deviation values
		for(j=0;j<counterlist.length;j++)
		{
			System.out.println(j+":"+deviationlist[j]);
		}
		*/
		double smallest=deviationlist[0]; 
		int index=0;
	// calculation the key that has the value close to 0.03125	
		for(j=0;j<deviationlist.length;j++)
		{
			if(deviationlist[j]<smallest)				
			{		
				smallest=deviationlist[j];
				index=j;
			}
		}
		//System.out.println(index);	
		System.out.println("The key is :"+ keylist[index]);
}
//Function for calculating the bias value for all the subkeys	
	private static void deviationcalculation() {
		// TODO Auto-generated method stub
		int i=0;
		for(i=0;i<counterlist.length;i++)
		{
			int count=Math.abs((counterlist[i]-5000));
			double bias=count/10000.0;
			bias=0.03125-bias;
			deviationlist[i]=bias;
		}		
}
private static void compute(String plaintext, String ciphertext, String key,int counternumber) {
		// TODO Auto-generated method stub
		
	//XOR a cipher text sample with the subkey
		String[] parts1=key.split("");
		int j;
		String[] parts=ciphertext.split("");
		
		for(j=0;j<parts.length;j++)
		{
			
				int d=Integer.parseInt(parts[j]);
				int e=Integer.parseInt(parts1[j]);
				int c=d^e;
				output=output+String.valueOf(c);
		}
		//System.out.println("Ciphertext"+ciphertext);
		//System.out.println("key"+key);
		//System.out.println("output:"+output);
	//	Running the result backwards through the substitution process
		
		String finaloutput=substitute(output,false);
		//System.out.println("finaloutput:"+finaloutput);
		String[] Uparts=finaloutput.split("");
		//System.out.println("U of Ciphertext");
		output="";
		String[] Pparts;
		/*
		for(int k=0;k<Uparts.length;k++)
		{
			System.out.println(k+":"+Uparts[k]);
		}
		*/
		 Pparts=plaintext.split("");
			/*
			System.out.println("plaintext");
			for(int l=0;l<Uparts.length;l++)
			{
				System.out.println(l+":"+Pparts[l]);
			}
			*/
	/*
	 * Operands value of the linear approximation	 
	 */
		int firstvalue=Integer.parseInt(Uparts[5]);
		int secondvalue=Integer.parseInt(Uparts[7]);
		int thirdvalue=Integer.parseInt(Uparts[13]);
		int fourthvalue=Integer.parseInt(Uparts[15]);
		int fifthvalue=Integer.parseInt(Pparts[4]);
		int sixthvalue=Integer.parseInt(Pparts[6]);
		int seventhvalue=Integer.parseInt(Pparts[7]);
		
		// computing the linear approximation value using U and the plaintext sample corresponding to this ciphertext
		
		int finalvalue=firstvalue^secondvalue^thirdvalue^fourthvalue^fifthvalue^sixthvalue^seventhvalue;
		if(finalvalue==0)
		{
		//Calculate the count values of the each key
			counterlist[counternumber]++;
		}
			//System.out.println("finaloutputvalue:"+finalvalue);		
	}
}
