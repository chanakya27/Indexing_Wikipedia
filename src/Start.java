import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.jhu.nlp.wikipedia.*;

/**
 * @author ravi
 *
 */
public class Start {

	static final String wikiXMLFilePath = "Input/sample.xml";
	
	/**
	 * Constructor
	 */
	public Start() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//System.out.println(" 0 - "+args[0]);
		//System.out.println(" 1 - "+args[1]);
		
		
		/* To parse Complete document in One G0
        try {
			WikiXMLSAXParser.parseWikipediaDump("/home/ravi/Sample/sample.xml", new PageCallbackHandler() {
				
				@Override
				public void process(WikiPage page) {
					// TODO Auto-generated method stub
					System.out.println(page.getTitle());
					return;
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		*/
		
		
		/*
		 *  Same thing as above
		 */
		/*
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser("/home/ravi/Sample/sample.xml");
		try {
            
            wxsp.setPageCallback(new PageCallbackHandler() { 
                           public void process(WikiPage page) {
                                  System.out.println(page.getTitle());
                           }
            });
                
           wxsp.parse();
        }catch(Exception e) {
                e.printStackTrace();
        }
        
        */
		
		/*
		 * For step wise Parsing :: It did not work
		 */
		/*
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser("/home/ravi/Sample/sample.xml");
		int i=0;
		try {
			WikiPageIterator wpi = wxsp.getIterator();
			
			while (wpi.hasMorePages()==true)
			{
				System.out.println(wpi.nextPage().getTitle());
				
				if (i==10)
				{
					break;
				}
				i++;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        */
		
		ParseAndIndex pai = new ParseAndIndex(wikiXMLFilePath);
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
			
		long startIndexTime = System.currentTimeMillis();
		Date startIndexDate = new Date();
		
		pai.createIndex();
		
		Date finishIndexDate = new Date();
		long estimatedIndexTime = System.currentTimeMillis() - startIndexTime;
		
		System.out.println("===========Details============\n");
		System.out.println("Start Time :- "+dateFormat.format(startIndexDate));
		System.out.println("Finish Time :- "+dateFormat.format(finishIndexDate));
		System.out.println("Estimated Time :- "+estimatedIndexTime);
		
		//pai.mergeIndexFiles();
		//pai.displayIndex();
		
	}

}
