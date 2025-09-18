package com.hawk.game.lianmengcyb.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.config.CYBORGBattleCfg;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitReason;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.march.MarchSet;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.CYBORG.PBCYBORGPlayerInfo;
import com.hawk.game.protocol.CYBORG.PBCYBORGPlayerMoveCitySync;
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
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.world.service.WorldPointService;

public abstract class ICYBORGPlayer extends Player implements ICYBORGWorldPoint {
	private CYBORGBattleRoom parent;
	private ICYBORGPlayerData playerData;
	private ICYBORGPlayerPush playerPush;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	/** 最近一次功击者 */
	private ICYBORGPlayer lastAttacker;
	private int killCount;
	/** 城点 */
	private int[] pos = { 0, 0 };
	/** 下次可免费迁城时间 */
	private long nextCityMoveTime;
	private int costCityMoveCount;
	private CYBORG_CAMP camp;
	/** 个人总积分 */
	private double honor;
	/** 联盟积分 */
	private int guildHonor;
	private String guildId;
	private String guildName;
	private String guildTag;
	private int guildFlag;

	/** 个人采集积分 */
	private double collectHonor;

	/** 击杀 */
	private double killPower;
	/** 受伤 */
	private int hurtTankCount;
	private double hurtTankPower;
	private double hurtTankHonor;

	/** 游戏中迁城总数 */
	private int gameMoveCityCount;
	
	private int killMonster;
	/** 上次同步额外加成*/
	private int lastbuildBuffLevelExtra;

	List<WorldFavoritePB.Builder> favoriteList = new ArrayList<>();

	private CYBORGQuitReason quitReason;
	private int emoticon;
	// 行军表情使用时间
	private long emoticonUseTime;
	/** 
	 * 至尊vip皮肤特效结束时间
	 */
	private long superVipSkinEffEndTime;
	private int superVipSkinLevel;
	private List<EquipTechLevel> builderList = new ArrayList<>();
	private PBDeployedSwInfo.Builder deployedSwInfo;
	private String mechacoreShowInfo;
	
	private int[] worldMovePos;
	private MarchSet inviewMarchs = new MarchSet();
	
	public ICYBORGPlayer(HawkXID xid) {
		super(xid);
	}
	
	public PBCYBORGPlayerInfo genPBCYBORGPlayerInfo() {
		PBCYBORGPlayerInfo.Builder prc = PBCYBORGPlayerInfo.newBuilder();
		prc.setCamp(getCamp().intValue());
		prc.setName(getName());
		prc.setHonor(getHonor());
		prc.setGuildTag(getGuildTag());
		prc.setGuildHonor(getGuildHonor());
		prc.setPlayerId(getId());
		prc.setKillPower((int) getKillPower());
		prc.setLostPower((int) getHurtTankPower());
		prc.setKillMonster(getKillMonster());
		prc.setIcon(getIcon());
		String pficon = getPfIcon();
		if(!HawkOSOperator.isEmptyString(pficon)){
			prc.setPfIcon(pficon);
		}
		return prc.build();
	}

	public void addFavorite(WorldFavoritePB.Builder favorite) {
		this.favoriteList.add(favorite);
	}

	public abstract void init();

	/** 行军加速 */
	public abstract double getMarchSpeedUp();

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.PLAYER;
	}

	public void moveCityCDSync() {
		PBCYBORGPlayerMoveCitySync.Builder builder = PBCYBORGPlayerMoveCitySync.newBuilder();
		builder.setCYBORGMoveCityCD(nextCityMoveTime);
		builder.setCYBORGMoveCityCost(costCityMoveCount);

		sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_MOVE_CITY_CD_SYNC, builder));
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream().filter(m -> m.getParent() == this).count();
	}

	public boolean isInSameGuild(ICYBORGPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	public ICYBORGPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(ICYBORGPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<ICYBORGWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE);
	}

	public void quitGame() {

	}

	@Override
	public WorldPointPB.Builder toBuilder(ICYBORGPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setCyborgMoveCityCD(nextCityMoveTime);
		builder.setCyborgMoveCityCost(costCityMoveCount);
		builder.setFlagView(getCamp().intValue());
		builder.setGuildId(guildId);
		builder.setGuildTag(guildTag);
		builder.setGuildFlag(guildFlag);

		ICYBORGWorldMarch march = getAssistanceMarch(viewer.getId(), getPointId());
		if (march != null) {
			builder.setHasMarchStop(true);
			builder.setFormation(march.getFormationInfo());
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
		if(Objects.nonNull(acRoleInfo)){
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
	public WorldPointDetailPB.Builder toDetailBuilder(ICYBORGPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		builder.setPlayerId(getId());
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setPlayerIcon(getIcon());
		builder.setPlayerPfIcon(getPfIcon());
		builder.setFlagView(getCamp().intValue());
		builder.setGuildId(guildId);
		builder.setGuildTag(guildTag);
		builder.setGuildFlag(guildFlag);

		ICYBORGWorldMarch march = getAssistanceMarch(viewer.getId(), getPointId());
		if (march != null) {
			builder.setHasMarchStop(true);
			builder.setFormation(march.getFormationInfo());
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
		return getParent().getStartTime();
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
	public CYBORGBattleRoom getParent() {
		return parent;
	}

	public void setParent(CYBORGBattleRoom parentRoom) {
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

	public ICYBORGPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(ICYBORGPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(ICYBORGPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public ICYBORGPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public ICYBORGPlayerPush getPush() {

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

	public void onMarchStart(ICYBORGWorldMarch march) {

	}

	public CYBORG_CAMP getCamp() {
		return camp;
	}

	public void setCamp(CYBORG_CAMP camp) {
		this.camp = camp;
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

	public void incrementPlayerHonor(double honorAdd) {
		this.honor += honorAdd;
	}

	public void incrementGuildHonor(int honorAdd) {
		this.guildHonor += honorAdd;
	}

	public void incrementCollectHonor(double honorAdd) {
		this.collectHonor += honorAdd;
	}

	public double getCollectHonor() {
		return collectHonor;
	}

	public void setCollectHonor(double collectHonor) {
		this.collectHonor = collectHonor;
	}

	public int getHonor() {
		CYBORGBattleCfg cfg = getParent().getCfg();
		return (int) (honor + killPower / cfg.getScoreForKill() + hurtTankHonor);
	}

	public int getKillHonor() {
		CYBORGBattleCfg cfg = getParent().getCfg();
		return (int) (killPower / cfg.getScoreForKill());
	}

	public void setHonor(double collectHonor) {
		this.honor = collectHonor;
	}

	public int getGuildHonor() {
		return guildHonor;
	}

	public void setGuildHonor(int collectGuildHonor) {
		this.guildHonor = collectGuildHonor;
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

	public double getHurtTankHonor() {
		return hurtTankHonor;
	}

	public void setHurtTankHonor(double hurtTankHonor) {
		this.hurtTankHonor = hurtTankHonor;
	}

	public int getHurtTankCount() {
		return hurtTankCount;
	}

	public void setHurtTankCount(int hurtTankCount) {
		this.hurtTankCount = hurtTankCount;
	}

	public ICYBORGPlayerPush getPlayerPush() {
		return playerPush;
	}

	public CYBORGQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(CYBORGQuitReason quitReason) {
		this.quitReason = quitReason;
	}

	@Override
	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	@Override
	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}

	@Override
	public int getGuildFlag() {
		return guildFlag;
	}

	public void setGuildFlag(int guildFlag) {
		this.guildFlag = guildFlag;
	}

	@Override
	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	public List<WorldFavoritePB.Builder> getFavoriteList() {
		return favoriteList;
	}

	public boolean isAnchor() {
		return this == getParent().getAnchor();
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

	public int getKillMonster() {
		return killMonster;
	}

	public void setKillMonster(int killMonster) {
		this.killMonster = killMonster;
	}

	public int getLastbuildBuffLevelExtra() {
		return lastbuildBuffLevelExtra;
	}

	public void setLastbuildBuffLevelExtra(int lastbuildBuffLevelExtra) {
		this.lastbuildBuffLevelExtra = lastbuildBuffLevelExtra;
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

	public int[] getWorldMovePos() {
		return worldMovePos;
	}

	public void setWorldMovePos(int[] worldMovePos) {
		this.worldMovePos = worldMovePos;
	}

	public MarchSet getInviewMarchs() {
		return inviewMarchs;
	}

	public void setInviewMarchs(MarchSet inviewMarchs) {
		this.inviewMarchs = inviewMarchs;
	}
	
	public ICYBORGWorldMarch getAssistanceMarch(String viewerId, int pointId) {
		ICYBORGWorldMarch bfalse = getParent().getPointMarches(pointId, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE).stream()
				.filter(march -> march.getPlayerId().equals(viewerId)).findAny().orElse(null);
		return bfalse;
	}

}
