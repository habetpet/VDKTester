package vdktester_v03;

import java.io.File;
import java.util.ArrayList;
import javafx.beans.value.ObservableStringValue;


public class Settings {
    
    public static File csvFile;
    public static final String[] sources = {"NKF", "UKF", "MZK", "VKOL", "KVKLI", "CBVK", "SVKHK"};
    public static final String http = "http://vdk.nkp.cz/vdk/?q=";
    
    public static final String msg = "default";
    
    public static ArrayList<Unit> units = new ArrayList<>();
    public static ArrayList<Unit> duplicities = new ArrayList<>();
    public static ArrayList<Unit> uniques = new ArrayList<>();
    
}
