package com.resumeiq.service.parsing;

import java.io.IOException;
import java.io.InputStream;

public interface TextExtractor {
    String extract(InputStream inputStream) throws IOException;

    boolean supports(String contentType);
}