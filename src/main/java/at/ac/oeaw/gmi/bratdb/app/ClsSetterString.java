package at.ac.oeaw.gmi.bratdb.app;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

public class ClsSetterString<T> {
	@SuppressWarnings("unchecked")
	public void setFields(T myClass, List<String> values, ArrayList<String> caps) {
		Class<T> cls=(Class<T>) myClass.getClass();
		for(int i=0;i<caps.size();++i){
			String cap=caps.get(i);
				Field fld;
				try {
					fld = cls.getDeclaredField(cap);
					fld.set(myClass, values.get(i));				
					
				} catch (NoSuchFieldException e) {
						e.printStackTrace();
						System.out.println("field not found: " + values.get(i));
				} catch (SecurityException | IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					System.out.println("can't access field " + caps.get(i) + " with value " + values.get(i));
					e.printStackTrace();
				}
		}
	}
	@SuppressWarnings("unchecked")
	public void setFieldsByAnnotation(T myClass, List<String> values, ArrayList<String> caps) {
		Class<T> cls=(Class<T>) myClass.getClass();
		String str0;
		Field flds[] = cls.getDeclaredFields();
				
		for(int i=0;i<caps.size();++i){
			String cap=caps.get(i);
			for (Field fld : flds)
				try {
					str0 = fld.getAnnotation(Column.class).name();
					if (str0.equals(cap)) {
						try {
							fld.set(myClass, values.get(i));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
						break;
					}
					// i know that
				} catch (Exception e) {
                    //
				}
		}
	}

	public void setFieldsByAnnotationType(T myClass, List<String> values, ArrayList<String> caps) {
		Class<T> cls = (Class<T>) myClass.getClass();
		String str0, aux;
		Field flds[] = cls.getDeclaredFields();

		for (Field fld : flds) {
			if (fld.isAnnotationPresent(Column.class)) {
				str0 = fld.getAnnotation(Column.class).name();
				for (String val : caps) {
					if (val.equalsIgnoreCase(str0)) {
						try {
							aux = fld.getType().getSimpleName();

							switch (aux) {
								case "String":
									fld.set(myClass, values.get(caps.indexOf(val)));
									break;
								case "Integer":
									fld.set(myClass, Integer.parseInt(values.get(caps.indexOf(val))));
									break;
								case "Double":
									fld.set(myClass, Double.parseDouble(values.get(caps.indexOf(val))));
									break;
								case "Boolean":
									fld.set(myClass, Boolean.parseBoolean(values.get(caps.indexOf(val))));
									break;
							}
						} catch (IllegalAccessException | NumberFormatException ex ) {
							Main.logger.warning(str0 + " skipping " + values.get(caps.indexOf(val)));
						} catch (IndexOutOfBoundsException ex) {

							// in case of misformated input no index is found
							// ignore and continue

//							Main.logger.severe(aux +' ' + val);
//							Main.StackTraceToString(ex);
						}
					}
				}
			}
		}
	}
}
