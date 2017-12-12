package at.ac.oeaw.gmi.bratdb.ormlite;
import javax.persistence.Column;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "experiments")
public class ExpTable {
	
	public static final String NAME_FIELD_NAME = "name";
	public static final String AUTHOR_FIELD_NAME = "author";
	public static final String DESC_FIELD_NAME = "description";
	
	// Id is has to be unique!!!
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private Integer id;
	
	// annotating Exp name table
	@Column(name = NAME_FIELD_NAME)
	public String name;
	
	// annotating Author name table
	@Column(name = AUTHOR_FIELD_NAME)
	public String author;
	
	// annotating Desr table
	@Column(name = DESC_FIELD_NAME)
	public String desc;
	
	// annotating exp def path
	@Column(name = "defPath")
	public String defPath;
	
	@Column(name = "coordinatesPath")
	public String cooPath;
	
	// annotating exp path to pix and coo files
	@Column(name = "picPath")
	public String picPath;
	
	// annotating alter test
//	@Column(name = "altTest8")
//	public Integer altTest8;
		
	@ForeignCollectionField
	private ForeignCollection<AccPhenTable> phenCol;
	
	@ForeignCollectionField
	private ForeignCollection<AccRatesTable> ratesCol;
	
	@ForeignCollectionField
	private ForeignCollection<SetTable> setCol;

	@ForeignCollectionField
	private ForeignCollection<GeneHunterTable> geneHunterCol;

	@Column(name = "BRAT_version")
	public String brat_version;
	
	// def. no-arg constructor
	public ExpTable() {
	}
	
	public ExpTable(String Exp, String Author, String Desc, String defPath, String picPath, String cooPath) {
		this.name = Exp;
		this.author = Author;
		this.desc = Desc;
		this.defPath = defPath; 
		this.picPath = picPath;
		this.cooPath = cooPath;
	}
	
	public ExpTable(String Exp, String Author, String Desc, String Path) {
		this.name = Exp;
		this.author = Author;
		this.desc = Desc;
		this.defPath = Path; 
	}
	
	public ExpTable(String Exp, String Author, String Desc) {
		this.name = Exp;
		this.author = Author;
		this.desc = Desc;
	}
	
	ExpTable(String Exp, String Author) {
		this.name = Exp;
		this.author = Author;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getPath() {
		return defPath;
	}
	
	public void setPath(String path) {
		this.defPath = path;
	}

	public void setPicPath(String inPath) {
		this.picPath = inPath;
	}
	
	public String getPicPath() {
		return this.picPath;
	}
	
	public void setCooPath(String inPath) {
		this.cooPath = inPath;
	}
	
	public String getCooPath() {
		return this.cooPath;
	}
	
	public ForeignCollection<AccPhenTable> getPhenCol() {
		return phenCol;
	}
	
	public ForeignCollection<AccRatesTable> getRatesCol() {
		return ratesCol;
	}
	
	public ForeignCollection<SetTable> getSetCol() {
		return setCol;
	}
}
