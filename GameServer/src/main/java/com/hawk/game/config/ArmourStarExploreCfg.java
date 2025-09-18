package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.cfgElement.EffectObject;

/**
 * 星能探索配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/star_explore.xml")
public class ArmourStarExploreCfg extends HawkConfigBase {
	
	/**
	 * 星球数量
	 */
	private static int starCount;
	
	/**
	 * 星球属性数量
	 */
	private static int starAttrCount;
	
	@Id
	private final int id;

	private final String firstAttribute;
	private final String secondAttribute;
	private final String thirdAttribute;

	/**
	 * 三个属性
	 */
	private EffectObject firstEff;
	private EffectObject secondEff;
	private EffectObject thirdEff;
	
	/**
	 * 三个属性最大进度
	 */
	private int firstRate;
	private int secondRate;
	private int thirdRate;
	
	public ArmourStarExploreCfg() {
		id = 0;
		firstAttribute = "";
		secondAttribute = "";
		thirdAttribute = "";
	}
	
	@Override
	protected boolean assemble() {
		String[] split = firstAttribute.split("_");
		firstEff = new EffectObject(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		firstRate = Integer.parseInt(split[2]);
		
		split = secondAttribute.split("_");
		secondEff = new EffectObject(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		secondRate = Integer.parseInt(split[2]);
		
		
		split = thirdAttribute.split("_");
		thirdEff = new EffectObject(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		thirdRate = Integer.parseInt(split[2]);
		
		starCount = Math.max(id, starCount);
		starAttrCount = 3;
		return true;
	}

	public EffectObject getFirstEff() {
		return firstEff.copy();
	}

	public EffectObject getSecondEff() {
		return secondEff.copy();
	}

	public EffectObject getThirdEff() {
		return thirdEff.copy();
	}

	public int getFirstRate() {
		return firstRate;
	}

	public int getSecondRate() {
		return secondRate;
	}

	public int getThirdRate() {
		return thirdRate;
	}

	public static boolean checkStarId(int starId) {
		return starId > 0 && starId <= starCount;
	}

	public static int getStarCount() {
		return starCount;
	}

	public static boolean checkStarAttrId(int starAttrId) {
		return starAttrId > 0 && starAttrId <= starAttrCount;
	}
	
	public static int getStarAttrCount() {
		return starAttrCount;
	}
	
	public int getRate(int attrId) {
		if (attrId == 1) {
			return firstRate;
		}
		if (attrId == 2) {
			return secondRate;
		}
		if (attrId == 3) {
			return thirdRate;
		}
		return 0;
	}
}
