
package circuit.component;

import circuit.Breadboard;
import circuitGUI.Sandbox;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javafx.scene.shape.Line;
import circuit.ComponentGUI;

public class Ground extends Node implements ComponentGUI
{
	private static final Map<Breadboard, Stack<Pin>> pins = new HashMap<>();
	
	private Pin pin;
	private Breadboard breadboard;
	
	public Ground()
	{
		super(0);
		this.pin = null;
		this.breadboard = null;
	}

	@Override
	public Pin getPin(int i) { return pins.get(this.breadboard).get(i); }

	@Override
	public int getPinCount() { return this.breadboard == null ? 0 : pins.get(this.breadboard).size(); }
	
	private void setBreadboard(Breadboard breadboard)
	{
		assert breadboard != null;
		assert this.breadboard == null;
		
		if (Ground.pins.get(breadboard) == null)
			Ground.pins.put(breadboard, new Stack<>());
		if (Ground.pins.get(breadboard).isEmpty())
			Ground.pins.get(breadboard).add(this.new Pin(0));
		else
			Ground.pins.get(breadboard).add(
					Ground.pins.get(breadboard).get(0).component
					.new Pin(Ground.pins.get(breadboard).size()));
		this.pin = Ground.pins.get(breadboard).peek();
		this.breadboard = breadboard;
	}
	
	private circuit.GraphicalNode nodeGUI;
	
	@Override
	public void initGUI() { this.initGUI(new GraphicalNode(), 0, 0); }

	@Override
	public void initGUI(circuit.GraphicalNode n, int x, int y) { if (x == 0 && y == 0) nodeGUI = n; }

	@Override
	public circuit.GraphicalNode getGraphicalNode(int x, int y)
	{
		if (x == 0 && y == 0) return nodeGUI;
		return null;
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(Ground.this);
			
			this.pinsGUI[0] = null;			// Should be set later
			this.pinsGUI[1] = null;
			this.pinsGUI[2] = null;
			this.pinsGUI[3] = null;
			
			Line term = new Line();
			term.startXProperty().bind(this.widthProperty().divide(2));
			term.startYProperty().set(0);
			term.endXProperty().bind(this.widthProperty().divide(2));
			term.endYProperty().bind(this.heightProperty().divide(2));
			
			final double WIDTH = 0.5;
			final double SPACE = 0.1;
			
			for (int i = 0; i < 3; i++)
			{
				Line l = new Line();
				l.startXProperty().bind(this.widthProperty().multiply((1-WIDTH)/2+SPACE*i));
				l.startYProperty().bind(this.heightProperty().multiply(SPACE*i+1/2d));
				l.endXProperty().bind(this.widthProperty().multiply((1+WIDTH)/2-SPACE*i));
				l.endYProperty().bind(this.heightProperty().multiply(SPACE*i+1/2d));
				this.getChildren().add(l);
			}
			
			this.getChildren().add(term);
		}

		@Override
		public void rotateNode(int r) { /* no */ }

		@Override
		public void flipNode(boolean x, boolean y) { /* no */ }

		@Override
		public void setSandbox(Sandbox sandbox)
		{
			super.setSandbox(sandbox);
			Ground.this.setBreadboard(sandbox.breadboard);
			
			this.component = Ground.this.pin.component;
			this.pinsGUI[this.rotation % 4] = Ground.this.pin;
			
			this.physicalConfigurationChanged();
		}
	}
}
