/*

 Copyright (c) 2013, CDev LLC
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package co.cdev.crashReporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GMailServiceImpl implements MailService {

    private final Logger LOGGER = LoggerFactory.getLogger(GMailServiceImpl.class.getSimpleName());

    private final String username;
    private final String password;
    private final Properties properties;
    private final ExecutorService executorService;

    public GMailServiceImpl(String username, String password) {
        this.username = username;
        this.password = password;

        properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void sendMessage(final String           sender,
                            final Iterable<String> recipients,
                            final String           subject,
                            final String           body,
                            final File...          attachments)
            throws MessagingException {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

                    final Multipart multipart = new MimeMultipart() {{
                        addBodyPart(new MimeBodyPart() {{
                            setText(body);
                        }});
                    }};

                    for (final File attachment : attachments) {
                        multipart.addBodyPart(new MimeBodyPart() {{
                            setDataHandler(new DataHandler(new FileDataSource(attachment.getAbsolutePath())));
                            setFileName(attachment.getName());
                        }});
                    }

                    final MimeMessage message = new MimeMessage(session) {{
                        setFrom(new InternetAddress(sender));

                        for (String recipient : recipients) {
                            addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                        }

                        setSubject(subject);
                        setContent(multipart);
                    }};

                    Transport.send(message);
                } catch (MessagingException ex) {
                    LOGGER.error("Unable to send email with crash log attached", ex);
                }
            }
        });
    }

}
