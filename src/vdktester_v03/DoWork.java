package vdktester_v03;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;


public class DoWork extends Task<Integer>{
    
    TextArea logTA;
    
    public DoWork(TextArea logTA) {
        this.logTA = logTA;
    }
    
    @Override
    protected Integer call() throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        int progress = 0;
        int max = Settings.units.size();
            
        for (Unit unit : Settings.units) {
            BufferedReader br = new BufferedReader(new InputStreamReader(unit.duplicityURL.openStream(), "UTF-8"));
            String line;
            while((line = br.readLine()) != null ) {
                if(line.contains("položek: ")){
                    String[] t = line.split("položek: ");
                    if(t[1].charAt(0) == '0') Settings.uniques.add(unit);
                    else Settings.duplicities.add(unit);
                }
            }
            progress++;
//            System.out.println("Progress: " + Settings.units.indexOf(unit) + " / " + Settings.units.size());
            this.updateProgress(progress, max);
        }
        
        System.out.println("Uniques: " + Settings.uniques.size() + " / " + Settings.units.size());
        System.out.println("Duplicities: " + Settings.duplicities.size() + " / " + Settings.units.size());
        
        long endTime = System.currentTimeMillis();
        System.out.println("Task done in: " + (endTime-startTime)/1000 + "s");
        
        // LOG
        
        File log = new File("log.txt");
        PrintWriter writer = new PrintWriter(log);
        writer.println("Celkem zkontrolováno: " + Settings.units.size() + " titulů");
        writer.println("čas: " + (endTime-startTime)/1000 + "s");
        writer.println("Potvrzených unikátů: " + Settings.uniques.size() + " (" + ((double)100/Settings.units.size()*Settings.uniques.size()) + "%)");
        writer.println("Možných duplicit: : " + Settings.duplicities.size() + " (" + ((double)100/Settings.units.size()*Settings.duplicities.size()) + "%)");
        System.out.println(log.getAbsoluteFile());
        writer.close();
        
        String result = "Celkem zkontrolováno: " + Settings.units.size() + " titulů\n"
                + "čas: " + (endTime-startTime)/1000 + "s\n"
                + "Potvrzených unikátů: " + Settings.uniques.size() + " (" + ((double)100/Settings.units.size()*Settings.uniques.size()) + "%)\n"
                + "Možných duplicit: : " + Settings.duplicities.size() + " (" + ((double)100/Settings.units.size()*Settings.duplicities.size()) + "%)\n\n"
                + "1. Choose .csv file you want to check\n"
                + "2. Select source\n"
                + "3. Load file in application\n"
                + "4. press Start\n"
                + " >> 5. Save results";
        
        logTA.setText(result);
        
        return 0;
    }

}