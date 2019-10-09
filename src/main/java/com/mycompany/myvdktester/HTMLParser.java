/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.myvdktester;

import java.util.ArrayList;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author HABETINOVAP
 */
public class HTMLParser {    

    public Integer getNumberOfResults(Document doc){
        Element el = doc.getElementById("totalHits");
        int results = Integer.parseInt((el.attr("value")));        
    return results;
    }
    
    // metoda získá všechny tabulky na stránce
    public Elements getAllItemsInList(Document doc) {
        Elements items = doc.select("li[id^=res_]");
    return items;
    }
    
    public String getAttributeDataCSV(Element item) {
        String dataCSV = item.attr("data-csv");
    return dataCSV;
    }
    
    public ArrayList<String> getAllTitlesOnPage(Document doc) {
        ArrayList<String> allTitlesOnPage = new ArrayList<>();
        Elements titles = getAllItemsInList(doc).select("div.title");
        titles.forEach((title) -> {
            allTitlesOnPage.add(title.text());
        });
    return allTitlesOnPage;
    }
    
    public ArrayList<String> getISBNInfofromMARC21(Document doc) {
        ArrayList<String> isbnInfo = new ArrayList<>();
        Elements records = doc.select("tr").select("th:contains(020)").next();
        records.forEach((el) -> {
            isbnInfo.add(el.text().replaceAll("\\s",""));
        });               
        return isbnInfo;
    }
    
    public ArrayList<String> getCCNBInfofromMARC21(Document doc) {
        ArrayList<String> ccnbInfo = new ArrayList<>();
        Elements records = doc.select("tr").select("th:contains(015)").next(); 
        records.forEach((el) -> {
            ccnbInfo.add(el.text().replaceAll("\\s",""));
        });
        return ccnbInfo;
    }
    
    public String getPublicationInfofromMARC21(Document doc) {
        Elements records = doc.select("tr");        
        String publicationInfo = records.select("th:contains(260)").next().text();     
    return publicationInfo;
    }
    
    public String getTitleStatementfromMARC21(Document doc) {
        String titleStatement;
        Elements records = doc.select("tr");        
        titleStatement = records.select("th:contains(245)").next().text();   
    return titleStatement;
    }
    
    public String getPhysicalDescriptionInfofromMARC21(Document doc) {
        String physicalDescriptionInfo;
        Elements records = doc.select("tr");        
        physicalDescriptionInfo = records.select("th:contains(300)").next().text();   
    return physicalDescriptionInfo;
    }
    
    public String getMainEntryPersonalNameInfofromMARC21(Document doc) {
        String mainEntryPersonalNameInfo;
        Elements records = doc.select("tr");        
        mainEntryPersonalNameInfo = records.select("th:contains(100)").next().text();   
    return mainEntryPersonalNameInfo;
    }
    
    public String getMainEntryCorporateNameInfofromMARC21(Document doc) {
        String mainEntryCorporateNameInfo;
        Elements records = doc.select("tr");        
        mainEntryCorporateNameInfo = records.select("th:contains(110)").next().text();   
    return mainEntryCorporateNameInfo;
    }
    
    public String getMainEntryMeetingNameInfofromMARC21(Document doc) {
        String mainEntryMeetingNameInfo;
        Elements records = doc.select("tr");        
        mainEntryMeetingNameInfo = records.select("th:contains(111)").next().text();   
    return mainEntryMeetingNameInfo;
    }
    
    public ArrayList<String> getAddedEntryPersonalNameInfofromMARC21(Document doc) {
        ArrayList<String> mainAddedPersonalNameInfo = new ArrayList<>();
        Elements records = doc.select("tr").select("th:contains(700)").next();
        records.forEach((el) -> {
            mainAddedPersonalNameInfo.add(el.text());
        });
    return mainAddedPersonalNameInfo;
    }
    
    
    // zjistí jestli jsou výsledky na více stránkách
    // propojit s URLBuilder().buildPaginatedURL
    // zatím neotestováno - není priorita - většina relevantních výsledků by měla být na první stránce
    public Integer getNextPage(Document doc) {
        int page = 0;
        Elements pages = doc.select ("a.next");
        if (!pages.isEmpty()) {
            page += 40;
        }
        else{
            page = -1;
        }
    return page;
    }
}
