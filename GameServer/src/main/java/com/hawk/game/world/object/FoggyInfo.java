package com.hawk.game.world.object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.hero.PlayerHero;

/**
 * 迷雾点信息
 * @author zhenyu.shang
 * @since 2018年2月22日
 */
public class FoggyInfo {
	/**
	 * NPC玩家
	 */
	@JSONField(serialize = false)
	private NpcPlayer npcPlayer;
	/**
	 * 陷阱信息
	 */
	private Map<Integer, Integer> trapInfo;
	
	/**
	 * 士兵信息
	 */
	private Map<Integer, Integer> soliderInfo;
	
	/**
	 * 英雄信息
	 */
	private List<Integer> heroIds;
	

	public FoggyInfo() {
		npcPlayer = new NpcPlayer(HawkXID.nullXid()); 
	}
	
	@JSONField(serialize = false)
	public NpcPlayer getNpcPlayer() {
		return npcPlayer;
	}

	public Map<Integer, Integer> getTrapInfo() {
		return trapInfo;
	}

	public void setTrapInfo(Map<Integer, Integer> trapInfo) {
		this.trapInfo = trapInfo;
	}

	public Map<Integer, Integer> getSoliderInfo() {
		return soliderInfo;
	}

	public void setSoliderInfo(Map<Integer, Integer> soliderInfo) {
		this.soliderInfo = soliderInfo;
	}

	public List<Integer> getHeroIds() {
		return heroIds;
	}

	public void setHeroIds(List<Integer> heroIds) {
		this.heroIds = heroIds;
	}
	
	@JSONField(serialize = false)
	public List<ArmyInfo> getArmyList(){
		List<ArmyInfo> armyList = new ArrayList<>();
		for(Entry<Integer, Integer> entry : soliderInfo.entrySet()){
			ArmyInfo armyInfo = new ArmyInfo(entry.getKey(), entry.getValue());
			armyList.add(armyInfo);
		}
		for(Entry<Integer, Integer> entry : trapInfo.entrySet()){
			ArmyInfo armyInfo = new ArmyInfo(entry.getKey(), entry.getValue());
			armyList.add(armyInfo);
		}
		return armyList;
	}
	
	/**
	 * 获取要塞总战力
	 * @return
	 */
	public long getTotalPower(){
		Float power = 0.0f;
		//先算士兵的
		for (Entry<Integer, Integer> entry : soliderInfo.entrySet()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entry.getKey());
			BigDecimal bdPower = new BigDecimal(Float.toString(cfg.getPower()));
			BigDecimal bdNum = new BigDecimal(Float.toString(entry.getValue()));
			power += bdPower.multiply(bdNum).floatValue();
		}
		//再算陷阱的
		for (Entry<Integer, Integer> entry : trapInfo.entrySet()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entry.getKey());
			BigDecimal bdPower = new BigDecimal(Float.toString(cfg.getPower()));
			BigDecimal bdNum = new BigDecimal(Float.toString(entry.getValue()));
			power += bdPower.multiply(bdNum).floatValue();
		}
		HawkAssert.notNull(npcPlayer);
		for (PlayerHero hero : npcPlayer.getHeroByCfgId(heroIds)) {
			power += hero.power();
		}
		return power.intValue();
	}
}
