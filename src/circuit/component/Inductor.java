package circuit.component;

import circuit.Units;
import circuit.Breadboard;
import circuit.SerializableDoubleProperty;
import circuit.Tickable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import circuit.ComponentGUI;


/**
 * Describes an ideal inductor
 * @author root
 */
public class Inductor extends CircuitElement implements Tickable, ComponentGUI
{
	/** Pin number with outgoing current when field is positive */
	public static final int CURRENT_SOURCE = 1;
	/** Pin number with ingoing current when field is negative */
	public static final int CURRENT_SINK = 0;
	
	public final ParallelWrapper<Inductor> wrapper;
	/** Inductance in Henrys (Webers/amp) */
	protected final SerializableDoubleProperty inductance;
	/** Magnetic field at a given time, in Webers */
	protected final SerializableDoubleProperty magneticField;
	
	public Inductor()
	{
		this.inductance = new SerializableDoubleProperty(this, "inductance", Units.HENRYS, 0.1);
		this.magneticField = new SerializableDoubleProperty(this, "magnetic field", Units.WEBERS, 0);
		this.wrapper = new ParallelWrapper<>(Inductor.this, inductance.multiply(Breadboard.BIG));
		this.magneticField.setTransient(true);
	}

	private double prevVoltage = 0;
	private static final double saturation = 5;
	
	/**
	 * Increments magnetic field appropriately, based on voltage and timeElapsed
	 * @param timeElapsed 
	 */
	@Override
	public void tick(double timeElapsed)
	{
		double currentVoltage = 
				this.getPin(CURRENT_SINK).voltageProperty().get() -
				this.getPin(CURRENT_SOURCE).voltageProperty().get();
		
//		if (Math.abs(currentVoltage) > saturation)
//			currentVoltage = Math.signum(currentVoltage) * saturation;
		
		this.magneticField.set(this.magneticField.get() +
				(prevVoltage +
				(prevVoltage = currentVoltage))
				/ 2 * timeElapsed);
		
		if (!Double.isFinite(magneticField.get()))
			magneticField.set(0);
	}
	
	/**
	 * Returns current on pin2, based on magnetic field
	 * @param pin1
	 * @param pin2
	 * @return 
	 */
	@Override
	public ElementInfo getInfo(int pin1, int pin2)
	{
		return new ElementInfo(
				this.magneticField.get() / this.inductance.get() * (pin2 == CURRENT_SOURCE ? +1 : -1),
				ElementInfo.TYPE.CURRENT);
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(Inductor.this, Inductor.this.wrapper);
			
			pinsGUI[0] = this.getComponent().getPin(Inductor.CURRENT_SOURCE);
			pinsGUI[1] = null;
			pinsGUI[2] = this.getComponent().getPin(Inductor.CURRENT_SINK);
			pinsGUI[3] = null;
			
			final double LENGTH = 0.6;
			
			Line term1 = new Line();
			term1.startXProperty().bind(this.widthProperty().divide(2));
			term1.startYProperty().bind(this.heightProperty().multiply((1-LENGTH)/2));
			term1.endXProperty().bind(this.widthProperty().divide(2));
			term1.endYProperty().set(0);
			
			Line term2 = new Line();
			term2.startXProperty().bind(this.widthProperty().divide(2));
			term2.startYProperty().bind(this.heightProperty().multiply((1+LENGTH)/2));
			term2.endXProperty().bind(this.widthProperty().divide(2));
			term2.endYProperty().bind(this.heightProperty());
			
			this.getChildren().addAll(term1, term2);
			
			final double HEIGHT = 0.15;
			final int BUMPS = 3;
			
			for (int i = 0; i < BUMPS; i++)
			{
				Arc bump = new Arc();
				bump.radiusXProperty().bind(this.widthProperty().multiply(HEIGHT));
				bump.radiusYProperty().bind(this.heightProperty().multiply(LENGTH/(BUMPS*2)));
				bump.centerXProperty().bind(this.widthProperty().divide(2));
				bump.centerYProperty().bind(this.heightProperty().multiply(i*LENGTH/BUMPS).add(term1.startYProperty()).add(bump.radiusYProperty()));
				bump.startAngleProperty().set(-90);
				bump.lengthProperty().set(180);
				bump.setStroke(Color.BLACK);
				bump.setType(ArcType.OPEN);
				bump.setFill(Color.TRANSPARENT);
				this.getChildren().add(bump);
			}
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
