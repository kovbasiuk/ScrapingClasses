

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class picsOfEnglandSearch {

	public static void main(String[] args) {
		getImgUrls("alnwick castle") ;

	}

	
	public static void getImgUrls(String searchKey) {
		
		String searchText = searchKey.replace(' ', '_');

		System.out.println(searchText);
		
		String url = "www.picturesofengland.com/"+searchText+"/pictures";
		System.out.println(url);
		try {
			Document ks = Jsoup.connect("http://www.picturesofengland.com/England/Northumberland/Alnwick/Alnwick_Castle/pictures").userAgent("Mozilla/5.0").get();
		
		
		
		
		
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}
