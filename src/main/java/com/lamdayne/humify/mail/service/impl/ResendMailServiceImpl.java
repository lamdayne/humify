package com.lamdayne.humify.mail.service.impl;

import com.lamdayne.humify.mail.service.MailService;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RESEND_MAIL_SERVICE")
public class ResendMailServiceImpl implements MailService {

    @Value("${resend.default.from}")
    private String defaultFrom;

    private final TemplateEngine templateEngine;
    private final Resend resend;

    @Async
    @Override
    public void sendEmailWithTemplatePlatform(String recipient, String subject, String templateId, Map<String, Object> variables) {
        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(defaultFrom)
                .addTo(recipient)
                .subject(subject)
                .template(Template.builder()
                        .id(templateId)
                        .variables(variables)
                        .build()
                )
                .build();

        executeSend(options);
    }

    @Async
    @Override
    public void sendEmailWithTemplateHTML(String recipient, String subject, String templateFileName, Map<String, Object> variables) {
        String htmlContent = renderHtmlContent(templateFileName, variables);

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(defaultFrom)
                .addTo(recipient)
                .subject(subject)
                .html(htmlContent)
                .build();

        executeSend(options);
    }

    @Async
    @Override
    public void sendBulkEmailWithTemplatePlatform(List<String> recipients, String subject, String templateId, Map<String, Object> variables) {
        if (recipients == null || recipients.isEmpty()) return;

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(defaultFrom)
                .addTo(defaultFrom)
                .bcc(recipients)
                .subject(subject)
                .template(Template.builder()
                        .id(templateId)
                        .variables(variables)
                        .build()
                )
                .build();

        executeSend(options);
    }

    @Async
    @Override
    public void sendBulkEmailWithTemplateHTML(List<String> recipients, String subject, String templateFileName, Map<String, Object> variables) {
        String htmlContent = renderHtmlContent(templateFileName, variables);

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(defaultFrom)
                .addTo(defaultFrom)
                .bcc(recipients)
                .subject(subject)
                .html(htmlContent)
                .build();

        executeSend(options);
    }

    private void executeSend(CreateEmailOptions options) {
        log.info("Sending email to: {}", options.getTo());
        try {
            resend.emails().send(options);
        } catch (Exception e) {
            log.error("Send email failed: {}", e.getMessage());
        }
    }

    private String renderHtmlContent(String templateFileName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }

        return templateEngine.process(templateFileName, context);
    }

}
