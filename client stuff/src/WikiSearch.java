
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;

import com.esotericsoftware.minlog.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class WikiSearch {

	public static void main(String[] args) throws MalformedURLException, IOException {
		WikiSearch ws = new WikiSearch();
		ws.getInfo("All Saints' Church, Newcastle upon Tyne");
		ws.getInfo("bamburgh castle");
		ws.getInfo("alnwick castle");
		ws.getInfo("dwfwe");
	}

	int getUrlTryCount = 0;

	public String getInfo(String searchKey1) throws MalformedURLException, IOException {

		String searchKey = searchKey1.toLowerCase();

		System.out.println(searchKey);
		Log.info("\nWIKIPEDIA search for: " + searchKey);
		String encoding = "UTF-8";
		
		//replaces chars that will be in the searchkey but not in the url which will affect finding the url
		searchKey = searchKey.replaceAll("  ", " ");
		searchKey = searchKey.replaceAll("&", "");
		searchKey = searchKey.replaceAll(",", "");
		searchKey = searchKey.replaceAll("'", "");
		searchKey = searchKey.replaceAll("\\.", "");
		String[] searchKeyPart = searchKey.split(" ");
		
		//initialises boolean one word using short handed if statement
		boolean oneWord = searchKeyPart.length < 2 ? true : false;
		String searchText = searchKey + " " + "northumberland Wikipedia";
		String url = "";
		int sitePick = 0;

		//gets url
		 url = getUrl(url, searchKeyPart, oneWord, searchKey, searchText, encoding, sitePick);

		 if(url==null) {
			 return null;
		 }
		 
		 //replaces &27 with a ' to get the accurate url
		url = url.replaceAll("%27", "'");

		// Making WIKI Json API url
		String wikiAPIJson = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="
				+ String.valueOf(URLEncoder.encode(url.replaceAll(("https://en.wikipedia.org/wiki/"), ""), encoding));

		wikiAPIJson = wikiAPIJson.replaceAll("%27", "'");

		// extract";"
		HttpURLConnection hc = (HttpURLConnection) new URL(wikiAPIJson).openConnection();
		hc.addRequestProperty("User-Agent", "Mozilla/5.0");
		BufferedReader br = new BufferedReader(new InputStreamReader(hc.getInputStream()));

		// read in line by line
		String data = br.lines().collect(Collectors.joining());
		br.close();

		Log.info("Data: "+data);

		//cleans up the extracted string
		try {
			String result = data.split("\"extract\":\"")[1];
			result = result.split("\"")[0];
			result = result.replaceAll("\\( \\(listen\\)\\)", "");
			result = result.replaceAll("  ", " ");
			result = result.replaceAll("; ", "");

			if (result != null) {
				Log.info("RESULT IS: " + result + "\n");
				return result;
			}

			else {
				result = null;
				Log.warn("NO DATA FOUND for: " + searchKey + "\n");
				return result;
			}
		} catch (Exception e) {
			return null;
		}
	}

	//gets url, explained in the combined search class
	public String getUrl(String url, String[] searchKeyPart, boolean oneWord, String searchKey, String searchText,
			String encoding, int sitePick) {

		try {
			Document google = Jsoup
					.connect("https://www.google.com/search?q=" + URLEncoder.encode(searchText, encoding))
					.userAgent("Mozilla/5.0").get();

			url = google.getElementsByTag("cite").get(0).text();

			if (oneWord == true) {

				boolean correctSite = containsParts(url, searchKeyPart, false, searchKey);

				while (correctSite == false) {
					sitePick++;
					url = google.getElementsByTag("cite").get(sitePick).text();
					correctSite = containsParts(url, searchKeyPart, false, searchKey);

					Log.info("!  URL = " + url);
					if (sitePick > 2) {
						Log.warn("URL for: " + searchKey + " NOT FOUND");
						getUrlTryCount = 0;
						return null;

					}
				}
				getUrlTryCount = 0;
			}

			else

			{

				boolean correctSite = containsParts(url, searchKeyPart, true, searchKey);

				while (correctSite == false) {
					sitePick++;
					url = google.getElementsByTag("cite").get(sitePick).text();
					correctSite = containsParts(url, searchKeyPart, true, searchKey);

					Log.info("!  URL = " + url);
					if (sitePick > 2) {
						Log.warn("URL for: " + searchKey + " NOT FOUND");
						getUrlTryCount = 0;
						return null;

					}
				}
				getUrlTryCount = 0;
			}

		
			Log.info("Wikipedia URL for " + searchKey + ": " + url);

			return url;
		} catch (Exception e) {
			Log.info("Something went wrong");
			if (getUrlTryCount < 4) {
				getUrlTryCount++;
				getUrl(url, searchKeyPart, oneWord, searchKey, searchText, encoding, sitePick);
			}
			getUrlTryCount=0;
			return null;
		}

	}

	//checks if url contains string search parts, explained in combined
	public boolean containsParts(String url, String[] searchKeyPart, boolean array, String searchKey) {
		url = url.toLowerCase();
		searchKey = searchKey.toLowerCase();

		if (array == true) {
			Log.info("Testing for key Parts:");

			if (!url.contains("wikipedia")) {
				Log.warn("doesnt contain wikipedia");
				return false;
			}

			for (String s : searchKeyPart) {
				if (!url.contains(s)) {
					Log.warn("Missing Key Part: "+s);
					return false;
				}

			}
			Log.info("All key parts found");
			return true;
		}

		else {
			if (url.contains(searchKey) && url.contains("wikipedia")) {
				return true;
			}
			Log.info("no url found");
			return false;
		}
	}
}