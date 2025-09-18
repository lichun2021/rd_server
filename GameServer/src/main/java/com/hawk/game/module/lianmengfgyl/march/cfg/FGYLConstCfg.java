package com.hawk.game.module.lianmengfgyl.march.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

@HawkConfigManager.KVResource(file = "xml/fgyl_const.xml")
public class FGYLConstCfg extends HawkConfigBase {
	private final int serverDelay;
	private final String warTimeHour;
	private final int forceMoveBackTime;
	private final int preCountingDown;
	private final String mapSize;
	private final String rewardShow;
	private final String shopCost;
	private final String shopStartTime;
	private final int shopRefreshTime;
	private final int battleTime;
	private final int startHonor;
	private final int applyTime;
	private final int levelUp;
	private final int challengeNum;
	private final int peaceTime;
	private final int monsterAttackMax;
	private final int termRankSize;
	private final int honorRankSize;
	
	
	private long shopStartTimeValue;
	private List<HawkTuple2<Integer, Integer>> warTimelist;
	public FGYLConstCfg() {
		serverDelay = 0;
		warTimeHour = "";
		forceMoveBackTime = 0;
		preCountingDown = 0;
		mapSize = "";
		rewardShow = "";
		shopCost = "";
		shopStartTime = "";
		shopRefreshTime = 0;
		battleTime = 0;
		startHonor = 0;
		applyTime = 0;
		levelUp = 0;
		challengeNum = 0;
		peaceTime = 0;
		monsterAttackMax = 0;
		termRankSize = 10;
		honorRankSize = 10;
	}
	
	@Override
	protected boolean assemble() {
		String[] arr = this.warTimeHour.trim().split("_");
		List<HawkTuple2<Integer, Integer>> timeListTemp = new ArrayList<>();
		for(String str :arr){
			String[] trr = str.split(":");
			timeListTemp.add(HawkTuples.tuple(Integer.parseInt(trr[0]), Integer.parseInt(trr[1])));
		}
		this.warTimelist = timeListTemp;
		return super.assemble();
	}

	public String getWarTimeHour() {
		return warTimeHour;
	}

	public int getForceMoveBackTime() {
		return forceMoveBackTime;
	}

	public int getPreCountingDown() {
		return preCountingDown;
	}

	public String getMapSize() {
		return mapSize;
	}

	public String getRewardShow() {
		return rewardShow;
	}

	public String getShopCost() {
		return shopCost;
	}

	public String getShopStartTime() {
		return shopStartTime;
	}

	public int getShopRefreshTime() {
		return shopRefreshTime;
	}

	public int getBattleTime() {
		return battleTime;
	}

	public int getStartHonor() {
		return startHonor;
	}

	public int getApplyTime() {
		return applyTime;
	}

	public int getLevelUp() {
		return levelUp;
	}

	public int getChallengeNum() {
		return challengeNum;
	}

	public int getPeaceTime() {
		return peaceTime;
	}

	public int getServerDelay() {
		return serverDelay;
	}

	public int getMonsterAttackMax() {
		return monsterAttackMax;
	}

	public long getShopStartTimeValue() {
		return shopStartTimeValue;
	}
	
	
	
	public HawkTuple2<Integer, Integer> getWarTimePoint(int index) {
		if(0 <= index  && index < this.warTimelist.size()){
			return this.warTimelist.get(index);
		}
		return null;
	}
	
	public int getHonorRankSize() {
		return honorRankSize;
	}
	
	public int getTermRankSize() {
		return termRankSize;
	}
	

}
