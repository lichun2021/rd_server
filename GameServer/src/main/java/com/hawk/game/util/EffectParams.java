package com.hawk.game.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 行军参数
 *
 */
public class EffectParams {
	
	private static class DefaultEffectParams extends EffectParams {

		@Override
		public void setArmys(List<ArmyInfo> armys) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setHeroIds(List<Integer> heroIds) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setSuperSoliderId(int superSoliderId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setArmourSuit(ArmourSuitType armourSuit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setTalent(int talent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setSuperLab(int superLab) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int getEffVal(Player player, EffType effType) {
			return 0;
		}
		
		/*@Override
		public void setPolicySkillId(List<Integer> policySkillId) {
			throw new UnsupportedOperationException();
		}*/
			
		
	}
	
	private static EffectParams Instance = new DefaultEffectParams();

	private WarEff troopEffType; // 只有行军战斗时才有
	
	private WorldMarch march; // 守家是个麻烦 他没行军. 我看看要不要搞个默认的什么
	private IWorldMarch imarch;
	/**
	 * 部队
	 */
	private List<ArmyInfo> armys = Collections.emptyList();
	
	/**
	 * 英雄
	 */
	private List<Integer> heroIds = Collections.emptyList();
	
	/**
	 * 机甲
	 */
	private int superSoliderId;
	
	/**
	 * 装备
	 */
	private ArmourSuitType armourSuit = ArmourSuitType.ARMOUR_NONE;
	
	/**
	 * 机甲核心
	 */
	private MechaCoreSuitType mechacoreSuit = MechaCoreSuitType.MECHA_ONE;
	
	/**
	 * 天赋
	 */
	private int talent = TalentType.TALENT_TYPE_DEFAULT_VALUE;
	
	/**
	 * 能量源
	 */
	private int superLab;
	/** 战斗发生地点*/
	private int battlePoint;
	/**
	 * 国家技能ID
	 */
	private List<Integer> policySkillId = Collections.emptyList();
	/**
	 * 出征携带皮肤
	 */
	private List<Integer> dressList;
	
	private Map<EffType,Integer> effCacheMap = new HashMap<>();
	private Map<EffType,Integer> extEffCacheMap = new HashMap<>();
	private int autoMarchIdentify;

	/**
	 * 出征参数
	 */
	private WorldMarchReq  worldmarchReq;

	private boolean staffPointGreat;

	private boolean isActivateDressGroup;
	
	/**
	 * 超武ID
	 */
	private int manhattanAtkSwId;
	private int manhattanDefSwId;
	/**
	 * 构造
	 */
	public EffectParams() {
	}
	
	public static EffectParams getDefaultVal(){
		return Instance;
	}
	
	public static EffectParams copyOf(EffectParams tar) {
		EffectParams result = new EffectParams();
		result.troopEffType = tar.troopEffType; // 只有行军战斗时才有
		result.armys = tar.armys;
		result.heroIds = tar.heroIds;
		result.superSoliderId = tar.superSoliderId;
		result.armourSuit = tar.armourSuit;
		result.mechacoreSuit = tar.mechacoreSuit;
		result.talent = tar.talent;
		result.superLab = tar.superLab;
		result.battlePoint = tar.battlePoint;
		result.policySkillId = tar.policySkillId;
		result.dressList = tar.dressList;
		result.effCacheMap = tar.effCacheMap;
		result.extEffCacheMap = tar.extEffCacheMap;
		result.autoMarchIdentify = tar.autoMarchIdentify;
		result.worldmarchReq = tar.worldmarchReq;
		result.staffPointGreat = tar.staffPointGreat;
		result.isActivateDressGroup = tar.isActivateDressGroup;
		result.manhattanAtkSwId = tar.manhattanAtkSwId;
		result.manhattanDefSwId = tar.manhattanDefSwId;
		return result;
	}
	
	public int getEffVal(Player player, EffType effType) {
		int extEffVal = this.extEffCacheMap.getOrDefault(effType, 0);
		if (effCacheMap.containsKey(effType)) {
			return effCacheMap.get(effType) + extEffVal;
		}
		int val = player.getEffect().getEffVal(effType, this) + extEffVal;
		effCacheMap.put(effType, val);
		return val;
	}
	
	public void cleanEffValCache(){
		this.effCacheMap.clear();
	}
	
	/**
	 * 构造
	 */
	public EffectParams(WorldMarchReq marchReq, List<ArmyInfo> armys) {
		this.armys = armys;
		this.heroIds = marchReq.getHeroIdList();
		this.superSoliderId = marchReq.getSuperSoldierId();
		this.manhattanAtkSwId = marchReq.getManhattan().getManhattanAtkSwId();
		this.manhattanDefSwId = marchReq.getManhattan().getManhattanDefSwId();
		this.armourSuit = marchReq.getArmourSuit();
		this.mechacoreSuit = marchReq.getMechacoreSuit();
		TalentType talentType = marchReq.getTalentType();
		if (talentType != null) {
			this.talent = talentType.getNumber();
		} else {
			talent = TalentType.TALENT_TYPE_DEFAULT_VALUE;
		}
		this.superLab = marchReq.getSuperLab();
		this.worldmarchReq = marchReq;
		this.dressList = new ArrayList<>(marchReq.getMarchDressList());
		this.isActivateDressGroup = marchReq.getIsActivateDressGroup();
	}
	
	public List<ArmyInfo> getArmys() {
		return armys;
	}

	public void setArmys(List<ArmyInfo> armys) {
		this.armys = armys;
	}

	public List<Integer> getHeroIds() {
		return heroIds;
	}

	public void setHeroIds(List<Integer> heroIds) {
		this.heroIds = heroIds;
	}

	public int getSuperSoliderId() {
		return superSoliderId;
	}

	public void setSuperSoliderId(int superSoliderId) {
		this.superSoliderId = superSoliderId;
	}
	
	public int getManhattanAtkSwId() {
		return manhattanAtkSwId;
	}

	public void setManhattanAdkSwId(int manhattanAtkSwId) {
		this.manhattanAtkSwId = manhattanAtkSwId;
	}
	
	public int getManhattanDefSwId() {
		return manhattanDefSwId;
	}

	public void setManhattanDefSwId(int manhattanDefSwId) {
		this.manhattanDefSwId = manhattanDefSwId;
	}

	public ArmourSuitType getArmourSuit() {
		return armourSuit;
	}

	public void setArmourSuit(ArmourSuitType armourSuit) {
		this.armourSuit = armourSuit;
	}
	
	public MechaCoreSuitType getMechacoreSuit() {
		return mechacoreSuit;
	}

	public void setMechacoreSuit(MechaCoreSuitType mechacoreSuit) {
		this.mechacoreSuit = mechacoreSuit;
	}

	public int getTalent() {
		return talent;
	}

	public void setTalent(int talent) {
		this.talent = talent;
	}

	public int getSuperLab() {
		return superLab;
	}
	
	public void setSuperLab(int superLab) {
		this.superLab = superLab;
	}

	public List<Integer> getPolicySkillId() {
		return policySkillId;
	}

	public void setPolicySkillId(List<Integer> policySkillId) {
		this.policySkillId = policySkillId;
	}

	public int getBattlePoint() {
		return battlePoint;
	}

	public void setBattlePoint(int battlePoint) {
		this.battlePoint = battlePoint;
	}

	public WarEff getTroopEffType() {
		return troopEffType;
	}

	public void setTroopEffType(WarEff troopEffType) {
		this.troopEffType = troopEffType;
	}


	public WorldMarch getMarch() {
		return march;
	}

	public void setMarch(WorldMarch march) {
		this.march = march;
	}

	public int getAutoMarchIdentify() {
		return autoMarchIdentify;
	}

	public void setAutoMarchIdentify(int autoMarchIdentify) {
		this.autoMarchIdentify = autoMarchIdentify;
	}

	public WorldMarchReq getWorldmarchReq() {
		return worldmarchReq;
	}

	public List<Integer> getDressList() {
		if (dressList == null) {
			return new ArrayList<>();
		}
		if (dressList.size() > 8) {
			dressList = new ArrayList<>();
		}
		return new ArrayList<>(dressList);
	}

	public void setDressList(List<Integer> dressList) {
		this.dressList = dressList;
	}

	public void setWorldmarchReq(WorldMarchReq worldmarchReq) {
		this.worldmarchReq = worldmarchReq;
	}

	public void setStaffPointGreat(boolean bfalse) {
		this.staffPointGreat = bfalse;
	}

	public boolean isStaffPointGreat() {
		return staffPointGreat;
	}

	public boolean isActivateDressGroup() {
		return isActivateDressGroup;
	}

	public IWorldMarch getImarch() {
		return imarch;
	}

	public void setImarch(IWorldMarch imarch) {
		this.imarch = imarch;
	}
	
	
	
	
	public void addExtEff(Map<Integer,Integer> emap){
		for(Map.Entry<Integer,Integer> entry : emap.entrySet()){
			int eid = entry.getKey();
			int val = entry.getValue();
			EffType effType = EffType.valueOf(eid);
			if(Objects.isNull(effType)){
				continue;
			}
			int effVal = this.extEffCacheMap.getOrDefault(effType, 0);
			effVal += val;
			this.extEffCacheMap.put(effType, effVal);
		}
	}
}
