package com.tarang.ewallet.transaction.util;

import com.tarang.ewallet.dto.VelocityAndThresholdDto;
import com.tarang.ewallet.dto.WalletThresholdDto;
import com.tarang.ewallet.model.VelocityAndThreshold;
import com.tarang.ewallet.model.WalletThreshold;


public class VelocityAndThresholdUtil {

public static void prepareVelocityAndThreshold(VelocityAndThresholdDto velocityDto, VelocityAndThreshold velocity) {
		velocity.setId(velocityDto.getId());
		velocity.setCountry(velocityDto.getCountry());
		velocity.setCurrency(velocityDto.getCurrency());
		velocity.setTransactiontype(velocityDto.getTransactiontype());
		velocity.setMinimumamount(velocityDto.getMinimumamount());
		velocity.setMaximumamount(velocityDto.getMaximumamount());
		velocity.setUserType(velocityDto.getUserType());
	}
public static void prepareWalletThreshold(WalletThresholdDto walletThresholdDto, WalletThreshold walletThreshold) {
	walletThreshold.setId(walletThresholdDto.getId());
	walletThreshold.setCountry(walletThresholdDto.getCountry());
	walletThreshold.setCurrency(walletThresholdDto.getCurrency());
	walletThreshold.setMaximumamount(walletThresholdDto.getMaximumamount());
	
}
}
