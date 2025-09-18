package com.hawk.game.world.march.impl;

import java.util.Set;

import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.AssistanceMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.ManorMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 联盟领地集结援助
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class ManorAssistanceMassMarch extends PlayerMarch implements MassMarch, AssistanceMarch, ManorMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public ManorAssistanceMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MANOR_ASSISTANCE_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return !this.isManorMarchReachStatus() && !this.isReturnBackMarch();
	}
	
	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}
	
	@Override
	public void onMarchReach(Player player) {
		ManorMarch.super.onMarchReach(player);
		this.removeAttackReport();
		this.pullAttackReport();
	}
	
	@Override
	public Set<String> attackReportRecipients() {
		return ReportRecipients.TargetManor.attackReportRecipients(this);
	}
	
	@Override
	public void teamMarchReached(MassJoinMarch teamMarch) {
		MassMarch.super.teamMarchReached(teamMarch);
		this.pushAttackReport();
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
	public boolean isEvident() {
		return IReportPushMarch.super.isEvident() || this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
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
}
