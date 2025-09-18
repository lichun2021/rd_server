package com.hawk.game.world.march.impl;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

/**
 * 集结加入攻打野怪行军
 * 
 * @author golden
 *
 */
public class MassMonsterJoinMarch extends PassiveMarch implements MassJoinMarch ,IReportPushMarch{
	public MassMonsterJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MONSTER_MASS_JOIN;
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
	}
	
	@Override
	public Set<String> attackReportRecipients() {
		Set<String> allToNotify = new HashSet<>();
		leaderMarch().ifPresent(march -> allToNotify.add(march.getMarchEntity().getPlayerId()));
		return allToNotify;
	}
}
