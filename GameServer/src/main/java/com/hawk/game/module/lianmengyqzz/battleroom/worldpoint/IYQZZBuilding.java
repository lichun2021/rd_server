package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZBuildCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZBuildTypeCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.state.IYQZZBuildingState;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.YQZZDeclareWar;
import com.hawk.game.protocol.World.YQZZHoldRec;
import com.hawk.game.protocol.YQZZ.PBYQZZFirstControlBuildMail;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.object.FoggyInfo;

/**
 * 
 *
 */
public abstract class IYQZZBuilding implements IYQZZWorldPoint {

	private YQZZDeclareWar onwerGuild = YQZZDeclareWar.getDefaultInstance(); // 当前归属联盟id
	private IYQZZBuildingState stateObj;

	private final YQZZBattleRoom parent;
	private int cfgId;
	private int buildTypeId;
	private int foggyFortressId;
	private FoggyInfo foggyInfoObj;
	private YQZZBuildType buildType;
	private int index; // 刷的序号
	private int x;
	private int y;
	private int aoiObjId = 0;
	private IYQZZWorldMarch leaderMarch;
	private List<YQZZHoldRec.Builder> holdRecList = new ArrayList<>();
	private long lastTick;
	private int subarea;
	// /** 联盟控制时间 */
	// private AtomicLongMap<String> controlGuildTimeMap = AtomicLongMap.create();
	// /** 联盟控制取得积分 */
	// private Map<String, Double> controlGuildHonorMap = new HashMap<>();
	// /**国家积分*/
	// private Map<String, Double> controlNationHonorMap = new HashMap<>();

	private Map<String, YQZZBuildingHonor> guildHonorMap = new ConcurrentHashMap<>();

	private boolean firstControl = true;
	private long protectedEndTime;

	Map<String, YQZZDeclareWarInfo> declareWar = new ConcurrentHashMap<>();
	WorldPointPB.Builder secondMapBuilder;

	public IYQZZBuilding(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	/**行军前检查*/
	public Optional<YQZZDeclareWar> getDeclareWarRecord(String guildid) {
		if (Objects.equals(guildid, onwerGuild.getGuildId())) {
			return Optional.ofNullable(onwerGuild);
		}
		if (declareWar.containsKey(guildid)) {
			return Optional.ofNullable(declareWar.get(guildid).getDecl());
		}
		return Optional.ofNullable(null);
	}

	@Override
	public final WorldPointType getPointType() {
		return WorldPointType.YQZZ_BUILDING;
	}

	public YQZZBuildingHonor getBuildingHonor(String guildId) {
		if (!guildHonorMap.containsKey(guildId)) {
			YQZZBuildingHonor value = new YQZZBuildingHonor();
			value.setGuildId(guildId);
			value.setServerId(getParent().getCampBase(guildId).campServerId);
			value.setX(x);
			value.setY(y);
			value.setBuildId(cfgId);
			guildHonorMap.put(guildId, value);
		}
		return guildHonorMap.get(guildId);
	}

	public void cleanGuildMarch(String guildId) {
		try {
			List<IYQZZWorldMarch> pms = getParent().getPointMarches(this.getPointId());
			for (IYQZZWorldMarch march : pms) {
				if (StringUtils.isNotEmpty(guildId) && !Objects.equals(guildId, march.getParent().getGuildId())) {
					continue;
				}

				if (march.isMassMarch() && march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					march.getMassJoinMarchs(true).forEach(jm -> jm.onMarchCallback());
					march.onMarchBack();
				} else {
					march.onMarchCallback();
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public boolean canBeAttack(String guildId) {
		if (underGuildControl(guildId)) {
			return true;
		}
		if (underNationControl(guildId)) {
			return false;
		}
		if (protectedEndTime > getParent().getCurTimeMil()) {
			return false;
		}
		YQZZDeclareWar declareWar = getDeclareWarRecord(guildId).orElse(null);
		if (Objects.isNull(declareWar)) {
			return false;
		}
		if (declareWar.getEndTime() > getParent().getCurTimeMil()) {
			return false;
		}
		for (int linkBuild : getCfg().getLinkList()) {
			IYQZZBuilding build = getParent().getWorldPointService().getBuildingByCfgId(linkBuild);
			if (build.underNationControl(guildId)) {
				return true;
			}
		}
		return false;
	}

	/** 清理宣战记录*/
	public void clearDeclareWarRecord() {
		declareWar.clear();
	}

	public void clearDeclareWarRecord(String guildId) {
		declareWar.remove(guildId);
	}

	public void declareWar(IYQZZPlayer player) {
		if (declareWar.containsKey(player.getGuildId())) {
			return;
		}
		YQZZGuildBaseInfo binfo = getParent().getCampBase(player.getGuildId());
		if (binfo.declareWarPoint < getBuildTypeCfg().getDeclareCost()) {
			return;
		}
		binfo.declareWarPoint -= getBuildTypeCfg().getDeclareCost();

		YQZZDeclareWar dwar = YQZZDeclareWar.newBuilder()
				.setStartTime(getParent().getCurTimeMil())
				.setEndTime(getParent().getCurTimeMil() + getBuildTypeCfg().getDeclareTime() * 1000)
				.setGuildId(player.getGuildId())
				.setGuildName(player.getGuildName())
				.setGuildTag(player.getGuildTag())
				.setGuildFlag(player.getGuildFlag())
				.setPlayerId(player.getId())
				.setPlayerName(player.getName())
				.setFlagView(player.getCamp().intValue())
				.setBattleEnd(getParent().getCurTimeMil() + getBuildTypeCfg().getDeclareTime() * 1000 + getBuildTypeCfg().getBattleTime() * 1000)
				.setServerId(player.getMainServerId())
				.setX(getX())
				.setY(getY())
				.build();
		declareWar.put(dwar.getGuildId(), new YQZZDeclareWarInfo(this, dwar));
		player.getParent().getCampBase(player.getGuildId()).declareWarRecords.add(dwar);

		worldPointUpdate();
		// 广播通知(主动宣战联盟)
		ChatParames paramesAtk = ChatParames.newBuilder()
				.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
				.setKey(NoticeCfgId.YQZZ_GUILD_DECLEAR_BUILD)
				.setGuildId(player.getGuildId())
				.addParms(this.getX())
				.addParms(this.getY())
				.addParms(getBuildTypeCfg().getDeclareTime())
				.build();
		this.parent.addWorldBroadcastMsg(paramesAtk);
		// 广播通知(建筑内被宣战联盟)
		if (this.onwerGuild != null && !Objects.equals(onwerGuild.getGuildId(), player.getGuildId())) {
			ChatParames paramesDef = ChatParames.newBuilder()
					.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
					.setKey(NoticeCfgId.YQZZ_ENEMY_GUILD_DECLEAR_BUILD)
					.setGuildId(this.onwerGuild.getGuildId())
					.addParms(player.getMainServerId())
					.addParms(player.getGuildName())
					.addParms(this.getX())
					.addParms(this.getY())
					.addParms(getBuildTypeCfg().getDeclareTime())
					.build();
			this.parent.addWorldBroadcastMsg(paramesDef);
		}
		LogUtil.logYQZZDeclareWarInfo(player.getId(), player.getName(), player.getGuildId(),
				player.getGuildName(), player.getMainServerId(), this.getCfgId(), this.getParent().getId());
	}

	public boolean underGuildControl(String guildId) {
		return Objects.equals(onwerGuild.getGuildId(), guildId);
	}

	public boolean underNationControl(String guildId) {
		return Objects.equals(onwerGuild.getServerId(), getParent().getCampBase(guildId).campServerId);
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}
		lastTick = getParent().getCurTimeMil();

		long stageOpenTime = getParent().getBattleStageTime().getProtectedEndTime(buildType);
		if (stageOpenTime == 0 && protectedEndTime - lastTick > getBuildTypeCfg().getOccupyProTime() * 1000) { // 当前是开放期, 保护时间不可能超过protime
			protectedEndTime = 0;
		}
		long protectedEndNew = Math.max(protectedEndTime, stageOpenTime);
		if (protectedEndNew != protectedEndTime) {
			protectedEndTime = protectedEndNew;
			worldPointUpdate();
		}
		if (Objects.nonNull(leaderMarch) && protectedEndTime > getParent().getCurTimeMil()) { // 开罩全部行军返回
			cleanGuildMarch("");
		}
		if (protectedEndTime > getParent().getCurTimeMil()) { // 开罩清除宣战
			clearDeclareWarRecord();
		}
		for (YQZZDeclareWarInfo declInfo : declareWar.values()) { // 宣战结束 未成功占据
			declInfo.tick();
			YQZZDeclareWar decl = declInfo.getDecl();
			if (decl.getBattleEnd() < getParent().getCurTimeMil()) {
				if (!Objects.equals(onwerGuild.getGuildId(), decl.getGuildId())) {
					cleanGuildMarch(decl.getGuildId());
				}
				declareWar.remove(decl.getGuildId());
			}
		}

		stateObj.onTick();

		// long timePass = getParent().getCurTimeMil() - lastTick;
		// if (timePass < 1000) {
		// return true;
		// }
		// lastTick = getParent().getCurTimeMil();
		//
		// if (state != YQZZBuildState.ZHAN_LING) {
		// lastControlGuild = "";
		// lastControlTime = 0;
		// }
		//
		// List<IYQZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		// if (stayMarches.isEmpty()) {
		// if (state != YQZZBuildState.ZHONG_LI) {
		// state = YQZZBuildState.ZHONG_LI;
		// getParent().worldPointUpdate(this);
		// }
		// return true;
		// }
		//
		// if (leaderMarch == null || !stayMarches.contains(leaderMarch)) {
		// leaderMarch = stayMarches.get(0);
		// getParent().worldPointUpdate(this);
		// }
		//
		// String cguildId = getGuildId();
		// if (state != YQZZBuildState.ZHONG_LI && !Objects.equals(cguildId, leaderMarch.getParent().getGuildId())) {
		// state = YQZZBuildState.ZHONG_LI;
		// getParent().worldPointUpdate(this);
		// return true;
		// }
		//
		// // 有行军
		// if (state == YQZZBuildState.ZHAN_LING) {
		// controlGuildTimeMap.addAndGet(cguildId, timePass); // 联盟占领时间++
		// controlGuildHonorMap.merge(cguildId, getGuildHonorPerSecond(), (v1, v2) -> v1 + v2);
		// lastControlGuild = cguildId;
		// lastControlTime += timePass;
		// for (IYQZZWorldMarch march : stayMarches) {
		// // 个人积分++ pers
		// double pers = Math.min(getPlayerHonorPerSecond() * 1D / getCollectArmyMin() * march.getMarchEntity().getArmyCount(), getPlayerHonorPerSecond());
		// march.getParent().incrementPlayerHonor(pers);
		// }
		// // System.out.println(getPointType() + "累计占领"+ lastControlGuild + " " + lastControlTime + "cguildId : "+ cguildId);
		// return true;
		// }
		//
		// if (state == YQZZBuildState.ZHONG_LI) {
		// state = YQZZBuildState.ZHAN_LING_ZHONG;
		// zhanLingKaiShi = getParent().getCurTimeMil();
		//
		// int controlCountDown = getControlCountDown();
		//
		// zhanLingJieShu = zhanLingKaiShi + controlCountDown * 1000;
		// getParent().worldPointUpdate(this);
		//
		// // ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_176)
		// // .addParms(getX())
		// // .addParms(getY())
		// // .addParms(cguildId)
		// // .addParms(getGuildTag())
		// // .addParms(getPlayerName())
		// // .build();
		// // getParent().addWorldBroadcastMsg(parames);
		// return true;
		// }
		//
		// if (state == YQZZBuildState.ZHAN_LING_ZHONG) {
		// if (getParent().getCurTimeMil() > zhanLingJieShu) {
		// state = YQZZBuildState.ZHAN_LING;
		// if (firstControl) {// 首控
		// firstControl = false;
		// controlGuildHonorMap.put(cguildId, getFirstControlGuildHonor());
		// for (IYQZZWorldMarch march : stayMarches) {
		// // 个人积分++
		// march.getParent().incrementPlayerHonor(getFirstControlPlayerHonor());
		// }
		// }
		//
		// YQZZHoldRec.Builder hrec = YQZZHoldRec.newBuilder().setHoldTime(zhanLingJieShu).setPlayerName(getPlayerName()).setGuildTag(getGuildTag()).setPtype(getPointType())
		// .setX(x)
		// .setY(y)
		// .setGuildId(cguildId)
		// .setServerId(getGuildServerId());
		// holdRecList.add(hrec);
		// getParent().worldPointUpdate(this);
		//
		// // ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_176)
		// // .addParms(getX())
		// // .addParms(getY())
		// // .addParms(cguildId)
		// // .addParms(getGuildTag())
		// // .addParms(getPlayerName())
		// // .addParms(getParent().getExtParm().getWarType().getNumber())
		// // .build();
		// // getParent().addWorldBroadcastMsg(parames);
		// return true;
		// }
		// }

		return true;
	}

	/** 占领倒计时 /秒 */
	public int getControlCountDown() {
		return getBuildTypeCfg().getOccupyTime();
	}

	public double getNationHonorLastScore() {
		return getBuildTypeCfg().getNationLastScore();
	}

	public double getNationHonorPerSecond() {
		return getBuildTypeCfg().getNationScore();
	}

	public double getGuildHonorPerSecond() {
		return getBuildTypeCfg().getAllianceScore();
	}

	public double getPlayerHonorPerSecond() {
		return getBuildTypeCfg().getPlayerScore();
	}

	public double getFirstNationGuildHonor() {
		return getBuildTypeCfg().getNationFirstScore();
	}

	public double getFirstControlGuildHonor() {
		return getBuildTypeCfg().getAllianceFirstScore();
	}

	public double getFirstControlPlayerHonor() {
		return getBuildTypeCfg().getPlayerFirstScore();
	}

	public int getCollectArmyMin() {
		return 1;
	}

	@Override
	public int getGridCnt() {
		return getBuildTypeCfg().getGridCnt();
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
			return secondMapBuilder;
		}
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getX());
		builder.setPointY(getY());
		builder.setPointType(getPointType());
		builder.setServerId(getGuildServerId());
		// builder.setGuildId(getGuildId());
		builder.setManorState(getState().intValue()); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setFlagView(getGuildCamp().intValue()); // 1 红 ,2 蓝
		for (YQZZDeclareWarInfo declInfo : getDeclareWar().values()) {
			builder.addDeclareWar(declInfo.getDecl());
		}

		builder.setProtectedEndTime(getProtectedEndTime());
		if (onwerGuild.getFlagView() != YQZZ_CAMP.FOGGY.intValue()) {
			builder.setOnwerGuild(onwerGuild);
		}
		secondMapBuilder = builder;
		return secondMapBuilder;
	}

	@Override
	public WorldPointPB.Builder toBuilder(IYQZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setPlayerId(getPlayerId());
		builder.setPlayerName(getPlayerName());
		builder.setGuildId(getGuildId());
		builder.setServerId(getGuildServerId());
		builder.setManorState(getState().intValue()); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setManorBuildName(getGuildName());
		builder.setFlagView(getGuildCamp().intValue()); // 1 红 ,2 蓝
		if (getGuildCamp() == YQZZ_CAMP.FOGGY) {
			builder.setMonsterId(foggyFortressId); // 按配置点顺序出生序号 0 , 1
		}
		List<IYQZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IYQZZWorldMarch march : stayMarches) {
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		if (getLeaderMarch() != null) {
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}

		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		for (YQZZDeclareWarInfo declInfo : declareWar.values()) {
			builder.addDeclareWar(declInfo.getDecl());
		}
		builder.setOnwerGuild(onwerGuild);
		stateObj.fillBuilder(builder);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		if (getLeaderMarch() != null) {
			IYQZZPlayer leader = leaderMarch.getParent();
			builder.setPlayerId(leader.getId());
			builder.setPlayerName(leader.getName());
			builder.setPlayerIcon(leader.getIcon());
			builder.setPlayerPfIcon(leader.getPfIcon());
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}
		builder.setServerId(getGuildServerId());
		builder.setGuildId(getGuildId());
		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		builder.setManorState(getState().intValue());// /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setManorBuildName(getGuildName());
		builder.setFlagView(getGuildCamp().intValue()); // 1 红 ,2 蓝
		if (getGuildCamp() == YQZZ_CAMP.FOGGY) {
			builder.setMonsterId(foggyFortressId); // 按配置点顺序出生序号 0 , 1
			builder.setPower(foggyInfoObj.getTotalPower());
		}
		boolean hasMarchStop = false;
		List<IYQZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IYQZZWorldMarch march : stayMarches) {
			if (!march.getPlayerId().equals(viewer.getId())) {
				continue;
			}
			hasMarchStop = true;
		}
		builder.setHasMarchStop(hasMarchStop);

		for (YQZZHoldRec.Builder hrec : holdRecList) {
			// hrec.setTotalHold(controlGuildTimeMap.get(hrec.getGuildId()));
			// System.out.println("guildId: "+ hrec.getGuildId() +" 控制时间"+ controlGuildTimeMap.get(hrec.getGuildId())+" RECC");
			builder.addYqzzHoldRec(hrec);
		}
		// System.out.println(lastControlTime);
		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		for (YQZZDeclareWarInfo declInfo : declareWar.values()) {
			builder.addDeclareWar(declInfo.getDecl());
		}
		builder.setOnwerGuild(onwerGuild);
		stateObj.fillDetailBuilder(builder);
		return builder;
	}

	@Override
	public long getProtectedEndTime() {
		return protectedEndTime;
	}

	public void setProtectedEndTime(long protectedEndTime) {
		this.protectedEndTime = protectedEndTime;
	}

	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		builder.setPointType(this.getPointType());
		builder.setX(this.getX());
		builder.setY(this.getY());
		// 队长id
		if (getLeaderMarch() == null) {
			return builder;
		}
		IYQZZPlayer leader = getLeaderMarch().getParent();
		String leaderId = leader.getId();
		// 队长
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
		}

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<IYQZZWorldMarch> assistandMarchs = getParent().getPointMarches(this.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IYQZZWorldMarch stayMarch : assistandMarchs) {
			if (Objects.equals(stayMarch.getPlayerId(), leaderId)) {
				// 队长信息
				builder.setLeaderArmyLimit(stayMarch.getMaxMassJoinSoldierNum(leader));
				builder.setLeaderMarch(stayMarch.getGuildWarSingleInfo());
				continue;
			}
			builder.addJoinMarchs(stayMarch.getGuildWarSingleInfo());
			reachArmyCount += WorldUtil.calcSoldierCnt(stayMarch.getMarchEntity().getArmys());
		}
		builder.setCityLevel(leader.getCityLv());
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	public void onMarchReach(IYQZZWorldMarch leaderMarch) {
		String guildId = leaderMarch.getParent().getGuildId();
		if (!canBeAttack(guildId)) {
			cleanGuildMarch(guildId);
			return;
		}
		stateObj.onMarchReach(leaderMarch);
	}

	public void firstHonor() {
		YQZZBuildingHonor buildingHonor = getBuildingHonor(getOnwerGuildId());
		buildingHonor.setFirstControlGuildHonor(getFirstControlGuildHonor());
		buildingHonor.setFirstControlNationHonor(getFirstNationGuildHonor());
		buildingHonor.setFirstControlPlayerHonor(getFirstControlPlayerHonor());
		setFirstControl(false);

		sendFirstControlReward();
	}

	private void sendFirstControlReward() {
		if (StringUtils.isEmpty(getBuildTypeCfg().getAllianceReward())) {
			return;
		}

		List<String> send = new ArrayList<>();
		for (IYQZZPlayer gamer : getParent().getPlayerList(YQZZState.GAMEING)) {
			if (!Objects.equals(gamer.getGuildId(), getGuildId())) {
				continue;
			}
			MailParames parames = MailParames.newBuilder().setPlayerId(gamer.getId())
					.addTitles(getX(), getY())
					.addSubTitles(getX(), getY())
					.setMailId(MailId.ATTACK_YQZZ_BUILD_FIRST_CONTROL)
					.addContents(getX())
					.addContents(getY())
					.setRewards(getBuildTypeCfg().getAllianceReward())
					.setAwardStatus(MailRewardStatus.NOT_GET).build();
			MailService.getInstance().sendMail(parames);
			send.add(gamer.getId());
		}

		PBYQZZFirstControlBuildMail.Builder builder = PBYQZZFirstControlBuildMail.newBuilder();
		builder.setGuildId(getGuildId());
		builder.addAllExclude(send);
		builder.setReward(getBuildTypeCfg().getAllianceReward());
		builder.setX(getX());
		builder.setY(getY());
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.YQZZ_FIRST_CONTROL_MAIL_VALUE, builder), onwerGuild.getServerId(), "");
	}

	public boolean assitenceWarPoint(List<IYQZZWorldMarch> atkMarchList, List<IYQZZWorldMarch> stayMarchList) {
		// 队长
		IYQZZPlayer leader = leaderMarch.getParent();

		// 8.21新增：检查当前自己的行军，如果行军中已有英雄，则将自己队列中的英雄撤回
		for (IYQZZWorldMarch worldMarch : atkMarchList) {
			if (worldMarch.getMarchEntity().getHeroIdList().isEmpty()) {// 如果当前行军中没有英雄直接跳过
				continue;
			}
			// 如果有英雄，则判断当前据点停留的行军有没有自己的
			for (IYQZZWorldMarch stayMarch : stayMarchList) {
				try {
					if (stayMarch.getPlayerId().equals(worldMarch.getPlayerId())) {
						List<Integer> heroId = worldMarch.getMarchEntity().getHeroIdList();
						if (stayMarch.getMarchEntity().getHeroIdList().size() > 0) {
							// 如果停留的行军有自己的并且已经带有英雄，将当前的英雄瞬间返回
							List<PlayerHero> OpHero = stayMarch.getParent().getHeroByCfgId(heroId);
							for (PlayerHero hero : OpHero) {
								hero.backFromMarch(stayMarch);
							}
							worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
							Optional<SuperSoldier> marchSsOp = stayMarch.getParent().getSuperSoldierByCfgId(worldMarch.getSuperSoldierId());
							if (marchSsOp.isPresent()) {
								marchSsOp.get().backFromMarch(stayMarch);
								worldMarch.getMarchEntity().setSuperSoldierId(0);
							}
						} else if (heroId.size() > 0) {
							// 停留行军没有英雄则直接加入
							stayMarch.getMarchEntity().setHeroIdList(heroId);
							worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}

		// 先处理过来行军中的队长行军，队长行军处理完毕后删除
		// if (stayMarchList != null && !stayMarchList.isEmpty()) {
		// int count = stayMarchList.size();// 加上队长
		// if (count > leader.getMaxMassJoinMarchNum() + leaderMarch.getMarchEntity().getBuyItemTimes()) {
		// return returnMarchList(atkMarchList);
		// }
		// }

		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);

		List<WorldMarch> ppList = new ArrayList<>();
		for (IYQZZWorldMarch worldMarch : stayMarchList) {
			ppList.add(worldMarch.getMarchEntity());
		}

		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
		// 剩余人口<0部队返回
		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;
		if (remainArmyPopu <= 0) {
			return returnMarchList(atkMarchList);
		}

		// 优先加入已在玩家
		for (IYQZZWorldMarch stayMarch : stayMarchList) {
			Iterator<IYQZZWorldMarch> it = atkMarchList.iterator();
			while (it.hasNext()) {
				IYQZZWorldMarch massMarch = it.next();
				if (!stayMarch.getPlayerId().equals(massMarch.getPlayerId())) {
					continue;
				}

				List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
				List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
				int stayCnt = WorldUtil.calcStayArmy(massMarch.getMarchEntity(), remainArmyPopu, stayList, backList);

				// 回家的士兵生成新行军
				if (backList.size() > 0) {
					EffectParams effParams = new EffectParams();
					effParams.setArmys(backList);
					IYQZZWorldMarch back = getParent().startMarch(massMarch.getParent(), this, massMarch.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
					back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
				}
				massMarch.remove();

				List<List<ArmyInfo>> lists = new ArrayList<List<ArmyInfo>>();
				lists.add(stayMarch.getMarchEntity().getArmys());
				lists.add(stayList);
				stayMarch.getMarchEntity().setArmys(WorldUtil.mergMultArmyList(lists));

				remainArmyPopu -= stayCnt;
				it.remove();
				break;
			}
		}

		// 一轮援助检查后是否所有行军已处理
		if (atkMarchList.isEmpty()) {
			return true;
		}

		// 加入要留驻的行军
		Iterator<IYQZZWorldMarch> it = atkMarchList.iterator();

		while (it.hasNext()) {
			IYQZZWorldMarch march = it.next();
			int eff1546 = march.getPlayer().getEffect().getEffVal(EffType.HERO_1546, march.getMarchEntity().getEffectParams());
			if (remainArmyPopu + eff1546 <= 0) {
				continue;
			}
			remainArmyPopu += eff1546;
			List<ArmyInfo> stayArmyList = new ArrayList<ArmyInfo>();
			List<ArmyInfo> backArmyList = new ArrayList<ArmyInfo>();
			int stayCnt = WorldUtil.calcStayArmy(march.getMarchEntity(), remainArmyPopu, stayArmyList, backArmyList);
			// 回家的士兵生成新行军
			if (backArmyList.size() > 0) {
				EffectParams effParams = new EffectParams();
				effParams.setArmys(backArmyList);
				IYQZZWorldMarch back = getParent().startMarch(march.getParent(), this, march.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
				back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
			}
			march.getMarchEntity().setArmys(stayArmyList);
			march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			march.updateMarch();
			remainArmyPopu -= stayCnt;
			it.remove();
		}

		// 有多余的行军就原路返回&&通知行军提示变更
		if (atkMarchList.size() > 0) {
			returnMarchList(atkMarchList);
		}
		return true;
	}

	private boolean returnMarchList(List<IYQZZWorldMarch> atkMarchList) {
		for (IYQZZWorldMarch defM : atkMarchList) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}
		return true;
	}

	/** 遣返 */
	public boolean repatriateMarch(IYQZZPlayer comdPlayer, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!Objects.equals(getGuildId(), comdPlayer.getGuildId())) {
			return false;
		}

		// 队长
		IYQZZPlayer leader = leaderMarch.getParent();

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<IYQZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IYQZZWorldMarch iWorldMarch : stayMarches) {
			if (iWorldMarch.isReturnBackMarch()) {
				continue;
			}
			if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
				continue;
			}
			iWorldMarch.onMarchReturn(this.getPointId(), iWorldMarch.getParent().getPointId(), iWorldMarch.getArmys());
		}
		return true;
	}

	/**
	 * 任命队长
	 * 
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean cheangeQuarterLeader(IYQZZPlayer comdPlayer, String targetPlayerId) {

		// 不是本盟的没有权限操作
		if (!comdPlayer.getGuildId().equals(getGuildId())) {
			return false;
		}

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leaderMarch.getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<IYQZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IYQZZWorldMarch march : stayMarches) {
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			leaderMarch = march;
			worldPointUpdate();
			break;
		}
		return true;
	}

	public void syncQuarterInfo(IYQZZPlayer player) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}

		List<IYQZZWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		IYQZZWorldMarch leaderMarch = getLeaderMarch();
		if (leaderMarch != null) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(leaderMarch));
			builder.setMassSoldierNum(leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent()));
		}
		for (IYQZZWorldMarch march : defMarchList) {
			if (march != leaderMarch) {
				builder.addQuarterMarch(getSuperWeaponQuarterMarch(march));
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}

	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(IYQZZWorldMarch march) {
		SuperWeaponQuarterMarch.Builder builder = SuperWeaponQuarterMarch.newBuilder();

		Player snapshot = march.getParent();

		builder.setPlayerId(snapshot.getId());
		builder.setName(snapshot.getName());
		builder.setIcon(snapshot.getIcon());
		builder.setPfIcon(snapshot.getPfIcon());
		builder.setGuildTag(snapshot.getGuildTag());
		builder.setMarchId(march.getMarchId());

		List<ArmyInfo> armys = march.getMarchEntity().getArmys();
		for (ArmyInfo army : armys) {
			if (army.getFreeCnt() <= 0) {
				continue;
			}
			builder.addArmy(army.toArmySoldierPB(snapshot).build());
		}
		for (PlayerHero hero : march.getHeros()) {
			builder.addHeroId(hero.getCfgId());
		}
		List<PlayerHero> heroList = snapshot.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : heroList) {
			builder.addHero(hero.toPBobj());
		}
		SuperSoldier ssoldier = snapshot.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId()).orElse(null);
		if (Objects.nonNull(ssoldier)) {
			builder.setSsoldier(ssoldier.toPBobj());
		}
		return builder;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getPointId() {
		return GameUtil.combineXAndY(x, y);
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

	public YQZZBuildState getState() {
		return stateObj.getState();
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public YQZZBattleRoom getParent() {
		return parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getServerId() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.campServerId;
	}

	@Override
	public String getGuildId() {
		if (getLeaderMarch() == null) {
			return onwerGuild.getGuildId();
		}
		return leaderMarch.getParent().getGuildId();
	}

	public String getGuildName() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.campGuildName;
	}

	public String getGuildTag() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.campGuildTag;
	}

	public int getGuildFlag() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return 0;
		}
		return binfo.campguildFlag;
	}

	public String getGuildServerId() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());

		if (binfo == null) {
			return "";
		}
		return binfo.campServerId;
	}

	public YQZZ_CAMP getGuildCamp() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());

		if (binfo == null) {
			return YQZZ_CAMP.FOGGY;
		}
		return binfo.camp;
	}

	public String getPlayerId() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getId();
	}

	public String getPlayerName() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getName();
	}

	public long getLastTick() {
		return lastTick;
	}

	public void setLastTick(long lastTick) {
		this.lastTick = lastTick;
	}

	public boolean isFirstControl() {
		return firstControl;
	}

	public void setFirstControl(boolean firstControl) {
		this.firstControl = firstControl;
	}

	// public AtomicLongMap<String> getControlGuildTimeMap() {
	// return controlGuildTimeMap;
	// }
	//
	// public void setControlGuildTimeMap(AtomicLongMap<String> controlGuildTimeMap) {
	// this.controlGuildTimeMap = controlGuildTimeMap;
	// }
	//
	// public Map<String, Double> getControlGuildHonorMap() {
	// return controlGuildHonorMap;
	// }
	//
	// public Map<String, Double> getControlNationHonorMap() {
	// return controlNationHonorMap;
	// }

	public IYQZZWorldMarch getLeaderMarch() {
		if (leaderMarch == null || leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			List<IYQZZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (stayMarches.isEmpty()) {
				leaderMarch = null;
				return null;
			}

			leaderMarch = stayMarches.get(0);
		}
		return leaderMarch;
	}

	public YQZZBuildCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(YQZZBuildCfg.class, cfgId);
	}

	public YQZZBuildTypeCfg getBuildTypeCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(YQZZBuildTypeCfg.class, buildTypeId);
	}

	public void setLeaderMarch(IYQZZWorldMarch leaderMarch) {
		this.leaderMarch = leaderMarch;
	}

	@Override
	public int getAoiObjId() {
		return aoiObjId;
	}

	@Override
	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public String getOnwerGuildId() {
		return onwerGuild.getGuildId();
	}

	public YQZZDeclareWar getOnwerGuild() {
		return onwerGuild;
	}

	public void setOnwerGuild(YQZZDeclareWar onwerGuild) {
		this.onwerGuild = onwerGuild;
	}

	public IYQZZBuildingState getStateObj() {
		return stateObj;
	}

	public void setStateObj(IYQZZBuildingState stateObj) {
		this.stateObj = stateObj;
		this.stateObj.init();
		worldPointUpdate();
	}

	public int getSubarea() {
		return subarea;
	}

	public void setSubarea(int subarea) {
		this.subarea = subarea;
	}

	public int getBuildTypeId() {
		return buildTypeId;
	}

	public void setBuildTypeId(int buildTypeId) {
		this.buildTypeId = buildTypeId;
		this.buildType = YQZZBuildType.valueOf(buildTypeId);
	}

	public int getFoggyFortressId() {
		return foggyFortressId;
	}

	public void setFoggyFortressId(int foggyFortressId) {
		this.foggyFortressId = foggyFortressId;
	}

	public List<YQZZHoldRec.Builder> getHoldRecList() {
		return holdRecList;
	}

	public YQZZBuildType getBuildType() {
		return buildType;
	}

	public void setBuildType(YQZZBuildType buildType) {
		this.buildType = buildType;
	}

	public FoggyInfo getFoggyInfoObj() {
		return foggyInfoObj;
	}

	public void setFoggyInfoObj(FoggyInfo foggyInfoObj) {
		this.foggyInfoObj = foggyInfoObj;
	}

	public Map<String, YQZZDeclareWarInfo> getDeclareWar() {
		return declareWar;
	}

}
