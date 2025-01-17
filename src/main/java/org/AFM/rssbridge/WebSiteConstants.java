package org.AFM.rssbridge;

import lombok.Getter;

@Getter
public enum WebSiteConstants {
    TENGRI("https://tengrinews.kz/news/"),
    KAZTAG("https://kaztag.kz/ru/news/");
    private String label;

    WebSiteConstants(String label) {
        this.label = label;
    }
}
