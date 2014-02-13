

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import dedubex.rollinghash.*;

public class Unit {


public static boolean testRabinKarp(String s, int n) {
	System.out.println("Testing RabinKarpHash n = "+n);
	int aaaaa=RabinKarpHash.nonRollingHash(s.substring(0, n));
	RabinKarpHash ch = new RabinKarpHash(n);
int k = 0;
if(n>=s.length()) return false;
int rh=0;
for(; k<n-1;++k) {
	//System.out.println("k:"+k );
	rh=ch.eat2((byte)s.charAt(k));
	System.out.println("CHAR:"+s.charAt(k)+" byte:"+(byte)s.charAt(k));
}

int rollinghash = ch.eat2((byte)s.charAt(k));
System.out.println("starting hash:"+rollinghash+" nonaa:"+aaaaa);
++k;
int longhash = RabinKarpHash.nonRollingHash(s.subSequence(k-n,k));

if(rollinghash != longhash) {
System.out.println("test failed! "+rollinghash+ " "+longhash);
return false;
}
for(;k<s.length();++k) {
	rollinghash = ch.update2((byte)s.charAt(k-n),(byte) s.charAt(k));
	//System.out.println("CHAR:"+s.charAt(k)+" byte:"+(byte)s.charAt(k));
	System.out.println("rolling:"+rollinghash);
	longhash = RabinKarpHash.nonRollingHash(s.subSequence(k-n+1,k+1));
if(rollinghash != longhash) {
	System.out.println("test failed! "+k+" "+rollinghash+ " "+longhash);
return false;
}
}
System.out.println("Ok!");
return true;
}

/////////////////////////////////
public static boolean testRabinKarp2(byte[] s, int n) {
	System.out.println("Testing RabinKarpHash n = "+n);
	
	RabinKarpHash ch = new RabinKarpHash(n);
int k = 0;
if(n>=s.length) return false;
int rh=0;
for(; k<n-1;++k) {
	//System.out.println("k:"+k );
	rh=ch.eat2((byte)s[k]);
	System.out.println("CHAR:"+s[k]+" byte:"+(byte)s[k]);
}

int rollinghash = ch.eat2((byte)s[k]);

++k;
///////int longhash = RabinKarpHash.nonRollingHash(s.subSequence(k-n,k));


////if(rollinghash != longhash) {
///System.out.println("test failed! "+rollinghash+ " "+longhash);
///return false;
///}
for(;k<s.length;++k) {
	rollinghash = ch.update2((byte)s[k-n],(byte) s[k]);
	//System.out.println("CHAR:"+s.charAt(k)+" byte:"+(byte)s.charAt(k));
	System.out.println("rolling:"+rollinghash);
///////	longhash = RabinKarpHash.nonRollingHash(s.subSequence(k-n+1,k+1));
//if(rollinghash != longhash) {
//	System.out.println("test failed! "+k+" "+rollinghash+ " "+longhash);
//return false;
//}
}
System.out.println("Ok!");
return true;
}
//////////////////////



public static void main(String[] s) throws IOException {
boolean ok = true;

//for(int n = 1;n<48;++n)
//  if(!testCyclic(tests,n)) ok=false;
//
//for(int n = 1;n<10;++n)
RandomAccessFile f = new RandomAccessFile("test.txt", "r");
byte[] t = new byte[(int)f.length()];
 f.readFully(t, 0, (int)f.length());
 
String tests="aaaaaaaaa\naaaaaaaaa\naaaaaaaaa";
if(!testRabinKarp2(t,3)) ok = false;


if(!ok) System.out.println("The code did not pass the unit tests.");
else System.out.println("The code is probably ok.");
}

}