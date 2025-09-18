package com.hawk.game.module.lianmengXianquhx.worldpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.XQHXGuildBaseInfo;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager.XQHX_CAMP;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXBuildCfg;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXBuildTypeCfg;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.worldmarch.IXQHXWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.XQHXHoldRec;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 
 *
 */
public abstract class IXQHXBuilding implements IXQHXWorldPoint {
	private final XQHXBattleRoom parent;
	private XQHXBuildState state = XQHXBuildState.ZHONG_LI;
	/** 占领开始 */
	private long zhanLingKaiShi;
	private long zhanLingJieShu;
	private int index; // 刷的序号
	private int x;
	private int y;
	private boolean firstControl = true;
	private long lastTick;
	private int cfgId;
	private int buildTypeId;
	private IXQHXWorldMarch leaderMarch;
	private List<XQHXHoldRec> holdRecList = new ArrayList<>();
	private List<String> defMailList = new LinkedList<>();
	private List<String> spyMailList = new LinkedList<>();

	/** 联盟控制时间 */
	private AtomicLongMap<String> controlGuildTimeMap = AtomicLongMap.create();
	/** 联盟控制取得积分 */
	private Map<String, Double> controlGuildHonorMap = new HashMap<>();

	/**camp围困 不能行军*/
	private XQHX_CAMP order10001DebuffCamp;

	public IXQHXBuilding(XQHXBattleRoom parent) {
		this.parent = parent;
	}

	public XQHXBuildCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(XQHXBuildCfg.class, cfgId);
	}

	public XQHXBuildTypeCfg getBuildTypeCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(XQHXBuildTypeCfg.class, buildTypeId);
	}

	public boolean underGuildControl(String guildId) {
		return getState() == XQHXBuildState.ZHAN_LING && Objects.equals(this.getGuildId(), guildId);
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}
		lastTick = getParent().getCurTimeMil();

		List<IXQHXWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		if (stayMarches.isEmpty()) {
			leaderMarch = null;
			if (state != XQHXBuildState.ZHONG_LI) {
				state = XQHXBuildState.ZHONG_LI;
				getParent().worldPointUpdate(this);
			}
			return true;
		}

		if (leaderMarch == null || !stayMarches.contains(leaderMarch)) {
			leaderMarch = stayMarches.get(0);
			getParent().worldPointUpdate(this);
		}

		if (state != XQHXBuildState.ZHONG_LI && !Objects.equals(getGuildId(), leaderMarch.getParent().getGuildId())) {
			state = XQHXBuildState.ZHONG_LI;
			getParent().worldPointUpdate(this);
			return true;
		}

		// 有行军
		if (state == XQHXBuildState.ZHAN_LING) {
			controlGuildTimeMap.addAndGet(getGuildId(), timePass); // 联盟占领时间++
			controlGuildHonorMap.merge(getGuildId(), getGuildHonorPerSecond(), (v1, v2) -> v1 + v2);
			
			XQHXGuildBaseInfo ginfo = getParent().getCampBase(getGuildId());
			ginfo.campOrder += getBuildTypeCfg().getAllianceOrder();
			ginfo.buildControlHonor += getGuildHonorPerSecond();
			for (IXQHXWorldMarch march : stayMarches) {
				// 个人积分++ pers
				double pers = Math.min(getPlayerHonorPerSecond() * 1D / Math.max(1, getCollectArmyMin()) * march.getMarchEntity().getArmyCount(), getPlayerHonorPerSecond());
				march.getParent().incrementBuildHonor(pers);
			}
			return true;
		}

		if (state == XQHXBuildState.ZHONG_LI) {
			state = XQHXBuildState.ZHAN_LING_ZHONG;
			zhanLingKaiShi = getParent().getCurTimeMil();

			int controlCountDown = controlTime();

			zhanLingJieShu = zhanLingKaiShi + controlCountDown * 1000;
			getParent().worldPointUpdate(this);

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.XQHX_122)
					.addParms(getX())
					.addParms(getY())
					.addParms(leaderMarch.getParent().getCamp().intValue())
					.addParms(getGuildTag())
					.addParms(getPlayerName())
					.build();
			getParent().addWorldBroadcastMsg(parames);
			return true;
		}

		if (state == XQHXBuildState.ZHAN_LING_ZHONG) {
			if (getParent().getCurTimeMil() > zhanLingJieShu) {
				state = XQHXBuildState.ZHAN_LING;
				if (firstControl) {// 首控
					firstControl = false;
					double firstControlGuildHonor = getFirstControlGuildHonor();
					controlGuildHonorMap.put(getGuildId(), firstControlGuildHonor);
					getParent().getCampBase(leaderMarch.getParent().getCamp()).firstControlHonor += firstControlGuildHonor;

					for (IXQHXWorldMarch march : stayMarches) {
						// 个人积分++
						march.getParent().incrementBuildHonor(getFirstControlPlayerHonor());
					}
				}

				XQHXHoldRec hrec = XQHXHoldRec.newBuilder().setHoldTime(zhanLingJieShu).setPlayerName(getPlayerName()).setGuildTag(getGuildTag()).setPtype(getPointType()).setX(x)
						.setY(y)
						.setFlagView(stayMarches.get(0).getParent().getCamp().intValue()).build();
				holdRecList.add(hrec);
				getParent().worldPointUpdate(this);

				if (getGuildHonorPerSecond() > 0) {
					NoticeCfgId noticeId = NoticeCfgId.XQHX_BUILD_CONTROL;
					ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(noticeId)
							.addParms(getX())
							.addParms(getY())
							.addParms(leaderMarch.getParent().getCamp().intValue())
							.addParms(getGuildTag())
							.addParms(getPlayerName())
							.build();
					getParent().addWorldBroadcastMsg(parames);
				}
				return true;
			}
		}

		return true;
	}

	private int controlTime() {
		int controlCountDown = getControlCountDown();
		return controlCountDown;
	}

	/** 占领倒计时 /秒 */
	public int getControlCountDown() {
		return getBuildTypeCfg().getOccupyTime();
	}

	public double getGuildHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getBuildTypeCfg().getAllianceScore() * beiShu;
	}

	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getBuildTypeCfg().getPlayerScore() * beiShu;
	}

	public double getFirstControlGuildHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getBuildTypeCfg().getAllianceFirstScore() * beiShu;
	}

	public double getFirstControlPlayerHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getBuildTypeCfg().getPlayerFirstScore() * beiShu;
	}

	/**保护罩时间*/
	public int getProtectTime() {
		return getBuildTypeCfg().getPeaceTime();
	}

	public int getCollectArmyMin() {
		return getBuildTypeCfg().getCollectArmyMin();
	}

	public void onPlayerLogin(IXQHXPlayer gamer) {
	}

	public void anchorJoin(IXQHXPlayer gamer) {
	}

	@Override
	public WorldPointPB.Builder toBuilder(IXQHXPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setPlayerId(getPlayerId());
		builder.setPlayerName(getPlayerName());
		builder.setGuildId(getGuildId());
		builder.setManorState(state.intValue()); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		int stayArmycount = 0;
		List<IXQHXWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXQHXWorldMarch march : stayMarches) {
			if (viewer.isAnchor()) {
				stayArmycount += ArmyService.getInstance().getArmysCount(march.getArmys());
			}
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 ,2 蓝
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		builder.setStayArmycount(stayArmycount);
		if (getLeaderMarch() != null) {
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}

		builder.setGuildTag(getGuildTag());
		builder.setGuildFlag(getGuildFlag());
		if (state != XQHXBuildState.ZHONG_LI) {
			builder.setManorBuildTime(zhanLingKaiShi); // 占领开始时间
			builder.setManorComTime(zhanLingJieShu); // 占领结束时间
		}

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		if (!Objects.equals(viewer.getGuildId(), getGuildId())) {// 核弹攻击
			builder.setCounterAttack(Objects.equals(viewer.getGuildId(), getParent().getNuclearReadGuild()));
		}
		builder.setTblyPerMinHonor((int) Math.ceil(getGuildHonorPerSecond() * 60));
		builder.setLastActiveTime(zhanLingJieShu);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IXQHXPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		if (getLeaderMarch() != null) {
			IXQHXPlayer leader = leaderMarch.getParent();
			builder.setPlayerId(leader.getId());
			builder.setPlayerName(leader.getName());
			builder.setPlayerIcon(leader.getIcon());
			builder.setPlayerPfIcon(leader.getPfIcon());
			builder.setGuildId(leader.getGuildId());
			builder.setGuildTag(leader.getGuildTag());
			builder.setGuildFlag(leader.getGuildFlag());
			BuilderUtil.buildMarchEmotion(builder, getLeaderMarch().getMarchEntity());
			builder.setMarchId(leaderMarch.getMarchId());
		}
		builder.setManorState(state.intValue());// /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setMonsterId(index); // 按配置点顺序出生序号 0 , 1
		int stayArmycount = 0;
		List<IXQHXWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXQHXWorldMarch march : stayMarches) {
			if (viewer.isAnchor()) {
				stayArmycount += ArmyService.getInstance().getArmysCount(march.getArmys());
			}
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 ,2 蓝
			if (march.getPlayerId().equals(viewer.getId())) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
				break;
			}
		}
		builder.setStayArmycount(stayArmycount);
		if (state != XQHXBuildState.ZHONG_LI) {
			builder.setManorBuildTime(zhanLingKaiShi);// 占领开始时间
			builder.setManorComTime(zhanLingJieShu);// 占领结束时间
		}
		builder.addAllXqhxHoldRec(holdRecList);

		// 城点保护时间
		builder.setProtectedEndTime(getProtectedEndTime());
		if (!Objects.equals(viewer.getGuildId(), getGuildId())) {// 核弹攻击
			builder.setCounterAttack(Objects.equals(viewer.getGuildId(), getParent().getNuclearReadGuild()));
		}
		builder.setTblyPerMinHonor((int) (getGuildHonorPerSecond() * 60 + 0.1));
		builder.setLastActiveTime(zhanLingJieShu);
		return builder;
	}

	@Override
	public long getProtectedEndTime() {
		return getParent().getCreateTime() + getProtectTime() * 1000;
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
		IXQHXPlayer leader = getLeaderMarch().getParent();
		String leaderId = leader.getId();
		// 队长
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = leader.getGuildTag();
			builder.setGuildTag(guildTag);
			builder.setGuildId(leader.getGuildId());
		}

		// 已经到达的士兵数量
		int reachArmyCount = 0;
		List<IXQHXWorldMarch> assistandMarchs = getParent().getPointMarches(this.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXQHXWorldMarch stayMarch : assistandMarchs) {
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

	@Override
	public void onMarchReach(IXQHXWorldMarch leaderMarch) {
		if (order10001DebuffCamp == leaderMarch.getParent().getCamp()) {
			if (leaderMarch.isMassMarch()) {
				leaderMarch.getMassJoinMarchs(true).forEach(jm -> jm.onMarchCallback());
			}
			leaderMarch.onMarchCallback();
			DungeonRedisLog.log(getParent().getId(), "{} beiWeiKun 立即返回 {}", order10001DebuffCamp, leaderMarch);
			return;
		}

		IXQHXPlayer player = leaderMarch.getParent();
		// 进攻方玩家
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<IXQHXWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (IXQHXWorldMarch iWorldMarch : atkMarchList) {
			// 去程到达目标点，变成停留状态
			iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
			iWorldMarch.getMarchEntity().setReachTime(leaderMarch.getMarchEntity().getEndTime());
			iWorldMarch.updateMarch();
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		List<IWorldMarch> defMarchs = new ArrayList<>();
		// 防守方行军
		List<IXQHXWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		IXQHXWorldMarch enemyLeadmarch = getLeaderMarch();
		if (Objects.nonNull(enemyLeadmarch)) {// 队长排第一
			defMarchList.remove(enemyLeadmarch);
			defMarchList.add(0, enemyLeadmarch);
		}
		for (IXQHXWorldMarch iWorldMarch : defMarchList) {
			defMarchs.add(iWorldMarch);
			defPlayers.add(iWorldMarch.getParent());
		}

		if (defMarchs.isEmpty()) {
			state = XQHXBuildState.ZHONG_LI;
			this.leaderMarch = leaderMarch;
			for (IXQHXWorldMarch iWorldMarch : atkMarchList) {
				iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				iWorldMarch.updateMarch();
			}
			return;
		}

		if (Objects.equals(player.getGuildId(), defPlayers.get(0).getGuildId())) { // 同阵营
			try {
				assitenceWarPoint(atkMarchList, defMarchList);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			// for (IXQHXWorldMarch iWorldMarch : atkMarchList) {
			// iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			// iWorldMarch.updateMarch();
			// }
			return;
		}

		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_TBLY_BUILD, this.getPointId(), atkPlayers, defPlayers, atkMarchs,
				defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.setXQHXMail(getParent().getExtParm());
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.XQHX);
		// 战斗胜利
		final boolean isAtkWin = battleOutcome.isAtkWin();
		/********* 击杀/击伤部队数据 *********/
		getParent().calcKillAndHurtPower(battleOutcome, atkPlayers, defPlayers);
		/********* 击杀/击伤部队数据 *********/

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		List<ArmyInfo> atkArmyLeft = WorldUtil.mergAllPlayerArmy(atkArmyLeftMap);
		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);
		// 发送战斗结果给前台播放动画
		leaderMarch.sendBattleResultInfo(isAtkWin, atkArmyLeft, defArmyList);

		for (IXQHXWorldMarch iWorldMarch : atkMarchList) {
			iWorldMarch.notifyMarchEvent(MarchEvent.MARCH_DELETE);
		}

		leaderMarch.updateDefMarchAfterWar(atkMarchList, atkArmyLeftMap);
		leaderMarch.updateDefMarchAfterWar(defMarchList, defArmyLeftMap);

		List<IXQHXWorldMarch> winMarches = null;
		List<IXQHXWorldMarch> losMarches = null;

		if (isAtkWin) {
			state = XQHXBuildState.ZHONG_LI;
			this.leaderMarch = leaderMarch;
			winMarches = atkMarchList;
			losMarches = defMarchList;
		} else {
			winMarches = defMarchList;
			losMarches = atkMarchList;
		}

		for (IXQHXWorldMarch atkM : winMarches) {
			atkM.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
			atkM.updateMarch();
		}
		for (IXQHXWorldMarch defM : losMarches) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}

		FightMailService.getInstance().sendFightMail(this.getPointType().getNumber(), battleIncome, battleOutcome, null);

		defMailList.add(0, battleOutcome.getDefMail(defPlayers.get(0).getId()));
	}

	private boolean assitenceWarPoint(List<IXQHXWorldMarch> atkMarchList, List<IXQHXWorldMarch> stayMarchList) {
		// 队长
		IXQHXPlayer leader = leaderMarch.getParent();

		// 8.21新增：检查当前自己的行军，如果行军中已有英雄，则将自己队列中的英雄撤回
		for (IXQHXWorldMarch worldMarch : atkMarchList) {
			if (worldMarch.getMarchEntity().getHeroIdList().isEmpty()) {// 如果当前行军中没有英雄直接跳过
				continue;
			}
			// 如果有英雄，则判断当前据点停留的行军有没有自己的
			for (IXQHXWorldMarch stayMarch : stayMarchList) {
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

		// // 先处理过来行军中的队长行军，队长行军处理完毕后删除
		// if (stayMarchList != null && !stayMarchList.isEmpty()) {
		// int count = stayMarchList.size();// 加上队长
		// if (count > leader.getMaxMassJoinMarchNum() + leaderMarch.getMarchEntity().getBuyItemTimes()) {
		// return returnMarchList(atkMarchList);
		// }
		// }

		int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);

		List<WorldMarch> ppList = new ArrayList<>();
		for (IXQHXWorldMarch worldMarch : stayMarchList) {
			ppList.add(worldMarch.getMarchEntity());
		}

		int curPopulationCnt = WorldUtil.calcMarchsSoldierCnt(ppList); // 已驻扎士兵人口
		// 剩余人口<0部队返回
		int remainArmyPopu = maxMassSoldierNum - curPopulationCnt;
		if (remainArmyPopu <= 0) {
			return returnMarchList(atkMarchList);
		}

		// 优先加入已在玩家
		for (IXQHXWorldMarch stayMarch : stayMarchList) {
			Iterator<IXQHXWorldMarch> it = atkMarchList.iterator();
			while (it.hasNext()) {
				IXQHXWorldMarch massMarch = it.next();
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
					IXQHXWorldMarch back = getParent().startMarch(massMarch.getParent(), this, massMarch.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
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
		Iterator<IXQHXWorldMarch> it = atkMarchList.iterator();

		while (it.hasNext()) {
			IXQHXWorldMarch march = it.next();
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
				IXQHXWorldMarch back = getParent().startMarch(march.getParent(), this, march.getParent(), WorldMarchType.ASSISTANCE, "", 0, effParams);
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

	private boolean returnMarchList(List<IXQHXWorldMarch> atkMarchList) {
		for (IXQHXWorldMarch defM : atkMarchList) {
			defM.onMarchReturn(this.getPointId(), defM.getParent().getPointId(), defM.getArmys());
		}
		return true;
	}

	/** 遣返 */
	public boolean repatriateMarch(IXQHXPlayer comdPlayer, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!Objects.equals(getGuildId(), comdPlayer.getGuildId())) {
			return false;
		}

		// 队长
		IXQHXPlayer leader = leaderMarch.getParent();

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		List<IXQHXWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXQHXWorldMarch iWorldMarch : stayMarches) {
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
	public boolean cheangeQuarterLeader(IXQHXPlayer comdPlayer, String targetPlayerId) {

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

		List<IXQHXWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IXQHXWorldMarch march : stayMarches) {
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			leaderMarch = march;
			getParent().worldPointUpdate(this);
			break;
		}
		return true;
	}

	public void syncQuarterInfo(IXQHXPlayer player) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.isAnchor() && !Objects.equals(player.getGuildId(), getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}

		List<IXQHXWorldMarch> defMarchList = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		IXQHXWorldMarch leaderMarch = getLeaderMarch();
		if (leaderMarch != null) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(leaderMarch));
			builder.setMassSoldierNum(leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getParent()));
		}
		for (IXQHXWorldMarch march : defMarchList) {
			if (march != leaderMarch) {
				builder.addQuarterMarch(getSuperWeaponQuarterMarch(march));
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}

	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(IXQHXWorldMarch march) {
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

	public XQHXBuildState getState() {
		return state;
	}

	public void setState(XQHXBuildState state) {
		this.state = state;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public XQHXBattleRoom getParent() {
		return parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public XQHX_CAMP getGuildCamp() {
		if (getLeaderMarch() == null) {
			return null;
		}
		return leaderMarch.getParent().getCamp();
	}

	@Override
	public String getGuildId() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getGuildId();
	}

	public String getGuildTag() {
		if (getLeaderMarch() == null) {
			return "";
		}
		return leaderMarch.getParent().getGuildTag();
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

	public long getZhanLingKaiShi() {
		return zhanLingKaiShi;
	}

	public void setZhanLingKaiShi(long zhanLingKaiShi) {
		this.zhanLingKaiShi = zhanLingKaiShi;
	}

	public long getZhanLingJieShu() {
		return zhanLingJieShu;
	}

	public void setZhanLingJieShu(long zhanLingJieShu) {
		this.zhanLingJieShu = zhanLingJieShu;
	}

	public long getLastTick() {
		return lastTick;
	}

	public void setLastTick(long lastTick) {
		this.lastTick = lastTick;
	}

	public List<XQHXHoldRec> getHoldRecList() {
		return holdRecList;
	}

	public void setHoldRecList(List<XQHXHoldRec> holdRecList) {
		this.holdRecList = holdRecList;
	}

	public int getGuildFlag() {
		if (getLeaderMarch() == null) {
			return 0;
		}
		return leaderMarch.getParent().getGuildFlag();
	}

	public boolean isFirstControl() {
		return firstControl;
	}

	public void setFirstControl(boolean firstControl) {
		this.firstControl = firstControl;
	}

	public AtomicLongMap<String> getControlGuildTimeMap() {
		return controlGuildTimeMap;
	}

	public void setControlGuildTimeMap(AtomicLongMap<String> controlGuildTimeMap) {
		this.controlGuildTimeMap = controlGuildTimeMap;
	}

	public Map<String, Double> getControlGuildHonorMap() {
		return controlGuildHonorMap;
	}

	public IXQHXWorldMarch getLeaderMarch() {
		if (leaderMarch == null || leaderMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			List<IXQHXWorldMarch> stayMarches = getParent().getPointMarches(getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (stayMarches.isEmpty()) {
				leaderMarch = null;
				return null;
			}

			leaderMarch = stayMarches.get(0);
		}
		return leaderMarch;
	}

	public void setLeaderMarch(IXQHXWorldMarch leaderMarch) {
		this.leaderMarch = leaderMarch;
	}

	public List<String> getSpyMailList() {
		return spyMailList;
	}

	public void setSpyMailList(List<String> spyMailList) {
		this.spyMailList = spyMailList;
	}

	public XQHX_CAMP getOrder10001DebuffCamp() {
		return order10001DebuffCamp;
	}

	public void setOrder10001DebuffCamp(XQHX_CAMP order10001DebuffCamp) {
		this.order10001DebuffCamp = order10001DebuffCamp;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getBuildTypeId() {
		return buildTypeId;
	}

	public void setBuildTypeId(int buildTypeId) {
		this.buildTypeId = buildTypeId;
	}

	public List<String> getDefMailList() {
		return defMailList;
	}

	public void setDefMailList(List<String> defMailList) {
		this.defMailList = defMailList;
	}

	public void setControlGuildHonorMap(Map<String, Double> controlGuildHonorMap) {
		this.controlGuildHonorMap = controlGuildHonorMap;
	}

}
