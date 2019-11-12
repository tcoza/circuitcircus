/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit.component;

import circuit.Units;
import circuit.SerializableDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author root
 */
public class LED extends Diode
{
	public static final String OVERLOAD = "overload";
	
	protected final SerializableDoubleProperty currentRating;
	protected final SerializableDoubleProperty lightHue;
	protected final SerializableDoubleProperty lightIntensity;
	
	public LED()
	{
		super();
		
		this.currentRating = new SerializableDoubleProperty(this, "current rating", Units.AMPS, 0.020);
		this.lightIntensity = new SerializableDoubleProperty(this, "light intensity", Units.PERCENT, 0.0);
		this.lightHue = new SerializableDoubleProperty(this, "light hue", Units.DEGREES, 120);
		this.lightHue.addListener(e -> this.tick(0));
		this.lightIntensity.setTransient(true);
		this.voltageDrop.set(2.0);
	}
	
	@Override
	public void tick(double timeElapsed)
	{
		super.tick(timeElapsed);
		
		this.lightIntensity.set(this.isBlocking() ? 0 :
				this.getPin(CATHODE).currentProperty().get() / this.currentRating.get() * 100);
		
		if (this.lightIntensity.get() > 100)
			this.stateOfOperation.set(LED.OVERLOAD);
		
		if (this.nodeGUI != null)
			((GraphicalNode)this.nodeGUI).update();
	}
	
	public class GraphicalNode extends Diode.GraphicalNode
	{
		private final Circle bulb;
		private final Color OVERLOAD = Color.YELLOW;
		
		public GraphicalNode()
		{
			super();
			
			final double RADIUS = 0.35;
			
			this.bulb = new Circle();
			this.bulb.centerXProperty().bind(this.widthProperty().divide(2));
			this.bulb.centerYProperty().bind(this.heightProperty().divide(2));
			this.bulb.radiusProperty().bind(this.widthProperty().multiply(RADIUS));
			this.bulb.setStroke(Color.BLACK);
	
			
			this.getChildren().add(this.bulb);
			this.bulb.toBack();
			this.update();
		}
		
		public void update()
		{
			double MIN_BRIGHTNESS = 30;
			
			double brightness = LED.this.lightIntensity.get();
			if (brightness < MIN_BRIGHTNESS)
				brightness = MIN_BRIGHTNESS;
			
			if (brightness <= 100)
				this.bulb.setFill(Color.hsb(LED.this.lightHue.get(), 1.0, brightness / 100));
			else
				this.bulb.setFill(OVERLOAD);
		}
	}
}
