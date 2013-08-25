# Crash Reporter

This is a simple crash report submission service. It sends emails with crash
reports as attachments through Gmail. In order to run it, configure the
`application.properties` to have the desired information, then start it with:

    mvn tomcat7:run-war

To test it, issue the following command in a directory that has a crash log
named `crash.log`:

    curl -F "appName=MyApp" \
         -F "appVersion=1.0.0" \
         -F "deviceId=someDeviceId" \
         -F "file=@crash.log;type=application/octet-stream" \
         http://localhost:9999/crash-reporter/api/crash-logs

## License

This project is licensed under the [BSD 3 Clause License](http://www.tldrlegal.com/license/bsd-3-clause-license).