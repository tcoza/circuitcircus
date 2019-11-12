
package circuit.component;

import static circuit.component.IntegratedCircuit.makeSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.w3c.dom.Element;
import circuit.ComponentGUI;

public class SPDT extends IntegratedCircuit implements ComponentGUI
{
	private Node node0, node1, node2;
	
	public SPDT()
	{
		super(3);
		
		this.node0 = new Node(2);
		this.node1 = new Node(2);
		this.node2 = new Node(2);
		
		this.pins[0] = node0.getPin(0);
		this.pins[1] = node1.getPin(0);
		this.pins[2] = node2.getPin(0);
		
		this.setPosition(0);
	}
	
	/** p is either 0 or 1 */
	private void setPosition(int p) { this.node0.getPin(1).connect((p == 0 ? node1 : node2).getPin(1)); }
	private int getPosition() { return this.node1.getPin(1).getPinConnected() == null ? 1 : 0; }

	@Override
	public Set<CircuitComponent> getElements() { return makeSet(this.node0, this.node2); }

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
		
		component.setAttribute("class", SPDT.class.getName());
		component.setAttribute("position", Integer.toString(this.getPosition()));
		
		if (nodeGUI == null)
			return component;
		
		component.appendChild(nodeGUI.serialize(component.getOwnerDocument().createElement("node")));
		
		return component;
	}
	
	public static ComponentGUI deserialize(Element component)
	{
		assert component.getNodeName().equals("component");
		assert component.getAttribute("class").equals(SPDT.class.getName());
		
		SPDT s = new SPDT();
		
		s.setPosition(Integer.parseInt(component.getAttribute("position")));
		
		s.initGUI(GraphicalNode.deserealize(s, (Element)component.getElementsByTagName("node").item(0)), 0, 0);
		
		return s;
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(SPDT.this);
			
			this.pinsGUI[0] = null;
			this.pinsGUI[1] = SPDT.this.getPin(0);
			this.pinsGUI[2] = null;
			this.pinsGUI[3] = SPDT.this.getPin(1);
			
			this.setOnMouseClicked(e ->
			{
				if (e.getButton().equals(MouseButton.PRIMARY))
					this.flip();
			});
			
			this.setOnKeyPressed(e ->
			{
				if (e.getCode().equals(KeyCode.SPACE))
					this.flip();
			});
			
			this.draw();
		}
		
		private void flip()
		{
			synchronized (this.sandbox == null ? new Object() : this.sandbox.breadboard)
			{
				SPDT.this.setPosition((SPDT.this.getPosition() + 1) % 2);
			}
			this.draw();
		}
		
		public void draw()
		{
			final double START = 0.1;
			final double HEIGHT = 0.2;
			final double RADIUS = 0.05;
			
			Line term0 = new Line();
			term0.startXProperty().set(0);
			term0.startYProperty().bind(this.heightProperty().divide(2));
			term0.endXProperty().bind(this.widthProperty().multiply(START));
			term0.endYProperty().bind(this.heightProperty().divide(2));
			
			Line term1 = new Line();
			term1.startXProperty().bind(this.widthProperty().divide(2));
			term1.startYProperty().set(0);
			term1.endXProperty().bind(this.widthProperty().divide(2));
			term1.endYProperty().bind(this.heightProperty().multiply(1/2d-HEIGHT));
			
			Line term2 = new Line();
			term2.startXProperty().bind(this.widthProperty().divide(2));
			term2.startYProperty().bind(this.heightProperty());
			term2.endXProperty().bind(this.widthProperty().divide(2));
			term2.endYProperty().bind(this.heightProperty().multiply(1/2d+HEIGHT));
			
			Line sw = new Line();
			sw.startXProperty().bind(term0.endXProperty());
			sw.startYProperty().bind(term0.endYProperty());
			sw.endXProperty().bind((SPDT.this.getPosition() == 0 ? term1 : term2).endXProperty());
			sw.endYProperty().bind((SPDT.this.getPosition() == 0 ? term1 : term2).endYProperty());
			
			this.getChildren().clear();
			this.getChildren().addAll(term0, term1, term2, sw);
			
			for (Line l : new Line[] {term0, term1, term2})
			{
				Circle c = new Circle();
				c.centerXProperty().bind(l.endXProperty());
				c.centerYProperty().bind(l.endYProperty());
				c.radiusProperty().bind(this.widthProperty().multiply(RADIUS));
				this.getChildren().add(c);
			}
		}
	}
}