package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 泰伯利亚联赛赛季排行奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tiberium_season_rank_award.xml")
public class TiberiumSeasonRankAwardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	private final int type;
	/** 排名区间 */
	private final String range;
	/** 奖励列表 */
	private final String award;
	
	private final String specialAward;

	private final int allianceFlagId;

	private HawkTuple2<Integer, Integer> rankRange;

	private List<ItemInfo> rewardList;

	public TiberiumSeasonRankAwardCfg() {
		id = 0;
		type = 0;
		range = "";
		award = "";
		specialAward = "";
		allianceFlagId = 0;
	}

	@Override
	protected boolean assemble() {
		rewardList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(specialAward)) {
			rewardList.addAll(ItemInfo.valueListOf(specialAward));
		}
		
		rewardList.addAll(ItemInfo.valueListOf(award));
		
		try {
			if (range.contains("_")) {
				String[] rankArr = range.split("_");
				rankRange = new HawkTuple2<Integer, Integer>(Integer.valueOf(rankArr[0]), Integer.valueOf(rankArr[1]));
			} else {
				rankRange = new HawkTuple2<Integer, Integer>(Integer.valueOf(range), Integer.valueOf(range));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("TiberiumSeasonRankAwardCfg range error, id: {}, range: {}", id, range);
			return false;
		}
		return true;
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(award);
		if (!valid) {
			throw new InvalidParameterException(String.format("TiberiumSeasonRankAwardCfg reward error, id: %s , award: %s", id, award));
		}
		if (rankRange.first > rankRange.second) {
			throw new InvalidParameterException(String.format("TiberiumSeasonRankAwardCfg range error, id: %s, range: %s", id, range));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public HawkTuple2<Integer, Integer> getRankRange() {
		return rankRange;
	}

	public List<ItemInfo> getRewardList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : rewardList) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public int getAllianceFlagId() {
		return allianceFlagId;
	}
}
