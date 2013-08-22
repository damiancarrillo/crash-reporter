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

package co.cdev.crashReporter;

import co.cdev.agave.configuration.HandlerDescriptor;
import co.cdev.agave.web.*;
import co.cdev.crashReporter.endpoint.CrashLogEndpoint;
import co.cdev.crashReporter.repository.CrashLogRepository;
import co.cdev.crashReporter.repository.CrashLogRepositoryImpl;
import co.cdev.crashReporter.service.GMailServiceImpl;
import co.cdev.crashReporter.service.MailService;
import co.cdev.gson.ISO8601DateTypeAdapter;
import co.cdev.gson.JSONResponseProcessor;
import co.cdev.gson.URITypeAdapter;
import co.cdev.gson.URLTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.cdev.crashReporter.endpoint.WelcomeEndpoint;

/**
 * A simple web service filter.
 *
 * This filter provides Agave with basic functionality to operate as a basic web service.
 * Application properties are exposed to the web service in the servlet context under the
 * key of {@code APPLICATION_PROPERTIES}.
 */
public class WebServiceFilter extends AgaveFilter {

    public static final String APPLICATION_PROPERTIES = "APPLICATION_PROPERTIES";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceFilter.class.getSimpleName());

    private final Map<Class<?>, Object> endpoints = new HashMap<Class<?>, Object>();

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);

        String imageDirectoryPath = null;
        Properties applicationProperties = new Properties();

        try {
            applicationProperties.load(getClass().getResourceAsStream("/application.properties"));
        } catch (IOException ex) {
            throw new ServletException(ex);
        }

        config.getServletContext().setAttribute(APPLICATION_PROPERTIES, applicationProperties);

        Gson gson = configureGson(applicationProperties);
        PersistenceManagerFactory pmf = configurePMF(config);

        DatastoreInitializer datastoreInitializer = new DatastoreInitializer();
        datastoreInitializer.initializeDatastore(pmf);

        addResultProcessor(new JSONResponseProcessor(gson));

        MailService gmailService = new GMailServiceImpl(
                applicationProperties.getProperty("crash-reporter.gmail.username"),
                applicationProperties.getProperty("crash-reporter.gmail.password")
        );

        CrashLogRepository crashLogRepository = new CrashLogRepositoryImpl();

        endpoints.put(WelcomeEndpoint.class, new WelcomeEndpoint());

        String sender = applicationProperties.getProperty("crash-reporter.crashLog.sender");
        String packedRecipients = applicationProperties.getProperty("crash-reporter.crashLog.recipients");

        List<String> recipients = new ArrayList<String>();

        if (packedRecipients != null && !packedRecipients.isEmpty()) {
            for (String recipient : packedRecipients.split(",")) {
                recipients.add(recipient.trim());
            }
        }

        endpoints.put(CrashLogEndpoint.class, new CrashLogEndpoint(pmf, sender, recipients, gmailService, crashLogRepository));
    }

    private Gson configureGson(Properties applicationProperties) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        if ("true".equals(applicationProperties.getProperty("crash-reporter.prettyPrintOutput"))) {
            gsonBuilder.setPrettyPrinting();
            LOGGER.info("Pretty printing JSON");
        }

        gsonBuilder.registerTypeAdapter(URI.class, new URITypeAdapter());
        gsonBuilder.registerTypeAdapter(URL.class, new URLTypeAdapter());
        gsonBuilder.registerTypeAdapter(Date.class, new ISO8601DateTypeAdapter());

        // Add any custom type adapters (for converting from model objects to JSON)

        return gsonBuilder.create();
    }

    private PersistenceManagerFactory configurePMF(FilterConfig config) {
        return JDOHelper.getPersistenceManagerFactory("application.properties");
    }

    /**
     * Provides a handler factory to Agave. In lieu of creating a handler for every request,
     * the provided handler factory will use a single handler for every request. The handler
     * is configured during the initialization of this filter.
     *
     * This assumes that handlers are stateless, so do NOT store information in instance variables
     * of any handler classes.
     */
    @Override
    protected HandlerFactory provideHandlerFactory(FilterConfig filterConfig)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return new HandlerFactory() {
            @Override
            public void initialize() {
                // do nothing
            }

            @Override
            public Object createHandlerInstance(ServletContext servletContext,
                                                HandlerDescriptor descriptor) throws HandlerException {
                if (!endpoints.containsKey(descriptor.getHandlerClass())) {
                    throw new HandlerException("An instance of " + descriptor.getHandlerClass().getName() +
                            " has not been configured. Check " + WebServiceFilter.class.getName() + "#init(...).");
                }

                return endpoints.get(descriptor.getHandlerClass());
            }
        };
    }
}
