/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit.component;

import circuit.Units;
import circuit.Breadboard;
import circuit.SerializableDoubleProperty;
import circuit.SerializableStringProperty;
import circuit.Tickable;
import javafx.beans.value.ChangeListener;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import circuit.ComponentGUI;

/**
 *
 * @author root
 */
public class Diode extends CircuitElement implements Tickable, ComponentGUI
{
	public static final int ANODE = 0;
	public static final int CATHODE = 1;
	
	public static final String FW_BIASED = "forward biased";
	public static final String RV_BIASED = "reverse biased";
	
	protected final SerializableDoubleProperty voltageDrop;
	protected final SerializableStringProperty stateOfOperation;
	
	public Diode()
	{
		this.voltageDrop = new SerializableDoubleProperty(this, "voltage drop", Units.VOLTS, 0.7);
		this.stateOfOperation = new SerializableStringProperty(this, "state of operation", "");
		this.stateOfOperation.setTransient(true);
		this.blocking = true;
	}
	
	private boolean blocking;
	public boolean isBlocking() { return blocking; }
	
	@Override
	public ElementInfo getInfo(int pin1, int pin2)
	{
		if (blocking)
			return new ElementInfo(Breadboard.BIG * this.voltageDrop.get(), ElementInfo.TYPE.RESISTANCE);
		else
			return new ElementInfo(voltageDrop.get() * (pin1 == pin2 ? 0 : pin2 == ANODE ? +1 : -1), ElementInfo.TYPE.VOLTAGE);
	}
	
	@Override
	public void tick(double timeElapsed)
	{
		blocking = blocking ?
				this.getPin(CATHODE).voltageProperty().get() - this.getPin(ANODE).voltageProperty().get() >= -voltageDrop.get() :
				this.getPin(CATHODE).currentProperty().get() <= 0;
		this.stateOfOperation.set(blocking ? RV_BIASED : FW_BIASED);
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(Diode.this);
			
			this.pinsGUI[0] = this.getComponent().getPin(Diode.ANODE);
			this.pinsGUI[1] = null;
			this.pinsGUI[2] = this.getComponent().getPin(Diode.CATHODE);
			this.pinsGUI[3] = null;
			
			Line terminals = new Line();
			terminals.startXProperty().bind(this.widthProperty().divide(2));
			terminals.startYProperty().set(0);
			terminals.endXProperty().bind(this.widthProperty().divide(2));
			terminals.endYProperty().bind(this.heightProperty());
			
			final double DIODE_WIDTH = 0.4;
			final double DIODE_LENGTH = 0.3;
			
			Line blocker = new Line();
			blocker.startXProperty().bind(this.widthProperty().multiply((1-DIODE_WIDTH)/2));
			blocker.startYProperty().bind(this.heightProperty().multiply((1+DIODE_LENGTH)/2));
			blocker.endXProperty().bind(this.widthProperty().multiply((1+DIODE_WIDTH)/2));
			blocker.endYProperty().bind(this.heightProperty().multiply((1+DIODE_LENGTH)/2));
			
			Polygon triangle = new Polygon();
			
			ChangeListener<Number> listener = (e, o, n) ->
			{
				triangle.getPoints().clear();
				triangle.getPoints().add(this.getWidth()/2);						// Tip of triangle, X
				triangle.getPoints().add(this.getHeight()*(1+DIODE_LENGTH)/2);		// Tip of triangle, Y
				triangle.getPoints().add(this.getWidth()*(1-DIODE_WIDTH)/2);
				triangle.getPoints().add(this.getHeight()*(1-DIODE_LENGTH)/2);
				triangle.getPoints().add(this.getWidth()*(1+DIODE_WIDTH)/2);
				triangle.getPoints().add(this.getHeight()*(1-DIODE_LENGTH)/2);
			};
			
			this.widthProperty().addListener(listener);
			this.heightProperty().addListener(listener);
			
			this.getChildren().addAll(terminals, blocker, triangle);
		}
		
		@Override
		public void flipNode(boolean x, boolean y)
		{
			if (x && this.rotation % 2 == 1)
				this.rotateNode(2);
			if (y && this.rotation % 2 == 0)
				this.rotateNode(2);
		}
	}
}
