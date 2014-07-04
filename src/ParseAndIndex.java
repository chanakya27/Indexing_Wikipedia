import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import java.util.*;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;


public class ParseAndIndex implements Serializable {

	private String wikiFilePath; 
	private final String stopWordListFilePath = "Input/StopWordList.txt";
	private final String stopCharListFilePath = "Input/StopCharListForToken.txt";
	private final String outputFolderPath = "Output/Exp";
	private final String outputFileName = "IndexDump";
	private final String outputFileExtension = ".csv";
	private final String endOfLine = "\n";
	
	private long outputFileCounter;
	
	public TreeMap<String, TreeMap<String, FieldValuesForPageId>> mainIndex; 
	private long pageCount;
	private final int pageWriteThreshold = 1000;
	private Porter stemmer = new Porter();
	StringBuilder sbWriteDataToFile = new StringBuilder();
	
	
	HashSet<String> stopWordSet;
	char[] stopCharArray;

	Comparator<FileNameAndSize> comparator = new Comparator<FileNameAndSize>() {
	    public int compare(FileNameAndSize v1, FileNameAndSize v2) {
	    	if (v1.fileSize>v2.fileSize)
	    	{
	    		return 1;
	    	}
	    	else if (v1.fileSize<v2.fileSize)
	    	{
	    		return -1;
	    	}
	    	else
	    	{
	    		return 0;
	    	}
	    }
	};

	
	public ParseAndIndex(String filePath) {
		// TODO Auto-generated constructor stub
		this.wikiFilePath = filePath;
		mainIndex = new TreeMap<String, TreeMap<String,FieldValuesForPageId>>();
		stopWordSet = new HashSet<String>();
		
		pageCount = 13295000;
		outputFileCounter = 50005;
		
		loadStopWordSet(stopWordListFilePath);
		loadStopCharSet(stopCharListFilePath);
	}
	
	
	public void tester()
	{
		String str1 = "a,b,c,d,e,f,g,h";
		String str2 = ",a,b,c,d,e,f,g,h,";
		
		String str11[] = str1.split(",");
		String str22[] = str2.split(",");
		
		
		System.out.println("========");
		
		for (String s : str11)
		{
			System.out.println(s);
		}
		
		System.out.println("========"+str11.length);
		
		for (String s : str22)
		{
			System.out.println(s);
		}
		
		System.out.println("========"+str22.length);
		
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
		//titleText = titleText.toLowerCase().replaceAll("[,\\.:;&-]", " ");
		titleText = titleText.toLowerCase();
		// Remove all Non-Alphabetical Values
		titleText = titleText.replaceAll("[^a-z]", " ");
		
		String tokens[] = titleText.split("[ \t\n\r\f]");
		
		//region Indexing for Title 
		for(String str : tokens)
		{
			String key = str;
						
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
				key = stemmer.stripAffixes(key);
				if (key.length()<=2)
				{
					//System.err.println("StopWord ; " + key );
					continue;
				}
				
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
				
		System.out.println("2");
		
		// Remove all Non-Alphabetical Values
		wikiText = wikiText.replaceAll("[^a-z]", " ");
		
		String tempWords[] = wikiText.split("[ \t\n\r\f]");
		
		
		int i=0;
		for (String str : tempWords)
		{
			i++;
				
			if (str.length()<=2)
			{
				continue;
			}
			else
			{

				// For catching "references"
				if (str.equalsIgnoreCase("references"))
				{
					return;
				}
								
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
						key = stemmer.stripAffixes(key);
						if (key.length()<=2)
						{
							//System.err.println("StopWord ; " + key );
							continue;
						}
						
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
		//outLinkText = outLinkText.toLowerCase().replaceAll("[,\\.:\\)\\(\\]\\[0-9&;-]", " ");
		outLinkText = outLinkText.toLowerCase();
		
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
				
				key = stemmer.stripAffixes(key);
				if (key.length()<=2)
				{
					//System.err.println("StopWord ; " + key );
					continue;
				}
				
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
					
					value.put(pageId, fieldValues);
					mainIndex.put(key, value);
				}
			}
		}
	}
	
	
	public void createIndexForCategories(String categoriesText , String pageId)
	{	
		//categoriesText = categoriesText.toLowerCase().replaceAll("[,\\.:\\)\\(\\]\\[0-9&;-]", " ");
		categoriesText = categoriesText.toLowerCase();
		
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
			
				
				key = stemmer.stripAffixes(key);
				if (key.length()<=2)
				{
					//System.err.println("StopWord ; " + key );
					continue;
				}
				
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
					
					
					key = stemmer.stripAffixes(key);
					if (key.length()<=2)
					{
						//System.err.println("StopWord ; " + key );
						continue;
					}
					
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
	    	 
	    	SAXParserFactory factory = SAXParserFactory.newInstance();
	    	SAXParser saxParser = factory.newSAXParser();
	    	
	    	DefaultHandler handler = new DefaultHandler() {

	    		boolean titleStatus = false;
		    	boolean idStatus = false;
		    	boolean idFlag = false;
		    	boolean textStatus = false;
		    	
		    	StringBuilder sbTitle = new StringBuilder();
		    	StringBuilder sbId = new StringBuilder();
		    	StringBuilder sbWikiText = new StringBuilder();	    	
		    	StringBuilder sbInfoText = new StringBuilder();
		    	StringBuilder sbOutLink = new StringBuilder();
		    	StringBuilder sbCategory = new StringBuilder();
		    	
		    	
				String pageId = "";
				String titleText = "";
				String wikiText = "";
				String outLinkText = "";
				String categoriesText = "";
				String infoboxText = "";
	    		
	    	public void startElement(String uri, String localName,String tag, Attributes attributes) throws SAXException 
	        {
	    		
	    		//System.out.println("Start Element :" + tag);
	     
	    		if (tag.equalsIgnoreCase("title")) {
	    			titleStatus = true;
	    			idFlag = true;
	    		}
	    		else if (tag.equalsIgnoreCase("id")) {
	    			idStatus = true;
	    		}
	    		else if (tag.equalsIgnoreCase("text")) {
	    			textStatus = true;
	    		}	
	     
	    	}
	     
	    	
	    	public void endElement(String uri, String localName,
	    		String qName) throws SAXException {
	     
	    		//System.out.println("End Element :" + qName);
	    		if (titleStatus) {
	    			titleStatus = false;
	    			
	    			titleText = sbTitle.toString().replace("\n", " ").trim().toLowerCase();
	    			
	    			sbTitle.setLength(0);
	    		}
	    		else if (idFlag && idStatus) 
	    		{	    			
	    			idStatus = false;
	    			idFlag = false;

	    			pageId= sbId.toString().replace("\n", " ").trim().toLowerCase();
	    			
	    			sbId.setLength(0);
	    		}	    		
	    		else if (textStatus)
	    		{
	    			textStatus = false;
	    			
	    			// As Multiple Id-Tags are present that make idStatus true even when its not required.
	    			idStatus = false;
	    			idFlag = false;
	    			
	    			String tempStr = sbWikiText.toString().trim().replace("\n", " ").toLowerCase();
	    			//System.out.println(tempStr);	    
	    			
	    			// Read InfoBoxText and OutLinksText also... 
	    			int counter = 0 , openBraceCount = 0 , squareBracketCount = 0 ;
	    			boolean skipForTag = false , readLink=false;
	    			
	    			for (char c : tempStr.toCharArray())
	    			{
	    				counter++;
	    				
	    				if (c=='{')
	    				{
	    					openBraceCount++;
	    				}
	    				else if (c=='}')
	    				{
	    					openBraceCount--;
	    				}
	    				else if (c=='<')
	    				{
	    					skipForTag = true;
	    				}
	    				else if (c=='>')
	    				{
	    					skipForTag = false;
	    				}
	    				else if (c=='[')
	    				{
	    					squareBracketCount++;
	    					
	    					if(squareBracketCount==2)
		    				{
		    					readLink=true;
		    				}
	    				}
	    				else if (c==']')
	    				{
	    					squareBracketCount--;
	    					
	    					if(squareBracketCount==0)
		    				{
		    					readLink=false;
		    					sbOutLink.append(" ");
		    				}
	    				}
	    				else if (readLink)
	    				{
	    					sbOutLink.append(c);
	    				}
	    				else if (!skipForTag && !readLink)
	    				{
	    					sbInfoText.append(c);
	    				}
	    				
	    				//Should be Independent-If , as string always starts with "{{infobox..."
	    				if (openBraceCount==0)
	    				{
	    					break;
	    				}
	    			}
	    			
	    			infoboxText = sbInfoText.toString();
	    			sbInfoText.setLength(0);
	    			//System.out.println("InfoboxText :: "+infoboxText);
	    			
	    			openBraceCount = 0 ; 
	    			squareBracketCount = 0 ;
	    			skipForTag = false ;
	    			readLink=false;
	    			
	    			boolean skipForBrace = false;
	    			char c = ' ';
	    			
	    			// To Work on Remaining Part (After Removal of InfoBox Part)
	    			sbWikiText.setLength(0);
	    			
	    			StringBuilder sbCheckForReferences = new StringBuilder();
	    			StringBuilder sbAmbiguosLinkCategory = new StringBuilder();
	    			
	    			// Read Elements for WikiText, OutlinkText  and skip {{...}} , <...>
	    			for (int i = counter ; i < tempStr.length() ; i++)
	    			{
	    				c = tempStr.charAt(i);
	    				counter++;
	    				
	    				if (c=='{')
	    				{
	    					openBraceCount++;
	    					skipForBrace = true;
	    				}
	    				else if (c=='}')
	    				{
	    					openBraceCount--;
	    					
	    					if (openBraceCount==0)
	    					{
	    						skipForBrace=false;
	    					}
	    				}
	    				else if (c=='<')
	    				{
	    					skipForTag = true;
	    				}
	    				else if (c=='>')
	    				{
	    					skipForTag = false;
	    						    					
	    					if(sbCheckForReferences.toString().replace("/", " ").trim().equalsIgnoreCase("references"))
	    					{
		    					sbCheckForReferences.setLength(0);
	    						break;
	    					}
	    					sbCheckForReferences.setLength(0);
	    				}
	    				else if (c=='[')
	    				{
	    					squareBracketCount++;
	    					
	    					if(squareBracketCount==2)
		    				{
		    					readLink=true;
		    				}
	    				}
	    				else if (c==']')
	    				{
	    					squareBracketCount--;
	    					readLink=false;
	    					
	    					if(squareBracketCount==0)
		    				{
	    						if (sbAmbiguosLinkCategory.toString().contains("category"))
	    						{
	    							String lastString = "";
	    			    			
	    			    			for(String str : tempStr.split(":|\\[\\[|\\]\\]"))
	    			    			{
	    			    				if (lastString.equalsIgnoreCase("category"))
	    			    				{
	    			    					sbCategory.append(str + " ");
	    			    				}
	    			    				
	    			    				lastString = str;

	    			    			}
	    			    			break;
	    						}
	    						else
	    						{
	    							sbOutLink.append(c+" ");
	    						}
		    					
		    				}
	    				}
	    				else if (readLink)
	    				{
	    					sbAmbiguosLinkCategory.append(c);
	    				}
	    				else if (!skipForTag && !skipForBrace && !readLink)
	    				{
	    					sbWikiText.append(c);
	    				}
	    				else if (skipForTag)
	    				{
	    					sbCheckForReferences.append(c);
	    				}
	    					
	    			}
	    			
	    			outLinkText = sbOutLink.toString();
	    			wikiText = sbWikiText.toString();
	    			
	    			sbOutLink.setLength(0);
	    			sbWikiText.setLength(0);
	    			
	    			//System.out.println("WikiText :: "+wikiText);
	    			//System.out.println("OutLink :: "+outLinkText);
	    			
	    			
	    			// For Finding Out the Category
	    			tempStr = tempStr.substring(counter);
	    			//infoboxText = tempStr.
	    			//System.out.println("Printing Rest :: " + tempStr);
	    			String lastString = "";
	    			
	    			for(String str : tempStr.split(":|\\[\\[|\\]\\]"))
	    			{
	    				if (lastString.equalsIgnoreCase("category"))
	    				{
	    					sbCategory.append(str + " ");
	    				}
	    				
	    				lastString = str;

	    			}
	    			
	    			categoriesText = sbCategory.toString();
	    			//System.out.println("Category :: " + categoriesText);
	    			
	    			sbCategory.setLength(0);
	    			
	    			
	    			// Indexing Each Part separately
					//System.out.println("======In Title");
					//System.out.println(titleText);
					createIndexForTitle(titleText,pageId);
					
					//System.out.println("======In Text");
					//System.out.println(wikiText);
					createIndexForWikiText(wikiText,pageId);
					
					//System.out.println("======In OutLink");
					//System.out.println(outLinkText);
					createIndexForOutLinks(outLinkText,pageId);
					
					//System.out.println("======In Categories");
					//System.out.println(categoriesText);
					createIndexForCategories(categoriesText,pageId);
					
					//System.out.println("======In Infobox");
					//System.out.println(infoboxText);
					createIndexForInfobox(infoboxText,pageId);
	    			
	    			
					// Writing Index to File
					ParseAndIndex.this.pageCount++;
					
					
					if (ParseAndIndex.this.pageCount%ParseAndIndex.this.pageWriteThreshold == 0)
					{
						
						// Write to files (Sorted Index of Multiple Pages) on Disk
						
						FileWriter fileWriter = null ;
						
					    try 
					    {					    	
					    	Set<Entry<String, TreeMap<String, FieldValuesForPageId>>> set = mainIndex.entrySet();
						    // Get an iterator
						    Iterator<Entry<String, TreeMap<String, FieldValuesForPageId>>> iter = set.iterator();
						    // Display elements
						    
						    while(iter.hasNext()) {
						    	Map.Entry<String, TreeMap<String, FieldValuesForPageId>> mainTreeInstance = (Map.Entry<String, TreeMap<String, FieldValuesForPageId>>)iter.next();
						    	
						    	String token = mainTreeInstance.getKey();
						    	sbWriteDataToFile.append(token);
						    	
						    	Set<Entry<String, FieldValuesForPageId>> subset = mainTreeInstance.getValue().entrySet();
							    // Get an iterator
							    Iterator<Entry<String, FieldValuesForPageId>> subIter = subset.iterator();
							    							    
							    while(subIter.hasNext()) {
							    	Map.Entry<String, FieldValuesForPageId> subTreeInstance = (Map.Entry<String, FieldValuesForPageId>)subIter.next();
							    	//System.out.print(subTreeInstance.getKey() + " : ");
							    	//System.out.print(subTreeInstance.getValue());
							    	//System.out.print(" ; ");
							    	sbWriteDataToFile.append(","+subTreeInstance.getKey()+","+subTreeInstance.getValue());
							    }
							    
							    sbWriteDataToFile.append(endOfLine);
						    }
						    
						    System.out.println("-------Dumping Files-------");
						    
						    // Writing files in Plain Text
					    	fileWriter = new FileWriter(outputFolderPath+"//"+outputFileName+(pageCount/pageWriteThreshold)+outputFileExtension);
						    fileWriter.write(sbWriteDataToFile.toString());

						    
						    sbWriteDataToFile.setLength(0);
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
	     
	    	}
	     
	    	
	    	
	    	public void characters(char ch[], int start, int length) throws SAXException {
	     
	    		if (titleStatus) {
	    			//System.out.println("Title : " + new String(ch, start, length));
	    			sbTitle.append(new String(ch, start, length));
	    		}
	    		else if (idStatus && idFlag) 
	    		{
	    			//System.out.println("Id : " + new String(ch, start, length));
	    			sbId.append(new String(ch, start, length));
	    		}	    		
	    		else if (textStatus) 
	    		{
	    			//System.out.println("Text : " + new String(ch, start, length));
	    			sbWikiText.append(new String(ch, start, length));
	    		}
	    	}
	     
	         };
	         
	           saxParser.parse(Start.wikiXMLFilePath, handler);
	         //saxParser.parse(new InputSource(new StringReader("Input/1page.xml")), handler);
	     
	         } catch (Exception e) {
	           e.printStackTrace();
	         }
	     
	    
		// For Writing the Index of Last Iteration
		FileWriter fileWriter = null ;
				
	    try 
	    {	
	    	Set<Entry<String, TreeMap<String, FieldValuesForPageId>>> set = mainIndex.entrySet();
		    // Get an iterator
		    Iterator<Entry<String, TreeMap<String, FieldValuesForPageId>>> iter = set.iterator();
		    // Display elements
			    
		    while(iter.hasNext()) {
		    	Map.Entry<String, TreeMap<String, FieldValuesForPageId>> mainTreeInstance = (Map.Entry<String, TreeMap<String, FieldValuesForPageId>>)iter.next();
				    	
		    	String token = mainTreeInstance.getKey();
		    	sbWriteDataToFile.append(token);
		    	
		    	Set<Entry<String, FieldValuesForPageId>> subset = mainTreeInstance.getValue().entrySet();
			    // Get an iterator
			    Iterator<Entry<String, FieldValuesForPageId>> subIter = subset.iterator();
			    							    
			    while(subIter.hasNext()) {
			    	Map.Entry<String, FieldValuesForPageId> subTreeInstance = (Map.Entry<String, FieldValuesForPageId>)subIter.next();
			    	//System.out.print(subTreeInstance.getKey() + " : ");
			    	//System.out.print(subTreeInstance.getValue());
			    	//System.out.print(" ; ");
			    	sbWriteDataToFile.append(","+subTreeInstance.getKey()+","+subTreeInstance.getValue());
			    }

			    sbWriteDataToFile.append(endOfLine);
				
		    }
		
		    if (sbWriteDataToFile.toString().trim().length()>2)
		    {
		    	// Writing files in Plain Text
		    	fileWriter = new FileWriter(outputFolderPath+"//"+outputFileName+(pageCount/pageWriteThreshold + 1)+outputFileExtension);
		    	
		    	//outputFileCounter = pageCount/pageWriteThreshold + 2;
		    	
		    	fileWriter.write(sbWriteDataToFile.toString());
		    	System.out.println("Hi");
		    }
		    
		    sbWriteDataToFile.setLength(0);
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
		  	try 
		  	{
		  		if (fileWriter != null)
		  		{
		  			fileWriter.close();
		  		}
			} 
		  	catch (IOException e) 
		  	{
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
	}
	
	
	public ArrayList<FileNameAndSize> readFileNameAndSize()
	{
		ArrayList<FileNameAndSize> fileSizes = new ArrayList<FileNameAndSize>();
		
		FileNameAndSize f1 = new FileNameAndSize("abc",423L);
		FileNameAndSize f2 = new FileNameAndSize("aby",623L);
		FileNameAndSize f3 = new FileNameAndSize("abe",223L);
		FileNameAndSize f4 = new FileNameAndSize("abz",823L);
		FileNameAndSize f5 = new FileNameAndSize("aba",123L);
		
		fileSizes.add(f1);
		fileSizes.add(f2);
		fileSizes.add(f3);
		fileSizes.add(f4);
		fileSizes.add(f5);
		
		return fileSizes;
	}
	
	
	public ArrayList<FileNameAndSize> getAllFilesFromFolder(String folderPath)
	{
		File folder = new java.io.File(folderPath);
		
		ArrayList<FileNameAndSize> value = new ArrayList<FileNameAndSize>();
		
		for (File file : folder.listFiles()) 
		{
	        if (file.isFile())
	        {
	        	FileNameAndSize fNameAndSize = new FileNameAndSize(file.getName(),file.length());
	        	value.add(fNameAndSize);
	        }	      
	    }
		
		return value;
		
		
		
		/*
		Path dir = FileSystems.getDefault().getPath(folderPath);
		
		DirectoryStream<Path> stream = null;
		
		try 
		{
			stream = Files.newDirectoryStream(dir);
			
			for (Path path : stream) {
				   System.out.println( path.getFileName() + " , " + path.getFileName().);
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (stream!=null)
			{
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}*/	
		
	}
	
	
	/*
	public String ReadBigStringIn(String filePath)
	{	
		FileReader fileIn = null;
		
		try 
		{
			fileIn = new FileReader(filePath);
			
			BufferedReader buffIn = new BufferedReader(fileIn);
			
			StringBuilder fileData = new StringBuilder();
		    String line;
		    
		    while( (line = buffIn.readLine()) != null ) 
		    {
		    	fileData.append(line);
		    }
		    
		    return fileData.toString();
			
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("File Does Not Exists : "+filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Cannot Read File Anymore : " + filePath);
		}  
	}
	
	*/
	
	
	public void mergeIndexFiles()
	{
		
		while (true)
		{
			
		// 1. Read all Files in Folder
		ArrayList<FileNameAndSize> fileNameAndSize = getAllFilesFromFolder(outputFolderPath);
		
		
		// 2. Sort the Files in Increasing Order of File-Size
		Collections.sort(fileNameAndSize, comparator); 
		
		
		// 3. Read top 1000 records from file1 and File2 and Store till anyone is not empty and store it on output File
		if (fileNameAndSize.size()>=2)
		{
			//System.out.println(((FileNameAndSize)(fileNameAndSize.toArray()[0])).fileName);
			//System.out.println(((FileNameAndSize)(fileNameAndSize.toArray()[1])).fileName);
			
			outputFileCounter++;
			
			FileReader fileIn1 = null;
			FileReader fileIn2 = null;
			FileWriter fileOut = null;
			
			BufferedReader buff1=null;
			BufferedReader buff2=null;
			
			StringBuilder sbOutputFileData = new StringBuilder();
			
    		int indexFile1 = 0;
    		int indexFile2 = 0;
			
			try 
			{
				String file1Path = outputFolderPath+"//"+((FileNameAndSize)(fileNameAndSize.toArray()[0])).fileName;
				String file2Path = outputFolderPath+"//"+((FileNameAndSize)(fileNameAndSize.toArray()[1])).fileName;
				
				fileIn1 = new FileReader(file1Path);
				fileIn2 = new FileReader(file2Path);	
				
				buff1 = new BufferedReader(fileIn1);
				buff2 = new BufferedReader(fileIn2);
				
				
			    String line1;
			    String line2;
			    
			    
			    line1 = buff1.readLine();
			    line2 = buff2.readLine();
			    
			    
			    System.out.println(line1 + "\n" + line2);

			    
			    // Read Lines from both the files and merge them
			    
			    // Reduce Writes toFile 
			    // Incremented only when sbOutputFileData is inserted by one row
			    long lineCount = 0;
			    fileOut = new FileWriter(outputFolderPath+"//"+outputFileName+outputFileCounter+outputFileExtension);
			    
			    
			    while( line1 != null && line2 != null) 
			    {
			    	// As due to Optimization of Split() 
			    	// It will neglect all the end-entries , if they all are null/empty
			    	// so keep a space or gabage char at the last index to keep all the entries even-if they are empty.
			    	line1 = line1 + " ";
			    	line2 = line2 + " ";
			    	
			    	String data1[] = line1.split(",");
			    	String data2[] = line2.split(",");
			    
			    	
				    //System.out.println("\nLine1 : " + lineCount + " ; Length : " + data1.length + "\nText : " + line1);
				    //System.out.println("Line2 : " + lineCount + " ; Length : "  + data2.length + "\nText : " + line2);

			    	
		    		//System.out.println("Total1 :- " + data1.length);
		    		//System.out.println("Total2 :- " + data2.length);
		    		
			    	String key1 = data1[0];
			    	String key2 = data2[0];
			    	
			    	boolean continueStatus = false;
			    	
			    	for (char c = 'a' ; c <= 'z' ; c++)
			    	{
			    		String tempStr = ""+c+c+c;
			    		//System.out.println(tempStr);
			    		if (key1.contains(tempStr))
			    		{
			    			line1 = buff1.readLine();
			    			continueStatus = true;
			    			break;
			    		}
			    		else if (key2.contains(tempStr))
			    		{
			    			line2 = buff2.readLine();
			    			continueStatus = true;
			    			break;
			    		} 
			    	}
			    	
			    	if (continueStatus)
			    	{
			    		continue;
			    	}
			    	
			    	int status = key1.compareToIgnoreCase(key2);
			    	
			    	if ( status < 0 )
			    	{
			    		//key1,key2
			    		sbOutputFileData.append(line1.trim());
			    		line1 = buff1.readLine();
			    	}
			    	else if ( status > 0 )
			    	{
			    		//Key2 , Key1
			    		sbOutputFileData.append(line2.trim());
			    		line2 = buff2.readLine();
			    	}
			    	else if ( status == 0 )
			    	{
			    		indexFile1=0;
			    		indexFile2=0;
			    		
			    		// Key1==Key2
		    			sbOutputFileData.append(data1[indexFile1].trim());
		    			
		    			indexFile1++;
		    			indexFile2++;
			    		
			    		//System.out.println("Total1 :- " + data1.length);
			    		//System.out.println("Total2 :- " + data2.length);

			    		
			    		for ( ; indexFile1<data1.length && indexFile2<data2.length ; )
			    		{			    			
			    			//System.out.println(indexFile1);
			    			
			    			int pageId1 = Integer.parseInt(data1[indexFile1]);
			    			int pageId2 = Integer.parseInt(data2[indexFile2]);
			    			
			    			if(pageId1 < pageId2)
			    			{
			    				StringBuilder sbTemp = new StringBuilder(); 
			    				int indexFileCounter1 = indexFile1;
			    				
			    				try
			    				{
				    				for(int k=0;k<FieldValuesForPageId.noOfElements+1;k++)
				    				{
				    					sbTemp.append(","+data1[indexFileCounter1].trim());
				    					indexFileCounter1++;
				    				}
				    				
				    				sbOutputFileData.append(sbTemp);
				    				sbTemp.setLength(0);
				    				indexFile1 = indexFileCounter1;
			    				}
			    				catch(Exception e)
			    				{
			    					// As Exception Occurred  , So the line was not fully configured
			    					// Reasons can be "Index File was not written in format due to some exception occurred while writing Index"
			    					// So leave last unformatted output to be written and break
			    					indexFile1 = data1.length;
			    					break;
			    				}
			    			}
			    			else if(pageId1 > pageId2)
			    			{
			    				StringBuilder sbTemp = new StringBuilder(); 
			    				int indexFileCounter2 = indexFile2;
			    				
			    				try
			    				{
				    				for(int k=0;k<FieldValuesForPageId.noOfElements+1;k++)
				    				{
				    					sbTemp.append(","+data2[indexFileCounter2].trim());
				    					indexFileCounter2++;
				    				}
				    				
				    				sbOutputFileData.append(sbTemp);
				    				sbTemp.setLength(0);
				    				indexFile2 = indexFileCounter2;
			    				}
			    				catch(Exception e)
			    				{
			    					// As Exception Occurred  , So the line was not fully configured
			    					// Reasons can be "Index File was not written in format due to some exception occurred while writing Index"
			    					// So leave last unformatted output to be written and break
			    					indexFile2 = data2.length;
			    					break;
			    				}
			    			}
			    			else if(pageId1 == pageId2)
			    			{
			    				sbOutputFileData.append(","+data1[indexFile1].trim());
			    				
			    				// To point to first item after pageId
			    				indexFile1++;
			    				indexFile2++;
			    				
			    				int val1 = 0;
			    				int val2 = 0;
			    						
			    				/*
			    				for(int k=0;k<FieldValuesForPageId.noOfElements;k++)
			    				{
			    					val1 = data1[indexFile1].trim().equalsIgnoreCase("") ? 0 : Integer.parseInt(data1[indexFile1]);
			    					val2 = data2[indexFile2].trim().equalsIgnoreCase("") ? 0 : Integer.parseInt(data2[indexFile2]);
			    					
			    					if (val1+val2 == 0)
			    					{
			    						sbOutputFileData.append( "," );
			    					}
			    					else
			    					{
			    						sbOutputFileData.append( "," + (val1+val2) );
			    					}
			    					
			    					indexFile1++;
			    					indexFile2++;
			    				}*/
			    				
			    				
			    				StringBuilder sbTemp = new StringBuilder(); 
			    				int indexFileCounter1 = indexFile1;
			    				int indexFileCounter2 = indexFile2;
			    				
			    				try
			    				{
				    				for(int k=0;k<FieldValuesForPageId.noOfElements;k++)
				    				{
				    					val1 = data1[indexFileCounter1].trim().equalsIgnoreCase("") ? 0 : Integer.parseInt(data1[indexFileCounter1]);
				    					val2 = data2[indexFileCounter2].trim().equalsIgnoreCase("") ? 0 : Integer.parseInt(data2[indexFileCounter2]);
				    					
				    					if (val1+val2 == 0)
				    					{
				    						sbTemp.append( "," );
				    					}
				    					else
				    					{
				    						sbTemp.append( "," + (val1+val2) );
				    					}
				    					
				    					indexFileCounter1++;
				    					indexFileCounter2++;
				    					
				    					// To be used later to check which "val" caused exception
				    					val1=-1;
				    					val2=-1;
				    				}
				    				
				    				sbOutputFileData.append(sbTemp);
				    				sbTemp.setLength(0);
				    				indexFile1 = indexFileCounter1;
				    				indexFile2 = indexFileCounter2;
			    				}
			    				catch(Exception e)
			    				{
			    					// As Exception Occurred  , So the line was not fully configured
			    					// Reasons can be "Index File was not written in format due to some exception occurred while writing Index"
			    					// So leave last unformatted output to be written and break
			    					if (val1==-1 && val2==-1)
			    					{
			    						// Exception occurred due to val1
			    						indexFile1 = data1.length;
			    					}
			    					else if (val1!=-1 && val2==-1)
			    					{
			    						// Exception occurred due to val2
			    						indexFile2 = data2.length;
			    					}
			    					
			    					break;
			    				}
			    				
			    				
			    				
			    			}
			    			//System.out.println("Index1 :- " + indexFile1);
				    		//System.out.println("Index2 :- " + indexFile2);			    			
			    		}
			    		
			    		// After AnyOne of the file gets finished
			    		// Check and continue to write the other file
			    		
			    		// If File2 got finished , so we will continue to write File1
			    		while(indexFile1<data1.length)
			    		{
			    			sbOutputFileData.append(","+data1[indexFile1]);
			    			indexFile1++;
			    		}
			    		
			    		while(indexFile2<data2.length)
			    		{
			    			sbOutputFileData.append(","+data2[indexFile2]);
			    			indexFile2++;
			    		}		
			    		
			    		line1 = buff1.readLine();
			    		line2 = buff2.readLine();
			    	}
			    	
			    	sbOutputFileData.append(endOfLine);
			    	
			    	lineCount++;
	    			
	    			if (lineCount%1000==0)
	    			{
	    				fileOut.write(sbOutputFileData.toString());
	    				sbOutputFileData.setLength(0);
	    			}
			    	
			    }
			    
			    // Read the left-over file and write it outputFile
			    while( line1 != null) 
			    {
			    	
			    	String data1[] = line1.split(",");
		    		
			    	String key1 = data1[0];
			    	
			    	boolean continueStatus = false;
			    	
			    	for (char c = 'a' ; c <= 'z' ; c++)
			    	{
			    		String tempStr = ""+c+c+c;
			    		//System.out.println(tempStr);
			    		if (key1.contains(tempStr))
			    		{
			    			line1 = buff1.readLine();
			    			continueStatus = true;
			    			break;
			    		}
			    	}
			    	
			    	if (continueStatus)
			    	{
			    		continue;
			    	}
			    	
			    	sbOutputFileData.append(line1);
			    	sbOutputFileData.append(endOfLine);
			    	
			    	lineCount++;
	    			
	    			if (lineCount%1000==0)
	    			{
	    				fileOut.write(sbOutputFileData.toString());
	    				sbOutputFileData.setLength(0);
	    			}
			    	
			    	line1 = buff1.readLine();
			    }
			    
			    // Read the left-over file and write it outputFile
			    while( line2 != null) 
			    {
			    	String data2[] = line2.split(",");
		    		
			    	String key2 = data2[0];
			    	
			    	boolean continueStatus = false;
			    	
			    	for (char c = 'a' ; c <= 'z' ; c++)
			    	{
			    		String tempStr = ""+c+c+c;
			    		//System.out.println(tempStr);
			    		if (key2.contains(tempStr))
			    		{
			    			line2 = buff2.readLine();
			    			continueStatus = true;
			    			break;
			    		}
			    	}
			    	
			    	if (continueStatus)
			    	{
			    		continue;
			    	}
			    	
			    	
			    	sbOutputFileData.append(line2);
			    	sbOutputFileData.append(endOfLine);
			    	
			    	lineCount++;
	    			
	    			if (lineCount%1000==0)
	    			{
	    				fileOut.write(sbOutputFileData.toString());
	    				sbOutputFileData.setLength(0);
	    			}
	    			
			    	line2 = buff2.readLine();
			    }
				
			    
			    
				if ( fileNameAndSize.size()>2 )
				{
					File delFile1 = new File(file1Path);
					File delFile2 = new File(file2Path);
				
					delFile1.delete();
					delFile2.delete();
				}
				else
				{
					System.out.println("Breaking");
					break;
				}
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("File Does Not Exists.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Cannot Read File Anymore.");
			}
			finally
			{
				try {
				    
					fileOut.close();
					fileIn1.close();
					fileIn2.close();
					buff1.close();
					buff2.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Cannot Close either Buff1 or Buff2 or FileWriter");
				}
			}
			
			
		}

		}
		
		System.out.println("Merging is Complete !!!");
		
	}

	
	public void compare(String s1 , String s2)
	{
		int i = s1.compareToIgnoreCase(s2);
		
		if (i<0)
		{
			System.out.println(s1+" , "+s2);
		}
		else if (i>0)
		{
			System.out.println(s2+" , "+s1);
		}
		else if (i==0)
		{
			System.out.println("Both are same : " + s1 + " , " + s2);
		}
		
		/*
		String str = "ab,bc,cd,de,ef,fg,gh,hi";
		
		String strTemp[] = str.split(",");
		
		System.out.println(strTemp. toString());
		*/
		
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
