package com.hawk.game.module.lianmengtaiboliya.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Table;
import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.data.ProtectSoldierInfo;
import com.hawk.game.data.RevengeInfo;
import com.hawk.game.data.RevengeSoldierInfo;
import com.hawk.game.data.TimeLimitStoreConditionInfo;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYNpcCfg;
import com.hawk.game.module.lianmengtaiboliya.msg.QuitReason;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayerData;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayerPush;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZData;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.plantsoldier.science.PlantScience;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.msg.DailyDataClearMsg;
import com.hawk.game.msg.PlayerAssembleMsg;
import com.hawk.game.msg.PlayerLoginMsg;
import com.hawk.game.msg.RefreshEffectMsg;
import com.hawk.game.msg.RemoveArmourMsg;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.PlayerPush;
import com.hawk.game.player.PlayerSerializeData;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.manhattan.PlayerManhattan;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.tick.PlayerTickTimeLine;
import com.hawk.game.player.vipsuper.PlayerVipSuper;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Chat.HPChatState;
import com.hawk.game.protocol.Common.SystemEvent;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.PlayerStatus;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.game.protocol.World.SignatureState;
import com.hawk.game.protocol.World.WorldFavoritePB.Builder;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.WorldShowDress;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.health.entity.UpdateUserInfoResult;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.sdk.msdk.entity.PayItemInfo;

public class TBLYNpcPlayer extends ITBLYPlayer {
	private String playerId;
	private String name;
	private String pfIcon;
	private int icon;
	private String guildId;
	private String guildName;
	private String guildTag;
	private int cfgId;
	private final TBLYPlayer source;
	public TBLYNpcPlayer(TBLYPlayer player) {
		super(player.getXid());
		source = player;
	}

	public TBLYPlayer getSource() {
		return source;
	}

	public void init() {
		playerId = BattleService.NPC_ID + HawkUUIDGenerator.genUUID();
		name = "小帅";
		pfIcon = "";
		int maxCityDef = 100;
		this.setMaxCityDef(maxCityDef);
		this.setCityDefVal(maxCityDef);
		this.setPlayerData(TBLYNPCPlayerData.valueOf(this));
		TBLYNPCPlayerPush playerPush = new TBLYNPCPlayerPush(this);
		this.setPlayerPush(playerPush);
		getParent();

		this.setDeployedSwInfo(PBDeployedSwInfo.newBuilder());
		this.setMechacoreShowInfo("");

	}

	public TBLYNpcCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(TBLYNpcCfg.class, cfgId);
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	@Override
	public TBLYNPCPlayerEffect getEffect() {
		return (TBLYNPCPlayerEffect) getPlayerData().getPlayerEffect();
	}

	@Override
	public int increaseNationMilitary(int addCnt, int resType, Action action, boolean needLog) {
		return 0;
	}

	public void setArmour(List<Integer> armourList) {
	}

	/**取得指定页套装*/
	@Override
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return source.genArmourBriefInfo(suit);
	}

	@Override
	public int getMaxMarchSoldierNum(EffectParams effParams, boolean isNationMarch) {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getSoldierStar(int armyId) {
		return source.getSoldierStar(armyId);
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
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
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
		return guildId;
	}

	@Override
	public String getGuildName() {
		return guildName;
	}

	@Override
	public String getGuildTag() {
		return guildTag;
	}

	/**
	 * 注意如果没有setHeros 则id是foggy_hero中的id . 如果事先有调用setHeros id 对应 hero.xml
	 */
	@Override
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {
		return source.getHeroByCfgId(heroId);
	}

	@Override
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {
		return source.getHeroByCfgId(heroIdList);
	}

	@Override
	public Optional<SuperSoldier> getSuperSoldierByCfgId(int soldierId) {
		return source.getSuperSoldierByCfgId(soldierId);
	}

	@Override
	public List<PlayerHero> getAllHero() {
		return Collections.emptyList();
	}

	@Override
	public List<SuperSoldier> getAllSuperSoldier() {
		return new ArrayList<>();
	}

	/**
	 * 是否已经加入联盟
	 */
	public boolean hasGuild() {
		return true;
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
		return source.getSoldierStep(armyId);
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeWorldPoint() {
		// TODO Auto-generated method stub

	}

	@Override
	public double getMarchSpeedUp() {
		return 1.5;
	}

	@Override
	public void addFavorite(Builder favorite) {
		// TODO Auto-generated method stub
		super.addFavorite(favorite);
	}

	@Override
	public WorldPointType getPointType() {
		// TODO Auto-generated method stub
		return super.getPointType();
	}

	@Override
	public void moveCityCDSync() {
		// TODO Auto-generated method stub
		super.moveCityCDSync();
	}

	@Override
	public int getMarchCount() {
		// TODO Auto-generated method stub
		return super.getMarchCount();
	}

	@Override
	public boolean isInSameGuild(ITBLYPlayer tar) {
		// TODO Auto-generated method stub
		return super.isInSameGuild(tar);
	}

	@Override
	public ITBLYPlayer getLastAttacker() {
		// TODO Auto-generated method stub
		return super.getLastAttacker();
	}

	@Override
	public void setLastAttacker(ITBLYPlayer lastAttacker) {
		// TODO Auto-generated method stub
		super.setLastAttacker(lastAttacker);
	}

	@Override
	public List<ITBLYWorldMarch> assisReachMarches() {
		// TODO Auto-generated method stub
		return super.assisReachMarches();
	}

	@Override
	public void quitGame() {
		// TODO Auto-generated method stub
		super.quitGame();
	}

	@Override
	public com.hawk.game.protocol.World.WorldPointPB.Builder toBuilder(ITBLYPlayer viewer) {
		// TODO Auto-generated method stub
		return super.toBuilder(viewer);
	}

	@Override
	public com.hawk.game.protocol.World.WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		// TODO Auto-generated method stub
		return super.toDetailBuilder(viewer);
	}

	@Override
	public void setDeployedSwInfo(com.hawk.game.protocol.Manhattan.PBDeployedSwInfo.Builder deployedSwInfo) {
		// TODO Auto-generated method stub
		super.setDeployedSwInfo(deployedSwInfo);
	}

	@Override
	public com.hawk.game.protocol.Manhattan.PBDeployedSwInfo.Builder getDeployedSwInfo() {
		// TODO Auto-generated method stub
		return super.getDeployedSwInfo();
	}

	@Override
	public String getMechacoreShowInfo() {
		// TODO Auto-generated method stub
		return super.getMechacoreShowInfo();
	}

	@Override
	public void setMechacoreShowInfo(String mechacoreShowInfo) {
		// TODO Auto-generated method stub
		super.setMechacoreShowInfo(mechacoreShowInfo);
	}

	@Override
	public List<WorldShowDress> getShowDress() {
		// TODO Auto-generated method stub
		return super.getShowDress();
	}

	@Override
	public long getProtectedEndTime() {
		// TODO Auto-generated method stub
		return super.getProtectedEndTime();
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return super.getX();
	}

	@Override
	public int getPointId() {
		// TODO Auto-generated method stub
		return super.getPointId();
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return super.getY();
	}

	@Override
	public int[] getPos() {
		// TODO Auto-generated method stub
		return super.getPos();
	}

	@Override
	public void setPos(int[] pos) {
		// TODO Auto-generated method stub
		super.setPos(pos);
	}

	@Override
	public TBLYBattleRoom getParent() {
		// TODO Auto-generated method stub
		return super.getParent();
	}

	@Override
	public void setParent(TBLYBattleRoom parentRoom) {
		// TODO Auto-generated method stub
		super.setParent(parentRoom);
	}

	@Override
	public void sendError(int hpCode, int errCode, int errFlag, String... params) {
		// TODO Auto-generated method stub
		super.sendError(hpCode, errCode, errFlag, params);
	}

	@Override
	public void sendError(int hpCode, ProtocolMessageEnum errCode, int errFlag) {
		// TODO Auto-generated method stub
		super.sendError(hpCode, errCode, errFlag);
	}

	@Override
	public void sendError(int hpCode, ProtocolMessageEnum errCode) {
		// TODO Auto-generated method stub
		super.sendError(hpCode, errCode);
	}

	@Override
	public KeyValuePair<SignatureState, String> getSignatureInfo() {
		// TODO Auto-generated method stub
		return super.getSignatureInfo();
	}

	@Override
	public ITBLYPlayerData getPlayerData() {
		// TODO Auto-generated method stub
		return super.getPlayerData();
	}

	@Override
	public void setPlayerData(ITBLYPlayerData playerData) {
		// TODO Auto-generated method stub
		super.setPlayerData(playerData);
	}

	@Override
	public void setPlayerPush(ITBLYPlayerPush playerPush) {
		// TODO Auto-generated method stub
		super.setPlayerPush(playerPush);
	}

	@Override
	public ITBLYPlayerData getData() {
		// TODO Auto-generated method stub
		return super.getData();
	}

	@Override
	public ITBLYPlayerPush getPush() {
		// TODO Auto-generated method stub
		return super.getPush();
	}

	@Override
	public int getCityDefVal() {
		// TODO Auto-generated method stub
		return super.getCityDefVal();
	}

	@Override
	public int getRealMaxCityDef() {
		// TODO Auto-generated method stub
		return super.getRealMaxCityDef();
	}

	@Override
	public long getCityDefNextRepairTime() {
		// TODO Auto-generated method stub
		return super.getCityDefNextRepairTime();
	}

	@Override
	public long getOnFireEndTime() {
		// TODO Auto-generated method stub
		return super.getOnFireEndTime();
	}

	@Override
	public int getMaxCityDef() {
		// TODO Auto-generated method stub
		return super.getMaxCityDef();
	}

	@Override
	public void setMaxCityDef(int maxCityDef) {
		// TODO Auto-generated method stub
		super.setMaxCityDef(maxCityDef);
	}

	@Override
	public void setCityDefVal(int cityDefVal) {
		// TODO Auto-generated method stub
		super.setCityDefVal(cityDefVal);
	}

	@Override
	public void setCityDefNextRepairTime(long cityDefNextRepairTime) {
		// TODO Auto-generated method stub
		super.setCityDefNextRepairTime(cityDefNextRepairTime);
	}

	@Override
	public void setOnFireEndTime(long onFireEndTime) {
		// TODO Auto-generated method stub
		super.setOnFireEndTime(onFireEndTime);
	}

	@Override
	public void onMarchStart(ITBLYWorldMarch march) {
		// TODO Auto-generated method stub
		super.onMarchStart(march);
	}

	@Override
	public CAMP getCamp() {
		// TODO Auto-generated method stub
		return super.getCamp();
	}

	@Override
	public void setCamp(CAMP camp) {
		// TODO Auto-generated method stub
		super.setCamp(camp);
	}

	@Override
	public long getNextCityMoveTime() {
		// TODO Auto-generated method stub
		return super.getNextCityMoveTime();
	}

	@Override
	public void setNextCityMoveTime(long nextCityMoveTime) {
		// TODO Auto-generated method stub
		super.setNextCityMoveTime(nextCityMoveTime);
	}

	@Override
	public int getCostCityMoveCount() {
		// TODO Auto-generated method stub
		return super.getCostCityMoveCount();
	}

	@Override
	public void setCostCityMoveCount(int costCityMoveCount) {
		// TODO Auto-generated method stub
		super.setCostCityMoveCount(costCityMoveCount);
	}

	@Override
	public void incrementPlayerHonor(double honorAdd) {
		// TODO Auto-generated method stub
		super.incrementPlayerHonor(honorAdd);
	}

	@Override
	public void incrementGuildHonor(double honorAdd) {
		// TODO Auto-generated method stub
		super.incrementGuildHonor(honorAdd);
	}

	@Override
	public void incrementCollectGuildHonor(double honorAdd) {
		// TODO Auto-generated method stub
		super.incrementCollectGuildHonor(honorAdd);
	}

	@Override
	public void incrementBuildHonor(double honorAdd) {
		// TODO Auto-generated method stub
		super.incrementBuildHonor(honorAdd);
	}

	@Override
	public int getCollectGuildHonor() {
		// TODO Auto-generated method stub
		return super.getCollectGuildHonor();
	}

	@Override
	public void setCollectHonor(double collectHonor) {
		// TODO Auto-generated method stub
		super.setCollectHonor(collectHonor);
	}

	@Override
	public int getHonor() {
		// TODO Auto-generated method stub
		return super.getHonor();
	}

	@Override
	public int getHurtHonor() {
		// TODO Auto-generated method stub
		return super.getHurtHonor();
	}

	@Override
	public int getKillHonor() {
		// TODO Auto-generated method stub
		return super.getKillHonor();
	}

	@Override
	public void setHonor(double collectHonor) {
		// TODO Auto-generated method stub
		super.setHonor(collectHonor);
	}

	@Override
	public int getGuildHonor() {
		// TODO Auto-generated method stub
		return super.getGuildHonor();
	}

	@Override
	public void setGuildHonor(double collectGuildHonor) {
		// TODO Auto-generated method stub
		super.setGuildHonor(collectGuildHonor);
	}

	@Override
	public int getGameMoveCityCount() {
		// TODO Auto-generated method stub
		return super.getGameMoveCityCount();
	}

	@Override
	public void setGameMoveCityCount(int gameMoveCityCount) {
		// TODO Auto-generated method stub
		super.setGameMoveCityCount(gameMoveCityCount);
	}

	@Override
	public double getKillPower() {
		// TODO Auto-generated method stub
		return super.getKillPower();
	}

	@Override
	public void setKillPower(double killPower) {
		// TODO Auto-generated method stub
		super.setKillPower(killPower);
	}

	@Override
	public double getHurtTankPower() {
		// TODO Auto-generated method stub
		return super.getHurtTankPower();
	}

	@Override
	public void setHurtTankPower(double hurtTankPower) {
		// TODO Auto-generated method stub
		super.setHurtTankPower(hurtTankPower);
	}

	@Override
	public ITBLYPlayerPush getPlayerPush() {
		// TODO Auto-generated method stub
		return super.getPlayerPush();
	}

	@Override
	public QuitReason getQuitReason() {
		// TODO Auto-generated method stub
		return super.getQuitReason();
	}

	@Override
	public void setQuitReason(QuitReason quitReason) {
		// TODO Auto-generated method stub
		super.setQuitReason(quitReason);
	}

	@Override
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	@Override
	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}

	@Override
	public int getGuildFlag() {
		return 0;
	}

	@Override
	public void setGuildFlag(int guildFlag) {
	}

	@Override
	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	@Override
	public List<Builder> getFavoriteList() {
		// TODO Auto-generated method stub
		return super.getFavoriteList();
	}

	@Override
	public boolean isAnchor() {
		// TODO Auto-generated method stub
		return super.isAnchor();
	}

	@Override
	public int getEmoticon() {
		// TODO Auto-generated method stub
		return super.getEmoticon();
	}

	@Override
	public void setEmoticon(int emoticon) {
		// TODO Auto-generated method stub
		super.setEmoticon(emoticon);
	}

	@Override
	public long getEmoticonUseTime() {
		// TODO Auto-generated method stub
		return super.getEmoticonUseTime();
	}

	@Override
	public void setEmoticonUseTime(long emoticonUseTime) {
		// TODO Auto-generated method stub
		super.setEmoticonUseTime(emoticonUseTime);
	}

	@Override
	public long getSuperVipSkinEffEndTime() {
		// TODO Auto-generated method stub
		return super.getSuperVipSkinEffEndTime();
	}

	@Override
	public void setSuperVipSkinEffEndTime(long superVipSkinEffEndTime) {
		// TODO Auto-generated method stub
		super.setSuperVipSkinEffEndTime(superVipSkinEffEndTime);
	}

	@Override
	public int getSuperVipSkinLevel() {
		// TODO Auto-generated method stub
		return super.getSuperVipSkinLevel();
	}

	@Override
	public void setSuperVipSkinLevel(int superVipSkinLevel) {
		// TODO Auto-generated method stub
		super.setSuperVipSkinLevel(superVipSkinLevel);
	}

	@Override
	public MarchSet getInviewMarchs() {
		// TODO Auto-generated method stub
		return super.getInviewMarchs();
	}

	@Override
	public void setInviewMarchs(MarchSet inviewMarchs) {
		// TODO Auto-generated method stub
		super.setInviewMarchs(inviewMarchs);
	}

	@Override
	public void updateEquipTechLevel(int techId, int level) {
		// TODO Auto-generated method stub
		super.updateEquipTechLevel(techId, level);
	}

	@Override
	public int[] getWorldMovePos() {
		// TODO Auto-generated method stub
		return super.getWorldMovePos();
	}

	@Override
	public void setWorldMovePos(int[] worldMovePos) {
		// TODO Auto-generated method stub
		super.setWorldMovePos(worldMovePos);
	}

	@Override
	public ITBLYWorldMarch getAssistanceMarch(String viewerId, int pointId) {
		// TODO Auto-generated method stub
		return super.getAssistanceMarch(viewerId, pointId);
	}

	@Override
	public int getBuildHonor() {
		// TODO Auto-generated method stub
		return super.getBuildHonor();
	}

	@Override
	public void setBuildHonor(double buildHonor) {
		// TODO Auto-generated method stub
		super.setBuildHonor(buildHonor);
	}

	@Override
	public int getKillMonster() {
		// TODO Auto-generated method stub
		return super.getKillMonster();
	}

	@Override
	public void setKillMonster(int killMonster) {
		// TODO Auto-generated method stub
		super.setKillMonster(killMonster);
	}

	@Override
	public void incKillMonster() {
		// TODO Auto-generated method stub
		super.incKillMonster();
	}

	@Override
	public void initModules() {
		// TODO Auto-generated method stub
		super.initModules();
	}

	@Override
	public void updateData(PlayerData playerData) {
		// TODO Auto-generated method stub
		super.updateData(playerData);
	}

	@Override
	public void setPlayerPush(PlayerPush playerPush) {
		// TODO Auto-generated method stub
		super.setPlayerPush(playerPush);
	}

	@Override
	public int getAoiObjId() {
		// TODO Auto-generated method stub
		return super.getAoiObjId();
	}

	@Override
	public void setAoiObjId(int aoiObjId) {
		// TODO Auto-generated method stub
		super.setAoiObjId(aoiObjId);
	}

	@Override
	public boolean isBackground() {
		// TODO Auto-generated method stub
		return super.isBackground();
	}

	@Override
	public void resetParam() {
		// TODO Auto-generated method stub
		super.resetParam();
	}

	@Override
	public void setBackground(long backgroundTime) {
		// TODO Auto-generated method stub
		super.setBackground(backgroundTime);
	}

	@Override
	public long getBackground() {
		// TODO Auto-generated method stub
		return super.getBackground();
	}

	@Override
	public JSONObject getPfTokenJson() {
		// TODO Auto-generated method stub
		return super.getPfTokenJson();
	}

	@Override
	public void setPfTokenJson(JSONObject pfTokenJson) {
		// TODO Auto-generated method stub
		super.setPfTokenJson(pfTokenJson);
	}

	@Override
	public void clearData(boolean isRemove) {
		// TODO Auto-generated method stub
		super.clearData(isRemove);
	}

	@Override
	public PlayerEntity getEntity() {
		// TODO Auto-generated method stub
		return super.getEntity();
	}

	@Override
	public PlayerBaseEntity getPlayerBaseEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerBaseEntity();
	}

	@Override
	public long getCreateTime() {
		// TODO Auto-generated method stub
		return super.getCreateTime();
	}

	@Override
	public int getActiveState() {
		// TODO Auto-generated method stub
		return super.getActiveState();
	}

	@Override
	public void setActiveState(int activeState) {
		// TODO Auto-generated method stub
		super.setActiveState(activeState);
	}

	@Override
	public boolean isActiveOnline() {
		// TODO Auto-generated method stub
		return super.isActiveOnline();
	}

	@Override
	public void idipChangeDiamonds(boolean diamondsChange) {
		// TODO Auto-generated method stub
		super.idipChangeDiamonds(diamondsChange);
	}

	@Override
	public List<Integer> getUnlockedSoldierIds() {
		// TODO Auto-generated method stub
		return super.getUnlockedSoldierIds();
	}

	@Override
	public String getPuid() {
		// TODO Auto-generated method stub
		return super.getPuid();
	}

	@Override
	public String getOpenId() {
		// TODO Auto-generated method stub
		return super.getOpenId();
	}

	@Override
	public String getMainServerId() {
		// TODO Auto-generated method stub
		return super.getMainServerId();
	}

	@Override
	public String getServerId() {
		// TODO Auto-generated method stub
		return super.getServerId();
	}

	@Override
	public String getNameEncoded() {
		// TODO Auto-generated method stub
		return super.getNameEncoded();
	}

	@Override
	public String getNameWithGuildTag() {
		// TODO Auto-generated method stub
		return super.getNameWithGuildTag();
	}

	@Override
	public int getShowVIPLevel() {
		// TODO Auto-generated method stub
		return super.getShowVIPLevel();
	}

	@Override
	public String getChannel() {
		// TODO Auto-generated method stub
		return super.getChannel();
	}

	@Override
	public String getCountry() {
		// TODO Auto-generated method stub
		return super.getCountry();
	}

	@Override
	public String getDeviceId() {
		// TODO Auto-generated method stub
		return super.getDeviceId();
	}

	@Override
	public int getExp() {
		// TODO Auto-generated method stub
		return super.getExp();
	}

	@Override
	public int getExpDec() {
		// TODO Auto-generated method stub
		return super.getExpDec();
	}

	@Override
	public int getVipExp() {
		// TODO Auto-generated method stub
		return super.getVipExp();
	}

	@Override
	public String getAppVersion() {
		// TODO Auto-generated method stub
		return super.getAppVersion();
	}

	@Override
	public String getLanguage() {
		// TODO Auto-generated method stub
		return super.getLanguage();
	}

	@Override
	public long getLoginTime() {
		// TODO Auto-generated method stub
		return super.getLoginTime();
	}

	@Override
	public long getLogoutTime() {
		// TODO Auto-generated method stub
		return super.getLogoutTime();
	}

	@Override
	public int getOnlineTimeHistory() {
		// TODO Auto-generated method stub
		return super.getOnlineTimeHistory();
	}

	@Override
	public String getClientIp() {
		// TODO Auto-generated method stub
		return super.getClientIp();
	}

	@Override
	public void setCityLevel(int level) {
		// TODO Auto-generated method stub
		super.setCityLevel(level);
	}

	@Override
	public String getGameAppId() {
		// TODO Auto-generated method stub
		return super.getGameAppId();
	}

	@Override
	public int getPlatId() {
		return 0;
	}

	@Override
	public String getChannelId() {
		// TODO Auto-generated method stub
		return super.getChannelId();
	}

	@Override
	public void setFirstLogin(int firstLogin) {
		// TODO Auto-generated method stub
		super.setFirstLogin(firstLogin);
	}

	@Override
	public int getFirstLogin() {
		// TODO Auto-generated method stub
		return super.getFirstLogin();
	}

	@Override
	public String getPlatform() {
		// TODO Auto-generated method stub
		return super.getPlatform();
	}

	@Override
	public String getPhoneInfo() {
		// TODO Auto-generated method stub
		return super.getPhoneInfo();
	}

	@Override
	public String getTelecomOper() {
		// TODO Auto-generated method stub
		return super.getTelecomOper();
	}

	@Override
	public String getNetwork() {
		// TODO Auto-generated method stub
		return super.getNetwork();
	}

	@Override
	public String getClientHardware() {
		// TODO Auto-generated method stub
		return super.getClientHardware();
	}

	@Override
	public int getGold() {
		// TODO Auto-generated method stub
		return super.getGold();
	}

	@Override
	public int getDiamonds() {
		// TODO Auto-generated method stub
		return super.getDiamonds();
	}

	@Override
	public int getVit() {
		// TODO Auto-generated method stub
		return super.getVit();
	}

	@Override
	public int getCoin() {
		// TODO Auto-generated method stub
		return super.getCoin();
	}

	@Override
	public int getCityLv() {
		// TODO Auto-generated method stub
		return super.getCityLv();
	}

	@Override
	public int getMilitaryRankLevel() {
		// TODO Auto-generated method stub
		return super.getMilitaryRankLevel();
	}

	@Override
	public int getVipLevel() {
		// TODO Auto-generated method stub
		return super.getVipLevel();
	}

	@Override
	public long[] getPlunderResAry(int[] RES_TYPE) {
		// TODO Auto-generated method stub
		return super.getPlunderResAry(RES_TYPE);
	}

	@Override
	public int getEffPerByResType(int resType, EffType... effTypes) {
		// TODO Auto-generated method stub
		return super.getEffPerByResType(resType, effTypes);
	}

	@Override
	public Map<Integer, Long> getResBuildOutput() {
		// TODO Auto-generated method stub
		return super.getResBuildOutput();
	}

	@Override
	public long getUnsafeOil() {
		// TODO Auto-generated method stub
		return super.getUnsafeOil();
	}

	@Override
	public void decResStoreByPercent(BuildingType buildType, double decPercent) {
		// TODO Auto-generated method stub
		super.decResStoreByPercent(buildType, decPercent);
	}

	@Override
	public long getGoldore() {
		// TODO Auto-generated method stub
		return super.getGoldore();
	}

	@Override
	public long getGoldoreUnsafe() {
		// TODO Auto-generated method stub
		return super.getGoldoreUnsafe();
	}

	@Override
	public long getOil() {
		// TODO Auto-generated method stub
		return super.getOil();
	}

	@Override
	public long getOilUnsafe() {
		// TODO Auto-generated method stub
		return super.getOilUnsafe();
	}

	@Override
	public long getSteel() {
		// TODO Auto-generated method stub
		return super.getSteel();
	}

	@Override
	public long getSteelUnsafe() {
		// TODO Auto-generated method stub
		return super.getSteelUnsafe();
	}

	@Override
	public long getTombarthite() {
		// TODO Auto-generated method stub
		return super.getTombarthite();
	}

	@Override
	public long getTombarthiteUnsafe() {
		// TODO Auto-generated method stub
		return super.getTombarthiteUnsafe();
	}

	@Override
	public long getSteelSpy() {
		// TODO Auto-generated method stub
		return super.getSteelSpy();
	}

	@Override
	public long getTombarthiteSpy() {
		// TODO Auto-generated method stub
		return super.getTombarthiteSpy();
	}

	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		// TODO Auto-generated method stub
		return super.sendProtocol(protocol);
	}

	@Override
	public boolean sendProtocol(HawkProtocol protocol, long delayTime) {
		// TODO Auto-generated method stub
		return super.sendProtocol(protocol, delayTime);
	}

	@Override
	public void responseSuccess(int hpCode) {
		// TODO Auto-generated method stub
		super.responseSuccess(hpCode);
	}

	@Override
	public void notifyPlayerKickout(int reason, String msg) {
		// TODO Auto-generated method stub
		super.notifyPlayerKickout(reason, msg);
	}

	@Override
	public void kickout(int reason, boolean notify, String msg) {
		// TODO Auto-generated method stub
		super.kickout(reason, notify, msg);
	}

	@Override
	public void lockPlayer() {
		// TODO Auto-generated method stub
		super.lockPlayer();
	}

	@Override
	public void unLockPlayer() {
		// TODO Auto-generated method stub
		super.unLockPlayer();
	}

	@Override
	public void unLockPlayer(int errorCode) {
		// TODO Auto-generated method stub
		super.unLockPlayer(errorCode);
	}

	@Override
	public boolean onTick() {
		// TODO Auto-generated method stub
		return super.onTick();
	}

	@Override
	public long onCityShieldChange(StatusDataEntity entity, long currentTime) {
		// TODO Auto-generated method stub
		return super.onCityShieldChange(entity, currentTime);
	}

	@Override
	public void cityShieldRemovePrepareNotice(StatusDataEntity entity, long leftTime) {
		// TODO Auto-generated method stub
		super.cityShieldRemovePrepareNotice(entity, leftTime);
	}

	@Override
	public void onBufChange(int statusId, long endTime) {
		// TODO Auto-generated method stub
		super.onBufChange(statusId, endTime);
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		return true;
	}

	@Override
	public boolean onMessage(HawkMsg msg) {
		// TODO Auto-generated method stub
		return super.onMessage(msg);
	}

	@Override
	protected void onProtocolException(int protocolId, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		super.onProtocolException(protocolId, errorCode, errorMsg);
	}

	@Override
	public void onSessionClosed() {
		// TODO Auto-generated method stub
		super.onSessionClosed();
	}

	@Override
	public boolean onPlayerAssembleMsg(PlayerAssembleMsg msg) {
		// TODO Auto-generated method stub
		return super.onPlayerAssembleMsg(msg);
	}

	@Override
	public boolean onPlayerLoginMsg(PlayerLoginMsg msg) {
		// TODO Auto-generated method stub
		return super.onPlayerLoginMsg(msg);
	}

	@Override
	public boolean doPlayerAssembleAndLogin(HawkSession session, HPLogin loginCmd) {
		// TODO Auto-generated method stub
		return super.doPlayerAssembleAndLogin(session, loginCmd);
	}

	@Override
	protected void makeRobotRich() {
		// TODO Auto-generated method stub
		super.makeRobotRich();
	}

	@Override
	public boolean onRobotAssembleMsg() {
		// TODO Auto-generated method stub
		return super.onRobotAssembleMsg();
	}

	@Override
	public boolean onRobotLoginMsg() {
		// TODO Auto-generated method stub
		return super.onRobotLoginMsg();
	}

	@Override
	public boolean onSessionClosedMsg(SessionClosedMsg msg) {
		// TODO Auto-generated method stub
		return super.onSessionClosedMsg(msg);
	}

	@Override
	public boolean onDailyDataClearMsg(DailyDataClearMsg msg) {
		// TODO Auto-generated method stub
		return super.onDailyDataClearMsg(msg);
	}

	@Override
	public void noticeSystemEvent(SystemEvent event) {
		// TODO Auto-generated method stub
		super.noticeSystemEvent(event);
	}

	@Override
	public void increaseVit(int count, Action action, boolean isRecover) {
		// TODO Auto-generated method stub
		super.increaseVit(count, action, isRecover);
	}

	@Override
	public int getMaxVit() {
		// TODO Auto-generated method stub
		return super.getMaxVit();
	}

	@Override
	public PlayerLevelExpCfg getCurPlayerLevelCfg() {
		// TODO Auto-generated method stub
		return super.getCurPlayerLevelCfg();
	}

	@Override
	public void increaseGold(long gold, Action action) {
		// TODO Auto-generated method stub
		super.increaseGold(gold, action);
	}

	@Override
	public void increaseGold(long gold, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseGold(gold, action, needLog);
	}

	@Override
	public boolean consumeGold(int needGold, Action action) {
		// TODO Auto-generated method stub
		return super.consumeGold(needGold, action);
	}

	@Override
	public boolean increaseDiamond(int diamond, Action action) {
		// TODO Auto-generated method stub
		return super.increaseDiamond(diamond, action);
	}

	@Override
	public boolean increaseDiamond(int diamond, Action action, String extendParam, String presentReason) {
		// TODO Auto-generated method stub
		return super.increaseDiamond(diamond, action, extendParam, presentReason);
	}

	@Override
	public String consumeDiamonds(int diamond, Action action, List<PayItemInfo> payItems) {
		// TODO Auto-generated method stub
		return super.consumeDiamonds(diamond, action, payItems);
	}

	@Override
	public void increaseCoin(int coin, Action action) {
		// TODO Auto-generated method stub
		super.increaseCoin(coin, action);
	}

	@Override
	public void increaseGuildContribution(int value, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseGuildContribution(value, action, needLog);
	}

	@Override
	public void increaseMilitaryScore(int value, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseMilitaryScore(value, action, needLog);
	}

	@Override
	public void increaseCyborgScore(int value, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseCyborgScore(value, action, needLog);
	}

	@Override
	public void increaseDYZZScore(int value, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseDYZZScore(value, action, needLog);
	}

	@Override
	public void increaseCrossTalentPoint(int value, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseCrossTalentPoint(value, action, needLog);
	}

	@Override
	public void consumeCoin(int coin, Action action) {
		// TODO Auto-generated method stub
		super.consumeCoin(coin, action);
	}

	@Override
	public List<ItemEntity> increaseTools(ItemInfo itemAdd, Action action, ItemCfg itemCfg, boolean countCheck) {
		// TODO Auto-generated method stub
		return super.increaseTools(itemAdd, action, itemCfg, countCheck);
	}

	@Override
	public List<ItemEntity> increaseTools(ItemInfo itemAdd, Action action, ItemCfg itemCfg) {
		// TODO Auto-generated method stub
		return super.increaseTools(itemAdd, action, itemCfg);
	}

	@Override
	public void consumeTool(String id, int itemType, int disCount, Action action) {
		// TODO Auto-generated method stub
		super.consumeTool(id, itemType, disCount, action);
	}

	@Override
	public void increaseResource(long addCnt, int resType, Action action) {
		// TODO Auto-generated method stub
		super.increaseResource(addCnt, resType, action);
	}

	@Override
	public void increaseResource(long addCnt, int resType, Action action, boolean needLog) {
		// TODO Auto-generated method stub
		super.increaseResource(addCnt, resType, action, needLog);
	}

	@Override
	public long getResByType(int type) {
		// TODO Auto-generated method stub
		return super.getResByType(type);
	}

	@Override
	public long getResbyType(PlayerAttr type) {
		// TODO Auto-generated method stub
		return super.getResbyType(type);
	}

	@Override
	public long getAllResByType(int type) {
		// TODO Auto-generated method stub
		return super.getAllResByType(type);
	}

	@Override
	public void consumeResource(long subCnt, int resType, Action action) {
		// TODO Auto-generated method stub
		super.consumeResource(subCnt, resType, action);
	}

	@Override
	public void consumeVit(int vit, Action action) {
		// TODO Auto-generated method stub
		super.consumeVit(vit, action);
	}

	@Override
	public void increaseLevel(int level, Action action) {
		// TODO Auto-generated method stub
		super.increaseLevel(level, action);
	}

	@Override
	public void decreaceVipExp(int subVipExp, Action action) {
		// TODO Auto-generated method stub
		super.decreaceVipExp(subVipExp, action);
	}

	@Override
	public void increaseVipExp(int addExp, Action action) {
		// TODO Auto-generated method stub
		super.increaseVipExp(addExp, action);
	}

	@Override
	public void decreaseExp(int subExp, Action action) {
		// TODO Auto-generated method stub
		super.decreaseExp(subExp, action);
	}

	@Override
	public void increaseExp(int exp, Action action, boolean push) {
		// TODO Auto-generated method stub
		super.increaseExp(exp, action, push);
	}

	@Override
	public int getMaxCaptiveNum() {
		// TODO Auto-generated method stub
		return super.getMaxCaptiveNum();
	}

	@Override
	public int getMaxMassJoinMarchNum() {
		// TODO Auto-generated method stub
		return super.getMaxMassJoinMarchNum();
	}

	@Override
	public int getMaxMassJoinMarchNum(boolean isNationMarch) {
		// TODO Auto-generated method stub
		return super.getMaxMassJoinMarchNum(isNationMarch);
	}

	@Override
	public int getMaxMassJoinMarchNum(IWorldMarch march) {
		// TODO Auto-generated method stub
		return super.getMaxMassJoinMarchNum(march);
	}

	@Override
	public int getMaxMarchSoldierNum(EffectParams effParams) {
		// TODO Auto-generated method stub
		return super.getMaxMarchSoldierNum(effParams);
	}

	@Override
	public int getMaxMarchNum() {
		// TODO Auto-generated method stub
		return super.getMaxMarchNum();
	}

	@Override
	public int getMaxAllMarchSoldierNum(EffectParams effParams) {
		// TODO Auto-generated method stub
		return super.getMaxAllMarchSoldierNum(effParams);
	}

	@Override
	public int getMaxAssistSoldier() {
		// TODO Auto-generated method stub
		return super.getMaxAssistSoldier();
	}

	@Override
	public int getRemainDisabledCap() {
		// TODO Auto-generated method stub
		return super.getRemainDisabledCap();
	}

	@Override
	public int getPlantRemainDisabledCap() {
		// TODO Auto-generated method stub
		return super.getPlantRemainDisabledCap();
	}

	@Override
	public int getCannonCap() {
		// TODO Auto-generated method stub
		return super.getCannonCap();
	}

	@Override
	public int getMaxCapNum() {
		// TODO Auto-generated method stub
		return super.getMaxCapNum();
	}

	@Override
	public int getPlantMaxCapNum() {
		// TODO Auto-generated method stub
		return super.getPlantMaxCapNum();
	}

	@Override
	public int getMarketBurden() {
		// TODO Auto-generated method stub
		return super.getMarketBurden();
	}

	@Override
	public void refreshPowerElectric(PowerChangeReason reason) {
		// TODO Auto-generated method stub
		super.refreshPowerElectric(reason);
	}

	@Override
	public void refreshPowerElectric(boolean isArmyCure, PowerChangeReason reason) {
		// TODO Auto-generated method stub
		super.refreshPowerElectric(isArmyCure, reason);
	}

	@Override
	public void joinGuild(String guildId, boolean isCreate) {
		// TODO Auto-generated method stub
		super.joinGuild(guildId, isCreate);
	}

	@Override
	public void quitGuild(String guildId) {
		// TODO Auto-generated method stub
		super.quitGuild(guildId);
	}

	@Override
	public String getGuildNumId() {
		// TODO Auto-generated method stub
		return super.getGuildNumId();
	}

	@Override
	public long getGuildContribution() {
		// TODO Auto-generated method stub
		return super.getGuildContribution();
	}

	@Override
	public void consumeGuildContribution(int value, Action action) {
		// TODO Auto-generated method stub
		super.consumeGuildContribution(value, action);
	}

	@Override
	public String getCollegeId() {
		// TODO Auto-generated method stub
		return super.getCollegeId();
	}

	@Override
	public int getCollegeAuth() {
		// TODO Auto-generated method stub
		return super.getCollegeAuth();
	}

	@Override
	public boolean hasCollege() {
		// TODO Auto-generated method stub
		return super.hasCollege();
	}

	@Override
	public long getMilitaryScore() {
		// TODO Auto-generated method stub
		return super.getMilitaryScore();
	}

	@Override
	public void consumeMilitaryScore(long value, Action action) {
		// TODO Auto-generated method stub
		super.consumeMilitaryScore(value, action);
	}

	@Override
	public long getCyborgScore() {
		// TODO Auto-generated method stub
		return super.getCyborgScore();
	}

	@Override
	public void consumeCyborgScore(long value, Action action) {
		// TODO Auto-generated method stub
		super.consumeCyborgScore(value, action);
	}

	@Override
	public long getDYZZScore() {
		// TODO Auto-generated method stub
		return super.getDYZZScore();
	}

	@Override
	public void consumeDYZZScore(long value, Action action) {
		// TODO Auto-generated method stub
		super.consumeDYZZScore(value, action);
	}

	@Override
	public int getGuildAuthority() {
		// TODO Auto-generated method stub
		return super.getGuildAuthority();
	}

	@Override
	public String getGuildLeaderName() {
		// TODO Auto-generated method stub
		return super.getGuildLeaderName();
	}

	@Override
	public String getGuildLeaderId() {
		// TODO Auto-generated method stub
		return super.getGuildLeaderId();
	}

	@Override
	public int getFreeBuildingTime() {
		// TODO Auto-generated method stub
		return super.getFreeBuildingTime();
	}

	@Override
	public int getFreeTechTime() {
		// TODO Auto-generated method stub
		return super.getFreeTechTime();
	}

	@Override
	public void unlockArea(int buildAreaId) {
		// TODO Auto-generated method stub
		super.unlockArea(buildAreaId);
	}

	@Override
	public boolean isZeroEarningState() {
		// TODO Auto-generated method stub
		return super.isZeroEarningState();
	}

	@Override
	public long getZeroEarningTime() {
		// TODO Auto-generated method stub
		return super.getZeroEarningTime();
	}

	@Override
	public long getOilConsumeTime() {
		// TODO Auto-generated method stub
		return super.getOilConsumeTime();
	}

	@Override
	public void sendIDIPZeroEarningMsg() {
		// TODO Auto-generated method stub
		super.sendIDIPZeroEarningMsg();
	}

	@Override
	public void sendIDIPZeroEarningMsg(int msgCode) {
		// TODO Auto-generated method stub
		super.sendIDIPZeroEarningMsg(msgCode);
	}

	@Override
	public Optional<PlayerShopEntity> getShopById(int shopId) {
		// TODO Auto-generated method stub
		return super.getShopById(shopId);
	}

	@Override
	public PlantSoldierSchool getPlantSoldierSchool() {
		// TODO Auto-generated method stub
		return source.getPlantSoldierSchool();
	}

	@Override
	public PlayerManhattan getManhattanBase() {
		// TODO Auto-generated method stub
		return source.getManhattanBase();
	}

	@Override
	public List<PlayerManhattan> getAllManhattanSW() {
		// TODO Auto-generated method stub
		return source.getAllManhattanSW();
	}

	@Override
	public PlayerManhattan getManhattanSWByCfgId(int cfgId) {
		// TODO Auto-generated method stub
		return source.getManhattanSWByCfgId(cfgId);
	}

	@Override
	public boolean manhattanSWContains(Set<Integer> desiredIds) {
		// TODO Auto-generated method stub
		return super.manhattanSWContains(desiredIds);
	}

	@Override
	public PlayerMechaCore getPlayerMechaCore() {
		// TODO Auto-generated method stub
		return source.getPlayerMechaCore();
	}

	@Override
	public MechaCoreModuleEntity getMechaCoreModuleEntity(String moduleUuid) {
		// TODO Auto-generated method stub
		return source.getMechaCoreModuleEntity(moduleUuid);
	}

	@Override
	public void productResourceQuickly(long timeLong, boolean safe) {
		// TODO Auto-generated method stub
		super.productResourceQuickly(timeLong, safe);
	}

	@Override
	public boolean collectResource(String buildingId, int buildCfgId, long timeLong, AwardItems award, boolean safe) {
		// TODO Auto-generated method stub
		return super.collectResource(buildingId, buildCfgId, timeLong, award, safe);
	}

	@Override
	public void addRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award, boolean safe) {
		// TODO Auto-generated method stub
		super.addRes(addTotalOre, addTotalOil, addTotalSteel, addTotalRare, award, safe);
	}

	@Override
	public void addSafeRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award) {
		// TODO Auto-generated method stub
		super.addSafeRes(addTotalOre, addTotalOil, addTotalSteel, addTotalRare, award);
	}

	@Override
	public int getHeroEffectValue(int heroId, EffType eType) {
		// TODO Auto-generated method stub
		return super.getHeroEffectValue(heroId, eType);
	}

	@Override
	public List<Integer> getCastedSkill() {
		// TODO Auto-generated method stub
		return super.getCastedSkill();
	}

	@Override
	public void updateRankScore(int msgId, RankType rankType, long score) {
		// TODO Auto-generated method stub
		super.updateRankScore(msgId, rankType, score);
	}

	@Override
	public void removeSkillBuff(int skillId) {
		// TODO Auto-generated method stub
		super.removeSkillBuff(skillId);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId, long endTime) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId, endTime);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId, String targetId) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId, targetId);
	}

	@Override
	public void removeCityShield() {
		// TODO Auto-generated method stub
		super.removeCityShield();
	}

	@Override
	public List<ArmyEntity> defArmy() {
		// TODO Auto-generated method stub
		return super.defArmy();
	}

	@Override
	public List<ArmyEntity> marchArmy() {
		// TODO Auto-generated method stub
		return super.marchArmy();
	}

	@Override
	public boolean isRobot() {
		// TODO Auto-generated method stub
		return super.isRobot();
	}

	@Override
	public Set<String> getShieldPlayers() {
		// TODO Auto-generated method stub
		return super.getShieldPlayers();
	}

	@Override
	public void addShieldPlayer(String shieldPlayerId) {
		// TODO Auto-generated method stub
		super.addShieldPlayer(shieldPlayerId);
	}

	@Override
	public void removeShieldPlayer(String shieldPlayerId) {
		// TODO Auto-generated method stub
		super.removeShieldPlayer(shieldPlayerId);
	}

	@Override
	public List<Integer> getUnlockedResourceType() {
		// TODO Auto-generated method stub
		return super.getUnlockedResourceType();
	}

	@Override
	public boolean isSecondaryBuildQueueUsable() {
		// TODO Auto-generated method stub
		return super.isSecondaryBuildQueueUsable();
	}

	@Override
	public void refreshEffectEvent(RefreshEffectMsg msg) {
		// TODO Auto-generated method stub
		super.refreshEffectEvent(msg);
	}

	@Override
	public void refreshVipBenefitBox() {
		// TODO Auto-generated method stub
		super.refreshVipBenefitBox();
	}

	@Override
	public int checkBalance() {
		// TODO Auto-generated method stub
		return super.checkBalance();
	}

	@Override
	public void rechargeSuccess(int playerSaveAmt, int rechargeAmt, int diamonds) {
		// TODO Auto-generated method stub
		super.rechargeSuccess(playerSaveAmt, rechargeAmt, diamonds);
	}

	@Override
	public String pay(int diamond, String actionName, List<PayItemInfo> payItems) {
		// TODO Auto-generated method stub
		return super.pay(diamond, actionName, payItems);
	}

	@Override
	public boolean cancelPay(int diamond, String billno) {
		// TODO Auto-generated method stub
		return super.cancelPay(diamond, billno);
	}

	@Override
	public int present(int diamond, String extendParam, String actionName, String presentReason) {
		// TODO Auto-generated method stub
		return super.present(diamond, extendParam, actionName, presentReason);
	}

	@Override
	public String payBuyItems(PayGiftCfg giftCfg) {
		// TODO Auto-generated method stub
		return super.payBuyItems(giftCfg);
	}

	@Override
	public boolean isLocker() {
		// TODO Auto-generated method stub
		return super.isLocker();
	}

	@Override
	public void setLocker(boolean isLocker) {
		// TODO Auto-generated method stub
		super.setLocker(isLocker);
	}

	@Override
	public void synPlayerStatus(PlayerStatus status) {
		// TODO Auto-generated method stub
		super.synPlayerStatus(status);
	}

	@Override
	public void synPlayerStatus(PlayerStatus status, int errorCode) {
		// TODO Auto-generated method stub
		super.synPlayerStatus(status, errorCode);
	}

	@Override
	public PlayerSerializeData getPlayerSerializeData() {
		// TODO Auto-generated method stub
		return super.getPlayerSerializeData();
	}

	@Override
	public String toAnchorJsonStr() {
		// TODO Auto-generated method stub
		return super.toAnchorJsonStr();
	}

	@Override
	public JSONObject toAnchorJsonObj() {
		// TODO Auto-generated method stub
		return super.toAnchorJsonObj();
	}

	@Override
	public void updateRemindTime() {
		// TODO Auto-generated method stub
		super.updateRemindTime();
	}

	@Override
	public void healthGameRemind(UpdateUserInfoResult userInfo) {
		// TODO Auto-generated method stub
		super.healthGameRemind(userInfo);
	}

	@Override
	public void sendHealthGameRemind(int type, int peroidTime, int restTime, long endTime) {
		// TODO Auto-generated method stub
		super.sendHealthGameRemind(type, peroidTime, restTime, endTime);
	}

	@Override
	public void sendHealthGameRemind(int type, long endTime, String zkTitle, String zkMsg, String zkTraceId, String jsonStr) {
		// TODO Auto-generated method stub
		super.sendHealthGameRemind(type, endTime, zkTitle, zkMsg, zkTraceId, jsonStr);
	}

	@Override
	public void increaseMilitaryRankExp(int add, Action action) {
		// TODO Auto-generated method stub
		super.increaseMilitaryRankExp(add, action);
	}

	@Override
	public void addMoneyReissueItem(int moneyCount, Action action, String additionalParam) {
		// TODO Auto-generated method stub
		super.addMoneyReissueItem(moneyCount, action, additionalParam);
	}

	@Override
	public String getAccessToken() {
		// TODO Auto-generated method stub
		return super.getAccessToken();
	}

	@Override
	public int getToolBackGoldToday(int golds) {
		// TODO Auto-generated method stub
		return super.getToolBackGoldToday(golds);
	}

	@Override
	public int getPlayerRegisterDays() {
		// TODO Auto-generated method stub
		return super.getPlayerRegisterDays();
	}

	@Override
	public HPChatState getChatState() {
		// TODO Auto-generated method stub
		return super.getChatState();
	}

	@Override
	public void setChatState(HPChatState chatState) {
		// TODO Auto-generated method stub
		super.setChatState(chatState);
	}

	@Override
	public void sendIdipMsg(String idipMsg) {
		// TODO Auto-generated method stub
		super.sendIdipMsg(idipMsg);
	}

	@Override
	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, int msgCode) {
		// TODO Auto-generated method stub
		super.sendIdipNotice(type, mode, relieveTime, msgCode);
	}

	@Override
	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, String msg) {
		// TODO Auto-generated method stub
		super.sendIdipNotice(type, mode, relieveTime, msg);
	}

	@Override
	public boolean isBuildingLockByYuri(int buildType, String buildIndex) {
		// TODO Auto-generated method stub
		return super.isBuildingLockByYuri(buildType, buildIndex);
	}

	@Override
	public boolean isBuildingLockByYuri(String buildIndex) {
		// TODO Auto-generated method stub
		return super.isBuildingLockByYuri(buildIndex);
	}

	@Override
	public int getMaxTrainNum() {
		// TODO Auto-generated method stub
		return super.getMaxTrainNum();
	}

	@Override
	public int getSoldierPlantSkillLevel(int armyId) {
		// TODO Auto-generated method stub
		return source.getSoldierPlantSkillLevel(armyId);
	}

	@Override
	public int getSoldierPlantMilitaryLevel(int armyId) {
		// TODO Auto-generated method stub
		return source.getSoldierPlantMilitaryLevel(armyId);
	}

	@Override
	public int getMaxSoldierPlantMilitaryLevel() {
		// TODO Auto-generated method stub
		return super.getMaxSoldierPlantMilitaryLevel();
	}

	@Override
	public LoginWay getLoginWay() {
		// TODO Auto-generated method stub
		return super.getLoginWay();
	}

	@Override
	public void setLoginWay(LoginWay loginWay) {
		// TODO Auto-generated method stub
		super.setLoginWay(loginWay);
	}

	@Override
	public long getLastPowerScore() {
		// TODO Auto-generated method stub
		return super.getLastPowerScore();
	}

	@Override
	public void setLastPowerScore(long lastPowerScore) {
		// TODO Auto-generated method stub
		super.setLastPowerScore(lastPowerScore);
	}

	@Override
	public String getLmjyRoomId() {
		// TODO Auto-generated method stub
		return super.getLmjyRoomId();
	}

	@Override
	public void setLmjyRoomId(String lmjyRoomId) {
		// TODO Auto-generated method stub
		super.setLmjyRoomId(lmjyRoomId);
	}

	@Override
	public PState getLmjyState() {
		// TODO Auto-generated method stub
		return super.getLmjyState();
	}

	@Override
	public void setLmjyState(PState lmjyState) {
		// TODO Auto-generated method stub
		super.setLmjyState(lmjyState);
	}

	@Override
	public String getSwRoomId() {
		// TODO Auto-generated method stub
		return super.getSwRoomId();
	}

	@Override
	public void setSwRoomId(String swRoomId) {
		// TODO Auto-generated method stub
		super.setSwRoomId(swRoomId);
	}

	@Override
	public SWState getSwState() {
		// TODO Auto-generated method stub
		return super.getSwState();
	}

	@Override
	public void setSwState(SWState swState) {
		// TODO Auto-generated method stub
		super.setSwState(swState);
	}

	@Override
	public long getBeAttacked() {
		// TODO Auto-generated method stub
		return super.getBeAttacked();
	}

	@Override
	public void setBeAttacked(long beAttacked) {
		// TODO Auto-generated method stub
		super.setBeAttacked(beAttacked);
	}

	@Override
	public boolean isCsPlayer() {
		// TODO Auto-generated method stub
		return super.isCsPlayer();
	}

	@Override
	public CsPlayer getCsPlayer() {
		// TODO Auto-generated method stub
		return super.getCsPlayer();
	}

	@Override
	public com.hawk.game.protocol.Login.HPLogin.Builder getHpLogin() {
		// TODO Auto-generated method stub
		return super.getHpLogin();
	}

	@Override
	public void setHpLogin(com.hawk.game.protocol.Login.HPLogin.Builder hpLogin) {
		// TODO Auto-generated method stub
		super.setHpLogin(hpLogin);
	}

	@Override
	public int getCrossStatus() {
		// TODO Auto-generated method stub
		return super.getCrossStatus();
	}

	@Override
	public void setCrossStatus(int crossStatus) {
		// TODO Auto-generated method stub
		super.setCrossStatus(crossStatus);
	}

	@Override
	public boolean isCrossStatus(int... crossStatuses) {
		// TODO Auto-generated method stub
		return super.isCrossStatus(crossStatuses);
	}

	@Override
	public long getCrossBackTime() {
		// TODO Auto-generated method stub
		return super.getCrossBackTime();
	}

	@Override
	public void setCrossBackTime(long crossBackTime) {
		// TODO Auto-generated method stub
		super.setCrossBackTime(crossBackTime);
	}

	@Override
	public Object getSyncObj() {
		// TODO Auto-generated method stub
		return super.getSyncObj();
	}

	@Override
	public void calcOilChangeEff(AwardItems items) {
		// TODO Auto-generated method stub
		super.calcOilChangeEff(items);
	}

	@Override
	public ProtectSoldierInfo getProtectSoldierInfo(boolean init) {
		// TODO Auto-generated method stub
		return super.getProtectSoldierInfo(init);
	}

	@Override
	public void setProtectSoldierInfo(ProtectSoldierInfo protectSoldierInfo) {
		// TODO Auto-generated method stub
		super.setProtectSoldierInfo(protectSoldierInfo);
	}

	@Override
	public void refreshProctectSoldierInfo() {
		// TODO Auto-generated method stub
		super.refreshProctectSoldierInfo();
	}

	@Override
	public RevengeInfo getRevengeInfo(boolean init) {
		// TODO Auto-generated method stub
		return super.getRevengeInfo(init);
	}

	@Override
	public void setRevengeInfo(RevengeInfo revengeInfo) {
		// TODO Auto-generated method stub
		super.setRevengeInfo(revengeInfo);
	}

	@Override
	public List<RevengeSoldierInfo> getLossTroopInfoList() {
		// TODO Auto-generated method stub
		return super.getLossTroopInfoList();
	}

	@Override
	public void resetRevengeInfo() {
		// TODO Auto-generated method stub
		super.resetRevengeInfo();
	}

	@Override
	public Map<Integer, Integer> getRevengeShopBuyInfo() {
		// TODO Auto-generated method stub
		return super.getRevengeShopBuyInfo();
	}

	@Override
	public int getAtkNianTimes(String nianUuid) {
		// TODO Auto-generated method stub
		return super.getAtkNianTimes(nianUuid);
	}

	@Override
	public void incrementAtkNianTimes(String nianUuid) {
		// TODO Auto-generated method stub
		super.incrementAtkNianTimes(nianUuid);
	}

	@Override
	public Map<Integer, TimeLimitStoreConditionInfo> getTimeLimitStoreConditionMap() {
		// TODO Auto-generated method stub
		return super.getTimeLimitStoreConditionMap();
	}

	@Override
	public TimeLimitStoreConditionInfo getTimeLimitStoreCondition(int triggerType) {
		// TODO Auto-generated method stub
		return super.getTimeLimitStoreCondition(triggerType);
	}

	@Override
	public void addTimeLimitStoreCondition(TimeLimitStoreConditionInfo conditionInfo) {
		// TODO Auto-generated method stub
		super.addTimeLimitStoreCondition(conditionInfo);
	}

	@Override
	public void resetTimeLimitStoreCondition() {
		// TODO Auto-generated method stub
		super.resetTimeLimitStoreCondition();
	}

	@Override
	public TimeLimitStoreConditionInfo getOnSellStoreCondition() {
		// TODO Auto-generated method stub
		return super.getOnSellStoreCondition();
	}

	@Override
	public void setOnSellStoreCondition(TimeLimitStoreConditionInfo onSellStoreCondition) {
		// TODO Auto-generated method stub
		super.setOnSellStoreCondition(onSellStoreCondition);
	}

	@Override
	public boolean isVipShopRedPoint() {
		// TODO Auto-generated method stub
		return super.isVipShopRedPoint();
	}

	@Override
	public void setVipShopRedPoint(boolean vipShopRedPoint) {
		// TODO Auto-generated method stub
		super.setVipShopRedPoint(vipShopRedPoint);
	}

	@Override
	public void refreshVipShopRedPoint() {
		// TODO Auto-generated method stub
		super.refreshVipShopRedPoint();
	}

	@Override
	public String getTBLYRoomId() {
		// TODO Auto-generated method stub
		return super.getTBLYRoomId();
	}

	@Override
	public void setTBLYRoomId(String tblyRoomId) {
		// TODO Auto-generated method stub
		super.setTBLYRoomId(tblyRoomId);
	}

	@Override
	public TBLYState getTBLYState() {
		// TODO Auto-generated method stub
		return super.getTBLYState();
	}

	@Override
	public void setTBLYState(TBLYState tblyState) {
		// TODO Auto-generated method stub
		super.setTBLYState(tblyState);
	}

	@Override
	public String getDungeonMap() {
		// TODO Auto-generated method stub
		return super.getDungeonMap();
	}

	@Override
	public boolean isInDungeonMap() {
		// TODO Auto-generated method stub
		return super.isInDungeonMap();
	}

	@Override
	public String getArmourSuit(int suitId, int pos) {
		// TODO Auto-generated method stub
		return super.getArmourSuit(suitId, pos);
	}

	@Override
	public Map<Integer, String> getArmourSuit(int suitId) {
		// TODO Auto-generated method stub
		return super.getArmourSuit(suitId);
	}

	@Override
	public Table<Integer, Integer, String> getArmourSuit() {
		// TODO Auto-generated method stub
		return super.getArmourSuit();
	}

	@Override
	public void wearArmour(int suit, int pos, String armourId) {
		// TODO Auto-generated method stub
		super.wearArmour(suit, pos, armourId);
	}

	@Override
	public void takeOffArmour(int suit, int pos) {
		// TODO Auto-generated method stub
		super.takeOffArmour(suit, pos);
	}

	@Override
	public Table<Integer, Integer, String> resetArmourSuit() {
		// TODO Auto-generated method stub
		return super.resetArmourSuit();
	}

	@Override
	public List<ArmourEntity> getSuitArmours(int suit) {
		// TODO Auto-generated method stub
		return super.getSuitArmours(suit);
	}

	@Override
	public void addArmour(Integer armourPoolId) {
		// TODO Auto-generated method stub
		super.addArmour(armourPoolId);
	}

	@Override
	public boolean removeArmour(RemoveArmourMsg msg) {
		// TODO Auto-generated method stub
		return super.removeArmour(msg);
	}

	@Override
	public Set<SoldierType> getMainForce() {
		// TODO Auto-generated method stub
		return super.getMainForce();
	}

	@Override
	public boolean isSuperLabActive(int labId) {
		// TODO Auto-generated method stub
		return super.isSuperLabActive(labId);
	}

	@Override
	public String getPlayerSecPasswd() {
		// TODO Auto-generated method stub
		return super.getPlayerSecPasswd();
	}

	@Override
	public void setPlayerSecPasswd(String playerSecPasswd) {
		// TODO Auto-generated method stub
		super.setPlayerSecPasswd(playerSecPasswd);
	}

	@Override
	public void setSecPasswdExpiryTime(long secPasswdExpiryTime) {
		// TODO Auto-generated method stub
		super.setSecPasswdExpiryTime(secPasswdExpiryTime);
	}

	@Override
	public long getSecPasswdExpiryTime() {
		// TODO Auto-generated method stub
		return super.getSecPasswdExpiryTime();
	}

	@Override
	public boolean canReceiveChristmasBox() {
		// TODO Auto-generated method stub
		return super.canReceiveChristmasBox();
	}

	@Override
	public int getReceivedChristmasBoxNumber() {
		// TODO Auto-generated method stub
		return super.getReceivedChristmasBoxNumber();
	}

	@Override
	public void receiveChristmasBox(int number) {
		// TODO Auto-generated method stub
		super.receiveChristmasBox(number);
	}

	@Override
	public void setCYBORGRoomId(String uuid) {
		// TODO Auto-generated method stub
		super.setCYBORGRoomId(uuid);
	}

	@Override
	public String getCYBORGRoomId() {
		// TODO Auto-generated method stub
		return super.getCYBORGRoomId();
	}

	@Override
	public void setCYBORGState(CYBORGState state) {
		// TODO Auto-generated method stub
		super.setCYBORGState(state);
	}

	@Override
	public CYBORGState getCYBORGState() {
		// TODO Auto-generated method stub
		return super.getCYBORGState();
	}

	@Override
	public int getPlayerGhostTowerStage() {
		// TODO Auto-generated method stub
		return super.getPlayerGhostTowerStage();
	}

	@Override
	public long getPlayerGhostTowerProductTime() {
		// TODO Auto-generated method stub
		return super.getPlayerGhostTowerProductTime();
	}

	@Override
	public void setPlayerGhostTowerStage(int stageId) {
		// TODO Auto-generated method stub
		super.setPlayerGhostTowerStage(stageId);
	}

	@Override
	public void setPlayerGhostTowerProductTime(long productTime) {
		// TODO Auto-generated method stub
		super.setPlayerGhostTowerProductTime(productTime);
	}

	@Override
	protected void onProtocolElapseDeny(HawkProtocol protocol) {
		// TODO Auto-generated method stub
		super.onProtocolElapseDeny(protocol);
	}

	@Override
	public int checkMonthCardPriceCut() {
		// TODO Auto-generated method stub
		return super.checkMonthCardPriceCut();
	}

	@Override
	public PlantScience getPlantScience() {
		// TODO Auto-generated method stub
		return super.getPlantScience();
	}

	@Override
	public PlayerYQZZData getPlayerYQZZData() {
		// TODO Auto-generated method stub
		return super.getPlayerYQZZData();
	}

	@Override
	public String getDYZZRoomId() {
		// TODO Auto-generated method stub
		return super.getDYZZRoomId();
	}

	@Override
	public void setDYZZRoomId(String dyzzRoomId) {
		// TODO Auto-generated method stub
		super.setDYZZRoomId(dyzzRoomId);
	}

	@Override
	public DYZZState getDYZZState() {
		// TODO Auto-generated method stub
		return super.getDYZZState();
	}

	@Override
	public void setDYZZState(DYZZState dyzzState) {
		// TODO Auto-generated method stub
		super.setDYZZState(dyzzState);
	}

	@Override
	public int getNoArmyPower() {
		// TODO Auto-generated method stub
		return super.getNoArmyPower();
	}

	@Override
	public long getStrength() {
		// TODO Auto-generated method stub
		return super.getStrength();
	}

	@Override
	public void updateNationRD(NationRedDot nationRedDot) {
		// TODO Auto-generated method stub
		super.updateNationRD(nationRedDot);
	}

	@Override
	public void updateNationRDAndNotify(NationRedDot nationRedDot) {
		// TODO Auto-generated method stub
		super.updateNationRDAndNotify(nationRedDot);
	}

	@Override
	public void rmNationRD(NationRedDot nationRedDot) {
		// TODO Auto-generated method stub
		super.rmNationRD(nationRedDot);
	}

	@Override
	public void rmNationRDAndNotify(NationRedDot nationRedDot) {
		// TODO Auto-generated method stub
		super.rmNationRDAndNotify(nationRedDot);
	}

	@Override
	public void syncNationRedDot(boolean checkModify) {
		// TODO Auto-generated method stub
		super.syncNationRedDot(checkModify);
	}

	@Override
	public void syncNationRedDot() {
		// TODO Auto-generated method stub
		super.syncNationRedDot();
	}

	@Override
	public PlayerVipSuper getSuperVipObject() {
		// TODO Auto-generated method stub
		return super.getSuperVipObject();
	}

	@Override
	public int getActivatedVipSuperLevel() {
		// TODO Auto-generated method stub
		return super.getActivatedVipSuperLevel();
	}

	@Override
	public int getSuperVipSkinActivatedLevel() {
		// TODO Auto-generated method stub
		return super.getSuperVipSkinActivatedLevel();
	}

	@Override
	public int getSuperVipLevel() {
		// TODO Auto-generated method stub
		return super.getSuperVipLevel();
	}

	@Override
	public String getYQZZRoomId() {
		// TODO Auto-generated method stub
		return super.getYQZZRoomId();
	}

	@Override
	public void setYQZZRoomId(String yqzzRoomId) {
		// TODO Auto-generated method stub
		super.setYQZZRoomId(yqzzRoomId);
	}

	@Override
	public YQZZState getYQZZState() {
		// TODO Auto-generated method stub
		return super.getYQZZState();
	}

	@Override
	public void setYQZZState(YQZZState yqzzState) {
		// TODO Auto-generated method stub
		super.setYQZZState(yqzzState);
	}

	@Override
	public String getIpBelongsAddr() {
		// TODO Auto-generated method stub
		return super.getIpBelongsAddr();
	}

	@Override
	public void initSendTimeLimitTool() {
		// TODO Auto-generated method stub
		super.initSendTimeLimitTool();
	}

	@Override
	public Map<Integer, Long> getSendTimeLimitTool() {
		// TODO Auto-generated method stub
		return super.getSendTimeLimitTool();
	}

	@Override
	public long getSendTimeLimitTool(int itemId) {
		// TODO Auto-generated method stub
		return super.getSendTimeLimitTool(itemId);
	}

	@Override
	public void updateSendTimeLimitTool(int itemId) {
		// TODO Auto-generated method stub
		super.updateSendTimeLimitTool(itemId);
	}

	@Override
	public boolean checkProtoCounter(int protoType) {
		// TODO Auto-generated method stub
		return super.checkProtoCounter(protoType);
	}

	@Override
	public boolean clearProtoCounter(int protoType) {
		// TODO Auto-generated method stub
		return super.clearProtoCounter(protoType);
	}

	@Override
	public StaffOfficerSkillCollection getStaffOffic() {
		// TODO Auto-generated method stub
		return source.getStaffOffic();
	}

	@Override
	public long getHealthGameUpdateTime() {
		// TODO Auto-generated method stub
		return super.getHealthGameUpdateTime();
	}

	@Override
	public void setHealthGameUpdateTime(long healthGameUpdateTime) {
		// TODO Auto-generated method stub
		super.setHealthGameUpdateTime(healthGameUpdateTime);
	}

	@Override
	public long getNextRemindTime() {
		// TODO Auto-generated method stub
		return super.getNextRemindTime();
	}

	@Override
	public void setNextRemindTime(long nextRemindTime) {
		// TODO Auto-generated method stub
		super.setNextRemindTime(nextRemindTime);
	}

	@Override
	public boolean isAdult() {
		// TODO Auto-generated method stub
		return super.isAdult();
	}

	@Override
	public void setAdult(boolean adult) {
		// TODO Auto-generated method stub
		super.setAdult(adult);
	}

	@Override
	public long getHealthGameTickTime() {
		// TODO Auto-generated method stub
		return super.getHealthGameTickTime();
	}

	@Override
	public void setHealthGameTickTime(long healthGameTickTime) {
		// TODO Auto-generated method stub
		super.setHealthGameTickTime(healthGameTickTime);
	}

	@Override
	public void setCareBanStartTime(long startTime) {
		// TODO Auto-generated method stub
		super.setCareBanStartTime(startTime);
	}

	@Override
	public void setScoreBatchTime(long scoreBatchTime) {
		// TODO Auto-generated method stub
		super.setScoreBatchTime(scoreBatchTime);
	}

	@Override
	public long getLastContinueOnlineTime() {
		// TODO Auto-generated method stub
		return super.getLastContinueOnlineTime();
	}

	@Override
	public void setLastContinueOnlineTime(long lastContinueOnlineTime) {
		// TODO Auto-generated method stub
		super.setLastContinueOnlineTime(lastContinueOnlineTime);
	}

	@Override
	public PlayerTickTimeLine getTickTimeLine() {
		// TODO Auto-generated method stub
		return super.getTickTimeLine();
	}

	@Override
	public int getGuildFormationChangeMark() {
		// TODO Auto-generated method stub
		return super.getGuildFormationChangeMark();
	}

	@Override
	public void setGuildFormationChangeMark(int mark) {
		// TODO Auto-generated method stub
		super.setGuildFormationChangeMark(mark);
	}

	@Override
	public void setRoleExchangeState(int state) {
		// TODO Auto-generated method stub
		super.setRoleExchangeState(state);
	}

	@Override
	public int getRoleExchangeState() {
		// TODO Auto-generated method stub
		return super.getRoleExchangeState();
	}

	@Override
	public XHJZState getXhjzState() {
		// TODO Auto-generated method stub
		return super.getXhjzState();
	}

	@Override
	public void setXhjzState(XHJZState xhjzState) {
		// TODO Auto-generated method stub
		super.setXhjzState(xhjzState);
	}

	@Override
	public String getXhjzRoomId() {
		// TODO Auto-generated method stub
		return super.getXhjzRoomId();
	}

	@Override
	public void setXhjzRoomId(String xhjzRoomId) {
		// TODO Auto-generated method stub
		super.setXhjzRoomId(xhjzRoomId);
	}

	@Override
	public com.hawk.game.protocol.Manhattan.PBManhattanInfo.Builder buildManhattanInfo() {
		// TODO Auto-generated method stub
		return source.buildManhattanInfo();
	}

	@Override
	public com.hawk.game.protocol.Manhattan.PBManhattanInfo.Builder buildManhattanInfo(PresetMarchManhattan presetMarchManhattan) {
		// TODO Auto-generated method stub
		return source.buildManhattanInfo(presetMarchManhattan);
	}

	@Override
	public boolean checkManhattanFuncUnlock() {
		// TODO Auto-generated method stub
		return source.checkManhattanFuncUnlock();
	}

	@Override
	public boolean checkMechacoreFuncUnlock() {
		// TODO Auto-generated method stub
		return source.checkMechacoreFuncUnlock();
	}

	@Override
	public com.hawk.game.protocol.MechaCore.PBMechaCoreInfo.Builder buildMechacoreInfo(MechaCoreSuitType suit) {
		// TODO Auto-generated method stub
		return source.buildMechacoreInfo(suit);
	}

	@Override
	public boolean isGetDressEnough() {
		// TODO Auto-generated method stub
		return super.isGetDressEnough();
	}

	@Override
	public void addGetDressNum() {
		// TODO Auto-generated method stub
		super.addGetDressNum();
	}

	@Override
	public int getRechargeTotal() {
		// TODO Auto-generated method stub
		return super.getRechargeTotal();
	}

	@Override
	public void rechargeTotalAdd(int amount) {
		// TODO Auto-generated method stub
		super.rechargeTotalAdd(amount);
	}

	@Override
	public String getFgylRoomId() {
		// TODO Auto-generated method stub
		return super.getFgylRoomId();
	}

	@Override
	public void setFgylRoomId(String fgylRoomId) {
		// TODO Auto-generated method stub
		super.setFgylRoomId(fgylRoomId);
	}

	@Override
	public FGYLState getFgylState() {
		// TODO Auto-generated method stub
		return super.getFgylState();
	}

	@Override
	public void setFgylState(FGYLState fgylState) {
		// TODO Auto-generated method stub
		super.setFgylState(fgylState);
	}

	@Override
	public String getXQHXRoomId() {
		// TODO Auto-generated method stub
		return super.getXQHXRoomId();
	}

	@Override
	public void setXQHXRoomId(String xqhxRoomId) {
		// TODO Auto-generated method stub
		super.setXQHXRoomId(xqhxRoomId);
	}

	@Override
	public XQHXState getXQHXState() {
		// TODO Auto-generated method stub
		return super.getXQHXState();
	}

	@Override
	public void setXQHXState(XQHXState xqhxState) {
		// TODO Auto-generated method stub
		super.setXQHXState(xqhxState);
	}

}
