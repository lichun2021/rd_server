package com.hawk.activity.type.impl.dressCollectionTwo.cfg;

import java.util.HashSet;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/dress_collection_new/dress_collection_new_kv_cfg.xml")
public class DressCollectionTwoKVCfg extends HawkConfigBase {
	
	// 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	// 装扮ID
	private final String dressId;
	
	// 属性ID（用于头像、头像框）
	private final String effectId;
	
	private Set<Integer> dressIdSet = new HashSet<>();
	private Set<Integer> effectIdSet = new HashSet<>();
	
	public DressCollectionTwoKVCfg(){
		serverDelay = 0;
		dressId = "";
		effectId = "";
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public Set<Integer> getDressIdSet() {
		return dressIdSet;
	}

	public Set<Integer> getEffectIdSet() {
		return effectIdSet;
	}
	
	public boolean assemble() {
		dressIdSet = SerializeHelper.stringToSet(Integer.class, dressId, ",");
		effectIdSet = SerializeHelper.stringToSet(Integer.class, effectId, ",");
		return super.assemble();
	}
	
	public boolean isValidDress(int dressId) {
		return dressIdSet.contains(dressId) || effectIdSet.contains(dressId);
	}
}
