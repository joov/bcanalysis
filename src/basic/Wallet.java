package basic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * The wallet class
 * 
 * @author yshi
 *
 */
public class Wallet {
	private HashSet<AddressT> addrs = new HashSet<AddressT>();
	private String primAddr;  // the address which also provide the first_seen info, only serve as an id
	private long firstSeen;
	private String firstSeenTime;
	private long lastSeen;
	private String lastSeenTime;
	
	public Wallet(ArrayList<AddressT> arrAddr){
		AddressT earliest = arrAddr.get(0);
		for(int s = 1; s < arrAddr.size(); s ++){
			if(earliest.getFirstSeenStamp() > arrAddr.get(s).getFirstSeenStamp()){
				earliest = arrAddr.get(s);
			}
		}
		AddressT latest = arrAddr.get(0);
		for(int s = 1; s < arrAddr.size(); s ++){
			if(latest.getLastSeenStamp() < arrAddr.get(s).getLastSeenStamp()){
				latest = arrAddr.get(s);
			}
		}
		this.primAddr = earliest.getAddr();
		this.firstSeenTime = earliest.getFirstSeen();
		this.firstSeen = earliest.getFirstSeenStamp();
		this.lastSeenTime = latest.getLastSeen();
		this.lastSeen = latest.getLastSeenStamp();
		this.add(arrAddr);	
	}
	
	public Wallet(AddressT primBegAddr, AddressT primEndAddr){
		this.primAddr = primBegAddr.getAddr();
		this.firstSeenTime = primBegAddr.getFirstSeen();
		this.firstSeen = primBegAddr.getFirstSeenStamp();
		this.lastSeenTime = primEndAddr.getLastSeen();
		this.lastSeen = primEndAddr.getLastSeenStamp();
		primBegAddr.setWallet(this.primAddr);
		primEndAddr.setWallet(this.primAddr);
		this.addrs.add(primBegAddr);
		this.addrs.add(primEndAddr);
	}
	
	public void setLastSeen(String lastSeen){
		this.lastSeenTime = lastSeen.split("\\+")[0];
		try {
			this.lastSeen = Util.getTime(this.lastSeenTime).getTime()/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void setFirstSeen(String firstSeen){
		this.lastSeenTime = firstSeen.split("\\+")[0];
		try {
			this.lastSeen = Util.getTime(this.lastSeenTime).getTime()/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	//primAddress,time
	public String toString(){
		return this.primAddr + ',' + this.firstSeenTime + ',' + this.lastSeenTime + ',' + this.addrs.size();
	}
	
	
	/**
	 * add an address to the addrs attribute
	 * @param addr
	 */
	public void add(AddressT addr){
		addr.setWallet(this.primAddr);
		this.addrs.add(addr);
		if(this.getFirstSeenStamp() > addr.getFirstSeenStamp()){
			this.firstSeen = addr.getFirstSeenStamp();
			this.firstSeenTime = addr.getFirstSeen();
		}
		if(this.getLastSeenStamp() < addr.getLastSeenStamp()){
			this.lastSeen = addr.getLastSeenStamp();
			this.lastSeenTime = addr.getLastSeen();
		}
	}
	
	/**
	 * add an Collection of address to the addrs attribute
	 * @param addr
	 */
	public void add(Collection<AddressT> addrs){
		for(AddressT addr: addrs){
			this.add(addr);
		}		
	}
	
	
	/**
	 * @param addr
	 * @return if the addrs of the wallet contain addr
	 */
	public boolean contain(AddressT addr){
		return this.addrs.contains(addr);
	}
	public String getPrimAdd(){
		return this.primAddr;
	}
	
	public String getFirstSeen(){
		return this.firstSeenTime;
	}
	
	public long getFirstSeenStamp(){
		return this.firstSeen;
	}
	
	public String getLastSeen(){
		return this.lastSeenTime;
	}
	
	public long getLastSeenStamp(){
		return this.lastSeen;
	}
	
	public HashSet<AddressT> getAddrSet(){
		return this.addrs;
	}
	
	
	/* (non-Javadoc)
	 * if two wallet has one or more common address, 
	 * the two wallets will be considered as the
	 * same wallet 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		HashSet<AddressT> addsO = ((Wallet)o).getAddrSet();
		for(AddressT add : addsO){
			if(this.addrs.contains(add)){
				return true;
			}
		}
		return false;
	}
	
	
	public void merge(ToCSVParser parser, Wallet w1){
		if(w1.getFirstSeenStamp() < this.getFirstSeenStamp()){
			w1.add(this.addrs);
			parser.adjustTxSetAccWallet(this, w1);
			parser.removeFromWallList(this);
		}else{
			this.add(w1.addrs);
			parser.adjustTxSetAccWallet(w1, this);
			parser.removeFromWallList(w1);
		}
	}
}
