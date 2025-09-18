package com.hawk.game.world.object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.EffType;

/**
 * 幽灵工厂怪物信息
 * @author che
 * 
 */
public class GhostInfo {
	
	/**
	 * NPC玩家
	 */
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
	
	/**
	 * 机甲
	 */
	private int superSoldierId;
	
	/**
	 * 机甲星级
	 */
	private int superSoldierStar;
	
	/**
	 * 装备
	 */
	private List<Integer> armours;
	
	/**
	 * 额外作用号
	 */
	private Map<EffType, Integer> effectmap;
	
	/**
	 * 挑战者
	 */
	private String challenger;
	

	public GhostInfo() {
		npcPlayer = new NpcPlayer(HawkXID.nullXid()); 
	}
	
	public void init(){
		//添加装备
		if(this.armours!= null ){
			npcPlayer.setArmour(this.armours);
		}
		//添加机甲
		if(this.superSoldierId >0){
			npcPlayer.setSuperSoldier(this.superSoldierId, this.superSoldierStar);
		}
		//初始化额外作用号
		if(this.effectmap != null){
			for (Entry<EffType, Integer> ent : effectmap.entrySet()) {
				this.npcPlayer.addEffectVal(ent.getKey(), ent.getValue());
			}
		}
		
	}
	
	
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
	
	
	

	public int getSuperSoldierId() {
		return superSoldierId;
	}


	public void setSuperSoldierId(int superSoldierId) {
		this.superSoldierId = superSoldierId;
	}


	public int getSuperSoldierStar() {
		return superSoldierStar;
	}


	public void setSuperSoldierStar(int superSoldierStar) {
		this.superSoldierStar = superSoldierStar;
	}


	public List<Integer> getArmours() {
		return armours;
	}

	public void setArmours(List<Integer> armours) {
		this.armours = armours;
	}
	
	

	public Map<EffType, Integer> getEffectmap() {
		return effectmap;
	}

	public void setEffectmap(Map<EffType, Integer> effectmap) {
		if(effectmap == null ){
			return;
		}
		this.effectmap = new HashMap<>();
		this.effectmap.putAll(effectmap);
	}
	
	

	public String getChallenger() {
		return challenger;
	}

	public void setChallenger(String challenger) {
		this.challenger = challenger;
	}

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
