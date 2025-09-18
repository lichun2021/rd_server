package com.hawk.activity.type.impl.pioneergift.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.serialize.string.SerializeHelper;


/**
 * 先锋豪礼活动奖励分组配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/pioneer_gift/pioneer_gift_layout.xml")
public class PioneerGiftActivityLayoutCfg extends HawkConfigBase {
	
	@Id
	private final int type; 
	
	private final int id;
	
	private final String combination;
	
	// 礼包档次类型

	// 礼包可选id
	private List<Integer> combinationList;
	
	public PioneerGiftActivityLayoutCfg(){
		this.id = 0;
		this.combination = "";
		this.type = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			combinationList = SerializeHelper.stringToList(Integer.class, combination, SerializeHelper.BETWEEN_ITEMS);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	public List<Integer> getPackages(){
		return this.combinationList;
	}
	
	public int getId(){
		return this.id;
	}
	
	public int getType(){
		return this.type;
	}
}
