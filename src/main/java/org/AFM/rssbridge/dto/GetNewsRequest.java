package org.AFM.rssbridge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetNewsRequest {
    private String name;
    private String link;
    private String type;
}
