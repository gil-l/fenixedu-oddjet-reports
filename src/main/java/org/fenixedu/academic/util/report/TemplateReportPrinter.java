package org.fenixedu.academic.util.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.oddjet.Template;
import org.fenixedu.oddjet.table.CategoricalTableData;
import org.fenixedu.oddjet.table.EntryListTableData;
import org.fenixedu.oddjet.table.PositionalTableData;
import org.fenixedu.oddjet.utils.OpenOfficePrintingService;
import org.fenixedu.oddjet.utils.PrintUtils;
import org.fenixedu.reports.domain.ReportTemplate;
import org.fenixedu.reports.domain.ReportTemplatesSystem;
import org.fenixedu.reports.exceptions.ReportsDomainException;

import com.google.common.base.Preconditions;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

public class TemplateReportPrinter implements ReportPrinter {

    private Template defaultReport;

    public TemplateReportPrinter() {
        InputStream fileStream = ReportsUtils.class.getClassLoader().getResourceAsStream("reports/default.odt");
        if (fileStream == null) {
            throw new RuntimeException("Default odt report is missing.");
        }
        defaultReport = new Template(fileStream);
    }

    @Override
    public ReportResult printReports(ReportDescription... reports) throws Exception {
        ReportResult document;
        Template template;
        ByteArrayOutputStream mergedPdfs = new ByteArrayOutputStream();
        PdfCopyFields copy = new PdfCopyFields(mergedPdfs);
        OpenOfficePrintingService service = ReportTemplatesSystem.getInstance().getPrintingService();
        if (service == null) {
            if (reports.length > 1) {
                throw ReportsDomainException.printMultipleOdts();
            } else {
                //template = getReportTemplate(reports[0].getKey(), reports[0].getLocale(), reports[0].getParameters());
                template = getReportTemplate(reports[0].getKey(), I18N.getLocale(), reports[0].getParameters());
                document = new ReportResult(template.getInstanceByteArray(), "application/vnd.oasis.opendocument.text", "odt");
            }
        } else {
            for (ReportDescription desc : reports) {
                //template = getReportTemplate(desc.getKey(), desc.getLocale(), desc.getParameters());
                template = getReportTemplate(desc.getKey(), I18N.getLocale(), desc.getParameters());
                copy.addDocument(new PdfReader(PrintUtils.print(template.getInstance(), service)));
            }
            copy.close();
            document = new ReportResult(mergedPdfs.toByteArray(), "application/pdf", "pdf");
        }
        return document;
    }

    private Template getReportTemplate(String key, Locale locale, Map<String, Object> parameters) {
        Template template;
        ReportTemplate report = ReportTemplatesSystem.getInstance().getReportTemplate(key);

        if (report == null) {
            template = defaultReport;
            prepareDefaultReport(key, "the provided key does not match any known report", parameters);
        } else if ((template = report.getTemplate(locale)) == null) {
            template = defaultReport;
            prepareDefaultReport(key, "the report does not have a template for the given locale (" + locale.getDisplayName()
                    + ")", parameters);
        } else {
            prepareReport(template, parameters);
        }
        return template;
    }

    private void prepareReport(Template report, Map<String, Object> parameters) {
        Preconditions.checkNotNull(parameters);
        Preconditions.checkNotNull(report);
        clearData(report);
        //Map<String,List> objects get converted to CategoricalTableData, Iterable<Iterable> and object[][] to PositionalTableData
        //and other Iterable objects to EntryListTableData. All others are taken as simple parameters.
        for (Entry<String, Object> parameter : parameters.entrySet()) {
            if (parameter.getValue() instanceof Map) {
                try {
                    report.addTableDataSource(parameter.getKey(),
                            new CategoricalTableData((Map<String, List>) parameter.getValue()));
                } catch (ClassCastException e) {
                    report.addTableDataSource(parameter.getKey(), new EntryListTableData((Iterable) parameter.getValue()));
                }
            } else if (parameter.getValue() instanceof Iterable) {
                try {
                    report.addTableDataSource(parameter.getKey(),
                            new PositionalTableData((Iterable<Iterable>) parameter.getValue()));
                } catch (ClassCastException e) {
                    report.addTableDataSource(parameter.getKey(), new EntryListTableData((Iterable) parameter.getValue()));
                }
            } else if (parameter.getValue() instanceof Object[][]) {
                report.addTableDataSource(parameter.getKey(), new PositionalTableData((Object[][]) parameter.getValue()));
            } else {
                report.addParameter(parameter.getKey(), parameter.getValue());
            }
        }
    }

    private void prepareDefaultReport(String key, String error, Map<String, Object> parameters) {
        Preconditions.checkNotNull(parameters);

        clearData(defaultReport);

        defaultReport.addParameter("error", error);
        defaultReport.addParameter("reportKey", key);

        Map<String, List> data = new HashMap<String, List>();
        List<String> keys = new ArrayList<String>();
        List<String> types = new ArrayList<String>();
        List<String> contents = new ArrayList<String>();
        data.put("key", keys);
        data.put("type", types);
        data.put("content", contents);

        for (Entry<String, Object> parameter : parameters.entrySet()) {
            keys.add(parameter.getKey());
            types.add(parameter.getValue().getClass().getSimpleName());
            contents.add(parameter.getValue().toString());
        }

        defaultReport.addTableDataSource("reportData", new CategoricalTableData(data));

    }

    private void clearData(Template report) {
        report.clearParameters();
        report.clearTableDataSources();
    }
}
