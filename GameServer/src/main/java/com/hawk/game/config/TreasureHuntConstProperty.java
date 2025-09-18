package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkWeightFactor;

/**
 * 寻宝配置
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "xml/treasure_hunt_const.xml")
public class TreasureHuntConstProperty extends HawkConfigBase {

	/**
	 * 单例
	 */
	private static TreasureHuntConstProperty instance = null;

	/**
	 * 
	 * @return
	 */
	public static TreasureHuntConstProperty getInstance() {
		return instance;
	}

	/**
	 * 寻宝空闲点最小搜索半径
	 */
	private final int minSearchRadius;

	/**
	 * 寻宝空闲点最大搜索半径
	 */
	private final int maxSearchRadius;

	/**
	 * 寻宝权重
	 */
	private final String weight;

	/**
	 * 世界上野怪最大数量
	 */
	private final int monsterMaxCount;

	/**
	 * 一次生成野怪数量
	 */
	private final int monsterOnceCount;

	/**
	 * 世界上资源最大数量
	 */
	private final int resMaxCount;

	/**
	 * 一次生成资源数量
	 */
	private final int resOnceCount;

	/**
	 * 随机范围
	 */
	private final int randomRadius;

	/**
	 * 随机奖励
	 */
	private final String randomAward;

	/**
	 * 随机野怪
	 */
	private final String randomMonster;

	/**
	 * 随机资源
	 */
	private final String randomRes;

	/**
	 * 生成野怪(资源)最小x坐标
	 */
	private final int bornMinX;

	/**
	 * 生成野怪(资源)最大x坐标
	 */
	private final int bornMaxX;

	/**
	 * 生成野怪(资源)最小y坐标
	 */
	private final int bornMinY;

	/**
	 * 生成野怪(资源)最大y坐标
	 */
	private final int bornMaxY;

	/**
	 * 寻宝权重
	 */
	private HawkWeightFactor<Integer> weightObj;

	/**
	 * 随机奖励
	 */
	private HawkWeightFactor<Integer> randomAwardObj;

	/**
	 * 随机野怪
	 */
	private HawkWeightFactor<Integer> randomMonsterObj;

	/**
	 * 随机资源
	 */
	private HawkWeightFactor<Integer> randomResObj;

	public TreasureHuntConstProperty() {
		instance = this;

		minSearchRadius = 30;
		maxSearchRadius = 80;
		weight = "";
		monsterMaxCount = 0;
		monsterOnceCount = 0;
		resMaxCount = 0;
		resOnceCount = 0;
		randomRadius = 50;
		randomAward = "";
		randomMonster = "";
		randomRes = "";
		bornMinX = 0;
		bornMaxX = 600;
		bornMinY = 0;
		bornMaxY = 1200;
	}

	public int getMinSearchRadius() {
		return minSearchRadius;
	}

	public int getMaxSearchRadius() {
		return maxSearchRadius;
	}

	public int randomTreasureHuntType() {
		return weightObj.randomObj();
	}

	public int getMonsterMaxCount() {
		return monsterMaxCount;
	}

	public int getMonsterOnceCount() {
		return monsterOnceCount;
	}

	public int getResMaxCount() {
		return resMaxCount;
	}

	public int getResOnceCount() {
		return resOnceCount;
	}

	public int getRandomRadius() {
		return randomRadius;
	}

	public int getBornMinX() {
		return bornMinX;
	}

	public int getBornMaxX() {
		return bornMaxX;
	}

	public int getBornMinY() {
		return bornMinY;
	}

	public int getBornMaxY() {
		return bornMaxY;
	}

	public int randomRewardId() {
		return randomAwardObj.randomObj();
	}

	public int randomMonsterId() {
		return randomMonsterObj.randomObj();
	}

	public int randomResId() {
		return randomResObj.randomObj();
	}

	@Override
	protected boolean assemble() {

		HawkWeightFactor<Integer> weightObj = new HawkWeightFactor<Integer>();
		if (!HawkOSOperator.isEmptyString(weight)) {
			String[] treasureHunt = weight.split(",");
			for (String single : treasureHunt) {
				String[] split = single.split("_");
				weightObj.addWeightObj(Integer.valueOf(split[1]), Integer.valueOf(split[0]));
			}
		}
		this.weightObj = weightObj;

		HawkWeightFactor<Integer> randomMonsterObj = new HawkWeightFactor<Integer>();
		if (!HawkOSOperator.isEmptyString(randomMonster)) {
			String[] monster = randomMonster.split(",");
			for (String single : monster) {
				String[] split = single.split("_");
				randomMonsterObj.addWeightObj(Integer.valueOf(split[1]), Integer.valueOf(split[0]));
			}
		}
		this.randomMonsterObj = randomMonsterObj;

		HawkWeightFactor<Integer> randomResObj = new HawkWeightFactor<Integer>();
		if (!HawkOSOperator.isEmptyString(randomRes)) {
			String[] res = randomRes.split(",");
			for (String single : res) {
				String[] split = single.split("_");
				randomResObj.addWeightObj(Integer.valueOf(split[1]), Integer.valueOf(split[0]));
			}
		}
		this.randomResObj = randomResObj;

		HawkWeightFactor<Integer> randomAwardObj = new HawkWeightFactor<Integer>();
		if (!HawkOSOperator.isEmptyString(randomAward)) {
			String[] reward = randomAward.split(",");
			for (String single : reward) {
				String[] split = single.split("_");
				randomAwardObj.addWeightObj(Integer.valueOf(split[1]), Integer.valueOf(split[0]));
			}
		}
		this.randomAwardObj = randomAwardObj;

		return true;
	}
}
