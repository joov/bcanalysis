package basic;

import java.text.ParseException;
import java.util.Date;

/**
 * The address class with timestamp
 * @author yshi
 *
 */
public class AddressOT extends Address{
	private long timestamp; //in seconds
	private String time;

	public AddressOT(String address, String addrTagLink, String addrTag, String time) {
		super(address, addrTagLink, addrTag);	  
	    try {
			this.time = time.split("\\+")[0];
			Date temp =  Util.getTime(time);
			this.timestamp = temp.getTime()/1000;	
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}
	
	public String getTime(){
		return this.time;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	//addrID,addr_tag_link,addr_tag,time
	public String toString(){
		return super.toString() + ',' + this.time;
	}
}
