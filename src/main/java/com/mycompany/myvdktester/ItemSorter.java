package com.mycompany.myvdktester;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.jsoup.nodes.Document;


/**
 *
 * @author HABETINOVAP
 */
public class ItemSorter {
    
    // Automatické porovnávání podle celkového počtu zpětně nalezených titulů
    public static ArrayList<Integer> sortAutomaticItems(String[] titles) throws IOException, URISyntaxException {
        ArrayList<Integer> positionOfUniques = new ArrayList<>();
        int totalHits;        
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            String searchedURL = new URLBuilder().buildRightURL(title);
            Document doc = DocumentManager.getDocument(searchedURL);
            totalHits = new HTMLParser().getNumberOfResults(doc);
            if (totalHits == 1) { 
                positionOfUniques.add(i);
            }
        }        
    return positionOfUniques;     
    } 
    
    // Porovnání dvou stringů pomocí Jaro–Winkler distance
    public static Double compareTitles(String firstTitle, String secondTitle) {
        Double distancesDecimal;
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        firstTitle = firstTitle.replaceAll("\\p{Punct}", "").replaceAll(" ", "").toLowerCase();
        secondTitle = secondTitle.replaceAll("\\p{Punct}", "").replaceAll(" ", "").toLowerCase();
            if(firstTitle.contains(secondTitle) || secondTitle.contains(firstTitle)) {
                distancesDecimal = 1.0;
            }
            else {
                distancesDecimal = jaroWinklerDistance.apply(firstTitle, secondTitle); 
            }  
    return distancesDecimal;
    }
}