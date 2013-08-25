<%@ include file="/WEB-INF/jsp/common.jsp" %>
<template:apply path="/WEB-INF/jsp/template.jsp">
  <template:supplyFragment name="title">
    <fmt:message key="crashLogs"/>
  </template:supplyFragment>
  <template:supplyFragment name="content">
    <c:choose>
      <c:when test="${fn:length(crashLogs) gt 0}">
        <table>
          <tbody>
            <c:forEach var="crashLog" items="${crashLogs}" varStatus="row">
              <c:choose>
                <c:when test="${row.count % 2 == 0}">
                  <c:set var="rowStyle" scope="page" value="odd"/>
                </c:when>
                <c:otherwise>
                  <c:set var="rowStyle" scope="page" value="even"/>
                </c:otherwise>
              </c:choose>
              <tr class="${rowStyle}">
                <td>${crashLog.createdDate}</td>
                <td>${crashLog.appName}</td>
                <td>${crashLog.appVersion}</td>
                <td><a href="${contextPath}/device-id/${crashLog.deviceId}">${crashLog.deviceId}</a></td>
                <td><a href="${contextPath}/crash-log/${crashLog.fileName}">${crashLog.fileName}</a></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
        <p><fmt:message key="noCrashLogs"/></p>
      </c:otherwise>
    </c:choose>
  </template:supplyFragment>
</template:apply>