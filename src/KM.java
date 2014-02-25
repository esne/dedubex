import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
import dedubex.rollinghash.*;



public class KM {
    public final static int CHUNK_SIZE=1024*1024;
    //private final static int CHUNK_SIZE=10;
    private final static int WSIZE=48;
    private static final Logger log = Logger.getLogger(
            KM.class.getName());
    private Set<Integer> hashset;
    private HashMap<Integer, HashMap<String, Integer>> hm;
    private Chunk chunk = new Chunk();
    long hashTime =0;
    long checkHashTime=0;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        KM km = new KM();
        km.init();


    }

    void init() throws IOException, NoSuchAlgorithmException {
        log.info("----------------- STARTING APP -------------------");
        String db="db.txt";
        String backupFile="playboy.pdf";


		/*
		 * Reading fingerprints into ram
		 */


        File file = new File(db);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        hashset = new HashSet<Integer>();
        hm= new HashMap<Integer, HashMap<String, Integer>>();

		/*
		 *
		 * 			LOAD HASH MAP FROM FILE
		 *
		  *
		 */
        log.debug("Reading:"+db+" file to get hash list");
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
        log.debug("Reading:"+backupFile+" data file.");
        RandomAccessFile f = new RandomAccessFile(backupFile, "r");
        byte[] data = new byte[(int) f.length()];
        f.readFully(data, 0, (int)f.length());

		 /*
		  *
		  * 		START ROLLING WINDOW METHOD
		  *
		  */
        log.debug("Data file("+backupFile+") size:" + data.length);
        log.debug("Starting rolling_window method");
        rolling_window(data, out);

        f.close(); /* close file */
        out.close();
    }



        private  void rolling_window(byte[] data, PrintWriter out) throws NoSuchAlgorithmException{

        RabinKarpHash ch = new RabinKarpHash(WSIZE);
        int k = 0;
        int rollinghash;
        int chunkStartPos= 0;
        int chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
        int winStartPos=0;
        int winEndPos;
        int chunkStartPosFingerprint=0;
        String chunkHash;
        int chunkSize;
        boolean jump=false;
        boolean match=false;
        long st1 = System.nanoTime(); // start time
         // Main loop
        for(;k<data.length;++k) {
            log.debug("---------------------------------------------");
            //if(k>t.length+1 ) break;
            winEndPos=k;
            // Jump from existing block. This is right after block.
            if(jump){
                log.debug("Landing from jump, position k:"+k);
                ch=new RabinKarpHash(WSIZE);
                jump=false;
            }

            // Check if rolling window reaches its size
            if(winEndPos-winStartPos<WSIZE) {
                //if(k>=data.length) break;
                winStartPos=chunkStartPos;

                rollinghash=ch.eat2(data[k]);
                log.debug("symbol add:"+data[k]+" rollinghash:"+rollinghash);
                //End of file. Write remaining to file
                if(winEndPos==data.length-1){

                    byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,winEndPos+1);
                    chunkHash=md5Hash(chunk);

                    //if chunk nothing else write to disk

                    if(checkHashTable(data,winStartPos,rollinghash)){
                        chunkHash=this.chunk.hash;
                        chunkSize=this.chunk.chunkSize;
                        log.debug("LAST bit. rh:"+rollinghash+" hash:"+chunkHash+" chunkSize:"+chunkSize);
                        break;
                    }else{
                        out.println(rollinghash+":"+chunkHash+":"+((winEndPos+1)-chunkStartPos));
                        addToHashMap(rollinghash,chunkHash,((winEndPos+1)-chunkStartPos));
                        ////print("------Writing:cs="+chunkStartPos+" ce:"+(winEndPos)+" f:"+chunkStartPosFingerprint+" h:"+chunkHash+" data:"+new String(chunk));
                        log.debug("Incompleate rollinghash. End of File:"+rollinghash+" chunkHash:"+chunkHash);
                        break;
                    }
                }


                // Rolling window reached its size
                if((winEndPos-winStartPos)==WSIZE-1){
                    log.debug("Rollinghash initialized:" + rollinghash+ " ("+winStartPos+","+winEndPos+")");
                    ///	init=true;
                    //print("Content after initialiaz:"+ new String(Arrays.copyOfRange(t, chunkStartPos,chunkEndPos+1)));
                }else{
                    // Rolling window not reached its size start continue with main loop.
                    continue;
                }
            }else{
                //Rolling window reached its size
                winStartPos=k-WSIZE+1;
                log.debug("remove:"+data[winStartPos]+" add:"+data[winEndPos]);
                rollinghash = ch.update2(data[winStartPos-1],data[winEndPos]);
            }


            // End of file + full rolling window.
            if(winEndPos==data.length-1){

                byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,winEndPos+1);
                chunkHash=md5Hash(chunk);
                out.println(chunkStartPosFingerprint+":"+chunkHash+":"+((winEndPos+1)-chunkStartPos));
                addToHashMap(chunkStartPosFingerprint,chunkHash,((winEndPos+1)-chunkStartPos));
                ////print("------Writing:cs="+chunkStartPos+" ce:"+(winEndPos)+" f:"+chunkStartPosFingerprint+" h:"+chunkHash+" data:"+new String(chunk));
                log.debug("Final byte of file. Writing chunk info to db. rollinghash of start of block:"+chunkStartPosFingerprint+" chunkHash:"+chunkHash);
                break;
            }


            log.debug("Before check. rollingwindows:("+winStartPos+","+winEndPos+") rh:"+rollinghash);


            // Check if we have rolling window in hashset
            if (hashset.contains(rollinghash)){
                log.debug("Found rollinghash:"+rollinghash);
                match=checkHashTable(data,winStartPos,rollinghash);
                chunkHash=chunk.hash;
                chunkSize=chunk.chunkSize;



                // We have rollinghash and hash match
                if(match){
			    	match=false;
			    /*
			     * 
			     * 		WE HAVE HAVE MATCH
			     *
			     */
                    if(chunkHash.isEmpty()) log.error("Match! chunkHash not initialized");
                    log.debug("Match! rollinghash:"+rollinghash+" chunkHash:"+chunkHash+" k:" + k);
                    //jump if we have start
                    if(winStartPos==chunkStartPos){
                        log.debug("Match! START of block. position:"+k+" rollinghash:"+rollinghash);
                        chunkStartPosFingerprint=rollinghash;
                        k=winStartPos+chunkSize-1;
                        chunkStartPos=winStartPos+chunkSize;
                        chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
                        winStartPos=chunkStartPos;
                        jump=true;
                        log.debug("Match! Jump from block START position:" + k);
                        continue;


                    }else{
                        //Jump from MIddle
                        log.debug("Match! Block MIDDLE. position:"+k+" rollinghash"+rollinghash);
                        byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,winStartPos);
                        int localChunkSize=winStartPos-chunkStartPos;
                        chunkHash=md5Hash(chunk);
                        log.debug(chunkStartPosFingerprint + ":" + chunkHash + ":" + localChunkSize);
                        out.println(chunkStartPosFingerprint+":"+chunkHash+":"+localChunkSize);
                        addToHashMap(chunkStartPosFingerprint, chunkHash, localChunkSize);
                        k=winStartPos+chunkSize-1;
                        chunkStartPos=winStartPos+chunkSize;
                        chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
                        winStartPos=chunkStartPos;
                        jump=true;
                        log.debug("Jump from MIDDLE from position:" + k);
                        continue;
                    }
                }

                if(winStartPos == chunkStartPos ){
                    log.debug("No match. winStartPos == chunkStartPos. Set rollinghash at start of block:"+rollinghash);
                    chunkStartPosFingerprint=rollinghash;
                }


            }else{
                // NO match for rollinghash and hash
                log.debug("No match. position:("+winStartPos+","+winEndPos+")");
                if(winStartPos == chunkStartPos ){
                    log.debug("No match. winStartPos == chunkStartPos. Set rollinghash at start of block:"+rollinghash);
                    chunkStartPosFingerprint=rollinghash;
                }

                if(winStartPos == chunkEndPos){
                    byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,chunkEndPos+1);
                    chunkHash=md5Hash(chunk);
                    out.println(chunkStartPosFingerprint+":"+chunkHash+":"+(chunkEndPos-chunkStartPos+1));
                    addToHashMap(chunkStartPosFingerprint,chunkHash,(chunkEndPos-chunkStartPos+1));
                    log.debug("No match. End of boundary. Saving rollinghash:"+rollinghash+" chunkHash:"+chunkHash+"");
                    chunkStartPos=chunkEndPos+1;


                   /*
                   int tillEnd=data.length - (chunkStartPos+CHUNK_SIZE);
                    if(tillEnd < WSIZE){

                        chunkEndPos=chunkStartPos+CHUNK_SIZE-1+tillEnd;
                        log.debug("No match! end of file adding EXTRA to make chunkEndPos:"+chunkEndPos);
                    }else{

                        chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
                        log.debug("No match! define chunkEndPos:"+chunkEndPos);
                    }
                    */
                    chunkEndPos=chunkStartPos+CHUNK_SIZE-1;
                    log.debug("No match! define chunkEndPos:"+chunkEndPos);


                }
            }
            /*
            if(winEndPos==data.length-1){

                byte[] chunk = Arrays.copyOfRange(data, chunkStartPos,winEndPos+1);
                chunkHash=md5Hash(chunk);
                out.println(chunkStartPosFingerprint+":"+chunkHash+":"+((winEndPos+1)-chunkStartPos));
                addToHashMap(chunkStartPosFingerprint,chunkHash,((winEndPos+1)-chunkStartPos));
                ////print("------Writing:cs="+chunkStartPos+" ce:"+(winEndPos)+" f:"+chunkStartPosFingerprint+" h:"+chunkHash+" data:"+new String(chunk));
                log.debug("Final byte of file. Writing chunk info to db. rollinghash of start of block:"+rollinghash+" chunkHash:"+chunkHash);
                break;
            }
            */

        // Generate rolling window has for next iteration


       // if(k>=data.length)break;
      /* End modified
        winStartPos=k-WSIZE;
        rollinghash = ch.update2(data[winStartPos],data[winEndPos]);
        winStartPos=k-WSIZE+1;
       */

        }
        long et1 = System.nanoTime() - st1; // end time
        log.info("Estimated time:" + (float) et1 / 1000000000);
        log.info("MD5 hashtime time:" + (float) this.hashTime / 1000000000);



    }


    private  String  md5Hash(byte[] data) throws NoSuchAlgorithmException{

        return "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    }


    private  String  md5Hash1(byte[] data) throws NoSuchAlgorithmException{


        MessageDigest md = MessageDigest.getInstance("SHA-512");
        long ht2 = System.nanoTime();

        return toHex(md.digest(data));


        //
    }


    private static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }


    private void addToHashMap(int rollinghash,String hash,int chunkSize){
        if (!hm.containsKey(rollinghash)) {
            hm.put(rollinghash, new HashMap<String, Integer>());
        }
        hashset.add(rollinghash);
        hm.get(rollinghash).put(hash, chunkSize);
    }

    private boolean checkHashTable(byte[] data,int winStartPos,int rollinghash) throws NoSuchAlgorithmException {
        boolean match=false;
        int chunkSize;
        String chunkHash;
        HashMap<String,Integer> hmcheck=this.hm.get(rollinghash);
        if(hmcheck !=null){
            for (Entry<String, Integer> stringIntegerEntry : hmcheck.entrySet()) {
                @SuppressWarnings("rawtypes")
                Entry entry2 = (Entry) stringIntegerEntry;
                String hash = (String) entry2.getKey();
                chunkSize = Integer.parseInt(entry2.getValue().toString());


                byte[] chunk = Arrays.copyOfRange(data, winStartPos, winStartPos + chunkSize);
                chunkHash = md5Hash(chunk);

                if (hash.endsWith(chunkHash)) {
                    //
                    this.chunk.hash=chunkHash;
                    this.chunk.rollinghash=rollinghash;
                    this.chunk.chunkSize=chunkSize;
                    match = true;

                    break;
                }
            }
        }
        return match;
    }

}

class Chunk{
    public int rollinghash;
    public String hash;
    public int chunkSize;
}




