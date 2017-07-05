package basic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;



/**
 * class for transaction
 * 
 * @author yshi
 *
 */
public class Transaction {
	private Wallet receiWallet;	
	private Wallet sendWallet;
	
	private long satoshi; 
	private double dollar;
	private long timestamp;
	private String time;
	private String hash;
	private String outputType; 
	private String estChanAddr;
	private int outputIndex;
	
	public Transaction(String hash, ArrayList<Wallet> availWallet, Wallet sender, AddressT receiver, 
			long satoshi, double dollar, String time, String outputType, String estChanAddr, ParserToCSVModel2 parser,
			int outputIndex){
		this.hash = hash;
		this.satoshi = satoshi;
		this.dollar = dollar;
		this.outputType = outputType;
		this.estChanAddr = estChanAddr;
		this.outputIndex = outputIndex;
	    try {
			this.time = time.split("\\+")[0];
			Date temp =  Util.getTime(time);
			this.timestamp = temp.getTime()/1000;	
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	    this.sendWallet = sender;
//		if the sender/receiver wallet is already in availWallet
		boolean receiverWPresent = false;		
		for(Wallet w : availWallet){
			if(w.contain(receiver)){
				this.receiWallet = w;
				receiverWPresent = true;
			}	
		}

		if(!receiverWPresent){	
			this.receiWallet = new Wallet(receiver, receiver);
			parser.addToWallList(this.receiWallet);
		}
		
		HashSet<AddressT> recAddr = this.receiWallet.getAddrSet();
		for(AddressT a: recAddr){
			a.setWallet(this.receiWallet.getPrimAdd());
		}
		HashSet<AddressT> sendAddr = this.sendWallet.getAddrSet();
		for(AddressT a: sendAddr){
			a.setWallet(this.sendWallet.getPrimAdd());
		}
	}
	
	//used when the output is multisig
	public Transaction(String hash, ArrayList<Wallet> availWallet, Wallet sender, ArrayList<AddressT> receivers, 
			long satoshi, double dollar, String time, String outputType, String estChanAddr, ParserToCSVModel2 parser,
			int outputIndex){
		this.hash = hash;
		this.satoshi = satoshi;
		this.dollar = dollar;
		this.outputType = outputType;
		this.estChanAddr = estChanAddr;
		this.outputIndex = outputIndex;
	    try {
			this.time = time.split("\\+")[0];
			Date temp =  Util.getTime(time);
			this.timestamp = temp.getTime()/1000;	
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	    
	    this.sendWallet = sender;
	    
		
//		// if the sender/receiver wallet is already in availWallet
//		boolean senderWPresent = false; 
		boolean receiverWPresent = false;
//		
		for(Wallet w : availWallet){
//			for(AddressT sender : senders){
//				if(w.contain(sender)){
//					this.sendWallet = w;
//					senderWPresent = true;
//					break;
//				}				
//			}
			for(AddressT receiver : receivers){
				if(w.contain(receiver)){
					this.receiWallet = w;
					receiverWPresent = true;
				}	
			}	
		}
//		
//		if(!senderWPresent){
//			AddressT earliest = senders.get(0);
//			for(int i = 1; i < senders.size(); i ++){
//				if(earliest.getTimestamp() > senders.get(i).getTimestamp()){
//					earliest = senders.get(i);
//				}
//			}			
//			this.sendWallet = new Wallet(earliest);
//		}
		if(!receiverWPresent){	
			this.receiWallet = new Wallet(receivers);
			parser.addToWallList(this.receiWallet);
		}
		
		HashSet<AddressT> recAddr = this.receiWallet.getAddrSet();
		for(AddressT a: recAddr){
			a.setWallet(this.receiWallet.getPrimAdd());
		}
		HashSet<AddressT> sendAddr = this.sendWallet.getAddrSet();
		for(AddressT a: sendAddr){
			a.setWallet(this.sendWallet.getPrimAdd());
		}
	}
	
	public void setReceiveWallet(Wallet rec){
		this.receiWallet = rec;
	}
	
	public void setSendWallet(Wallet send){
		this.sendWallet = send;
	}
	
	public Wallet getReceiver(){
		return this.receiWallet;
	}
	
	public Wallet getSender(){
		return this.sendWallet;
	}
	
	public String getHash(){
		return this.hash;
	}
	
	// if receiving address has a first seen time earlier or equal to
	// the first seen time of the transaction, then it is quite possible 
	// that the receiving address belongs to the sending address
	public String getTime(){
		return this.time;
	}
	public long getTimeStamp(){
		return this.timestamp;
	}
	public double getDollar(){
		return this.dollar;
	}
	public long getSato(){
		return this.satoshi;
	}
	
	public String getType(){
		return this.outputType;
	}
	
	public String getEstChanAddr(){
		return this.estChanAddr;
	}
	
	//sendWallet,receWallet,tranHashString,time,value_bitcoin,value_dollar,type,estChanAddr,outputIndex
	public String toString(){
		return this.sendWallet.getPrimAdd() + ',' + this.receiWallet.getPrimAdd()
		+ ',' + this.hash + ',' + this.time + ',' + this.satoshi + ',' + this.dollar
		+ ',' + this.outputType + ',' + this.estChanAddr  + ',' + this.outputIndex;
	}

}
