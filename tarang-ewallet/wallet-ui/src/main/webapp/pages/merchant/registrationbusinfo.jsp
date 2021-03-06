<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.tarang.ewallet.walletui.util.HelpLinkConstants,com.tarang.ewallet.util.GlobalLitterals,com.tarang.ewallet.walletui.controller.AttributeValueConstants"%>
<%@include file="/pages/helptipespopup.jsp" %>

<script type="text/javascript">
	$(window).load(function () { 
		enableMerchantCode();
		if(document.forms[0].homeAddress.checked == true) {
			$("#countryBOi_").hide();
			$("#stateOrRegionBOi_").hide();
			$("#address1BOi_").hide();
			$("#address2BOi_").hide();
			$("#cityOrTownBOi_").hide();
			$("#postalCodeBOi_").hide(); 
			$("#countryBOi").hide();
			$("#stateOrRegionBOi").hide();
			$("#address1BOi").hide();
			$("#address2BOi").hide();
			$("#cityOrTownBOi").hide();
			$("#postalCodeBOi").hide(); 
			document.getElementById("countryBO").value = document.getElementById("countryBI").value; 
			getMasterData('stateOrRegionBO', 'regions', document.getElementById("countryBO").value);
			setTimeout(function() { document.getElementById("stateOrRegionBO").value = document.getElementById("stateorRegionBI").value;}, 500);
		}
		 else{
			$("#countryBOi_").show();
			$("#stateOrRegionBOi_").show();
			$("#address1BOi_").show();
			$("#address2BOi_").show();
			$("#cityOrTownBOi_").show();
			$("#postalCodeBOi_").show();
			$("#countryBOi").show();
			$("#stateOrRegionBOi").show();
			$("#address1BOi").show();
			$("#address2BOi").show();
			$("#cityOrTownBOi").show();
			$("#postalCodeBOi").show();
		} 
		   
	});
	
	function enableMerchantCode() {
		if(document.getElementById("cCheck").checked){
			$("#merchantCode").show();
			$("#merchantCode_").show();
			$("#successUrl").show();
			$("#successUrl_").show();
			$("#failureUrl").show();
			$("#failureUrl_").show();
		}
		else{
			$("#merchantCode").hide();
			$("#merchantCode_").hide();
			$("#successUrl").hide();
			$("#successUrl_").hide();
			$("#failureUrl").hide();
			$("#failureUrl_").hide();
		}
	}
	
	function hideBOAddress() {
		if(document.forms[0].homeAddress.checked == false) {
			$("#countryBOi_").show();
			$("#stateOrRegionBOi_").show();
			$("#address1BOi_").show();
			$("#address2BOi_").show();
			$("#cityOrTownBOi_").show();
			$("#postalCodeBOi_").show();
			$("#countryBOi").show();
			$("#stateOrRegionBOi").show();
			$("#address1BOi").show();
			$("#address2BOi").show();
			$("#cityOrTownBOi").show();
			$("#postalCodeBOi").show();
			
			document.getElementById("countryBO").value = document.getElementById("countryBI").value;
			//getStates2(document.getElementById("countryBO").value);
			document.getElementById("address1BO").value = document.getElementById("address1BI").value;
			document.getElementById("address2BO").value = document.getElementById("address2BI").value;
			document.getElementById("cityOrTownBO").value = document.getElementById("cityOrTownBI").value;
			document.getElementById("postalCodeBO").value = document.getElementById("postalCodeBI").value;
			getMasterData('stateOrRegionBO', 'regions', document.getElementById("countryBO").value);
			setTimeout(function() { document.getElementById("stateOrRegionBO").value = document.getElementById("stateorRegionBI").value;}, 90);
		
		}
		else {
			$("#countryBOi_").hide();
			$("#stateOrRegionBOi_").hide();
			$("#address1BOi_").hide();
			$("#address2BOi_").hide();
			$("#cityOrTownBOi_").hide();
			$("#postalCodeBOi_").hide(); 
			$("#countryBOi").hide();
			$("#stateOrRegionBOi").hide();
			$("#address1BOi").hide();
			$("#address2BOi").hide();
			$("#cityOrTownBOi").hide();
			$("#postalCodeBOi").hide(); 
			/* assigning values */
			document.getElementById("countryBO").value = document.getElementById("countryBI").value;
			//getStates2(document.getElementById("countryBO").value);
			document.getElementById("address1BO").value = document.getElementById("address1BI").value;
			document.getElementById("address2BO").value = document.getElementById("address2BI").value;
			document.getElementById("cityOrTownBO").value = document.getElementById("cityOrTownBI").value;
			document.getElementById("postalCodeBO").value = document.getElementById("postalCodeBI").value;
			getMasterData('stateOrRegionBO', 'regions', document.getElementById("countryBO").value);
			setTimeout(function() { document.getElementById("stateOrRegionBO").value = document.getElementById("stateorRegionBI").value;}, 90);
		}
	}
	
	  function getBITextInBOI(){
		if(document.forms[0].homeAddress.checked == true) {
			document.getElementById("address1BO").value = document.getElementById("address1BI").value;
			document.getElementById("address2BO").value = document.getElementById("address2BI").value;
			document.getElementById("cityOrTownBO").value = document.getElementById("cityOrTownBI").value;
			document.getElementById("postalCodeBO").value = document.getElementById("postalCodeBI").value;
		} 
	} 
	
	function forRegister() {
		submitFormData(document.merchantregistrationform);
	}
	
	function forBack(){
		document.getElementById("mode").value = "back";
		document.merchantregistrationform.submit();
		ajaxLoader($("body"));
	}
	
	function forCancel() {
		var message = '<spring:message code="cancel.confirm.msg"/>';
		var yesAction = function () {
          	 					window.location.href ='<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/home';
           				};
        confirmationDialog(yesAction, null, null, message);	
	} 
	
	function loadProducts(){
		popupWindow = window.open('<%=request.getContextPath()%>/pages/terms.jsp', 'TermsAndCondition', 'scrollbars=yes, width=550, height=500');
	}
		
</script>
<jsp:include page="/pages/pageerrors.jsp"></jsp:include>
<form:form name="merchantregistrationform" method="POST" commandName="merchantForm" action="registrationsave">
	<div class="pagelayout">
		<div class="block">
			<div>
				<h3><spring:message code="business.information.lbl" /></h3>
			</div>
			<table class="form" style="width:55%">
				<tr class="formtr">
					<td class="formtd" style="width:36%"></td>
					<td class="formtd"></td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="ownerType" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="ownerType" cssClass="formlebel">
							<spring:message code="ownertype.lbl" />
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd">
						<form:select path="ownerType" cssClass="formlist">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${ownerTypes}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="businessLegalname" cssClass="error" /></td>
					</tr>
				<tr>
					<td class="formtd">
						<form:label path="businessLegalname" cssClass="formlebel">
							<spring:message code="legalname.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd"><form:input path="businessLegalname" cssClass="forminput"></form:input> </td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="countryBI" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="countryBI" cssClass="formlebel">
							<spring:message code="country.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd">
						<form:select path="countryBI" cssClass="formlist" onchange="getMasterData('stateorRegionBI', 'regions', this.value);">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${countryList}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="address1BI" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="address1BI"  cssClass="formlebel">
							<spring:message code="address1.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd"><form:textarea path="address1BI"  cssClass="forminput"></form:textarea></td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="address2BI" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="address2BI" cssClass="formlebel">
							<spring:message code="address2.lbl"/>
						</form:label>
					</td>
					<td class="formtd"><form:textarea path="address2BI"  cssClass="forminput"></form:textarea> </td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="cityOrTownBI" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="cityOrTownBI" cssClass="formlebel">
							<spring:message code="city.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd"><form:input path="cityOrTownBI"  cssClass="forminput"></form:input> </td>
				</tr>
	            <tr>
					<td></td>
					<td><form:errors path="stateorRegionBI" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="stateorRegionBI" cssClass="formlebel">
							<spring:message code="stateorregion.lbl"/>
						</form:label>
					</td>
					<td class="formtd">
						<form:select path="stateorRegionBI"  cssClass="formlist">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${stateList}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="postalCodeBI" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="postalCodeBI" cssClass="formlebel">
							<spring:message code="zipcode.lbl"/>
						</form:label>
					</td>
					<td class="formtd"><form:input path="postalCodeBI"  cssClass="forminput"></form:input> </td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="phoneCountryCode1" cssClass="error" />
				</td>
				<tr>
					<td></td>
					<td><form:errors path="phoneNumber" cssClass="error" />
				</td>
				</tr>
				<tr>
					<td class="formtd">
					    <form:label path="phoneNumber" cssClass="formlebel">
							<spring:message code="phone.number.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd">
						<%-- <form:select path="phoneCountryCode1" cssClass="formlist"  style="width: 100px">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>"><spring:message code="please.select.lbl" /></form:option>
								<c:forEach items="${phoneCodes}" var="entry">
								<form:option value="${entry.key}" title="${entry.value}" label="${entry.value}" />
							</c:forEach>
						</form:select> --%>
					   <form:input path="phoneCountryCode1" class="phonecodeinput" ></form:input> -
					   <form:input path="phoneNumber" class="phoneinput" ></form:input> 
					</td>
				    <td>
				    </td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="businessCategory" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="businessCategory" cssClass="formlebel">
							<spring:message code="category.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd">
						<form:select path="businessCategory"  cssClass="formlist" onchange="getMasterData('subCategory', 'subcategories', this.value);">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${businessCategories}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="subCategory" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="subCategory" cssClass="formlebel">
							<spring:message code="subcategory.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label></td>
					<td class="formtd">
						<form:select path="subCategory" cssClass="formlist">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${subcategories}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="businessEstablishedMonth" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="businessEstablishedMonth" cssClass="formlebel">
							<spring:message code="establishedmonth.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label></td>
						<td class="formtd"><form:select path="businessEstablishedMonth" cssClass="formlist">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${establishMonth}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="businessEstablishedYear" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="businessEstablishedYear" cssClass="formlebel">
							<spring:message code="establishedyear.lbl"/>
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
						<td class="formtd"><form:select path="businessEstablishedYear"  cssClass="formlist">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${establishYear}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="website" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="website" cssClass="formlebel">
							<spring:message code="website.lbl"/>
						</form:label>
					</td>
					<td class="formtd"><form:input path="website" cssClass="forminput"></form:input> </td>
				</tr>
	
				<tr>
					<td></td>
					<td><form:errors path="currency" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="currency" cssClass="formlebel">
							<spring:message code="currency.lbl" />
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd">
						<form:select path="currency"  cssClass="formlist">
							<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
							<c:forEach items="${currencyList}" var="std">
								<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
							</c:forEach>
						</form:select> 
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="averageTransactionAmount" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="averageTransactionAmount" cssClass="formlebel">
							<spring:message code="avgtransamount.lbl" /><span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td style="padding:0px;">
						<table>
							<tr>
								<td style="padding:0px;">
									<form:input path="averageTransactionAmount" cssClass="forminput"></form:input>
								</td>
								<td>
					    			<!-- average transation amount popup related code -->
					    			<%=showHelpTipes(HelpLinkConstants.AVERAGE_TRANSACTION_AMOUNT_TIPS, request) %>
								</td>
							</tr>
						</table>
					 </td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="highestMonthlyVolume" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="highestMonthlyVolume" cssClass="formlebel">
							<spring:message code="higmonthvolume.lbl" />
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td style="padding:0px;">
						<table>
							<tr>
								<td style="padding:0px;">
									<form:input path="highestMonthlyVolume" cssClass="forminput"></form:input>
								</td>
								<td>
									<%--highest monthly volume popup related code --%>
									<%=showHelpTipes(HelpLinkConstants.HIGHEST_MONTHLY_VOLUME_TIPS, request) %>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td></td>
					<td><form:errors path="percentageOfAnnualRevenueFromOnlineSales" cssClass="error" /></td>
				</tr>
				<tr>
					<td class="formtd">
						<form:label path="percentageOfAnnualRevenueFromOnlineSales" cssClass="formlebel">
							<spring:message code="percentage.of.annual.revenue.lbl" />
							<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td style="padding:0px;">
						<table>
							<tr>
								<td style="padding:0px;">
									<form:select path="percentageOfAnnualRevenueFromOnlineSales" cssClass="formlist">
										<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
											<spring:message code="please.select.lbl" />
										</form:option>
										<c:forEach items="${merchantPercenatgeAnualRevenues}" var="std">
											<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
										</c:forEach>
									</form:select>
								</td>
								<td>
								<%-- annual revenue pop up related code --%>
								<%=showHelpTipes(HelpLinkConstants.PERCENTAGE_OF_ANNUAL_REVENUE_FROM_ONLINE_SALES_TIPS, request) %>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
				    <td class="formtd" style="width: 36%">
					<form:label path="codeCheck" cssClass="formlebel"> 
			      		<spring:message code="is.online.payment.lbl" />
					</form:label>
					</td>
					<td class="formtd"><form:checkbox path="codeCheck" id="cCheck" onclick="enableMerchantCode();"/></td>
				</tr> 
				 <tr id = 'merchantCode_'>
					<td></td>
					<td><form:errors path="merchantCode" cssClass="error" /></td>
				</tr>
				<tr id = 'merchantCode'>
					<td class="formtd">
						<form:label path="merchantCode" cssClass="formlebel"><spring:message code="payment.code.lbl" />
						<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td class="formtd"><form:input path="merchantCode" cssClass="forminput"></form:input> </td>
				</tr> 
				
				 <tr id = 'successUrl_'>
					<td></td>
					<td><form:errors path="successUrl" cssClass="error" /></td>
				</tr>
				<tr id = 'successUrl'>
					<td>
						<form:label path="successUrl"  cssClass="formlebel"><spring:message code="payment.successurl.lbl" />
						<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td><form:input path="successUrl"  cssClass="forminput"></form:input> </td>
				</tr> 

				 <tr id = 'failureUrl_'>
					<td></td>
					<td><form:errors path="failureUrl" cssClass="error" /></td>
				</tr>
				<tr id = 'failureUrl'>
					<td>
						<form:label path="failureUrl"  cssClass="formlebel"><spring:message code="payment.failureurl.lbl" />
						<span class="mfield">&nbsp;*</span>
						</form:label>
					</td>
					<td><form:input path="failureUrl"  cssClass="forminput"></form:input> </td>
				</tr> 
				
			</table>
		</div>
	</div>
	<div class="pagelayout">
		<div><h3><spring:message code="business.owner.information.lbl" /></h3></div>
		<table class="form" style="width:55%">
			<tr>
				<td></td>
				<td> <form:errors path="firstName" cssClass="error" /></td>
			</tr>
			<tr>
				<td class="formtd">
					<form:label path="firstName" cssClass="formlebel">
						<spring:message code="firstname.lbl" />
						<span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td class="formtd"><form:input path="firstName" cssClass="forminput"></form:input></td>
			</tr>
			<tr>
				<td></td>
				<td> <form:errors path="lastName" cssClass="error" /></td>
			</tr>
			<tr>
				<td class="formtd">
					<form:label path="lastName" cssClass="formlebel">
						<spring:message code="lastname.lbl" />
						<span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td class="formtd"><form:input path="lastName" cssClass="forminput"></form:input></td>
			</tr>
			<tr>
				<td></td>
				<td class="formtd"><form:errors path="homeAddress" cssClass="error" /></td>
			</tr>
			<tr>
				<td class="formtd" style="width: 36%">
					<form:label path="homeAddress" cssClass="formlebel"> 
			      		<spring:message code="home.address.sameas.business.address.lbl" />
						<span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td class="formtd"><form:checkbox path="homeAddress" onclick="hideBOAddress()"/></td>
	        </tr> 
			<tr id="countryBOi_">
				<td></td>
				<td><form:errors path="countryBO" cssClass="error" /></td>
			</tr>
			<tr id="countryBOi">
				<td class="formtd">
					<form:label path="countryBO" cssClass="formlebel">
						<spring:message code="country.lbl"/>
						<span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td class="formtd"><form:select path="countryBO" cssClass="formlist" onchange="getMasterData('stateOrRegionBO', 'regions', this.value);">
					<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
						<spring:message code="please.select.lbl" />
					</form:option>
					<c:forEach items="${countryList}" var="std">
						<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
					</c:forEach>
					</form:select> 
				</td>
			</tr>
			<tr id="address1BOi_">
				<td></td>
				<td> <form:errors path="address1BO" cssClass="error" /></td>
			</tr>
			<tr id="address1BOi">
				<td class="formtd">
					<form:label path="address1BO" cssClass="formlebel">
						<spring:message code="address1.lbl"/>
						<span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td class="formtd"><form:textarea path="address1BO" cssClass="forminput"></form:textarea></td>
			</tr>
			<tr id="address2BOi_">
				<td></td>
				<td><form:errors path="address2BO" cssClass="error" /></td>
			</tr>
			<tr id="address2BOi">
				<td class="formtd">
					<form:label path="address2BO" cssClass="formlebel">
						<spring:message code="address2.lbl"/>
					</form:label>
				</td>
				<td class="formtd"><form:textarea path="address2BO" cssClass="forminput"></form:textarea> </td>
			</tr>
			<tr id="cityOrTownBOi_">
				<td></td>
				<td> <form:errors path="cityOrTownBO" cssClass="error" /></td>
			</tr>
			<tr id="cityOrTownBOi">
				<td class="formtd">
					<form:label path="cityOrTownBO" cssClass="formlebel">
						<spring:message code="city.lbl"/>
						<span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td class="formtd"><form:input path="cityOrTownBO" cssClass="forminput"></form:input></td>
			</tr>
			<tr id="stateOrRegionBOi_">
				<td></td>
				<td><form:errors path="stateOrRegionBO" cssClass="error" /></td>
			</tr>
			<tr id="stateOrRegionBOi">
				<td class="formtd">
					<form:label path="stateOrRegionBO" cssClass="formlebel">
						<spring:message code="stateorregion.lbl"/>
					</form:label>
				</td>
				<td class="formtd">
					<form:select path="stateOrRegionBO"  cssClass="formlist">
						<form:option value="<%=GlobalLitterals.ZERO_LONG%>">
								<spring:message code="please.select.lbl" />
							</form:option>
						<c:forEach items="${stateList2}" var="std">
							<form:option value="${std.key}" title="${std.value}" label="${std.value}" />
						</c:forEach>
					</form:select> 
				</td>
			</tr>
			<tr id="postalCodeBOi_">
				<td></td>
				<td><form:errors path="postalCodeBO" cssClass="error" /></td>
			</tr>
			<tr id="postalCodeBOi">
				<td class="formtd">
					<form:label path="postalCodeBO" cssClass="formlebel">
						<spring:message code="zipcode.lbl"/>
					</form:label>
				</td>
				<td class="formtd"><form:input path="postalCodeBO" cssClass="forminput"></form:input> </td>
			</tr>
		</table>
	</div>
	<div class="pagelayout">
		<div><h3><spring:message code="customer.service.information.lbl" /></h3></div>
		<table class="form" style="width:55%">
			<tr>
				<td></td>
				<td><form:errors path="emailCSI" cssClass="error" /></td>
			</tr>
			<tr>
				<td class="formtd" style="width:36%">
					<form:label path="emailCSI" cssClass="formlebel">
						<spring:message code="emailid.lbl"/>
						<span class="mfield">&nbsp;*</span>
					</form:label>
				</td>
				<td class="formtd"><form:input path="emailCSI" cssClass="forminput"></form:input> 
			</tr>
			<tr>
		   		<td></td>
				<td><form:errors path="code" cssClass="error"/></td>
			</tr>
			<tr>
			   <td></td>
				<td><form:errors path="phone" cssClass="error" /></td>
			</tr>
			<tr>
				<td class="formtd">
				   <form:label path="phone" cssClass="formlebel">
						<spring:message code="phone.number.lbl"/>
					</form:label>
				</td>
				<td style="padding:0px;">
					<table><tr>
					<td style="padding:0px;">
							<%-- <form:select path="code" cssClass="formlist"  style="width: 100px">
								<form:option value="0"><spring:message code="please.select.lbl" /></form:option>
									<c:forEach items="${phoneCodes}" var="entry">
									<form:option value="${entry.key}" title="${entry.value}" label="${entry.value}" />
								</c:forEach>
							</form:select> --%>
						<form:input path="code" class="phonecodeinput" ></form:input> -
						<form:input path="phone" class="phoneinput" ></form:input>
					</td>
					<td><%=showHelpTipes(HelpLinkConstants.SERVICE_PHONE_NO_TIPS, request) %></td>
					</tr></table>
				</td>
			</tr>
			<tr>
				<td></td>
				<td class="formerror">
					<form:errors path="terms" cssClass="error" />
				</td>
			</tr>
			<tr>
				<td />
				<td>
					<spring:message code="yes.lbl" />
					<form:checkbox path="terms" />
					<spring:message code="agree.lbl" />
					<a href="javascript:void(o)" style="text-decoration: none;" onclick="loadProducts()">&nbsp;<spring:message code="termsandconditions.lbl" /></a>
				</td>
				<td class="formtd">
				<form:hidden path="mode" />
				<form:hidden path="emailId" /> 
				<form:hidden path="confirmEmailId" /> 
				<form:hidden path="password" /> 
				<form:hidden path="confirmPassword" />
				<form:hidden path="question1" /> 
				<form:hidden path="hint1" />
				<form:hidden path="existPersonPhoneNo" /> 
				<form:hidden path="existServicePhoneNo" />
				<form:hidden path="oldEmailId" />  
				</td>
			</tr>
			
			<tr>
				<td colspan=2 align="center">
					<div class="formbtns">
						<input type="button" class="styled-button" value='<spring:message code="register.lbl" />' onclick="forRegister();" />
						<input type="button" class="styled-button" value='<spring:message code="back.lbl" />' onclick="forBack();" />
						<input type="button" class="styled-button" value='<spring:message code="cancel.lbl" />' onclick="forCancel();" />
					</div>
				</td>
			</tr>
		</table>
	</div>
</form:form>