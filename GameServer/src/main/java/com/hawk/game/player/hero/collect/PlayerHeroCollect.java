package com.hawk.game.player.hero.collect;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.PlayerHeroCollectCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.EffType;

/**
 * 玩家图鉴实体类
 * @author zhenyu.shang
 * @since 2019年11月28日
 */
public class PlayerHeroCollect {
	
	private PlayerHero hero;
	
	/** 是否激活 */
	private boolean active;
	
	/** 羁绊作用号 */
	private ImmutableMap<EffType, Integer> effects;
	
	/** 激活羁绊总条数 */
	private int collectNum;
	
	public PlayerHeroCollect(PlayerHero hero) {
		this.hero = hero;
		this.active = getCurrentCfg() != null;
		this.effects = ImmutableMap.of();
	}
	
	/**
	 * 获取当前配置
	 * @return
	 */
	public PlayerHeroCollectCfg getCurrentCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(PlayerHeroCollectCfg.class, hero.getCfgId());
	}
	
	/**
	 * 检查作用号
	 */
	public void checkCollectEffect() {
		this.collectNum = 0;
		int totalLevel = 0;
		int totalStar = 0;
		Map<EffType, Integer> effVal = new HashMap<EffType, Integer>();
		for (Integer heroCfgId : getCurrentCfg().getRefHeroEff().rowKeySet()) {
			Optional<PlayerHero> heroOp = this.getHeroByCfgId(heroCfgId);
			if(heroOp.isPresent()){
				totalLevel += heroOp.get().getLevel();
				totalStar += heroOp.get().getStar();
				
				Map<EffType, Integer> eff = getCurrentCfg().getRefHeroEff().row(heroCfgId);
				for (Entry<EffType, Integer> entry : eff.entrySet()) {
					effVal.merge(entry.getKey(), entry.getValue(), (v1, v2) -> v1 + v2);
				}
				this.collectNum++;
			}
		}
		//遍历取出大于等于要求等级的所有作用号
		for (Integer rowKey : getCurrentCfg().getLevelEffTable().rowKeySet()) {
			if(totalLevel >= rowKey){
				Map<EffType, Integer> eff = getCurrentCfg().getLevelEffTable().row(rowKey);
				for (Entry<EffType, Integer> entry : eff.entrySet()) {
					effVal.merge(entry.getKey(), entry.getValue(), (v1, v2) -> v1 + v2);
				}
			}
		}
		//遍历取出大于等于要求星级的所有作用号
		for (Integer rowKey : getCurrentCfg().getStarEffTable().rowKeySet()) {
			if(totalStar >= rowKey){
				Map<EffType, Integer> eff = getCurrentCfg().getStarEffTable().row(rowKey);
				for (Entry<EffType, Integer> entry : eff.entrySet()) {
					effVal.merge(entry.getKey(), entry.getValue(), (v1, v2) -> v1 + v2);
				}
			}
		}
				
		this.effects = ImmutableMap.copyOf(effVal);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ImmutableMap<EffType, Integer> getEffects() {
		return effects;
	}

	public PlayerHero getHero() {
		return hero;
	}

	public int getCollectNum() {
		return collectNum;
	}
	
	public boolean checkHasNotify(int heroId){
		//先检查此英雄是否是在羁绊英雄中
		if(!getCurrentCfg().getRefHeroEff().containsRow(heroId)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 取得英雄 按照配置文件ID. 不强制加载做用号. 
	 */
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {
		for(HeroEntity entity : hero.getParent().getData().getHeroEntityList()){
			if(entity.getHeroId() == heroId){
				return Optional.of(entity.getHeroObj(false));
			}
		}
		return Optional.empty();
	}
}
