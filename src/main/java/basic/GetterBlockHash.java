package basic;

import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;
import org.json.JSONException;

/**
 * To be run once to write all blockhashs of certain .dat files into a txt file
 * 
 * @author yshi
 *
 */
public class GetterBlockHash {
	public static void main(String[] args) throws JSONException, IOException{
		Context.getOrCreate(MainNetParams.get());
		NetworkParameters np = new MainNetParams();
		PrintWriter blockhash = new PrintWriter(new File("blockhash.txt"));

		//ArrayList<String> strList = new ArrayList<String>(); 
		for(int i = 408; i < 726; i++){
			List<File> blockChainFiles = new ArrayList<File>();
			blockChainFiles.add(new File("C:\\Users\\tsutomu\\AppData\\Roaming\\Bitcoin\\blocks\\" + "blk00"+i+".dat"));			
			BlockFileLoader bfl = new BlockFileLoader(np, blockChainFiles);
			blockhash.println("blk00"+i+".dat");
			System.out.println("blk00"+i+".dat");
			for (Block block : bfl) {
				blockhash.println(block.getHashAsString());
			}
		}
		blockhash.close();
	}
}
