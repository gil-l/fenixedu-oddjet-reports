package org.fenixedu.reports.ui.beans;

import java.io.Serializable;

public class AdminDataErrorBean implements Serializable {

    private static final long serialVersionUID = -1108186100882718887L;
    public String onKey;
    public String onName;
    public String onDescription;
    public String onFiles;
    public String[] onFile;

    public AdminDataErrorBean(int nFiles) {
        onFile = new String[nFiles];
    }

    public String getOnKey() {
        return onKey;
    }

    public String getOnName() {
        return onName;
    }

    public String getOnDescription() {
        return onDescription;
    }

    public String[] getOnFile() {
        return onFile;
    }

    public String getOnFiles() {
        return onFiles;
    }

    public boolean isEmpty() {
        if (onKey != null || onName != null || onDescription != null || onFiles != null) {
            return false;
        }
        for (String element : onFile) {
            if (element != null) {
                return false;
            }
        }
        return true;
    }
};