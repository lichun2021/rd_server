package com.hawk.activity.type.impl.radiationWarTwo.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 新版辐射战争2活动K-V配置
 */
@HawkConfigManager.KVResource(file = "activity/radiation_war_two/radiation_war_two_cfg.xml")
public class RadiationWarTwoActivityKVCfg extends HawkConfigBase {
	
	//** 服务器开服延时开启活动时间*//*
	private final int serverDelay;
	
	private final String itemIdTab;
	
	private final String itemIdTabOld;
	
	private ImmutableList<Integer> itemIdTabList = ImmutableList.of();
	private ImmutableList<Integer> itemIdTabOldList = ImmutableList.of();
	
	
	public RadiationWarTwoActivityKVCfg() {
		serverDelay = 0;
		itemIdTab = "";
		itemIdTabOld = "";
	}
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(itemIdTab)){
			List<Integer> list = SerializeHelper.stringToList(Integer.class, this.itemIdTab,SerializeHelper.ATTRIBUTE_SPLIT);
			this.itemIdTabList = ImmutableList.copyOf(list);
		}
		if(!HawkOSOperator.isEmptyString(itemIdTabOld)){
			List<Integer> list = SerializeHelper.stringToList(Integer.class, this.itemIdTabOld,SerializeHelper.ATTRIBUTE_SPLIT);
			this.itemIdTabOldList = ImmutableList.copyOf(list);
		}
		return super.assemble();
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public List<Integer> getOldBossItemList() {
		return this.itemIdTabOldList;
	}
	
	
	public List<Integer> getNewBossItemList() {
		return this.itemIdTabList;
	}
	
	
	
}