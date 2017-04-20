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
				+ "a.first_seen=line[3], a.last_seen=line[4], a.primWallAddr=line[5], a.multiExist=apoc.convert.toBoolean(line[6])" //(case line[6] when '' then true else false end)
				);
		
		System.out.println("Address nodes created");
		sR = session.run("MATCH (n:Address) "
				+ " WHERE n.primWallAddr IS NULL"
				+ " WITH n"
				+ " DELETE n");
		System.out.println("Delete some addr nodes just created");

		sR = session.run(queryWallet 
				+ " MERGE (w:Wallet { primWallAddr:line[0]})"
				+ " ON CREATE SET w.first_seen = line[1], w.last_seen=line[2]");
		
		System.out.println("Wallet nodes created");
		
		sR = session.run("MATCH (addr:Address), (wa:Wallet) WHERE addr.primWallAddr=wa.primWallAddr"
		+ " MERGE (addr)-[:BelongTo{uniqueReferenceBelongTo:(addr.AddId + wa.primWallAddr)}]->(wa)");
		System.out.println("BelongTo rel created");
		
		if(withOutputIndex){
			sR = session.run(queryTran + " MATCH (sender:Wallet {primWallAddr:line[0]}), (receiver:Wallet {primWallAddr:line[1]}) "
					+ " MERGE (sender)-[:SendTo{tranHashString:line[2],time:line[3],value_bitcoin:line[4],"
					+ "value_dollar:line[5],type:line[6],estChanAddr:line[7],outputIndex:line[8],uniqueReferenceTran:(line[2] + line[8])}]->(receiver)");
		}else{
			System.out.println("withoutOutputIndex");
			sR = session.run(queryTran + " MATCH (sender:Wallet {primWallAddr:line[0]}), (receiver:Wallet {primWallAddr:line[1]}) "
					+ " MERGE (sender)-[:SendTo{tranHashString:line[2],time:line[3],value_bitcoin:line[4],"
					+ "value_dollar:line[5],type:line[6],estChanAddr:line[7],outputIndex:'default',uniqueReferenceTran:(line[2] + 'default')}]->(receiver)");			
		}

		System.out.println("Tran rel created");
		
		
		//merge same addresses and wallet
		//merge same addresses and have new BelongTo relation to the "address for two" to another wallet as needed
		 sR = session.run("MATCH (a:Address), (b:Address)" 
				+ " WHERE a.AddId=b.AddId AND ID(a) < ID(b)"
				+ " SET a.multiExist = a.multiExist OR b.multiExist, a.uniqueReferenceAddr = a.AddId+a.addr_tag_link+a.addr_tag+a.primWallAddr+apoc.convert.toString(a.multiExist)," 
				+ " a.addr_tag = CASE WHEN a.addr_tag IS NOT NULL THEN a.addr_tag ELSE b.addr_tag END,"
				+ " a.addr_tag_link = CASE WHEN a.addr_tag_link IS NOT NULL THEN a.addr_tag_link ELSE b.addr_tag_link END"
				+ " WITH a, b"
				+ " MATCH (b)-[r:BelongTo]->(wa:Wallet)"
				+ " WITH wa, a, b, r"
				+ " MERGE (a)-[:BelongTo{uniqueReferenceBelongTo:(a.AddId + wa.primWallAddr)}]->(wa)"
				+ " WITH b, r"
				+ " DELETE r"
				+ " WITH b"
				+ " DELETE b"
				);
			List<Record> records = sR.list();

			for(Record r : records){
				System.out.println(r);
//				System.out.println(r.get("n").get("first_seen"));			
			}	
		 
		System.out.println("same addr merged");

		
		//merge any wallet containing same addresses and adjust the transaction as well (w2 merge to w1)
		sR = session.run("MATCH  (a:Address)-[:BelongTo]->(w1:Wallet), (a)-[r0:BelongTo]->(w2:Wallet)" 
				+ " WHERE apoc.date.parse(w1.first_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") < apoc.date.parse(w2.first_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\")"
				+ " SET w1.last_seen = CASE WHEN apoc.date.parse(w1.last_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") > apoc.date.parse(w2.last_seen, 's',\"yyyy-MM-dd'T'HH:mm:ss\") THEN w1.last_seen ELSE w2.last_seen END"
				+ " WITH w1, w2, r0"
				+ " DELETE r0"
				+ " WITH w1, w2"
				+ " MATCH (b:Address)-[r:BelongTo]->(w2)"
				+ " SET b.primWallAddr = w1.primWallAddr, b.uniqueReferenceAddr = b.AddId+b.addr_tag_link+b.addr_tag+w1.primWallAddr+apoc.convert.toString(b.multiExist)"  		//update all address for uniqueReference and primWallAddr
				+ " WITH b, w1, w2, r"
				+ " MERGE (b)-[:BelongTo{uniqueReferenceBelongTo:(b.AddId + w1.primWallAddr)}]->(w1)"
				+ " WITH r, w2, w1"
				+ " DELETE r"
				+ " WITH w2, w1 "
				+ " MATCH (w3:Wallet)-[r2:SendTo]->(w2)"
				+ " MERGE (w3)-[:SendTo{tranHashString:r2.tranHashString,time:r2.time,value_bitcoin:r2.value_bitcoin,"
				+ "value_dollar:r2.value_dollar,type:r2.type,estChanAddr:r2.estChanAddr,outputIndex:r2.outputIndex,"
				+ "uniqueReferenceTran:r2.uniqueReferenceTran}]->(w1)"
				+ " WITH w2, w1, r2"
				+ " DELETE r2"
				+ " WITH w2, w1"
				+ " MATCH (w2)-[r1:SendTo]->(w4:Wallet)"
				+ " MERGE (w1)-[:SendTo{tranHashString:r1.tranHashString,time:r1.time,value_bitcoin:r1.value_bitcoin,"
				+ "value_dollar:r1.value_dollar,type:r1.type,estChanAddr:r1.estChanAddr,outputIndex:r1.outputIndex,"
				+ "uniqueReferenceTran:r1.uniqueReferenceTran}]->(w4)"
				+ " WITH r1"
				+ " DELETE r1");	
		
		System.out.println("same wallet merged");
		
		//Delete all self transactions 
		sR = session.run("MATCH (n:Wallet)-[r:SendTo]->(n)"
				+ " DELETE r");
		
		System.out.println("Self tran deleted");

		//delete all wallets with no addresses  
		sR = session.run(
//				"MATCH (n:Wallet) WHERE NOT ()-[:BelongTo]-(n) MATCH (n)-[r:SendTo]-() DETACH r DELETE r, n"
//				"MATCH (n:Wallet) WHERE NOT ()-[:BelongTo]-(n) MATCH (n)-[r:SendTo]-() RETURN r"
				" MATCH ()-[r:SendTo]->(n:Wallet) WHERE NOT ()-[:BelongTo]->(n)"
				+ " DELETE r"
//				+ " RETURN r"
				+ " With n"
				+ " DELETE n"
//				+ " return n"
);
		
		
		System.out.println("Wall with no addr deleted");
		records = sR.list();

		for(Record r : records){
			System.out.println(r);
//			System.out.println(r.get("n").get("first_seen"));			
		}	
		
		//to wipe all nodes and relationships:  rm -rf data/databases/graph.db
		
		sR = session.run("MATCH (n:Wallet) Return n;");
		records = sR.list();

		for(Record r : records){
			System.out.println(r);
			System.out.println(r.get("n").get("first_seen"));			
		}		
	}
	public static void main(String[] args) throws FileNotFoundException{
		Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "neo7474" ) );
		try(Session session = driver.session()){
			String mainDir = "C:\\Program Files\\neo4j-enterprise-3.1.1\\import\\csvsToImport\\csvs";
			// create index
			session.run( "CREATE CONSTRAINT ON (addr:Address) ASSERT addr.uniqueReferenceAddr IS UNIQUE");
	//		session.run( "CREATE INDEX ON :Address(uniqueReference)");
	
			session.run( "CREATE CONSTRAINT ON (wa:Wallet) ASSERT wa.primWallAddr IS UNIQUE"); 
	//		session.run( "CREATE INDEX ON :Wallet(uniqueReference)");
			
	//		session.run( "CREATE CONSTRAINT ON ()-[b:BelongTo]->() ASSERT b.uniqueReference IS UNIQUE"); //it seems to be impossible to create unique constraints on relationships
	//		session.run( "CREATE CONSTRAINT ON ()-[s:SendTo]->() ASSERT s.uniqueReference IS UNIQUE");
	
	//		Importer i1 = new Importer(3, false, session, mainDir);
	
			int counter = 1;
			while(counter < 1000){
				File dir = new File(mainDir + counter);
				if(dir.exists()){
					Importer a = new Importer(counter, counter > 260, session, mainDir);				
				}
				counter ++;
			}		
		}
	}
}
