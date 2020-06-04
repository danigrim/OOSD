package assign5;

public class Transaction {

	 final int reciever_id;
	 final int giver_id;
     final int transaction_amount;
	

	public Transaction(int giver, int reciever, int amount) {
		this.reciever_id=reciever;
		this.giver_id=giver;
		this.transaction_amount= amount;
	}

	public int get_recieverId() {
		return reciever_id;
	}
	
	public int get_giverId() {
		return giver_id;
	}
	
	public int get_amount() {
		return transaction_amount;
	}
	
	public boolean isNullTransaction() {
	  return((this.giver_id==-1)&&(this.reciever_id==0)&&(this.transaction_amount==0));
	}
	
}

