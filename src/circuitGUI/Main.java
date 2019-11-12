package circuitGUI;


import java.io.File;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application
{
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		System.out.println("hi");
		
		new Theme();
		
		for (String file : this.getParameters().getRaw())
		{
			Sandbox sandbox = new Sandbox(new File(file));
			if (sandbox.currentFile != null) sandbox.show();
		}
		
		if (this.getParameters().getRaw().isEmpty())
			new Sandbox().show();
	}
	
	public static void main(String[] args) throws Exception
	{
		launch(args);
	}
}