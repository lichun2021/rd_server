package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 赛博联赛赛季段位排行奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_season_rank_award.xml")
public class CyborgSeasonRankAwardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 排名区间 */
	private final String rank;
	/** 奖励列表 */
	private final String award;

	private HawkTuple2<Integer, Integer> rankRange;

	private List<ItemInfo> rewardList;

	public CyborgSeasonRankAwardCfg() {
		id = 0;
		rank = "";
		award = "";
	}

	@Override
	protected boolean assemble() {
		rewardList = ItemInfo.valueListOf(award);
		try {
			if (rank.contains("_")) {
				String[] rankArr = rank.split("_");
				rankRange = new HawkTuple2<Integer, Integer>(Integer.valueOf(rankArr[0]), Integer.valueOf(rankArr[1]));
			} else {
				rankRange = new HawkTuple2<Integer, Integer>(Integer.valueOf(rank), Integer.valueOf(rank));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("CyborgSeasonRankAwardCfg range error, id: {}, range: {}", id, rank);
			return false;
		}
		return true;
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(award);
		if (!valid) {
			throw new InvalidParameterException(String.format("CyborgSeasonRankAwardCfg reward error, id: %s , awardPack: %s", id, award));
		}
		if (rankRange.first > rankRange.second) {
			throw new InvalidParameterException(String.format("CyborgSeasonRankAwardCfg range error, id: %s, range: %s", id, rank));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
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

}
