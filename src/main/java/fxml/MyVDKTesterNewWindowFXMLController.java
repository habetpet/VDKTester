package fxml;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author HABETINOVAP
 */
public class MyVDKTesterNewWindowFXMLController implements Initializable {  
    @FXML
    private Button loadFile;
    @FXML
    private ComboBox<String> chooseSourceCB;
    @FXML
    private Button fileChooserButton;
    @FXML
    private Label fileDirectory;
    
     
    
    ObservableList<String> sources = FXCollections.observableArrayList
        ("UKF", "MZK", "VKOL", "NKF", "SVKHK", "CBVK", "KVKLI", "RF");
    
    Window stage = null;
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV File", "*.csv");
    String filePath;
    String source;
    File file;


    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chooseSourceCB.setItems(sources);
    }
    
    public String getPath(){
        filePath = file.getPath();
        return filePath;
    }
    
    public String getSource(){
        source = chooseSourceCB.getValue();
        return source;
    }
    
    private List<String> getAllSources(){
        List<String> allSources = new ArrayList<String>();
        for(int i = 0; i < this.sources.size(); i++) {
            allSources.add(sources.get(i));
        }
        return allSources;
    }
    
    public HashMap getSelectedSourcesResult() {
        HashMap<String, Boolean> selectionResult = new HashMap<String, Boolean>();
        List<String> allSources = this.getAllSources();
        this.getSource();
        for(int i = 0; i < this.sources.size(); i++) {
            if(allSources.get(i) == null ? this.source != null : !allSources.get(i).equals(this.source)) {
                selectionResult.put(allSources.get(i), Boolean.FALSE);
            }
            else {
                selectionResult.put(allSources.get(i), Boolean.TRUE);
            }
        }
        return selectionResult;
    }

    @FXML
    private void openFileChooser(ActionEvent event) {
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Open Resource File");       
        file = fileChooser.showOpenDialog(stage);      
        fileDirectory.setText(file.getName());
    }
    
    @FXML
    private void choosingSource(ActionEvent event) {
        source = chooseSourceCB.getValue();
    }
    
    @FXML
    private void loadAndCloseWindow(ActionEvent event) {
        if (file != null && source != null) {              
            ((Node)(event.getSource())).getScene().getWindow().hide();            
        }
        fileDirectory.setText("Vyber soubor a knihovnu!");
    }


    

    
}
