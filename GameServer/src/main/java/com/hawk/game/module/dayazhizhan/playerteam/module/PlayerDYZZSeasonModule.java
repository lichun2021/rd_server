package com.hawk.game.module.dayazhizhan.playerteam.module;

import com.hawk.game.protocol.DYZZWar;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZSeasonOrderAdvanceBuyMsg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonOrderRewardAchiveReq;
import com.hawk.game.protocol.HP;

public class PlayerDYZZSeasonModule extends PlayerModule {

	
	public PlayerDYZZSeasonModule(Player player) {
		super(player);
	}
	
	
	
	@Override
	protected boolean onPlayerLogin() {
		DYZZSeasonService.getInstance().checkDYZZSeasonDataSync(this.player);
		return true;
	}
	
	/**
	 * 领取战令奖励
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_SEASON_ORDER_REWARD_ACHIVE_REQ_VALUE)
	public void achiveOrderReward(HawkProtocol protocol){
		PBDYZZSeasonOrderRewardAchiveReq req = protocol
				.parseProtocol(PBDYZZSeasonOrderRewardAchiveReq.getDefaultInstance());
		int type = req.getRewardType();
		int level = req.getRewardlevel();
		DYZZSeasonService.getInstance().achiveDYZZSeasonOrderReward(player, type, level);
	}

	@ProtocolHandler(code = HP.code2.DYZZ_SEASON_ORDER_REWARD_CHOOSE_VALUE)
	public void seasonOrderRewardChoose(HawkProtocol protocol){
		DYZZWar.PBDYZZSeasonOrderChoose choose = protocol
				.parseProtocol(DYZZWar.PBDYZZSeasonOrderChoose.getDefaultInstance());
		int type = choose.getType();
		int level = choose.getLevel();
		int itemid = choose.getChoose();
		DYZZSeasonService.getInstance().rewardChoose(player, type, level, itemid);
	}
	
	
	@MessageHandler
	public void buySeasonOrderAdvance(DYZZSeasonOrderAdvanceBuyMsg msg){
		int giftId = msg.getGiftId();
		DYZZSeasonService.getInstance().buyDYZZSeasonOrderAdvacne(player, giftId);
	}
	
	/**
	 * 获取排行榜数据
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_SEASON_SCORE_RANK_REQ_VALUE)
	public void getDYZZSeasonScoreRankInfo(HawkProtocol protocol){
		DYZZSeasonService.getInstance().getScoreRankPlayers(player);
	}
	
	
	
}
