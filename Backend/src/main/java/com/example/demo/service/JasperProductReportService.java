package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class JasperProductReportService {
    private static final Logger log = LoggerFactory.getLogger(JasperProductReportService.class);

    private static final String PRODUCT_LIST_TEMPLATE = "/reports/product-list.jrxml";
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private volatile JasperReport productListReport;

    public byte[] buildProductListPdf(List<ProductDTO> products) {
        List<ProductDTO> safeProducts = products == null ? List.of() : products;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Product List");
            parameters.put("TOTAL_PRODUCTS", safeProducts.size());
            parameters.put("GENERATED_AT", LocalDateTime.now().format(EXPORT_TIME_FORMATTER));
            parameters.put(JRParameter.REPORT_LOCALE, Locale.forLanguageTag("vi-VN"));

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(safeProducts, false);
            JasperPrint jasperPrint = JasperFillManager.fillReport(getProductListReport(), parameters, dataSource);
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);

            byte[] pdfBytes = out.toByteArray();
            if (pdfBytes.length == 0) {
                throw new JRException("JasperReports returned an empty PDF");
            }
            return pdfBytes;
        } catch (Exception e) {
            log.error("Could not export product PDF with JasperReports", e);
            throw new RuntimeException("Could not export product PDF with JasperReports: " + rootMessage(e), e);
        }
    }

    private JasperReport getProductListReport() {
        JasperReport cachedReport = productListReport;
        if (cachedReport != null) {
            return cachedReport;
        }

        synchronized (this) {
            if (productListReport == null) {
                try (InputStream template = getClass().getResourceAsStream(PRODUCT_LIST_TEMPLATE)) {
                    if (template == null) {
                        throw new IllegalStateException("Jasper template not found: " + PRODUCT_LIST_TEMPLATE);
                    }
                    productListReport = JasperCompileManager.compileReport(template);
                } catch (Exception e) {
                    log.error("Could not compile Jasper template {}", PRODUCT_LIST_TEMPLATE, e);
                    throw new RuntimeException("Could not compile Jasper template " + PRODUCT_LIST_TEMPLATE + ": " + rootMessage(e), e);
                }
            }
            return productListReport;
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
