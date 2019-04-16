package com.mycompany.myvdktester;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author HABETINOVAP
 */
public class DocumentManager {
    
    public static Document getDocument (String searchedURL) throws IOException, URISyntaxException{
        Document doc = Jsoup.connect(searchedURL).get();
    return doc;
    }

    public static String getMainID(String[] line, Elements items) throws IOException{
        String id = null;
        String lineRaw = Arrays.toString(line);
        lineRaw = lineRaw.replaceAll("[^A-Za-z0-9]", "");
        for(Element e : items) {           
            String datacsv = e.attr("data-csv");
            datacsv = datacsv.replaceAll("[^A-Za-z0-9]", "");
            if(lineRaw.equals(datacsv)) {
                id = e.id();
                break;
            }   
        }
    return id;    
    }
    
    public static Map<String,ArrayList<String>> connectIDWithRespectiveOAIs(Elements items) {
        Map<String,ArrayList<String>> idWithOAIs = new HashMap<>();           
        items.forEach((el) -> {
            String id = el.id();
            ArrayList<String> allOAIsInTable = new ArrayList<String>();
            String info = el.select("div.ex").attr("data-ex");             
            int start_index = info.indexOf("oai");
            int end_index = info.indexOf("\",");
            while(start_index >= 0) {
                String oai = info.substring(start_index, end_index);  
                allOAIsInTable.add(oai);
                start_index = info.indexOf("oai", start_index + 1);
                end_index = info.indexOf("\",", start_index);
            }         
            idWithOAIs.put(id, allOAIsInTable);
        });
    return idWithOAIs;
    }
    
    // metoda získá všechny ID těch titulů, které mají s příslušným titulem z csv shodu min 70%
    public static ArrayList<String> getIDsOfSimilarTitles(Elements items, String titleFromCSV, ArrayList<String> allTitlesOnPage) {
        ArrayList<String> selectedIDsOfSimilarTitles = new ArrayList<>();
        ArrayList<Double> distancesDecimal = ItemSorter.compareTitles(titleFromCSV, allTitlesOnPage);
        for(int i = 0; i < distancesDecimal.size(); i++) {
            if(distancesDecimal.get(i) > 0.7) {
                selectedIDsOfSimilarTitles.add(items.select("li").get(i).id());
            }
        }
    return selectedIDsOfSimilarTitles;
    }
    
    public static ArrayList<String> getOAIsOfID(Map<String,ArrayList<String>> idWithOAIs, ArrayList<String> selectedIDs) {
        ArrayList<String> selectedOAIs = new ArrayList<String>();
        for(int i = 0; i < selectedIDs.size(); i++) {
            selectedOAIs.addAll(i, idWithOAIs.get(selectedIDs.get(i)));
        }
    return selectedOAIs;
    }
    
    public static String getIDOfOAI(Map<String,ArrayList<String>> idWithOAIs, String OAI) {
        for(Map.Entry<String,ArrayList<String>> entry : idWithOAIs.entrySet()) {
            if(entry.getValue().contains(OAI)) {
                return entry.getKey();
            }
        }
    return null;
    }
    public static String getISBNfromMARC21(String isbnInfo) {
        String isbnRecord = null;
        int startIndex = isbnInfo.indexOf("|a");
        if(startIndex >= 0) {
            isbnRecord = isbnInfo.substring(startIndex + 2);
            int endIndex = isbnRecord.indexOf("|");
            if(endIndex >= 0) {
                isbnRecord = isbnInfo.substring(startIndex + 2,startIndex + 2 + endIndex);
            }
        }
    return isbnRecord;
    }
    
    public static String getCCNBfromMARC21(String isbnInfo) {
        String ccnbRecord = null;
        int startIndex = isbnInfo.indexOf("|acnb");
        if(startIndex >= 0) {
            ccnbRecord = isbnInfo.substring(startIndex + 5);
            int endIndex = ccnbRecord.indexOf("|");
            if(endIndex >= 0) {
                ccnbRecord = isbnInfo.substring(startIndex + 5,startIndex + 5 + endIndex);
            }
        }
    return ccnbRecord;
    }
    public static String getPublicationDatefromMARC21(String publicationInfo) {
        String publicationDate = null;
        int startIndex = publicationInfo.indexOf("|c");
        if(startIndex >= 0) {
            publicationDate = publicationInfo.substring(startIndex + 2);
            int endIndex = publicationDate.indexOf("|");
            if(endIndex >= 0) {
                publicationDate = publicationInfo.substring(startIndex + 2,startIndex + 2 + endIndex);
            }
        }      
    return publicationDate;
    }
    
    public static String getTitlefromMARC21(String titleInfo) {
        String titleMARC21 = null;
        int startIndex = titleInfo.indexOf("| a");
        if(startIndex >= 0) {
            titleMARC21 = titleInfo.substring(startIndex + 3);
            int endIndex = titleMARC21.indexOf("|");
            if(endIndex >= 0) {
                titleMARC21 = titleInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return titleMARC21;
    }
}
