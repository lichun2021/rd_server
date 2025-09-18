package com.hawk.game.world.march.impl;

import java.util.HashSet;
import java.util.Set;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.AssistanceResMarchReachMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 资源援助盟友
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class AssistanceResMarch extends PassiveMarch implements BasedMarch,IReportPushMarch {

	public AssistanceResMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ASSISTANCE_RES;
	}

	@Override
	public void onMarchReach(Player player) {
		removeAttackReport();
		WorldMarch march = getMarchEntity();
		WorldPoint tarPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		final Player tarPlayer = GlobalData.getInstance().makesurePlayer(march.getTargetId());

		if (tarPlayer == null || tarPoint == null || !tarPoint.getPlayerId().equals(march.getTargetId())
				|| tarPoint.getPointType() != WorldPointType.PLAYER_VALUE) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			if (tarPlayer != null) {
				// 发邮件---资源援助失败（受助者高迁或被打飞）
				int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
				GuildMailService.getInstance().sendMail(MailParames
						.newBuilder()
						.setPlayerId(march.getPlayerId())
						.setMailId(MailId.RES_ASSISTANCE_FAILED_TARGET_CHANGED)
						.addSubTitles(tarPlayer.getName())
						.addContents(tarPlayer.getName())
						.setIcon(icon)
						.build());
			}
			return;
		}

		// 检查玩家是否在同一联盟
		if (!GuildService.getInstance().isPlayerInGuild(player.getGuildId(), tarPlayer.getId())) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}

		// 玩家援助次数达到上限
		int playerAssResTimes = WorldMarchService.getInstance().getPlayerAssistanceResTimes(player);
		if (playerAssResTimes >= WorldMarchConstProperty.getInstance().getAssistanceResTimes()) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}
		
		// 玩家被援助次数达到上限
		int playerBeAssResTimes = WorldMarchService.getInstance().getPlayerBeAssistanceResTimes(tarPlayer);
		if (playerBeAssResTimes >= WorldMarchConstProperty.getInstance().getBeAssistanceResTimes()) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}
		
		int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
		int taxRate = player.getData().getTradeTaxRate();
		// 发邮件---援助者资源援助成功
		GuildMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
				.setMailId(MailId.RES_ASSISTANCE_SUCC_TO_FROM).addSubTitles(tarPlayer.getName())
				.addContents(MailBuilderUtil.createResAssistanceMail(march, tarPlayer, taxRate)).setIcon(icon).build());
		// 发邮件---被援助者资源援助成功
		GuildMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getTargetId())
				.setMailId(MailId.RES_ASSISTANCE_SUCC_TO_TARGET).addSubTitles(player.getName())
				.addContents(MailBuilderUtil.createResAssistanceMail(march, player, taxRate)).setIcon(icon).build());

		// 推送收到资源援助
		PushService.getInstance().pushMsg(tarPlayer.getId(), PushMsgType.ALLIANCE_ASSISTANCE_RES_VALUE);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_ASSISTANT_RES,
				Params.valueOf("marchData", march), Params.valueOf("targetPlayerId", tarPlayer.getId()),
				Params.valueOf("targetPlayerName", tarPlayer.getName()),
				Params.valueOf("taxRate", taxRate));
		// 消息调用
		tarPlayer.dealMsg(MsgId.ASSISTANCE_RES_MARCH_REACH, new AssistanceResMarchReachMsgInvoker(player, tarPlayer, 
				march.getAssistantStr(), taxRate));

		// 行军返回
		WorldMarchService.getInstance().clearAssistantResource(march);
		WorldMarchService.getInstance().onMarchReturn(this, march.getArmys(), 0);
		
		WorldMarchService.getInstance().updatePlayerAssistanceResTimes(player, playerAssResTimes + 1);
		WorldMarchService.getInstance().updatePlayerBeAssistanceResTimes(tarPlayer, playerBeAssResTimes + 1);
	}
	
	@Override
	public void onWorldMarchReturn(Player player) {
		WorldMarch march = getMarchEntity();
		String awardStr = march.getAssistantStr();
		if (!HawkOSOperator.isEmptyString(awardStr)) {
			AwardItems award = AwardItems.valueOf(awardStr);
			award.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_ASSISTANT_RES_RETURN);
		}

		// 行为日志
		boolean isCallBack = false;
		if (march.getCallBackTime() > 0) {
			isCallBack = true;
		}
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_ASSISTANT_RES_RETURN,
				Params.valueOf("march", march), Params.valueOf("awardStr", awardStr),
				Params.valueOf("isCallBack", isCallBack));

	}
	
	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		removeAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		removeAttackReport();
	}
	
	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		allToNotify.add(this.getMarchEntity().getTargetId());
		return allToNotify;
	}
	
	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		if(getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE){
			WorldMarchService.getInstance().assistanceMarchPlayerMoveCityProcess(targetPlayer, this, currentTime);
		}
	}
}
