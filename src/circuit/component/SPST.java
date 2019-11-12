
package circuit.component;

import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.w3c.dom.Element;
import circuit.ComponentGUI;

public class SPST extends IntegratedCircuit implements ComponentGUI
{
	private Node node1, node2;
	
	public SPST()
	{
		super(2);
		
		this.node1 = new Node(2);
		this.node2 = new Node(2);
		
		this.pins[0] = node1.getPin(0);
		this.pins[1] = node2.getPin(0);
		
		this.open();
	}
	
	private void open() { this.node1.getPin(1).disconnect(); }
	private void close() { this.node1.getPin(1).connect(this.node2.getPin(1)); }
	public boolean isOpen() { return this.node1.getPin(1).getPinConnected() == null; }
	public boolean isClosed() { return !isOpen(); }

	@Override
	public Set<CircuitComponent> getElements() { return makeSet(this.node1, this.node2); }

	private GraphicalNode nodeGUI;
	
	@Override
	public void initGUI() { this.initGUI(new GraphicalNode(), 0, 0); }

	@Override
	public void initGUI(circuit.GraphicalNode n, int x, int y)
	{
		if (x == 0 && y == 0) nodeGUI = (GraphicalNode)n;
	}

	@Override
	public GraphicalNode getGraphicalNode(int x, int y)
	{
		if (x == 0 && y == 0) return nodeGUI;
		return null;
	}

	@Override
	public Element serialize(Element component)
	{
		assert component.getNodeName().equals("component");
		
		component.setAttribute("class", SPST.class.getName());
		component.setAttribute("open", Boolean.toString(this.isOpen()));
		
		if (nodeGUI == null)
			return component;
		
		component.appendChild(nodeGUI.serialize(component.getOwnerDocument().createElement("node")));
		
		return component;
	}
	
	public static ComponentGUI deserialize(Element component)
	{
		assert component.getNodeName().equals("component");
		assert component.getAttribute("class").equals(SPST.class.getName());
		
		SPST s = new SPST();
		
		if (Boolean.parseBoolean(component.getAttribute("open")))
			s.open();
		else
			s.close();
		
		s.initGUI(GraphicalNode.deserealize(s, (Element)component.getElementsByTagName("node").item(0)), 0, 0);
		
		return s;
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(SPST.this);
			
			this.pinsGUI[0] = null;
			this.pinsGUI[1] = SPST.this.getPin(0);
			this.pinsGUI[2] = null;
			this.pinsGUI[3] = SPST.this.getPin(1);
			
			this.setOnKeyPressed(e ->
			{
				if (e.getCode().equals(KeyCode.SPACE))
					this.flip();
			});
			this.setOnMouseClicked(e ->
			{
				if (e.getButton().equals(MouseButton.PRIMARY))
					this.flip();
			});
			
			this.draw();
		}
		
		private void flip()
		{
			synchronized (this.sandbox == null ? new Object() : this.sandbox.breadboard)
			{
				if (SPST.this.isOpen())
					SPST.this.close();
				else
					SPST.this.open();
			}
			
			this.draw();
		}
		
		public void draw()
		{
			final double WIDTH = 0.5;
			final double ANGLE = (this.rotation == 2 ? +1 : -1) * (SPST.this.isOpen() ? 30 : 0);
			final double RADIUS = 0.05;
			
			Line term1 = new Line();
			term1.startXProperty().set(0);
			term1.startYProperty().bind(this.heightProperty().divide(2));
			term1.endXProperty().bind(this.widthProperty().multiply((1-WIDTH)/2));
			term1.endYProperty().bind(this.heightProperty().divide(2));
			
			Line term2 = new Line();
			term2.startXProperty().bind(this.widthProperty());
			term2.startYProperty().bind(this.heightProperty().divide(2));
			term2.endXProperty().bind(this.widthProperty().multiply((1+WIDTH)/2));
			term2.endYProperty().bind(this.heightProperty().divide(2));
			
			Line sw = new Line();
			sw.startXProperty().bind(term1.endXProperty());
			sw.startYProperty().bind(term1.endYProperty());
			sw.endXProperty().bind(term1.endXProperty().add(this.widthProperty().multiply(WIDTH*Math.cos(Math.toRadians(ANGLE)))));
			sw.endYProperty().bind(term1.endYProperty().add(this.heightProperty().multiply(WIDTH*Math.sin(Math.toRadians(ANGLE)))));
		
			Circle c1 = new Circle();
			c1.centerXProperty().bind(term1.endXProperty());
			c1.centerYProperty().bind(term1.endYProperty());
			c1.radiusProperty().bind(this.widthProperty().multiply(RADIUS));
			
			Circle c2 = new Circle();
			c2.centerXProperty().bind(term2.endXProperty());
			c2.centerYProperty().bind(term2.endYProperty());
			c2.radiusProperty().bind(this.widthProperty().multiply(RADIUS));
			
			this.getChildren().clear();
			this.getChildren().addAll(term1, term2, sw, c1, c2);
		}

		@Override
		public void rotateNode(int r)
		{
			super.rotateNode(r);
			this.draw();
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