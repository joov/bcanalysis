package basic;

import org.json.JSONObject;

public class AddressJSON {
	private String address;
	private String firstSeen;
	private String lastSeen;
	
	public AddressJSON(String address){
		this.address = address;
	}
	
	public AddressJSON(JSONObject job){
		this.address = job.getString("address");
		this.firstSeen = job.getString("first_seen").split("\\+")[0];
		this.lastSeen = job.getString("last_seen").split("\\+")[0];
	}

	
	public String getAddr(){
		return this.address;
	}
	
	public String getFirstSeen(){
		return this.firstSeen;
	}
	
	public String getLastSeen(){
		return this.lastSeen;
	}
	public boolean equal(Object o){
		return this.address.equals(((AddressJSON)o).address);
	}
	
	public int hashCode(){
		return this.address.hashCode();
	}
}
