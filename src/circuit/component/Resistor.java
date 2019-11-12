package circuit.component;

import circuit.Units;
import circuit.SerializableDoubleProperty;
import javafx.scene.shape.Line;
import circuit.ComponentGUI;

/**
 * Describes an ideal resistor
 * @author root
 */
public class Resistor extends CircuitElement implements ComponentGUI
{
	/** Arbitrary identifiers */
	public static final int PIN1 = 0, PIN2 = 1;
	
	/** Resistance of resistor in ohms */
	protected final SerializableDoubleProperty resistance;
	
	/**
	 * Creates new resistor with specified resistance
	 * @param resistance 
	 */
	public Resistor()
	{
		this.resistance = new SerializableDoubleProperty(this, "resistance", Units.OHMS, 100);
	}

	/**
	 * Returns resistance of resistor
	 * @param pin1
	 * @param pin2
	 * @return 
	 */
	@Override
	public ElementInfo getInfo(int pin1, int pin2)
	{
		return new ElementInfo(this.resistance.get(), ElementInfo.TYPE.RESISTANCE);
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(Resistor.this);
			
			this.pinsGUI[0] = this.getComponent().getPin(Resistor.PIN1);
			this.pinsGUI[1] = null;
			this.pinsGUI[2] = this.getComponent().getPin(Resistor.PIN2);
			this.pinsGUI[3] = null;
			
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
			
			final double HEIGHT = 0.1;
			final int PEAKS = 3;
			
			{
				Line l = new Line();
				l.startXProperty().bind(term1.startXProperty());
				l.startYProperty().bind(term1.startYProperty());
				l.endXProperty().bind(term1.startXProperty().add(this.widthProperty().multiply(HEIGHT)));
				l.endYProperty().bind(term1.startYProperty().add(this.heightProperty().multiply(LENGTH/(PEAKS*4))));
				this.getChildren().add(l);
			}
			
			double distY = LENGTH/(PEAKS*4);
			for (int i = 0; i < PEAKS * 2 - 1; i++)
			{
				Line l = new Line();
				l.startXProperty().bind(term1.startXProperty().add(this.widthProperty().multiply(HEIGHT * (i % 2 == 0 ? +1 : -1))));
				l.startYProperty().bind(term1.startYProperty().add(this.heightProperty().multiply(distY)));
				l.endXProperty().bind(term1.startXProperty().add(this.widthProperty().multiply(HEIGHT * (i % 2 == 0 ? -1 : +1))));
				l.endYProperty().bind(term1.startYProperty().add(this.heightProperty().multiply(distY += LENGTH/(PEAKS*2))));
				this.getChildren().add(l);
			}
			
			{
				Line l = new Line();
				l.startXProperty().bind(term2.startXProperty());
				l.startYProperty().bind(term2.startYProperty());
				l.endXProperty().bind(term2.startXProperty().subtract(this.widthProperty().multiply(HEIGHT)));
				l.endYProperty().bind(term2.startYProperty().subtract(this.heightProperty().multiply(LENGTH/(PEAKS*4))));
				this.getChildren().add(l);
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
