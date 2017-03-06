<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="com.tarang.ewallet.walletui.util.HelpLinkConstants,com.tarang.ewallet.walletui.controller.AttributeValueConstants"%>
<script type="text/javascript">

	function forCancel() {
		var message = '<spring:message code="cancel.confirm.msg" />';
		var yesAction = function () {
				        	document.getElementById("mode").value = "";
				            window.location.href = '<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/home';
				        };
		confirmationDialog(yesAction, null, null, message);			        
	}
	
	function forNext(){
		submitFormData(document.customerRegFormTwo);
	}
	
</script>
<%@include file="/pages/helptipespopup.jsp" %>
<jsp:include page="/pages/pageerrors.jsp"></jsp:include>
<form:form method="POST" commandName="customerRegFormTwo" autocomplete="off" action="registration" name="customerRegFormTwo">
	<div class="block">
		<table class="form" style="width: 60%">
			<tr>
				<td></td>
				<td><form:errors path="emailId" cssClass="error" /></td>
			</tr>
			<tr>
				<td></td>
				<td><form:errors path="emailNote" cssClass="errorsnote" /></td>
			</tr>
			<tr>
				<td>
					<form:label path="emailId" cssClass="formlebel">
						<spring:message code="emailid.lbl"/><span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td style="padding:0px;">
     				<table><tr>
     					<td style="padding:0px;"><form:input path="emailId" cssClass="forminput" /></td>
     					<td><%=showHelpTipes(HelpLinkConstants.EMAIL_HELP_TIPS, request) %></td>
     				</tr></table>
    			</td>
			</tr>
			<tr>
				<td></td>
				<td><form:errors path="cemailId" cssClass="error" /></td>
			</tr>
			<tr>
				<td>
					<form:label path="cemailId" cssClass="formlebel">
						<spring:message code="confirmmailid.lbl" /><span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td><form:input path="cemailId" cssClass="forminput" /></td>
			</tr>
			<tr>
				<td></td>
				<td><form:errors path="password" cssClass="error" /></td>
			</tr>
			<tr>
				<td>
					<form:label path="password" cssClass="formlebel">
						<spring:message code="password.lbl" /><span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td style="padding:0px;">
					<table><tr>
					<td style="padding:0px;"><form:password path="password" cssClass="forminput" /></td>
					<td><%=showHelpTipes(HelpLinkConstants.PASSWORD_TIPS, request) %></td>
					</tr></table>
				</td>
			</tr>
			<tr>
				<td></td>
				<td><form:errors path="cpassword" cssClass="error" /></td>
			</tr>
			<tr> 
				<td>
					<form:label path="cpassword" cssClass="formlebel">
						<spring:message code="confirmnewpassword.lbl" /><span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td><form:password path="cpassword" cssClass="forminput" /></td>
			</tr>
			<jsp:include page="commonhintquestions.jsp"></jsp:include>			
			<tr>
				<td>
					<form:hidden path="mode"></form:hidden>
					<form:hidden path="ptitle"></form:hidden>
					<form:hidden path="firstName"></form:hidden>
					<form:hidden path="lastName"></form:hidden>
					<form:hidden path="dateOfBirth"></form:hidden>
					<form:hidden path="country"></form:hidden>
					<form:hidden path="addrOne"></form:hidden>
					<form:hidden path="addrTwo"></form:hidden>
					<form:hidden path="city"></form:hidden>
					<form:hidden path="state"></form:hidden>
					<form:hidden path="postalCode"></form:hidden>
					<form:hidden path="phoneCode"></form:hidden>
					<form:hidden path="phoneNo"></form:hidden>
					<form:hidden path="mobileCode"></form:hidden>
					<form:hidden path="mobileNo"></form:hidden>
					<form:hidden path="terms"></form:hidden>
					<form:hidden path="oldEmailId" />
				</td>
			</tr>
			<tr>
				<td colspan=2 align="center">
		           <div class="formbtns">
			          <input type="button" class="styled-button"  value='<spring:message code="next.lbl" />' onclick="forNext()"/>
			          <input type="button" class="styled-button" value='<spring:message code="cancel.lbl" />' onclick="forCancel()"/>
					</div>
				</td>
		    </tr>
		</table>
	</div>
</form:form>