package basic;

import java.util.Objects;


/**
 * a class for bitcoin address, possibly including the  
 * values of addr_tag_link and addr_tag found on blockchain.info
 * @author yshi
 *
 */

public class Address{
	private String address;
	private String addrTagLink;
	private String addrTag;
	
	public Address(String address, String addrTagLink, String addrTag){
		this.address = address;
		this.addrTagLink = addrTagLink;
		this.addrTag = addrTag;
	}
	
	public String getAddr(){
		return this.address;
	}
	public String getAddLink(){
		return this.addrTagLink;
	}
	public String getAddTag(){
		return this.addrTag;
	}


	/**
	 * To ensure that when no duplicate will present in the same hashset
	 * overrides hashCode() in Object
	 */
	public int hashCode() {
		return 0;	
	}
	
	/**
	 * To ensure that when no duplicate will present in the same hashset
	 * overrides equals(Object) in Object
	 */
	public boolean equals(Object a){
		return 	Objects.equals(((Address) a).address, this.address)
				&&	Objects.equals(((Address) a).addrTagLink, this.addrTagLink)
				&& Objects.equals(((Address) a).addrTag, this.addrTag);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return this.address +  "," + this.addrTagLink + ","+ this.addrTag;
	}
}
