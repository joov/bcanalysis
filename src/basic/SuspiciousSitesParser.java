package basic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

class BadSequence extends ArrayList<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static ArrayList<String> dom;

	@Override
	public boolean add(String e) {
		// if com, .com, net, .net, org, .org, variants, any similar domains,
		// similiar: do not take them in!
		String[] domain = { "com", "net", "org", "xx", ".com", ".net", ".org", ".xx", "variants", "variant", "multiplier", "similar"};
		BadSequence.dom = new ArrayList<String>();
		for (String doma : domain) {
			BadSequence.dom.add(doma);
		}
		if (!BadSequence.dom.contains(e.trim().toLowerCase())) {
			return super.add(e.trim());
		} else {
			return false;
		}
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		System.out.println("size " + c.size());
		return super.addAll(c);
	}
}

public class SuspiciousSitesParser {

	private static Collection<? extends String> matchExtract(String text) {
		Pattern p1 = Pattern.compile("[^\\s]+[\\s]*\\([.*]\\)"); // brackets
		Pattern p2 = Pattern.compile(
				"([^\\s]+[\\s]+)?((\\band\\b|\\bor\\b|\\baka\\b|\\b&amp;\\b|\\bRelated to\\b"
				+ "|\\bmoved from\\b|\\bnow\\b|\\bformerly\\b|\\band by the same host\\b|\\band any address starting with\\b)[\\s]+[^\\s]+)+");
		Pattern p3 = Pattern.compile("[^\\s]+[\\s]*(,|\\b&amp;\\b|;|[\\s]+/|/[\\s]+)[\\s]*[^\\s]+"); // separated
																								// by
																								// punctuation
		Pattern p4 = Pattern.compile("([^\\s]+([\\.][^\\s]+)+)[\\s]+([^\\s]+([\\.][^\\s]+)+)"); // two websites saparated by space
		BadSequence result = new BadSequence();
		Matcher m1 = p1.matcher(text);
		Matcher m2 = p2.matcher(text);
		Matcher m3 = p3.matcher(text);
		Matcher m4 = p4.matcher(text);
		
		System.out.println(m1.matches());
		System.out.println(m2.matches());
		System.out.println(m3.matches());
		System.out.println(m4.matches());

		if (m1.matches()) {
			String inBracket = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
			if (m2.matches()) {
				String[] items = inBracket.split("(\\band\\b|\\bor\\b|\\baka\\b|\\b&amp;\\b|\\bRelated to\\b"
						+ "|\\bmoved from\\b|\\bnow\\b|\\bformerly\\b|\\band by the same host\\b|\\band any address starting with\\b)");
				for (String item : items) {
					result.addAll(matchExtract(item));
				}
			} else if (m3.matches()) {
				String[] items = inBracket.split("(,|\\b&amp;\\b|;|[\\s]+/|/[\\s]+)");
				for (String item : items) {
					result.addAll(matchExtract(item));
				}
			} else if (m4.matches()) {
				String[] websites = inBracket.split("[\\s]+");
				for (String web : websites) {
					result.add(web);
				}
			} else if(inBracket.startsWith("formerly")) {
				result.add(inBracket.split("[\\s]+")[1]);
			} else{
				result.add(inBracket);				
			}
		}

		if (m2.matches()) {
			String[] items = text.split("(\\band\\b|\\bor\\b|\\baka\\b|\\b&amp;\\b|\\bRelated to\\b"
					+ "|\\bmoved from\\b|\\bnow\\b|\\bformerly\\b|\\band by the same host\\b|\\band any address starting with\\b)");
			for (String item : items) {
				result.addAll(matchExtract(item));
			}
		} else if (m3.matches()) {
			String[] items = text.split("(,|\\b&amp;\\b|;|[\\s]+/|/[\\s]+)");
			for (String item : items) {
				result.addAll(matchExtract(item));
			}
		} else if (m4.matches()) {
			String[] websites = text.split("[\\s]+");
			for (String web : websites) {
				result.add(web);
			}
		}

		return result;
	}

	public static void main(String[] args) throws IOException {
		Document doc = Jsoup.connect("http://www.badbitcoin.org/thebadlist/").get();
		Elements listBadSites = doc.select("div.bltabcontent ul.badlistul li");
		//The most basic
		for (Element site : listBadSites) {
			if (site.select("b").first() != null) {
				System.out.println(site.select("b").first().html());				
			}
		}

//		ArrayList<String> badList = new ArrayList<String>();
//
//		for (Element site : listBadSites) {
//			if (site.select("b").first() != null) {
//				Element bEle = site.select("b").first();
//				if (bEle.select("a") != null && bEle.select("a").html().length() > 0) {
//					System.out.println(site.select("b"));
//					Elements aEle = bEle.select("a");
//					for (Element ele : aEle) {
//						badList.addAll(matchExtract(ele.html()));
//					}
//					bEle.empty();
//					System.out.println("aaaaa" + bEle.html());
//				}
//				badList.addAll(matchExtract(bEle.html()));
//				// ignore capitalization when using it!
//			}
//		}
//		String[] specBadList = { "AltSwap", "Mineco", "Iluveunc", "Bitbillions.com", "Bitcoin Funding Union",
//				"Firemine", "Bitcoin TriFecta Funnel System", "offer4573645.surge.sh", "Zarfund", "Biddoge",
//				"alexzver33.wix.com/biddoge", "Justdoubleit", "BitcoinFU", "Makebtc", "Satoshi][^\\s+][ne",
//				"Btcbanking.weebly.com", "Btc Bank Ltd", "Mycryptoworld", "bbc.bitbillions", "Bitcoinx3.blogspot", "Hyipbtc100.blogspot" };
//		for (String item : specBadList) {
//			badList.add(item);
//		}
//
//		for(String item : badList){
//			System.out.println(item);
//		}
//		
		
		
//		try (PrintWriter out = new PrintWriter("resource/badList.txt")) {
//			for (String text : badList) {
//				out.println(text + "\n");
//			}
//		}

		// when in use: Pattern p = Pattern.compile(".*(" + text + ").*");
		// Matcher m = p1.matcher(temp);
		// m.matches()

	}

}
