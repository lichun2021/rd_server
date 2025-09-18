package com.hawk.game.module.lianmengyqzz.battleroom.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZMapBlock;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZBattleCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitReason;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZBattleStatics;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZBuildStayTime;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZFoggyHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZMonsterHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZPylonHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZResourceHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.YQZZArmyModule;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.YQZZIdleModule;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.YQZZMarchModule;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.YQZZWorldModule;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.guildformation.YQZZGuildFormationModule;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBase;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.EquipTechLevel;
import com.hawk.game.protocol.World.SignatureState;
import com.hawk.game.protocol.World.SuperVipSkinEffect;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.WorldShowDress;
import com.hawk.game.protocol.YQZZ.PBYQZZPlayerInfo;
import com.hawk.game.protocol.YQZZ.PBYQZZPlayerMoveCitySync;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;

public abstract class IYQZZPlayer extends Player implements IYQZZWorldPoint {
	private YQZZBattleRoom parent;
	private IYQZZPlayerData playerData;
	private IYQZZPlayerPush playerPush;
	private long giveupBuildCd;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	/** 最近一次功击者 */
	private IYQZZPlayer lastAttacker;
	private MarchSet inviewMarchs = new MarchSet();
	private int killCount;
	/** 城点 */
	private int[] pos = { 0, 0 };
	/** 下次可免费迁城时间 */
	private long nextCityMoveTime;
	private int costCityMoveCount;

	/** 击杀 */
	private double killPower;
	/** 受伤 */
	private double hurtTankPower;
	/**死兵总数 */
	private int deadCnt;

	/** 游戏中迁城总数 */
	private int gameMoveCityCount;
	private int emoticon;
	// 行军表情使用时间
	private long emoticonUseTime;

	List<WorldFavoritePB.Builder> favoriteList = new ArrayList<>();
	private int aoiObjId = 0;
	private YQZZPlayerEye eye;
	private YQZZQuitReason quitReason;
	/** 
	 * 至尊vip皮肤特效结束时间
	 */
	private long superVipSkinEffEndTime;
	private int superVipSkinLevel;
	private List<EquipTechLevel> builderList = new ArrayList<>();
	private PBDeployedSwInfo.Builder deployedSwInfo;
	private String mechacoreShowInfo;
	
	private YQZZ_CAMP camp;
	// 野怪击杀记录
	private Map<Integer, YQZZMonsterHonor> monsterHonorMap = new HashMap<>();
	private Map<YQZZBuildType, YQZZBuildStayTime> buildStayTimeMap = new HashMap<>();
	private Map<Integer, YQZZFoggyHonor> foggyHonorMap = new HashMap<>();
	private Map<Integer, YQZZResourceHonor> resourceHonorMap = new HashMap<>();
	private Map<Integer, YQZZBattleStatics> battleStatics = new HashMap<>();
	private Map<Integer, YQZZPylonHonor> pylonHonorMap = new HashMap<>();
	// 幽灵基地统计
	private long protectedEndTime;
	// 副本中获得军功
	private int nationMilitary;
	WorldPointPB.Builder secondMapBuilder;
	// 玩家所占3点的建筑
	private Set<IYQZZBuilding> subareaBuilds;
	/**死兵总数 */
	private int pushGiftDeadCnt;
	public IYQZZPlayer(HawkXID xid) {
		super(xid);
	}

	@Override
	public int getHashThread(int threadNum) {
		return getXid().getHashThread(threadNum);
	}

	@Override
	public int increaseNationMilitary(int addCnt, int resType, Action action, boolean needLog) {
		final int oldnationMilitary = nationMilitary;
		nationMilitary = Math.min(getParent().getCfg().getNationMilitaryMax(), nationMilitary + addCnt);
		return nationMilitary - oldnationMilitary;

//		if (addCnt <= 0) {
//			return 0;
//		}
//
//		if (resType == PlayerAttr.NATION_MILITARY_VALUE) {
//			return super.increaseNationMilitary(addCnt, resType, action, needLog);
//		}
//		addCnt = Math.min(addCnt, getParent().getCfg().getNationMilitaryMax() - nationMilitary);
//		if (addCnt <= 0) {
//			return 0;
//		}
//		NationMilitaryEntity commanderEntity = getData().getNationMilitaryEntity();
//		commanderEntity.setNationMilitaryExp(commanderEntity.getNationMilitaryExp() + addCnt);
//		nationMilitary += addCnt;
//		if (needLog) {// 资源流水日志
//			LogUtil.logResourceFlow(this, action, LogInfoType.resource_add, PlayerAttr.NATION_MILITARY_VALUE, commanderEntity.getNationMilitaryExp(), addCnt);
//		}
//		DungeonRedisLog.log(getId(), "addCnt {} total {} nationMilitary {} ", addCnt, nationMilitary, commanderEntity.getNationMilitaryExp());
//		return addCnt;
	}
	
	
	@Override
	public void add2ViewPoint() {
		IYQZZWorldPoint.super.add2ViewPoint();
		Set<Integer> set = new HashSet<>();
		Set<IYQZZBuilding> builds = new HashSet<>();
		this.fillWithOcuPointId(set);
		for (int pid : set) {
			IYQZZBuilding build = getParent().getWorldPointService().getBuildingBySubarea(YQZZMapBlock.getInstance().subareaNum(pid));
			if (Objects.nonNull(build)) {
				builds.add(build);
			}
		}
		this.setSubareaBuilds(builds);
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

	/**
	 * 是否有空闲出征队列
	 * 
	 * @return
	 */
	public boolean isHasFreeMarch() {
		// 机器人不受行军数量限制
		if (isRobot()) {
			return true;
		}
		List<IYQZZWorldMarch> playerMarches = getParent().getPlayerMarches(getId());
		// 行军个数
		int marchCount = 0;
		for (IYQZZWorldMarch march : playerMarches) {
			if (!march.isExtraSpyMarch()) {
				marchCount++;
			}
		}
		if (marchCount >= getMaxMarchNum()) {
			return false;
		}
		return true;
	}

	public void mergerFrom(IYQZZPlayer player) {
		this.killCount = player.killCount;
		this.nextCityMoveTime = player.nextCityMoveTime;
		this.costCityMoveCount = player.costCityMoveCount;
		this.killPower = player.killPower;
		this.hurtTankPower = player.hurtTankPower;
		this.deadCnt = player.deadCnt;
		this.gameMoveCityCount = player.gameMoveCityCount;
		this.emoticon = player.emoticon;
		this.emoticonUseTime = player.emoticonUseTime;
		this.favoriteList = player.favoriteList;
		this.superVipSkinEffEndTime = player.superVipSkinEffEndTime;
		this.superVipSkinLevel = player.superVipSkinLevel;
		// 野怪击杀记录
		this.monsterHonorMap = player.monsterHonorMap;
		this.buildStayTimeMap = player.buildStayTimeMap;
		this.foggyHonorMap = player.foggyHonorMap;
		this.resourceHonorMap = player.resourceHonorMap;
		this.nationMilitary = player.nationMilitary;
		this.battleStatics = player.battleStatics;
		this.giveupBuildCd = player.giveupBuildCd;
		this.pylonHonorMap = player.pylonHonorMap;
	}

	public void addFavorite(WorldFavoritePB.Builder favorite) {
		this.favoriteList.add(favorite);
	}

	public YQZZPylonHonor getPylonHonorStat(int pylonId) {
		if (!pylonHonorMap.containsKey(pylonId)) {
			YQZZPylonHonor value = new YQZZPylonHonor();
			value.setPylonId(pylonId);
			pylonHonorMap.put(pylonId, value);
		}
		return pylonHonorMap.get(pylonId);
	}

	public YQZZMonsterHonor getMonsterHonorStat(int monsterId) {
		if (!monsterHonorMap.containsKey(monsterId)) {
			YQZZMonsterHonor value = new YQZZMonsterHonor();
			value.setMonsterId(monsterId);
			monsterHonorMap.put(monsterId, value);
		}
		return monsterHonorMap.get(monsterId);
	}

	public YQZZResourceHonor getResourceHonorStat(int resType) {
		if (!resourceHonorMap.containsKey(resType)) {
			YQZZResourceHonor value = new YQZZResourceHonor();
			value.setResourceId(resType);
			resourceHonorMap.put(resType, value);
		}
		return resourceHonorMap.get(resType);
	}

	public YQZZFoggyHonor getFoggyHonorStat(int foggyFortressId) {
		if (!foggyHonorMap.containsKey(foggyFortressId)) {
			YQZZFoggyHonor value = new YQZZFoggyHonor();
			value.setFoggyFortressId(foggyFortressId);
			foggyHonorMap.put(foggyFortressId, value);
		}
		return foggyHonorMap.get(foggyFortressId);
	}

	public YQZZBuildStayTime getBuildControlTimeStat(YQZZBuildType buildType) {
		if (!buildStayTimeMap.containsKey(buildType)) {
			YQZZBuildStayTime value = new YQZZBuildStayTime();
			value.setBuildType(buildType);
			buildStayTimeMap.put(buildType, value);
		}
		return buildStayTimeMap.get(buildType);
	}
	
	public YQZZBattleStatics getBattleStaticsStat(int armyId) {
		if (!battleStatics.containsKey(armyId)) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class,armyId);
			YQZZBattleStatics value = new YQZZBattleStatics();
			value.setArmyId(armyId);
			value.setLevel(cfg.getLevel());
			battleStatics.put(armyId, value);
			
		}
		return battleStatics.get(armyId);
	}

	@Override
	public int getGridCnt() {
		return 2;
	}

	public abstract void init();
	
	@Override
	public void initModules() {
		registerModule(YQZZConst.ModuleType.YQZZWorld, new YQZZWorldModule(this));
		registerModule(YQZZConst.ModuleType.YQZZMarch, new YQZZMarchModule(this));
		registerModule(YQZZConst.ModuleType.YQZZArmy, new YQZZArmyModule(this));
		registerModule(YQZZConst.ModuleType.YQZZIdle, new YQZZIdleModule(this));
		registerModule(YQZZConst.ModuleType.YQZZGuildFormation, new YQZZGuildFormationModule(this));
	}

	/** 行军加速 */
	public abstract double getMarchSpeedUp();

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.PLAYER;
	}

	public void moveCityCDSync() {
		PBYQZZPlayerMoveCitySync.Builder builder = PBYQZZPlayerMoveCitySync.newBuilder();
		builder.setYqzzMoveCityCD(nextCityMoveTime);
		builder.setYqzzMoveCityCost(costCityMoveCount);

		sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_MOVE_CITY_CD_SYNC, builder));
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream().filter(m -> m.getParent() == this).count();
	}

	public boolean isInSameGuild(IYQZZPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	public IYQZZPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(IYQZZPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<IYQZZWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE);
	}

	public void quitGame() {

	}
	
	@Override
	public void worldPointUpdate() {
		secondMapBuilder = null;
		toSecondMapBuilder();
		IYQZZWorldPoint.super.worldPointUpdate();
	}

	@Override
	public WorldPointPB.Builder toSecondMapBuilder() {
		if (secondMapBuilder != null) {
			secondMapBuilder.setPointX(getPosXY()[0]);
			secondMapBuilder.setPointY(getPosXY()[1]);
			return secondMapBuilder;
		}
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);
		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setGuildId(getGuildId());

		secondMapBuilder = builder;
		return secondMapBuilder;
	}

	@Override
	public WorldPointPB.Builder toBuilder(IYQZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);
		builder.setOfficerId(GameUtil.getOfficerId(getId()));
		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setYqzzMoveCityCD(nextCityMoveTime);
		builder.setYqzzMoveCityCost(costCityMoveCount);
		builder.setFlagView(getCamp().intValue());
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		builder.setServerId(getMainServerId());
		if (!HawkOSOperator.isEmptyString(viewer.getId())) {
			if (getParent().isHasAssistanceMarch(viewer.getId(), getPointId())) {
				builder.setHasMarchStop(true);
			}
		}

		int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(thisPlayerId);
		if (cityBaseShow != 0) {
			builder.setShowEffect(cityBaseShow);
		}

		long timeNow = HawkTime.getMillisecond();
		// 装扮
		for (Entry<Integer, DressItem> entry : WorldPointService.getInstance().getShowDress(thisPlayerId).entrySet()) {
			WorldShowDress.Builder dressInfo = WorldShowDress.newBuilder();
			DressItem dressItem = entry.getValue();
			dressInfo.setDressType(DressType.valueOf(entry.getKey()));
			dressInfo.setModelType(dressItem.getModelType());
			dressInfo.setRemainShowTime(GameUtil.getDressRemainTime(dressItem) / 1000);
			if (dressItem.getShowType() != 0 && dressItem.getShowEndTime() > timeNow) {
				dressInfo.setShowType(dressItem.getShowType());
				dressInfo.setShowRemainTime((dressItem.getShowEndTime() - timeNow) / 1000);
			}
			builder.addDressShow(dressInfo);
		}

		AccountRoleInfo acRoleInfo = GlobalData.getInstance().getAccountRoleInfo(getId());
		if (Objects.nonNull(acRoleInfo)) {
			builder.setCityPlantLevel(acRoleInfo.getCityPlantLevel());
		}

		// 签名
		KeyValuePair<SignatureState, String> signatureInfo = getSignatureInfo();
		builder.setSignatureState(signatureInfo.getKey());
		if (signatureInfo.getKey().equals(SignatureState.CAN_SIGNATURE)) {
			builder.setSignature(signatureInfo.getValue());
		}

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());

		// 普通伤害结束时间点
		builder.setCommonEndTime(onFireEndTime);

		builder.setShowEquipTech(WorldPointService.getInstance().getShowEquipTech(thisPlayerId));
		BuilderUtil.buildPlayerEmotion(builder, getEmoticonUseTime(), getEmoticon());
		builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(getId()));
		builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(getId()));
		builder.setSuperVipSkin(superVipSkinBuilder());
		builder.addAllEquipTechLevel(equipTechLevelToBuilder());
		builder.setDeployedSwInfo(getDeployedSwInfo());
		builder.setMechacoreShowInfo(getMechacoreShowInfo());
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);
		builder.setOfficerId(GameUtil.getOfficerId(getId()));
		builder.setPlayerId(getId());
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setPlayerIcon(getIcon());
		builder.setPlayerPfIcon(getPfIcon());
		builder.setFlagView(getCamp().intValue());
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		builder.setServerId(getMainServerId());
		if (!HawkOSOperator.isEmptyString(viewer.getId())) {
			if (getParent().isHasAssistanceMarch(viewer.getId(), getPointId())) {
				builder.setHasMarchStop(true);
			}
		}

		int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(getId());
		if (cityBaseShow != 0) {
			builder.setShowEffect(cityBaseShow);
		}

		long timeNow = HawkTime.getMillisecond();
		// 装扮
		for (Entry<Integer, DressItem> entry : WorldPointService.getInstance().getShowDress(getId()).entrySet()) {
			WorldShowDress.Builder dressInfo = WorldShowDress.newBuilder();
			DressItem dressItem = entry.getValue();
			dressInfo.setDressType(DressType.valueOf(entry.getKey()));
			dressInfo.setModelType(dressItem.getModelType());
			dressInfo.setRemainShowTime(GameUtil.getDressRemainTime(dressItem) / 1000);
			if (dressItem.getShowType() != 0 && dressItem.getShowEndTime() > timeNow) {
				dressInfo.setShowType(dressItem.getShowType());
				dressInfo.setShowRemainTime((dressItem.getShowEndTime() - timeNow) / 1000);
			}
			builder.addDressShow(dressInfo);
		}

		// 签名
		KeyValuePair<SignatureState, String> signatureInfo = getSignatureInfo();
		builder.setSignatureState(signatureInfo.getKey());
		if (signatureInfo.getKey().equals(SignatureState.CAN_SIGNATURE)) {
			builder.setSignature(signatureInfo.getValue());
		}
		builder.setCommonEndTime(onFireEndTime);
		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		builder.setShowEquipTech(WorldPointService.getInstance().getShowEquipTech(getId()));
		BuilderUtil.buildPlayerEmotion(builder, getEmoticonUseTime(), getEmoticon());
		builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(getId()));
		builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(getId()));
		builder.setSuperVipSkin(superVipSkinBuilder());
		builder.addAllEquipTechLevel(equipTechLevelToBuilder());
		builder.setDeployedSwInfo(getDeployedSwInfo());
		builder.setMechacoreShowInfo(getMechacoreShowInfo());
		return builder;
	}

	private SuperVipSkinEffect.Builder superVipSkinBuilder() {
		SuperVipSkinEffect.Builder superVipBuilder = SuperVipSkinEffect.newBuilder();
		superVipBuilder.setSkinLevel(this.getSuperVipSkinLevel());
		superVipBuilder.setSkinEndTime(this.getSuperVipSkinEffEndTime());
		return superVipBuilder;
	}
	
	public void setDeployedSwInfo(PBDeployedSwInfo.Builder deployedSwInfo) {
		this.deployedSwInfo = deployedSwInfo;
	}
	
	public PBDeployedSwInfo.Builder getDeployedSwInfo() {
		return deployedSwInfo;
	}
	
	public String getMechacoreShowInfo() {
		return mechacoreShowInfo;
	}

	public void setMechacoreShowInfo(String mechacoreShowInfo) {
		this.mechacoreShowInfo = mechacoreShowInfo;
	}

	public YQZZBase getBase(){
		return getParent().getWorldPointService().getBaseByCamp(camp);
	}
	
	/**
	 * 装备科技等级
	 * 
	 * @return
	 */
	private List<EquipTechLevel> equipTechLevelToBuilder() {
		return builderList;
	}

	@Override
	public long getProtectedEndTime() {
		return protectedEndTime;
	}

	public void setProtectedEndTime(long protectedEndTime) {
		this.protectedEndTime = protectedEndTime;
	}

	public int getKillCount() {
		return killCount;
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
	}

	public void incKillCount() {
		this.killCount++;
	}

	@Override
	public int getPlayerPos() {
		return GameUtil.combineXAndY(pos[0], pos[1]);
	}

	@Override
	public int[] getPosXY() {
		return pos;
	}

	@Override
	public int getX() {
		return pos[0];
	}

	@Override
	public int getPointId() {
		return getPlayerPos();
	}

	@Override
	public int getY() {
		return pos[1];
	}

	public int[] getPos() {
		return pos;
	}

	public void setPos(int[] pos) {
		this.pos = pos;
	}

	@Override
	public YQZZBattleRoom getParent() {
		return parent;
	}

	public void setParent(YQZZBattleRoom parentRoom) {
		this.parent = parentRoom;
	}

	@Override
	public void sendError(int hpCode, int errCode, int errFlag, String... params) {

	}

	@Override
	public void sendError(int hpCode, ProtocolMessageEnum errCode, int errFlag) {

	}

	public void sendError(int hpCode, ProtocolMessageEnum errCode) {
	}

	public KeyValuePair<SignatureState, String> getSignatureInfo() {
		return WorldPointService.getInstance().getPlayerSignatureInfo(getId());
	}

	public IYQZZPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(IYQZZPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(IYQZZPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public IYQZZPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public IYQZZPlayerPush getPush() {

		return playerPush;
	}

	public int getCityDefVal() {
		return cityDefVal;
	}

	public int getRealMaxCityDef() {
		return maxCityDef;
	}

	public long getCityDefNextRepairTime() {
		return cityDefNextRepairTime;
	}

	public long getOnFireEndTime() {
		return onFireEndTime;
	}

	public int getMaxCityDef() {
		return maxCityDef;
	}

	public void setMaxCityDef(int maxCityDef) {
		if (isRobot()) {
			maxCityDef = 10000;
		}
		this.maxCityDef = maxCityDef;
	}

	public void setCityDefVal(int cityDefVal) {
		this.cityDefVal = Math.min(cityDefVal, maxCityDef);
	}

	public void setCityDefNextRepairTime(long cityDefNextRepairTime) {
		this.cityDefNextRepairTime = cityDefNextRepairTime;
	}

	public void setOnFireEndTime(long onFireEndTime) {
		this.onFireEndTime = onFireEndTime;
	}

	public void onMarchStart(IYQZZWorldMarch march) {

	}

	public long getNextCityMoveTime() {
		return nextCityMoveTime;
	}

	public void setNextCityMoveTime(long nextCityMoveTime) {
		this.nextCityMoveTime = nextCityMoveTime;
	}

	public int getCostCityMoveCount() {
		return costCityMoveCount;
	}

	public void setCostCityMoveCount(int costCityMoveCount) {
		this.costCityMoveCount = Math.min(1, costCityMoveCount);
	}

	public int getHonor() {
		int honor = (int) (monsterHonorMap.values().stream().mapToDouble(YQZZMonsterHonor::getPlayerHonor).sum()
				+ resourceHonorMap.values().stream().mapToDouble(YQZZResourceHonor::getPlayerHonor).sum()
				+ foggyHonorMap.values().stream().mapToDouble(YQZZFoggyHonor::getPlayerHonor).sum()
				+ pylonHonorMap.values().stream().mapToDouble(YQZZPylonHonor::getPlayerHonor).sum()
				);
		YQZZBattleCfg cfg = getParent().getCfg();
		int armyHonor = (int) (killPower / cfg.getScoreForKill() + hurtTankPower / cfg.getScoreForDefense());
		int buildHonor = getParent().getBaseByCamp(getCamp()).getNationPlayerHonor(); // getParent().getCampBase(getGuildId()).buildPlayerHonor;
		return honor + armyHonor + buildHonor + nationMilitary;
	}

	public int getGuildHonor() {
		return (int) (monsterHonorMap.values().stream().mapToDouble(YQZZMonsterHonor::getGuildHonor).sum()
				+ resourceHonorMap.values().stream().mapToDouble(YQZZResourceHonor::getGuildHonor).sum()
				+ foggyHonorMap.values().stream().mapToDouble(YQZZFoggyHonor::getGuildHonor).sum()
				+pylonHonorMap.values().stream().mapToDouble(YQZZPylonHonor::getGuildHonor).sum());
	}

	public int getNationHonor() {
		return (int) (monsterHonorMap.values().stream().mapToDouble(YQZZMonsterHonor::getNationHonor).sum()
				+ resourceHonorMap.values().stream().mapToDouble(YQZZResourceHonor::getNationHonor).sum()
				+ foggyHonorMap.values().stream().mapToDouble(YQZZFoggyHonor::getNationHonor).sum()
				+ pylonHonorMap.values().stream().mapToDouble(YQZZPylonHonor::getNationHonor).sum());
	}

	public int getKillHonor() {
		YQZZBattleCfg cfg = getParent().getCfg();
		return (int) (killPower / cfg.getScoreForKill());
	}

	public int getGameMoveCityCount() {
		return gameMoveCityCount;
	}

	public void setGameMoveCityCount(int gameMoveCityCount) {
		this.gameMoveCityCount = gameMoveCityCount;
	}

	public double getKillPower() {
		return killPower;
	}

	public void setKillPower(double killPower) {
		this.killPower = killPower;
	}

	public double getHurtTankPower() {
		return hurtTankPower;
	}

	public void setHurtTankPower(double hurtTankPower) {
		this.hurtTankPower = hurtTankPower;
	}

	public IYQZZPlayerPush getPlayerPush() {
		return playerPush;
	}

	public YQZZQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(YQZZQuitReason quitReason) {
		this.quitReason = quitReason;
	}

	public List<WorldFavoritePB.Builder> getFavoriteList() {
		return favoriteList;
	}

	public boolean isExtraSpyMarchOpen() {
		return WorldMarchService.getInstance().isExtraSpyMarchOpen(this);
	}

	public boolean isExtraSypMarchOccupied() {
		List<IYQZZWorldMarch> spyMarchs = getParent().getPlayerMarches(this.getId(), WorldMarchType.SPY);
		for (IWorldMarch march : spyMarchs) {
			if (march.isExtraSpyMarch()) {
				return true;
			}
		}

		return false;
	}

	public int getEmoticon() {
		return emoticon;
	}

	public void setEmoticon(int emoticon) {
		this.emoticon = emoticon;
	}

	public long getEmoticonUseTime() {
		return emoticonUseTime;
	}

	public void setEmoticonUseTime(long emoticonUseTime) {
		this.emoticonUseTime = emoticonUseTime;
	}

	public MarchSet getInviewMarchs() {
		return inviewMarchs;
	}

	public void setInviewMarchs(MarchSet inviewMarchs) {
		this.inviewMarchs = inviewMarchs;
	}

	@Override
	public int getAoiObjId() {
		return aoiObjId;
	}

	@Override
	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	public YQZZPlayerEye getEye() {
		return eye;
	}

	public void setEye(YQZZPlayerEye eye) {
		this.eye = eye;
	}

	public long getSuperVipSkinEffEndTime() {
		return superVipSkinEffEndTime;
	}

	public void setSuperVipSkinEffEndTime(long superVipSkinEffEndTime) {
		this.superVipSkinEffEndTime = superVipSkinEffEndTime;
	}

	public int getSuperVipSkinLevel() {
		return superVipSkinLevel;
	}

	public void setSuperVipSkinLevel(int superVipSkinLevel) {
		this.superVipSkinLevel = superVipSkinLevel;
	}

	public void updateEquipTechLevel(int techId, int level) {
		EquipTechLevel.Builder builder = EquipTechLevel.newBuilder();
		builder.setTechId(techId);
		builder.setLevel(level);
		builderList.add(builder.build());
	}

	public int getDeadCnt() {
		return deadCnt;
	}

	public void setDeadCnt(int deadCnt) {
		this.deadCnt = deadCnt;
	}

	public PBYQZZPlayerInfo genPBYQZZPlayerInfo() {
		PBYQZZPlayerInfo.Builder prc = PBYQZZPlayerInfo.newBuilder();
		prc.setCamp(getCamp().intValue());
		prc.setName(getName());
		prc.setHonor(getHonor());
		prc.setGuildId(getGuildId());
		prc.setGuildTag(getGuildTag());
		// prc.setGuildHonor(getGuildHonor());
		prc.setPlayerId(getId());
		prc.setKillPower((int) getKillPower());
		prc.setLostPower((int) getHurtTankPower());
		prc.setKillMonster(getKillMonster());
		prc.setIcon(getIcon());
		String pficon = getPfIcon();
		if (!HawkOSOperator.isEmptyString(pficon)) {
			prc.setPfIcon(pficon);
		}
		monsterHonorMap.values().forEach(honor -> prc.addMonsterHonors(honor.toPBObj()));
		foggyHonorMap.values().forEach(honor -> prc.addFoggyHonors(honor.toPBObj()));
		resourceHonorMap.values().forEach(honor -> prc.addResHonors(honor.toPBObj()));
		buildStayTimeMap.values().forEach(honor -> prc.addBuildTimes(honor.toPBObj()));
		pylonHonorMap.values().forEach(honor -> prc.addPylonHonors(honor.toPBObj()));
		return prc.build();
	}

	public YQZZ_CAMP getCamp() {
		return camp;
	}

	public void setCamp(YQZZ_CAMP camp) {
		this.camp = camp;
	}

	public int getKillMonster() {
		return monsterHonorMap.values().stream().mapToInt(YQZZMonsterHonor::getKillCount).sum();
	}
	
	public int getKillFoggy() {
		return foggyHonorMap.values().stream().mapToInt(YQZZFoggyHonor::getKillCount).sum();
	}
	
	public int getJoinKillFoggy() {
		return foggyHonorMap.values().stream().mapToInt(YQZZFoggyHonor::getJoinKillCount).sum();
	}

	public int getCollPylon(){
		return pylonHonorMap.values().stream().mapToInt(YQZZPylonHonor::getPylonCount).sum();
	}

	public boolean isAnchor() {
		return false;
	}

	public List<EquipTechLevel> getBuilderList() {
		return builderList;
	}

	public Map<Integer, YQZZMonsterHonor> getMonsterHonorMap() {
		return monsterHonorMap;
	}

	public Map<Integer, YQZZPylonHonor> getPylonHonorMap() {
		return pylonHonorMap;
	}

	public Map<YQZZBuildType, YQZZBuildStayTime> getBuildStayTimeMap() {
		return buildStayTimeMap;
	}

	public Map<Integer, YQZZFoggyHonor> getFoggyHonorMap() {
		return foggyHonorMap;
	}

	public Map<Integer, YQZZResourceHonor> getResourceHonorMap() {
		return resourceHonorMap;
	}

	public Map<Integer, YQZZBattleStatics> getBattleStatics() {
		return battleStatics;
	}

	public int getNationMilitary() {
		return nationMilitary;
	}

	public Set<IYQZZBuilding> getSubareaBuilds() {
		return subareaBuilds;
	}

	public void setSubareaBuilds(Set<IYQZZBuilding> subareaBuilds) {
		this.subareaBuilds = subareaBuilds;
	}

	public int getPushGiftDeadCnt() {
		return pushGiftDeadCnt;
	}

	public void setPushGiftDeadCnt(int pushGiftDeadCnt) {
		this.pushGiftDeadCnt = pushGiftDeadCnt;
	}

	public long getGiveupBuildCd() {
		return giveupBuildCd;
	}

	public void setGiveupBuildCd(long giveupBuildCd) {
		this.giveupBuildCd = giveupBuildCd;
	}

	public boolean underNationControl(String guildId) {
		return Objects.equals(getGuildId(), guildId);
	}


}
