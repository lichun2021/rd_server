package com.hawk.activity.type.impl.commandAcademySimplify.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;


/**
 * 场景分享活动配置
 * @author huangfei -> lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/commander_college_cut/%s/commander_college_cut.xml", autoLoad=false, loadParams="284")
public class CommandAcademySimplifyStageCfg extends HawkConfigBase{
	/** 成就id*/
	@Id
	private final int stageId;
	/** 条件类型*/
	private final int order;
	/** 条件值*/
	private final int funcType;
	/** 奖励列表*/
	private final int rankId;
	
	private final int time;
	/** 注水*/
	private final String buyCountAssist;
	/** 团购人数显示最大*/
	private final int showNumLimit;
	
	private List<int[]> buyCountAssistList = new ArrayList<>();
	
	public CommandAcademySimplifyStageCfg(){
		this.stageId = 0;
		this.order =0;
		this.funcType = 0;
		this.rankId = 0;
		this.time = 0;
		buyCountAssist = "";
		showNumLimit = 0;
		
	}
	
	public CommandAcademySimplifyStageCfg(int stageId,int orderId){
		this.stageId = stageId;
		this.order =orderId;
		this.funcType = 0;
		this.rankId = 0;
		this.time = 0;
		buyCountAssist = "";
		showNumLimit = 0;
	}

	public int getStageId() {
		return stageId;
	}

	public int getOrder() {
		return order;
	}

	public int getFuncType() {
		return funcType;
	}

	public int getRankId() {
		return rankId;
	}

	public int getTime() {
		return time * 1000;
	}
	
	public String getBuyCountAssist() {
		return buyCountAssist;
	}

	public int getShowNumLimit() {
		return showNumLimit;
	}

	@Override
	protected boolean assemble() {
		buyCountAssistList = SerializeHelper.str2intList(this.buyCountAssist);
		return true;
	}

	public List<int[]> getBuyCountAssistList() {
		return buyCountAssistList;
	}
}
