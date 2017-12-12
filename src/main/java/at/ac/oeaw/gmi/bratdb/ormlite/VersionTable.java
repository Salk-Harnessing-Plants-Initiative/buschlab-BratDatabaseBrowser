package at.ac.oeaw.gmi.bratdb.ormlite;

import javax.persistence.Column;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Version_variablesNames")
public class VersionTable {

	public static final String DESIGN_NAME = "designName";
	public static final String V0_NAME = "v0Name";

	public VersionTable() {}
	
	public VersionTable(String cName, String v0Name) { 
		this.currentName = cName;
		this.v0Name = v0Name;
	}
	
	@DatabaseField(generatedId = true) 
	private Integer id;
	
	@Column(name = DESIGN_NAME)
	public String currentName;
	
	@Column(name = V0_NAME)
	public String v0Name;	

}
