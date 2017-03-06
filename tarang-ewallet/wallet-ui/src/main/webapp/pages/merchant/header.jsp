<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ page import="com.tarang.ewallet.walletui.controller.AttributeValueConstants"%>

<script type="text/javascript">
	$(window).load(function () {   
		DateTime();
	});
</script>

<ul class="inline-ul floatright">
	<li><img src="<%=request.getContextPath()%>/img/img-profile.jpg" alt="Profile Pic" /> </li>
	<li class="tophead"><spring:message code="hello.lbl"/>&nbsp;&nbsp;<%=session.getAttribute("name")%>&nbsp;|&nbsp;</li>
	<li class="tophead"><spring:message code="last.login.lbl"/>&nbsp;<span id="GMTTimeDate"></span>&nbsp;|&nbsp;</li>
	<li>
		<a class="merchantheader" href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/merchantlogin/logout?selfinvoke=true">
			<spring:message code="logout.lbl"/>
		</a>
		&nbsp;&nbsp;
	</li> 
</ul>
