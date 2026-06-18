package com.resumeiq.service.parsing;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class DocxTextExtractor implements TextExtractor {

    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Override
    public String extract(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (!text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            String text = paragraph.getText();
                            if (!text.isBlank()) {
                                sb.append(text).append(" ");
                            }
                        }
                    }
                    sb.append("\n");
                }
            }

            return sb.toString();
        }
    }

    @Override
    public boolean supports(String contentType) {
        return DOCX_CONTENT_TYPE.equalsIgnoreCase(contentType);
    }
}