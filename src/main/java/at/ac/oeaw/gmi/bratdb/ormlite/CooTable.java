package at.ac.oeaw.gmi.bratdb.ormlite;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

class CooTable {

	// Id is has to be unique!!!
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private Integer id;
	
	@DatabaseField(columnName = "Source_Exp")
	public String sourceExp;
	
	@DatabaseField(columnName = "Source_Acc")
	public String sourceAcc;
	
	@DatabaseField(columnName = "Timestep")
	public String timestep;
	
	@DatabaseField(columnName = "Roi")
	public Integer roi;
	
	@DatabaseField(columnName = "startX")
	public Integer startX;
	
	@DatabaseField(columnName = "startY")
	public Integer startY;
	
	@DatabaseField(columnName = "endX")
	public Integer endX;
	
	@DatabaseField(columnName = "endY")
	public Integer endY;
	
	@DatabaseField(columnName = "shoot_mass_centerX")
	public Integer shootMcX;
	
	@DatabaseField(columnName = "shoot_mass_centerY")
	public Integer shootMcY;
	
	@DatabaseField(columnName = "bit_per_entry")
	public Integer bits;
	
	@DatabaseField(columnName = "number_of_primaryRoot_pixel")
	public Integer nPpixel;
		
	@DatabaseField(columnName = "primaryRootXY", dataType = DataType.BYTE_ARRAY)
	public byte[] pRootCoo;
	
	@DatabaseField(columnName = "number_of_plantOutline_pixel")
	public Integer nOpixel;
		
	@DatabaseField(columnName = "plantOutlineXY", dataType = DataType.BYTE_ARRAY)
	public byte[] pOutlineCoo;
	
	@DatabaseField(columnName = "number_of_shoot_pixel")
	public Integer nSpixel;
		
	@DatabaseField(columnName = "plantShootXY", dataType = DataType.BYTE_ARRAY)
	public byte[] pShootCoo;
	
	public CooTable() {
	}
	
	public CooTable(String sourceExp, String sourceAcc, String sourceTime) {
		this.sourceExp = sourceExp;
		this.sourceAcc = sourceAcc;
		this.timestep = sourceTime;
	}
}
