package at.ac.oeaw.gmi.bratdb.ormlite;

import javax.persistence.Column;

import com.j256.ormlite.field.DatabaseField;

public class SetTable {
	
	public static final String SOURCE_EXP_NAME = "sourceExp";
		
	public SetTable() {
	}
	
	// Id is has to be unique!!!
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	private Integer id;
	
	@DatabaseField(columnName = SOURCE_EXP_NAME,foreign = true)
	private ExpTable source_expID;

	/// added meta data moved here from ExpTable class04.06.2016
	@Column(name = "batch") 
	public String expBatch;
	@Column(name = "date_plates_made") 
	public String expDatePlatesMade;
	@Column(name = "who_made_plates") 
	public String expWhoMadePlates;
	@Column(name = "type_of_media")
	public String expMedia;
	@Column(name = "parallel_control_plates")
	public String expControlPlates;
	@Column(name = "pH")
	public String expPh;
	@Column(name = "agar_lot")
	public String expAgarLot;
	@Column(name = "volume__plate")
	public String expVolPerPlate;
	@Column(name = "plate_cooling_without_lid")
	public String expPlateCoolingWLid;
	@Column(name = "plate_cooling_on_with_lid")
	public String expPlateCoolingOnWLid;
	@Column(name = "plate_storage")
	public String expPlateStorage;
	@Column(name = "seed_size")
	public String expSeedSize;
	@Column(name = "seed_storage")
	public String expSeedStorage;
	@Column(name = "gas_sterilization")
	public String expGasSterilization;
	@Column(name = "stratification_on_plate")
	public String expStratOnPlate;
	@Column(name = "stratification_in_tube")
	public String expStratInTube;
	@Column(name = "number_of_seeds_accession")
	public String expNumberOfSeedsPerAcc;
	@Column(name = "position_of_seeds_on_plate")
	public String expPosOfSeeds;
	@Column(name = "time_of_day_plated")
	public String expTimePlated;
	@Column(name = "date_seeds_plated")
	public String expDatePlated;
	@Column(name = "type_of_lights")
	public String expTypeLights;
	@Column(name = "light_cycle")
	public String expLightCycle;
	@Column(name = "position_of_rack_to_lights")
	public String expPosOfRackToLights;
	@Column(name = "position_of_plates_on_rack")
	public String expPosOfPlatesOnRack;
	@Column(name = "temperature_of_growth_room")
	public String expTempOfGrRoom;
	@Column(name = "level_of_the_rack")
	public String expLevelOfRack;
	@Column(name = "scanning_schedule")
	public String expScanningSchedule;
	@Column(name = "time_of_1st_scan")
	public String expTimeOfFirstScan;
	@Column(name = "date_of_1st_scan")
	public String expDateOfFirstScan;
	@Column(name = "scanning_settings")
	public String expScanningSettings;
	@Column(name = "purpose_of_experiment")
	public String expPurpose;
	
	void setSourceExp(ExpTable exp) {
		this.source_expID = exp;
	}
	
	public ExpTable getSourceExp() {
		return this.source_expID;
	}
}
