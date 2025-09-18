package com.hawk.game.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 举报过滤信息回调数据
 * 
 * @author lating
 *
 */
public class ReportingFilterCallbackData {

	private String targetPlayerId;
	
	private int reportScene;
	
	private List<Integer> reportTypeList = new ArrayList<Integer>();
	
	private List<String> picUrlList = new ArrayList<String>();
	
	public ReportingFilterCallbackData() {
		
	}
	
	public ReportingFilterCallbackData(String targetPlayerId, int reportScene, List<Integer> reportTypeList, List<String> picUrlList) {
		this.targetPlayerId = targetPlayerId;
		this.reportScene = reportScene;
		this.reportTypeList.addAll(reportTypeList);
		this.picUrlList.addAll(picUrlList);
	}

	public String getTargetPlayerId() {
		return targetPlayerId;
	}

	public void setTargetPlayerId(String targetPlayerId) {
		this.targetPlayerId = targetPlayerId;
	}

	public List<Integer> getReportTypeList() {
		return reportTypeList;
	}

	public void setReportTypeList(List<Integer> reportTypeList) {
		this.reportTypeList = reportTypeList;
	}

	public List<String> getPicUrlList() {
		return picUrlList;
	}

	public void setPicUrlList(List<String> picUrlList) {
		this.picUrlList = picUrlList;
	}

	public int getReportScene() {
		return reportScene;
	}

	public void setReportScene(int reportScene) {
		this.reportScene = reportScene;
	}
	
}
