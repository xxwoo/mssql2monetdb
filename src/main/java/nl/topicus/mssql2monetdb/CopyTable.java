package nl.topicus.mssql2monetdb;

import java.util.ArrayList;
import java.util.List;

import nl.topicus.mssql2monetdb.util.MonetDBUtil;

import org.apache.commons.lang.StringUtils;

/**
 * The global CopyTables object which contains the configuration for a table that needs to
 * be copied like which copy method should be used, but also contains {@link MonetDBTable}
 * s which is usually one, but can also contain a temporary table definition in case of
 * replaceTempTable.
 * 
 * @author bloemendal
 */
public class CopyTable
{
	public static final int COPY_METHOD_NOTSET = Integer.MIN_VALUE;
	
	public static final int COPY_METHOD_INSERT = 0;

	public static final int COPY_METHOD_COPYINTO = 1;

	// contains the actual result table and possible a temp table
	private List<MonetDBTable> monetDBTables = new ArrayList<MonetDBTable>();

	private boolean truncate = false;

	private boolean create = true;

	private boolean drop = true;
	
	private String source;

	// secret view name
	private String toName;

	private String fromName;
	
	private String fromColumns;
	
	private String fromQuery;
	
	private String fromCountQuery;

	private String schema = "sys";

	private int copyMethod = COPY_METHOD_NOTSET;

	// copies the table to a temp table and then replaces the 'to' table with the temp
	// table to reduce down-time
	private boolean copyViaTempTable = false;

	// prefix of the temp table that is created
	private String tempTablePrefix = "tmp_";

	// this will create views with the table.example.to name and will backup your table to
	// a backup table this will make it possible to switch the underlying table of the
	// view when data copying is complete resulting in almost no down-time of your
	// database table
	private boolean useFastViewSwitching = true;
	
	// this will do COPY INTO with LOCKED MODE
	private boolean useLockedMode = true;
	
	// determines when or not a copy job should allow this table to be empty or not
	private boolean allowEmpty = false;
	
	// determine whether or not to auto-trim all values
	private boolean autoTrim = true;
	
	private String loadDate;
	
	public void setLoadDate(String loadDateStr)
	{
		this.loadDate = loadDateStr;
	}
	
	public String getLoadDate()
	{
		return this.loadDate;
	}

	public void setCopyMethod(int copyMethod)
	{
		this.copyMethod = copyMethod;
	}

	public int getCopyMethod()
	{
		return this.copyMethod;
	}

	public void setDrop(boolean drop)
	{
		this.drop = drop;
	}

	public boolean drop()
	{
		return this.drop;
	}

	public void setTruncate(boolean truncate)
	{
		this.truncate = truncate;
	}

	public boolean truncate()
	{
		return this.truncate;
	}

	public void setCreate(boolean create)
	{
		this.create = create;
	}

	public boolean create()
	{
		return this.create;
	}
	
	public String getSource ()
	{
		return source;
	}
	
	public void setSource(String source)
	{
		this.source = source;
	}

	public String getFromName()
	{
		return fromName;
	}

	public void setFromName(String fromName)
	{
		this.fromName = fromName;
	}

	public String getToName()
	{
		return toName;
	}

	public void setToName(String toName)
	{
		this.toName = toName;
	}

	public String getSchema()
	{
		return schema;
	}

	public void setSchema(String schema)
	{
		if (schema != null)
			schema = schema.toLowerCase();
		
		this.schema = schema;
	}
	
	public String getTempFilePrefix ()
	{
		return "table_" + this.source + "_" + this.getToName();
	}

	public List<MonetDBTable> getMonetDBTables()
	{
		return monetDBTables;
	}

	public void setMonetDBTables(List<MonetDBTable> monetDBTables)
	{
		this.monetDBTables = monetDBTables;
	}

	public boolean isCopyViaTempTable()
	{
		return copyViaTempTable;
	}

	public void setCopyViaTempTable(boolean copyViaTempTable)
	{
		this.copyViaTempTable = copyViaTempTable;
	}

	public String getTempTablePrefix()
	{
		return tempTablePrefix;
	}

	public void setTempTablePrefix(String tempTablePrefix)
	{
		this.tempTablePrefix = tempTablePrefix;
	}



	public boolean isUseFastViewSwitching()
	{
		return useFastViewSwitching;
	}

	public void setUseFastViewSwitching(boolean useViews)
	{
		this.useFastViewSwitching = useViews;
	}
	
	public void setUseLockedMode(boolean useLockedMode)
	{
		this.useLockedMode = useLockedMode;
	}
	
	public boolean isUseLockedMode ()
	{
		return this.useLockedMode;
	}

	public MonetDBTable getCurrentTable()
	{
		for (MonetDBTable table : monetDBTables)
			if (!table.isTempTable())
				return table;

		return null;
	}

	public MonetDBTable getTempTable()
	{
		for (MonetDBTable table : monetDBTables)
		{
			if (table.isTempTable())
				return table;
		}

		return null;
	}

	public String getToViewSql()
	{
		String sql = "";

		if (StringUtils.isNotEmpty(schema))
		{
			sql = MonetDBUtil.quoteMonetDbIdentifier(schema);
			sql = sql + ".";
		}

		sql = sql + MonetDBUtil.quoteMonetDbIdentifier(toName);

		return sql;
	}

	public String getFromColumns() {
		return fromColumns;
	}

	public void setFromColumns(String fromColumns) {
		this.fromColumns = fromColumns;
	}

	public String getFromQuery() {
		return fromQuery;
	}

	public void setFromQuery(String fromQuery) {
		this.fromQuery = fromQuery;
	}

	public String getFromCountQuery() {
		return fromCountQuery;
	}

	public void setFromCountQuery(String fromCountQuery) {
		this.fromCountQuery = fromCountQuery;
	}
	
	public String getDescription ()
	{
		if (StringUtils.isNotEmpty(getFromName()))
		{
			return "(table) " + getFromName();
		}
		else
		{
			String desc = "(query) " + StringUtils.abbreviate(getFromQuery(), 100);
			desc = desc.replaceAll("\\s+"," "); 
			desc = desc.replaceAll(" +", " ");
			return desc;
		}
	}
	
	public String generateCountQuery ()
	{
		if (StringUtils.isNotEmpty(getFromName()))
		{
			return "SELECT COUNT(*) FROM " + getFromName();
		} else {
			return getFromCountQuery();
		}
	}
	
	public String generateSelectQuery ()
	{
		if (StringUtils.isNotEmpty(getFromName()))
		{
			final String columns;
			if (StringUtils.isNotEmpty(getFromColumns()))
			{
				columns = getFromColumns();
			}
			else
			{
				columns = "*";
			}
			
			return "SELECT " + columns + " FROM " + getFromName() + "";
		} else {
			return getFromQuery();
		}
	}

	public boolean isAllowEmpty() {
		return allowEmpty;
	}

	public void setAllowEmpty(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	public boolean isAutoTrim() {
		return autoTrim;
	}

	public void setAutoTrim(boolean autoTrim) {
		this.autoTrim = autoTrim;
	}
}
