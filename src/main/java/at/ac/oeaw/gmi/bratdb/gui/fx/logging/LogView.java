package at.ac.oeaw.gmi.bratdb.gui.fx.logging;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.logging.LogRecord;
import java.util.logging.Level;

public class LogView extends ListView<LogRecord> {
    private static final int MAX_ENTRIES = 10_000;

    private final static PseudoClass finest = PseudoClass.getPseudoClass("finest");
    private final static PseudoClass finer  = PseudoClass.getPseudoClass("finer");
    private final static PseudoClass fine = PseudoClass.getPseudoClass("fine");
    private final static PseudoClass config = PseudoClass.getPseudoClass("config");
    private final static PseudoClass info = PseudoClass.getPseudoClass("info");
    private final static PseudoClass warning  = PseudoClass.getPseudoClass("warning");
    private final static PseudoClass severe = PseudoClass.getPseudoClass("severe");

    private final static SimpleDateFormat timestampFormatter = new SimpleDateFormat("HH:mm:ss.SSS");

    private final ObjectProperty<Level> filterLevel   = new SimpleObjectProperty<>(null);
    private final BooleanProperty       tail          = new SimpleBooleanProperty(true);

    private final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();

    public ObjectProperty<Level> filterLevelProperty() {
        return filterLevel;
    }

    public LogView(Log logger) {
        getStyleClass().add("log-view");

        Timeline logTransfer = new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                (ActionEvent event) -> {
                    if (logItems.size() > MAX_ENTRIES) {
                        logItems.remove(0, logItems.size() - MAX_ENTRIES);
                    }

                    //VirtualFlow virtualFlow = null;
                    for( Node node: getChildrenUnmodifiable()) {
                        if( node instanceof VirtualFlow) {
                            VirtualFlow virtualFlow = (VirtualFlow) node;
                            IndexedCell last = virtualFlow.getLastVisibleCellWithinViewPort();

                            if (last != null && (last.getIndex() == logItems.size()-1))
                                tail.setValue(true);
                            else
                                tail.setValue(false);
                        }
                    }

                    logger.drainTo(logItems);

                    if (tail.get()) {
                        scrollTo(logItems.size());
                    }
                }
            )
        );
        logTransfer.setCycleCount(Timeline.INDEFINITE);
        logTransfer.play();

        filterLevel.addListener((observable, oldValue, newValue) -> setItems(
            new FilteredList<>(logItems,logRecord -> logRecord.getLevel().intValue() >=
                    filterLevel.get().intValue()
            )
        ));
        filterLevel.set(Level.INFO);

        setCellFactory(param -> new ListCell<LogRecord>() {

            @Override
            protected void updateItem(LogRecord item, boolean empty) {
            super.updateItem(item, empty);

            pseudoClassStateChanged(finest, false);
            pseudoClassStateChanged(finer, false);
            pseudoClassStateChanged(fine, false);
            pseudoClassStateChanged(config, false);
            pseudoClassStateChanged(info, false);
            pseudoClassStateChanged(warning, false);
            pseudoClassStateChanged(severe, false);

            if (item == null || empty) {
                setText(null);
                return;
            }
//            String context = String.format("%s %s.%s@%d",item.getLevel(),item.getSourceClassName(),
//                    item.getSourceMethodName(),item.getThreadID());
            String timestamp = timestampFormatter.format(item.getMillis());
            setText(String.format("%s: %s %s",timestamp,item.getLevel(),item.getMessage()));

            switch (item.getLevel().getName()) {
                case "FINEST":
                    pseudoClassStateChanged(finest, true);
                    break;
                case "FINER":
                    pseudoClassStateChanged(finer, true);
                    break;
                case "FINE":
                    pseudoClassStateChanged(fine, true);
                    break;
                case "CONFIG":
                    pseudoClassStateChanged(config, true);
                    break;
                case "INFO":
                    pseudoClassStateChanged(info, true);
                    break;
                case "WARNING":
                    pseudoClassStateChanged(warning, true);
                    break;
                case "SEVERE":
                    pseudoClassStateChanged(severe, true);
                    break;
            }
            }
        });
    }
}