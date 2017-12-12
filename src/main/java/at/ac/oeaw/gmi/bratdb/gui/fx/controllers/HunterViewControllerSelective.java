package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import at.ac.oeaw.gmi.bratdb.app.ConfigCls;
import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.GeneHunterTable;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.table.TableFilter;

import java.util.ArrayList;
import java.util.List;

public class HunterViewControllerSelective {

    private InitGUI mainApp;

    @FXML
    private HBox hBoxSpacer;

    @FXML
    private Button csvExpButton, rButton;

    @FXML
    private ChoiceBox<String> selectProp;

    @FXML
    private TableView<GeneHunterTable> hunterList;

    @FXML
    private void initialize() {

        hunterList.setOnMouseClicked(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                if (e.getTarget() instanceof TableColumnHeader ) {
                    e.consume();
                } else {
                    String target = hunterList.getSelectionModel().getSelectedItem().target_AGI;
                    target = target.replace("ID=","");

                    if (target.startsWith("AT")) {

                        if (target.contains("."))
                            target = target.substring(0, target.indexOf("."));

                        HostServices services = mainApp.getHostServices();
                        String link = ConfigCls.getMyProp("AT_Gene_Hunter_Link") + target;
                        Main.logger.info("connecting to " + link);
                        services.showDocument(link);

                    } else if (target.startsWith("Lj")) {

                        if (target.contains("."))
                            target = target.substring(0, target.indexOf("."));

                        HostServices services = mainApp.getHostServices();
                        String link = ConfigCls.getMyProp("LJ_Gene_Hunter_Link") + target;
                        Main.logger.info("connecting to " + link);
                        services.showDocument(link);

                    } else {
                        mainApp.showMdPane();
                        Main.logger.severe("cannot open page " + ConfigCls.getMyProp("Gene_Hunter_Link")
                                + target);
                    }
                }
            }
        });

        csvExpButton.setOnAction(event -> {
            try {
                mainApp.writeCSV(this.hunterList.getItems());
            } catch (Exception e) {
                mainApp.showMdPane();
                Main.logger.severe("error");
            }
        });

        rButton.setOnAction(event -> {
            ObservableList<GeneHunterTable> huntTab = FXCollections.observableArrayList(this.hunterList.getItems());
            mainApp.showRCode(huntTab, hunterList.getItems().get(0).getHunterFile());
        });

        HBox.setHgrow(hBoxSpacer, Priority.ALWAYS);

        // tooltips
        hunterList.setTooltip(new Tooltip("double click to open browser link"));
        selectProp.setTooltip(new Tooltip("move focus to column"));
        csvExpButton.setTooltip(new Tooltip("export table contents to csv file \nshortcut: cmd + e"));
        rButton.setTooltip(new Tooltip("show sample code to import table contents into R \nshortcut: cmd + r"));

        Platform.runLater(() -> rButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.R,
                KeyCombination.SHORTCUT_DOWN), () -> rButton.fire()));

        Platform.runLater(() -> csvExpButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.E,
                KeyCombination.SHORTCUT_DOWN), () -> csvExpButton.fire()));

        Main.logger.info(" done");
    }


//    private void armThisButton(final Button button, KeyCode letter) {
//        button.getScene().getAccelerators().put(
//            new KeyCodeCombination(letter, KeyCombination.SHORTCUT_DOWN),
//            new Runnable() {
//            @Override public void run() {
//                button.fire();
//            }
//        });
//    }

    public void populateHunterListTab(List<GeneHunterTable> inGeneHunterTables, ArrayList<String> inSelectedFields ) {

        for (int i = 0; i< inSelectedFields.size(); ++i ) {
            final int k = i;
            TableColumn<GeneHunterTable, ?> col = new TableColumn<>(inSelectedFields.get(i));
            col.setCellValueFactory(cellData -> Main.database.getValueOfTableAsProperty(cellData.getValue(),
                    inSelectedFields.get(k)));
            hunterList.getColumns().add(col);
        }

        this.hunterList.setItems(FXCollections.observableArrayList(inGeneHunterTables));
        applyTableFilter();

        // moved here from former populateChoice() methode
        ObservableList<String> chList = FXCollections.observableArrayList(inSelectedFields);
        selectProp.setValue(chList.get(0));
        selectProp.setItems(chList);
        selectProp.getSelectionModel().selectedItemProperty().
                addListener((ObservableValue<? extends String> obs, String oldVal, String newVal) ->
                        scrollToColumn(chList.indexOf(newVal)));
    }

    private void applyTableFilter() {

        TableFilter.forTableView(hunterList).apply().setSearchStrategy((input,target) -> {
            try {
                if (input.startsWith("<")) {
                    double res = Double.parseDouble(input.substring(1));
                    double aux = Double.parseDouble(target);
                    return (aux < res);
                }

                if (input.startsWith(">")) {
                    double res = Double.parseDouble(input.substring(1));
                    double aux = Double.parseDouble(target);
                    return (aux > res);
                }

                if (input.contains("-")) {
                    String[] parts = input.split("-");
                    double aux = Double.parseDouble(target);
                    return Double.parseDouble(parts[0]) <= aux && Double.parseDouble(parts[1]) >= aux;
                }

                Main.logger.info(input);
                return target.matches( "(.*)" +input + "(.*)");
                // CUIDADO: captuando doto !!!
            } catch (Exception e) {
                return false;
            }
        });
    }

    private void scrollToColumn(int index) {
        hunterList.scrollToColumnIndex(index);
    }

    public void setMainApp(InitGUI mainApp) {
        this.mainApp = mainApp;
    }
}
