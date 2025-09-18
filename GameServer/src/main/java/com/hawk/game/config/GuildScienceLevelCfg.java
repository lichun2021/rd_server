package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月4日
 */
@HawkConfigManager.XmlResource(file = "xml/guild_science_level.xml")
@HawkConfigBase.CombineId(fields = {"level", "scienceId"})
public class GuildScienceLevelCfg extends HawkConfigBase {
	/** 等级 */
	protected final int level;

	/** 科技Id */
	protected final int scienceId;

	/** 作用号 */
	protected final String effect;

	/** 研究时间 */
	protected final int costTime;

	/** 星级 */
	protected final String scienceVal;
	
	/** 普通捐献消耗 */
	protected final String norDonateRes;

	/** 作用号集合 */
	private List<int[]> effects;

	/** 升星科技值列表 */
	private List<Integer> starValList;

	/** 科研值上限 */
	private int fullDonate;
	
	/** 开放时间限制*/
	protected final int openLimitTime;
	
	protected final String atkAttr;
	protected final String hpAttr;
	
	/** 资源捐献消耗列表 */
	private List<List<ItemInfo>> resCostList;

	public GuildScienceLevelCfg() {
		this.level = 0;
		this.scienceId = 0;
		this.effect = "";
		this.costTime = 0;
		this.scienceVal = "";
		this.norDonateRes = "";
		openLimitTime = 0;
		this.atkAttr = "";
		this.hpAttr = "";
	}

	public int getLevel() {
		return level;
	}

	public int getScienceId() {
		return scienceId;
	}

	public String getEffect() {
		return effect;
	}

	public int getCostTime() {
		return costTime;
	}

	public List<int[]> getEffects() {
		return effects;
	}

	public List<Integer> getStarValList() {
		return starValList;
	}

	public int getFullDonate() {
		return fullDonate;
	}
	
	public int getOpenLimitTime() {
		return openLimitTime;
	}
	
	/**
	 * 根据科技星级获取消耗资源
	 * @param star
	 * @return
	 */
	public List<ItemInfo> getResCost(int star) {
		List<ItemInfo> listCopy = new ArrayList<>();
		for (ItemInfo info : resCostList.get(star)) {
			listCopy.add(info.clone());
		}
		return listCopy;
	}

	public String getNorDonateRes() {
		return norDonateRes;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	@Override
	protected boolean assemble() {
		resCostList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(norDonateRes)) {
			for (String resStr : norDonateRes.split(";")) {
				List<ItemInfo> resCost = new ArrayList<>();
				resCost = ItemInfo.valueListOf(resStr);
				resCostList.add(resCost);
			}
		}

		effects = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			String[] array = effect.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 2) {
					logger.error("guild_science_level.xml error, scienceId : {}, level : {} error", scienceId, level);
					return false;
				}
				effects.add(new int[] { Integer.parseInt(eff[0]), Integer.parseInt(eff[1]) });
			}
		}
		fullDonate = 0;
		starValList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(scienceVal)) {
			String[] array = scienceVal.split(";");
			for (String val : array) {
				int intVal = Integer.valueOf(val);
				starValList.add(intVal);
				fullDonate += intVal;
			}
		}
		if(resCostList.size()!=starValList.size()){
			logger.error("guild_science_level.xml error, resCost not match star, scienceId : {}, level : {} ", scienceId, level);
			return false;
		}
		return true;
	}
}
