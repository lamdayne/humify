package com.lamdayne.humify.mail.service;

import java.util.List;
import java.util.Map;

public interface MailService {

    void sendEmailWithTemplatePlatform(String recipient, String subject, String templateId, Map<String, Object> variables);

    void sendEmailWithTemplateHTML(String recipient, String subject, String templateFileName, Map<String, Object> variables);

    void sendBulkEmailWithTemplatePlatform(List<String> recipients, String subject, String templateId, Map<String, Object> variables);

    void sendBulkEmailWithTemplateHTML(List<String> recipients, String subject, String templateFileName, Map<String, Object> variables);

}
