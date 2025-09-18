package com.hawk.game.data;

/**
 * 举报中需要过滤的信息
 * 
 * @author lating
 *
 */
public class ReportingFilterInfo {
	/**
	 * 举报信息描述
	 */
	private String reportDesc;
	/**
	 * 举报信息内容
	 */
	private String reportContent;
	
	public ReportingFilterInfo() {
		
	}
	
	public ReportingFilterInfo(String reportDesc, String reportContent) {
		this.reportDesc = reportDesc;
		this.reportContent = reportContent;
	}

	public String getReportDesc() {
		return reportDesc;
	}

	public void setReportDesc(String reportDesc) {
		this.reportDesc = reportDesc;
	}

	public String getReportContent() {
		return reportContent;
	}

	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}

}
