package com.mycompany.myvdktester;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.text.similarity.JaroWinklerDistance;
//import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.nodes.Document;


/**
 *
 * @author HABETINOVAP
 */
public class ItemSorter {
    
    public static ArrayList<Integer> sortAutomaticItems(ArrayList<String> titles, HashMap selectedSources) throws IOException, URISyntaxException {
        ArrayList<Integer> positionOfUniques = new ArrayList<>();
        int totalHits;        
        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            String searchedURL = new URLBuilder().buildRightURL(title);
            Document doc = DocumentManager.getDocument(searchedURL);
            totalHits = new HTMLParser().getNumberOfResults(doc);
            if (totalHits == 1) { 
                positionOfUniques.add(i);
            }
        }        
    return positionOfUniques;     
    } 
    
    // vstup: String titleFromCSV - název daného titulu z exportovaného souboru csv
    //        ArrayList<String> potencialTitles - názvy všech titulů nalezených po vyhledání searchedTitle ve VDK
    // výstup: ArrayList<Double> distancesDecimal - Levenshteinovy vzdálenosti mezi searchedTitle a jednotlivými potenciálními tituly
    //                                              pokud je jeden prefix druhého, vzdálenost je 1.0   
    // TODO: rozdělit potencialTitle a titleFromCSV na slova a ty zkontrolovat podle Levenshteina
    // získá se tím důslednější porovnávání titulů!
    public static Double compareTitles(String firstTitle, String secondTitle) {
        Double distancesDecimal;
        //LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        firstTitle = firstTitle.replaceAll("\\p{Punct}", "").replaceAll(" ", "").toLowerCase();
        secondTitle = secondTitle.replaceAll("\\p{Punct}", "").replaceAll(" ", "").toLowerCase();
            if(firstTitle.contains(secondTitle) || secondTitle.contains(firstTitle)) {
                distancesDecimal = 1.0;
            }
            else {
                //int longerLength = Math.max(firstTitle.length(), secondTitle.length());
                //int distance = levenshteinDistance.apply(firstTitle, secondTitle);
                //distancesDecimal = (longerLength - distance)/(double)longerLength;
                distancesDecimal = jaroWinklerDistance.apply(firstTitle, secondTitle); 
            }
     
    return distancesDecimal;
    }
}