package at.ac.oeaw.gmi.bratdb.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public final class ConfigCls {
	
	private static final Properties prop = new Properties();
	private static final String filePath = System.getProperty("user.home") + "/.bratDbSettings.ini";
	
	static {
		if (new File(filePath).isFile()) {
			try (InputStream inStream = new FileInputStream(filePath)){
				prop.load(inStream);
				System.out.println("reading settings from file " + filePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try (InputStream inStream = ConfigCls.class.getResourceAsStream("/config.properties")) {
				prop.load(inStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getFilePath() {
		return filePath;
	}
		
		/// fetches from src/main/resources
		//try(InputStream inStream = getClass().getClassLoader().getResourceAsStream("config.properties")){
		/////////////////	
			
		/// fetches from from folder where <class> is
		//try(InputStream inStream = ConfigCls.class.getClassLoader().getResourceAsStream("./config.properties")){
		/////////////////	
		
		/// does not work after export
		//try(InputStream inStream = ConfigCls.class.getClassLoader().getResourceAsStream("config.properties")){
		/////////////////	
	
	public static Properties getMyPropList() {
		return prop;
	}
	
	public static void setMyPropList(Properties inTab) {
		prop.putAll(inTab);
	}
	
	public static void setMyProp(String myProp, String toSet) {
		prop.put(myProp, toSet);
	}
	
	public static String getMyProp(String key){
		return prop.getProperty(key);
	}
	
	//https://stackoverflow.com/questions/35982967/project-throwing-ioexception-file-not-found-when-jar-is-run
	//https://stackoverflow.com/questions/16372374/move-a-text-file-into-target-folder-when-compiling-a-maven-project
	public static void persistProps() {
		System.out.println(filePath);
		
		try (OutputStream outStream = new FileOutputStream(filePath)) {
			prop.store(outStream, "stored BratDB settings");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
