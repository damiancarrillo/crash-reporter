<%@ page contentType="application/xhtml+xml" %>
<%@ include file="/WEB-INF/jsp/common.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8" />
    <title>
      <template:useFragment name="title" />
      <fmt:message key="applicationName" />
    </title>
    <style type="text/css">

    </style>
  </head>
  <body>
    <div id="content">
      <template:useFragment name="content" />
    </div>
    <div id="sidebar">
      <template:useFragment name="sidebarHeader" />
      SIDEBAR CONTENT
    </div>
  </body>
</html>