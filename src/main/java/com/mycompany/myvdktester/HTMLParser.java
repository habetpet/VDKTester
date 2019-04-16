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
    //vstup: html dokument doc
    // výstup: celkový počet vyhledaných titulů ve formátu Integer 
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
    
//    public ArrayList<String> getAllIDsOnPage(Document doc) {
//        ArrayList<String> ids = null;
//        Elements items = getAllItemsInList(doc);
//        items.forEach((item) -> {
//            ids.add(item.id());
//        });
//    return ids;
//    }
    
    // metoda zapíše všechny názvy titulů z načtené stránky
    public ArrayList<String> getAllTitlesOnPage(Document doc) {
        ArrayList<String> allTitlesOnPage = new ArrayList<>();
        Elements titles = getAllItemsInList(doc).select("div.title");
        titles.forEach((title) -> {
            allTitlesOnPage.add(title.text());
        });
    return allTitlesOnPage;
    }
    
    public String getISBNInfofromMARC21(Document doc) {
        String isbnInfo;
        Elements records = doc.select("tr");        
        isbnInfo = records.select("th:contains(020)").next().text().replaceAll("\\s","");
        return isbnInfo;
    }
    
    public String getCCNBInfofromMARC21(Document doc) {
        String ccnbInfo;
        Elements records = doc.select("tr");        
        ccnbInfo = records.select("th:contains(015)").next().text().replaceAll("\\s","");
        return ccnbInfo;
    }
    
    // metoda získá datum vydání parsováním stránky se záznamem v MARC 21
    public String getPublicationDateInfofromMARC21(Document doc) {
        Elements records = doc.select("tr");        
        String publicationInfo = records.select("th:contains(260)").next().text().replaceAll("\\s","");     
    return publicationInfo;
    }
    
    public String getTitleInfofromMARC21(Document doc) {
        String titleInfo;
        Elements records = doc.select("tr");        
        titleInfo = records.select("th:contains(245)").next().text();   
    return titleInfo;
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
