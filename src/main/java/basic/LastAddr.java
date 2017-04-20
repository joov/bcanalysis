package basic;

/**
 * A lightweight object to store the last address 
 * added in a single run of parse(), along with the transaction 
 * it belongs to.
 * 
 * @author yshi
 *
 */
public class LastAddr {
	private String address;
	private String transaction;
	private boolean isOutput;
	private int index;
	public LastAddr(String address, String transaction, boolean isOutput, int index){
		this.address = address;
		this.transaction = transaction;
		this.isOutput = isOutput;
		this.index = index;
	}
	
	public String toString(){
		return "Address: " + this.address + " Trans: "+ this.transaction 
				+ " isOutput: "+ this.isOutput + " index: "+ this.index;
	}
	public boolean isOutput(){
		return this.isOutput;
	}
	public int getIndex(){
		return this.index;
	}
	
	public String getAddr(){
		return this.address;
	}

	public String getTran(){
		return this.transaction;
	}
}
