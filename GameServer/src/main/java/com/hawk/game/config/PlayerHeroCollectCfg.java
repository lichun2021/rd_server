package com.hawk.game.config;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.Table;
import com.hawk.game.protocol.Const.EffType;

/**
 * 英雄图鉴配置表
 * @author zhenyu.shang
 * @since 2019年11月28日
 */
@HawkConfigManager.XmlResource(file = "xml/hero_collect.xml")
public class PlayerHeroCollectCfg extends HawkConfigBase {

	/** Id */
	@Id
	protected final int heroId;
	
	protected final String refHeroIds;
	
	protected final String levelEff;
	
	protected final String starEff;
	
	private Table<Integer, EffType, Integer> refHeroEff;
	
	private Table<Integer, EffType, Integer> levelEffTable;
	
	private Table<Integer, EffType, Integer> starEffTable;
	
	public PlayerHeroCollectCfg() {
		this.heroId = 0;
		this.refHeroIds = "";
		this.levelEff = "";
		this.starEff = "";
	}

	public int getHeroId() {
		return heroId;
	}

	public String getRefHeroIds() {
		return refHeroIds;
	}
	
	public String getLevelEff() {
		return levelEff;
	}

	public String getStarEff() {
		return starEff;
	}

	public Table<Integer, EffType, Integer> getRefHeroEff() {
		return refHeroEff;
	}

	public Table<Integer, EffType, Integer> getLevelEffTable() {
		return levelEffTable;
	}

	public Table<Integer, EffType, Integer> getStarEffTable() {
		return starEffTable;
	}

	@Override
	protected boolean assemble() {
		refHeroEff = ConcurrentHashTable.create();
		if (!HawkOSOperator.isEmptyString(refHeroIds)) {
			String[] array = refHeroIds.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 3) {
					return false;
				}
				refHeroEff.put(Integer.valueOf(eff[0]), EffType.valueOf(Integer.valueOf(eff[1])), Integer.valueOf(eff[2]));
			}
		}

		levelEffTable = ConcurrentHashTable.create();
		if (!HawkOSOperator.isEmptyString(levelEff)) {
			String[] array = levelEff.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 3) {
					return false;
				}
				levelEffTable.put(Integer.valueOf(eff[0]), EffType.valueOf(Integer.valueOf(eff[1])), Integer.valueOf(eff[2]));
			}
		}
		
		starEffTable = ConcurrentHashTable.create();
		if (!HawkOSOperator.isEmptyString(starEff)) {
			String[] array = starEff.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 3) {
					return false;
				}
				starEffTable.put(Integer.valueOf(eff[0]), EffType.valueOf(Integer.valueOf(eff[1])), Integer.valueOf(eff[2]));
			}
		}
		
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		if (refHeroEff == null || refHeroEff.size() == 0) {
			return false;
		}
		//检查对应表中的作用号ID
		boolean effectIdCheckResult = refHeroEff.columnKeySet().stream().filter(effect -> {
			return EffectCfg.isExistEffectId(effect.getNumber());
		}).findAny().isPresent();
		
		//检查对应表中的英雄ID
		boolean heroIdCheckResult = refHeroEff.rowKeySet().stream().filter(heroId -> {
			return HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, heroId) != null;
		}).findAny().isPresent();
				
		return effectIdCheckResult && heroIdCheckResult;
	}
}
