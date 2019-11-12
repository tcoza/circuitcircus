package circuit.component;

import circuit.Units;
import circuit.SerializableDoubleProperty;
import circuit.Tickable;
import javafx.scene.shape.Line;
import circuit.ComponentGUI;


/**
 * Ideal capacitor component
 * @author root
 */
public class Capacitor extends CircuitElement implements ComponentGUI, Tickable
{
	/** Pin number for positive plate */
	public static final int POSITIVE_PLATE = 1;
	/** Pin number for negative plate */
	public static final int NEGATIVE_PLATE = 0;
	
	/** Capacitance of capacitor, in Farads (Coulombs/Volt) */
	protected final SerializableDoubleProperty capacitance;
	/** Charge on capacitor at a certain moment */
	protected final SerializableDoubleProperty charge;
	
	public Capacitor()
	{
		this.capacitance = new SerializableDoubleProperty(this, "capacitance", Units.FARADS, 0.1);
		this.charge = new SerializableDoubleProperty(this, "charge", Units.COULOMBS);
		this.charge.setTransient(true);
	}
	
	private double prevCurrent = 0;
	/**
	 * adds the appropriate amount of charge based on timeElapsed and current through capacitor
	 * @param timeElapsed 
	 */
	@Override
	public void tick(double timeElapsed)
	{
		this.charge.set(
				this.charge.get() +
				(prevCurrent +
				(prevCurrent = this.getPin(NEGATIVE_PLATE).currentProperty().get())) / 2			// Trapezoidal approximation
				* timeElapsed);
		
		if (!Double.isFinite(this.charge.get()))
			this.charge.set(0);
	}
	
	/**
	 * Returns ElementInfo voltage from pin1 to pin2
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
			return new ElementInfo(
					this.charge.get() / this.capacitance.get() *
					((pin2 == POSITIVE_PLATE) ? +1 : -1),
					ElementInfo.TYPE.VOLTAGE);
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(Capacitor.this);
			
			pinsGUI[0] = this.getComponent().getPin(Capacitor.POSITIVE_PLATE);
			pinsGUI[1] = null;
			pinsGUI[2] = this.getComponent().getPin(Capacitor.NEGATIVE_PLATE);
			pinsGUI[3] = null;
			
			final double PLATE_SEPARATION = 0.15;
			final double PLATE_LENGTH = 0.6;
			
			Line term1 = new Line();
			term1.startXProperty().bind(this.widthProperty().divide(2));
			term1.startYProperty().set(0);
			term1.endXProperty().bind(this.widthProperty().divide(2));
			term1.endYProperty().bind(this.heightProperty().multiply((1 - PLATE_SEPARATION) / 2));
			
			Line plate1 = new Line();
			plate1.startXProperty().bind(this.widthProperty().multiply((1 - PLATE_LENGTH) / 2));
			plate1.startYProperty().bind(this.heightProperty().multiply((1 - PLATE_SEPARATION) / 2));
			plate1.endXProperty().bind(this.widthProperty().multiply((1 + PLATE_LENGTH) / 2));
			plate1.endYProperty().bind(this.heightProperty().multiply((1 - PLATE_SEPARATION) / 2));
			
			Line plate2 = new Line();
			plate2.startXProperty().bind(this.widthProperty().multiply((1 - PLATE_LENGTH) / 2));
			plate2.startYProperty().bind(this.heightProperty().multiply((1 + PLATE_SEPARATION) / 2));
			plate2.endXProperty().bind(this.widthProperty().multiply((1 + PLATE_LENGTH) / 2));
			plate2.endYProperty().bind(this.heightProperty().multiply((1 + PLATE_SEPARATION) / 2));
			
			Line term2 = new Line();
			term2.startXProperty().bind(this.widthProperty().divide(2));
			term2.startYProperty().bind(this.heightProperty().multiply((1 + PLATE_SEPARATION) / 2));
			term2.endXProperty().bind(this.widthProperty().divide(2));
			term2.endYProperty().bind(this.heightProperty());
			
			this.getChildren().addAll(term1, term2, plate1, plate2);
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
