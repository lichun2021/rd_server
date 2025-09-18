package com.hawk.activity.type.impl.energies.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 机甲觉醒活动K-V配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/energies/energies_activity_cfg.xml")
public class EnergiesActivityKVCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 */
	private final int serverDelay;

	/** 是否每日重置(零点跨天重置) */
	private final int isDailyReset;

	/** 个人排行数量 */
	private final int personRankSize;

	/** 联盟排行数量 */
	private final int guildRankSize;

	/** 放置积分 */
	private final String summonScore;

	/** 击杀积分 */
	private final String killScore;

	/** 掉落奖励 */
	private final int extraRewardRate;

	/** 掉落概率 */
	private final String extraReward;

	/**
	 * 掉落奖励
	 */
	private List<RewardItem.Builder> extraRewardList;
	
	private Map<Integer, Integer> summonScoreMap;
	
	private Map<Integer, Integer> killScoreMap;

	/** 排行刷新周期(毫秒) */
	private final long rankPeriod;

	public EnergiesActivityKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
		personRankSize = 0;
		guildRankSize = 0;
		summonScore = "";
		killScore = "";
		extraReward = "";
		extraRewardRate = 0;
		rankPeriod = 60000;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isDailyReset() {
		return isDailyReset == 1;
	}

	public int getIsDailyReset() {
		return isDailyReset;
	}

	public int getPersonRankSize() {
		return personRankSize;
	}

	public int getGuildRankSize() {
		return guildRankSize;
	}

	public int getExtraRewardRate() {
		return extraRewardRate;
	}

	public long getRankPeriod() {
		return rankPeriod;
	}
	
	public List<RewardItem.Builder> getExtraRewardList() {
		return extraRewardList;
	}
	
	public String getExtraReward() {
		return extraReward;
	}

	@Override
	protected boolean assemble() {
		try {
			extraRewardList = RewardHelper.toRewardItemImmutableList(extraReward);
			Map<Integer, Integer> summonMap = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(summonScore)) {
				String[] strArrs = summonScore.split(",");
				for (String strArr : strArrs) {
					String[] vals = strArr.split("_");
					summonMap.put(Integer.valueOf(vals[0]), Integer.valueOf(vals[1]));
				}
			}
			summonScoreMap = summonMap;
			Map<Integer, Integer> killMap = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(killScore)) {
				String[] strArrs = killScore.split(",");
				for (String strArr : strArrs) {
					String[] vals = strArr.split("_");
					killMap.put(Integer.valueOf(vals[0]), Integer.valueOf(vals[1]));
				}
			}
			killScoreMap = killMap;
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 获取放置积分
	 * @param id
	 * @return
	 */
	public int getSummonScore(int id) {
		if (summonScoreMap.containsKey(id)) {
			return summonScoreMap.get(id);
		}
		return 0;
	}

	/**
	 * 获取击杀积分
	 * @param id
	 * @return
	 */
	public int getKillScore(int id) {
		if (killScoreMap.containsKey(id)) {
			return killScoreMap.get(id);
		}
		return 0;
	}

	@Override
	protected boolean checkValid() {
		// TODO Auto-generated method stub
		return super.checkValid();
	}

}