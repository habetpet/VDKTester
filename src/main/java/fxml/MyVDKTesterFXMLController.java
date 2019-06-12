/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxml;

import com.mycompany.myvdktester.CSVParserAndWriter;
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
import java.util.List;
import java.util.Map;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
//import org.apache.commons.text.similarity.LevenshteinDistance;
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
    
    WebEngine engine;
    String filePath;
    String source;
    String leftURL;
    String rightURL;
    String leftURLOfMARC21Record;
    String rightURLOfMARC21Record;
    List<String[]> lines;
    ArrayList<Integer> positionOfUniques = new ArrayList<>();
    ArrayList<Integer> positionOfDuplicities = new ArrayList<>();
    ArrayList<String> selectedOAIsOfSimmilarTitles = new ArrayList<>();
    ArrayList<String> selectedIDsForHighlighting = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> potencialTitles = new ArrayList<>();
    ArrayList<String> publicationDates = new ArrayList<>();
    ArrayList<String> OAIsOfEqualItems = new ArrayList<>();
    HashMap<String, Boolean> selectionResult = new HashMap<String, Boolean>();
    Map<String,ArrayList<String>> connectedIDWithOAIs = new HashMap<String,ArrayList<String>>();
    Integer i = 0;
    Integer numberOfDuplicities = 0;
    Window stage = null;
    String mainID;
    ArrayList<String> selectedIDsOfSimilarTitles = new ArrayList<>();;
    FileChooser fc = new FileChooser();
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV File", "*.csv");
    URLBuilder urlBuilder;
    HTMLParser htmlParser;
    Service serviceAutomaticSorter;
    Service serviceManualSorter;
    Elements allItemsInListOnRight;
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
        

        
        this.serviceAutomaticSorter = new Service(){
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Void call() throws Exception {
                        checkFieldsButton.setDisable(true);
                        positionOfUniques = ItemSorter.sortAutomaticItems(titles, selectionResult);
                            for(int i = 0; i < lines.size(); i++) {
                                if(!positionOfUniques.contains(i))
                                positionOfDuplicities.add(i);
                            }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Text text = new Text ("Nalezeno celkem " + positionOfUniques.size() + " unikátů a "
                                    + positionOfDuplicities.size() + " duplicit."+ "\n" 
                                    + "Zkontrolován soubor " + filePath + " pro knihovnu " + source + ".");
                                textFlow.getChildren().clear();
                                textFlow.getChildren().add(text);
                            }
                        });
                        this.updateProgress(0, 0);
                        progressIndicator.setVisible(false);
                        return null;
                    }
                };
            }
        };
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
                    String titleFromCSV = titles.get(i);
                    String[] line = lines.get(i); 
        
                    leftURL = urlBuilder.buildLeftURL(titleFromCSV, selectionResult);
                    rightURL = urlBuilder.buildRightURL(titleFromCSV);
        
                    org.jsoup.nodes.Document docOnLeft = DocumentManager.getDocument(leftURL);
                    org.jsoup.nodes.Document docOnRight = DocumentManager.getDocument(rightURL);

                    allItemsInListOnRight = htmlParser.getAllItemsInList(docOnRight);
                    allItemsInListOnLeft = htmlParser.getAllItemsInList(docOnLeft);

                    mainID = DocumentManager.getMainID(line, allItemsInListOnLeft);
                    potencialTitles = htmlParser.getAllTitlesOnPage(DocumentManager.getDocument(rightURL));
                    connectedIDWithOAIs = DocumentManager.connectIDWithRespectiveOAIs(allItemsInListOnRight);
                    selectedIDsOfSimilarTitles = DocumentManager.getIDsOfSimilarTitles(allItemsInListOnRight, titleFromCSV, potencialTitles);
                    selectedOAIsOfSimmilarTitles = DocumentManager.getOAIsOfID(connectedIDWithOAIs, selectedIDsOfSimilarTitles);
                    for(int k = 0; k < selectedOAIsOfSimmilarTitles.size(); k++) {
                        rightURLOfMARC21Record = urlBuilder.buildMarc21RecordURL(selectedOAIsOfSimmilarTitles.get(k));
                        org.jsoup.nodes.Document docOfMARC21Record = DocumentManager.getDocument(rightURLOfMARC21Record);
                        String publicationDate = DocumentManager.getPublicationDatefromMARC21(htmlParser.getPublicationInfofromMARC21(docOfMARC21Record));
                        publicationDate = publicationDate.replaceAll("\\D+","");
                        String publicationDateCSV = publicationDates.get(i);
//                        publicationDateCSV = publicationDateCSV.replaceAll("\\D+","");
//                        System.out.println(publicationDateCSV);
                        if (!"---".equals(publicationDate) && publicationDateCSV != null && publicationDate.equals(publicationDateCSV)) {
                            String OAIOfEqualItems = selectedOAIsOfSimmilarTitles.get(k);
                            String id = DocumentManager.getIDOfOAI(connectedIDWithOAIs, OAIOfEqualItems);
                            if(id == null ? mainID != null : !id.equals(mainID)) {
                                selectedIDsForHighlighting.add(id);
                            }
                        }            
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            webView1.getEngine().load(leftURL);
                            webView2.getEngine().load(rightURL);
                            Text text = new Text ("Zkontrolováno " + i + "/" + lines.size() + " titulů. \n"
                                + "Unikátů: " + positionOfUniques.size() + " Duplicit: " + positionOfDuplicities.size());
                            textFlow.getChildren().clear();
                            textFlow.getChildren().add(text);
                        }
                    });
                    progressIndicator.progressProperty().bind(webView1.getEngine().getLoadWorker().progressProperty());
                    progressIndicator.progressProperty().bind(webView2.getEngine().getLoadWorker().progressProperty());
                    this.updateProgress(0, 0);
                    //progressIndicator.setVisible(false);
                    return null;
                    }
                };
            }
        };
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        this.progressIndicator.setVisible(false);
        this.checkFieldsButton.setDisable(true);
        this.undoButton.setDisable(true);
        MenuItem saveU = new MenuItem("Save uniques"); 
        MenuItem saveD = new MenuItem("Save duplicities"); 
        this.saveButton.getItems().clear();
        this.saveButton.getItems().add(saveU);
        this.saveButton.getItems().add(saveD);

        saveU.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    saveUniques(event);
                } catch (IOException ex) {
                    Logger.getLogger(MyVDKTesterFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
             }
        });
        
        saveD.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    saveDuplicities(event);
                } catch (IOException ex) {
                    Logger.getLogger(MyVDKTesterFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                            elMain.setAttribute("style", "background-color:#ffad99;");
                            for(int i = 0; i < allItemsInListOnLeft.size(); i++) {
                                org.jsoup.nodes.Element el = allItemsInListOnLeft.get(i);
                                String elID = el.id();
                                if(el != null && !elID.equals(mainID)) {
                                    Element elHide = doc.getElementById(elID);
                                    elHide.setAttribute("style", "display: none;");
                                    Node line = elHide.getNextSibling().getNextSibling();
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
                        if(elMainID != null) {
                            elMainID.setAttribute("style", "display: none;");
                        }
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
                
        this.automaticSearchButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    positionOfUniques.clear();
                    positionOfDuplicities.clear();
                    numberOfDuplicities = 0;
                    i = 0;
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
                    Text text = new Text ("Zkontrolováno 0" + "/" + lines.size() + " titulů. \n"
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
                Text text = new Text ("Zkontrolováno " + i + "/" + lines.size() + " titulů. \n"
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
                Text text = new Text ("Zkontrolováno " + i + "/" + lines.size() + " titulů. \n"
                    + "Unikátů: " + positionOfUniques.size() + " Duplicit: " + positionOfDuplicities.size());
                textFlow.getChildren().clear();
                textFlow.getChildren().add(text);
                webView2.getEngine().load(rightURL);
            }   
        });
    }    

    @FXML
    private void openNewWindow(ActionEvent event) throws IOException {
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("MyVDKTesterNewWindowFXML.fxml"));
        Parent root = (Parent)fxmlloader.load();     
        Stage stage1 = new Stage();
        stage1.setTitle("My New Stage Title");
        stage1.setScene(new Scene(root));
        stage1.showAndWait();
      
        MyVDKTesterNewWindowFXMLController controller = fxmlloader.<MyVDKTesterNewWindowFXMLController>getController();
        this.filePath = controller.getPath();
        this.source = controller.getSource();
        this.selectionResult  = controller.getSelectedSourcesResult();
        Text text = new Text ("Nahrán soubor.");
        this.textFlow.getChildren().clear();
        this.textFlow.getChildren().add(text);
        this.lines = CSVParserAndWriter.getAllElements(this.source, this.filePath);
        this.titles = CSVParserAndWriter.getTitles(this.lines);
        this.publicationDates = CSVParserAndWriter.getPublicationDates(lines);
    }

    @FXML
    private void automaticSearching(ActionEvent event) throws IOException {
        Text text = new Text ("Prosím čekejte.");
        this.textFlow.getChildren().clear();
        this.textFlow.getChildren().add(text);
        this.progressIndicator.progressProperty().bind(this.serviceAutomaticSorter.progressProperty());
        this.serviceAutomaticSorter.reset();
        this.serviceAutomaticSorter.start();
        
    }

    @FXML
    private void manualSearching(ActionEvent event) throws UnsupportedEncodingException, IOException, URISyntaxException { 
        this.progressIndicator.progressProperty().bind(this.serviceManualSorter.progressProperty());
//        this.progressIndicator.progressProperty().bind(webView1.getEngine().getLoadWorker().progressProperty());
//        this.progressIndicator.progressProperty().bind(webView2.getEngine().getLoadWorker().progressProperty());
        this.serviceManualSorter.reset();
        this.serviceManualSorter.start();
    }

    private void saveUniques(ActionEvent event) throws IOException {
        fc.getExtensionFilters().add(extFilter);
        fc.setTitle("Save Uniques");
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
        fc.setTitle("Save Duplicities");
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
        this.positionOfUniques.add(i);
        i++;
        if (i < this.lines.size()) {              
            this.manualSearching(event);
        }
        else {
            Text text = new Text ("Zkontrolováno " + i + "/" + this.lines.size() + " titulů\n"
                + "Unikátů: " + this.positionOfUniques.size() + " Duplicit: " + this.positionOfDuplicities.size());
            this.textFlow.getChildren().clear();
            this.textFlow.getChildren().add(text);
            this.addToDuplicitiesButton.setDisable(true);
            this.addToUniquesButton.setDisable(true);
        }
    }

    @FXML
    private void addToDuplicities(ActionEvent event) throws UnsupportedEncodingException, IOException, URISyntaxException {
        this.positionOfDuplicities.add(i);
        i++;
        if (i < this.lines.size()) {                 
            this.manualSearching(event);
        }
        else {
            Text text = new Text ("Zkontrolováno " + i + "/" + this.lines.size() + " titulů\n"
                + "Unikátů: " + this.positionOfUniques.size() + " Duplicit: " + this.positionOfDuplicities.size());
            this.textFlow.getChildren().clear();
            this.textFlow.getChildren().add(text);
            this.addToDuplicitiesButton.setDisable(true);
            this.addToUniquesButton.setDisable(true);
        }
    }

    @FXML
    private void checkFields(ActionEvent event) throws IOException, URISyntaxException {
        org.jsoup.nodes.Document docRightMARC21 = DocumentManager.getDocument(this.rightURLOfMARC21Record);
        org.jsoup.nodes.Document docLeftMARC21 = DocumentManager.getDocument(this.leftURLOfMARC21Record);
        String isbnRight = DocumentManager.getISBNfromMARC21(htmlParser.getISBNInfofromMARC21(docRightMARC21));       
        String isbnLeft = DocumentManager.getISBNfromMARC21(htmlParser.getISBNInfofromMARC21(docLeftMARC21));
        String ccnbRight = DocumentManager.getCCNBfromMARC21(htmlParser.getCCNBInfofromMARC21(docRightMARC21));       
        String ccnbLeft = DocumentManager.getCCNBfromMARC21(htmlParser.getCCNBInfofromMARC21(docLeftMARC21));
        String mainEntryRight;
        String mainEntryLeft;
        String mainEntryInfoRight = htmlParser.getMainEntryPersonalNameInfofromMARC21(docRightMARC21);
        String mainEntryInfoLeft = htmlParser.getMainEntryPersonalNameInfofromMARC21(docLeftMARC21);
        int mainEntryField = 100;
        if ("".equals(mainEntryInfoRight) && "".equals(mainEntryInfoLeft)) {
            mainEntryInfoRight = htmlParser.getMainEntryCorporateNameInfofromMARC21(docRightMARC21);       
            mainEntryInfoLeft = htmlParser.getMainEntryCorporateNameInfofromMARC21(docLeftMARC21);
            mainEntryRight = DocumentManager.getMainEntryPersonalNamefromMARC21(mainEntryInfoRight);       
            mainEntryLeft = DocumentManager.getMainEntryPersonalNamefromMARC21(mainEntryInfoLeft);
            mainEntryField = 110;
            if ("".equals(mainEntryInfoRight) && "".equals(mainEntryInfoLeft)) {
                mainEntryInfoRight = htmlParser.getMainEntryMeetingNameInfofromMARC21(docRightMARC21);
                mainEntryInfoLeft = htmlParser.getMainEntryMeetingNameInfofromMARC21(docLeftMARC21);
                mainEntryRight = DocumentManager.getMainEntryPersonalNamefromMARC21(mainEntryInfoRight);       
                mainEntryLeft = DocumentManager.getMainEntryPersonalNamefromMARC21(mainEntryInfoLeft);
                mainEntryField = 111;
            }
        } else {
        mainEntryRight = DocumentManager.getMainEntryPersonalNamefromMARC21(mainEntryInfoRight);       
        mainEntryLeft = DocumentManager.getMainEntryPersonalNamefromMARC21(mainEntryInfoLeft);
        }
        
        ArrayList<String> addedPersonalNameRight = DocumentManager.getAddedEntryPersonalNamefromMARC21(htmlParser.getAddedEntryPersonalNameInfofromMARC21(docRightMARC21));       
        ArrayList<String> addedPersonalNameLeft = DocumentManager.getAddedEntryPersonalNamefromMARC21(htmlParser.getAddedEntryPersonalNameInfofromMARC21(docLeftMARC21));
        String placeOfPublicationLeft = DocumentManager.getPlaceOfPublicationfromMARC21(htmlParser.getPublicationInfofromMARC21(docLeftMARC21));
        String placeOfPublicationRight = DocumentManager.getPlaceOfPublicationfromMARC21(htmlParser.getPublicationInfofromMARC21(docRightMARC21));
        String publisherLeft = DocumentManager.getPublisherfromMARC21(htmlParser.getPublicationInfofromMARC21(docLeftMARC21));
        String publisherRight = DocumentManager.getPublisherfromMARC21(htmlParser.getPublicationInfofromMARC21(docRightMARC21));
        String publicationDateLeft = DocumentManager.getPublicationDatefromMARC21(htmlParser.getPublicationInfofromMARC21(docLeftMARC21));
        String publicationDateRight = DocumentManager.getPublicationDatefromMARC21(htmlParser.getPublicationInfofromMARC21(docRightMARC21));
        String titleRight = DocumentManager.getTitlefromMARC21(htmlParser.getTitleStatementfromMARC21(docRightMARC21));
        String titleLeft = DocumentManager.getTitlefromMARC21(htmlParser.getTitleStatementfromMARC21(docLeftMARC21));
        String remainderOfTitleRight = DocumentManager.getRemainderOfTitlefromMARC21(htmlParser.getTitleStatementfromMARC21(docRightMARC21));
        String remainderOfTitleLeft = DocumentManager.getRemainderOfTitlefromMARC21(htmlParser.getTitleStatementfromMARC21(docLeftMARC21));
        String extentRight = DocumentManager.getExtentfromMARC21(htmlParser.getPhysicalDescriptionInfofromMARC21(docRightMARC21));
        String extentLeft = DocumentManager.getExtentfromMARC21(htmlParser.getPhysicalDescriptionInfofromMARC21(docLeftMARC21));
        Text boldISBN = new Text("ISBN:");
        boldISBN.setStyle("-fx-font-weight: bold");
        Text boldCCNB = new Text("čČNB:");
        boldCCNB.setStyle("-fx-font-weight: bold");
        Text boldTitle = new Text("Název:");
        boldTitle.setStyle("-fx-font-weight: bold");
        Text boldRemainderOfTitle = new Text("Podnázev:");
        boldRemainderOfTitle.setStyle("-fx-font-weight: bold");
        Text boldMainEntry = new Text();
        switch (mainEntryField) {
            case 100:
                {
                    boldMainEntry = new Text("Hlavní autor:");
                    break;
                }
            case 110:
                {
                    boldMainEntry = new Text("Jméno korporace - hlavní záhlaví:");
                    break;
                }
            case 111:
                {
                    boldMainEntry = new Text("Jméno akce - hlavní záhlaví:");
                    break;
                }
        }
        
        boldMainEntry.setStyle("-fx-font-weight: bold");
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
        Text comparingISBN = new Text (" \n " + isbnLeft + " / " + isbnRight + " \n ");
        if(!"---".equals(isbnLeft) && !"---".equals(isbnRight)) {
            if(isbnRight.equals(isbnLeft)) {
                comparingISBN.setStyle("-fx-fill: green");
            } else {
                comparingISBN.setStyle("-fx-fill: red");
            }  
        }      
        Text comparingCCNB = new Text (" \n " + ccnbLeft + " / " + ccnbRight + " \n ");
        if(!"---".equals(ccnbLeft) && !"---".equals(ccnbRight)) {
            if(ccnbRight.equals(ccnbLeft)) {
                comparingCCNB.setStyle("-fx-fill: green");
            } else {
                comparingCCNB.setStyle("-fx-fill: red");
            }
        }
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
        Text comparingMainEntry = new Text (" \n " + mainEntryLeft + " / " + mainEntryRight + " \n ");
        if(!"---".equals(mainEntryLeft) && !"---".equals(mainEntryRight)) {
            if(ItemSorter.compareTitles(mainEntryLeft, mainEntryRight) > 0.7){
                comparingMainEntry.setStyle("-fx-fill: green");
            } else {
                comparingMainEntry.setStyle("-fx-fill: red");
            }
        }
        Text comparingAddedPersonalName = new Text();
        String stringToDisplay = "";
        int maxSize = Math.max(addedPersonalNameLeft.size(), addedPersonalNameRight.size());
        if(!addedPersonalNameLeft.isEmpty() && !addedPersonalNameRight.isEmpty()) {
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
            boldRemainderOfTitle, comparingRemainderOfTitle,
            boldMainEntry, comparingMainEntry, boldAddedPersonalName, comparingAddedPersonalName,
            boldPlaceOfPublication, comparingPlaceOfPublication, boldPublisher, comparingPublisher, 
            boldPublicationDate, comparingPublicationDate, boldExtent, comparingExtent
            );
    }
    
    private Optional<ButtonType> confirmationWindow(String typeOfSortedTitles) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Nenalezeny žádné " + typeOfSortedTitles + ".");
        alert.setContentText("Chcete uložit prázdný soubor?");
        
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
        if (i != 0) {
            i = i - 1;
            if (this.positionOfUniques.contains(i)) {
                this.positionOfUniques.remove(i);
            }
            if (this.positionOfDuplicities.contains(i)) {
                this.positionOfDuplicities.remove(i);
            }
            this.manualSearching(event);
        }
        this.addToUniquesButton.setDisable(false);
        this.addToDuplicitiesButton.setDisable(false);
    }
}
