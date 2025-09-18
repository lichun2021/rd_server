package com.hawk.game.module.spacemecha.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;

/**
 * 星甲召唤舱体等级配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/space_mechine_level.xml")
public class SpaceMechaLevelCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 主舱
	 */
	protected final int cabin;
	/**
	 * 子舱
	 */
	protected final int subcabin;
	/**
	 * 阶段1进攻波次
	 */
	protected final int stage1Wave;
	/**
	 * 阶段1进攻时间间隔（是一个差值）
	 */
	protected final int stage1WaveCd;
	/**
	 * 刷怪物点的时间间隔（是一个差值）
	 */
	protected final int stage1RefreshCd;
	/**
	 * 阶段1普通怪兵力配置
	 */
	protected final String stage1Enemy;
	/**
	 * 阶段1普通怪每个波次输出怪的数量配置
	 */
	protected final String stage1EnemyNum;
	/**
	 * 阶段1精英怪兵力配置
	 */
	protected final String stage1SpEnemy;
	/**
	 * 阶段1精英怪每个波次输出怪的数量配置
	 */
	protected final String stage1SpEnemyNum;
	/**
	 * 两个子舱分别发多少支敌军进攻部队
	 */
	protected final String stage1AtkSubNum;
	/**
	 * 阶段2刷出据点的数量配置
	 */
	protected final String stage2Stronghold;
	/**
	 * 阶段3 boss对应的兵力配置
	 */
	protected final int stage3Boss;
	/**
	 * 阶段4对应的箱子
	 */
	protected final int stage4box;
	/**
	 * 阶段4对应的箱子掉落个数随机值
	 */
	protected final String stage4boxNum;
	
	protected final int damagePara;
	
	/**
	 * 普通怪波次-怪数量关系map
	 */
	private Map<Integer, Integer> stage1WaveEnemyNumMap = new HashMap<>();
	/**
	 * 普通怪敌军id和权重
	 */
	private List<Integer> stage1EnemyIdList = new ArrayList<>();
	private List<Integer> stage1EnemyWeightList = new ArrayList<>();
	/**
	 * 精英怪波次-怪数量关系map
	 */
	private Map<Integer, Integer> stage1WaveSpEnemyNumMap = new HashMap<>();
	private int spEnemyWaveMin = Integer.MAX_VALUE;
	
	/**
	 * 精英怪敌军id和权重
	 */
	private List<Integer> stage1SpEnemyIdList = new ArrayList<>();
	private List<Integer> stage1SpEnemyWeightList = new ArrayList<>();
	
	private HawkTuple2<Integer, Integer> stage2HoldTuple;
	private HawkTuple2<Integer, Integer> stage2SpHoldTuple;
	
	private int[] stage4boxNumMinMax = new int[2];
	private int[] atkSlaveSpaceEnemyNum = new int[2];
	
	static int maxLevel = 0;

	public SpaceMechaLevelCfg() {
		id = 0;
		cabin = 0;
		subcabin = 0;
		stage1Wave = 0;
		stage1WaveCd = 0;
		stage1Enemy = "";
		stage1EnemyNum = "";
		stage1SpEnemy = "";
		stage1SpEnemyNum = "";
		stage2Stronghold = "";
		stage3Boss = 0;
		stage4box = 0;
		stage4boxNum = "";
		stage1AtkSubNum = "";
		stage1RefreshCd = 0;
		damagePara = 0;
	}

	public int getId() {
		return id;
	}

	public int getCabin() {
		return cabin;
	}

	public int getSubcabin() {
		return subcabin;
	}

	public int getStage1Wave() {
		return stage1Wave;
	}

	public long getStage1WaveCd() {
		return stage1WaveCd * 1000L;
	}

	public String getStage1Enemy() {
		return stage1Enemy;
	}

	public String getStage1EnemyNum() {
		return stage1EnemyNum;
	}

	public String getStage1SpEnemy() {
		return stage1SpEnemy;
	}

	public String getStage1SpEnemyNum() {
		return stage1SpEnemyNum;
	}

	public String getStage2Stronghold() {
		return stage2Stronghold;
	}

	public int getStage3Boss() {
		return stage3Boss;
	}

	public int getStage4box() {
		return stage4box;
	}

	public String getStage4boxNum() {
		return stage4boxNum;
	}
	
	public long getStage1RefreshCd() {
		return stage1RefreshCd * 1000L;
	}
	
	public int getDamagePara() {
		return damagePara;
	}
	
	public boolean assemble() {
		if (id > maxLevel) {
			maxLevel = id;
		}
		String[] enemyArray = stage1Enemy.split(",");
		for (String enemy : enemyArray) {
			String[] idWeight = enemy.split("_");
			stage1EnemyIdList.add(Integer.parseInt(idWeight[0]));
			stage1EnemyWeightList.add(Integer.parseInt(idWeight[1]));
		}
		
		String[] enemyNumArray = stage1EnemyNum.split("\\|");
		for (String enemyNum : enemyNumArray) {
			String[] enemyNumSplit = enemyNum.split("_");
			stage1WaveEnemyNumMap.put(Integer.parseInt(enemyNumSplit[0]), Integer.parseInt(enemyNumSplit[1]));
		}
		
		String[] spEnemyArray = stage1SpEnemy.split(",");
		for (String enemy : spEnemyArray) {
			String[] idWeight = enemy.split("_");
			stage1SpEnemyIdList.add(Integer.parseInt(idWeight[0]));
			stage1SpEnemyWeightList.add(Integer.parseInt(idWeight[1]));
		}
		
		String[] spEnemyNumArray = stage1SpEnemyNum.split("\\|");
		for (String enemyNum : spEnemyNumArray) {
			String[] enemyNumSplit = enemyNum.split("_");
			int wave = Integer.parseInt(enemyNumSplit[0]); 
			spEnemyWaveMin = Math.min(wave, spEnemyWaveMin);
			stage1WaveSpEnemyNumMap.put(wave, Integer.parseInt(enemyNumSplit[1]));
		}
		
		String[] holdNumArray = stage2Stronghold.split(",");
		if (holdNumArray.length < 2) {
			return false;
		}
		
		String[] holdNum = holdNumArray[0].split("_");
		stage2HoldTuple = new HawkTuple2<>(Integer.parseInt(holdNum[0]), Integer.parseInt(holdNum[1]));
		String[] spHoldNum = holdNumArray[1].split("_");
		stage2SpHoldTuple = new HawkTuple2<>(Integer.parseInt(spHoldNum[0]), Integer.parseInt(spHoldNum[1]));
		
		String[] minMax = stage4boxNum.split(",");
		stage4boxNumMinMax[0] = Integer.parseInt(minMax[0]);
		stage4boxNumMinMax[1] = Integer.parseInt(minMax[1]);
		
		String[] split = stage1AtkSubNum.split("_");
		atkSlaveSpaceEnemyNum[0] = Integer.parseInt(split[0]);
		atkSlaveSpaceEnemyNum[1] = Integer.parseInt(split[1]);
		
		return true;
	}
	
	public List<Integer> getStage1EnemyIdList() {
		return stage1EnemyIdList;
	}
	
	public List<Integer> getStage1EnemyWeightList() {
		return stage1EnemyWeightList;
	}
	
	public int getEnemyNumByWave(int wave) {
		return stage1WaveEnemyNumMap.getOrDefault(wave, 0);
	}
	
	public List<Integer> getStage1SpEnemyIdList() {
		return stage1SpEnemyIdList;
	}
	
	public List<Integer> getStage1SpEnemyWeightList() {
		return stage1SpEnemyWeightList;
	}
	
	public int getSpEnemyNumByWave(int wave) {
		return stage1WaveSpEnemyNumMap.getOrDefault(wave, 0);
	}
	
	public HawkTuple2<Integer, Integer> getStage2HoldNum() {
		return stage2HoldTuple;
	}
	
	public HawkTuple2<Integer, Integer> getStage2SpHoldNum() {
		return stage2SpHoldTuple;
	}
	
	public int[] getStage4boxNumMinMax() {
		return stage4boxNumMinMax;
	}
	
	public int[] getAtkSlaveSpaceEnemyNum() {
		return atkSlaveSpaceEnemyNum;
	}
	
	public int getSpEnemyWaveMin() {
		return spEnemyWaveMin;
	}
	
	public static int getMaxLevel() {
		return maxLevel;
	}
}
