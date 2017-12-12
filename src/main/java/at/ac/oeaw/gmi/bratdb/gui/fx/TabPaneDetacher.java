package at.ac.oeaw.gmi.bratdb.gui.fx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TabPaneDetacher {

    private TabPane tabPane;
    private Tab currentTab;
    private final List<Tab> originalTabs;
    private final Map<Integer, Tab> tapTransferMap;
    private final BooleanProperty alwaysOnTop;

    private TabPaneDetacher() {
        originalTabs = new ArrayList<>();
        tapTransferMap = new HashMap<>();
        alwaysOnTop = new SimpleBooleanProperty();
    }

    public static TabPaneDetacher create() {
        return new TabPaneDetacher();
    }

    private Boolean isAlwaysOnTop() {
        return alwaysOnTop.get();
    }

    public TabPaneDetacher makeTabsDetachable(TabPane tabPane) {
        this.tabPane = tabPane;
        originalTabs.addAll(tabPane.getTabs());
        for (int i = 0; i < tabPane.getTabs().size(); ++i) {
            tapTransferMap.put(i, tabPane.getTabs().get(i));
        }
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        tabPane.setOnDragDetected(
            (MouseEvent event) -> {
                if (event.getSource() instanceof TabPane) {
                    tabPane.getScene().getRoot().setOnDragOver((event1) -> {
                        event1.acceptTransferModes(TransferMode.ANY);
                        event1.consume();
                    });
                    currentTab = tabPane.getSelectionModel().getSelectedItem();
                    SnapshotParameters snapshotParams = new SnapshotParameters();
                    snapshotParams.setTransform(Transform.scale(0.4, 0.4));
                    WritableImage snapshot = currentTab.getContent().snapshot(snapshotParams, null);
                    Dragboard db = tabPane.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString("");
                    db.setDragView(snapshot, 40, 40);
                    db.setContent(clipboardContent);
                }
                event.consume();
            }
        );
        tabPane.setOnDragDone(
            (DragEvent event) -> {
                openTabInStage(currentTab);
                tabPane.setCursor(Cursor.DEFAULT);
                event.consume();
            }
        );
        return this;
    }

    private void openTabInStage(final Tab tab) {
        if(tab == null){
            return;
        }
        int originalTab = originalTabs.indexOf(tab);
        tapTransferMap.remove(originalTab);
        Node content = tab.getContent();
        if (content == null) {
            throw new IllegalArgumentException("Can not detach Tab '" + tab.getText() + "': content is empty (null).");
        }
        tab.setContent(null);
        final Scene scene = new Scene((Parent)content, tab.getTabPane().getWidth(), tab.getTabPane().getWidth());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(tab.getText());
        stage.setAlwaysOnTop(isAlwaysOnTop());
        Point2D p = MouseRobot.getMousePosition();
        stage.setX(p.getX());
        stage.setY(p.getY());
        stage.setOnCloseRequest((WindowEvent t) -> {
            stage.close();
            tab.setContent(content);
            int originalTabIndex = originalTabs.indexOf(tab);
            tapTransferMap.put(originalTabIndex, tab);
            int index = 0;
            SortedSet<Integer> keys = new TreeSet<>(tapTransferMap.keySet());
            for (Integer key : keys) {
                Tab value = tapTransferMap.get(key);
                if(!tabPane.getTabs().contains(value)){
                    tabPane.getTabs().add(index, value);
                }
                index++;
            }
            tabPane.getSelectionModel().select(tab);
        });
        stage.setOnShown((WindowEvent t) -> tab.getTabPane().getTabs().remove(tab));
        stage.show();
    }
}