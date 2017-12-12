package at.ac.oeaw.gmi.bratdb.ormlite;

import at.ac.oeaw.gmi.bratdb.app.ClsSetterString;
import at.ac.oeaw.gmi.bratdb.app.ConfigCls;
import at.ac.oeaw.gmi.bratdb.app.Main;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.table.TableUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.Column;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DB {

	private Dao<ExpTable, Integer> expDao;
	private Dao<AccPhenTable, Integer> acc_phenDao;
	private Dao<AccRatesTable, Integer> acc_ratesDao;
	private Dao<SinglePhenTable,Integer> single_phenDao;
	private Dao<SingleRatesTable, Integer> single_ratesDao;	
	private Dao<SetTable,Integer> setDao;
	private Dao<VersionTable,Integer> versionDao;

	private Dao<GeneHunterTable,Integer> hunterDao;
	
	private ConnectionSource connectionSource = null;
	
	private File db;

	public DB(){
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
	}

	public DB(File dbFile) {
		try {
			connectDB(dbFile);
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
	
	public void connectDB(File dbIn) throws SQLException {
		if (dbIn != null)
			db = dbIn;
		else try {
			String DbFile = ConfigCls.getMyProp("Connect_to_DB");

			if (!DbFile.isEmpty()) {
				db = new File(DbFile);
				if (!db.isFile())
					db = new File("DB1.db");
			}
			else
				db = new File("DB1.db");
		} catch (NullPointerException ex) {
			Main.logger.info("error reading config " + ConfigCls.getMyProp("Connect_to_DB"));
			db = new File("DB1.db");
		}

        String DATABASE_URL = "jdbc:sqlite:" + db.getPath();
		//DATABASE_URL = "jdbc:mysql://localhost:3306/BratDB"; // 3306 def port for mysql
		//DATABASE_URL = "jdbc:h2:" + db.getPath();
		
		if (DATABASE_URL.contains("mysql")) try {
			try {
				//Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
				Main.logger.severe(Main.StackTraceToString(ex));
			}
			connectionSource = new JdbcConnectionSource(DATABASE_URL, "root", "mysql_lws24");
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		else
			connectionSource = new JdbcConnectionSource(DATABASE_URL);
				
		expDao = DaoManager.createDao(this.connectionSource, ExpTable.class);
		acc_phenDao = DaoManager.createDao(this.connectionSource, AccPhenTable.class);
		acc_ratesDao = DaoManager.createDao(this.connectionSource, AccRatesTable.class);
		single_phenDao = DaoManager.createDao(this.connectionSource, SinglePhenTable.class);
		single_ratesDao = DaoManager.createDao(this.connectionSource, SingleRatesTable.class);
		setDao = DaoManager.createDao(this.connectionSource, SetTable.class);
		versionDao = DaoManager.createDao(this.connectionSource, VersionTable.class);	

		hunterDao = DaoManager.createDao(this.connectionSource,GeneHunterTable.class);

		if (!db.exists()) {
			Main.logger.info("creating new database " + db.getName() + " in " + db.getAbsolutePath());
			createDB();
			fillVersTable();
		}
	}

	private void createDB () {
		try {
			TableUtils.createTable(connectionSource, ExpTable.class);
			TableUtils.createTable(connectionSource, AccPhenTable.class);
			TableUtils.createTable(connectionSource, AccRatesTable.class);
			TableUtils.createTable(connectionSource, SinglePhenTable.class);
			TableUtils.createTable(connectionSource, SingleRatesTable.class);
			TableUtils.createTable(connectionSource, SetTable.class);
			TableUtils.createTable(connectionSource, VersionTable.class);
			TableUtils.createTable(connectionSource, GeneHunterTable.class);
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}	
		
	public void closeConnection() {
		if (connectionSource != null) try {
			connectionSource.close();
			Main.logger.info("connection to " + db.getName() + " closed");
		} catch (IOException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
	
//	public ExpTable AddData (String Exp, String Author, String Desc, String Path){
//		ExpTable exptable = new ExpTable(Exp, Author, Desc, Path);
//		try {
//			expDao.create(exptable);
//			Main.logger.info("Added experiment " + Exp);
//			//logger.info("Added experiment " + Exp);
//		}
//		catch (Exception ex){
//			Main.logger.info("error adding " + Exp);
//			Main.logger.severe(Main.StackTraceToString(ex));
//			//logger.error("error occured while adding experiment " + Exp + "/n", e);
//		}
//		return exptable;
//	}
	
	private ExpTable AddData (String Exp, String Author, String Desc, String defPath, String picPath, String cooPath){
		ExpTable exptable = new ExpTable(Exp, Author, Desc, defPath, picPath, cooPath);
		try {
			expDao.create(exptable);
			Main.logger.info("Added experiment " + Exp);
		}
		catch (Exception ex){
			Main.logger.severe("error adding " + Exp);
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return exptable;
	}

	public void AddExp(ExpTable exp) {
		try {
			expDao.create(exp);
		}
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

	public void AddPhenTable(ArrayList<AccPhenTable> in) {
		try {
			acc_phenDao.callBatchTasks(() -> {
                acc_phenDao.create(in);
                return null;
            });
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

	public void AddRatesTable(ArrayList<AccRatesTable> in) {
		try {
			acc_ratesDao.callBatchTasks(() -> {
                acc_ratesDao.create(in);
                return null;
            });
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
		
	public ExpTable AddData (String Exp, String Author, String Desc){
		ExpTable exptable = new ExpTable(Exp, Author, Desc);
		try {
			expDao.create(exptable);
			Main.logger.info("Added experiment " + Exp);
		} 
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
			Main.logger.severe("error experiment " + Exp);
		}
		return exptable;
	}

	public void removeExpTableByName(ExpTable toRemove) {

		DeleteBuilder<AccPhenTable,Integer> deleteBuilder = acc_phenDao.deleteBuilder();
		DeleteBuilder<AccRatesTable,Integer> deleteBuilder1 = acc_ratesDao.deleteBuilder();
		DeleteBuilder<SinglePhenTable,Integer> deleteBuilder2 = single_phenDao.deleteBuilder();
		DeleteBuilder<SingleRatesTable,Integer> deleteBuilder3 = single_ratesDao.deleteBuilder();

		try {
			deleteBuilder.where().eq(AccPhenTable.SOURCE_EXP_NAME,toRemove.getId());
			deleteBuilder.delete();

			deleteBuilder1.where().eq(AccRatesTable.SOURCE_EXP_NAME,toRemove.getId());
			deleteBuilder1.delete();

			deleteBuilder2.where().eq(SinglePhenTable.SOURCE_EXP_NAME,toRemove.getId());
			deleteBuilder2.delete();

			deleteBuilder3.where().eq(SingleRatesTable.SOURCE_EXP_NAME,toRemove.getId());
			deleteBuilder3.delete();

			expDao.delete(toRemove);

		} catch (SQLException ex) {
			Main.logger.severe(ex.getMessage());
		}
	}

	public void removeHunterFileByName(String toRemove) {

		DeleteBuilder<GeneHunterTable,Integer> deleteBuilder = hunterDao.deleteBuilder();
		try {
			deleteBuilder.where().eq("Hunter_file", toRemove);
			deleteBuilder.delete();
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

	void addPhenTable(ArrayList<AccPhenTable> in){
		try {
			acc_phenDao.callBatchTasks(() -> {
				acc_phenDao.create(in);
                return null;
			});
		} 
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
	
	void addSingleTable(ArrayList<SinglePhenTable> input, ArrayList<String> accIDs, ExpTable experiment){
		long startTime = System.currentTimeMillis();
		
		HashSet<String> accSet = new HashSet<>(accIDs);
		HashSet<String> timeSet = new HashSet<>();
		input.forEach((item) -> timeSet.add(item.timestep));
		
		Map<String,AccPhenTable> idMap = new LinkedHashMap<>();
		
		try {
			QueryBuilder<AccPhenTable,Integer> accphen_statementBuilder = acc_phenDao.queryBuilder();
			
			single_phenDao.callBatchTasks(() -> {
                for (String accession : accSet)
                    for (String timestep : timeSet) {

                        accession = accession.replace("^_+", "");

                        accphen_statementBuilder.where().eq(AccPhenTable.SOURCE_EXP_NAME, experiment.getId()).
                                and().eq(AccPhenTable.SOURCE_ACC_NAME, accession).
                                and().eq(AccPhenTable.SOURCE_TIMESTEP, timestep);

                        AccPhenTable phenparent = acc_phenDao.queryForFirst(accphen_statementBuilder.prepare());

                        try {
                            if (phenparent.getId() != null) idMap.put(accession + timestep, phenparent);
                        } catch (Exception ex) {
                            Main.logger.info("acc - ignoring " + accession + " at timestep " + timestep);
                            //Main.logger.severe(Main.StackTraceToString(ex));
                        }
                    }

                for (int i = 0; i< input.size();++i) {
                    String lookup = accIDs.get(i)+input.get(i).timestep;

                    if (idMap.containsKey(lookup)) {
                        input.get(i).setAccTable(idMap.get(lookup));
                        input.get(i).setExpTable(experiment);
                        single_phenDao.create(input.get(i));
                    }
                }
                return null;
			});
		} 
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		Main.logger.fine("took me for accessions: " +elapsedTime);
	}
	
	void addSingleRatesTable(ArrayList<SingleRatesTable> input, ArrayList<String> accIDs, ExpTable experiment){
		long startTime = System.currentTimeMillis();
		
		HashSet<String> accSet = new HashSet<>(accIDs);
		HashSet<String> timeSet = new HashSet<>();

        input.forEach((item) -> timeSet.add(item.timestep));
		Map<String,AccRatesTable> idMap = new LinkedHashMap<>();
		
		try {
			QueryBuilder<AccRatesTable,Integer> accrates_statementBuilder = acc_ratesDao.queryBuilder();
			
			single_phenDao.callBatchTasks(() -> {
                for (String accession : accSet)
                    for (String timestep : timeSet) {

                        accrates_statementBuilder.where().eq(AccRatesTable.SOURCE_EXP_NAME, experiment.getId()).
                                and().eq(AccRatesTable.SOURCE_ACC_NAME, accession).
                                and().eq(AccRatesTable.TIMESTEP, timestep);

                        AccRatesTable ratesparent = acc_ratesDao.queryForFirst(accrates_statementBuilder.prepare());

                        try {
                            if (ratesparent.getId() != null) idMap.put(accession + timestep, ratesparent);
                        } catch (Exception ex) {
                            Main.logger.info("rates - ignoring " + accession + " at timestep " + timestep);
                        }
                    }

                for (int i = 0; i< input.size();++i) {
                    String lookup = accIDs.get(i)+input.get(i).timestep;

                    if (idMap.containsKey(lookup)) {
                        input.get(i).setRateTable(idMap.get(lookup));
                        input.get(i).setExpTable(experiment);
                        single_ratesDao.create(input.get(i));
                    }
                }
                return null;
			});
		} 
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		Main.logger.fine("took me for rates: " +elapsedTime);
	}
	
	void addAccRatesTable (ArrayList<AccRatesTable> input){
		try {
			acc_ratesDao.callBatchTasks(() -> acc_ratesDao.create(input));
		}
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
	
// --Commented out by Inspection START (30.03.17 15:10):
//	public ArrayList<AccPhenTable> getPhenOfAccession(ExpTable exp, String acc) {
//		ArrayList<AccPhenTable> res = new ArrayList<>();
//		ForeignCollection<AccPhenTable> phens = exp.getPhenCol(); //
//
//		for (AccPhenTable found : phens) if (found.accId.equals(acc)) res.add(found);
//		return res;
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

// --Commented out by Inspection START (30.03.17 15:10):
//	public void SearchExp(String Subject, String Field) throws Exception {
//		QueryBuilder<ExpTable, Integer> statementBuilder = expDao.queryBuilder();
//		statementBuilder.where().like(Field, Subject); //% !!!!
//		PreparedQuery<ExpTable> preparedQuery = statementBuilder.prepare();
//
//		List<ExpTable> results = expDao.query(preparedQuery);
//
//		for (ExpTable aux : results) Main.logger.info(aux.getAuthor());
//		Main.logger.info(results.size());
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

// --Commented out by Inspection START (30.03.17 15:10):
//	public List<ExpTable> GetExp(String Subject, String Field) throws Exception {
//		QueryBuilder<ExpTable, Integer> statementBuilder = expDao.queryBuilder();
//		statementBuilder.where().like(Field, Subject);
//		PreparedQuery<ExpTable> preparedQuery = statementBuilder.prepare();
//
//		return expDao.query(preparedQuery);
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

	public ArrayList<String> getOrigFilesOfHunter(String inHunter) {

		ArrayList<String> res = new ArrayList<>();
		QueryBuilder<GeneHunterTable, Integer> statementBuilder = hunterDao.queryBuilder();

		try {
			statementBuilder.distinct().selectColumns("Original_file").where().eq("Hunter_file", inHunter);
			PreparedQuery<GeneHunterTable> preparedQuery = statementBuilder.prepare();
			List<GeneHunterTable> found = hunterDao.query(preparedQuery);
			for (GeneHunterTable foundEntry : found)
				res.add(foundEntry.Original_file);

		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}

		return res;
	}

	public List<GeneHunterTable> lookUpHunter(Hashtable<String,String> criteria) {
		QueryBuilder<GeneHunterTable, Integer> statementBuilder = hunterDao.queryBuilder();

		try {
			if (criteria.size() > 0) {
				Where where = statementBuilder.where();
				for (String field : criteria.keySet()) {
					if (tableInfo(field).equals("String")) {

						// in case of comma separated criteria
						if (criteria.get(field).contains(",")) {
							String[] parts = criteria.get(field).split(",");

//                            for (int k = 0; k < parts.length; ++k)
//                                where.like(field, "%" + parts[k] + "%");

							for (String part : parts)
								where.like(field, "%" + part + "%");

                            where.or(parts.length);

						} else
							where.like(field, "%" + criteria.get(field) + "%");
					}
					else {
						if (criteria.get(field).contains(">")) {
							where.gt(field, criteria.get(field).replace(">", ""));
						} else {
                            if (criteria.get(field).contains("<")) {
                                where.lt(field, criteria.get(field).replace("<", ""));
                            } else {
                                if (criteria.get(field).contains("-")) {
                                    String aux[] = criteria.get(field).split("-");
                                    where.between(field, aux[0], aux[1]);
                                }
                            }
                        }
					}
				}
                where.and(criteria.size());
				PreparedQuery<GeneHunterTable> preparedQuery = statementBuilder.prepare();
				Main.logger.info(preparedQuery.toString());
				return hunterDao.query(preparedQuery);
			} else {
				return hunterDao.queryForAll();
			}
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
			return null;
		}
		}

		public HashSet<String> getAccOfExp(ExpTable experiment) {
		
		HashSet<String> out = new HashSet<>();
		ForeignCollection<AccPhenTable> phens = experiment.getPhenCol();

		//long startTime = System.currentTimeMillis();
		for (AccPhenTable result : phens) out.add(result.getAcc());
		//Main.logger.info(System.currentTimeMillis() - startTime);

//		long startTime1 = System.currentTimeMillis();
//		Iterator<AccPhenTable> it = phens.iterator();
//		while (it.hasNext()) {
//			out.add(it.next().getAcc());
//		}
//		Main.logger.info(System.currentTimeMillis() - startTime1);

		return out;
	}
	
	public HashSet<String> findAllAccLike(String searchAcc) throws SQLException {
		
		HashSet<String> res = new HashSet<>();
		
		QueryBuilder<AccPhenTable, Integer> statementBuilder = acc_phenDao.queryBuilder();
		statementBuilder.where().like(AccPhenTable.SOURCE_ACC_NAME, "%"+searchAcc+"%");		
		PreparedQuery<AccPhenTable> preparedQuery = statementBuilder.prepare();

        List<AccPhenTable> found = acc_phenDao.query(preparedQuery);
		for(AccPhenTable tab : found) res.add(tab.accId);
		
		return res;
	}

	public ArrayList<ExpTable> findallExpwithAccList(HashSet<String> searchAccessions) throws SQLException {
			
		ArrayList<ExpTable> res = new ArrayList<>();
		HashSet<Integer> expIDs = new HashSet<>();
				
		QueryBuilder<AccPhenTable, Integer> statementBuilder = acc_phenDao.queryBuilder();
		
		for(String searchAcc : searchAccessions) {
			statementBuilder.where().eq(AccPhenTable.SOURCE_ACC_NAME, searchAcc);		
			PreparedQuery<AccPhenTable> preparedQuery = statementBuilder.prepare();
            List<AccPhenTable> found = acc_phenDao.query(preparedQuery);
			for (AccPhenTable dentro : found) expIDs.add(dentro.getExpTable().getId());
		}			
		for (int i : expIDs)
			res.add(expDao.queryForId(i));

		return res;
	}	

// --Commented out by Inspection START (30.03.17 15:10):
//	public ArrayList<ExpTable> findallExpwithAcc(String searchAcc) throws SQLException {
//
//		ArrayList<ExpTable> res = new ArrayList<>();
//		HashSet<Integer> expIDs = new HashSet<>();
//
//		QueryBuilder<AccPhenTable, Integer> statementBuilder = acc_phenDao.queryBuilder();
//		statementBuilder.where().like(AccPhenTable.SOURCE_ACC_NAME, "%"+searchAcc+"%");
//		PreparedQuery<AccPhenTable> preparedQuery = statementBuilder.prepare();
//        List<AccPhenTable> found = acc_phenDao.query(preparedQuery);
//
//		for (AccPhenTable dentro : found) expIDs.add(dentro.getExpTable().getId());
//		for (int i : expIDs) res.add(expDao.queryForId(i));
//		return res;
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

// --Commented out by Inspection START (30.03.17 15:10):
//	private ExpTable Get_ExpTablefrom_ID (Integer ID) throws Exception {
//        return expDao.queryForId(ID);
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

	public ArrayList<AccPhenTable> getPhenOfExp (ExpTable exp){
        return new ArrayList<>(exp.getPhenCol());
	}
	
	public ArrayList<AccPhenTable> getAccPhenOfExp (ExpTable exp, String acc){
        ArrayList<AccPhenTable> res = new ArrayList<>();
        ForeignCollection<AccPhenTable> phens = exp.getPhenCol();

        for (AccPhenTable found : phens) if (found.accId.equals(acc)) res.add(found);
        return res;
	}
	
	public ArrayList<AccRatesTable> getAccPhenRatesOfExp (ExpTable exp, String acc){
		ArrayList<AccRatesTable> res = new ArrayList<>();
		ForeignCollection<AccRatesTable> rates = exp.getRatesCol();
		
		for (AccRatesTable found : rates) if (found.ACC_ID.equals(acc)) res.add(found);
		return res;
	}
	
	public ArrayList<AccRatesTable> getPhenRatesOfExp (ExpTable exp){
	    return new ArrayList<>(exp.getRatesCol());
	}
	
	public ArrayList<AccPhenTable> getSameAccTimesteps(AccPhenTable in) {
		QueryBuilder<AccPhenTable, Integer> statementBuilder = acc_phenDao.queryBuilder();
		try {
			statementBuilder.where().eq(SinglePhenTable.SOURCE_EXP_NAME, in.getExpTable().getId())
			.and().eq(AccPhenTable.SOURCE_ACC_NAME,in.accId);
			PreparedQuery<AccPhenTable> preparedQuery = statementBuilder.prepare();

            return (ArrayList<AccPhenTable>) acc_phenDao.query(preparedQuery);
		} catch (SQLException ex) {
		    return new ArrayList<>();
		}
	}

	public void AddSinglePhensAll(ArrayList<SinglePhenTable> in) {
		try {
			single_phenDao.callBatchTasks(() -> {
				single_phenDao.create(in);
				return null;
			});
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

	public void AddSingleRatesAll(ArrayList<SingleRatesTable> in) {
		try {
			single_ratesDao.callBatchTasks(() -> {
				single_ratesDao.create(in);
				return null;
			});
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

	void AddGeneHunterTablesAll(ArrayList<GeneHunterTable> in) {
		try {
			hunterDao.callBatchTasks( () -> {
				try {
					hunterDao.create(in);
				} catch (SQLException ex1) {
					Main.logger.info("table missing - trying to fix...");
					TableUtils.createTable(connectionSource,GeneHunterTable.class);
					Main.logger.info("table missing - fixed...rerunning");

					// mini recursion...
					AddGeneHunterTablesAll(in);
				}
				return null;
			});
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

	public ArrayList<SinglePhenTable> getSingleAccOfExp(ExpTable exp) {
		ArrayList<SinglePhenTable> res = new ArrayList<>();
		QueryBuilder<SinglePhenTable, Integer> statementBuilder = single_phenDao.queryBuilder();
		try {
			statementBuilder.where().eq(SinglePhenTable.SOURCE_EXP_NAME, exp.getId());
			PreparedQuery<SinglePhenTable> preparedQuery = statementBuilder.prepare();
			return (ArrayList<SinglePhenTable>) single_phenDao.query(preparedQuery);
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return res;
	}

	public ArrayList<SingleRatesTable> getSingleRatesOfExp(ExpTable exp) {
		ArrayList<SingleRatesTable> res = new ArrayList<>();
		QueryBuilder<SingleRatesTable, Integer> statementBuilder = single_ratesDao.queryBuilder();
		try {
			statementBuilder.where().eq(SingleRatesTable.SOURCE_EXP_NAME, exp.getId());
			PreparedQuery<SingleRatesTable> preparedQuery = statementBuilder.prepare();
			return (ArrayList<SingleRatesTable>) single_ratesDao.query(preparedQuery);
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return res;
	}

// --Commented out by Inspection START (30.03.17 15:10):
//	public void AddSingleRates(ArrayList<AccRatesTable> in) {
//		try {
//			single_ratesDao.callBatchTasks(() -> {
//				in.forEach((AccRatesTable rate) -> {
//					try {
//						single_ratesDao.create(rate.getSingleValues());
//					} catch (SQLException ex) {
//						Main.logger.severe(Main.StackTraceToString(ex));
//					}
//				});
//				return null;
//			});
//		} catch (Exception ex) {
//			Main.logger.severe(Main.StackTraceToString(ex));
//		}
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

	public void AddMetas(List<SetTable> in) {
		try{
			setDao.callBatchTasks(() -> {
				setDao.create(in);
				return null;
			});
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

// --Commented out by Inspection START (30.03.17 15:10):
//	// unused
//	public List<SinglePhenTable> getSinglesOfExpPhens(ExpTable exp, AccPhenTable acc) {
//
//		QueryBuilder<SinglePhenTable,Integer> statementBuilder = single_phenDao.queryBuilder();
//		try {
//			statementBuilder.where().eq(SinglePhenTable.SOURCE_EXP_NAME, exp.getId()).and().
//			eq(SinglePhenTable.SOURCE_ACC_NAME,acc.accId);
//			PreparedQuery<SinglePhenTable> preparedQuery = statementBuilder.prepare();
//
//			return single_phenDao.query(preparedQuery);
//		} catch (SQLException ex) {
//		    return new ArrayList<>();
//		}
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

	public void addMetaToExp(ExpTable exp, ArrayList<String> values, ArrayList<String> property) {
		
		SetTable set2add = new SetTable();
		ClsSetterString<SetTable> setFieldTable = new ClsSetterString<>();
		
		setFieldTable.setFieldsByAnnotation(set2add, values, property);		
		set2add.setSourceExp(exp);
		
		try {
			setDao.create(set2add);
			updateExpTable(exp);
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}		
	}
	
	public ArrayList<ExpTable> getAllExp () { //part1

		Main.logger.info(" ******************** ");

		ArrayList<ExpTable> results = new ArrayList<>();
		
		boolean done = false;
		while(!done) try {
			for (ExpTable found : expDao) results.add(found);
			done = true;
		} catch (IllegalStateException ex) {
			//if (alterTable(expDao)) Main.logger.info("redone");
		}
		return results;
	}

	public ArrayList<String> getHunterFilesAsString(ExpTable exp) throws SQLException {
		ArrayList<String> res = new ArrayList<>();

		QueryBuilder<GeneHunterTable,Integer> hunterQuery = hunterDao.queryBuilder();
		hunterQuery.distinct().selectColumns("Hunter_file").where().
				eq(GeneHunterTable.SOURCE_EXP_NAME,exp.getId()).prepare();
		hunterDao.query(hunterQuery.prepare()).forEach((GeneHunterTable tab) -> res.add(tab.getHunterFile()));
		return res;
	}

//	public List<GeneHunterTable> getDistinctHunterFilesOfExp(ExpTable exp) throws SQLException {
//		Main.logger.info(exp.getId() + " " + exp.getName());
//
//		QueryBuilder<GeneHunterTable,Integer> hunterQuery = hunterDao.queryBuilder();
//		hunterQuery.distinct().selectColumns(GeneHunterTable.HUNTER_FILE).where().
//				eq(GeneHunterTable.SOURCE_EXP_NAME,exp.getId()).prepare();
//		return hunterDao.query(hunterQuery.prepare());
//	}
//
//	public List<GeneHunterTable> getHunterFilesOfExp(ExpTable exp) throws SQLException {
//		QueryBuilder<GeneHunterTable,Integer> hunterQuery = hunterDao.queryBuilder();
//		hunterQuery.where().eq(GeneHunterTable.SOURCE_EXP_NAME,exp.getId());
//		return hunterDao.query(hunterQuery.prepare());
//	}

	public List<GeneHunterTable> getHunterTabFromFileName(String hunterFile) {
        QueryBuilder<GeneHunterTable,Integer> hunterQuery = hunterDao.queryBuilder();
        try {
            hunterQuery.where().eq("Hunter_file",hunterFile);
            return hunterDao.query(hunterQuery.prepare());
        } catch (SQLException ex) {
            Main.logger.severe(Main.StackTraceToString(ex));
        }
        return null;
    }

	public List<GeneHunterTable> getHunterFilesFromStrings(ArrayList<String> inHunter) {
		QueryBuilder<GeneHunterTable,Integer> hunterQuery = hunterDao.queryBuilder();
		try {
			hunterQuery.where().in("Hunter_file",inHunter);
			return hunterDao.query(hunterQuery.prepare());
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return null;
	}

	public List<GeneHunterTable> getAllHunterFiles() throws SQLException {
		QueryBuilder<GeneHunterTable,Integer> hunterQuery = hunterDao.queryBuilder();
		hunterQuery.distinct().selectColumns("Hunter_file").prepare();
		return hunterDao.query(hunterQuery.prepare());
	}

	public List<SetTable> getSetsOfExp(ExpTable exp) {
		List<SetTable> res = new ArrayList<>();
		QueryBuilder<SetTable, Integer> statementBuilder = setDao.queryBuilder();		
		try {
			statementBuilder.where().eq(SetTable.SOURCE_EXP_NAME, exp.getId());
			PreparedQuery<SetTable> preparedQuery = statementBuilder.prepare();
			res = setDao.query(preparedQuery);
		} catch (SQLException ex) {
			return res;
		}				
		return res;				
	}

	private List<SinglePhenTable> getAllSinglePhensOfExp (ExpTable exp) {

		List<SinglePhenTable> res = new ArrayList<>();
		QueryBuilder<SinglePhenTable, Integer> statementBuilder = single_phenDao.queryBuilder();
		try {
			statementBuilder.where().eq(SinglePhenTable.SOURCE_EXP_NAME, exp.getId());
			PreparedQuery<SinglePhenTable> preparedQuery = statementBuilder.prepare();
			res = single_phenDao.query(preparedQuery);
		} catch (SQLException ex) {
			Main.logger.severe(" sql error");
		}
		return res;
	}

	// returns all "@Column" annotated annotations != variable names !!!
	public <T> ArrayList<String> getTableCaptions(Class<T> inCls)  {
		ArrayList<String> res = new ArrayList<>();
		// get just publics
		for (Field fld : inCls.getFields())
			if (fld.isAnnotationPresent(Column.class)) try {
				res.add(fld.getAnnotation(Column.class).name());
			} catch (NullPointerException ex) {
				//logger.debug("unable to retrieve values from " + inCls.getName() + " " + fld.getName(), e);
			}
		return res;
	}

	// returns all (ALSO PRIVATE) "@Column" annotated annotations != variable names !!!
	public <T> ArrayList<String> getAllTableCaptions(Class<T> inCls)  {
		ArrayList<String> res = new ArrayList<>();
		// get's all
		for (Field fld : inCls.getDeclaredFields())
			if (fld.isAnnotationPresent(Column.class)) try {
				res.add(fld.getAnnotation(Column.class).name());
			} catch (NullPointerException ex) {
				//logger.debug("unable to retrieve values from " + inCls.getName(), e);
			}
		return res;
	}
	
	// returns an ArrayList<String> of "@Column" annotated annotations!!!
	public <T> ArrayList<String> getTableFieldNames(Class<T> inCls)  {
		ArrayList<String> res = new ArrayList<>();
		//just for PUBLIC annotated fields!
		for (Field fld : inCls.getFields())
			if (fld.isAnnotationPresent(Column.class)) try {
				res.add(fld.getName());
			} catch (NullPointerException ex) {
				//logger.debug("unable to retrieve values from " + inCls.getName(), e);
			}
		return res;
	}
	
	// returns an ArrayList<String> of "@Column" annotated annotations!!! - ALSO PRIVATES
	public <T> ArrayList<String> getAllTableFieldNames(Class<T> inCls)  {
		ArrayList<String> res = new ArrayList<>();
		//just for annotated fields!
		for (Field fld : inCls.getDeclaredFields())
			if (fld.isAnnotationPresent(Column.class)) try {
				res.add(fld.getName());
			} catch (NullPointerException ex) {
				//logger.debug("unable to retrieve values from " + inCls.getName(), e);
			}
		return res;
	}

	@SuppressWarnings("unchecked")
	public <T,E> E getValueOfTableAsProperty(T inCls, String fieldName) {
		try {
			Field fld = inCls.getClass().getDeclaredField(fieldName);
			String typename = fld.getType().getSimpleName();
			
			switch (typename) {
				case "Double":
					Double tableValue = (Double)fld.get(inCls);				
					if (tableValue != null) return (E) new SimpleDoubleProperty(tableValue);
					else return (E) new SimpleDoubleProperty(Double.NaN);
				case "String":
					return (E) new SimpleStringProperty(String.valueOf(fld.get(inCls)));
				case "Integer":
					Integer intValue = (Integer)fld.get(inCls);
					if (intValue != null) return (E) new SimpleIntegerProperty(intValue);
					else return (E) new SimpleIntegerProperty(0);
//				case "Boolean":
//					// 300317: sqlite has not boolean type => cast to int
//					Boolean boolVal = (Boolean) fld.get(inCls);
//					Main.logger.info(fieldName + " " + boolVal);
//					return (E) new SimpleBooleanProperty(boolVal);
				}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
        return null;
	}
	
	public <T> String getValueOfTableAsString(T inCls, String fieldName) {
		String value = null;
		try {
			Field fld = inCls.getClass().getDeclaredField(fieldName);
			value = String.valueOf(fld.get(inCls));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return value;
	}
	
	private void fillVersTable() {
		
		Map<String,List<String>> translate = new LinkedHashMap<>();
		ArrayList<String> v0Variables = new ArrayList<>();
		List<String> prepos = Arrays.asList("mean_", "median_", "stddev_","nindiv_");
		String aux;
				
		///////////////////////   acc_phenTable ///////////////////////
		Field[] flds = AccPhenTable.class.getDeclaredFields();

		for (Field fld : flds) {
			aux = fld.toString();
			aux = aux.substring(aux.lastIndexOf(".") + 1, aux.length()); // +1 to remove "." itself too

			for (String pre : prepos) {
				if (aux.contains(pre)) {
					aux = aux.replace(pre, "");
					continue;
				}
				v0Variables.add(aux);
			}
		}
		
		for (String str0 : v0Variables) {
			
			translate.put(str0, new ArrayList<>());
			
			switch(str0) {
				case "total_length":
					translate.get(str0).add("rootlength"); 	// v0
					break;
				case "euclidian_length":
					translate.get(str0).add(""); 
					break;
				case "root_tortuosity":
					translate.get(str0).add(""); 
					break;
				case "root_angle":
					translate.get(str0).add("angle2grav"); 
					break;
				case "root_directional_equivalent":
					translate.get(str0).add("diridx"); 
					break;
				case "root_horizontal_index":
					translate.get(str0).add(""); 	
					break;
				case "root_vertical_index":
					translate.get(str0).add(""); 	
					break;
				case "root_linearity":
					translate.get(str0).add("corrxy"); 	
					break;
				case "average_root_width":
					translate.get(str0).add("avgrootwidth"); 	
					break;
				case "root_width_20":
					translate.get(str0).add("rootwidth20"); 	
					break;
				case "root_width_40":
					translate.get(str0).add("rootwidth40"); 	
					break;
				case "root_width_60":
					translate.get(str0).add("rootwidth60"); 	
					break;
				case "root_width_80":
					translate.get(str0).add("rootwidth80"); 	
					break;
				case "root_width_100":
					translate.get(str0).add("rootwidth100"); 	
					break;
			}
		}
		
		// for acc rates table just:		
		translate.put("root_growth_rate", new ArrayList<>());
		translate.get("root_growth_rate").add("gr_rootlength");
				
		// for singles the following are kept:
		translate.put("set", new ArrayList<>());
		translate.get("set").add("set");
		
		translate.put("plate", new ArrayList<>());
		translate.get("plate").add("plate");
		
		translate.put("abs_pos", new ArrayList<>());
		translate.get("abs_pos").add("abs_pos");
		
		translate.put("core_filename", new ArrayList<>());
		translate.get("core_filename").add("core_filename");
		
		translate.put("roi", new ArrayList<>());
		translate.get("roi").add("roi");
	
		// for singles some have changed:
		
		addVersionTable(translate);	
		Main.logger.info("done");
	}
	
	ArrayList<String> translateCaptionsAcc(ArrayList<String> in) {
		
		ArrayList<String> out = new ArrayList<>();
		List<String> prepos = Arrays.asList("mean_","median_","stddev_","nindiv_");
		QueryBuilder<VersionTable,Integer> sB = versionDao.queryBuilder();
				
		try {
			versionDao.callBatchTasks(() -> {
				String aux = null;
				for (String lookup : in) {
					// cutting prepos; mean_gr_rootlength => gr_rootlength
					for (String pre : prepos) {
						if (lookup.contains(pre)) {
							lookup = lookup.replace(pre, "");
							aux = pre;
							break;
						}
					}

					// find trait variable name in current version
					//sB.where().eq(version, lookup);
					sB.where().eq(VersionTable.V0_NAME,lookup);
					VersionTable found = versionDao.queryForFirst(sB.prepare());

					// if found use it and add prepos again
					// else it has no equivalent in curr version
					// and label it as "not_present"
					if (found != null) {
						lookup = found.currentName;
						//aux = aux.concat(lookup);
						aux += lookup;
						out.add(aux);
					} else out.add("not_present"); // raises caught error in set field reflection!!!
				}
				return null;
			});
		}
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}	
		return out;
	}
	
	// version for single measurements - no prepos (mean, median, stddev, indiv)! 
	ArrayList<String> translateCaptionsSingles(ArrayList<String> in) {
			
		ArrayList<String> out = new ArrayList<>();
		QueryBuilder<VersionTable,Integer> sB = versionDao.queryBuilder();
				
		try {
			versionDao.callBatchTasks(() -> {
				for (String lookup : in) {
					sB.where().eq(VersionTable.V0_NAME, lookup);
					VersionTable found = versionDao.queryForFirst(sB.prepare());

					// currently 2408 not translated and therefore not stored of
					// v0 single values are:
					// gravitropicDir
					// GravScore
					// sdX_sdY_per_Pix
					// TotLen/EurcLen

					if (found != null) {
						out.add(found.currentName);
					} else out.add("not_present"); // raises caught error in set field reflection!!!
				}
				return null;
			});
		}
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return out;
	}
	
	private void addVersionTable(Map<String, List<String>> translate) {
		try {
            versionDao.callBatchTasks(() -> {
                for (Map.Entry<String, List<String>> mentry : translate.entrySet() )
                    if (!mentry.getValue().isEmpty()) {
                        VersionTable versionEntry = new VersionTable(mentry.getKey(), mentry.getValue().get(0));
                        versionDao.create(versionEntry);
                    }
                // manual hack for multiple keys
                versionDao.create(new VersionTable("total_length", "tl_root"));
                return null;
            });
		}
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
	
	public void updateExpTable(ExpTable exp) {
		try {
			expDao.callBatchTasks(() -> {
                expDao.update(exp);
                return null;
			});
		}
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
	
	public void addExperimentsFromFile(String inputFile) {
        
        String line,str1,expName, author, expDesc, accFile, singleFile, quantFile, metaFile, defPath;
        String myFiles[] = new String[3];
        int count;

        count = 0;
        expDesc = "";
        
        metaFile = null;
       
        try (FileReader fr = new FileReader(inputFile); BufferedReader br = new BufferedReader(fr)) {
            try {
                while ((line = br.readLine()) != null) {
                	
                    if(line.startsWith("!"))
                    	continue;  // my comment token

                    if(line.length() < 2)
                    	continue;		// ignore empty line

					str1 = line.substring(line.lastIndexOf('/')+1,line.length());
                    
                    if(str1.startsWith("."))
                    	continue;  // hidden file issue
                   
                    if (line.contains("meta") || line.contains(".csv"))
                    	metaFile = line;
					else {
                    	myFiles[count] = line;
                    	Main.logger.finest("count is: " + count + " with file: " + myFiles[count]);
                    	count++;
                    }
                                   
                    if (count == 3) {
                                       
                        accFile = null;
                        singleFile = null;
                        quantFile = null;
                        
                        expName = str1.substring(0, str1.indexOf("_single"));
                        defPath = line.substring(0,line.lastIndexOf("/"));

                        if (expName.length() < 1)
							expName = line.substring(line.indexOf("/")+1,line.lastIndexOf("/"));

                    	//author = Files.getOwner(Paths.get(line)).getName();
                        
						author = line.substring(0, line.indexOf("/",1));

                        for (int k = 0;k<3;++k) {
                            str1 = myFiles[k].substring(myFiles[k].lastIndexOf('/')+1,myFiles[k].lastIndexOf('_'));
                            Main.logger.info(str1);
                           
                            // hopefully nobody ever reads that....
                            if (str1.contains("acc_phen")) {
                                accFile = myFiles[k];
                                continue;
                            }
                            if (myFiles[k].contains("single_root_phenotypes")) {
                                singleFile = myFiles[k];
                                continue;
                            }
                            if (myFiles[k].contains("quant_single_root")) {
                                quantFile = myFiles[k];
                            }
                        }
                                                                               
                        ExpTable addme = AddData(expName,author,expDesc,defPath,
                        		author + "/" + expName + "/Jpgs", author + "/" + expName + "/Coordinate_Files");

						Path auxPath = Paths.get(ConfigCls.getMyProp("mount_path"), accFile);
                        if (accFile != null) try {

							FileReadAcc fi = new FileReadAcc(auxPath.toString());
							try {
								Main.logger.fine("trying to add phenTable to " + addme.name);
								fi.ReadInData(addme, this);
							} catch (IOException ex) {
								Main.logger.severe(Main.StackTraceToString(ex));
							}
						} catch (FileNotFoundException ex) {
							Main.logger.info(new Date().toString() + " file error " + auxPath.toString() + "\n" + ex);
						}
						else Main.logger.info("accession file not found");
                        
                        if (metaFile != null) {
							auxPath = Paths.get(ConfigCls.getMyProp("mount_path"), metaFile);
                        	Main.logger.info(new Date().toString() + " meta file here");
                        	FileReadMeta fi = new FileReadMeta(auxPath.toString());
                        	fi.ReadInData(addme, this);
                        }
                       
                        if (singleFile != null && quantFile != null) {
                        	auxPath = Paths.get(ConfigCls.getMyProp("mount_path"), singleFile);
                        	Path auxPath2 = Paths.get(ConfigCls.getMyProp("mount_path"), quantFile);
	                        FileReadSingle fi = new FileReadSingle(auxPath.toString(), auxPath2.toString());
	                        fi.ReadInData(addme, this);
	                        count = 0;
						}
                        metaFile = null;
                    	}
                    }
                } catch (IOException ex) {
                        Main.logger.severe(Main.StackTraceToString(ex));
                }
        } catch (FileNotFoundException ex) {
        	Main.logger.info("file: " + inputFile + " not found");
        } catch (IOException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
        } 
	}
		
	private void updateSingleTables(List<SinglePhenTable> in) {
		try {
			single_phenDao.callBatchTasks(() -> {
                in.forEach((SinglePhenTable i) -> {
                    try {
                        single_phenDao.update(i);
                    } catch (SQLException ex) {
                        Main.logger.severe(Main.StackTraceToString(ex));
                    }
                });
                return null;
            });
		}
		catch (Exception ex){
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}
	
// --Commented out by Inspection START (30.03.17 15:10):
//	public void addMetaFileToExp(ExpTable exp, String metaFile ) {
//		try {
//			FileReadMeta fi = new FileReadMeta(metaFile);
//			fi.ReadInData(exp, Main.database);
//		} catch (FileNotFoundException ex) {
//			Main.logger.severe(Main.StackTraceToString(ex));
//		}
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

	public void addCooForAllExps() {
		
		ArrayList<ExpTable> allExps = getAllExp();

		for (ExpTable exp : allExps) {

			if (exp.getCooPath() != null) {

				try {
					if (isDirEmpty(Paths.get(ConfigCls.getMyProp("mount_path"), exp.getCooPath()))) {
						exp.setCooPath("");
						Main.logger.warning("no coordinate files found for " + exp.name
								+ " in directory " + exp.getCooPath());
					} else {
						addCooToExp(exp);
						Main.logger.warning("coordinate files ok " + exp.name
								+ " in directory " + exp.getCooPath());
					}
				} catch (IOException e) {
					// in case of IOEx just ignore
					// e.printStackTrace();
				}
			}
		}

		printNow();	

//		 Task<Void> longTask = new Task<Void>() {
//	            @Override
//	            protected Void call() throws Exception {
//	            	ArrayList<ExpTable> allExps = getAllExp();
//	        		for (ExpTable exp : allExps) {
//	        			//logger.info("trying to set coordinates for experiment: " + exp.name);
//	        			addCooToExp(exp);
//	        		}
//	            	return null;
//	            }
//	        };
//	        longTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//	            @Override
//	            public void handle(WorkerStateEvent t) {
////					rootMasker.setVisible(false);
////					addExp.setDisable(false);
////					lookUpButton.setDisable(false);
////					resetButton.setDisable(false);
////			         				
////	 				expList.setItems(InitGUI.getExpData());
////	 				updateDbInfoGrid();
//	 				//logger.info("updated paths for " + InitGUI.database.getDbName());
//	            }
//	        });
//	        new Thread(longTask).start();    	
		
//		for (ExpTable exp : allExps) {
//			//logger.info("trying to set coordinates for experiment: " + exp.name);
//			
//			addCooToExp(exp);
//		}
//		printNow();		
	}

	private static boolean isDirEmpty(final Path directory) throws IOException {
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		}
	}
	
	public void addCooToExp(ExpTable exp) {

		ArrayList<String> listOfFiles = new ArrayList<>();	// files in directory
		ArrayList<String> foundFiles = new ArrayList<>();	// coordinate files in directory

		Map<Integer,List<Integer>> roiMap = new HashMap<>();
		Map<String, List<Integer>> fileMap = new HashMap<>();

		Integer roi;
		String setStr, dayStr, fullStr, plateStr, filePrefix;
		Path fullpath;
		Boolean readInError = false;
		
		// sourcePath is exp.picPath, if empty then there are no pix nor coordinates present
		if ((exp.getCooPath() == null) || (exp.getCooPath().isEmpty())) {
			Main.logger.severe("empty path for " + exp.name);
			return;
		}
		
		DecimalFormat form = new DecimalFormat("000");

		// no slashs there!!!!!!
		filePrefix = ConfigCls.getMyProp("coordinates_prefix"); 
		
		Path filePath = Paths.get(ConfigCls.getMyProp("mount_path"),exp.getCooPath());
		Main.logger.info(filePath.toString());
		
		try(DirectoryStream<Path> myFiles = Files.newDirectoryStream(filePath,"*.{ser,txt}")) {
			for (Path entry : myFiles)
				listOfFiles.add(entry.toString());
		} catch (IOException ex) {
			Main.logger.warning("directory stream error in add coordinates " + ex);
			Main.logger.warning("skipping coordinates and clearing cooPath and "
					+ "picPath for experiment " + exp.getName());
			exp.cooPath = null;
			exp.picPath = null;
			updateExpTable(exp);
			return;
		}
		
		Main.logger.info(new Date().toString() + " found " + listOfFiles.size() + " files in folder " + filePath.toString());

		// for safety reasons...folder could contain other stuff too
		if (listOfFiles.size() != 0) {
			listOfFiles.forEach((String item) -> {
				String file = item.substring(item.lastIndexOf("/") + 1);
				if (file.contains(filePrefix)) {
					file = file.replace(filePrefix, ""); // 2312 prior use?!
					file = file.replace(".txt", "");
					file = file.replace(".ser", "");
					foundFiles.add(file);
				}
			});
		}

		List<SinglePhenTable> singles = getAllSinglePhensOfExp(exp);
		Main.logger.info("found " + singles.size() + " single measurements for " + exp.getName());

		Integer count = 0;
		for (SinglePhenTable sig : singles) {
			if (sig.core_filename != null) {
				if (sig.core_filename.contains("_set")) {

					if(!fileMap.containsKey(sig.core_filename)) {
						fileMap.put(sig.core_filename,new ArrayList<>());
					}

					fileMap.get(sig.core_filename).add(count);
				}
				else //old BRAT version workaround
				{
					setStr = "_set" + String.valueOf(sig.set.intValue());
					dayStr = String.format("%d", Integer.parseInt(sig.timestep));

					fullStr = setStr + "_day" + dayStr + "_";  //0901: day1_ and day11_ bug corrected !!!
					plateStr = "_" + form.format(sig.plate);

					for (String aux1 : foundFiles) {
						if (aux1.contains(fullStr) && aux1.contains(plateStr)) {
							sig.core_filename = aux1;

							if(!fileMap.containsKey(sig.core_filename)) {
								fileMap.put(sig.core_filename,new ArrayList<>());
							}

							fileMap.get(sig.core_filename).add(count);
							break;
						}
					}
				}
			}
			count++;
		}

		if (fileMap.keySet().size() == 0)
			Main.logger.info("no coordinates files found for " + exp.name + " in folder " + exp.getCooPath());
		else
			Main.logger.info("found " + fileMap.keySet().size() + " files, reading in... ");

		for (String inStr : fileMap.keySet()) {
			fullpath = Paths.get(filePath.toString(),filePrefix + inStr + ".txt");
			if (Files.exists(fullpath)) {  // look for pod txt coordinates file
				FileReadCoo readFile = new FileReadCoo(fullpath.toString());
				roiMap = readFile.readInCoo();
			} else {
				fullpath = Paths.get(filePath.toString(),filePrefix + inStr + ".ser");

				if (Files.exists(fullpath)) {  // has to be serialized coordinates file
					FileReadCooSer readFile = new FileReadCooSer();
					roiMap = readFile.readIn(fullpath.toString());
				}
			}
			// roiMap can be empty in case coo file does exist but is also empty
			if (!roiMap.isEmpty()) {
				for (Integer singleId : fileMap.get(inStr)) {
					roi = singles.get(singleId).roi;
					try {
						singles.get(singleId).setx1(roiMap.get(roi).get(1)); // x min
						singles.get(singleId).setx2(roiMap.get(roi).get(0)); // x max
						singles.get(singleId).sety1(roiMap.get(roi).get(3)); // y min
						singles.get(singleId).sety2(roiMap.get(roi).get(2)); // y max
					} catch (NullPointerException ex) {

						// we get here in case a Roi is given in the
						// quant file which is not present in the resp.
						// coordinates file => something messed up
						// during processing

						Main.logger.severe("ROI "+ roi  +
										"timestep " + singles.get(singleId).timestep + "  found in " +
										fullpath.toString() +" but not in brat results");
						readInError = true;
					}
				}
			} else {
				Main.logger.info("empty roi map for file " + fullpath.toString());
			}
		}			
		updateSingleTables(singles);

		if (readInError) {
			exp.desc += "\npossible issues with coordinate files detected...";
			updateExpTable(exp);
		}
	}

	void setExpBratVersion(ExpTable exp, String bratVersion){
		exp.brat_version = bratVersion;
		updateExpTable(exp);
	}
	
	public String getDbPath() {
		return db.getAbsolutePath();
	}
	
	public String getDbName() {
		return db.getName();
	}
	
	public double getDbSize() {
		return this.db.length()/ (1024.0 * 1024.0);
	}
	
	public String getDbDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");		
		return sdf.format(db.lastModified());				
	}
	
//	private Boolean alterTable(Dao inDao) { //part2
//
//		Dao updateDao = inDao;
//		String tableName, cmdSql;
//		ArrayList<String> sourceTable = getTableCaptions(ExpTable.class);
//
//		tableName = DatabaseTableConfig.extractTableName(expDao.getDataClass());
//
//        Main.logger.info(tableName);
//
//		try {
//			updateDao.executeRaw("ALTER TABLE experiments ADD COLUMN altTest8 INTEGER"); //- WORKS!!!
//
//			//// NOTE:
//			// SQLite supports a limited subset of ALTER TABLE. The ALTER TABLE command in
//			// SQLite allows the user to rename a table or to add a new column to an existing table.
//			// It is not possible to rename a column, remove a column, or add or remove constraints
//			// from a table.
//			/////////////
//
//
//		} catch (SQLException ex) {
//			Main.logger.severe(Main.StackTraceToString(ex));
//		}
//
//		return true;
//	}

	private void printNow() {
	    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 	    Main.logger.info("finished at: " + dateFormat.format(new Date()));
	}
	
	private Dao<CooTable, Integer> getCooDao() {
		
		Dao<CooTable, Integer> cooDao = null;
		try {
			cooDao = DaoManager.createDao(this.connectionSource, CooTable.class);	
			if (!cooDao.isTableExists()) {
				try {
					TableUtils.createTable(connectionSource, CooTable.class);
				} catch (SQLException ex) {
					Main.logger.severe("could not create coordinates table/n" + ex);
					Main.logger.severe(Main.StackTraceToString(ex));
				}
			}
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return cooDao;
	}
	
	public void addVictorsCooToExp(ExpTable exp) {
		
		ArrayList<SinglePhenTable> subsample = new ArrayList<>();
		ArrayList<RoiCoo> roiCooArray = new ArrayList <>();
		Map <Integer, RoiCoo> readIn = new LinkedHashMap<>();
		Set<String> coreFiles = new HashSet<>();
		Integer roi;
		String filePathStr;
		String sourcePath = exp.getCooPath();
		Path path;
		Boolean found;
		
		// sourcePath is exp.picPath, if empty then there are no pix nor coordinates present
		if ((sourcePath == null) || (sourcePath.isEmpty())) {
			Main.logger.info("viktor, empty cooPath for exp " + exp.getName());
			return;
		}
		
		List<SinglePhenTable> singles = getAllSinglePhensOfExp(exp);
		
		// here all core file names should already be in correct format
//		for (int i = 0; i< singles.size(); i++) //
//			if (singles.get(i).core_filename != null) coreFiles.add(singles.get(i).core_filename);

		for (SinglePhenTable single : singles)
			if (single.core_filename != null) coreFiles.add(single.core_filename);

		Path filePath = Paths.get(ConfigCls.getMyProp("mount_path"),exp.getCooPath());
		filePathStr = filePath.toString();
		Main.logger.info(filePathStr);
		
		for (String inStr : coreFiles) {
			found = false;
			
			path = Paths.get(filePathStr,"Object_Coordinates_" + inStr + ".txt");
			
			if (Files.exists(path)) {  // look for pod txt coordinates file
				FileReadCooAll readFile = new FileReadCooAll(path.toString());
				readIn = readFile.readInCoo();

				// roiMap can be empty in case coo file does exist but is also empty
				if (!readIn.isEmpty())
					found = true;
				else
					Main.logger.info("read file " + filePathStr);

			} else {
				filePathStr = sourcePath + "/Object_Coordinates_" + inStr + ".ser"; 
				path = Paths.get(filePathStr);
				
				if (Files.exists(path)) {  // has to be serialized coordinates file
					
					Main.logger.info("MISSING STILL");
					
//					FileReadCooSer readFile = new FileReadCooSer();
//					roiMap = readFile.readIn(filePathStr);

					// roiMap can be empty in case coo file does exist but is also empty
					if (!readIn.isEmpty())
						found = true;
					else
						Main.logger.info("reading file " + filePathStr);

				} else
					Main.logger.info("cant find coo input for " + filePathStr);
			}	
			
			if (found) for (SinglePhenTable single : singles) {
				if (single.core_filename != null) if (single.core_filename.equals(inStr)) {
					roi = single.roi;
					subsample.add(single);
					roiCooArray.add(readIn.get(roi));
				}
			}
			
			addVictorCooTable(exp,subsample,roiCooArray);
			subsample.clear();
			roiCooArray.clear();
			Main.logger.info("added victors coordinates for exp " + exp.name + " from file " + inStr);
		}			
	}
	
	private void addVictorCooTable(ExpTable exp, ArrayList<SinglePhenTable> input, ArrayList<RoiCoo> roiCoo){
		Dao<CooTable, Integer> cooDao = getCooDao();
		try {
			cooDao.callBatchTasks(() -> {
				for (int i = 0;i<input.size();++i) {
					CooTable newCoo = new CooTable(exp.getName(),input.get(i).getAccPhenTable().accId, input.get(i).timestep);
					roiCoo.get(i).addRoiCooToTable(newCoo);
					cooDao.create(newCoo);
				}
				return null;
			});
		} catch (Exception ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}		
	}
	
// --Commented out by Inspection START (30.03.17 15:10):
//	public void readVictorCooTable(ExpTable exp, String acc, String timestep, Integer roiId) {
//		Dao<CooTable, Integer> cooDao = getCooDao();
//		QueryBuilder<CooTable,Integer> cooStatementBuilder = cooDao.queryBuilder();
//
//		try {
//			cooDao.callBatchTasks(() ->{
//				cooStatementBuilder.where().eq("Source_Exp", exp.name).and().
//					eq("Source_Acc",acc).and().eq("Timestep", timestep).and().eq("Roi", roiId);
//				CooTable found = cooDao.queryForFirst(cooStatementBuilder.prepare());
//
//				Main.logger.info("oRootCoo " + found.nPpixel);
//				ArrayList<Integer> test = readBytes(found.pRootCoo, 3*found.nPpixel); //3 for x,y, distanceMap
//				Main.logger.info("test size: " + test.size());
//
//				int aux = test.size()/3;
//				for (int i = 0; i< aux ; ++i)
//					Main.logger.info("entry: " + i + " x: " + test.get(i) + " y: " + test.get(aux + i) +
//							" distanceMap: " + test.get(2 * aux + i));
//
//				Main.logger.info("in Roi: " + found.roi);
//				return null;
//			});
//		} catch (Exception ex) {
//			Main.logger.severe(Main.StackTraceToString(ex));
//		}
//	}
// --Commented out by Inspection STOP (30.03.17 15:10)

	public void refreshExpTable(ExpTable in) {
		try {
			expDao.refresh(in);
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
		}
	}

	public String tableInfo(String colName) {
		try {
			TableInfo<GeneHunterTable, String> test = new TableInfo<>(connectionSource,null,GeneHunterTable.class);
			return test.getFieldTypeByColumnName(colName).getType().getSimpleName();
		} catch (SQLException ex) {
			Main.logger.severe(Main.StackTraceToString(ex));
			return null;
		}
	}

	public ArrayList<String> dataBaseStats() {
		ArrayList<String> stats = new ArrayList<>();
//		try {
//			GenericRawResults<String[]> results
//					= acc_ratesDao.queryRaw("SELECT name FROM sqlite_master WHERE type='table'");
//			for (String[] result : results) {
//				String tabName = Arrays.toString(result).replaceAll("[^a-zA-Z_]","");
//				GenericRawResults<String[]> entities = acc_ratesDao.queryRaw("SELECT count(*) FROM " + tabName);
//				for (String[] res : entities) stats.put(tabName,Arrays.toString(res));  //Main.logger.info(Arrays.toString(res));
//			}
//		} catch (SQLException ex) {
//			Main.logger.severe(Main.StackTraceToString(ex));
//		}

		try {
			GenericRawResults<String[]> results = acc_ratesDao.queryRaw("SELECT * FROM sqlite_sequence");
			for (String[] res : results) {
				stats.add(Arrays.toString(res));//.replaceAll("[^a-zA-Z_0-9]",""));
			}
		} catch (SQLException ex) {
			Main.logger.info("error fetching SQLITE stats from sqlite_master or sqlite_sequence table" +
					" - no SQLITE database used?!");
			Main.logger.severe(Main.StackTraceToString(ex));
		}
		return stats;
	}

	// test fct for Viktor blob reading/ writing
//	private ArrayList<Integer> readBytes(byte[] in, Integer size) {
//		ArrayList<Integer> res = new ArrayList<>(size);
//		try (InputStream is = new ByteArrayInputStream(in);	DataInputStream dis = new DataInputStream(is)) {
//			while(dis.available()>0)
//			{
//			   Integer k = dis.readInt();
//			   res.add(k);
//			}
//		} catch (IOException ex) {
//			Main.logger.severe(Main.StackTraceToString(ex));
//		}
//		return res;
//	}
}