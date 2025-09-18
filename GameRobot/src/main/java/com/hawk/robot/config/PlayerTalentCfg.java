package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 天赋功能配置
 * 
 * @author
 *
 */
@HawkConfigManager.XmlResource(file = "xml/player_talent.xml")
public class PlayerTalentCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 技能大类 1-战争，2-发展
	protected final int type;
	// 最高等级
	protected final int maxLevel;
	// 对应作用号
	protected final int effect;
	protected final int numType;
	protected final String value;
	protected final String frontTalent;
	protected final int heroLevel;
	protected final int owner;
	protected List<Integer> values;
	
	// 不需要前置条件的天赋
	private static final List<Integer> frontBlankTalents = new ArrayList<>();
	// 需要前置条件的天赋
	private static final List<Integer> frontNeedTalents = new ArrayList<>();

	public PlayerTalentCfg() {
		id = 0;
		type = 0;
		maxLevel = 0;
		effect = 0;
		numType = 0;
		value = "";
		frontTalent = "";
		owner = 0;
		heroLevel = 0;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public int getEffect() {
		return effect;
	}

	public int getNumType() {
		return numType;
	}

	public String getValue() {
		return value;
	}

	public String getFrontTalent() {
		return frontTalent;
	}

	public List<Integer> getEffValList() {
		return values;
	}

	/**
	 * 计算升级效果
	 * 
	 * @param curLevel
	 * @param tarLevel
	 * @return
	 */
	public int getAddEffVal(int curLevel, int tarLevel) {
		if (curLevel < 0 || curLevel >= maxLevel || tarLevel <= 0 || tarLevel > maxLevel) {
			return 0;
		}
		int curVal = curLevel >= 1 ? values.get(curLevel - 1) : 0;
		int tarVal = values.get(tarLevel - 1);
		return tarVal - curVal;
	}

	@Override
	protected boolean assemble() {
		if(HawkOSOperator.isEmptyString(frontTalent)) {
			frontBlankTalents.add(id);
		} else {
			frontNeedTalents.add(id);
		}
		
		values = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(value)) {
			String[] array = value.split("_");
			for (String val : array) {
				values.add(Integer.parseInt(val));
			}
		}

		return true;
	}

	@Override
	protected boolean checkValid() {
		if (values == null || values.size() != maxLevel) {
			return false;
		}
		return true;
	}
	
	public static List<Integer> getFrontblanktalents() {
		return frontBlankTalents;
	}

	public static List<Integer> getFrontneedtalents() {
		return frontNeedTalents;
	}
}
