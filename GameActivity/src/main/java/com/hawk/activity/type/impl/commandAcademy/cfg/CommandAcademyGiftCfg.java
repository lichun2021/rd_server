package com.hawk.activity.type.impl.commandAcademy.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 指挥官学院活动礼包配置
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/commander_college/%s/commander_college_gfit.xml", autoLoad=false, loadParams="162")
public class CommandAcademyGiftCfg extends HawkConfigBase {
	/** 成就id*/
	@Id
	private final int id;
	/** 条件类型*/
	private final int stageId;
	/** 条件值*/
	private final int buyLimit;
	/** 奖励列表*/
	private final String price;
	
	private final String awards;
	
	private final int allianceGift;

	public CommandAcademyGiftCfg() {
		id = 0;
		stageId = 0;
		buyLimit = 0;
		price = "";
		awards = "";
		allianceGift= 0;
	}



	public int getId() {
		return id;
	}



	public int getStageId() {
		return stageId;
	}



	public int getBuyLimit() {
		return buyLimit;
	}



	public String getPrice() {
		return price;
	}



	public String getAwards() {
		return awards;
	}



	public int getAllianceGift() {
		return allianceGift;
	}



	
	
	
	


	

	
	
	
	
	
	
}
