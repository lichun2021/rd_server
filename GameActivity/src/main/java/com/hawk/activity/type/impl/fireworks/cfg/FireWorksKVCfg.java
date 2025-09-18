package com.hawk.activity.type.impl.fireworks.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple;
import org.hawk.tuple.HawkTuple2;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/celebration_frieworks/celebration_frieworks_cfg.xml")
public class FireWorksKVCfg extends HawkConfigBase {

	// # 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	//免费领取烟花道具重置时间
	private final String resetTime;  //20:10_
	//免费领取烟花道具
	private final String freeItem;
	//放烟花消耗道具
	private final String consumeItem;
	// 放烟花领取道具，多个以逗号隔开
	private final String frieworksReward;
	//烟花动画时间
	private final int frieworksDuration;
	
	private List<RewardItem.Builder> frieworksRewardList;

	private HawkTuple2<Long, Long> resetTimeTuple;
	
	
	public FireWorksKVCfg() {
		serverDelay = 0;
		resetTime = "";
		freeItem = "";
		consumeItem = "";
		frieworksReward ="";
		frieworksDuration = 0;
	}

	public long getServerDelay() {
		return ((long) serverDelay) * 1000;
	}

	@Override
	protected boolean assemble() {
		try {
			frieworksRewardList = RewardHelper.toRewardItemImmutableList(frieworksReward);
			List<String> resetTimeList = SerializeHelper.stringToList(String.class, resetTime, SerializeHelper.ATTRIBUTE_SPLIT);
			String first = resetTimeList.get(0);
			String second = resetTimeList.get(1);
			List<Integer> firstList = SerializeHelper.cfgStr2List(first, SerializeHelper.COLON_ITEMS);
			List<Integer> secondList = SerializeHelper.cfgStr2List(second, SerializeHelper.COLON_ITEMS);
			long firstTime = firstList.get(0) * HawkTime.HOUR_MILLI_SECONDS + firstList.get(1) * HawkTime.MINUTE_MILLI_SECONDS;
			long secondTime = secondList.get(0) * HawkTime.HOUR_MILLI_SECONDS + secondList.get(1) * HawkTime.MINUTE_MILLI_SECONDS;
			resetTimeTuple = new HawkTuple2<Long, Long>(firstTime, secondTime);
			
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}", this.getClass().getAnnotation(XmlResource.class).file());
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}


	public String getResetTime() {
		return resetTime;
	}

	public String getFreeItem() {
		return freeItem;
	}

	public String getConsumeItem() {
		return consumeItem;
	}

	public String getFrieworksReward() {
		return frieworksReward;
	}

	public List<RewardItem.Builder> getFrieworksRewardList() {
		return frieworksRewardList;
	}

	public long getFrieworksDuration() {
		return frieworksDuration * 1000L;
	}

	public HawkTuple2<Long, Long> getResetTimeTuple() {
		return resetTimeTuple;
	}

}
