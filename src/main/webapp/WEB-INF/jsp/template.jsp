<%@ page contentType="application/xhtml+xml" %>
<%@ include file="/WEB-INF/jsp/common.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8" />
    <title>
      <template:useFragment name="title"/>
      - <fmt:message key="applicationName"/>
    </title>
    <link rel="stylesheet" href="style/tuktuk.css"/>
    <link rel="stylesheet" href="style/tuktuk.icons.css"/>
    <link rel="stylesheet" href="style/tuktuk.theme.css"/>
    <link rel="stylesheet" href="style/main.css"/>
  </head>
  <body>
    <div id="header" class="bck theme">
      <h1 class="text bold"><span class="icon leaf"></span><template:useFragment name="title"/></h1>
    </div>
    <div id="content" class="bck light">
      <template:useFragment name="content" />
    </div>
    <div id="footer" class="bck color">
      <fmt:message key="copyright"/>
    </div>
  </body>
</html>