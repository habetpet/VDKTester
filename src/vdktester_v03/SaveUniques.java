package vdktester_v03;

import java.io.File;
import java.io.PrintWriter;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;


public class SaveUniques extends Task<Integer>{
    
    File file;
    TextArea logTA;
    
    public SaveUniques(File file, TextArea logTA) {
        this.file = file;
        this.logTA = logTA;
    }
    
    @Override
    protected Integer call() throws Exception {
        
        try{
            PrintWriter w = new PrintWriter(file, "UTF-8");
            int progress = 1;
            int size = Settings.uniques.size();
            for (Unit unit : Settings.uniques) {
                w.println(unit.data);
                progress++;
                this.updateProgress(progress, size);
            }
            w.close();            
        } catch(Exception ex) {
            
        }
        
        logTA.setText("Uniques saved as:\n"
                    + file.getAbsolutePath());
        
        return 0;
    }

}
