package org.example.parser.config;

import lombok.AllArgsConstructor;
import org.example.parser.uitl.ProxyParser;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@AllArgsConstructor
public class WebDriverConfig {
    private final ProxyParser proxyParser;

    private final int MAX_WAIT = 20;

    @Bean
    public WebDriver webDriver() {
        String proxyAddress = proxyParser.getRandomValidProxy();
        ChromeOptions options = new ChromeOptions();

        if (proxyAddress != null) {
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyAddress)
                    .setSslProxy(proxyAddress);
            options.setProxy(proxy);
        }
        options.addArguments("--headless");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        options.addArguments("accept-language=en-US,en;q=0.9");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");


        return new ChromeDriver(options);
    }

    @Bean
    public WebDriverWait webDriverWait() {
        return new WebDriverWait(webDriver(), Duration.ofSeconds(MAX_WAIT));
    }
}