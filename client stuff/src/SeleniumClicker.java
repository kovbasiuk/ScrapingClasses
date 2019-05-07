import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.esotericsoftware.minlog.Log;

public class SeleniumClicker {
	public List<String> certainDesc = new ArrayList<String>();
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException, InterruptedException {

		System.setProperty("webdriver.chrome.driver", "/Users/robertkovbasiuk/coding stuff/chromedriver");
		SeleniumClicker sc = new SeleniumClicker();
		WebDriver driver = new ChromeDriver();

		//testing attractions of different categories
		sc.getInfo("airbox", driver);
		sc.getInfo("all saints church", driver);
		sc.getInfo("Allen Banks and Staward Gorge", driver);

		sc.getInfo("The Bamburgh Castle Inn", driver);
		sc.getInfo("alnwick lodge" + "", driver);
		sc.getInfo("Bamburgh Castle", driver);
		sc.getInfo("warkworth castle", driver);
		sc.getInfo("lindisfarne", driver);
		sc.getInfo("northumberland national park", driver);

	}

	//needed in case errors come up, max 4 attempts.
	int getUrlTryCount = 0;


	//
	public String getInfo(String searchKey1, WebDriver driver)
			throws UnsupportedEncodingException, IOException, InterruptedException {
	
		//to lower case to avoid case sensitivity when comparing urls in methods inside this method as searchKey is passed through to the parameters.
		String searchKey = searchKey1.toLowerCase();
		searchKey = searchKey.replaceAll("  ", " ");
		searchKey = searchKey.replaceAll("&", "");
		searchKey = searchKey.replaceAll(",", "");
		searchKey = searchKey.replaceAll("'", "");
		searchKey = searchKey.replaceAll("\\.", "");
		Log.info("\nTRIPADVISOR SEARCH FOR: " + searchKey);
		
		//necessary variables
		String encoding = "UTF-8";
		String[] searchKeyPart = searchKey.split(" ");
		String searchText = searchKey + " " + "northumberland Tripadvisor";
		Document google;
		int sitePick = 0;
		String info;
		boolean oneWord = searchKeyPart.length < 2 ? true : false;
		String url = "";

		//finds the correct url, if not found returns null;
		url = getUrl(driver, url, searchKeyPart, oneWord, searchKey, searchText, encoding, sitePick);

		if (url == null) {
			return null;

		}
		
		//finds the info if there is any
		info = findInfo(driver, url, searchKey);
		return info;

	}

	//method to get the correct url, if conditions arent met then returns null
	public String getUrl(WebDriver driver, String url, String[] searchKeyPart, boolean oneWord, String searchKey,
			String searchText, String encoding, int sitePick) {

		//uses jsoup to get the correct url by analysing url contents
		try {
			Document google = Jsoup
					.connect("https://www.google.com/search?q=" + URLEncoder.encode(searchText, encoding))
					.userAgent("Mozilla/5.0").get();

			//selects first result
			url = google.getElementsByTag("cite").get(0).text();

			
			//checks if search is for one word.
			if (oneWord == true) {

				//boolean correctSite gets assigned a value based what the contains parts method returns
				//based on the inputs
				boolean correctSite = containsParts(url, searchKeyPart, false, searchKey);

				//loops until stop condition is met in the if statement or unless correctSite==true;
				//getUrlTryCount is reset back to 0 to reset the count for errors for next search
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

			
			//excecuted if oneword==false
			//following the same structure
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

			Log.info("TripAdvisor URL for " + searchKey + ": " + url);

			return url;
			
		} 
		//catches any random exceptions and repeats search if the condition is met 
		catch (Exception e) {
			Log.info("Something went wrong1");
			if (getUrlTryCount < 4) {
				getUrlTryCount++;
				getUrl(driver, url, searchKeyPart, oneWord, searchKey, searchText, encoding, sitePick);
			}
			return null;
		}
	}

	// checks if element exists
	public boolean elementExists(String url, String elementID, WebDriver driver) {

		boolean exists = true;

		try {
			driver.findElement(By.className(elementID));
		}

		catch (Exception e) {
			exists = false;
		}

		return exists;
	}

	//method checks if url contains all parts of the searchKey and if it contains tripadvisor. returns a boolean
	public boolean containsParts(String url, String[] searchKeyPart, boolean array, String searchKey) {
		url = url.toLowerCase();
		searchKey = searchKey.toLowerCase();

		if (array == true) {
			Log.info("Testing for key Parts:");

			if (!url.contains("tripadvisor")) {
				Log.warn("doesnt contain trip advisor");
				return false;
			}

			for (String s : searchKeyPart) {
				if (!url.contains(s)) {
					Log.warn("Missing Key Part");
					Log.info("Doesnt contain:" + s);
					return false;
				}

			}
			Log.info("All key parts found");
			return true;
		}

		else {
			if (url.contains(searchKey) && url.contains("tripadvisor")) {
				return true;
			}
			Log.info("no url found");
			return false;
		}
	}

	//tryCount for findInfo incase errors occur
	int tryCount = 0;

	
	public String findInfo(WebDriver driver, String url, String searchKey) {

		//driver opens web page
		driver.get(url);

		try {

			//strings to look for in the source code
			String moreHotelButton = "hotels-hotel-review-about-with-photos-Description__readMore--PRgQ8";
			String moreButton = "attractions-attraction-detail-about-card-Description__readMore--2pd33";
			String hotelFullInfo = "common-text-ReadMore__content--2X4LR";
			String fullInfo = "attractions-attraction-detail-about-card-Description__modalText--1oJCY";
			String RestuarantLongInfoButton = "restaurants-detail-overview-cards-DetailsSectionOverviewCard__viewDetails--ule3z";
			String RestuarantLongInfoDesc = "restaurants-detail-overview-cards-DetailsSectionOverviewCard__desktopAboutText--VY6hs";
			String restaurantShortInfo = "restaurants-detail-overview-cards-SnippetsOverviewCard__heading--2jhMN";
			String shortInfo = "attractions-attraction-detail-about-card-AttractionDetailAboutCard__section--1_Efg";

			//info to be returned
			String attInfo;

			if (url == null) {
				return null;
			}

			//checks through a logical hierachy of elements in source code if my element exists method returns true
			if (elementExists(url, moreButton, driver)) {

				
				try {
					//clicks on more info for the extra information ti appear, otherwise its not found in source code, similar to other elements in here
					driver.findElement(By.className(moreButton)).click();
					Log.info("Clicked on more button");
					driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
					driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

					//finds using search string
					WebElement element = driver.findElement(By.className(fullInfo));

					attInfo = element.getText();

					Log.info(attInfo + "\n");
					// System.out.println(info);
					tryCount = 0;
					return attInfo;
				} catch (Exception e) {
					if (!certainDesc.contains(searchKey)) {

						tryCount = 0;
						certainDesc.add(searchKey);
						return null;
					}
				}
			}

			if (elementExists(url, moreHotelButton, driver)) {
				driver.findElement(By.className(moreHotelButton)).click();
				driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
				Log.info("Clicked on hotel more button");
				WebElement element = driver.findElement(By.className(hotelFullInfo));

				// restaurants-detail-overview-cards-SnippetsOverviewCard__heading--2jhMN the
				// food description thing

				attInfo = element.getText();

				Log.info(attInfo + "\n");
				// System.out.println(info);
				tryCount = 0;
				return attInfo;
			}

			if (elementExists(url, hotelFullInfo, driver)) {
				driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
				Log.info("found hotel info");
				WebElement element = driver.findElement(By.className(hotelFullInfo));

				// restaurants-detail-overview-cards-SnippetsOverviewCard__heading--2jhMN the
				// food description thing

				attInfo = element.getText();

				Log.info(attInfo + "\n");

				tryCount = 0;
				return attInfo;
			}

			if (elementExists(url, RestuarantLongInfoButton, driver)) {

				driver.findElement(By.className(RestuarantLongInfoButton)).click();
				if (elementExists(url, RestuarantLongInfoDesc, driver)) {
					WebElement element = driver.findElement(

							By.className(RestuarantLongInfoDesc));

					attInfo = element.getText();
					Log.info("Restaurant long info found!");

					Log.info(attInfo + "\n");
					tryCount = 0;
					return attInfo;
				}

				else {
					Log.warn("No Restuarant Long Information found");
					tryCount = 0;
					return null;
				}

			}

			if (elementExists(url, restaurantShortInfo, driver)) {
				WebElement element = driver.findElement(By.className(restaurantShortInfo));

				attInfo = element.getText();
				Log.info("Restaurant short info found!");
				Log.info(attInfo + "\n");
				tryCount = 0;
				return attInfo;

			}
			if (elementExists(url, shortInfo, driver)) {
				if (driver.findElements(By.className(shortInfo)).size() > 1) {
					WebElement element = driver.findElements(By.className(shortInfo)).get(1);
					Log.info("Short info found");

					attInfo = element.getText();

					if (shortInfoCheck(attInfo) == false) {
						tryCount = 0;
						return null;
					}

					Log.info("attinfo: " + attInfo + "\n");

					if (attInfo != null) {
						tryCount = 0;
						return attInfo;
					}

				}
				tryCount = 0;
				return null;

			}

			Log.warn("NO INFO FOUND \n");
			tryCount = 0;
			return null;

		} 
		
		//if any random errors/exceptions occur the program tries again as long as conditiion is met
		catch (Exception e) {

			if (tryCount < 4) {
				e.printStackTrace();
				Log.info("Something went wrong, trying again");
				tryCount++;
				return findInfo(driver, url, searchKey);
			}

			return null;
		}

	}

	//needed to remove any unwanted string found
	public boolean shortInfoCheck(String attInfo) {
		if ((attInfo.contains("Closed Now")) || (attInfo.length() < 15)
				|| (attInfo.contains("F°") || attInfo.contains("C°") || attInfo.contains("Hours Today")
						|| attInfo.contains("Suggested duration") || attInfo.contains("See all hours"))) {
			Log.info("short info check=FALSE");
			return false;
		}
		Log.info("short info check=TRUE");
		return true;
	}
}
