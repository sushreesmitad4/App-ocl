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
import com.tarang.ewallet.common.business.CommonService;
import com.tarang.ewallet.customer.service.CustomerService;
import com.tarang.ewallet.dto.CustomerDto;
import com.tarang.ewallet.dto.MerchantDto;
import com.tarang.ewallet.exception.WalletException;
import com.tarang.ewallet.merchant.business.MerchantService;
import com.tarang.ewallet.model.Authentication;
import com.tarang.ewallet.model.PhoneNumber;
import com.tarang.ewallet.util.GlobalLitterals;
import com.tarang.ewallet.walletui.controller.AttributeConstants;
import com.tarang.ewallet.walletui.controller.AttributeValueConstants;
import com.tarang.ewallet.walletui.form.CustomerRegFormTwo;
import com.tarang.ewallet.walletui.form.MerchantForm;
import com.tarang.ewallet.walletui.util.CustomerDataUtil;
import com.tarang.ewallet.walletui.util.MerchantDataUtil;
import com.tarang.mwallet.rest.services.model.RestRequest;
import com.tarang.mwallet.rest.services.util.CommonRestServiceHelper;
import com.tarang.mwallet.rest.services.util.CommonWebserviceUtil;
import com.tarang.mwallet.rest.services.util.Constants;
import com.tarang.mwallet.rest.services.util.ServerProcessorStatus;
import com.tarang.mwallet.rest.services.util.ServerUtility;

/**
 * @author hari.santosh
 *
 */
@Path("/profilemgmt")
public class ProfileMgmtRestService implements Constants , AttributeConstants, AttributeValueConstants, GlobalLitterals{

	private static final Logger LOGGER = Logger.getLogger(ProfileMgmtRestService.class);
	
	private CustomerService customerService;
	
	private MerchantService merchantService;
	
	private CommonService commonService;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response profileManagement(@Context HttpServletRequest request,String profileMgmtInput) 
			throws WalletException {

		papulateServices(request);
		LOGGER.info(" Entering profileManagement ");

		RestRequest restRequest = null;
		Authentication authentication = null;

		if (CommonWebserviceUtil.isEmpty(profileMgmtInput)) {
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		try {
			Gson gson = new Gson();
			restRequest = gson.fromJson(profileMgmtInput, RestRequest.class);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.EMPTY_DATA.getValue());
		}
		LOGGER.info(USER_EMAIL + ":" + restRequest.getEmail() + USER_TYPE
				+ restRequest.getUserType() + "MsisdnNumber" +restRequest.getMsisdnNumber()
				+ "simnumber" + restRequest.getSimNumber() + "ImeiNumber" + restRequest.getImeiNumber() );
		Long customerId = null;
		Long longMerchantId = null;
		String merchantId = EMPTY_STRING;
		try {
			authentication = commonService.getAuthentication(
					restRequest.getEmail(), restRequest.getUserType());
			/* validate requested user phone number */
			PhoneNumber phoneNumber = commonService.getPhoneNumber(
					restRequest.getEmail(), restRequest.getUserType());
			Response response = CommonRestServiceHelper
					.checkAutherizedUserAccess(request, restRequest,
							phoneNumber, authentication, Boolean.TRUE);
			if (response != null) {
				return response;
			}

			if (CUSTOMER_USER_TYPE.equals(restRequest.getUserType())) {
				customerId = customerService.getCustomerId(restRequest.getEmail(), restRequest.getUserType());
				CustomerDto customerDto = customerService.getCustomerDto(customerId);

				CustomerRegFormTwo customerRegFormTwoObject = CustomerDataUtil.convertCustomerDtoToCustomerForm(request,
						customerDto);
				customerRegFormTwoObject.setId(customerId);
				
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.RECORDS_FOUND.getValue(),customerRegFormTwoObject);

			} else if (MERCHANT_USER_TYPE.equals(restRequest.getUserType())) {
				longMerchantId = merchantService.getMerchantId(restRequest.getEmail(), restRequest.getUserType());

				merchantId = longMerchantId != null ? longMerchantId.toString(): EMPTY_STRING;
				MerchantDto merchantDto = merchantService.getMerchantDetailsById(Long.parseLong(merchantId));

				MerchantForm merchForm = MerchantDataUtil.convertMerchantDtoMerchantForm(request, merchantDto);
				merchForm.setId(Long.parseLong(merchantId));
				return ServerUtility.papulateSuccessCode(request,
						ServerProcessorStatus.RECORDS_FOUND.getValue(), merchForm);
			}
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.UNABLE_TO_PROCESS_MSG.getValue());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return ServerUtility.papulateErrorCode(request,
					ServerProcessorStatus.UNABLE_TO_PROCESS_MSG.getValue());
		}
	}
	
	/**
	 * Get the service instance from context
	 * @param request
	 */
	private void papulateServices(HttpServletRequest request){
		commonService = (CommonService) ServerUtility.getServiceInstance(request.getSession(), COMMON_SERVICE);
		customerService = (CustomerService) ServerUtility.getServiceInstance(request.getSession(), CUSTOMER_SERVICE);
		merchantService = (MerchantService) ServerUtility.getServiceInstance(request.getSession(), MERCHANT_SERVICE);
	}
	
}
