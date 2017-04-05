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
	 
	 public boolean add(AddressT a){
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
	 
	 public void addAddrAccJSON(String address, JSONObject item, AddressJSON addrObj){
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
			this.addrSet.add(new AddressT(address, null, null, addrObj, primWallet, containMultiSig));
			return;
		}
		if (item.has("addr_tag_link") || item.has("addr_tag")) {
			if (item.has("addr_tag_link") && item.has("addr_tag")) {
				System.out.println(address);
				this.addrSet.remove(new Address(address, item.getString("addr_tag_link"), null));
				this.addrSet.remove(new Address(address, null, item.getString("addr_tag")));
				this.addrSet.add(new AddressT(address, item.getString("addr_tag_link"), item.getString("addr_tag"), 
						addrObj, primWallet, containMultiSig));
			} else if (item.has("addr_tag_link")) {
				System.out.println(address);
				this.addrSet.add(new AddressT(address, item.getString("addr_tag_link"), null, 
						addrObj, primWallet, containMultiSig));
			} else {
				System.out.println(address);
				this.addrSet.add(new AddressT(address, null, item.getString("addr_tag"), 
						addrObj, primWallet, containMultiSig));
			}
		}else{
			this.addrSet.add(new AddressT(address, null, null, addrObj, primWallet, containMultiSig));
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
