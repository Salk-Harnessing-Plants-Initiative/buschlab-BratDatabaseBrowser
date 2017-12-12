package at.ac.oeaw.gmi.bratdb.ormlite;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.ac.oeaw.gmi.bratdb.app.Main;

class FileReadMeta {
	
	private BufferedReader br;

	FileReadMeta(String Fname) throws FileNotFoundException {
		br = new BufferedReader(new FileReader(Fname));
	} 
	
	final void ReadInData(ExpTable exp, DB dataBase) {
		
		String firstline;
		String aux, aux1;
		Integer numberOfSets = 0;
		Integer paraIndex = 0;
		Integer headerCount = 0;
		Boolean headerPresent = false;
		Boolean nameMissing = false;
		ArrayList<Integer> setIndices = new ArrayList<>();
		ArrayList<String> metaFields = Main.database.getTableCaptions(SetTable.class);
		ArrayList<String> readProps = new ArrayList<>();
		
		Map<Integer,ArrayList<String>> setMap = new LinkedHashMap<>(); //LinkedHashMap to keep order!

		try {			
			while ((firstline = br.readLine()) != null) { 
				
				if(firstline.replace(",", "").length() == 0) continue; //skipping empty rows
				
				List<String> tokensList = new ArrayList<>();
				boolean inQuotes = false;
				StringBuilder b = new StringBuilder();
				
				if (firstline.contains("Responsible person")) {
					headerPresent = true;
					nameMissing = true;
				}
				
				for (char c : firstline.toCharArray()) {
				    switch (c) {
				    case ',':
				        if (inQuotes) {
				            b.append(c);
				        } else {
				            tokensList.add(b.toString());
				            b = new StringBuilder();
				        }
				        break;
				    case '\uFFFD': 			// if unreadable
				    	 b.append("\u00b0");  // just guessing it's a degree symbol...
				    	 break;				        
				    case '\"':
				        inQuotes = !inQuotes;
				    case ':':
				    	if (nameMissing) {
				    		tokensList.add(b.toString());
				    		b = new StringBuilder();
				    		break;
				    	} 			            
				    default:
				        b.append(c);
				    break;
				    }
				}
				tokensList.add(b.toString());
					
				if (headerPresent) {															
					if (tokensList.contains("Responsible person")){
						for (int k = 1; k < tokensList.size();++k) {
							if (!tokensList.get(k).isEmpty()) {
								exp.author = tokensList.get(k).trim(); // trim leading whitespaces 1605
								nameMissing = false;
								break;
							}
						}
					} else 
					{
						for (String aTokensList : tokensList) {
							if (!aTokensList.isEmpty()) {
								exp.desc += "\n" + aTokensList;
							}
						}
					}
					headerCount += 1;
					if (headerCount == 5) headerPresent = false;	
					continue;
				}
				
				if (paraIndex == 0) {
					for (int i = 0; i< tokensList.size(); ++i) {
						 if (tokensList.get(i).equals("Parameters")) {
							 paraIndex = i;
							 break;
						 }
					}																
				}
				
				// of case that caption "Parameters" is not provided
				if (paraIndex == 0) {
					for (int i = 0; i< tokensList.size(); ++i) {
						 if (tokensList.get(i).equals("Batch")) {
							 paraIndex = i;
							 break;
						 }
					}	
				}
				
				if (numberOfSets == 0) {
					if (tokensList.get(paraIndex).equals("Batch")) {														
						for (int i = paraIndex+1; i<tokensList.size(); ++i) { // start one col right of parameter column
							if (!tokensList.get(i).isEmpty()) {								
								setIndices.add(i);
							}
						}
						numberOfSets = setIndices.size();
						for (int i = 0; i< numberOfSets; ++i) {
							setMap.put(i,new ArrayList<>());
						}
					}					
				}
										
				aux = tokensList.get(paraIndex).toLowerCase().trim();
				
				if (!aux.isEmpty()) {
					aux = aux.replace(" ", "_").replace("/", "");
					
					if (aux.equals("ph"))
						aux = "pH";

					if (metaFields.contains(aux)) {
						readProps.add(aux);
						for (int i = 0; i< numberOfSets; ++i) {
							aux1 = tokensList.get(setIndices.get(i));								
							setMap.get(i).add(aux1);																																								
						}	
					} else {
						aux = checkMetaField(aux, metaFields);
						if (aux != null) {												
							readProps.add(aux);
							for (int i = 0; i< numberOfSets; ++i) {
								aux1 = tokensList.get(setIndices.get(i));								
								setMap.get(i).add(aux1);																																								
							}	
						}
					}

					// stuff that's not in db schema
				} else if (!tokensList.get(paraIndex -1).isEmpty())  {
					if (tokensList.get(paraIndex -1).equals("purpose of experiment")) {
						readProps.add("purpose_of_experiment");
						for (int i = 0; i< numberOfSets; i++) {					
							aux1 = tokensList.get(setIndices.get(i));								
							setMap.get(i).add(aux1);																																	
						}	
					}
					if (tokensList.get(paraIndex -1).equals("who")) {	
						if (exp.author == null) exp.author = tokensList.get(setIndices.get(1));						
					}
					if (tokensList.get(paraIndex -1).equals("where to find more details")) {						
						//if(!tokensList.get(setIndices.get(1)).isEmpty()) exp.defPath = tokensList.get(setIndices.get(1));
						//
						if(exp.defPath == null) exp.defPath = tokensList.get(setIndices.get(0));
					}					
				}
			}
			this.br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = 0;i< numberOfSets;++i) {
			Main.database.addMetaToExp(exp, setMap.get(i), readProps);
		}		
		Main.database.updateExpTable(exp);
	}
	
	private String checkMetaField(String in, ArrayList<String> inArray) {
		for (String anInArray : inArray) {
			if (in.contains(anInArray)) {
				return anInArray;
			}
		}
		return null;
	}
}