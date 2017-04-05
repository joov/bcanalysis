package basic;

import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

/**
 * Only contain a main method parse sites from the http://www.badbitcoin.org/thebadlist/
 * Need manual correction to enable the result to be used
 * @author yshi
 *
 */
public class SuspiciousSitesParser {
	public static void main(String[] args) throws IOException {
		Document doc = Jsoup.connect("http://www.badbitcoin.org/thebadlist/").get();
		Elements listBadSites = doc.select("div.bltabcontent ul.badlistul li");
		//The most basic
		for (Element site : listBadSites) {
			if (site.select("b").first() != null) {
				System.out.println(site.select("b").first().html());				
			}
		}

	}

}
