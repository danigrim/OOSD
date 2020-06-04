package assign5;

public class Account {
	
	private int ID;
    private int balance;
    private int transcation_count;
	private Object account_lock;
	public static final int INITIAL_BALANCE=1000;
	
	public Account(int id) {
		this.ID=id;
		this.balance=INITIAL_BALANCE;
		this.transcation_count=0;
		this.account_lock = new Object();
	}
	
	
	public void deposit(int amount){
		synchronized(account_lock){		
			this.balance += amount;
			this.transcation_count++;
		}
	}

	public void withdrawl(int amount){
		synchronized(account_lock){		
			this.balance -= amount;
			this.transcation_count++;
		}
	}
	
	public double get_balance() {
		return this.balance;
	}
	
	public int get_id() {
		return this.ID;
	}
	
	public int get_t_count() {
		return this.transcation_count;
	}
	
	@Override
	public String toString() {
		String acc_str = "";
		acc_str+= "acct: " + Integer.toString(this.ID) + " bal:" + Integer.toString(balance) + " trans" + Integer.toString(this.transcation_count);
		return acc_str;
	}
		
}
