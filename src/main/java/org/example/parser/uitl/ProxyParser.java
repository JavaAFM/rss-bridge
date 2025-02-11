package org.example.parser.uitl;

import lombok.AllArgsConstructor;
import org.example.parser.constants.WebSiteConstants;
import org.example.parser.service.impl.TengriServiceImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ProxyParser {
    private final ProxyChecker proxyChecker;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyParser.class);

    public String getRandomValidProxy() {
        List<String> proxies = getProxies();
        for (String proxy : proxies) {
            if (proxyChecker.isProxyWorking(proxy)) {
                LOGGER.info("Selected working proxy: {}", proxy);
                return proxy;
            }
        }
        LOGGER.warn("No working proxies found!");
        return null;
    }

    public List<String> getProxies(){
        List<String> proxyList = new ArrayList<>();

        addProxies(WebSiteConstants.PROXY_1.getLabel(), proxyList);
        addProxies(WebSiteConstants.PROXY_2.getLabel(), proxyList);

        LOGGER.warn("Found proxies: " + proxyList);
        return proxyList;
    }

    private void addProxies(String url, List<String> proxyList){
        try{
            Document doc = Jsoup.connect(url).get();
            Elements rows = doc.select("table tbody tr");

            for (Element row : rows) {
                String ip = row.select("td:nth-child(1)").text();
                String port = row.select("td:nth-child(2)").text();
                if (!ip.isEmpty() && !port.isEmpty()) {
                    proxyList.add(ip + ":" + port);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
