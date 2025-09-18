package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.entifytype.EntityType;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.config.ArmourAdditionalCfg;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourPoolCfg;
import com.hawk.game.config.FoggyArmourCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.item.ArmourAttrTemplate;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.RandomUtil;
import com.hawk.log.Action;

public class NpcPlayer extends Player {
	private PlayerData playerData;
	private String playerId;
	private NPCEffect playerEffect;
	private int playerPos;
	private String name;
	private String pfIcon;
	private SuperSoldier superSoldier;
	private List<PlayerHero> heroList;

	public static final NpcPlayer DEFAULT_INSTANCE = new NpcPlayer(HawkXID.nullXid());

	public NpcPlayer(HawkXID xid) {
		super(xid);
		playerId = BattleService.NPC_ID;
		playerData = new NPCPlayerData(playerId);
		playerEffect = new NPCEffect(this, playerData);
		name = "";
		pfIcon = "";
		heroList = Collections.emptyList();
	}

	@Override
	public int increaseNationMilitary(int addCnt, int resType, Action action, boolean needLog) {
		return 0;
	}
	
	public void setHeros(List<PlayerHero> heros){
		this.heroList = heros;
	}
	public void setSuperSoldier(int soldierId, int star) {
		this.superSoldier = NPCHeroFactory.getInstance().getSuperSoldier(soldierId, star);
	}

	public void setArmour(List<Integer> armourList) {
		for (Integer key : armourList) {
			try {
				addArmour(HawkConfigManager.getInstance().getConfigByKey(FoggyArmourCfg.class, key));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	public int addEffectVal(EffType effType, int val) {
		return playerEffect.addEffectVal(effType, val);
	}

	/**取得指定页套装*/
	@Override
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return super.genArmourBriefInfo(suit);
	}

	public void addArmour(FoggyArmourCfg cfg) {
		ArmourPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, cfg.getArmourPoolId());
		if (poolCfg == null) {
			logger.error("add armour error, armourPoolId not exit, playerId:{}, armourPoolId:{}", getId(), cfg.getArmourPoolId());
			return;
		}

		ArmourCfg amourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, poolCfg.getArmourId());
		if (amourCfg == null) {
			logger.error("add armour error, armourId not exit, playerId:{}, armourId:{}", getId(), poolCfg.getArmourId());
			return;
		}

		ArmourEntity entity = new ArmourEntity();
		entity.setId(HawkUUIDGenerator.genUUID());
		entity.setPlayerId(getId());
		entity.setArmourId(poolCfg.getArmourId());
		entity.setQuality(poolCfg.getQuality());
		entity.setPersistable(false);
		entity.setEntityType(EntityType.TEMPORARY);

		// 随机额外属性
		List<ArmourEffObject> extrAttrs = new ArrayList<>();

		// 常规铠甲额外属性随机
		List<Integer> alreadyRandType = new ArrayList<>();
		for (int key : cfg.getExtrAttrsList()) {
			ArmourAdditionalCfg randAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, key);
			ArmourAttrTemplate randAttr = RandomUtil.random(randAttrCfg.getAttrList());
			if (alreadyRandType.contains(randAttr.getEffect())) {
				continue;
			}
			extrAttrs.add(new ArmourEffObject(randAttrCfg.getId(), randAttr.getEffect(), HawkRand.randInt(randAttr.getRandMin(), randAttr.getRandMax())));
			alreadyRandType.add(randAttr.getEffect());
		}
		Collections.shuffle(extrAttrs);

		for (ArmourEffObject attr : extrAttrs) {
			entity.addExtraAttrEff(attr);
		}

		// 初始等级为0
		entity.setLevel(cfg.getLevel());
		entity.addSuit(ArmourSuitType.ONE_VALUE);
		getData().getArmourEntityList().add(entity);

	}

	@Override
	public int getSoldierStar(int armyId) {
		return 0;
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
	
	public int getCityPlantLv(){
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
		for(PlayerHero hero : this.heroList){
			if(hero.getCfgId() == heroId){
				return Optional.ofNullable(hero);
			}
		}
		return Optional.ofNullable(NPCHeroFactory.getInstance().get(heroId));
	}

	@Override
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {
		HawkAssert.notNull(heroIdList, "heroIdList must not be null! use empty instead");
		List<PlayerHero> heros = new ArrayList<>();
		for(int heroId : heroIdList){
			Optional<PlayerHero> optional = this.getHeroByCfgId(heroId);
			if(optional.isPresent()){
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

	@Override
	public int getSoldierStep(int armyId) {
		return 0;
	}
}