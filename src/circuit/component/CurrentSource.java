package circuit.component;

import circuit.Units;
import circuit.Breadboard;
import circuit.SerializableDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import circuit.ComponentGUI;


/**
 * Describes an ideal current source
 * @author root
 */
public class CurrentSource extends CircuitElement implements ComponentGUI
{
	/** Pin with outgoing current */
	public static final int CURRENT_SINK = 0;
	/** Pin with ingoing current */
	public static final int CURRENT_SOURCE = 1;
	
	public final ParallelWrapper<CurrentSource> wrapper;
	/** Current of this current source */
	protected final SerializableDoubleProperty current;
	
	public CurrentSource()
	{
		this.current = new SerializableDoubleProperty(this, "current", Units.AMPS, 0.2);
		this.wrapper = new ParallelWrapper<>(this, new SimpleDoubleProperty(Breadboard.BIG).add(0));
	}
	
	@Override
	public ElementInfo getInfo(int pin1, int pin2)
	{
		return new ElementInfo(current.get() * (pin2 == CURRENT_SOURCE ? +1 : -1), CircuitElement.ElementInfo.TYPE.CURRENT);
	}
	
	@Override
	public String getSimpleName() { return "Current Source"; }

	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(CurrentSource.this, CurrentSource.this.wrapper);
			
			this.pinsGUI[0] = this.getComponent().getPin(CurrentSource.CURRENT_SOURCE);
			this.pinsGUI[1] = null;
			this.pinsGUI[2] = this.getComponent().getPin(CurrentSource.CURRENT_SINK);
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
			
			final double ARROW_LENGTH = 0.4;			// Length of arrow relative to side
			
			Line arrow = new Line();
			arrow.startXProperty().bind(c.centerXProperty());
			arrow.startYProperty().bind(c.centerYProperty().subtract(this.heightProperty().multiply(ARROW_LENGTH/2)));
			arrow.endXProperty().bind(c.centerXProperty());
			arrow.endYProperty().bind(c.centerYProperty().add(this.heightProperty().multiply(ARROW_LENGTH/2)));
			
			final double ARROW_TIP_WIDTH = 0.1;			// Width of tip of arrow relative to side
			final double ARROW_TIP_HEIGHT = 0.1;			// Width of tip of arrow relative to side
			
			
			Line tip1 = new Line();
			tip1.startXProperty().bind(arrow.startXProperty());
			tip1.startYProperty().bind(arrow.startYProperty());
			tip1.endXProperty().bind(arrow.startXProperty().subtract(this.widthProperty().multiply(ARROW_TIP_WIDTH/2)));
			tip1.endYProperty().bind(arrow.startYProperty().add(this.heightProperty().multiply(ARROW_TIP_HEIGHT/2)));
			
			Line tip2 = new Line();
			tip2.startXProperty().bind(arrow.startXProperty());
			tip2.startYProperty().bind(arrow.startYProperty());
			tip2.endXProperty().bind(arrow.startXProperty().add(this.widthProperty().multiply(ARROW_TIP_WIDTH/2)));
			tip2.endYProperty().bind(arrow.startYProperty().add(this.heightProperty().multiply(ARROW_TIP_HEIGHT/2)));
			
			this.getChildren().addAll(terminals, c, arrow, tip1, tip2);
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