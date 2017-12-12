package at.ac.oeaw.gmi.bratdb.gui.fx.controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

class ImgViewZoomable extends ImageView {

	private final ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();
	private static final int MIN_PIXELS = 10;
	private double width, height;

	ImgViewZoomable() {
		super();
	}
	
	void getViewportContent() {
		final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        
        content.putImage(this.snapshot(null, null));
        clipboard.setContent(content);
	}
	
	void activateZoom(double width, double height ) {
		
		this.width = width;
		this.height = height;
		
		setOnMousePressed(e -> {            
			Point2D mousePress = imageViewToImage(this, new Point2D(e.getX(), e.getY()));
			mouseDown.set(mousePress);
		});
		
	   setOnMouseDragged(e -> {
	    	Point2D dragPoint = imageViewToImage(this, new Point2D(e.getX(), e.getY()));
	    	shift(this, dragPoint.subtract(mouseDown.get()));
	    	mouseDown.set(imageViewToImage(this, new Point2D(e.getX(), e.getY())));
	    });
	   
	    setOnScroll(e -> {
	        double delta = e.getDeltaY();
	        Rectangle2D viewport = this.getViewport();
	        
	        double scale = clamp(Math.pow(1.01, delta),
	
	            // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
	            Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),
	
	            // don't scale so that we're bigger than image dimensions:
	            Math.max(width / viewport.getWidth(), height / viewport.getHeight())                             
	        );
       
	        Point2D mouse = imageViewToImage(this, new Point2D(e.getX(), e.getY()));
	
	        double newWidth = viewport.getWidth() * scale;
	        double newHeight = viewport.getHeight() * scale;
	       
	        // To keep the visual point under the mouse from moving, we need
	        // (x - newViewportMinX) / (x - currentViewportMinX) = scale
	        // where x is the mouse X coordinate in the image
	
	        // solving this for newViewportMinX gives
	
	        // newViewportMinX = x - (x - currentViewportMinX) * scale 
	
	        // we then clamp this value so the image never scrolls out
	        // of the imageview:
	
	        double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale, 
	                0, width - newWidth);
	        double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale, 
	                0, height - newHeight);
	      
	        setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));           
	    });
	}
	
	private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
		double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();
		
		Rectangle2D viewport = imageView.getViewport();
		return new Point2D(
		          viewport.getMinX() + xProportion * viewport.getWidth(), 
		          viewport.getMinY() + yProportion * viewport.getHeight());
	}	
	
	private void shift(ImageView imageView, Point2D delta) {
		Rectangle2D viewport = imageView.getViewport();

		//double width = imageView.getImage().getWidth() ;
		//double height = imageView.getImage().getHeight() ;

		double maxX = width - viewport.getWidth();
		double maxY = height - viewport.getHeight();
  
		double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
		double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

		imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
	}
	
	private double clamp(double value, double min, double max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}
}