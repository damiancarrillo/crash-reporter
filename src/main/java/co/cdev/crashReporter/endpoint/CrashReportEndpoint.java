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

package co.cdev.crashReporter.endpoint;

import co.cdev.agave.HTTPMethod;
import co.cdev.agave.Param;
import co.cdev.agave.Part;
import co.cdev.agave.Route;
import co.cdev.agave.configuration.RoutingContext;
import co.cdev.agave.web.HTTPResponse;
import co.cdev.agave.web.StatusCode;
import co.cdev.crashReporter.service.MailService;
import co.cdev.gson.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.File;
import java.util.HashMap;

public class CrashReportEndpoint {

    private final Logger logger = LoggerFactory.getLogger(CrashReportEndpoint.class.getSimpleName());
    private static final int FILE_MAX_SIZE = 200000;

    private final String sender;
    private final Iterable<String> recipients;
    private final MailService mailService;

    public CrashReportEndpoint(String           sender,
                               Iterable<String> recipients,
                               MailService      mailService) {
        this.sender = sender;
        this.recipients = recipients;
        this.mailService = mailService;
    }

    /**
     * Accepts a submitted crash report.
     *
     * Usage:
     *     curl -F "app=MyApp" \
     *          -F "version=1.0.0" \
     *          -F "file=@crash.log;type=application/octet-stream" \
     *          http://localhost:9999/crash-report
     *
     * @param routingContext the context that this handler method executes under
     * @param crashReportPart The crash report file.
     * @throws Exception if anything goes wrong
     * @return a destination object that wraps the index.jsp page
     */
    @Route(method = HTTPMethod.POST, uri = "/crash-report")
    public HTTPResponse crashReport(RoutingContext routingContext,
                                    @Param("app")     String     app,
                                    @Param("version") String     version,
                                    @Param("file")    Part<File> crashReportPart)
            throws Exception {
        File crashReport = crashReportPart.getContents();

        logger.info("POST /crash-report ({} bytes, {})", crashReport.length(), crashReport.getPath());

        if (crashReport.length() > FILE_MAX_SIZE) {
            logger.warn("Ignoring crash report. File size exceeded {} bytes", FILE_MAX_SIZE);
            return new JSONResponse(StatusCode._400_BadRequest, new HashMap<String, String>() {{
                put("message", "Crash report is too large.");
            }});
        }

        try {
            mailService.sendMessage(
                sender,
                recipients,
                String.format("Crash in %s %s", app, version),
                String.format("A crash has occurred in in %s version %s. The crash log is attached.\n\n", app, version),
                crashReport
            );
        } catch (MessagingException ex) {
            logger.error("Unable to send crash report email", ex);
            return new HTTPResponse(StatusCode._500_InternalServerError, ex);
        }

        return new HTTPResponse(StatusCode._200_Ok);
    }

}
