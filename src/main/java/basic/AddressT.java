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
	private String tranHash;
	private boolean isOutput;
	private int index;

	public AddressT(String address, String addrTagLink, String addrTag, AddressJSON adJ, 
			String primWAdd, boolean multiExist, String tranHash, boolean isOutput, int index) {
		super(address, addrTagLink, addrTag, adJ);
		this.primWAdd = primWAdd; 
		this.multiExist = multiExist;
		this.tranHash = tranHash;
		this.isOutput = isOutput;
		this.index = index;
	}
	
	public AddressT(String address, String addrTagLink, String addrTag, String firstSeen, 
			String lastSeen, String primWAdd, boolean multiExist, String tranHash, boolean isOutput, int index) {
		super(address, addrTagLink, addrTag, firstSeen, lastSeen);
		this.primWAdd = primWAdd; 
		this.multiExist = multiExist;
		this.tranHash = tranHash;
		this.isOutput = isOutput;
		this.index = index;
	}
	
	public String getTran(){
		return this.tranHash;
	}
	
	public void setWallet(String primWAdd){
		this.primWAdd = primWAdd;
	}
	
	public boolean isOutput(){
		return this.isOutput;
	}
	public int getIndex(){
		return this.index;
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
