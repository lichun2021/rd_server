package com.hawk.game.config;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GameUtil;

/**
 * 英雄档案馆配置
 *
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/hero_archives_content.xml")
public class HeroArchivesContentCfg extends HawkConfigBase {
	@Id
	protected final int heroId;
	// 消耗
	protected final String cost;
	// 作用号
	protected final String effect;

	private Set<EffType> effTypeSet;
	
	public HeroArchivesContentCfg() {
		heroId = 0;
		cost = "";
		effect = "";
	}

	/**
	 * 获取消耗
	 * @param level
	 * @return
	 */
	public List<ItemInfo> getCost(int level) {
		// 由于等级1自动解锁,cost第一段是等级2的消耗,以此类推
		return ItemInfo.valueListOf(cost.split(";")[level - 2]);
	}
	
	/**
	 * 获取作用号
	 * @param level
	 * @return
	 */
	public Map<Integer, Integer> getEff(int level) {
		return GameUtil.assambleEffectMap(effect.split(";")[level - 1]);
	}
	
	/**
	 * 获取所有作用号类型
	 * @return
	 */
	public Set<EffType> getEffTypeSet() {
		return effTypeSet;
	}
	
	@Override
	protected boolean assemble() {
		
		Set<EffType> effTypeSet = new HashSet<>();
		String[] effInfo = effect.split(";");
		for (int i = 0; i < effInfo.length; i++) {
			List<EffectObject> assambleEffectObject = GameUtil.assambleEffectObject(effInfo[i]);
			for (EffectObject obj : assambleEffectObject) {
				effTypeSet.add(obj.getType());
			}
		}
		this.effTypeSet = effTypeSet;
		
		return super.assemble();
	}
	
	@Override
	protected boolean checkValid() {
		if (cost.split(";").length != 4) {
			return false;
		}
		if (effect.split(";").length != 5) {
			return false;
		}
		return true;
	}
}
