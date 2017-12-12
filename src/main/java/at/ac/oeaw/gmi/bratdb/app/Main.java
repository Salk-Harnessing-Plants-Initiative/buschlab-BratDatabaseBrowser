package at.ac.oeaw.gmi.bratdb.app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.logging.Logger;

import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.DB;

public class Main {
		
	public static final DB database = new DB();
	public final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public static void main (String[] args) throws Exception{

		if(args.length!=0){
			System.out.println("creating new database from file " + args[0]);
			
			try {
				database.connectDB(null);
			} catch (SQLException e) {
				System.out.println("SQL error - exit");
				System.exit(1);
			}
			
			database.addExperimentsFromFile(args[0]);
			database.addCooForAllExps();
			System.exit(0);
			
		} else
			InitGUI.mainApp();
	}

	public static String StackTraceToString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}
}

