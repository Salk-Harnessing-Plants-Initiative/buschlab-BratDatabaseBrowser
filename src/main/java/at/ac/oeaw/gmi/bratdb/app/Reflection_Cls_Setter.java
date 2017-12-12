package at.ac.oeaw.gmi.bratdb.app;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class Reflection_Cls_Setter<T> {
	@SuppressWarnings("unchecked")
	public void setFields(T myClass, List<Double> values, ArrayList<String> caps) {
		Class<T> cls=(Class<T>) myClass.getClass();
		Field fld;
		for(int i=0;i<caps.size();++i){
			String cap=caps.get(i);   // VARIABLE NAMES
			
			if (!cap.equals("not_present")) { // quick & dirty for version check
				try {
					fld = cls.getDeclaredField(cap);					
					fld.set(myClass, values.get(i)); // setDouble and getDouble just for primitives !!!
				} catch (NoSuchFieldException e) {
					//workaround for one intermediate output version
					if (cap.contains("root_direction_index")) {
						cap = cap.replace("root_direction_index","root_directional_equivalent");
						try {
							fld = cls.getDeclaredField(cap);
							try {
								//fld.setDouble(myClass,values.get(i));
								fld.set(myClass,values.get(i));
							} catch (IllegalArgumentException | IllegalAccessException e1) {
								e1.printStackTrace();
							}
							//System.out.println("replaced: root_direction_index => root_directional_equivalent");
							
						} catch (NoSuchFieldException e1) {
							e1.printStackTrace();
						}
					}
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e ) {
					e.printStackTrace();
				}
			} 
		}
	}
}