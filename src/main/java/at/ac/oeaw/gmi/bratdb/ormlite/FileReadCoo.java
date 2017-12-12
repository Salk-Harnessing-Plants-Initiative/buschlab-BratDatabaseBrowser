package at.ac.oeaw.gmi.bratdb.ormlite;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class FileReadCoo {

	private final Map<Integer,List<Integer>> indexMap = new HashMap<>();
	private FileReader fr;
	private final BufferedReader br;
	
	FileReadCoo(String filename) {
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			//logger.error("input file " + filename + " not found");
		}
		br = new BufferedReader(fr);
	}
	
	Map<Integer, List<Integer>> readInCoo() {

		// Object_Coordinates_...txt structure:
		// some header line
		// roi Id /t x coo /t /y coo /t ? /t type
		//
		// types: 
		// 99    crop region
		// 0      general skeleton
		// 1      primary root
		// 2      start point
		// 3      end point
		// 4      shoot centre of mass
		// 5      plant outline
		// 6      shoot outline
		//
		
		String firstline, seperator = "\t";
		Integer RoiId, aux, plantOutline = 5;

		// for safety reasons don't rely on ordering
		try {
			br.readLine(); // skipping first line showing captions
			while ((firstline = br.readLine()) != null) { 
				RoiId = Integer.parseInt(firstline.split(seperator)[0]);

				if (!indexMap.containsKey(RoiId))
					indexMap.put(RoiId,new ArrayList<>(Arrays.asList(0,10000,0,10000))); // dummy init
			
				if (Integer.parseInt(firstline.split(seperator)[4]) == plantOutline) { // just if it's shootOutline
					aux = (int) Double.parseDouble(firstline.split(seperator)[1]);  // x coordinate							
					if (aux != 0) { // excluding brat bug showing impossible 0 values
						if (aux > indexMap.get(RoiId).get(0)) { // x max
							indexMap.get(RoiId).set(0, aux);
						} else if (aux < indexMap.get(RoiId).get(1)) { // x min
							indexMap.get(RoiId).set(1, aux);
						}
					}
					aux = (int) Double.parseDouble(firstline.split(seperator)[2]); // y coordinate					
					if (aux != 0) {  // excluding brat bug showing impossible 0 values
						if (aux > indexMap.get(RoiId).get(2)) { // x max
							indexMap.get(RoiId).set(2, aux);
						} else if (aux < indexMap.get(RoiId).get(3)) { // x min
							indexMap.get(RoiId).set(3, aux);
						}
					}
				}
			}
			this.br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return indexMap;
	}
}