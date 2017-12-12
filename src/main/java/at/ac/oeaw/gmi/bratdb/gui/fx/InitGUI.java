package at.ac.oeaw.gmi.bratdb.gui.fx;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.*;

import at.ac.oeaw.gmi.bratdb.gui.fx.controllers.*;
import at.ac.oeaw.gmi.bratdb.gui.fx.logging.BratDbLogHandler;
import at.ac.oeaw.gmi.bratdb.ormlite.*;
import org.controlsfx.control.MasterDetailPane;
import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.gui.fx.logging.Log;
import at.ac.oeaw.gmi.bratdb.gui.fx.logging.LogView;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static at.ac.oeaw.gmi.bratdb.app.Main.logger;

public class InitGUI extends Application {

    private Stage primaryStage;
    public final MasterDetailPane mdPane = new MasterDetailPane();
    private ExpTable selectedExp;
    
    private ObservableList<ExpTable> experimentTable = FXCollections.observableArrayList();
    private ObservableList<String> accTable = FXCollections.observableArrayList();
    private final ObservableList<String> hunterTable = FXCollections.observableArrayList();

	private final static Log queue = new Log();
	private final static BratDbLogHandler handlerFx = new BratDbLogHandler(queue);

	public static LogView logView;
	
	@Override
    public void start(Stage primaryStage) {

		handlerFx.setLevel(Level.ALL);
		logger.addHandler(handlerFx);

		//Application.setUserAgentStylesheet(STYLESHEET_CASPIAN);
		
		this.primaryStage = primaryStage;
	    this.primaryStage.setTitle("BRAT DataBase v0.3");
	    
	    logView = new LogView(queue);
		readInExps();

        try {
            FXMLLoader loader = new FXMLLoader();
			loader.setLocation(InitGUI.class.getResource("/views/ExpAccView.fxml"));
            VBox rootLayout = loader.load();
            
            this.mdPane.setMasterNode(rootLayout);
            this.mdPane.setDetailNode(logView);
            
            this.mdPane.setDetailSide(Side.BOTTOM);
            this.mdPane.setDividerPosition(0.80);
            this.mdPane.setShowDetailNode(false);
                  
            Scene scene = new Scene(this.mdPane);
            scene.getStylesheets().add(LogView.class.getResource("/css/log-view.css").toExternalForm());
			scene.getStylesheets().add(InitGUI.class.getResource("/css/jfoenix-components.css").toExternalForm());
            
            ExpAccViewController controller = loader.getController();
            controller.setMainApp(this);
            
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException ex) {
            logger.severe(Main.StackTraceToString(ex));
        }
    }
    
    public void picView(SinglePhenTable showMe, Stage parentScene) {
		try {
			FXMLLoader loaderPic = new FXMLLoader(getClass().getResource("/views/PicView.fxml"));
			AnchorPane page = loaderPic.load();
			
			Stage picStage = new Stage();
			picStage.initOwner(parentScene);
			
			Scene scene = new Scene(page);
			picStage.setScene(scene);
			
			PicViewController picController = loaderPic.getController();
			picController.setMainApp(this);
			picController.setStage(picStage);
			picStage.setX(getPrimaryStage().getX()+getPrimaryStage().getWidth());
			picStage.setY(getPrimaryStage().getY());
			
			picController.populateView(showMe, selectedExp);
			
		} catch (IOException ex) {
			logger.severe(Main.StackTraceToString(ex));
		}
    }

    public void showHunterView(String hunter) {
		try {
			FXMLLoader loaderHunter = new FXMLLoader(getClass().getResource("/views/GeneHunterView.fxml"));
			AnchorPane page = loaderHunter.load();

			Stage hunterStage = new Stage();
			hunterStage.setTitle("Gene Hunter Results");
			hunterStage.initOwner(this.primaryStage);
			Scene scene = new Scene(page,700,400);
			hunterStage.setScene(scene);
			hunterStage.setX(getPrimaryStage().getX()+getPrimaryStage().getWidth());
			hunterStage.setY(getPrimaryStage().getY());

            HunterViewController hunterViewController = loaderHunter.getController();
			hunterViewController.setMainApp(this);

			hunterStage.show();

			hunterViewController.populateHunterList(hunter);
			hunterViewController.populateChoice();

		} catch (IOException ex) {
			logger.severe(Main.StackTraceToString(ex));
		}
	}

    public void showHunterViewSelected(Hashtable<String,String> inSelectedFilters, ArrayList<String> inSelectedFields) {

		List<GeneHunterTable> foundGeneHunter = Main.database.lookUpHunter(inSelectedFilters);

		if (foundGeneHunter.size() > 0) {
			FXMLLoader loaderHunter = new FXMLLoader(getClass().getResource("/views/GeneHunterView2.fxml"));
			try {

				AnchorPane page = loaderHunter.load();
				Stage hunterStage = new Stage();
				hunterStage.setTitle("Gene Hunter Results");
				hunterStage.initOwner(this.primaryStage);
				Scene scene = new Scene(page, 700, 400);
				hunterStage.setScene(scene);
				hunterStage.setX(getPrimaryStage().getX() + getPrimaryStage().getWidth());
				hunterStage.setY(getPrimaryStage().getY());

				HunterViewControllerSelective hunterViewController = loaderHunter.getController();
				hunterViewController.setMainApp(this);

				hunterStage.show();

				hunterViewController.populateHunterListTab(foundGeneHunter, inSelectedFields);

			} catch (IOException ex) {
				logger.severe(Main.StackTraceToString(ex));
			}
		} else {
			showMdPane();
			logger.warning("no gene hunter fields selected to show");
		}
    }

    public void showPhenView(String selectedExp, String selectedAcc) {
		try {
			FXMLLoader loaderPhen = new FXMLLoader(getClass().getResource("/views/PhenView.fxml"));
			AnchorPane page = loaderPhen.load();
						
			Stage phenStage = new Stage();
			phenStage.setTitle("Phenotypes of "+ selectedExp);
			phenStage.initOwner(this.primaryStage);
			Scene scene = new Scene(page,700,400);
			phenStage.setScene(scene);
			phenStage.setX(getPrimaryStage().getX()+getPrimaryStage().getWidth());
			phenStage.setY(getPrimaryStage().getY());
			
			PhenViewController phenController = loaderPhen.getController();
			phenController.setMainApp(this);
			
			phenStage.show();

			//phenController.armButtons();
			
			if (selectedAcc == null) {
				phenController.populatePhen();
			} else {
				phenController.populatePhenAcc(selectedAcc);
			}
			phenController.populateChoice();

		} catch (IOException ex) {
			logger.severe(Main.StackTraceToString(ex));
		}
    }
    
    public void showPhenRatesView(String selectedAcc) {
		try {
			FXMLLoader loaderRatesPhen = new FXMLLoader(getClass().getResource("/views/PhenRatesView.fxml"));
			AnchorPane page = loaderRatesPhen.load();
						
			Stage phenRatesStage = new Stage();
			phenRatesStage.setTitle("Rates for Phenotypes of "+ selectedExp.name);
			phenRatesStage.initOwner(this.primaryStage);
			Scene scene = new Scene(page);
			phenRatesStage.setScene(scene);
			phenRatesStage.setX(getPrimaryStage().getX()+getPrimaryStage().getWidth());
			phenRatesStage.setY(getPrimaryStage().getY());
			
			PhenRatesViewController phenRatesController = loaderRatesPhen.getController();
			phenRatesController.setMainApp(this);
			
			phenRatesStage.show();
			
			if (selectedAcc == null)
				phenRatesController.populateRatesPhen();
			else
				phenRatesController.populateRatesPhenAcc(selectedAcc);

		} catch (IOException ex) {
			logger.severe(Main.StackTraceToString(ex));
		}
    }
    
    public void showSinglePhenView(AccPhenTable accession) {
		try {
			FXMLLoader loaderSinglePhen = new FXMLLoader(getClass().getResource("/views/SinglePhenView.fxml"));
			AnchorPane page = loaderSinglePhen.load();
					
			Stage singlePhenStage = new Stage();
			singlePhenStage.setTitle("Experiment " + selectedExp.name + " Single Values of Accession " + accession.getAcc());
			singlePhenStage.initOwner(this.primaryStage);
			Scene scene = new Scene(page);
			singlePhenStage.setScene(scene);
			singlePhenStage.setX(getPrimaryStage().getX()+getPrimaryStage().getWidth());
			singlePhenStage.setY(getPrimaryStage().getY());
			
			SinglePhenViewController singlePhenController = loaderSinglePhen.getController();
			singlePhenController.setMainApp(this);
			
			if (singlePhenController.readInSinglePhen(accession)) {
				singlePhenController.populateSinglePhen();
				singlePhenStage.show();
			} else {
				logger.severe("empty table");
			}
		} catch (IOException ex) {
			logger.severe("InitGUI " + ex.getMessage());
		}
    }
    
    public void showSingleRatesView(AccRatesTable rate) {
		try {
			FXMLLoader loaderSingleRate = new FXMLLoader(getClass().getResource("/views/SingleRatesView.fxml"));
			AnchorPane page = loaderSingleRate.load();
									
			Stage singleRateStage = new Stage();
			singleRateStage.setTitle("Experiment " + selectedExp.name + " Single Rates Values of Accession " + rate.ACC_ID);
			singleRateStage.initOwner(this.primaryStage);
			Scene scene = new Scene(page);
			singleRateStage.setScene(scene);
			singleRateStage.setX(getPrimaryStage().getX()+getPrimaryStage().getWidth());
			singleRateStage.setY(getPrimaryStage().getY());
			
			SingleRatesViewController singleRateController = loaderSingleRate.getController();
			singleRateController.setMainApp(this);
			
			if (singleRateController.readInSingleRates(rate)) {
				singleRateController.populateSingleRate();
				singleRateStage.show();
			} else {
				logger.severe("empty table");
			}
		} catch (IOException e) {
			logger.severe("InitGUI " + e.getMessage());
		}
    }
    
    public void addExpDialog() {
		try {
			FXMLLoader loaderaddExp = new FXMLLoader(getClass().getResource("/views/AddExpView.fxml"));
			AnchorPane page = loaderaddExp.load();
				
			Stage addExpStage = new Stage();
			addExpStage.setTitle("Please enter experiment properties ");
			addExpStage.initOwner(this.primaryStage);
			Scene scene = new Scene(page);
									
			addExpStage.setScene(scene);
			addExpStage.setX(getPrimaryStage().getX()+getPrimaryStage().getWidth());
			addExpStage.setY(getPrimaryStage().getY());
						
			AddExpViewController addExpController = loaderaddExp.getController();
			addExpController.setMainApp(this);
			addExpController.setStage(addExpStage);
			
			addExpStage.initModality(Modality.APPLICATION_MODAL);
			
			addExpStage.showAndWait();
			
		} catch (IOException ex) {
			logger.severe(Main.StackTraceToString(ex));
		}
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

	public static void mainApp() {
    	try {
			Main.database.connectDB(null);
		} catch (SQLException e) {
			System.out.println("SQL error - exit");
			System.exit(1);
		}

		// suppress the logging output to the console
//		Logger rootLogger = LogManager.getLogManager().getLogger("");
//		Handler[] handlers = rootLogger.getHandlers();
//
//		for(Handler h : handlers)
//			rootLogger.removeHandler(h);

		Application.launch(InitGUI.class);
    	Main.database.closeConnection();
    }

    public ArrayList<String> getHunterFiles() {
		return new ArrayList<>(hunterTable);
	}

    // STINGS!!!
//    private void readInHunter(ExpTable exp) {
//		try {
//			List<GeneHunterTable> temp = Main.database.getDistinctHunterFilesOfExp(exp);
//			hunterTable.clear();
//			temp.forEach((geneHunterTable -> hunterTable.add(geneHunterTable.getHunterFile())));
//		} catch (NullPointerException e) {
//			Main.logger.severe("gene hunter table of selected database is empty!");
//		} catch (SQLException e1) {
//			Main.logger.severe("database does not contain gene hunter table");
//		}
//	}

	// STINGS!!!
//	private void readInAllHunter() {
//		try {
//			List<GeneHunterTable> temp = Main.database.getAllHunterFiles();
//			hunterTable.clear();
//			temp.forEach((geneHunterTable -> hunterTable.add(geneHunterTable.getHunterFile())));
//		} catch (SQLException e) {
//			logger.severe(Main.StackTraceToString(ex));
//		}
//	}

    public void readInExps()  {
    	try {
    		logger.info(Main.database.getDbName());
    		experimentTable = FXCollections.observableArrayList(Main.database.getAllExp());
    		logger.info("read " + experimentTable.size() + " entries");
    	} catch (NullPointerException e) {
    		logger.severe("experiment table of selected database is empty!");
    	}
    }
    
    public void setExpList(ArrayList<ExpTable> exps) {
    	if (exps.size() > 0 )
    		experimentTable = FXCollections.observableArrayList(exps);
    	else
    		logger.info("no match found in DB...");
    }
    
    public void readInAccData(ExpTable inExp) {
    	try {
    		accTable = FXCollections.observableArrayList(Main.database.getAccOfExp(inExp));
    		Collections.sort(accTable);
		} catch (Exception ex) {
			logger.severe(Main.StackTraceToString(ex));
		}
    }

	public boolean readInAllDistinctHunter() {
		try {
			List<GeneHunterTable> temp = Main.database.getAllHunterFiles();
			hunterTable.clear();
			temp.forEach((geneHunterTable -> hunterTable.add(geneHunterTable.getHunterFile())));
			logger.info("read " + hunterTable.size() + " hunter files");
			return hunterTable.size() > 0;
		} catch (SQLException e1) {
			hunterTable.clear();
			logger.severe("database does not contain gene hunter table");
		}
		return false;
	}

    public ObservableList<ExpTable> getExpData() {
    	return this.experimentTable;
    }

    public ObservableList<String> getHunterTable() {
		return this.hunterTable;
	}
	public boolean removeHunter (String toRemove) {
		return hunterTable.remove(toRemove);
	}
    public ObservableList<String> getAccData() {
    	return accTable;
    }
    public void setSelectedExp(ExpTable exp) {
    	this.selectedExp = exp;
    }
    public ExpTable getSelectedExp() {
    	return selectedExp;
    }
    public boolean removeExp(ExpTable toRemove) {
    	return experimentTable.remove(toRemove);
    }
        
    public <T> void writeCSV(ObservableList<T> toWrite) {

		ArrayList<String> caps;
		ArrayList<String> valuesNames;

        try {
			caps = Main.database.getTableCaptions(toWrite.get(0).getClass());
			valuesNames = Main.database.getTableFieldNames(toWrite.get(0).getClass());
    	} catch (IndexOutOfBoundsException ex) {
			logger.info("empty selection!");
    		return;
    	}

//    	for (String aux : caps)
//    		System.out.println(aux);
                        
        FileChooser myFileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new ExtensionFilter("csv files (*.csv)","*.csv");
        myFileChooser.getExtensionFilters().add(extFilter);
        myFileChooser.setTitle("Select directory and file name");
		myFileChooser.setInitialFileName(toWrite.get(0).getClass().getSimpleName() + ".csv");
        File file = myFileChooser.showSaveDialog(getPrimaryStage());
        String newline = System.getProperty("line.separator");
                                     
        if (file != null) {      
        	logger.info("exporting sheet into " + file.getAbsoluteFile());
                   
    		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
        
	            String text = "";
	            for (String caption : caps) {
	            	text = text.concat(caption + ",");
	            }
	            text = text.concat(newline);
	            writer.write(text);
	            text = "";

				for (T aToWrite : toWrite) {
					for (int k = 0; k < caps.size(); ++k) {
						String aux = String.valueOf(aToWrite.getClass().getDeclaredField(valuesNames.get(k)).
								get(aToWrite));
						text = text.concat(aux + ",");
					}
					text = text.concat(newline);
					writer.write(text);
					text = "";
				}
	            
	           logger.info("file successfully written");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
        }
    }
    
    public boolean switchMdPane() {
    	if (this.mdPane.showDetailNodeProperty().getValue()) {
    		this.mdPane.setShowDetailNode(false);
    		return true;
    	} else {
    		this.mdPane.setShowDetailNode(true);
    		return false;
    	}
    }
    
    public void showMdPane() {
    	if (!this.mdPane.showDetailNodeProperty().getValue())
    		this.mdPane.setShowDetailNode(true);
    }
    
    public <T> void showRCode(ObservableList<T> inTable, String selected) {
		final Stage rCode = new Stage();
		TextArea tA = new TextArea();

		boolean singleFlag = false;
		String avTable = null;
		String singleTable = null;

		tA.setText("# sample code for importing table contents into R\n\n"
				+ "if (!require(RSQLite)) {\n\tinstall.packages('RSQLite')\n"
				+ "\tlibrary(RSQLite)\n}\n\nsqlite <- dbDriver(\"SQLite\")\n"
				+ "con <- dbConnect(sqlite,\"" + Main.database.getDbPath() + "\")\n");

		switch (inTable.get(0).getClass().getSimpleName()) {
			case "AccPhenTable":
				avTable = "acc_phenotypes_avg";
				break;
			case "AccRatesTable":
				avTable = "acc_rates";
				break;
			case  "SinglePhenTable":
				singleFlag = true;
				avTable = "acc_phenotypes_avg";
				singleTable = "single_phenotypes";
				break;
			case "SingleRatesTable":
				singleFlag = true;
				avTable = "acc_rates";
				singleTable = "single_ratesphenotypes";
				break;
			case "GeneHunterTable":
				tA.appendText("FullGeneHunterTable <- dbReadTable(con,\"" + "geneHunter" + "\")\n" +
						"GeneHunterTable <- subset(FullGeneHunterTable, FullGeneHunterTable$Hunter_file " +
						" == \"" + selected + "\")\ndbDisconnect(con)");

				Scene dialogScene = new Scene(tA, 800, 250);
				rCode.setTitle("Sample R code");
				rCode.setScene(dialogScene);
				rCode.show();
				return;
		}

		logger.finest("avTable: " + avTable);
		logger.finest("singleTable: " + singleTable);

		Integer expId = getSelectedExp().getId();

		tA.appendText(avTable + "<- dbReadTable(con,\"" + avTable + "\")\n"
				+ avTable + " <- subset(" + avTable + "," + avTable + "$Source_Experiment == " + expId + ")\n"
		);

		if (selected != null) {
			tA.appendText(avTable + " <- subset(" + avTable + "," + avTable + "$Source_Accession == \"" + selected
					+ "\")\n");
			System.out.println("here got " + selected);
		}

		if (singleFlag) {
			tA.appendText(singleTable + "<- dbReadTable(con,\"" + singleTable + "\")\n"
					+ singleTable + " <- subset(" + singleTable + "," + singleTable + "$Source_Experiment == " + expId
					+ ")\n"
					+ singleTable + "$Source_Accession <- " + avTable + "$Source_Accession[match("
					+ singleTable + "$Source_Accession, " + avTable + "$id)]"
			);
		}

		tA.appendText("\ndbDisconnect(con)");

	    Scene dialogScene = new Scene(tA, 800, 250);
	    rCode.setTitle("Sample R code");
	    rCode.setScene(dialogScene);
	    rCode.show();   	
    }
}


