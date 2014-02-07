
import java.io.IOException;
import java.io.RandomAccessFile;
import dedubex.rollinghash.*;


public class KM {

	/**
	 * @param args
	 */

	

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

		int n = p.length; //hash all sequences of 3 characters
		RabinKarpHash ch = new RabinKarpHash(n);
		RabinKarpHash ch1 = new RabinKarpHash(n);
		int k = 0;
		int rollinghash=0;

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
	

}
