package com.hawk.game.config;

import java.util.HashSet;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.Const.EffType;

/**
 * 作用号条件配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/effectid.xml")
public class EffectCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 作用号生效条件
	protected final String condition;
	protected final int heroEffectImmBool;
	protected final int unvisible;// 1 做用号不同步给客户端
	protected final int type; //作用号类型，对应GsConst的effectType枚举的值

	private int[] conditionElements;

	private static Set<EffType> notvisibleSet = new HashSet<>();

	public EffectCfg() {
		id = 0;
		condition = "";
		heroEffectImmBool = 0;
		unvisible = 0;
		type = 0;
	}

	public static boolean effectNotVisible(EffType effType) {
		return notvisibleSet.contains(effType);
	}

	public int getId() {
		return id;
	}

	public int getHeroEffectImmBool() {
		return heroEffectImmBool;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(condition)) {
			String[] strs = condition.split("_");
			conditionElements = new int[strs.length];
			for (int i = 0; i < strs.length; i++) {
				conditionElements[i] = Integer.parseInt(strs[i]);
			}
		}
		if (unvisible == 1) {
			notvisibleSet.add(EffType.valueOf(id));
		} else {
			notvisibleSet.remove(EffType.valueOf(id));
		}

		return true;
	}

	public int[] getConditionElements() {
		return conditionElements;
	}

	/**
	 * 是否是一个存在的作用号
	 * 
	 * @param effectId
	 * @return
	 */
	public static boolean isExistEffectId(int effectId) {
		return HawkConfigManager.getInstance().getConfigByKey(EffectCfg.class, effectId) != null;
	}

	public int getType() {
		return type;
	}
}
