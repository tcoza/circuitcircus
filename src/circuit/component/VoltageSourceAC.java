package circuit.component;


import circuit.Units;
import circuit.SerializableDoubleProperty;
import circuit.SerializableProperty;
import circuit.Tickable;
import java.util.Set;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class VoltageSourceAC extends VoltageSource implements Tickable
{
	protected final SerializableDoubleProperty voltagePeak;
	protected final SerializableDoubleProperty frequency;
	
	public VoltageSourceAC()
	{
		this.voltagePeak = new SerializableDoubleProperty(this, "voltage peak", Units.VOLTS, this.voltage.get());
		this.frequency = new SerializableDoubleProperty(this, "frequency", Units.HERTZ, 1 / (Math.PI * 2));
		this.voltage.set(0);
	}
	
	private double time = 0;
	
	@Override
	public void tick(double timeElapsed)
	{
		this.voltage.set(voltagePeak.get() *
				Math.sin(frequency.get() * Math.PI * 2 *
				(time += timeElapsed)));
	}

	@Override
	public Set<SerializableProperty> getProperties()
	{
		Set s = super.getProperties();
		s.remove(this.voltage);
		return s;
	}
	
	@Override
	public String getSimpleName() { return "AC Voltage Source"; }
	
	@Override
	public void initGUI() { initGUI(this.new GraphicalNode(), 0, 0); }
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		private final Pane sinePane;
		
		public GraphicalNode()
		{
			super(VoltageSourceAC.this);
			
			this.pinsGUI[0] = this.getComponent().getPin(VoltageSourceAC.POSITIVE_TERM);
			this.pinsGUI[1] = null;
			this.pinsGUI[2] = this.getComponent().getPin(VoltageSourceAC.NEGATIVE_TERM);
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
			
			final double SINE_WIDTH = 0.25;
			final double SINE_HEIGHT = 0.25;
			
			sinePane = new Pane();
			for (int i = 0; i < 20; i++)
			{
				Line l = new Line();
				l.startXProperty().bind(sinePane.widthProperty().multiply(i/20d));
				l.startYProperty().bind(sinePane.heightProperty().multiply((Math.sin(2*Math.PI*i/20)+1)/2));
				l.endXProperty().bind(sinePane.widthProperty().multiply((i+1)/20d));
				l.endYProperty().bind(sinePane.heightProperty().multiply((Math.sin(2*Math.PI*(i+1)/20)+1)/2));
				sinePane.getChildren().add(l);
			}
			
			sinePane.layoutXProperty().bind(this.widthProperty().multiply((1-SINE_WIDTH)/2));
			sinePane.layoutYProperty().bind(this.heightProperty().multiply((1-SINE_HEIGHT)/2));
			sinePane.prefWidthProperty().bind(this.widthProperty().multiply(SINE_WIDTH));
			sinePane.prefHeightProperty().bind(this.heightProperty().multiply(SINE_HEIGHT));
			
			this.getChildren().addAll(terminals, c, sinePane);
		}

		@Override
		public void rotateNode(int r)
		{
			super.rotateNode(r);
			sinePane.setRotate(-this.rotation*90);
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
