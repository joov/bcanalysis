package basicANeo4j;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

//import org.neo4j.cypher.javacompat.ExecutionEngine;
//import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import org.neo4j.kernel.impl.util.FileUtils;
//import org.neo4j.kernel.impl.util.*;
import java.util.logging.Logger;


public class Importer {
	private static String path = "file:///C:/csvsToImport/csvs";
	private int num;
	
	private boolean headerPresent(String filePath) throws FileNotFoundException{
        Scanner scanner = new Scanner(new File(filePath));
        String firstLine = scanner.nextLine();
        scanner.close();
        
		if(filePath.contains("address")){
			System.out.println("address! " + this.num + " " + firstLine.contains("addr_tag_links"));
			return firstLine.contains("addr_tag_links");
		}else if (filePath.contains("transaction")){
			System.out.println("transaction! "  + this.num + " " + firstLine.contains("value_bitcoin"));
			return firstLine.contains("value_bitcoin");
		}else{
			System.out.println("wallet! "  + this.num + " " + firstLine.contains("primAddress,firstSeenTime"));
			return firstLine.contains("primAddress,firstSeenTime");
		}
	}
	
	public static boolean isAllFileCSV(String mainDir, int num) throws FileNotFoundException{
		String mainFolder = mainDir + num;
		String walletFile = mainFolder+ "\\addresses.csv";
		String addressFile = mainFolder+ "\\wallet.csv";
		String tranFile = mainFolder+ "\\transactionRelation.csv";
        Scanner scannerWall = new Scanner(new File(walletFile));
        String firstLineWall = scannerWall.nextLine();
        scannerWall.close();
        Scanner scannerAddr = new Scanner(new File(addressFile));
        String firstLineAddr = scannerAddr.nextLine();
        scannerAddr.close();
        Scanner scannerTran = null;
        if(new File(tranFile).exists()){
        	scannerTran = new Scanner(new File(tranFile));
        }else{
        	scannerTran = new Scanner(new File(mainFolder+ "\\transactionRelation1.csv"));        	
        }
        String firstLineTran = scannerTran.nextLine();
        scannerTran.close();  
		if(firstLineAddr.contains(",") && firstLineWall.contains(",") && firstLineTran.contains(",")){
			return true;
		}else{
			System.out.println("-----------------------------------------------gytyfty");
			Logger.getLogger("InfoLogging").info("Not csv files " + num);
		}
		return false;
		
	}
	
	
	public Importer(int num, boolean withOutputIndex, Session session, String mainDir) throws FileNotFoundException{
		this.num = num;
		
		//import csvs and create corresponding nodes
		String folderPath = Importer.path + num + "/";
		System.out.println(folderPath);

		
		String queryAddr = null;
		if(this.headerPresent(mainDir + num + "\\addresses.csv")){
			queryAddr = "LOAD CSV From '" + folderPath + "addresses.csv' AS line"
					+ " WITH line"
					+ " SKIP 1";			
		}else{
			queryAddr = "LOAD CSV From '" + folderPath + "addresses.csv' AS line";		
		}
		String queryWallet = null;
		if(this.headerPresent(mainDir + num + "\\wallet.csv")){
			queryWallet = "LOAD CSV From '" + folderPath + "wallet.csv' AS line"
					+ " WITH line"
					+ " SKIP 1";			
		}else{
			queryWallet = "LOAD CSV From '" + folderPath + "wallet.csv' AS line";
		}

		File tranFile = new File(mainDir + num + "\\transactionRelation.csv");
		String queryTran = null;
		System.out.println(tranFile);
		System.out.println(tranFile.exists());
		if(tranFile.exists()){
			if(this.headerPresent(mainDir + num + "\\transactionRelation.csv")){
				queryTran = "LOAD CSV From '" + folderPath + "transactionRelation.csv' AS line"
						+ " WITH line"
						+ " SKIP 1";					
			}else{
				queryTran = "LOAD CSV From '" + folderPath +  "transactionRelation.csv' AS line";					
			}
		}else{
			if(this.headerPresent(mainDir + num + "\\transactionRelation1.csv")){
				queryTran = "LOAD CSV From '" + folderPath +  "transactionRelation1.csv' AS line"
						+ " WITH line"
						+ " SKIP 1";					
			}else{
				queryTran = "LOAD CSV From '" + folderPath + "transactionRelation1.csv' AS line";					
			}
		}

//		StatementResult sR = session.run(queryAddr + " MERGE (:Address { AddId:line[0], addr_tag_link:line[1], addr_tag:line[2],"
//				+ "first_seen:line[3], last_seen:line[4], primWallAddr:line[5], multiExist:apoc.convert.toBoolean(line[6])," //(case line[6] when '' then true else false end)
//				+ "uniqueReferenceAddr:(line[0] + line[1] + line[2] + line[5] + line[6])})");

		StatementResult sR = session.run(queryAddr + " MERGE (a:Address {uniqueReferenceAddr:(line[0] + line[1] + line[2] + line[5] + line[6])})"
				+ " ON CREATE SET a.AddId=line[0], a.addr_tag_link=line[1], a.addr_tag=line[2],"
				+ "a.first_seen=line[3], a.last_seen=line[4], a.primWallAddr=line[5], a.multiExist=apoc.convert.toBoolean(line[6])" 
				);
		
		System.out.println("Address nodes created");
		sR = session.run("MATCH (n:Address) "
				+ " WHERE n.primWallAddr='null' OR n.primWallAddr IS NULL"
				+ " WITH n"
				+ " DELETE n");
		System.out.println("Delete some addr nodes just created");

		sR = session.run(queryWallet 
				+ " MERGE (w:Wallet { primWallAddr:line[0]})"
				+ " ON CREATE SET w.first_seen = line[1], w.last_seen=line[2]");
		
		System.out.println("Wallet nodes created");
		
		sR = session.run("MATCH (addr:Address), (wa:Wallet) WHERE addr.primWallAddr=wa.primWallAddr"
		+ " MERGE (addr)-[r:BelongTo{uniqueReferenceBelongTo:(addr.AddId + wa.primWallAddr)}]->(wa)"
		+ " ON CREATE SET r.primWallAddr = wa.primWallAddr,"
		+ " wa.first_seen =  CASE WHEN apoc.date.parse(wa.first_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") < apoc.date.parse(addr.first_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") THEN wa.first_seen ELSE addr.first_seen END,"
		+ " wa.last_seen =  CASE WHEN apoc.date.parse(wa.last_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") > apoc.date.parse(addr.last_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") THEN wa.last_seen ELSE addr.last_seen END");
		System.out.println("BelongTo rel created");
		
		
		if(withOutputIndex){
			sR = session.run(queryTran 
					+ " MATCH (senderAddress:Address {AddId:line[0]}), (senderAddress)-[:BelongTo]->(sender:Wallet),"
					+ " (receiverAddress:Address {AddId:line[1]}), (receiverAddress)-[:BelongTo]->(receiver:Wallet)"
					+ " MERGE (sender)-[r:SendTo{uniqueReferenceTran:(line[2] + line[8])}]->(receiver)"
					+ " ON CREATE SET r.tranHashString=line[2],r.time=line[3],r.value_bitcoin=line[4],"
					+ "r.value_dollar=line[5],r.type=line[6],r.estChanAddr=line[7],r.outputIndex=line[8]");
		}else{
			System.out.println("withoutOutputIndex");
			sR = session.run(queryTran 
					+ " MATCH (senderAddress:Address {AddId:line[0]}), (senderAddress)-[:BelongTo]->(sender:Wallet),"
					+ " (receiverAddress:Address {AddId:line[1]}), (receiverAddress)-[:BelongTo]->(receiver:Wallet)"
					+ " MERGE (sender)-[r:SendTo{uniqueReferenceTran:(line[2] + 'default')}]->(receiver)"
					+ " ON CREATE SET r.tranHashString=line[2],r.time=line[3],r.value_bitcoin=line[4],"
					+ " r.value_dollar=line[5],r.type=line[6],r.estChanAddr=line[7],r.outputIndex='default'");			
		}

		sR.list();
		System.out.println("Tran rel created");
		
		//merge same addresses and wallet
		
		String queryForAddMerge = "MATCH (a:Address), (b:Address), (a)-[r:BelongTo]->(:Wallet), (b)-[:BelongTo]->(:Wallet)"
				+ " WHERE a.AddId=b.AddId AND ID(a) > ID(b)" 
				
				+ " WITH DISTINCT r, a, b, a {.*, rel: r {.*, type:type(r)}} as snapshot"  
				+ " DELETE r"
				+ " WITH DISTINCT a, b, snapshot"
				+ " DELETE a"
				+ " WITH DISTINCT b, snapshot"
				
				+ " MERGE (c:Address {uniqueReferenceAddr:(snapshot.AddId"
				+ " + CASE WHEN snapshot.addr_tag_link<>'null' THEN snapshot.addr_tag_link ELSE b.addr_tag_link END"
				+ " + CASE WHEN snapshot.addr_tag<>'null' THEN snapshot.addr_tag ELSE b.addr_tag END"
				+ " + snapshot.primWallAddr + apoc.convert.toString(snapshot.multiExist OR b.multiExist))})"
				+ " ON CREATE SET c.AddId=snapshot.AddId,"
				+ " c.addr_tag_link=CASE WHEN snapshot.addr_tag_link<>'null' THEN snapshot.addr_tag_link ELSE b.addr_tag_link END,"
				+ " c.addr_tag=CASE WHEN snapshot.addr_tag<>'null' THEN snapshot.addr_tag ELSE b.addr_tag END,"
				+ " c.first_seen=snapshot.first_seen, c.last_seen=snapshot.last_seen, c.primWallAddr=snapshot.primWallAddr,"
				+ " c.multiExist=snapshot.multiExist OR b.multiExist"			
				+ " WITH DISTINCT c, b, snapshot" 
				
				+ " MATCH (wa:Wallet) WHERE wa.primWallAddr=snapshot.rel.primWallAddr"
				+ " MERGE (c)-[r:BelongTo{uniqueReferenceBelongTo:(c.AddId + wa.primWallAddr)}]->(wa)"
				+ " ON CREATE SET r.primWallAddr = wa.primWallAddr"
						
				+ " WITH DISTINCT c, b" 
				+ " MATCH (b)-[r0:BelongTo]->(wb:Wallet)" 
				+ " WITH DISTINCT wb, c, b, r0"
				+ " MERGE (c)-[r1:BelongTo{uniqueReferenceBelongTo:(c.AddId + wb.primWallAddr)}]->(wb)" 
				+ " ON CREATE SET r1.primWallAddr = wb.primWallAddr"
				+ " WITH DISTINCT r0, b"  
				+ " DELETE r0" 
				+ " WITH DISTINCT b" 
				+ " DELETE b"
				;
		
		//merge same addresses and have new BelongTo relation to the "address for two" to another wallet as needed
		try{
			sR = session.run(queryForAddMerge);			
		}catch(Exception e){
			e.printStackTrace();
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			sR = session.run(queryForAddMerge);						
		}
			
		List<Record> records = sR.list();

		for(Record r : records){
			System.out.println(r);
//				System.out.println(r.get("n").get("first_seen"));			
		}	
		 

		System.out.println("same addr merged");

		String queryAddrDiffWallet = "MATCH  (a:Address)-[:BelongTo]->(w1:Wallet), (a)-[r0:BelongTo]->(w2:Wallet)" 
				+ " WHERE w1 <> w2" 
				+ " RETURN count(a)";
		sR = session.run(queryAddrDiffWallet);
		
		records = sR.list();
		

		System.out.println(records.get(0).get("count(a)").asInt());
		System.out.println(records.get(0).get("count(a)").asInt() == 0);
		if(records.get(0).get("count(a)").asInt() != 0){
			sR = session.run("MATCH  (a:Address)-[:BelongTo]->(w1:Wallet), (a)-[r0:BelongTo]->(w2:Wallet)"
					+ " WHERE w1 <> w2" 
					+ " RETURN a");
			
			List<Record> recordsHelp = sR.list();
		
			for(Record r : recordsHelp){
				System.out.println(r);
				System.out.println(r.get("a").get("AddId"));			
			}
		}

		String queryMergeWallet = "MATCH  (a:Address)-[:BelongTo]->(w1:Wallet)"
				+ " WITH DISTINCT a, min(ID(w1)) as minId" 
				+ " MATCH (minW:Wallet)"
				+ " WHERE ID(minW) = minId"
				+ " SET a.primWallAddr = minW.primWallAddr"
				+ " WITH DISTINCT minW, a"
				+ " MATCH (a)-[r0:BelongTo]->(w2:Wallet)"
				+ " WHERE minW <> w2"
//				+ " RETURN minW"

				+ " WITH DISTINCT minW, w2, r0"
				+ " SET minW.first_seen = CASE WHEN apoc.date.parse(minW.first_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") < apoc.date.parse(w2.first_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") THEN minW.first_seen ELSE w2.first_seen END,"
				+ " minW.last_seen = CASE WHEN apoc.date.parse(minW.last_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") > apoc.date.parse(w2.last_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") THEN minW.last_seen ELSE w2.last_seen END"
				+ " WITH DISTINCT minW, w2, r0"
				+ " DELETE r0"
				+ " WITH DISTINCT minW, w2"
				+ " MATCH (b:Address)-[r:BelongTo]->(w2)"
				+ " WITH DISTINCT r, minW, w2, b, b {.*} as snapshot"  
				+ " DELETE r"  
				+ " WITH DISTINCT minW, w2, b, snapshot"  
				+ " DELETE b"
				+ " WITH DISTINCT minW, w2, snapshot"  
				+ " MERGE (b:Address {uniqueReferenceAddr:(snapshot.AddId +snapshot.addr_tag_link+snapshot.addr_tag"
				+ " +minW.primWallAddr+apoc.convert.toString(snapshot.multiExist))})"
				+ " SET b.AddId=snapshot.AddId, b.addr_tag_link=snapshot.addr_tag_link,"
				+ " b.addr_tag=snapshot.addr_tag,"
				+ " b.first_seen=snapshot.first_seen, b.last_seen=snapshot.last_seen, "
				+ " b.primWallAddr=minW.primWallAddr, b.multiExist=snapshot.multiExist"			
							
				+ " WITH DISTINCT b, minW, w2"
				+ " MERGE (b)-[be:BelongTo{uniqueReferenceBelongTo:(b.AddId + minW.primWallAddr)}]->(minW)"
				+ " ON CREATE SET be.primWallAddr = minW.primWallAddr"
				
				+ " WITH DISTINCT w2, minW"
				+ " MATCH (w3:Wallet)-[r2:SendTo]->(w2)"
				
				+ " MERGE (w3)-[rN:SendTo{uniqueReferenceTran:r2.uniqueReferenceTran}]->(minW)"
				+ " ON CREATE SET rN.tranHashString=r2.tranHashString,rN.time=r2.time,"
				+ "rN.value_bitcoin=r2.value_bitcoin,rN.value_dollar=r2.value_dollar,"
				+ "rN.type=r2.type,rN.estChanAddr=r2.estChanAddr,rN.outputIndex=r2.outputIndex"

				+ " WITH DISTINCT w2, minW, r2"
				+ " DELETE r2"
				+ " WITH DISTINCT w2, minW"
				+ " MATCH (w2)-[r1:SendTo]->(w4:Wallet)"
				+ " MERGE (minW)-[rN2:SendTo{uniqueReferenceTran:r1.uniqueReferenceTran}]->(w4)"
				+ " ON CREATE SET rN2.tranHashString=r1.tranHashString,rN2.time=r1.time,"
				+ "rN2.value_bitcoin=r1.value_bitcoin,rN2.value_dollar=r1.value_dollar,"
				+ "rN2.type=r1.type,rN2.estChanAddr=r1.estChanAddr,rN2.outputIndex=r1.outputIndex"
				+ " WITH DISTINCT r1"
				+ " DELETE r1"
				;
		if(records.get(0).get("count(a)").asInt() != 0){
		//merge any wallet containing same addresses and adjust the transaction as well (w2 merge to w1)
			try{
				sR = session.run(queryMergeWallet);					
			}catch(Exception e){
				e.printStackTrace();
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				sR = session.run(queryMergeWallet);						
			}
		}
		System.out.println("same wallet merged");

		
		records = sR.list();
		System.out.println("----------abc----------------------" + num);
		
		//Delete all self transactions 
		sR = session.run("MATCH (n:Wallet)-[r:SendTo]->(n)"
				+ " DELETE r");
		
		System.out.println("Self tran deleted");


		
		//delete all wallets with no addresses  
		sR = session.run(
				" MATCH (n:Wallet)"
				+ " WHERE NOT ()-[:BelongTo]->(n)"
				+ " DETACH DELETE n"
//				+ " return n"
		);
		records = sR.list();
		System.out.println("Wall with no addr deleted");

		
		String queryNumSameIdAdd = "MATCH (a:Address)-[:BelongTo]->(),(b:Address)-[:BelongTo]->()"
				+ " WHERE a.AddId = b.AddId AND a<>b"
				+ " RETURN count(*)";
		sR = session.run(queryNumSameIdAdd);
		records = sR.list();
		while(records.get(0).get("count(*)").asInt() != 0){
			//Merge all addr with the same id together
			session.run("MATCH  (a:Address)"
					+ " WITH a.AddId as addId, min(ID(a)) as minId"
					+ " MATCH (minA:Address)"
					+ " WHERE ID(minA) = minId"
					+ " WITH minA"
					+ " MATCH (b:Address)"
					+ " WHERE b <> minA AND b.AddId = minA.AddId"
					+ " MATCH (b)-[:BelongTo]->(wa:Wallet)"
//					+ " WHERE b.primWallAddr = wa.primWallAddr"
					+ " DETACH DELETE b"
					+ " WITH minA, wa"
					+ " MERGE (minA)-[r0:BelongTo{uniqueReferenceBelongTo:(minA.AddId + wa.primWallAddr)}]->(wa)"
					+ " ON CREATE SET r0.primWallAddr = wa.primWallAddr");
			//Merge all same wallets together

			
			sR = session.run(queryAddrDiffWallet);
			records = sR.list();
			if(records.get(0).get("count(a)").asInt() != 0){
				sR = session.run(queryMergeWallet);
				sR.list();		
			}
			sR = session.run(queryNumSameIdAdd);
			records = sR.list();
			
			System.out.println("Loop: " + records.get(0).get("count(*)").asInt());
		}
//		//delete all addresses with no wallets   or deal with them separately
//		sR = session.run(
//				" MATCH (n:Address)"
//				+ " WHERE NOT ()-[:BelongTo]->(n)"
//				+ " DELETE n"
//		);		
		
		
		records = sR.list();

		for(Record r : records){
			System.out.println(r);
//			System.out.println(r.get("n").get("first_seen"));			
		}	
		
		//to wipe all nodes and relationships:  rm -rf data/databases/graph.db		
	}
	public static void main(String[] args) throws FileNotFoundException{
		Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "neo7474" ) );
		try(Session session = driver.session()){
			String mainDir = "C:\\Program Files\\neo4j-enterprise-3.1.1\\import\\csvsToImport\\csvs";
			// create index
			session.run( "CREATE CONSTRAINT ON (addr:Address) ASSERT addr.uniqueReferenceAddr IS UNIQUE");
	
			session.run( "CREATE CONSTRAINT ON (wa:Wallet) ASSERT wa.primWallAddr IS UNIQUE"); 
			
	//		session.run( "CREATE CONSTRAINT ON ()-[b:BelongTo]->() ASSERT b.uniqueReference IS UNIQUE"); //impossible to create unique constraints on relationships
	//		session.run( "CREATE CONSTRAINT ON ()-[s:SendTo]->() ASSERT s.uniqueReference IS UNIQUE");
		
			int counter = 1;
			while(counter < 1000){
				File dir = new File(mainDir + counter);
				if(dir.exists() && Importer.isAllFileCSV(mainDir, counter)){
					Importer a = new Importer(counter, counter > 260, session, mainDir);				
				}
				counter ++;
			}		
		}
	}
}
