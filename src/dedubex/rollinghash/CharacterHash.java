package dedubex.rollinghash;

import java.util.Random;


//not to be used directly, see CyclicHash and RabinKarpHash instead
public class CharacterHash {
	//public int hashvalues[] = new int[1<<16];
	public int hashvalues[] = new int[1<<8];
	
	
	public CharacterHash() {
		Random r = new Random();
		for(int k = 0; k<hashvalues.length;++k){
			//hashvalues[k] = r.nextInt();
			hashvalues[k] = k;
			
		}
		//System.out.println("hasvalues length:"+hashvalues.length);
	}
	
	public static CharacterHash getInstance() {
		return charhash;
	}
	
	static CharacterHash charhash = new CharacterHash();

}