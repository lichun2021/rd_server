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
 * 联盟锦标赛奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/championship_award.xml")
public class ChampionshipAwardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 排行奖励类型*/
	private final int section;
	/** 排名区间*/
	private final String range;
	/** 奖励列表*/
	private final String award;
	
	private HawkTuple2<Integer, Integer> rankRange;
	
	private List<ItemInfo> rewardList;
	
	public ChampionshipAwardCfg() {
		id = 0;
		section = 0;
		range = "";
		award = "";
	}
	
	@Override
	protected boolean assemble() {
		rewardList = ItemInfo.valueListOf(award);
		try {
			if (range.contains("_")) {
				String[] rankArr = range.split("_");
				rankRange = new HawkTuple2<Integer, Integer>(Integer.valueOf(rankArr[0]), Integer.valueOf(rankArr[1]));
			} else {
				rankRange = new HawkTuple2<Integer, Integer>(Integer.valueOf(range), Integer.valueOf(range));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("ChampionshipAwardCfg range error, id: {}, range: {}", id, range);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(award);
		if (!valid) {
			throw new InvalidParameterException(String.format("ChampionshipAwardCfg reward error, id: %s , award: %s", id, award));
		}
		if (rankRange.first > rankRange.second) {
			throw new InvalidParameterException(String.format("ChampionshipAwardCfg range error, id: %s, range: %s", id, range));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}


	public HawkTuple2<Integer, Integer> getRankRange() {
		return rankRange;
	}

	public int getSection() {
		return section;
	}

	public List<ItemInfo> getRewardList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for(ItemInfo info : rewardList){
			listCopy.add(info.clone());
		}
		return listCopy;
	}
	
}
