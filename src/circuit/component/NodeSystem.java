
package circuit.component;

import java.util.Set;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import circuit.ComponentGUI;

public class NodeSystem extends IntegratedCircuit implements ComponentGUI
{
	private final Node node0, node1, node2;
	private final Pin[] pinsJoined, pinsDisjoined;
	
	public NodeSystem()
	{
		super(0);
		this.node0 = new Node(4);
		this.node1 = new Node(2);
		this.node2 = new Node(2);
		
		pinsJoined = new Pin[4];
		pinsJoined[0] = this.node0.getPin(0);
		pinsJoined[1] = this.node0.getPin(1);
		pinsJoined[2] = this.node0.getPin(2);
		pinsJoined[3] = this.node0.getPin(3);
		
		pinsDisjoined = new Pin[4];
		pinsDisjoined[0] = this.node1.getPin(0);
		pinsDisjoined[1] = this.node2.getPin(0);
		pinsDisjoined[2] = this.node1.getPin(1);
		pinsDisjoined[3] = this.node2.getPin(1);
		
		this.join();
	}
	
	public boolean isJoined() { return this.pins == pinsJoined; }
	public void join()
	{
		this.pins = pinsJoined;
		if (nodeGUI != null)
			nodeGUI.rotateNode(0);
	}
	public void disjoin()
	{
		this.pins = pinsDisjoined;
		if (nodeGUI != null)
			nodeGUI.rotateNode(0);
	}
	
	@Override
	public Set<CircuitComponent> getElements() { return IntegratedCircuit.makeSet(this.pins[0].component, this.pins[1].component); }
	
	private GraphicalNode nodeGUI;

	@Override
	public void initGUI() { this.initGUI(new GraphicalNode(), 0, 0); }

	@Override
	public void initGUI(circuit.GraphicalNode n, int x, int y)
	{
		if (x == 0 && y == 0)
			nodeGUI = (GraphicalNode)n;
	}

	@Override
	public circuit.GraphicalNode getGraphicalNode(int x, int y)
	{
		if (x == 0 && y == 0)
			return nodeGUI;
		return null;
	}

	@Override
	public String getSimpleName() { return "Node"; }

	@Override
	public Element serialize(Element component)
	{
		assert component.getNodeName().equals("component");
		
		component.setAttribute("class", NodeSystem.class.getName());
		component.setAttribute("joined", Boolean.toString(this.isJoined()));
		
		if (nodeGUI == null) return component;
		
		for (int i = 0; i < nodeGUI.pinsGUI.length; i++)
		{
			Element pin = component.getOwnerDocument().createElement("pin");
			pin.setAttribute("index", Integer.toString(i));
			pin.setTextContent(Boolean.toString(nodeGUI.pinsGUI[i] != null));
			component.appendChild(pin);
		}
		
		return component;
	}
	
	public static ComponentGUI deserialize(Element component)
	{
		assert component.getNodeName().equals("component");
		assert component.getAttribute("class").equals(NodeSystem.class.getName());
		
		NodeSystem n = new NodeSystem();
		if (Boolean.parseBoolean(component.getAttribute("joined"))) n.join();
		else n.disjoin();
		
		n.initGUI();
		
		NodeList pins = component.getElementsByTagName("pin");
		for (int i = 0; i < pins.getLength(); i++)
			if (Boolean.parseBoolean(pins.item(i).getTextContent()))
				n.nodeGUI.addPin(Integer.parseInt(((Element)pins.item(i)).getAttribute("index")));
		return n;
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		private final double HOP_RADIUS = 0.1;
		
		public GraphicalNode()
		{
			super(NodeSystem.this);
			
			for (int i = 0; i < pinsGUI.length; i++)
				this.pinsGUI[i] = null;			// Starts with nothing
			
			this.setOnMouseClicked(e ->
			{
				if (!e.getButton().equals(MouseButton.PRIMARY))
					return;
				System.out.println("clicked");
				if (
						(e.getX() - this.getWidth() / 2) *
						(e.getX() - this.getWidth() / 2) +
						(e.getY() - this.getHeight() / 2) *
						(e.getY() - this.getHeight() / 2)
						<=
						(this.getWidth() * HOP_RADIUS * 2) *
						(this.getWidth() * HOP_RADIUS * 2))
					if (NodeSystem.this.isJoined())
						NodeSystem.this.disjoin();
					else
						NodeSystem.this.join();
				else
				{
					int pin = this.getPinFromPosition(e.getX(), e.getY());
					if (pinsGUI[pin] == null)
						this.addPin(pin);
					else
						this.removePin(pin);
				}
			});
			
			this.setOnKeyPressed(e ->
			{
				int pin = -1;
				switch (e.getCode())
				{
				case DIGIT0:
				case NUMPAD0:
				case DIGIT4:
				case NUMPAD4:
					pin = 0;
					break;
				case DIGIT1:
				case NUMPAD1:
					pin = 1;
					break;
				case DIGIT2:
				case NUMPAD2:
					pin = 2;
					break;
				case DIGIT3:
				case NUMPAD3:
					pin = 3;
					break;
				case SPACE:
					if (NodeSystem.this.isJoined())
						NodeSystem.this.disjoin();
					else
						NodeSystem.this.join();
					break;
				}
				if (pin >= 0)
					if (pinsGUI[pin] == null)
						this.addPin(pin);
					else
						this.removePin(pin);
			});
			
			this.update();
		}
		
		public void addPin(int pin)
		{
			if (pinsGUI[pin] != null)
				return;
			pinsGUI[pin] = NodeSystem.this.getPin(pin);
			this.update();
		}
		
		public void removePin(int pin)
		{
			if (pinsGUI[pin] == null)
				return;
			pinsGUI[pin] = null;
			this.update();
		}

		@Override
		public void rotateNode(int r)
		{
			boolean[] hasPin = new boolean[pinsGUI.length];
			for (int i = 0; i < hasPin.length; i++)
				hasPin[i] = pinsGUI[(i-r+4)%4] != null;
			for (int i = 0; i < hasPin.length; i++)
				pinsGUI[i] = hasPin[i] ? NodeSystem.this.getPin(i) : null;
			this.update();
		}
		
		@Override
		public void flipNode(boolean x, boolean y)
		{
			boolean[] hasPin = new boolean[pinsGUI.length];
			for (int i = 0; i < hasPin.length; i++)
				hasPin[i] = pinsGUI[i%2==0?y?(i+2)%4:i:x?(i+2)%4:i] != null;
			for (int i = 0; i < hasPin.length; i++)
				pinsGUI[i] = hasPin[i] ? NodeSystem.this.getPin(i) : null;
			this.update();
		}
		
		private void update()
		{
			this.getChildren().clear();
			
			boolean hasArc =
					!NodeSystem.this.isJoined() &&
					(pinsGUI[0] != null || pinsGUI[2] != null) &&
					(pinsGUI[1] != null || pinsGUI[3] != null);
			
			for (int i = 0; i < pinsGUI.length; i++)
			{
				if (pinsGUI[i] == null)
					continue;
				
				int neighborX = -((i%2)*(i-2));
				int neighborY = +(((i+1)%2)*(i-1));

				Line l = new Line();
				
				l.startXProperty().bind(this.widthProperty().divide(2));
				l.startYProperty().bind(this.heightProperty().multiply(1/2d + (!hasArc ? 0 : ((i-1)%2)*HOP_RADIUS)));
				l.endXProperty().bind(this.widthProperty().multiply((neighborX + 1) / 2d));
				l.endYProperty().bind(this.heightProperty().multiply((neighborY + 1) / 2d));

				this.getChildren().add(l);
			}
			
			if (this.countPins() < 2)
			{
				Circle c = new Circle();
				c.centerXProperty().bind(this.widthProperty().divide(2));
				c.centerYProperty().bind(this.heightProperty().divide(2));
				c.radiusProperty().bind(this.widthProperty().multiply(HOP_RADIUS/2));
				this.getChildren().add(c);
			}
			else if (hasArc)
			{
				Arc a = new Arc();
				a.centerXProperty().bind(this.widthProperty().divide(2));
				a.centerYProperty().bind(this.heightProperty().divide(2));
				a.radiusXProperty().bind(this.widthProperty().multiply(HOP_RADIUS));
				a.radiusYProperty().bind(this.heightProperty().multiply(HOP_RADIUS));
				a.startAngleProperty().set(-90);
				a.lengthProperty().set(180);
				a.setFill(Color.TRANSPARENT);
				a.setStroke(Color.BLACK);
				a.setType(ArcType.OPEN);
				
				this.getChildren().add(a);
			}
			
			this.physicalConfigurationChanged();
		}
		
		private int getPinFromPosition(double x, double y)
		{
			if (x * this.getHeight() < y * this.getWidth())
				if ((this.getWidth() - x) * this.getHeight() < y * this.getWidth())
					return 2;
				else
					return 3;
			else
				if ((this.getWidth() - x) * this.getHeight() < y * this.getWidth())
					return 1;
				else
					return 0;
		}
		
		public int countPins()
		{
			int c = 0;
			for (Pin p : pinsGUI)
				if (p != null)
					c++;
			return c;
		}
	}
}
