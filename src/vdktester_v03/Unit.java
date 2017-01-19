package vdktester_v03;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Unit {
    
    public String data;
    public String title;
    public URL sourceURL;
    public URL duplicityURL;
    
    public Unit(String data, String source) {
        this.data = data;
        
        String[] cols = data.split("\t");
        cols[3] = cols[3].replaceAll("\"", "");
        cols[3] = cols[3].replaceAll("\\?", "");
        cols[3] = cols[3].trim();
        
        this.title = cols[3];
        
        String url = "";
        
        //SOURCE URL
        
        try{
            url = Settings.http + URLEncoder.encode(title, "UTF-8");
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        for (int i = 0; i < Settings.sources.length; i++) {
            if(Settings.sources[i].equalsIgnoreCase(source)) {
                url = url+"&zdroj=" + Settings.sources[i];
            }
            else url = url+"&zdroj=-" + Settings.sources[i];
        }
        try {
            this.sourceURL = new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Unit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //DUPLICITY URL
        
        try {
            url = Settings.http + URLEncoder.encode(title, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        url = url+"&zdroj=-" + source;
        try {
            this.duplicityURL = new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Unit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
