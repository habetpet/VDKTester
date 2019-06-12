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
        Double distancesDecimal;
        for(int i = 0; i < allTitlesOnPage.size(); i++) {
            distancesDecimal = ItemSorter.compareTitles(titleFromCSV, allTitlesOnPage.get(i));
            if(distancesDecimal > 0.7) {
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
        String isbn = "---";
        int startIndex = isbnInfo.indexOf("|a");
        if(startIndex >= 0) {
            isbn = isbnInfo.substring(startIndex + 2);
            int endIndex = isbn.indexOf("|");
            if(endIndex >= 0) {
                isbn = isbnInfo.substring(startIndex + 2,startIndex + 2 + endIndex);
            }
        }
    return isbn;
    }
    
    public static String getWrongISBNfromMARC21(String isbnInfo) {
        String wrongISBN = "---";
        int startIndex = isbnInfo.indexOf("|z");
        if(startIndex >= 0) {
            wrongISBN = isbnInfo.substring(startIndex + 2);
            int endIndex = wrongISBN.indexOf("|");
            if(endIndex >= 0) {
                wrongISBN = isbnInfo.substring(startIndex + 2,startIndex + 2 + endIndex);
            }
        }
    return wrongISBN;
    }
    public static String getCCNBfromMARC21(String isbnInfo) {
        String ccnb = "---";
        int startIndex = isbnInfo.indexOf("|acnb");
        if(startIndex >= 0) {
            ccnb = isbnInfo.substring(startIndex + 5);
            int endIndex = ccnb.indexOf("|");
            if(endIndex >= 0) {
                ccnb = isbnInfo.substring(startIndex + 5,startIndex + 5 + endIndex);
            }
        }
    return ccnb;
    }
    public static String getPublicationDatefromMARC21(String publicationInfo) {
        String publicationDate = "---";
        publicationInfo = publicationInfo.replaceAll("\\s", "");
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
    
    public static String getPublisherfromMARC21(String publicationInfo) {
        String publisher = "---";
        int startIndex = publicationInfo.indexOf("| b");
        if(startIndex >= 0) {
            publisher = publicationInfo.substring(startIndex + 3);
            int endIndex = publisher.indexOf("|");
            if(endIndex >= 0) {
                publisher = publicationInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return publisher;
    }
    
    public static String getPlaceOfPublicationfromMARC21(String publicationInfo) {
        String placeOfPublication = "---";
        int startIndex = publicationInfo.indexOf("| a");
        if(startIndex >= 0) {
            placeOfPublication = publicationInfo.substring(startIndex + 3);
            int endIndex = placeOfPublication.indexOf("|");
            if(endIndex >= 0) {
                placeOfPublication = publicationInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return placeOfPublication;
    }

    public static String getTitlefromMARC21(String titleInfo) {
        String title = "---";
        int startIndex = titleInfo.indexOf("| a");
        if(startIndex >= 0) {
            title = titleInfo.substring(startIndex + 3);
            int endIndex = title.indexOf("|");
            if(endIndex >= 0) {
                title = titleInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return title;
    }
    
    public static String getRemainderOfTitlefromMARC21(String titleInfo) {
        String remainderOfTitle = "---";
        int startIndex = titleInfo.indexOf("| b");
        if(startIndex >= 0) {
            remainderOfTitle = titleInfo.substring(startIndex + 3);
            int endIndex = remainderOfTitle.indexOf("|");
            if(endIndex >= 0) {
                remainderOfTitle = titleInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return remainderOfTitle;
    }
    
    public static String getExtentfromMARC21(String physicalDescriptionInfo) {
        String extent = "---";
        int startIndex = physicalDescriptionInfo.indexOf("| a");
        if(startIndex >= 0) {
            extent = physicalDescriptionInfo.substring(startIndex + 3);
            int endIndex = extent.indexOf("|");
            if(endIndex >= 0) {
                extent = physicalDescriptionInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return extent;
    }
    
    public static String getMainEntryPersonalNamefromMARC21(String mainEntryPersonalNameInfo) {
        String mainEntryPersonalName = "---";
        int startIndex = mainEntryPersonalNameInfo.indexOf("| a");
        if(startIndex >= 0) {
            mainEntryPersonalName = mainEntryPersonalNameInfo.substring(startIndex + 3);
            int endIndex = mainEntryPersonalName.indexOf("|");
            if(endIndex >= 0) {
                mainEntryPersonalName = mainEntryPersonalNameInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return mainEntryPersonalName;
    }
    
    public static String getMainEntryfromMARC21(String mainEntryInfo) {
        String mainEntryPersonalName = "---";
        int startIndex = mainEntryInfo.indexOf("| a");
        if(startIndex >= 0) {
            mainEntryPersonalName = mainEntryInfo.substring(startIndex + 3);
            int endIndex = mainEntryPersonalName.indexOf("|");
            if(endIndex >= 0) {
                mainEntryPersonalName = mainEntryInfo.substring(startIndex + 3,startIndex + 3 + endIndex);
            }
        }      
    return mainEntryPersonalName;
    }
    
    public static ArrayList<String> getAddedEntryPersonalNamefromMARC21(ArrayList<String> addedEntryPersonalNameInfo) {
        ArrayList<String> addedEntryPersonalName = new ArrayList<>();
        if (addedEntryPersonalNameInfo.isEmpty()) {
            addedEntryPersonalName.add("---");
        } else {
            for (int i = 0; i < addedEntryPersonalNameInfo.size(); i++) {
                String substrg = addedEntryPersonalNameInfo.get(i);
                int startIndex = substrg.indexOf("| a");
                if(startIndex >= 0) {
                    substrg = substrg.substring(startIndex + 3);
                    int endIndex = substrg.indexOf("|");
                    if(endIndex >= 0) {
                        addedEntryPersonalName.add(addedEntryPersonalNameInfo.get(i).substring(startIndex + 3,startIndex + 3 + endIndex));
                    } else {
                        addedEntryPersonalName.add(substrg);
                    } 
                }  
            }
        }
    return addedEntryPersonalName;
    }
}
