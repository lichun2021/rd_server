package com.hawk.game.guild.championship.member;

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
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.GuildChampionship.PBChampionSoldier;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo.Builder;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.MechaCore.PBMechaCoreInfo;
import com.hawk.game.protocol.Player.PlayerSnapshotPB;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.game.util.GameUtil;

public class CHAMPlayer extends Player {
	private PlayerData playerData;
	private PlayerSnapshotPB ordainData;
	private CHAMPlayerEffect playerEffect;
	private Map<Integer, PBChampionSoldier> starMap;
	private List<PlayerHero> heros = new ArrayList<>();
	private SuperSoldier superSoldier;
	private ArmourBriefInfo armour;
	private PBChampionPlayer battleData;
	public CHAMPlayer(HawkXID xid, PBChampionPlayer data) {
		super(xid);
		init(data);
	}

	private void init(PBChampionPlayer data) {
		this.battleData = data;
		playerData = new CHAMPlayerData();
		playerEffect = new CHAMPlayerEffect(playerData, data.getEffsList());
		playerEffect.init();
		ordainData = data.getPlayerInfo();
		starMap = new HashMap<>();
		for (PBChampionSoldier pbs : data.getSoldiersList()) {
			starMap.put(pbs.getArmyId(), pbs);
		}
		for (PBHeroInfo heroPb : data.getHerosList()) {
			heros.add(CHAMPlayerHero.create(heroPb));
		}
		if (data.hasSuperSoldier()) {
			superSoldier = CHAMSuperSoldier.create(data.getSuperSoldier());
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
		if(!starMap.containsKey(armyId)){
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
		return ordainData.getName();
	}

	@Override
	public String getId() {
		return BattleService.NPC_ID + ordainData.getPlayerId();
	}

	@Override
	public int getIcon() {
		return ordainData.getIcon();
	}

	@Override
	public String getPfIcon() {
		return ordainData.getPfIcon();
	}

	@Override
	public long getPower() {
		return ordainData.getPower();
	}

	@Override
	public int getLevel() {
		return ordainData.getLevel();
	}

	@Override
	public int getCityLevel() {
		return ordainData.getCityLevel();
	}
	
	@Override
	public int getCityLv() {
		return ordainData.getCityLevel();
	}
	
	@Override
	public int getCityPlantLv(){
		return ordainData.getCityPlantLevel();
	}

	@Override
	public String getGuildId() {
		return ordainData.getGuildId();
	}

	@Override
	public String getGuildName() {
		return ordainData.getGuildName();
	}

	@Override
	public String getGuildTag() {
		return ordainData.getGuildTag();
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