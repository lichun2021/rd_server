package com.hawk.game.world.march.impl;

import java.util.HashSet;
import java.util.Set;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 加入集结攻打单人基地
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class MassJoinSingleMarch extends PassiveMarch implements MassJoinMarch,IReportPushMarch  {

	public MassJoinSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.MASS_JOIN;
	}

	@Override
	public void onMarchReach(Player player) {
		// TODO Auto-generated method stub
		MassJoinMarch.super.onMarchReach(player);
		leaderMarch().ifPresent(march -> march.teamMarchReached(this));
		this.removeAttackReport();
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
	
	@Override
	public void onWorldMarchReturn(Player player) {
		WorldMarch march = getMarchEntity();
		
		String awardStr = march.getAwardStr();
		if (!HawkOSOperator.isEmptyString(awardStr)) {
			AwardItems award = AwardItems.valueOf(awardStr);
			award.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_MASS_RETURN);
		}
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_MASS_RETURN, Params.valueOf("march", march), Params.valueOf("awardStr", awardStr));
	}
}
