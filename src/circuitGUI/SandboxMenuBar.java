/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuitGUI;

import circuit.GraphicalNode;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import jfx.messagebox.MessageBox;
import org.w3c.dom.Element;
import wrapper.Wrapper;
import circuit.ComponentGUI;

class SandboxMenuBar extends MenuBar
{
	public static final KeyCombination HK_NEW = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_OPEN = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_SAVE = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_SAVEAS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	public static final KeyCombination HK_CLOSE = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_PROPERTIES = new KeyCodeCombination(KeyCode.ENTER);
	public static final KeyCombination HK_CUT = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_PASTE = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_DELETE = new KeyCodeCombination(KeyCode.DELETE);
	public static final KeyCombination HK_SELECTALL = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination HK_ROTATECW = new KeyCodeCombination(KeyCode.R);
	public static final KeyCombination HK_ROTATECCW = new KeyCodeCombination(KeyCode.R, KeyCombination.SHIFT_DOWN);
	public static final KeyCombination HK_FLIPH = new KeyCodeCombination(KeyCode.F);
	public static final KeyCombination HK_FLIPV = new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN);
	public static final KeyCombination HK_SELECT = new KeyCodeCombination(KeyCode.S);
	public static final KeyCombination HK_MOVE = new KeyCodeCombination(KeyCode.M);
	public static final KeyCombination HK_DRAW = new KeyCodeCombination(KeyCode.D);
	public static final KeyCombination HK_TOOLBOX = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
	
	private final static Set<Position<Element>> CLIPBOARD = new HashSet<>();
	
	private FileChooser fileChooser = new FileChooser();
	
	public ContextMenu contextMenu = null;
	
	public SandboxMenuBar(Sandbox sandbox)
	{
		Menu file, edit, breadboard, tools;
		{
			file = new Menu("_File");
			{
				MenuItem _new, open, save, saveAs, close, exit;
				_new = new MenuItem("New");
				open = new MenuItem("Open");
				save = new MenuItem("Save");
				saveAs = new MenuItem("Save As");
				close = new MenuItem("Close");
				exit = new MenuItem("E_xit");
				_new.setAccelerator(HK_NEW);
				open.setAccelerator(HK_OPEN);
				save.setAccelerator(HK_SAVE);
				saveAs.setAccelerator(HK_SAVEAS);
				close.setAccelerator(HK_CLOSE);

				fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Circuit Circus files", ".cir"));

				_new.setOnAction(e -> new Sandbox().show());

				open.setOnAction(e ->
				{
					File newFile = fileChooser.showOpenDialog(sandbox);
					if (newFile == null) 
						return;

					if (sandbox.mainPane.getComponents().isEmpty())
						sandbox.openFile(newFile);
					else
					{
						Sandbox s = new Sandbox(newFile);
						if (s.currentFile != null)
							s.show();
					}
				});

				save.setOnAction(e ->
				{
					if (sandbox.currentFile == null) saveAs.fire();
					else sandbox.saveFile();
				});

				saveAs.setOnAction(e ->
				{
					File newFile = fileChooser.showSaveDialog(sandbox);
					if (newFile == null)
						return;
					if (!newFile.toString().endsWith(".cir"))
						newFile = new File(newFile.toString() + ".cir");
					sandbox.saveToFile(newFile);
				});

				close.setOnAction(e ->
				{
					if (sandbox.fileModified.get())
						switch (MessageBox.show(sandbox, "Save changes to '" + sandbox.getTitle().replaceAll("^\\*", "") + "' before closing?",
								"", MessageBox.ICON_WARNING | MessageBox.YES | MessageBox.NO | MessageBox.CANCEL))
						{
						case MessageBox.YES:
							save.fire();
							if (sandbox.fileModified.get()) return;
							else sandbox.close();
							break;
						case MessageBox.NO:
							sandbox.close();
							break;
						case MessageBox.CANCEL:
							return;
						}
					else
						sandbox.close();
					
					if (Sandbox.sandboxes.isEmpty())
						System.exit(0);
				});
				
				exit.setOnAction(e -> Sandbox.sandboxes.forEach(s ->
				{
					if (s != null) s.getOnCloseRequest().handle(null);
				}));
				
				file.getItems().addAll(
						_new,
						new SeparatorMenuItem(),
						open,
						new SeparatorMenuItem(),
						save,
						saveAs,
						new SeparatorMenuItem(),
						close,
						new SeparatorMenuItem(),
						exit);
				
				sandbox.setOnCloseRequest(e -> { close.fire(); if (e != null) e.consume(); });
			}

			edit = new Menu("_Edit");
			{
				MenuItem cut, copy, paste, delete, selectAll, rotate1, rotate3, flipH, flipV, properties;
				properties = new MenuItem("Properties...");
				properties.setStyle("-fx-font-weight: bold;");
				cut = new MenuItem("Cut");
				copy = new MenuItem("Copy");
				paste = new MenuItem("Paste");
				delete = new MenuItem("Delete");
				selectAll = new MenuItem("Select All");
				rotate1 = new MenuItem("Rotate 90° CW");
				rotate3 = new MenuItem("Rotate 90° CCW");
				flipH = new MenuItem("Flip Horizontal");
				flipV = new MenuItem("Flip Vertical");
				properties.setAccelerator(HK_PROPERTIES);
				cut.setAccelerator(HK_CUT);
				copy.setAccelerator(HK_COPY);
				paste.setAccelerator(HK_PASTE);
				delete.setAccelerator(HK_DELETE);
				selectAll.setAccelerator(HK_SELECTALL);
				rotate1.setAccelerator(HK_ROTATECW);
				rotate3.setAccelerator(HK_ROTATECCW);
				flipH.setAccelerator(HK_FLIPH);
				flipV.setAccelerator(HK_FLIPV);

				edit.setOnShowing(e ->
				{
					cut.setDisable(sandbox.mainPane.getSelection().isEmpty());
					copy.setDisable(sandbox.mainPane.getSelection().isEmpty());
					paste.setDisable(sandbox.mainPane.getSelection().isEmpty() || SandboxMenuBar.CLIPBOARD.isEmpty());
					delete.setDisable(sandbox.mainPane.getSelection().isEmpty());
					rotate1.setDisable(sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()) == null);
					rotate3.setDisable(rotate1.disableProperty().get());
					flipH.setDisable(rotate1.disableProperty().get());
					flipV.setDisable(rotate1.disableProperty().get());
					properties.setDisable(rotate1.disableProperty().get() ||
							sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()).getGUI().getProperties().isEmpty());
				});
				
				edit.setOnHidden(e -> edit.getItems().forEach(i -> i.setDisable(false)));
				
				properties.setOnAction(e ->
				{
					GraphicalNode node = sandbox.mainPane.get(sandbox.mainPane.getCursorPosition());
					if (node != null && !node.getGUI().getProperties().isEmpty())
					{
						for (PropertiesDialog d : sandbox.propertyDialogs)
							if (node.getGUI() == d.component)
							{
								d.show();
								d.requestFocus();
								return;
							}
						
						new PropertiesDialog(sandbox, node.getGUI()).show();
					}
				});
				
				Wrapper<Boolean> isCut = new Wrapper<>(false);

				cut.setOnAction(e ->
				{
					copy.fire();
					for (Position p : sandbox.mainPane.getSelection())
						if (sandbox.mainPane.get(p.x, p.y) != null)
							sandbox.mainPane.get(p.x, p.y).setOpacity(0.5);
					isCut.v = true;
				});

				copy.setOnAction(e ->
				{
					for (Position p : CLIPBOARD)
						if (sandbox.mainPane.get(p.x, p.y) != null)
							sandbox.mainPane.get(p.x, p.y).setOpacity(1.0);
					CLIPBOARD.clear();

					Set<ComponentGUI> components = new HashSet<>();
					for (Position p : sandbox.mainPane.getSelection())
						if (sandbox.mainPane.get(p.x, p.y) != null)
							components.add(sandbox.mainPane.get(p.x, p.y).getGUI());
					for (ComponentGUI c : components)
						CLIPBOARD.add(new Position(
								c.getGraphicalNode(0, 0).getPosition().x,
								c.getGraphicalNode(0, 0).getPosition().y,
								c.serialize()));

					isCut.v = false;
				});

				paste.setOnAction(e ->
				{
					Position source = getMinPos((Set)CLIPBOARD);
					Position target = getMinPos((Set)sandbox.mainPane.getSelection());

					sandbox.mainPane.clearSelection();
					if (isCut.v) for (Position<Element> p : CLIPBOARD)
						sandbox.deleteComponent(p.x, p.y);
					for (Position<Element> p : CLIPBOARD)
					{
						try { sandbox.addComponent(p.value, target.x - source.x + p.x, target.y - source.y + p.y); }
						catch (Exception ex) { assert false; }
						sandbox.mainPane.select(new Position(target.x - source.x + p.x, target.y - source.y + p.y), false);
					}

					sandbox.mainPane.update();
					isCut.v = false;
				});

				delete.setOnAction(e -> sandbox.mainPane.getSelection().forEach(p -> sandbox.deleteComponent(p.x, p.y)));

				selectAll.setOnAction(e ->
				{
					sandbox.mainPane.clearSelection();
					for (ComponentGUI c : sandbox.mainPane.getComponents())
						for (int i = 0; c.getGraphicalNode(i, 0) != null; i++)
							for (int j = 0; c.getGraphicalNode(i, j) != null; j++)
								sandbox.mainPane.select(c.getGraphicalNode(i, j).getPosition(), false);
				});
				
				rotate1.setOnAction(e ->
				{
					if (sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()) != null)
						sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()).rotateNode(1);
				});
				rotate3.setOnAction(e ->
				{
					if (sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()) != null)
						sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()).rotateNode(3);
				});
				
				flipH.setOnAction(e ->
				{
					if (sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()) != null)
						sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()).flipNode(true, false);
				});
				flipV.setOnAction(e ->
				{
					if (sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()) != null)
						sandbox.mainPane.get(sandbox.mainPane.getCursorPosition()).flipNode(false, true);
				});
				
				edit.getItems().addAll(
						properties,
						new SeparatorMenuItem(),
						cut,
						copy,
						paste,
						delete,
						new SeparatorMenuItem(),
						selectAll,
						new SeparatorMenuItem(),
						rotate1,
						rotate3,
						new SeparatorMenuItem(),
						flipH,
						flipV);
				
				this.contextMenu = new ContextMenu();
				this.contextMenu.setOnShowing(e -> edit.getOnShowing().handle(e));
				this.contextMenu.setOnHidden(e -> edit.getOnHidden().handle(e));
				for (MenuItem item : edit.getItems())
				{
					if (item instanceof SeparatorMenuItem)
					{
						this.contextMenu.getItems().add(new SeparatorMenuItem());
						continue;
					}
					MenuItem newItem = new MenuItem();
					newItem.textProperty().set(item.textProperty().get());
					newItem.styleProperty().set(item.styleProperty().get());
					newItem.acceleratorProperty().set(item.acceleratorProperty().get());
					newItem.disableProperty().bind(item.disableProperty());
					newItem.onActionProperty().set(item.onActionProperty().get());
					this.contextMenu.getItems().add(newItem);
				}
			}
			
			breadboard = new Menu("_Breadboard");
			{
				MenuItem tick, speed;
				tick = new MenuItem("Tick");
				speed = new MenuItem("Set Simulation Speed...");
				tick.setAccelerator(new KeyCodeCombination(KeyCode.T));
				
				tick.setOnAction(e -> sandbox.breadboard.tick(0));
				
				speed.setOnAction(e ->
				{
					while (true)
					{
						String s = InputBox.show(sandbox, sandbox.breadboard.speed.getStringValue(), "Set Simulation Speed");
						if (s == null) break;
						try { sandbox.breadboard.speed.setStringValue(s); }
						catch (Exception ex)
						{
							switch (MessageBox.show(sandbox, "'" + s + "' is not a valid double value. Please enter a valid input.",
									"Invalid input", MessageBox.ICON_ERROR | MessageBox.OK | MessageBox.CANCEL))
							{
							case MessageBox.OK: continue;
							case MessageBox.CANCEL: return;
							}
						}
						break;
					}
				});
				
				breadboard.getItems().addAll(
						tick,
						speed);
			}
			
			tools = new Menu("_Tools");
			{
				MenuItem select, move, draw, toolbox;
				select = new MenuItem("Select");
				move = new MenuItem("Move");
				draw = new MenuItem("Draw");
				toolbox = new MenuItem("Show Toolbox");
				select.setAccelerator(HK_SELECT);
				move.setAccelerator(HK_MOVE);
				draw.setAccelerator(HK_DRAW);
				toolbox.setAccelerator(HK_TOOLBOX);
				
				select.setOnAction(e -> sandbox.toolbox.selectTool(Toolbox.TOOLTYPE.SELECT));
				move.setOnAction(e -> sandbox.toolbox.selectTool(Toolbox.TOOLTYPE.MOVE));
				draw.setOnAction(e -> sandbox.toolbox.selectTool(Toolbox.TOOLTYPE.DRAW));
				
				
				toolbox.setOnAction(e ->
				{
					sandbox.toolbox.show();
					sandbox.toolbox.requestFocus();
				});
				
				tools.getItems().addAll(
						select,
						move,
						draw,
						new SeparatorMenuItem(),
						toolbox);
			}
		}

		this.getMenus().addAll(file, edit, breadboard, tools);
	}
	
	private Position getMinPos(Set<Position<?>> s)
	{
		int x = Integer.MAX_VALUE;
		int y = Integer.MAX_VALUE;
		
		for (Position p : s)
		{
			if (p.x < x) x = p.x;
			if (p.y < y) y = p.y;
		}
		
		return new Position(x, y);
	}
}