package org.fenixedu.reports.domain;

import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

import org.fenixedu.bennu.io.domain.GenericFile;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class LocalizedFileHistory extends LocalizedFileHistory_Base {

    public LocalizedFileHistory(Locale locale, GenericFile file) {
        setLocale(locale);
        addTemplateFile(file);
    }

    public GenericFile getLatestFile() {
        return stream().findFirst().orElse(null);
    }

    @Atomic(mode = TxMode.WRITE)
    public void delete() {
        Iterator<GenericFile> iterator = getTemplateFileSet().iterator();
        while (iterator.hasNext()) {
            GenericFile file = iterator.next();
            removeTemplateFile(file);
            file.delete();
        };
        deleteDomainObject();
    }

    public Stream<GenericFile> stream() {
        return getTemplateFileSet().stream().sorted((f1, f2) -> f2.getCreationDate().compareTo(f1.getCreationDate()));
    }
}
