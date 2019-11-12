
package circuit;

import circuit.component.CircuitComponent;
import circuitGUI.Position;
import circuitGUI.Sandbox;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

/**
 *
 * @author root
 */
public abstract class GraphicalNode extends Pane
{
	/**
	 * [0] = top pin
	 * [1] = right pin
	 * [2] = bottom pin
	 * [3] = left pin
	 */
	public CircuitComponent.Pin[] pinsGUI;
	
	protected CircuitComponent component;
	protected ComponentGUI componentGUI;
	
	public GraphicalNode(ComponentGUI componentGUI) { this(componentGUI, (CircuitComponent)componentGUI); }
	public GraphicalNode(ComponentGUI componentGUI, CircuitComponent component)
	{
		pinsGUI = new CircuitComponent.Pin[4];
		this.componentGUI = componentGUI;
		this.component = component;
		this.initTooltip();
	}
	
	public CircuitComponent getComponent() { return this.component; }
	public ComponentGUI getGUI() { return this.componentGUI; }
	
	/** USED ONLY IN THE CONTEXT OF THE SANDBOX */
	private Position position;
	public Position getPosition() { return position; }
	public void setPosition(int x, int y) { this.position = new Position(x, y); }
	
	protected Sandbox sandbox;
	public void setSandbox(Sandbox sandbox) { this.sandbox = sandbox; }
	
	private EventHandler<Event> onPhysicalConfigurationChanged = null;
	public void setOnPhysicalConfigurationChanged(EventHandler<Event> onPhysicalConfigurationChanged) { this.onPhysicalConfigurationChanged = onPhysicalConfigurationChanged; }
	public void physicalConfigurationChanged()
	{
		if (onPhysicalConfigurationChanged != null)
			onPhysicalConfigurationChanged.handle(new Event(this, null, EventType.ROOT));
	}
	
	protected int rotation = 0;
	public void rotateNode(int r)
	{
		CircuitComponent.Pin[] newPinsGUI = pinsGUI.clone();
		for (int i = 0; i < pinsGUI.length; i++)
			newPinsGUI[(i+r)%4] = pinsGUI[i];
		pinsGUI = newPinsGUI;
		
		this.setRotate((rotation += r) * 90);
		
		this.physicalConfigurationChanged();
	}
	
	protected boolean flipX = false;
	protected boolean flipY = false;
	public void flipNode(boolean x, boolean y)
	{
		CircuitComponent.Pin[] newPinsGUI = pinsGUI.clone();
		for (int i = 0; i < newPinsGUI.length; i++)
			if ((x && i % 2 == 1) || (y && i % 2 == 0))
				newPinsGUI[i] = pinsGUI[(i+2)%4];
		pinsGUI = newPinsGUI;
		
		flipX ^= (this.rotation % 2 == 0) ? x : y;
		flipY ^= (this.rotation % 2 == 0) ? y : x;
		
		this.setScaleX(flipX ? -1 : +1);
		this.setScaleY(flipY ? -1 : +1);
		
		this.physicalConfigurationChanged();
	}
	
	protected void initTooltip()
	{
		final List<SerializableProperty> properties = new ArrayList<>();
		for (SerializableProperty p : this.getGUI().getProperties())
			if (!p.isTransient()) properties.add(p);
		if (properties.isEmpty()) return;
		properties.sort((p1, p2) -> (p1.getName().compareTo(p2.getName())));
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(Bindings.createStringBinding(() ->
				{
					String s = new String();
					for (SerializableProperty p : properties)
					{
						s += Character.toUpperCase(p.getName().charAt(0)) + p.getName().substring(1) + ": " + p.getStringValue();
						if (p instanceof SerializableDoubleProperty && ((SerializableDoubleProperty)p).getUnits() != null)
							s += " " + ((SerializableDoubleProperty)p).getUnits();
						s += '\n';
					}
					return s;
				}, properties.toArray(new SerializableProperty[properties.size()])));
		this.setOnMouseMoved(e -> tooltip.show(this, e.getScreenX(), e.getScreenY() + 15));
		this.setOnMouseExited(e -> tooltip.hide());
	}
	
	public Element serialize(Element node)
	{
		assert node.getNodeName().equals("node");
		
		node.setAttribute("class", this.getClass().getName());
		node.setAttribute("rotation", Integer.toString(this.rotation));
		node.setAttribute("flipX", Boolean.toString(flipX));
		node.setAttribute("flipY", Boolean.toString(flipY));
		
		return node;
	}
	
	public static GraphicalNode deserealize(ComponentGUI outer, Element node)
	{
		assert node.getNodeName().equals("node");
		
		try
		{
			GraphicalNode n = (GraphicalNode)Class.forName(node.getAttribute("class")).getConstructor(outer.getClass()).newInstance(outer);
			n.flipNode(
					Boolean.parseBoolean(node.getAttribute("flipX")),
					Boolean.parseBoolean(node.getAttribute("flipY")));
			n.rotateNode(Integer.parseInt(node.getAttribute("rotation")));
			return n;
		}
		catch (Exception ex) { ex.printStackTrace(); }
		
		return null;
	}
}
