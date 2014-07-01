import java.io.Serializable;


public class FieldValuesForPageId implements Serializable{

	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	
	public Integer totalCount;
	public Integer titleCount;
	public Integer textCount;
	public Integer infoBoxCount;
	public Integer linksCount;
	public Integer categoryCount;
	
		
	public FieldValuesForPageId()
	{
		//Let them be Null at first (unless they are initialised)
		/*
		this.totalCount = 0;
		this.titleCount = 0;
		this.textCount = 0;
		this.infoBoxCount = 0;
		this.linksCount = 0;
		this.categoryCount = 0;
		*/
	}
	
	
	public FieldValuesForPageId(Integer totalCount, Integer titleCount, Integer textCount, Integer infoBoxCount, Integer linksCount, Integer categoryCount)
	{
		this.totalCount = totalCount;
		this.titleCount = titleCount;
		this.textCount = textCount;
		this.infoBoxCount = infoBoxCount;
		this.linksCount = linksCount;
		this.categoryCount = categoryCount;
	}
	
	@Override
    public String toString() {
		String total,title,text,infoBox,links,category;
		
		if (totalCount==null)
		{
			total="";
		}
		else
		{
			total=totalCount.toString();
		}
		
		if (titleCount==null)
		{
			title="";
		}
		else
		{
			title=titleCount.toString();
		}
		
		if (textCount==null)
		{
			text="";
		}
		else
		{
			text=textCount.toString();
		}
		
		if (infoBoxCount==null)
		{
			infoBox="";
		}
		else
		{
			infoBox=infoBoxCount.toString();
		}
		
		if (linksCount==null)
		{
			links="";
		}
		else
		{
			links=linksCount.toString();
		}
		
		if (categoryCount==null)
		{
			category="";
		}
		else
		{
			category=categoryCount.toString();
		}
		
        return String.format(total + "," + title + "," + text + "," + infoBox + "," + links + "," + category );
    }
	
}
