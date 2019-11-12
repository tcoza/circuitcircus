
package circuitGUI;

import circuit.SerializableDoubleProperty;
import circuit.SerializableProperty;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import circuit.ComponentGUI;

public class PropertiesDialog extends Stage
{
	public final ComponentGUI component;
	
	private final GridPane mainPane;
	private final Sandbox sandbox;
	
	public PropertiesDialog(Window owner, ComponentGUI component)
	{
		this(null, component);
		
		this.initOwner(owner);
	}
	public PropertiesDialog(Sandbox sandbox, ComponentGUI component)
	{
		this.sandbox = sandbox;
		this.component = component;
		this.mainPane = new GridPane();
		mainPane.setHgap(10);
		
		SerializableProperty[] properties = new SerializableProperty[component.getProperties().size()];
		component.getProperties().toArray(properties);
		Arrays.sort(properties, (SerializableProperty o1, SerializableProperty o2) ->
		{
			if (o1.isTransient() == o2.isTransient())
				return o1.getName().compareTo(o2.getName());
			return o1.isTransient() ? +1 : -1;
		});
		
		this.setScene(new Scene(mainPane));
		this.setResizable(false);
		this.setTitle(component.getSimpleName() + " properties");
		this.initOwner(sandbox);
		
		for (int i = 0; i < properties.length; i++)
			this.new PropertyField(properties[i], i);
		
		mainPane.addEventFilter(KeyEvent.KEY_PRESSED, e ->
		{
			if (e.getTarget() == mainPane && e.getCode().equals(KeyCode.ESCAPE))
				this.close();
		});
		
		if (sandbox != null)
		{	
			this.setOnShowing(e -> sandbox.propertyDialogs.add(this));
			this.setOnHiding(e -> sandbox.propertyDialogs.remove(this));
		}
	}
	
	class PropertyField extends TextField
	{
		public SerializableProperty property;

		public PropertyField(SerializableProperty property, int row)
		{
			this.property = property;

			this.setOnAction(e -> this.handle());
			this.setOnMouseClicked(e -> this.requestFocus());
			this.focusedProperty().addListener(e ->
			{
				if (this.isFocused())
					this.unbindText();
				else
				{
					this.bindText();
					this.selectRange(0, 0);
				}
			});

			Text name = new Text(this.formatName());
			this.setPrefWidth(200);
			mainPane.add(name, 0, row);
			mainPane.add(this, 1, row);

			this.setDisable(this.property.isTransient());
			this.bindText();
			
			ContextMenu contextMenu = new ContextMenu();
			Menu viewIn = new Menu("View in");
			contextMenu.getItems().add(viewIn);
			
			viewIn.setDisable(!(this.property instanceof DoubleProperty) || PropertiesDialog.this.sandbox == null);
			viewIn.getItems().add(new MenuItem());			// Dummy

			viewIn.setOnShowing(e ->
			{
				assert this.property instanceof DoubleProperty;
				
				viewIn.getItems().clear();
				sandbox.graphs.sort((g1, g2) -> (g1.getTitle().compareTo(g2.getTitle())));

				for (Graph g : sandbox.graphs)
				{
					final Graph currentGraph = g;
					MenuItem addToGraph = new MenuItem(currentGraph.getTitle());
					addToGraph.setOnAction(f ->
					{
						if (!currentGraph.monitoredProperties.contains((DoubleProperty)this.property))
							currentGraph.monitoredProperties.add((DoubleProperty)this.property);
						currentGraph.show();
						currentGraph.requestFocus();
					});
					viewIn.getItems().add(addToGraph);
				}
				
				viewIn.getItems().add(new SeparatorMenuItem());
				
				MenuItem newGraph = new MenuItem("New Graph...");
				newGraph.setOnAction(f -> new Graph(sandbox, (DoubleProperty)this.property).show());
				viewIn.getItems().add(newGraph);
			});
			
			name.setOnContextMenuRequested(e -> contextMenu.show(PropertiesDialog.this, e.getScreenX(), e.getScreenY()));
		}

		private String formatName()
		{
			String s = " ";
			s += Character.toUpperCase(property.getName().charAt(0));
			s += property.getName().substring(1);
			if (property instanceof SerializableDoubleProperty && ((SerializableDoubleProperty)property).getUnits() != null)
				s += " (" + ((SerializableDoubleProperty)property).getUnits() + ")";
			s += " ";
			return s;
		}

		public void handle()
		{
			if (this.property.isTransient())
				return;
			if (this.textProperty().isBound())
				return;

			try { this.property.setStringValue(this.getText()); }
			catch (Exception ex)
			{
				this.setStyle("-fx-background-color: #FF0000;");
				this.setFocused(true);
				return;
			}
			
			this.setStyle("");
			PropertiesDialog.this.mainPane.requestFocus();
		}

		private void bindText() { this.textProperty().bind(Bindings.createStringBinding(() -> { return property.getStringValue(); }, property)); }
		private void unbindText() { this.textProperty().unbind(); }
	}
}
