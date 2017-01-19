package vdktester_v03;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;


public class LoadCSV extends Task<Integer>{
    
    String zdroj;
    TextArea logTA;
    
    public LoadCSV(String zdroj, TextArea logTA) {
        this.zdroj = zdroj;
        this.logTA = logTA;
    }
    
    @Override
    protected Integer call() throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Settings.csvFile), "UTF-8"));
        int size = br.lines().toArray().length;
        br.close();
        br = new BufferedReader(new InputStreamReader(new FileInputStream(Settings.csvFile), "UTF-8"));
        int counter = 0;
        String line;

        while ((line = br.readLine()) != null) {
            Settings.units.add(new Unit(line, zdroj));
            counter++;
            this.updateProgress(counter, size);
//            System.out.println("Loading progress: " + counter + " / " + size);
        }
        br.close();
        
        logTA.setText("File contains " + Settings.units.size() + " titles\n\n"
                + "1. Choose .csv file you want to check\n"
                + "2. Select source\n"
                + "3. Load file in application\n"
                + ">> 4. press Start\n"
                + "5. Save results");
        
        return 0;
    }
    
}
