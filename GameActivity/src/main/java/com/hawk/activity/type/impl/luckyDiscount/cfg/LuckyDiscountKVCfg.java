package com.hawk.activity.type.impl.luckyDiscount.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/lucky_discount/luckydiscount_cfg.xml")
public class LuckyDiscountKVCfg extends HawkConfigBase {
	

	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	//# 刷新折扣消耗道具
	private final String needItem;
	private List<RewardItem.Builder> needItemList;
	
	//# 是否每日0点重置免费刷新次数
	private final int refresh;
	
	//# 折扣商城重置倒计时（秒）
	private final int resetTime;
	
	//# 每期活动前几次刷新是伪随机
	private final int pseudorandomNum;
	
	//# 每期活动伪随机的折扣组
	private final int refreshPoolRange;
	
	//# 每期活动真随机的折扣组
	private final int refreshPoolRealRange;
	
	// 折扣商城倒计时毫秒
	private long resetTimeMS;

	public LuckyDiscountKVCfg(){
		serverDelay = 0;
		refresh = 0;
		resetTime = 0;
		pseudorandomNum = 3;
		refreshPoolRange = 2;
		needItem = "";
		refreshPoolRealRange = 1;
		resetTimeMS = 3600000;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
	

	public String getNeedItem() {
		return needItem;
	}

	public List<RewardItem.Builder> getNeedItemList() {
		return needItemList;
	}

	public int getRefresh() {
		return refresh;
	}

	public long getResetTime() {
		return resetTimeMS;
	}

	public int getPseudorandomNum() {
		return pseudorandomNum;
	}

	public int getRefreshPoolRange() {
		return refreshPoolRange;
	}

	
	public int getNormalPoolRange() {
		return refreshPoolRealRange;
	}

	@Override
	protected boolean assemble() {
		resetTimeMS = resetTime * 1000;
		try {
			needItemList = RewardHelper.toRewardItemImmutableList(needItem);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}", this.getClass().getAnnotation(XmlResource.class).file());
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}
}
