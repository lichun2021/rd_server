package com.hawk.game.module.lianmengtaiboliya.player;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.hawk.app.HawkObjModule;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.protocol.HawkProtocolException;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.game.config.EquipResearchCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.player.action.TBLYTestMarchAction;
import com.hawk.game.module.lianmengtaiboliya.player.module.TBLYArmyModule;
import com.hawk.game.module.lianmengtaiboliya.player.module.TBLYMarchModule;
import com.hawk.game.module.lianmengtaiboliya.player.module.TBLYWorldModule;
import com.hawk.game.module.lianmengtaiboliya.player.module.guildformation.TBLYGuildFormationModule;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerSerializeData;
import com.hawk.game.player.hero.PlayerHero;
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
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Player.HPPlayerInfoSync;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.PlayerInfo;
import com.hawk.game.protocol.Player.PlayerStatus;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.health.entity.UpdateUserInfoResult;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.sdk.msdk.entity.PayItemInfo;

public class TBLYPlayer extends ITBLYPlayer {
	private long lastTickTime;
	private Player source;

	private boolean inited;
	private long nextLoghonor;

	private TBLYTestMarchAction action;
	
	public TBLYPlayer(Player player) {
		super(player.getXid());
		this.setGuildId(player.getGuildId());
		this.source = player;
	}

	public void init() {
		if (inited) {
			return;
		}
		int maxCityDef = source.getData().getRealMaxCityDef();
		this.setMaxCityDef(maxCityDef);
		this.setCityDefVal(maxCityDef);
		this.setPlayerData(TBLYPlayerData.valueOf(this));
		TBLYPlayerPush playerPush = new TBLYPlayerPush(this);
		this.setPlayerPush(playerPush);
		getParent();
		this.nextLoghonor = getParent().getCurTimeMil() + TBLYConst.MINUTE_MICROS;
		this.checkEquipResearchUnlock();
		this.setDeployedSwInfo(source.getData().getDeployedSwInfo());
		this.setMechacoreShowInfo(source.getMechacoreShowInfo());
		inited = true;

		if (isRobot()) {
			action = new TBLYTestMarchAction();
			action.setParent(this);
		}
	}
	
	@Override
	public void initModules() {
		registerModule(TBLYConst.ModuleType.TBLYWorld, new TBLYWorldModule(this));
		registerModule(TBLYConst.ModuleType.TBLYMarch, new TBLYMarchModule(this));
		registerModule(TBLYConst.ModuleType.TBLYArmy, new TBLYArmyModule(this));
		// 联盟编队
		registerModule(TBLYConst.ModuleType.TBLYGuildFormation, new TBLYGuildFormationModule(this));
	}
	

	@Override
	public ArmourBriefInfo genArmourBriefInfo(ArmourSuitType suit) {
		return source.genArmourBriefInfo(suit);
	}

	@Override
	public <T extends HawkObjModule> T getModule(int moduleId) {
		T result = source.getModule(moduleId);
		if (result == null) {
			result = super.getModule(moduleId);
		}
		return result;
	}

	@Override
	public boolean hasCollege() {
		return source.hasCollege();
	}

	@Override
	public boolean onTick() {
		long now = getParent().getCurTimeMil();
		if (now - lastTickTime > 1000) {
			checkSuperVipSkinToMassMarch();
			setTBLYState(TBLYState.GAMEING);
			getData().lockOriginalData();
			lastTickTime = now;
			// 为安全. 结束前5秒偷偷清理队列
			if (getParent().maShangOver()) {
				getData().getQueueEntities().clear();
			}

			if (getOnFireEndTime() > now) {
				int cityDefVal = getCityDefVal();
				// double beforePct = cityDefVal * 1D / getMaxCityDef();
				cityDefVal = Math.max(0, cityDefVal - getParent().getCfg().getFireSpeed());
				setCityDefVal(cityDefVal);
				getPush().syncCityDef(false);

				// double afterPct = getCityDefVal() * 1D / getMaxCityDef();
				// if (beforePct > 0.3 && afterPct < 0.3) {
				// ChatParames parames = ChatParames.newBuilder()
				// .setChatType(ChatType.CHAT_TBLY_SYS)
				// .setKey(NoticeCfgId.TBLY_CITY_DEF_30)
				// .addParms(getName())
				// .build();
				// getParent().addWorldBroadcastMsg(parames);
				// }
			}

			if (Objects.nonNull(action)) {
				action.onTick();
			}
		}

		if (now > nextLoghonor) {
			nextLoghonor = now + TBLYConst.MINUTE_MICROS;
			LogUtil.logTBLYPlayerHonor(this, getHonor());
		}
		for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
			try {
				entry.getValue().onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}
	
	/**
	 * 检测集结行军
	 */
	public void checkSuperVipSkinToMassMarch() {
		try {
			if (!getSuperVipObject().isSuperVipOpen() || getSuperVipSkinActivatedLevel() <= 0) {
				return;
			}
			List<ITBLYWorldMarch> marchs = getParent().getPlayerMarches(getId());
			OptionalLong optional = marchs.stream().filter(e -> e.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE)
					.mapToLong(e -> e.getMarchEntity().getStartTime()).max();
			long maxTime = Math.max(0, optional.orElse(0));

			if (maxTime != getSuperVipSkinEffEndTime()) {
				setSuperVipSkinEffEndTime(maxTime);
				setSuperVipSkinLevel(getSuperVipSkinActivatedLevel());
				getParent().worldPointUpdate(this);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void checkEquipResearchUnlock() {
		if (!GameUtil.checkPhaseOneArmourTechMaxLevel(getData())) {
			return;
		}
		List<EquipResearchEntity> equipResearchEntityList = getData().getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, researchEntity.getResearchId());
			if (researchCfg != null && researchCfg.getPhaseTwo() > 0) {
				updateEquipTechLevel(researchEntity.getResearchId(), researchEntity.getResearchLevel());
			}
		}
	}
	
	@Override
	public PlayerVipSuper getSuperVipObject() {
		return getSource().getSuperVipObject();
	}
	

	@Override
	public void quitGame() {
		if (isAnchor()) {
			getParent().synAnchorPageInfo(this, false);
		}
		PlayerInfo.Builder newBuilder = PlayerInfo.newBuilder();
		newBuilder.setLmjyState(0);
		newBuilder.setIsTBLYAnchor(false);
		sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_INFO_SYNC_S, HPPlayerInfoSync.newBuilder().setPlayerInfo(newBuilder)));

		source.setTBLYRoomId("");
		source.setTBLYState(null);
		source.getPush().syncPlayerInfo();

		TBLYRoomManager.getInstance().invalidate(this);
	}

	@Override
	public String getTBLYRoomId() {
		return source.getTBLYRoomId();
	}

	@Override
	public void setTBLYRoomId(String tblyRoomId) {
		source.setTBLYRoomId(tblyRoomId);
	}

	@Override
	public TBLYState getTBLYState() {
		return source.getTBLYState();
	}

	@Override
	public void setTBLYState(TBLYState tblyState) {
		source.setTBLYState(tblyState);
	}

	@Override
	public int getSoldierStar(int armyId) {
		return source.getSoldierStar(armyId);
	}
	
	@Override
	public int getSoldierStep(int armyId) {
		return source.getSoldierStep(armyId);
	}

	@Override
	public void updateData(PlayerData playerData) {

		source.updateData(playerData);
	}

	@Override
	public int getAoiObjId() {

		return source.getAoiObjId();
	}

	@Override
	public void setAoiObjId(int aoiObjId) {

	}

	@Override
	public boolean isBackground() {

		return source.isBackground();
	}

	@Override
	public void resetParam() {

		source.resetParam();
	}

	@Override
	public void setBackground(long backgroundTime) {

		source.setBackground(backgroundTime);
	}

	@Override
	public long getBackground() {

		return source.getBackground();
	}

	@Override
	public JSONObject getPfTokenJson() {

		return source.getPfTokenJson();
	}

	@Override
	public void setPfTokenJson(JSONObject pfTokenJson) {

		source.setPfTokenJson(pfTokenJson);
	}

	@Override
	public long getHealthGameUpdateTime() {

		return source.getHealthGameUpdateTime();
	}

	@Override
	public void setHealthGameUpdateTime(long healthGameUpdateTime) {

		source.setHealthGameUpdateTime(healthGameUpdateTime);
	}

	@Override
	public long getNextRemindTime() {

		return source.getNextRemindTime();
	}

	@Override
	public void setNextRemindTime(long nextRemindTime) {

		source.setNextRemindTime(nextRemindTime);
	}

	@Override
	public long getHealthGameTickTime() {

		return source.getHealthGameTickTime();
	}

	@Override
	public void setHealthGameTickTime(long healthGameTickTime) {

		source.setHealthGameTickTime(healthGameTickTime);
	}

	@Override
	public boolean isAdult() {

		return source.isAdult();
	}

	@Override
	public void setAdult(boolean adult) {

		source.setAdult(adult);
	}

	@Override
	public void clearData(boolean isRemove) {

		source.clearData(isRemove);
	}

	@Override
	public PlayerEntity getEntity() {

		return source.getEntity();
	}

	@Override
	public PlayerBaseEntity getPlayerBaseEntity() {

		return source.getPlayerBaseEntity();
	}

	@Override
	public long getCreateTime() {

		return source.getCreateTime();
	}

	@Override
	public int getActiveState() {

		return source.getActiveState();
	}

	@Override
	public void setActiveState(int activeState) {

		source.setActiveState(activeState);
	}

	@Override
	public boolean isActiveOnline() {

		return source.isActiveOnline();
	}

	@Override
	public void idipChangeDiamonds(boolean diamondsChange) {

		source.idipChangeDiamonds(diamondsChange);
	}

	@Override
	public List<Integer> getUnlockedSoldierIds() {

		return source.getUnlockedSoldierIds();
	}

	@Override
	public String getId() {

		return source.getId();
	}

	@Override
	public String getPuid() {

		return source.getPuid();
	}

	@Override
	public String getOpenId() {

		return source.getOpenId();
	}

	@Override
	public String getServerId() {

		return source.getServerId();
	}
	
	@Override
	public String getMainServerId() {
		return source.getMainServerId();
	}

	@Override
	public String getName() {

		return source.getName();
	}

	@Override
	public String getNameEncoded() {

		return source.getNameEncoded();
	}

	@Override
	public String getNameWithGuildTag() {

		return source.getNameWithGuildTag();
	}

	@Override
	public int getIcon() {

		return source.getIcon();
	}

	@Override
	public String getPfIcon() {

		return source.getPfIcon();
	}

	@Override
	public int getShowVIPLevel() {

		return source.getShowVIPLevel();
	}

	@Override
	public String getChannel() {

		return source.getChannel();
	}

	@Override
	public String getCountry() {

		return source.getCountry();
	}

	@Override
	public String getDeviceId() {

		return source.getDeviceId();
	}

	@Override
	public int getExp() {

		return source.getExp();
	}

	@Override
	public int getExpDec() {

		return source.getExpDec();
	}

	@Override
	public int getVipExp() {

		return source.getVipExp();
	}

	@Override
	public String getAppVersion() {

		return source.getAppVersion();
	}

	@Override
	public String getLanguage() {

		return source.getLanguage();
	}

	@Override
	public long getLoginTime() {

		return source.getLoginTime();
	}

	@Override
	public long getLogoutTime() {

		return source.getLogoutTime();
	}

	@Override
	public int getOnlineTimeHistory() {

		return source.getOnlineTimeHistory();
	}

	@Override
	public String getClientIp() {

		return source.getClientIp();
	}

	@Override
	public void setCityLevel(int level) {

		source.setCityLevel(level);
	}

	@Override
	public int getCityLevel() {

		return source.getCityLevel();
	}

	@Override
	public String getGameAppId() {

		return source.getGameAppId();
	}

	@Override
	public int getPlatId() {

		return source.getPlatId();
	}

	@Override
	public String getChannelId() {

		return source.getChannelId();
	}

	@Override
	public void setFirstLogin(int firstLogin) {

		source.setFirstLogin(firstLogin);
	}

	@Override
	public int getFirstLogin() {

		return source.getFirstLogin();
	}

	@Override
	public String getPlatform() {

		return source.getPlatform();
	}

	@Override
	public String getPhoneInfo() {

		return source.getPhoneInfo();
	}

	@Override
	public String getTelecomOper() {

		return source.getTelecomOper();
	}

	@Override
	public String getNetwork() {

		return source.getNetwork();
	}

	@Override
	public String getClientHardware() {

		return source.getClientHardware();
	}

	@Override
	public int getGold() {

		return source.getGold();
	}

	@Override
	public int getDiamonds() {

		return source.getDiamonds();
	}

	@Override
	public int getVit() {

		return source.getVit();
	}

	@Override
	public int getCoin() {

		return source.getCoin();
	}

	@Override
	public long getPower() {
		return source.getPower();
	}

	@Override
	public int getLevel() {

		return source.getLevel();
	}

	@Override
	public int getCityLv() {

		return source.getCityLv();
	}

	@Override
	public int getMilitaryRankLevel() {

		return source.getMilitaryRankLevel();
	}

	@Override
	public int getVipLevel() {

		return source.getVipLevel();
	}

	@Override
	public long[] getPlunderResAry(int[] RES_TYPE) {

		return source.getPlunderResAry(RES_TYPE);
	}

	@Override
	public int getEffPerByResType(int resType, EffType... effTypes) {

		return source.getEffPerByResType(resType, effTypes);
	}

	@Override
	public Map<Integer, Long> getResBuildOutput() {

		return source.getResBuildOutput();
	}

	@Override
	public void decResStoreByPercent(BuildingType buildType, double decPercent) {

		source.decResStoreByPercent(buildType, decPercent);
	}

	@Override
	public long getGoldore() {

		return source.getGoldore();
	}

	@Override
	public long getGoldoreUnsafe() {

		return source.getGoldoreUnsafe();
	}

	@Override
	public long getOil() {

		return source.getOil();
	}

	@Override
	public long getOilUnsafe() {

		return source.getOilUnsafe();
	}

	@Override
	public long getSteel() {

		return source.getSteel();
	}

	@Override
	public long getSteelUnsafe() {

		return source.getSteelUnsafe();
	}

	@Override
	public long getTombarthite() {

		return source.getTombarthite();
	}

	@Override
	public long getTombarthiteUnsafe() {

		return source.getTombarthiteUnsafe();
	}

	@Override
	public long getSteelSpy() {

		return source.getSteelSpy();
	}

	@Override
	public long getTombarthiteSpy() {

		return source.getTombarthiteSpy();
	}

	@Override
	public void sendError(int hpCode, int errCode, int errFlag, String... params) {

		source.sendError(hpCode, errCode, errFlag);
	}

	@Override
	public void sendError(int hpCode, ProtocolMessageEnum errCode, int errFlag) {

		source.sendError(hpCode, errCode, errFlag);
	}

	public void sendError(int hpCode, ProtocolMessageEnum errCode) {
		sendError(hpCode, errCode, 0);
	}

	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		if (Objects.equals(getTBLYRoomId(), getParent().getXid().getUUID())) {
			return source.sendProtocol(protocol);
		}
		return true;
	}

	@Override
	public boolean sendProtocol(HawkProtocol protocol, long delayTime) {

		return source.sendProtocol(protocol, delayTime);
	}

	@Override
	public void responseSuccess(int hpCode) {

		source.responseSuccess(hpCode);
	}

	@Override
	public void notifyPlayerKickout(int reason, String msg) {

		source.notifyPlayerKickout(reason, msg);
	}

	@Override
	public void kickout(int reason, boolean notify, String msg) {

		source.kickout(reason, notify, msg);
	}

	@Override
	public void lockPlayer() {

		source.lockPlayer();
	}

	@Override
	public void unLockPlayer() {

		source.unLockPlayer();
	}

	@Override
	public void unLockPlayer(int errorCode) {

		source.unLockPlayer(errorCode);
	}

	@Override
	public void setCareBanStartTime(long startTime) {

		source.setCareBanStartTime(startTime);
	}

	@Override
	public void setScoreBatchTime(long scoreBatchTime) {
		source.setScoreBatchTime(scoreBatchTime);
	}
	
	public PlayerTickTimeLine getTickTimeLine() {
		return source.getTickTimeLine();
	}

	@Override
	public long onCityShieldChange(StatusDataEntity entity, long currentTime) {
		return source.onCityShieldChange(entity, currentTime);
	}

	@Override
	public void cityShieldRemovePrepareNotice(StatusDataEntity entity, long leftTime) {
		source.cityShieldRemovePrepareNotice(entity, leftTime);
	}

	@Override
	public void onBufChange(int statusId, long endTime) {
		source.onBufChange(statusId, endTime);
	}

	/** 返回true 协议处理在此技术. false 由玩家继续处理 */
	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		if (!checkProtocolElapse(protocol)) {
			HawkLog.errPrintln("appObj protocol elapse check failed, xid: {}, protocol: {}", getXid().toString(), protocol.getType());
			return false;
		}

		boolean processed = false;
		for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
			try {
				if (entry.getValue().isListenProto(protocol.getType())) {
					entry.getValue().onProtocol(protocol);
					processed |= true;
				}
			} catch (HawkProtocolException pe) {
				onProtocolException(pe.getProtocolId(), pe.getErrorCode(), pe.getErrorMsg());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		return processed;
	}

	@Override
	protected void onProtocolException(int protocolId, int errorCode, String errorMsg) {

		// player.onProtocolException(protocolId, errorCode, errorMsg);
	}

	@Override
	public void onSessionClosed() {

		source.onSessionClosed();
	}

	@Override
	public boolean doPlayerAssembleAndLogin(HawkSession session, HPLogin loginCmd) {

		return source.doPlayerAssembleAndLogin(session, loginCmd);
	}

	@Override
	public void noticeSystemEvent(SystemEvent event) {

		source.noticeSystemEvent(event);
	}

	@Override
	public void increaseVit(int count, Action action, boolean isRecover) {

		source.increaseVit(count, action, isRecover);
	}

	@Override
	public int getMaxVit() {

		return source.getMaxVit();
	}

	@Override
	public PlayerLevelExpCfg getCurPlayerLevelCfg() {

		return source.getCurPlayerLevelCfg();
	}

	@Override
	public void increaseGold(long gold, Action action) {

		source.increaseGold(gold, action);
	}

	@Override
	public void increaseGold(long gold, Action action, boolean needLog) {

		source.increaseGold(gold, action, needLog);
	}

	@Override
	public boolean consumeGold(int needGold, Action action) {

		return source.consumeGold(needGold, action);
	}

	@Override
	public boolean increaseDiamond(int diamond, Action action) {

		return source.increaseDiamond(diamond, action);
	}

	@Override
	public boolean increaseDiamond(int diamond, Action action, String extendParam, String midasReason) {

		return source.increaseDiamond(diamond, action, extendParam, midasReason);
	}

	@Override
	public String consumeDiamonds(int diamond, Action action, List<PayItemInfo> payItems) {

		return source.consumeDiamonds(diamond, action, payItems);
	}

	@Override
	public void increaseCoin(int coin, Action action) {

		source.increaseCoin(coin, action);
	}

	@Override
	public void consumeCoin(int coin, Action action) {

		source.consumeCoin(coin, action);
	}

	@Override
	public List<ItemEntity> increaseTools(ItemInfo itemAdd, Action action, ItemCfg itemCfg) {

		return source.increaseTools(itemAdd, action, itemCfg);
	}

	@Override
	public void consumeTool(String id, int itemType, int disCount, Action action) {

		source.consumeTool(id, itemType, disCount, action);
	}

	@Override
	public void increaseResource(long addCnt, int resType, Action action) {

		source.increaseResource(addCnt, resType, action);
	}

	@Override
	public void increaseResource(long addCnt, int resType, Action action, boolean needLog) {

		source.increaseResource(addCnt, resType, action, needLog);
	}

	@Override
	public long getResByType(int type) {

		return source.getResByType(type);
	}

	@Override
	public long getResbyType(PlayerAttr type) {

		return source.getResbyType(type);
	}

	@Override
	public long getAllResByType(int type) {

		return source.getAllResByType(type);
	}

	@Override
	public void consumeResource(long subCnt, int resType, Action action) {

		source.consumeResource(subCnt, resType, action);
	}

	@Override
	public void consumeVit(int vit, Action action) {

		source.consumeVit(vit, action);
	}

	@Override
	public void increaseLevel(int level, Action action) {

		source.increaseLevel(level, action);
	}

	@Override
	public void decreaceVipExp(int subVipExp, Action action) {

		source.decreaceVipExp(subVipExp, action);
	}

	@Override
	public void increaseVipExp(int addExp, Action action) {

		source.increaseVipExp(addExp, action);
	}

	@Override
	public void decreaseExp(int subExp, Action action) {

		source.decreaseExp(subExp, action);
	}

	@Override
	public void increaseExp(int exp, Action action, boolean push) {

		source.increaseExp(exp, action, push);
	}

	@Override
	public int getMaxCaptiveNum() {

		return 1000000000;
	}

	@Override
	public int getMaxMassJoinMarchNum() {
		int defNum = WorldMarchConstProperty.getInstance().getAssemblyQueueNum();
		defNum += getData().getEffVal(EffType.GUILD_TEAM_MEMBER);
		return defNum;
	}

	@Override
	public int getMaxMarchSoldierNum(EffectParams effParams) {

		return source.getMaxMarchSoldierNum(effParams);
	}

	@Override
	public int getMaxMarchNum() {

		return source.getMaxMarchNum();
	}

	@Override
	public int getMaxAllMarchSoldierNum(EffectParams effParams) {

		return source.getMaxAllMarchSoldierNum(effParams);
	}

	@Override
	public int getMaxAssistSoldier() {

		return source.getMaxAssistSoldier();
	}

	@Override
	public int getRemainDisabledCap() {

		return 99999999;
	}

	@Override
	public int getCannonCap() {

		return source.getCannonCap();
	}

	@Override
	public int getMaxCapNum() {

		return 999999999;
	}
	
	@Override
	public int getPlantRemainDisabledCap() {
		return 999999999;
	}
	
	
	@Override
	public int getPlantMaxCapNum() {
		return 999999999;
	}

	@Override
	public int getMarketBurden() {

		return source.getMarketBurden();
	}

	@Override
	public void refreshPowerElectric(PowerChangeReason reason) {

		source.refreshPowerElectric(reason);
	}

	@Override
	public void refreshPowerElectric(boolean isArmyCure, PowerChangeReason reason) {

		source.refreshPowerElectric(isArmyCure, reason);
	}

	@Override
	public void joinGuild(String guildId, boolean isCreate) {

		source.joinGuild(guildId, isCreate);
	}

	@Override
	public void quitGuild(String guildId) {

		source.quitGuild(guildId);
	}

	@Override
	public long getGuildContribution() {

		return source.getGuildContribution();
	}

	@Override
	public void consumeGuildContribution(int value, Action action) {

		source.consumeGuildContribution(value, action);
	}

	@Override
	public int getGuildAuthority() {

		return source.getGuildAuthority();
	}

	@Override
	public int getFreeBuildingTime() {

		return source.getFreeBuildingTime();
	}

	@Override
	public int getFreeTechTime() {

		return source.getFreeTechTime();
	}

	@Override
	public void unlockArea(int buildAreaId) {

		source.unlockArea(buildAreaId);
	}

	@Override
	public boolean isZeroEarningState() {

		return source.isZeroEarningState();
	}

	@Override
	public long getZeroEarningTime() {

		return source.getZeroEarningTime();
	}

	@Override
	public long getOilConsumeTime() {

		return source.getOilConsumeTime();
	}

	@Override
	public void sendIDIPZeroEarningMsg() {

		source.sendIDIPZeroEarningMsg();
	}

	@Override
	public void sendIDIPZeroEarningMsg(int msgCode) {

		source.sendIDIPZeroEarningMsg(msgCode);
	}

	@Override
	public Optional<PlayerHero> getHeroByCfgId(int heroId) {

		return source.getHeroByCfgId(heroId);
	}

	@Override
	public Optional<SuperSoldier> getSuperSoldierByCfgId(int soldierId) {

		return source.getSuperSoldierByCfgId(soldierId);
	}

	@Override
	public List<PlayerHero> getHeroByCfgId(List<Integer> heroIdList) {

		return source.getHeroByCfgId(heroIdList);
	}

	@Override
	public List<PlayerHero> getAllHero() {

		return source.getAllHero();
	}

	@Override
	public List<SuperSoldier> getAllSuperSoldier() {

		return source.getAllSuperSoldier();
	}

	@Override
	public void productResourceQuickly(long timeLong, boolean safe) {

		source.productResourceQuickly(timeLong, safe);
	}

	@Override
	public boolean collectResource(String buildingId, int buildCfgId, long timeLong, AwardItems award, boolean safe) {

		return source.collectResource(buildingId, buildCfgId, timeLong, award, safe);
	}

	@Override
	public void addRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award, boolean safe) {

		source.addRes(addTotalOre, addTotalOil, addTotalSteel, addTotalRare, award, safe);
	}

	@Override
	public void addSafeRes(double addTotalOre, double addTotalOil, double addTotalSteel, double addTotalRare, AwardItems award) {

		source.addSafeRes(addTotalOre, addTotalOil, addTotalSteel, addTotalRare, award);
	}

	@Override
	public int getHeroEffectValue(int heroId, EffType eType) {

		return source.getHeroEffectValue(heroId, eType);
	}

	@Override
	public List<Integer> getCastedSkill() {

		return source.getCastedSkill();
	}

	@Override
	public void updateRankScore(int msgId, RankType rankType, long score) {

		source.updateRankScore(msgId, rankType, score);
	}

	@Override
	public void removeSkillBuff(int skillId) {

		source.removeSkillBuff(skillId);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId, long endTime) {

		return source.addStatusBuff(buffId, endTime);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId) {

		return source.addStatusBuff(buffId);
	}

	@Override
	public StatusDataEntity addStatusBuff(int buffId, String targetId) {

		return source.addStatusBuff(buffId, targetId);
	}

	@Override
	public void removeCityShield() {

		source.removeCityShield();
	}

	@Override
	public List<ArmyEntity> defArmy() {

		return source.defArmy();
	}

	@Override
	public List<ArmyEntity> marchArmy() {

		return source.marchArmy();
	}

	@Override
	public boolean isRobot() {
		if (getParent().IS_GO_MODEL && !isActiveOnline()) {
			return true;
		}
		return source.isRobot();
	}

	@Override
	public Set<String> getShieldPlayers() {

		return source.getShieldPlayers();
	}

	@Override
	public void addShieldPlayer(String shieldPlayerId) {

		source.addShieldPlayer(shieldPlayerId);
	}

	@Override
	public void removeShieldPlayer(String shieldPlayerId) {

		source.removeShieldPlayer(shieldPlayerId);
	}

	@Override
	public List<Integer> getUnlockedResourceType() {

		return source.getUnlockedResourceType();
	}

	@Override
	public boolean isSecondaryBuildQueueUsable() {

		return source.isSecondaryBuildQueueUsable();
	}

	@Override
	public void refreshVipBenefitBox() {

		source.refreshVipBenefitBox();
	}

	@Override
	public int checkBalance() {

		return source.checkBalance();
	}

	@Override
	public void rechargeSuccess(int playerSaveAmt, int rechargeAmt, int diamonds) {

		source.rechargeSuccess(playerSaveAmt, rechargeAmt, diamonds);
	}

	@Override
	public String pay(int diamond, String actionName, List<PayItemInfo> payItems) {

		return source.pay(diamond, actionName, payItems);
	}

	@Override
	public boolean cancelPay(int diamond, String billno) {

		return source.cancelPay(diamond, billno);
	}

	@Override
	public int present(int diamond, String extendParam, String actionName, String presentReason) {

		return source.present(diamond, extendParam, actionName, presentReason);
	}

	@Override
	public String payBuyItems(PayGiftCfg giftCfg) {

		return source.payBuyItems(giftCfg);
	}

	@Override
	public boolean isLocker() {

		return source.isLocker();
	}

	@Override
	public void setLocker(boolean isLocker) {

		source.setLocker(isLocker);
	}

	@Override
	public void synPlayerStatus(PlayerStatus status) {

		source.synPlayerStatus(status);
	}

	@Override
	public void synPlayerStatus(PlayerStatus status, int errorCode) {

		source.synPlayerStatus(status, errorCode);
	}

	@Override
	public PlayerSerializeData getPlayerSerializeData() {

		return source.getPlayerSerializeData();
	}

	@Override
	public String toAnchorJsonStr() {

		return source.toAnchorJsonStr();
	}

	@Override
	public JSONObject toAnchorJsonObj() {

		return source.toAnchorJsonObj();
	}

	@Override
	public void updateRemindTime() {

		source.updateRemindTime();
	}

	@Override
	public void healthGameRemind(UpdateUserInfoResult userInfo) {

		source.healthGameRemind(userInfo);
	}

	@Override
	public void sendHealthGameRemind(int type, int peroidTime, int restTime, long endTime) {

		source.sendHealthGameRemind(type, peroidTime, restTime, endTime);
	}

	@Override
	public void increaseMilitaryRankExp(int add, Action action) {

		source.increaseMilitaryRankExp(add, action);
	}

	@Override
	public void addMoneyReissueItem(int moneyCount, Action action, String additionalParam) {

		source.addMoneyReissueItem(moneyCount, action, additionalParam);
	}

	@Override
	public String getAccessToken() {

		return source.getAccessToken();
	}

	@Override
	public int getToolBackGoldToday(int golds) {

		return source.getToolBackGoldToday(golds);
	}

	@Override
	public int getPlayerRegisterDays() {

		return source.getPlayerRegisterDays();
	}

	@Override
	public HPChatState getChatState() {

		return source.getChatState();
	}

	@Override
	public void setChatState(HPChatState chatState) {

		source.setChatState(chatState);
	}

	@Override
	public void sendIdipMsg(String idipMsg) {

		source.sendIdipMsg(idipMsg);
	}

	@Override
	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, int msgCode) {

		source.sendIdipNotice(type, mode, relieveTime, msgCode);
	}

	@Override
	public void sendIdipNotice(NoticeType type, NoticeMode mode, long relieveTime, String msg) {

		source.sendIdipNotice(type, mode, relieveTime, msg);
	}

	@Override
	public boolean isBuildingLockByYuri(int buildType, String buildIndex) {

		return source.isBuildingLockByYuri(buildType, buildIndex);
	}

	@Override
	public boolean isBuildingLockByYuri(String buildIndex) {

		return source.isBuildingLockByYuri(buildIndex);
	}

	@Override
	public int getMaxTrainNum() {

		return source.getMaxTrainNum();
	}

	@Override
	public LoginWay getLoginWay() {

		return source.getLoginWay();
	}

	@Override
	public void setLoginWay(LoginWay loginWay) {

		source.setLoginWay(loginWay);
	}

	@Override
	public long getLastPowerScore() {

		return source.getLastPowerScore();
	}

	@Override
	public void setLastPowerScore(long lastPowerScore) {
		source.setLastPowerScore(lastPowerScore);
	}

	@Override
	public boolean needJoinGuild() {
		return false;
	}

	public Player getSource() {
		return source;
	}

	public void setSource(Player source) {
		this.source = source;
	}

	@Override
	public double getMarchSpeedUp() {
		return getParent().getCfg().getPlayerMarchSpeedUp();
	}

	@Override
	public void removeWorldPoint() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCsPlayer() {
		return source.isCsPlayer();
	}

	@Override
	public CsPlayer getCsPlayer() {
		if (this.isCsPlayer()) {
			return (CsPlayer) source;
		} else {
			return null;
		}
	}
}
