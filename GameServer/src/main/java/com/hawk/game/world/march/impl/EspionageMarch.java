package com.hawk.game.world.march.impl;

import java.util.Set;

import org.hawk.os.HawkTime;

import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.MarchVitReturnBackMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 间谍行军
 * @author golden
 *
 */
public class EspionageMarch extends PassiveMarch implements BasedMarch , IReportPushMarch {

	public EspionageMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}
	
	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ESPIONAGE_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {
		// 删除行军报告
		removeAttackReport();
		// 目标点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(getTerminalId());
		
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		
		// 目标点改变
		if (point == null || point.getPointType() != WorldPointType.PLAYER_VALUE) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.SNOWBALL_MAIL_12)
					.build());
			returnCost(player);
			return;
		}
		
		// 目标玩家发生改变
		String targetId = getMarchEntity().getTargetId();
		if (!targetId.equals(point.getPlayerId())) {
			returnCost(player);
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.SNOWBALL_MAIL_12)
					.build());
			return;
		}
		
		// 判断对方保护罩状态
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
		boolean intShield = targetPlayer.getData().getCityShieldTime() > HawkTime.getMillisecond();
		if (!intShield && HawkTime.getMillisecond() > point.getShowProtectedEndTime()) {
			returnCost(player);
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.SNOWBALL_MAIL_13)
					.build());
			return;
		}
		
		// 保护罩结束时间
		long protectEndTime = Math.max(targetPlayer.getData().getCityShieldTime(), point.getShowProtectedEndTime());
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.SNOWBALL_MAIL_10)
				.addContents(protectEndTime)
				.addContents(point.getX())
				.addContents(point.getY())
				.addContents(targetPlayer.getName())
				.build());
		
		int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		int[] pos = GameUtil.splitXAndY(playerPos);
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(targetPlayer.getId())
				.setMailId(MailId.SNOWBALL_MAIL_11)
				.addContents(player.getName())
				.addContents(pos[0])
				.addContents(pos[1])
				.build());
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
	
	/**
	 * 返回消耗
	 */
	private void returnCost(Player player) {
		player.dealMsg(MsgId.RETURN_VIT, new MarchVitReturnBackMsgInvoker(player, this));
	}
	
	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		// 删除行军报告
		removeAttackReport();
	}
}
