package com.mycompany.myvdktester;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;
import org.apache.http.client.utils.URIBuilder;

/**
 *
 * @author HABETINOVAP
 */
public class URLBuilder {
   
   private final String COMMON_PATH = "http://vdk.nkp.cz/vdk";
   private String title = "";
   private HashMap<String, Boolean> sources = new HashMap<String, Boolean>();;
    
    private String adjustTitle(String title) {
        String adjustedTitle = title;
        if(title.contains(":") || title.contains("/") || title.contains("[") || title.contains("]")) {
            adjustedTitle = title.replaceAll(":", "\\\\:").replaceAll("/", "\\\\/").replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
        }     
    return adjustedTitle;
    }
    
    // výstup: adresa stránky potřebná v manuálním třídění v levém okně
    public String buildLeftURL(String searchedTitle, HashMap selectedSources) throws URISyntaxException, MalformedURLException {
        URIBuilder builder = new URIBuilder();
        builder.setPath(this.COMMON_PATH);
        this.title = adjustTitle(searchedTitle);
        builder.addParameter("title", this.title);
        builder.addParameter("fq", "bohemika:\"true\"");
        this.sources = selectedSources; 
        this.sources.forEach((key, value) -> {            
            if(Objects.equals(value, Boolean.TRUE)) {
                builder.addParameter("zdroj", key);
            }
            else {
                builder.addParameter("zdroj", "-" + key);
            }
        });
    return builder.build().toURL().toString();
    }
    
    // výstup: adresa stránky potřebná v manuálním třídění v pravém okně
    // !!!PROBLÉM: pokud se funce zavolá po builLeftURL zůstává v URL filtr zdroj
    public String buildRightURL(String searchedTitle) throws URISyntaxException, MalformedURLException {
        URIBuilder builder = new URIBuilder();
        builder.setPath(this.COMMON_PATH);
        this.title = adjustTitle(searchedTitle); 
        builder.addParameter("title", this.title);
        builder.addParameter("fq", "bohemika:\"true\"");
    return builder.build().toURL().toString();
    }
    
    // NETESTOVÁNO - zatím se nikde nepoužívá
    public String buildPaginatedURL(String page) throws URISyntaxException, MalformedURLException {
        URIBuilder builder = new URIBuilder();
        this.sources.forEach((key, value) -> {            
            if(Objects.equals(value, Boolean.TRUE)) {
                builder.addParameter("zdroj", "-" + key);
            }
        });
        builder.addParameter("offset" , page);
    return builder.build().toURL().toString();
    }
    
    public String buildMarc21RecordURL(String OAI) throws URISyntaxException, MalformedURLException {
        URIBuilder builder = new URIBuilder();
        builder.setPath(this.COMMON_PATH + "/original");
        builder.addParameter("fq", "bohemika:\"true\"");
        builder.addParameter("id", OAI);
    return builder.build().toURL().toString();
    }
}
