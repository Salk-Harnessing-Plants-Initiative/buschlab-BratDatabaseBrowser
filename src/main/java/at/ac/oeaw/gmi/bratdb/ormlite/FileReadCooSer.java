package at.ac.oeaw.gmi.bratdb.ormlite;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import at.ac.oeaw.gmi.brat.segmentation.output.PlateCoordinates;
import at.ac.oeaw.gmi.bratdb.app.Main;

class FileReadCooSer {
	
	@SuppressWarnings("unchecked")
	Map<Integer, List<Integer>> readIn(String filePath) {

		Map<Integer,List<Integer>> indexMap = new HashMap<>();

		try (InputStream fileIn = new FileInputStream(filePath);
			 InputStream buffer = new BufferedInputStream(fileIn);
			 ObjectInput in = new ObjectInputStream(buffer)
			) {

			PlateCoordinates coo = (PlateCoordinates) in.readObject();
			Integer RoiId, aux;

			for (Map.Entry<String,List<List<Point>>> entry: coo.plantCoordinates.entrySet()) {
				RoiId = Integer.parseInt(entry.getKey());

				if (!indexMap.containsKey(RoiId))
					indexMap.put(RoiId,new ArrayList<>(Arrays.asList(-10,10000,-10,10000))); // dummy init

				// index 0 contains shoot pixels
				// index 1 contains root pixels
				for (int i = 0; i < 2; ++i) {
					if (entry.getValue().get(i) != null) {	// for safety check in shoot and root
						for (Point onePoint : entry.getValue().get(i)) {
							aux = onePoint.x;
							if (aux != 0) { // excluding brat bug showing impossible 0 values
								if (aux > indexMap.get(RoiId).get(0)) { // x max
									indexMap.get(RoiId).set(0, aux);
								} else if (aux < indexMap.get(RoiId).get(1)) { // x min
									indexMap.get(RoiId).set(1, aux);
								}
							}
							aux = onePoint.y; // y coordinate
							if (aux != 0) {  // excluding brat bug showing impossible 0 values
								if (aux > indexMap.get(RoiId).get(2)) { // y max
									indexMap.get(RoiId).set(2, aux);
								} else if (aux < indexMap.get(RoiId).get(3)) { // y min
									indexMap.get(RoiId).set(3, aux);
								}
							}
						}
					}
				}
			}
		} catch (IOException | ClassNotFoundException ex) {
			Main.StackTraceToString(ex);
		}
		return indexMap;
	}

	Boolean reverse(String filePath) {
		PlateCoordinates coo;
		//Integer RoiId, aux;

		//Map<Integer,List<Integer>> indexMap = new HashMap<>();

		try (FileInputStream fileIn = new FileInputStream(filePath);
			 ObjectInputStream in = new ObjectInputStream(fileIn)) {

			coo = (PlateCoordinates) in.readObject();

			return coo.refPt != null;

			//Point2D refPt = coo.refPt;

//			for (Map.Entry<String,List<Object>> entry: coo.plantCoordinates.entrySet()) {
//
//				RoiId = Integer.parseInt(entry.getKey());
//
//				if (!indexMap.containsKey(RoiId))
//					indexMap.put(RoiId,new ArrayList<>(Arrays.asList(0,1000,0,1000))); // dummy init
//
//				for (Object oList : entry.getValue()) {
//					if (oList != null ) {
//						for (Point onePoint : (ArrayList<Point>) oList) {
//							aux = onePoint.x;
//							if (aux != 0) { // excluding brat bug showing impossible 0 values
//								if (aux > indexMap.get(RoiId).get(0)) { // x max
//									indexMap.get(RoiId).set(0, aux);
//								} else if (aux < indexMap.get(RoiId).get(1)) { // x min
//									indexMap.get(RoiId).set(1, aux);
//								}
//							}
//							aux = onePoint.y; // y coordinate
//							if (aux != 0) {  // excluding brat bug showing impossible 0 values
//								if (aux > indexMap.get(RoiId).get(2)) { // y max
//									indexMap.get(RoiId).set(2, aux);
//								} else if (aux < indexMap.get(RoiId).get(3)) { // y min
//									indexMap.get(RoiId).set(3, aux);
//								}
//							}
//						}
//					}
//				}
//			}
		} catch (IOException | ClassNotFoundException ex) {
			Main.StackTraceToString(ex);
		}
		return true;
	}
}