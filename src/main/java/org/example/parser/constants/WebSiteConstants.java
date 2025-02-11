package org.example.parser.constants;

import lombok.Getter;

@Getter
public enum WebSiteConstants {
    TENGRI_NEWS("http://tengrinews.kz/news/"),
    TENGRI_MAIN("http://tengrinews.kz/"),
    KAZTAG_NEWS("http://kaztag.kz/ru/news/"),
    KAZTAG_MAIN("http://kaztag.kz"),
    AZATTYQ_NEWS("http://rus.azattyq.org/z/360"),
    AZATTYQ_MAIN("http://rus.azattyq.org/"),
    ORDA_NEWS("http://orda.kz/last-news/"),
    ORDA_MAIN("http://orda.kz/"),
    ZAKON_NEWS("http://www.zakon.kz/news/"),
    ZAKON_MAIN("http://www.zakon.kz/"),

    PROXY_1("https://www.sslproxies.org/"),
    PROXY_2("https://free-proxy-list.net/"),
    PROXY_3("https://spys.me/proxy.txt");
    private String label;
    WebSiteConstants(String label) {
        this.label = label;
    }
}
