
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
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import dedubex.rollinghash.*;


public class KM {

	/**
	 * @param args
	 */

	

	public final static int CHUNK_SIZE=64*1024;
	public final static int WSIZE=48;
	
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
		System.out.println("initializing hashmap for fasth search");
		while (dis.available() != 0) {
			@SuppressWarnings("deprecation")
			String line = dis.readLine();
			String[] parts = line.split(":");
			hashset.add(Integer.parseInt(parts[0]));
			hashsetFULL.add(line);
		}
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
		 
		 
		byte p[]={'a','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','a','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','a','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e'};
		//byte p[]={'P','r','e','v'};
		
		
		
		rolling_window(t,0,hashset,hashsetFULL,out);
		

		
		/* end rolling window */
		
		
		f.close(); /* close file */
		out.close();

	}
	
	public static void rolling_window(byte[] t,int startPos,Set<Integer> hashset,Set<String> hashsetFULL,PrintWriter out) throws NoSuchAlgorithmException{
 
		int n = WSIZE; 
		RabinKarpHash ch = new RabinKarpHash(n);
		RabinKarpHash ch1 = new RabinKarpHash(n);


		 /*
		  * Read first n bytes from file and to get first fingerprint
		  */
		 int k = startPos;
		 int j = 0;
		 int rollinghash=0;
		 int chunkStartPos=startPos;
		 int chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
		 int winStartPos=0;
	     int winEndPos=0;
	     int chunkStartPosFingerprint=0;
		 // INITIAL FINGERPRINT   FILL ROLLING WINDOW
	     /*
	     for(; j<n;++j) {

				rollinghash=ch1.eat2(t[j]);
				winEndPos=j;
				
			}	
			chunkStartPosFingerprint=rollinghash;
			
			if (hashset.contains(rollinghash)){
		    	byte[] chunk = Arrays.copyOfRange(t, chunkStartPos, chunkEndPos);
		    	String chunkHash=md5Hash(chunk);
		    	if(hashsetFULL.contains(rollinghash+":"+chunkHash)){			    	
			    		System.out.println("We have match at pos:"+chunkStartPos+" fingerprint:"+chunkStartPosFingerprint+" chunk hash:"+chunkHash);
		    	}
			}	
		
*/
				
				
		// ROLLING WINDOW
		
	//	for(; k<n-1;++k) {
	//		rollinghash=ch.eat2(t[k]);
	//	}
	//    rollinghash = ch.eat2(t[k]); // the first or last 32-(n-1) bits are


	    for(; k<n-1;++k) {
			rollinghash=ch.eat2(t[k]);	
		}	

	    rollinghash = ch.eat2(t[k]);
	    winEndPos=k;
		chunkStartPosFingerprint=rollinghash;
		
		if (hashset.contains(rollinghash)){
	    	byte[] chunk = Arrays.copyOfRange(t, chunkStartPos, chunkEndPos);
	    	String chunkHash=md5Hash(chunk);
	    	if(hashsetFULL.contains(rollinghash+":"+chunkHash)){			    	
		    		System.out.println("We have match at pos:"+chunkStartPos+" fingerprint:"+chunkStartPosFingerprint+" chunk hash:"+chunkHash);
	    	}
		}
		
		
		
		
	/*    
	    //Write fist data
	    if(boundPos==0){
	    	int chunkFingerprint = rollinghash;
	    	byte[] chunk = Arrays.copyOfRange(t, boundPos, boundPos+CHUNK_SIZE);
	    	String chunkHash=md5Hash(chunk);
	    	
	    	if (!hashset.contains(rollinghash)){
	    		out.println(chunkFingerprint+":"+chunkHash);
			}else if(!hashsetFULL.contains(chunkFingerprint+":"+chunkHash)){
				out.println(chunkFingerprint+":"+chunkHash);
			}else{
					//exist
			}	
	    }
	    
	  */  
	    long st1 = System.nanoTime(); // start time
	    int hops=0;
		for(;k<t.length-1;++k) {
			
			rollinghash=ch.eat2(t[k]);
			
			
			
			
			
			winStartPos=k+1-n;
			winEndPos=k+1;
			rollinghash = ch.update2(t[winStartPos],t[winEndPos]);	
			//                           ^^^^^^^       ^^^^^^^^
			//							  drop char    add char

			
				if (hashset.contains(rollinghash)){
				//System.out.println("jump");
				//rolling_window(t, k+CHUNK_SIZE, hashset, hashsetFULL, out);
				//byte[] chunk = Arrays.copyOfRange(t, k, k+CHUNK_SIZE);							
			    //	String chunkHash=md5Hash(chunk);
			    //	if(hashsetFULL.contains(rollinghash+":"+chunkHash)){
			    		/////System.out.println("jump");
			    		//rolling_window(t, startPos+n, hashset, hashsetFULL, out);
			    //	}
			    	
				}	
			
			//rollinghash = ch.update2(t[k+1-n], t[k+1]);	
			/*						 ^^^^^^^^  ^^^^^^
			 * 						  out        in	
			 */

		}
		System.out.println("hops:"+hops);
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
