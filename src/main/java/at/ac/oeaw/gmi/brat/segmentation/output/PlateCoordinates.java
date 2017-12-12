package at.ac.oeaw.gmi.brat.segmentation.output;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Shape;
import java.awt.Point;

public class PlateCoordinates implements Serializable{
	/**
	 * 	plateCoords.plantCoordinates.put(plantID,new ArrayList<Object>());
	 *	plateCoords.plantCoordinates.get(plantID).add(shootPixels); // shootPixels instanceof List<Point>()
	 *	plateCoords.plantCoordinates.get(plantID).add(rootPixels);  // rootPixels instanceof List<Point>()
	 *  plateCoords.plantCoordinates.get(plantID).add(plant.getRootMainPathPoints(time)); plant.getRootMainPathPoints(time) instanceof List<Point>
    *
	 */
	private static final long serialVersionUID = -2826357954582508727L;
	public double rotation;
	public double scalefactor;
	public Point refPt;
	public Shape plateShape;
	
	//public final Map<String,List<Object>> plantCoordinates=new HashMap<String,List<Object>>();
	public final Map<String, List<List<Point>>> plantCoordinates= new HashMap<>();
}

