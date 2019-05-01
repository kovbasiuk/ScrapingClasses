

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.esotericsoftware.minlog.Log;

/*
 * Author: Robert Kovbasiuk
 * 
 * Purpose: A class that contains and combines the wikipedia scraper and the tripadvisor scraper.
 * 
 */
public class CombinedSearch {

	public static void main(String[] args)
			throws UnsupportedEncodingException, IOException, ParseException, InterruptedException {
		CombinedSearch cs = new CombinedSearch();
		
		
		
		JSONParser jsonParser = new JSONParser();

		JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader(
				"/Users/robertkovbasiuk/Desktop/Uni Projects/client stuff/src/northumberland-project.json"));
		//List <String> l = Arrays.asList(new String[]{"ff","dd",null,"hello"});
		
//		
//		//String jsonString = FileUtils. readFileToString(File, Charset)
//		JSONObject rootObj = (JSONObject) jsonArray.get(0);
//		System.out.println(rootObj.get("Name"));
		
		//rootObj.put("Description1", "test");
		
		//initialises settings and objects needed
		System.setProperty("webdriver.chrome.driver", "/Users/robertkovbasiuk/coding stuff/chromedriver");
		SeleniumClicker sc = new SeleniumClicker();
		WebDriver driver = new ChromeDriver();
		WikiSearch ws = new WikiSearch();
	
		

		//fills the lists with informations relevant to the lists
		cs.makeLists(driver, sc, ws,jsonArray);
		
		//makes files and writes to them based on the lists existing. sc.certainDesc is a String list of all attractions that contained the info
		//on tripadvisor but something went wrong when scraping the information.
		cs.makeFiles(cs.foundResults, cs.noResults, "notFound1.txt", "descfiles1.json", jsonArray, sc.certainDesc,"certaindesc1.csv");
	}


	
//1 list for found results and the other list for not found results	
	private List<String> foundResults = new ArrayList<String>();
	private List<String> noResults = new ArrayList<String>();
	
	public void makeLists(WebDriver driver, SeleniumClicker sc, WikiSearch ws, JSONArray jsonArray)
			throws FileNotFoundException, IOException, ParseException, InterruptedException {

		//parses json objects through to a loop which performs relevant methods
		
			int length = jsonArray.size();
			
			for (int i = 0; i < length; i++) {
				Log.info("----- LAST NUM: "+ String.valueOf(i)+"--------");
				JSONObject rootObj = (JSONObject) jsonArray.get(i);
				String searchKey = (String) rootObj.get("Name");
				String desc = doSearch(searchKey, driver, sc, ws);
				
				
				foundResults.add(desc);
				
				if(desc==null) {
					
				}
				noResults.add((String) rootObj.get("Name"));
				
				
				
			}
			Log.info("Num of found results: "+String.valueOf(foundResults.size()));
			Log.info(String.valueOf("Num of NOT FOUND results: "+noResults.size()));

	}

	//method uses information gathered in other methods which is now in a list and creates files. 
	//1 file for found attractions with information
	//1 file for attractions with no information on wikipedia and tripadvisor
	//1 file for attractions with information on tripadvisor but web driver encountered problems extracting it
	private void makeFiles(List<String>foundList,List<String>notFoundList, String notFoundFileName,  String fileName, JSONArray jsonArray,
			List<String>certainDesc, String certainDescFile) throws IOException {
		File file = new File(fileName);
		
		File fileNotFoundStuff = new File(notFoundFileName);
		int length = jsonArray.size();
		
		JSONArray newOne = new JSONArray();
		
		for (int i = 0; i < length; i++) {
			JSONObject rootObj = (JSONObject) jsonArray.get(i);
			String name = (String) rootObj.get("Name");
			rootObj.put("name",name);
			
			String description = foundList.get(i)==null?"":foundList.get(i);
			rootObj.put("Description", description);
			newOne.add(rootObj);
		}
		    
		 Files.write(Paths.get(fileName), newOne.toJSONString().getBytes());
		   

	
		
		
		PrintWriter pw = new  PrintWriter(new FileWriter(fileNotFoundStuff,true));
		pw.println("NOT FOUND");
		for (String s : notFoundList) {
			
			if(s!=null) {
			pw.print(s+"\n");
			}
			
			pw.print("\n")
			;
			
		}
		PrintWriter pw2 = new  PrintWriter(new FileWriter(certainDescFile,true));
		for (String s : certainDesc) {
			
			if(s!=null) {
			pw2.print(s+"\n");
			}
			
			pw2.print("\n")
			;
			
		}
		
			
		
				pw.close();
				pw2.close();
	}
	
	//attempts wikipedia search and if nothing is found then attemots tripadvisor
	public String doSearch(String searchKey, WebDriver driver, SeleniumClicker sc, WikiSearch ws)
			throws UnsupportedEncodingException, IOException, InterruptedException {

		String searchRes = ws.getInfo(searchKey);

		if (searchRes != null) {
			Log.info("INFO FOR: " + searchKey + " FOUND ON WIKIPEDIA");
			return searchRes;
		}

		else {
			searchRes = sc.getInfo(searchKey, driver);

			if (searchRes == null) {

				Log.warn("NO INFO COULD BE FOUND ON: " + searchKey + " ANYWHERE");
				return null;
			} else {
				Log.info("INFO FOR: " + searchKey + "FOUND ON TRIPADVISOR");
				return searchRes;
			}

		}

	}
	

}