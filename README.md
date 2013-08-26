# Crash Reporter

This is a simple crash report submission service. It sends emails with crash
reports as attachments through Gmail. Configure the crash report service by
defining a profile in your ~/.m2/settings.xml file like so (replace `###` with
actual values):

    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
      <profiles>
        <profile>
          <id>crash-reporter-dev</id>
          <activation>
            <activeByDefault>true</activeByDefault>
          </activation>
          <properties>
            <logbackRootAppender>dev</logbackRootAppender>
            <prettyPrintOutput>true</prettyPrintOutput>
            <maxListCount>200</maxListCount>
            <gmailUsername>###</gmailUsername>
            <gmailPassword>###</gmailPassword>
            <crashLogSender>###</crashLogSender>
            <crashLogRecipients>###</crashLogRecipients>
            <crashLogDirectory>${project.build.directory}/crashLogs</crashLogDirectory>
            <databaseMapping>hsql</databaseMapping>
            <databaseDriverName>org.hsqldb.jdbcDriver</databaseDriverName>
            <databaseConnectionURL>jdbc:hsqldb:mem:crash-reporter</databaseConnectionURL>
            <databaseUsername>sa</databaseUsername>
            <databasePassword></databasePassword>
          </properties>
        </profile>
        <profile>
          <id>crash-reporter-prod</id>
          <activation>
            <activeByDefault>false</activeByDefault>
            <property>
              <name>env</name>
              <value>prod</value>
            </property>
          </activation>
          <properties>
            <logbackRootAppender>prod</logbackRootAppender>
            <prettyPrintOutput>true</prettyPrintOutput>
            <maxListCount>200</maxListCount>
            <gmailUsername>###</gmailUsername>
            <gmailPassword>###</gmailPassword>
            <crashLogSender>###</crashLogSender>
            <crashLogRecipients>###</crashLogRecipients>
            <crashLogDirectory>###</crashLogDirectory>
            <databaseMapping>postgresql</databaseMapping>
            <databaseDriverName>org.postgresql.Driver</databaseDriverName>
            <databaseConnectionURL>jdbc:postgresql://localhost:5432/###</databaseConnectionURL>
            <databaseUsername>###</databaseUsername>
            <databasePassword>###</databasePassword>
          </properties>
        </profile>
      </profiles>
    </settings>

Then, start the service with:

    mvn tomcat7:run-war

To test it, issue the following command in a directory that has a crash log
named `crash.log`:

    curl -F "appName=MyApp" \
         -F "appVersion=1.0.0" \
         -F "deviceId=someDeviceId" \
         -F "file=@crash.log;type=application/octet-stream" \
         http://localhost:9999/crash-reporter/api/crash-logs

Lastly, to build a release version use the following:

    mvn package -P-crash-reporter-dev,crash-reporter-prod

This will deactivate the development profile and activate the production profile. The
resultant war file can then be deployed to a servlet container like Tomcat or Jetty.

## License

This project is licensed under the [BSD 3 Clause License](http://www.tldrlegal.com/license/bsd-3-clause-license).