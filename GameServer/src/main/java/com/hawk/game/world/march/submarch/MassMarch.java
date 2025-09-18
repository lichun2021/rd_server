package com.hawk.game.world.march.submarch;

import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildtask.event.MemberMasstCountEvent;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.YuriMailService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 集结行军
 * 
 * @author zhenyu.shang
 * @since 2017年8月26日
 */
public interface MassMarch extends BasedMarch {

	default boolean isMassMarch() {
		return true;
	}

	@Override
	default boolean marchHeartBeats(long time) {
		// 集结等待中
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			waitingStatusMarchProcess();
			return true;
		}
		return false;
	}

	/**
	 * 获取加入类型行军
	 * 
	 * @return
	 */
	public WorldMarchType getJoinMassType();

	default void waitingStatusMarchProcess() {
		if (!isNeedCalcTickMarch()) {
			return;
		}
		// 当前时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		// 集结等待中
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			// 集结者还未出发
			if (getMarchEntity().getStartTime() > currTime) {
				return;
			}
			// 没有加入集结的行军到达，集结解散, 未到达的行军返回
			Set<IWorldMarch> reachJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
			if (reachJoinMarchs.size() <= 0) {
				// 行军返还
				WorldMarchService.getInstance().onMarchReturnImmediately(this, getMarchEntity().getArmys());
				// 发邮件：没有集结加入的行军 集结已解散
				YuriMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(getMarchEntity().getPlayerId())
						.setMailId(MailId.MASS_HAS_NO_JOIN_MARCH)
						.build());

				// 未到达的行军返回
				Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
				for (IWorldMarch joinMarch : joinMarchs) {

					double backX = getMarchEntity().getOrigionX();
					double backY = getMarchEntity().getOrigionY();
					AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(joinMarch.getMarchEntity());
					if (currPoint != null) {
						backX = currPoint.getX();
						backY = currPoint.getY();
					}

					// 体力返还
					WorldMarchService.getInstance().onMonsterRelatedMarchAction(joinMarch);
					// 行军返回
					WorldMarchService.getInstance().onMarchReturn(joinMarch, currTime, getMarchEntity().getAwardItems(), joinMarch.getMarchEntity().getArmys(), backX, backY);
					// 发邮件：队长行军已经出发
					YuriMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(getMarchEntity().getPlayerId())
							.setMailId(MailId.MASS_NOT_REACH)
							.build());
				}
				WorldMarchService.logger.info("mass  march failed, have no mass join march, march:{}", getMarchEntity());
				return;
			}
			
			// 更新行军状态
			getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
			long needTime = getMarchNeedTime();
			getMarchEntity().setEndTime(getMarchEntity().getStartTime() + needTime);
			getMarchEntity().setMarchJourneyTime((int) needTime);

			this.updateMarch();
			String guildId = this.getPlayer().getGuildId();

			// 没有到达队长家的行军者回家
			Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
			if (joinMarchs != null) {
				StringBuilder bufferName = new StringBuilder();
				boolean isFrist = false;
				for (IWorldMarch joinMarch : joinMarchs) {
					// 已到达队长家的参与者目标点和队长进行同步
					if (joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
						WorldMarchService.getInstance().removeWorldPointMarch(joinMarch.getMarchEntity().getTerminalX(), joinMarch.getMarchEntity().getTerminalY(), joinMarch);
						joinMarch.getMarchEntity().setTerminalId(getMarchEntity().getTerminalId());
						// 此处要将加入的行军状态改成集结行军状态
						joinMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE);
						joinMarch.getMarchEntity().setMarchJourneyTime(getMarchEntity().getMarchJourneyTime());
						joinMarch.getMarchEntity().setEndTime(getMarchEntity().getEndTime());
						joinMarch.updateMarch();
					} else {
						if (isFrist) {
							bufferName.append("，");
						}
						isFrist = true;
						bufferName.append(joinMarch.getMarchEntity().getPlayerName());

						// 未到达队长家的行军原路返回
						double backX = getMarchEntity().getOrigionX();
						double backY = getMarchEntity().getOrigionY();
						AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(joinMarch.getMarchEntity());
						if (currPoint != null) {
							backX = currPoint.getX();
							backY = currPoint.getY();
						}
						WorldMarchService.getInstance().onMarchReturn(joinMarch, currTime, getMarchEntity().getAwardItems(), joinMarch.getMarchEntity().getArmys(), backX, backY);
					}
				}
				if (isFrist) {
					int guildFlag = GuildService.getInstance().getGuildFlagByPlayerId(getMarchEntity().getPlayerId());
					joinMarchs.add(this);
					for (IWorldMarch joinMarch : joinMarchs) {
						// 发邮件---发车前没到达集结点
						GuildMailService.getInstance().sendMail(
								MailParames.newBuilder().setPlayerId(joinMarch.getPlayerId()).setMailId(MailId.MASS_PALYER_NOT_ARRIVE)
										.addSubTitles(this.getMarchEntity().getPlayerName())
										.addContents(bufferName).setIcon(guildFlag).build());
					}
				}
			}
			// 联盟任务-集结次数
			if(!HawkOSOperator.isEmptyString(guildId)){
				GuildService.getInstance().postGuildTaskMsg(new MemberMasstCountEvent(guildId));
				//队长和成员记录联盟排行榜
				for( IWorldMarch march : getMassMarchList(this)){
					String playerId = march.getPlayerId();
					GuildRankMgr.getInstance().onPlayerMass(playerId, guildId, 1);
				}
			}
		}
	}

	@Override
	default void updateMarch() {
		if (this.getMarchEntity().isInvalid()) {
			return;
		}
		WorldMarchService.getInstance().broadcastMassMarch2Team(this);
		BasedMarch.super.updateMarch();
	}
	
	@Override
	default boolean needShowInGuildWar() {
		return true;
	}
	
	/**
	 * 成员行军到达
	 */
	default void teamMarchReached(MassJoinMarch teamMarch){
	}
	/**
	 * 成员行军离开
	 */
	default void teamMarchCallBack(MassJoinMarch teamMarch) {}
	
	
	@Override
	default void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		WorldMarchService service = WorldMarchService.getInstance();
		if(getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE){
			Set<IWorldMarch> joinMarchs = service.getMassJoinMarchs(this, false);
			for (IWorldMarch joinMarch : joinMarchs) {
				WorldMarch joinMarchEntity = joinMarch.getMarchEntity();
				if (joinMarchEntity.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(joinMarchEntity);
					joinMarch.getMarchEntity().setCallBackX(point.getX());
					joinMarch.getMarchEntity().setCallBackY(point.getY());
					joinMarch.getMarchEntity().setCallBackTime(currentTime);
					service.onMarchCallBack(joinMarch);
				} else {
					service.onPlayerNoneAction(joinMarch, HawkTime.getMillisecond());
				}
			}
			service.onMarchReturnImmediately(this, getMarchEntity().getArmys());
		}
		
		if(this.isMarchState()){
			AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(getMarchEntity());
			Set<IWorldMarch> joinMarchs = service.getMassJoinMarchs(this, true);
			for (IWorldMarch joinMarch : joinMarchs) {
				joinMarch.getMarchEntity().setCallBackX(point.getX());
				joinMarch.getMarchEntity().setCallBackY(point.getY());
				joinMarch.getMarchEntity().setCallBackTime(currentTime);
				service.onMarchCallBack(joinMarch);
			}
			service.onMarchCallBack(this);
		}
		
		service.rmGuildMarch(getMarchId());
	}
	
	@Override
	default void moveCityProcess(long currentTime) {
		WorldMarchService.getInstance().mantualMassMarch(this, currentTime);
	}
}
