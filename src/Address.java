package basic;

import java.util.Objects;

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

   public int hashCode() {
	   return 0;
   }
	
	public boolean equals(Object a){
		return 	Objects.equals(((Address) a).address, this.address)
				&&	Objects.equals(((Address) a).addrTagLink, this.addrTagLink)
				&& Objects.equals(((Address) a).addrTag, this.addrTag);
	}
	
	public String toString(){
		return this.address +  "," + this.addrTagLink + ","+ this.addrTag;
	}
}
