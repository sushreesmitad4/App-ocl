package com.tarang.mwallet.rest.services.util;


/**
 * @author kedarnathd
 * This will hold the server response success and error codes.
 * This enum has to be match with ErrorCode table
 *
 */
public enum ServerProcessorStatus {

	EMPTY_DATA("M0001"), 
	USER_DOES_NOT_EXIST("M0002"), 
	FAILED_TO_LOGOUT_FROM_MOBILE_WALLET("M0003"),
	LOGIN_ERROR_INVALID_USER("M0004"),
	EMAIL_VARIFICATION_NOT_DONE("M0005"),
	USER_BLOCK("M0006"),
	USER_ACCOUNT_DELETED("M0007"),
	PASSWORD_MATCH_FAIL("M0008"),
	EMAIL_MATCH_FAIL("M0009"),
	ACCOUNT_REJECTED("M0010"),
	LOGIN_STATUS("M0011"),
	ACCOUNT_DEACTIVE("M0012"),
	EMAIL_OR_PASSWORD_FAIL("M0013"),
	JOINT_CARD_ACCOUNT_EXCEEDED("M0014"),
	JOINT_CARD_ACCOUNT_EXISTWITHAUTHID("M0015"),
	DUPLICATE_CARD_ACCOUNT("M0016"),
	CARD_FRAUD_CHECK_FAILED("M0017"),
	ERROR_MSG_FAILS_TO_GET_ACCOUNT("M0018"),
	PG_SERVICE_IS_NOT_ESTABLISH("M0019"),
	COMMUNICATION_WITH_PAYMENT_SYSTEM_TIMED_OUT("M0020"),
	CARD_SAVE_ERRMSG("M0021"),
	UNABLE_RETRIVE_WALLET_BALANCE_LIST("M0022"),
	NO_TRNASACTION_RECORD_FOUND("M0023"),
	MSISDN_NUMBER_IS_EMPTY("M0024"),
	SIM_NUMBER_IS_EMPTY("M0025"),
	IMEI_NUMBER_IS_EMPTY("M0026"),
	USER_MOBILE_DOESNOT_MATCHES_REG_NUMBER("M0027"),
	UNABLE_TO_REGISTERED_YOUR_MOBILE_WALLET("M0028"),
	SUCCESSFULLY_LOGOUT("M0029"),
	NO_RECORDS_FOUND("M0030"),
	CARD_VARIFICATION_FAILED("M0031"),
	ACCOUNT_FAILED_TO_RETRIVE("M0032"),
	ERROR_MSG_WRONG_CARD_INFO("M0033"),
	EDIT_CARD_ERRMSG("M0034"),
	SELF_TRANSFER_FAILED("M0035"),
	ERROR_USER_WALLET_NOT_ENOUGH_BALANCE("M0036"),
	ERROR_OVER_LIMIT_THRESHOLD_AMOUNT("M0037"),
	USER_DOES_NOT_HAVE_WALLET("M0038"),
	NOT_AUTHORIZED_USER("M0039"),
	NOT_ALLOWED_EMAIL_ID("M0040"),
	C_EMAIL_NOT_REGISTERED_ERR_MSG("M0041"),
	M_EMAIL_NOT_REGISTERED_ERR_MSG("M0042"),
	SELF_TRANSFERRED_TRANSACTION_SUCCESS("M0043"),
	ERROR_WHILE_GETING_WALLETS("M0044"),
	UNKNOW_ERROR("M0045"),
	USER_DELETED("M0046"),
	USER_LOCKED("M0047"),
	USER_REJECTED("M0048"),
	USER_INACTIVE("M0049"),
	USER_NOT_APPROVED("M0050"),
	EMAIL_CUSTOMER_NOTREGISTERED_ERROR("M0051"),
	EMAIL_REGISTERED_ERROR("M0052"),
	TRANSACTION_FAILED("M0053"),
	TRANSACTION_SUCCESS("M0054"),
	SENDMONEY_MERCHANT_EMAILID_NOTREGISTERD_ERRMSG("M0055"),
	EMAIL_MERCHANT_NOTREGISTERED_ERROR("M0056"),
	ERROR_MSG_ALREADY_DEFAULT_ACCOUNT("M0057"),
	ERROR_MSG_VERIFIED_ACCOUNT_REQUIRED("M0058"),
	ACCOUNT_SETDEFAULT_ERROR_MESSAGE("M0059"),
	ERROR_MSG_UNABLE_DELETE_DEFAULT_ACCOUNT("M0060"),
	ERROR_MSG_NOT_VERIFIED_AND_VERIFIED_ACCOUNT_REQUIRED("M0061"),
	ACCOUNT_DELETE_ERROR_MESSAGE("M0062"),
	CARD_REGISTER_SUCCESS_MSG("M0063"),
	EXISTING_ACCOUNT_ADDED_AGAIN("M0064"),
	CARD_UPDATED_SUCCESS_MSG("M0065"),
	ACCOUNT_DELETE_SUCCESS_MESSAGE("M0066"),
	ACCOUNT_SETDEFAULT_SUCCESS_MESSAGE("M0067"),
	ACCOUNTS_SIZE_EXCEEDS_ERROR_MESSAGE("M0068"),
	ACCOUNTS_SIZE_UNKNOWN_ERROR_MESSAGE("M0069"),
	NOT_VERIFIED_AND_PENDING_ACCOUNT_REQUIRED("M0070"),
	NOT_VERIFIED_AND_VERIFIED_ACCOUNT_REQUIRED("M0071"),
	NOT_AUTHORIZED_TRANSACTION("M0072"),
	CARD_VERIFIED_SUCCESSFULLY("M0073"),
	
	FAILEDTO_RETRIEVE_ACCOUNT_DETAILS_RELOAD_MONEY_ERRMSG("M0074"),
	THRESHOLD_DOEST_SUPPORT_FOR_RELOAD_FUND("M0075"),
	MONEY_RELOADED_SUCCESSFULLY("M0076"),
	RELOAD_FAILED_MESSAGE("M0077"),
	UNKNOWN_PG_RESPONSE("M0078"),
	ERROR_MSG_UNKNOWN_PG_RES("M0079"),
	INVALID_ACCOUNT_ID("M0080"),
	INVALID_AMOUNT_FORMAT("M0081"),
	ACCOUNT_TYPE_IS_NOT_CARD("M0082"),
	ACCOUNT_IS_NOT_VARIFIED("M0083"),
	INVALID_CVV_FORMATE("M0084"),
	USER_NOT_YET_LOGIN("M0085"),
	ERROR_MSG_INVALID_SESSION("M0086"),
	ERROR_MSG_INVALID_USER_ACCESS("M0087"),
	USER_NOT_REGISTER_AS_MOBILE_WALLET("M0088"),
	WRONG_ACCOUNT_DETAILS("M0089"),
	ERROR_MSG_UNABLE_EDIT_DEFAULT_ACCOUNT("M0090"),
	ACCOUNT_HAS_ALREADY_DELETED("M0091"),
	DUPLICATE_MOBILE_REGISTRATION("M0092"),
	EMAIL_SHOULD_NOT_BE_EMPTY("M0093"),
	USERTYPE_SHOULD_NOT_BE_EMPTY("M0094"),
	PASSWORD_SHOULD_NOT_BE_EMPTY("M0095"),
	FROM_TOWALLET_SHOULDNOTBE_EMPTY("M0096"),
	FROM_TOWALLET_SHOULDNOTBE_EMPTYSTRING("M0097"),
	FROM_TOWALLET_SHOULDNOTBE_SAME("M0098"),
	FROM_TOWALLET_COULDNOT_SUPPORT("M0099"),
	AMOUNT_SHOULDNOTBE_EMPTY("M00100"),
	NOT_A_VALID_DESTINATION_TYPE("M00101"),
	USER_NOT_HAVE_BALANCE_ERR_MSG("M00102"),
	UNABLE_TO_RETRIEVE_LISTOFCURRENCY("M00103"),
	UNABLE_TO_RETRIEVE_DESTINATIONTYPES("M00104"),
	UNABLE_TO_RETRIEVE_TYPEOFCARDS("M00105"),
	USER_AUTHENTICATION_SUCCESS("M00106"),
	RECORDS_FOUND("M00107"),
	SELF_TRANSFER_CONFORMATION_REQUIRED("M00108"),
	TRANSACTION_CONFORMATION_REQUIRED("M00109"),
	SUCCESSFULLY_REGISTERED_WITH_DEVICE("M00110"),
	ALREADY_REGISTOR_AS_MOBILE_WALLET("M00111"),
	SUCCESSFULLY_MPIN_GENERATED("M00112"),
	UNABLE_TO_GENERATE_MPIN("M00113"),
	INVALID_MPIN_FORMAT("M00114"),
	INVALID_MPIN("M00115"),
	MOBILE_USER_BLOCK("M00116"),
	MPIN_MATCH_FAIL("M00117"),
	SUCCESSFULLY_MPIN_CHANGED("M00118"),
	UNABLE_TO_DEACTIVATE_YOUR_MOBILE_WALLET("M00119"), 
	SUCCESSFULLY_DEACTIVATED_WITH_DEVICE("M00120"),
	UNABLE_TO_RETRIEVE_MPIN_HINT_QUESTIONS("M00121"),
	USER_REQUESTED_FROM_DIFFERENT_DEVICE_OR_SIM("M00122"),
	FORGOT_MPIN_FAIL("M00123"),
	FORGOT_MPIN_SUCCESS_AND_SENT_TO_EMAIL("M00124"),
	HINT_QUESTION_ONE_NOT_MATCHING("M00125"),
	HINT_ANSW_ONE_NOT_MATCHING("M00126"),
	MOBILE_WALLET_ACCOUNT_IS_NOT_ACTIVE("M00127"), 
	OLDMPIN_NEWMPIN_SHOULDNOT_BE_SAME("M00128"),
	MOBILEWALLET_ACCOUNT_EXISTS_GENERATE_MPIN("M00129"),
	UNABLE_TO_PROCESS_MSG("M00130"),
	QUERY_SENT_SUCCESSFULLY("M00131"),
	QUERY_SENT_FAILED("M00132"),
	FEEDBACK_QUERY_TYPE_NOT_SUPPORT("M00133"),
	CUSTOMER_MOBILE_EXIST("M00134"),
	CUSTOMER_EMAIL_EXIST("M00135"),
	CUSTOMER_REGISTRATION_FAILED("M00136"),
	MOBILE_CODE_SHOULD_NOT_BE_EMPTY("M00137"),
	MOBILE_NUMBER_SHOULD_NOT_BE_EMPTY("M00138"),
	CUSTOMER_MOBILE_NOT_EXIST("M00139"),
	NOT_ALLOWED_MOBILE_NUMBER("M00140"),
	PLEASE_CHECK_YOUR_INPUTS("M00141"),
	PREPAID_CARD_APPLY_FAILED("M00142"),
	PREPAID_CARD_APPLY_SUCCESS("M00143"),
	PREPAID_CARD_GOT_SUCCESSFULLY("M00144"),
	PREPAID_CARD_NOT_APPLIED("M00145"),
	SECRET_QA_SUCCESS_AND_SENT_OTP("M00146"),
	SUCCESSFULLY_OTP_SENT_TO_DEVICE("M00147"),
	FAILED_TO_SENT_OTP_TO_DEVICE("M00148"),
	INVALID_OTP_FORMAT("M00149"),
	INVALID_OTP_OR_EXPIRED_PLEASE_TRY_AGAIN("M00150"),
	CUSTOMER_MOBILE_AND_EMAIL_EXIST("M00151"),
	MERCHANT_MOBILE_NOT_EXIST("M00152"),
	FEEDBACK_SUBMITTED_SUCCESSFULLY("M00153"),
	PLEASE_ENTER_VALID_CARD_NUMBER("M00154"),
	PLEASE_CHECK_YOUR_MOBILE_NUMBER_AND_MPIN("M00155"),
	MONEY_ADDED_SUCCESSFULLY("M00156"),
	FAILED_TO_ADD_MONEY("M00157");
	
	private final String value;

	private ServerProcessorStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
}
