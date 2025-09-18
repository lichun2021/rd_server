package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.IXHJZWorldPoint;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZGuildBaseInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.cfg.XHJZBuildCfg;
import com.hawk.game.module.lianmenxhjz.battleroom.cfg.XHJZBuildTypeCfg;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.state.IXHJZBuildingState;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.XHJZHoldRec;
import com.hawk.game.protocol.World.XHJZQuateredState;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.object.FoggyInfo;

/**
 * 
 *
 */
public abstract class IXHJZBuilding implements IXHJZWorldPoint {

	private XHJZGuildBaseInfo onwerGuild = XHJZGuildBaseInfo.getDefaultinstance(); // 当前归属联盟id
	private IXHJZBuildingState stateObj;

	private final XHJZBattleRoom parent;
	private int cfgId;
	private int buildTypeId;
	private int foggyFortressId;
	private FoggyInfo foggyInfoObj;
	private XHJZBuildType buildType;
	private int index; // 刷的序号
	private int x;
	private int y;
	private int aoiObjId = 0;
	private IXHJZWorldMarch leaderMarch;
	private List<XHJZHoldRec.Builder> holdRecList = new ArrayList<>();
	private long lastTick;
	private int subarea;
	// /** 联盟控制时间 */
	// private AtomicLongMap<String> controlGuildTimeMap = AtomicLongMap.create();
	// /** 联盟控制取得积分 */
	// private Map<String, Double> controlGuildHonorMap = new HashMap<>();
	// /**国家积分*/
	// private Map<String, Double> controlNationHonorMap = new HashMap<>();

	private Map<String, XHJZBuildingHonor> guildHonorMap = new ConcurrentHashMap<>();

	private boolean firstControl = true;
	private long protectedEndTime;

	WorldPointPB.Builder lastbuilder;

	public IXHJZBuilding(XHJZBattleRoom parent) {
		this.parent = parent;
	}
	
	@Override
	public final WorldPointType getPointType() {
		return WorldPointType.XHJZ_BUILDING;
	}

	public XHJZBuildingHonor getBuildingHonor(String guildId) {
		if (!guildHonorMap.containsKey(guildId)) {
			XHJZBuildingHonor value = new XHJZBuildingHonor();
			value.setGuildId(guildId);
			value.setServerId(getParent().getCampBase(guildId).getServerId());
			value.setX(x);
			value.setY(y);
			value.setBuildId(cfgId);
			guildHonorMap.put(guildId, value);
		}
		return guildHonorMap.get(guildId);
	}

	public void cleanGuildMarch(String guildId) {
		try {
			List<IXHJZWorldMarch> pms = getParent().getPointMarches(this.getPointId());
			for (IXHJZWorldMarch march : pms) {
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
		
		if (getCfg().getFuelDuce() > 0 && getGuildCamp() != XHJZ_CAMP.NONE && getCfg().getCampType() != getGuildCamp().intValue()) {
			List<IXHJZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			for (IXHJZWorldMarch march : stayMarches) {
				double curGas = Math.max(0, march.getGasoline() - getCfg().getFuelDuce());
				march.setGasoline(curGas);
			}
		}

		return true;
	}

	/** 占领倒计时 /秒 */
	public int getControlCountDown() {
		return getBuildTypeCfg().getOccupyTime();
	}
	
	public double getAllianceScoreAdd() {
		return getBuildTypeCfg().getAllianceScoreAdd();
	}

	public int getCollectArmyMin() {
		return 1;
	}

	@Override
	public void worldPointUpdate() {
		lastbuilder = null;
		IXHJZWorldPoint.super.worldPointUpdate();
	}

	@Override
	public WorldPointPB.Builder toBuilder(IXHJZPlayer viewer) {
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
		List<IXHJZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		int smCnt = 0;
		for (IXHJZWorldMarch march : stayMarches) {
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				builder.addXhjzBianduiNum(march.getMarchEntity().getXhjzBianduiNum());
			}
			if(march.getMarchType() != WorldMarchType.XHJZ_BUILDING_MASS_JOIN){
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
		stateObj.fillBuilder(builder);
		lastbuilder = builder;
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IXHJZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		if (getLeaderMarch() != null) {
			IXHJZPlayer leader = leaderMarch.getParent();
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
		List<IXHJZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		int smCnt = 0;
		for (IXHJZWorldMarch march : stayMarches) {
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
			}
			if(march.getMarchType() != WorldMarchType.XHJZ_BUILDING_MASS_JOIN){
				smCnt++;
			}
		}
		builder.setStayArmycount(smCnt);
		for(XHJZHoldRec.Builder hrec: holdRecList){
			builder.addXhjzHoldRec(hrec);
		}
		// System.out.println(lastControlTime);
		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
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
		IXHJZPlayer leader = getLeaderMarch().getParent();
		String leaderId = leader.getId();
		// 队长
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
		}

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<IXHJZWorldMarch> assistandMarchs = getParent().getPointMarches(this.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXHJZWorldMarch stayMarch : assistandMarchs) {
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

	public void onMarchReach(IXHJZWorldMarch leaderMarch) {
		stateObj.onMarchReach(leaderMarch);
		worldPointUpdate();
	}

	// public void firstHonor() {
	// XHJZBuildingHonor buildingHonor = getBuildingHonor(getOnwerGuildId());
	// buildingHonor.setFirstControlGuildHonor(getFirstControlGuildHonor());
	// buildingHonor.setFirstControlNationHonor(getFirstNationGuildHonor());
	// buildingHonor.setFirstControlPlayerHonor(getFirstControlPlayerHonor());
	// setFirstControl(false);
	//
	// sendFirstControlReward();
	// }

	// private void sendFirstControlReward() {
	// if (StringUtils.isEmpty(getBuildTypeCfg().getAllianceReward())) {
	// return;
	// }
	//
	// List<String> send = new ArrayList<>();
	// for (IXHJZPlayer gamer : getParent().getPlayerList(XHJZState.GAMEING)) {
	// if (!Objects.equals(gamer.getGuildId(), getGuildId())) {
	// continue;
	// }
	// MailParames parames = MailParames.newBuilder().setPlayerId(gamer.getId())
	// .addTitles(getX(), getY())
	// .addSubTitles(getX(), getY())
	// .setMailId(MailId.ATTACK_XHJZ_BUILD_FIRST_CONTROL)
	// .addContents(getX())
	// .addContents(getY())
	// .setRewards(getBuildTypeCfg().getAllianceReward())
	// .setAwardStatus(MailRewardStatus.NOT_GET).build();
	// MailService.getInstance().sendMail(parames);
	// send.add(gamer.getId());
	// }
	//
	// PBXHJZFirstControlBuildMail.Builder builder = PBXHJZFirstControlBuildMail.newBuilder();
	// builder.setGuildId(getGuildId());
	// builder.addAllExclude(send);
	// builder.setReward(getBuildTypeCfg().getAllianceReward());
	// builder.setX(getX());
	// builder.setY(getY());
	// CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.XHJZ_FIRST_CONTROL_MAIL_VALUE, builder), onwerGuild.getServerId(), "");
	// }

//	public boolean assitenceWarPoint(List<IXHJZWorldMarch> atkMarchList, List<IXHJZWorldMarch> stayMarchList) {
//		// 队长
//		IXHJZPlayer leader = leaderMarch.getParent();
//
//		// 8.21新增：检查当前自己的行军，如果行军中已有英雄，则将自己队列中的英雄撤回
//		for (IXHJZWorldMarch worldMarch : atkMarchList) {
//			if (worldMarch.getMarchEntity().getHeroIdList().isEmpty()) {// 如果当前行军中没有英雄直接跳过
//				continue;
//			}
//			// 如果有英雄，则判断当前据点停留的行军有没有自己的
//			for (IXHJZWorldMarch stayMarch : stayMarchList) {
//				try {
//					if (stayMarch.getPlayerId().equals(worldMarch.getPlayerId())) {
//						List<Integer> heroId = worldMarch.getMarchEntity().getHeroIdList();
//						if (stayMarch.getMarchEntity().getHeroIdList().size() > 0) {
//							// 如果停留的行军有自己的并且已经带有英雄，将当前的英雄瞬间返回
//							List<PlayerHero> OpHero = stayMarch.getParent().getHeroByCfgId(heroId);
//							for (PlayerHero hero : OpHero) {
//								hero.backFromMarch(stayMarch);
//							}
//							worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
//							Optional<SuperSoldier> marchSsOp = stayMarch.getParent().getSuperSoldierByCfgId(worldMarch.getSuperSoldierId());
//							if (marchSsOp.isPresent()) {
//								marchSsOp.get().backFromMarch(stayMarch);
//								worldMarch.getMarchEntity().setSuperSoldierId(0);
//							}
//						} else if (heroId.size() > 0) {
//							// 停留行军没有英雄则直接加入
//							stayMarch.getMarchEntity().setHeroIdList(heroId);
//							worldMarch.getMarchEntity().setHeroIdList(Collections.emptyList());
//						}
//					}
//				} catch (Exception e) {
//					HawkException.catchException(e);
//				}
//			}
//		}
//
//		// 先处理过来行军中的队长行军，队长行军处理完毕后删除
//		// if (stayMarchList != null && !stayMarchList.isEmpty()) {
//		// int count = stayMarchList.size();// 加上队长
//		// if (count > leader.getMaxMassJoinMarchNum() + leaderMarch.getMarchEntity().getBuyItemTimes()) {
//		// return returnMarchList(atkMarchList);
//		// }
//		// }
//
//		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);
//
//		List<WorldMarch> ppList = new ArrayList<>();
//		for (IXHJZWorldMarch worldMarch : stayMarchList) {
//			ppList.add(worldMarch.getMarchEntity());
//		}
//
//		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
//		// 剩余人口<0部队返回
//		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;
//		if (remainArmyPopu <= 0) {
//			return returnMarchList(atkMarchList);
//		}
//
//		// 优先加入已在玩家
//		for (IXHJZWorldMarch stayMarch : stayMarchList) {
//			Iterator<IXHJZWorldMarch> it = atkMarchList.iterator();
//			while (it.hasNext()) {
//				IXHJZWorldMarch massMarch = it.next();
//				if (!stayMarch.getPlayerId().equals(massMarch.getPlayerId())) {
//					continue;
//				}
//
//				List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
//				List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
//				int stayCnt = WorldUtil.calcStayArmy(massMarch.getMarchEntity(), remainArmyPopu, stayList, backList);
//
//				// 回家的士兵生成新行军
//				if (backList.size() > 0) {
//					EffectParams effParams = new EffectParams();
//					effParams.setArmys(backList);
//					IXHJZWorldMarch back = getParent().startMarch(massMarch.getParent(), this, massMarch.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
//					back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
//				}
//				massMarch.remove();
//
//				List<List<ArmyInfo>> lists = new ArrayList<List<ArmyInfo>>();
//				lists.add(stayMarch.getMarchEntity().getArmys());
//				lists.add(stayList);
//				stayMarch.getMarchEntity().setArmys(WorldUtil.mergMultArmyList(lists));
//
//				remainArmyPopu -= stayCnt;
//				it.remove();
//				break;
//			}
//		}
//
//		// 一轮援助检查后是否所有行军已处理
//		if (atkMarchList.isEmpty()) {
//			return true;
//		}
//
//		// 加入要留驻的行军
//		Iterator<IXHJZWorldMarch> it = atkMarchList.iterator();
//
//		while (it.hasNext()) {
//			IXHJZWorldMarch march = it.next();
//			int eff1546 = march.getPlayer().getEffect().getEffVal(EffType.HERO_1546, march.getMarchEntity().getEffectParams());
//			if (remainArmyPopu + eff1546 <= 0) {
//				continue;
//			}
//			remainArmyPopu += eff1546;
//			List<ArmyInfo> stayArmyList = new ArrayList<ArmyInfo>();
//			List<ArmyInfo> backArmyList = new ArrayList<ArmyInfo>();
//			int stayCnt = WorldUtil.calcStayArmy(march.getMarchEntity(), remainArmyPopu, stayArmyList, backArmyList);
//			// 回家的士兵生成新行军
//			if (backArmyList.size() > 0) {
//				EffectParams effParams = new EffectParams();
//				effParams.setArmys(backArmyList);
//				IXHJZWorldMarch back = getParent().startMarch(march.getParent(), this, march.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
//				back.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
//			}
//			march.getMarchEntity().setArmys(stayArmyList);
//			march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
//			march.updateMarch();
//			remainArmyPopu -= stayCnt;
//			it.remove();
//		}
//
//		// 有多余的行军就原路返回&&通知行军提示变更
//		if (atkMarchList.size() > 0) {
//			returnMarchList(atkMarchList);
//		}
//		return true;
//	}

//	private boolean returnMarchList(List<IXHJZWorldMarch> atkMarchList) {
//		for (IXHJZWorldMarch defM : atkMarchList) {
//			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
//		}
//		return true;
//	}

	/** 遣返 */
	public boolean repatriateMarch(IXHJZPlayer comdPlayer, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!Objects.equals(getGuildId(), comdPlayer.getGuildId())) {
			return false;
		}

		// 队长
		IXHJZPlayer leader = leaderMarch.getParent();

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<IXHJZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXHJZWorldMarch iWorldMarch : stayMarches) {
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
	public boolean cheangeQuarterLeader(IXHJZPlayer comdPlayer, String targetPlayerId) {

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

		List<IXHJZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXHJZWorldMarch march : stayMarches) {
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			leaderMarch = march;
			worldPointUpdate();
			break;
		}
		return true;
	}

	public void syncQuarterInfo(IXHJZPlayer player) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}

		List<IXHJZWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		IXHJZWorldMarch leaderMarch = getLeaderMarch();
		if (leaderMarch != null) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(leaderMarch));
			builder.setMassSoldierNum(leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent()));
		}
		for (IXHJZWorldMarch march : defMarchList) {
			if (march != leaderMarch) {
				builder.addQuarterMarch(getSuperWeaponQuarterMarch(march));
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}

	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(IXHJZWorldMarch march) {
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

	public XHJZBuildState getState() {
		return stateObj.getState();
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public XHJZBattleRoom getParent() {
		return parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getServerId() {
		XHJZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
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
		XHJZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.getGuildName();
	}

	public String getGuildTag() {
		XHJZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.getGuildTag();
	}

	public int getGuildFlag() {
		XHJZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return 0;
		}
		return binfo.getGuildFlag();
	}

	public String getGuildServerId() {
		XHJZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());

		if (binfo == null) {
			return "";
		}
		return binfo.getServerId();
	}

	public XHJZ_CAMP getGuildCamp() {
		XHJZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return XHJZ_CAMP.NONE;
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

	public IXHJZWorldMarch getLeaderMarch() {
		if (leaderMarch == null || leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			List<IXHJZWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (stayMarches.isEmpty()) {
				leaderMarch = null;
				return null;
			}

			leaderMarch = stayMarches.get(0);
		}
		return leaderMarch;
	}

	public XHJZBuildCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(XHJZBuildCfg.class, cfgId);
	}

	public XHJZBuildTypeCfg getBuildTypeCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(XHJZBuildTypeCfg.class, buildTypeId);
	}

	@Override
	public int getGridCnt() {
		return getBuildTypeCfg().getGridCnt();
	}

	public void setLeaderMarch(IXHJZWorldMarch leaderMarch) {
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

	public XHJZGuildBaseInfo getOnwerGuild() {
		return onwerGuild;
	}

	public void setOnwerGuild(XHJZGuildBaseInfo onwerGuild) {
		this.onwerGuild = onwerGuild;
	}

	public IXHJZBuildingState getStateObj() {
		return stateObj;
	}

	public void setStateObj(IXHJZBuildingState stateObj) {
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
		this.buildType = XHJZBuildType.valueOf(buildTypeId);
	}

	public int getFoggyFortressId() {
		return foggyFortressId;
	}

	public void setFoggyFortressId(int foggyFortressId) {
		this.foggyFortressId = foggyFortressId;
	}

	public List<XHJZHoldRec.Builder> getHoldRecList() {
		return holdRecList;
	}

	public XHJZBuildType getBuildType() {
		return buildType;
	}

	public void setBuildType(XHJZBuildType buildType) {
		this.buildType = buildType;
	}

	public FoggyInfo getFoggyInfoObj() {
		return foggyInfoObj;
	}

	public void setFoggyInfoObj(FoggyInfo foggyInfoObj) {
		this.foggyInfoObj = foggyInfoObj;
	}

	public XHJZQuateredState getMarchQuateredStatus(IXHJZWorldMarch march) {
		return stateObj.getMarchQuateredStatus(march);
		
	}
	
}
