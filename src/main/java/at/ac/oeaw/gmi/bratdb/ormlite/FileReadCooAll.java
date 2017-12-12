package at.ac.oeaw.gmi.bratdb.ormlite;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class FileReadCooAll {

	private FileReader fr;
	private final BufferedReader br;
	
	FileReadCooAll(String filename) {
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			System.out.println("input file " + filename + " not found");
		}
		br = new BufferedReader(fr);
	} //def. constructor
	
	Map <Integer, RoiCoo> readInCoo() {
		
		Map <Integer, RoiCoo> readIn = new HashMap<>();
		List<Integer> referTo;
		String seperator, aux, firstline;
		Integer roiId, type;
		
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

		seperator = "\t";
//		genSkel = 0;
//		primRoot = 1;
//		startP = 2;
//		endP = 3;
//		cM = 4;
//		plantOutline = 5; 
//		shootOutline = 6;
		
		String values[]; 

		// for safety reasons don't rely on ordering
		try {
			br.readLine(); // skipping first line showing captions
			while ((firstline = br.readLine()) != null) {
				values = firstline.split(seperator);
				roiId = Integer.parseInt(values[0]);
				
				if (!readIn.containsKey(roiId)) {
					RoiCoo newEntry = new RoiCoo();
					newEntry.roiId = roiId;
					readIn.put(roiId, newEntry);
				}

				type = Integer.parseInt(values[4]);
				RoiCoo coo = readIn.get(roiId);
				
				switch(type) {
					case 0: //general skeleton
						break;
					case 1: //primary root
						referTo = coo.pRootCoo.get("x");
						referTo.add(Integer.parseInt(values[1]));
						
						referTo = coo.pRootCoo.get("y");
						referTo.add(Integer.parseInt(values[2]));
						
						referTo = coo.pRootCoo.get("distanceMap");
						referTo.add(Integer.parseInt(values[3]));
						break;
					case 2: //start point
						coo.startX = Integer.parseInt(values[1]);
						coo.startY = Integer.parseInt(values[2]);
						break;
					case 3: //end point
						coo.endX = Integer.parseInt(values[1]);
						coo.endY = Integer.parseInt(values[2]);
						break;
					case 4: //shoot centre of mass
						coo.shootMcX = Integer.parseInt(values[1]);
						coo.shootMcY = Integer.parseInt(values[2]);
						break;
					case 5: //plant outline
						// sometimes formated as doubles
						referTo = coo.pOutLineCoo.get("x");
						aux = values[1].substring(0, values[1].indexOf('.'));
						referTo.add(Integer.parseInt(aux));
						
						referTo = coo.pOutLineCoo.get("y");
						aux = values[2].substring(0, values[2].indexOf('.'));
						referTo.add(Integer.parseInt(aux));
						
						referTo = coo.pOutLineCoo.get("distanceMap");
						referTo.add(Integer.parseInt(values[3]));
						break;
					case 6: //shoot outline
						// sometimes formated as doubles
						referTo = coo.sOutLineCoo.get("x");
						aux = values[1].substring(0, values[1].indexOf('.'));
						referTo.add(Integer.parseInt(aux));
						
						referTo = coo.sOutLineCoo.get("y");
						aux = values[2].substring(0, values[2].indexOf('.'));
						referTo.add(Integer.parseInt(aux));
						
						referTo = coo.sOutLineCoo.get("distanceMap");
						referTo.add(Integer.parseInt(values[3]));
						break;
				}
			}
			this.br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
//		for (Map.Entry<Integer,RoiCoo> entry : readIn.entrySet()) {
//			System.out.println("read roiID: " + entry.getKey());
//		}
		return readIn;
	}
}

class RoiCoo {
	
	Integer roiId;
	Integer startX, startY, endX, endY, shootMcX, shootMcY;
	
	final Map<String,ArrayList<Integer>> pRootCoo = new LinkedHashMap<>();
	final Map<String,ArrayList<Integer>> pOutLineCoo = new LinkedHashMap<>();
	final Map<String,ArrayList<Integer>> sOutLineCoo = new LinkedHashMap<>();
	
	RoiCoo() {
		pRootCoo.put("x", new ArrayList<>());
		pRootCoo.put("y", new ArrayList<>());
		pRootCoo.put("distanceMap", new ArrayList<>());
		
		pOutLineCoo.put("x", new ArrayList<>());
		pOutLineCoo.put("y", new ArrayList<>());
		pOutLineCoo.put("distanceMap", new ArrayList<>());
		
		sOutLineCoo.put("x", new ArrayList<>());
		sOutLineCoo.put("y", new ArrayList<>());
		sOutLineCoo.put("distanceMap", new ArrayList<>());
	}	
	
	void addRoiCooToTable(CooTable in) {
		
		ArrayList<Integer> parts = pRootCoo.get("x");
		
		in.nPpixel = parts.size();
		parts.addAll(pRootCoo.get("y"));
		parts.addAll(pRootCoo.get("distanceMap"));
		
		in.pRootCoo = createByteArray(parts);
		
		parts = pOutLineCoo.get("x");
		in.nOpixel = parts.size();
		parts.addAll(pOutLineCoo.get("y"));
		parts.addAll(pOutLineCoo.get("distanceMap"));
		in.pOutlineCoo = createByteArray(parts);
		
		parts = sOutLineCoo.get("x");
		in.nSpixel = parts.size();
		parts.addAll(sOutLineCoo.get("y"));
		parts.addAll(sOutLineCoo.get("distanceMap"));
		in.pShootCoo = createByteArray(parts);
		
//		Integer roiId;
//		Integer startX, startY, endX, endY, shootMcX, shootMcY, bits, nPpixel;
		
		in.roi = this.roiId;
		in.startX = this.startX;
		in.startY = this.startY;
		in.endX = this.endX;
		in.endY = this.endY;
		in.shootMcX = this.shootMcX;
		in.shootMcY = this.shootMcY;
	}

	// the following is Java specific and won't work for C and other languages!!!
//	private byte [] createByteArray(Object obj)
//    {
//        byte [] bArray = null;
//        try
//        {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream objOstream = new ObjectOutputStream(baos);
//            objOstream.writeObject(obj);
//            bArray = baos.toByteArray();
//        }
//        catch (Exception e)
//        {
//        	e.printStackTrace();
//        }
//           
//        return bArray;
//    }
	
	private byte[] createByteArray(ArrayList<Integer> in) {
		byte [] bArray = null;
		ByteArrayOutputStream bAos = new ByteArrayOutputStream();
		DataOutputStream dOut = new DataOutputStream(bAos); // for output to bArray
				
		try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home")+"/Desktop/cooTest.txt", true);
			 DataOutputStream dos = new DataOutputStream(fos)
		) {
			
			// for debugging output
			//FileOutputStream fos = new FileOutputStream(System.getProperty("user.home")+"/Desktop/cooTest.txt", true);
			//DataOutputStream dos = new DataOutputStream(fos);
			
			for (Integer entry : in){
				dOut.writeInt(entry.intValue());  	// for output to bArray
				dos.writeInt(entry.intValue());		// for debugging output
			}
			
			//dOut.flush();
			//dos.flush();
			
			 // debugging 
//			FileInputStream is = new FileInputStream("/Users/alexander.bindeus/Desktop/cooTest.txt");
//			DataInputStream dis = new DataInputStream(is);
//	         
//			while(dis.available()>0)
//			{
//			   int k = dis.readInt();
//			   System.out.print(k+" ");
//			}
			
			bArray = bAos.toByteArray();
			
			//System.out.println("bArray size: " + bArray.length);
			
			bAos.close();
			dOut.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return bArray;
	}
	
//	public void getCooProps(CooTable in) {
//		
//		System.out.println("CooTable form exp " + in.sourceExp + " acc: " + in.sourceAcc + " timestep: " + in.timestep);
//		
//		byte []  bytes = in.pRootCoo;
//		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//		DataInputStream inStream = new DataInputStream(bais);
//		
//		try {
//			while (inStream.available() > 0) {
//			    //String element = inStream.readInt();
//			    System.out.println(inStream.readInt());
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
}
