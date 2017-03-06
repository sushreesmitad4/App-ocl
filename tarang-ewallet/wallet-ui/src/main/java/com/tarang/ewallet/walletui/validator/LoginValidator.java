package com.tarang.ewallet.walletui.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.tarang.ewallet.util.GlobalLitterals;
import com.tarang.ewallet.walletui.form.LoginUserForm;
import com.tarang.ewallet.walletui.validator.common.Common;
import com.tarang.ewallet.walletui.validator.common.CommonValidator;


public class LoginValidator implements Validator, GlobalLitterals {

	// variable names
	public static final String EMAIL_VAR = "email";
	public static final String PASSWORD_VAR = "password";

	@Override
	public boolean supports(Class<?> form) {
		return LoginUserForm.class.equals(form);

	}

	@Override
	public void validate(Object target, Errors errors) {
		
		LoginUserForm form = (LoginUserForm) target;
		form.setEmail(form.getEmail().trim());
		CommonValidator.emailValidation(errors, form.getEmail(), EMAIL_VAR);
		
		/*Password does not required password policy, 
		 * because it has to allow random generated passwords*/
		form.setPassword(form.getPassword().trim());
		if (form.getPassword().equals(EMPTY_STRING)) {
			CommonValidator.mandatoryValidator(errors, PASSWORD_VAR, Common.PASSWORD_REQUIRED);
		}
		/*else if (form.getPassword().length() < Common.PASSWORD_MIN_LENGTH ) {
			CommonValidator.mandatoryValidator(errors, PASSWORD_VAR, Common.PASSWORD_MIN_LENGTH_MSG);
		}
		else if (form.getPassword().length() > Common.PASSWORD_MAX_LENGTH ) {
			CommonValidator.mandatoryValidator(errors, PASSWORD_VAR, Common.PASSWORD_MAX_LENGTH_MSG);
		}*/ 
	}

}