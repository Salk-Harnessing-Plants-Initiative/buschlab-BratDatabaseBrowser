package at.ac.oeaw.gmi.bratdb.ormlite;
import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.Column;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "single_phenotypes")
public class SinglePhenTable {

	public static final String SOURCE_EXP_NAME = "Source_Experiment";
	public static final String SOURCE_ACC_NAME = "Source_Accession";
	
	SinglePhenTable() {
	}
	// Id is has to be unique!!!
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private int id;

	@DatabaseField(columnName = SOURCE_EXP_NAME,foreign = true)
	private ExpTable sourceExpId;
	
	@DatabaseField(columnName = SOURCE_ACC_NAME,foreign = true)
	private AccPhenTable sourceAccId;
	
	@Column(name = "timestep")
	public String timestep;
	
	@Column(name = "set")
	public Double set = Double.NaN;
	
	@Column(name = "plate")
	public Double plate = Double.NaN;
	
	@Column(name = "abs_pos")
	public Double abs_pos = Double.NaN;
	
	@Column(name = "Total_length") //
	public Double total_length = Double.NaN;
	
	@Column(name = "Euclidian_length") //
	public Double euclidian_length = Double.NaN;
	
	@Column(name = "Root_tortuosity") //
	public Double root_tortuosity = Double.NaN;
	
	@Column(name = "Root_angle") // Angle2Grav
	public Double root_angle = Double.NaN;
	
	@Column(name = "Root_directional_equivalent") // DirIdx
	public Double root_directional_equivalent = Double.NaN;
	
	@Column(name = "Root_horizontal_index") 
	public Double root_horizontal_index = Double.NaN;
	
	@Column(name = "Root_vertical_index") 
	public Double root_vertical_index = Double.NaN;
	
	@Column(name = "Root_linearity")  // CorrXY
	public Double root_linearity = Double.NaN;
		
	@Column(name = "Average_root_width")
	public Double average_root_width = Double.NaN;
	
	@Column(name = "Root_witdh_20")
	public Double root_width_20 = Double.NaN;
	
	@Column(name = "Root_width_40")
	public Double root_width_40 = Double.NaN;
	
	@Column(name = "Root_width_60")
	public Double root_width_60 = Double.NaN;
	
	@Column(name = "Root_width_80")
	public Double root_width_80 = Double.NaN;
	
	@Column(name = "Root_width_100")
	public Double root_width_100 = Double.NaN;
		
//	@Column(name = "GravScore")
//	public Double gravscore;
	
//	@Column(name = "sdX_sdY_per_Pix")
//	public Double sdx_sdy_per_pix;
		
	@Column(name = "Core_filename")
	public String core_filename;
	
	@Column(name = "ROI")
	//public String roi;
	public Integer roi = 0;
	
	@Column(name = "x1")
	public Integer x1 = 0;
	
	@Column(name = "y1")
	public Integer y1 = 0;
	
	@Column(name = "x2")
	public Integer x2 = 0;
	
	@Column(name = "y2")
	public Integer y2 = 0;
	
	public void setx1(Integer val){
		this.x1 = val;
	}
	public void sety1(Integer val){
		this.y1 = val;
	}
	public void setx2(Integer val){
		this.x2 = val;
	}
	public void sety2(Integer val){
		this.y2 = val;
	}

	public Integer getx1() {
		return x1;
	}
	public Integer gety1() {
		return y1;
	}
	public Integer getx2() {
		return x2;
	}
	public Integer gety2() {
		return y2;
	}

	public ArrayList<Integer> getCoordinates() {
		return new ArrayList<>(Arrays.asList(this.x1, this.x2, this.y1, this.y2));
	}
	void setExpTable(ExpTable exp) {
		this.sourceExpId = exp;
	}
	public ExpTable getExpTable() {
		return sourceExpId;
	}

	void setAccTable(AccPhenTable acc) {
		this.sourceAccId = acc;
	}
	public AccPhenTable getAccPhenTable() {
		return sourceAccId;
	}
}