package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.service.ActivityService;
import com.hawk.gamelib.GameConst;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.invoker.CityOnFireMsgInvoker;
import com.hawk.game.invoker.PlayerArmyBackMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.GuildWar.GuildWarMarchType;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.DefenderIdentity;
import com.hawk.log.Source;

/**
 * 集结攻打单人基地
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class MassSingleMarch extends PassiveMarch implements MassMarch,IReportPushMarch {

	public MassSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.MASS_JOIN;
	}

	@Override
	public void onMarchReach(Player atkLeader) {
		this.removeAttackReport();
		// 行军
		WorldMarch atkLeaderMarch = getMarchEntity();
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(atkLeaderMarch.getTerminalId());
		// 防守方玩家
		Player defLeader = GlobalData.getInstance().makesurePlayer(atkLeaderMarch.getTargetId());

		// 战斗前检查条件
		if (!checkPVPBeforeWar(targetPoint, defLeader, atkLeader)) {
			// 队员行军返回
			Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
			for (IWorldMarch massJoinMarch : massJoinMarchs) {
				WorldMarchService.getInstance().onMarchReturn(massJoinMarch, massJoinMarch.getMarchEntity().getArmys(), atkLeaderMarch.getTerminalId());
			}
			// 队长行军返回
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			return;
		}

		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(atkLeader);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);

		// 填充参与集结信息
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(GlobalData.getInstance().makesurePlayer(massJoinMarch.getPlayerId()));
			atkMarchs.add(massJoinMarch);
		}

		// 防御方玩家
		List<Player> defPlayers = new ArrayList<Player>();
		defPlayers.add(defLeader);

		// 防御方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		Set<IWorldMarch> helpMarchList = getDefMarch4War(defLeader, defPlayers);
		for (IWorldMarch iWorldMarch : helpMarchList) {
			defMarchs.add(iWorldMarch);
		}

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_CITY, targetPoint.getId(), atkPlayers, defPlayers, atkMarchs, defMarchs);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);

		// 战斗胜利
		boolean isAtkWin = battleOutcome.isAtkWin();

		/**********************    战斗数据组装及战斗***************************/

		// 攻击方剩余兵力
		Map<String, List<ArmyInfo>> atkArmyLeftMap = battleOutcome.getAftArmyMapAtk();
		List<ArmyInfo> atkArmyList = WorldUtil.mergAllPlayerArmy(atkArmyLeftMap);

		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();
		List<ArmyInfo> defArmyList = WorldUtil.mergAllPlayerArmy(defArmyLeftMap);

		// 攻方玩家负重列表
		int[] weightAry = new int[atkPlayers.size()];
		List<String> atkPlayerIds = new ArrayList<>();
		for (int i = 0; i < atkMarchs.size(); i++) {
			IWorldMarch atkMarch = atkMarchs.get(i);
			Player atkPlayer = atkMarch.getPlayer();
			List<ArmyInfo> leftList = atkArmyLeftMap.get(atkPlayer.getId());
			weightAry[i] = isAtkWin ? WorldUtil.calcTotalWeight(atkPlayer, leftList, atkMarch.getMarchEntity().getEffectParams()) : 0;
			StatisticsEntity statisticsEntity = atkPlayer.getData().getStatisticsEntity();
			statisticsEntity.addCommonStatisData(StatisticDataType.GROUP_PVP_TOTAL_TODAY, 1);
			statisticsEntity.addCommonStatisData(StatisticDataType.GROUP_TOTAL_TODAY, 1);
			atkPlayerIds.add(atkPlayer.getId());
		}
		try {
			ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(atkPlayerIds));
		}catch (Exception e){
			HawkException.catchException(e);
		}

		// 播放战斗动画
		WorldMarchService.getInstance().sendBattleResultInfo(this, isAtkWin, atkArmyList, defArmyList, isAtkWin);

		// 保存联盟战争信息
		LocalRedis.getInstance().saveGuildBattleInfo(atkPlayers, defPlayers, isAtkWin, GuildWarMarchType.MASS_TYPE, helpMarchList.isEmpty() ? GuildWarMarchType.SINGLE_TYPE : GuildWarMarchType.MASS_TYPE);
		GuildService.getInstance().pushNewGuildWarRecordCount(atkLeader.getGuildId(), defLeader.getGuildId());
		
		int[] resType = GsConst.RES_TYPE;

		// 资源掠夺
		long[] hasResAry = defLeader.getPlunderResAry(resType);
		Map<String, long[]> grabResAry = BattleService.getInstance().calcGrabRes(atkPlayers, weightAry, hasResAry);
		int[] lostAry = new int[resType.length];

		// 双方资源结算
		Map<String, AwardItems> awardMap = new HashMap<String, AwardItems>();
		for (int p = 0; p < atkPlayers.size(); p++) {
			AwardItems awardItems = AwardItems.valueOf();
			String pid = atkPlayers.get(p).getId();
			awardMap.put(pid, awardItems);
			long[] js = grabResAry.get(pid);
			for (int i = 0; i < resType.length; i++) {
				int count = (int) js[i];
				lostAry[i] += count;
				if (count > 0) {
					awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, resType[i], count);
				}
			}
		}

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		for (int i = 0; i < resType.length; i++) {
			int lostCnt = lostAry[i];
			if (lostCnt > 0) {
				consumeItems.addConsumeInfo(PlayerAttr.valueOf(resType[i]), lostAry[i]);
			}
		}

		// 记录战斗安全日志
		LogUtil.logSecBattleFlow(atkLeader, defLeader, "", DefenderIdentity.CITY, isAtkWin, awardMap.get(atkLeader.getId()), consumeItems, atkArmyList, defArmyList, 0, atkLeaderMarch, true);

		// 处理任务、统计等
		sendMsgUpdateAtkPlayerListAfterWar(isAtkWin, atkPlayers, atkArmyLeftMap, defPlayers, atkLeader, battleOutcome);
		sendMsgUpdateDefPlayerListAfterWar(defPlayers, battleOutcome, consumeItems);

		// 防御者战后部队结算
		defLeader.dealMsg(MsgId.ARMY_BACK, new PlayerArmyBackMsgInvoker(targetPoint.getPointType(), defLeader, battleOutcome, battleIncome, grabResAry, true, isMassMarch(), this.getMarchType()));
		// 攻方行军返回
		long reachTime = atkLeaderMarch.getReachTime();
		// 队长行军返回
		WorldMarchService.getInstance().onMarchReturn(this, reachTime, awardMap.get(atkLeaderMarch.getPlayerId()), atkArmyLeftMap.get(atkLeaderMarch.getPlayerId()), 0, 0);
		// 队员行军返回
		for (IWorldMarch tmpMarch : massJoinMarchs) {
			String playerId = tmpMarch.getPlayerId();
			WorldMarchService.getInstance().onMarchReturn(tmpMarch, reachTime, awardMap.get(playerId), atkArmyLeftMap.get(playerId), 0, 0);
		}

		// 更新援助防御玩家行军的部队
		List<IWorldMarch> helpMarchs = new ArrayList<IWorldMarch>();
		for (IWorldMarch tempMarch : helpMarchList) {
			helpMarchs.add(tempMarch);
		}
		updateDefMarchAfterWar(helpMarchs, defArmyLeftMap);

		// 防守方失败了 , 城墙着火
		if (isAtkWin) {
			// 投递回玩家线程执行
			defLeader.dealMsg(MsgId.CITY_ON_FIRE, new CityOnFireMsgInvoker(atkLeader, defLeader, atkLeaderMarch.getHeroIdList()));
		}

		// 行为日志
		BehaviorLogger.log4Service(atkLeader, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_COLLECT,
				Params.valueOf("marchData", atkLeaderMarch),
				Params.valueOf("isAtkWin", isAtkWin),
				Params.valueOf("atkLeftArmyList", atkArmyList),
				Params.valueOf("defLeftArmyList", defArmyList));
		
		// 刷新战力
		refreshPowerAfterWar(atkPlayers, defPlayers);
	}

	@Override
	public void onWorldMarchReturn(Player player) {
		WorldMarch march = getMarchEntity();
		
		String awardStr = march.getAwardStr();
		if (!HawkOSOperator.isEmptyString(awardStr)) {
			AwardItems award = AwardItems.valueOf(awardStr);
			award.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_MASS_RETURN);
		}
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_MASS_RETURN, Params.valueOf("march", march), Params.valueOf("awardStr", awardStr));
	}

	/**
	 * 攻打玩家基地前检查
	 * 
	 * @param march
	 * @param tarPoint
	 * @param defPlayer
	 * 
	 * @return
	 */
	@Override
	public boolean checkPVPBeforeWar(WorldPoint tarPoint, Player defPlayer, Player atkPlayer) {

		// 行军信息
		WorldMarch march = getMarchEntity();
		int x = march.getTerminalX();
		int y = march.getTerminalY();
		String targetId = march.getTargetId();

		// 攻击方部队
		Set<IWorldMarch> atkMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		atkMarchs.add(this);

		// 攻击方icon
		int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());

		// 找不到目标: 发送邮件 -> 攻击目标不存在
		if (defPlayer == null || tarPoint == null) {

			for (IWorldMarch atkMarch : atkMarchs) {
				GuildMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(atkMarch.getPlayerId())
						.setMailId(MailId.MASS_FAILED_TARGET_DISAPPEAR)
						.addSubTitles(x, y)
						.addContents(x, y)
						.setIcon(icon)
						.build());
			}

			WorldMarchService.logger.info("mass single march check, point null, x:{}, y{}, targetId:{}", x, y, targetId);
			return false;
		}

		// 目标非玩家 || 目标已换人 || 或者目标和自己同盟 : 发送邮件 -> 攻击目标状态改变
		if (!WorldUtil.isPlayerPoint(tarPoint)
				|| !tarPoint.getPlayerId().equals(targetId)
				|| GuildService.getInstance().isInTheSameGuild(atkPlayer.getId(), defPlayer.getId())) {

			for (IWorldMarch atkMarch : atkMarchs) {
				GuildMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(atkMarch.getPlayerId())
						.setMailId(MailId.MASS_FAILED_TARGET_CHANGED)
						.addSubTitles(x, y)
						.addContents(x, y)
						.setIcon(icon)
						.build());
			}

			WorldMarchService.logger.info("mass single march check, point change, x:{}, y{}, targetId:{}", x, y, targetId);
			return false;
		}

		// 目标开启保护罩：发送邮件 -> 攻击目标开保护
		if (HawkTime.getMillisecond() <= tarPoint.getShowProtectedEndTime()) {

			for (IWorldMarch atkMarch : atkMarchs) {
				GuildMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(atkMarch.getPlayerId())
						.setMailId(MailId.MASS_FAILED_TARGET_PROTECTED)
						.addSubTitles(x, y)
						.addContents(x, y)
						.setIcon(icon)
						.build());
			}

			WorldMarchService.logger.info("mass single march check, point protected, x:{}, y{}, targetId:{}", x, y, targetId);
			return false;
		}

		return true;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
	
	
	
	@Override
	public void teamMarchReached(MassJoinMarch teamMarch) {
		// TODO Auto-generated method stub
		MassMarch.super.teamMarchReached(teamMarch);
		this.pushAttackReport();
	}

	@Override
	public void teamMarchCallBack(MassJoinMarch teamMarch) {
		// TODO Auto-generated method stub
		MassMarch.super.teamMarchCallBack(teamMarch);
		this.pushAttackReport();
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public Set<String> attackReportRecipients() {
		return ReportRecipients.TargetAndHisAssistance.attackReportRecipients(this);
	}
	
	@Override
	public boolean isEvident() {
		return IReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长id
		String leaderId = this.getMarchEntity().getTargetId();
		// 队长
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		// 队长位置
		int[] pos = leader.getPosXY();
		
		builder.setPointType(WorldPointType.PLAYER);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setLeaderArmyLimit(leader.getMaxAssistSoldier());
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		if (!HawkOSOperator.isEmptyString(leader.getGuildId())) {
			String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());
			builder.setGuildTag(guildTag);
		}

		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(leader.getId());
		leaderInfo.setPlayerName(leader.getName());
		leaderInfo.setIconId(leader.getIcon());
		leaderInfo.setPfIcon(leader.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING);
		builder.setLeaderMarch(leaderInfo);
		
		// 已经到达的士兵数量
		int reachArmyCount = 0;
		Set<IWorldMarch> assistandMarchs = WorldMarchService.getInstance().getPlayerPassiveMarchs(leaderId, WorldMarchType.ASSISTANCE_VALUE);
		for (IWorldMarch assistandMarch : assistandMarchs) {
			if (assistandMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			builder.addJoinMarchs(getGuildWarSingleInfo(assistandMarch.getMarchEntity()));
			reachArmyCount += WorldUtil.calcSoldierCnt(assistandMarch.getMarchEntity().getArmys());
		}
		builder.setCityLevel(leader.getCityLv());
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}
	
}
