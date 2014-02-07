
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;


import dedubex.xxHash.*;
import dedubex.rollinghash.*;


public class KM {

	/**
	 * @param args
	 */
	public final static int PRIME_BASE=257;
	public final static int PRIME_MOD=2147483647;
	
	public final static int SEED=1;
	public final static int CHUNK_SIZE=64*1024;
	public final static int WSIZE=16;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		 
		RandomAccessFile f = new RandomAccessFile("payboy.pdf", "r");
		byte[] t = new byte[(int)f.length()];
		 f.readFully(t, 0, (int)f.length());
		 
		 
		byte p[]={'a','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','a','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','a','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e','c','a','c','d','l','k','u','b','c','d','e'};
		//byte p[]={'P','r','e','v'};
		String decoded = new String(p, "UTF-8");  
		System.out.println("Pattern text:"+decoded);
		//int p_hash=XXHash.digestFast32(p, SEED, false);
		//int r=hash(p);
		//System.out.println("hashxx:"+p_hash+" robin:"+r);
		
		//sliding_window(t,WSIZE);
		//int a = rabin_Karp(t,p);
		//String str = "some string";
		//byte s[]=str.getBytes();
		//String pat = "strin";
	
		
		
		int n = p.length; //hash all sequences of 3 characters
		RabinKarpHash ch = new RabinKarpHash(n);
		RabinKarpHash ch1 = new RabinKarpHash(n);
		int k = 0;
		int rollinghash=0;
		/* 
		 * Starting hash
		 */

		 /*
		  * Build Pattern hash
		  */

		 int j = 0;
		 int pattern_hash = 0;
			for(; j<n;++j) {
				 pattern_hash=ch1.eat2(p[j]);
				
			}	
			System.out.println("pattern hash:"+pattern_hash+" j:"+j);
	
		 /* end build pattern hash */
		 
		 
		/*
		 * Rolling windows
		 * 
		 */
		long st1 = System.nanoTime(); // start time
		for(; k<n-1;++k) {
			rollinghash=ch.eat2(t[k]);
		}
	    rollinghash = ch.eat2(t[k]); // the first or last 32-(n-1) bits are
	 			
		for(;k<t.length-1;++k) {
			rollinghash = ch.update2(t[k+1-n], t[k+1]);	
			if (rollinghash == pattern_hash){
				System.out.println("We have match at pos:"+k);
			}
		}
		
		
		/* end rolling window */
		
		long et1 = System.nanoTime() - st1; // end time
		System.out.println("Estimated time:"+(float)et1/1000000000);
		f.close(); /* close file */

	}
	
static void sliding_window(byte chunk[],int wsize) throws UnsupportedEncodingException{

    for (int i = 0; i < chunk.length-wsize; i++)
    {	

    	byte[] temp= new byte[wsize];
    	int temp_pos=0;
    	/* create window char buffer */
    	for (int j=i;j<i+wsize;j++){
    		temp[temp_pos]=chunk[j];
    		temp_pos++;
    	}
    	int c_hash=XXHash.digestFast32(temp, SEED, false);

    //	if(pattern == c_hash){
    //		String str = new String(temp, "UTF-8");
    //		System.out.println("We have match at pos:"+i+" match content:"+str);
    	
    //	}
    	//System.out.println("chunk hash:"+c_hash);
    }
	
	
}


/*
 * 		    	MessageDigest md = MessageDigest.getInstance("MD5");
		    	byte[] tmp = Arrays.copyOf(buffer, nRead);			    	
		    	byte[] thedigest = md.digest(tmp);
		    	String hash= toHex(thedigest);	
		    	
 */

public  static String toHex(byte[] bytes) {
    BigInteger bi = new BigInteger(1, bytes);
    return String.format("%0" + (bytes.length << 1) + "X", bi);
}

	
	static int hash(byte s[]){
		int ret =0;
	    for (int i = 0; i < s.length; i++)
	    {
	    	ret = ret*PRIME_BASE + s[i];
	    	ret %= PRIME_MOD;
	    }
		return ret;
	}
	
	static int rabin_Karp(byte text[], byte pattern[]) throws UnsupportedEncodingException
	{
		  

		int p_hash=hash(pattern);
		int t_hash=0;
		long power = 1;
	    for (int i = 0; i < pattern.length; i++)
	    	power = (power * PRIME_BASE) % PRIME_MOD;

	    for (int i = 0; i < text.length; i++)
	    {
	    	//System.out.println("position:"+i);
	    	//add the last letter
	    	t_hash = t_hash*PRIME_BASE + text[i];
	    	t_hash %= PRIME_MOD;

	    	//remove the first character, if needed
	    	if (i >= pattern.length)
	    	{
	    		
	    		t_hash -= power * text[i-pattern.length] % PRIME_MOD;
	    		//System.out.println("remove character hash:"+t_hash);
	    		if (t_hash < 0) //negative can be made positive with mod
	    			t_hash += PRIME_MOD;
	    		//System.out.println("remove character hash1:"+t_hash);
	    	}

	    	//match?
	    	System.out.println("text hash:"+t_hash);
	    	
	    	
	    	if (i >= pattern.length-1 && p_hash == t_hash){
	    		
	    		byte tb[] = Arrays.copyOfRange(text, i+1-pattern.length,i+1);
	    		String d = new String(tb, "UTF-8"); 
	    		System.out.println("Match:"+d+" pos:"+i+" t_hash:"+t_hash);	 
	    		//System.out.println("rK match:"+i);
	    		return i - (pattern.length-1);
	    	}
	    	
	    	
	    }
		  

	    return -1;	
	    
	}	
	

}
