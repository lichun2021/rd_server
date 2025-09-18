package com.hawk.game.cfgElement;

/**
 * 科技配置文件中科技特效
 * 
 * @author shadow
 *
 */
public class TechEffect {
	private final int effectType;
	private final int effectValue;

	public TechEffect(int effectType, int effectValue) {
		this.effectType = effectType;
		this.effectValue = effectValue;
	}

	/**
	 * 
	 * @return 效果值
	 */
	public int getEffectValue() {
		return effectValue;
	}

	/**
	 * 
	 * @return 效果类型
	 */
	public int getEffectType() {
		return effectType;
	}
}
