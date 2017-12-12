package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.ac.oeaw.gmi.bratdb.app.ConfigCls;
import at.ac.oeaw.gmi.bratdb.app.Main;
import at.ac.oeaw.gmi.bratdb.gui.fx.InitGUI;
import at.ac.oeaw.gmi.bratdb.ormlite.ExpTable;
import at.ac.oeaw.gmi.bratdb.ormlite.SinglePhenTable;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class PicViewController {
	
	private double width, height, rectInitX, rectInitY, rectInitH, rectInitW;
    private String roiId;
    
    private InitGUI mainApp;

    private final Group g = new Group();
	
	private final Double scaleFac = 8.0;
	private Stage picStage;
	
	private final Rectangle rect = new Rectangle();

	@FXML
	private VBox container;

	private final ImgViewZoomable imgViewZoom = new ImgViewZoomable();
	private final StackPane stackPane = new StackPane();
			
	@FXML
	private void initialize() {
	}

    // reset to the top left:
   private void reset(ImageView imageView, double width, double height) {
        imageView.setViewport(new Rectangle2D(0, 0, width, height));    
   }
	
   private void focus(ImageView imageView) {
	   Main.logger.info("focusing ROI " + this.roiId);
	   		   	
	   	double initW = width/ scaleFac;  // viewport def W = pic original dim / scaleFac
	   	double initH = height/ scaleFac;	   	
	   	
	   	// in case that rectangle height is bigger than def. viewport height
	   	if (rectInitH > initH) {	//   	98
	   		
	   		// set viewport height to rectInitH + 20 
	   		// and rescale initW accordingly 
	   		initW *= (rectInitH+20)/ initH;
	   		initH = (rectInitH+20);

			Main.logger.info("increased viewport to fit...");
	   	}
	   		   	
	   	double x0 = rectInitX - (initW - rectInitW)/2.0;
	   	double y0 = rectInitY - (initH - rectInitH)/2.0;
	   	
	   	x0 = clamp(x0, width - initW);
	   	y0 = clamp(y0, height - initH);
	   	
	   	imageView.setViewport(new Rectangle2D(x0,y0,initW,initH));	  
   }
   
	public PicViewController() {
    }
		
	public void setStage(Stage myStage) {
    	this.picStage = myStage;
    }
	
	public void setMainApp(InitGUI mainApp) {
    	this.mainApp = mainApp;
    }	

	public void populateView(SinglePhenTable showMe, ExpTable exp) {
		
		Integer offSet;
		
		try {
			offSet = Integer.parseInt(ConfigCls.getMyProp("ImageView_Rectangle_Padding"));
		} catch (NumberFormatException e) {
			offSet = 10;
			Main.logger.info("padding value for rectangle not set, using "+ offSet +" pixel...");
		}
		
		this.roiId = String.valueOf(showMe.roi);
		
		Path picPath = Paths.get(ConfigCls.getMyProp("mount_path"),exp.getPicPath(),
				ConfigCls.getMyProp("pic_prefix")+showMe.core_filename + ".jpg");
		Main.logger.info(picPath.toString());
		Image mypic = new Image("file:"+picPath);
			
		if (mypic.isError()) {
			mainApp.showMdPane();

			if (ConfigCls.getMyProp("pic_prefix").equals(""))
				Main.logger.severe("pic_prefix property not set => check settings ");
			if (!Files.exists(Paths.get(ConfigCls.getMyProp("mount_path"),exp.getPicPath()))) {
				Main.logger.severe("cannot reach directory " + Paths.get(ConfigCls.getMyProp("mount_path"),
						exp.getPicPath()));
			}

			Main.logger.severe("unable to open " + picPath);
        	return;
		}
		
        width = mypic.getWidth();
        height = mypic.getHeight();

		imgViewZoom.activateZoom(width, height);
		imgViewZoom.setImage(mypic);

		g.getChildren().add(imgViewZoom);

		if (offSet > 0) {

			rectInitH = (showMe.gety2() - showMe.gety1()) + 2 * offSet;
			rectInitW = (showMe.getx2() - showMe.getx1()) + 2 * offSet;

			rectInitX = showMe.getx1() - offSet;
			rectInitY = showMe.gety1() - offSet;

			rect.setHeight(rectInitH);
			rect.setWidth(rectInitW);
			rect.setX(rectInitX);
			rect.setY(rectInitY);

			rect.setFill(null);
			rect.setStrokeWidth(8);
			rect.setStroke(Color.RED);

			rect.setArcHeight(30);
			rect.setArcWidth(30);

			g.getChildren().add(rect);

		} else {
			rectInitH = (showMe.gety2() - showMe.gety1()) + 20;
			rectInitW = (showMe.getx2() - showMe.getx1()) + 20;

			rectInitX = showMe.getx1() - 10;
			rectInitY = showMe.gety1() - 10;
		}

        imgViewZoom.setImage(g.snapshot(new SnapshotParameters(), null));
        imgViewZoom.setPreserveRatio(true);
        
        // initial setting size; call stage.show method before focusing otherwise
        // imageView dimensions get messed up !!!
        imgViewZoom.setViewport(new Rectangle2D(0,0,width/scaleFac, height/scaleFac));
       				                       
        HBox buttons = createButtons(width, height, imgViewZoom);
        Tooltip tooltip = new Tooltip("Scroll to zoom, drag to pan");
        Tooltip.install(imgViewZoom, tooltip);
        
        imgViewZoom.fitWidthProperty().bind(stackPane.widthProperty());
        imgViewZoom.fitHeightProperty().bind(stackPane.heightProperty());
       
        stackPane.getChildren().add(imgViewZoom);
        
        container.getChildren().addAll(stackPane,buttons);
        container.setFillWidth(true);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
                              
        picStage.setTitle(showMe.core_filename + ".jpg" + " (" + width + " x " + height + ")");
        picStage.setResizable(false);
        picStage.show();
        focus(imgViewZoom);
	}
	
    private double clamp(double value, double max) {
        if (value < 0.0)
            return 0.0;
        if (value > max)
            return max;
        return value;
    }

    private HBox createButtons(double width, double height, ImageView imageView) {
        Button focus = new Button("Focus ROI " + this.roiId);
        focus.setOnAction(e -> focus(imageView));
        Button full = new Button("Show full Plate");
        Button toClipboard = new Button("Copy to Clipboard");
        toClipboard.setOnAction(e -> imgViewZoom.getViewportContent() );
        full.setOnAction(e -> reset(imageView, width, height));
        HBox buttons = new HBox(10, focus, full, toClipboard);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));
        return buttons;
    }
    
//    private void bringOnTop() {
//    	ObservableList<Node> childs = stackPane.getChildren();
//    	if (childs.size() > 1) {
//    		Node topNode = childs.get(childs.size() -1);
//    		topNode.toBack();
//    		InitGUI.logger.info("topped");
//    	}
//    }
}
