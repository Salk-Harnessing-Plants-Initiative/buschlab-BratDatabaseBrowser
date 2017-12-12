package at.ac.oeaw.gmi.bratdb.ormlite;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import javax.persistence.Column;

@DatabaseTable(tableName = "acc_phenotypes_avg")
public class AccPhenTable{

	public static final String SOURCE_EXP_NAME = "Source_Experiment";
	public static final String SOURCE_ACC_NAME = "Source_Accession";
	public static final String SOURCE_TIMESTEP = "Timestep";
	
	public AccPhenTable() {
	}
		
	// Id is has to be unique!!!
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private Integer id;
		
	@DatabaseField(columnName = SOURCE_EXP_NAME,foreign = true) //foreignAutoCreate ?!
	private ExpTable sourceExpId;
	
	@ForeignCollectionField
	private ForeignCollection<SinglePhenTable> singleValues;
	
	@Column(name = SOURCE_ACC_NAME)
	public String accId;
	
	@Column(name = SOURCE_TIMESTEP)
	public String timestep;
	
	@Column(name = "mean_Total_length")
	public Double mean_total_length = Double.NaN;
		
	@Column(name = "mean_Euclidian_lenght")	
	public Double mean_euclidian_length = Double.NaN;
	
	@Column(name = "mean_Root_tortuosity")
	public Double mean_root_tortuosity = Double.NaN;	
	
	@Column(name = "mean_Root_angle")
	public Double mean_root_angle = Double.NaN;
	
	@Column(name = "mean_Root_directional_equivalent")
	public Double mean_root_directional_equivalent = Double.NaN;
	
	@Column(name = "mean_Root_horizontal_index")
	public Double mean_root_horizontal_index = Double.NaN;
	
	@Column(name = "mean_Root_vertical_index")
	public Double mean_root_vertical_index = Double.NaN;
	
	@Column(name = "mean_Root_linearity")
	public Double mean_root_linearity = Double.NaN;
	
	@Column(name = "mean_Average_rootwidth")
	public Double mean_average_root_width = Double.NaN;
	
	@Column(name = "mean_Root_width20")
	public Double mean_root_width_20 = Double.NaN;
	
	@Column(name = "mean_Root_width40")
	public Double mean_root_width_40 = Double.NaN;
	
	@Column(name = "mean_Root_width60")
	public Double mean_root_width_60 = Double.NaN;
	
	@Column(name = "mean_Root_width80")
	public Double mean_root_width_80 = Double.NaN;
	
	@Column(name = "mean_Root_width100")
	public Double mean_root_width_100 = Double.NaN;
			
	// medians: 
	@Column(name = "median_Total_length")
	public Double median_total_length = Double.NaN;
		
	@Column(name = "median_Euclidian_lenght")
	public Double median_euclidian_length = Double.NaN;
	
	@Column(name = "median_Root_tortuosity")
	public Double median_root_tortuosity = Double.NaN;	
	
	@Column(name = "median_Root_angle")
	public Double median_root_angle = Double.NaN;
	
	@Column(name = "median_Root_directional_equivalent")
	public Double median_root_directional_equivalent = Double.NaN;
	
	@Column(name = "median_Root_horizontal_index")
	public Double median_root_horizontal_index = Double.NaN;
	
	@Column(name = "median_Root_vertical_index")
	public Double median_root_vertical_index = Double.NaN;
	
	@Column(name = "median_Root_linearity")
	public Double median_root_linearity = Double.NaN;
	
	@Column(name = "median_Average_rootwidth")
	public Double median_average_root_width = Double.NaN;
	
	@Column(name = "median_Root_width20")
	public Double median_root_width_20 = Double.NaN;
	
	@Column(name = "median_Root_width40")
	public Double median_root_width_40 = Double.NaN;
	
	@Column(name = "median_Root_width60")
	public Double median_root_width_60 = Double.NaN;
	
	@Column(name = "median_Root_width80")
	public Double median_root_width_80 = Double.NaN;
	
	@Column(name = "median_Root_width100")
	public Double median_root_width_100 = Double.NaN;
	
	// nIndiv: 
	@Column(name = "nIndiv_Total_length")
	public Double nindiv_total_length = Double.NaN;
		
	@Column(name = "nIndiv_Euclidian_lenght")
	public Double nindiv_euclidian_length = Double.NaN;
	
	@Column(name = "nIndiv_Root_tortuosity")
	public Double nindiv_root_tortuosity = Double.NaN;	
	
	@Column(name = "nIndiv_Root_angle")
	public Double nindiv_root_angle = Double.NaN;
	
	@Column(name = "nIndiv_Root_directional_equivalent")
	public Double nindiv_root_directional_equivalent = Double.NaN;
	
	@Column(name = "nIndiv_Root_horizontal_index")
	public Double nindiv_root_horizontal_index = Double.NaN;
	
	@Column(name = "nIndiv_Root_vertical_index")
	public Double nindiv_root_vertical_index = Double.NaN;
	
	@Column(name = "nIndiv_Root_linearity")
	public Double nindiv_root_linearity = Double.NaN;
	
	@Column(name = "nIndiv_Average_rootwidth")
	public Double nindiv_average_root_width = Double.NaN;
	
	@Column(name = "nIndiv_Root_width20")
	public Double nindiv_root_width_20 = Double.NaN;
	
	@Column(name = "nIndiv_Root_width40")
	public Double nindiv_root_width_40 = Double.NaN;
	
	@Column(name = "nIndiv_Root_width60")
	public Double nindiv_root_width_60 = Double.NaN;
	
	@Column(name = "nIndiv_Root_width80")
	public Double nindiv_root_width_80 = Double.NaN;
	
	@Column(name = "nIndiv_Root_width100")
	public Double nindiv_root_width_100 = Double.NaN;
	
	// store just one nIndiv - number of individuals 0710 
//	@Column(name = "nIndiv")
//	public Double nindiv = Double.NaN;
	
	
	// stdDevs: 
	@Column(name = "stdDev_Total_length")
	public Double stddev_total_length = Double.NaN;
		
	@Column(name = "stdDev_Euclidian_length")
	public Double stddev_euclidian_length = Double.NaN;
	
	@Column(name = "stdDev_Root_tortuosity")
	public Double stddev_root_tortuosity = Double.NaN;	
	
	@Column(name = "stdDev_Root_angle")
	public Double stddev_root_angle = Double.NaN;
	
	@Column(name = "stdDev_Root_directional_equivalent")
	public Double stddev_root_directional_equivalent = Double.NaN;
	
	@Column(name = "stdDev_Root_horizontal_index")
	public Double stddev_root_horizontal_index = Double.NaN;
	
	@Column(name = "stdDev_Root_vertical_index")
	public Double stddev_root_vertical_index = Double.NaN;
	
	@Column(name = "stdDev_Root_linearity")
	public Double stddev_root_linearity = Double.NaN;
	
	@Column(name = "stdDev_Average_rootwidth")
	public Double stddev_average_root_width = Double.NaN;
	
	@Column(name = "stdDev_Root_width20")
	public Double stddev_root_width_20 = Double.NaN;
	
	@Column(name = "stdDev_Root_width40")
	public Double stddev_root_width_40 = Double.NaN;
	
	@Column(name = "stdDev_Root_width60")
	public Double stddev_root_width_60 = Double.NaN;
	
	@Column(name = "stdDev_Root_width80")
	public Double stddev_root_width_80 = Double.NaN;
	
	@Column(name = "stdDev_Root_width100")
	public Double stddev_root_width_100 = Double.NaN;
		
	public Integer getId() {
		return this.id;
	}
	
	public void setAcc(String acc) {
		this.accId = acc;
	}
	
	public String getAcc() {
		return accId;
	}
	
	public void setExpTable(ExpTable exp) {
		this.sourceExpId = exp;
	}
	
	public ExpTable getExpTable() {
		return sourceExpId;
	}
	
	public String getTimestep() {
		return timestep;
	}
	
	public ForeignCollection<SinglePhenTable> getSingleValues() {
		return singleValues;
	}
}
