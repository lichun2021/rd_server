package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.util.GameUtil;

/**
 * 军衔配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/military_rank_level.xml")
public class MilitaryRankCfg extends HawkConfigBase {

	/**
	 * id
	 */
	@Id
	protected final int rankId;
	
	/**
	 * 军衔等级 
	 */
	protected final int rankLevel;
	
	/**
	 * 功勋经验
	 */
	protected final int rankExp;
	
	/**
	 * 触发作用号
	 */
	protected final String effect;
	
	/**
	 * 每日津贴奖励
	 */
	protected final String dateAward;
	
	/**
	 * 每日津贴奖励
	 */
	List<ItemInfo> awardList;
	
	/**
	 * 触发作用号
	 */
	List<EffectObject> touchEffect;
	
	public MilitaryRankCfg() {
		rankId = 0;
		rankLevel = 0;
		rankExp = 0;
		effect = "";
		dateAward = "";
	}

	public int getRankId() {
		return rankId;
	}

	public int getRankLevel() {
		return rankLevel;
	}

	public int getRankExp() {
		return rankExp;
	}

	public String getEffect() {
		return effect;
	}

	public String getDateAward() {
		return dateAward;
	}

	/**
	 * 获取每日津贴奖励
	 * @return
	 */
	public List<ItemInfo> getAwardList() {
		return awardList;
	}

	/**
	 * 获取触发的作用号
	 * @return
	 */
	public List<EffectObject> getTouchEffect() {
		return touchEffect;
	}

	@Override
	protected boolean assemble() {
		awardList = ItemInfo.valueListOf(this.dateAward);
		touchEffect = GameUtil.assambleEffectObject(effect);
		return true;
	}
}
