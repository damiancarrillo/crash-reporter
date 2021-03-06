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

package co.cdev.crashReporter.web;

import co.cdev.agave.Param;
import co.cdev.agave.Route;
import co.cdev.agave.configuration.RoutingContext;
import co.cdev.agave.web.Destinations;
import co.cdev.agave.web.HTTPResponse;
import co.cdev.agave.web.StatusCode;
import co.cdev.crashReporter.model.CrashLog;
import co.cdev.crashReporter.repository.CrashLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class CrashLogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrashLogController.class.getSimpleName());

    private final PersistenceManagerFactory pmf;
    private final int maxListCount;
    private final File crashLogDirectory;
    private final CrashLogRepository crashLogRepository;

    public CrashLogController(PersistenceManagerFactory pmf,
                              int                       maxListCount,
                              File                      crashLogDirectory,
                              CrashLogRepository        crashLogRepository) {
        this.pmf = pmf;
        this.maxListCount = maxListCount;
        this.crashLogDirectory = crashLogDirectory;
        this.crashLogRepository = crashLogRepository;
    }

    @Route("/")
    public Object welcome(RoutingContext routingContext) throws Exception {
        return viewCrashLogs(routingContext, 0, maxListCount);
    }

    @Route("/crash-logs")
    public Object viewCrashLogs(RoutingContext routingContext) throws Exception {
        return viewCrashLogs(routingContext, 0, maxListCount);
    }

    @Route("/crash-logs")
    public Object viewCrashLogs(RoutingContext routingContext,
                                @Param("index") int index,
                                @Param("count") int count)
            throws Exception {
        List<CrashLog> crashLogs = null;

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();
            crashLogs = crashLogRepository.fetch(pm, index, count);
            tx.commit();

            routingContext.getRequest().setAttribute("crashLogs", crashLogs);
        } catch (JDOObjectNotFoundException ex) {
            LOGGER.error("Unable to fetch crash log list", ex);
            return new HTTPResponse(StatusCode._500_InternalServerError, "Unable to store crash log");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        return Destinations.forward("/WEB-INF/jsp/crashLogList.jsp");
    }

    @Route("/crash-logs/device-id/${deviceId}")
    public Object viewCrashLogsWithDeviceId(RoutingContext routingContext,
                                            @Param("deviceId") String deviceId)
            throws Exception {
        return viewCrashLogsWithDeviceId(routingContext, deviceId, 0, maxListCount);
    }

    @Route("/crash-logs/device-id/${deviceId}")
    public Object viewCrashLogsWithDeviceId(RoutingContext routingContext,
                                            @Param("deviceId") String deviceId,
                                            @Param("index") long index,
                                            @Param("count") long count)
            throws Exception {
        List<CrashLog> crashLogs = null;

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();
            crashLogs = crashLogRepository.fetchCrashLogsWithDeviceId(pm, deviceId, index, count);
            tx.commit();

            routingContext.getRequest().setAttribute("crashLogs", crashLogs);
            routingContext.getRequest().setAttribute("deviceId", deviceId);
        } catch (JDOObjectNotFoundException ex) {
            LOGGER.error("Unable to fetch crash log list", ex);
            return new HTTPResponse(StatusCode._500_InternalServerError, "Unable to store crash log");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        return Destinations.forward("/WEB-INF/jsp/crashLogList.jsp");
    }

    @Route("/crash-logs/device-id/crash-log/${fileName}")
    public void viewCrashLogWithDeviceId(RoutingContext routingContext,
                                         @Param("fileName") String fileName)
            throws Exception {
        viewCrashLog(routingContext, fileName);
    }

    @Route("/crash-log/${fileName}")
    public void viewCrashLog(RoutingContext routingContext,
                             @Param("fileName") String fileName)
            throws Exception {
        File crashLog = new File(crashLogDirectory, fileName);

        routingContext.getResponse().setContentType("text/plain");
        routingContext.getResponse().setContentLength((int) crashLog.length());

        FileInputStream input = new FileInputStream(crashLog);
        FileChannel channel = input.getChannel();

        byte[] buffer = new byte[256 * 1024];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        try {
            for (int length = 0; (length = channel.read(byteBuffer)) != -1;) {
                routingContext.getResponse().getOutputStream().write(buffer, 0, length);
                byteBuffer.clear();
            }
        } finally {
            input.close();
        }

        routingContext.getResponse().setStatus(StatusCode._200_Ok.getNumericCode());
        routingContext.getResponse().flushBuffer();
    }

    @Route("/crash-logs/device-id/download/crash-log/${fileName}")
    public void downloadCrashLog(RoutingContext routingContext,
                                 @Param("fileName") String fileName)
            throws Exception {
        downloadCrashLog(routingContext, fileName);
    }

    @Route("/crash-log/download/${fileName}")
    public void downloadCrashLogWithDeviceId(RoutingContext routingContext,
                                             @Param("fileName") String fileName)
            throws Exception {
        File crashLog = new File(crashLogDirectory, fileName);

        routingContext.getResponse().setContentType("application/octet-stream");
        routingContext.getResponse().setContentLength((int) crashLog.length());

        FileInputStream input = new FileInputStream(crashLog);
        FileChannel channel = input.getChannel();

        byte[] buffer = new byte[256 * 1024];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        try {
            for (int length = 0; (length = channel.read(byteBuffer)) != -1;) {
                routingContext.getResponse().getOutputStream().write(buffer, 0, length);
                byteBuffer.clear();
            }
        } finally {
            input.close();
        }

        routingContext.getResponse().setStatus(StatusCode._200_Ok.getNumericCode());
        routingContext.getResponse().flushBuffer();
    }

}
