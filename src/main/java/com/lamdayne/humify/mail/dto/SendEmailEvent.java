package com.lamdayne.humify.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailEvent {

    private String to;

    private String subject;

    private String templateId;

    private String templateFileName;

    private Map<String, Object> variables;

}
