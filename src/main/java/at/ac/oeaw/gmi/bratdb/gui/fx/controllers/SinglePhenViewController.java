package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import java.util.ArrayList;

import at.ac.oeaw.gmi.bratdb.app.Main;

import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.AccPhenTable;
import at.ac.oeaw.gmi.bratdb.ormlite.SinglePhenTable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class SinglePhenViewController {

	private static ArrayList<String> caps, valuesNames;
	private static ObservableList<SinglePhenTable> singlePhenTable = FXCollections.observableArrayList();
	
	private InitGUI mainApp;
	
	@FXML
	private Button csvExpButton, rButton; 
	@FXML
	private ChoiceBox<String> selectProp;
	@FXML
	private TableView<SinglePhenTable> singlePhenList;
	
	@FXML
	private void initialize() {
		
		Main.logger.info("initializing SinglePhenView...");		
		
		caps = Main.database.getTableCaptions(SinglePhenTable.class);	
		valuesNames = Main.database.getTableFieldNames(SinglePhenTable.class);	
				
		for (int i = 0; i< caps.size(); ++i ) {
			final int k = i;
			TableColumn<SinglePhenTable,?> col = new TableColumn<>(caps.get(i));
			col.setCellValueFactory(cellData -> Main.database.getValueOfTableAsProperty(cellData.getValue(),
					valuesNames.get(k)));
			singlePhenList.getColumns().add(col);
		}

		singlePhenList.setOnMouseClicked((MouseEvent e) -> {
			if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
				SinglePhenTable selectedSingle = singlePhenList.getSelectionModel().getSelectedItem();
				if (mainApp.getSelectedExp().picPath == null) {
					mainApp.showMdPane();
					Main.logger.severe("picture path not set...");
				} else
				if (selectedSingle.core_filename == null) {
					mainApp.showMdPane();
					Main.logger.severe("no core filename given for selection...");
				} else {
					Main.logger.info("fetching " + selectedSingle.core_filename + ".jpg");
					mainApp.picView(selectedSingle, (Stage) singlePhenList.getScene().getWindow());
				}
			}
		});

		ButtonBar.setButtonData(csvExpButton, ButtonData.RIGHT);
		ButtonBar.setButtonData(rButton, ButtonData.RIGHT);
		ButtonBar.setButtonData(selectProp, ButtonData.LEFT);
		
		rButton.setOnAction(event -> mainApp.showRCode(singlePhenTable, null));				
		
		csvExpButton.setOnAction(event -> {	
			try {
				mainApp.writeCSV(singlePhenTable);
				} catch (Exception e) {
					mainApp.showMdPane();
    				Main.logger.info("No selection made for single rates values");
    			}
			});
		
		selectProp.setTooltip(new Tooltip("move focus to column"));
		csvExpButton.setTooltip(new Tooltip("export table contents to csv file \nshortcut: cmd + e"));
		rButton.setTooltip(new Tooltip("show sample code to import table contents into R \nshortcut: cmd + r"));
		singlePhenList.setTooltip(new Tooltip("double click to show image (if present...)"));

		Platform.runLater(() ->
	    	rButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN),
					() -> rButton.fire()));
		
		Platform.runLater(() ->
        	csvExpButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN),
					() -> csvExpButton.fire()));
		
		populateChoice();
	}
	    
    public SinglePhenViewController() {
    }
    
    public boolean readInSinglePhen(AccPhenTable acc) {
    	singlePhenTable = FXCollections.observableArrayList(acc.getSingleValues());
		return singlePhenTable.size() != 0;
    }
    
    public void populateSinglePhen() {
    	singlePhenList.setItems(singlePhenTable);	
    }
  
    public void setMainApp(InitGUI mainApp) {
    	this.mainApp = mainApp;
    }

	// drop down button
    private void populateChoice() {
    	ObservableList<String> chList = FXCollections.observableArrayList(caps);
    	selectProp.setValue(chList.get(0));
    	selectProp.setItems(chList);
    	selectProp.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) ->
				scrollToColumn(chList.indexOf(newVal)));
    }
    
    private void scrollToColumn(int index) {
		singlePhenList.scrollToColumnIndex(index);
    }
}
