package com.hawk.game.service.simulatewar.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.xid.HawkXID;

import com.hawk.game.battle.BattleService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.MechaCore.PBMechaCoreInfo;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarBattleData;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarSoldier;
import com.hawk.game.protocol.SimulateWar.SimulateWarBasePlayerStruct;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.game.util.GameUtil;

/**
 * 攻防模拟战
 * @author jm
 *
 */
public class SimulateWarPlayer extends Player {
	private PlayerData playerData;
	private SimulateWarBasePlayerStruct playerInfo;
	private SimulateWarPlayerEffect playerEffect;
	private Map<Integer, PBSimulateWarSoldier> starMap;
	private List<PlayerHero> heros = new ArrayList<>();
	private SuperSoldier superSoldier;
	private ArmourBriefInfo armour;
	private PBSimulateWarBattleData battleData;

	public SimulateWarPlayer(HawkXID xid, SimulateWarBasePlayerStruct playerInfo, PBSimulateWarBattleData data, Map<EffType, Integer> extMap) {
		super(xid);
		init(playerInfo, data, extMap);
	}

	private void init(SimulateWarBasePlayerStruct snbPlayer, PBSimulateWarBattleData data, Map<EffType, Integer> extMap) {
		this.battleData = data;
		playerData = new SimulateWarPlayerData();
		playerEffect = new SimulateWarPlayerEffect(playerData, data.getEffsList(), extMap);
		playerEffect.init();
		playerInfo = snbPlayer;
		starMap = new HashMap<>();
		for (PBSimulateWarSoldier pbs : data.getSoldiersList()) {
			starMap.put(pbs.getArmyId(), pbs);
		}
		
		for (PBHeroInfo heroPb : data.getHerosList()) {
			heros.add(SimulateWarPlayerHero.create(heroPb));
		}
		if (data.hasSuperSoldier()) {
			superSoldier = SimulateWarSuperSoldier.create(data.getSuperSoldier());
		}
		armour = data.getArmourBrief();
	}
	
	@Override
	public PBManhattanInfo.Builder buildManhattanInfo() {
		return battleData.getManhattanInfo().toBuilder();
	}
	
	@Override
	public PBManhattanInfo.Builder buildManhattanInfo(PresetMarchManhattan presetMarchManhattan) {
		return battleData.getManhattanInfo().toBuilder();
	}
	
	@Override
	public boolean checkManhattanFuncUnlock() {
		return battleData.getManhattanFuncUnlock();
	}
	
	@Override
	public PBMechaCoreInfo.Builder buildMechacoreInfo(MechaCoreSuitType suit) {
		return battleData.getMechacoreInfo().toBuilder();
	}

	@Override
	public boolean checkMechacoreFuncUnlock() {
		return battleData.getMechacoreFuncUnlock();
	}
	
	@Override
	public int getMechaCoreRankLevel() {
		return battleData.getMechacoreInfo().getBreakthroughLv();
	}

	/**取得指定页套装*/
	@Override
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return armour;
	}

	@Override
	public int getSoldierStar(int armyId) {
		if(!starMap.containsKey(armyId)){
			return 0;
		}
		return starMap.get(armyId).getStar();
	}

	@Override
	public int getSoldierStep(int armyId) {
		if(!starMap.containsKey(armyId)){
			return 0;
		}
		return starMap.get(armyId).getPlantStep();
	}
	
	@Override
	public int getSoldierPlantSkillLevel(int armyId) {
		if(!starMap.containsKey(armyId)){
			return 0;
		}
		return starMap.get(armyId).getPlantSkillLevel();
	}

	@Override
	public int getSoldierPlantMilitaryLevel(int armyId) {
		if (!starMap.containsKey(armyId)){
			return 0;
		}
		return starMap.get(armyId).getPlantMilitaryLevel();
	}

	@Override
	public int[] getPosXY() {
		return GameUtil.splitXAndY(0);
	}

	@Override
	public int getPlayerPos() {
		return 0;
	}

	@Override
	public String getName() {
		return playerInfo.getName();
	}

	@Override
	public String getId() {
		return BattleService.NPC_ID + playerInfo.getPlayerId();
	}

	@Override
	public int getIcon() {
		return playerInfo.getIcon();
	}

	@Override
	public String getPfIcon() {
		return playerInfo.getPfIcon();
	}

	@Override
	public long getPower() {
		return playerInfo.getBattlePoint();
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public int getCityLevel() {
		return 0;
	}
	
	@Override
	public int getCityPlantLv(){
		return playerInfo.getCityPlantLevel();
	}

	@Override
	public String getGuildId() {
		return playerInfo.getGuildID();
	}

	@Override
	public String getGuildName() {
		return "";
	}

	@Override
	public String getGuildTag() {
		return playerInfo.getGuildTag();
	}

	@Override
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {
		for (PlayerHero hero : heros) {
			if (hero.getCfgId() == heroId) {
				return Optional.of(hero);
			}
		}
		return Optional.empty();

	}

	@Override
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {
		return heros;
	}

	@Override
	public Optional<SuperSoldier> getSuperSoldierByCfgId(int soldierId) {
		return Optional.ofNullable(superSoldier);
	}

	@Override
	public List<SuperSoldier> getAllSuperSoldier() {
		return new ArrayList<>();
	}

	@Override
	public PlayerEffect getEffect() {
		return playerEffect;
	}

	/**
	 * 获取玩家数据
	 */
	public PlayerData getData() {
		return playerData;
	}

	/**
	 * 是否已经加入联盟
	 */
	public boolean hasGuild() {
		return true;
	}

}