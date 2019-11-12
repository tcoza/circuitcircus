package circuitGUI;

import circuit.Breadboard;
import circuit.GraphicalNode;
import circuit.component.NodeSystem;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.w3c.dom.Element;
import wrapper.Wrapper;
import circuit.ComponentGUI;

public class Sandbox extends Stage
{
	public final InfiniteGridPane mainPane;
	public final Breadboard breadboard;
	public final Toolbox toolbox;
	public final BooleanProperty fileModified;
	public final List<Graph> graphs;
	public final Set<PropertiesDialog> propertyDialogs; 
	public final int circuitNo;
	public File currentFile = null;
	
	public static Set<Sandbox> sandboxes = new HashSet<>();
	
	public Sandbox()
	{
		toolbox = new Toolbox(this);
		mainPane = new InfiniteGridPane();
		breadboard = new Breadboard();
		fileModified = new SimpleBooleanProperty(this, "file modified", false);
		circuitNo = Sandbox.sandboxes.size();
		graphs = new ArrayList<>();
		propertyDialogs = new HashSet<>();
		
		fileModified.addListener(e -> this.updateTitle());
		this.updateTitle();
		
		SandboxMenuBar menu = new SandboxMenuBar(this);
		final Wrapper<MouseEvent> prevEvent = new Wrapper<>();
		final Wrapper<Position> prevPosition = new Wrapper<>();
		final Wrapper<Position> dragStart = new Wrapper<>();
		
		mainPane.addEventFilter(MouseEvent.ANY, e ->
		{
			if (e.getButton().equals(MouseButton.NONE))
				return;
			
			if (!mainPane.isSelected(mainPane.getCoordinates(e)))
				e.consume();
			
			if ((!toolbox.getSelectedTool().type.equals(Toolbox.TOOLTYPE.SELECT) ||
				!e.getButton().equals(MouseButton.PRIMARY)) &&
				e.getEventType().equals(MouseEvent.MOUSE_PRESSED))
			{
				Position currentPosition = mainPane.getCoordinates(e);
				mainPane.select(currentPosition, !e.isControlDown());
				
				mainPane.update();
			}
			
			if (!e.getButton().equals(MouseButton.PRIMARY))
				return;
			
			switch (toolbox.getSelectedTool().type)
			{
			case COMPONENT:
				switch (e.getEventType().getName())
				{
				case "MOUSE_CLICKED":
					try { addComponent(toolbox.getSelectedTool().component.serialize(), mainPane.getCoordinates(e).x, mainPane.getCoordinates(e).y); }
					catch (Exception ex) { assert false; }
					break;
				}
				mainPane.update();
				e.consume();
				break;
			case SELECT:
				switch (e.getEventType().getName())
				{
				case "MOUSE_PRESSED":
					dragStart.v = mainPane.getCoordinates(e);
					if (mainPane.isSelected(dragStart.v) && e.isControlDown())
					{
						mainPane.unselect(dragStart.v);
						break;
					}
				case "MOUSE_DRAGGED":
					if (dragStart.v == null)
						break;
					
					mainPane.select(dragStart.v, mainPane.getCoordinates(e), !e.isControlDown());
					break;
				case "MOUSE_RELEASED":
//					mainPane.select(mainPane.getCoordinates(e), false);
					dragStart.v = null;
					break;
				}
				mainPane.update();
				e.consume();
				break;
			case MOVE:
				switch (e.getEventType().getName())
				{
				case "MOUSE_PRESSED":
					prevEvent.v = e;
					mainPane.setCursor(Cursor.CLOSED_HAND);
				case "MOUSE_DRAGGED":
					mainPane.drag(e.getX() - prevEvent.v.getX(), e.getY() - prevEvent.v.getY());
					break;
				case "MOUSE_RELEASED":
					mainPane.setCursor(Cursor.HAND);
					break;
				}
				e.consume();
				break;
			case DRAW:
				switch (e.getEventType().getName())
				{
				case "MOUSE_DRAGGED":
					Position c = mainPane.getCoordinates(e);
					if (prevPosition.v != null && !c.equals(prevPosition.v))
					{
						int signX = (c.x - prevPosition.v.x) >= 0 ? 1 : -1;
						int signY = (c.y - prevPosition.v.y) >= 0 ? 1 : -1;
						
						Position[] positions =
								new Position[(c.x - prevPosition.v.x) * signX + (c.y - prevPosition.v.y) * signY + 1];
						int x, y = prevPosition.v.y, i = 0;
						for (x = prevPosition.v.x; x * signX <= c.x * signX; x += signX)
							positions[i++] = new Position(x, y);
						for (x -= signX, y += signY; y * signY <= c.y * signY; y += signY)
							positions[i++] = new Position(x, y);
							
						this.drawWire(positions);
					}
					prevPosition.v = c;
					break;
				case "MOUSE_RELEASED":
					prevPosition.v = null;
					break;
				}
				break;
			default:
				throw new RuntimeException("you friggin moron");
			}
			
			prevEvent.v = e;
		});
		
		mainPane.setOnContextMenuRequested(e -> menu.contextMenu.show(this, e.getScreenX(), e.getScreenY()));
		
		mainPane.setOnScroll(e ->
			mainPane.zoom(e.getX(), e.getY(), e.getDeltaY() / 400));
		
		Pane root = new Pane(menu, mainPane);
		menu.setLayoutX(0);
		menu.setLayoutY(0);
		menu.prefWidthProperty().bind(root.widthProperty());
		mainPane.setLayoutX(0);
		mainPane.layoutYProperty().bind(menu.heightProperty());
		mainPane.prefWidthProperty().bind(root.widthProperty());
		mainPane.prefHeightProperty().bind(root.heightProperty().subtract(menu.heightProperty()));
		
		this.setScene(new Scene(root, 500, 300));
		this.setResizable(true);
		this.setOnShown(e ->
		{
			mainPane.requestFocus();
			mainPane.update();
			menu.toFront();
		});
		
		this.setOnHidden(e ->
		{
			Sandbox.sandboxes.remove(this);
			this.breadboard.updateLoop.interrupt();
		});
		this.setOnShowing(e -> Sandbox.sandboxes.add(this));
	}
	
	public Sandbox(File file)
	{
		this();
		this.openFile(file);
	}
	
	public void openFile(File file)
	{
		if (new CircuitSerializer(this).load(file))
		{
			this.currentFile = file;
			this.updateTitle();
		}
	}
	public void saveFile() { this.saveToFile(currentFile); }
	public void saveToFile(File file)
	{
		if (new CircuitSerializer(this).save(file))
			this.currentFile = file;
		this.updateTitle();
	}
	
	public void updateTitle()
	{
		this.setTitle(currentFile == null ? "Untitled Circuit " + (this.circuitNo + 1) : currentFile.getName());
		if (this.fileModified.get())
			this.setTitle("*" + this.getTitle());
	}
	
	public void addComponent(Element c, int x, int y) throws Exception { synchronized (breadboard)
	{
		ComponentGUI componentGUI = ComponentGUI.deserialize(c);
		
		for (int i = 0; componentGUI.getGraphicalNode(i, 0) != null; i++)
			for (int j = 0; componentGUI.getGraphicalNode(i, j) != null; j++)
			{
				if (mainPane.get(x+i, y+j) != null)
					breadboard.remove(mainPane.get(x+i, y+j).getComponent());
				
				GraphicalNode newNode = componentGUI.getGraphicalNode(i, j);
				
				newNode.setSandbox(this);
				newNode.setPosition(x+i, y+j);
				newNode.setOnPhysicalConfigurationChanged(e ->
					interconnectPinsGUI(((GraphicalNode)e.getSource()).getPosition().x, ((GraphicalNode)e.getSource()).getPosition().y));
				
				mainPane.set(newNode, x+i, y+j);
				newNode.physicalConfigurationChanged();
				
				breadboard.add(newNode.getComponent());
			}
	}}
	
	private void interconnectPinsGUI(int x, int y) { synchronized (breadboard)
	{
		this.fileModified.set(true);
		
		if (mainPane.get(x, y) == null)
		{
			for (int i = 0; i < 4; i++)
			{
				int neighborX = x-((i%2)*(i-2));
				int neighborY = y+(((i+1)%2)*(i-1));
				int neighborPin = (i+2)%4;

				if (mainPane.get(neighborX, neighborY) != null &&
					mainPane.get(neighborX, neighborY).pinsGUI[neighborPin] != null)
						mainPane.get(neighborX, neighborY).pinsGUI[neighborPin].disconnect();
			}
			return;
		}
		
		for (int i = 0; i < mainPane.get(x, y).pinsGUI.length; i++)
		{
			int neighborX = x-((i%2)*(i-2));
			int neighborY = y+(((i+1)%2)*(i-1));
			int neighborPin = (i+2)%4;
			
			if (mainPane.get(x, y).pinsGUI[i] == null &&
				mainPane.get(neighborX, neighborY) != null &&
				mainPane.get(neighborX, neighborY).pinsGUI[neighborPin] != null)
					mainPane.get(neighborX, neighborY).pinsGUI[neighborPin].disconnect();
			if (mainPane.get(x, y).pinsGUI[i] != null && (
				mainPane.get(neighborX, neighborY) == null ||
				mainPane.get(neighborX, neighborY).pinsGUI[neighborPin] == null))
					mainPane.get(x, y).pinsGUI[i].disconnect();
			if (mainPane.get(x, y).pinsGUI[i] != null &&
				mainPane.get(neighborX, neighborY) != null &&
				mainPane.get(neighborX, neighborY).pinsGUI[neighborPin] != null)
					mainPane.get(x, y).pinsGUI[i].connect(mainPane.get(neighborX, neighborY).pinsGUI[neighborPin]);
		}
	}}

	public void deleteComponent(int x, int y) { synchronized (breadboard)
	{
		if (mainPane.get(x, y) == null)
			return;
		
		breadboard.remove(mainPane.get(x, y).getComponent());
		ComponentGUI component = mainPane.get(x, y).getGUI();
		for (int i = 0; component.getGraphicalNode(i, 0) != null; i++)
			for (int j = 0; component.getGraphicalNode(i, j) != null; j++)
			{
				mainPane.set(null,
						component.getGraphicalNode(i, j).getPosition().x,
						component.getGraphicalNode(i, j).getPosition().y);
				interconnectPinsGUI(x, y);
			}
	}}
	
	public void drawWire(Position[] positions)
	{
		Position p = null;
		for (Position c : positions)
		{
			if (p == null)
				{ p = c; continue; }
			
			int pin = (p.x-c.x+4)%4+(((p.y-c.y)+1)*(p.y-c.y))%4;
			
			for (Position t : new Position[] {c, p})
			{
				if (mainPane.get(t.x, t.y) == null || mainPane.get(t.x, t.y) instanceof NodeSystem.GraphicalNode)
				{
					NodeSystem.GraphicalNode nodeGUI = (NodeSystem.GraphicalNode)mainPane.get(t.x, t.y);

					if (nodeGUI == null)
					{
						NodeSystem n = new NodeSystem(); n.join(); n.initGUI();
						try { this.addComponent(n.serialize(), t.x, t.y); } catch (Exception ex) { assert false; }
						nodeGUI = (NodeSystem.GraphicalNode)mainPane.get(t.x, t.y);
					}
					else if (nodeGUI.countPins() == 3 && nodeGUI.pinsGUI[pin] == null && t == p)
						((NodeSystem)nodeGUI.getComponent()).disjoin();
					
					nodeGUI.addPin(pin);
				}
				
				pin = (pin+2)%4;
			}
			
			p = c;
		}
		
		mainPane.update();
	}
}
