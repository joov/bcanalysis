package basic;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bitcoinj.core.*;
import org.json.*;

import basic.Address;

/**
 * This class will extract data regarding bitcoin transaction
 * from blocktrail api and blockchain api
 * The exchange rate between bitcoin is obtained from a
 * website which has hourly exchange rate available
 * @author yshi
 *
 */
public abstract class ParserToCSVModel2 extends ToCSVParser{	
	private AddressT lastAdded;
	
	HashSet<AddressT> addrSet = new HashSet<AddressT>();
	private PrintWriter addresses;
	private StringBuilder addStr = new StringBuilder();
	
	private PrintWriter transactions;
	private StringBuilder tranStr = new StringBuilder();
	HashSet<Transaction> tranSet = new HashSet<Transaction>();

	private PrintWriter wallet;
	private StringBuilder wallStr = new StringBuilder();
	private ArrayList<Wallet> wallSet = new ArrayList<Wallet>();
	
	public ParserToCSVModel2(ArrayList<String> datFileNames) throws FileNotFoundException {
		super(datFileNames);
		// define files to be written into
		File baseDir = new File(System.getProperty("user.home") + "/csvs");
		if(!baseDir.exists()){
			baseDir.mkdirs();
		}
		this.addresses = new PrintWriter(new File(System.getProperty("user.home") + "/csvs/addresses.csv"));
		this.addStr = new StringBuilder();
		this.addStr.append("addrID,addr_tag_links,addr_tags,time,primWallAdd\n");  

		this.wallet = new PrintWriter(new File(System.getProperty("user.home") + "/csvs/wallet.csv"));
		this.wallStr = new StringBuilder();
		this.wallStr.append("primAddress,time\n");
		
		this.transactions = new PrintWriter(new File(System.getProperty("user.home") + "/csvs/transactionRelation.csv"));
		this.tranStr = new StringBuilder();
		this.tranStr.append("sendWallet,receWallet,tranHashString,time,value_bitcoin,value_dollar,type,estChanAddr\n");
	}
	
	
	/* (non-Javadoc)
	 * @see basic.ToCSVParser#parse()
	 */
	public void parse() throws JSONException, IOException{
		// Iterate over the blocks in the dataset.
		long time = System.currentTimeMillis();
		boolean readBl = false;
		for (Block block : bfl) {
			//comment out if not needed (i.e. when starting from the first block of a file)
			// fill in the last second blockhash printed
			if(block.getHashAsString().equals("000000000000000002b23542736b86e8b4616ae7876b87a05c369d408486c80b")){
				readBl = true;
				continue;
			}else if(!readBl){
				continue;
			}
			System.out.println(block.getHashAsString());
			System.out.println(System.currentTimeMillis() - time);
			time = System.currentTimeMillis();

			JSONObject blockJson = null;
			JSONObject blockAltJson = null;
			Map<Object, JSONObject> tranFromBC = new HashMap<Object, JSONObject>(); //transaction information from blockchain.info
			try{
				blockJson = readJsonFromUrl("https://api.blocktrail.com/v1/btc/block/" + block.getHashAsString() + "/transactions?api_key=" + Util.apiKey);
				blockAltJson = readJsonFromUrl("https://blockchain.info/rawblock/" + block.getHashAsString());
				JSONArray transAlt = blockAltJson.getJSONArray("tx");
				for(int i = 0; i < transAlt.length(); i ++){
					JSONObject ta = transAlt.getJSONObject(i);
					tranFromBC.put(ta.get("hash"), ta);
				}
				System.out.println("JSON obtained!");
			}catch(java.net.SocketException se){
				this.end();
				System.out.println("finish exception!");
				System.exit(1);
			}
			//System.out.println(blockJson);
			JSONArray tas = blockJson.getJSONArray("data");
			for(int i = 0; i < tas.length(); i ++){
				JSONObject ta = tas.getJSONObject(i);
				if(!ta.get("is_coinbase").toString().equals("true")){
					String taHash = ta.getString("hash");
					//input addr:ID(SendAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray inps = ta.getJSONArray("inputs");
					ArrayList<AddressT> inputAddrsTa = new ArrayList<AddressT>();
					for(int j = 0; j < inps.length(); j ++){
						JSONObject inp = inps.getJSONObject(j);
						if(!inp.get("type").equals("op_return")){
							if(inp.get("type").equals("multisig")){
								JSONArray multiAdd = inp.getJSONArray("multisig_addresses");
								for(int k = 0; k < multiAdd.length(); k++){
									JSONObject addrObj = null;
									try{
										addrObj = this.readJsonFromUrl("https://api.blocktrail.com/v1/btc/address/" + multiAdd.get(k) +"?api_key="+ Util.apiKey);
									}catch(java.net.SocketException se){
										this.end();
										System.out.println("finish exception when getting address time!");
										System.exit(1);;
									}
									String addrTime = addrObj.getString("first_seen");
									AddressT addrToAdd = new AddressT(multiAdd.get(k).toString(), null, null, null, addrTime, true);
									inputAddrsTa.add(addrToAdd);
									if(!this.addrSet.add(addrToAdd)){
										this.addrSet.remove(addrToAdd);
										this.addrSet.add(addrToAdd);										
									}
								}								
							}else if(inp.has("address") && inp.get("address") != null){
								// for address, addr_tag_link,addr_tag
								JSONArray inputsArr = tranFromBC.get(taHash).getJSONArray("inputs");	 
								this.getAddTagL(inputsArr, inp.getString("address"), true);	
								inputAddrsTa.add(this.lastAdded);
							}
						}
					}					
					// Define the input wallet for this transaction, on the assumption that
					// all the address involved in the inputs of the same transaction belongs to
					// the same wallet.
					Wallet inputWall = null;
					// check if the sender receiver wallet is already in this.wallSet
					boolean senderWPresent = false; 				
					for(Wallet w : this.wallSet){
						for(AddressT sender : inputAddrsTa){
							if(w.contain(sender)){
								senderWPresent = true;
								w.add(inputAddrsTa);
								inputWall = w;
								break;
							}				
						}	
					}
					
					if(!senderWPresent ){
						AddressT earliest = inputAddrsTa.get(0);
						for(int s = 1; s < inputAddrsTa.size(); s ++){
							if(earliest.getTimestamp() > inputAddrsTa.get(s).getTimestamp()){
								earliest = inputAddrsTa.get(s);
							}
						}			
						inputWall = new Wallet(earliest);
						inputWall.add(inputAddrsTa);
						this.wallSet.add(inputWall);
					}
					
					String taTime = ta.getString("time");
					//System.out.println(taTime);
					//output addr:ID(ReceAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray outps = ta.getJSONArray("outputs");

					for(int j = 0; j < outps.length(); j ++){
						JSONObject outp = outps.getJSONObject(j);
						if(!outp.get("type").equals("op_return")){
							if(outp.get("type").equals("multisig")){
								ArrayList<AddressT> multiSigAdd = new ArrayList<AddressT>();
								JSONArray multiAdd = outp.getJSONArray("multisig_addresses");
								for(int k = 0; k < multiAdd.length(); k++){
									JSONObject addrObj = null;
									try{
										addrObj = this.readJsonFromUrl("https://api.blocktrail.com/v1/btc/address/" + multiAdd.get(k) +"?api_key="+ Util.apiKey);
									}catch(java.net.SocketException se){
										this.end();
										System.out.println("finish exception when getting address time!");
										System.exit(1);;
									}
									String addrTime = addrObj.getString("first_seen");
									AddressT addrToAdd = new AddressT(multiAdd.get(k).toString(), null, null, null, addrTime, true);
									multiSigAdd.add(addrToAdd);
									if(!this.addrSet.add(addrToAdd)){
										this.addrSet.remove(addrToAdd);
										this.addrSet.add(addrToAdd);										
									}
								}
								boolean includedInSenderWallet = false;
								String estChanAddr = ta.get("estimated_change_address").toString();

								for(AddressT multiSig : multiSigAdd){
									if(inputAddrsTa.contains(multiSig)){
										includedInSenderWallet = true;
										break;
									}
									if(estChanAddr.equals(multiSig.getAddr())){
										includedInSenderWallet = true;
										break;
									}
								}
								if(!includedInSenderWallet){
									long bitVal = outp.getLong("value");
									double dollVal = this.getDollarValDayorHour(ta.get("time").toString(), outp.get("value").toString());
									String outputType = outp.getString("type") + ';' + outp.getString("multisig");	
									Transaction taToAdd = new Transaction(taHash, this.wallSet, inputWall, multiSigAdd, 
											bitVal, dollVal, taTime, outputType, estChanAddr, this);
									this.tranSet.add(taToAdd);
									
								}else{
									inputWall.add(multiSigAdd);
								}
							}else if(outp.has("address") && outp.get("address") != null){
								String outAddr = outp.getString("address");
								boolean includedInSenderWallet = false;
								String estChanAddr = ta.get("estimated_change_address").toString();

								if(inputAddrsTa.contains(outAddr)){
									includedInSenderWallet = true;
								}
								if(estChanAddr.equals(outAddr)){
									includedInSenderWallet = true;
								}
								
								// for addr_tag_link,addr_tag
								JSONArray outs = tranFromBC.get(taHash).getJSONArray("out");
								this.getAddTagL(outs, outp.get("address").toString(), false);
								
								if(!includedInSenderWallet){
									long bitVal = outp.getLong("value");
									double dollVal = this.getDollarValDayorHour(ta.get("time").toString(), outp.get("value").toString());
									String outputType = outp.getString("type");	
									Transaction taToAdd = new Transaction(taHash, this.wallSet, inputWall, lastAdded, 
											bitVal, dollVal, taTime, outputType, estChanAddr, this);
									this.tranSet.add(taToAdd);						
								}else{
									inputWall.add(this.lastAdded);
								}								
							}
						}
					}	
				//for all the addresses contained in a inputwallet, set the attribute primWAdd to the corresponding wallet
					HashSet<AddressT> inputWallAdds = inputWall.getAddrSet();
					for(AddressT a : inputWallAdds){
						a.setWallet(inputWall.getPrimAdd());
					}
				}else{
					continue;
				}
			}
			
		}
		this.end();
		System.out.println("finish!");
	}
	
	protected void getAddTagL(JSONArray xputs, String address, boolean isInput) throws JSONException, IOException{
		JSONObject item = null;
		if(isInput){
			for(int i = 0; i < xputs.length(); i ++){
				JSONObject input = xputs.getJSONObject(i);
				if(input.has("prev_out")){
					item = input.getJSONObject("prev_out");
					if(item.has("addr") && item.getString("addr").equals(address)){
						break;
					}else{
						continue;
					}
				}
			}			
		}else{
			for(int i = 0; i < xputs.length(); i ++){
				item = xputs.getJSONObject(i);
				if(item.has("addr") && item.getString("addr").equals(address)){
					break;
				}else{
					continue;
				}
			}		
		}
		
		this.addrSet.remove(new Address(address, null, null));
		JSONObject addrObj = null;
		try{
			addrObj = this.readJsonFromUrl("https://api.blocktrail.com/v1/btc/address/" + address +"?api_key="+ Util.apiKey);
		}catch(java.net.SocketException se){
			this.end();
			System.out.println("finish exception when getting address time!");
			System.exit(1);;
		}
		String time = addrObj.getString("first_seen");
		
		// no addr entry or no correct addr entry
		if(!(item.has("addr") && item.get("addr").toString().equals(address))){
			this.lastAdded = new AddressT(address, null, null, null, time, false);
			this.addrSet.add(this.lastAdded);
			return;
		}
		if (item.has("addr_tag_link") || item.has("addr_tag")) {
			if (item.has("addr_tag_link") && item.has("addr_tag")) {
				System.out.println(address);
				this.addrSet.remove(new AddressT(address, item.getString("addr_tag_link"), null, null, time, false));
				this.addrSet.remove(new AddressT(address, null, item.getString("addr_tag"), null, time, false));
				this.lastAdded = new AddressT(address, item.getString("addr_tag_link"), item.getString("addr_tag"), null, time, false);
				this.addrSet.add(this.lastAdded);
			} else if (item.has("addr_tag_link")) {
				System.out.println(address);
				this.lastAdded = new AddressT(address, item.getString("addr_tag_link"), null, null, time, false);
				this.addrSet.add(this.lastAdded);
			} else {
				System.out.println(address);
				this.lastAdded = new AddressT(address, null, item.getString("addr_tag"), null, time, false);
				this.addrSet.add(this.lastAdded);
			}
		}else{
			this.lastAdded = new AddressT(address, null, null, null, time, false);
			this.addrSet.add(this.lastAdded);						
		}
	}
	
	protected void end(){
		
		for(AddressT ad : this.addrSet){
			this.addStr.append(ad);
			this.addStr.append("\n");					
		}				
		this.addresses.write(this.addStr.toString());
		this.addresses.close();
		
		for(Transaction tran : this.tranSet){
			this.tranStr.append(tran);
			this.tranStr.append("\n");
		}
		this.transactions.write(this.tranStr.toString());
		this.transactions.close();

		for(Wallet wall : this.wallSet){
			this.wallStr.append(wall);
			this.wallStr.append("\n");
		}
		this.wallet.write(this.wallStr.toString());
		this.wallet.close();
	}
	
	protected void addToWallList(Wallet toAdd){
		this.wallSet.add(toAdd);
	}
	protected void removeFromWallList(Wallet toRe){
		this.wallSet.add(toRe);
	}
}
