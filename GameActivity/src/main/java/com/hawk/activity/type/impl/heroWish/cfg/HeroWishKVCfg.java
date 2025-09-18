package com.hawk.activity.type.impl.heroWish.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 装扮投放系列活动四:硝烟再起
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/hero_wish/hero_wish_cfg.xml")
public class  HeroWishKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	
	private final String wishCost;
	
	
	
	public HeroWishKVCfg(){
		serverDelay =0;
		wishCost = "";
	}
	
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	
	
	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(wishCost);
		if (!valid) {
			return false;
		}
		return super.checkValid();
	}


	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	public List<RewardItem.Builder> getWishCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.wishCost);
	}
	
		
}