import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.jhu.nlp.wikipedia.*;

/**
 * @author ravi
 *
 */
public class Start {

	static final String wikiXMLFilePath = "/home/user/Desktop/Remaining_Part_Full_Crawl.xml";
			//"/home/user/Desktop/enwiki-20140502-pages-articles-multistream.xml";
	
	/**
	 * Constructor
	 */
	public Start() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ParseAndIndex pai = new ParseAndIndex(wikiXMLFilePath);
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
			
		long startIndexTime = System.currentTimeMillis();
		Date startIndexDate = new Date();

		//pai.tester();
		
		pai.mergeIndexFiles();
		
		//pai.displayIndex();
		
		Date finishIndexDate = new Date();
		long estimatedIndexTime = System.currentTimeMillis() - startIndexTime;
		
		System.out.println("===========Details============\n");
		System.out.println("Start Time :- "+dateFormat.format(startIndexDate));
		System.out.println("Finish Time :- "+dateFormat.format(finishIndexDate));
		System.out.println("Estimated Time :- "+estimatedIndexTime);
		

		
	}

}
