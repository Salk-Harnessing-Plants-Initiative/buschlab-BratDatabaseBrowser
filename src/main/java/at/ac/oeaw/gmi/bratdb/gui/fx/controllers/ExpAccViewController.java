package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import at.ac.oeaw.gmi.bratdb.app.ClsSetterString;
import at.ac.oeaw.gmi.bratdb.app.ConfigCls;
import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.gui.fx.TabPaneDetacher;
import at.ac.oeaw.gmi.bratdb.ormlite.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import com.jfoenix.controls.JFXButton;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.controlsfx.tools.Borders;

import javax.persistence.Column;
import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import java.util.logging.Level;

public class ExpAccViewController {
		
	@FXML
	private ListView<ExpTable> expList;
	@FXML
	private ListView<String> geneHunterList;
	@FXML
	private ToggleSwitch perspectiveSwitch;
	@FXML
	private ListView <String> accIdList, hunterOrigList;
	@FXML
	private JFXButton addExp, lookUpButton, consoleButton, resetButton;
	@FXML
	private Label accLabel;
	@FXML
	private MenuBar menuBar;
	@FXML
	private Menu menuFile, menuEdit, menuHelp;
	@FXML
	private GridPane dbGridInfo;
	@FXML
	private MaskerPane rootMasker;
	@FXML
	private StackPane mainStack;
	@FXML
	private SplitPane detailSplitPane, hunterSplit;
	@FXML
	private TableView <SearchCriterion> lookUpTableView;

	private final Label dbShow = new Label();
	private final Label dbSize = new Label();
	private final Label dbDate = new Label();
	
	private final GridPane expInfo = new GridPane();
	private final ArrayList<Label> expDetailLabels = new ArrayList<>();
	private final Accordion allSets = new Accordion();
	private final TabPane setHunterTabPane = new TabPane();
	private final ListView<String> hunterOfExp = new ListView<>();

	private final ArrayList<String> metaTableCaptions = Main.database.getTableCaptions(SetTable.class);
	private final ArrayList<String> setTableFieldNames = Main.database.getTableFieldNames(SetTable.class);
	private final ArrayList<String> metaHelper = Main.database.getTableFieldNames(ExpTable.class);

	private final ArrayList<TitledPane> setInfos = new ArrayList<>();

	private final Tab hunterTab = new Tab("gene hunter files");
	private final ObservableList<SearchCriterion> hunterObs = FXCollections.observableArrayList();

	private final List<String> hunterFields = Arrays.stream(GeneHunterTable.class.getFields())
			.filter(f -> f.getAnnotation(Column.class)!=null).map(f -> f.getAnnotation(Column.class).name()).
					collect(Collectors.toList());

	private String selAcc;
	
	@FXML
	private void initialize() {

		expList.setCellFactory(new Callback<ListView<ExpTable>, ListCell<ExpTable>>() {
			@Override
			public ListCell<ExpTable> call (ListView<ExpTable> list) {
				ListCell<ExpTable> cell = new ListCell<ExpTable> () {
					@Override
					protected void updateItem(ExpTable exp, boolean empty) {
						super.updateItem(exp, empty);
						if ((empty || exp == null)) {
							setItem(null);
							setText("");
							setContextMenu(null);
						} else {
							setText(exp.getName());
							setContextMenu(showRightClickMenu(exp));
						}
					}
				};
			
				cell.setOnMouseClicked((MouseEvent event) -> {
	                if (cell.isEmpty() || cell.getText() == null) {
	                    event.consume(); 
	                }
	                else {
	                	if (event.getButton().equals(MouseButton.PRIMARY)) {
	    					if (event.getClickCount() == 2) {
	    						mainApp.setSelectedExp(expList.getSelectionModel().getSelectedItem());
	    						mainApp.readInAccData(mainApp.getSelectedExp());	    							    		
	    						accIdList.setItems(mainApp.getAccData());
	    						accLabel.setText("Accessions of " + mainApp.getSelectedExp().name +
										" (items " +accIdList.getItems().size()+")");
	    						mainApp.showPhenView(mainApp.getSelectedExp().name,null);
	    					
	    						Main.logger.info("initializing PhenView for " + mainApp.getSelectedExp().name);	
	    					}
	    				}
	                }
	            });
				return cell;
			}
		});
		
		expList.setTooltip(new Tooltip("Double click to show all phenotypes for experiment"));

		expList.getSelectionModel().selectedItemProperty().addListener((arg0, oldExp, newExpc) -> {
			if (expList.getSelectionModel().getSelectedItem() != null) {
				populateDetails(expList.getSelectionModel().getSelectedItem());
				mainApp.setSelectedExp(expList.getSelectionModel().getSelectedItem());
				mainApp.readInAccData(mainApp.getSelectedExp());
				accIdList.setItems(mainApp.getAccData());
				accLabel.setText("Accessions of " + mainApp.getSelectedExp().name +
						" (items " +accIdList.getItems().size()+")");
			}
		});

		accIdList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call (ListView<String> list) {
				ListCell<String> cell = new ListCell<String> () {
					@Override 
					protected void updateItem(String acc, boolean empty) {
					super.updateItem(acc, empty);
						if ((empty || acc == null)) {
							setItem(null);
							setText("");
						} else {
							setText(acc);
						}
					}	
				};
				cell.setOnMouseClicked((MouseEvent event) -> {
	                if (cell.isEmpty() || cell.getText() == null) {
	                    event.consume(); 
	                }
	                else {
	                	if (event.getButton().equals(MouseButton.PRIMARY)) {
	    					if (event.getClickCount() == 2) {
	    						Main.logger.info("initializing PhenView for experiment " + mainApp.getSelectedExp().name + " accession " + selAcc);
	    						mainApp.showPhenView(mainApp.getSelectedExp().name,selAcc);
	    					}	    					
	    				}
	                }
	            });
				return cell;
			}
		});
				
		accIdList.setTooltip(new Tooltip("Double click to show phenotypes of selected accession"));
		accIdList.getSelectionModel().selectedItemProperty().addListener((arg0, oldAcc, newAcc) -> {
			selAcc = newAcc;
			Main.logger.info("selected Accession: " + selAcc);
		});

		accIdList.setOnKeyPressed(event -> {
			if(event.getCode() == KeyCode.ENTER) { 
				Main.logger.info("initializing PhenView for experiment " + mainApp.getSelectedExp().name +
						" accession " + selAcc);
				mainApp.showPhenView(mainApp.getSelectedExp().name,selAcc);
			} 
		});

		hunterOfExp.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call (ListView<String> list) {
				ListCell<String> cell = new ListCell<String> () {
					@Override
					protected void updateItem(String hunterfile, boolean empty) {
					super.updateItem(hunterfile, empty);
					if ((empty || hunterfile == null)) {
						setItem(null);
						setText("");
					} else {
						setText(hunterfile);
					}
					}
				};
				cell.setOnMouseClicked((MouseEvent event) -> {
					if (cell.isEmpty() || cell.getText() == null) {
						event.consume();
					}
					else {
						if (event.getButton().equals(MouseButton.PRIMARY)) {
							if (event.getClickCount() == 2) {
								// ATTENTION: here still use exp!
								Main.logger.info("initializing GeneHunterView for experiment " +
										mainApp.getSelectedExp().name + " hunterfile " + cell.getText());
								mainApp.showHunterView(cell.getText());
							}
						}
					}
				});
				return cell;
			}
		});

//		addExp.setOnAction(event -> {
//			mainApp.addExpDialog();
//			mainApp.readInExps();
//			expList.setItems(mainApp.getExpData());
//			updateDbInfoGrid();
//		});

		/////////// GENE HUNTER VIEW ///////////////////
		geneHunterList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
		  @Override
		  public ListCell<String> call(ListView<String> list) {
			  ListCell<String> cell = new ListCell<String>() {
				  @Override
				  protected void updateItem(String hunter, boolean empty) {
				  super.updateItem(hunter, empty);
				  if ((empty || hunter == null)) {
					  setItem(null);
					  setText("");
					  setContextMenu(null);
				  } else {
					  setText(hunter);
					  setContextMenu(showRightClickMenuHunterList(hunter));
				  }
			  }
		  };
			  cell.setOnMouseClicked((MouseEvent event) -> {
				  if (cell.isEmpty() || cell.getText() == null) {
					  event.consume();
				  } else {
					  if (event.getButton().equals(MouseButton.PRIMARY)) {
						  if (event.getClickCount() == 2) {
							  Main.logger.info(" hunterfile " + cell.getText());
							  mainApp.showHunterView(cell.getItem());
						  }
					  }
				  }
			  });
			  return cell;
		  }
		});

		geneHunterList.getSelectionModel().selectedItemProperty().addListener((arg0, oldHunter, newHunter) -> {
			if (geneHunterList.getSelectionModel().getSelectedItem() != null) {
				hunterOrigList.setItems(FXCollections.observableArrayList(Main.database.getOrigFilesOfHunter(newHunter)));
			}
		});

		for (String hunterField : hunterFields) {
			hunterObs.add(new SearchCriterion(hunterField, "all", true));
		}

		TableRowExpanderColumn<SearchCriterion> expander = new TableRowExpanderColumn<>(this::createEditor);
		lookUpTableView.getColumns().add(expander);

		TableColumn<SearchCriterion,String> col = new TableColumn<>("   Field   ");
		col.setCellValueFactory(cellData -> cellData.getValue().getFieldName());

		TableColumn<SearchCriterion,String> col2 = new TableColumn<>("   Filter   ");
		col2.setCellValueFactory(cellData -> cellData.getValue().getFilter());

		TableColumn<SearchCriterion,Boolean> col3 = new TableColumn<>("   Display in Results  ");
		col3.setCellValueFactory(cellData -> cellData.getValue().getSelected());

		// lookUpTableView.getColumns().addAll(col,col2,col3); => uncheck generics warning
		lookUpTableView.getColumns().addAll(Arrays.asList(col,col2,col3));
		lookUpTableView.setItems(hunterObs);

		//lookUpTableView.setColumnResizePolicy((param) -> true );
		Platform.runLater(() -> customResize(lookUpTableView));

		//////////////////////////////////////

        addExp.setOnAction(event -> buttonActionAddFile());
		lookUpButton.setOnAction(event -> buttonActionSearch());
		resetButton.setOnAction(event -> buttonActionReload());
		consoleButton.setOnAction(event -> mainApp.switchMdPane());

		Tooltip toolTip1 = new Tooltip("Click to add a single experiment");
		Tooltip.install(addExp, toolTip1);

		Tooltip toolTip2 = new Tooltip("Click to Show / Hide console output"); 
		Tooltip.install(consoleButton, toolTip2);

		populateDbInfoGrid();
		populateMenu();		
		prepareDetails();
		
		// def. no hunter files present
		perspectiveSwitch.setSelected(false);
		// initially remove huntersplit
		this.mainStack.getChildren().remove(1);

		perspectiveSwitch.selectedProperty().addListener((observable, oldVal, newVal) -> switchPerspective());

		ButtonBar.setButtonData(resetButton, ButtonData.LEFT);		
		rootMasker.setVisible(false);
		//hunterSplit.setStyle("-fx-background-color: red");

		addExp.getStyleClass().add("button-raised");// lookUpButton, consoleButton,
		resetButton.getStyleClass().add("button-raised");
		lookUpButton.getStyleClass().add("button-raised");
		consoleButton.getStyleClass().add("button-raised");

		//expList.getStyleClass().add("mylistview");
	}
	
    private InitGUI mainApp;

	private void switchPerspective() {
		// if huntersplit present delete it
		// too many layers is mainStack slow down resizing...
		if (perspectiveSwitch.isSelected()) {
			this.mainStack.getChildren().add(hunterSplit);
			lookUpButton.setText("_Search Hunter Files");
			resetButton.setText("_Reload Hunter Files");

			addExp.setText("_add GeneHunter File");
			// else add it
		} else {
			this.mainStack.getChildren().remove(2);
			lookUpButton.setText("_Look up Accession");
			resetButton.setText("_Reload Exp List");

			addExp.setText("_add Single Exp");
		}
	}

    public ExpAccViewController() {
    }
    
    public void setMainApp(InitGUI mainApp) {
    	this.mainApp = mainApp;
    	this.expList.setItems(mainApp.getExpData());

    	if (!mainApp.readInAllDistinctHunter()) {
			perspectiveSwitch.setDisable(true);
			hunterTab.setDisable(true);
		} else {
			this.geneHunterList.setItems(mainApp.getHunterTable());
		}

		mainApp.mdPane.showDetailNodeProperty().addListener((observable, oldVal, newVal) -> {
			if (newVal)
				consoleButton.setText("Hide Console");
            else
				consoleButton.setText("Show Console");
		});
    }

	private void buttonActionReload() {
		if (perspectiveSwitch.isSelected()) {
			mainApp.readInAllDistinctHunter(); //actually return true/ false
		} else {
			mainApp.readInExps();
			expList.setItems(mainApp.getExpData());
		}
	}

    private void buttonActionAddFile() {
        if (perspectiveSwitch.isSelected()) {

        	addGeneHunterFile(null);
//            try {
//                FileChooser fileChooser = new FileChooser();
//                fileChooser.setTitle("Select gene hunter file to add");
//                File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
//
//                if(file != null) {
//                    ReadGeneHunterTable newGeneHunterTab = new ReadGeneHunterTable(file.getAbsolutePath());
//                    newGeneHunterTab.getfirstline();
//                    newGeneHunterTab.readIn(null, Main.database);
//
//                    if (mainApp.readInAllDistinctHunter()) {
//                        perspectiveSwitch.setDisable(false);
//                        hunterTab.setDisable(false);
//                        this.geneHunterList.setItems(mainApp.getHunterTable());
//                    }
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
        } else {
            mainApp.addExpDialog();
			mainApp.readInExps();
			expList.setItems(mainApp.getExpData());
			updateDbInfoGrid();
        }
    }

    private void buttonActionSearch() {
    	if (perspectiveSwitch.isSelected()) {
			ArrayList<String> selectedFields = new ArrayList<>();
			Hashtable<String,String> selectedFilters = new Hashtable<>();
			hunterObs.forEach((SearchCriterion searchCriterion) -> {
				if (searchCriterion.isSelected()) {
					selectedFields.add(searchCriterion.getFieldName().getValue());
					if (!searchCriterion.getFilter().getValue().isEmpty() &&
							!searchCriterion.getFilter().getValue().equals("all")) {
						selectedFilters.put(searchCriterion.getFieldName().getValue(),
								searchCriterion.getFilter().getValue());
					}
				}
			} );

			if (selectedFields.size() > 0)
            	mainApp.showHunterViewSelected(selectedFilters, selectedFields);
			else {
				mainApp.showMdPane();
				Main.logger.severe("no gene hunter fields selected to show");
			}

		} else {
			TextInputDialog dlg = new TextInputDialog("");
			dlg.setTitle("Look up Accession in Database");
			dlg.setHeaderText("Please type accession");
			dlg.setContentText("enter here");
			Optional<String> result = dlg.showAndWait();
			result.ifPresent((String accToLookUp) -> {
				try {
					HashSet<String> res = new HashSet<>(Main.database.findAllAccLike(accToLookUp));

					if (res.isEmpty()) {
						Alert nothingFound = new Alert(AlertType.ERROR);
						nothingFound.setTitle(result.get());
						nothingFound.setContentText("no matching accession found");
						nothingFound.showAndWait();
					} else {
						accLookUp(res);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	private ContextMenu showRightClickMenuHunterList(String hunter) {
		ContextMenu rightClickMenu = new ContextMenu();

		MenuItem removefromList = new MenuItem("remove " + hunter + " from browser");
		MenuItem removefromDB = new MenuItem("remove " + hunter + " from database (permanently)");

		removefromList.setOnAction( (ActionEvent ev) -> {
			if (mainApp.removeHunter(hunter)) {

				if (mainApp.getHunterTable().size() < 1)
					hunterOrigList.getItems().clear();
				else
					geneHunterList.setItems(mainApp.getHunterTable());

				Main.logger.info("removed " + hunter + " from browser");
			}
			else {
				Main.logger.info("error while removing "+ hunter + " from browser");
			}
		});

		removefromDB.setOnAction((ActionEvent event) -> {
			Alert confirm = new Alert(AlertType.CONFIRMATION);
			confirm.setTitle("Please Confirm");
			confirm.setContentText("permanently remove hunter file " + hunter + " from " +
					Main.database.getDbName() + "?");

			confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			confirm.showAndWait()
				.filter(response -> response == ButtonType.OK)
				.ifPresent(response -> {
					disableGuiFct();

					Task<Void> longTask = new Task<Void>() {
						@Override
						protected Void call() throws Exception {
						updateMessage("removing " + hunter);
						Main.database.removeHunterFileByName(hunter);
						return null;
						}
					};

					longTask.setOnSucceeded(WorkerStateEvent -> {
						enableGuiFct();
						if (mainApp.removeHunter(hunter)) {

							ExpTable selected = expList.getSelectionModel().getSelectedItem();

							if (selected != null)
								populateExpHunterTab(selected);

							if (mainApp.getHunterTable().size() < 1) {
								perspectiveSwitch.setSelected(false);
								//manually clearing list, listener does nothing
								// in case of empty list
								hunterOrigList.getItems().clear();
							}
							else
								geneHunterList.setItems(mainApp.getHunterTable());

							updateDbInfoGrid();
							Main.logger.info("removed " + hunter + " from " + Main.database.getDbName());
						}
						Main.logger.fine("in succeeded - i am " + Thread.currentThread().getName());
					});

					rootMasker.textProperty().bind(longTask.messageProperty());
					new Thread(longTask).start();
				}
			);
		});

		rightClickMenu.getItems().addAll(removefromList, removefromDB);
		return rightClickMenu;
	}

	private void addGeneHunterFile(ExpTable i_exp) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select gene hunter file to add");
		fileChooser.getExtensionFilters().addAll  (new FileChooser.ExtensionFilter(
				"Gene Hunter Files (*.txt, *.csv)",
				"*.txt", "*.csv"));
		File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

		if(file != null) {

			disableGuiFct();
			Task<Void> longTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
				updateMessage("reading " + file.getAbsolutePath());
				ReadGeneHunterTable newGeneHunterTab = new ReadGeneHunterTable(file.getAbsolutePath());
				newGeneHunterTab.getfirstline();
				newGeneHunterTab.readIn(i_exp, Main.database);

				return null;
				}
			};
			longTask.setOnSucceeded(WorkerStateEvent -> {
				enableGuiFct();

				if (mainApp.readInAllDistinctHunter()) {
					perspectiveSwitch.setDisable(false);
					hunterTab.setDisable(false);
					geneHunterList.setItems(mainApp.getHunterTable());
				}

				if (i_exp != null)
					populateExpHunterTab(i_exp);

				updateDbInfoGrid();
				Main.logger.fine("in succeeded - i am " + Thread.currentThread().getName());
			});

			rootMasker.textProperty().bind(longTask.messageProperty());
			new Thread(longTask).start();
		}
	}

    private ContextMenu showRightClickMenu(ExpTable exp) {

    	ContextMenu rightClickMenu = new ContextMenu(); 
    
    	MenuItem sortByAuthor = new MenuItem("sort experiment list by author");
    	MenuItem sortByName = new MenuItem("sort experiment list by name");
    	MenuItem removefromList = new MenuItem("remove " + exp.name + " from browser");
    	MenuItem removefromDB = new MenuItem("remove " + exp.name + " from database (permanently)");
    	MenuItem editExpMeta = new MenuItem("edit " + exp.name + " meta data");
    	MenuItem updateCoords = new MenuItem("update picture coordinates of " + exp.name);
    	MenuItem getAllCoords = new MenuItem("read in all available coordinates of " + exp.name);
    	MenuItem exportExp = new MenuItem("export experiment " + exp.name);
    	MenuItem addHunterFile = new MenuItem("add genehunter file to " + exp.getName());

    	addHunterFile.setOnAction( (ActionEvent ev) -> {

			addGeneHunterFile(exp);

//			FileChooser fileChooser = new FileChooser();
//			fileChooser.setTitle("Select gene hunter file to add");
//			fileChooser.getExtensionFilters().addAll  (new FileChooser.ExtensionFilter(
//					"Gene Hunter Files (*.txt, *.csv)",
//					"*.txt", "*.csv"));
//			File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
//
//			if(file != null) {
//
//				disableGuiFct();
//				Task<Void> longTask = new Task<Void>() {
//					@Override
//					protected Void call() throws Exception {
//						updateMessage("reading " + file.getAbsolutePath());
//						ReadGeneHunterTable newGeneHunterTab = new ReadGeneHunterTable(file.getAbsolutePath());
//						newGeneHunterTab.getfirstline();
//						newGeneHunterTab.readIn(exp, Main.database);
//
//						if (mainApp.readInAllDistinctHunter()) {
//							perspectiveSwitch.setDisable(false);
//							hunterTab.setDisable(false);
//							geneHunterList.setItems(mainApp.getHunterTable());
//						}
//
//						return null;
//					}
//				};
//				longTask.setOnSucceeded(WorkerStateEvent -> {
//					enableGuiFct();
//					populateExpHunterTab(exp);
//					Main.logger.info("in succeeded - i am " + Thread.currentThread().getName());
//				});
//
//				rootMasker.textProperty().bind(longTask.messageProperty());
//				new Thread(longTask).start();
//			}
		});

    	exportExp.setOnAction( (ActionEvent ActionEvent) -> {

			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DB files (*.db)", "*.db");
			fileChooser.setTitle("Select output database");
			fileChooser.setInitialFileName(exp.name + ".db");
			fileChooser.getExtensionFilters().add(extFilter);
			File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

			if(file != null){
				disableGuiFct();
				Task<Void> longTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
					updateMessage("started");
					Main.logger.info("exporting " + exp.name + " in thread " + Thread.currentThread().getName());

					ArrayList<AccPhenTable> phens = Main.database.getPhenOfExp(exp);
					ArrayList<AccRatesTable> rates = Main.database.getPhenRatesOfExp(exp);
					List<SetTable> sets = Main.database.getSetsOfExp(exp);

					DB exportDB = new DB(file);
					updateMessage("fetched data...");

					exportDB.AddExp(exp);
					updateMessage("exported exp");
					exportDB.AddPhenTable(phens);
					updateMessage("exported phenotypes");

					exportDB.AddRatesTable(rates);
					updateMessage("exported rates");

					exportDB.AddSinglePhensAll(Main.database.getSingleAccOfExp(exp));
					updateMessage("exported single phenotypes");

					exportDB.AddSingleRatesAll(Main.database.getSingleRatesOfExp(exp));
					updateMessage("exported singles rates");

					exportDB.AddMetas(sets);
					updateMessage("exported meta data");

					exportDB.closeConnection();
					return null;
					}
				};
				longTask.setOnSucceeded(WorkerStateEvent -> {
					enableGuiFct();
					Main.logger.fine("in succeeded - i am " + Thread.currentThread().getName());
//					Notifications.create()
//		              .title("Background Task")
//		              .text(exp.name + " exported to " + file.getPath())
//		              .showInformation();
				});

				rootMasker.textProperty().bind(longTask.messageProperty());
				new Thread(longTask).start();
			}
		});

    	sortByName.setOnAction((ActionEvent event) -> {
			SortedList<ExpTable> sortedData = new SortedList<>(mainApp.getExpData());
            sortedData.setComparator((exp1, exp2) -> exp1.getName().compareToIgnoreCase(exp2.getName()));

			expList.setItems(sortedData);
			Main.logger.info("sorted list by experiment name");
    	});
    	
    	sortByAuthor.setOnAction((ActionEvent event) -> {
			SortedList<ExpTable> sortedData = new SortedList<>(mainApp.getExpData());
            sortedData.setComparator((exp1, exp2) -> exp1.getAuthor().compareToIgnoreCase(exp2.getAuthor()));

			expList.setItems(sortedData);
			Main.logger.info("sorted list by author name");
    	});
    	
    	removefromList.setOnAction(event -> {
			if (mainApp.removeExp(exp)) {
				expList.setItems(mainApp.getExpData());
				Main.logger.info("removed " + exp.name + " from browser");
			} 
			else {
				Main.logger.info("Error");
			}
    	});
    	
    	removefromDB.setOnAction(event -> {
	        Alert confirm = new Alert(AlertType.CONFIRMATION);
	        confirm.setTitle("Please Confirm");
	        confirm.setContentText("permanently remove experiment " + exp.getName() + " from " +
					Main.database.getDbName() + "?");

	        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

			confirm.showAndWait()
				.filter(response -> response == ButtonType.OK)
				.ifPresent(response -> {
					disableGuiFct();

					Task<Void> longTask = new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							updateMessage("removing " + exp.getName());
							Main.database.removeExpTableByName(exp);
							return null;
						}
					};
					longTask.setOnSucceeded(WorkerStateEvent -> {
						enableGuiFct();

						if (mainApp.removeExp(exp)) {
							expList.setItems(mainApp.getExpData());
							updateDbInfoGrid();
							Main.logger.info("removed " + exp.name + " from " + Main.database.getDbName());
						}

						Main.logger.fine("in succeeded - i am " + Thread.currentThread().getName());
					});

					rootMasker.textProperty().bind(longTask.messageProperty());
					new Thread(longTask).start();
				}
			);
		});
    	
    	editExpMeta.setOnAction(event -> {
    			
			GridPane editExpMetaPane = new GridPane(); 
			SplitPane container = new SplitPane();
			ArrayList<TextField> newLabels = new ArrayList<>();
			
			ArrayList<String> expCaptions = Main.database.getAllTableCaptions(ExpTable.class);    	
	    	ArrayList<String> helper = Main.database.getAllTableFieldNames(ExpTable.class);
	    	
	    	for (int i = 0; i< expCaptions.size();++i) {
	    		editExpMetaPane.add(new Label(expCaptions.get(i)),0,i);

	    		TextField newEntry = new TextField(Main.database.getValueOfTableAsString(exp, helper.get(i)));
	    		newEntry.isEditable();
	    		newLabels.add(newEntry);
	    		
	    		editExpMetaPane.add(newEntry, 1, i);
	    	}
	    	
	    	ColumnConstraints column1 = new ColumnConstraints();
			column1.setPercentWidth(20);
			editExpMetaPane.getColumnConstraints().add(column1);
			
			ColumnConstraints column2 = new ColumnConstraints();
			column2.setPercentWidth(80);
			editExpMetaPane.getColumnConstraints().add(column2);
			
			ButtonBar bBar = new ButtonBar();
			Button okButton = new Button("Ok");
			okButton.setDefaultButton(true);
			ButtonBar.setButtonData(okButton, ButtonData.OK_DONE); 
			
			Button cancButton = new Button("Cancel");
			ButtonBar.setButtonData(cancButton, ButtonData.CANCEL_CLOSE); 
			bBar.getButtons().addAll(cancButton, okButton);
			
			container.setOrientation(Orientation.VERTICAL);
			container.getItems().addAll(editExpMetaPane,bBar);
			container.setDividerPositions(0.1);

	    	Node wrappedSplit = Borders.wrap(container)
				.lineBorder()
				.thickness(1)
				.radius(5, 5, 5, 5)
				.build()
				.build();

	    	Stage editExpMetaStage = new Stage(StageStyle.UTILITY);
	    	editExpMetaStage.setTitle("Edit Meta data of " + exp.name);
	    	editExpMetaStage.setScene(new Scene((Parent) wrappedSplit,700,280));
	    	editExpMetaStage.show();
	    	
	    	cancButton.setOnAction(subEvent -> editExpMetaStage.close());
	    	//okButton.setOnAction(new MyEventHandler(exp, newLabels, helper,editExpMetaStage));
			okButton.setOnAction(new MyEventHandler(exp, newLabels, helper,editExpMetaStage));
    	});
    	    	
    	updateCoords.setOnAction((ActionEvent event) -> {
			disableGuiFct();
	        Task<Void> longTask = new Task<Void>() {
	            @Override
	            protected Void call() throws Exception {
	            	Main.database.addCooToExp(exp);
	                return null;
	            }
	        };
	        longTask.setOnSucceeded((WorkerStateEvent) -> {
				mainApp.readInExps();
				enableGuiFct();
	        });

	        new Thread(longTask).start();
    	});
    	
    	getAllCoords.setOnAction(event -> {
			disableGuiFct();
	        Task<Void> longTask = new Task<Void>() {
	            @Override
	            protected Void call() throws Exception {
	            	Main.database.addVictorsCooToExp(exp);
	                return null;
	            }
	        };

			longTask.setOnSucceeded((WorkerStateEvent) -> enableGuiFct());
	        new Thread(longTask).start();
    	});
    	
    	rightClickMenu.getItems().addAll(sortByAuthor, sortByName, new SeparatorMenuItem(), exportExp,
				new SeparatorMenuItem(), removefromList, removefromDB, editExpMeta, updateCoords, getAllCoords,
				addHunterFile);
    	
    	return rightClickMenu;
    }

    private void prepareDetails() {
    	
    	ArrayList<String> expCaptions = Main.database.getTableCaptions(ExpTable.class);   	

    	for (int i = 0; i< expCaptions.size();++i) {
    		expInfo.add(new Label(expCaptions.get(i)),0,i);
    		expDetailLabels.add(new Label(" "));
    		expInfo.add(expDetailLabels.get(i), 1, i);
    	}
    	
    	Node wrappedGrid = Borders.wrap(expInfo)
			.lineBorder()
			.thickness(1)
			.radius(5, 5, 5, 5)
			.build()
			.build();

		detailSplitPane.getItems().addAll(wrappedGrid, setHunterTabPane);
		detailSplitPane.setDividerPosition(0, 0.1);

		Tab setTab = new Tab("Meta data", allSets);
		hunterTab.setContent(hunterOfExp);

		TabPaneDetacher.create().makeTabsDetachable(setHunterTabPane);
		setHunterTabPane.getTabs().addAll(setTab, hunterTab);

    	ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(30);
		expInfo.getColumnConstraints().add(column1);
    }
    
    private void populateDetails(ExpTable exp) {
    	
    	String value, value1;

    	ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(50);
		
		for (int i = 0; i< expDetailLabels.size();++i) {
			value = Main.database.getValueOfTableAsString(exp, metaHelper.get(i));
    		if (value.equals("")) value = "not set";
    		
    		expDetailLabels.get(i).setText(value);  
    		expDetailLabels.get(i).setWrapText(true);
    		
    		Tooltip tp = new Tooltip(value);
    		bindTooltip(expDetailLabels.get(i), tp);
    	}
		
		Main.logger.info("showing details for experiment " + exp.getName());

		List<SetTable> expSets;
		try {
			expSets = Main.database.getSetsOfExp(exp);
    	} catch (NullPointerException e) {
    		allSets.getPanes().clear();
    		return;
    	}

    	setInfos.clear();

    	for (SetTable expSet : expSets) {
    		StringBuilder allValues = new StringBuilder();
    		GridPane setInfo = new GridPane();
    		setInfo.setPadding(new Insets(5, 5, 5, 5));
    		setInfo.getColumnConstraints().add(column1);

    		for (int k = 0;k< metaTableCaptions.size();++k) {
    			value1 = metaTableCaptions.get(k);
    			setInfo.add(new Label(value1), 0, k);
    			value = Main.database.getValueOfTableAsString(expSet, setTableFieldNames.get(k));
    			Label wrappedLabel = new Label(value);

    			wrappedLabel.setWrapText(true);
    			allValues.append(value1).append(": ").append(value).append("\n");
    			setInfo.add(wrappedLabel, 1, k);
    		}
    		   		
    		Tooltip tp1 = new Tooltip(allValues.toString());
    		bindTooltip(setInfo, tp1); 
    		ScrollPane scrollPane = new ScrollPane(setInfo);
    		scrollPane.setFitToHeight(true);
    		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    		
    		setInfo.prefWidthProperty().bind(scrollPane.widthProperty());
    		
    		value = Main.database.getValueOfTableAsString(expSet, "expBatch");
    		TitledPane info = new TitledPane(value,scrollPane);
    		setInfos.add(info);
    	}
    	    	
    	allSets.getPanes().clear();
    	allSets.getPanes().addAll(setInfos);

    	if (setInfos.size() > 0)
    		allSets.setExpandedPane(setInfos.get(0));

		populateExpHunterTab(exp);

//		try {
//			ObservableList<String> hunterList = FXCollections.observableList(Main.database.getHunterFilesAsString(exp));
//			hunterOfExp.setItems(hunterList);
//		} catch (SQLException e) {
//			perspectiveSwitch.setDisable(true);
//			hunterTab.setDisable(true);
//			Main.logger.info("no hunter files found in database");
//		}
    }

    // 2609: moved outside of populateDetails method to
	// update hunterOfExp in case hunter file was deleted by user
    private void populateExpHunterTab(ExpTable i_exp) {
		try {
			ObservableList<String> hunterList = FXCollections.observableList(Main.database.getHunterFilesAsString(i_exp));

			if (hunterList.size() < 1)
				hunterTab.setDisable(true);
			else {
				hunterOfExp.setItems(hunterList);
				hunterTab.setDisable(false);
			}

		} catch (SQLException e) {
			perspectiveSwitch.setDisable(true);
			hunterTab.setDisable(true);
			Main.logger.info("no hunter files found in database");
		}
	}
   
	private void populateMenu() {
			
		MenuItem newDb = new MenuItem("Create new database");
		newDb.setOnAction(event -> {

			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DB files (*.db)", "*.db");
			fileChooser.getExtensionFilters().add(extFilter);
			File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
 
			if(file != null){
				try {
					Main.database.connectDB(file);
					accIdList.getItems().clear();
					expList.getItems().clear();
					accLabel.setText("Accessions");
					populateDetails(new ExpTable());
					updateDbInfoGrid();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		MenuItem fileQuit = new MenuItem("Exit");

		fileQuit.setOnAction(event -> {
			Main.database.closeConnection();
			System.exit(0);
		});		
		
		MenuItem selectDb = new MenuItem("Select database");

		selectDb.setOnAction(event -> {
			selectDataBase();
			ConfigCls.setMyProp("Connect_to_DB", Main.database.getDbPath());
			updateDbInfoGrid();
		});		
		
		MenuItem addFilesViaFile = new MenuItem("Select input File to add experiments");
		addFilesViaFile.setOnAction((ActionEvent event) -> {
			
			String filename = selectInputExpInput();
			
			if (filename != null) {						
		        final double wndwWidth = 800.0d;
		        Label updateLabel = new Label("reading file: " + filename);
		        updateLabel.setPrefWidth(wndwWidth);
		        ProgressBar progress = new ProgressBar();
		        progress.setPrefWidth(wndwWidth);
	
		        VBox updatePane = new VBox();
		        updatePane.setPadding(new Insets(10));
		        updatePane.setSpacing(5.0d);
		        updatePane.getChildren().addAll(updateLabel, progress);
	
		        Stage taskUpdateStage = new Stage(StageStyle.UTILITY);
		        taskUpdateStage.setScene(new Scene(updatePane));
		        taskUpdateStage.show();
		        disableGuiFct();

				Task<Void> longTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
					Main.database.addExperimentsFromFile(filename);
					Main.database.addCooForAllExps();
					return null;
					}
				};

				longTask.setOnSucceeded(WorkerStateEvent -> {
					mainApp.readInExps();
					expList.setItems(mainApp.getExpData());
					enableGuiFct();
					taskUpdateStage.hide();
					updateDbInfoGrid();
		        });
		        progress.progressProperty().bind(longTask.progressProperty());				        
		        taskUpdateStage.show();
		        new Thread(longTask).start();
		        				
		        Main.logger.info("input file read");
			}
		});
		
		menuFile.getItems().addAll(newDb, selectDb, addFilesViaFile, new SeparatorMenuItem(),  fileQuit);

		Menu changeLogLevel = new Menu("Edit Log Level");
//		List<String> logLevels = Stream.of(Level)
//                .map(Enum::name)
//                .collect(Collectors.toList());

		List<String> logLevels = Arrays.asList("OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER",
				"FINEST", "ALL");

		logLevels.forEach((choice) -> {
			MenuItem option = new MenuItem(choice);
			option.setOnAction(event -> {
				String before = InitGUI.logView.filterLevelProperty().toString();
				InitGUI.logView.filterLevelProperty().set(Level.parse(choice));
				Main.logger.info("changed log level from " + before + " to " +
						InitGUI.logView.filterLevelProperty().toString());
			});
			changeLogLevel.getItems().add(option);
		});

		MenuItem showDbStats = new MenuItem("Show database statistics");
		showDbStats.setOnAction( (ActionEvent event) -> {
			Alert info = new Alert(AlertType.INFORMATION);
			info.setTitle("statistics of " + Main.database.getDbName());
			info.setHeaderText(Main.database.getDbName() +" tables and tuples therein  ");
			StringBuilder aux = new StringBuilder();
			Main.database.dataBaseStats().forEach((String s) -> {
				s = s.replace("[","");
				s = s.replace("]","");
				s = s.replace(",",": ");
				aux.append(s).append("\n");
			});

			DecimalFormat df = new DecimalFormat("#.##");

			aux.append("\ndatabase size [MB]: ").append(String.valueOf(df.format(Main.database.getDbSize()))).
					append("\nmodification date:").append(Main.database.getDbDate()).append("\nfull path:\n").
					append(Main.database.getDbPath());

			info.setContentText(aux.toString());
			info.setResizable(true);
			info.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			info.initOwner(mainApp.getPrimaryStage());
			info.initModality(Modality.APPLICATION_MODAL);
			info.showAndWait();
		});

		MenuItem editSettings = new MenuItem("Edit System Settings");
		editSettings.setOnAction((ActionEvent event) -> {
			
			GridPane editSettingsPane = new GridPane(); 
			editSettingsPane.setPadding(new Insets(5, 5, 5, 5)); //margins around the whole grid
            //(top/right/bottom/left)
			
			editSettingsPane.setHgap(5);
			
			SplitPane container = new SplitPane();
			ArrayList<TextField> newSettings = new ArrayList<>();
			int i = 0;
			
			Properties res = ConfigCls.getMyPropList();
			
			Enumeration<?> e = res.propertyNames();
		    while (e.hasMoreElements()) {
		      String key = (String) e.nextElement();
		      
		      Label expProp = new Label(key);
		      editSettingsPane.add(expProp, 0, i);
		      
		      TextField newEntry = new TextField(res.getProperty(key));
		      newEntry.setPrefWidth(500);
		      newEntry.isEditable();
		      
		      newSettings.add(newEntry);
		      editSettingsPane.add(newEntry, 1, i);
		      i++;
		    }
			
		    ButtonBar bBar = new ButtonBar();
			Button okButton = new Button("Ok");
			ButtonBar.setButtonData(okButton, ButtonData.OK_DONE); 
			
			Button cancButton = new Button("Cancel");
			ButtonBar.setButtonData(cancButton, ButtonData.CANCEL_CLOSE); 
			
			Button saveButton = new Button("save to local file");
			ButtonBar.setButtonData(saveButton, ButtonData.LEFT);
			
			bBar.getButtons().addAll(saveButton, cancButton, okButton);
			bBar.setPadding(new Insets(5, 5, 5, 5));
			//bBar.getItems().addAll(defButton, saveButton, cancButton, okButton);
			
			container.setOrientation(Orientation.VERTICAL);
			container.getItems().addAll(editSettingsPane,bBar);
			
	    	Scene editScene = new Scene(container, 700, 300);
	    	//editSettingsStage.setScene(new Scene((Parent) wrappedSplit,700,280));
	    	Stage editSettingsStage = new Stage(StageStyle.UTILITY);
	    	editSettingsStage.setTitle("Edit Settings");
	    	editSettingsStage.setScene(editScene);
	    	
	    	editSettingsStage.show();
	    	editSettingsStage.setResizable(false);
	    	
	    	saveButton.setOnAction((ActionEvent subEvent) -> {
	    		Alert alert = new Alert(AlertType.CONFIRMATION);
	    		alert.setTitle("store settings into file?");
	    		alert.setHeaderText("store settings in " + ConfigCls.getFilePath());
	    		alert.setContentText("");
	    		alert.initOwner(editSettingsStage);
	    		alert.initModality(Modality.APPLICATION_MODAL);
	    		Optional<ButtonType> result = alert.showAndWait();
	    		
    			if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
    				int k = 0;
	    			Enumeration<?> e1 = res.propertyNames();
	    			while (e1.hasMoreElements()) {
	    				res.put(e1.nextElement(),newSettings.get(k).getText());
	    				k++;
    				}
	    			ConfigCls.setMyPropList(res);
    				ConfigCls.persistProps();
    			}
	    	});
	    	
	    	cancButton.setOnAction(subEvent -> editSettingsStage.close());
	    	okButton.setDefaultButton(true);
	    	okButton.setOnAction(subEvent -> {
    			int k = 0;
    			Enumeration<?> e1 = res.propertyNames();
    			while (e1.hasMoreElements()) {
    				res.put(e1.nextElement(), newSettings.get(k).getText());
    				k++;
				}
    			ConfigCls.setMyPropList(res);
    			editSettingsStage.close();
	    	});
		});

		menuEdit.getItems().addAll(editSettings,changeLogLevel);
		
		MenuItem aboutBratDb = new MenuItem("Abount BratDb Browser");
		aboutBratDb.setOnAction(event -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setHeaderText(null);
			alert.setContentText("Brat Database Browser v0.3");
			alert.showAndWait();

			// doda !!!
			//Main.database.addCooForAllExps();

		});
		menuHelp.getItems().addAll(aboutBratDb, showDbStats);
	}

	private String selectInputExpInput() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Input File for experiments");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		
		FileChooser.ExtensionFilter extFilter1 = 
                new FileChooser.ExtensionFilter("File lists", "*.txt");
		fileChooser.getExtensionFilters().addAll  (extFilter1);
		File inpSelect = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

		return ((inpSelect == null) ? null : inpSelect.getAbsolutePath());
	}
		
	private void selectDataBase() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Brat Database");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		
		FileChooser.ExtensionFilter extFilter1 = 
                new FileChooser.ExtensionFilter("BRAT Db files (*.db)", "*.db");
		FileChooser.ExtensionFilter extFilter2 = 
                new FileChooser.ExtensionFilter("All files", "*.*");
		
		fileChooser.getExtensionFilters().addAll  (extFilter1, extFilter2);
		File dbFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
		
		if (dbFile != null) {
			Main.logger.info("trying to connect to " + dbFile.getName() );	
			Main.database.closeConnection();

			try {
				Main.database.connectDB(dbFile);
				Main.logger.info("connected to " + dbFile.getAbsolutePath() );
				
				accIdList.getItems().clear();
				accLabel.setText("Accessions");
				geneHunterList.getItems().clear();
				populateDetails(new ExpTable());
									
				mainApp.readInExps();

				// if there are hunters present
				if (mainApp.readInAllDistinctHunter()) {
					this.geneHunterList.setItems(mainApp.getHunterTable());
					perspectiveSwitch.setDisable(false);
					hunterTab.setDisable(false);
				} else { // if not
					perspectiveSwitch.setSelected(false); // switch selected, rest is done from listener
					perspectiveSwitch.setDisable(true); // disable toggle button
					hunterTab.setDisable(true);	// disable detachable hunterTab
				}

				expList.setItems(mainApp.getExpData());

			} catch (SQLException ex) {
				Main.logger.severe(Main.StackTraceToString(ex));
			}
		}
	}
	
	private void updateDbInfoGrid() {
		DecimalFormat df = new DecimalFormat("#.##");
		dbSize.setText(String.valueOf(df.format(Main.database.getDbSize())));
		dbDate.setText(Main.database.getDbDate());
		dbShow.setText(Main.database.getDbName());
	}
		
	private void populateDbInfoGrid() {
		Label conTo = new Label("connected to database");
		Label modOn = new Label("modified on");
		Label fileSize = new Label("file size [MB]");
		
		updateDbInfoGrid();
		
		dbShow.setFont(Font.font(dbShow.getFont().getFamily(), FontPosture.ITALIC, 14));
		dbGridInfo.setPadding(new Insets(5, 5, 5, 5));
		
		dbGridInfo.add(conTo, 0, 0);
		dbGridInfo.add(dbShow, 0, 1);
		dbGridInfo.add(modOn, 1, 0);
		dbGridInfo.add(dbDate, 1, 1);
		dbGridInfo.add(fileSize, 2, 0);
		dbGridInfo.add(dbSize, 2, 1);		
	}
	
	private void accLookUp(HashSet<String> inputArgs) {
		
		HashSet<String> res = new HashSet<>();
		ListSelectionView<String> view = new ListSelectionView<>();
		((Label) view.getSourceHeader()).setText("found in database");
		((Label) view.getTargetHeader()).setText("choose to view");
		
        view.getSourceItems().addAll(inputArgs);
 
        GridPane pane = new GridPane();
        Button ok = new Button();
        ok.setText("Ok");
        
        pane.add(view, 0, 0);
        pane.add(ok, 0, 1);
        ok.setAlignment(Pos.CENTER);
       
        GridPane.setHalignment(ok, HPos.CENTER); // To align horizontally in the cell
        GridPane.setValignment(ok, VPos.CENTER); // To align vertically in the cell
        
        pane.setMinSize(400, 300);
        view.prefWidthProperty().bind(pane.widthProperty());
        view.prefHeightProperty().bind(pane.heightProperty());
   
        pane.setAlignment(Pos.CENTER);
        
        Stage accLookUpStage = new Stage();
        accLookUpStage.setTitle("machting accessions found");
		Scene scene = new Scene(pane);
        accLookUpStage.setScene(scene);
        accLookUpStage.show();
        
		ok.setOnAction(event -> {
			res.addAll(view.getTargetItems());
			try {
				mainApp.setExpList(Main.database.findallExpwithAccList(res));
				expList.setItems(mainApp.getExpData());				
			} catch (Exception ex) {
				Main.StackTraceToString(ex);
			}
			accLookUpStage.close();
        });        		
	}
	
	private static void bindTooltip(final Node node, final Tooltip tooltip){
	   node.setOnMouseMoved(event -> {
	         // +15 moves the tooltip 15 pixels below the mouse cursor;
	         // if you don't change the y coordinate of the tooltip, you
	         // will see constant screen flicker
	         tooltip.show(node, event.getScreenX(), event.getScreenY() + 15);
	   });  
	   node.setOnMouseExited(event -> tooltip.hide());
	}

	private void disableGuiFct() {
		mainStack.getChildren().get(0).toFront();
		rootMasker.setVisible(true);
		addExp.setDisable(true);
		lookUpButton.setDisable(true);
		resetButton.setDisable(true);
		menuBar.setDisable(true);
	}

	private void enableGuiFct() {
		mainStack.getChildren().get(mainStack.getChildren().size()-1).toBack();
		rootMasker.setVisible(false);
		addExp.setDisable(false);
		lookUpButton.setDisable(false);
		resetButton.setDisable(false);
		menuBar.setDisable(false);
	}

	private void customResize(TableView<?> view) {
		AtomicLong width = new AtomicLong();
		view.getColumns().forEach(col -> width.addAndGet((long) col.getWidth()));
		double tableWidth = view.getWidth();

		if (tableWidth > width.get()) {
			view.getColumns().forEach(col -> col.setPrefWidth(col.getWidth()+
					((tableWidth-width.get())/view.getColumns().size())));
		}
	}

	static class SearchCriterion {
		final SimpleStringProperty fieldNameProp = new SimpleStringProperty(this,"fieldName");
		final SimpleStringProperty filterTextProp = new SimpleStringProperty(this, "filterText");
		final SimpleBooleanProperty selectedProp = new SimpleBooleanProperty(this,"selected");

		SearchCriterion(String i_fieldName, String i_filterText, Boolean i_selected) {
			SetFieldName(i_fieldName);
			SetFilterText(i_filterText);
			SetSelected(i_selected);
		}

		SimpleStringProperty getFieldName() {
			return fieldNameProp;
		}

		SimpleStringProperty getFilter() {
			return filterTextProp;
		}

		SimpleBooleanProperty getSelected() {
			return selectedProp;
		}

		boolean isSelected() {return selectedProp.getValue();}

		private void SetFieldName(String fname) {
			this.fieldNameProp.set(fname);
		}

		private void SetFilterText(String fname) {
			this.filterTextProp.set(fname);
		}

		private void SetSelected(Boolean bselected) {
			this.selectedProp.set(bselected);
		}
	}

	private GridPane createEditor(TableRowExpanderColumn.TableRowDataFeatures<SearchCriterion> param) {

		SearchCriterion selectedField = param.getValue();
		GridPane editor = new GridPane();

		editor.setPadding(new Insets(10));
		editor.setHgap(10);
		editor.setVgap(5);

		TextField searchText = new TextField();
		CheckBox displayCheck = new CheckBox();
		displayCheck.setSelected(selectedField.getSelected().getValue());

		editor.addRow(0, new Label("Search"), searchText);
		editor.addRow(1, new Label("Display"), displayCheck);

		if ( Main.database.tableInfo(selectedField.getFieldName().getValue()).equals("String") ) {
			searchText.setPromptText("string like criteria");
		} else {
			searchText.setPromptText("numeric <, >, x - y");
		}

		Button saveButton = new Button("Ok");
		saveButton.setOnAction(event -> {
			selectedField.SetSelected(displayCheck.selectedProperty().get());

			if( searchText.getText().isEmpty() )
				searchText.setText("all");
			else {
				selectedField.SetFilterText(searchText.getText());
			}

			param.toggleExpanded();
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(event -> param.toggleExpanded());

		editor.addRow(2, saveButton, cancelButton);
		return editor;
	}
}

class MyEventHandler implements EventHandler{
	private final ExpTable exp;
	private final ArrayList<TextField> newMetas;
	private final ArrayList<String> expFields;
	private final Stage stage; //
	
	MyEventHandler(ExpTable exp, ArrayList<TextField> newMetas, ArrayList<String> expFields, Stage stage){
		this.exp=exp;
		this.newMetas=newMetas;
		this.expFields=expFields;
		this.stage=stage;
	}
	@Override
	public void handle(Event event) {
    	ClsSetterString<ExpTable> expTableSetter = new ClsSetterString<>();
    	ArrayList<String> newLabels = new ArrayList<>();

    	newMetas.forEach((item) -> newLabels.add(item.getText()));

		expTableSetter.setFields(exp,newLabels,expFields);	
		Main.database.updateExpTable(exp);

		stage.close();
	}
}