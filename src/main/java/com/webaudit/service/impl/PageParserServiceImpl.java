package com.webaudit.service.impl;

import com.webaudit.service.PageParserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageParserServiceImpl implements PageParserService {

    @Override
    public String extractTitle(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return null;
        }

        try {
            Document doc = Jsoup.parse(htmlContent);
            String title = doc.title();
            if (title != null && !title.trim().isEmpty()) {
                return title.trim();
            }
        } catch (Exception e) {
            log.warn("Failed to parse HTML title with Jsoup: {}", e.getMessage());
        }

        return null;
    }
}
