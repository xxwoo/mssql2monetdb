package nl.topicus.mssql2monetdb;

public class CopyToolException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public CopyToolException (String msg)
	{
		super(msg);
	}
	
	public CopyToolException (String msg, Exception e)
	{
		super(msg, e);
	}
	
}
