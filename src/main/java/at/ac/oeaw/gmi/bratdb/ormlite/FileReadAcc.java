package at.ac.oeaw.gmi.bratdb.ormlite;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.app.Reflection_Cls_Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileReadAcc {

	private String bratVersion = null;
	private String firstline = null;
	private String seperator = "\t";
	private Integer accIdCol;
	private ArrayList<String> Captions = new ArrayList<>();
	private final ArrayList<String> RowValues = new ArrayList<>();
	private final Map<String,List<Integer>> indexMap = new LinkedHashMap<>(); //LinkedHashMap to keeps order!
	private final Map<String,List<Integer>> indexMap2 = new LinkedHashMap<>();
	
	private BufferedReader br;
	
	private int need2translate = 0;
	
	public FileReadAcc(String Fname) throws FileNotFoundException {		
		br = new BufferedReader(new FileReader(Fname));
	} //def. constructor
	
	private void getfirstline() {
		try {
			firstline = this.br.readLine();
			
			int count = firstline.length() - firstline.replace(seperator, "").length();
			if (count == 0)
				seperator = ",";
			
			if (firstline.contains("mean_GR_rootLength_day")) {
				bratVersion = "v0";
				need2translate = 1; // translate into v0 traits flag
				getfirstline_v0(firstline);
			} else {
				bratVersion = "v1";
				need2translate = 0;
				getfirstline_v1(firstline);   // that's the current version !!!
			}
		} catch (IOException ex) {
			Main.StackTraceToString(ex);
		}
	}

	private void getfirstline_v0(String firstline) {
		int i;
		String pattern = "(.*)(_day_)(\\d+)(.*)"; //pattern to find "day" entries
		
		/* pattern splits "mean_total_length_day001" into:
		 * group (0) mean_total_length_day001
		 * group (1) mean_total_length
		 * group (2) _day
		 * group (3) 001
		 */
		
		String pattern2 = "(.*)(_avg)(.*)";		  //pattern to find "avg" entries
		String found, aux;

		Collections.addAll(RowValues, firstline.split(seperator));
		Pattern r = Pattern.compile(pattern);   // search for "_day_"
		Pattern r2 = Pattern.compile(pattern2); // search for "_avg"

		for (i=0; i<RowValues.size(); ++i) {

			aux = RowValues.get(i).trim().toLowerCase();
			Matcher m = r.matcher(aux);

			if (m.find()) { // if found "_day_"
				if (m.group(4).isEmpty()) { // just single day values e.g. day 1,but not rate 1-2
					found = m.group(3);     // daytoken e.g. "1", "2",...
					if (!indexMap.containsKey(found)) {
						indexMap.put(found,new ArrayList<>());
					}
					indexMap.get(found).add(i);
				} else {   // else its a rate
					found = m.group(3) + m.group(4);
					if (!indexMap2.containsKey(found)) {
						indexMap2.put(found,new ArrayList<>());
					}
					indexMap2.get(found).add(i);
				}
				Captions.add(m.group(1).replace("/","_"));
			}
			else { // else find "_avg"
				Matcher m2 = r2.matcher(aux);		
				if (m2.find()) { 

					if (! m2.group(1).contains("_gr_")) { // manually excluding *_gr_1-2 in captions

						found = m2.group(2).replace("_", "");
						if (! m2.group(3).isEmpty()) {
							Main.logger.warning("Read in Error!!! " + aux );
						}
						if (!indexMap.containsKey(found)) {
							indexMap.put(found,new ArrayList<>());
						}
						indexMap.get(found).add(i);
					} else {
						found = m2.group(2).replace("_", "");
						if (! m2.group(3).isEmpty()) {
							Main.logger.warning("Read in Error!!! " + aux );
						}
						if (!indexMap2.containsKey(found)) {
							indexMap2.put(found,new ArrayList<>());
						}
						indexMap2.get(found).add(i);
					}

					Captions.add(m2.group(1).replace("/","_")); //captions have to be appended always !!!

				} else {
					if(RowValues.get(i).toLowerCase().equals("acc_id")) {
						accIdCol = i;
						Captions.add(RowValues.get(i));
					}
				}
			}
		}
	}
			
	private void getfirstline_v1(String firstline) {
		int i;
		String patternRate = "(.*)(_rate_)(.*)";      // pattern to find "_rate_"
		String patternAvg = "(.*)(avg)(.*)";          // pattern to find "_avg" entries
		String patternDay = "(.*)(_day)(\\d+)(-day)(\\d+)(.*)";  // pattern to find "_day00X-day00Y" entries
		String patternDay2 = "(.*)(_day)(\\d+)(.*)";
		String found, aux;

		Collections.addAll(RowValues, firstline.split(seperator));
		Pattern pRate = Pattern.compile(patternRate);   // search for "_rate"
		Pattern pAvg = Pattern.compile(patternAvg); // search for "_avg"
		Pattern pDay = Pattern.compile(patternDay);
		Pattern pDay2 = Pattern.compile(patternDay2);

		for (i = 0; i < RowValues.size(); ++i) {
			aux = RowValues.get(i).trim().toLowerCase();

			if (aux.equals("acc_id")) {
				accIdCol = i;
				Captions.add(aux);
				continue;
			}

			Matcher m = pRate.matcher(aux);
			if (m.find()) { // if found "_rate_"

				Captions.add(m.group(1) + "_rate");
				found = null;
				m = pAvg.matcher(aux); // keep it separated for easier applying changes 
				if (m.find()) {        // if it's avg
					found = m.group(2);
				} else { // has to be something like "_day001-day002"
					m = pDay.matcher(aux);
					if (m.find()) {
						found = m.group(3) + "-" + m.group(5);
					}
				}
				if (found != null) {
					if (!indexMap2.containsKey(found)) {
						indexMap2.put(found, new ArrayList<>());
					}
					indexMap2.get(found).add(i);
				}
			} else { // no rate
				found = null;
				m = pDay2.matcher(aux); // single timestep
				if (m.find()) {
					found = m.group(3);
					Captions.add(m.group(1));
				} else { // single timestep avg
					m = pAvg.matcher(aux);
					if (m.find()) {
						Captions.add((m.group(1).substring(0, m.group(1).length() - 1)));
						found = m.group(2);
					}
				}
				if (found != null) { // just writing "001"
					if (!indexMap.containsKey(found)) {
						indexMap.put(found, new ArrayList<>());
					}
					indexMap.get(found).add(i);
				}
			}
		}
	}
		
	public final void ReadInData(ExpTable Exp1, DB dataBase) throws IOException {
		
		ArrayList <AccPhenTable> acc2addList = new ArrayList<>();		// List <Acc_PhenTable> to store phenotypes
		ArrayList <AccRatesTable> rates2addList = new ArrayList<>();	// List <Acc_RatesTable> to store rates
		
		getfirstline();

		if (need2translate != 0)
			Captions = dataBase.translateCaptionsAcc(Captions);

		Reflection_Cls_Setter<AccPhenTable> rcs_phenTable=new Reflection_Cls_Setter<>();
		Reflection_Cls_Setter<AccRatesTable> rcs_ratesTable=new Reflection_Cls_Setter<>();
			
		while ((firstline = br.readLine()) != null) { 
			
			RowValues.clear();
			Collections.addAll(RowValues, firstline.split(seperator));		
			
			if (RowValues.isEmpty()) break; //16.08 for (manually) misformated inputs
		
			//accessions.add(RowValues.get(accIdCol));		 //3012								
			// writing Acc_PhenTable "for all days + avg
			for (Map.Entry<String,List<Integer>> mentry:indexMap.entrySet()) {
				
				String k=mentry.getKey();
				List<Integer> idxLst=mentry.getValue();
				ArrayList<Double> AccValues = new ArrayList<>();
				ArrayList<String> singleCaps = new ArrayList<>();
				
//				nIndivRead = false;
				
				for(Integer index:idxLst){
					
					// 0710 just store one nIndiv - kept in cause they DO can vary!!
//					if (Captions.get(index).contains("nindiv")) {
//						if (!nIndivRead) {
//							Double w0 = Double.parseDouble(RowValues.get(index));
//							
//							///
//							//singleCaps.add(Captions.get(index));	
//							//AccValues.add(w0);
//							///
//							
//							if (!w0.isNaN()) {
//								singleCaps.add("nindiv");
//								AccValues.add(w0);
//								nIndivRead = true;
//								//continue;
//							}
//						}
//					}
					
					singleCaps.add(Captions.get(index));					
					
					try {
						AccValues.add(Double.parseDouble(RowValues.get(index)));
					} catch (NumberFormatException e){
						AccValues.add(Double.NaN);
					} catch (IllegalArgumentException e) {
						// added for ACC_ID col !!!
					}
				}
								 
				try {
					AccPhenTable aux = new AccPhenTable();
					rcs_phenTable.setFields(aux,AccValues,singleCaps);	// assigning values(AccValues) with captions (singleCaps) to aux

					aux.setExpTable(Exp1);
					aux.accId = RowValues.get(accIdCol).replaceAll("^0+", "");
					aux.timestep = k;										// assigning timestep k (= Map key)
					acc2addList.add(aux);
					
				} catch (Exception ex) {
					Main.StackTraceToString(ex);
				}
			}
			
			// writing Acc_Rates_Table
			for (Map.Entry<String,List<Integer>> mentry:indexMap2.entrySet()) {
			
				String k=mentry.getKey();
				List<Integer> idxLst=mentry.getValue();
				ArrayList<Double> AccValues = new ArrayList<>();
				ArrayList<String> singleCaps = new ArrayList<>();

				for(Integer index:idxLst){
					try {
						AccValues.add(Double.parseDouble(RowValues.get(index)));
					} catch (NumberFormatException e){
						AccValues.add(Double.NaN);
					} catch (IllegalArgumentException e) {
						// added for ACC_ID col !!!
					}
					singleCaps.add(Captions.get(index));
				}
				try {
					AccRatesTable aux = new AccRatesTable();
					rcs_ratesTable.setFields(aux,AccValues,singleCaps);

					aux.setExpTable(Exp1);
					aux.ACC_ID = RowValues.get(accIdCol).replaceAll("^0+", "");
					aux.timestep = k;
					rates2addList.add(aux);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
	}

	dataBase.setExpBratVersion(Exp1, bratVersion);
	dataBase.addPhenTable(acc2addList);
	dataBase.addAccRatesTable(rates2addList);
	
	//System.out.println("time needed to read " + filename + ": " + (System.currentTimeMillis() - start_time) + " ms");
	//logger.info("time needed to read " + filename + ": " + (System.currentTimeMillis() - start_time) + " ms");
    this.br.close();
	}
}
