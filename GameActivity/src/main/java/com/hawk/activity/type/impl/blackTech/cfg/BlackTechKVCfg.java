package com.hawk.activity.type.impl.blackTech.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/black_tech/black_tech_cfg.xml")
public class BlackTechKVCfg extends HawkConfigBase {

	// # 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;

	// # 刷新折扣消耗道具
	private final String needItem;
	private List<RewardItem.Builder> needItemList;

	// # 是否每日0点重置免费刷新次数
	private final int refresh;

	// 每日可激活的次数
	private final int activeTimes;

	public BlackTechKVCfg() {
		serverDelay = 0;
		refresh = 0;
		needItem = "";
		activeTimes = 3;
	}

	public long getServerDelay() {
		return ((long) serverDelay) * 1000;
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

	@Override
	protected boolean assemble() {
		try {
			needItemList = RewardHelper.toRewardItemImmutableList(needItem);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}", this.getClass().getAnnotation(XmlResource.class).file());
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}

	public int getActiveTimes() {
		return activeTimes;
	}

	/**
	 * 根据刷新的次数获取刷新消耗
	 * 
	 * @param drawTimes
	 *            当前在进行第几次刷新 1, 2, 3, 4
	 * @return
	 */
	public RewardItem.Builder getCostByDrawTimes(int drawTimes) {
		if(null == needItemList || needItemList.size() == 0){
			return null;
		}
		// 免费次数
		if (drawTimes <= this.refresh) {
			return null;
		}
		// 非免费
		int costIdx = (drawTimes - this.refresh - 1);

		if (needItemList.size() > costIdx) {
			return needItemList.get(costIdx);
		} else {
			return needItemList.get(needItemList.size() - 1);
		}
	}
}
