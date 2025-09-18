package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkWeightFactor;

import com.google.common.base.Splitter;
import com.hawk.game.march.ArmyInfo;

/**
 * 世界地图怪物刷新配置
 * 
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_enemy.xml")
public class WorldEnemyCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 类型
	protected final int type;
	// 等级
	protected final int level;
	// 怪物占位长度 areaSize="1_1"
	protected final String areaSize;
	// 指挥官经验
	protected final int commanderExp;
	// 怪物血量权重随机 3_10,4_50,5_40
	protected final String hp;
	// 最后一刀奖励,奖励id，在award表配置
	protected final String killAward;
	// 存活周期
	protected final int lifeTime;
	// 最低大本等级限制
	private final int lowerLimit;
	// 是否为新手怪
	protected final boolean newly;
	// 首杀奖励
	protected final int firstKillaward;
	// 血量权重因子
	protected HawkWeightFactor<Integer> hpFactor;
	// 怪物士兵列表
	protected final String soldiers;
	// 伤害阈值
	protected final int damageAwardCoefficient;
	// 伤害奖励
	protected final String damageAward;
	// 联盟奖励
	protected final String unionAward;
	// 怪物名字
	protected final String name;
	// 攻击奖励
	protected final String attackAward;
	// 打野体力消耗
	protected final int costPhysicalPower;
	// 野怪modelType
	private final int modelType;
	/** 联盟礼物 */
	protected final int allianceGift;

	/**
	 * 跨服掉落概率
	 */
	protected final int crossDropRate;
	
	/**
	 * 跨服掉落奖励
	 */
	protected final String crossDropAward;
	
	/** 是否是情报中心配置*/
	protected final int isAgency;
	
	protected final int relateActivity;
	
	// 怪物列表
	private List<ArmyInfo> armyList;
	// 击杀奖励
	private List<Integer> killAwards;
	// 伤害奖励
	private List<Integer> damageAwards;
	// 联盟奖励
	private List<Integer> unionAwards;
	// 攻击奖励
	private List<Integer> attackAwards;

	public WorldEnemyCfg() {
		id = 0;
		type = 0;
		level = 0;
		areaSize = "";
		hp = "";
		killAward = "";
		lifeTime = 0;
		lowerLimit = 0;
		newly = false;
		firstKillaward = 0;
		soldiers = "";
		commanderExp = 0;
		damageAward = "";
		unionAward = "";
		name = "";
		modelType = 0;
		damageAwardCoefficient = 0;
		attackAward = "";
		costPhysicalPower = 0;
		allianceGift = 0;
		crossDropRate = 0;
		crossDropAward = "";
		isAgency = 0;
		relateActivity= 0;
	}

	public String getAreaSize() {
		return areaSize;
	}

	public int getCommanderExp() {
		return commanderExp;
	}

	public String getHp() {
		return hp;
	}

	public int getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getLifeTime() {
		return lifeTime;
	}

	public boolean isNewly() {
		return newly;
	}

	public int getLowerLimit() {
		return lowerLimit;
	}

	public List<Integer> getKillAwards() {
		return new ArrayList<>(killAwards);
	}

	public int getFirstKillaward() {
		return firstKillaward;
	}

	public String getSoldiers() {
		return soldiers;
	}

	public List<Integer> getDamageAwards() {
		return new ArrayList<>(damageAwards);
	}

	public List<Integer> getUnionAwards() {
		return new ArrayList<>(unionAwards);
	}

	public String getName() {
		return name;
	}

	public int getModelType() {
		return modelType;
	}

	public int getDamageAwardCoefficient() {
		return damageAwardCoefficient;
	}

	public int getCostPhysicalPower() {
		return costPhysicalPower;
	}

	public List<Integer> getAttackAwards() {
		return new ArrayList<>(attackAwards);
	}

	/**
	 * 获取怪物部队信息
	 * 
	 * @return
	 */
	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}

	/**
	 * 计算随机血量
	 * 
	 * @return
	 */
	public int randomHp() {
		return hpFactor.randomObj();
	}

	@Override
	protected boolean assemble() {
		hpFactor = new HawkWeightFactor<Integer>();
		if (!HawkOSOperator.isEmptyString(hp)) {
			String[] items = hp.split(",");
			if (items != null && items.length > 0) {
				for (String item : items) {
					String valWeight[] = item.split("_");
					if (valWeight != null && valWeight.length == 2) {
						hpFactor.addWeightObj(Integer.valueOf(valWeight[1]), Integer.valueOf(valWeight[0]));
					}
				}
			}
		}
		armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldiers)) {
			for (String army : Splitter.on("|").split(soldiers)) {
				String[] armyStrs = army.split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(armyStrs[0]), Integer.parseInt(armyStrs[1])));
			}
		}

		killAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(killAward)) {
			Arrays.asList(killAward.split(";")).forEach(award -> {
				killAwards.add(Integer.parseInt(award));
			});
		}

		attackAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(attackAward)) {
			Arrays.asList(attackAward.split(";")).forEach(award -> {
				attackAwards.add(Integer.parseInt(award));
			});
		}

		// 伤害奖励
		damageAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(damageAward)) {
			Arrays.asList(damageAward.split(";")).forEach(award -> {
				damageAwards.add(Integer.valueOf(award));
			});
		}

		// 联盟奖励
		unionAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(unionAward)) {
			Arrays.asList(unionAward.split(";")).forEach(award -> {
				unionAwards.add(Integer.parseInt(award));
			});
		}

		return true;
	}

	@Override
	protected boolean checkValid() {
		for (int awardId : killAwards) {
			AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
			if (cfg == null) {
				logger.error("WorldEnemyCfg error, id : {}, killAward : {} error", id, killAward);
				return false;
			}
		}

		for (int awardId : unionAwards) {
			AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
			if (cfg == null) {
				logger.error("WorldEnemyCfg error, id : {}, unionAward : {} error", id, unionAward);
				return false;
			}
		}

		for (int awardId : damageAwards) {
			AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
			if (cfg == null) {
				logger.error("WorldEnemyCfg error, id : {}, damageAward : {} error", id, damageAward);
				return false;
			}
		}

		for (int awardId : attackAwards) {
			AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
			if (cfg == null) {
				logger.error("WorldEnemyCfg error, id : {}, attackAward : {} error", id, attackAward);
				return false;
			}
		}

		if (allianceGift > 0) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, allianceGift);
			HawkAssert.notNull(itemCfg, " itemcfg error cfgid = " + allianceGift);
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
			HawkAssert.notNull(awardCfg, " awardcfg error cfgid = " + itemCfg.getRewardId());
		}
		return true;
	}

	public int getAllianceGift() {
		return allianceGift;
	}

	public int getCrossDropRate() {
		return crossDropRate;
	}

	public String getCrossDropAward() {
		return crossDropAward;
	}

	public int getIsAgency() {
		return isAgency;
	}
	
	
	public int getRelateActivity() {
		return relateActivity;
	}
	
	
}