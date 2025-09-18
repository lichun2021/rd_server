package com.hawk.game.world.march.impl;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.PushService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

public class FoggyMassJoinMarch extends PassiveMarch implements MassJoinMarch, IReportPushMarch {

	public FoggyMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FOGGY_FORTRESS_MASS_JOIN;
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
		MassJoinMarch.super.autoMassJoinStop();
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		leaderMarch().ifPresent(march -> march.teamMarchCallBack(this));
		this.removeAttackReport();
	}

	@Override
	public void onMarchReach(Player player) {
		MassJoinMarch.super.onMarchReach(player);
		this.removeAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
		leaderMarch().ifPresent(march -> {
			if (march instanceof IReportPushMarch) {
				((IReportPushMarch) march).pushAttackReport();
			}
		});
		
		String playerId = getMarchEntity().getPlayerId();
		if((this.getMarchEntity().getMarchProcMask() & GsConst.MarchProcMask.IS_MARCHREACH) > 0) {
			PushService.getInstance().pushMsg(playerId, PushMsgType.ATTACK_FOGGY_ARMY_RETURN_VALUE);
		}
	}

	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		leaderMarch().ifPresent(march -> allToNotify.add(march.getMarchEntity().getPlayerId()));
		return allToNotify;
	}
}
