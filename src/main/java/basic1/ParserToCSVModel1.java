package basic1;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.bitcoinj.core.*;
import org.json.*;

import basic.Util;
import basic.AddressOT;
import basic.Main2;


/**
 * This class will is a super class for the classes which extract 
 * data of bitcoin transaction from blocktrail api and blockchain api
 * according to Model 1.
 * @author yshi
 *
 */
public abstract class ParserToCSVModel1 extends ToCSVParser{	
	private PrintWriter addresses ;
	private StringBuilder addStr = new StringBuilder();
	private PrintWriter transactions ;
	private StringBuilder traStr = new StringBuilder();
	private PrintWriter inputs ;
	private StringBuilder inputStr = new StringBuilder();
	private PrintWriter outputs ;
	private StringBuilder outputStr = new StringBuilder();

	private PrintWriter inTran ;
	private StringBuilder inStr = new StringBuilder();
	private PrintWriter outTran ;
	private StringBuilder outStr = new StringBuilder();
	
	public ParserToCSVModel1(ArrayList<String> datFileNames) throws FileNotFoundException {
		super(datFileNames);
		// define files to be written into
		File baseDir = new File(Main2.getProperties().getProperty("data.dir",System.getProperty("user.home") + "/csvs"));
		if(!baseDir.exists()){
			baseDir.mkdirs();
		}
		this.addresses = new PrintWriter(new File(baseDir + "/addresses.csv"));
		this.addStr = new StringBuilder();
		this.addStr.append("address:ID(Addr),addr_tag_link,addr_tag,time\n");
		this.transactions = new PrintWriter(new File(baseDir + "/transactions.csv"));
		this.traStr = new StringBuilder();
		this.traStr.append("tranHashString:ID(Trans),time\n");
		this.inputs = new PrintWriter(new File(baseDir + "/inputs.csv"));
		this.inputStr = new StringBuilder();
		this.inputStr.append("addr:ID(SendAdd),tranHashString,value_bitcoin,value_dollar,type\n");
		this.outputs = new PrintWriter(new File(baseDir + "/outputs.csv"));
		this.outputStr = new StringBuilder();
		this.outputStr.append("addr:ID(ReceAdd),tranHashString,value_bitcoin,value_dollar,type\n");

		inTran = new PrintWriter(new File(baseDir + "/intran.csv"));
		inStr = new StringBuilder();
		inStr.append(":START_ID(SendAdd),:END_ID(Trans)\n");
		outTran = new PrintWriter(new File(baseDir + "/outtran.csv"));
		outStr = new StringBuilder();
		outStr.append(":START_ID(Trans),:END_ID(ReceAdd)\n");
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
//			if(block.getHashAsString().equals("0000000000000000026806253ad80b75a43ba9937984b5fb6e6826b2297744f2")){
//				readBl = true;
//				continue;
//			}else if(!readBl){
//				continue;
//			}
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
				System.exit(1);;
			}
			//System.out.println(blockJson);
			JSONArray tas = blockJson.getJSONArray("data");
			for(int i = 0; i < tas.length(); i ++){
				JSONObject ta = tas.getJSONObject(i);
				if(!ta.getString("is_coinbase").equals("true")){
					String taHash = ta.getString("hash");
					traStr.append(taHash);
					traStr.append(",");
					traStr.append(ta.get("time"));				
					traStr.append("\n");
					//input addr:ID(SendAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray inps = ta.getJSONArray("inputs");
					for(int j = 0; j < inps.length(); j ++){
						JSONObject inp = inps.getJSONObject(j);
						if(!inp.get("type").equals("op_return")){
							if(inp.get("type").equals("multisig")){
								JSONArray multiAdd = inp.getJSONArray("multisig_addresses");
								StringBuilder multiAddList = new StringBuilder();
								for(int k = 0; k < multiAdd.length(); k++){
									multiAddList.append(multiAdd.get(k));
									multiAddList.append(';');
									JSONObject addrObj = null;
									try{
										addrObj = this.readJsonFromUrl("https://api.blocktrail.com/v1/btc/address/" + multiAdd.get(k) +"?api_key="+ Util.apiKey);
									}catch(java.net.SocketException se){
										this.end();
										System.out.println("finish exception when getting address time!");
										System.exit(1);;
									}
									String addrTime = addrObj.getString("first_seen");
									addrSet.add(new AddressOT(multiAdd.get(k).toString(), null, null, addrTime));
								}
//								addrSet.add(multiAddList.toString());
								inputStr.append(multiAddList.toString());
								inputStr.append(',');
								inputStr.append(taHash);
								inputStr.append(',');
								inputStr.append(inp.get("value"));
								inputStr.append(',');
								double temp = this.getDollarValDayorHour(ta.getString("time"), inp.getString("value"));
								inputStr.append(temp);								
								inputStr.append(',');
								inputStr.append(inp.get("type"));
								inputStr.append(";");		
								inputStr.append(inp.get("multisig"));
								inputStr.append("\n");		
								
								//inStr :START_ID(SendAdd),:END_ID(Trans)
								inStr.append(multiAddList.toString());
								inStr.append(',');
								inStr.append(taHash);								
								inStr.append("\n");								
							}else if(inp.has("address") && inp.get("address") != null){
								inputStr.append(inp.get("address"));
								inputStr.append(',');
								inputStr.append(taHash);
								inputStr.append(',');
								inputStr.append(inp.get("value"));
								inputStr.append(',');
								double temp = this.getDollarValDayorHour(ta.getString("time"), inp.getString("value"));
								inputStr.append(temp);								
								inputStr.append(',');
								inputStr.append(inp.get("type"));
								inputStr.append("\n");
								
								// for address, addr_tag_link,addr_tag
								JSONArray inputsArr = tranFromBC.get(taHash).getJSONArray("inputs");	 
								this.getAddTagL(inputsArr, inp.getString("address"), true);
								
								//inStr :START_ID(SendAdd),:END_ID(Trans)
								inStr.append(inp.get("address"));
								inStr.append(',');
								inStr.append(taHash);								
								inStr.append("\n");	
							}
						}
					}	
					
					//output addr:ID(ReceAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray outps = ta.getJSONArray("outputs");
					for(int j = 0; j < outps.length(); j ++){
						JSONObject outp = outps.getJSONObject(j);
						if(!outp.get("type").equals("op_return")){
							if(outp.get("type").equals("multisig")){
								JSONArray multiAdd = outp.getJSONArray("multisig_addresses");
								StringBuilder multiAddList = new StringBuilder();
								for(int k = 0; k < multiAdd.length(); k++){
									multiAddList.append(multiAdd.get(k));
									multiAddList.append(';');
									JSONObject addrObj = null;
									try{
										addrObj = this.readJsonFromUrl("https://api.blocktrail.com/v1/btc/address/" + multiAdd.get(k) +"?api_key="+ Util.apiKey);
									}catch(java.net.SocketException se){
										this.end();
										System.out.println("finish exception when getting address time!");
										System.exit(1);;
									}
									String addrTime = addrObj.getString("first_seen");
									addrSet.add(new AddressOT(multiAdd.get(k).toString(), null, null, addrTime));
								}
								outputStr.append(multiAddList.toString());
								outputStr.append(',');
								outputStr.append(taHash);
								outputStr.append(',');
								outputStr.append(outp.get("value"));
								outputStr.append(',');
								double temp = this.getDollarValDayorHour(ta.getString("time"), outp.getString("value"));
								outputStr.append(temp);								
								outputStr.append(',');
								outputStr.append(outp.get("type"));	
								outputStr.append(";");		
								outputStr.append(outp.get("multisig"));	
								outputStr.append("\n");		
								
								//outStr :START_ID(Trans),:END_ID(ReceAdd)
								outStr.append(taHash);
								outStr.append(',');
								outStr.append(multiAddList.toString());
								outStr.append("\n");
							}else if(outp.has("address") && outp.get("address") != null){
								outputStr.append(outp.get("address"));
								outputStr.append(',');
								outputStr.append(taHash);
								outputStr.append(',');
								outputStr.append(outp.get("value"));
								outputStr.append(',');
								double temp = this.getDollarValDayorHour(ta.getString("time"), outp.getString("value"));
								outputStr.append(temp);								
								outputStr.append(',');
								outputStr.append(outp.get("type"));
								outputStr.append("\n");
								
								// for addr_tag_link,addr_tag
								JSONArray outs = tranFromBC.get(taHash).getJSONArray("out");
								super.getAddTagL(outs, outp.getString("address"), false);
								
								//outStr :START_ID(Trans),:END_ID(ReceAdd)
								outStr.append(taHash);
								outStr.append(',');
								outStr.append(outp.get("address"));
								outStr.append("\n");
							}
						}
					}	
				
				
				}else{
					continue;
				}
			}
			
		}
		this.end();
		System.out.println("finish!");
	}
	
	
	protected void end(){
		this.transactions.write(traStr.toString());
		this.transactions.close();
		this.inputs.write(inputStr.toString());
		this.inputs.close();
		this.outputs.write(outputStr.toString());
		this.outputs.close();
		this.inTran.write(inStr.toString());
		this.inTran.close();
		this.outTran.write(outStr.toString());
		this.outTran.close();
		
		for(AddressOT ad : this.addrSet){
			this.addStr.append(ad);
			this.addStr.append("\n");					
		}				
		this.addresses.write(addStr.toString());
		this.addresses.close();
	}
}
