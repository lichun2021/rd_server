package com.hawk.activity.type.impl.urlModelEight.cfg;

import java.util.List;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * url模板活动2 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/url_model_eight/url_model_eight_cfg.xml")
public class UrlModelEightActivityKVCfg extends URLRewardBaseCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	private final String reward;

	private List<RewardItem.Builder> rewardList;
	
	public UrlModelEightActivityKVCfg() {
		reward = "";
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	@Override
	protected boolean assemble() {
		
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}", this.getClass().getAnnotation(XmlResource.class).file());
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
}
