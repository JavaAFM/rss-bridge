package org.AFM.rssbridge.uitl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.AFM.rssbridge.model.WebsiteConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class JsonReader {
    public WebsiteConfig readWebsiteConfig(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(jsonFilePath), WebsiteConfig.class);
    }
}
