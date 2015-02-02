package org.fenixedu.reports.domain;

import java.util.Iterator;
import java.util.Locale;

import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.oddjet.Template;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class ReportTemplate extends ReportTemplate_Base {

    public ReportTemplate(String key, LocalizedString name, LocalizedString description, Locale locale, GenericFile file) {
        setReportKey(key);
        setName(name);
        setDescription(description);
        addLocalizedFileHistory(new LocalizedFileHistory(locale, file));
        setSystem(ReportTemplatesSystem.getInstance());
    }

    public Template getTemplate(Locale l) {
        GenericFile f = getTemplateFile(l);
        return f != null ? new Template(f.getContent(), l) : null;
    }

    public LocalizedFileHistory getFileHistory(Locale l) {
        Iterator<LocalizedFileHistory> fhIt = getLocalizedFileHistorySet().iterator();
        LocalizedFileHistory fh;
        while (fhIt.hasNext()) {
            fh = fhIt.next();
            if (fh.getLocale().equals(l)) {
                return fh;
            }
        }
        return null;
    }

    public GenericFile getTemplateFile(Locale l) {
        LocalizedFileHistory fh = getFileHistory(l);
        return fh != null ? fh.getLatestFile() : null;
    }

    @Atomic(mode = TxMode.WRITE)
    public void addTemplateFile(Locale l, GenericFile f) {
        LocalizedFileHistory fh = getFileHistory(l);
        if (fh == null) {
            addLocalizedFileHistory(new LocalizedFileHistory(l, f));
        } else {
            fh.addTemplateFile(f);
        }
    }

    @Atomic(mode = TxMode.WRITE)
    public void delete() {
        Iterator<LocalizedFileHistory> iterator = getLocalizedFileHistorySet().iterator();
        while (iterator.hasNext()) {
            LocalizedFileHistory fh = iterator.next();
            removeLocalizedFileHistory(fh);
            fh.delete();
        };
        setSystem(null);
        deleteDomainObject();
    };

}
