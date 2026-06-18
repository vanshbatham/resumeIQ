package com.resumeiq.service.parsing;

import com.resumeiq.exception.ScannedPdfException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfTextExtractor implements TextExtractor {

    private static final int SCANNED_TEXT_THRESHOLD = 100;

    @Override
    public String extract(InputStream inputStream) throws IOException {
        byte[] pdfBytes = inputStream.readAllBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String text = stripper.getText(document);

            if (text.trim().length() < SCANNED_TEXT_THRESHOLD) {
                throw new ScannedPdfException();
            }

            return text;
        }
    }

    @Override
    public boolean supports(String contentType) {
        return "application/pdf".equalsIgnoreCase(contentType);
    }
}