package circuitGUI;


import circuit.component.*;
import circuit.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author root
 */
public class Toolbox extends Stage
{
	private final Background NORMAL = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
	private final Background SELECTED = new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY));
	
	private final double CELL_SIZE = 30;
	
	private Tool selectedTool;
	private Map<Tool, Button> toolList;
	private Sandbox sandbox;
	
	public Toolbox(Sandbox sandbox)
	{
		GridPane toolboxPane = new GridPane();
		this.toolList = new HashMap<>();
		this.sandbox = sandbox;
		
		final int CELLS_X = 2;
		
		final List<Tool> tools = getTools();
		
		int i = 0;
		for (Tool t : tools)
		{
			Button button = new Button();
			button.setGraphic(t.pane);
			t.pane.setPrefSize(CELL_SIZE, CELL_SIZE);
			Tooltip.install(button, t.tooltip);
			button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			button.setBackground(null);
			button.setOnAction(e ->
			{
				this.selectedTool = t;
				for (Tool u : tools)
				{
					u.pane.setStyle(null);
					u.pane.setBackground(NORMAL);
					u.pane.setDisable(true);
				}
				t.pane.setStyle("-fx-border-color: blue;");
				t.pane.setBackground(SELECTED);
				t.pane.setDisable(false);
				t.pane.requestFocus();
				t.loadCursor();
			});
			button.setOnMouseEntered(e -> button.requestFocus());
			button.setOnMouseExited(e -> this.selectedTool.pane.requestFocus());
			button.focusedProperty().addListener(e ->
			{
				if (button.isFocused())
					if (this.selectedTool == t)
						t.pane.requestFocus();
					else
					{
						t.pane.setBackground(SELECTED);
						this.selectedTool.pane.setBackground(NORMAL);
					}
				else
					if (this.selectedTool != t)
						t.pane.setBackground(NORMAL);
					else
						t.pane.setBackground(SELECTED);
			});
			toolboxPane.add(button, i % CELLS_X, i / CELLS_X);
			toolList.put(t, button);
			if (t == this.selectedTool)
				this.setOnShown(e -> button.fire());
			i++;
		}
		
		this.setScene(new Scene(toolboxPane));
		this.setTitle("Toolbox");
		this.setResizable(false);
		this.initOwner(sandbox);
	}
	
	public Tool getSelectedTool() { return selectedTool; }
	public void selectTool(TOOLTYPE tool)
	{
		for (Map.Entry<Tool, Button> e : this.toolList.entrySet())
			if (e.getKey().type.equals(tool))
				e.getValue().fire();
	}
	
	private List<Tool> getTools()
	{
		List<Tool> tools = new ArrayList<>();
		tools.add(new Tool(VoltageSource.class));
		tools.add(new Tool(CurrentSource.class));
		tools.add(new Tool(VoltageSourceAC.class));
		tools.add(new Tool(Resistor.class));
		tools.add(new Tool(TOOLTYPE.DRAW));
		tools.add(new Tool(Ground.class));
		tools.add(new Tool(SPST.class));
		tools.add(new Tool(SPDT.class));
		tools.add(new Tool(Capacitor.class));
		tools.add(new Tool(Inductor.class));
		tools.add(new Tool(Diode.class));
		tools.add(new Tool(LED.class));
		tools.add(new Tool(BJT.class));
		tools.add(new Tool(FET.class));
		tools.add(selectedTool = new Tool(TOOLTYPE.SELECT));
		tools.add(new Tool(TOOLTYPE.MOVE));
		return tools;
	}
	
	public enum TOOLTYPE { COMPONENT, SELECT, MOVE, DRAW };
	
	public class Tool
	{
		Pane pane;
		Tooltip tooltip;
		ComponentGUI component;
		Cursor cursor = Cursor.DEFAULT;
		TOOLTYPE type;
		
		public Tool(Class<? extends ComponentGUI> type)
		{
			ComponentGUI c;
			try { c = type.newInstance(); }
			catch (InstantiationException ex)
			{
				System.err.println("uh-oh, you friggin moron, you forgot to write a default constructor for class " + type.getName());
				return;
			}
			catch (IllegalAccessException ignore) { ignore.printStackTrace(); return; }
			
			c.initGUI();
			GraphicalNode node = c.getGraphicalNode(0, 0);
			node.setOnPhysicalConfigurationChanged(e -> this.loadCursor());
			node.setOnMouseMoved(null);			// Disable tooltip
			node.addEventFilter(KeyEvent.KEY_PRESSED, e ->
			{
				if (SandboxMenuBar.HK_PROPERTIES.match(e))
					new PropertiesDialog(Toolbox.this, component).show();
				if (SandboxMenuBar.HK_ROTATECW.match(e))
					node.rotateNode(1);
				if (SandboxMenuBar.HK_ROTATECCW.match(e))
					node.rotateNode(3);
				if (SandboxMenuBar.HK_FLIPH.match(e))
					node.flipNode(true, false);
				if (SandboxMenuBar.HK_FLIPV.match(e))
					node.flipNode(false, true);
			});
			
			this.pane = node;
			this.tooltip = new Tooltip(c.getSimpleName());
			this.component = c;
			this.type = TOOLTYPE.COMPONENT;
		}
		
		public Tool(TOOLTYPE t)
		{
			pane = new Pane();
			
			ImageView img;
			switch (t)
			{
			case SELECT:
				img = new ImageView(Main.class.getClassLoader()
				.getResource("images/cursor.png")
				.toString());
				tooltip = new Tooltip("Select");
				cursor = Cursor.CROSSHAIR;
				break;
			case MOVE:
				img = new ImageView(Main.class.getClassLoader()
				.getResource("images/hand.png")
				.toString());
				tooltip = new Tooltip("Move");
				cursor = Cursor.HAND;
				break;
			case DRAW:
				Image pencil = new Image(Main.class.getClassLoader()
						.getResource("images/pencil.png")
						.toString());
				img = new ImageView(pencil);
				tooltip = new Tooltip("Draw");
				cursor = new ImageCursor(pencil, 20, 80);
				break;
			default:
				return;
			}
			
			img.fitWidthProperty().bind(pane.heightProperty());
			img.fitHeightProperty().bind(pane.heightProperty());
			
			pane.getChildren().add(img);
			type = t;
		}
		
		public void loadCursor()
		{
			if (this.type == TOOLTYPE.COMPONENT)
			{
				Background saveBackground = this.pane.getBackground();
				String saveStyle = this.pane.getStyle();
				this.pane.setBackground(null);
				this.pane.setStyle(null);

				SnapshotParameters params = new SnapshotParameters();
				params.setFill(Color.TRANSPARENT);
				WritableImage img = this.pane.snapshot(params, new WritableImage((int)this.pane.getWidth(), (int)this.pane.getHeight()));
				this.cursor = new ImageCursor(img, img.getWidth() / 2, img.getHeight() / 2);

				this.pane.setBackground(saveBackground);
				this.pane.setStyle(saveStyle);
			}
			
			sandbox.mainPane.setCursor(this.cursor);
		}
	}
}
