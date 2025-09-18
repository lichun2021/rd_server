package com.hawk.game.module.lianmengfgyl.battleroom.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;
import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLBattleCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitReason;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.FGYL.PBFGYLPlayerMoveCitySync;
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
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.world.service.WorldPointService;

public abstract class IFGYLPlayer extends Player implements IFGYLWorldPoint {
	private FGYLBattleRoom parent;
	private IFGYLPlayerData playerData;
	private IFGYLPlayerPush playerPush;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	private int plantMilitaryLevel;
	/** 最近一次功击者 */
	private IFGYLPlayer lastAttacker;
	private MarchSet inviewMarchs = new MarchSet();
	/** 城点 */
	private int[] pos = { 0, 0 };
	/** 下次可免费迁城时间 */
	private long nextCityMoveTime;
	private int costCityMoveCount;
	private FGYL_CAMP camp;
	/** 个人总积分 */
	private double honor;
	/** 联盟积分 */
	private double guildHonor;
	private String guildId;
	private String guildName;
	private String guildTag;
	private int guildFlag;

	/** 个人采集积分 */
	private double collectGuildHonor;

	private double buildHonor;
	private int killMonster;
	/** 击杀 */
	private double killPower;
	/** 受伤 */
	private double hurtTankPower;

	/** 游戏中迁城总数 */
	private int gameMoveCityCount;

	List<WorldFavoritePB.Builder> favoriteList = new ArrayList<>();

	private FGYLQuitReason quitReason;
	private int emoticon;
	// 行军表情使用时间
	private long emoticonUseTime;
	/** 
	 * 至尊vip皮肤特效结束时间
	 */
	private long superVipSkinEffEndTime;
	private int superVipSkinLevel;
	private List<EquipTechLevel> builderList = new ArrayList<>();

	private int[] worldMovePos;
	private int aoiObjId = 0;
	private FGYLPlayerEye eye;
	private int skillOrder;
	private int monstKill;
	public IFGYLPlayer(HawkXID xid) {
		super(xid);
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
		PBFGYLPlayerMoveCitySync.Builder builder = PBFGYLPlayerMoveCitySync.newBuilder();
		builder.setFgylMoveCityCD(nextCityMoveTime);
		builder.setFgylMoveCityCost(costCityMoveCount);

		sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_MOVE_CITY_CD_SYNC, builder));
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream().filter(m -> m.getParent() == this).count();
	}

	public boolean isInSameGuild(IFGYLPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	public IFGYLPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(IFGYLPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<IFGYLWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE);
	}

	public void quitGame() {

	}

	@Override
	public WorldPointPB.Builder toBuilder(IFGYLPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setFgylMoveCityCD(nextCityMoveTime);
		builder.setFgylMoveCityCost(costCityMoveCount);
		builder.setFlagView(getCamp().intValue());
		builder.setGuildId(guildId);
		builder.setGuildTag(guildTag);
		builder.setGuildFlag(guildFlag);

		int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(thisPlayerId);
		if (cityBaseShow != 0) {
			builder.setShowEffect(cityBaseShow);
		}

		builder.addAllDressShow(getShowDress());

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
		builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(getId()));
		builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(getId()));
		BuilderUtil.buildPlayerEmotion(builder, getEmoticonUseTime(), getEmoticon());
		builder.setSuperVipSkin(superVipSkinBuilder());
		builder.addAllEquipTechLevel(equipTechLevelToBuilder());
		builder.setDeployedSwInfo(getDeployedSwInfo());
		builder.setMechacoreShowInfo(getMechacoreShowInfo());
		builder.setPlantMilitaryLevel(plantMilitaryLevel);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IFGYLPlayer viewer) {
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
		builder.setFgylMoveCityCD(nextCityMoveTime);
		builder.setFgylMoveCityCost(costCityMoveCount);
		builder.setGuildId(guildId);
		builder.setGuildTag(guildTag);
		builder.setGuildFlag(guildFlag);

		int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(getId());
		if (cityBaseShow != 0) {
			builder.setShowEffect(cityBaseShow);
		}

		// 装扮
		builder.addAllDressShow(getShowDress());

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
		builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(getId()));
		builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(getId()));
		BuilderUtil.buildPlayerEmotion(builder, getEmoticonUseTime(), getEmoticon());
		builder.setSuperVipSkin(superVipSkinBuilder());
		builder.addAllEquipTechLevel(equipTechLevelToBuilder());
		builder.setDeployedSwInfo(getDeployedSwInfo());
		builder.setMechacoreShowInfo(getMechacoreShowInfo());
		builder.setPlantMilitaryLevel(plantMilitaryLevel);
		return builder;
	}

	public List<WorldShowDress> getShowDress() {
		long timeNow = HawkTime.getMillisecond();
		List<WorldShowDress> list = new ArrayList<>();
		try {
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
				// builder.addDressShow(dressInfo);
				list.add(dressInfo.build());
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return list;
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
	public FGYLBattleRoom getParent() {
		return parent;
	}

	public void setParent(FGYLBattleRoom parentRoom) {
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

	public IFGYLPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(IFGYLPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(IFGYLPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public IFGYLPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public IFGYLPlayerPush getPush() {

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

	public void onMarchStart(IFGYLWorldMarch march) {

	}

	public FGYL_CAMP getCamp() {
		return camp;
	}

	public void setCamp(FGYL_CAMP camp) {
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

	public void incrementGuildHonor(double honorAdd) {
		this.guildHonor += honorAdd;
	}

	public void incrementCollectGuildHonor(double honorAdd) {
		this.collectGuildHonor += honorAdd;
	}

	public void incrementBuildHonor(double honorAdd) {
		this.buildHonor += honorAdd;
	}

	/** 采集获得联盟积分*/
	public int getCollectGuildHonor() {
		return (int) collectGuildHonor;
	}

	public void setCollectHonor(double collectHonor) {
		this.collectGuildHonor = collectHonor;
	}

	public int getHonor() {
		FGYLBattleCfg cfg = getParent().getCfg();
		return (int) (honor + killPower / cfg.getScoreForKill() + hurtTankPower / cfg.getScoreForDefense());
	}

	public int getHurtHonor() {
		FGYLBattleCfg cfg = getParent().getCfg();
		return (int) (hurtTankPower / cfg.getScoreForDefense());
	}

	public int getKillHonor() {
		FGYLBattleCfg cfg = getParent().getCfg();
		return (int) (killPower / cfg.getScoreForKill());
	}

	public void setHonor(double collectHonor) {
		this.honor = collectHonor;
	}

	public int getGuildHonor() {
		return (int) guildHonor;
	}

	public void setGuildHonor(double collectGuildHonor) {
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

	public IFGYLPlayerPush getPlayerPush() {
		return playerPush;
	}

	public FGYLQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(FGYLQuitReason quitReason) {
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
		return getParent().isAnchor(this);
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

	public int getBuildHonor() {
		return (int) buildHonor;
	}

	public void setBuildHonor(double buildHonor) {
		this.buildHonor = buildHonor;
	}

	public int getKillMonster() {
		return killMonster;
	}

	public void setKillMonster(int killMonster) {
		this.killMonster = killMonster;
	}

	public void incKillMonster() {
		this.killMonster++;
	}

	public FGYLPlayerEye getEye() {
		return eye;
	}

	public void setEye(FGYLPlayerEye eye) {
		this.eye = eye;
	}

	public int getSkillOrder() {
		return skillOrder;
	}

	public void setSkillOrder(int skillOrder) {
		this.skillOrder = skillOrder;
		getPlayerPush().syncFGYLPlayerInfo();
	}

	public int getMonstKill() {
		return monstKill;
	}

	public void setMonstKill(int monstKill) {
		this.monstKill = monstKill;
	}

	public int getPlantMilitaryLevel() {
		return plantMilitaryLevel;
	}

	public void setPlantMilitaryLevel(int plantMilitaryLevel) {
		this.plantMilitaryLevel = plantMilitaryLevel;
	}

}
