package com.hawk.game.battle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.hawk.game.lianmengcyb.CYBORGExtraParam;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;

/**
 * 战后结算信息
 * 
 * @author admin
 */
public class BattleOutcome {
	
	/**
	 * 进攻方本次参战部队结算信息 <playerId,list<ArmyInfo>>
	 */
	private Map<String, List<ArmyInfo>> battleArmyMapAtk;

	/**
	 * 防御方本次参战部队结算信息
	 */
	private Map<String, List<ArmyInfo>> battleArmyMapDef;
	
	/**
	 * 进攻方剩余部队汇总信息 <playerId,list<ArmyInfo>>
	 */
	private Map<String, List<ArmyInfo>> aftArmyMapAtk;
	
	/**
	 * 防御方剩余部队汇总信息
	 */
	private Map<String, List<ArmyInfo>> aftArmyMapDef;
	
	private Map<String,String> defMailIdMap;
	/** 战斗获得的国家军功*/
	private Map<String,Integer> nationMilitary;
	boolean isAtkWin = true;
	/**联盟军演邮件*/
	private DungeonMailType duntype = DungeonMailType.NONE;
	private SWWarType swWarType;
	private boolean yqzzNationMilly;
	private Map<String,Integer> fgylSkill;
	private int fgylMaxBlood;// = 20;  // 反攻幽灵最大血
	private int fgylRemainBlood;// = 21;  //剩余
	private int fgylKillBlood;// = 22;  // 打掉
	public BattleOutcome(Map<String, List<ArmyInfo>> battleArmyMapAtk, Map<String, List<ArmyInfo>> battleArmyMapDef, Map<String, List<ArmyInfo>> aftArmyMapAtk, Map<String, List<ArmyInfo>> aftArmyMapDef, boolean isAtkWin) {
		this.battleArmyMapAtk = battleArmyMapAtk;
		this.battleArmyMapDef = battleArmyMapDef;
		this.aftArmyMapAtk = aftArmyMapAtk;
		this.aftArmyMapDef = aftArmyMapDef;
		this.isAtkWin = isAtkWin;
	}
	
	/** 记录防守方邮件 */
	public void recordDefMail(String playerId, String mailUUID) {
		if (Objects.isNull(defMailIdMap)) {
			defMailIdMap = new HashMap<String, String>();
		}
		defMailIdMap.put(playerId, mailUUID);
	}
	
	public String getDefMail(String playerId){
		if (Objects.isNull(defMailIdMap)) {
			return "";
		}
		return defMailIdMap.getOrDefault(playerId, "");
	}
	
	public Map<String, List<ArmyInfo>> getBattleArmyMapAtk() {
		return battleArmyMapAtk;
	}

	public Map<String, List<ArmyInfo>> getBattleArmyMapDef() {
		return battleArmyMapDef;
	}

	public Map<String, List<ArmyInfo>> getAftArmyMapAtk() {
		return aftArmyMapAtk;
	}

	public Map<String, List<ArmyInfo>> getAftArmyMapDef() {
		return aftArmyMapDef;
	}

	public boolean isAtkWin() {
		return isAtkWin;
	}
	
	public String genBattleArmyMapAtkStr(){
		return armyMapToString(battleArmyMapAtk);
	}
	
	public String genBattleArmyMapDefStr(){
		return armyMapToString(battleArmyMapDef);
	}
	
	private String armyMapToString(Map<String, List<ArmyInfo>> armyMap){
		StringBuilder sb = new StringBuilder();
		for(Entry<String, List<ArmyInfo>> entry : armyMap.entrySet()){
			String playerId = entry.getKey();
			List<ArmyInfo> armyList = entry.getValue();
			sb.append(",").append(playerId).append("||");
			if(armyList == null){
				continue;
			}
			armyList.stream().forEach(army -> sb.append(army.getArmyId()).append("_").append(army.getTotalCount()).append("_")
					.append(army.getFreeCnt()).append("_")
					.append(army.getDeadCount()).append("_")
					.append(army.getKillCount()).append("|"));
		}
		return sb.toString().replaceFirst("", ",");
	}

//	public void setTBLYMail(TBLYExtraParam tblyMail) {
//		duntype = DungeonMailType.TBLY;
//		this.dungeon = "Tiberium";
//		this.dungeonId = tblyMail.getBattleId();
//		this.season = tblyMail.getSeason();
//		this.isLeaguaWar = tblyMail.isLeaguaWar() ? 1 : 0;
//	}

	public SWWarType getSwWarType() {
		return swWarType;
	}

	public void setSwWarType(SWWarType swWarType) {
		this.swWarType = swWarType;
	}

//	public String getDungeon() {
//		return dungeon;
//	}
//
//	public void setDungeon(String dungeon) {
//		this.dungeon = dungeon;
//	}
//
//	public String getDungeonId() {
//		return dungeonId;
//	}
//
//	public void setDungeonId(String dungeonId) {
//		this.dungeonId = dungeonId;
//	}
//
//	public int getIsLeaguaWar() {
//		return isLeaguaWar;
//	}
//
//	public void setIsLeaguaWar(int isLeaguaWar) {
//		this.isLeaguaWar = isLeaguaWar;
//	}
//
//	public int getSeason() {
//		return season;
//	}
//
//	public void setSeason(int season) {
//		this.season = season;
//	}
//
//	public void setCYBORGMail(CYBORGExtraParam extParm) {
//		duntype = DungeonMailType.CYBORG;
//		this.dungeon = "Cyborg";
//		this.dungeonId = extParm.getBattleId();
//		this.season = extParm.getSeason();
//		this.isLeaguaWar = extParm.isLeaguaWar() ? 1 : 0;
//	}

	public DungeonMailType getDuntype() {
		return duntype;
	}

	public void setDuntype(DungeonMailType duntype) {
		this.duntype = duntype;
	}
	
	public int getPlayerNationMilitary(String playerId){
		if(Objects.isNull(nationMilitary)){
			return 0;
		}
		return nationMilitary.getOrDefault(playerId, 0);
	}

	public void setNationMilitary(Map<String, Integer> nationMilitary) {
		this.nationMilitary = nationMilitary;
	}

	public boolean isYqzzNationMilly() {
		return yqzzNationMilly;
	}

	public void setYqzzNationMilly(boolean yqzzNationMilly) {
		this.yqzzNationMilly = yqzzNationMilly;
	}

	public int getFgylSkill(String playerId){
		if(Objects.isNull(fgylSkill)){
			return 0;
		}
		return fgylSkill.getOrDefault(playerId, 0);
	}
	
	public Map<String, Integer> getFgylSkill() {
		return fgylSkill;
	}

	public void setFgylSkill(Map<String, Integer> fgylSkill) {
		this.fgylSkill = fgylSkill;
	}

	public void setAtkWin(boolean isAtkWin) {
		this.isAtkWin = isAtkWin;
	}

	public int getFgylMaxBlood() {
		return fgylMaxBlood;
	}

	public void setFgylMaxBlood(int fgylMaxBlood) {
		this.fgylMaxBlood = fgylMaxBlood;
	}

	public int getFgylRemainBlood() {
		return fgylRemainBlood;
	}

	public void setFgylRemainBlood(int fgylRemainBlood) {
		this.fgylRemainBlood = fgylRemainBlood;
	}

	public int getFgylKillBlood() {
		return fgylKillBlood;
	}

	public void setFgylKillBlood(int fgylKillBlood) {
		this.fgylKillBlood = fgylKillBlood;
	}
	
}
