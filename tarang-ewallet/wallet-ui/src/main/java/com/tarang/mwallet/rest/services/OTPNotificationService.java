package com.tarang.mwallet.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.tarang.ewallet.audittrail.business.AuditTrailService;
import com.tarang.ewallet.audittrail.util.AuditTrailConstrain;
import com.tarang.ewallet.common.business.CommonService;
import com.tarang.ewallet.common.util.CommonConstrain;
import com.tarang.ewallet.customer.service.CustomerService;
import com.tarang.ewallet.dto.CustomerDto;
import com.tarang.ewallet.exception.WalletException;
import com.tarang.ewallet.model.Authentication;
import com.tarang.ewallet.sms.dto.OtpDto;
import com.tarang.ewallet.sms.service.SmsService;
import com.tarang.ewallet.util.GlobalLitterals;
import com.tarang.ewallet.walletui.controller.AttributeConstants;
import com.tarang.ewallet.walletui.controller.AttributeValueConstants;
import com.tarang.ewallet.walletui.controller.constants.Login;
import com.tarang.ewallet.walletui.validator.common.Common;
import com.tarang.mwallet.rest.services.model.RestRequest;
import com.tarang.mwallet.rest.services.util.CommonRestServiceHelper;
import com.tarang.mwallet.rest.services.util.CommonWebserviceUtil;
import com.tarang.mwallet.rest.services.util.Constants;
import com.tarang.mwallet.rest.services.util.ServerProcessorStatus;
import com.tarang.mwallet.rest.services.util.ServerUtility;

/**
 * @author kedarnathd
 * This service will publish as rest full access for OTP functionality of the wallet system.
 */

@Path("/otpservice")
public class OTPNotificationService implements Login, AttributeConstants, AttributeValueConstants, Constants, Common {
	
private static final Logger LOGGER = Logger.getLogger(OTPNotificationService.class);
	
	private CommonService commonService;
	
	private CustomerService customerService;
	
	//private MerchantService merchantService;
	
	private AuditTrailService auditTrailService;
	
	private SmsService smsService;
	
	@Path("/sendOTP")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendOTP(@Context HttpServletRequest request, String otpInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" Entering sendOTP.... ");
		
		Response responce = null;
		RestRequest restRequest = null;
		CustomerDto customerDto = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(otpInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(otpInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		
		LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE +" :" + restRequest.getUserType());
		try {
			customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			if(null == customerDto){
				throw new WalletException(CUSTOMER_MOBILE_NOT_EXIST);
			}
			authentication = commonService.getAuthentication(customerDto.getEmailId(), GlobalLitterals.CUSTOMER_USER_TYPE);
			if(null == authentication){
				throw new WalletException(CUSTOMER_MOBILE_NOT_EXIST);
			}
			/*Skip phone number check, so passed value as null*/
			responce = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					null, authentication, Boolean.FALSE);
			if(responce != null){
				return responce;
			}
			if(null == authentication.getSimNumber() && null == authentication.getImeiNumber()){
				throw new WalletException(CommonConstrain.USER_NOT_REGISTER_AS_MOBILE_WALLET);   
			}
				
				LOGGER.info("Email :" + authentication.getEmailId() + "UserType : " + authentication.getUserType());
			  	
				OtpDto otpDto = new OtpDto(); 
				
				otpDto.setMobileCode(restRequest.getPhoneCode());
				otpDto.setMobileNumber(restRequest.getPhoneNo());
				otpDto.setCustomerId(customerDto.getId());
				otpDto.setEmailId(restRequest.getEmail());
				otpDto.setOtpModuleName(restRequest.getOtpModuleName());
				otpDto.setMessage(restRequest.getMessage());
							
				OtpDto otp = new OtpDto();
				otp = smsService.sendOTP(otpDto);
				
				auditTrailService.createAuditTrail(otpDto.getCustomerId(), AuditTrailConstrain.MODULE_OTP_NOTIFICATION_CREATE, 
						AuditTrailConstrain.STATUS_CREATE, authentication.getEmailId(), authentication.getUserType());			
				
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.SUCCESSFULLY_OTP_SENT_TO_DEVICE.getValue(), otp);
			
		} catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	
	@Path("/sendregistrationOTP")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendregistrationOTP(@Context HttpServletRequest request, String otpInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" Entering sendregistrationOTP.... ");
		RestRequest restRequest = null;
		if(CommonWebserviceUtil.isEmpty(otpInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(otpInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE +" :" + restRequest.getUserType());
		try {
			OtpDto otpDto = new OtpDto(); 
			
			otpDto.setMobileCode(restRequest.getPhoneCode());
			otpDto.setMobileNumber(restRequest.getPhoneNo());
			otpDto.setCustomerId(null);
			otpDto.setEmailId(restRequest.getEmail());
			otpDto.setOtpModuleName(restRequest.getOtpModuleName());
			otpDto.setMessage(restRequest.getMessage());
						
			OtpDto otp = new OtpDto();
			otp = smsService.sendOTP(otpDto);
			return ServerUtility.papulateSuccessCode(request,
					ServerProcessorStatus.SUCCESSFULLY_OTP_SENT_TO_DEVICE.getValue(), otp);
			
		} catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	/**
	 * Get the service instance from context
	 * @param request
	 */
	private void papulateServices(HttpServletRequest request){
		HttpSession oldsession = request.getSession();
		if (oldsession != null) {
			oldsession.invalidate();
		}
		HttpSession session = request.getSession(true);
		commonService = (CommonService) ServerUtility.getServiceInstance(session, COMMON_SERVICE);
		smsService = (SmsService) ServerUtility.getServiceInstance(session, SMS_SERVICE);
		customerService = (CustomerService) ServerUtility.getServiceInstance(session, CUSTOMER_SERVICE);
		//merchantService = (MerchantService) ServerUtility.getServiceInstance(session, MERCHANT_SERVICE);
		auditTrailService = (AuditTrailService) ServerUtility.getServiceInstance(session, AUDIT_TRAIL_SERVICE);
	}
}
