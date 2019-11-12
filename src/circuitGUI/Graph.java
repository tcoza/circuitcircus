
package circuitGUI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Graph extends Stage
{
	/** Scales in px/u, origins in u */
	public final DoubleProperty originX, originY, scaleX, scaleY;
	private Pane mainPane = new Pane();
	private final Animation update;
	private long startTime;
	
	public List<DoubleProperty> monitoredProperties = new ArrayList<>();
	
	public Graph(Sandbox sandbox, DoubleProperty... properties)
	{
		this.scaleX = new SimpleDoubleProperty(20);
		this.scaleY = new SimpleDoubleProperty(1);
		this.originX = new SimpleDoubleProperty(0);
		this.originY = new SimpleDoubleProperty(1);
		
		for (DoubleProperty p : properties)
			monitoredProperties.add(p);
		
		this.scaleY.addListener(e ->
		{
			mainPane.getChildren().removeIf(n -> (n instanceof Shape && Color.GRAY.equals(((Shape)n).getStroke())));
			
			final double LINE_SPACING = 40;
			final double start = (originY.get() * scaleY.get()) % LINE_SPACING;
			final int numberOfLines = (int)(this.getHeight() / LINE_SPACING) + 1;
			
			for (int i = 0; i < numberOfLines; i++)
			{
				Line l = new Line();
				l.startXProperty().set(0);
				l.startYProperty().set(start + LINE_SPACING * i);
				l.endXProperty().bind(this.widthProperty());
				l.endYProperty().set(start + LINE_SPACING * i);
				l.setStroke(Color.GRAY);
				mainPane.getChildren().add(l);
				l.toBack();
				
				Text t = new Text(String.format("%.3g", originY.get() - l.getStartY() / scaleY.get()));
				t.setX(0);
				t.setY(l.getStartY());
				t.setStroke(Color.GRAY);
				mainPane.getChildren().add(t);
				
			}
		});
		
		this.update = new Timeline(new KeyFrame(Duration.millis(100), e ->
		{
			double[] points = new double[this.monitoredProperties.size()];
			int i = 0;
			for (DoubleProperty property : this.monitoredProperties)
				points[i++] = property.get();
			this.addPoint((System.currentTimeMillis() - startTime) / 1000d, points);
		}));
		this.update.setCycleCount(Animation.INDEFINITE);
		
		mainPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		mainPane.prefWidthProperty().bind(this.widthProperty());
		mainPane.prefHeightProperty().bind(this.heightProperty());
		
		this.setScene(new Scene(mainPane, 500, 200));
		this.initOwner(sandbox);
		this.setTitle("Untitled Graph " + (sandbox.graphs.size() + 1));
		
		this.setOnShown(e -> sandbox.graphs.add(this));
		this.setOnHidden(e -> sandbox.graphs.remove(this));
		
		ContextMenu contextMenu = new ContextMenu();
		MenuItem setName = new MenuItem("Set Graph Name...");
		setName.setOnAction(e ->
		{
			String newTitle = InputBox.show(this, this.getTitle(), "Set Graph Name");
			if (newTitle != null)
				this.setTitle(newTitle);
		});
		contextMenu.getItems().add(setName);
		
		mainPane.setOnContextMenuRequested(e -> contextMenu.show(this, e.getScreenX(), e.getScreenY()));
		
		this.play();
	}
	
	public void play()
	{
		if (this.update.getStatus().equals(Animation.Status.STOPPED))
			this.startTime = System.currentTimeMillis();
		this.update.play();
	}
	
	public void pause() { this.update.pause(); }
	public void stop() { this.update.stop(); this.values.clear(); mainPane.getChildren().clear(); }
	
	private Color[] colors = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW };
	
	private LinkedList<double[]> values = new LinkedList<>();
	public void addPoint(double x, double... ys)
	{
		if (values.isEmpty())
			values.add(new double[1]);
		
		while (!mainPane.getChildren().isEmpty() &&
				mainPane.getChildren().get(0) instanceof Line &&
				((Line)mainPane.getChildren().get(0)).getStartX() < 0)
			mainPane.getChildren().remove(0);
		
		for (int i = 0; i < ys.length; i++)
		{
			int index = values.size() - 1;
			if (values.get(index).length <= i+1)
				continue;
			
			double y = ys[i];
			
			Line l = new Line();
			l.setStroke(colors[i]);
			
			l.startXProperty().bind(originX.negate().add(values.get(index)[0]).multiply(scaleX));
			l.startYProperty().bind(originY.subtract(values.get(index)[i+1]).multiply(scaleY));
			l.endXProperty().bind(originX.negate().add(x).multiply(scaleX));
			l.endYProperty().bind(originY.subtract(y).multiply(scaleY));
			
			mainPane.getChildren().add(l);
		}
		
		double[] nextEntry = new double[ys.length + 1];
		for (int i = 1; i < nextEntry.length; i++)
			nextEntry[i] = ys[i-1];
		nextEntry[0] = x;
		
		values.add(nextEntry);
		
		while (!values.isEmpty() && values.get(0)[0] < originX.get())
			values.remove(0);
		
		originX.set(nextEntry[0] - mainPane.getWidth() / scaleX.get());
		if (originX.get() < 0) originX.set(0);
		originY.set(getMaxY());
		double range = originY.get() - getMinY();
		scaleY.set(mainPane.getHeight() / (range == 0 ? 1 : range));
	}
	
	private double getMaxY()
	{
		double max = 0;
		for (double[] a : values)
			for (int i = 1; i < a.length; i++)
				if (a[i] > max)
					max = a[i];
		return max;
	}
	
	private double getMinY()
	{
		double min = 0;
		for (double[] a : values)
			for (int i = 1; i < a.length; i++)
				if (a[i] < min)
					min = a[i];
		return min;
	}
}
