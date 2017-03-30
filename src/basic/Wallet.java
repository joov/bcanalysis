package basic;

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
	private String primAddr;
	private long timestamp;
	private String time;
	
	public Wallet(AddressT primAddr){
		this.primAddr = primAddr.getAddr();
		this.time = primAddr.getTime();
		this.timestamp = primAddr.getTimestamp();
		this.addrs.add(primAddr);
	}
	
	//primAddress,time
	public String toString(){
		return this.primAddr + ',' + this.time+ ',' + this.addrs.size();
	}
	
	
	/**
	 * add an address to the addrs attribute
	 * @param addr
	 */
	public void add(AddressT addr){
		addr.setWallet(this.primAddr);
		this.addrs.add(addr);
	}
	
	/**
	 * add an Collection of address to the addrs attribute
	 * @param addr
	 */
	public void add(Collection<AddressT> addrs){
		for(AddressT a: addrs){
			a.setWallet(this.primAddr);
		}
		this.addrs.addAll(addrs);
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
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public String getTime(){
		return this.time;
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
	
	
	public void merge(ParserToCSVModel2 parser, Wallet w1){
		if(w1.getTimestamp() < this.getTimestamp()){
			for(AddressT a : this.addrs){
				a.setWallet(w1.primAddr);
			}
			w1.addrs.addAll(this.addrs);
			parser.removeFromWallList(this);
		}else{
			for(AddressT a : w1.addrs){
				a.setWallet(this.primAddr);
			}
			this.addrs.addAll(w1.addrs);
			parser.removeFromWallList(w1);
		}
	}
}
