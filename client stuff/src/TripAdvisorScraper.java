import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TripAdvisorScraper {

	public static void main(String[]args) throws UnsupportedEncodingException, IOException {
		getInfo("alnwick castle");
	}
	
	
	static String encoding = "UTF-8";
	
	public static void getInfo(String searchKey) throws UnsupportedEncodingException, IOException {
	
	 //creates google search text string
    String searchText= searchKey + "northumberland"+ "tripadvisor";

    //google search result
    Document google = Jsoup.connect("https://www.google.com/search?q="+
            URLEncoder.encode(searchText,encoding)).userAgent("Mozilla/5.0").get();
	
    //selects first site, if not tripadvisor, selects next one 
    String url = google.getElementsByTag("cite").get(0).text();
    int sitePick = 0;
    while (!url.contains("tripadvisor")) {
    	sitePick++;
    	url=	google.getElementsByTag("cite").get(sitePick).text();
    }
    
    
    //google search result
    Document tp = Jsoup.connect("https://www.google.com/search?q="+
            URLEncoder.encode(searchText,encoding)).userAgent("Mozilla/5.0").get();

  
    Element image1 = tp.select("img").get(0);
  //  String src1 = image1.attr("data-src");
    String src1 = image1.attr("abs:src");
    Elements elemImages = tp.select("img[src$=.jpg]");
  
   for (Element x : elemImages) {
	   System.out.println(String.valueOf(x));
   }
    
   // System.out.println(url);
 //  System.out.println(src1);
   
    
}
}

