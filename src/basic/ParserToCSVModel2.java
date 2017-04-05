package basic;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

//import org.bitcoinj.core.*;
import org.json.*;


/**
 * This class will extract data regarding bitcoin transaction
 * from blocktrail api and blockchain api
 * The exchange rate between bitcoin is obtained from a
 * website which has hourly exchange rate available
 * @author yshi
 *
 */
public abstract class ParserToCSVModel2 extends ToCSVParser{	
	
	private PrintWriter addresses;
	private StringBuilder addStr = new StringBuilder();
	
	private PrintWriter transactions;
	private StringBuilder tranStr = new StringBuilder();

	private PrintWriter wallet;
	private StringBuilder wallStr = new StringBuilder();
	
	private String lastTranHashFromBefore;   // the hash of the transaction which has been finished parsing
	private String currentParsingTranHash;  // the hash of the transaction is currently been parsed, could be from before
	
	public ParserToCSVModel2(int numBlock, boolean begin, String lastBlockHashFromBefore, 
			String lastTranHashFromBefore, String currentParsingTranHash, String lastAddrFromBefore, int folderCounter) throws FileNotFoundException {
		super(numBlock, begin, lastBlockHashFromBefore, lastAddrFromBefore, folderCounter);
		this.currentParsingTranHash = currentParsingTranHash;
		this.lastAddrFromBefore = lastAddrFromBefore;
		// define files to be written into
		String folderPath = Util.path + this.folderCounter;
		File baseDir = new File(System.getProperty("user.home") + folderPath );
		if(!baseDir.exists()){
			baseDir.mkdirs();
		}
		this.addresses = new PrintWriter(new File(System.getProperty("user.home") + folderPath + "/addresses.csv"));
		this.addStr = new StringBuilder();
		this.addStr.append("addrID,addr_tag_links,addr_tags,firstSeen,lastSeen,primWallAdd,multiExist\n");  

		this.wallet = new PrintWriter(new File(System.getProperty("user.home") + folderPath + "/wallet.csv"));
		this.wallStr = new StringBuilder();
		this.wallStr.append("primAddress,firstSeenTime,lastSeenTime,numAddress\n");
		
		this.transactions = new PrintWriter(new File(System.getProperty("user.home") + folderPath + "/transactionRelation.csv"));
		this.tranStr = new StringBuilder();
		this.tranStr.append("sendWallet,receWallet,tranHashString,time,value_bitcoin,value_dollar,type,estChanAddr\n");
	}
	
	
	
	/* (non-Javadoc)
	 * @see basic.ToCSVParser#parse()
	 */
	public void parse() throws JSONException, IOException{
		boolean justStarted1 = true;
		boolean justStarted2 = true;

		boolean startAddrP = Util.equalsWithNulls(this.lastTranHashFromBefore, this.currentParsingTranHash);
		
		// Iterate over the blocks in the dataset.
		blockLoop: for (String block : this.blocklists) {
			System.out.println(block);
			JSONObject blockJson = null;
			JSONObject blockAltJson = null;
			Map<Object, JSONObject> tranFromBC = new HashMap<Object, JSONObject>(); //transaction information from blockchain.info
			try{
				blockJson = readJsonFromUrl("https://api.blocktrail.com/v1/btc/block/" + block + "/transactions?api_key=" + Util.apiKey);
				blockAltJson = readJsonFromUrl("https://blockchain.info/rawblock/" + block);
				JSONArray transAlt = blockAltJson.getJSONArray("tx");
				for(int i = 0; i < transAlt.length(); i ++){
					JSONObject ta = transAlt.getJSONObject(i);
					tranFromBC.put(ta.get("hash"), ta);
				}
				System.out.println("JSON obtained!");
			}catch(Exception se){
				this.end();
				System.out.println("finish exception!");
				se.printStackTrace();
//				System.exit(1);
			}
			//System.out.println(blockJson);
			
			JSONArray tas = blockJson.getJSONArray("data");
			transactionLoop: for(int i = 0; i < tas.length(); i ++){
				JSONObject ta = tas.getJSONObject(i);
				if(justStarted1){
					justStarted1 = false;
					if(this.lastTranHashFromBefore != null){	
						while(i < tas.length()){
							ta = tas.getJSONObject(i);
							if(ta.getString("hash").equals(this.lastTranHashFromBefore)){
								i++;
								if(i < tas.length()){
									ta = tas.getJSONObject(i);
									break;									
								}else{
									justStarted2 = false;
									this.lastTranHashFromBefore = ta.getString("hash");
									continue blockLoop;
								}

							}else{
								i++;
								continue;
							}
						}
						if(i == tas.length()){
							this.lastTranHashFromBefore = null;
							i = 0;
							ta = tas.getJSONObject(i);
						}
					}else{
						System.out.println("this.lastTranHashFromBefore == null");
						ta = tas.getJSONObject(i);
					}
				}
				
				if(!ta.get("is_coinbase").toString().equals("true")){
					String taHash = ta.getString("hash");
					this.currentParsingTranHash = taHash;
					//input addr:ID(SendAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray inps = ta.getJSONArray("inputs");
					ArrayList<AddressT> inputAddrsTa = new ArrayList<AddressT>();
					for(int j = 0; j < inps.length(); j ++){
						JSONObject inp = inps.getJSONObject(j);
						if(!inp.get("type").equals("op_return")){
							if(inp.get("type").equals("multisig")){
								JSONArray multiAdd = inp.getJSONArray("multisig_addresses");
								for(int k = 0; k < multiAdd.length(); k++){
									AddressT addrToAdd= this.addrSet.getCertainAddress(multiAdd.get(k).toString());
									if(addrToAdd == null){
										AddressJSON addrJ = this.getAddrJSON(multiAdd.get(k).toString());
										addrToAdd = new AddressT(multiAdd.get(k).toString(), null, null, addrJ, null, true);	
									}
									if(!this.addrSet.add(addrToAdd, this)){
										if(!addrToAdd.getMulti()){
											this.addrSet.remove(addrToAdd);
											addrToAdd.setMultiTrue();
											this.addrSet.add(addrToAdd, this);
										}
									}
									inputAddrsTa.add(addrToAdd);
									this.addrSet.add(addrToAdd, this);
								}								
							}else if(inp.has("address") && inp.get("address") != null && inp.getLong("value") != 0){
								// for addresses which might have addr_tag_link,addr_tag
								JSONArray inputsArr = tranFromBC.get(taHash).getJSONArray("inputs");	 
								this.addAddrWithTagL(inputsArr, inp.getString("address"), true);	
								inputAddrsTa.add(this.addrSet.getLastAdded());
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
						inputWall = new Wallet(inputAddrsTa);
						this.wallSet.add(inputWall);
					}
					
					String taTime = ta.getString("time");
					//System.out.println(taTime);
					//output addr:ID(ReceAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray outps = ta.getJSONArray("outputs");

					for(int j = 0; j < outps.length(); j ++){
						JSONObject outp = outps.getJSONObject(j);
						
						if(justStarted2){
							justStarted2 = false;
							if(!startAddrP){
								while(j < outps.length()){
									outp = outps.getJSONObject(j);
									if(outp.getString("address").equals(this.lastAddrFromBefore)){
										startAddrP = true;
										j++;
										if(j < outps.length()){
											outp = outps.getJSONObject(j);
											break;								
										}else{
											this.lastAddrFromBefore = outp.getString("address");
											continue transactionLoop;
										}

									}else{
										j++;
										continue;
									}
								}	
								if(j == outps.length()){  //no address from outputs of the current ta has been parsed
									startAddrP = true;
									j = 0;
									outp = outps.getJSONObject(j);
								}							
							}else{
								outp = outps.getJSONObject(j);
							}
						}

						if(!outp.get("type").equals("op_return")){
							if(outp.get("type").equals("multisig")){
								ArrayList<AddressT> multiSigAdd = new ArrayList<AddressT>();
								JSONArray multiAdd = outp.getJSONArray("multisig_addresses");
								for(int k = 0; k < multiAdd.length(); k++){
									AddressT addrToAdd= this.addrSet.getCertainAddress(multiAdd.get(k).toString());
									if(addrToAdd == null){
										AddressJSON addrJ = this.getAddrJSON(multiAdd.get(k).toString());
										addrToAdd = new AddressT(multiAdd.get(k).toString(), null, null, addrJ, null, true);	
									}
									if(!this.addrSet.add(addrToAdd, this)){
										if(!addrToAdd.getMulti()){
											this.addrSet.remove(addrToAdd);
											addrToAdd.setMultiTrue();
											this.addrSet.add(addrToAdd, this);
										}
									}
									multiSigAdd.add(addrToAdd);
									this.addrSet.add(addrToAdd, this);
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
								
							}else if(outp.has("address") && outp.get("address") != null && outp.getLong("value") != 0){
								
								String outAddr = outp.getString("address");
								boolean includedInSenderWallet = false;
								String estChanAddr = ta.get("estimated_change_address").toString();
								for(AddressT a: inputAddrsTa){
									if(a.getAddr().equals(outAddr)){
										includedInSenderWallet = true;
									}
									if(estChanAddr.equals(outAddr)){
										includedInSenderWallet = true;
									}									
								}

								
								// for addr_tag_link,addr_tag
								JSONArray outs = tranFromBC.get(taHash).getJSONArray("out");
								this.addAddrWithTagL(outs, outp.get("address").toString(), false);
								
								if(!includedInSenderWallet){
									long bitVal = outp.getLong("value");
									double dollVal = this.getDollarValDayorHour(ta.get("time").toString(), outp.get("value").toString());
									String outputType = outp.getString("type");	
									Transaction taToAdd = new Transaction(taHash, this.wallSet, inputWall, this.addrSet.getLastAdded(), 
											bitVal, dollVal, taTime, outputType, estChanAddr, this);
									this.tranSet.add(taToAdd);						
								}else{
									inputWall.add(this.addrSet.getLastAdded());
								}								
							}
						}
					}	
				//for all the addresses contained in a inputwallet, set the attribute primWAdd to the corresponding wallet
					HashSet<AddressT> inputWallAdds = inputWall.getAddrSet();
					for(AddressT a : inputWallAdds){
						a.setWallet(inputWall.getPrimAdd());
					}
					this.lastTranHashFromBefore = taHash;
				}else{
					continue;
				}
			}
			this.lastBlockHashFromBefore = block;
		}
		this.end();
		System.out.println("finish!");
	}
	
	protected void end(){	
		Main3.lastBlockHash = this.lastBlockHashFromBefore;
		Main3.lastTranHash = this.lastTranHashFromBefore;
		Main3.currTranHash = this.currentParsingTranHash;
		Main3.lastAddr = this.lastAddrFromBefore;
		LinkedHashSet<AddressT> finalAddresses = this.addrSet.getAddrSet();
		for(AddressT ad : finalAddresses){
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
		System.out.println("Last Tran Hash: " + this.lastTranHashFromBefore);
		System.out.println("Last Block Hash: " + this.lastBlockHashFromBefore);
		System.out.println("Current Tran Hash: " + this.currentParsingTranHash);
		System.out.println("Last Address: " + this.lastAddrFromBefore);
	}

}