package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import java.util.ArrayList;

import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.AccRatesTable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class PhenRatesViewController {
	private static ArrayList<String> caps, valuesNames;
	private static ObservableList<AccRatesTable> accPhenRatesTable = FXCollections.observableArrayList();
	
	private AccRatesTable selRate;
	private String selectedRate = null;
	
	@FXML
	private HBox hBoxSpacer;
	@FXML
	private Button getSingleRatesButton, rButton, csvExpButton;
	@FXML
	private ChoiceBox<String> selectProp;
	@FXML
	private TableView<AccRatesTable> phenRatesList;
	
	@FXML
	private void initialize() {
		
		caps = Main.database.getTableCaptions(AccRatesTable.class);
		valuesNames = Main.database.getTableFieldNames(AccRatesTable.class);

		for (int i = 0; i< caps.size(); ++i ) {
			final int k = i;
			TableColumn<AccRatesTable,?> col = new TableColumn<>(caps.get(i));
			col.setCellValueFactory(cellData -> Main.database.getValueOfTableAsProperty(cellData.getValue(), valuesNames.get(k)));
			phenRatesList.getColumns().add(col);
		}

		phenRatesList.getSelectionModel().selectedItemProperty().addListener((arg0, oldRate, newRate) -> {
			selRate = newRate;
			Main.logger.info("selected " + newRate.ACC_ID + " at timestep " + newRate.timestep);
		});

		rButton.setOnAction(event -> mainApp.showRCode(accPhenRatesTable, selectedRate));
		
		csvExpButton.setOnAction(event -> {	
			try {
				mainApp.writeCSV(accPhenRatesTable);
				} catch (Exception e) {
    				Main.logger.info("No selection made for single rates values");
    			}
			});
		
		getSingleRatesButton.setOnAction(event -> {
			try {
				Main.logger.info("fetching single rates values for accession " + selRate.ACC_ID + " timestep " + selRate.timestep);
				mainApp.showSingleRatesView(selRate);
			} catch (NullPointerException e) {
				Main.logger.info("No selection made for single rates values");
			}
		});

		selectProp.setTooltip(new Tooltip("move focus to column"));
		csvExpButton.setTooltip(new Tooltip("export table contents to csv file"));
		rButton.setTooltip(new Tooltip("show sample code to import table contents into R"));
		getSingleRatesButton.setTooltip(new Tooltip("click to show single measurements"));
		
		populateChoice();
		HBox.setHgrow(hBoxSpacer, Priority.ALWAYS);
	}
	private InitGUI mainApp;
    
    public PhenRatesViewController() {
    }
           
    public void setMainApp(InitGUI mainApp) {
    	this.mainApp = mainApp;
    }
    
    // to show just the ONE selected accessions of experiment
    public void populateRatesPhenAcc(String selectedRate) {
    	this.selectedRate = selectedRate;
    	accPhenRatesTable = FXCollections.observableArrayList(Main.database.
				getAccPhenRatesOfExp(mainApp.getSelectedExp(),selectedRate));
    	this.phenRatesList.setItems(accPhenRatesTable);	
    }
    
    // to show all accessions of experiment
    public void populateRatesPhen() {
    	accPhenRatesTable = FXCollections.observableArrayList(Main.database.getPhenRatesOfExp(mainApp.getSelectedExp()));	
    	this.phenRatesList.setItems(accPhenRatesTable);	
    }
    
    // drop down button
    private void populateChoice() {
    	ObservableList<String> chList = FXCollections.observableArrayList(caps);
    	selectProp.setValue(chList.get(0));
    	selectProp.setItems(chList);
    	selectProp.getSelectionModel().selectedItemProperty().
    		addListener((ObservableValue<? extends String> obs, String oldVal, String newVal) -> 
    		scrollToColumn(chList.indexOf(newVal)));
    }
    
    private void scrollToColumn(int index) {
    	phenRatesList.scrollToColumnIndex(index);
    }
}
