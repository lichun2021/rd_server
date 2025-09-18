package com.hawk.game.module.SampleFight.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.helper.HawkAssert;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.NPCPlayerData;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.util.GameUtil;

public class SFPlayer extends Player {
	private PlayerData playerData;
	private String playerId;
	private SFEffect playerEffect;
	private int playerPos;
	private String name;
	private String pfIcon;
	private SuperSoldier superSoldier;
	private List<PlayerHero> heroList;
	public static final SFPlayer DEFAULT_INSTANCE = new SFPlayer(HawkXID.nullXid());

	public SFPlayer(HawkXID xid) {
		super(xid);
		playerId = BattleService.NPC_ID;
		playerData = new NPCPlayerData(playerId);
		playerEffect = new SFEffect(this, playerData);
		name = "";
		pfIcon = "";
		heroList = Collections.emptyList();
	}

	public void setHeros(List<PlayerHero> heros) {
		this.heroList = heros;
	}

	public void setSuperSoldier(int soldierId, int star) {
		this.superSoldier = NPCHeroFactory.getInstance().getSuperSoldier(soldierId, star);
	}

	/**取得指定页套装*/
	@Override
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return ArmourBriefInfo.getDefaultInstance();
	}

	@Override
	public int getSoldierStar(int armyId) {
		return 10;
	}

	@Override
	public int getSoldierStep(int armyId) {
		return 3;
	}

	@Override
	public int getSoldierPlantSkillLevel(int armyId) {
		return 0;
	}

	@Override
	public int getSoldierPlantMilitaryLevel(int armyId) {
		return 5;
	}

	@Override
	public int getMaxSoldierPlantMilitaryLevel() {
		return 5;
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
		return playerId;
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
	 * 注意如果没有setHeros 则id是foggy_hero中的id . 如果事先有调用setHeros id 对应 hero.xml
	 */
	@Override
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {
		for (PlayerHero hero : this.heroList) {
			if (hero.getCfgId() == heroId) {
				return Optional.ofNullable(hero);
			}
		}
		return Optional.ofNullable(null);
	}

	@Override
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {
		HawkAssert.notNull(heroIdList, "heroIdList must not be null! use empty instead");
		List<PlayerHero> heros = new ArrayList<>();
		for (int heroId : heroIdList) {
			Optional<PlayerHero> optional = this.getHeroByCfgId(heroId);
			if (optional.isPresent()) {
				heros.add(optional.get());
			}
		}
		return heros;
	}

	@Override
	public Optional<SuperSoldier> getSuperSoldierByCfgId(int soldierId) {
		if (Objects.nonNull(this.superSoldier) && this.superSoldier.getCfgId() == soldierId) {
			return Optional.of(superSoldier);
		}
		return Optional.empty();
	}

	@Override
	public List<PlayerHero> getAllHero() {
		return heroList;
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
		return false;
	}

	public void setPlayerId(String playerId) {
		this.playerId = BattleService.NPC_ID + playerId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPfIcon(String pfIcon) {
		this.pfIcon = pfIcon;
	}

}
