package com.hawk.activity.type.impl.commandAcademySimplify.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 指挥官学院活动礼包配置
 * @author huangfei -> lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/commander_college_cut/%s/commander_college_cut_gfit.xml", autoLoad=false, loadParams="284")
public class CommandAcademySimplifyGiftCfg extends HawkConfigBase {
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

	public CommandAcademySimplifyGiftCfg() {
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
