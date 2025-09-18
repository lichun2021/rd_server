package com.hawk.game.lianmengjunyan.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengjunyan.ILMJYWorldPoint;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.World.EquipTechLevel;
import com.hawk.game.protocol.World.SignatureState;
import com.hawk.game.protocol.World.SuperVipSkinEffect;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointPB.Builder;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.WorldShowDress;
import com.hawk.game.service.ArmyService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.KeyValuePair;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

public abstract class ILMJYPlayer extends Player implements ILMJYWorldPoint {
	private LMJYBattleRoom parent;
	private ILMJYPlayerData playerData;
	private ILMJYPlayerPush playerPush;
	private long onFireEndTime;
	private int cityDefVal;
	private int maxCityDef;
	private long cityDefNextRepairTime;
	/** 最近一次功击者 */
	private ILMJYPlayer lastAttacker;
	private int killCount;
	/** 城点 */
	private int[] pos = { 0, 0 };
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
	
	public ILMJYPlayer(HawkXID xid) {
		super(xid);
	}

	public abstract void init();

	/** 行军加速 */
	public abstract double getMarchSpeedUp();

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.PLAYER;
	}
	
	/**行军出去的兵回来 */
	public boolean onMarchArmyBack(ILMJYWorldMarch worldMarch) {
		// 部队回城
		return ArmyService.getInstance().onArmyBack(this, worldMarch.getMarchEntity().getArmys(), worldMarch.getMarchEntity().getHeroIdList(),
				worldMarch.getMarchEntity().getSuperSoldierId(), worldMarch);
	}

	public int getMarchCount() {
		return (int) getParent().getWorldMarchList().stream()
				.filter(m -> m.getParent() == this)
				.count();
	}

	public boolean isInSameGuild(ILMJYPlayer tar) {
		return Objects.equals(getGuildId(), tar.getGuildId());
	}

	public ILMJYPlayer getLastAttacker() {
		return lastAttacker;
	}

	public void setLastAttacker(ILMJYPlayer lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	/** 到达援助行军 */
	public List<ILMJYWorldMarch> assisReachMarches() {
		return getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE);
	}
	
	public boolean isExtraSpyMarchOpen() {
		return WorldMarchService.getInstance().isExtraSpyMarchOpen(this);
	}

	public boolean isExtraSypMarchOccupied() {
		List<ILMJYWorldMarch> spyMarchs = getParent().getPlayerMarches(this.getId(), WorldMarchType.SPY);
		for (IWorldMarch march : spyMarchs) {
			if (march.isExtraSpyMarch()) {
				return true;
			}
		}

		return false;
	}
	
	public void quitGame() {

	}

	@Override
	public Builder toBuilder(String viewerId) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		String thisPlayerId = getId();
		builder.setPlayerId(thisPlayerId);
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());

		if (hasGuild()) {
			builder.setGuildId(getGuildId());
			builder.setGuildTag(getGuildTag());
			builder.setGuildFlag(getGuildFlag());
		}

		if (!HawkOSOperator.isEmptyString(viewerId)) {
			if (getParent().isHasAssistanceMarch(viewerId, getPointId())) {
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
		builder.setProtectedEndTime(getParent().getStartTime());

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
	public WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(getPosXY()[0]);
		builder.setPointY(getPosXY()[1]);
		builder.setPointType(WorldPointType.PLAYER);

		builder.setPlayerId(getId());
		builder.setPlayerName(getName());
		builder.setCityLevel(getCityLevel());
		builder.setPlayerIcon(getIcon());
		builder.setPlayerPfIcon(getPfIcon());
		String guildId = getGuildId();
		if (hasGuild()) {
			builder.setGuildId(guildId);
			builder.setGuildTag(getGuildTag());
			builder.setGuildFlag(getGuildFlag());
		}

		if (!HawkOSOperator.isEmptyString(viewerId)) {
			if (getParent().isHasAssistanceMarch(viewerId, getPointId())) {
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
		builder.setProtectedEndTime(getParent().getStartTime());
		
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

	public LMJYBattleRoom getParent() {
		return parent;
	}

	public void setParent(LMJYBattleRoom parentRoom) {
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

	public ILMJYPlayerData getPlayerData() {
		return playerData;
	}

	public void setPlayerData(ILMJYPlayerData playerData) {
		this.playerData = playerData;
	}

	public void setPlayerPush(ILMJYPlayerPush playerPush) {
		this.playerPush = playerPush;
	}

	@Override
	public ILMJYPlayerData getData() {
		return playerData;
	}

	@Override
	public PlayerEffect getEffect() {

		return playerData.getPlayerEffect();
	}

	@Override
	public ILMJYPlayerPush getPush() {

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

	public void onMarchStart(ILMJYWorldMarch march) {

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
	
	public void updateEquipTechLevel(int techId, int level) {
		EquipTechLevel.Builder builder = EquipTechLevel.newBuilder();
		builder.setTechId(techId);
		builder.setLevel(level);
		builderList.add(builder.build());
	}

}
