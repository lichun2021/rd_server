package com.hawk.game.module.lianmengXianquhx.player;

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
import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager.XQHX_CAMP;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXBattleCfg;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXQuitReason;
import com.hawk.game.module.lianmengXianquhx.worldmarch.IXQHXWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.XQHX.PBXQHXPlayerMoveCitySync;
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

public abstract class IXQHXPlayer extends Player implements IXQHXWorldPoint {
	private XQHXBattleRoom parent;
	private IXQHXPlayerData playerData;
	private IXQHXPlayerPush playerPush;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	/** 最近一次功击者 */
	private IXQHXPlayer lastAttacker;
	private MarchSet inviewMarchs = new MarchSet();
	/** 城点 */
	private int[] pos = { 0, 0 };
	/** 下次可免费迁城时间 */
	private long nextCityMoveTime;
	private int costCityMoveCount;
	private XQHX_CAMP camp;
	private String guildId;
	private String guildName;
	private String guildTag;
	private int guildFlag;
	
	private double buildHonor;
	private double pylonHonor;
	private int pylonCnt;
	private double monsterHonor;
	private int killMonster;
	/** 击杀 */
	private double killPower;
	/** 受伤  积分*/
	private double hurtTankHonor;

	/** 游戏中迁城总数 */
	private int gameMoveCityCount;

	List<WorldFavoritePB.Builder> favoriteList = new ArrayList<>();

	private XQHXQuitReason quitReason;
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
	
	public IXQHXPlayer(HawkXID xid) {
		super(xid);
	}
	@Override
	public int getGridCnt() {
		return 2;
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
		PBXQHXPlayerMoveCitySync.Builder builder = PBXQHXPlayerMoveCitySync.newBuilder();
		builder.setXqhxMoveCityCD(nextCityMoveTime);
		builder.setXqhxMoveCityCost(costCityMoveCount);

		sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_MOVE_CITY_CD_SYNC, builder));
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream().filter(m -> m.getParent() == this).count();
	}

	public boolean isInSameGuild(IXQHXPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	public IXQHXPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(IXQHXPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<IXQHXWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE);
	}

	public void quitGame() {

	}

	@Override
	public WorldPointPB.Builder toBuilder(IXQHXPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setTblyMoveCityCD(nextCityMoveTime);
		builder.setTblyMoveCityCost(costCityMoveCount);
		builder.setFlagView(getCamp().intValue());
		builder.setGuildId(guildId);
		builder.setGuildTag(guildTag);
		builder.setGuildFlag(guildFlag);

		IXQHXWorldMarch march = getAssistanceMarch(viewer.getId(), getPointId());
		if (march != null) {
			builder.setHasMarchStop(true);
			builder.setFormation(march.getFormationInfo());
		}

		int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(thisPlayerId);
		if (cityBaseShow != 0) {
			builder.setShowEffect(cityBaseShow);
		}

		builder.addAllDressShow(getShowDress());
		
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
		builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(getId()));
		builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(getId()));
		BuilderUtil.buildPlayerEmotion(builder, getEmoticonUseTime(), getEmoticon());
		builder.setSuperVipSkin(superVipSkinBuilder());
		builder.addAllEquipTechLevel(equipTechLevelToBuilder());
		builder.setDeployedSwInfo(getDeployedSwInfo());
		builder.setMechacoreShowInfo(getMechacoreShowInfo());
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IXQHXPlayer viewer) {
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

		IXQHXWorldMarch march = getAssistanceMarch(viewer.getId(), getPointId());
		if (march != null) {
			builder.setHasMarchStop(true);
			builder.setFormation(march.getFormationInfo());
		}

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
		return builder;
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
	public XQHXBattleRoom getParent() {
		return parent;
	}

	public void setParent(XQHXBattleRoom parentRoom) {
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

	public IXQHXPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(IXQHXPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(IXQHXPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public IXQHXPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public IXQHXPlayerPush getPush() {

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

	public void onMarchStart(IXQHXWorldMarch march) {

	}

	public XQHX_CAMP getCamp() {
		return camp;
	}

	public void setCamp(XQHX_CAMP camp) {
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


	public void incrementBuildHonor(double honorAdd) {
		this.buildHonor += honorAdd;
	}
	
	public void incrementPylonHonor(double honorAdd) {
		this.pylonHonor += honorAdd;
	}
	public void incrementMonsterHonor(double honorAdd) {
		this.monsterHonor += honorAdd;
	}
	
	public int getHonor() {
		return getBuildHonor() + getMonsterHonor() + getPylonHonor() + getHurtHonor() + getKillHonor();
	}
	
	public int getHurtHonor() {
		return (int) (hurtTankHonor);
	}

	public int getKillHonor() {
		XQHXBattleCfg cfg = getParent().getCfg();
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

	public double getHurtTankHonor() {
		return hurtTankHonor;
	}

	public void setHurtTankHonor(double hurtTankHonor) {
		this.hurtTankHonor = hurtTankHonor;
	}

	public IXQHXPlayerPush getPlayerPush() {
		return playerPush;
	}

	public XQHXQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(XQHXQuitReason quitReason) {
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
	
	public IXQHXWorldMarch getAssistanceMarch(String viewerId, int pointId) {
		IXQHXWorldMarch bfalse = getParent().getPointMarches(pointId, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE).stream()
				.filter(march -> march.getPlayerId().equals(viewerId)).findAny().orElse(null);
		return bfalse;
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
	public int getPylonHonor() {
		return (int) pylonHonor;
	}
	public int getMonsterHonor() {
		return (int) monsterHonor;
	}
	public int getPylonCnt() {
		return pylonCnt;
	}
	public void setPylonCnt(int pylonCnt) {
		this.pylonCnt = pylonCnt;
	}

}
