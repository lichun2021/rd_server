package com.hawk.game.lianmengstarwars.player;

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
import com.hawk.game.config.SWBattleCfg;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.msg.SWQuitReason;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.march.MarchSet;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SW.PBSWPlayerMoveCitySync;
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
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

public abstract class ISWPlayer extends Player implements ISWWorldPoint {
	private SWBattleRoom parent;
	private ISWPlayerData playerData;
	private ISWPlayerPush playerPush;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	/** 最近一次功击者 */
	private ISWPlayer lastAttacker;
	private MarchSet inviewMarchs = new MarchSet();
	private int killCount;
	/** 城点 */
	private int[] pos = { 0, 0 };
	/** 下次可免费迁城时间 */
	private long nextCityMoveTime;
	private int costCityMoveCount;
	/** 个人总积分 */
	private double honor;
	/** 联盟积分 */
	private int guildHonor;

	/** 个人采集积分 */
	private int collectHonor;

	/** 击杀 */
	private double killPower;
	/** 受伤 */
	private double hurtTankPower;
	/**死兵总数 */
	private int deadCnt;
	private int deadPower;

	/** 游戏中迁城总数 */
	private int gameMoveCityCount;
	private int emoticon;
	// 行军表情使用时间
	private long emoticonUseTime;

	List<WorldFavoritePB.Builder> favoriteList = new ArrayList<>();
	private int aoiObjId = 0;
	private SWPlayerEye eye;
	private SWQuitReason quitReason;
	/** 
	 * 至尊vip皮肤特效结束时间
	 */
	private long superVipSkinEffEndTime;
	private int superVipSkinLevel;
	private List<EquipTechLevel> builderList = new ArrayList<>();
	private PBDeployedSwInfo.Builder deployedSwInfo;
	private String mechacoreShowInfo;
	
	public ISWPlayer(HawkXID xid) {
		super(xid);
	}

	public void addFavorite(WorldFavoritePB.Builder favorite) {
		this.favoriteList.add(favorite);
	}
	
	
	@Override
	public int getWorldPointRadius() {
		return 2;
	}

	public abstract void init();

	/** 行军加速 */
	public abstract double getMarchSpeedUp();

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.PLAYER;
	}

	public void moveCityCDSync() {
		PBSWPlayerMoveCitySync.Builder builder = PBSWPlayerMoveCitySync.newBuilder();
		builder.setSWMoveCityCD(nextCityMoveTime);
		builder.setSWMoveCityCost(costCityMoveCount);

		sendProtocol(HawkProtocol.valueOf(HP.code.SW_MOVE_CITY_CD_SYNC, builder));
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream().filter(m -> m.getParent() == this).count();
	}

	public boolean isInSameGuild(ISWPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	public ISWPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(ISWPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<ISWWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE);
	}

	public void quitGame() {

	}

	@Override
	public WorldPointPB.Builder toBuilder(ISWPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setSwMoveCityCD(nextCityMoveTime);
		builder.setSwMoveCityCost(costCityMoveCount);
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());

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
	public WorldPointDetailPB.Builder toDetailBuilder(ISWPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		builder.setPlayerId(getId());
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setPlayerIcon(getIcon());
		builder.setPlayerPfIcon(getPfIcon());
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());

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
	public SWBattleRoom getParent() {
		return parent;
	}

	public void setParent(SWBattleRoom parentRoom) {
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

	public ISWPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(ISWPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(ISWPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public ISWPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public ISWPlayerPush getPush() {

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
		if(isRobot()){
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

	public void onMarchStart(ISWWorldMarch march) {

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

	public void incrementCollectHonor(int honorAdd) {
		this.collectHonor += honorAdd;
	}

	public int getCollectHonor() {
		return collectHonor;
	}

	public void setCollectHonor(int collectHonor) {
		this.collectHonor = collectHonor;
	}

	public int getHonor() {
		SWBattleCfg cfg = getParent().getCfg();
		return (int) (honor + killPower / cfg.getScoreForKill() + hurtTankPower / cfg.getScoreForDefense());
	}

	public int getKillHonor() {
		SWBattleCfg cfg = getParent().getCfg();
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

	public int getDeadPower() {
		return deadPower;
	}

	public void setDeadPower(int deadPower) {
		this.deadPower = deadPower;
	}

	public double getHurtTankPower() {
		return hurtTankPower;
	}

	public void setHurtTankPower(double hurtTankPower) {
		this.hurtTankPower = hurtTankPower;
	}

	public ISWPlayerPush getPlayerPush() {
		return playerPush;
	}

	public SWQuitReason getSWQuitReason() {
		return quitReason;
	}

	public void setSWQuitReason(SWQuitReason quitReason) {
		this.quitReason = quitReason;
	}

	public List<WorldFavoritePB.Builder> getFavoriteList() {
		return favoriteList;
	}

	public boolean isExtraSpyMarchOpen() {
		return WorldMarchService.getInstance().isExtraSpyMarchOpen(this);
	}

	public boolean isExtraSypMarchOccupied() {
		List<ISWWorldMarch> spyMarchs = getParent().getPlayerMarches(this.getId(), WorldMarchType.SPY);
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

	public SWPlayerEye getEye() {
		return eye;
	}

	public void setEye(SWPlayerEye eye) {
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

}
