package com.hawk.game.module.dayazhizhan.battleroom.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tuple.HawkTuple4;
import org.hawk.xid.HawkXID;

import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitReason;
import com.hawk.game.module.dayazhizhan.battleroom.player.rogue.DYZZRogueCollection;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.DYZZ.PBDYZZPlayerMoveCitySync;
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
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.world.service.WorldPointService;

public abstract class IDYZZPlayer extends Player implements IDYZZWorldPoint {
	private DYZZBattleRoom parent;
	private IDYZZPlayerData playerData;
	private IDYZZPlayerPush playerPush;
	private DYZZRogueCollection rogueCollec;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	/** 最近一次功击者 */
	private IDYZZPlayer lastAttacker;
	/** 城点 */
	private int[] pos = { 0, 0 };
	/** 下次可免费迁城时间 */
	private long nextCityMoveTime;
	private int costCityMoveCount;
	private DYZZCAMP camp;

	/** 个人采集积分 */
	private double collectHonor;

	private int killCount;
	/** 击杀 */
	private int killPower;
	/** 受伤 */
	private int hurtTankCount;
	private int hurtTankPower;

	private int hurtCount;
	private int hurtPower;

	/** 游戏中迁城总数 */
	private int gameMoveCityCount;

	List<WorldFavoritePB.Builder> favoriteList = new ArrayList<>();

	private DYZZQuitReason quitReason;
	private int emoticon;
	// 行军表情使用时间
	private long emoticonUseTime;
	private int mvp;
	private String guildId;
	private String guildName;
	private String guildTag;
	private int guildFlag;
	// 获奖次数
	private int rewardCount;
	private long power;
	/** 
	 * 至尊vip皮肤特效结束时间
	 */
	private long superVipSkinEffEndTime;
	private int superVipSkinLevel;
	private List<EquipTechLevel> builderList = new ArrayList<>();
	private PBDeployedSwInfo.Builder deployedSwInfo;
	private String mechacoreShowInfo;
	
	// 进场时赛季积分
	private int seasonScore;
	// 赛季积分增长
	private int seasonScoreAdd;
	// 胜利次数
	private int winCount;
	// 首胜奖励
	private int seasonFirstReward;

	private long campNoticeTime;

	private boolean speedItemFree = true;// = 74; // 免费加速可领取
	private int speedItemBuyCnt;// = 75; // 免费加速购买数

	public IDYZZPlayer(HawkXID xid) {
		super(xid);
	}

	public void onLogin(Player player) {
	}

	public void addFavorite(WorldFavoritePB.Builder favorite) {
		this.favoriteList.add(favorite);
	}

	public abstract void init();

	/** 行军加速 */
	public abstract double getMarchSpeedUp();

	@Override
	public long getPower() {
		return power;
	}

	public void setPower(long power) {
		this.power = power;
	}

	/**
	 * 获取玩家发起集结的最大的可参与行军队伍数目
	 */
	@Override
	public int getMaxMassJoinMarchNum() {
		return 5;
	}

	@Override
	public int getRemainDisabledCap() {
		return 99999999;
	}

	/**
	 * 获取泰能医院剩余容量
	 * @return
	 */
	@Override
	public int getPlantRemainDisabledCap() {
		return 99999999;
	}

	@Override
	public int getPlantMaxCapNum() {
		return 999999999;
	}

	@Override
	public int getCannonCap() {

		return 99999999;
	}

	@Override
	public int getMaxCapNum() {

		return 999999999;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.PLAYER;
	}

	@Override
	public boolean hasGuild() {
		return true;
	}

	public String getDYZZGuildId() {
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

	public void moveCityCDSync() {
		PBDYZZPlayerMoveCitySync.Builder builder = PBDYZZPlayerMoveCitySync.newBuilder();
		builder.setDYZZMoveCityCD(nextCityMoveTime);
		builder.setDYZZMoveCityCost(costCityMoveCount);

		sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_MOVE_CITY_CD_SYNC, builder));
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream().filter(m -> m.getParent() == this).count();
	}

	public boolean isInSameGuild(IDYZZPlayer tar) {
		return Objects.equals(getDYZZGuildId(), tar.getDYZZGuildId());
	}

	public IDYZZPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(IDYZZPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<IDYZZWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE);
	}

	public void quitGame() {

	}

	@Override
	public WorldPointPB.Builder toBuilder(IDYZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setDyzzMoveCityCD(nextCityMoveTime);
		builder.setDyzzMoveCityCost(costCityMoveCount);
		builder.setFlagView(getCamp().intValue());
		builder.setGuildId(getDYZZGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());

		IDYZZWorldMarch march = getAssistanceMarch(viewer.getId(), getPointId());
		if (march != null) {
			builder.setHasMarchStop(true);
			builder.setFormation(march.getFormationInfo());
		}

		int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(thisPlayerId);
		if (cityBaseShow != 0) {
			builder.setShowEffect(cityBaseShow);
		}

		long timeNow = getParent().getCurTimeMil();
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
	public WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
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
		builder.setGuildId(getDYZZGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());

		IDYZZWorldMarch march = getAssistanceMarch(viewer.getId(), getPointId());
		if (march != null) {
			builder.setHasMarchStop(true);
			builder.setFormation(march.getFormationInfo());
		}

		int cityBaseShow = WorldPointService.getInstance().getCityBaseShow(getId());
		if (cityBaseShow != 0) {
			builder.setShowEffect(cityBaseShow);
		}

		long timeNow = getParent().getCurTimeMil();
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
		builder.setEquipStarShow(WorldPointService.getInstance().getEquipStarShow(getId()));
		builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(getId()));
		BuilderUtil.buildPlayerEmotion(builder, getEmoticonUseTime(), getEmoticon());
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
		return getParent().getCollectStartTime();
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

	/**
	 * # 玩家战斗评分=A*玩家副本击杀数+B*玩家副本陨晶矿获取数+C*玩家防御坦克阵亡数（基地攻防不计数）
	 * @return
	 */
	public int getKda() {
		HawkTuple4<Double, Double, Double, Double> sp3 = getParent().getCfg().getScoreparameter3();
		return (int) Math.pow(killCount * sp3.first + collectHonor * sp3.second + hurtTankCount * sp3.third, sp3.fourth);
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

	@Override
	public int getRedis() {
		return 2;
	}

	public int[] getPos() {
		return pos;
	}

	public void setPos(int[] pos) {
		this.pos = pos;
	}

	@Override
	public DYZZBattleRoom getParent() {
		return parent;
	}

	public void setParent(DYZZBattleRoom parentRoom) {
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

	public IDYZZPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(IDYZZPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(IDYZZPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public IDYZZPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public IDYZZPlayerPush getPush() {

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

	public void onMarchStart(IDYZZWorldMarch march) {

	}

	public DYZZCAMP getCamp() {
		return camp;
	}

	public void setCamp(DYZZCAMP camp) {
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

	public void incrementCollectHonor(double honorAdd) {
		this.collectHonor += honorAdd;
	}

	/** 采集获得联盟积分*/
	public double getCollectHonor() {
		return collectHonor;
	}

	public void setCollectHonor(double collectHonor) {
		this.collectHonor = collectHonor;
	}

	public int getGameMoveCityCount() {
		return gameMoveCityCount;
	}

	public void setGameMoveCityCount(int gameMoveCityCount) {
		this.gameMoveCityCount = gameMoveCityCount;
	}

	public int getKillPower() {
		return killPower;
	}

	public void setKillPower(int killPower) {
		this.killPower = killPower;
	}

	public int getHurtTankPower() {
		return hurtTankPower;
	}

	public void setHurtTankPower(int hurtTankPower) {
		this.hurtTankPower = hurtTankPower;
	}

	public int getHurtTankCount() {
		return hurtTankCount;
	}

	public void setHurtTankCount(int hurtTankCount) {
		this.hurtTankCount = hurtTankCount;
	}

	public IDYZZPlayerPush getPlayerPush() {
		return playerPush;
	}

	public DYZZQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(DYZZQuitReason quitReason) {
		this.quitReason = quitReason;
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

	public int getMvp() {
		return mvp;
	}

	public void setMvp(int mvp) {
		this.mvp = mvp;
	}

	public void cleanTempData() {
		// TODO Auto-generated method stub

	}

	public int getRewardCount() {
		return rewardCount;
	}

	public void setRewardCount(int rewardCount) {
		this.rewardCount = rewardCount;
	}

	public DYZZRogueCollection getRogueCollec() {
		return rogueCollec;
	}

	public void setRogueCollec(DYZZRogueCollection rogueCollec) {
		this.rogueCollec = rogueCollec;
	}

	public int getHurtCount() {
		return hurtCount;
	}

	public void setHurtCount(int hurtCount) {
		this.hurtCount = hurtCount;
	}

	public int getHurtPower() {
		return hurtPower;
	}

	public void setHurtPower(int hurtPower) {
		this.hurtPower = hurtPower;
	}

	public int getNegative() {
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		if (this.getKda() < cfg.getNegativeintegral()) {
			return 1;
		}
		return 0;
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

	public int getSeasonScore() {
		return seasonScore;
	}

	public void setSeasonScore(int seasonScore) {
		this.seasonScore = seasonScore;
	}

	public int getSeasonScoreAdd() {
		return seasonScoreAdd;
	}

	public void setSeasonScoreAdd(int seasonScoreAdd) {
		this.seasonScoreAdd = seasonScoreAdd;
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getSeasonFirstReward() {
		return seasonFirstReward;
	}

	public void setSeasonFirstReward(int seasonFirstReward) {
		this.seasonFirstReward = seasonFirstReward;
	}

	/**
	 * 计算增加赛季积分
	 * @return
	 */
	public int calSeasonScoreAdd() {
		DYZZCAMP winCamp = this.getParent().getWinCamp();
		boolean win = this.getCamp() == winCamp;
		int addScore = DYZZSeasonService.getInstance().calScoreAdd(this.seasonScore, this.getKda(), win);
		this.seasonScoreAdd = addScore;
		return addScore;
	}

	/**
	 * 计算首胜，结束结算时候调用
	 * @return
	 */
	public int calSeasonFirstWin() {
		DYZZCAMP winCamp = this.getParent().getWinCamp();
		boolean win = (this.getCamp() == winCamp);
		int gradeId = 0;
		if (win && this.winCount == 0) {
			gradeId = DYZZSeasonService.getInstance()
					.recordSeasonFirstWinGrade(this.seasonScore);
		}
		this.seasonFirstReward = gradeId;
		return gradeId;
	}

	public long getCampNoticeTime() {
		return campNoticeTime;
	}

	public void setCampNoticeTime(long campNoticeTime) {
		this.campNoticeTime = campNoticeTime;
	}

	public IDYZZWorldMarch getAssistanceMarch(String viewerId, int pointId) {
		IDYZZWorldMarch bfalse = getParent().getPointMarches(pointId, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE).stream()
				.filter(march -> march.getPlayerId().equals(viewerId)).findAny().orElse(null);
		return bfalse;
	}

	public boolean isSpeedItemFree() {
		return speedItemFree;
	}

	public void setSpeedItemFree(boolean speedItemFree) {
		this.speedItemFree = speedItemFree;
	}

	public int getSpeedItemBuyCnt() {
		return speedItemBuyCnt;
	}

	public void setSpeedItemBuyCnt(int speedItemBuyCnt) {
		this.speedItemBuyCnt = speedItemBuyCnt;
	}

}
