package com.kadirdemirel.ott.handler;

import com.kadirdemirel.ott.enums.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OttSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OttSuccessHandler.class);
    private final OneTimeTokenGenerationSuccessHandler redirectHandler = new RedirectOneTimeTokenGenerationSuccessHandler("/ott/sent");
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${email.username}")
    private String emailUsername;

    public OttSuccessHandler(JavaMailSender mailSender, SpringTemplateEngine springTemplateEngine) {
        this.mailSender = mailSender;
        this.springTemplateEngine = springTemplateEngine;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) throws IOException, ServletException {
        String templateName = EmailTemplate.ONE_TIME_TOKEN_TEMPLATE.getName();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .path("/login/ott")
                .queryParam("token", oneTimeToken.getTokenValue());

        String magicLink = builder.toUriString();

        System.out.println("Magic Link: " + magicLink);

        try {
            var sendTo = oneTimeToken.getUsername();
            String to = "<alıcı_email_adresi>";
            log.info("Sending One Time Token to username: {}", sendTo);

            String emailContent = buildEmailContent(sendTo, magicLink, templateName);
            sendEmail(to, emailContent);

            log.info("One Time Token email successfully sent to: {}", sendTo);
        } catch (MessagingException e) {
            log.error("Failed to send One Time Token email to: {}", oneTimeToken.getUsername(), e);
        }


        this.redirectHandler.handle(request, response, oneTimeToken);
    }

    private String buildEmailContent(String username, String magicLink, String templateName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", magicLink);

        Context context = new Context();
        context.setVariables(properties);

        return springTemplateEngine.process(templateName, context);
    }

    private void sendEmail(String to, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setFrom(emailUsername);
        helper.setSubject("One Time Token Login");
        helper.setText(content, true);

        mailSender.send(message);
    }


}
