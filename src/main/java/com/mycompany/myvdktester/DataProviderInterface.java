/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.myvdktester;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author HABETINOVAP
 */
public interface DataProviderInterface {
    public HashMap<String, String[]> getCSVData(String source, String filePath);   
    public HashMap<String, ArrayList<String>> getMARC21Data(String url);
}

