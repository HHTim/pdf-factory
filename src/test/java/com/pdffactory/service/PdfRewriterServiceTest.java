package com.pdffactory.service;

import com.pdffactory.dto.PdfRewriteRequest;
import com.pdffactory.dto.PdfRewriteResponse;
import com.pdffactory.dto.PdfSecurityInfo;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PDF 重寫服務測試
 */
@SpringBootTest
class PdfRewriterServiceTest {

    @Autowired
    private PdfRewriterService pdfRewriterService;

    @Autowired
    private PdfReaderService pdfReaderService;

    private Path testDir;
    private String testInputPath;
    private String testOutputPath;
    private final String testPassword = "testPassword123";

    @BeforeEach
    void setUp() throws Exception {
        // 建立測試目錄
        testDir = Files.createTempDirectory("pdf-rewriter-test");
        testInputPath = testDir.resolve("test-input.pdf").toString();
        testOutputPath = testDir.resolve("test-output.pdf").toString();

        // 建立測試 PDF（有加密）
        createTestPdf(testInputPath, testPassword);
    }

    @AfterEach
    void tearDown() throws Exception {
        // 清理測試檔案
        Files.deleteIfExists(Path.of(testInputPath));
        Files.deleteIfExists(Path.of(testOutputPath));
        Files.deleteIfExists(testDir);
    }

    /**
     * 建立測試 PDF
     */
    private void createTestPdf(String path, String password) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));

        // 設定加密
        writer.setEncryption(
                null,  // 無使用者密碼
                password.getBytes(),  // 擁有者密碼
                PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_SCREENREADERS,  // 允許列印和螢幕閱讀器
                PdfWriter.ENCRYPTION_AES_128  // 128-bit AES
        );

        document.open();
        document.add(new Paragraph("This is a test PDF document."));
        document.add(new Paragraph("這是一個測試 PDF 文件。"));
        document.add(new Paragraph("Line 3: Testing Chinese characters 中文測試"));
        document.close();
    }

    @Test
    void testRewritePdfPreserveSecurity() {
        // 準備請求
        PdfRewriteRequest request = PdfRewriteRequest.builder()
                .inputPath(testInputPath)
                .outputPath(testOutputPath)
                .ownerPassword(testPassword)
                .preserveSecurity(true)
                .build();

        // 執行重寫
        PdfRewriteResponse response = pdfRewriterService.rewritePdf(request);

        // 驗證
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessage());
        assertEquals(testOutputPath, response.getOutputPath());

        // 驗證輸出檔案存在
        File outputFile = new File(testOutputPath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        // 驗證安全性設定保留
        PdfSecurityInfo originalSecurity = response.getOriginalSecurityInfo();
        PdfSecurityInfo newSecurity = response.getNewSecurityInfo();

        assertNotNull(originalSecurity);
        assertNotNull(newSecurity);

        assertTrue(originalSecurity.isEncrypted());
        assertTrue(newSecurity.isEncrypted());

        assertEquals(originalSecurity.getEncryptionLevel(), newSecurity.getEncryptionLevel());
        assertEquals(originalSecurity.isAllowPrinting(), newSecurity.isAllowPrinting());
        assertEquals(originalSecurity.isAllowScreenReaders(), newSecurity.isAllowScreenReaders());
    }

    @Test
    void testRewritePdfToBytes() {
        // 準備請求（不提供 outputPath）
        PdfRewriteRequest request = PdfRewriteRequest.builder()
                .inputPath(testInputPath)
                .ownerPassword(testPassword)
                .preserveSecurity(true)
                .build();

        // 執行重寫
        PdfRewriteResponse response = pdfRewriterService.rewritePdf(request);

        // 驗證
        assertTrue(response.isSuccess());
        assertNotNull(response.getPdfBytes());
        assertTrue(response.getPdfBytes().length > 0);
        assertNotNull(response.getFileSize());
        assertTrue(response.getFileSize() > 0);
    }

    @Test
    void testExtractSecurityInfo() {
        // 提取安全性資訊
        PdfSecurityInfo securityInfo = pdfReaderService.extractSecurityInfo(testInputPath, testPassword);

        // 驗證
        assertNotNull(securityInfo);
        assertTrue(securityInfo.isEncrypted());
        assertEquals(128, securityInfo.getEncryptionLevel());
        assertTrue(securityInfo.getEncryptionAlgorithm().contains("AES"));
        assertTrue(securityInfo.isAllowPrinting());
        assertTrue(securityInfo.isAllowScreenReaders());
    }

    @Test
    void testRewriteUnencryptedPdf() throws Exception {
        // 建立無加密的測試 PDF
        String unencryptedPath = testDir.resolve("test-unencrypted.pdf").toString();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(unencryptedPath));
        document.open();
        document.add(new Paragraph("Unencrypted PDF"));
        document.close();

        // 準備請求
        PdfRewriteRequest request = PdfRewriteRequest.builder()
                .inputPath(unencryptedPath)
                .outputPath(testOutputPath)
                .preserveSecurity(true)
                .build();

        // 執行重寫
        PdfRewriteResponse response = pdfRewriterService.rewritePdf(request);

        // 驗證
        assertTrue(response.isSuccess());

        // 驗證安全性資訊
        PdfSecurityInfo originalSecurity = response.getOriginalSecurityInfo();
        assertNotNull(originalSecurity);
        assertFalse(originalSecurity.isEncrypted());

        // 清理
        Files.deleteIfExists(Path.of(unencryptedPath));
    }
}
