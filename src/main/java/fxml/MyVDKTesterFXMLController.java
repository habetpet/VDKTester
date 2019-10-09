/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxml;

import com.mycompany.myvdktester.CSVParserAndWriter;
import com.mycompany.myvdktester.CSVAndWebDataProvider;
import com.mycompany.myvdktester.DocumentManager;
import com.mycompany.myvdktester.HTMLParser;
import com.mycompany.myvdktester.ItemSorter;
import com.mycompany.myvdktester.URLBuilder;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * FXML Controller class
 *
 * @author HABETINOVAP
 */
public class MyVDKTesterFXMLController implements Initializable {
    
    String filePath = null;
    String fileName = null;
    String source = null;
    String leftURL;
    String rightURL;
    String leftURLOfMARC21Record;
    String rightURLOfMARC21Record;
    String mainID;
    Integer numberOfRows;
    Integer i = 0;
    Integer numberOfDuplicities = 0;
    ArrayList<String[]> lines = new ArrayList<>();
    ArrayList<Integer> positionOfUniques = new ArrayList<>();
    ArrayList<Integer> positionOfDuplicities = new ArrayList<>();
    ArrayList<String> selectedIDsForHighlighting = new ArrayList<>();
    String[] titlesForComparison;
    String[] publicationDatesForComparison;
    HashMap<String, Boolean> selectionResult = new HashMap<>();
    HashMap<String, String[]> csvData;
    Window stage = null;   
    FileChooser fc = new FileChooser();
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV File", "*.csv");
    URLBuilder urlBuilder;
    HTMLParser htmlParser;
    CSVAndWebDataProvider csvAndWebDataProvider;
    Service serviceAutomaticSorter;
    Service serviceManualSorter;
    Elements allItemsInListOnLeft;
    
    @FXML
    private Button newProjectButton;
    @FXML
    private Button automaticSearchButton;
    @FXML
    private Button manualSearchButton;
    @FXML
    private Button addToUniquesButton;
    @FXML
    private Button addToDuplicitiesButton;
    @FXML
    private WebView webView1;
    @FXML
    private WebView webView2;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button reloadLeftButton;
    @FXML
    private Button reloadRightButton;
    @FXML
    private Button checkFieldsButton;
    @FXML
    private TextFlow textFlow;
    @FXML
    private MenuButton saveButton;
    @FXML
    private Button undoButton;

    public MyVDKTesterFXMLController() {
        this.urlBuilder = new URLBuilder();
        this.htmlParser = new HTMLParser();
        this.csvAndWebDataProvider = new CSVAndWebDataProvider();
        
        // Najednou roztřídí všechny dokumenty z csv souboru, podle počtu výsledků ve VDK po dosazení názvu do vyhledávacího políčka pro název
        this.serviceAutomaticSorter = new Service(){
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Void call() throws Exception {
                        checkFieldsButton.setDisable(true);
                        positionOfUniques.clear();
                        positionOfDuplicities.clear();
                        numberOfDuplicities = 0;
                        positionOfUniques = ItemSorter.sortAutomaticItems(titlesForComparison);
                            for(int i = 0; i < numberOfRows; i++) {
                                if(!positionOfUniques.contains(i))
                                positionOfDuplicities.add(i);
                            }

                        Platform.runLater(() -> {
                            Text text = new Text ("Nalezeno celkem " + positionOfUniques.size() + " unikátů a "
                                    + positionOfDuplicities.size() + " duplicit."+ "\n"
                                            + "Zkontrolován soubor " + fileName + " pro knihovnu " + source + ".");
                            textFlow.getChildren().clear();
                            textFlow.getChildren().add(text);
                        });
                        this.updateProgress(0, 0);
                        progressIndicator.setVisible(false);
                        return null;
                    }
                };
            }
        };
        
        // Nahraje do webView1 první dokukument z CSV souboru ve VDK, do webView2 všechny ostatní potenciální duplicity se shodným názvem 
        this.serviceManualSorter = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                 @Override
                 protected Void call() throws URISyntaxException, MalformedURLException, IOException{
                    selectedIDsForHighlighting.clear();
                    checkFieldsButton.setDisable(true);
                    if (i != 0) {
                        undoButton.setDisable(false);
                    }
                    else {
                        undoButton.setDisable(true);
                    }
                    
                    // Název právě zkoumaného dokumentu (bráno z CSV souboru)
                    String currentTitle = titlesForComparison[i]; 
                    String publicationDate = publicationDatesForComparison[i];
                    
                    // Sestavená url webView1
                    leftURL = urlBuilder.buildLeftURL(currentTitle, selectionResult);
                    
                    // Sestavená url webView2
                    rightURL = urlBuilder.buildRightURL(currentTitle);  
                    
                    // Nahrány všechny dokumenty ve webView1
                    allItemsInListOnLeft = htmlParser.getAllItemsInList(DocumentManager.getDocument(leftURL));   
                    
                    // Nalezeno jedinečné ID (v HTML) právě zkoumaného dokumentu 
                    mainID = DocumentManager.getMainID(lines.get(i), allItemsInListOnLeft);
                    
                    // Vybrána všechna ID potenciálních duplicitních dokumentů nahrané ve webViev2 
                    // (s právě prohledávaným dokumentem mají shodu v názvu a v datu vydání)
                    selectedIDsForHighlighting = DocumentManager.getIDsForHighlighting(mainID, currentTitle, publicationDate);
                    

 
                    Platform.runLater(() -> {
                        webView1.getEngine().load(leftURL);
                        webView2.getEngine().load(rightURL);
                        Text text = new Text ("Zkontrolováno " + i + "/" + numberOfRows + " titulů. \n"
                                + "Unikátů: " + positionOfUniques.size() + " Duplicit: " + positionOfDuplicities.size());
                        textFlow.getChildren().clear();
                        textFlow.getChildren().add(text);
                    });
                    progressIndicator.progressProperty().bind(webView1.getEngine().getLoadWorker().progressProperty());
                    progressIndicator.progressProperty().bind(webView2.getEngine().getLoadWorker().progressProperty());
                    this.updateProgress(0, 0);
                    return null;
                    }
                };
            }
        };
    }

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        this.progressIndicator.setVisible(false);
        this.addToDuplicitiesButton.setDisable(true);
        this.addToUniquesButton.setDisable(true);
        this.automaticSearchButton.setDisable(true);
        this.manualSearchButton.setDisable(true);
        this.checkFieldsButton.setDisable(true);
        this.reloadLeftButton.setDisable(true);
        this.reloadRightButton.setDisable(true);
        this.saveButton.setDisable(true);
        this.undoButton.setDisable(true);
        MenuItem saveU = new MenuItem("Uložit unikáty"); 
        MenuItem saveD = new MenuItem("Uložit duplicity"); 
        this.saveButton.getItems().clear();
        this.saveButton.getItems().add(saveU);
        this.saveButton.getItems().add(saveD);
        Text text = new Text ("Prosím vyberte knihovnu a soubor CSV pomocí tlačítka \"Nový projekt\".");
        this.textFlow.getChildren().clear();
        this.textFlow.getChildren().add(text);

        saveU.setOnAction((ActionEvent event) -> {
            try {
                saveUniques(event);
            } catch (IOException ex) {
                Logger.getLogger(MyVDKTesterFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        saveD.setOnAction((ActionEvent event) -> {
            try {
                saveDuplicities(event);
            } catch (IOException ex) {
                Logger.getLogger(MyVDKTesterFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        this.serviceAutomaticSorter.stateProperty().addListener(new ChangeListener<State>(){
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {
            if (newState.equals(State.RUNNING)){
                progressIndicator.setVisible(true);
            }
            }
        });
        
        this.serviceManualSorter.stateProperty().addListener(new ChangeListener<State>(){
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {
            if (newState.equals(State.RUNNING)){
                progressIndicator.setVisible(true);
            }
            }
        });
        
        this.webView1.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {           
            @Override            
            public void changed(ObservableValue ov, State oldState, State newState) {
                if(newState.equals(State.SUCCEEDED)) {
                    progressIndicator.setVisible(false);
                    if(mainID != null){
                        Document doc = webView1.getEngine().getDocument();
                        Element elMain = doc.getElementById(mainID);
                        if(elMain != null && allItemsInListOnLeft != null) {
                            
                            // Zvýraznění právě prohledávaného dokumentu červenou barvou ve webViev1 
                            elMain.setAttribute("style", "background-color:#ffad99;");
                            for(int i = 0; i < allItemsInListOnLeft.size(); i++) {
                                org.jsoup.nodes.Element el = allItemsInListOnLeft.get(i);
                                String elID = el.id();
                                
                                // Schování všech dokumnetů (krom právě prohledávaného ve webViev1)
                                if(el != null && !elID.equals(mainID)) {
                                    Element elHide = doc.getElementById(elID);
                                    elHide.setAttribute("style", "display: none;");
                                    Node line = elHide.getNextSibling().getNextSibling();
                                    
                                    // Schování oddělujících čar mezi jednotlivými dokumenty ve VDK ve webViev1
                                    if (line.getNodeType() == Node.ELEMENT_NODE) {
                                        Element elLine = (Element) line;
                                        elLine.setAttribute("style", "display: none;");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        
        this.webView2.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override           
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState.equals(State.SUCCEEDED)) {
                    progressIndicator.setVisible(false);
                    if(mainID != null) {
                        Document doc = (Document) webView2.getEngine().getDocument();
                        Element elMainID = (Element) doc.getElementById(mainID);
                        
                        // Schování prohledávaného dokumentu mezi potenciálními duplicitami ve webViev2
                        if(elMainID != null) {
                            elMainID.setAttribute("style", "display: none;");
                        }
                        
                        // Pomocné modré obarvení shodných dokumnetů ve webViev2
                        for(int i = 0; i < selectedIDsForHighlighting.size(); i++) {
                            String selectedID = selectedIDsForHighlighting.get(i);
                            Element elID = doc.getElementById(selectedID);                        
                            if(elID != null) {
                                elID.setAttribute("style", "background-color:#80e5ff;");                           
                            }
                        }
                    }
                }
            }
        });
        
        this.webView1.getEngine().locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs1, String oldLocation, String newLocation) {
                leftURLOfMARC21Record = newLocation;
                checkChangedURL (leftURLOfMARC21Record, rightURLOfMARC21Record);               
            }}
        ); 
        
        this.webView2.getEngine().locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs1, String oldLocation, String newLocation) {
                rightURLOfMARC21Record = newLocation;
                checkChangedURL (leftURLOfMARC21Record, rightURLOfMARC21Record);                 
            }}
        ); 

//        this.newProjectButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
//            @Override
//            public void handle(MouseEvent event){
//                if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
//                            positionOfUniques.clear();
//                            positionOfDuplicities.clear();
//                            numberOfDuplicities = 0;
//                            i = 0;
//                            automaticSearchButton.setDisable(false);
//                            manualSearchButton.setDisable(false);
//                            addToDuplicitiesButton.setDisable(true);
//                            addToUniquesButton.setDisable(true);                  
//                            reloadLeftButton.setDisable(true);
//                            reloadRightButton.setDisable(true);
//                            saveButton.setDisable(true);
//                }
//            }        
//        });        
        
        this.automaticSearchButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    webView1.getEngine().loadContent("");
                    webView2.getEngine().loadContent("");
                    positionOfUniques.clear();
                    positionOfDuplicities.clear();
                    numberOfDuplicities = 0;
                    i = 0;
                    addToDuplicitiesButton.setDisable(true);
                    addToUniquesButton.setDisable(true);
                    reloadLeftButton.setDisable(true);
                    reloadRightButton.setDisable(true);
                    undoButton.setDisable(true);
                }
            }        
        });
        
        this.manualSearchButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    positionOfUniques.clear();
                    positionOfDuplicities.clear();
                    numberOfDuplicities = 0;
                    i = 0;
                    addToDuplicitiesButton.setDisable(false);
                    addToUniquesButton.setDisable(false);
                    undoButton.setDisable(false);
                    Text text = new Text ("Zkontrolováno " + i + "/" + numberOfRows + " titulů. \n"
                        + "Unikátů: 0" + " Duplicit: 0");
                    textFlow.getChildren().clear();
                    textFlow.getChildren().add(text);
                }
            }        
        });
        
        this.reloadLeftButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                checkFieldsButton.setDisable(true);
                Text text = new Text ("Zkontrolováno " + i + "/" + numberOfRows + " titulů. \n"
                    + "Unikátů: " + positionOfUniques.size() + " Duplicit: " + positionOfDuplicities.size());
                textFlow.getChildren().clear();
                textFlow.getChildren().add(text);
                webView1.getEngine().load(leftURL);
            }   
        });
        
        this.reloadRightButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                checkFieldsButton.setDisable(true);
                Text text = new Text ("Zkontrolováno " + i + "/" + numberOfRows + " titulů. \n"
                    + "Unikátů: " + positionOfUniques.size() + " Duplicit: " + positionOfDuplicities.size());
                textFlow.getChildren().clear();
                textFlow.getChildren().add(text);
                webView2.getEngine().load(rightURL);
            }   
        });
    }    

    @FXML
    private void newProject(ActionEvent event) throws IOException {
        if((positionOfDuplicities == null && positionOfUniques.isEmpty()) || 
            (filePath == null || source == null)) {   
                // Nové okno s výběrem knihovny (source) a souboru (filePath)
                FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("MyVDKTesterNewWindowFXML.fxml"));
                Parent root = (Parent)fxmlloader.load();     
                Stage stage1 = new Stage();
                stage1.setTitle("VDKTester - Výběr souboru a zdroje.");
                Image icon = new Image(getClass().getResourceAsStream("/favicon.png"));
                stage1.getIcons().add(icon);
                stage1.setScene(new Scene(root));
                stage1.showAndWait();

                // Návrání uživatelských vstupů do proměnných (source, filePath)
                MyVDKTesterNewWindowFXMLController controller = fxmlloader.<MyVDKTesterNewWindowFXMLController>getController();
                this.filePath = controller.getPath();
                this.fileName = controller.getFileName();
                this.source = controller.getSource();
                this.selectionResult  = controller.getSelectedSourcesResult();

                if(this.filePath == null || this.source == null) { 
                    Text text = new Text ("Nebyl nahrán žádný soubor. Prosím vyberte knihovnu a soubor CSV pomocí tlačítka \"Nový projekt\".");
                    this.textFlow.getChildren().clear();
                    this.textFlow.getChildren().add(text);           
                } else {
                    this.positionOfUniques.clear();
                    this.positionOfDuplicities.clear();
                    this.lines.clear();
                    this.numberOfDuplicities = 0;
                    this.i = 0;
                    this.automaticSearchButton.setDisable(false);
                    this.manualSearchButton.setDisable(false);

                    // Náhraní všech položek csv souboru do HasMap csvData
                    this.csvData = this.csvAndWebDataProvider.getCSVData(this.source, this.filePath);
                    for(int i = 0; i < (this.csvData.size() - 2); i++) {
                        String[] str = this.csvData.get("row_" + i);
                        this.lines.add(str);
                    }

                    // Počet řádků csv souboru (= počet iterací)
                    this.numberOfRows = this.lines.size();

                    // Nahrání všech titulů z csv souboru do String[] titlesForComparison 
                    titlesForComparison = new String[this.numberOfRows];
                    this.titlesForComparison = this.csvData.get("titles");

                    publicationDatesForComparison = new String[this.numberOfRows];
                    this.publicationDatesForComparison = this.csvData.get("publicationDates");

                    Text text = new Text ("Nahrán soubor.");
                    this.textFlow.getChildren().clear();
                    this.textFlow.getChildren().add(text);
                }
            } else {
                Optional<ButtonType> result = confirmationWindow("novyProjekt");
                if(result.get() == ButtonType.OK) {
                    this.webView1.getEngine().loadContent("");
                    this.webView2.getEngine().loadContent("");
                    this.positionOfUniques.clear();
                    this.positionOfDuplicities.clear();
                    this.numberOfDuplicities = 0;
                    this.i = 0;
                    this.automaticSearchButton.setDisable(false);
                    this.manualSearchButton.setDisable(false);
                    this.addToDuplicitiesButton.setDisable(true);
                    this.addToUniquesButton.setDisable(true);                  
                    this.reloadLeftButton.setDisable(true);
                    this.reloadRightButton.setDisable(true);
                    this.saveButton.setDisable(true);
                    this.undoButton.setDisable(true);
                    this.filePath = null;
                    this.source = null;
                    newProject(event);
                }
        }
   
    }
        

    @FXML
    private void automaticSearching(ActionEvent event) throws IOException {
        Text text = new Text ("Prosím čekejte.");
        this.textFlow.getChildren().clear();
        this.textFlow.getChildren().add(text);
        
        this.progressIndicator.progressProperty().bind(this.serviceAutomaticSorter.progressProperty());
        this.serviceAutomaticSorter.reset();
        this.serviceAutomaticSorter.start();
        this.saveButton.setDisable(false);
    }

    @FXML
    private void manualSearching(ActionEvent event) throws UnsupportedEncodingException, IOException, URISyntaxException { 
        this.progressIndicator.progressProperty().bind(this.serviceManualSorter.progressProperty());
        this.serviceManualSorter.reset();
        this.serviceManualSorter.start();
        this.reloadLeftButton.setDisable(false);
        this.reloadRightButton.setDisable(false);
        this.saveButton.setDisable(false);
    }

    private void saveUniques(ActionEvent event) throws IOException {
        fc.getExtensionFilters().add(extFilter);
        fc.setTitle("Uložení unikátů");
        if(this.positionOfUniques.size() > 0) {            
            File file = fc.showSaveDialog(stage);
            if(file != null) {
                CSVParserAndWriter.saveToCSV(this.positionOfUniques, lines, file);
                Text text = new Text ("Unikáty uloženy");
                this.textFlow.getChildren().clear();
                this.textFlow.getChildren().add(text);
            }
        }
        else {
            Optional<ButtonType> result = this.confirmationWindow("unikáty");
            if(result.get() == ButtonType.CANCEL) {
                Text text = new Text ("Nic se neukládá.");
                this.textFlow.getChildren().clear();
                this.textFlow.getChildren().add(text);
            }
            else {                
                File file = fc.showSaveDialog(stage);
                if(file != null) {
                    CSVParserAndWriter.saveToCSV(this.positionOfUniques, lines, file);
                    Text text = new Text ("Vytvořen prázdný soubor pro unikáty.");
                    this.textFlow.getChildren().clear();
                    this.textFlow.getChildren().add(text);
                }
            }
        }          
    }

    private void saveDuplicities(ActionEvent event) throws IOException {
        fc.getExtensionFilters().add(extFilter);
        fc.setTitle("Uložení duplicit");
        if(this.positionOfDuplicities.size() > 0) {
            File file = fc.showSaveDialog(stage);
            if(file != null) {
                CSVParserAndWriter.saveToCSV(this.positionOfDuplicities, lines, file);
                Text text = new Text ("Duplicity uloženy");
                this.textFlow.getChildren().clear();
                this.textFlow.getChildren().add(text);
            }
        }
        else {
            Optional<ButtonType> result = this.confirmationWindow("duplicity");
            if(result.get() == ButtonType.CANCEL) {
                Text text = new Text ("Nic se neukládá.");
                this.textFlow.getChildren().clear();
                this.textFlow.getChildren().add(text);
            }
            else {
                File file = fc.showSaveDialog(stage);
                if(file != null) {
                    CSVParserAndWriter.saveToCSV(this.positionOfDuplicities, lines, file);
                    Text text = new Text ("Vytvořen prázdný soubor pro duplicity.");
                    this.textFlow.getChildren().clear();
                    this.textFlow.getChildren().add(text);
                }
            }
        } 
    }

    @FXML
    private void addToUniques(ActionEvent event) throws UnsupportedEncodingException, IOException, URISyntaxException {
        this.undoButton.setDisable(false);
        this.positionOfUniques.add(this.i);
        this.i++;
        if (this.i < this.numberOfRows) {              
            this.manualSearching(event);
        }
        else {
            Text text = new Text ("Zkontrolováno " + this.i + "/" + this.numberOfRows + " titulů\n"
                + "Unikátů: " + this.positionOfUniques.size() + " Duplicit: " + this.positionOfDuplicities.size());
            this.textFlow.getChildren().clear();
            this.textFlow.getChildren().add(text);
            this.addToDuplicitiesButton.setDisable(true);
            this.addToUniquesButton.setDisable(true);
        }
    }

    @FXML
    private void addToDuplicities(ActionEvent event) throws UnsupportedEncodingException, IOException, URISyntaxException {
        this.undoButton.setDisable(false);
        this.positionOfDuplicities.add(this.i);
        this.i++;
        if (this.i < this.numberOfRows) {                 
            this.manualSearching(event);
        }
        else {
            Text text = new Text ("Zkontrolováno " + this.i + "/" + this.numberOfRows + " titulů\n"
                + "Unikátů: " + this.positionOfUniques.size() + " Duplicit: " + this.positionOfDuplicities.size());
            this.textFlow.getChildren().clear();
            this.textFlow.getChildren().add(text);
            this.addToDuplicitiesButton.setDisable(true);
            this.addToUniquesButton.setDisable(true);
        }
    }

    @FXML
    private void checkFields(ActionEvent event) throws IOException, URISyntaxException {
        HashMap<String, ArrayList<String>> rightRecordsMARC21 = this.csvAndWebDataProvider.getMARC21Data(this.rightURLOfMARC21Record);
        HashMap<String, ArrayList<String>> leftRecordsMARC21 = this.csvAndWebDataProvider.getMARC21Data(this.leftURLOfMARC21Record);
        ArrayList<String> isbnRight = rightRecordsMARC21.get("isbn");
        ArrayList<String> isbnLeft = leftRecordsMARC21.get("isbn");
        ArrayList<String> ccnbRight = rightRecordsMARC21.get("ccnb");
        ArrayList<String> ccnbLeft = leftRecordsMARC21.get("ccnb");
        
        // titul (pole 245a) je vždy právě jeden
        String titleRight = rightRecordsMARC21.get("title").get(0);
        String titleLeft = leftRecordsMARC21.get("title").get(0);
        
        // podtitul (pole 245b) je vždy právě jeden
        String remainderOfTitleRight = rightRecordsMARC21.get("remainderOfTitle").get(0);
        String remainderOfTitleLeft = leftRecordsMARC21.get("remainderOfTitle").get(0);
        
        // hlavní záhlaví (100, 110, 111) je vždy právě jedno
        String mainEntryPersonalNameRight = rightRecordsMARC21.get("mainEntryPersonalName").get(0);
        String mainEntryPersonalNameLeft = leftRecordsMARC21.get("mainEntryPersonalName").get(0);
        String mainEntryCorporateNameRight = rightRecordsMARC21.get("mainEntryCorporateName").get(0);
        String mainEntryCorporateNameLeft = leftRecordsMARC21.get("mainEntryCorporateName").get(0);
        String mainEntryMeetingNameRight = rightRecordsMARC21.get("mainEntryMeetingName").get(0);
        String mainEntryMeetingNameLeft = leftRecordsMARC21.get("mainEntryMeetingName").get(0);
        ArrayList<String> addedPersonalNameRight = rightRecordsMARC21.get("addedPersonalName");
        ArrayList<String> addedPersonalNameLeft = leftRecordsMARC21.get("addedPersonalName");
        
        // Místo vydání (260a) je vždy právě jedno
        String placeOfPublicationRight = rightRecordsMARC21.get("placeOfPublication").get(0);
        String placeOfPublicationLeft = leftRecordsMARC21.get("placeOfPublication").get(0);
        
        // Vydavatel (260b) je vždy právě jeden
        String publisherRight = rightRecordsMARC21.get("publisher").get(0);
        String publisherLeft = leftRecordsMARC21.get("publisher").get(0);
        
        // Datum vydání (260c) je vždy právě jedno
        String publicationDateRight = rightRecordsMARC21.get("publicationDate").get(0);
        String publicationDateLeft = leftRecordsMARC21.get("publicationDate").get(0);
        
        // Počet stran (300a) je vždy právě jedno
        String extentRight = rightRecordsMARC21.get("extent").get(0);
        String extentLeft = leftRecordsMARC21.get("extent").get(0);
        
        Text boldISBN = new Text("ISBN:");
        boldISBN.setStyle("-fx-font-weight: bold");
        Text boldCCNB = new Text("čČNB:");
        boldCCNB.setStyle("-fx-font-weight: bold");
        Text boldTitle = new Text("Název:");
        boldTitle.setStyle("-fx-font-weight: bold");
        Text boldRemainderOfTitle = new Text("Podnázev:");
        boldRemainderOfTitle.setStyle("-fx-font-weight: bold");
        Text boldAddedPersonalName = new Text("Přispěvatel:");
        boldAddedPersonalName.setStyle("-fx-font-weight: bold");
        Text boldPlaceOfPublication = new Text("Místo vydání:");
        boldPlaceOfPublication.setStyle("-fx-font-weight: bold");
        Text boldPublisher = new Text("Nakladatel:");
        boldPublisher.setStyle("-fx-font-weight: bold");
        Text boldPublicationDate = new Text("Datum vydání");
        boldPublicationDate.setStyle("-fx-font-weight: bold");
        Text boldExtent = new Text("Počet stran:");
        boldExtent.setStyle("-fx-font-weight: bold"); 
        
        Text comparingISBN = new Text ();
        String stringToDisplay = "";
        int maxSize = Math.max(isbnLeft.size(), isbnRight.size());
        for (int i = 0; i < maxSize; i++) {
            if(isbnLeft.size() != maxSize) {
                isbnLeft.add((isbnLeft.size()), "---");
            }
            if(isbnRight.size() != maxSize) {
                isbnRight.add((isbnRight.size()), "---");
            }
            stringToDisplay = stringToDisplay + isbnLeft.get(i) + " / " + isbnRight.get(i) + " \n ";
            if(!"---".equals(isbnLeft.get(i)) && !"---".equals(isbnRight.get(i))) {
                if((isbnRight.get(i)).equals(isbnLeft.get(i))) {
                    comparingISBN.setStyle("-fx-fill: green");
                } else {
                    comparingISBN.setStyle("-fx-fill: red");
                }
            }
        }
        comparingISBN.setText(" \n " + stringToDisplay);
        
        Text comparingCCNB = new Text ();
        stringToDisplay = "";
        maxSize = Math.max(ccnbLeft.size(), ccnbRight.size());
        for (int i = 0; i < maxSize; i++) {
            if(ccnbLeft.size() != maxSize) {
                ccnbLeft.add((ccnbLeft.size()), "---");
            }
            if(ccnbRight.size() != maxSize) {
                ccnbRight.add((ccnbRight.size()), "---");
            }
            stringToDisplay = stringToDisplay + ccnbLeft.get(i) + " / " + ccnbRight.get(i) + " \n ";
            if(!"---".equals(ccnbLeft.get(i)) && !"---".equals(ccnbRight.get(i))) {
                if((ccnbRight.get(i)).equals(ccnbLeft.get(i))) {
                    comparingCCNB.setStyle("-fx-fill: green");
                } else {
                    comparingCCNB.setStyle("-fx-fill: red");
                }
            }
        }
        comparingCCNB.setText(" \n " + stringToDisplay);
 
        Text comparingTitle = new Text (" \n " + titleLeft + " / " + titleRight + " \n ");
        if(!"---".equals(titleLeft) && !"---".equals(titleRight)) {
            if(ItemSorter.compareTitles(titleLeft, titleRight) > 0.7){
                comparingTitle.setStyle("-fx-fill: green");
            } else {
                comparingTitle.setStyle("-fx-fill: red");
            }
        }
        
        Text comparingRemainderOfTitle = new Text (" \n " + remainderOfTitleLeft + " / " + remainderOfTitleRight + " \n ");
        if(!"---".equals(remainderOfTitleLeft) && !"---".equals(remainderOfTitleRight)) {
            if(ItemSorter.compareTitles(remainderOfTitleLeft, remainderOfTitleRight) > 0.7){
                comparingRemainderOfTitle.setStyle("-fx-fill: green");
            } else {
                comparingRemainderOfTitle.setStyle("-fx-fill: red");
            }
        }
        
        Text boldMainEntryPersonalName = new Text();
        Text comparingMainEntryPersonalName = new Text();
        
        Text boldMainEntryCorporateName = new Text();
        Text comparingMainEntryCorporateName = new Text();
        
        Text boldMainEntryMeetingName = new Text();
        Text comparingMainEntryMeetingName = new Text();
        
        if(!"---".equals(mainEntryPersonalNameRight) || !"---".equals(mainEntryPersonalNameLeft) ) {
            boldMainEntryPersonalName.setText("Hlavní autor:");
            boldMainEntryPersonalName.setStyle("-fx-font-weight: bold");
            String mainEntryPersonalNameToDisplay = " \n " + mainEntryPersonalNameLeft + " / " + mainEntryPersonalNameRight + " \n ";
            comparingMainEntryPersonalName.setText(mainEntryPersonalNameToDisplay);
            if(!"---".equals(mainEntryPersonalNameRight) && 
                !"---".equals(mainEntryPersonalNameLeft) &&
                ItemSorter.compareTitles(mainEntryPersonalNameLeft, mainEntryPersonalNameRight) > 0.7) {
                comparingMainEntryPersonalName.setStyle("-fx-fill: green");
            } else {
                comparingMainEntryPersonalName.setStyle("-fx-fill: red");
            }   
         }
          
        if(!"---".equals(mainEntryCorporateNameRight) || !"---".equals(mainEntryCorporateNameLeft)) {
            boldMainEntryCorporateName.setText("Jméno korporace - hlavní záhlaví:");
            boldMainEntryCorporateName.setStyle("-fx-font-weight: bold");
            String mainEntryCorporateNameToDisplay = " \n " + mainEntryCorporateNameLeft + " / " + mainEntryCorporateNameRight + " \n ";
            comparingMainEntryCorporateName.setText(mainEntryCorporateNameToDisplay);
            if(!"---".equals(mainEntryCorporateNameRight) && 
                !"---".equals(mainEntryCorporateNameLeft) &&
                ItemSorter.compareTitles(mainEntryCorporateNameLeft, mainEntryCorporateNameRight) > 0.7) {
                comparingMainEntryCorporateName.setStyle("-fx-fill: green");
            } else {
                comparingMainEntryCorporateName.setStyle("-fx-fill: red");
            }
        }
        
        if(!"---".equals(mainEntryMeetingNameRight) || !"---".equals(mainEntryMeetingNameLeft)) {
            boldMainEntryMeetingName.setText("Jméno korporace - hlavní záhlaví:");
            boldMainEntryMeetingName.setStyle("-fx-font-weight: bold");
            String mainEntryMeetingNameToDisplay = " \n " + mainEntryMeetingNameLeft + " / " + mainEntryMeetingNameRight + " \n ";
            comparingMainEntryMeetingName.setText(mainEntryMeetingNameToDisplay);
            if(!"---".equals(mainEntryMeetingNameRight) && 
                !"---".equals(mainEntryMeetingNameLeft) &&
                ItemSorter.compareTitles(mainEntryMeetingNameLeft, mainEntryMeetingNameRight) > 0.7) {
                comparingMainEntryMeetingName.setStyle("-fx-fill: green");
            } else {
                comparingMainEntryMeetingName.setStyle("-fx-fill: red");
            }
        }
        
        Text comparingAddedPersonalName = new Text();
        stringToDisplay = "";
        maxSize = Math.max(addedPersonalNameLeft.size(), addedPersonalNameRight.size());
        if(addedPersonalNameLeft.size() > 1 && addedPersonalNameRight.size() > 1) {
            Collections.sort(addedPersonalNameLeft, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(addedPersonalNameRight, String.CASE_INSENSITIVE_ORDER);
        }
        for (int i = 0; i < maxSize; i++) {
            if(addedPersonalNameLeft.size() != maxSize) {
                addedPersonalNameLeft.add((addedPersonalNameLeft.size()), "---");
            }
            if(addedPersonalNameRight.size() != maxSize) {
                addedPersonalNameRight.add((addedPersonalNameRight.size()), "---");
            }
            stringToDisplay = stringToDisplay + addedPersonalNameLeft.get(i) + " / " + addedPersonalNameRight.get(i) + " \n ";
            if(!"---".equals(addedPersonalNameLeft.get(i)) && !"---".equals(addedPersonalNameRight.get(i))) {
                if(ItemSorter.compareTitles(addedPersonalNameLeft.get(i), addedPersonalNameRight.get(i)) > 0.7){
                    comparingAddedPersonalName.setStyle("-fx-fill: green");
                } else {
                    comparingAddedPersonalName.setStyle("-fx-fill: red");
                }
            }
        }
        comparingAddedPersonalName.setText(" \n " + stringToDisplay);
        
        Text comparingPlaceOfPublication = new Text (" \n " + placeOfPublicationLeft + " / " + placeOfPublicationRight + " \n ");
        if(!"---".equals(placeOfPublicationLeft) && !"---".equals(placeOfPublicationRight)) {
            if(ItemSorter.compareTitles(placeOfPublicationLeft, placeOfPublicationRight) > 0.7){
                comparingPlaceOfPublication.setStyle("-fx-fill: green");
            } else {
                comparingPlaceOfPublication.setStyle("-fx-fill: red");
            }
        }
        
        Text comparingPublisher = new Text (" \n " + publisherLeft + " / " + publisherRight + " \n ");
        if(!"---".equals(publisherLeft) && !"---".equals(publisherRight)) {
            if(ItemSorter.compareTitles(publisherLeft, publisherRight) > 0.7){
                comparingPublisher.setStyle("-fx-fill: green");
            } else {
                comparingPublisher.setStyle("-fx-fill: red");
            }
        }
        Text comparingPublicationDate = new Text (" \n " + publicationDateLeft + " / " + publicationDateRight + " \n ");
        if(!"---".equals(publicationDateLeft) && !"---".equals(publicationDateRight)) {
            if(publicationDateRight.equals(publicationDateLeft)) {
                comparingPublicationDate.setStyle("-fx-fill: green");
            } else {
                comparingPublicationDate.setStyle("-fx-fill: red");
            }
        }
        Text comparingExtent = new Text (" \n " + extentLeft + " / " + extentRight + " \n ");
        if(!"---".equals(extentLeft) && !"---".equals(extentRight)) {
            extentLeft = extentLeft.replaceAll("\\D+","");
            extentRight = extentRight.replaceAll("\\D+","");
            if(extentRight.equals(extentLeft)) {
                comparingExtent.setStyle("-fx-fill: green");
            } else {
                comparingExtent.setStyle("-fx-fill: red");
            }
        }
        this.textFlow.getChildren().clear();
        this.textFlow.getChildren().addAll
            (boldISBN, comparingISBN, boldCCNB, comparingCCNB, boldTitle, comparingTitle,
            boldRemainderOfTitle, comparingRemainderOfTitle, boldMainEntryPersonalName, 
            comparingMainEntryPersonalName, boldMainEntryCorporateName, comparingMainEntryCorporateName,
            boldMainEntryMeetingName, comparingMainEntryMeetingName, boldAddedPersonalName, comparingAddedPersonalName,
            boldPlaceOfPublication, comparingPlaceOfPublication, boldPublisher, comparingPublisher, 
            boldPublicationDate, comparingPublicationDate, boldExtent, comparingExtent
            );
    }
    
    private Optional<ButtonType> confirmationWindow(String confirmation) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        if ("novyProjekt".equals(confirmation)) {
            alert.setHeaderText("Nahráním nového projektu se odstraní veškeré neuložené soubory.");
            alert.setContentText("Chcete přesto nahrát nový projekt?");
        } else {
            alert.setHeaderText("Nenalezeny žádné " + confirmation + ".");
            alert.setContentText("Chcete uložit prázdný soubor?");
        }
        Optional<ButtonType> result = alert.showAndWait();        
    return result; 
    }
    
    
    private void checkChangedURL (String rightURL, String leftURL) {
        if (rightURL.startsWith("http://vdk.nkp.cz/vdk/original") &&
            leftURL.startsWith("http://vdk.nkp.cz/vdk/original")) {
            this.checkFieldsButton.setDisable(false);
        }
    }

    @FXML
    private void saveItems(ActionEvent event) {
    }

    @FXML
    private void undoAction(ActionEvent event) throws IOException, UnsupportedEncodingException, URISyntaxException {
        if (this.i != 0) {
            this.i = this.i - 1;
            if (this.positionOfUniques.contains(this.i)) {
                this.positionOfUniques.remove(this.i);
            }
            if (this.positionOfDuplicities.contains(this.i)) {
                this.positionOfDuplicities.remove(this.i);
            }
            this.manualSearching(event);
        }
        this.addToUniquesButton.setDisable(false);
        this.addToDuplicitiesButton.setDisable(false);
    }

}
