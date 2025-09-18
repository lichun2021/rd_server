package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AssistantEvent;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.GuildAssistant.AssistanceCallbackNotifyPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.guildtask.event.GuildAssistTaskEvent;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.AssistanceMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 援助行军
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class AssistanceSingleMarch extends PassiveMarch implements AssistanceMarch, IPassiveAlarmTriggerMarch, IReportPushMarch {

	public AssistanceSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ASSISTANCE;
	}

	@Override
	public void onMarchReach(Player player) {
		removeAttackReport();
		pullAttackReport();
		WorldMarch march = getMarchEntity();
		// 目标点
		WorldPoint tarPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (tarPoint == null) {
			WorldMarchService.getInstance().onMarchReturn(this, march.getArmys(), 0);
			return;
		}
		final Player tarPlayer = GlobalData.getInstance().makesurePlayer(march.getTargetId());

		if (tarPlayer == null || tarPoint == null || !tarPoint.getPlayerId().equals(march.getTargetId())
				|| tarPoint.getPointType() != WorldPointType.PLAYER_VALUE) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			if (tarPlayer == null) {
				return;
			}
			// 发邮件---士兵援助失败（受助者高迁或被打飞）
			int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
			Object[] subTitle = new Object[] { tarPlayer.getName() };

			MailParames.Builder mailParames = MailParames.newBuilder().setPlayerId(march.getPlayerId())
					.setMailId(MailId.SOILDER_ASSISTANCE_FAILED_TARGET_CHANGED).addSubTitles(subTitle)
					.addContents(tarPlayer.getName()).setIcon(icon);
			GuildMailService.getInstance().sendMail(mailParames.build());
			return;
		}

		// 检查玩家是否在同一联盟
		if (!GuildService.getInstance().isPlayerInGuild(tarPlayer.getGuildId(), player.getId())) {
			WorldMarchService.logger.error("world assistance march not in same guild");
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}
		
		// 检查集结士兵数是否超上限
		int maxPopulationCnt = tarPlayer.getMaxAssistSoldier();
		int curPopulationCnt = 0;// 已援助士兵数目
		Set<IWorldMarch> helpMarchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(tarPlayer.getId(),
				WorldMarchType.ASSISTANCE_VALUE, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);

		// 计算已援助士兵数量
		if (helpMarchList != null && helpMarchList.size() > 0) {
			for (IWorldMarch helpMarch : helpMarchList) {
				curPopulationCnt += WorldUtil.calcSoldierCnt(helpMarch.getMarchEntity().getArmys());
			}
		}
		// 士兵数已达上限
		if (curPopulationCnt >= maxPopulationCnt) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}
		// 计算剩余兵力空位
		int remainSpace = maxPopulationCnt - curPopulationCnt;

		List<ArmyInfo> stayList = new ArrayList<ArmyInfo>();
		List<ArmyInfo> backList = new ArrayList<ArmyInfo>();
		WorldUtil.calcStayArmy(march, remainSpace, stayList, backList);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_ASSISTANT,
				Params.valueOf("marchData", march), Params.valueOf("remainSpace", remainSpace),
				Params.valueOf("stayList", stayList), Params.valueOf("backList", backList));

		
		// 回家的士兵生成新行军
		if (backList.size() > 0) {
			WorldMarchService.getInstance().onNewMarchReturn(player, this, backList, new ArrayList<Integer>(),0, null);
		}

		// 留下援助
		this.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE, stayList, tarPoint);

		// 获得部队援助消息推送
		PushService.getInstance().pushMsg(tarPlayer.getId(), PushMsgType.ALLIANCE_ASSISTANCE_ARMY_VALUE);

		int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
		List<PlayerHero> hero = player.getHeroByCfgId(march.getHeroIdList());
		// 发邮件---援助者士兵援助成功
		GuildMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
				.setMailId(MailId.SOILDER_ASSISTANCE_SUCC_TO_FROM).addSubTitles(tarPlayer.getName())
				.addContents(MailBuilderUtil.createSoilderAssistanceMail(march, hero, tarPlayer)).setIcon(icon).build());
		// 发邮件---被援助者士兵援助成功
		GuildMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getTargetId())
				.setMailId(MailId.SOILDER_ASSISTANCE_SUCC_TO_TARGET).addSubTitles(player.getName())
				.addContents(MailBuilderUtil.createSoilderAssistanceMail(march, hero, player)).setIcon(icon).build());
		int soldierCnt = WorldUtil.calcSoldierCnt(stayList);
		ActivityManager.getInstance().postEvent(new AssistantEvent(player.getId(), soldierCnt));
		
		// 联盟任务-部队援助
		GuildService.getInstance().postGuildTaskMsg(new GuildAssistTaskEvent(player.getGuildId()));
		
		// 士兵援助行军变化通知
		WorldMarchService.getInstance().notifyAssistanceMarchChange(tarPlayer, march.getMarchId());
	}

	@Override
	public void onWorldMarchReturn(Player player) {
		// 推送消息
		PushService.getInstance().pushMsg(player.getId(), PushMsgType.ASSISTANCE_ARMY_RETURN_VALUE);
	}
	
	@Override
	public void onMarchStart() {
		pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		pullAttackReport();
		removeAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		pullAttackReport();
		removeAttackReport();
	}
	
	
	
	@Override
	public void pullAttackReport() {
		BlockingQueue<IWorldMarch> helpMarchList = WorldMarchService.getInstance().getPlayerPassiveMarch(this.getMarchEntity().getTargetId());
		helpMarchList.stream()
		.filter(m -> m instanceof IReportPushMarch )
		.map(m -> (IReportPushMarch)m)
		.forEach(m -> m.pushAttackReport());
	}
	
	@Override
	public void pullAttackReport(String playerId) {
		BlockingQueue<IWorldMarch> helpMarchList = WorldMarchService.getInstance().getPlayerPassiveMarch(this.getMarchEntity().getTargetId());
		helpMarchList.stream()
		.filter(m -> m instanceof IReportPushMarch )
		.map(m -> (IReportPushMarch)m)
		.forEach(m -> m.pushAttackReport(playerId));
	}

	@Override
	public Set<String> attackReportRecipients() {

		Set<String> allToNotify = new HashSet<>();
		allToNotify.add(this.getMarchEntity().getTargetId());
		return allToNotify;
	}
	
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		AssistanceCallbackNotifyPB.Builder callbackNotifyPB = AssistanceCallbackNotifyPB.newBuilder();
		callbackNotifyPB.setMarchId(getMarchId());
		Player assistPlayer = GlobalData.getInstance().makesurePlayer(getMarchEntity().getTargetId());
		assistPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.ASSISTANCE_MARCH_CALLBACK, callbackNotifyPB));
		
		WorldMarchService.getInstance().onMarchReturn(this, callbackTime, getMarchEntity().getAwardItems(), getMarchEntity().getArmys(), 0, 0);
	}
	
	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		if(getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE){
			WorldMarchService.getInstance().assistanceMarchPlayerMoveCityProcess(targetPlayer, this, currentTime);
		} else if(getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE){
			WorldMarchService.getInstance().onMarchReturnImmediately(this, this.getMarchEntity().getArmys());
		}
		
		// 士兵援助行军变化通知
		WorldMarchService.getInstance().notifyAssistanceMarchChange(targetPlayer, getMarchId());		
	}
	
	public void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().accountMarchBeforeRemove(this);
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(getTargetId());
		if (targetPlayer != null) {
			WorldMarchService.getInstance().notifyAssistanceMarchChange(targetPlayer, getMarchId());
		}
	}
}
