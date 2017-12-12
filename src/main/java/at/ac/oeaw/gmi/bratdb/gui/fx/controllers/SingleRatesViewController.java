package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import java.util.ArrayList;

import at.ac.oeaw.gmi.bratdb.app.Main;

import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.AccRatesTable;
import at.ac.oeaw.gmi.bratdb.ormlite.SingleRatesTable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ButtonBar.ButtonData;

public class SingleRatesViewController {
	
	private static ArrayList<String> caps, valuesNames;
	private static ObservableList<SingleRatesTable> singleRatesTable = FXCollections.observableArrayList();
	
	@FXML
	private TableView<SingleRatesTable> singleRatesList;
	
	@FXML
	private Button csvExpButton, rButton; 
	@FXML
	private ChoiceBox<String> selectProp;
	
	@FXML
	private void initialize() {
		
		Main.logger.info("initializing SingleRatesView...");
		
		caps = Main.database.getTableCaptions(SingleRatesTable.class);
		valuesNames = Main.database.getTableFieldNames(SingleRatesTable.class);	

		for (int i = 0; i< caps.size(); ++i ) { //oida
			final int k = i;
			TableColumn<SingleRatesTable,?> col = new TableColumn<>(caps.get(i));
			col.setCellValueFactory(cellData ->
					Main.database.getValueOfTableAsProperty(cellData.getValue(), valuesNames.get(k)));
			singleRatesList.getColumns().add(col);
		}
		
		rButton.setOnAction(event -> mainApp.showRCode(singleRatesTable, null));

		csvExpButton.setOnAction(event -> {
			try {
				mainApp.writeCSV(singleRatesTable);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	});

		ButtonBar.setButtonData(csvExpButton, ButtonData.RIGHT);
		ButtonBar.setButtonData(rButton, ButtonData.RIGHT);
		ButtonBar.setButtonData(selectProp, ButtonData.LEFT);
			
		selectProp.setTooltip(new Tooltip("move focus to column"));
		csvExpButton.setTooltip(new Tooltip("export table contents to csv file"));
		rButton.setTooltip(new Tooltip("show sample code to import table contents into R"));
		
		populateChoice();
	}
	private InitGUI mainApp;
    	
    public SingleRatesViewController() {
    }
    
    public boolean readInSingleRates(AccRatesTable rate) {
    	singleRatesTable = FXCollections.observableArrayList(rate.getSingleValues());
		return singleRatesTable.size() != 0;
    }   
        
    public void populateSingleRate() {
    	singleRatesList.setItems(singleRatesTable);	
    }

    public void setMainApp(InitGUI mainApp) {
    	this.mainApp = mainApp;
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
    	singleRatesList.scrollToColumnIndex(index);
    }
}

