package basic;

/**
 * The address class with timestamp and 
 * the primary address of the wallet it belongs to.
 * 
 * @author yshi
 *
 */
public class AddressT extends AddressOT{
	private String primWAdd;
	private boolean multiExist; // if the address has participated in an multisignature transaction
	

	public AddressT(String address, String addrTagLink, String addrTag, String primWAdd, String time, boolean multiExist) {
		super(address, addrTagLink, addrTag, time);
		this.primWAdd = primWAdd; 
		this.multiExist = multiExist;
	}
	
	public void setWallet(String primWAdd){
		this.primWAdd = primWAdd;
	}
	
	
	public boolean getMulti(){
		return this.multiExist;
	}

	public String getPrimWAdd(){
		return this.primWAdd;
	}
	
	public void setMultiTrue(){
		this.multiExist = true;
	}
	
	//addrID,addr_tag_links,addr_tags,time,primWallAdd,multisig
	public String toString(){
		return super.toString() + ',' + this.primWAdd+ ',' + this.multiExist;
	}
}
