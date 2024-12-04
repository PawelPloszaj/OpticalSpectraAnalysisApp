package com.pbs.edu.opticalspectraanalysis;

import java.io.File;
import java.util.prefs.*;


public class Configuration {
     String SavePath;

    public String getSavePathValue() {
        String path = System.getProperty("user.home") +  File.separator + "Downloads";
        Preferences systemNode = Preferences.systemNodeForPackage(Configuration.class);
        SavePath = systemNode.get("SavePath", path);
        return SavePath;
    }

    public void setSavePath(String path) {
        SavePath = path;
        Preferences systemNode = Preferences.systemNodeForPackage(Configuration.class);
        systemNode.put("SavePath", path);
    }

}
