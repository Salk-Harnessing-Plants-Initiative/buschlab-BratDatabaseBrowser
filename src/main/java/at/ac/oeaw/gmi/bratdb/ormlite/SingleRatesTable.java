package at.ac.oeaw.gmi.bratdb.ormlite;
import javax.persistence.Column;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "single_ratesphenotypes")
public class SingleRatesTable {

	public static final String SOURCE_EXP_NAME = "Source_Experiment";
	public static final String SOURCE_ACC_NAME = "Source_Accession";
	
	public SingleRatesTable(){
	}
	
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private Integer id;
		
	@DatabaseField(columnName = SOURCE_EXP_NAME,foreign = true)
	private ExpTable sourceExpId;
	
	@DatabaseField(columnName = "SOURCE_ACC_RATES",foreign = true)
	private AccRatesTable sourceRateId;
	
	@Column(name = "Time_Step")
	public String timestep;
	
	@Column(name = "Root_growth_rate") //GR_Root
	public Double root_growth_rate = Double.NaN;
	
//	@Column(name = "GR/TL")
//	public double gr_tl;
		
	@Column(name = "Relative_root_growth_rate") //GR_ShootArea
	public Double relative_root_growth_rate = Double.NaN;
				
	void setExpTable(ExpTable exp) {
		this.sourceExpId = exp;
	}
	public ExpTable getExpTable() {
		return sourceExpId;
	}

	public AccRatesTable getAccRatesTable() {
		return sourceRateId;
	}

	void setRateTable(AccRatesTable rate) {
		this.sourceRateId = rate;
	}
}
