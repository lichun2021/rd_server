package com.hawk.game.module.lianmenxhjz.battleroom.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.lianmenxhjz.battleroom.IXHJZWorldPoint;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.cfg.XHJZBattleCfg;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZQuitReason;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Dress.DressType;
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
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.world.service.WorldPointService;

public abstract class IXHJZPlayer extends Player implements IXHJZWorldPoint {
	private XHJZBattleRoom parent;
	private IXHJZPlayerData playerData;
	private IXHJZPlayerPush playerPush;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	/** 最近一次功击者 */
	private IXHJZPlayer lastAttacker;
	private MarchSet inviewMarchs = new MarchSet();
	/** 城点 */
	private int[] pos = { 0, 0 };
	/** 下次可免费迁城时间 */
	private long nextCityMoveTime;
	private int costCityMoveCount;
	private XHJZ_CAMP camp;
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

	private XHJZQuitReason quitReason;
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
	private double gasoline;
	private int commonder;
	private int xhjzDistributeGasoline;
	private XHJZPlayerEye eye;
	public IXHJZPlayer(HawkXID xid) {
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
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream().filter(m -> m.getParent() == this).count();
	}

	public boolean isInSameGuild(IXHJZPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	public IXHJZPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(IXHJZPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<IXHJZWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE);
	}

	public void quitGame() {

	}

	@Override
	public WorldPointPB.Builder toBuilder(IXHJZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IXHJZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		
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
	public XHJZBattleRoom getParent() {
		return parent;
	}

	public void setParent(XHJZBattleRoom parentRoom) {
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

	public IXHJZPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(IXHJZPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(IXHJZPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public IXHJZPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public IXHJZPlayerPush getPush() {

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

	public void onMarchStart(IXHJZWorldMarch march) {

	}

	public XHJZ_CAMP getCamp() {
		return camp;
	}

	public void setCamp(XHJZ_CAMP camp) {
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
		XHJZBattleCfg cfg = getParent().getCfg();
		return (int) (honor + killPower / cfg.getScoreForKill() + hurtTankPower / cfg.getScoreForDefense());
	}
	
	public int getHurtHonor(){
		XHJZBattleCfg cfg = getParent().getCfg();
		return (int) (hurtTankPower / cfg.getScoreForDefense());
	}

	public int getKillHonor() {
		XHJZBattleCfg cfg = getParent().getCfg();
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

	public IXHJZPlayerPush getPlayerPush() {
		return playerPush;
	}

	public XHJZQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(XHJZQuitReason quitReason) {
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

	public XHJZPlayerEye getEye() {
		return eye;
	}

	public void setEye(XHJZPlayerEye eye) {
		this.eye = eye;
	}

	public double getGasoline() {
		return gasoline;
	}

	public void setGasoline(double gasoline) {
		this.gasoline = gasoline;
	}
	
	public double getMaxGasoline(){
		return getParent().getCfg().getFuelMax();
	}

	public int getCommonder() {
		return commonder;
	}

	public void setCommonder(int commonder) {
		this.commonder = commonder;
	}

	public int getXhjzDistributeGasoline() {
		return xhjzDistributeGasoline;
	}

	public void setXhjzDistributeGasoline(int xhjzDistributeGasoline) {
		this.xhjzDistributeGasoline = xhjzDistributeGasoline;
	}

}
