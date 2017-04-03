package basic;

import java.text.ParseException;
import java.util.Date;

/**
 * The address class with timestamp
 * @author yshi
 *
 */
public class AddressOT extends Address{
	private long firstSeen; //in seconds
	private String firstSeenTime;
	private long lastSeen; //in seconds
	private String lastSeenTime;
	
	
	public AddressOT(String address, String addrTagLink, String addrTag, String firstSeen, String lastSeen) {
		super(address, addrTagLink, addrTag);	  
	    try {
			this.firstSeenTime = firstSeen;
			Date temp =  Util.getTime(this.firstSeenTime);
			this.firstSeen = temp.getTime()/1000;	
			
			this.lastSeenTime = lastSeen;
			temp =  Util.getTime(this.firstSeenTime);
			this.lastSeen = temp.getTime()/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}
	
	public AddressOT(String address, String addrTagLink, String addrTag, AddressJSON adJ) {
		super(address, addrTagLink, addrTag);	  
	    try {
			this.firstSeenTime = adJ.getFirstSeen();
			Date temp =  Util.getTime(this.firstSeenTime);
			this.firstSeen = temp.getTime()/1000;	
			
			this.lastSeenTime = adJ.getLastSeen();
			temp =  Util.getTime(this.firstSeenTime);
			this.lastSeen = temp.getTime()/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		} 
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
	
	//addrID,addr_tag_link,addr_tag,time
	public String toString(){
		return super.toString() + ',' + this.firstSeenTime+ ',' + this.lastSeenTime;
	}
}
