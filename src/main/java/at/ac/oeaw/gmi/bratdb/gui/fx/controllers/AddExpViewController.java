package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.controlsfx.control.MaskerPane;
import org.controlsfx.tools.Borders;

import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.ExpTable;
import at.ac.oeaw.gmi.bratdb.ormlite.FileReadAcc;
import at.ac.oeaw.gmi.bratdb.ormlite.FileReadSingle;
import at.ac.oeaw.gmi.bratdb.ormlite.SetTable;
import javafx.stage.DirectoryChooser;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AddExpViewController {

	private File accFile = null;
	private File singleFile = null;
	private File quantsingleFile = null;

	private String expDefPath, expPixPath, expCooPath, defPath = "";

	private Stage addExpStage;
	private InitGUI mainApp;
		
	private int numberMetas;
	
	@FXML
	private VBox tab1VBox;
	@FXML
	private Tab mainTab;
	@FXML
	private Tab metaInfo;	
	@FXML
	private TextField NameText; 
	@FXML
	private TextField AuthorText; 
 	@FXML
 	private TextField defPathText; 
 	@FXML
 	private TextField picPathText; 
 	@FXML
 	private TextField cooPathText; 
 	@FXML
 	private TextField accFileText;
 	@FXML
 	private TextField singleFileText; 
 	@FXML
 	private TextField quantFileText; 
	@FXML
	private TextArea ExpDesc; 
	@FXML
	private Button CancButton;
	@FXML
	private MaskerPane masker; 
	
	private final ScrollPane scrollPane = new ScrollPane();
	private final GridPane gridPane = new GridPane();
	
	public AddExpViewController() {
	}
	
	public void setStage(Stage currStage) {
    	this.addExpStage = currStage;
    }
		
	public void setMainApp(InitGUI mainApp) {
    	this.mainApp = mainApp;
    }
	
	@FXML
    private void handleCancButtonAction() {
		Stage stage = (Stage) CancButton.getScene().getWindow();
		stage.close();
	}
	
	@FXML
    private void handleDefPathselect() {
		expDefPath = selectedPath("default path"); 
	}
	
	@FXML
    private void handlePixPathselect() {
		expPixPath = selectedPath("picture path"); 
	}
	
	@FXML
    private void handleCooPathselect() {
		expCooPath = selectedPath("picture path"); 
	}
	
	@FXML
    private void handleAddSingleButton() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Single Values Input File");
		
		if (this.defPath.equals("")) {
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		} else {
			fileChooser.setInitialDirectory(new File(this.defPath));
		}
		
		fileChooser.getExtensionFilters().addAll  (new ExtensionFilter("BRAT files (*.txt)",
				"*.txt"));
		this.singleFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
		if (this.singleFile != null) {
			Main.logger.info("choosen file: " + this.singleFile.getName() );
			singleFileText.setText(this.singleFile.getAbsolutePath());
			if (defPath.equals("")) {				
				defPath = this.singleFile.getParent();
				updatePromptText();
			}
		}
	}
	
	@FXML
    private void handleAddAccButton() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Acc Values Input File");
		
		if (this.defPath.equals("")) {
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		} else {
			fileChooser.setInitialDirectory(new File(this.defPath));
		}
		
		fileChooser.getExtensionFilters().addAll  (new ExtensionFilter("BRAT files (*.txt)", "*.txt"));
		this.accFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
		if (this.accFile != null) {
			accFileText.setText(this.accFile.getAbsolutePath());
			Main.logger.info("choosen file: " + this.accFile.getName() );
			if (defPath.equals("")) {
				defPath = this.accFile.getParent();
				updatePromptText();
			}
		}
	}
	
	@FXML
    private void handleAddSingleQuantButton() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Single Quant Values Input File");
		
		if (this.defPath.equals("")) {
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		} else {
			fileChooser.setInitialDirectory(new File(this.defPath));
		}
		
		fileChooser.getExtensionFilters().addAll  (new ExtensionFilter("BRAT files (*.txt)", "*.txt"));
		this.quantsingleFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
		if (this.quantsingleFile != null) {
			quantFileText.setText(this.quantsingleFile.getAbsolutePath());
			Main.logger.info("choosen file: " + this.quantsingleFile.getName() );
			if (defPath.equals("")) {
				defPath = this.quantsingleFile.getParent();
				updatePromptText();
			}
		}
	}
	
	@FXML
    private void handleOkButton() {
		
		String expName;
		String authorName = null;
		String expDesc = "nothing entered";
						
		if (singleFile == null || quantsingleFile == null) {
			Main.logger.severe("Input file(s) for single values is missing!");
		}
	
		if (accFile == null) {
			Main.logger.severe("Input file for phenotypes values is missing!");
			return;
		}
		
		if (AuthorText.getText() != null && !AuthorText.getText().isEmpty()) {
			authorName = AuthorText.getText();
		}
		else {
			Main.logger.info("omitting author name...");
		}
		if (ExpDesc.getText() != null && !ExpDesc.getText().isEmpty()) {
			expDesc = ExpDesc.getText();
		}
		else {
			Main.logger.info("omitting experiment description...");
		}
		
		expName = NameText.getText();
		
		final String expDesc1 = expDesc;
		final String authorName1 = authorName;
		final String expName1 = expName;
		
		if (expName1 != null && !expName1.isEmpty()) {
			
			masker.setVisible(true);
			CancButton.setDisable(true);
			///////////////
			
			 Task<ExpTable> longTask = new Task<ExpTable>() {
				@Override
				protected ExpTable call() throws Exception {
				ExpTable addme = Main.database.AddData(expName1,authorName1, expDesc1);
				try {
					FileReadAcc fi = new FileReadAcc(accFile.getAbsolutePath());
					try {
						fi.ReadInData(addme,Main.database);
					} catch (IOException ex) {
						Main.StackTraceToString(ex);
					}
				} catch (FileNotFoundException ex) {
					Main.StackTraceToString(ex);
					Main.logger.severe("file error "+ accFile.getName());
				}

				if (singleFile != null && quantsingleFile != null) {
					FileReadSingle fi = new FileReadSingle(singleFile.getAbsolutePath(), quantsingleFile.getAbsolutePath());
					fi.ReadInData(addme, Main.database);
				}
				else
				{
					Main.logger.info("input files for single values missing...no single measurements added");
				}
				return addme;
				}
			};

			longTask.setOnSucceeded(t -> {
				ExpTable addme = longTask.getValue();
				getMetaData(addme);
				addPathsToExp(addme);

				masker.setVisible(false);
				CancButton.setDisable(false);
				addExpStage.close();
			});

			new Thread(longTask).start();
		}
	}
	
	private void addPathsToExp(ExpTable exp) {
		if (expDefPath != null) {
			exp.setPath(expDefPath);
		} else {
			Main.logger.info("omitting experiment default path");
		}
		if (expPixPath != null) {
			exp.setPicPath(expPixPath);
		} else {
			Main.logger.info("omitting experiment pictures path");
		}
		if (expCooPath != null) {
			exp.setCooPath(expCooPath);
		} else {
			Main.logger.info("omitting experiment coordinates path");
		}
		Main.database.updateExpTable(exp);
	}
	
	@FXML
	private void initialize(){
		NameText.setPromptText("Experiment Name");
		AuthorText.setPromptText(System.getProperty("user.name"));
		defPathText.setPromptText(System.getProperty("user.dir"));
		picPathText.setPromptText(System.getProperty("user.dir"));
		cooPathText.setPromptText(System.getProperty("user.dir"));
						
		Node wrappedTab1 = Borders.wrap(tab1VBox)
          .lineBorder()
              .thickness(1)
              .radius(5, 5, 5, 5)
              .build()
          .build();
		
		mainTab.setContent(wrappedTab1);
		
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(50);
		gridPane.getColumnConstraints().add(column1);
		
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(50);
		gridPane.getColumnConstraints().add(column2);

		Node wrappedgrid = Borders.wrap(gridPane)
		  .lineBorder()
			  .title("Please enter meta data")
			  .thickness(1)
			  .radius(5, 5, 5, 5)
			  .build()
		  .build();
		
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setContent(wrappedgrid);
		scrollPane.setFitToWidth(true);
	
		metaInfo.setContent(scrollPane);
		
		gridPane.setAlignment(Pos.CENTER);

		ArrayList<String> metaInfos = Main.database.getTableCaptions(SetTable.class);
		
		for (int i = 0; i< metaInfos.size(); ++i) {
			Label meta = new Label (metaInfos.get(i).replace("_", " "));			
			meta.setFont(Font.font(defPath, FontWeight.BOLD, 15));

			TextArea tA = new TextArea();
			tA.setPromptText("please enter " + meta.getText() );
			
			gridPane.add(meta, 0, i); // col, row
			gridPane.add(tA, 1, i);
		}
				
		numberMetas = metaInfos.size();		
		masker.setVisible(false);
	}
		
	private void updatePromptText() {
		if (!this.defPath.equals("")) {
			accFileText.setPromptText(defPath);
			singleFileText.setPromptText(defPath);
		 	quantFileText.setPromptText(defPath);		
		}
	}
	
	private String selectedPath(String typeStr) {
		String choice = null;
				
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select directory " + typeStr);
		File defaultDirectory = new File(System.getProperty("user.home"));
		chooser.setInitialDirectory(defaultDirectory);
		File selectedDirectory = chooser.showDialog(mainApp.getPrimaryStage());
				
		if (selectedDirectory != null) {
			choice = selectedDirectory.getAbsolutePath();
			Main.logger.info("choosen directory for " + typeStr + " " + choice);
			if (defPath.equals("")) {
				defPath = selectedDirectory.getParent();
				updatePromptText();
			}
		}
		return choice;
	}
	
	private void getMetaData(ExpTable addme) {
				
		ArrayList<String> tableValue = new ArrayList<>(numberMetas);  // Variable Value to be set
		ArrayList<String> tableProperty = new ArrayList<>(numberMetas); // Variable Field Name
					
		for (int i=0;i<numberMetas; ++i) {
			tableValue.add("");
			tableProperty.add("");
		}
				
		for (Node node : gridPane.getChildren()) {
			if (node instanceof TextArea) {				
				tableValue.set(GridPane.getRowIndex(node), ((TextArea)node).getText());	
			} else if(node instanceof Label) {
				if (GridPane.getRowIndex(node) != 0) {						
					tableProperty.set(GridPane.getRowIndex(node),((Label)node).getText().replace(" ", "_"));
				}
			}
		}		
		
		Main.logger.fine("adding");
		Main.database.addMetaToExp(addme, tableValue, tableProperty);
	}
}
