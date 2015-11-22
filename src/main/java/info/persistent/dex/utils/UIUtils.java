package info.persistent.dex.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;

/**
 * Created by Vasiliy on 22.11.2015
 */
public class UIUtils {

    public static void infoBox(String infoMessage, String titleBar, String headerMessage){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(!titleBar.isEmpty()? titleBar : alert.getAlertType().name());
        if(!headerMessage.isEmpty())
            alert.setHeaderText(headerMessage);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }

    public static void warningBox(String infoMessage, String titleBar, String headerMessage){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(!titleBar.isEmpty()? titleBar : alert.getAlertType().name());
        if(!headerMessage.isEmpty())
            alert.setHeaderText(headerMessage);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }

    public static void errorBox(String infoMessage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }

    public static void bindSize(Pane parent, Control child){
        child.prefWidthProperty().bind(parent.widthProperty());
        child.prefHeightProperty().bind(parent.heightProperty());
    }

    public static  void bindSize(Pane parent, Pane child){
        child.prefWidthProperty().bind(parent.widthProperty());
        child.prefHeightProperty().bind(parent.heightProperty());
    }

    public static  void checkInjects(Object... controls){
        for(int i = 0 ; i < controls.length ; i++)
            if (controls[i] == null) throw new RuntimeException("Control position " + i + " was not inject!");
    }

}
