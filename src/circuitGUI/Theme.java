package circuitGUI;


import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class Theme
{
	public static Background NORMAL_CELL_BG;
	public static Background SELECTED_CELL_BG;
	public static String CELL_STYLE;
	
	public Theme()
	{
		NORMAL_CELL_BG = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
		SELECTED_CELL_BG = new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY));
		CELL_STYLE = "";
	}
}
