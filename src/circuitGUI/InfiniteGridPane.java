package circuitGUI;


import circuit.GraphicalNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import wrapper.Wrapper;
import circuit.ComponentGUI;

public class InfiniteGridPane extends Pane
{
	private double cellSize = 50;
	private Point2D origin = new Point2D(0, 0);
	private Position cursor = new Position(0, 0);
	private final Set<Position> selection = new HashSet<>();
	private final ArrayList<ArrayList<GraphicalNode>> grid = new ArrayList<>();
	
	public InfiniteGridPane()
	{
		this.widthProperty().addListener(e -> this.update());
		this.heightProperty().addListener(e -> this.update());
		
		Wrapper<Position> selectStart = new Wrapper<>();
		this.addEventFilter(KeyEvent.ANY, e ->
		{
			if (!this.isSelected(cursor))
				e.consume();
			
			if (e.getEventType().equals(KeyEvent.KEY_PRESSED))
				switch(e.getCode())
				{
				case SHIFT:
					selectStart.v = this.cursor;
					break;
				case UP:
				case KP_UP:
					this.cursor = new Position(this.cursor.x, this.cursor.y-1);
					break;
				case DOWN:
				case KP_DOWN:
					this.cursor = new Position(this.cursor.x, this.cursor.y+1);
					break;
				case LEFT:
				case KP_LEFT:
					this.cursor = new Position(this.cursor.x-1, this.cursor.y);
					break;
				case RIGHT:
				case KP_RIGHT:
					this.cursor = new Position(this.cursor.x+1, this.cursor.y);
					break;
				default:
					return;
				}
			if (e.getEventType().equals(KeyEvent.KEY_RELEASED))
				switch (e.getCode())
				{
				case SHIFT:
					selectStart.v = null;
					break;
				}
			
			if (e.getEventType().equals(KeyEvent.KEY_PRESSED))
				this.select(selectStart.v == null ? this.cursor : selectStart.v, this.cursor, true);
			
			this.update();
		});
	}
	
	public Position getCoordinates(MouseEvent e) { return this.getCoordinates(e.getX(), e.getY()); }
	public Position getCoordinates(double cursorPositionX, double cursorPositionY)
	{
		return new Position(
				(int)Math.floor(this.origin.getX() + cursorPositionX / this.cellSize),
				(int)Math.floor(this.origin.getY() + cursorPositionY / this.cellSize));
	}
	
	public void drag(double byX, double byY)
	{
		origin = origin.subtract(new Point2D(byX, byY).multiply(1 / cellSize));
		this.update();
	}
	public void zoom(double centerX, double centerY, double factor)
	{
		double newCellSize = cellSize * (factor + 1);
		if (newCellSize < 10) newCellSize = 10;
		if (newCellSize > 300) newCellSize = 300;
		origin = origin.add(centerX / cellSize, centerY / cellSize).subtract(centerX / newCellSize, centerY / newCellSize);
		cellSize = newCellSize;
		this.update();
	}
	
	public void select(Position p, boolean clearPrevious) { this.select(p, p, clearPrevious); }
	public void select(Position from, Position to, boolean clearPrevious)
	{
		if (clearPrevious)
			this.selection.clear();
		
		for (int i = Math.min(from.x, to.x); i <= Math.max(from.x, to.x); i++)
			for (int j = Math.min(from.y, to.y); j <= Math.max(from.y, to.y); j++)
				this.selection.add(new Position(i, j));
		
		this.cursor = to;
		
		if (this.cursor.x < origin.getX())
			this.origin = new Point2D(this.cursor.x, this.origin.getY());
		else if (this.cursor.x + 1 > origin.getX() + this.getWidth() / this.cellSize)
			this.origin = new Point2D(this.cursor.x - this.getWidth() / this.cellSize + 1, this.origin.getY());
		if (this.cursor.y < origin.getY())
			this.origin = new Point2D(this.origin.getX(), this.cursor.y);
		else if (this.cursor.y + 1 > origin.getY() + this.getHeight() / this.cellSize)
			this.origin = new Point2D(this.origin.getX(), this.cursor.y - this.getHeight() / this.cellSize + 1);
	}
	
	public void unselect(Position p) { this.selection.remove(p); this.cursor = p; }
	public void clearSelection() { this.selection.clear(); }
	public boolean isSelected(Position p) { return this.selection.contains(p); }
	public Set<Position> getSelection() { return Collections.unmodifiableSet(this.selection); }
	public Position getCursorPosition() { return this.cursor; }
	
	public void set(GraphicalNode n, Position p) { this.set(n, p.x, p.y); }
	public void set(GraphicalNode n, int x, int y)
	{
		x = (x >= 0) ? x * 2 : x * -2 + 1;
		y = (y >= 0) ? y * 2 : y * -2 + 1;
		
		for (int i = grid.size(); i <= x; i++)
			grid.add(new ArrayList<>());
		for (int i = grid.get(x).size(); i <= y; i++)
			grid.get(x).add(null);
		grid.get(x).set(y, n);
	}
	
	public GraphicalNode get(Position p) { return this.get(p.x, p.y); }
	public GraphicalNode get(int x, int y)
	{
		x = (x >= 0) ? x * 2 : x * -2 + 1;
		y = (y >= 0) ? y * 2 : y * -2 + 1;
		
		if (x >= grid.size() || y >= grid.get(x).size())
			return null;
		else
			return grid.get(x).get(y);
	}
	
	private List<Cell> cells = new ArrayList<>();
	public synchronized void update()
	{
		this.getChildren().removeIf(c -> (! (c instanceof Cell)));
		this.requestFocus();
		
		int cellIndex = 0;
		int x = (int)Math.floor(origin.getX());
		for (double layoutX = (origin.getX() - x) * cellSize * -1; layoutX < this.getWidth(); layoutX += cellSize, x++)
		{
			boolean addCoordinatesY = (layoutX <= 0);
			int y = (int)Math.floor(origin.getY());
			for (double layoutY = (origin.getY() - y) * cellSize * -1; layoutY < this.getHeight(); layoutY += cellSize, y++)
			{
				assert cellIndex <= cells.size();
				if (cellIndex == cells.size())
					cells.add(new Cell());
				
				cells.get(cellIndex).setSelected(selection.contains(new Position(x, y)));
				cells.get(cellIndex).setPrefSize(cellSize, cellSize);
				cells.get(cellIndex).setLayoutX(layoutX);
				cells.get(cellIndex).setLayoutY(layoutY);
				cells.get(cellIndex).getChildren().clear();
				
				if (!this.getChildren().contains(cells.get(cellIndex)))
					this.getChildren().add(cells.get(cellIndex));
				
				GraphicalNode n = this.get(x, y);
				if (n != null)
				{
					n.setPrefSize(cellSize, cellSize);
					cells.get(cellIndex).getChildren().add(n);
					
					if (new Position(x, y).equals(this.cursor))
						n.requestFocus();
				}
				
				if (addCoordinatesY)
				{
					Text coordinateY = new Text(y + "");
					coordinateY.setLayoutX(0);
					coordinateY.setLayoutY((layoutY > 0 ? layoutY : 0) + coordinateY.getFont().getSize());
					this.getChildren().add(coordinateY);
				}
				
				cellIndex++;
			}
			
			Text coordinateX = new Text(x + "");
			coordinateX.setLayoutX(layoutX > 0 ? layoutX : 0);
			coordinateX.setLayoutY(0 + coordinateX.getFont().getSize());
			this.getChildren().add(coordinateX);
		}
		
		for (; cellIndex < cells.size(); cellIndex++)
			this.getChildren().remove(cells.get(cellIndex));
	}
	
	private class Cell extends Pane
	{
		public void setSelected(boolean selected)
		{
			this.setBackground(selected ? Theme.SELECTED_CELL_BG : Theme.NORMAL_CELL_BG);
		}
	}
	
	public Set<ComponentGUI> getComponents()
	{
		Set<ComponentGUI> set = new HashSet<>();
		for (ArrayList<GraphicalNode> a : this.grid)
			for (GraphicalNode n : a)
				if (n != null)
					set.add(n.getGUI());
		return set;
	}
}