package vdktester_v03;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;


public class SaveDuplicitiesHTML extends Task<Integer>{
    
    File file;
    TextArea logTA;
    
    public SaveDuplicitiesHTML(File file, TextArea logTA) {
        this.file = file;
        this.logTA = logTA;
    }
    
    @Override
    protected Integer call() throws Exception {
        if(file == null) return -1;
        
        try{
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println("<html> <head> </head> <body>");
            int progress = 1;
            int max = Settings.duplicities.size();
            
            for (Unit unit : Settings.duplicities) {
                writer.println("#" + progress + "\t" + unit.title + "\t" + "<a href=\"" + unit.sourceURL + "\">orig</a>\t<a href=\"" + unit.duplicityURL + "\">conflict</a><br>");
                progress++;
                this.updateProgress(progress, max);
            }
            
            writer.println("</body> </html>");
            writer.close();
        }
        catch (IOException ex) {
        }
        
        logTA.setText("Duplicities saved as:\n"
                    + file.getAbsolutePath());
        
        return 0;
    }

}
