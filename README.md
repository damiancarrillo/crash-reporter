# Crash Reporter

This is a simple crash report submission service. It sends emails with crash
reports as attachments through Gmail. Configure the crash report service by
defining a profile in your ~/.m2/settings.xml (see an
[example settings.xml file](https://bitbucket.org/damiancarrillo/crash-reporter/wiki/Home#markdown-header-sample-settingsxml)).
Once you have done that, start the service with:

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