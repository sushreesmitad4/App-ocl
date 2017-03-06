package com.tarang.mwallet.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.tarang.ewallet.accounts.business.AccountsService;
import com.tarang.ewallet.accounts.util.AccountsConstants;
import com.tarang.ewallet.audittrail.business.AuditTrailService;
import com.tarang.ewallet.audittrail.util.AuditTrailConstrain;
import com.tarang.ewallet.common.business.CommonService;
import com.tarang.ewallet.common.util.TypeOfRequest;
import com.tarang.ewallet.customer.service.CustomerService;
import com.tarang.ewallet.dto.AddMoneyDto;
import com.tarang.ewallet.dto.CustomerDto;
import com.tarang.ewallet.dto.ReloadMoneyDto;
import com.tarang.ewallet.exception.WalletException;
import com.tarang.ewallet.http.util.ErrorCodeConstants;
import com.tarang.ewallet.merchant.business.MerchantService;
import com.tarang.ewallet.model.Authentication;
import com.tarang.ewallet.model.PhoneNumber;
import com.tarang.ewallet.model.VelocityAndThreshold;
import com.tarang.ewallet.transaction.business.AddMoneyService;
import com.tarang.ewallet.transaction.business.ReloadMoneyService;
import com.tarang.ewallet.transaction.business.VelocityAndThresholdService;
import com.tarang.ewallet.transaction.util.WalletTransactionConstants;
import com.tarang.ewallet.transaction.util.WalletTransactionTypes;
import com.tarang.ewallet.util.GlobalLitterals;
import com.tarang.ewallet.walletui.controller.AttributeConstants;
import com.tarang.ewallet.walletui.form.ReloadMoneyForm;
import com.tarang.ewallet.walletui.util.MasterDataUtil;
import com.tarang.ewallet.walletui.util.UIUtil;
import com.tarang.mwallet.rest.services.model.RestRequest;
import com.tarang.mwallet.rest.services.util.CommonRestServiceHelper;
import com.tarang.mwallet.rest.services.util.CommonWebserviceUtil;
import com.tarang.mwallet.rest.services.util.Constants;
import com.tarang.mwallet.rest.services.util.ServerProcessorStatus;
import com.tarang.mwallet.rest.services.util.ServerUtility;
import com.tarang.mwallet.rest.services.util.TransactionServiceHelper;

/**
 * @author kedarnathd
 * This rest service will provide the add fund functionality to the device
 */
@Path("/addmoney")
public class AddMoneyServices implements AttributeConstants , GlobalLitterals, Constants{
	
	private static final Logger LOGGER = Logger.getLogger(AddMoneyServices.class);
	
	private AccountsService accountsService;
	
	private AddMoneyService addMoneyService;
	
	private CommonService commonService;
	
	private CustomerService customerService;
	
	private MerchantService merchantService;
	
	private VelocityAndThresholdService velocityAndThresholdService;
	
	private AuditTrailService auditTrailService;

	@Path("/addmoneyservice")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addmoneyrequestSave(@Context HttpServletRequest request, 
			String addfundInput) throws WalletException {
		papulateServices(request);
		Response response = null;
		RestRequest restRequest = null;
		CustomerDto customerDto = null;
		Authentication authentication = null;
		LOGGER.info(" addmoneyrequestSave ");
		if (CommonWebserviceUtil.isEmpty(addfundInput)) {
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(addfundInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		/*Required input fields for Add money service*/
		LOGGER.info("emailId: " + restRequest.getEmail() + "userType: " + restRequest.getUserType() 
				+ "orderId: " + restRequest.getOrderId() +" amount: "+restRequest.getAmount()
				+ "requestedCurrency: "+restRequest.getRequestedCurrency());
		
		//ReloadMoneyForm reloadMoneyForm = getReloadMoneyForm(restRequest);
		String userType = restRequest.getUserType();
		try {
			customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), userType);
			restRequest.setEmail(authentication.getEmailId());
			
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			Long status = authentication.getStatus();
			Response responce = CommonRestServiceHelper.checkTransaction(request, userType, 
		    		status);
			if(responce != null){
				return responce;
			} 
			/*AccountDto accountDto  = accountsService.getAccount(Long.parseLong(reloadMoneyForm.getAccountId()));
			response = CommonRestServiceHelper.isAccountBelongsToUser(request, authentication, accountDto);
			if(response != null){
				return response;
			}
			if(RestCustomValidation.numberValidation(reloadMoneyForm.getCvv(), accountDto.getCardType())){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.INVALID_CVV_FORMATE.getValue());
			}
			if(!AccountsConstants.CARD_ACCOUNT.equals(accountDto.getAtype())){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.ACCOUNT_TYPE_IS_NOT_CARD.getValue());
			}
			if(!FundingAccountStatus.VERIFIED_STATUS.equals(accountDto.getStatus())){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.ACCOUNT_IS_NOT_VARIFIED.getValue());
			}*/
			/*ReloadMoneyDto reloadMnyDto = getReloadMoneyDto(request, reloadMoneyForm, restRequest.getEmail(), 
					restRequest.getUserType(), accountDto);*/
			AddMoneyDto addMnyDto = getAddMoneyDto(request, restRequest.getEmail(), 
					restRequest.getUserType());
			addMnyDto.setOrderId(restRequest.getOrderId());
			addMnyDto.setAuthId(authentication.getId());
			VelocityAndThreshold velocity = velocityAndThresholdService.getThreshold(addMnyDto.getCountryId(), 
					addMnyDto.getCurrencyId(), addMnyDto.getTypeOfTransaction(), addMnyDto.getUserType());
			if(restRequest.getAmount() == null || restRequest.getAmount().equals(EMPTY_STRING)){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.INVALID_AMOUNT_FORMAT.getValue());
			} else {
				if(!restRequest.getAmount().equals(EMPTY_STRING)){
					Boolean amountError = UIUtil.currencyValidator(restRequest.getAmount());
					if(amountError){
						return ServerUtility.papulateErrorCode(request, 
								ServerProcessorStatus.INVALID_AMOUNT_FORMAT.getValue());
					} else {
						addMnyDto.setAddMoneyAmount(Double.parseDouble(restRequest.getAmount()));
					}
					if((velocity != null && addMnyDto.getAddMoneyAmount() != null) &&
							!(velocity.getMinimumamount() <= addMnyDto.getAddMoneyAmount() && 
							velocity.getMaximumamount() >= addMnyDto.getAddMoneyAmount())){
						return ServerUtility.papulateErrorCode(request, 
								ServerProcessorStatus.THRESHOLD_DOEST_SUPPORT_FOR_RELOAD_FUND.getValue());
					}
				}	
			}
			try {
				AddMoneyDto addMoneyDto = addMoneyService.createAddMoney(addMnyDto);
				auditTrailService.createAuditTrail(addMnyDto.getAuthId(), AuditTrailConstrain.MODULE_RELAOD_MONEY_CREATE, 
						AuditTrailConstrain.STATUS_CREATE, addMnyDto.getPayeeEmail(), restRequest.getUserType());
				
				if(addMnyDto.getOrderId() != null && addMoneyDto != null && addMoneyDto.getIsReloadMoneySucc()){
					return ServerUtility.papulateSuccessCode(request, 
							ServerProcessorStatus.MONEY_ADDED_SUCCESSFULLY.getValue(), null);
				} else {
					//failed
					/*return ServerUtility.papulatePGErrorCode(request, 
							TransactionServiceHelper.papulatePgResponseCode(reloadMoneyDto.getPgResponseCode()));*/
					return ServerUtility.papulateSuccessCode(request, 
							ServerProcessorStatus.FAILED_TO_ADD_MONEY.getValue(), null);
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				String em = null;
				if(e.getMessage().contains(WalletTransactionConstants.ERROR_OVER_LIMIT_THRESHOLD_AMOUNT)){
					em = ServerProcessorStatus.ERROR_OVER_LIMIT_THRESHOLD_AMOUNT.getValue();
				} else if(e.getMessage().equals(ErrorCodeConstants.PG_SERVICE_IS_NOT_ESTABLISH)){
					em = ServerProcessorStatus.PG_SERVICE_IS_NOT_ESTABLISH.getValue();
				} else if(e.getMessage().equals(ErrorCodeConstants.PG_SERVICE_IS_NOT_ESTABLISH)){
					em = ServerProcessorStatus.PG_SERVICE_IS_NOT_ESTABLISH.getValue();
				} else if(e.getMessage().equals(ErrorCodeConstants.COMMUNICATION_WITH_PAYMENT_SYSTEM_TIMED_OUT)){
					em = ServerProcessorStatus.COMMUNICATION_WITH_PAYMENT_SYSTEM_TIMED_OUT.getValue();
				} else{
					em = ServerProcessorStatus.RELOAD_FAILED_MESSAGE.getValue();
				}
				return ServerUtility.papulateErrorCode(request,	em);
			}
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			if(AccountsConstants.ERROR_MSG_NON_EXISTS.equals(ex.getMessage())){
				LOGGER.error(ex.getMessage(), ex);
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.FAILEDTO_RETRIEVE_ACCOUNT_DETAILS_RELOAD_MONEY_ERRMSG.getValue());
			}
		}
		return null;
	}
	
	private AddMoneyDto getAddMoneyDto(HttpServletRequest request, String emailId, String userType) throws WalletException{
		AddMoneyDto addMoneyDto = new AddMoneyDto();
		if(CUSTOMER_USER_TYPE.equals(userType)){
			addMoneyDto.setTypeOfTransaction(WalletTransactionTypes.ADD_MONEY_FROM_CARD_CUSTOMER);
			addMoneyDto.setUserType(USERTYPE_CUSTOMER);
		} else if(MERCHANT_USER_TYPE.equals(userType)){
			addMoneyDto.setTypeOfTransaction(WalletTransactionTypes.ADD_MONEY_FROM_CARD_MERCHANT);
			addMoneyDto.setUserType(USERTYPE_MERCHANT);
		}
		//addMoneyDto.setAccountId(accountDto.getId());
		//addMoneyDto.setCvv(reloadMoneyForm.getCvv());
		//addMoneyDto.setCardNumber(accountDto.getCardNumber());
		//addMoneyDto.setCardExpairyDate(accountDto.getCardExpairyDate());
		addMoneyDto.setCountryId(4L);
		addMoneyDto.setCurrencyCode(MasterDataUtil.getCountryCurrencyCode(
				request.getSession().getServletContext(), 4L));
		addMoneyDto.setCurrencyId(MasterDataUtil.getCountryCurrencyId(
				request.getSession().getServletContext(), 4L));
		addMoneyDto.setTypeOfRequest(MasterDataUtil.getTypeOfRequest(request.getSession().getServletContext(), 
				(Long) request.getSession().getAttribute(LANGUAGE_ID), TypeOfRequest.MOBILE.getValue()));
		String personName = null;
		if(GlobalLitterals.CUSTOMER_USER_TYPE.equals(userType)){
			personName = customerService.getPersonName(emailId, userType);
			LOGGER.info("Customer Name :: " + personName);
		} else if (GlobalLitterals.MERCHANT_USER_TYPE.equals(userType)){
			personName = merchantService.getPersonName(emailId, userType);
			LOGGER.info("Merchant Name :: " + personName);
		}
		addMoneyDto.setPayeeName(personName);
		addMoneyDto.setPayeeEmail(emailId);
		//Time being take it as a null
		addMoneyDto.setLanguageId(null);
		addMoneyDto.setAccountOrCardHolderName(personName);
		return addMoneyDto;
	}
	
	/**
	 * Convert the request object to the actual form object
	 * @param restRequest
	 * @return
	 */
	private ReloadMoneyForm getReloadMoneyForm(RestRequest restRequest){
		ReloadMoneyForm reloadMoneyForm = new ReloadMoneyForm();
		if(null == restRequest){
			return null;
		}
		reloadMoneyForm.setAccountId(GlobalLitterals.EMPTY_STRING + restRequest.getAccountId());
		reloadMoneyForm.setAmount(restRequest.getAmount());
		reloadMoneyForm.setCvv(restRequest.getCvv());
		return reloadMoneyForm;
	}
	
	/**
	 * Get service instances from request object
	 * @param request
	 */
	private void papulateServices(HttpServletRequest request){
		accountsService = (AccountsService) ServerUtility.getServiceInstance(request.getSession(), ACCOUNTS_SERVICE );
		addMoneyService = (AddMoneyService) ServerUtility.getServiceInstance(request.getSession(), ADD_MONEY_SERVICE);
		commonService = (CommonService) ServerUtility.getServiceInstance(request.getSession(), COMMON_SERVICE);
		customerService = (CustomerService) ServerUtility.getServiceInstance(request.getSession(), CUSTOMER_SERVICE);
		merchantService = (MerchantService) ServerUtility.getServiceInstance(request.getSession(), MERCHANT_SERVICE);
		velocityAndThresholdService = (VelocityAndThresholdService) ServerUtility.getServiceInstance(request.getSession(), VELOCITY_AND_THRESHOLD_SERVICE);
		auditTrailService = (AuditTrailService) ServerUtility.getServiceInstance(request.getSession(), AUDIT_TRAIL_SERVICE);
	}
}
