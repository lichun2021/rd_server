package com.hawk.game.module.lianmengtaiboliya.worldmarch.submarch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.YuriMailService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/** 集结行军 */
public abstract class ITBLYMassMarch extends ITBLYWorldMarch {

	public ITBLYMassMarch(ITBLYPlayer parent) {
		super(parent);
	}

	public boolean isMassMarch() {
		return true;
	}

	/**
	 * 获取加入类型行军
	 * 
	 * @return
	 */
	public abstract WorldMarchType getJoinMassType();

	public void waitingStatusMarchProcess() {
		// 当前时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		// 集结等待中
		if (getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			return;
		}
		// 集结者还未出发
		if (getMarchEntity().getStartTime() > currTime) {// 还在等待中
			return;
		}
		// 没有加入集结的行军到达，集结解散, 未到达的行军返回
		Set<ITBLYWorldMarch> reachJoinMarchs = getMassJoinMarchs(true);
		if (reachJoinMarchs.size() <= 0) {
			// 行军返还
			onMarchBack();
			// 发邮件：没有集结加入的行军 集结已解散
			YuriMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(getMarchEntity().getPlayerId())
					.setDuntype(DungeonMailType.TBLY)
					.setMailId(MailId.MASS_HAS_NO_JOIN_MARCH)
					.build());

			// 未到达的行军返回
			Set<ITBLYWorldMarch> joinMarchs = getMassJoinMarchs(false);
			for (ITBLYWorldMarch joinMarch : joinMarchs) {
				joinMarch.onMarchCallback();
				// 发邮件：队长行军已经出发
				YuriMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(getMarchEntity().getPlayerId())
						.setDuntype(DungeonMailType.TBLY)
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

		boolean isFrist = false;
		StringBuilder bufferName = new StringBuilder();
		Set<ITBLYWorldMarch> joinMarchs = getMassJoinMarchs(false);
		for (ITBLYWorldMarch joinMarch : joinMarchs) {

			if (joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				joinMarch.getMarchEntity().setTerminalId(getMarchEntity().getTerminalId());
				// 此处要将加入的行军状态改成集结行军状态
				joinMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE);
				joinMarch.getMarchEntity().setMarchJourneyTime(getMarchEntity().getMarchJourneyTime());
				joinMarch.getMarchEntity().setEndTime(getMarchEntity().getEndTime());
				joinMarch.updateMarch();
			} else {
				if (isFrist) {
					bufferName.append(",");
				}
				isFrist = true;
				bufferName.append(joinMarch.getMarchEntity().getPlayerName());
				joinMarch.onMarchCallback();
			}
		}

		if (isFrist) {
			int guildFlag = getParent().getGuildFlag();
			joinMarchs.add(this);
			for (IWorldMarch joinMarch : joinMarchs) {
				// 发邮件---发车前没到达集结点
				GuildMailService.getInstance().sendMail(
						MailParames.newBuilder().setPlayerId(joinMarch.getPlayerId()).setMailId(MailId.MASS_PALYER_NOT_ARRIVE)
								.setDuntype(DungeonMailType.TBLY)
								.addSubTitles(this.getMarchEntity().getPlayerName())
								.addContents(bufferName).setIcon(guildFlag).build());
			}
		}
	}

	@Override
	public Set<ITBLYWorldMarch> getMassJoinMarchs(boolean needReach) {
		// 等待和到达的行军
		Set<ITBLYWorldMarch> retMarchs = new HashSet<>();
		// 不是队长行军
		if (isMassMarch()) {
			// 已到达队长家的行军
			List<ITBLYWorldMarch> passiveMarchs = getParent().getParent().getWorldMarchList();
			for (ITBLYWorldMarch march : passiveMarchs) {
				// 类型不匹配
				if (getJoinMassType() != march.getMarchType()) {
					continue;
				}
				// 返程加入行军不处理
				if (march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
					continue;
				}
				if (needReach && march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE
						&& march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE
						&& march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE) {
					continue;
				}
				if (getMarchId().equals(march.getMarchEntity().getTargetId())) {
					retMarchs.add(march);
				}
			}
		}
		return retMarchs;
	}

	public void teamMarchReached(ITBLYMassJoinMarch JBSMassJoinMarch) {
		if(this instanceof ITBLYReportPushMarch){
			((ITBLYReportPushMarch)this).pushAttackReport();
		}
	}

	public void teamMarchCallBack(ITBLYMassJoinMarch JBSMassJoinMarch) {
		if(this instanceof ITBLYReportPushMarch){
			((ITBLYReportPushMarch)this).pushAttackReport();
		}
	}

}
