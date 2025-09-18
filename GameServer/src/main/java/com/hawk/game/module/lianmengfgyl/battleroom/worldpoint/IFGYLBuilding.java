package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLGuildBaseInfo;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;
import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLEnemyCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.state.FGYLBuildingStateYuri;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.state.IFGYLBuildingState;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.protocol.World.PBFGYLBuildHurtRank;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.object.FoggyInfo;

/**
 * 
 *
 */
public abstract class IFGYLBuilding implements IFGYLWorldPoint {

	private FGYLGuildBaseInfo onwerGuild = FGYLGuildBaseInfo.getDefaultinstance(); // 当前归属联盟id
	private IFGYLBuildingState stateObj;
	private int foggyFortressId;
	private FoggyInfo foggyInfoObj;
	private final FGYLBattleRoom parent;
	private int cfgId;
	private int buildTypeId;
	private FGYLBuildType buildType;
	private int index; // 刷的序号
	private int x;
	private int y;
	private int aoiObjId = 0;
	private IFGYLWorldMarch leaderMarch;
	private long lastTick;
	private int subarea;
	// /** 联盟控制时间 */
	// private AtomicLongMap<String> controlGuildTimeMap = AtomicLongMap.create();
	// /** 联盟控制取得积分 */
	// private Map<String, Double> controlGuildHonorMap = new HashMap<>();
	// /**国家积分*/
	// private Map<String, Double> controlNationHonorMap = new HashMap<>();

	private boolean firstControl = true;
	private long protectedEndTime;

	private Map<String, FGYLBuildHurtRecord> foggyHurtMap = new HashMap<>();
	WorldPointPB.Builder lastbuilder;

	public IFGYLBuilding(FGYLBattleRoom parent) {
		this.parent = parent;
	}

	public void addHurtVal(IFGYLPlayer player, int kill) {
		FGYLBuildHurtRecord record = foggyHurtMap.get(player.getId());
		if (record == null) {
			record = new FGYLBuildHurtRecord();
			record.setName(player.getName());
			record.setPlayerId(player.getId());
			foggyHurtMap.put(player.getId(), record);
		}
		record.addKill(kill);
	}

	public List<PBFGYLBuildHurtRank> foggyHurtRank() {
		return foggyHurtMap.values().stream()
				.sorted(Comparator.comparingInt(FGYLBuildHurtRecord::getKill).reversed().thenComparing(Comparator.comparingLong(FGYLBuildHurtRecord::getLastUpdate)))
				.limit(50)
				.map(FGYLBuildHurtRecord::toPBObj)
				.collect(Collectors.toList());
	}
	
	public TemporaryMarch getYuriMarch(){
		FGYLEnemyCfg enemy = getCfg();
		
		NpcPlayer ghostplayer = new NpcPlayer(HawkXID.nullXid());
		for (Entry<EffType, Integer> ent : enemy.getNpcEffectList().entrySet()) {
			ghostplayer.addEffectVal(ent.getKey(), ent.getValue());
		}
		
		List<PlayerHero> hero = NPCHeroFactory.getInstance().get(foggyInfoObj.getHeroIds());
		ghostplayer.setPlayerId("OiO");
		ghostplayer.setName("妖妖灵");
		ghostplayer.setPlayerPos(getPointId());
		ghostplayer.setHeros(hero);
		TemporaryMarch asmarch = new TemporaryMarch();
		asmarch.setPlayer(ghostplayer);
		asmarch.setMarchId(ghostplayer.getId());
		asmarch.setMarchType(WorldMarchType.GHOST_STRIKE);
		asmarch.setArmys(foggyInfoObj.getArmyList());
		asmarch.setHeros(hero);
		
		return asmarch;
	}

	@Override
	public int getGridCnt() {
		return getCfg().getGridCnt();
	}

	@Override
	public final WorldPointType getPointType() {
		return WorldPointType.FGYL_BUILDING;
	}

	public void cleanGuildMarch(String guildId) {
		try {
			List<IFGYLWorldMarch> pms = getParent().getPointMarches(this.getPointId());
			for (IFGYLWorldMarch march : pms) {
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

	public boolean underGuildControl(String guildId) {
		return Objects.equals(onwerGuild.getGuildId(), guildId);
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}
		lastTick = getParent().getCurTimeMil();

		stateObj.onTick();

		return true;
	}

	public int getCollectArmyMin() {
		return 1;
	}

	@Override
	public void worldPointUpdate() {
		lastbuilder = null;
		IFGYLWorldPoint.super.worldPointUpdate();
	}

	@Override
	public WorldPointPB.Builder toBuilder(IFGYLPlayer viewer) {
		if (lastbuilder != null) {
			return lastbuilder;
		}

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
		List<IFGYLWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		int smCnt = 0;
		for (IFGYLWorldMarch march : stayMarches) {
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
			}
			if (march.getMarchType() != WorldMarchType.FGYL_BUILDING_MASS_JOIN) {
				smCnt++;
			}
		}
		builder.setStayArmycount(smCnt);
		if (getLeaderMarch() != null) {
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}

		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		lastbuilder = builder;
		if(getState() ==FGYLBuildState.YOULING){
			FGYLBuildingStateYuri yuristate = (FGYLBuildingStateYuri) getStateObj();
			builder.setMonsterMaxBlood(yuristate.getMaxBlood());
			builder.setRemainBlood(yuristate.getRemainBlood());
		}
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IFGYLPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		if (getLeaderMarch() != null) {
			IFGYLPlayer leader = leaderMarch.getParent();
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
		List<IFGYLWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		int smCnt = 0;
		for (IFGYLWorldMarch march : stayMarches) {
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
			}
			if (march.getMarchType() != WorldMarchType.FGYL_BUILDING_MASS_JOIN) {
				smCnt++;
			}
		}
		builder.setStayArmycount(smCnt);
		// System.out.println(lastControlTime);
		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		builder.addAllFgylHurtRank(foggyHurtRank());

		if(getState() ==FGYLBuildState.YOULING){
			FGYLBuildingStateYuri yuristate = (FGYLBuildingStateYuri) getStateObj();
			builder.setMonsterMaxBlood(yuristate.getMaxBlood());
			builder.setRemainBlood(yuristate.getRemainBlood());
		}
		
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
		IFGYLPlayer leader = getLeaderMarch().getParent();
		String leaderId = leader.getId();
		// 队长
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
		}

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<IFGYLWorldMarch> assistandMarchs = getParent().getPointMarches(this.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IFGYLWorldMarch stayMarch : assistandMarchs) {
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

	public void onMarchReach(IFGYLWorldMarch leaderMarch) {
		stateObj.onMarchReach(leaderMarch);
		worldPointUpdate();
	}

	/** 遣返 */
	public boolean repatriateMarch(IFGYLPlayer comdPlayer, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!Objects.equals(getGuildId(), comdPlayer.getGuildId())) {
			return false;
		}

		// 队长
		IFGYLPlayer leader = leaderMarch.getParent();

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<IFGYLWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IFGYLWorldMarch iWorldMarch : stayMarches) {
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
	public boolean cheangeQuarterLeader(IFGYLPlayer comdPlayer, String targetPlayerId) {

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

		List<IFGYLWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IFGYLWorldMarch march : stayMarches) {
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			leaderMarch = march;
			worldPointUpdate();
			break;
		}
		return true;
	}

	public void syncQuarterInfo(IFGYLPlayer player) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}

		List<IFGYLWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		IFGYLWorldMarch leaderMarch = getLeaderMarch();
		if (leaderMarch != null) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(leaderMarch));
			builder.setMassSoldierNum(leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent()));
		}
		for (IFGYLWorldMarch march : defMarchList) {
			if (march != leaderMarch) {
				builder.addQuarterMarch(getSuperWeaponQuarterMarch(march));
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}

	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(IFGYLWorldMarch march) {
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

	public FGYLBuildState getState() {
		return stateObj.getState();
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public FGYLBattleRoom getParent() {
		return parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getServerId() {
		FGYLGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.getServerId();
	}

	@Override
	public String getGuildId() {
		if (getLeaderMarch() == null) {
			return onwerGuild.getGuildId();
		}
		return leaderMarch.getParent().getGuildId();
	}

	public String getGuildName() {
		FGYLGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.getGuildName();
	}

	public String getGuildTag() {
		FGYLGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.getGuildTag();
	}

	public int getGuildFlag() {
		FGYLGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return 0;
		}
		return binfo.getGuildFlag();
	}

	public String getGuildServerId() {
		FGYLGuildBaseInfo binfo = getParent().getCampBase(getGuildId());

		if (binfo == null) {
			return "";
		}
		return binfo.getServerId();
	}

	public FGYL_CAMP getGuildCamp() {
		FGYLGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return FGYL_CAMP.NONE;
		}
		return binfo.getCamp();
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

	public IFGYLWorldMarch getLeaderMarch() {
		if (leaderMarch == null || leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			List<IFGYLWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (stayMarches.isEmpty()) {
				leaderMarch = null;
				return null;
			}

			leaderMarch = stayMarches.get(0);
		}
		return leaderMarch;
	}

	public FGYLEnemyCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(FGYLEnemyCfg.class, cfgId);
	}

	public void setLeaderMarch(IFGYLWorldMarch leaderMarch) {
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

	public FGYLGuildBaseInfo getOnwerGuild() {
		return onwerGuild;
	}

	public void setOnwerGuild(FGYLGuildBaseInfo onwerGuild) {
		this.onwerGuild = onwerGuild;
	}

	public IFGYLBuildingState getStateObj() {
		return stateObj;
	}

	public void setStateObj(IFGYLBuildingState stateObj) {
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
		this.buildType = FGYLBuildType.valueOf(buildTypeId);
	}

	public FGYLBuildType getBuildType() {
		return buildType;
	}

	public void setBuildType(FGYLBuildType buildType) {
		this.buildType = buildType;
	}

	public int getFoggyFortressId() {
		return foggyFortressId;
	}

	public void setFoggyFortressId(int foggyFortressId) {
		this.foggyFortressId = foggyFortressId;
	}

	public FoggyInfo getFoggyInfoObj() {
		return foggyInfoObj;
	}

	public void setFoggyInfoObj(FoggyInfo foggyInfoObj) {
		this.foggyInfoObj = foggyInfoObj;
	}

	public Map<String, FGYLBuildHurtRecord> getFoggyHurtMap() {
		return foggyHurtMap;
	}

	public void setFoggyHurtMap(Map<String, FGYLBuildHurtRecord> foggyHurtMap) {
		this.foggyHurtMap = foggyHurtMap;
	}

	public WorldPointPB.Builder getLastbuilder() {
		return lastbuilder;
	}

	public void setLastbuilder(WorldPointPB.Builder lastbuilder) {
		this.lastbuilder = lastbuilder;
	}

}
