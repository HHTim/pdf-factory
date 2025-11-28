package com.pdffactory.service;

import com.pdffactory.dto.PdfSecurityInfo;
import com.pdffactory.exception.PdfProcessingException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * PDF 讀取服務
 * 負責讀取 PDF 檔案並提取安全性資訊
 */
@Slf4j
@Service
public class PdfReaderService {

    /**
     * 讀取 PDF 並提取安全性資訊
     *
     * @param inputPath PDF 檔案路徑
     * @param password 密碼（可選）
     * @return PDF 安全性資訊
     */
    public PdfSecurityInfo extractSecurityInfo(String inputPath, String password) {
        try {
            byte[] passwordBytes = password != null ? password.getBytes() : null;
            PdfReader reader = new PdfReader(inputPath, passwordBytes);

            PdfSecurityInfo securityInfo = buildSecurityInfo(reader);

            reader.close();

            log.info("成功讀取 PDF 安全性資訊: {}", inputPath);
            return securityInfo;

        } catch (IOException e) {
            log.error("讀取 PDF 失敗: {}", inputPath, e);
            throw new PdfProcessingException("讀取 PDF 失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 讀取 PDF 並返回 PdfReader
     *
     * @param inputPath PDF 檔案路徑
     * @param password 密碼（可選）
     * @return PdfReader 實例
     */
    public PdfReader getPdfReader(String inputPath, String password) {
        try {
            byte[] passwordBytes = password != null ? password.getBytes() : null;
            return new PdfReader(inputPath, passwordBytes);
        } catch (IOException e) {
            log.error("建立 PdfReader 失敗: {}", inputPath, e);
            throw new PdfProcessingException("建立 PdfReader 失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 從 PdfReader 建立安全性資訊
     *
     * @param reader PdfReader 實例
     * @return PDF 安全性資訊
     */
    private PdfSecurityInfo buildSecurityInfo(PdfReader reader) {
        boolean encrypted = reader.isEncrypted();
        int permissions = encrypted ? reader.getPermissions() : -1;

        // 取得 PDF metadata
        Map<String, String> info = reader.getInfo();
        String creator = info.getOrDefault("Creator", "Unknown");
        String producer = info.getOrDefault("Producer", "Unknown");

        // 解析權限
        Map<String, Boolean> permissionMap = parsePermissions(permissions);

        // 判斷加密等級
        Integer encryptionLevel = null;
        String encryptionAlgorithm = "None";

        if (encrypted) {
            int cryptoMode = reader.getCryptoMode();
            encryptionLevel = determineEncryptionLevel(cryptoMode);
            encryptionAlgorithm = getEncryptionAlgorithmName(cryptoMode);
        }

        return PdfSecurityInfo.builder()
                .encrypted(encrypted)
                .encryptionLevel(encryptionLevel)
                .encryptionAlgorithm(encryptionAlgorithm)
                .permissions(permissions)
                .allowPrinting(permissionMap.get("allowPrinting"))
                .allowDegradedPrinting(permissionMap.get("allowDegradedPrinting"))
                .allowModifyContents(permissionMap.get("allowModifyContents"))
                .allowCopy(permissionMap.get("allowCopy"))
                .allowModifyAnnotations(permissionMap.get("allowModifyAnnotations"))
                .allowFillIn(permissionMap.get("allowFillIn"))
                .allowScreenReaders(permissionMap.get("allowScreenReaders"))
                .allowAssembly(permissionMap.get("allowAssembly"))
                .pdfVersion(reader.getPdfVersion() + "")
                .creator(creator)
                .producer(producer)
                .hasUserPassword(encrypted)
                .hasOwnerPassword(encrypted)
                .build();
    }

    /**
     * 解析 PDF 權限碼
     *
     * @param permissions 權限碼
     * @return 權限映射表
     */
    private Map<String, Boolean> parsePermissions(int permissions) {
        Map<String, Boolean> permissionMap = new HashMap<>();

        if (permissions == -1) {
            // 無加密，所有權限開放
            permissionMap.put("allowPrinting", true);
            permissionMap.put("allowDegradedPrinting", true);
            permissionMap.put("allowModifyContents", true);
            permissionMap.put("allowCopy", true);
            permissionMap.put("allowModifyAnnotations", true);
            permissionMap.put("allowFillIn", true);
            permissionMap.put("allowScreenReaders", true);
            permissionMap.put("allowAssembly", true);
        } else {
            // 根據 PDF 權限位元遮罩解析
            permissionMap.put("allowPrinting", (permissions & PdfWriter.ALLOW_PRINTING) != 0);
            permissionMap.put("allowDegradedPrinting", (permissions & PdfWriter.ALLOW_DEGRADED_PRINTING) != 0);
            permissionMap.put("allowModifyContents", (permissions & PdfWriter.ALLOW_MODIFY_CONTENTS) != 0);
            permissionMap.put("allowCopy", (permissions & PdfWriter.ALLOW_COPY) != 0);
            permissionMap.put("allowModifyAnnotations", (permissions & PdfWriter.ALLOW_MODIFY_ANNOTATIONS) != 0);
            permissionMap.put("allowFillIn", (permissions & PdfWriter.ALLOW_FILL_IN) != 0);
            permissionMap.put("allowScreenReaders", (permissions & PdfWriter.ALLOW_SCREENREADERS) != 0);
            permissionMap.put("allowAssembly", (permissions & PdfWriter.ALLOW_ASSEMBLY) != 0);
        }

        return permissionMap;
    }

    /**
     * 判斷加密等級
     *
     * @param cryptoMode 加密模式
     * @return 加密等級 (40/128/256)
     */
    private Integer determineEncryptionLevel(int cryptoMode) {
        switch (cryptoMode) {
            case PdfWriter.STANDARD_ENCRYPTION_40:
                return 40;
            case PdfWriter.STANDARD_ENCRYPTION_128:
            case PdfWriter.ENCRYPTION_AES_128:
                return 128;
            // OpenPDF 1.3.34 使用 ENCRYPTION_AES_256，但如果不存在則使用 128
            default:
                // 嘗試匹配 256-bit（某些版本可能沒有此常數）
                if (cryptoMode == 3) { // ENCRYPTION_AES_256 的值通常是 3
                    return 256;
                }
                return 128; // 預設
        }
    }

    /**
     * 取得加密演算法名稱
     *
     * @param cryptoMode 加密模式
     * @return 演算法名稱
     */
    private String getEncryptionAlgorithmName(int cryptoMode) {
        switch (cryptoMode) {
            case PdfWriter.STANDARD_ENCRYPTION_40:
                return "40-bit RC4";
            case PdfWriter.STANDARD_ENCRYPTION_128:
                return "128-bit RC4";
            case PdfWriter.ENCRYPTION_AES_128:
                return "128-bit AES";
            default:
                // 嘗試匹配 256-bit
                if (cryptoMode == 3) { // ENCRYPTION_AES_256 的值
                    return "256-bit AES";
                }
                return "Unknown";
        }
    }
}
