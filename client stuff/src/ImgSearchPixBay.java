import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ImgSearchPixBay {
	
	public static void main (String []args) throws IOException {
		getUrls("alnwick castle");
	}
    public static /*String[]*/ void getUrls(String searchKey) throws IOException {

   	String s = "https://pixabay.com/images/search/" + String.valueOf(URLEncoder.encode(searchKey, "UTF-8"));
        //google search result
     Document pix = Jsoup.connect("https://pixabay.com/images/search/" + URLEncoder.encode(searchKey, "UTF-8")).userAgent("Mozilla/5.0").get();
  
       

        String[] imgUrls = new String[3];
     
        //selects 1st,3rd and 5th img
        Element image1 = pix.select("img").get(0);
        Element image2 = pix.select("img").get(2);
        Element image3 = pix.select("img").get(4);
        String src1 = image1.attr("srcset").split(" ")[2];
        String src2 = image2.attr("srcset").split(" ")[2];
        String src3 = image3.attr("srcset").split(" ")[2];
        System.out.println(s);
        System.out.println(src1);
        System.out.println(src2);
        System.out.println(src3);

    //    return imgUrls;
    
     
    }
}
