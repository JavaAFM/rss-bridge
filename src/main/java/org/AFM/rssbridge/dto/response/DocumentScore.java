package org.AFM.rssbridge.dto.response;

import lombok.Data;

@Data
public class DocumentScore {
    private Long news_id;
    private String tag;
    private double similarity_score;
    private String text;
    private boolean isNegative;
    private double sentiment_score;
    private String lang;
}
