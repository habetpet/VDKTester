package com.mycompany.myvdktester;

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author HABETINOVAP
 */
public class MyVDKTester extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/fxml/MyVDKTesterFXML.fxml"));
        Parent root = (Parent)fxmlloader.load();
        Scene scene = new Scene(root);
        stage.setTitle("VDKTester");
        stage.setScene(scene);
        stage.show();
    }
 
    public static void main(String[] args) throws IOException {
       launch(args);
    }
}


   
   
                     
                
    





   
  
