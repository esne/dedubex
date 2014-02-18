import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import dedubex.rollinghash.*;


public class KM {
	//public final static int CHUNK_SIZE=64*1024;
	private final static int CHUNK_SIZE=10;
	private final static int WSIZE=5;

    File file;
    FileInputStream fis;
    BufferedInputStream bis;
    DataInputStream dis;
    private Set<Integer> hashset;
    private HashMap<Integer, HashMap<String, Integer>> hm;
    byte[] data;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        KM km = new KM();
        km.init();


	}

    public void init() throws IOException, NoSuchAlgorithmException {
        String db="db.txt";
        String backupFile="test.txt";

		/*
		 * Reading fingerprints into ram
		 */


        file = new File(db);
        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);
        dis = new DataInputStream(bis);
        hashset = new HashSet<Integer>();
        hm= new HashMap<Integer, HashMap<String, Integer>>();

		/*
		 *
		 * 			LOAD HASH MAP FROM FILE
		 *
		  *
		 */

        while (dis.available() != 0) {
            @SuppressWarnings("deprecation")
            String line = dis.readLine();
            String[] parts = line.split(":");
            if(!parts[0].isEmpty()){
                hashset.add(Integer.parseInt(parts[0]));
                if (!hm.containsKey(Integer.parseInt(parts[0]))) {
                    hm.put(Integer.parseInt(parts[0]), new HashMap<String, Integer>());
                }
                int hashSize;
                hashSize = Integer.parseInt(parts[2]);

                if(hm.get(Integer.parseInt(parts[0])).containsKey(parts[1]) ){


                    if(hm.get(Integer.parseInt(parts[0])).get(parts[1]) != hashSize){
                        //print("add to HM2 key:"+parts[0]+" hash:"+parts[1]+" size:"+parts[2]);
                        hm.get(Integer.parseInt(parts[0])).put(parts[1], hashSize);
                    }

                }else{
                    hm.get(Integer.parseInt(parts[0])).put(parts[1], hashSize);
                    //print("add to HM key:"+parts[0]+" hash:"+parts[1]+" size:"+parts[2]);
                }

            }
        }


        fis.close();
        bis.close();
        dis.close();

		/*
		 * Open file to write fingerprint:hash
		 */

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(db, true)));



		/*
		 *
		 * 			READ FILE TO ROLL
		 *
		 */
        RandomAccessFile f = new RandomAccessFile(backupFile, "r");
        data = new byte[(int)f.length()];
        f.readFully(data, 0, (int)f.length());

		 /*
		  *
		  * 		START ROLLING WINDOW METHOD
		  *
		  */
        print("txt size:"+data.length);
        rolling_window(data, hashset, out,hm);

        f.close(); /* close file */
        out.close();
    }
	private  void rolling_window(byte[] data, Set<Integer> hashset, PrintWriter out, HashMap<Integer, HashMap<String, Integer>> hsdb) throws NoSuchAlgorithmException{
 

		RabinKarpHash ch = new RabinKarpHash(WSIZE);


		 /*
		  * Read first n bytes from file and to get first fingerprint
		  */
		 int k = 0;								//start position
		 int rollinghash=0;
		 int chunkStartPos= 0;
		 int chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
		 int winStartPos=0;
	     int winEndPos;
	     int chunkStartPosFingerprint=0;
	     String chunkHash;
	     int chunkSize=0;

		 // INITIAL FINGERPRINT   FILL ROLLING WINDOW

				


	   
	     boolean jump=false;
	     boolean match=false;
	 //    boolean init=false;
	     long st1 = System.nanoTime(); // start time
		 for(;k<data.length+1;++k) {
			//if(k>t.length+1 ) break;
			if(jump){
				//print("k after jump:"+k);
				//ch=null;
				ch=new RabinKarpHash(WSIZE);
				jump=false;
			}
			winEndPos=k;
			
			if(winEndPos-winStartPos<WSIZE) {
				if(k>=data.length) break;
				winStartPos=chunkStartPos;	
				print("k:"+k);
				rollinghash=ch.eat2(data[k]);
				if((winEndPos-winStartPos)==WSIZE-1){
					print("initialized hash:"+rollinghash);
				///	init=true;
					//print("Content after initialiaz:"+ new String(Arrays.copyOfRange(t, chunkStartPos,chunkEndPos+1)));
					}
				continue;
			}

			if (hashset.contains(rollinghash)){
				
				/*
				 * 
				 * 			CHECK IF WE HAVE VALID HASH FOR ROLLING WINDOW
				 * 
				 */
				HashMap<String,Integer> hmcheck=hsdb.get(rollinghash);

                for (Entry<String, Integer> stringIntegerEntry : hmcheck.entrySet()) {
                    @SuppressWarnings("rawtypes")
                    Entry entry2 = (Entry) stringIntegerEntry;
                    String hash = (String) entry2.getKey();
                    chunkSize = Integer.parseInt(entry2.getValue().toString());


                    byte[] chunk = Arrays.copyOfRange(data, winStartPos, winStartPos + chunkSize);
                    chunkHash = md5Hash(chunk);

                    if (hash.endsWith(chunkHash)) {
                        //print("CHUNKSIIIIZE:"+chunkSize+" startpos:"+winStartPos+" hash:"+chunkHash+" db hash:"+hash);


                        //TODO: do jump as we have chunk size to jump forward. before jump we need to create chunk and add to database.

                        match = true;

                        break;
                    }

                }
			          
			      
			      
			      
				
				
				
				
			    if(match){
			    	
			    /*
			     * 
			     * 		WE HAVE HAVE MATCH
			     *
			     */
			    	print("k  match:"+k);
					if(winStartPos==chunkStartPos){
						//print("winStartPos==chunkStartPos");
						chunkStartPosFingerprint=rollinghash;	
						
							//byte[] chunk = Arrays.copyOfRange(t, chunkStartPos,chunkEndPos+1);
							//chunkHash=md5Hash(chunk);	
							
							//if(hashsetFULL.contains(chunkStartPosFingerprint+":"+chunkHash+":"+CHUNK_SIZE)){
								
						//print("We have match at pos:"+chunkStartPos+" fingerprint:"+chunkStartPosFingerprint+" chunk hash:"+chunkHash);
								//print("k first value:"+k);
								rollinghash=0;
								k=winStartPos+chunkSize-1;
								chunkStartPos=winStartPos+chunkSize;
								chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
								winStartPos=chunkStartPos;
								////winEndPos=winStartPos;
								
								jump=true;
								print("k before jump:"+k);
								continue;
								
							//}
					}else{
						
						byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,winStartPos-1);
						int localChunkSize=winStartPos-1-chunkStartPos;
						chunkHash=md5Hash(chunk);		
						print(chunkStartPosFingerprint+":"+chunkHash+":"+localChunkSize);
						out.println(chunkStartPosFingerprint+":"+chunkHash+":"+localChunkSize);
                        addToHashMap(chunkStartPosFingerprint,chunkHash,localChunkSize);

						rollinghash=0;
						//k=chunkEndPos;
						k=winStartPos+chunkSize-1;
						chunkStartPos=winStartPos+chunkSize;
						chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
						winStartPos=chunkStartPos;
						///winEndPos=winStartPos;
						jump=true;
						print("k before jump(else):"+k);
						continue;
				    }
			    }
				
				
				
				
			}else{

				if(winStartPos == chunkStartPos ){	
					chunkStartPosFingerprint=rollinghash;
				}
				
				if(winStartPos == chunkEndPos){
					byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,chunkEndPos+1);
					chunkHash=md5Hash(chunk);					
					out.println(chunkStartPosFingerprint+":"+chunkHash+":"+(chunkEndPos-chunkStartPos+1));
                    addToHashMap(chunkStartPosFingerprint,chunkHash,(chunkEndPos-chunkStartPos+1));
					////print("Writing:cs="+chunkStartPos+" ce:"+(chunkEndPos+1)+" f:"+chunkStartPosFingerprint+" h:"+chunkHash+" data:"+new String(chunk));

					chunkStartPos=chunkEndPos+1;					
					
					int tillEnd=data.length - (chunkStartPos+CHUNK_SIZE);
					if(tillEnd < WSIZE){

						chunkEndPos=chunkStartPos+CHUNK_SIZE-1+tillEnd;
					}else{
						chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
					}
					
					
					
				}
				
				

				
				if(winEndPos==data.length){
					byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,winEndPos);
					chunkHash=md5Hash(chunk);
					out.println(chunkStartPosFingerprint+":"+chunkHash+":"+(winEndPos-chunkStartPos));
                    addToHashMap(chunkStartPosFingerprint,chunkHash,(winEndPos-chunkStartPos));
					////print("------Writing:cs="+chunkStartPos+" ce:"+(winEndPos)+" f:"+chunkStartPosFingerprint+" h:"+chunkHash+" data:"+new String(chunk));
					break;
				}
				
				
			}
			
			
			
			
		


			// Generate rolling window has for next iteration
			if(k>=data.length)break;
			winStartPos=k-WSIZE;
			rollinghash = ch.update2(data[winStartPos],data[winEndPos]);
			winStartPos=k-WSIZE+1;
		}
		long et1 = System.nanoTime() - st1; // end time
		print("Estimated time:"+(float)et1/1000000000);
		
				
	}
	
	
	
	
	
	private static String  md5Hash(byte[] data) throws NoSuchAlgorithmException{
    	MessageDigest md = MessageDigest.getInstance("MD5");
		return toHex(md.digest(data));
		//
	}

	
	private static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	private static void print(String txt){
		System.out.println(txt);
	}
    private void addToHashMap(int rollinghash,String hash,int chunkSize){
        if (!hm.containsKey(rollinghash)) {
            hm.put(rollinghash, new HashMap<String, Integer>());
        }
        hashset.add(rollinghash);
        hm.get(rollinghash).put(hash, chunkSize);
    }
    private void printHashMap(){
        //
    }
}




