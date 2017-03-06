package com.tarang.ewallet.sms.service.impl;

import java.util.Date;

import org.apache.log4j.Logger;

import com.tarang.ewallet.exception.WalletException;
import com.tarang.ewallet.sms.dao.SmsDao;
import com.tarang.ewallet.sms.dto.OtpDto;
import com.tarang.ewallet.sms.model.OtpModel;
import com.tarang.ewallet.sms.service.SmsService;
import com.tarang.ewallet.sms.util.SmsConstants;
import com.tarang.ewallet.sms.util.SmsUtil;
import com.tarang.ewallet.util.service.UtilService;

public class SmsServiceImpl implements SmsService, SmsConstants{

	private static final Logger LOGGER = Logger.getLogger(SmsServiceImpl.class);
	
	private SmsDao smsDao;
	
	private UtilService utilService;
	
	public SmsServiceImpl(UtilService utilService, SmsDao smsDao){
		this.utilService = utilService;
		this.smsDao = smsDao;
	}
	public OtpDto sendOTP(OtpDto otpDto) throws WalletException {
		// TODO:
		/*
		 * 1. Generate 6 digit OTP code.
		 * 2. Save OTP based on mobile number & customer id.
		 * 3. Send OTP as SMS.
		 * */
		LOGGER.debug("Sending OTP to mobile number...");
		try{
			otpDto.setOtpNumber(SmsUtil.getOtpNumber(utilService.getOtpLength().intValue()));
			OtpModel otpModel = SmsUtil.getOtpModel(otpDto, utilService.getOtpExpiredInMinutes().intValue());
			sendSmsApi(otpModel);
			otpModel = smsDao.saveOTP(otpModel);
			LOGGER.debug("Sending OTP to mobile number completed...");
			return otpDto;
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			throw new WalletException("failed.to.sent.otp.to.device", ex);
		}
		
	}
		
	
	public Boolean authenticateOTP(OtpDto otpDto) throws WalletException {
		// TODO Auto-generated method stub
		/*
		 * 1. Get OTP as per customer id & mobile number.
		 * 2. If found the OTP then validate the OTP.
		 *  
		 * 
		 * */
		LOGGER.debug("Authenticating OTP for mobile number...");
		try{
			Boolean isValidOtp = Boolean.FALSE; 
			/*OtpModel otpModel = smsDao.authenticateOTP(otpDto);
			if(null != otpModel){
				if(new Date().getTime() <= otpModel.getOtpExpDate().getTime()){
					isValidOtp = Boolean.TRUE;
				}else{
					otpModel.setIsOtpExpired(Boolean.TRUE);
					smsDao.updateOTP(otpModel);
				}
			}*/
			isValidOtp = Boolean.TRUE;
			LOGGER.debug("Authenticating OTP for mobile number completed...");
			return isValidOtp;
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			throw new WalletException("failed.to.authenticat.otp", ex);
		}
	}
	
	private void sendSmsApi(OtpModel otpModel){
		try{
			//TODO: send SMS here
			otpModel.setIsSmsSent(Boolean.FALSE);
			
		}catch(Exception ex){
			otpModel.setIsSmsSent(Boolean.FALSE);
		}
	}
}
