package com.tarang.ewallet.util.service.impl;

import java.util.Properties;

import com.tarang.ewallet.util.CommonProjectUtil;
import com.tarang.ewallet.util.service.UtilService;

public class UtilServiceImpl implements UtilService {
	
	private Properties commonProperties;
	
	private String fileName;
	
	public UtilServiceImpl(String props){
		this.fileName = props;
		commonProperties = CommonProjectUtil.loadProperties(props);
	}
	
	@Override
	public void reloadProperties(){
		this.commonProperties = CommonProjectUtil.loadProperties(fileName);
	}
	
	@Override
	public String getDataSourceDriverClassName(){
		return commonProperties.getProperty("dataSource.driverClassName");	
	}
	
	@Override
	public String getDataSourceUrl(){
		return commonProperties.getProperty("dataSource.url");
	}
	
	@Override
	public String getDataSourceUserName(){
		return commonProperties.getProperty("dataSource.username");
	}
	
	@Override
	public String getDataSourcePassword(){
		return commonProperties.getProperty("dataSource.password");
	}
	
	@Override
	public String getHibernateDialect(){
		return commonProperties.getProperty("hibernate.dialect");
	}
	
	@Override
	public String getMailHost(){
		return commonProperties.getProperty("mail.host");
	}
	
	@Override
	public String getMailUserName(){
		return commonProperties.getProperty("mail.username");
	}
	
	@Override
	public String getMailPassword(){
		 return commonProperties.getProperty("mail.password");
	}
	
	@Override
	public String getMailFromEmail(){
		return commonProperties.getProperty("mail.fromEmail");
	}
	
	@Override
	public String getMailTemplateFile(){
		return commonProperties.getProperty("mail.template.file");
	}
	
	@Override
	public String getMailPropsFile(){
		return commonProperties.getProperty("mail.props.file");
	}
	
	@Override
	public String getEncryptedKey1(){
		return commonProperties.getProperty("encriptedKey1");
	}
	
	@Override
	public Integer getMaxPasswordHistory(){
		return Integer.parseInt(commonProperties.getProperty("maxPasswordHistory").trim());
	}
	
	@Override
	public String getSchedularPropsFile(){
		return commonProperties.getProperty("scheduler.props.file");
	}
	
	@Override
	public String getWalletJobsPropsFile(){
		return commonProperties.getProperty("wallet.jobs.props.file");
	}
	
	@Override
	public String getHttpServiceFile(){
		return commonProperties.getProperty("http.service.file");
	}

	@Override
	public Integer getPennyDropAmountUsd(){
		return Integer.parseInt(commonProperties.getProperty("penny.drop.amount.usd").trim());
	}
	
	@Override
	public Integer getPennyDropAmountJpy(){
		return Integer.parseInt(commonProperties.getProperty("penny.drop.amount.jpy").trim());
	}
	
	@Override
	public Integer getPennyDropAmountThb(){
		return Integer.parseInt(commonProperties.getProperty("penny.drop.amount.thb").trim());
	}
	
	@Override
	public Integer getReportLimit(){
		return Integer.parseInt(commonProperties.getProperty("report.limit").trim());
	}
	
	@Override
	public Integer getDisputeDays(){
		return Integer.parseInt(commonProperties.getProperty("dispute.days").trim());
	}
	
	@Override
	public Integer getDisputeAllowedHours(){
		return Integer.parseInt(commonProperties.getProperty("dispute.allowed.hours").trim());
	}
	
	@Override
	public Double getFeeMaxPercentage(){
		return Double.parseDouble(commonProperties.getProperty("fee.max.percentage").trim());
	}
	
	@Override
	public Double getFeeMaxFlat(){
		return Double.parseDouble(commonProperties.getProperty("fee.max.flat").trim());
	}
	
	@Override
	public Double getFeeUpperLimit(){
		return Double.parseDouble(commonProperties.getProperty("fee.upperlimit").trim());
	}
	
	@Override
	public Integer getMaxFundResources(){
		return Integer.parseInt(commonProperties.getProperty("max.fund.resources").trim());
	}
	
	@Override
	public Integer getAccountCloseDays(){
		return Integer.parseInt(commonProperties.getProperty("accountclose.days").trim());
	}

	@Override
	public String getPgFileExtention() {
		return commonProperties.getProperty("pg.file.extension");
	}

	@Override
	public String getSourceFolderLocation() {
		return commonProperties.getProperty("source.folder.location");
	}

	@Override
	public String getSuccessFileLocation() {
		return commonProperties.getProperty("success.file.location");
	}

	@Override
	public String getNullRecordsFileLocation() {
		return commonProperties.getProperty("nullrecords.file.location");
	}

	@Override
	public String getMissMathFileLocation() {
		return commonProperties.getProperty("missmatch.file.location");
	}

	@Override
	public String getCurruptedFileLocation() {
		return commonProperties.getProperty("currupted.file.location");
	}

	@Override
	public String getFailureFileLocation() {
		return commonProperties.getProperty("failure.file.location");
	}
	@Override
	public Integer getRegistrationAllowedHours() {
		return Integer.parseInt(commonProperties.getProperty("registration.allowed.hours").trim());
	}

   @Override
	public Integer getRegistrationAllowedAccounts() {
		return Integer.parseInt(commonProperties.getProperty("registration.allowed.accounts").trim());
	}
	
	@Override
	public Integer getNumberOfDisputes() {
		return Integer.parseInt(commonProperties.getProperty("fraud.disputes").trim());
	}
	
	@Override
	public Integer getBankAllowedHours() {
		return Integer.parseInt(commonProperties.getProperty("bank.allowed.hours").trim());
	}
	
	@Override
	public Integer getBankAllowedAccounts() {
		return Integer.parseInt(commonProperties.getProperty("bank.allowed.accounts").trim());
	}
	
	@Override
	public Integer getCardAllowedHours() {
		return Integer.parseInt(commonProperties.getProperty("card.allowed.hours").trim());
	}
	
	@Override
	public Integer getCardAllowedAccounts() {
		return Integer.parseInt(commonProperties.getProperty("card.allowed.accounts").trim());
	}
	
	@Override
	public Integer getDefaultLastNTransactions(){
		return Integer.parseInt(commonProperties.getProperty("default.last.n.transactions").trim());
	}
	
	@Override
	public Integer getEmailPatternAllowedHours() {
		return Integer.parseInt(commonProperties.getProperty("email.pattern.allowed.hours").trim());
	}
	
	@Override
	public Integer getEmailPatternAllowedAccounts() {
		return Integer.parseInt(commonProperties.getProperty("email.pattern.allowed.accounts").trim());
	}
	
	@Override
	public Integer getDormantAccountInterval(){
		return Integer.parseInt(commonProperties.getProperty("dormant.account.interval").trim());
	}
	
	@Override
	public Integer getSendMoneyFileUploadRecordLimit(){
		return Integer.parseInt(commonProperties.getProperty("fileupload.record.limit").trim());
	}
	@Override
	public Integer getLoginAttemptsLimit() {
		return Integer.parseInt(commonProperties.getProperty("login.attempts.limit").trim());
	}

	@Override
	public Integer getCancelNonregWalletTxnsAllowedDays() {
		return Integer.parseInt(commonProperties.getProperty("cancel.nonreg.wallet.txns.allowed.days").trim());
	}

	@Override
	public Integer getDaysForPendingAccountClosers() {
		return Integer.parseInt(commonProperties.getProperty("days.for.pending.account.closers").trim());
	}

	@Override
	public String getMessageForPendingAccountClosers() {
		return commonProperties.getProperty("message.for.pending.account.closers");
	}

	@Override
	public Integer getJointBankAccountsLimit() {
		return Integer.parseInt(commonProperties.getProperty("joint.account.bank.limit").trim());
	}

	@Override
	public Integer getJointCardAccountsLimit() {
		return Integer.parseInt(commonProperties.getProperty("joint.account.card.limit").trim());
	}

	@Override
	public Integer getEmailPatternLength() {
		return Integer.parseInt(commonProperties.getProperty("email.pattern.length").trim());
	}

	@Override
	public String getUploadImageFileExtenstion() {
		return commonProperties.getProperty("file.upload.brand.extension").trim();
	}

	@Override
	public Integer getUploadImageFileSize() {
		return Integer.parseInt(commonProperties.getProperty("file.upload.brand.size").trim());
	}

	@Override
	public String getUploadImageFileLocation() {
		return commonProperties.getProperty("file.upload.brand.location").trim();
	}

	@Override
	public String getUploadImageRelativeLocation() {
		return commonProperties.getProperty("file.upload.brand.relativepath").trim();
	}

	@Override
	public Integer getReportDays() {
		return Integer.parseInt(commonProperties.getProperty("default.reports.past.days").trim());
	}

	@Override
	public String getDefaultImageFileName() {
		return commonProperties.getProperty("default.merchant.logo").trim();
	}

	@Override
	public Integer getOtpExpiredInMinutes() {
		return Integer.valueOf(commonProperties.getProperty("otp.expired.in.minutes").trim());
		
	}

	@Override
	public Integer getOtpLength() {
		return Integer.valueOf(commonProperties.getProperty("otp.length").trim());
	}
}
