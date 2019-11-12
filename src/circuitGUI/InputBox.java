
package circuitGUI;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import wrapper.Wrapper;

public class InputBox
{
	public static String show(Window owner, String initialValue, String title)
	{
		TextField text = new TextField(initialValue);
		
		Stage stage = new Stage();
		stage.setScene(new Scene(text));
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setResizable(true);
		stage.setTitle(title);
		
		wrapper.Wrapper<Boolean> isAccept = new Wrapper<>(false);
		text.setOnAction(e ->
		{
			stage.close();
			isAccept.v = true;
		});
		text.selectAll();
		text.requestFocus();
		
		stage.showAndWait();
		
		return isAccept.v ? text.getText() : null;
	}
}
