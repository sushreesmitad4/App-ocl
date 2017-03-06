package com.tarang.mwallet.rest.services;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.google.gson.Gson;
import com.tarang.ewallet.common.business.CommonService;
import com.tarang.ewallet.customer.service.CustomerService;
import com.tarang.ewallet.dto.CustomerDto;
import com.tarang.ewallet.exception.WalletException;
import com.tarang.ewallet.model.Authentication;
import com.tarang.ewallet.model.PhoneNumber;
import com.tarang.ewallet.walletui.controller.AttributeConstants;
import com.tarang.ewallet.walletui.controller.AttributeValueConstants;
import com.tarang.ewallet.walletui.util.MasterDataConstants;
import com.tarang.ewallet.walletui.util.MasterDataUtil;
import com.tarang.mwallet.rest.services.model.RestRequest;
import com.tarang.mwallet.rest.services.util.CommonRestServiceHelper;
import com.tarang.mwallet.rest.services.util.CommonWebserviceUtil;
import com.tarang.mwallet.rest.services.util.Constants;
import com.tarang.mwallet.rest.services.util.ServerProcessorStatus;
import com.tarang.mwallet.rest.services.util.ServerUtility;

import net.sf.json.JSONObject;

/**
 * @author kedarnathd
 * This rest service will hold the list of request methods for master data population at device end
 */
@Path("/masterdatarestservice")
public class MasterDataRestService implements AttributeConstants, AttributeValueConstants, Constants {
	
	private static final Logger LOGGER = Logger.getLogger(MasterDataRestService.class);
	
	private CustomerService customerService;
	
	private CommonService commonService;
	
	@Path("/listofcurrency")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listOfCurrency(@Context HttpServletRequest request,	String listOfCurrencyInput) throws WalletException {
		
		LOGGER.info(" Entering listofcurrency ");
		/* get service instances */
		papulateServices(request);
		if(CommonWebserviceUtil.isEmpty(listOfCurrencyInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		Authentication authentication = null;
		RestRequest	restRequest = null;
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(listOfCurrencyInput, RestRequest.class);
		} catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL +":" + restRequest.getEmail() + USER_TYPE +":" + restRequest.getUserType());
		
		try {
			//authentication = commonService.getAuthentication(restRequest.getEmail(), restRequest.getUserType());
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), 
					restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			}else{
				Long authenticationId = authentication.getId();
				LOGGER.info(AUTHENTICATION_ID + ":" + authenticationId);
				List<JSONObject> jsonList = new ArrayList<JSONObject>();
				JSONObject currencyJson = null;

				Map<Long, String> currencyMap = MasterDataUtil.getCurrencyNames(
					request.getSession().getServletContext(), 
					(Long) request.getSession().getAttribute(LANGUAGE_ID));
				
				for (Map.Entry<Long, String> entry : currencyMap.entrySet()){
					currencyJson = new JSONObject();
					currencyJson.put(CURRENCY_ID, entry.getKey());    
					currencyJson.put(CURRENCY_CODE, entry.getValue());
					jsonList.add(currencyJson);
				}
				return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.RECORDS_FOUND.getValue(), jsonList);  
			}
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(),ex);
		   return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.UNABLE_TO_RETRIEVE_LISTOFCURRENCY.getValue());
		}
    }
	
	@Path("/destinationtypes")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listOfDestinationTypes(@Context HttpServletRequest request,	String destinationTypesInput) throws WalletException {
		LOGGER.info(" Entering listOfDestinationTypes");
		if(CommonWebserviceUtil.isEmpty(destinationTypesInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		Authentication authentication = null;
		RestRequest	restRequest = null;
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(destinationTypesInput, RestRequest.class);
		} catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL +":" + restRequest.getEmail() + USER_TYPE +":"+ restRequest.getUserType());
		
		/* get service instances */
		papulateServices(request);
		try {
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), 
					restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			} else{
				Long authenticationId = authentication.getId();
				LOGGER.info(AUTHENTICATION_ID +":" + authenticationId);
				List<JSONObject> jsonList = new ArrayList<JSONObject>();
				JSONObject destinationTypesJson = null;
				
				Map<Long,String> userTypes = MasterDataUtil.getSimpleDataMap(
						request.getSession().getServletContext(), 
						(Long) request.getSession().getAttribute(LANGUAGE_ID),
						MasterDataConstants.MD_USER_TYPES);
				
			    for(Map.Entry<Long, String> entry : userTypes.entrySet()){
					if(!(entry.getKey().equals(ADMIN_USER_TYPE_ID))){
						destinationTypesJson = new JSONObject();
						destinationTypesJson.put("destinationTypeId", entry.getKey());
						destinationTypesJson.put("destinationTypeName", entry.getValue());
						jsonList.add(destinationTypesJson);
					}
				}
				return ServerUtility.papulateSuccessCode(request, 
							ServerProcessorStatus.RECORDS_FOUND.getValue(), jsonList);  
			}
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(),ex);
			   return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.UNABLE_TO_RETRIEVE_DESTINATIONTYPES.getValue());
		}
	}
	
	@Path("/typesofcards")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listOfTypesOfCards(@Context HttpServletRequest request, String typesOfCardsInput) throws WalletException {
		LOGGER.info(" Entering listOfTypesOfCards");
		if(CommonWebserviceUtil.isEmpty(typesOfCardsInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		Authentication authentication = null;
		RestRequest	restRequest = null;
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(typesOfCardsInput, RestRequest.class);
		} catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL +":" + restRequest.getEmail() + USER_TYPE +":"+ restRequest.getUserType());
		/* get service instances */
		papulateServices(request);
		
		try {
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), 
					restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			} else{
				List<JSONObject> jsonList = new ArrayList<JSONObject>();
				JSONObject cardtypesjson = null;
				Long authenticationId = authentication.getId();
				LOGGER.info(AUTHENTICATION_ID +":"  + authenticationId);
				
				LOGGER.info( " getCardTypes " );
				Map<Long, String> cardTypesMap = MasterDataUtil.getSimpleDataMap(
						request.getSession().getServletContext(), 
						(Long) request.getSession().getAttribute(LANGUAGE_ID), 
						MasterDataUtil.MD_CARD_TYPES);
				for(Map.Entry<Long, String> entry : cardTypesMap.entrySet()){
					cardtypesjson = new JSONObject();
					cardtypesjson.put("cardTypeId", entry.getKey());
					cardtypesjson.put("cardTypeName", entry.getValue());
					jsonList.add(cardtypesjson);
				}
				return ServerUtility.papulateSuccessCode(request, 
							ServerProcessorStatus.RECORDS_FOUND.getValue(), jsonList); 
				}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			   return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.UNABLE_TO_RETRIEVE_TYPEOFCARDS.getValue());
		}
	}
	
	@Path("/hintquestions")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listOfHintQuestions(@Context HttpServletRequest request, String hintQuestionsInput) throws WalletException {
		LOGGER.info(" Entering listOfHintQuestions");
		if(CommonWebserviceUtil.isEmpty(hintQuestionsInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		RestRequest	restRequest = null;
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(hintQuestionsInput, RestRequest.class);
		} catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(SIM_NUMBER + ":" + restRequest.getSimNumber() + IMEI_NUMBER + ":" + restRequest.getImeiNumber() + 
				LANGUAGE_ID + ":" +restRequest.getLanguageId());
		/* get service instances */
		papulateServices(request);
		try {
			List<JSONObject> jsonList = new ArrayList<JSONObject>();
			JSONObject hintJson = null;
			
			Map<Long, String> hintQuestionsMap = MasterDataUtil.getSimpleDataMap(
					request.getSession().getServletContext(), (Long) request
					.getSession().getAttribute(LANGUAGE_ID), MasterDataConstants.HINT_QUESTIONS);
			for(Map.Entry<Long, String> entry : hintQuestionsMap.entrySet()){
				hintJson = new JSONObject();
				hintJson.put("hintQuestionId", entry.getKey());
				hintJson.put("hintQuestionName", entry.getValue());
				jsonList.add(hintJson);
			}
			return ServerUtility.papulateSuccessCode(request, 
						ServerProcessorStatus.RECORDS_FOUND.getValue(), jsonList); 
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			   return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.UNABLE_TO_RETRIEVE_MPIN_HINT_QUESTIONS.getValue());
		}
	}
	
	@Path("/destinationtypesandcurrency")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listOfDestinationTypesAndCurrency(@Context HttpServletRequest request,	String destinationTypesInput) throws WalletException {
		LOGGER.info(" Entering listOfDestinationTypesAndCurrency");
		if(CommonWebserviceUtil.isEmpty(destinationTypesInput)){
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		Authentication authentication = null;
		RestRequest	restRequest = null;
		try{
			Gson gson = new Gson();
			restRequest = gson.fromJson(destinationTypesInput, RestRequest.class);
		} catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request, 
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		
		LOGGER.info(USER_EMAIL +":" + restRequest.getEmail() + USER_TYPE +":"+ restRequest.getUserType());
		/* get service instances */
		papulateServices(request);
		
		try {
			CustomerDto customerDto = customerService.getCustomerByPhoneNo(restRequest.getPhoneCode(), restRequest.getPhoneNo());
			authentication = commonService.getAuthentication(customerDto.getEmailId(), restRequest.getUserType());
			restRequest.setEmail(authentication.getEmailId());
			PhoneNumber phoneNumber = commonService.getPhoneNumber(restRequest.getEmail(), 
					restRequest.getUserType());
			Response response = CommonRestServiceHelper.checkAutherizedUserAccess(request, restRequest, 
					phoneNumber, authentication, Boolean.TRUE);
			if(response != null){
				return response;
			} else{
				Long authenticationId = authentication.getId();
				LOGGER.info(AUTHENTICATION_ID +":" + authenticationId);
				List<JSONObject> jsonList1 = new ArrayList<JSONObject>();
				List<JSONObject> jsonList2 = new ArrayList<JSONObject>();
				JSONObject destinationTypesJson = null;
				
				Map<Long,String> userTypes = MasterDataUtil.getSimpleDataMap(
						request.getSession().getServletContext(), 
						(Long) request.getSession().getAttribute(LANGUAGE_ID),
						MasterDataConstants.MD_USER_TYPES);
				
			    for(Map.Entry<Long, String> entry : userTypes.entrySet()){
					if(!(entry.getKey().equals(ADMIN_USER_TYPE_ID))){
						destinationTypesJson = new JSONObject();
						destinationTypesJson.put("destinationTypeId", entry.getKey());
						destinationTypesJson.put("destinationTypeName", entry.getValue());
						jsonList1.add(destinationTypesJson);
					}
				}
			    
			    Map<Long, String> currencyMap = MasterDataUtil.getCurrencyNames(
						request.getSession().getServletContext(), 
						(Long) request.getSession().getAttribute(LANGUAGE_ID));
			    JSONObject currencyJson = null;	
				for (Map.Entry<Long, String> entry : currencyMap.entrySet()){
					currencyJson = new JSONObject();
					currencyJson.put(CURRENCY_ID, entry.getKey());    
					currencyJson.put(CURRENCY_CODE, entry.getValue());
					jsonList2.add(currencyJson);
				}
				Map<String, List<JSONObject>> jsonMap = new HashMap<String, List<JSONObject>>();
				jsonMap.put(CURRENCY_LIST, jsonList2);
				jsonMap.put(DESTINATION_TYPE_LIST, jsonList1);
				
				return ServerUtility.papulateSuccessCode(request, 
							ServerProcessorStatus.RECORDS_FOUND.getValue(), jsonMap);  
			}
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(),ex);
			   return ServerUtility.papulateErrorCode(request, 
						ServerProcessorStatus.UNABLE_TO_RETRIEVE_DESTINATIONTYPES.getValue());
		}
	}
	
	/**
	 * Get service instances from request object
	 * 
	 * @param request
	 */
	private void papulateServices(HttpServletRequest request) {
		commonService = (CommonService) ServerUtility.getServiceInstance(
				request.getSession(), COMMON_SERVICE);
		customerService = (CustomerService) ServerUtility.getServiceInstance(
				request.getSession(), CUSTOMER_SERVICE);
	}
	
}
