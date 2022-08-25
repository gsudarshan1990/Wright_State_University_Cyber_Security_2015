import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/***
 * 
 * 
 * @author sudarshan
 * Computation of the last subkey with first 4 bits being 0000,second 4 bits randomly generated
 * third 4 bits being 0000 and fourth four bits randomly selected.
 * 
 */

public class KeyGeneration {

	static int[] A={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	static List<String> subkey1 = new ArrayList<String>();
	static List<String> subkey2 = new ArrayList<String>(); 
	static List<String> subkey3=new ArrayList<String>();
//Declaration of first 4 bits 0000	
	static String first="0000";
	static String initial;


	static StringBuilder value1=new StringBuilder();
	static StringBuilder value2=new StringBuilder();
	static int counter=0;
	 //Storing of the list to array string
	static String[] list1;
	static String[] list2;
	static String finalvalue;
	// Function to Generate the keys and returns a list of subkeys 
	public static List sampleKeyGenerator()
	{
		
		// Conversion of 0-15 integer values to the string and storing it in 
		//list subkey1 and subkey2
		
		for(int i=0;i<A.length;i++)
		{
		
			if(A[i]==0)
			{
				StringBuilder value=new StringBuilder();
				value.append("0000000");
				value.append(Integer.toBinaryString(A[i]));
				subkey1.add(value.toString());
				subkey2.add(value.toString());
				
			}
			

				if(A[i]==1)
				{
					StringBuilder value=new StringBuilder();
					value.append("0000000");
					value.append(Integer.toBinaryString(A[i]));
					subkey1.add(value.toString());
					subkey2.add(value.toString());
					
									
				}
				
				if(A[i]==2)
				{
					StringBuilder value=new StringBuilder();
					value.append("000000");
					value.append(Integer.toBinaryString(A[i]));
					subkey1.add(value.toString());
					subkey2.add(value.toString());
					
									
				}
				
				if(A[i]==3)
				{
					StringBuilder value=new StringBuilder();
					value.append("000000");
					value.append(Integer.toBinaryString(A[i]));
					subkey1.add(value.toString());
					subkey2.add(value.toString());
					
									
				}
				

				if(A[i]==4)
				{
					StringBuilder value=new StringBuilder();
					value.append("00000");
					value.append(Integer.toBinaryString(A[i]));
					subkey1.add(value.toString());
					subkey2.add(value.toString());
					
									
				}

				if(A[i]==5)
				{
					StringBuilder value=new StringBuilder();
					value.append("00000");
					value.append(Integer.toBinaryString(A[i]));
					subkey1.add(value.toString());
					subkey2.add(value.toString());
					
									
				}
				
				if(A[i]==6)
				{
					StringBuilder value=new StringBuilder();
					value.append("00000");
					value.append(Integer.toBinaryString(A[i]));
					subkey1.add(value.toString());
					subkey2.add(value.toString());
					
									
				}
				
				if(A[i]==7)
				{
					StringBuilder value=new StringBuilder();
					value.append("00000");
					value.append(Integer.toBinaryString(A[i]));
					subkey1.add(value.toString());
					subkey2.add(value.toString());
					
									
				}
				
			if(A[i] >= 8)
			{	
				value1.append("0000");
				value1.append(Integer.toBinaryString(A[i]));
				subkey1.add(value1.toString());
				subkey2.add(value1.toString());
				value1.setLength(0);
			}
			
			//System.out.println(Integer.toBinaryString(A[i]));
		}
		
	
	
		list1=subkey1.toArray(new String[subkey1.size()]);
		list2=subkey2.toArray(new String[subkey2.size()]);
		
	
		for(int i=0;i<list1.length;i++)
		{
			for(int j=0;j<list2.length;j++)
			{	
			
				finalvalue=list1[i]+list2[j];
				subkey3.add(finalvalue);
				
			}
		}
		Iterator<String> itr=subkey3.iterator();
	/*	printing all the keys
		while(itr.hasNext())
		{
			System.out.println(counter+":"+itr.next());
			counter++;
		}
		*/
		// returns a list of 255 subkeys
		return subkey3;
}	
}
