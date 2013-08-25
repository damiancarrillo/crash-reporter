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

package co.cdev.crashReporter.api;

import co.cdev.agave.HTTPMethod;
import co.cdev.agave.Param;
import co.cdev.agave.Part;
import co.cdev.agave.Route;
import co.cdev.agave.configuration.RoutingContext;
import co.cdev.agave.web.HTTPResponse;
import co.cdev.agave.web.StatusCode;
import co.cdev.crashReporter.service.MailService;
import co.cdev.crashReporter.repository.CrashLogRepository;
import co.cdev.gson.JSONResponse;
import co.cdev.crashReporter.model.CrashLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.mail.MessagingException;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class CrashLogEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrashLogEndpoint.class.getSimpleName());
    private static final int FILE_MAX_SIZE = 200000; // bytes

    private final PersistenceManagerFactory pmf;
    private final String sender;
    private final Iterable<String> recipients;
    private final int maxFileSize;
    private final File crashLogDirectory;
    private final MailService mailService;
    private final CrashLogRepository crashLogRepository;

    public CrashLogEndpoint(PersistenceManagerFactory pmf,
                            String                    sender,
                            Iterable<String>          recipients,
                            int                       maxFileSize,
                            File                      crashLogDirectory,
                            MailService               mailService,
                            CrashLogRepository        crashLogRepository) {
        this.pmf = pmf;
        this.sender = sender;
        this.recipients = recipients;
        this.maxFileSize = maxFileSize;
        this.crashLogDirectory = crashLogDirectory;
        this.mailService = mailService;
        this.crashLogRepository = crashLogRepository;
    }

    @Route(method = HTTPMethod.POST, uri = "/api/crash-logs")
    public HTTPResponse submitCrashLog(RoutingContext routingContext,
                                       @Param("deviceId")   String     deviceId,
                                       @Param("appName")    String     appName,
                                       @Param("appVersion") String     appVersion,
                                       @Param("file")       Part<File> crashLogPart)
            throws Exception {
        return this.submitCrashLog10(routingContext, deviceId, appName, appVersion, crashLogPart);
    }

    /**
     * Accepts a submitted crash log.
     *
     * Usage:
     *     curl -F "appName=MyApp" \
     *          -F "appVersion=1.0.0" \
     *          -F "deviceId=someDeviceId" \
     *          -F "file=@crash.log;type=application/octet-stream" \
     *          http://localhost:9999/crash-reporter/api/crash-logs
     *
     * @param routingContext the context that this handler method executes under
     * @param deviceId the ID of the device that is submitting the crash log (for aggregation purposes)
     * @param appName the name of the application (taken from CFBundleName, possibly)
     * @param appVersion the version of the application (taken from CFBundleShortVersion possibly)
     * @param crashLogPart the part (as in <form type="multipart/form-data"></form>) that describes the crash log
     * @throws Exception if anything goes wrong
     * @return a destination object that wraps the index.jsp page
     * @since 1.0
     */
    @Route(method = HTTPMethod.POST, uri = "/api/1.0/crash-logs")
    public HTTPResponse submitCrashLog10(RoutingContext routingContext,
                                         @Param("deviceId")   String     deviceId,
                                         @Param("appName")    String     appName,
                                         @Param("appVersion") String     appVersion,
                                         @Param("file")       Part<File> crashLogPart)
            throws Exception {
        File crashLogFile = crashLogPart.getContents();

        LOGGER.info("POST /api/1.0/crash-logs ({} bytes, {})", crashLogFile.length(), crashLogFile.getPath());

        if (crashLogFile.length() > FILE_MAX_SIZE) {
            LOGGER.warn("Ignoring crash report. File size exceeded {} bytes", FILE_MAX_SIZE);
            return new JSONResponse(StatusCode._400_BadRequest, new HashMap<String, String>() {{
                put("message", "Crash report is too large.");
            }});
        }

        File storedCrashLogFile = new File(crashLogDirectory, String.format("%s.crash", UUID.randomUUID().toString()));

        if (crashLogFile.renameTo(storedCrashLogFile)) {
            CrashLog crashLog = new CrashLog();
            crashLog.setDeviceId(deviceId);
            crashLog.setAppName(appName);
            crashLog.setAppVersion(appVersion);
            crashLog.setFileName(storedCrashLogFile.getName());

            LOGGER.info("Moved crash log to: {}", storedCrashLogFile.getAbsolutePath());

            PersistenceManager pm = pmf.getPersistenceManager();
            Transaction tx = pm.currentTransaction();

            try {
                tx.begin();
                crashLog = crashLogRepository.store(pm, crashLog);
                tx.commit();

                LOGGER.info("Saved crash log information in database for {}", storedCrashLogFile.getName());
            } catch (JDOObjectNotFoundException ex) {
                LOGGER.error("Failed to saved crash log information in database for {}", storedCrashLogFile.getName(), ex);
                return new HTTPResponse(StatusCode._500_InternalServerError, "Unable to store crash log");
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                pm.close();
            }

            try {
                mailService.sendMessage(
                    sender,
                    recipients,
                    String.format("Crash in %s %s", appName, appVersion),
                    String.format("A crash has occurred in in %s version %s. The crash log is attached.\n\n", appName, appVersion),
                    storedCrashLogFile
                );
            } catch (MessagingException ex) {
                LOGGER.error("Unable to send crash report email", ex);
                return new HTTPResponse(StatusCode._500_InternalServerError, ex);
            }
        } else {
            LOGGER.error("Failed to move crash log to: {}, ", storedCrashLogFile.getAbsolutePath());
        }

        return new HTTPResponse(StatusCode._200_Ok);
    }

}
