package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.j256.ormlite.dao.ForeignCollection;

import at.ac.oeaw.gmi.bratdb.app.ConfigCls;
import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.AccPhenTable;
import at.ac.oeaw.gmi.bratdb.ormlite.ExpTable;
import at.ac.oeaw.gmi.bratdb.ormlite.SinglePhenTable;
import javafx.beans.value.ObservableValue;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.table.TableFilter;

public class PhenViewController {

    private static ArrayList<String> caps, valuesNames;
    private static ObservableList<AccPhenTable> accPhenTable = FXCollections.observableArrayList();
    
    private AccPhenTable selAcc;
    private InitGUI mainApp;
    private String selectedAcc = null;
    
    @FXML
    private HBox hBoxSpacer;
    
    @FXML
    private Button getSingleButton, ratesButton, csvExpButton, rButton;
            
    @FXML
    private ChoiceBox<String> selectProp;
    
    @FXML
    private TableView<AccPhenTable> phenList;

    @FXML
    private void initialize() {
        
        // getting captions of table
        caps = Main.database.getTableCaptions(AccPhenTable.class);
        // getting variables names
        valuesNames = Main.database.getTableFieldNames(AccPhenTable.class);
        
        for (int i = 0; i< caps.size(); ++i ) {
            int k = i;
            TableColumn<AccPhenTable, ?> col = new TableColumn<>(caps.get(i)); //
            col.setCellValueFactory(cellData -> Main.database.getValueOfTableAsProperty(cellData.getValue(),
                    valuesNames.get(k)));
            phenList.getColumns().add(col);
        }

        phenList.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) -> selAcc = newVal);

        phenList.setOnMouseClicked(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                AccPhenTable toAnimate = phenList.getSelectionModel().getSelectedItem();
                if (toAnimate.timestep.equals("avg")) {
                    mainApp.showMdPane();
                    Main.logger.severe("nothing to show for averaged values");
                }
                else {
                    Main.logger.info("trying to create animation for " + toAnimate.getAcc());
                    createAnimation(toAnimate);
                }
            }
        });

        // 2702: duplicate of above => check usage !!!
        phenList.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                try {
                    Main.logger.info("fetching single values for accession " + selAcc.accId + " timestep " + selAcc.getTimestep());
                    mainApp.showSinglePhenView(selAcc);
                } catch (NullPointerException e) {
                    mainApp.showMdPane();
                    Main.logger.severe("No selection made for single values");
                }
            }
        });            

        csvExpButton.setOnAction(event -> {
            try {
                mainApp.writeCSV(accPhenTable);
            } catch (Exception e) {
                mainApp.showMdPane();
                Main.logger.severe("No selection made for single rates values");
            }
        });
        
        rButton.setOnAction(event -> mainApp.showRCode(accPhenTable, selectedAcc));                
        ratesButton.setOnAction(event -> {
            Main.logger.info("sel acc: " + selectedAcc);
            mainApp.showPhenRatesView(selectedAcc);
        });
        
        getSingleButton.setOnAction(event -> {
            try {
                if (selAcc.getSingleValues().size() > 0) {
                    Main.logger.info("fetching single values for accession " + selAcc.accId + " timestep " + selAcc.getTimestep());
                    mainApp.showSinglePhenView(selAcc);
                } else {
                    mainApp.showMdPane();
                    Main.logger.severe("empty table for single values");
                }
            } catch (NullPointerException e) {
                mainApp.showMdPane();
                Main.logger.severe("No selection made for single values");
            }
        });

        HBox.setHgrow(hBoxSpacer, Priority.ALWAYS);
        
        // tooltips
        selectProp.setTooltip(new Tooltip("move focus to column"));
        csvExpButton.setTooltip(new Tooltip("export table contents to csv file \nshortcut: cmd + e"));
        rButton.setTooltip(new Tooltip("show sample code to import table contents into R \nshortcut: cmd + r"));
        getSingleButton.setTooltip(new Tooltip("click to show single measurements \nshortcut: cmd + s"));
        ratesButton.setTooltip(new Tooltip("click to show measurements related to two subsequent timesteps \nshortcut: cmd + T"));
        
        // keyboard shortcuts
        Platform.runLater(() -> getSingleButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.S,
                KeyCombination.SHORTCUT_DOWN), () -> getSingleButton.fire()));

        Platform.runLater(() -> rButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.R,
                KeyCombination.SHORTCUT_DOWN), () -> rButton.fire()));

        Platform.runLater(() -> csvExpButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.E,
                        KeyCombination.SHORTCUT_DOWN), () -> csvExpButton.fire()));

        Platform.runLater(() -> ratesButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.T,
                        KeyCombination.SHORTCUT_DOWN), () -> ratesButton.fire()));

        Main.logger.info(" done");
    }
        
    public PhenViewController() {
    }
       
    // to show just the ONE selected accessions of experiment
    public void populatePhenAcc(String selectedAcc) {
        this.selectedAcc = selectedAcc;
        accPhenTable = FXCollections.observableArrayList(Main.database.getAccPhenOfExp(mainApp.getSelectedExp(),
                selectedAcc));
        this.phenList.setItems(accPhenTable);
        //applyTableFilter();
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
    
    // to show all accessions of experiment
    public void populatePhen() {
        accPhenTable = FXCollections.observableArrayList(Main.database.getPhenOfExp(mainApp.getSelectedExp()));    
        this.phenList.setItems(accPhenTable);
        //applyTableFilter();
    }

    private void applyTableFilter() {
        TableFilter.forTableView(phenList).apply().setSearchStrategy((input, target) -> {
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

                return target.matches( "(.*)" +input + "(.*)");
                // CUIDADO: captuando doto !!!
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    // drop down button
    public void populateChoice() {
        ObservableList<String> chList = FXCollections.observableArrayList(caps);
        selectProp.setValue(chList.get(0));
        selectProp.setItems(chList);
        selectProp.getSelectionModel().selectedItemProperty().
            addListener((ObservableValue<? extends String> obs, String oldVal, String newVal) ->
            scrollToColumn(chList.indexOf(newVal)));
    }
    
    private void scrollToColumn(int index) {
        phenList.scrollToColumnIndex(index);
    }
    
    public void setMainApp(InitGUI mainApp) {
        this.mainApp = mainApp;
    }
    
    private void createAnimation(AccPhenTable in) {

        int maxCol,i;
        List<Image> myImages = new ArrayList<>();
        List<String> timestepNames = new ArrayList<>();
        VBox container = new VBox();
        ImgViewZoomable imgView = new ImgViewZoomable();
        Map<String,List<String>> picMap = new LinkedHashMap<>();
        Map<String,List<Point>> picRectangles = new HashMap<>();

        final ExpTable exp = in.getExpTable();
        Path picPath;

        if (exp.getPicPath() != null)
            picPath = Paths.get(ConfigCls.getMyProp("mount_path"),exp.getPicPath());
        else {
            mainApp.showMdPane();
            Main.logger.severe("experiment pic path undefined");
            return;
        }

        if ((picPath.toFile()).exists()) {
            Main.database.refreshExpTable(exp);

            // getting all present timesteps for selected accession
            List<AccPhenTable> accTimeSteps = Main.database.getSameAccTimesteps(in);

            Integer displayWidth = 900;

            for (AccPhenTable acc : accTimeSteps) {
                ForeignCollection<SinglePhenTable> singles = acc.getSingleValues();
                Set<String> jpgFilesHash = new HashSet<>();

                for (SinglePhenTable sing : singles) {
                    try {
                        String aux = sing.core_filename;

                        // had to add "xxx.contains("_") for manually manipulated (hence useless) exps (some of claudias...)
                        if (aux.contains("_")) {
                            jpgFilesHash.add(aux);

                            // ok png version
                            Point locationPoint = new Point((sing.getx2() - sing.getx1()) / 2 + sing.getx1(), sing.gety1());

                            if (picRectangles.containsKey(aux)) {
                                picRectangles.get(aux).add(locationPoint);
                            } else {
                                picRectangles.put(aux, new ArrayList<>(Arrays.asList(locationPoint)));
                            }
                        }
                    } catch (NullPointerException e) {
                        Main.logger.info("pic file name found for " + sing.timestep + " " + sing.roi);
                    }
                }

                // ignoring emtpy timesteps and avgs
                if (jpgFilesHash.size() > 0) {
                    List<String> jpgFiles = new ArrayList<>(jpgFilesHash);
                    picMap.put(acc.timestep, jpgFiles);
                }
            }

            // retrieve the unique plate numbers from images to calc their fixed position on the screen during animation
            Set<String> plateHash = new HashSet<>();

            for (String timestep : picMap.keySet()) {
                timestepNames.add(timestep);

                for (String pic : picMap.get(timestep)) {
                    // getting plate nr
                    String str1 = pic.substring(pic.lastIndexOf("_"), pic.length());
                    plateHash.add(str1);
                }
            }

            // returning if empty
            if (plateHash.size() == 0) {
                mainApp.showMdPane();
                Main.logger.severe("no plate pictures found");
                return;
            }

            ///////////////////////////////////
            // calc fix position of the plate pix
            // every pic file name ends with _xxx plate number, to keep their order
            // trough out the animation all pix of same plate (but coming from different timesteps)
            // stay on one fixed position => stored in LinkedHashMap<String,List<Integer>>(), e.g.:
            // picPos<"_001"> <column, row>
            ///////////////////////////////////

            List<String> plates = new ArrayList<>(plateHash);
            Map<String, List<Integer>> picPos = new LinkedHashMap<>();

            maxCol = (int) Math.ceil(Math.sqrt((double) plateHash.size()));

            Integer maxRow = 0;
            Integer col = 0;

            for (String entry : plates) {
                picPos.put(entry, Stream.of(col, maxRow).collect(Collectors.toList()));
                col++;
                if (col == maxCol) {
                    col = 0;
                    maxRow++;
                }
            }

            // thats bad
            if (maxCol * maxRow < plates.size()) maxRow++;

            GridPane res = new GridPane();
            for (i = 0; i < maxCol; ++i) {
                ColumnConstraints colConst = new ColumnConstraints();
                colConst.setPercentWidth(100.0 / maxCol);
                res.getColumnConstraints().add(colConst);
            }
            for (i = 0; i < maxRow; ++i) {
                RowConstraints rowConst = new RowConstraints();
                rowConst.setPercentHeight(100.0 / maxRow);
                res.getRowConstraints().add(rowConst);
            }

            Scene dummy = new Scene(res);

//        ExecutorService myExecutor = Executors.newSingleThreadExecutor();
//        myExecutor.submit(() -> {
//            String threadName = Thread.currentThread().getName();
//            System.out.println(" Hi, i am " + threadName);
//        });
//        myExecutor.shutdown();

//        ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<>();
//
//        for (Map.Entry<String,List<String>> entry : picMap.entrySet()) {
//            map.put(entry.getKey(), picMap.get(entry.getKey()));
//        }
//
//        System.out.println(ForkJoinPool.getCommonPoolParallelism());

//        map.forEach(3, (key, value) -> {
//                value.forEach((a)-> System.out.println("this works"));
//                System.out.printf("key: %s; value: %s; thread: %s\n",
//                        key, value, Thread.currentThread().getName());
//                });

//        ExecutorService executor = Executors.newWorkStealingPool();

//        List<Callable<String>> callables = Arrays.asList(
//                () -> "task1",
//                () -> "task2",
//                () -> "task3");
//
//        try {
//            executor.invokeAll(callables)
//                    .stream()
//                    .map(future -> {
//                        try {
//                            return future.get();
//                        } catch (Exception e) {
//                            throw new IllegalStateException(e);
//                        }
//                    })
//                    .forEach(System.out::println);
//        } catch(InterruptedException e) {
//
//        }

            double factor = 1.0;

            Image okPng = new Image(getClass().getResourceAsStream("/check.png"));
            ImagePattern imagePattern = new ImagePattern(okPng);

            double tokenSize = Double.parseDouble(ConfigCls.getMyProp("Animation_token_size"));
            double offset = Double.parseDouble(ConfigCls.getMyProp("Animation_token_offset"));

            for (String key : picMap.keySet()) {
                for (String fName : picMap.get(key)) {

                    Path singlePath = Paths.get(picPath.toString(),
                            ConfigCls.getMyProp("pic_prefix") + fName + ".jpg");

                    Main.logger.info("fetching pic: " + singlePath.toString());
                    Group g = new Group();

                    try {
                        factor = (double) JpegProperties(new File(singlePath.toString())).getX() * maxCol / displayWidth;
                        g.getChildren().add(new ImageView(new Image("file:" + singlePath, displayWidth / maxCol,
                                0, true, false)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    for (Point rect : picRectangles.get(fName)) {
                        Circle circle = new Circle(rect.getX() / factor, rect.getY() / factor - offset, tokenSize);
                        circle.setFill(imagePattern);
                        g.getChildren().add(circle);
                    }

                    col = picPos.get(fName.substring(fName.lastIndexOf("_"), fName.length())).get(0);
                    int row = picPos.get(fName.substring(fName.lastIndexOf("_"), fName.length())).get(1);
                    res.add(g, col, row);
                }

                res.setStyle("-fx-background-color: black;");
                myImages.add(res.snapshot(new SnapshotParameters(), null));
                res.getChildren().clear();
            }

            Main.logger.info("found pics: " + myImages.size());

            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);

            final Slider picSlider = new Slider(0, myImages.size() - 1, 0);
            picSlider.setBlockIncrement(1.0);
            picSlider.setMajorTickUnit(1.0);
            picSlider.setMinorTickCount(0);
            picSlider.setShowTickMarks(true);
            picSlider.setSnapToTicks(true);

            Label timeStep = new Label();

            for (i = 0; i < myImages.size(); ++i) {
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(i * 1.0), Integer.toString(i),
                        new KeyValue(imgView.imageProperty(), myImages.get(i), Interpolator.DISCRETE),
                        new KeyValue(timeStep.textProperty(), "timestep: " + timestepNames.get(i)),
                        new KeyValue(picSlider.valueProperty(), i, Interpolator.DISCRETE)));
            }

            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(++i),
                    new KeyValue(imgView.imageProperty(), myImages.get(myImages.size() - 1), Interpolator.DISCRETE)));

            Main.logger.info(in.accId + " " + in.timestep + " from exp " + exp.getPicPath());

            imgView.setImage(myImages.get(0));
            imgView.setViewport(new Rectangle2D(0, 0, myImages.get(0).getWidth(), myImages.get(0).getHeight()));
            imgView.activateZoom(myImages.get(0).getWidth(), myImages.get(0).getHeight());

            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(imgView);

            imgView.fitWidthProperty().bind(stackPane.widthProperty());
            imgView.fitHeightProperty().bind(stackPane.heightProperty());

            ButtonBar buttonBar = new ButtonBar();

            Button startButton = new Button("Start");
            Button pauseButton = new Button("Pause");
            Button picExport = new Button("Export Pictures");
            Button closeButton = new Button("Close");

            pauseButton.setOnAction(e -> {
                timeline.pause();
                pauseButton.setDisable(true);
                startButton.setText("Resume");
                startButton.setDisable(false);
            });

            startButton.setDisable(true);

            startButton.setOnAction(e -> {
                pauseButton.setDisable(false);
                if (startButton.getText().equals("Resume")) {
                    startButton.setText("Start");
                    timeline.play();
                } else {
                    timeline.playFromStart();
                }
            });

            picExport.setOnAction(e -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select directory");
                File defaultDirectory = new File(System.getProperty("user.home"));
                directoryChooser.setInitialDirectory(defaultDirectory);
                File selectedDirectory = directoryChooser.showDialog(mainApp.getPrimaryStage());

                int count;
                if (selectedDirectory != null) {
                    String choice = selectedDirectory.getAbsolutePath();
                    count = 0;
                    for (String key : picMap.keySet()) {
                        for (String fName : picMap.get(key)) {

                            Path singlePath = Paths.get(picPath.toString(),
                                    ConfigCls.getMyProp("pic_prefix") + fName + ".jpg");

                            Main.logger.info("fetching pic: " + singlePath.toString());
                            Group g = new Group();

                            g.getChildren().add(new ImageView(new Image("file:" + singlePath)));

                            for (Point rect : picRectangles.get(fName)) {
                                Circle circle = new Circle(rect.getX(), rect.getY() - offset * 10, tokenSize * 10);
                                circle.setFill(imagePattern);
                                g.getChildren().add(circle);
                            }

                            String expName = choice + "/pic_" + count + "_" + exp.getName() + "_" + in.accId + "_timestep_" + key;
                            count++;
                            File newPic = new File(expName);

                            Image aux = g.snapshot(new SnapshotParameters(), null);
                            try {
                                ImageIO.write(SwingFXUtils.fromFXImage(aux, null), "png", newPic);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
                Main.logger.fine("done");
            });

            buttonBar.getButtons().addAll(timeStep, startButton, pauseButton, picExport, closeButton);
            buttonBar.setPadding(new Insets(5, 5, 5, 5));

            timeStep.setFont(Font.font(timeStep.getFont().getFamily(), FontWeight.BOLD,
                    FontPosture.ITALIC, timeStep.getFont().getSize()));

            ButtonBar.setButtonData(timeStep, ButtonData.LEFT);

            picSlider.valueProperty().addListener((ov, oldVal, newVal) -> {
                if (timeline.getStatus() != Status.RUNNING) timeline.jumpTo(Integer.toString(newVal.intValue()));
            });

            container.getChildren().addAll(stackPane, picSlider, buttonBar);

            Stage multiPicStage = new Stage(StageStyle.UTILITY);
            multiPicStage.setTitle("accession " + in.accId + " animation (" + myImages.size() + " timesteps)");
            multiPicStage.setScene(new Scene(container));
            //multiPicStage.sizeToScene();
            multiPicStage.show();

            timeline.play();

            multiPicStage.setResizable(false);

            closeButton.setOnAction(e -> {
                timeline.stop();
                multiPicStage.close();
            });
        } else {
            mainApp.showMdPane();
            Main.logger.severe("picture path not correctly set for experiment " + exp.getName());
        }
    }

   private Point JpegProperties(File file) throws IOException{
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));

            // check for "magic" header
            byte[] buf = new byte[2];
            int count = in.read(buf, 0, 2);
            if (count < 2) {
                throw new RuntimeException("Not a valid Jpeg file!");
            }
            if ((buf[0]) != (byte) 0xFF || (buf[1]) != (byte) 0xD8) {
                throw new RuntimeException("Not a valid Jpeg file!");
            }

            int width = 0;
            int height = 0;
            char[] comment;

            boolean hasDims = false;
            boolean hasComment = false;
            int ch = 0;

            while (ch != 0xDA && !(hasDims && hasComment)) {
                /* Find next marker (JPEG markers begin with 0xFF) */
                while (ch != 0xFF) {
                    ch = in.read();
                }
                /* JPEG markers can be padded with unlimited 0xFF's */
                while (ch == 0xFF) {
                    ch = in.read();
                }
                /* Now, ch contains the value of the marker. */

                int length = 256 * in.read();
                length += in.read();
                if (length < 2) {
                    throw new RuntimeException("Not a valid Jpeg file!");
                }
                /* Now, length contains the length of the marker. */

                if (ch >= 0xC0 && ch <= 0xC3) {
                    in.read();
                    height = 256 * in.read();
                    height += in.read();
                    width = 256 * in.read();
                    width += in.read();
                    for (int foo = 0; foo < length - 2 - 5; foo++) {
                        in.read();
                    }
                    hasDims = true;
                }
                else if (ch == 0xFE) {
                    // that's the comment marker
                    comment = new char[length-2];
                    for (int foo = 0; foo < length - 2; foo++)
                        comment[foo] = (char) in.read();
                    hasComment = true;
                }
                else {
                    // just skip marker
                    for (int foo = 0; foo < length - 2; foo++) {
                        in.read();
                    }
                }
            }
            return (new Point(width, height));

        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    Main.logger.severe("error reading jpg properties of " + file);
                }
            }
        }
    }
    
    private class Point {
    	final int x;
        final int y;
    	
    	Point(int x, int y) {
    		this.x = x;
    		this.y = y;
    	}
    	int getX() {
    		return this.x;
    	}
    	int getY() {
    		return this.y;
    	}
    }


    
//    private class picThread implements Runnable {
//
//    	private double factor;
//    	private Point dims = new Point();
//    	private Path picPath;
//
//    	public picThread(Path inPath) {
//    		this.picPath = inPath;
//    	}
//
//    	public Point getDim() {
//    		return dims;
//    	}
//
//    	public double getFactor() {
//    		return factor;
//    	}
//
//    	//  factor = new Image("file:"+singlePath).getWidth() * maxCol/ displayWidth;
//
//		@Override
//		public void run() {
//			factor = new Image("file:"+picPath).getWidth();
//			System.out.println("thread " + Thread.currentThread().getName());
//		}
    	
//    }
}

