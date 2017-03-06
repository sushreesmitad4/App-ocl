/**
 * 
 */
package com.tarang.ewallet.prepaidcard.repository;

import com.tarang.ewallet.dto.PrepaidCardDto;
import com.tarang.ewallet.exception.WalletException;


/**
 * @author  : kedarnathd
 * @date    : July 26, 2016
 * @time    : 4:02:16 PM
 * @version : 1.0.0
 * @comments: 
 *
 */
public interface PrepaidCardRepository {

	PrepaidCardDto createPrepaidCard(PrepaidCardDto prepaidCardDto) throws WalletException;
	
	PrepaidCardDto getPrepaidCard(Long customerId) throws WalletException;

	
}
