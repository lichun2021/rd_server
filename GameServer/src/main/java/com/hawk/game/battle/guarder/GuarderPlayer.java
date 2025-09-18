package com.hawk.game.battle.guarder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.xid.HawkXID;

import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.NPCPlayerData;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;

/**- 【万分比】【11044】基地防守战斗时，在战斗开始时，召唤其守护玩家 XX% 比率的部队（至多 XX 万且无法超出自身部队数量）加入战斗（不算实际战损）
 * 
 * 召唤其守护玩家
 * */
public class GuarderPlayer extends Player {

	private Player player; // 自己
	private Player guarder; // 守护

	private int playerPos;
	private String name;
	private String pfIcon;
	private PlayerData playerData;
	public GuarderPlayer(HawkXID xid) {
		super(xid);
		name = "";
		pfIcon = "";
		playerData = new NPCPlayerData(xid.getUUID());
	}

	public static String guarderPlayerId(String playerId) {
		return BattleService.NPC_ID + "heiheihahei" + playerId;
	}
	
	public static boolean isGuarderPlayer(String playerId){
		if (playerId == null) {
			return false;
		}
		return playerId.startsWith(BattleService.NPC_ID + "heiheihahei");
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Player getGuarder() {
		return guarder;
	}

	public void setGuarder(Player guarder) {
		this.guarder = guarder;
	}

	@Override
	public int increaseNationMilitary(int addCnt, int resType, Action action, boolean needLog) {
		return 0;
	}

	/**取得指定页套装*/
	@Override
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return ArmourBriefInfo.getDefaultInstance();
	}

	@Override
	public int[] getPosXY() {
		return GameUtil.splitXAndY(playerPos);
	}

	@Override
	public int getPlayerPos() {
		return playerPos;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return guarderPlayerId(player.getId());
	}

	@Override
	public int getIcon() {
		return 0;
	}

	@Override
	public String getPfIcon() {
		return pfIcon == null ? "" : pfIcon;
	}

	@Override
	public long getPower() {
		return 0;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public int getCityLevel() {
		return 1;
	}

	public int getCityPlantLv() {
		return 0;
	}

	@Override
	public String getGuildId() {
		return "";
	}

	@Override
	public String getGuildName() {
		return "";
	}

	@Override
	public String getGuildTag() {
		return "";
	}

	public void setPlayerPos(int playerPos) {
		this.playerPos = playerPos;
	}

	/**
	 * 是否已经加入联盟
	 */
	public boolean hasGuild() {
		return false;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setPfIcon(String pfIcon) {
		this.pfIcon = pfIcon;
	}

	@Override
	public int getSoldierStar(int armyId) {
		return guarder.getSoldierStar(armyId);
	}

	@Override
	public int getSoldierStep(int armyId) {
		return guarder.getSoldierStep(armyId);
	}

	@Override
	public int getSoldierPlantSkillLevel(int armyId) {
		return guarder.getSoldierPlantSkillLevel(armyId);
	}

	@Override
	public int getSoldierPlantMilitaryLevel(int armyId) {
		return guarder.getSoldierPlantMilitaryLevel(armyId);
	}

	@Override
	public int getMaxSoldierPlantMilitaryLevel() {
		return guarder.getMaxSoldierPlantMilitaryLevel();
	}
	@Override
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {
		return Optional.empty();
	}

	@Override
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {
		return new ArrayList<>();
	}

	@Override
	public Optional<SuperSoldier> getSuperSoldierByCfgId(int soldierId) {
		return Optional.empty();
	}
	
	@Override
	public List<PlayerHero> getAllHero() {
		return new ArrayList<>();
	}

	@Override
	public List<SuperSoldier> getAllSuperSoldier() {
		return new ArrayList<>();
	}
	@Override
	public PlayerEffect getEffect() {
		return player.getEffect();
	}

	/**
	 * 获取玩家数据
	 */
	public PlayerData getData() {
		return playerData;
	}
	
	public int getMechaCoreRankLevel() {
		if (player != null) {
			return player.getMechaCoreRankLevel();
		}
		return 0;
	}
	
}
