package com.pdffactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PDF Factory Application
 * 使用 OpenPDF 產生新的安全 PDF 或重寫現有 PDF 以移除 iText 字樣
 */
@SpringBootApplication
public class PdfFactoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfFactoryApplication.class, args);
    }
}
