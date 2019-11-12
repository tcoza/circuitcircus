package circuit;

import java.lang.reflect.Method;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * ALL CLASSES IMPLEMENTING THIS INTERFACE MUST PROVIDE A DEFAULT CONSTRUCTOR
 * @author root
 */
public interface ComponentGUI
{
	public default String getSimpleName() { return this.getClass().getSimpleName(); }
	
	public Set<SerializableProperty> getProperties();
	public SerializableProperty getProperty(String name);
	
	public void initGUI();
	public void initGUI(GraphicalNode n, int x, int y);
	public circuit.GraphicalNode getGraphicalNode(int x, int y);
	
	public default Element serialize() { try { return this.serialize(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("component")); } catch (ParserConfigurationException ex) { ex.printStackTrace(); return null; } }
	public default Element serialize(Element component)
	{
		assert component.getNodeName().equals("component");
		
		component.setAttribute("class", this.getClass().getName());
		
		for (SerializableProperty p : this.getProperties())
		{
			if (p.isTransient())
				continue;
			Element property = component.getOwnerDocument().createElement("property");
			property.setAttribute("name", p.getName());
			property.setTextContent(p.getStringValue());
			component.appendChild(property);
		}
		
		for (int i = 0; this.getGraphicalNode(i, 0) != null; i++)
			for (int j = 0; this.getGraphicalNode(i, j) != null; j++)
			{
				Element node = component.getOwnerDocument().createElement("node");
				node.setAttribute("dx", Integer.toString(i));
				node.setAttribute("dy", Integer.toString(j));
				component.appendChild(this.getGraphicalNode(i, j).serialize(node));
			}
		
		return component;
	}
	
	public static ComponentGUI deserialize(Element component) throws Exception
	{
		assert component.getNodeName().equals("component");
		
		try { return (ComponentGUI)Class.forName(component.getAttribute("class")).getMethod("deserialize", Element.class).invoke(null, component); }
		catch (NoSuchMethodException ex) { /* Then just move on */ }

		ComponentGUI c = (ComponentGUI)Class.forName(component.getAttribute("class")).newInstance();

		NodeList properties = component.getElementsByTagName("property");
		for (int i = 0; i < properties.getLength(); i++)
			c.getProperty(((Element)properties.item(i)).getAttribute("name")).setStringValue(properties.item(i).getTextContent());

		NodeList graphicalNodes = component.getElementsByTagName("node");
		for (int i = 0; i < graphicalNodes.getLength(); i++)
			c.initGUI(
				GraphicalNode.deserealize(c, (Element)graphicalNodes.item(i)),
				Integer.parseInt(((Element)graphicalNodes.item(i)).getAttribute("dx")),
				Integer.parseInt(((Element)graphicalNodes.item(i)).getAttribute("dy")));

		return c;
	}
}