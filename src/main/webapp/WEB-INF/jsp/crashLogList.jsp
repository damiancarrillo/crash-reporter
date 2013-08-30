<%@ include file="/WEB-INF/jsp/common.jsp" %>
<template:apply path="/WEB-INF/jsp/template.jsp">
  <template:supplyFragment name="title">
    <fmt:message key="crashLogs"/>
  </template:supplyFragment>
  <template:supplyFragment name="content">
    <c:choose>
      <c:when test="${fn:length(crashLogs) gt 0}">
        <span class="text bold">
          <c:choose>
            <c:when test="${empty deviceId}">
              <fmt:message key="allCrashLogs"/>
            </c:when>
            <c:otherwise>
              <a href="../.."><fmt:message key="allCrashLogs"/></a>
              &raquo;
              <fmt:message key="crashLogsForDeviceId">
                <fmt:param value="${deviceId}"/>
              </fmt:message>
            </c:otherwise>
          </c:choose>
        </span>
        <table>
          <thead>
            <tr>
              <th><fmt:message key="submissionDate"/></th>
              <th><fmt:message key="app"/></th>
              <th><fmt:message key="version"/></th>
              <th><fmt:message key="device"/></th>
              <th><fmt:message key="os"/></th>
              <th><fmt:message key="deviceId"/></th>
              <th><fmt:message key="crashLog"/></th>
            </tr>
          </thead>
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
                <td><fmt:formatDate value="${crashLog.createdDate}" pattern="MMMM dd, yyyy 'at' h:mm:ss a"/></td>
                <td><c:out value="${crashLog.appName}" escapeXml="true"/></td>
                <td><c:out value="${crashLog.appVersion}" escapeXml="true"/></td>
                <td><c:out value="${crashLog.deviceModel}" escapeXml="true"/></td>
                <td><c:out value="${crashLog.osVersion}" escapeXml="true"/></td>
                <c:choose>
                  <c:when test="${empty deviceId}">
                    <td>
                      <a href="./crash-logs/device-id/${crashLog.deviceId}"><c:out value="${crashLog.deviceId}"/></a>
                    </td>
                  </c:when>
                  <c:otherwise>
                    <td><c:out value="${crashLog.deviceId}"/></td>
                  </c:otherwise>
                </c:choose>
                <td class="align center">
                  <a href="./crash-log/${crashLog.fileName}" class="button tiny">
                    <span class="icon eye-open"></span>
                    View
                  </a>
                  <a href="./crash-log/download/${crashLog.fileName}" class="button secondary tiny">
                    <span class="icon file-alt"></span>
                    Download
                  </a>
                </td>
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