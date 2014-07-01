
public class PreprocessingTextForIndex {

	/*
	 * Helpful for WikiText to remove Infobox
	 */	
	public static String removeInfobox(String text)
	{
		// Assumption : As text is already in lowercase and we know that "{{infobox ...}}" text is present
		return text.replaceAll("\\{\\{[Ii]nfobox([^\\}]*\\n+)*|[^\\}]*\\}\\}", " ");
	}
	
}
