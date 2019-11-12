package circuit.component;

import circuit.Units;
import circuit.SerializableDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import circuit.ComponentGUI;

public class VoltageSource extends CircuitElement implements ComponentGUI
{
	/** Pin number with positive voltage relative to ground */
	public static final int POSITIVE_TERM = 1;
	/** Ground */
	public static final int NEGATIVE_TERM = 0;
	
	/** Voltage of source, in volts */
	protected final SerializableDoubleProperty voltage;
	
	public VoltageSource()
	{
		this.voltage = new SerializableDoubleProperty(this, "voltage", Units.VOLTS, 12);
	}

	/**
	 * Returns voltage across pin1 and pin2
	 * @param pin1
	 * @param pin2
	 * @return 
	 */
	@Override
	public ElementInfo getInfo(int pin1, int pin2)
	{
		if (pin1 == pin2)
			return new ElementInfo(0, ElementInfo.TYPE.VOLTAGE);
		else
			return new ElementInfo(voltage.get() * ((pin1 == NEGATIVE_TERM) ? +1 : -1), CircuitElement.ElementInfo.TYPE.VOLTAGE);
	}
	
	@Override
	public String getSimpleName() { return "Voltage Source"; }
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(VoltageSource.this);
			
			this.pinsGUI[0] = this.getComponent().getPin(VoltageSource.POSITIVE_TERM);
			this.pinsGUI[1] = null;
			this.pinsGUI[2] = this.getComponent().getPin(VoltageSource.NEGATIVE_TERM);
			this.pinsGUI[3] = null;
			
			final double RADIUS = 0.3;			// Radius of circle relative to side
			
			Circle c = new Circle();
			c.centerXProperty().bind(this.widthProperty().divide(2));
			c.centerYProperty().bind(this.heightProperty().divide(2));
			c.radiusProperty().bind(this.heightProperty().multiply(RADIUS));
			c.setFill(Color.WHITE);
			c.setStroke(Color.BLACK);
			
			Line terminals = new Line();
			terminals.startXProperty().bind(c.centerXProperty());
			terminals.startYProperty().set(0);
			terminals.endXProperty().bind(c.centerXProperty());
			terminals.endYProperty().bind(this.heightProperty());
			
			final double SIGN_POSITION = 0.15;		// Distance from center of signs to center of circle relative to side
			final double SIGN_SIZE = 0.05;			// Length of half the minus sign
			
			Line plus1 = new Line();
			plus1.startXProperty().bind(c.centerXProperty());
			plus1.startYProperty().bind(c.centerYProperty().subtract(this.heightProperty().multiply(SIGN_POSITION)).subtract(this.heightProperty().multiply(SIGN_SIZE)));
			plus1.endXProperty().bind(c.centerXProperty());
			plus1.endYProperty().bind(c.centerYProperty().subtract(this.heightProperty().multiply(SIGN_POSITION)).add(this.heightProperty().multiply(SIGN_SIZE)));
			
			Line plus2 = new Line();
			plus2.startXProperty().bind(c.centerXProperty().subtract(this.widthProperty().multiply(SIGN_SIZE)));
			plus2.startYProperty().bind(c.centerYProperty().subtract(this.heightProperty().multiply(SIGN_POSITION)));
			plus2.endXProperty().bind(c.centerXProperty().add(this.widthProperty().multiply(SIGN_SIZE)));
			plus2.endYProperty().bind(c.centerYProperty().subtract(this.heightProperty().multiply(SIGN_POSITION)));
			
			Line minus = new Line();
			minus.startXProperty().bind(c.centerXProperty().subtract(this.widthProperty().multiply(SIGN_SIZE)));
			minus.startYProperty().bind(c.centerYProperty().add(this.heightProperty().multiply(SIGN_POSITION)));
			minus.endXProperty().bind(c.centerXProperty().add(this.widthProperty().multiply(SIGN_SIZE)));
			minus.endYProperty().bind(c.centerYProperty().add(this.heightProperty().multiply(SIGN_POSITION)));
			
			this.getChildren().addAll(terminals, c, plus1, plus2, minus);
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
