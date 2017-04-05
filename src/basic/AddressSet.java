package basic;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.json.JSONObject;

public class AddressSet{
	
	 LinkedHashSet<AddressT> addrSet = new  LinkedHashSet<AddressT>();
	 
	 public AddressSet(){		 
	 }
	 
	 public LinkedHashSet<AddressT> getAddrSet(){
		 return this.addrSet;
	 }
	 
	 public void remove(Address a){
		 this.addrSet.remove(a);
	 }
	 
	 public boolean add(AddressT a, ToCSVParser p){
		 p.setLastAddrBef(a.getAddr());
		 return this.addrSet.add(a);
	 }
	 
	 public AddressT getLastAdded(){
		Iterator<AddressT> itr = this.addrSet.iterator();
		AddressT lastElement = itr.next();
		
		while(itr.hasNext()) {
		    lastElement=itr.next();
		}		 	
		return lastElement;
	 }
	 
	 public void addAddrAccJSON(String address, JSONObject item, AddressJSON addrObj, ToCSVParser p){
		AddressT presentAddr = this.getCertainAddress(address); 
		boolean containMultiSig = false;
		String primWallet = null;
		if(presentAddr != null){
			containMultiSig = presentAddr.getMulti();
			primWallet = presentAddr.getPrimWAdd();
		}		
		this.addrSet.remove(new Address(address, null, null));
		// no addr entry or no correct addr entry
		if(!(item.has("addr") && item.getString("addr").equals(address))){
			this.add(new AddressT(address, null, null, addrObj, primWallet, containMultiSig), p);
			return;
		}
		if (item.has("addr_tag_link") || item.has("addr_tag")) {
			if (item.has("addr_tag_link") && item.has("addr_tag")) {
				System.out.println(address);
				this.addrSet.remove(new Address(address, item.getString("addr_tag_link"), null));
				this.addrSet.remove(new Address(address, null, item.getString("addr_tag")));
				this.add(new AddressT(address, item.getString("addr_tag_link"), item.getString("addr_tag"), 
						addrObj, primWallet, containMultiSig), p);
			} else if (item.has("addr_tag_link")) {
				System.out.println(address);
				this.add(new AddressT(address, item.getString("addr_tag_link"), null, 
						addrObj, primWallet, containMultiSig), p);
			} else {
				System.out.println(address);
				this.add(new AddressT(address, null, item.getString("addr_tag"), 
						addrObj, primWallet, containMultiSig), p);
			}
		}else{
			this.add(new AddressT(address, null, null, addrObj, primWallet, containMultiSig), p);
		}		 
	 }
	 
	 public AddressT getCertainAddress(String address){
		 for(AddressT a : this.addrSet){
			 if(a.getAddr().equals(address)){
				 return a;
			 }
		 }
		 return null;
	 }

}
