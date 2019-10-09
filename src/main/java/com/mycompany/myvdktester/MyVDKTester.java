package com.mycompany.myvdktester;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author HABETINOVAP
 */
public class MyVDKTester extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/fxml/MyVDKTesterFXML.fxml"));
        Parent root = null;
        try {
            root = (Parent)fxmlloader.load();
        } catch (IOException ex) {
            Logger.getLogger(DataProviderInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scene scene = new Scene(root);
        stage.setTitle("VDKTester");
        Image icon = new Image(getClass().getResourceAsStream("/favicon.png"));
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show(); 
    }
 
    public static void main(String[] args) throws IOException {
       launch(args);
    }
}


   
   
                     
                
    





   
  
