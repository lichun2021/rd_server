package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 星能探索配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/star_explore_collect.xml")
public class ArmourStarExploreCollectCfg extends HawkConfigBase {
	@Id
	private final int id;
	// 满养成星球数量
	private final int unlockNum;
	// 解锁技能
	private final String unlockFixedSkill;
	private final String fixedSkillGrow;
	private final String consume;
	private final String fixedSkillLimit;
	private final String unlockRandomSkill;
	private final String randomSkillRange;
	private final int randomSkillFinal;
	
	 /**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;
    protected final int maxLogistics;
    
	private HawkTuple2<Integer, Integer> fixAttr;
	private HawkTuple2<Integer, Integer> fixAttrGrow;
	private HawkTuple2<Integer, Integer> fixAttrLimit;
	private HawkTuple2<Integer, Integer> randomAttr;
	private HawkTuple3<Integer, Integer, Integer> randomAttrRange;


	public ArmourStarExploreCollectCfg() {
		this.id = 0;
		this.unlockNum = 0;
		this.unlockFixedSkill = "";
		this.fixedSkillGrow = "";
		this.consume = "";
		this.fixedSkillLimit = "";
		this.unlockRandomSkill = "";
		this.randomSkillRange = "";
		this.randomSkillFinal = 0;
		atkAttr = "";
		hpAttr = "";
		maxLogistics = 0;
	}

	@Override
	protected boolean assemble() {
		String[] split = unlockFixedSkill.split("_");
		fixAttr = new HawkTuple2<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		split = fixedSkillGrow.split("_");
		fixAttrGrow = new HawkTuple2<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		split = fixedSkillLimit.split("_");
		fixAttrLimit = new HawkTuple2<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		split = unlockRandomSkill.split("_");
		randomAttr = new HawkTuple2<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		split = randomSkillRange.split("_");
		randomAttrRange = new HawkTuple3<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
		return true;
	}

	public int getId() {
		return id;
	}

	public int getUnlockNum() {
		return unlockNum;
	}

	public String getUnlockFixedSkill() {
		return unlockFixedSkill;
	}

	public String getFixedSkillGrow() {
		return fixedSkillGrow;
	}

	public String getConsume() {
		return consume;
	}

	public String getFixedSkillLimit() {
		return fixedSkillLimit;
	}

	public String getUnlockRandomSkill() {
		return unlockRandomSkill;
	}

	public String getRandomSkillRange() {
		return randomSkillRange;
	}

	public int getRandomSkillFinal() {
		return randomSkillFinal;
	}

	public HawkTuple2<Integer, Integer> getFixAttr() {
		return fixAttr;
	}

	public HawkTuple2<Integer, Integer> getFixAttrGrow() {
		return fixAttrGrow;
	}

	public HawkTuple2<Integer, Integer> getFixAttrLimit() {
		return fixAttrLimit;
	}

	public HawkTuple2<Integer, Integer> getRandomAttr() {
		return randomAttr;
	}

	public HawkTuple3<Integer, Integer, Integer> getRandomAttrRange() {
		return randomAttrRange;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
    
    public int getMaxLogistics() {
		return maxLogistics;
	}
}
