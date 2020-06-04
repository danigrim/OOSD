package assign5;

import java.util.ArrayList;
import java.util.concurrent.*;

import java.util.*;
import java.io.*;

public class Bank {
	
	private static ArrayList<Account> account_List;
    private static ArrayBlockingQueue<Transaction> transaction_queue; 
	private final static int QUEUE_SIZE = 80;
    private static CountDownLatch latch;
	private final static int ACCOUNTS = 20;
	
	
	private static class Worker extends Thread{
		@Override 
		public void run(){
	
			while(true) {
				try {
					Transaction to_process = transaction_queue.take();
					if(to_process.isNullTransaction()) {
						break;
					}
						process_transaction(to_process);
				} catch (InterruptedException e) {
					System.out.println("Worker threads interrupted");
					e.printStackTrace();
				}	
			}
			latch.countDown();
	}
	}
	
	public static void process_transaction(Transaction t) {
		Account giver = account_List.get(t.giver_id);
		Account reciever = account_List.get(t.reciever_id);
		giver.withdrawl(t.transaction_amount);
		reciever.deposit(t.transaction_amount);
	}
	
	public Bank(int threads){
		transaction_queue = new ArrayBlockingQueue<Transaction>(QUEUE_SIZE);
		for(int i=0; i<threads;i++) {
			 Worker new_w = new Worker();
			 new_w.start();//start worker threads
		}
		account_List = new ArrayList<Account>(ACCOUNTS); 
		for(int i=0; i<ACCOUNTS; i++) {
			account_List.add(new Account(i)); //initialize all accounts
		}
		
	}
	
	public static void readFile(String file_name, int num_threads) throws IOException {
		try {
			BufferedReader file_read = new BufferedReader(new FileReader(file_name));
			String line = file_read.readLine();
			while(line!=null) {
				String [] t_arr = line.split(" ");
				Transaction new_t = new Transaction(Integer.parseInt(t_arr[0]), Integer.parseInt(t_arr[1]),Integer.parseInt(t_arr[2]));
				try {
					transaction_queue.put(new_t);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				line = file_read.readLine();
			}
			file_read.close();
			for(int i=0; i<num_threads; i++) {
				try {
					transaction_queue.put(new Transaction(-1,0,0));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	 
	public static void main(String[] args) throws IOException { //check this exception throwing
		int threads = Integer.parseInt(args[1]);
		latch = new CountDownLatch(threads);
		Bank new_b = new Bank(threads);
	    readFile(args[0], threads);
			try {
				latch.await();
			} catch(InterruptedException ignore){
			}
			 
		 for(Account a: account_List) {
			 System.out.println(a.toString());
		 }
	}



}
