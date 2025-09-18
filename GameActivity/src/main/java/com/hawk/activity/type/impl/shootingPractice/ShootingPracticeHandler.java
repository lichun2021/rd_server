package com.hawk.activity.type.impl.shootingPractice;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBShootingPracticeExchangeReq;
import com.hawk.game.protocol.Activity.PBShootingPracticeOverReq;
import com.hawk.game.protocol.HP;

public class ShootingPracticeHandler extends ActivityProtocolHandler {
	

	/**
	 * 上报游戏数据
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SHOOTING_PRACTICE_GAME_OVER_REQ_VALUE)
	public boolean onGameOver(HawkProtocol protocol, String playerId) {
		PBShootingPracticeOverReq req = protocol.parseProtocol(PBShootingPracticeOverReq.getDefaultInstance());
		ShootingPracticeActivity activity = getActivity(ActivityType.SHOOTING_PRACTICE);
		activity.onGameOver(playerId, req);
		return true;
	}
	
	
	/**
	 * 兑换
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SHOOTING_PRACTICE_EXCHANGE_ITEM_REQ_VALUE)
	public boolean onExchangeItem(HawkProtocol protocol, String playerId) {
		PBShootingPracticeExchangeReq req = protocol.parseProtocol(PBShootingPracticeExchangeReq.getDefaultInstance());
		ShootingPracticeActivity activity = getActivity(ActivityType.SHOOTING_PRACTICE);
		int exchangeId = req.getCfgId();
		int exchangeCount = req.getCount();
		activity.itemExchange(playerId, exchangeId, exchangeCount);
		return true;
	}
	
	
	
	/**
	 * 购买游戏次数
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SHOOTING_PRACTICE_BUY_GAME_COUNT_REQ_VALUE)
	public boolean onDailyScore(HawkProtocol protocol, String playerId) {
		ShootingPracticeActivity activity = getActivity(ActivityType.SHOOTING_PRACTICE);
		activity.buyGameCount(playerId, 1);
		return true;
	}
	
	
	
	/**
	 * 排行榜
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SHOOTING_PRACTICE_RANK_REQ_VALUE)
	public boolean onRankInfo(HawkProtocol protocol, String playerId) {
		ShootingPracticeActivity activity = getActivity(ActivityType.SHOOTING_PRACTICE);
		activity.getRanKInfo(playerId);
		return true;
	}

	
	/**
	 * 弹幕
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SHOOTING_PRACTICE_BARRAGE_REQ_VALUE)
	public boolean onBarrageInfo(HawkProtocol protocol, String playerId) {
		ShootingPracticeActivity activity = getActivity(ActivityType.SHOOTING_PRACTICE);
		activity.getBarrageInfo(playerId);
		return true;
	}

	
	
	
	
	
}
