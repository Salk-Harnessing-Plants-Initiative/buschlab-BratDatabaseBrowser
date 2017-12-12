package at.ac.oeaw.gmi.bratdb.ormlite;
import javax.persistence.Column;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "acc_rates")
public class AccRatesTable {
		
	public static final String SOURCE_EXP_NAME = "Source_Experiment";
	public static final String SOURCE_ACC_NAME = "Source_Accession";
	public static final String TIMESTEP = "Timestep";
	
	AccRatesTable () {
	}
	
	// Id is has to be unique!!!
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private Integer id;
	
	@DatabaseField(columnName = SOURCE_EXP_NAME,foreign = true) //foreignAutoCreate ?!
	private ExpTable sourceExpId;
	
	@ForeignCollectionField
	private ForeignCollection<SingleRatesTable> singleRatesValues;
	
	@Column(name = SOURCE_ACC_NAME)
	public String ACC_ID;
	
	@Column(name = TIMESTEP)
	public String timestep;
	
	@Column(name = "mean_Root_growth_rate") //mean_GR_rootLength
	public Double mean_root_growth_rate  = Double.NaN;
	
	@Column(name = "mean_Relative_root_growth_rate") // != mean_GR_shootArea
	public Double mean_relative_root_growth_rate = Double.NaN;
	
	@Column(name = "median_Root_groth_rate")
	public Double median_root_growth_rate = Double.NaN;
	
	@Column(name = "median_Relative_root_growth_rate")
	public Double median_relative_root_growth_rate = Double.NaN;
	
	@Column(name = "nIndiv_Root_groth_rate")
	public Double nindiv_root_growth_rate = Double.NaN;
	
	@Column(name = "nIndiv_Relative_root_growth_rate")
	public Double nindiv_relative_root_growth_rate = Double.NaN;
	
	@Column(name = "stdDev_Root_groth_rate")
	public Double stddev_root_growth_rate = Double.NaN;
	
	@Column(name = "stdDev_Relative_root_growth_rate")
	public Double stddev_relative_root_growth_rate = Double.NaN;	
	
	public Integer getId() {
		return this.id;
	}

	void setExpTable(ExpTable exp) {
		this.sourceExpId = exp;
	}
	
	public ForeignCollection<SingleRatesTable> getSingleValues() {
		return singleRatesValues;
	}
}
