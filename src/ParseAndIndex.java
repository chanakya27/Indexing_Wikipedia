import edu.jhu.nlp.wikipedia.InfoBox;
import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLSAXParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class ParseAndIndex implements Serializable {

	private String wikiFilePath;
	private final String stopWordListFilePath = "Input/StopWordList.txt";
	private final String stopCharListFilePath = "Input/StopCharListForToken.txt";
	private final String outputFolderPath = "Output/IndexFiles/";
	private final String outputFileName = "IndexDump";
	private final String outputFileExtension = ".csv";
	private final String endOfLine = "\n";
	
	public TreeMap<String, TreeMap<String, FieldValuesForPageId>> mainIndex; 
	private long pageCount;
	private final int pageWriteThreshold = 10;
	
	
	HashSet<String> stopWordSet;
	char[] stopCharArray;
	
	public ParseAndIndex(String filePath) {
		// TODO Auto-generated constructor stub
		this.wikiFilePath = filePath;
		mainIndex = new TreeMap<String, TreeMap<String,FieldValuesForPageId>>();
		stopWordSet = new HashSet<String>();
		
		pageCount=0;
		loadStopWordSet(stopWordListFilePath);
		loadStopCharSet(stopCharListFilePath);
	}
	
	
	public void loadStopCharSet(String stopCharFilePath)
	{
		ArrayList<String> tempStopChar = new ArrayList<String>();
		
		int i=0;
		
		BufferedReader br;
		try {
			
			br = new BufferedReader(new FileReader(stopCharFilePath));
			String line=null;
			
			while( (line=br.readLine()) != null)
			{	
				String value = line.toLowerCase();
				// No need to check for unique values as "Set" will contain unique values only
				tempStopChar.add(value);
			}
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //System.out.println(i);
		
		// Putting Stop Chars Read to Char List
		stopCharArray = new char[tempStopChar.size()];
		
		Iterator<String> iter = tempStopChar.iterator();
		int index = 0;
		
		while(iter.hasNext())
		{
		   char item = iter.next().toCharArray()[0];
		   stopCharArray[index] = item;
		   index++;
		   //System.out.println(item + " : " + index);
		}
		
		
		//System.out.println(" StopChar Count : " + tempStopChar.size());
		//System.out.println(" HashSet contains Zero : " + stopWordSet.contains("zero"));
	}
	
	
	public void loadStopWordSet(String stopWordFilePath)
	{
		int i=0;
		
		BufferedReader br;
		try {
			
			br = new BufferedReader(new FileReader(stopWordFilePath));
			String line=null;
			
			while( (line=br.readLine()) != null)
			{	
				String value = line.trim().toLowerCase();
				// No need to check for unique values as "Set" will contain unique values only
				stopWordSet.add(value);
			}
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //System.out.println(i);
		/*
		
		Iterator<String> setIterator = stopWordSet.iterator();
			
		while(setIterator.hasNext())
		{
		   String item = (String) setIterator.next();
		   i++;
		   //System.out.println(item + " : " + i);
		}
		*/
		
		//System.out.println(" HashSet Count : " + stopWordSet.size());
		//System.out.println(" HashSet contains Zero : " + stopWordSet.contains("zero"));
	}
	
	
	public void createIndexForTitle(String titleText , String pageId)
	{
		titleText = titleText.toLowerCase().replaceAll("[,\\.:;&-]", " ");
		
		// Remove all Non-Alphabetical Values
		titleText = titleText.replaceAll("[^a-z]", " ");
		
		String tokens[] = titleText.split("[ \t\n\r\f]");
		
		//region Indexing for Title 
		for(String str : tokens)
		{
			String key = str.trim();
			
			if (key.length() <= 2)
			{
				//System.err.println("Title Length <= 2 : " + key );
				continue;
			}
			else if (stopWordSet.contains(key))
			{
				//System.err.println("Title is StopWord : " + key );
				continue;
			}
			else
			{
				//key = key.replaceAll("[^a-zA-Z0-9]+","");
				//key = key.replaceAll("[^a-zA-Z]+","");
				
				if (mainIndex.containsKey(key))
				{
					TreeMap<String, FieldValuesForPageId> value = mainIndex.get(key);
					
					if (value.containsKey(pageId))
					{
						FieldValuesForPageId fieldValues = value.get(pageId);
						fieldValues.totalCount = fieldValues.totalCount + 1;
						
						if (fieldValues.titleCount==null)
						{
							fieldValues.titleCount = 1;
						}
						else
						{
							fieldValues.titleCount = fieldValues.titleCount + 1;
						}
						
						value.put(pageId, fieldValues);
						mainIndex.put(key,value);
					}
					else
					{
						// Create a New Node for Given Page in Given Token
						FieldValuesForPageId fieldValues = new FieldValuesForPageId();
						fieldValues.totalCount = 1;
						fieldValues.titleCount = 1;
						
						//System.err.println("Field Values :- "+fieldValues+"\n");
						value.put(pageId, fieldValues);
						mainIndex.put(key,value);
					}
				}
				else
				{
					// If Key does not exists in Index , 
					// then create a new entry in Index for new Token
					
					TreeMap<String, FieldValuesForPageId> value = new TreeMap<String, FieldValuesForPageId>();
					
					FieldValuesForPageId fieldValues = new FieldValuesForPageId();
					fieldValues.totalCount = 1;
					fieldValues.titleCount = 1;
					//fieldValues.textCount = 0;
					//fieldValues.infoBoxCount = 0;
					//fieldValues.linksCount = 0;
					//fieldValues.categoryCount = 0;
					
					value.put(pageId, fieldValues);
					mainIndex.put(key, value);
				}
			}
		}
	}
	
	
	public void createIndexForWikiText(String wikiText , String pageId)
	{
		System.out.println("1");	
		
		//System.out.println(wikiText);
		wikiText = PreprocessingTextForIndex.removeInfobox(wikiText).toLowerCase();
		//System.out.println("================");
		//System.out.println(wikiText);
				
		System.out.println("2");
		
		// To Replce All ' " . , : | [ ] ( ) 0-9 % / ? & _ by space 
		wikiText = wikiText.replaceAll("[\'\"\\.,:\\|\\]\\[\\)\\(0-9%/\\?&_;-]", " ");
		
		System.out.println("3");
		// To Replace All < > &nbsp; &gt; &lt; &amp; * - by space
		wikiText = wikiText.replace("<", " ").replace(">", " ").replace("nbsp;", " ").replace("gt;", " ").replace("lt;", " ").replace("amp;", " ").replace("*", " ").replace("-", " ");
		//System.out.println("4");
		// To Remove Single Line {{...}} occurrences
		wikiText = wikiText.replaceAll("\\{\\{[^\\}].*\\}\\}", " ");
		System.out.println("5");
		// To Remove Multiple Line {{...}} occurrences
		//wikiText = wikiText.replaceAll("\\{\\{([^\\}].*\n+)*\\}\\}", " ");
		System.out.println("6");
		// To convert "something1 = something2" into "something2" only
		// eg:- last = rhode ;    first = robert t ;    title = a history of warren county  indiana
		wikiText = wikiText.replaceAll("[ ]*[a-z]*[ ]*[=]", " ");
		System.out.println("7");
		// Remove All {{ , }} 
		wikiText = wikiText.replace("{{", " ").replace("}}", " ");
		System.out.println("8");
		// Remove all <ref> , < ref> , <ref > , < ref > occurrences
		wikiText = wikiText.replaceAll("\\<[ ]*ref[ ]*\\>", " ");
		System.out.println("9");
		// Remove ==history== , ==demographics== , ==references== , etc.
		wikiText = wikiText.replaceAll("[=][=][^=].*[=][=]", " ");
		System.out.println("10"); 
		
		// Remove all Non-Alphabetical Values
		wikiText = wikiText.replaceAll("[^a-z]", " ");
		
		String tempWords[] = wikiText.split("[ \t\n\r\f]");
		
		
		int i=0;
		for (String str : tempWords)
		{
			i++;
			/*
			if (str.matches("\\{\\{[^\\}]*\\}\\}"))
			{
				System.out.println(str + " : Yes");
			}
			else if (str.matches(".*[=][=][A-Za-z]*[=][=].*"))
			{
				System.out.println(str + " : Yes");
			}
			else if (str.matches("^$"))
			{
				//System.out.println(str + " : Yes");
			}
			*/
			if (str.matches("^$") || str.length()<=2)
			{
				continue;
			}
			else
			{
				//str = str.replaceAll("[\'\"\\]\\[\\|,\\.:)(\\}\\{0-9/]", " ");
				//str = str.replace("<", " ").replace(">", " ").replace("&nbsp;", " ").replace("&gt;", " ").replace("&lt;", " ").replace("&amp;", " ").replace("*", " ").replace("-", " ");
				//str = str.replaceAll("[^a-zA-Z-]", " ");
				//str = str.replaceAll("^[-].*", " ");
				
						
						
				if (str.matches("^[=][=].*") || str.matches(".*[=][=]$"))
				{
					// Do not Index after "==references==" point
					if (str.equalsIgnoreCase("==references=="))
					{
						break;
					}
					else
					{
						// For all other ==history== , ==art and culture== , do not index the title
						//System.out.println(i+":"+str.trim()+" : Yes");
						continue;
					}
				}
				else if (str.matches(".*[=][^=].*"))
				{
					// Single "=" either just before or just after text then replace it by space. 
					str=str.replace("=", " ");
					//System.out.println(i+":"+str.trim()+" : YYes");
				}

				/*
				if(str.matches(".*[&][_].*"))
				{
					// Do Not Index terms like &_county , , etc
					continue;
				}
				*/
				
				// Given entry in the array already splited by white spaces , may still contain internal space
				// So loop through it and index it
				
				String tokens[] = str.split("[ \t\n\r\f]");
				
				for (String key : tokens)
				{
					// Module to make Index Entry in MAIN-INDEX
					
					if (key.length()<=2 || stopWordSet.contains(key))
					{
						//System.err.println("StopWord ; " + key );
						continue;
					}
					else
					{				
						if (mainIndex.containsKey(key))
						{
							TreeMap<String, FieldValuesForPageId> value = mainIndex.get(key);
					
							if (value.containsKey(pageId))
							{
								FieldValuesForPageId fieldValues = value.get(pageId);
								fieldValues.totalCount = fieldValues.totalCount + 1;
								
								if (fieldValues.textCount==null)
								{
									fieldValues.textCount = 1;
								}
								else
								{
									fieldValues.textCount = fieldValues.textCount + 1;
								}
								
								value.put(pageId, fieldValues);
								mainIndex.put(key,value);
								//System.out.println(key + "Level1");
							}
							else
							{
								// Create a New Node for Given Page in Given Token
								FieldValuesForPageId fieldValues = new FieldValuesForPageId();
								fieldValues.totalCount = 1;
								fieldValues.textCount = 1;
								
								//System.err.println("Field Values :- "+fieldValues+"\n");
								value.put(pageId, fieldValues);
								mainIndex.put(key,value);
								//System.err.println(mainIndex.get(key));
								//System.out.println(key + "Level2");
							}
						}
						else
						{
							// If Key does not exists in Index , 
							// then create a new entry in Index for new Token
							
							TreeMap<String, FieldValuesForPageId> value = new TreeMap<String, FieldValuesForPageId>();
							
							FieldValuesForPageId fieldValues = new FieldValuesForPageId();
							fieldValues.totalCount = 1;
							fieldValues.textCount = 1;
							//fieldValues.textCount = 0;
							//fieldValues.infoBoxCount = 0;
							//fieldValues.linksCount = 0;
							//fieldValues.categoryCount = 0;
							
							value.put(pageId, fieldValues);
							mainIndex.put(key, value);
							//System.out.println(key + "Home");
						}
					}
					
					
					
				}
				
			}
		}
		
	}
	
	
	public void createIndexForOutLinks(String outLinkText , String pageId)
	{	
		outLinkText = outLinkText.toLowerCase().replaceAll("[,\\.:\\)\\(\\]\\[0-9&;-]", " ");
		
		// Remove all Non-Alphabetical Values
		outLinkText = outLinkText.replaceAll("[^a-z]", " ");
		
		String tokens[] = outLinkText.split("[ \t\n\r\f]");
		
		//region Indexing for Title 
		for(String str : tokens)
		{
			String key = str.trim();
			
			if (key.length() <= 2)
			{
				//System.err.println("OutLink Length <= 2 : " + key );
				continue;
			}
			else if (stopWordSet.contains(key))
			{
				//System.err.println("OutLink is StopWord : " + key );
				continue;
			}
			else
			{
				//key = key.replaceAll("[^a-zA-Z0-9]+","");
				//key = key.replaceAll("[^a-zA-Z]+","");
				
				if (mainIndex.containsKey(key))
				{
					TreeMap<String, FieldValuesForPageId> value = mainIndex.get(key);
			
					if (value.containsKey(pageId))
					{
						FieldValuesForPageId fieldValues = value.get(pageId);
						fieldValues.totalCount = fieldValues.totalCount + 1;
						
						if (fieldValues.linksCount==null)
						{
							fieldValues.linksCount = 1;
						}
						else
						{
							fieldValues.linksCount = fieldValues.linksCount + 1;
						}
						
						value.put(pageId, fieldValues);
						mainIndex.put(key,value);
					}
					else
					{
						// Create a New Node for Given Page in Given Token
						FieldValuesForPageId fieldValues = new FieldValuesForPageId();
						fieldValues.totalCount = 1;
						fieldValues.linksCount = 1;
						
						//System.err.println("Field Values :- "+fieldValues+"\n");
						value.put(pageId, fieldValues);
						mainIndex.put(key,value);
					}
				}
				else
				{
					// If Key does not exists in Index , 
					// then create a new entry in Index for new Token
					
					TreeMap<String, FieldValuesForPageId> value = new TreeMap<String, FieldValuesForPageId>();
					
					FieldValuesForPageId fieldValues = new FieldValuesForPageId();
					fieldValues.totalCount = 1;
					fieldValues.linksCount = 1;
					//fieldValues.textCount = 0;
					//fieldValues.infoBoxCount = 0;
					//fieldValues.linksCount = 0;
					//fieldValues.categoryCount = 0;
					
					value.put(pageId, fieldValues);
					mainIndex.put(key, value);
				}
			}
		}
	}
	
	
	public void createIndexForCategories(String categoriesText , String pageId)
	{	
		categoriesText = categoriesText.toLowerCase().replaceAll("[,\\.:\\)\\(\\]\\[0-9&;-]", " ");
		
		// Remove all Non-Alphabetical Values
		categoriesText = categoriesText.replaceAll("[^a-z]", " ");
		
		String tokens[] = categoriesText.split("[ \t\n\r\f]");
		
		//region Indexing for Title 
		for(String str : tokens)
		{
			String key = str.trim();
			
			if (key.length() <= 2)
			{
				//System.err.println("Categories Length <= 2 : " + key );
				continue;
			}
			else if (stopWordSet.contains(key))
			{
				//System.err.println("Categories is StopWord : " + key );
				continue;
			}
			else
			{
				//key = key.replaceAll("[^a-zA-Z0-9]+","");
				//key = key.replaceAll("[^a-zA-Z]+","");
				
				if (mainIndex.containsKey(key))
				{
					TreeMap<String, FieldValuesForPageId> value = mainIndex.get(key);
			
					if (value.containsKey(pageId))
					{
						FieldValuesForPageId fieldValues = value.get(pageId);
						fieldValues.totalCount = fieldValues.totalCount + 1;
						
						if (fieldValues.categoryCount==null)
						{
							fieldValues.categoryCount = 1;
						}
						else
						{
							fieldValues.categoryCount = fieldValues.categoryCount + 1;
						}
						
						value.put(pageId, fieldValues);
						mainIndex.put(key,value);
					}
					else
					{
						// Create a New Node for Given Page in Given Token
						FieldValuesForPageId fieldValues = new FieldValuesForPageId();
						fieldValues.totalCount = 1;
						fieldValues.categoryCount = 1;
						
						//System.err.println("Field Values :- "+fieldValues+"\n");
						value.put(pageId, fieldValues);
						mainIndex.put(key,value);
					}
				}
				else
				{
					// If Key does not exists in Index , 
					// then create a new entry in Index for new Token
					
					TreeMap<String, FieldValuesForPageId> value = new TreeMap<String, FieldValuesForPageId>();
					
					FieldValuesForPageId fieldValues = new FieldValuesForPageId();
					fieldValues.totalCount = 1;
					fieldValues.categoryCount = 1;
					//fieldValues.textCount = 0;
					//fieldValues.infoBoxCount = 0;
					//fieldValues.linksCount = 0;
					//fieldValues.categoryCount = 0;
					
					value.put(pageId, fieldValues);
					mainIndex.put(key, value);
				}
			}
		}
		
	}
	
	
	public void createIndexForInfobox(String infoboxText , String pageId)
	{	
		// Remove { } [ ] , _ . 0-9 : ; / ' " -
		infoboxText = infoboxText.toLowerCase().replaceAll("[\'\"\\}\\{\\]\\[\\._0-9,:;/\\)\\(&-]", " ");

		// Split by "\\|" into "key=value" pair and some other key words
		// eg:- |monitored by = American Standard Institute | ASI
		//      |size		=
		//		|location	=
		String tokens[] = infoboxText.split("[\\|]");
		
		//region Indexing for Title 
		for(String str : tokens)
		{
			// Trim and replace elements of the form "|...=value" to "<space> value" form
			str = str.replaceAll("[^=]*[=]", " ");
			
			// Remove all Non-Alphabetical Values
			str = str.replaceAll("[^a-z]", " ");

			String temp[] = str.split("[ \t\n\r\f]");
			
			for (String key : temp)
			{
				if (key.length() <= 2)
				{
					//System.err.println("InfoBox Length <= 2 : " + key );
					continue;
				}
				else if (stopWordSet.contains(key))
				{
					//System.err.println("InfoBox is StopWord : " + key );
					continue;
				}
				else
				{
					//key = key.replaceAll("[^a-zA-Z0-9]+","");
					//key = key.replaceAll("[^a-zA-Z]+","");
					
					if (mainIndex.containsKey(key))
					{
						TreeMap<String, FieldValuesForPageId> value = mainIndex.get(key);
				
						if (value.containsKey(pageId))
						{
							FieldValuesForPageId fieldValues = value.get(pageId);
							fieldValues.totalCount = fieldValues.totalCount + 1;
							
							if (fieldValues.infoBoxCount==null)
							{
								fieldValues.infoBoxCount = 1;
							}
							else
							{
								fieldValues.infoBoxCount = fieldValues.infoBoxCount + 1;
							}
							
							value.put(pageId, fieldValues);
							mainIndex.put(key,value);
						}
						else
						{
							// Create a New Node for Given Page in Given Token
							FieldValuesForPageId fieldValues = new FieldValuesForPageId();
							fieldValues.totalCount = 1;
							fieldValues.infoBoxCount = 1;
							
							//System.err.println("Field Values :- "+fieldValues+"\n");
							value.put(pageId, fieldValues);
							mainIndex.put(key,value);
						}
					}
					else
					{
						// If Key does not exists in Index , 
						// then create a new entry in Index for new Token
						
						TreeMap<String, FieldValuesForPageId> value = new TreeMap<String, FieldValuesForPageId>();
						
						FieldValuesForPageId fieldValues = new FieldValuesForPageId();
						fieldValues.totalCount = 1;
						fieldValues.infoBoxCount = 1;
						//fieldValues.textCount = 0;
						//fieldValues.infoBoxCount = 0;
						//fieldValues.linksCount = 0;
						//fieldValues.categoryCount = 0;
						
						value.put(pageId, fieldValues);
						mainIndex.put(key, value);
					}
				}
			}
			
		}
		
	}
	
	
	public void createIndex()
	{
		try {
			
			WikiXMLSAXParser.parseWikipediaDump(this.wikiFilePath, new PageCallbackHandler() {
				
				@Override
				public void process(WikiPage page) 
				{
					// TODO Auto-generated method stub
					String pageId = page.getID();
					String titleText = page.getTitle();
					String wikiText = page.getText();
					String outLinkText = page.getLinks().toString();
					String categoriesText = page.getCategories().toString();
					InfoBox infoBox = page.getInfoBox();
					String infoboxText;
					
					if (infoBox==null)
					{
						infoboxText = "";
					}
					else
					{
						infoboxText = infoBox.dumpRaw();
					}
					
					
					/*
					System.out.println("\n\n----- Page Id ------");					 
					System.out.println(page.getID());
					System.out.println("\n----- Page Title ------");					 
					System.out.println(page.getTitle());
					System.out.println("----- Page Text ------");
					System.out.println(page.getText());
					System.out.println("----- Wiki Text ------");
					System.out.println(page.getWikiText());
					System.out.println("----- Page InfoBox ------");
					System.out.println(infoboxText);
					System.out.println("\n\n----- Page Links ------\n");
					System.out.println(page.getLinks());
					System.out.println("----- Page Categories ------");
					System.out.println(page.getCategories());
					*/
					
					// Indexing Each Part separately
					System.out.println("In Title");
					createIndexForTitle(titleText,pageId);
					System.out.println("In Text");
					createIndexForWikiText(wikiText,pageId);
					System.out.println("In OutLink");
					createIndexForOutLinks(outLinkText,pageId);
					System.out.println("In Categories");
					createIndexForCategories(categoriesText,pageId);
					System.out.println("In Infobox");
					createIndexForInfobox(infoboxText,pageId);
					
					
					ParseAndIndex.this.pageCount++;
					
					
					if (ParseAndIndex.this.pageCount%ParseAndIndex.this.pageWriteThreshold == 0)
					{
						System.out.println("-------Dumping Files-------");
						
						// Write to files (Sorted Index of Multiple Pages) on Disk
						
						FileWriter fileWriter = null ;
						
					    try 
					    {
					    	// Writing files in Plain Text
					    	fileWriter = new FileWriter(outputFolderPath+"//"+outputFileName+(pageCount/pageWriteThreshold)+outputFileExtension);
					    	
					    	
					    	Set<Entry<String, TreeMap<String, FieldValuesForPageId>>> set = mainIndex.entrySet();
						    // Get an iterator
						    Iterator<Entry<String, TreeMap<String, FieldValuesForPageId>>> iter = set.iterator();
						    // Display elements
						    
						    while(iter.hasNext()) {
						    	Map.Entry<String, TreeMap<String, FieldValuesForPageId>> mainTreeInstance = (Map.Entry<String, TreeMap<String, FieldValuesForPageId>>)iter.next();
						    	
						    	String token = mainTreeInstance.getKey();
						    	fileWriter.write(token);
						    	
						    	Set<Entry<String, FieldValuesForPageId>> subset = mainTreeInstance.getValue().entrySet();
							    // Get an iterator
							    Iterator<Entry<String, FieldValuesForPageId>> subIter = subset.iterator();
							    							    
							    while(subIter.hasNext()) {
							    	Map.Entry<String, FieldValuesForPageId> subTreeInstance = (Map.Entry<String, FieldValuesForPageId>)subIter.next();
							    	//System.out.print(subTreeInstance.getKey() + " : ");
							    	//System.out.print(subTreeInstance.getValue());
							    	//System.out.print(" ; ");
							    	fileWriter.write(","+subTreeInstance.getKey()+","+subTreeInstance.getValue());
							    }
							    fileWriter.write(endOfLine);
						    }
						    
						    //mainIndex = new TreeMap<String, TreeMap<String,FieldValuesForPageId>>();
						    mainIndex.clear();
						    
					    } 
					    catch (FileNotFoundException e) 
					    {
					    	e.printStackTrace();
					    } 
					    catch (IOException e) 
					    {
					    	e.printStackTrace();
					    }
					    finally
					    {
					    	try {
								fileWriter.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					    }
					    
						
					}
					
				}
			});
			
			
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				

	}
	
	
	public void mergeIndexFiles()
	{
		// Sort the Files in Increasing Order of File-Size 
	}

	
	public void displayIndex()
	{
		//region Reading from MainIndex and Displaying
		System.out.println("In Display");

		Set<Entry<String, TreeMap<String, FieldValuesForPageId>>> set = mainIndex.entrySet();
	    // Get an iterator
	    Iterator<Entry<String, TreeMap<String, FieldValuesForPageId>>> i = set.iterator();
	    // Display elements
	    
	    int counter = 0;
	    
	    while(i.hasNext()) {
	    	Map.Entry<String, TreeMap<String, FieldValuesForPageId>> me = (Map.Entry<String, TreeMap<String, FieldValuesForPageId>>)i.next();
	    	
	    	String token = me.getKey();
	    	
	    	Set<Entry<String, FieldValuesForPageId>> subset = me.getValue().entrySet();
		    // Get an iterator
		    Iterator<Entry<String, FieldValuesForPageId>> subi = subset.iterator();
		    
		    counter++;
		    System.out.print("\n"+counter+token +" :: ");
		    
		    while(subi.hasNext()) {
		    	Map.Entry<String, FieldValuesForPageId> subme = (Map.Entry<String, FieldValuesForPageId>)subi.next();
		    	System.out.print(subme.getKey() + " : ");
		    	System.out.print(subme.getValue());
		    	System.out.print(" ; ");
		    }
		    
	    }
	    
	    //endregion
		
		
		/*
		 * Reading from all the files created for Index
		 */
		
	   // Get all file 
		/*
		String fileName = null;
        boolean bName = false;
        int iCount = 0;
        File dir = new File(outputFolderPath);
        File[] files = dir.listFiles();
        System.out.println("List Of Files ::");

        for (File f : files) {

            fileName = f.getName();
            System.out.println(fileName);

        }
		*/
		
		
		/*
		FileInputStream fis;
		ObjectInputStream ois;
		
		try 
		{
			fis = new FileInputStream(outputFolderPath+"//"+outputFileName+"1"+outputFileExtension);
			ois = new ObjectInputStream(fis);
			Map<String,String> anotherList = (Map<String,String>) ois.readObject();
			ois.close();
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/
		

	     
		
	}
	
		

}
