/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuitGUI;

import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jfx.messagebox.MessageBox;
import org.w3c.dom.*;
import circuit.ComponentGUI;

/**
 *
 * @author root
 */
public class CircuitSerializer
{
	public final Sandbox sandbox;
	
	public CircuitSerializer(Sandbox sandbox)
	{
		this.sandbox = sandbox;
	}
	
	public boolean load(File file)
	{
		Document doc;
		try
		{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			if (!doc.getDocumentElement().getNodeName().equals("breadboard"))
				throw new Exception();
		}
		catch (Exception ex)
		{
			MessageBox.show(this.sandbox, "'" + file.getName() + "' not a Circuit Circus file.", "Error opening specified file", MessageBox.ICON_ERROR | MessageBox.OK);
			return false;
		}
		
		NodeList components = doc.getDocumentElement().getElementsByTagName("component");
		ArrayList<String> unloadableComponents = new ArrayList<>();
		for (int i = 0; i < components.getLength(); i++)
			try
			{
				sandbox.addComponent(
						(Element)components.item(i),
						Integer.parseInt(((Element)components.item(i)).getAttribute("x")),
						Integer.parseInt(((Element)components.item(i)).getAttribute("y")));
			}
			catch (Exception ex) { unloadableComponents.add(components.item(i).toString()); }
		
		if (!unloadableComponents.isEmpty())
			MessageBox.show(this.sandbox, "The following components could not be loaded: " + unloadableComponents.toString(), "Error loading specified file", MessageBox.ICON_WARNING | MessageBox.OK);
		
		sandbox.fileModified.set(false);
		sandbox.mainPane.update();
		return true;
	}
	
	public boolean save(File file)
	{
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			doc.appendChild(doc.createElement("breadboard"));

			for (ComponentGUI c : sandbox.mainPane.getComponents())
			{
				Element component = doc.createElement("component");
				component.setAttribute("x", Integer.toString(c.getGraphicalNode(0, 0).getPosition().x));
				component.setAttribute("y", Integer.toString(c.getGraphicalNode(0, 0).getPosition().y));
				doc.getDocumentElement().appendChild(c.serialize(component));
			}

			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(file));

			sandbox.fileModified.set(false);
		}
		catch (Exception ex)
		{
			MessageBox.show(this.sandbox, "Error saving to '" + file.getName() + "': " + ex.getMessage(), "Error saving to specified file", MessageBox.ICON_ERROR | MessageBox.OK);
			return false;
		}
		return true;
	}
}
