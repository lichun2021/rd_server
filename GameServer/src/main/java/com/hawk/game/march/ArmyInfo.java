package com.hawk.game.march;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * 军队详情
 * 
 * @author julia
 */
public class ArmyInfo implements Comparable<ArmyInfo> {
	/**按照战报邮件显示的顺序排序*/
	public static final Comparator<ArmyInfo> fightMailShow =  Comparator.comparingInt(ArmyInfo::getLevel).reversed().thenComparing(ArmyInfo::getBuildingWeight);
	// 兵种
	private int armyId; 
	
	// 所属玩家Id
	private String playerId;
	
	// 派遣数量
	private int totalCount;
	
	// 死亡数目
	private int deadCount; 
	
	// 直接死亡数目（不包含因超出医院容量上限造成的死兵）
	private int directDeadCount;
	
	// 击杀数目
	private int killCount; 
	
	private double killPower;
	
	// 伤兵数目
	private int woundedCount; 
	
	// 战斗实际损失数
	private int realLoseCount;
	
	// 影子部队数量(不参与部队归还结算)
	private int shadowCnt;
	
	// 影子部队死亡数量(不参与部队归还结算)
	private int shadowDeadCnt;
	// 荣耀
	private int star;
	private int plantStep;
	private int plantSkillLevel;

	private int plantMilitaryLevel;
	
	// 击杀信息 <soldierId, count>
	private Map<Integer, Integer> killInfo;
	
	// 击杀详细信息
	private Map<Long, Integer> killDetail;
	
	// 黑洞导致死亡数量
	private Map<String, Integer> blackHoleDead;
	// 单场战斗中损失的战力
	private double disBattlePoint;
	// 1634 回城直接转为free的数量
	private int save1634;
	private int help1635TarCount ;// 帮助几个目标
	private int help1635Count; // 帮了多少次
	private int dodgeCnt;
	private int extrAtktimes; // 连击数
	private int liao1645Cnt;
	private int liao1645Kill;
	private int tszzNationalHospital; // 进入国家医院的统帅之战死兵数
	private int kunNa1652Help;
	private int kunNa1653Kill;
	private boolean sssSLM1667Kill;
	private ArmyInfo sssSLMPet;
	
	private SoldierType soldierType;
	private Boolean isPlant;
	private int sssKaiEn1656Cnt;
	private int eff12086Zhuan;
	private int eff12086ZhuanAll;
	private int eff12111Cnt;
	private int eff12339Cnt;
	private double eff12339Power;
	public ArmyInfo() {
	}

	public ArmyInfo(int armyId, int totalCount) {
		this.armyId = armyId;
		this.totalCount = totalCount;
	}

	public ArmyInfo(String string) {
		String[] strs = string.split("_");
		this.armyId = Integer.parseInt(strs[0]);
		this.totalCount = Integer.parseInt(strs[1]);
		this.deadCount = Integer.parseInt(strs[2]);
		this.woundedCount = Integer.parseInt(strs[3]);
		if (strs.length >= 5) { // 防御坦克在战斗中所损失和受伤的数量，战斗结束后可直接修复 +7.50%
			this.save1634 = Integer.parseInt(strs[4]);
		}
	}

	public SoldierType getType() {
		if (Objects.isNull(soldierType)) {
			soldierType = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId).getSoldierType();
		}
		return soldierType;
	}
	
	public int getBuildingWeight() {
		try {
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			switch (armyCfg.getBuilding()) {
			case BuildingType.BARRACKS_VALUE:
				return 1;
			case BuildingType.WAR_FACTORY_VALUE:
				return 2;
			case BuildingType.AIR_FORCE_COMMAND_VALUE:
				return 4;
			case BuildingType.REMOTE_FIRE_FACTORY_VALUE:
				return 3;
			default:
				break;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 1;
	}

	public boolean isPlant() {
		if (Objects.isNull(isPlant)) {
			isPlant = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId).isPlantSoldier();
		}
		return isPlant.booleanValue();
	}

	public int getArmyId() {
		return armyId;
	}

	public void setArmyId(int armyId) {
		this.armyId = armyId;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTotalCount() {
		return totalCount;
	}
	
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getDeadCount() {
		return deadCount;
	}

	public void setDeadCount(int deadCount) {
		this.deadCount = deadCount;
	}

	public int getKillCount() {
		return killCount;
	}
	
	public int getMailShowKillCount() { 
		if (Objects.isNull(sssSLMPet)) {
			return killCount;
		}
		return killCount - sssSLMPet.killCount;
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
	}

	public int getWoundedCount() {
		return woundedCount;
	}

	public void setWoundedCount(int woundedCount) {
		this.woundedCount = woundedCount;
	}
	
	public int getRealLoseCount() {
		return realLoseCount;
	}

	public void setRealLoseCount(int realLoseCount) {
		this.realLoseCount = realLoseCount;
	}
	
	public int getShadowCnt() {
		return shadowCnt;
	}

	public void setShadowCnt(int shadowCnt) {
		this.shadowCnt = shadowCnt;
	}

	public int getShadowDeadCnt() {
		return shadowDeadCnt;
	}

	public void setShadowDeadCnt(int shadowDeadCnt) {
		this.shadowDeadCnt = shadowDeadCnt;
	}

	public int getFreeCnt() {
		return Math.max(0, totalCount - deadCount - woundedCount);
	}
	
	public Map<Integer, Integer> getKillInfo() {
		return killInfo;
	}
	
	public Map<Long, Integer> getKillDetail() {
		return killDetail;
	}

	/**
	 * 合并击杀信息
	 * @param killInfo
	 */
	public void mergeKillInfo(Map<Integer, Integer> killInfo) {
		if (killInfo == null) {
			return;
		}
		if (this.killInfo == null) {
			this.killInfo = new HashMap<>();
		}
		for (Entry<Integer, Integer> entry : killInfo.entrySet()) {
			int key = entry.getKey();
			if (this.killInfo.containsKey(key)) {
				this.killInfo.put(key, this.killInfo.get(key) + entry.getValue());
			} else {
				this.killInfo.put(key, entry.getValue());
			}
		}

	}
	
	/**
	 * 合并击杀详情
	 * @param killDetail
	 */
	public void mergeKillDetail(Map<Long, Integer> killDetail) {
		if (killDetail == null) {
			return;
		}
		if (this.killDetail == null) {
			this.killDetail = new HashMap<>();
		}
		for (Entry<Long, Integer> entry : killDetail.entrySet()) {
			long key = entry.getKey();
			if (this.killDetail.containsKey(key)) {
				this.killDetail.put(key, this.killDetail.get(key) + entry.getValue());
			} else {
				this.killDetail.put(key, entry.getValue());
			}
		}
		
	}
	
	/**
	 * 合并黑洞导致死亡数量信息
	 * @param blackHoleDead
	 */
	public void mergeBlackHoleDead(Map<String, Integer> blackHoleDead) {
		if (blackHoleDead == null) {
			return;
		}
		if (this.blackHoleDead == null) {
			this.blackHoleDead = new HashMap<>();
		}
		for (Entry<String, Integer> entry : blackHoleDead.entrySet()) {
			String key = entry.getKey();
			if (this.blackHoleDead.containsKey(key)) {
				this.blackHoleDead.put(key, this.blackHoleDead.get(key) + entry.getValue());
			} else {
				this.blackHoleDead.put(key, entry.getValue());
			}
		}
		
	}

	/**
	 * 受到箭塔攻击
	 * @param attckRate
	 */
	public int killByTower(int attckRate){
		int dead = (int) Math.floor(getFreeCnt() * (attckRate * GsConst.EFF_PER));
		this.deadCount += dead;
		return dead;
	}

	public ArmyInfo getCopy() {
		ArmyInfo armyInfo = new ArmyInfo(armyId, totalCount);
		armyInfo.setPlayerId(playerId);
		armyInfo.setDeadCount(deadCount);
		armyInfo.setKillCount(killCount);
		armyInfo.setWoundedCount(woundedCount);
		armyInfo.mergeKillInfo(killInfo);
		armyInfo.mergeKillDetail(killDetail);
		armyInfo.setRealLoseCount(realLoseCount);
		armyInfo.setShadowCnt(shadowCnt);
		armyInfo.setShadowDeadCnt(shadowDeadCnt);
		armyInfo.setStar(star);
		armyInfo.setPlantStep(plantStep);
		armyInfo.setPlantSkillLevel(plantSkillLevel);
		armyInfo.setPlantMilitaryLevel(plantMilitaryLevel);
		armyInfo.mergeBlackHoleDead(blackHoleDead);
		armyInfo.setDisBattlePoint(disBattlePoint);
		armyInfo.setSave1634(save1634);
		return armyInfo;
	}

	public ArmySoldierPB.Builder toArmySoldierPB(Player player) {
		ArmySoldierPB.Builder builder = ArmySoldierPB.newBuilder();
		builder.setArmyId(armyId);
		builder.setCount(getFreeCnt());
		builder.setWoundedCount(getWoundedCount());
		builder.setSave1634(save1634);
		if(Objects.nonNull(player)){
			builder.setStar(player.getSoldierStar(armyId));
			builder.setPlantStep(player.getSoldierStep(armyId));
			builder.setPlantSkillLevel(player.getSoldierPlantSkillLevel(armyId));
			builder.setPlantMilitaryLevel(player.getSoldierPlantMilitaryLevel(armyId));
		}
		return builder;
	}

	@Override
	public String toString() {
		return String.format("%d_%d_%d_%d_%d", armyId, totalCount, deadCount, woundedCount, save1634);
	}
	
	@Override
	public int compareTo(ArmyInfo target) {
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		BattleSoldierCfg targetCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, target.armyId);

		if (armyCfg.getLevel() < targetCfg.getLevel()) {
			return -1;
		}
		
		if (armyCfg.getLevel() > targetCfg.getLevel()) {
			return 1;
		}
		
		if (armyCfg.getType() > targetCfg.getType()) {
			return -1;
		}
		
		if (armyCfg.getType() < targetCfg.getType()) {
			return 1;
		}
		
		return 0;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public int getPlantStep() {
		return plantStep;
	}

	public void setPlantStep(int plantStep) {
		this.plantStep = plantStep;
	}

	public int getPlantSkillLevel() {
		return plantSkillLevel;
	}
	
	public void setPlantSkillLevel(int plantSkillLevel) {
		this.plantSkillLevel = plantSkillLevel;
	}

	public Map<String, Integer> getBlackHoleDead() {
		return blackHoleDead;
	}

	
	public double getDisBattlePoint() {
		return disBattlePoint;
	}

	public void setDisBattlePoint(double disBattlePoint) {
		this.disBattlePoint = disBattlePoint;
	}

	public int getSave1634() {
		return save1634;
	}

	public void setSave1634(int save1634) {
		this.save1634 = save1634;
	}

	public int getHelp1635TarCount() {
		return help1635TarCount;
	}

	public void setHelp1635TarCount(int help1635TarCount) {
		this.help1635TarCount = help1635TarCount;
	}

	public int getHelp1635Count() {
		return help1635Count;
	}

	public void setHelp1635Count(int help1635Count) {
		this.help1635Count = help1635Count;
	}

	public void setDodgeCnt(int dodgeCnt) {
		this.dodgeCnt = dodgeCnt;
	}

	public int getDodgeCnt() {
		return dodgeCnt;
	}

	public int getExtrAtktimes() {
		return extrAtktimes;
	}

	public void setExtrAtktimes(int extrAtktimes) {
		this.extrAtktimes = extrAtktimes;
	}

	public int getDirectDeadCount() {
		return directDeadCount;
	}

	public void setDirectDeadCount(int directDeadCount) {
		this.directDeadCount = directDeadCount;
	}

	public int getLiao1645Cnt() {
		return liao1645Cnt;
	}

	public void setLiao1645Cnt(int liao1645Cnt) {
		this.liao1645Cnt = liao1645Cnt;
	}

	public int getSssKaiEn1656Cnt() {
		return sssKaiEn1656Cnt;
	}

	public void setSssKaiEn1656Cnt(int sssKaiEn1656Cnt) {
		this.sssKaiEn1656Cnt = sssKaiEn1656Cnt;
	}

	public int getLiao1645Kill() {
		return liao1645Kill;
	}

	public void setLiao1645Kill(int liao1645Kill) {
		this.liao1645Kill = liao1645Kill;
	}

	public int getTszzNationalHospital() {
		return tszzNationalHospital;
	}

	public void setTszzNationalHospital(int tszzNationalHospital) {
		this.tszzNationalHospital = tszzNationalHospital;
	}

	public int getKunNa1652Help() {
		return kunNa1652Help;
	}

	public void setKunNa1652Help(int kunNa1652Help) {
		this.kunNa1652Help = kunNa1652Help;
	}

	public int getKunNa1653Kill() {
		return kunNa1653Kill;
	}

	public void setKunNa1653Kill(int kunNa1653Kill) {
		this.kunNa1653Kill = kunNa1653Kill;
	}

	public SoldierType getSoldierType() {
		return soldierType;
	}

	public void setSoldierType(SoldierType soldierType) {
		this.soldierType = soldierType;
	}

	public Boolean getIsPlant() {
		return isPlant;
	}

	public void setIsPlant(Boolean isPlant) {
		this.isPlant = isPlant;
	}

	public void setKillInfo(Map<Integer, Integer> killInfo) {
		this.killInfo = killInfo;
	}

	public void setKillDetail(Map<Long, Integer> killDetail) {
		this.killDetail = killDetail;
	}

	public void setBlackHoleDead(Map<String, Integer> blackHoleDead) {
		this.blackHoleDead = blackHoleDead;
	}
	
	public boolean isSssSLM1667Kill() {
		return sssSLM1667Kill;
	}

	public void setSssSLM1667Kill(boolean sssSLM1667Kill) {
		this.sssSLM1667Kill = sssSLM1667Kill;
	}

	public ArmyInfo getSssSLMPet() {
		return sssSLMPet;
	}

	public void setSssSLMPet(ArmyInfo sssSLMPet) {
		this.sssSLMPet = sssSLMPet;
	}

	public double getKillPower() {
		return killPower;
	}

	public void setKillPower(double killPower) {
		this.killPower = killPower;
	}

	public int getLevel() {
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		return armyCfg.getLevel();
	}

	public int getEff12086Zhuan() {
		return eff12086Zhuan;
	}

	public void setEff12086Zhuan(int eff12086Zhuan) {
		this.eff12086Zhuan = eff12086Zhuan;
	}

	public int getEff12086ZhuanAll() {
		return eff12086ZhuanAll;
	}

	public void setEff12086ZhuanAll(int eff12086ZhuanAll) {
		this.eff12086ZhuanAll = eff12086ZhuanAll;
	}

	public int getEff12111Cnt() {
		return eff12111Cnt;
	}

	public void setEff12111Cnt(int eff12111Cnt) {
		this.eff12111Cnt = eff12111Cnt;
	}

	public int getPlantMilitaryLevel() {
		return plantMilitaryLevel;
	}

	public void setPlantMilitaryLevel(int plantMilitaryLevel) {
		this.plantMilitaryLevel = plantMilitaryLevel;
	}

	public int getEff12339Cnt() {
		return eff12339Cnt;
	}

	public void setEff12339Cnt(int eff12339Cnt) {
		this.eff12339Cnt = eff12339Cnt;
	}

	public double getEff12339Power() {
		return eff12339Power;
	}

	public void setEff12339Power(double eff12339Power) {
		this.eff12339Power = eff12339Power;
	}
	
	
}
