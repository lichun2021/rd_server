package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;

public class UpdateRankScoreMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	private RankType rankType;
	private long score;
	
	public UpdateRankScoreMsgInvoker(Player player, RankType rankType, long score) {
		this.player = player;
		this.rankType = rankType;
		this.score = score;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		boolean beBan = RankService.getInstance().isBan(player.getId(), rankType);
		if (!beBan && !player.isZeroEarningState()) {
			RankService.getInstance().updateRankScore(rankType, score, player.getId());
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public RankType getRankType() {
		return rankType;
	}

	public void setRankType(RankType rankType) {
		this.rankType = rankType;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}
	
}
