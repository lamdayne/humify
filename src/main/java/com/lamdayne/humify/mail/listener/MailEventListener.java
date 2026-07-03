package com.lamdayne.humify.mail.listener;

import com.lamdayne.humify.mail.dto.SendEmailEvent;
import com.lamdayne.humify.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "MAIL_EVENT_LISTENER")
public class MailEventListener {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSendEmailEvent(SendEmailEvent event) {
        if (event.getTemplateId() != null && !event.getTemplateId().isBlank()) {
            mailService.sendEmailWithTemplatePlatform(
                    event.getTo(),
                    event.getSubject(),
                    event.getTemplateId(),
                    event.getVariables()
            );
            return;
        }

        if (event.getTemplateFileName() != null && !event.getTemplateFileName().isBlank()) {
            mailService.sendEmailWithTemplateHTML(
                    event.getTo(),
                    event.getSubject(),
                    event.getTemplateFileName(),
                    event.getVariables()
            );
        }
    }

}
