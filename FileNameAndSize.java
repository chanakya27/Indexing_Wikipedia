
public class FileNameAndSize {
	
	public String fileName;
	public Long fileSize;
	

	public FileNameAndSize()
	{
		;
	}
	
	public FileNameAndSize(String name , Long size)
	{
		this.fileName = name;
		this.fileSize = size;
	}
	
	@Override
	public String toString() {
        return String.format(fileName + " , " + fileSize + "\n");
    }
}
