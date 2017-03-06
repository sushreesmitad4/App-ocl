/**
 * 
 */
package com.tarang.mwallet.rest.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.tarang.ewallet.accounts.business.AccountsService;
import com.tarang.ewallet.accounts.util.AccountsConstants;
import com.tarang.ewallet.accounts.util.FundingAccountStatus;
import com.tarang.ewallet.audittrail.business.AuditTrailService;
import com.tarang.ewallet.audittrail.util.AuditTrailConstrain;
import com.tarang.ewallet.common.business.CommonService;
import com.tarang.ewallet.common.util.CommonUtil;
import com.tarang.ewallet.common.util.TypeOfRequest;
import com.tarang.ewallet.customer.service.CustomerService;
import com.tarang.ewallet.dto.AccountDto;
import com.tarang.ewallet.dto.CustomerDto;
import com.tarang.ewallet.exception.WalletException;
import com.tarang.ewallet.http.util.ErrorCodeConstants;
import com.tarang.ewallet.http.util.HttpServiceConstants;
import com.tarang.ewallet.merchant.business.MerchantService;
import com.tarang.ewallet.model.Address;
import com.tarang.ewallet.model.Authentication;
import com.tarang.ewallet.model.History;
import com.tarang.ewallet.model.PhoneNumber;
import com.tarang.ewallet.transaction.business.FraudCheckService;
import com.tarang.ewallet.transaction.business.PGService;
import com.tarang.ewallet.transaction.util.WalletTransactionTypes;
import com.tarang.ewallet.util.DateConvertion;
import com.tarang.ewallet.util.GlobalLitterals;
import com.tarang.ewallet.util.service.UtilService;
import com.tarang.ewallet.walletui.controller.AttributeConstants;
import com.tarang.ewallet.walletui.controller.AttributeValueConstants;
import com.tarang.ewallet.walletui.controller.constants.Accounts;
import com.tarang.ewallet.walletui.form.AddCardAccountForm;
import com.tarang.ewallet.walletui.util.ManageAccountUtil;
import com.tarang.ewallet.walletui.util.ManageAccountsHelper;
import com.tarang.ewallet.walletui.util.MasterDataConstants;
import com.tarang.ewallet.walletui.util.MasterDataUtil;
import com.tarang.ewallet.walletui.validator.common.Common;
import com.tarang.ewallet.walletui.validator.common.CommonValidator;
import com.tarang.mwallet.rest.services.model.RestRequest;
import com.tarang.mwallet.rest.services.util.CommonRestServiceHelper;
import com.tarang.mwallet.rest.services.util.CommonWebserviceUtil;
import com.tarang.mwallet.rest.services.util.Constants;
import com.tarang.mwallet.rest.services.util.ServerProcessorStatus;
import com.tarang.mwallet.rest.services.util.ServerUtility;
import com.tarang.mwallet.rest.services.util.TransactionServiceHelper;

/**
 * @author kedarnathd 
 *	This rest service will publish the card related activities
 *  to the mobile request in the wallet system.
 */
@Path("/manageaccounts")
public class ManageAccountsRestService implements Accounts, AttributeConstants,
		AttributeValueConstants, GlobalLitterals, Constants {
	private static final Logger LOGGER = Logger.getLogger(ManageAccountsRestService.class);

	private AuditTrailService auditTrailService;

	private FraudCheckService fraudCheckService;

	private AccountsService accountsService;

	private CustomerService customerService;

	private MerchantService merchantService;

	private CommonService commonService;

	private UtilService utilService;
	
	private PGService pGService;
	
	@Path("/addcard")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addCard(@Context HttpServletRequest request, String addCardInput) throws WalletException {
		/* get service instances */
		papulateServices(request);
		Long authenticationId = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		CustomerDto customerDto = null;
		
		LOGGER.info(" Entering add card rest service ");
		if (CommonWebserviceUtil.isEmpty(addCardInput)) {
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			LOGGER.info(" addCardInput :: " +addCardInput);
			Gson gson = new Gson();
			restRequest = gson.fromJson(addCardInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		//String emailId = restRequest.getEmail();
		//String userType = restRequest.getUserType();
		
		LOGGER.info("PHONE_CODE :" + restRequest.getPhoneCode() + "PHONE_NUMBER : " + restRequest.getPhoneNo());
			
		LOGGER.info(" email: " + restRequest.getEmail() + " userType: " 
		+ restRequest.getUserType()+ CARD_TYPE +": "+restRequest.getCardType());
		
		try {
			String errMsg = CommonValidator.cardValidator(restRequest.getCardNumber(), restRequest.getCardType());
			if(null != errMsg){
				throw new WalletException(errMsg);
			}
			customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), GlobalLitterals.CUSTOMER_USER_TYPE);
			restRequest.setEmail(authentication.getEmailId());
			String emailId = authentication.getEmailId();
			String userType = authentication.getUserType();

			AddCardAccountForm addCardAccountForm = ManageAccountsHelper.getAddCardAccountForm(restRequest);
			addCardAccountForm.setStatus(FundingAccountStatus.NOT_VERIFIED_STATUS);
			//authentication = commonService.getAuthentication(emailId, userType);
			
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			authenticationId = authentication.getId();
			String errorMsg = accountsCountVerification(authenticationId);
			if(errorMsg != null){
				return ServerUtility.papulateErrorCode(request, errorMsg);
			}
			// TODO: Need to add here the field validation
			
			// ************ JOINT ACCOUNT CHECK START ***************//
			Integer totalExistCardAccouts = accountsService
					.getTotalCardAccounts(addCardAccountForm.getCardNumber(),
							userType, AccountsConstants.NOT_DELETE);

			if (userType.equals(CUSTOMER_USER_TYPE)) {
				if (!totalExistCardAccouts.equals(GlobalLitterals.ZERO_INTEGER)) {
					if (addCardAccountForm.getJointAccount()) {
						if (totalExistCardAccouts.intValue() >= utilService.getJointCardAccountsLimit()) {
							return ServerUtility.papulateErrorCode(request,
								ServerProcessorStatus.JOINT_CARD_ACCOUNT_EXCEEDED.getValue());
						} else {
							Boolean cardExistByAuthId = accountsService.cardAccountExistWithSameUser(
									addCardAccountForm.getCardNumber(), authenticationId, AccountsConstants.NOT_DELETE);
							if (cardExistByAuthId.equals(Boolean.TRUE)) {
								return ServerUtility.papulateErrorCode(request, 
									ServerProcessorStatus.JOINT_CARD_ACCOUNT_EXISTWITHAUTHID.getValue());
							}
						}
					} else {
						return ServerUtility.papulateErrorCode(request, 
								ServerProcessorStatus.DUPLICATE_CARD_ACCOUNT.getValue());
					}
				}
			} else if (userType.equals(MERCHANT_USER_TYPE) && !totalExistCardAccouts.equals(ZERO_INTEGER)) {
				Boolean cardExistByAuthId = accountsService.cardAccountExistWithSameUser(addCardAccountForm.getCardNumber(),
								authenticationId, AccountsConstants.NOT_DELETE);
				if (cardExistByAuthId.equals(Boolean.TRUE)) {
					return ServerUtility.papulateErrorCode(request,
						ServerProcessorStatus.JOINT_CARD_ACCOUNT_EXISTWITHAUTHID.getValue());
				} else {
					return ServerUtility.papulateErrorCode(request,
							ServerProcessorStatus.DUPLICATE_CARD_ACCOUNT.getValue());
				}
			}
			// ************ JOINT ACCOUNT CHECK CLOSED ***************//
			// ************ FRAUD CHECK START -8 ***************//
			Integer hours = utilService.getCardAllowedHours();
			Integer noOfAccountsCard = utilService.getCardAllowedAccounts();
			Date fromDateCard = DateConvertion.getPastDateByHours(hours);
			Boolean flag = fraudCheckService.fundingSourcePatternCheckCard(
					addCardAccountForm.getCardNumber().substring(0, CARD_BIN_LENGTH), fromDateCard, 
					new Date(), noOfAccountsCard);
			if (flag) {
				return ServerUtility.papulateErrorCode(request,
						ServerProcessorStatus.CARD_FRAUD_CHECK_FAILED.getValue());
			}
			// ************ FRAUD CHECK END ***************//

			Address profileAddress = getProfileAddress(emailId, userType);
			AccountDto rstDto = saveCard(request, addCardAccountForm, profileAddress, authenticationId);
						
			// Audit Trail service
			auditTrailService.createAuditTrail(authenticationId, 
					AuditTrailConstrain.ACCOUNT_MODULE_CARD_CREATE,
					AuditTrailConstrain.STATUS_CREATE, emailId, userType);
			String verifyCardInput = "";
			restRequest.setAccountId(rstDto.getId());
			restRequest.setCode(rstDto.getOrderId());
			restRequest.setAmount(rstDto.getTransactionAmount());
			
			try {
				LOGGER.info(" Converting verifyCardInput :: " +addCardInput);
				Gson gson = new Gson();
				verifyCardInput = gson.toJson(restRequest);
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.EMPTY_DATA.getValue());
			}
			if (!AccountsConstants.OLD_ACCOUNT.equals(rstDto.getPgResponseCode())) {
				if(HttpServiceConstants.RESPONSE_DECISION_SUCCESS.equals(rstDto.getPgResponseCode())){
					LOGGER.info(" Add Card Success and calling Verify Card Now...");
					response = this.verifyCards(request, verifyCardInput);
					LOGGER.info(" Verify Card Success...");
					return response;
					/*return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.CARD_REGISTER_SUCCESS_MSG.getValue(), 
						CommonRestServiceHelper.getDisplayAccountDto(rstDto));*/
				} else{
					return ServerUtility.papulatePGErrorCode(request, 
							TransactionServiceHelper.papulatePgResponseCode(rstDto.getPgResponseCode()));
				}
			} else {
				LOGGER.info(" Existing Card Added Again and calling To Verify For Not Verify Card...");
				response = this.verifyCards(request, verifyCardInput);
				if(Constants.SUCCESS == response.getStatus()){
					
					String actJsonBody = (String) response.getEntity();
					System.out.println("actJsonBody  ::  "+actJsonBody);
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser.parse(actJsonBody);
					 // get a String from the JSON object
					//verifyCardInput = (String) jsonObject.get(REQUESTED_DATA);
					JSONObject geoPosition = (JSONObject) jsonObject.get(REQUESTED_DATA);
					verifyCardInput = geoPosition.toString();
					LOGGER.info("verifyCardInput :" + verifyCardInput);
				}
				
				LOGGER.info("orderID :" + rstDto.getOrderId());
				LOGGER.info(" Verify Card Called Success and now Card Is Pending, So calling one more time to verify it...");
				response = this.verifyCards(request, verifyCardInput);
				LOGGER.info(" Verify Card Success...");
				
				return response;
				/*return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.EXISTING_ACCOUNT_ADDED_AGAIN.getValue(), 
						CommonRestServiceHelper.getDisplayAccountDto(rstDto));*/
			}

		} catch (NullPointerException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.ERROR_MSG_FAILS_TO_GET_ACCOUNT.getValue());
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			String eMsg = null;
			if(ex.getMessage().equals(GlobalLitterals.DB_DUPLICATE_ENTRY)) {
				eMsg = ServerProcessorStatus.DUPLICATE_CARD_ACCOUNT.getValue();
			} else if(ex.getMessage().equals(
					ErrorCodeConstants.PG_SERVICE_IS_NOT_ESTABLISH)) {
				eMsg = ServerProcessorStatus.PG_SERVICE_IS_NOT_ESTABLISH.getValue();
			} else if(ex.getMessage()
					.equals(ErrorCodeConstants.COMMUNICATION_WITH_PAYMENT_SYSTEM_TIMED_OUT)) {
				eMsg = ServerProcessorStatus.COMMUNICATION_WITH_PAYMENT_SYSTEM_TIMED_OUT.getValue();
			} else if(ex.getMessage().equals(Common.CARD_NUMBER_ERROR_MSG)) {
				eMsg = ServerProcessorStatus.PLEASE_ENTER_VALID_CARD_NUMBER.getValue();
			} else {
				eMsg = ServerProcessorStatus.CARD_SAVE_ERRMSG.getValue();
			}
			return ServerUtility.papulateErrorCode(request, eMsg);
		}
	}

	@Path("/accountrecords")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response viewListOfAccountRecords(@Context HttpServletRequest request, String listOfCardInput)throws WalletException {
		papulateServices(request);
		Authentication authentication = null;
		RestRequest restRequest = null;
		
		LOGGER.info(" Entering viewListOfAccountRecords to get list of cards ");
		if (CommonWebserviceUtil.isEmpty(listOfCardInput)) {
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(listOfCardInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE +":"+ restRequest.getUserType()+" msisdnNo:"
				 + restRequest.getMsisdnNumber() + " simNo:"+ restRequest.getSimNumber() +" imeiNo:" 
				 + restRequest.getImeiNumber());
		
		List<AccountDto> displayList = new ArrayList<AccountDto>();
		try {
			//authentication = commonService.getAuthentication(emailId, userType);
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());		
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			
			Map<Long, String> moneyAccountStatusMap = MasterDataUtil.getSimpleDataMap(
					request.getSession().getServletContext(),
					(Long) request.getSession().getAttribute(LANGUAGE_ID),
					MasterDataConstants.MONEY_ACCOUNT_STATUSES);

			Map<Long, Object> bankAccountTypeMap = MasterDataUtil.getObjectDataMap(
					request.getSession().getServletContext(), (Long) request.getSession().getAttribute(LANGUAGE_ID),
					MasterDataConstants.BANK_ACCOUNT_TYPES);

			Map<Long, String> cardTypeMap = MasterDataUtil.getSimpleDataMap(
					request.getSession().getServletContext(), (Long) request.getSession().getAttribute(LANGUAGE_ID),
					MasterDataConstants.MD_CARD_TYPES);

			displayList = ManageAccountUtil.getDisplayAccountsForDevice(accountsService.getCardAccounts(authentication.getId()), moneyAccountStatusMap,
					bankAccountTypeMap, cardTypeMap);
			
			if(displayList.isEmpty()){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.NO_RECORDS_FOUND.getValue());
			} else {
				return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.RECORDS_FOUND.getValue(), displayList);
			}

		} catch (Exception e) {
			LOGGER.error("NO_RECORDS_FOUND", e);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.NO_RECORDS_FOUND.getValue());
		}
	}
	
	@Path("/verifycards")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyCards(@Context HttpServletRequest request, String cardInputs)throws WalletException {
		papulateServices(request);
		Authentication authentication = null;
		Response response = null;
		RestRequest restRequest = null;
		LOGGER.info(" Entering verifycards ");
		if (CommonWebserviceUtil.isEmpty(cardInputs)) {
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(cardInputs, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		try{
			//authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			
			LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE +":"+ restRequest.getUserType()
			+ " accountId:" +restRequest.getAccountId()+ " msisdn_no:" + restRequest.getMsisdnNumber() 
			+ " sim_np:" + restRequest.getSimNumber() + " imei_no:"	+ restRequest.getImeiNumber());
			
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			AccountDto dto = accountsService.getAccount(restRequest.getAccountId());
			response = CommonRestServiceHelper.isAccountBelongsToUser(request, authentication, dto);
			if(response != null){
				return response;
			}
			LOGGER.info("status of card :"+ dto.getStatus());
			if(!FundingAccountStatus.NOT_VERIFIED_STATUS.equals(dto.getStatus()) 
					& !FundingAccountStatus.PENDING_STATUS.equals(dto.getStatus())){
				return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.NOT_VERIFIED_AND_PENDING_ACCOUNT_REQUIRED.getValue());
			}
			if(FundingAccountStatus.NOT_VERIFIED_STATUS.equals(dto.getStatus())){
				dto = ManageAccountsHelper.getDtoForNotVerifedAccountStatus(request, dto, restRequest);
			} else if(FundingAccountStatus.PENDING_STATUS.equals(dto.getStatus())){	
				LOGGER.info("orderID :" + restRequest.getCode());
				dto = ManageAccountsHelper.getDtoForPendingAccountStatus(request, dto, restRequest);
	
				if(restRequest.getUserType().equalsIgnoreCase(GlobalLitterals.CUSTOMER_USER_TYPE)){
					dto.setTransactionType(WalletTransactionTypes.PENNY_DROP_FROM_CARD_CUSTOMER);	
				} else {
					dto.setTransactionType(WalletTransactionTypes.PENNY_DROP_FROM_CARD_MERCHANT);	
				}
			}
			Long defultCurrency = MasterDataUtil.getCountryCurrencyId(request.getSession().getServletContext(), 
					dto.getCountry());
			dto.setCurrency(defultCurrency);
			dto.setTypeOfRequest(MasterDataUtil.getTypeOfRequest(request.getSession().getServletContext(), 
						(Long) request.getSession().getAttribute(LANGUAGE_ID), TypeOfRequest.MOBILE.getValue()));
			AccountDto rstDto = accountsService.varifyCard(dto);
			/*Prepared display account object*/
			restRequest.setAccountId(rstDto.getId());
			restRequest.setCode(rstDto.getOrderId());
			restRequest.setAmount(rstDto.getTransactionAmount());
			restRequest.setCardNumber(ManageAccountUtil.xNumber(rstDto.getCardNumber()));
			restRequest.setExpireDateMonth(null);
			restRequest.setExpireDateYear(null);
			restRequest.setCvv(null);
			restRequest.setIsSameAsProfileAddress(null);
			restRequest.setJointAccount(null);
			
			if(HttpServiceConstants.RESPONSE_DECISION_SUCCESS.equals(rstDto.getPgResponseCode())){
				return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.CARD_VERIFIED_SUCCESSFULLY.getValue(), restRequest);
			} else {
				LOGGER.info(" PgResponseCode: " + rstDto.getPgResponseCode());
				return ServerUtility.papulatePGErrorCode(request, 
						TransactionServiceHelper.papulatePgResponseCode(rstDto.getPgResponseCode()));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			if(e.getMessage().contains(AccountsConstants.ERROR_MSG_WRONG_INFO)){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.ERROR_MSG_WRONG_CARD_INFO.getValue());
			} else {
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.CARD_VARIFICATION_FAILED.getValue());
			}
		}
	}
	
	@Path("/editcard")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response editCard(@Context HttpServletRequest request, String editCardInput) throws WalletException {
		/* get service instances */
		papulateServices(request);
		String errorMsg = null;
		Long authenticationId = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		LOGGER.info(" Entering editcard ");
		if (CommonWebserviceUtil.isEmpty(editCardInput)) {
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(editCardInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		try{
			//authentication = commonService.getAuthentication(emailId, userType);
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());		
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			authenticationId = authentication.getId();
			AccountDto dto = accountsService.getAccount(restRequest.getAccountId());
			response = CommonRestServiceHelper.isAccountBelongsToUser(request, authentication, dto);
			if(response != null){
				return response;
			}
			if(dto.getDefaultAccount()){
				return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.ERROR_MSG_UNABLE_EDIT_DEFAULT_ACCOUNT.getValue());
			}
			if(!FundingAccountStatus.NOT_VERIFIED_STATUS.equals(dto.getStatus()) && 
					!FundingAccountStatus.VERIFIED_STATUS.equals(dto.getStatus())){
				return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.NOT_VERIFIED_AND_VERIFIED_ACCOUNT_REQUIRED.getValue());
			}

			dto.setCvv(restRequest.getCvv());
			AccountDto oldAccountDto = null;
			try{
				oldAccountDto = (AccountDto)dto.clone();
			} catch (CloneNotSupportedException ex) {
				LOGGER.error(ex.getMessage(), ex);
				errorMsg = AuditTrailConstrain.AUDITTRAIL_CLONING_ERROR;
			}
			/*Populate the updated values for edit card*/
			dto.setTypeOfRequest(MasterDataUtil.getTypeOfRequest(request.getSession().getServletContext(), 
					(Long) request.getSession().getAttribute(LANGUAGE_ID), TypeOfRequest.MOBILE.getValue()));
			if(dto.getStatus().equals(FundingAccountStatus.VERIFIED_STATUS)){
				dto.setCardExpairyDate( restRequest.getExpireDateMonth() + SLASH + restRequest.getExpireDateYear() );
			} else if(dto.getStatus().equals(FundingAccountStatus.NOT_VERIFIED_STATUS)){
				dto.setAccountHolderName(restRequest.getAccountHolderName());
				dto.setCardNumber(restRequest.getCardNumber());
				dto.setCardType(restRequest.getCardType());
				dto.setCardExpairyDate(restRequest.getExpireDateMonth() + SLASH + restRequest.getExpireDateYear());
			}
			dto.setCurrencyCode(MasterDataUtil.getCountryCurrencyCode(request.getSession().getServletContext(), 
					dto.getCountry()));
			LOGGER.info("country id:::"+dto.getCountry());
			Long defultCurrency = MasterDataUtil.getCountryCurrencyId(request.getSession().getServletContext(), dto.getCountry());
			dto.setCurrency(defultCurrency);
			
			/*For mobile request need to store SIM number in IPAddress*/
			dto.setSimNumber(restRequest.getSimNumber());
			dto.setImeiNumber(restRequest.getImeiNumber());
			AccountDto rstDto = accountsService.updateAccount(dto);
			
			//Audit Trail service
			auditTrailService.createAuditTrail(authenticationId, AuditTrailConstrain.ACCOUNT_MODULE_CARD_EDIT, AuditTrailConstrain.STATUS_EDIT, 
					restRequest.getEmail(), restRequest.getUserType(), oldAccountDto, dto);

			if(HttpServiceConstants.RESPONSE_DECISION_SUCCESS.equals(rstDto.getPgResponseCode())){
				return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.CARD_UPDATED_SUCCESS_MSG.getValue(), null);
			} else {
				return ServerUtility.papulatePGErrorCode(request, 
						TransactionServiceHelper.papulatePgResponseCode(rstDto.getPgResponseCode()));
			}
		} catch(NullPointerException e){
			LOGGER.error("NullPointerException", e);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.ERROR_MSG_FAILS_TO_GET_ACCOUNT.getValue());
		} catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			if(ex.getMessage().equals(GlobalLitterals.DB_DUPLICATE_ENTRY)){
				errorMsg = ServerProcessorStatus.DUPLICATE_CARD_ACCOUNT.getValue();
			} else if(ex.getMessage().equals(ErrorCodeConstants.PG_SERVICE_IS_NOT_ESTABLISH)){
				errorMsg = ServerProcessorStatus.PG_SERVICE_IS_NOT_ESTABLISH.getValue();
			} else if(ex.getMessage().equals(ErrorCodeConstants.COMMUNICATION_WITH_PAYMENT_SYSTEM_TIMED_OUT)){
				errorMsg = ServerProcessorStatus.COMMUNICATION_WITH_PAYMENT_SYSTEM_TIMED_OUT.getValue();
			} else if(ex.getMessage().equals(AccountsConstants.ERROR_MSG_NON_EXISTS)){
				errorMsg = ServerProcessorStatus.ERROR_MSG_FAILS_TO_GET_ACCOUNT.getValue();
			} else {
				errorMsg = ServerProcessorStatus.EDIT_CARD_ERRMSG.getValue();
			}
			return ServerUtility.papulateErrorCode(request, errorMsg);
		}
	}
	
	@Path("/delete")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@Context HttpServletRequest request, String accountManagementInput) throws WalletException {
		/* get service instances */
		papulateServices(request);
		Response response = null;
		RestRequest restRequest = null;
		Authentication authentication = null;
		LOGGER.info(" Entering delete ");
		if (CommonWebserviceUtil.isEmpty(accountManagementInput)) {
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(accountManagementInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		
		Long accountId = restRequest.getAccountId();
		LOGGER.info(restRequest.getAccountId()+ USER_EMAIL + ":" + restRequest.getEmail() 
				+ USER_TYPE +":" + restRequest.getUserType()+ CARD_TYPE +":"+restRequest.getCardType());
				
		LOGGER.info("Account id :" + accountId);
		AccountDto dto = null;
		try {
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			
			//authentication = commonService.getAuthentication(emailId, userType);
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			dto = accountsService.getAccount(accountId);
			response = CommonRestServiceHelper.isAccountBelongsToUser(request, authentication, dto);
			if(response != null){
				return response;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.ERROR_MSG_FAILS_TO_GET_ACCOUNT.getValue());
		}
		
		try {
			Boolean flag = null;
			if(dto.getDefaultAccount()){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.ERROR_MSG_UNABLE_DELETE_DEFAULT_ACCOUNT.getValue());
			} else if(!FundingAccountStatus.NOT_VERIFIED_STATUS.equals(dto.getStatus()) && 
					!FundingAccountStatus.VERIFIED_STATUS.equals(dto.getStatus())){
				return ServerUtility.papulateErrorCode(request, 
							ServerProcessorStatus.ERROR_MSG_NOT_VERIFIED_AND_VERIFIED_ACCOUNT_REQUIRED.getValue());
			} else{
				flag = accountsService.delete(accountId, MasterDataUtil.getTypeOfRequest(request.getSession().getServletContext(), 
						(Long) request.getSession().getAttribute(LANGUAGE_ID), TypeOfRequest.MOBILE.getValue()));
				if(flag){
					return ServerUtility.papulateSuccessCode(request, 
							ServerProcessorStatus.ACCOUNT_DELETE_SUCCESS_MESSAGE.getValue(), null);
				} else {
					return ServerUtility.papulateErrorCode(request,
							ServerProcessorStatus.ACCOUNT_DELETE_ERROR_MESSAGE.getValue());
					}
				}
			} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.ACCOUNT_DELETE_ERROR_MESSAGE.getValue());
		}
	}
	
	@Path("/setdefault")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setDefault(@Context HttpServletRequest request, String accountManagementInput) throws WalletException {
		/* get service instances */
		papulateServices(request);
		RestRequest restRequest = null;
		Long authenticationId = null;
		Authentication authentication = null;
		LOGGER.info(" Entering setdefault ");
		if (CommonWebserviceUtil.isEmpty(accountManagementInput)) {
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(accountManagementInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		Long accountId = restRequest.getAccountId();

		LOGGER.info(restRequest.getAccountId()+ USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE + ": " + restRequest.getUserType()+CARD_TYPE +":"+restRequest.getCardType());
		AccountDto dto = null;
		try {
			//authentication = commonService.getAuthentication(emailId, userType);
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			authenticationId = authentication.getId();
			LOGGER.info("Account Id is :" + accountId);
			dto = accountsService.getAccount(accountId);
			response = CommonRestServiceHelper.isAccountBelongsToUser(request, authentication, dto);
			if(response != null){
				return response;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.ERROR_MSG_FAILS_TO_GET_ACCOUNT.getValue());
		}
		
		try {
			Boolean flag = null;
			if(dto.getDefaultAccount()){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.ERROR_MSG_ALREADY_DEFAULT_ACCOUNT.getValue());
			} else if(!FundingAccountStatus.VERIFIED_STATUS.equals(dto.getStatus())){
				return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.ERROR_MSG_VERIFIED_ACCOUNT_REQUIRED.getValue());
			} else {
				flag = accountsService.setAsDefaultAccount(authenticationId, accountId);
			}
			if(flag){
				return ServerUtility.papulateSuccessCode(request, 
					ServerProcessorStatus.ACCOUNT_SETDEFAULT_SUCCESS_MESSAGE.getValue(), null);
			} else {
				return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.ACCOUNT_SETDEFAULT_ERROR_MESSAGE.getValue());
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.ACCOUNT_SETDEFAULT_ERROR_MESSAGE.getValue());
		}				
	}
	
	@Path("/getaccountdetails")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccountDetails(@Context HttpServletRequest request, String accountManagementInput) throws WalletException {
		/* get service instances */
		papulateServices(request);
		RestRequest restRequest = null;
		Authentication authentication = null;
		AccountDto dto = null;
		LOGGER.info(" Entering getAccountDetails ");
		if (CommonWebserviceUtil.isEmpty(accountManagementInput)) {
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(accountManagementInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		Long accountId = restRequest.getAccountId();
		if(null == accountId){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.ERROR_MSG_FAILS_TO_GET_ACCOUNT.getValue());
		}
		LOGGER.info(restRequest.getAccountId()+ USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE + ": " +
				"" + restRequest.getUserType()+CARD_TYPE +":"+restRequest.getCardType());
		try {
			//authentication = commonService.getAuthentication(emailId, userType);
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}
			LOGGER.info("Account Id is :" + accountId);
			dto = accountsService.getAccount(accountId);
			History history = pGService.getHistoryByAccountId(accountId);			
			response = CommonRestServiceHelper.isAccountBelongsToUser(request, authentication, dto);
			if(response != null){
				return response;
			}
			Map<Long, String> cardTypeMap = MasterDataUtil.getSimpleDataMap(
					request.getSession().getServletContext(), (Long) request.getSession().getAttribute(LANGUAGE_ID),
					MasterDataConstants.MD_CARD_TYPES);
			dto.setCardNumberDisplay(ManageAccountUtil.xNumber(dto.getCardNumber()));
			dto.setCardName(cardTypeMap.get(dto.getCardType()));
			dto.setTransactionAmount(CommonUtil.getConvertedAmountInString(history.getAmount()));
			dto.setOrderId(history.getOrderId());
			return ServerUtility.papulateSuccessCode(request, 
					ServerProcessorStatus.RECORDS_FOUND.getValue(), dto);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.ERROR_MSG_FAILS_TO_GET_ACCOUNT.getValue());
		}
	}
	
	/**
	 * @param emailId
	 * @param userType
	 * @return
	 */
	private Address getProfileAddress(String emailId, String userType) {
		Address profileAddress = null;
		try {
			if (userType.equalsIgnoreCase(GlobalLitterals.CUSTOMER_USER_TYPE)) {
				Long longCustomerId = customerService.getCustomerId(emailId, userType);
				profileAddress = customerService.getCustomerAddress(longCustomerId);
			} else {
				Long longMerchantId = merchantService.getMerchantId(emailId, userType);
				profileAddress = merchantService.getMerchantAddress(longMerchantId);
			}
		} catch (Exception e) {
			LOGGER.error(" AddCard :: unable to retrive profile address details", e);
		}
		return profileAddress;
	}

	/**
	 * Save the card requested from device
	 * @param request
	 * @param addCardAccountForm
	 * @param profileAddress
	 * @param authId
	 * @return
	 * @throws WalletException
	 */
	private AccountDto saveCard(HttpServletRequest request,
			AddCardAccountForm addCardAccountForm, Address profileAddress,
			Long authId) throws WalletException {

		AccountDto dto = new AccountDto();
		dto = ManageAccountsHelper.getAccountDto(request, addCardAccountForm,
				profileAddress, authId, TypeOfRequest.MOBILE);
				
		AccountDto savedto = accountsService.createAccount(dto);
		savedto.setTypeOfRequest(MasterDataUtil.getTypeOfRequest(request.getSession().getServletContext(), 
				(Long) request.getSession().getAttribute(LANGUAGE_ID), TypeOfRequest.MOBILE.getValue()));
		savedto.setUserAgent(null);
		savedto.setClientIpAddress("172.30.66.144");
		savedto.setCurrencyCode(MasterDataUtil.getCountryCurrencyCode(request.getSession().getServletContext(), 
				dto.getCountry()));
		
		Long defultCurrency = MasterDataUtil.getCountryCurrencyId(request.getSession().getServletContext(), dto.getCountry());
		savedto.setCurrency(defultCurrency);
		return savedto;
	}
	
	private String accountsCountVerification(Long authId){
		String eMsg = null;
		try {
			if(authId == null){
				eMsg = ServerProcessorStatus.USER_DOES_NOT_EXIST.getValue();
			} else if(accountsService.getAccountsCount(authId) >=  utilService.getMaxFundResources()){
				eMsg = ServerProcessorStatus.ACCOUNTS_SIZE_EXCEEDS_ERROR_MESSAGE.getValue();
			}
		} catch (Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			eMsg =  ServerProcessorStatus.ACCOUNTS_SIZE_UNKNOWN_ERROR_MESSAGE.getValue();
		}
		return eMsg;
	}
	
	/**
	 * Get service instances from request object
	 * 
	 * @param request
	 */
	private void papulateServices(HttpServletRequest request) {
		auditTrailService = (AuditTrailService) ServerUtility
				.getServiceInstance(request.getSession(), AUDIT_TRAIL_SERVICE);
		accountsService = (AccountsService) ServerUtility.getServiceInstance(
				request.getSession(), ACCOUNTS_SERVICE);
		utilService = (UtilService) ServerUtility.getServiceInstance(
				request.getSession(), UTIL_SERVICE);
		commonService = (CommonService) ServerUtility.getServiceInstance(
				request.getSession(), COMMON_SERVICE);
		fraudCheckService = (FraudCheckService) ServerUtility
				.getServiceInstance(request.getSession(), FRAUDCHECK_SERVICE);
		customerService = (CustomerService) ServerUtility.getServiceInstance(
				request.getSession(), CUSTOMER_SERVICE);
		merchantService = (MerchantService) ServerUtility.getServiceInstance(
				request.getSession(), MERCHANT_SERVICE);
		pGService = (PGService) ServerUtility.getServiceInstance(
				request.getSession(), PG_SERVICE);
	}
}
