package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 赛博联赛赛季段位排行奖励配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_season_division.xml")
public class CyborgSeasonDivisionCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;

	/** 排名区间 */
	private final String starRange;

	/** 额外奖励列表 */
	private final String potionBonusAward;

	/** 单场联盟积分 */
	private final int cyborgSeasonGuildScore;

	/** 段位结算奖励 */
	private final String divisionAward;

	/** 星级结算 */
	private final int cyborgStarChangeRank1;

	/** 星级结算 */
	private final int cyborgStarChangeRank2;

	/** 星级结算 */
	private final int cyborgStarChangeRank3;

	/** 星级结算 */
	private final int cyborgStarChangeRank4;
	
	
	
	private final int cyborgSeasonGuildScore1;
	private final int cyborgSeasonGuildScore2;
	private final int cyborgSeasonGuildScore3;
	private final int cyborgSeasonGuildScore4;

	private HawkTuple2<Integer, Integer> starRangeTuple;

	/** 段位结算奖励 */
	private List<ItemInfo> divisionAwardList;

	/** 星级战斗额外奖励 */
	private Map<Integer, List<ItemInfo>> starExtraRewardMap;

	/** 星级变更结算 */
	private Map<Integer, Integer> starChangeMap;
	
	private Map<Integer, Integer> seasonGuildScoreMap;

	public CyborgSeasonDivisionCfg() {
		id = 0;
		starRange = "";
		potionBonusAward = "";
		cyborgSeasonGuildScore = 0;
		divisionAward = "";
		cyborgStarChangeRank1 = 0;
		cyborgStarChangeRank2 = 0;
		cyborgStarChangeRank3 = 0;
		cyborgStarChangeRank4 = 0;
		cyborgSeasonGuildScore1 = 0;
		cyborgSeasonGuildScore2 = 0;
		cyborgSeasonGuildScore3 = 0;
		cyborgSeasonGuildScore4 = 0;
		
	}

	@Override
	protected boolean assemble() {
		divisionAwardList = ItemInfo.valueListOf(divisionAward);
		Map<Integer, Integer> starChangeMap = new HashMap<>();
		starChangeMap.put(1, this.cyborgStarChangeRank1);
		starChangeMap.put(2, this.cyborgStarChangeRank2);
		starChangeMap.put(3, this.cyborgStarChangeRank3);
		starChangeMap.put(4, this.cyborgStarChangeRank4);
		this.starChangeMap = starChangeMap;
		
		Map<Integer, Integer> scoreMap = new HashMap<>();
		scoreMap.put(1, this.cyborgSeasonGuildScore1);
		scoreMap.put(2, this.cyborgSeasonGuildScore2);
		scoreMap.put(3, this.cyborgSeasonGuildScore3);
		scoreMap.put(4, this.cyborgSeasonGuildScore4);
		this.seasonGuildScoreMap = scoreMap;
		
		try {
			Map<Integer, List<ItemInfo>> starExtarRewardMap = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(potionBonusAward)) {
				String[] rewardStrs = potionBonusAward.split(",");
				if (rewardStrs.length != 4) {
					HawkLog.logPrintln("CyborgSeasonDivisionCfg potionBonusAward error, id: {}, potionBonusAward: {}", id, starRange);
					return false;
				}
				for (int i = 0; i < 4; i++) {
					starExtarRewardMap.put(i + 1, ItemInfo.valueListOf(rewardStrs[i]));
				}
			}
			this.starExtraRewardMap = starExtarRewardMap;
			if (starRange.contains("_")) {
				String[] rankArr = starRange.split("_");
				starRangeTuple = new HawkTuple2<Integer, Integer>(Integer.valueOf(rankArr[0]), Integer.valueOf(rankArr[1]));
			} else {
				starRangeTuple = new HawkTuple2<Integer, Integer>(Integer.valueOf(starRange), Integer.valueOf(starRange));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("CyborgSeasonDivisionCfg  error, id: {}, range: {}, potionBonusAward: {}", id, starRange, potionBonusAward);
			return false;
		}
		return true;
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(divisionAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("CyborgSeasonDivisionCfg reward error, id: %s , divisionAward: %s", id, divisionAward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(potionBonusAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("CyborgSeasonDivisionCfg reward error, id: %s , potionBonusAward: %s", id, potionBonusAward));
		}
		ConfigIterator<CyborgSeasonDivisionCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonDivisionCfg.class);
		int baseStar = 0;
		for (CyborgSeasonDivisionCfg cfg : its) {
			int lowStar = cfg.getStarRangeTuple().first;
			int highStar = cfg.getStarRangeTuple().second;
			if (lowStar > highStar) {
				throw new InvalidParameterException(String.format("CyborgSeasonDivisionCfg range error, id: %s, range: %s", cfg.getId(), cfg.starRange));
			}
			if (lowStar <= baseStar) {
				throw new InvalidParameterException(String.format("CyborgSeasonDivisionCfg range error, id: %s, range: %s", cfg.getId(), cfg.starRange));
			}
			baseStar = highStar;
		}
		
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public HawkTuple2<Integer, Integer> getStarRangeTuple() {
		return starRangeTuple;
	}

	/**
	 * 获取段位结算奖励
	 * 
	 * @return
	 */
	public List<ItemInfo> getDivisionAwardList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : divisionAwardList) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	/**
	 * 根据排行获取段位变化
	 * 
	 * @param rank
	 * @return
	 */
	public int getStarChangeByRank(int rank) {
		Integer change = 0;
		change = starChangeMap.get(rank);
		return change == null ? 0 : change;
	}

	/**
	 * 根据排行获取段位战斗额外奖励
	 * 
	 * @param rank
	 * @return
	 */
	public List<ItemInfo> getExtraRewardListByRank(int rank) {
		List<ItemInfo> listCopy = new ArrayList<>();
		List<ItemInfo> rewardList = starExtraRewardMap.get(rank);
		if (rewardList != null) {
			for (ItemInfo info : rewardList) {
				listCopy.add(info.clone());
			}
		}
		return listCopy;
	}

	public int getCyborgSeasonGuildScore() {
		return cyborgSeasonGuildScore;
	}

	
	
	public int getSeasonGuildScoreByRank(int rank) {
		return this.seasonGuildScoreMap.getOrDefault(rank, 0);
	}
}
