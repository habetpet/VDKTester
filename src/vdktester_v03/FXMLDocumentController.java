/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vdktester_v03;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

/**
 *
 * @author Martin
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private Button chooseBT;
    
    @FXML
    private Button loadBT;
    
    @FXML
    private ComboBox sourceCB;
    
    @FXML
    private Button startBT;
    
    @FXML
    private Button saveUniquesBT;
    
    @FXML
    private Button saveDuplicitiesHTMLBT;
    
    @FXML
    private Button saveDuplicitiesCSVBT;
    
    @FXML
    private Button helpBT;
    
    @FXML
    private TextArea logTA;
    
    @FXML
    private ProgressBar bar;
    
    @FXML
    private ProgressIndicator indicator;
    
    @FXML
    private void choose(ActionEvent event) {
        Settings.csvFile = new FileChooser().showOpenDialog(null);
        logTA.setText("File: " + Settings.csvFile.getAbsolutePath() 
                + "\n\n1. Choose .csv file you want to check\n"
                + ">>2. Select source\n"
                + "3. Load file in application\n"
                + "4. press Start\n"
                + "5. Save results");
    }
    
    @FXML
    private void load(ActionEvent event) {
        
        if(sourceCB.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("zdroj")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Opps!");
            alert.setHeaderText("Něco nám tu nehraje...");
            alert.setContentText("Nejdříve vyberte zdroj");
            alert.showAndWait();
            return;
        }
        
        if(Settings.csvFile == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Opps!");
            alert.setHeaderText("Něco nám tu nehraje...");
            alert.setContentText("file == null");
            alert.showAndWait();
            return;
        }
        
        LoadCSV task = new LoadCSV(sourceCB.getSelectionModel().getSelectedItem().toString(), logTA);
        bar.progressProperty().bind(task.progressProperty());
        indicator.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }
    
    @FXML
    private void startWork(ActionEvent event) {
        logTA.setText("WORKING\n"
                + "this might take a while\n"
                + "so please wait...");
        DoWork task = new DoWork(logTA);
        bar.progressProperty().bind(task.progressProperty());
        indicator.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }
    
    @FXML
    private void saveUniques(ActionEvent event) {
        File file = new FileChooser().showSaveDialog(null);
        SaveUniques task = new SaveUniques(file, logTA);
        bar.progressProperty().bind(task.progressProperty());
        indicator.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }
    
    @FXML
    private void saveDuplicitiesHTML(ActionEvent event) {
        File file = new FileChooser().showSaveDialog(null);
        SaveDuplicitiesHTML task = new SaveDuplicitiesHTML(file, logTA);
        bar.progressProperty().bind(task.progressProperty());
        indicator.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }
    
    @FXML
    private void saveDuplicitiesCSV(ActionEvent event) {
        File file = new FileChooser().showSaveDialog(null);
        SaveDuplicitiesCSV task = new SaveDuplicitiesCSV(file, logTA);
        bar.progressProperty().bind(task.progressProperty());
        indicator.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }
    
    @FXML
    private void help(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URL("http://nkp.svrcina.eu/version.html").toURI());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        sourceCB.getItems().clear();
        sourceCB.getItems().addAll("zdroj", "NKF", "UKF", "MZK", "VKOL", "KVKLI", "CBVK", "SVKHK");
        sourceCB.getSelectionModel().select(0);
        
        sourceCB.setOnAction((event) -> {
            
            logTA.setText("Source selected: " + sourceCB.getSelectionModel().getSelectedItem().toString() + "\n\n"
                    + "1. Choose .csv file you want to check\n"
                    + "2. Select source\n"
                    + ">> 3. Load file in application\n"
                    + "4. press Start\n"
                    + "5. Save results");
            
        });
        
        logTA.setText(">> 1. Choose .csv file you want to check\n"
                + "2. Select source\n"
                + "3. Load file in application\n"
                + "4. press Start\n"
                + "5. Save results");
    }    
    
}
