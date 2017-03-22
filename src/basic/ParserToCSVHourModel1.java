package basic;

import java.io.*;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.SimpleTimeZone;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.net.URL;

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
public class ParserToCSVHourModel1 extends ToCSVParser implements BitCoinExRateGetterHour{	
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
	
	public ParserToCSVHourModel1(String[] datFileNames) throws FileNotFoundException {
		super(datFileNames);
		// define files to be written into
		this.addresses = new PrintWriter(new File("./csvs/addresses.csv"));
		this.addStr = new StringBuilder();
		this.addStr.append("address:ID(Addr),addr_tag_link,addr_tag\n");
		this.transactions = new PrintWriter(new File("./csvs/transactions.csv"));
		this.traStr = new StringBuilder();
		this.traStr.append("tranHashString:ID(Trans),time\n");
		this.inputs = new PrintWriter(new File("./csvs/inputs.csv"));
		this.inputStr = new StringBuilder();
		this.inputStr.append("addr:ID(SendAdd),tranHashString,value_bitcoin,value_dollar,type\n");
		this.outputs = new PrintWriter(new File("./csvs/outputs.csv"));
		this.outputStr = new StringBuilder();
		this.outputStr.append("addr:ID(ReceAdd),tranHashString,value_bitcoin,value_dollar,type\n");

		inTran = new PrintWriter(new File("./csvs/intran.csv"));
		inStr = new StringBuilder();
		inStr.append(":START_ID(SendAdd),:END_ID(Trans)\n");
		outTran = new PrintWriter(new File("./csvs/outtran.csv"));
		outStr = new StringBuilder();
		outStr.append(":START_ID(Trans),:END_ID(ReceAdd)\n");
	}	

	
	/* (non-Javadoc)
	 * @see basic.BitCoinExRateGetterDay#getDollarValDay(java.lang.String, java.lang.String)
	 */
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public double getDollarValHour(String time, String value) throws IOException, JSONException{
		double val = Long.parseLong(value);
		String target = time.split("\\+")[0];
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
	    long timePara = 0;
	    try {
			Date result =  df.parse(target);
			timePara = result.getTime()/1000;	
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	    JSONObject rateJson = readJsonFromUrl("https://winkdex.com/api/v0/price?time=" + timePara);
		double penny = rateJson.getInt("price");	
		return val*BitCoinExRateGetterHour.satToBit*penny/100.0;
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
			if(block.getHashAsString().equals("0000000000000000026806253ad80b75a43ba9937984b5fb6e6826b2297744f2")){
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
				blockJson = readJsonFromUrl("https://api.blocktrail.com/v1/btc/block/" + block.getHashAsString() + "/transactions?api_key=b88ae2fc47fdd1b7fd132ad189734a0c783a4f5f");
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
					String taHash = ta.get("hash").toString();
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
									addrSet.add(new Address(multiAdd.get(k).toString(), null, null));
								}
//								addrSet.add(multiAddList.toString());
								inputStr.append(multiAddList.toString());
								inputStr.append(',');
								inputStr.append(taHash);
								inputStr.append(',');
								inputStr.append(inp.get("value"));
								inputStr.append(',');
								double temp = this.getDollarValHour(ta.get("time").toString(), inp.get("value").toString());
								inputStr.append(temp);								
								inputStr.append(',');
								inputStr.append(inp.get("type"));		
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
								double temp = this.getDollarValHour(ta.get("time").toString(), inp.get("value").toString());
								inputStr.append(temp);								
								inputStr.append(',');
								inputStr.append(inp.get("type"));
								inputStr.append("\n");
								
								// for address, addr_tag_link,addr_tag
								JSONArray inputsArr = tranFromBC.get(taHash).getJSONArray("inputs");	 
								this.getAddTagL(inputsArr, inp.get("address").toString(), true);
								
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
									addrSet.add(new Address(multiAdd.get(k).toString(), null, null));
								}
								outputStr.append(multiAddList.toString());
								outputStr.append(',');
								outputStr.append(taHash);
								outputStr.append(',');
								outputStr.append(outp.get("value"));
								outputStr.append(',');
								double temp = this.getDollarValHour(ta.get("time").toString(), outp.get("value").toString());
								outputStr.append(temp);								
								outputStr.append(',');
								outputStr.append(outp.get("type"));	
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
								double temp = this.getDollarValHour(ta.get("time").toString(), outp.get("value").toString());
								outputStr.append(temp);								
								outputStr.append(',');
								outputStr.append(outp.get("type"));
								outputStr.append("\n");
								
								// for addr_tag_link,addr_tag
								JSONArray outs = tranFromBC.get(taHash).getJSONArray("out");
								super.getAddTagL(outs, outp.get("address").toString(), false);
								
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
	


	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		System.setProperty("http.agent", "Chrome");
		InputStream is = null;
		try{
			is = new URL(url).openStream();			
		}catch(Exception e){
			this.end();
			System.out.println("finish exception (some transaction not fully parsed)!");
			e.printStackTrace();
			System.exit(1);	
		}
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = ToCSVParser.readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
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
		
		for(Address ad : this.addrSet){
			this.addStr.append(ad);
			this.addStr.append("\n");					
		}				
		this.addresses.write(addStr.toString());
		this.addresses.close();
	}
}
