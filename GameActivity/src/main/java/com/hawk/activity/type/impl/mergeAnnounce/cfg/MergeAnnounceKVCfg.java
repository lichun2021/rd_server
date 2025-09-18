package com.hawk.activity.type.impl.mergeAnnounce.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 可拆分和服通告
 * @author che
 */
@HawkConfigManager.KVResource(file = "activity/merge_announce/merge_announce_cfg.xml")
public class MergeAnnounceKVCfg extends HawkConfigBase {
	
	private final int noticeTime;
	
	
	
	public MergeAnnounceKVCfg(){
		noticeTime =0;
	}
	
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	
	
	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}


	public int getNoticeTime() {
		return noticeTime;
	}
}