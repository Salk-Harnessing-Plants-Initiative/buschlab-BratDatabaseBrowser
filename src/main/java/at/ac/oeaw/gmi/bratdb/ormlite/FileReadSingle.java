package at.ac.oeaw.gmi.bratdb.ormlite;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.app.Reflection_Cls_Setter;

public class FileReadSingle {

	private String firstline = null;
	private final String comp2 = "\t";
	private Integer accIdCol;
	private ArrayList<String> Captions = new ArrayList<>();
	private final ArrayList<String> RowValues = new ArrayList<>();
	private final Map<String,List<Integer>> indexMap = new LinkedHashMap<>(); //LinkedHashMap to keeps order!
	private final Map<String,List<Integer>> indexMap2 = new LinkedHashMap<>();
	
	private BufferedReader br, br2;
	
	private boolean need2translate = false;
	
	public FileReadSingle(String filename_single, String filename_quant) {
		try {
			br = new BufferedReader(new FileReader(filename_single));
			br2 = new BufferedReader(new FileReader(filename_quant));
		} catch (FileNotFoundException ex) {
			Main.logger.severe("input files not found - in FileReadSingle...");
			Main.StackTraceToString(ex);
		}
	}
		
	private void getfirstline() {
		try {
			firstline = this.br.readLine(); //stop here
			if (firstline.contains("GR_Root_day_")) {
				Main.logger.info("reading in brat output v0");
				need2translate = true; // trans flag
				getfirstline_v0(firstline);
			} else {
				Main.logger.info("reading in brat output v1");
				getfirstline_v1(firstline);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void getfirstline_v0(String firstline)  {
		int i;
		String pattern = "(.*)(_day_)(\\d+)(.*)"; //pattern to find "day" entries
		String pattern2 = "(.*)(_avg)(.*)";		  //pattern to find "avg" entries
		String found, aux;
					    	    
		Collections.addAll(RowValues, firstline.split(comp2));
		Pattern r = Pattern.compile(pattern);   // search for "_day_"
		Pattern r2 = Pattern.compile(pattern2); // search for "_avg"
		
		for (i=0;i<RowValues.size();i++) {
			aux = RowValues.get(i).trim().toLowerCase().replace("/","_");
			Matcher m = r.matcher(aux);
		
			if (m.find()) { // if found "_day_"											
				if (m.group(4).isEmpty()) { // just single day values e.g. day 1,but not rate 1-2
					
					found = m.group(3);     // daytoken e.g. "1", "2",...
					if (!indexMap.containsKey(found)) {
						indexMap.put(found,new ArrayList<>());
						// manually adding cols for set, plate and abs_pos
						for (int c=1;c<4;c++) indexMap.get(found).add(c);													
					}
					indexMap.get(found).add(i);
				} 
				else {   // else its a rate
					found = m.group(3) + m.group(4);
					if (!indexMap2.containsKey(found)) {
						indexMap2.put(found,new ArrayList<>());
					}
					indexMap2.get(found).add(i);
				}
				Captions.add(m.group(1));
			}
			else { // else find "_avg"
				Matcher m2 = r2.matcher(aux);		
				
				if (m2.find()) { 					
					found = m2.group(2);
					if (! m2.group(1).contains("gr_")) { 

						if (! m2.group(3).isEmpty()) //logger.warn("Read in Error!!!");  
						found = found.replace("_", ""); 
						if (!indexMap.containsKey(found)) {
							indexMap.put(found,new ArrayList<>());
							
							// manually adding cols for set, plate and abs_pos
							for (int c=1;c<4;c++) indexMap.get(found).add(c);
						}
						indexMap.get(found).add(i);
					} else {

						if (!m2.group(3).isEmpty()) //logger.warn("Read in Error!!!");  
						found = found.replace("_", ""); 
						if (!indexMap2.containsKey(found)) {
							indexMap2.put(found,new ArrayList<>());
						}
						indexMap2.get(found).add(i);
					}
					
					Captions.add(m2.group(1));
					
				} else {
					Captions.add(aux);
					if(RowValues.get(i).toLowerCase().equals("acc_id")) {
						accIdCol = i;
					}
				}
			}
		}
		/// BRAT output bug workaround
		for (int u=0;u<Captions.size();u++) {
			if (Captions.get(u).equals("sdx_sdyperpix")) {
				Captions.set(u, "sdx_sdy_per_pix");
				break;
			}
		}			
	}
	
	private void getfirstline_v1(String firstline) {
		int i;
		int crapCounter = 0; // 3112 increase counter for manually added columns
		String patternRate = "(.*)(_rate_)(.*)"; 	  // pattern to find "_rate_" 
		String patternAvg = "(.*)(avg)(.*)";		  // pattern to find "_avg" entries
		String patternDay = "(.*)(_day)(\\d+)(-day)(\\d+)(.*)";  // pattern to find "_day00X-day00Y" entries
		String patternDay2 = "(.*)(_day)(\\d+)(.*)";		
		String found, aux;
				
		Collections.addAll(RowValues, firstline.split(comp2));
		Pattern pRate = Pattern.compile(patternRate);   // search for "_rate"
		Pattern pAvg = Pattern.compile(patternAvg); // search for "_avg"
		Pattern pDay = Pattern.compile(patternDay);
		Pattern pDay2 = Pattern.compile(patternDay2);

		for (i=0;i<RowValues.size();i++) {
			aux = RowValues.get(i).trim().toLowerCase();
			
			if(aux.equals("acc_id")) {
				accIdCol = i;
				Captions.add(aux);
				continue;
			}
			
			Matcher m = pRate.matcher(aux);
			if (m.find()) { // if found "_rate_"
				
				Captions.add(m.group(1)+"_rate");
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
				
				if(found != null) {
					if (!indexMap2.containsKey(found)) {
						indexMap2.put(found,new ArrayList<>());
					}
					indexMap2.get(found).add(i);	
				} else {
					Main.logger.severe("readin error at acc " + aux);
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
						Captions.add((m.group(1).substring(0, m.group(1).length()-1)));
						found = m.group(2);
					}
				}
				if(found != null) { // just writing "001"
					if (!indexMap.containsKey(found)) {
						indexMap.put(found,new ArrayList<>());
						// manually adding cols for set, plate and abs_pos
						for (int c=1+crapCounter;c<4+crapCounter;c++) indexMap.get(found).add(c);
					}
					indexMap.get(found).add(i);	
				} else if(aux.contentEquals("set") || aux.contentEquals("plate") || aux.contentEquals("abs_pos")) {
					Captions.add(aux);
				} else {
					crapCounter++;
					Main.logger.info("read unknown caption " + aux + " => increasing trash counter to " + crapCounter);
					Captions.add(aux); // add trash ALSO to Captions otherwise mismatch in sizes!!! 3112 
				}
			}
		}		
	}
	
	public final void ReadInData(ExpTable experiment, DB dataBase) {
		
		ArrayList <String> accessions = new ArrayList<>();
		ArrayList <String> ratesAux = new ArrayList<>();						// List <String> to store acc names/ IDs at corr. indices for rates
		ArrayList <SinglePhenTable> single2addList = new ArrayList<>();		// List <Acc_PhenTable> to store phenotypes
		ArrayList <SingleRatesTable> rates2addList = new ArrayList<>();	// List <Acc_RatesTable> to store rates
		String withoutLeadingZeros;				
		
		getfirstline();
		
		if (need2translate) {
			Captions = dataBase.translateCaptionsSingles(Captions);	
		}
		
		Reflection_Cls_Setter<SinglePhenTable> rcs_phenTable=new Reflection_Cls_Setter<>();
		Reflection_Cls_Setter<SingleRatesTable> rcs_ratesTable=new Reflection_Cls_Setter<>();
		
		try {
			while ((firstline = br.readLine()) != null) { 
							
				RowValues.clear();
				Collections.addAll(RowValues, firstline.split(comp2));
									
				// writing Single_PhenTable "for all days 
				for (Map.Entry<String,List<Integer>> mentry:indexMap.entrySet()) {
					
					String k=mentry.getKey();
					if (k.equals("_avg")) k = "avg";
					
					List<Integer> idxLst=mentry.getValue();
					ArrayList<Double> singleValues = new ArrayList<>();
					ArrayList<String> caps = new ArrayList<>();
					
					try {
						for(Integer index:idxLst){
							try {
								singleValues.add(Double.parseDouble(RowValues.get(index)));
							} catch (NumberFormatException e){
								singleValues.add(Double.NaN);
							}
							caps.add(Captions.get(index));
						}
					} catch (IndexOutOfBoundsException e) {
						Main.logger.warning("out of bounds at: " + k + " after " + caps.get(caps.size()-1));
					}
					 
					try {
						SinglePhenTable aux = new SinglePhenTable();			// new entry "aux" in Single_PhenTable
						rcs_phenTable.setFields(aux,singleValues,caps);			// assigning values(AccValues) with captions (singleCaps) to aux
						aux.timestep = k;										// assigning timestep k (= Map key)
						single2addList.add(aux);								// place new Acc_PhenTable in List to add
												
						withoutLeadingZeros = RowValues.get(accIdCol);
						withoutLeadingZeros = withoutLeadingZeros.replaceAll("^0+", "");
						
						accessions.add(withoutLeadingZeros);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				// writing Single_Rates_Table
				for (Map.Entry<String,List<Integer>> mentry:indexMap2.entrySet()) {
				
					String k=mentry.getKey();
					
					if (k.equals("_avg")) k = "avg";
					
					List<Integer> idxLst=mentry.getValue();
					ArrayList<Double> singleValues = new ArrayList<>();
					ArrayList<String> singleCaps = new ArrayList<>();

					for(Integer index:idxLst){
						try {
							singleValues.add(Double.parseDouble(RowValues.get(index)));
						} catch (NumberFormatException e){
							singleValues.add(Double.NaN);
						}
						singleCaps.add(Captions.get(index));
					}
					try {
						SingleRatesTable aux = new SingleRatesTable();
						rcs_ratesTable.setFields(aux,singleValues,singleCaps);
						
						aux.timestep = k;
						rates2addList.add(aux);		
						
						withoutLeadingZeros = RowValues.get(accIdCol).replaceAll("^0+", "");
						ratesAux.add(withoutLeadingZeros);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}
			this.br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	// reading in quant_images file
	ArrayList<String> search = new ArrayList<>(Arrays.asList("acc_id","abs_pos","day","core_filename","roi"));
	ArrayList<Integer> search_idx = new ArrayList<>(5);

	try {
		firstline = br2.readLine();
	} catch (IOException e) {
		e.printStackTrace();
	}
	RowValues.clear();
	
	Collections.addAll(RowValues, firstline.split(comp2));
	
	for (int i1 = 0; i1<search.size();i1++) {
		for (int i2 = 0; i2 < RowValues.size();i2++) {
			if (search.get(i1).equals(RowValues.get(i2).toLowerCase())) {
				search_idx.add(i1, i2);
				break;
			}
		}
	}
	
	try {
		while ((firstline = br2.readLine()) != null) {
			String acc, strDay;
			Double abs_pos, day_col;			
			RowValues.clear();
			Collections.addAll(RowValues, firstline.split(comp2));
			
			acc = RowValues.get(search_idx.get(0)).replaceAll("^0+", ""); // 1511!!!
			abs_pos = Double.parseDouble(RowValues.get(search_idx.get(1)));
			
			if (need2translate) {
				strDay = RowValues.get(search_idx.get(2));
			} else { // if current version convert timestep in quant file "1" => "001" 
				day_col = Double.parseDouble(RowValues.get(search_idx.get(2)));	
				strDay = String.format("%03.0f", day_col);
			}
			
			for (int k=0;k<accessions.size();k++) {//
				if (accessions.get(k).equals(acc)) {
					if (single2addList.get(k).abs_pos.equals(abs_pos)){ // == abs_pos) {					
						if (single2addList.get(k).timestep.equals(strDay)) {		
							single2addList.get(k).core_filename = RowValues.get(search_idx.get(3));
							single2addList.get(k).roi = Integer.parseInt(RowValues.get(search_idx.get(4)));
							break;
						}
					}
				}
			}	
		}
		this.br2.close();
	} catch (NumberFormatException | IOException e) {
		e.printStackTrace();
	}

	dataBase.addSingleTable(single2addList,accessions, experiment);
	dataBase.addSingleRatesTable(rates2addList,ratesAux, experiment);				
	}
}

