
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import dedubex.rollinghash.*;


public class KM {
	//public final static int CHUNK_SIZE=64*1024;
	public final static int CHUNK_SIZE=15;
	public final static int WSIZE=9;
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		String db="db.txt";
		
		/*
		 * Reading fingerprints into ram
		 */
		File file = new File(db);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;	
		
		
		fis = new FileInputStream(file);		 
		bis = new BufferedInputStream(fis);
		dis = new DataInputStream(bis);
		
		Set<Integer> hashset = new HashSet<Integer>();
		Set<String> hashsetFULL = new HashSet<String>();
		//Map <Integer,ChunkHash> hm = new HashMap<Integer,ChunkHash>();
		HashMap<Integer, HashMap<String, Integer>> hm = new HashMap<Integer, HashMap<String, Integer>>();

		System.out.println("initializing hashmap for fasth search");
		while (dis.available() != 0) {
			@SuppressWarnings("deprecation")
			String line = dis.readLine();
			String[] parts = line.split(":");
			if(!parts[0].isEmpty()){
				hashset.add(Integer.parseInt(parts[0]));
				hashsetFULL.add(line);
				//hm.put(Integer.parseInt(parts[0]), new ChunkHash(parts[1], Integer.parseInt(parts[2])));
				if (!hm.containsKey(Integer.parseInt(parts[0]))) {
			    	hm.put(Integer.parseInt(parts[0]), new HashMap<String, Integer>());
					}
					hm.get(Integer.parseInt(parts[0])).put(parts[1], Integer.parseInt(parts[2]));
				
				
			}
		}
	/*
	 * 
	 * HashMap<String, HashMap<String, String>> myArray = new HashMap<String, HashMap<String, String>>();

		if (!myArray.containsKey("en")) {
    	myArray.put("en", new HashMap<String, String>());
		}
		myArray.get("en").put("name", "english name");
	
	 */
		
		
		
        

      //12312:aaaaaaaaaaaaaaaa:32
		 //  System.out.println("get info:"+hm.get(12312).get("aaaaaaaaaaaaaaaa"));
		
		
		fis.close();
		bis.close();
		dis.close();
	
		/*
		 * Open file to write fingerprint:hash
		 */
		
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(db, true)));
	    
	    
		
		
	
		
		/*
		 * Open file to read data for chunking
		 */
		RandomAccessFile f = new RandomAccessFile("test.txt", "r");
		byte[] t = new byte[(int)f.length()];
		 f.readFully(t, 0, (int)f.length());
				
		rolling_window(t,0,hashset,hashsetFULL,out,hm);
		

		
		/* end rolling window */
		
		
		f.close(); /* close file */
		out.close();

	}
	
	public static void rolling_window(byte[] t,int startPos,Set<Integer> hashset,Set<String> hashsetFULL,PrintWriter out, HashMap<Integer, HashMap<String, Integer>> hsdb) throws NoSuchAlgorithmException{
 

		RabinKarpHash ch = new RabinKarpHash(WSIZE);
		RabinKarpHash ch1 = new RabinKarpHash(WSIZE);


		 /*
		  * Read first n bytes from file and to get first fingerprint
		  */
		 int k = startPos;								//start position
		 int j = 0;										
		 int rollinghash=0;
		 int chunkStartPos=startPos;
		 int chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
		 int winStartPos=0;
	     int winEndPos=0;
	     int chunkStartPosFingerprint=0;
	     String chunkHash="";
	     
	     boolean setRolling=false;
		 // INITIAL FINGERPRINT   FILL ROLLING WINDOW

				


	   
	     boolean jump=false;
	     boolean init=false;
	     long st1 = System.nanoTime(); // start time
	     int hops=0;
		 for(;k<t.length+1;++k) {
			if(jump){
				System.out.println("k after jump:"+k);
				ch=null;
				ch=new RabinKarpHash(WSIZE);
				jump=false;
			}
			winEndPos=k;
			if(winEndPos-winStartPos<WSIZE) {
				winStartPos=chunkStartPos;		
				rollinghash=ch.eat2(t[k]);
				if((winEndPos-winStartPos)==WSIZE-1){
					System.out.println("initialized hash:"+rollinghash);
					init=true;
					//System.out.println("Content after initialiaz:"+ new String(Arrays.copyOfRange(t, chunkStartPos,chunkEndPos+1)));
					}
				continue;
			}

			if (hashset.contains(rollinghash)){
				
				//load hashes
				HashMap<String,Integer> hmcheck=hsdb.get(rollinghash);
 
			          for (Iterator it2 = hmcheck.entrySet().iterator(); it2.hasNext();) {  
			              Map.Entry entry2 = (Map.Entry) it2.next();  
			              Object key2 = entry2.getKey();
			              Object value2 = entry2.getValue();
			        	  System.out.println("Iterator2a: key:"+key2+" value2:"+value2);
			          }

			        
			      
			      
			      
			      
				
				
				
				// we have hash
				if(winStartPos==chunkStartPos){
					System.out.println("winStartPos==chunkStartPos");
					chunkStartPosFingerprint=rollinghash;	
					
						byte[] chunk = Arrays.copyOfRange(t, chunkStartPos,chunkEndPos+1);
						chunkHash=md5Hash(chunk);	
						
						if(hashsetFULL.contains(chunkStartPosFingerprint+":"+chunkHash+":"+CHUNK_SIZE)){
							System.out.println("We have match at pos:"+chunkStartPos+" fingerprint:"+chunkStartPosFingerprint+" chunk hash:"+chunkHash);
							System.out.println("k first value:"+k);
							rollinghash=0;
							k=chunkEndPos;
							chunkStartPos=chunkEndPos+1;
							chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
							winStartPos=chunkStartPos;
							winEndPos=winStartPos;
							jump=true;
							System.out.println("k before jump:"+k);
							continue;
							
						}
					}
				
				
			}else{
				// no rollinghashmatch

				if(winStartPos == chunkStartPos ){	
					chunkStartPosFingerprint=rollinghash;
				}
				
				if(winStartPos == chunkEndPos){
					// nebija match pa visu chunku taisam chunkam hashu un saglabâjam kaut kur

					
					byte[] chunk = Arrays.copyOfRange(t, chunkStartPos,chunkEndPos+1);
					chunkHash=md5Hash(chunk);					
					out.println(chunkStartPosFingerprint+":"+chunkHash+":"+(chunkEndPos-chunkStartPos+1));
					System.out.println("Writing:cs="+chunkStartPos+" ce:"+(chunkEndPos+1)+" f:"+chunkStartPosFingerprint+" h:"+chunkHash+" data:"+new String(chunk));

					chunkStartPos=chunkEndPos+1;					
					chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
				}

				if(winEndPos==t.length){
					byte[] chunk = Arrays.copyOfRange(t, chunkStartPos,winEndPos);
					chunkHash=md5Hash(chunk);
					out.println(chunkStartPosFingerprint+":"+chunkHash+":"+(winEndPos-chunkStartPos));
					System.out.println("Writing:cs="+chunkStartPos+" ce:"+(winEndPos)+" f:"+chunkStartPosFingerprint+" h:"+chunkHash+" data:"+new String(chunk));
				}
				
				
			}
			
			
			
			
		


			// Generate rolling window has for next iteration
			if(k>=t.length)break;
			winStartPos=k-WSIZE;
			rollinghash = ch.update2(t[winStartPos],t[winEndPos]);
			winStartPos=k-WSIZE+1;
		}
		long et1 = System.nanoTime() - st1; // end time
		System.out.println("Estimated time:"+(float)et1/1000000000);
		
				
	}
	
	
	
	
	
	static String  md5Hash(byte[] data) throws NoSuchAlgorithmException{
    	MessageDigest md = MessageDigest.getInstance("MD5");			    	
    	byte[] thedigest = md.digest(data);
    	String hash= toHex(thedigest);	
		return hash;
		//
	}

	
	public  static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}


}

class ChunkHash{
	private String chunkHash="";
	private int chunkSize=0;
	
	
	ChunkHash(String chunkHash,int chunkSize ){
		this.chunkHash=chunkHash;
		this.chunkSize=chunkSize;			
	}
	
	public String getChunkHash(){
		return this.chunkHash;
	}
	public int getChunkSize(){
		return this.chunkSize;
	}
}
