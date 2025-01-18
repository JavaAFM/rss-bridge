package org.AFM.rssbridge.constants;

import lombok.Getter;

@Getter
public enum WebSiteConstants {
    TENGRI_NEWS("https://tengrinews.kz/news/"),
    TENGRI_MAIN("https://tengrinews.kz/"),
    KAZTAG_NEWS("https://kaztag.kz/ru/news/"),
    KAZTAG_MAIN("https://kaztag.kz");
    private String label;

    WebSiteConstants(String label) {
        this.label = label;
    }
}
