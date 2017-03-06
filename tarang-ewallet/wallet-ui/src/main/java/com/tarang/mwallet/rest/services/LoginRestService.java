/**
 * 
 */
package com.tarang.mwallet.rest.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.tarang.ewallet.common.business.LoginService;
import com.tarang.ewallet.common.util.CommonConstrain;
import com.tarang.ewallet.common.util.CommonUtil;
import com.tarang.ewallet.common.util.TypeOfRequest;
import com.tarang.ewallet.crypt.business.CryptService;
import com.tarang.ewallet.customer.service.CustomerService;
import com.tarang.ewallet.dto.CustomerDto;
import com.tarang.ewallet.dto.PreferencesDto;
import com.tarang.ewallet.exception.WalletException;
import com.tarang.ewallet.merchant.business.MerchantService;
import com.tarang.ewallet.model.Authentication;
import com.tarang.ewallet.model.PhoneNumber;
import com.tarang.ewallet.scheduler.business.SchedulerService;
import com.tarang.ewallet.scheduler.util.JobConstants;
import com.tarang.ewallet.scheduler.util.SchedulerGroupNames;
import com.tarang.ewallet.sms.dto.OtpDto;
import com.tarang.ewallet.sms.service.SmsService;
import com.tarang.ewallet.util.DateConvertion;
import com.tarang.ewallet.util.GlobalLitterals;
import com.tarang.ewallet.util.service.UtilService;
import com.tarang.ewallet.walletui.controller.AttributeConstants;
import com.tarang.ewallet.walletui.controller.AttributeValueConstants;
import com.tarang.ewallet.walletui.controller.constants.Login;
import com.tarang.ewallet.walletui.util.MasterDataUtil;
import com.tarang.ewallet.walletui.util.UIUtil;
import com.tarang.ewallet.walletui.validator.common.Common;
import com.tarang.ewallet.webservice.validation.RestCustomValidation;
import com.tarang.mwallet.rest.services.model.RestRequest;
import com.tarang.mwallet.rest.services.util.CommonRestServiceHelper;
import com.tarang.mwallet.rest.services.util.CommonWebserviceUtil;
import com.tarang.mwallet.rest.services.util.Constants;
import com.tarang.mwallet.rest.services.util.ServerProcessorStatus;
import com.tarang.mwallet.rest.services.util.ServerUtility;

/**
 * @author kedarnathd
 * This service will publish as rest full access for login and 
 * logout functionality of the wallet system.
 */

@Path("/devicehome")
public class LoginRestService implements Login, AttributeConstants, AttributeValueConstants, Constants, Common{
	
	private static final Logger LOGGER = Logger.getLogger(LoginRestService.class);
	
	private LoginService loginService;
	
	private CommonService commonService;
	
	private CustomerService customerService;
	
	private MerchantService merchantService;
	
	private SchedulerService schedulerService;
	
	private CryptService cryptService;
	
	private AuditTrailService auditTrailService;
	
	private SmsService smsService;
	
	private UtilService utilService;
	
	//private SendMoneyService sendMoneyService;
	
	@Path("/activate")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response activateDevice(@Context HttpServletRequest request, String loginInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" Entering activateDevice ");
		
		Response responce = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(loginInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(loginInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		responce = mandatoryFieldsCheck(request, restRequest);
		if(responce != null){
			return responce;
		}
		LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE +" :" + restRequest.getUserType());
		try {
			authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			/* From mobile user state and registration validation is taken care at service level. */
			/* so passed as false to skip login validation */ 
			responce = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.FALSE);
			if(responce != null){
				return responce;
			}else{
				/*Deactivated user requested for activate mobile wallet*/
			  	if(checkDeviceActiveStatus(authentication, restRequest)){
			  		if(authentication.getIsMwalletActive() != null 
			  				&& !authentication.getIsMwalletActive()){
			  			if(!authentication.getPassword().equals(cryptService.encryptData(restRequest.getPassword()))){
			  				throw new WalletException(CommonConstrain.PASSWORD_MATCH_FAIL);
			  			}
			  			return ServerUtility.papulateSuccessCode(request,
							ServerProcessorStatus.MOBILEWALLET_ACCOUNT_EXISTS_GENERATE_MPIN.getValue(), null);
			  		}else{
			  			return ServerUtility.papulateErrorCode(request, 
								ServerProcessorStatus.ALREADY_REGISTOR_AS_MOBILE_WALLET.getValue());
			  		}
			  	}
				Boolean flag = commonService.mobileRegistration(restRequest.getEmail(), restRequest.getUserType(), 
						restRequest.getPassword(), restRequest.getMsisdnNumber(), 
						restRequest.getSimNumber(), restRequest.getImeiNumber());
				if(!flag){
					return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.UNABLE_TO_REGISTERED_YOUR_MOBILE_WALLET.getValue());
				}
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.SUCCESSFULLY_REGISTERED_WITH_DEVICE.getValue(), null);
			}
		} catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	
	@Path("/customervalidate")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response Customervalidate(@Context HttpServletRequest request, String loginInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" Entering customerRegistration ");
		
		//Response responce = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(loginInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			LOGGER.info("Login Input :: "+loginInput);
			Gson gson = new Gson();
			restRequest = gson.fromJson(loginInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE +" :" + restRequest.getUserType());
		String response = RestCustomValidation.checkFieldInput(restRequest, CUSTOMER_REGISTRATION_SERVICE);
		if(response != null){
			return ServerUtility.papulateErrorCode(request, response);
		}
		
		if(RestCustomValidation.numberValidator(restRequest.getmPin(), null , Common.M_PIN_MAX_LENGHT)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.INVALID_MPIN_FORMAT.getValue());
		}
		try{
			/*Validate Mobile Number*/
			authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			Boolean isPhoneExist = commonService.phoneNOExist(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			if(isPhoneExist && authentication != null){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.CUSTOMER_MOBILE_AND_EMAIL_EXIST.getValue());
			}else if(isPhoneExist){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.CUSTOMER_MOBILE_EXIST.getValue());
			}else if(authentication != null){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.CUSTOMER_EMAIL_EXIST.getValue());
			}
			/*Send OTP now*/
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
	
	@Path("/customerregistration")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response customerRegistration(@Context HttpServletRequest request, String loginInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" Entering customerRegistration ");
		
		//Response responce = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(loginInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			LOGGER.info("Login Input :: "+loginInput);
			Gson gson = new Gson();
			restRequest = gson.fromJson(loginInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		/*responce = mandatoryFieldsCheck(request, restRequest);
		if(responce != null){
			return responce;
		}*/
		LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE +" :" + restRequest.getUserType());
		String response = RestCustomValidation.checkFieldInput(restRequest, CUSTOMER_REGISTRATION_SERVICE);
		if(response != null){
			return ServerUtility.papulateErrorCode(request, response);
		}
		
		if(RestCustomValidation.numberValidator(restRequest.getmPin(), null , Common.M_PIN_MAX_LENGHT)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.INVALID_MPIN_FORMAT.getValue());
		}
		try{
			/*Validate Mobile Number*/
			authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			Boolean isPhoneExist = commonService.phoneNOExist(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			if(isPhoneExist && authentication != null){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.CUSTOMER_MOBILE_AND_EMAIL_EXIST.getValue());
			}else if(isPhoneExist){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.CUSTOMER_MOBILE_EXIST.getValue());
			}else if(authentication != null){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.CUSTOMER_EMAIL_EXIST.getValue());
			}
			/*Validate OTP*/
			if(RestCustomValidation.numberValidator(restRequest.getOTP(), null , utilService.getOtpLength())){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.INVALID_OTP_FORMAT.getValue());
			}
			
			Boolean isValidOtp = smsService.authenticateOTP(CommonRestServiceHelper.getOtpDto(restRequest, null));
			if(isValidOtp){
				CustomerDto customerDto = new CustomerDto();
				customerDto.setIpAddress("172.30.66.144");
				customerDto.setEmailId(restRequest.getEmail());
				customerDto.setPassword("aaaa1A@");
				customerDto.setHintQuestion1(restRequest.getQuestion());
				customerDto.setAnswer1(restRequest.getAnswer());
				customerDto.setTitle(1L);
				customerDto.setFirstName(restRequest.getFirstName());
				customerDto.setLastName(restRequest.getLastName());
				customerDto.setAddrOne("Marathali");
				customerDto.setAddrTwo("Post Office");
				customerDto.setCity("Bangalore");
				customerDto.setState(17L);
				customerDto.setCountry(1L);
				customerDto.setPostelCode("20037-8001");
				customerDto.setPhoneCode(restRequest.getPhoneCode());
				customerDto.setPhoneNo(restRequest.getPhoneNo());
				customerDto.setDateOfBirth(DateConvertion.stirngToDate(restRequest.getDateOfBirth()));
				customerDto.setKycRequired(false);
				customerDto.setLanguageId(1L);//ENG
				customerDto.setDefaultCurrency(4L);//INR
				customerDto.setMsisdnNumber(restRequest.getMsisdnNumber());
				customerDto.setSimNumber(restRequest.getSimNumber());
				customerDto.setImeiNumber(restRequest.getImeiNumber());
				customerDto.setmPin(restRequest.getmPin());
				CustomerDto customerDto1 = null;
				/*if(mode != null && mode.equalsIgnoreCase(REQUEST_FOR_REGISTRATION_AFTER_24HOURS)){
					Note: User is requested for registration after 24hours from his/her first time registration, 
					 * then the system will update the existing record including IP address, password and creation date
					Long customerId = customerService.getCustomerId(customerRegFormTwo.getEmailId(), CUSTOMER_USER_TYPE);
					CustomerDto customerDtoCopy = customerService.getCustomerDto(customerId);
					customerDto1 = customerService.registrationAfter24hours(customerDto);
					auditTrailService.createAuditTrail(customerDto.getAuthenticationId(), 
							AuditTrailConstrain.MODULE_CUSTOMER_REGISTRATION_AFTER_24HOURS, AuditTrailConstrain.STATUS_UPDATE, 
							customerDto.getEmailId(), GlobalLitterals.CUSTOMER_USER_TYPE, 
							customerDtoCopy, customerDto1);
				} else{*/
				customerDto1 = customerService.mobileNewRegistration(customerDto);
				//call transaction update method for non register person txn records.
				/*	List<NonRegisterWallet> nonRegWallets = commonService.getMoneyFromTemporaryWallet(customerDto.getEmailId());
					if(nonRegWallets != null){
						for(NonRegisterWallet nonRegWallet: nonRegWallets){
							Date date = nonRegWallet.getCreationDate();
							int nonRegWalletExpDays = utilService.getCancelNonregWalletTxnsAllowedDays();
							Date nonWalletExpireDate = DateConvertion.futureDate(date, nonRegWalletExpDays);
							customerDto.setCreationDate(new Date());
							if(customerDto.getCreationDate().compareTo(nonWalletExpireDate) <= GlobalLitterals.ZERO_INTEGER 
									&& !nonRegWallet.getRegister().equals(WalletTransactionStatus.CANCEL)){
								nonRegWallet.setRegister(WalletTransactionStatus.SUCCESS);
								commonService.updateTemporaryWalletRecord(nonRegWallet);
								sendMoneyService.updateSendMoneyForNonRegisters(nonRegWallet.getTxnId(), customerDto1.getAuthenticationId());
							}
						}
					}*/
				//}//else block
				//Audit Trail service
				auditTrailService.createAuditTrail(customerDto1.getAuthenticationId(), AuditTrailConstrain.MODULE_CUSTOMER_REGISTRATION, 
						AuditTrailConstrain.STATUS_REGISTRATION, customerDto1.getEmailId(), GlobalLitterals.CUSTOMER_USER_TYPE);
				// to set default preferences
				PreferencesDto preferencesDto = new PreferencesDto();
			    preferencesDto.setCurrency(CommonConstrain.DEFAULT_CURRENCY);
			    preferencesDto.setLanguage(CommonConstrain.DEFAULT_LANGUAGE);
				preferencesDto.setAuthentication(customerDto1.getAuthenticationId());
				commonService.createPreferences(preferencesDto);
				Boolean flag = Boolean.TRUE;
				if(!flag){
					return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.UNABLE_TO_REGISTERED_YOUR_MOBILE_WALLET.getValue());
				}
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.SUCCESSFULLY_REGISTERED_WITH_DEVICE.getValue(), customerDto1);
			}else{
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.INVALID_OTP_OR_EXPIRED_PLEASE_TRY_AGAIN.getValue());
			}
		} catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	
	@Path("/mpingeneration")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response mPinGeneration(@Context HttpServletRequest request, String loginInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" Entering mPinGeneration ");
		
		Response responce = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(loginInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(loginInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		responce = mandatoryFieldsCheckExcludePassword(request, restRequest);
		if(responce != null){
			return responce;
		}
		
		LOGGER.info(USER_EMAIL +": " + restRequest.getEmail() + USER_TYPE + " :" + restRequest.getUserType());
		
		try {
			if(RestCustomValidation.numberValidator(restRequest.getmPin(), null , Common.M_PIN_MAX_LENGHT)){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.INVALID_MPIN_FORMAT.getValue());
			}
			authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			responce = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.FALSE);
			if(responce != null){
				return responce;
			}else{
				String personName = getPersonName(restRequest.getEmail(), restRequest.getUserType());
				String userTypeName = UIUtil.getUserType(restRequest.getUserType());
				Boolean flag = commonService.mPinGeneration(restRequest.getEmail(), restRequest.getUserType(), restRequest.getMsisdnNumber(),
							restRequest.getSimNumber(), restRequest.getImeiNumber(), restRequest.getmPin(), personName, userTypeName);
				//Audit Trail service
				auditTrailService.createAuditTrail(authentication.getId(), AuditTrailConstrain.MODULE_CUSTOMER_MPIN_GENERATION, 
						AuditTrailConstrain.STATUS_CREATE, authentication.getEmailId(), GlobalLitterals.CUSTOMER_USER_TYPE);
				if(!flag){
					return ServerUtility.papulateErrorCode(request,
							ServerProcessorStatus.UNABLE_TO_GENERATE_MPIN.getValue());
				}
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.SUCCESSFULLY_MPIN_GENERATED.getValue(), null);
			}
		} catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	
	@Path("/logout")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(@Context HttpServletRequest request, String logoutInput) throws IOException {
		papulateServices(request);
		LOGGER.info(" Entering logout ");
		if(CommonWebserviceUtil.isEmpty(logoutInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		RestRequest restRequest = null;
		Authentication authentication = null;
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(logoutInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL +": " + restRequest.getEmail() + USER_TYPE +": " + restRequest.getUserType());
		try {
			authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			Boolean flag = loginService.logout(restRequest.getEmail(), restRequest.getUserType());
			if(flag){
				removeUserDetailsFromSession(request.getSession());
				return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.SUCCESSFULLY_LOGOUT.getValue(), null);
			}else{
				return ServerUtility.papulateErrorCode(request,
						ServerProcessorStatus.FAILED_TO_LOGOUT_FROM_MOBILE_WALLET.getValue());
			}
			
		} catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.FAILED_TO_LOGOUT_FROM_MOBILE_WALLET.getValue());
		}
		
	}
	
	@Path("/devicelogin")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deviceLogin(@Context HttpServletRequest request, String loginInput) throws WalletException {
		papulateServices(request);
		LOGGER.info(" Entering deviceLogin ");
		
		Response responce = null;
		RestRequest restRequest = null;
		CustomerDto customerDto = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(loginInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(loginInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		responce = mandatoryFieldsCheckForMobilePhone(request, restRequest);
		if(responce != null){
			return responce;
		}
		
		LOGGER.info("PHONE_CODE :" + restRequest.getPhoneCode() + "PHONE_NUMBER : " + restRequest.getPhoneNo());
		try {
			/*this check will take mPin should not be empty and it should not be null and it should be four digit number*/
			if(RestCustomValidation.numberValidator(restRequest.getmPin(), null , Common.M_PIN_MAX_LENGHT)){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.INVALID_MPIN_FORMAT.getValue());
			}
			
			customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			if(null == customerDto){
				throw new WalletException(CUSTOMER_MOBILE_NOT_EXIST);
			}
			authentication = commonService.getAuthentication(customerDto.getEmailId(), GlobalLitterals.CUSTOMER_USER_TYPE);
			if(null == authentication){
				throw new WalletException(CUSTOMER_MOBILE_NOT_EXIST);
			}
			restRequest.setEmail(authentication.getEmailId());
			restRequest.setUserType(authentication.getUserType());
			/*Skip phone number check, so passed value as null*/
			responce = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					null, authentication, Boolean.FALSE);
			if(responce != null){
				return responce;
			}
			if(null == authentication.getSimNumber() && null == authentication.getImeiNumber()){
				throw new WalletException(CommonConstrain.USER_NOT_REGISTER_AS_MOBILE_WALLET);   
			}else if(Boolean.TRUE.equals(authentication.getmPinBlocked())){
				throw new WalletException(CommonConstrain.MOBILE_USER_BLOCK);
			}else if(authentication.getSimNumber().equals(restRequest.getSimNumber())
				    && authentication.getImeiNumber().equals(restRequest.getImeiNumber())){
				LOGGER.info("Email :" + authentication.getEmailId() + "UserType : " + authentication.getUserType());
				Authentication loginAuth = loginService.loginWithDevice(authentication.getEmailId(), 
					restRequest.getmPin(), authentication.getUserType(), 
					MasterDataUtil.getTypeOfRequest(request.getSession().getServletContext(), 
							(Long) request.getSession().getAttribute(LANGUAGE_ID), TypeOfRequest.MOBILE.getValue()));
				
				if(Boolean.TRUE.equals(loginAuth.getmPinBlocked())){
					Map<String, String> jobProperties = new HashMap<String, String>();
					jobProperties.put(JobConstants.GROUP_NAME, SchedulerGroupNames.UNBLOCKED_USERS);
					jobProperties.put(JobConstants.USER_JOB_NAME, JobConstants.UNBLOCKED_USER_JOB_NAME);
					jobProperties.put(JobConstants.AUTH_ID, authentication.getId().toString());
					jobProperties.put(JobConstants.PERSON_NAME, getPersonName(restRequest.getEmail(), restRequest.getUserType()));
					jobProperties.put(JobConstants.USER_TYPE_NAME, UIUtil.getUserType(restRequest.getUserType()));
					
			    	schedulerService.scheduleUnblockedUsersNewJob(jobProperties);
			    		throw new WalletException(CommonConstrain.MOBILE_USER_BLOCK);
				}
				adddUserDetailsToSession(request, loginAuth);
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.USER_AUTHENTICATION_SUCCESS.getValue(), customerDto);
			}else{
				throw new WalletException(CommonConstrain.USER_REQUESTED_FROM_DIFFERENT_DEVICE_OR_SIM);
			}
		} catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			if(ex.getMessage().contains("phone.number.does.not.exist")){
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.PLEASE_CHECK_YOUR_MOBILE_NUMBER_AND_MPIN.getValue(), customerDto);
			}else{
				return ServerUtility.papulateErrorCode(request, 
						CommonWebserviceUtil.papulateErrorMessage(ex));
			}
			
		}
	}
	
	@Path("/devicechangempin")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeMPin(@Context HttpServletRequest request, String loginInput) throws WalletException {
		papulateServices(request);
		LOGGER.info(" changeMPin ");
		
		Response responce = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(loginInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			LOGGER.info(" changeMPin "+loginInput);
			Gson gson = new Gson();
			restRequest = gson.fromJson(loginInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		responce = mandatoryFieldsCheckExcludePassword(request, restRequest);
		if(responce != null){
			return responce;
		}
		
		LOGGER.info(USER_EMAIL +": " + restRequest.getEmail() + USER_TYPE +": " + restRequest.getUserType());
		
		try {
			/*this check will take mPin should not be empty and it should not be null and it should be four digit number*/
			
			authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			responce = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			
			if(responce != null){
				return responce;
			}
			String personName = getPersonName(restRequest.getEmail(), restRequest.getUserType());
			String userTypeName = UIUtil.getUserType(restRequest.getUserType());
			Boolean flagForChangPin = null;
			if(null != restRequest.getIsNewMPIN() && restRequest.getIsNewMPIN()){
				/*Validate OTP*/
				if(RestCustomValidation.numberValidator(restRequest.getOTP(), null , utilService.getOtpLength())){
					return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.INVALID_OTP_FORMAT.getValue());
				}
				restRequest.setEmail(authentication.getEmailId());
				Boolean isValidOtp = smsService.authenticateOTP(CommonRestServiceHelper.getOtpDto(restRequest, restRequest.getCustomerNumber()));
				if(isValidOtp){
				if(RestCustomValidation.numberValidator(restRequest.getNewMPin(), null , Common.M_PIN_MAX_LENGHT)){
					return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.INVALID_MPIN_FORMAT.getValue());
				}
				flagForChangPin = loginService.newMPin(restRequest.getEmail(), restRequest.getUserType(), 
						restRequest.getNewMPin(), personName, userTypeName);
				
				auditTrailService.createAuditTrail(restRequest.getCustomerNumber(), AuditTrailConstrain.MODULE_NEW_MPIN_REQUEST, 
						AuditTrailConstrain.STATUS_UPDATE, authentication.getEmailId(), authentication.getUserType());
				
				}else{
					return ServerUtility.papulateSuccessCode(request,
							ServerProcessorStatus.INVALID_OTP_OR_EXPIRED_PLEASE_TRY_AGAIN.getValue(), null);
				}
			}else{
				if(RestCustomValidation.numberValidator(restRequest.getmPin(), null , Common.M_PIN_MAX_LENGHT) 
						|| RestCustomValidation.numberValidator(restRequest.getNewMPin(), null , Common.M_PIN_MAX_LENGHT)){
					return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.INVALID_MPIN_FORMAT.getValue());
				}else if(restRequest.getmPin().equals (restRequest.getNewMPin())){			
					return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.OLDMPIN_NEWMPIN_SHOULDNOT_BE_SAME.getValue());
				}
				flagForChangPin = loginService.changeMPin(restRequest.getEmail(), restRequest.getUserType(), 
						restRequest.getmPin(), restRequest.getNewMPin(), personName, userTypeName);
				
				auditTrailService.createAuditTrail(restRequest.getCustomerNumber(), AuditTrailConstrain.MODULE_CHANGE_MPIN_REQUEST, 
						AuditTrailConstrain.STATUS_UPDATE, authentication.getEmailId(), authentication.getUserType());
			}
			if(!flagForChangPin){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.MPIN_MATCH_FAIL.getValue());
			}
			
			return ServerUtility.papulateSuccessCode(request,
					ServerProcessorStatus.SUCCESSFULLY_MPIN_CHANGED.getValue(), null);
		}catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	
	@Path("/deactivate")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deactivateDevice(@Context HttpServletRequest request, String deactivateInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" deactivateDevice ");
		
		Response responce = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(deactivateInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(deactivateInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		responce = mandatoryFieldsCheckExcludePassword(request, restRequest);
		if(responce != null){
			return responce;
		}
		
		LOGGER.info(USER_EMAIL +": " + restRequest.getEmail() + USER_TYPE + ": " + restRequest.getUserType());
		
		try {
			if(RestCustomValidation.numberValidator(restRequest.getmPin(), null , Common.M_PIN_MAX_LENGHT)){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.INVALID_MPIN_FORMAT.getValue());
			}
			commonService.validateMpin(restRequest.getEmail(), restRequest.getUserType(),restRequest.getmPin());
			authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			responce = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(responce != null){
				return responce;
			}else{
				String personName = getPersonName(restRequest.getEmail(), restRequest.getUserType());
				String userTypeName = UIUtil.getUserType(restRequest.getUserType());
				Boolean flag  = commonService.deActivateDevice(restRequest.getEmail(), restRequest.getUserType(),
						personName, userTypeName);
				if(!flag){
					return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.UNABLE_TO_DEACTIVATE_YOUR_MOBILE_WALLET.getValue());
				}
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.SUCCESSFULLY_DEACTIVATED_WITH_DEVICE.getValue(), null);
			}
		}catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	
	/* MPIN will not send to the user as email notification. 
	 * 1. User will enter mobile number & Secrete Q & A
	 * 2. System will validate the inputs
	 * 3. System will send OTP to user
	 * 4. Once OTP validated, mobile will navigate to new MPIN screen 
	 * 5. User will choose new MPIN */
	@Path("/forgotmpin")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forgotMpin(@Context HttpServletRequest request, String forgotMpinInput) throws WalletException {
		
		papulateServices(request);
		LOGGER.info(" forgotMpin ");
		
		Response responce = null;
		RestRequest restRequest = null;
		CustomerDto customerDto = null;
		Authentication authentication = null;
		
		if(CommonWebserviceUtil.isEmpty(forgotMpinInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(forgotMpinInput, RestRequest.class);
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		responce = mandatoryFieldsCheckForMobilePhone(request, restRequest);
		if(responce != null){
			return responce;
		}
		
		LOGGER.info("PHONE_CODE :" + restRequest.getPhoneCode() + "PHONE_NUMBER : " + restRequest.getPhoneNo());
		try {
						
			customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			if(null == customerDto){
				throw new WalletException(CUSTOMER_MOBILE_NOT_EXIST);
			}
			authentication = commonService.getAuthentication(customerDto.getEmailId(), GlobalLitterals.CUSTOMER_USER_TYPE);
			if(null == authentication){
				throw new WalletException(CUSTOMER_MOBILE_NOT_EXIST);
			}
			restRequest.setEmail(authentication.getEmailId());
			restRequest.setUserType(authentication.getUserType());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			responce = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.FALSE);
			if(responce != null){
				return responce;
			}else{
			   String personName = getPersonName(restRequest.getEmail(), restRequest.getUserType());	
			   String userTypeName = UIUtil.getUserType(restRequest.getUserType());
			   Boolean flag  = loginService.forgotMpin(restRequest.getEmail(),restRequest.getUserType(),
							restRequest.getMsisdnNumber(), restRequest.getSimNumber(), restRequest.getImeiNumber(),restRequest.getQuestion()
							, restRequest.getAnswer(), personName , userTypeName);
					if(!flag){
						return ServerUtility.papulateErrorCode(request, 
								ServerProcessorStatus.FORGOT_MPIN_FAIL.getValue());
					}
					auditTrailService.createAuditTrail(customerDto.getId(), AuditTrailConstrain.MODULE_FORGOT_MPIN_REQUEST, 
							AuditTrailConstrain.STATUS_UPDATE, authentication.getEmailId(), authentication.getUserType());
					restRequest.setQuestion(null);
					restRequest.setAnswer(null);
					restRequest.setImeiNumber(null);
					restRequest.setSimNumber(null);
					restRequest.setMsisdnNumber(null);
					restRequest.setCustomerNumber(customerDto.getId());
					restRequest.setIsNewMPIN(Boolean.TRUE);
					return ServerUtility.papulateSuccessCode(request,
							ServerProcessorStatus.SECRET_QA_SUCCESS_AND_SENT_OTP.getValue(), restRequest);
			}
		}catch (WalletException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					CommonWebserviceUtil.papulateErrorMessage(ex));
		}
	}
	
	/**
	 * Empty check for mandatory fields
	 * @param request
	 * @param restRequest
	 * @return
	 */
	private Response mandatoryFieldsCheck(HttpServletRequest request, RestRequest restRequest) {
		if(CommonWebserviceUtil.isEmpty(restRequest.getEmail())
				|| restRequest.getEmail().equals(NULL_STRING)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMAIL_SHOULD_NOT_BE_EMPTY.getValue());
		}
		if(CommonWebserviceUtil.isEmpty(restRequest.getUserType())
				|| restRequest.getUserType().equals(NULL_STRING)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.USERTYPE_SHOULD_NOT_BE_EMPTY.getValue());
		}
		if(CommonWebserviceUtil.isEmpty(restRequest.getPassword())
				|| restRequest.getPassword().equals(NULL_STRING)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.PASSWORD_SHOULD_NOT_BE_EMPTY.getValue());
		}
		return null;
	}
	
	/**
	 * Empty check for mandatory fields for mobile
	 * @param request
	 * @param restRequest
	 * @return
	 */
	private Response mandatoryFieldsCheckForMobilePhone(HttpServletRequest request, RestRequest restRequest) {
		
		if(CommonWebserviceUtil.isEmpty(restRequest.getPhoneCode()) || restRequest.getPhoneCode().equals(NULL_STRING)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.MOBILE_CODE_SHOULD_NOT_BE_EMPTY.getValue());
		}
		if(CommonWebserviceUtil.isEmpty(restRequest.getPhoneNo()) || restRequest.getPhoneNo().equals(NULL_STRING)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.MOBILE_NUMBER_SHOULD_NOT_BE_EMPTY.getValue());
		}
		
		return null;
	}
		
	
	/**
	 * Empty check for mandatory fields exclude password
	 * @param request
	 * @param restRequest
	 * @return
	 */
	private Response mandatoryFieldsCheckExcludePassword(HttpServletRequest request, RestRequest restRequest) {
		if(CommonWebserviceUtil.isEmpty(restRequest.getEmail())
				|| restRequest.getEmail().equals(NULL_STRING)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMAIL_SHOULD_NOT_BE_EMPTY.getValue());
		}
		if(CommonWebserviceUtil.isEmpty(restRequest.getUserType())
				|| restRequest.getUserType().equals(NULL_STRING)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.USERTYPE_SHOULD_NOT_BE_EMPTY.getValue());
		}
		return null;
	}
	
	private String getPersonName(String email, String userType) throws WalletException{
		String personName = null;
		if(GlobalLitterals.CUSTOMER_USER_TYPE.equals(userType)){
			personName = customerService.getPersonName(email, userType);
			LOGGER.info("Customer Name :: " + personName);
		}else if (GlobalLitterals.MERCHANT_USER_TYPE.equals(userType)){
			personName = merchantService.getPersonName(email, userType);
			LOGGER.info("Merchant Name :: " + personName);
		}
		return personName;
	}
	
	/**
	 * Deactivated user requested for activate mobile wallet
	 * @param authentication
	 * @param restRequest
	 * @return
	 */
	private Boolean checkDeviceActiveStatus(Authentication authentication, RestRequest restRequest){
		Boolean flag = CommonUtil.checkUserHasRequestedFromRegisterMobileWallet(authentication, restRequest.getMsisdnNumber(), 
				restRequest.getSimNumber(), restRequest.getImeiNumber());
		return flag;
	}

	/**
	 * @param request
	 * @param authentication
	 */
	private void adddUserDetailsToSession(HttpServletRequest request,Authentication authentication){
		HttpSession session = request.getSession(true);
		session.setAttribute(USER_ID, authentication.getEmailId());
		session.setAttribute(USER_TYPE, authentication.getUserType());
		session.setAttribute(USER_STATUS, authentication.getStatus());
	}
	
	/**
	 * After logout removed user details from session object
	 * @param session
	 */
	private void removeUserDetailsFromSession(HttpSession session){
		if(session.getAttribute(USER_ID) != null){
			session.removeAttribute(USER_ID);
		}
		if(session.getAttribute(USER_TYPE) != null){
			session.removeAttribute(USER_TYPE);
		}
		if(session.getAttribute(USER_STATUS) != null){
			session.removeAttribute(USER_STATUS);
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
		loginService = (LoginService) ServerUtility.getServiceInstance(session, LOGIN_SERVICE);
		commonService = (CommonService) ServerUtility.getServiceInstance(session, COMMON_SERVICE);
		customerService = (CustomerService) ServerUtility.getServiceInstance(session, CUSTOMER_SERVICE);
		merchantService = (MerchantService) ServerUtility.getServiceInstance(session, MERCHANT_SERVICE);
		schedulerService = (SchedulerService) ServerUtility.getServiceInstance(session, SCHEDULER_SERVICE);
		cryptService = (CryptService) ServerUtility.getServiceInstance(session, CRYPT_SERVICE);
		auditTrailService = (AuditTrailService) ServerUtility.getServiceInstance(session, AUDIT_TRAIL_SERVICE);
		smsService = (SmsService) ServerUtility.getServiceInstance(request.getSession(), SMS_SERVICE);
		utilService = (UtilService) ServerUtility.getServiceInstance(request.getSession(), UTIL_SERVICE);
		//sendMoneyService  = (SendMoneyService) ServerUtility.getServiceInstance(session, SENDMONEY_SERVICE);
	}
	
}

