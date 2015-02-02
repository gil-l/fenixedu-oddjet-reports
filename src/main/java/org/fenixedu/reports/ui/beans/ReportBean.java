package org.fenixedu.reports.ui.beans;

import java.io.Serializable;

import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.reports.domain.ReportTemplate;

public class ReportBean implements Serializable {

    private static final long serialVersionUID = 7061427982068967745L;
    public String key;
    public LocalizedString name;
    public LocalizedString description;

    public ReportBean(ReportTemplate report) {
        key = report.getReportKey();
        name = report.getName();
        description = report.getDescription();
    }

    public String getKey() {
        return key;
    }

    public LocalizedString getName() {
        return name;
    }

    public LocalizedString getDescription() {
        return description;
    }

}
