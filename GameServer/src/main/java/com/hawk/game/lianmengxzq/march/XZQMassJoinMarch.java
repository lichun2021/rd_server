package com.hawk.game.lianmengxzq.march;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

/**
 * 超级武器集结加入行军
 * @author golden
 *
 */
public class XZQMassJoinMarch extends PassiveMarch implements MassJoinMarch, IXZQMarch, IReportPushMarch, IPassiveAlarmTriggerMarch {

	public XZQMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.XZQ_MASS_JOIN;
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReach(Player player) {
		MassJoinMarch.super.onMarchReach(player);
		leaderMarch().ifPresent(march -> march.teamMarchReached(this));
		this.removeAttackReport();
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
		if (this.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			IXZQMarch.super.detailMarchStop(targetPoint);
		}
	}

	@Override
	public void onMarchReturn() {
		leaderMarch().ifPresent(march -> march.teamMarchCallBack(this));
		this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		leaderMarch().ifPresent(march -> allToNotify.add(march.getMarchEntity().getPlayerId()));
		return allToNotify;
	}

	@Override
	public void pullAttackReport() {
		leaderMarch().ifPresent(leaderMarch -> {
			if (leaderMarch instanceof IPassiveAlarmTriggerMarch) {
				((IPassiveAlarmTriggerMarch) leaderMarch).pullAttackReport();
			}
		});
	}

	@Override
	public void pullAttackReport(String playerId) {
		leaderMarch().ifPresent(leaderMarch -> {
			if (leaderMarch instanceof IPassiveAlarmTriggerMarch) {
				((IPassiveAlarmTriggerMarch) leaderMarch).pullAttackReport(playerId);
			}
		});
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
		this.removeAttackReportFromPoint(getOrigionX(), getOrigionY());
	}
}
