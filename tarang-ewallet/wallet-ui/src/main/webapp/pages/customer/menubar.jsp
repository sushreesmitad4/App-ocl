<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ page import="com.tarang.ewallet.common.util.UserStatusConstants,com.tarang.ewallet.walletui.controller.AttributeValueConstants"%>

<div id="mbtnavbar">
	<ul id="mbtnav">		
		<li class="top">
			<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/customer">
				<spring:message code="my.account.lbl"/>
			</a>
		</li>
		<li class="top">
			<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/manageaccounts">
				<spring:message code="account.management.lbl"/>
			</a>
			<ul class="sub">
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/manageaccounts/addbank">
						<spring:message code="addbank.account.lbl"/>
					</a>
				</li>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/manageaccounts/addcard">
						<spring:message code="addcard.account.lbl"/>
					</a>
				</li>
			</ul>
		</li>
		<li class="top"><a href="javascript:void(0)"><spring:message code="transaction.lbl"/></a>
			<%
			Long userStatus = (Long)session.getAttribute("userStatus");
			if(!UserStatusConstants.APPROVED.equals(userStatus)){
			%>
			<ul class="sub">
				<li>
					<a href="javascript:void(0)" style="color: #8b7d7b"><spring:message code="self.transfer.lbl"/></a>
				</li>
				<li>
					<a href="javascript:void(0)" style="color: #8b7d7b"><spring:message code="add.money.lbl"/></a>
				</li>
				<li>
					<a href="javascript:void(0)" style="color: #8b7d7b"><spring:message code="reload.money.lbl"/></a>
				</li>
				<li>
					<a href="javascript:void(0)" style="color: #8b7d7b"><spring:message code="withdraw.lbl"/></a>
				</li>
				<li>
					<a href="javascript:void(0)" style="color: #8b7d7b"><spring:message code="money.transfer.lbl"/></a>
					<ul class="sub">
						<li><a href="javascript:void(0)" style="color: #8b7d7b"><spring:message code="to.single.lbl"/></a></li>
					</ul>
				</li>
				<li class="top">
					<a href="javascript:void(0)" style="color: #8b7d7b" class="fly"><spring:message code="request.money.lbl"/></a>
				</li>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/scheduler/list">
						Recurring Details<%-- <spring:message code="scheduler.recurring.lbl"/> --%>
					</a>
				</li>
				<%-- <li class="top"><a href="javascript:void(0)" class="menucolor" class="fly"><spring:message code="receive.money.lbl"/></a>
				</li> --%>
			</ul>
			<%}
			else{%>
				<ul class="sub">
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/sendmoney/selftransfer">
						<spring:message code="self.transfer.lbl"/>
					</a>
				</li>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/addmoney/addmoneyrequest">
						<spring:message code="add.money.lbl"/>
					</a>
				</li>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/reloadmoney">
						<spring:message code="reload.money.lbl"/>
					</a>
				</li>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/customer/underconstruction">
						<spring:message code="withdraw.lbl"/>
					</a>
				</li>
				<li>
					<a href="javascript:void(0)"><spring:message code="money.transfer.lbl"/></a>
					<ul class="sub">
						<li>
							<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/sendmoney/tosingle">
								<spring:message code="to.single.lbl"/>
							</a>
						</li>
					</ul>
				</li>
				<li class="top">
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/requestmoney/list" class="fly">
						<spring:message code="request.money.lbl"/>
					</a>
				</li>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/scheduler/list">
						Recurring Details<%-- <spring:message code="scheduler.recurring.lbl"/> --%>
					</a>
				</li>
				<%-- <li class="top"><a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/receivemoney/view" class="fly"><spring:message code="receive.money.lbl"/></a>
				</li> --%>
			</ul>
			<%} %>
		</li>
		<li class="top">
			<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/customerdispute">
				<spring:message code="dispute.lbl"/>
			</a>
		</li>
		<li class="top">
			<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/feedback/query">
				<spring:message code="queryor.feedback.lbl"/>
			</a>
		</li>
		<li class="top"><a href="javascript:void(0)"><spring:message code="profile.lbl"/></a>
			<ul class="sub">
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/customerlogin/changepassword">
						<spring:message code="change.password.lbl"/>
					</a>
				</li>
				<li>
					<a href="javascript:void(0)"><spring:message code="profile.mgmt.lbl"/></a>
					<ul class="sub">
						<li>
							<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/customer/updateprofile">
								<spring:message code="update.profile.lbl"/>
							</a>
						</li>
					</ul>
				</li>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/accountcloser/addaccountclose">
						<spring:message code="closing.account.lbl"/>
					</a>
				</li>
				<%-- <li><a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/customer/underconstruction"><spring:message code="merchantwise.threshold.lbl"/></a></li> --%>
				<li>
					<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/preferences">
						<spring:message code="preferences.lbl"/>
					</a>
				</li>
			</ul>
		</li>
		<li class="top">
			<a href="<%=request.getContextPath()%>/<%=AttributeValueConstants.URL_PATH_PREFIX%>/reports/cm">
				<spring:message code="reports.lbl"/>
			</a>
		</li>
	</ul>
</div>