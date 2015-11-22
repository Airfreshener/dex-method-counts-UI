package info.persistent.dex;

import info.persistent.dex.controllers.MainController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;

/**
 * Created by Vasiliy on 21.11.2015
 */
public class MainUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ClassLoader resLoader = getClass().getClassLoader();
        if(resLoader == null)
            throw new RuntimeException("Class loader can not be null");
        URL xml = resLoader.getResource("main.fxml");
        if(xml == null)
            throw new RuntimeException("Resource not found.");
        FXMLLoader xmlLoader = new FXMLLoader(xml);
        Parent root = xmlLoader.load();
        MainController controller = xmlLoader.getController();
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        final int WIDTH = 300, HEIGHT = 200;
        Scene scene = new Scene(root, 600, screenSize.getHeight() - 50);
        controller.setScene(scene);
        primaryStage.setTitle("Dex Method Counts");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMinHeight(200);
        primaryStage.setMinWidth(300);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

    }

    public static void main(String[] args) {
        launch(args);

        //Main main = new Main();
        //main.run(args);
    }
}
