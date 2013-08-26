<%@ include file="/WEB-INF/jsp/common.jsp" %>
<template:apply path="/WEB-INF/jsp/template.jsp">
  <template:supplyFragment name="title">
    <fmt:message key="oops"/>
  </template:supplyFragment>
  <template:supplyFragment name="content">
    <p><fmt:message key="somethingWentWrong"/></p>
  </template:supplyFragment>
</template:apply>