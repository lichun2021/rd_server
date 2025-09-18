package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

public class MonsterAtkAwardMsgInvoker extends HawkMsgInvoker {

	private static Logger logger = LoggerFactory.getLogger("Server");
	
	private Player player;
	private int monsterId;
	private AwardItems attackAward;
	private AwardItems firstKillAward;
	private AwardItems damageAward;
	private int attackTimes;

	public MonsterAtkAwardMsgInvoker(Player player, int monsterId, AwardItems attackAward, AwardItems firstKillAward, AwardItems damageAward, int attackTimes) {
		this.player = player;
		this.attackAward = attackAward;
		this.firstKillAward = firstKillAward;
		this.damageAward = damageAward;
		this.attackTimes = attackTimes;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		if (attackAward.hasAwardItem()) {
			try {
				attackAward.rewardTakeAffectAndPush(player, Action.FIGHT_MONSTER, false, RewardOrginType.KILL_MONSTER);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (firstKillAward.hasAwardItem()) {
			try {
				firstKillAward.rewardTakeAffectAndPush(player, Action.FIRST_KILL_MOSNTER_AWARD, false, RewardOrginType.KILL_MONSTER);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (damageAward.hasAwardItem()) {
			try {
				damageAward.rewardTakeAffectAndPush(player, Action.DAMAGE_MONSTER_AWARD, false, RewardOrginType.KILL_MONSTER);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		logger.debug("send monster attack award, playerId:{}, monsterId:{}, attackAward:{}, firstKillAward:{}, damageAward:{}, attackTimes:{}",
				player.getId(), monsterId, attackAward.toString(), firstKillAward.toString(), damageAward.toString(), attackTimes);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public AwardItems getAttackAward() {
		return attackAward;
	}

	public void setAttackAward(AwardItems attackAward) {
		this.attackAward = attackAward;
	}

	public AwardItems getFirstKillAward() {
		return firstKillAward;
	}

	public void setFirstKillAward(AwardItems firstKillAward) {
		this.firstKillAward = firstKillAward;
	}

	public AwardItems getDamageAward() {
		return damageAward;
	}

	public void setDamageAward(AwardItems damageAward) {
		this.damageAward = damageAward;
	}

	public int getAttackTimes() {
		return attackTimes;
	}

	public void setAttackTimes(int attackTimes) {
		this.attackTimes = attackTimes;
	}
}
