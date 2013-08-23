<%@ include file="/WEB-INF/jsp/common.jsp" %>
<template:apply path="/WEB-INF/template.jsp">
  <template:supplyFragment name="title">
    title
  </template:supplyFragment>
  <template:supplyFragment name="sidebarHeader">
    <div id="header">
      <h1>SIDEBAR HEADER</h1>
    </div>
  </template:supplyFragment>
  <template:supplyFragment name="content">
    <p>YO!</p>
  </template:supplyFragment>
</template:apply>