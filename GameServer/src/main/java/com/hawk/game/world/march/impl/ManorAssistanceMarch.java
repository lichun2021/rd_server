package com.hawk.game.world.march.impl;

import java.util.Set;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.AssistanceMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.ManorMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 联盟领地单人援助
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class ManorAssistanceMarch extends PlayerMarch implements AssistanceMarch, ManorMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public ManorAssistanceMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MANOR_ASSISTANCE;
	}

	@Override
	public Set<String> attackReportRecipients() {
		return ReportRecipients.TargetManor.attackReportRecipients(this);
	}

	@Override
	public void onMarchReach(Player player) {
		ManorMarch.super.onMarchReach(player);
		// 删除行军报告
		this.removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
		this.pullAttackReport();
	}

	@Override
	public void pullAttackReport() {
		for (IWorldMarch targetMarch : alarmPointMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport();
			}
		}
	}
	
	@Override
	public void pullAttackReport(String playerId) {
		for (IWorldMarch targetMarch : alarmPointMarches()) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).pushAttackReport(playerId);
			}
		}
	}
	
	/**
	 * 是否需要在联盟战争界面显示
	 */
	@Override
	public boolean needShowInGuildWar() {
		if (this.isReturnBackMarch() || this.isManorMarchReachStatus()) {
			return false;
		}
		String manorLeaderId = GuildManorService.getInstance().getManorLeaderId(this.getMarchEntity().getTerminalId());
		if (HawkOSOperator.isEmptyString(manorLeaderId)) {
			return false;
		}
		return !GuildService.getInstance().isInTheSameGuild(this.getPlayerId(), manorLeaderId);
	}
	
	/**
	 * 获取被动方联盟战争界面信息
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		builder.setPointType(WorldPointType.GUILD_TERRITORY);
		builder.setX(this.getMarchEntity().getTerminalX());
		builder.setY(this.getMarchEntity().getTerminalY());
		// 建筑id
		String manorId = this.getMarchEntity().getTargetId();
		// 建筑
		int manorPostion = GuildManorService.getInstance().getManorPostion(manorId);

		// 建筑世界点
		WorldPoint buildPoint = WorldPointService.getInstance().getWorldPoint(manorPostion);
		if (buildPoint == null) {
			return builder;
		}

		GuildManorObj manor = GuildManorService.getInstance().getGuildManorByPoint(buildPoint);
		builder.setGuildBuildName(manor.getEntity().getManorName());
		return builder;
	}
}
