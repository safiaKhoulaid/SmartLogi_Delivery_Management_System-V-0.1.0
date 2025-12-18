package com.smartlogi.sdms.application.dto.Email;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
}

