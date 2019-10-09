/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.myvdktester;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author HABETINOVAP
 */
public class CSVAndWebDataProvider implements DataProviderInterface {
    String filePath;
    String source;
    HTMLParser htmlParser = new HTMLParser();
    
    @Override
    public HashMap<String, String[]> getCSVData(String source, String filePath) {
        try {
            HashMap<String, String[]> csvData = new HashMap<>(); 
            List<String[]> rows = CSVParserAndWriter.getAllElements(source, filePath);
            for(int i = 0; i < rows.size(); i++) {
                String currentRow = "row_" + i; 
                csvData.put(currentRow, rows.get(i));               
            }           
            String[] titles = CSVParserAndWriter.getTitles(rows);
            String[] publicationDates = CSVParserAndWriter.getPublicationDates(rows);
            csvData.put("titles", titles);
            csvData.put("publicationDates", publicationDates);
    return csvData;
        } catch (IOException ex) {
            Logger.getLogger(CSVAndWebDataProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    return null;        
    }

    @Override
    public HashMap<String, ArrayList<String>> getMARC21Data(String url) {
        try {
            HashMap<String, ArrayList<String>> recordsMARC21 = new HashMap<>();
            ArrayList<String> isbn;
            ArrayList<String> ccnb;
            ArrayList<String> mainEntryPersonalName = new ArrayList<>();
            ArrayList<String> mainEntryCorporateName = new ArrayList<>();
            ArrayList<String> mainEntryMeetingName = new ArrayList<>();
            ArrayList<String> addedPersonalName;
            ArrayList<String> placeOfPublication = new ArrayList<>();
            ArrayList<String> publisher = new ArrayList<>();
            ArrayList<String> publicationDate = new ArrayList<>();
            ArrayList<String> title = new ArrayList<>();
            ArrayList<String> remainderOfTitle = new ArrayList<>();
            ArrayList<String> extent = new ArrayList<>();
            org.jsoup.nodes.Document doc = DocumentManager.getDocument(url);
            isbn = DocumentManager.getISBNfromMARC21(this.htmlParser.getISBNInfofromMARC21(doc));
            ccnb = DocumentManager.getCCNBfromMARC21(this.htmlParser.getCCNBInfofromMARC21(doc));
            mainEntryPersonalName.add(DocumentManager.getMainAuthorityfromMARC21(this.htmlParser.getMainEntryPersonalNameInfofromMARC21(doc)));
            mainEntryCorporateName.add(DocumentManager.getMainAuthorityfromMARC21(this.htmlParser.getMainEntryCorporateNameInfofromMARC21(doc)));
            mainEntryMeetingName.add(DocumentManager.getMainAuthorityfromMARC21(htmlParser.getMainEntryMeetingNameInfofromMARC21(doc)));
            addedPersonalName = DocumentManager.getAddedEntryPersonalNamefromMARC21(htmlParser.getAddedEntryPersonalNameInfofromMARC21(doc));
            placeOfPublication.add(DocumentManager.getPlaceOfPublicationfromMARC21(htmlParser.getPublicationInfofromMARC21(doc)));
            publisher.add(DocumentManager.getPublisherfromMARC21(htmlParser.getPublicationInfofromMARC21(doc)));
            publicationDate.add(DocumentManager.getPublicationDatefromMARC21(htmlParser.getPublicationInfofromMARC21(doc)));
            title.add(DocumentManager.getTitlefromMARC21(htmlParser.getTitleStatementfromMARC21(doc)));
            remainderOfTitle.add(DocumentManager.getRemainderOfTitlefromMARC21(htmlParser.getTitleStatementfromMARC21(doc)));
            extent.add(DocumentManager.getExtentfromMARC21(htmlParser.getPhysicalDescriptionInfofromMARC21(doc)));
            recordsMARC21.put("isbn", isbn);
            recordsMARC21.put("ccnb", ccnb);
            recordsMARC21.put("mainEntryPersonalName", mainEntryPersonalName);
            recordsMARC21.put("mainEntryCorporateName", mainEntryCorporateName);
            recordsMARC21.put("mainEntryMeetingName", mainEntryMeetingName);
            recordsMARC21.put("addedPersonalName", addedPersonalName);
            recordsMARC21.put("placeOfPublication", placeOfPublication);
            recordsMARC21.put("publisher", publisher);
            recordsMARC21.put("publicationDate", publicationDate);
            recordsMARC21.put("title", title);
            recordsMARC21.put("remainderOfTitle", remainderOfTitle);
            recordsMARC21.put("extent", extent);
            return recordsMARC21;
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(DataProviderInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}


