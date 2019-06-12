package com.mycompany.myvdktester;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HABETINOVAP
 */
public class CSVParserAndWriter {
    
    // Uložení vybraných řádků do souboru .csv
    public static void saveToCSV(ArrayList<Integer> position, List<String[]> lines, File file) throws IOException {
        OutputStreamWriter encodedWriter = new OutputStreamWriter(
            new FileOutputStream(file),
            Charset.forName("UTF-8").newEncoder()
        );  
        CSVWriter writer = new CSVWriter(
            encodedWriter, 
            '\t', 
            CSVWriter.DEFAULT_QUOTE_CHARACTER, 
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, 
            CSVWriter.DEFAULT_LINE_END
        );
        for (int i = 0; i < position.size(); i++){
                writer.writeNext(lines.get(position.get(i)));
            }  
        writer.close(); 
    }
    
    // Čtení a parsování souboru csv   
    public static List<String[]> getAllElements(String zdroj, String cesta) throws IOException {
        List<String[]> lines;  
        final CSVParser parser = new CSVParserBuilder()
            .withSeparator('\t')
            .withQuoteChar('"')
            .build();        
        final CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(cesta), "UTF-8"))
            .withCSVParser(parser)
            .build();
        lines = reader.readAll();        
    return lines;
    }
    
    // Uložení všech názvů knih do ArrayList<String> titles
    public static ArrayList<String> getTitles(List<String[]> lines) {
        ArrayList<String> titles = new ArrayList<>();        
        for (int i = 0; i < lines.size(); i++){               
            titles.add(lines.get(i)[3]);              
        }
    return titles;
    }
    
    // Získání dat vydání
    public static ArrayList<String> getPublicationDates(List<String[]> lines) {
        ArrayList<String> publicationDates = new ArrayList<>();
        String publicationInfo;
        String publicationDate;
        for (int i = 0; i < lines.size(); i++){ 
            publicationInfo = lines.get(i)[10]; 
            if(publicationInfo != null && publicationInfo.lastIndexOf(",") > 0) {
                publicationDate = publicationInfo.substring(publicationInfo.lastIndexOf(",") + 1, publicationInfo.length());
                publicationDates.add(publicationDate);             
            }
            else {
                publicationDates.add(null);  
            } 
        }      
    return publicationDates;
    }
}
