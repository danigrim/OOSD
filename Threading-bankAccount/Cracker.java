package assign5;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.security.*;

public class Cracker {
	
	
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();
	private static String hash;
	private static int max_length;
	private static CountDownLatch latch;
	private static ArrayList<String> passwords = new ArrayList<String>();

	private static class Worker extends Thread{
		int start_ind;
		int end_ind;
		
		public Worker(int begin, int end) {
			this.start_ind=begin;
			this.end_ind=end;
		}

		@Override 
		public void run(){
			for(int i=start_ind; i<end_ind; i++) {
				search_helper(""+CHARS[i]);
			}
			latch.countDown();
		}
		}
	
	public static void search_helper(String so_far) {
		if(so_far.length()>max_length) {
			return;
		}
		else if(generate_mode(so_far).equals(hash)) {
			passwords.add(so_far);
		}else {
		for(int i=0; i<CHARS.length; i++) {
			search_helper(so_far+CHARS[i]);
		}	
		}
	}

	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
		public static String hexToString(byte[] bytes) {
			StringBuffer buff = new StringBuffer();
			for (int i=0; i<bytes.length; i++) {
				int val = bytes[i];
				val = val & 0xff;  // remove higher bits, sign
				if (val<16) buff.append('0'); // leading 0
				buff.append(Integer.toString(val, 16));
			}
			return buff.toString();
		}
		
		/*
		 Given a string of hex byte values such as "24a26f", creates
		 a byte[] array of those values, one byte value -128..127
		 for each 2 chars.
		 (provided code)
		*/
		public static byte[] hexToArray(String hex) {
			byte[] result = new byte[hex.length()/2];
			for (int i=0; i<hex.length(); i+=2) {
				result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
			}
			return result;
		}
		
		// possible test values:
		// a ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb
		// fm 440f3041c89adee0f2ad780704bcc0efae1bdb30f8d77dc455a2f6c823b87ca0
		// a! 242ed53862c43c5be5f2c5213586d50724138dea7ae1d8760752c91f315dcd31
		// xyz 3608bca1e44ea6c4d268eb6db02260269892c0b42b86bbf1e77a6fa16c3c9282
		
		public Cracker(String hash_value, int max_l, int n_threads) {
			 hash=hash_value;
		     max_length=max_l;
			
			int chars_chunk = CHARS.length/n_threads;
			for(int i=0; i<n_threads; i++) {
				int endpoint =(i+1)*chars_chunk;
				if(endpoint>=CHARS.length) {
					endpoint = CHARS.length-1;
				}
				Worker new_w = new Worker((i*chars_chunk), endpoint);
				new_w.start();
			}
		}
		
		
		private static String generate_mode(String password) {
			try {
				MessageDigest message = MessageDigest.getInstance("SHA-256");
				return hexToString(message.digest(password.getBytes()));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			}
						
		}
		public static void main(String args[]) {
			if(args.length==1) {
				System.out.println(generate_mode(args[0]));
			}else if(args.length==3) {
				String hash = args[0];
				int pass_length = Integer.parseInt(args[1]);
				int number_threads = Integer.parseInt(args[2]);
				if(number_threads>CHARS.length) {
					number_threads = CHARS.length;
				}
				latch = new CountDownLatch(number_threads);
				Cracker c = new Cracker(hash, pass_length, number_threads);
				try {
					latch.await();
				} catch(InterruptedException ignore){
				}
			}
			for(int i=0; i<passwords.size(); i++) {
				System.out.println(passwords.get(i));
			}
			System.out.println("All done!");
		}

	}

	
