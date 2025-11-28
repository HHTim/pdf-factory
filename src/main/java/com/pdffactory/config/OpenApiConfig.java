package com.pdffactory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置
 * 提供互動式 API 文檔
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pdfFactoryOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("本地開發環境");

        Contact contact = new Contact();
        contact.setName("PDF Factory Team");
        contact.setEmail("support@pdffactory.com");

        License license = new License()
                .name("LGPL 2.1 / MPL 2.0")
                .url("https://github.com/LibrePDF/OpenPDF");

        Info info = new Info()
                .title("PDF Factory API")
                .version("1.0.0")
                .description("PDF Factory 是一個專業的 PDF 處理服務，使用 OpenPDF 提供以下功能：\n\n" +
                        "## 核心功能\n" +
                        "### 1. PDF 重寫服務\n" +
                        "- 重寫現有 PDF，移除 iText 字樣\n" +
                        "- 保留或修改安全性設定\n" +
                        "- 支援檔案路徑或 Base64 上傳\n\n" +
                        "### 2. PDF 安全性服務\n" +
                        "- 套用自訂安全性設定到現有 PDF\n" +
                        "- 建立新的加密 PDF 文件\n" +
                        "- 支援多種加密演算法（RC4/AES）\n" +
                        "- 細緻的權限控制（列印、複製、修改等）\n\n" +
                        "**重要**: 所有 API 均使用 POST method，所有參數均透過 @RequestBody 傳遞")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
