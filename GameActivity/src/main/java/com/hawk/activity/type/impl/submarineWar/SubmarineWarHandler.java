package com.hawk.activity.type.impl.submarineWar;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SubmarineWarGameBuyReq;
import com.hawk.game.protocol.Activity.SubmarineWarGameStagePassReq;
import com.hawk.game.protocol.Activity.SubmarineWarGameStartReq;
import com.hawk.game.protocol.Activity.SubmarineWarOrderRewardReq;
import com.hawk.game.protocol.Activity.SubmarineWarShopBuyReq;
import com.hawk.game.protocol.Activity.SubmarineWarSkillItemBuyReq;
import com.hawk.game.protocol.HP;


public class SubmarineWarHandler extends ActivityProtocolHandler {
	
	
	//活动整体信息
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_INFO_REQ_VALUE)
	public void getPageInfo(HawkProtocol protocol, String playerId){
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.getPageInfo(playerId,protocol.getType());
	}
	
	
	//开始游戏
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_START_REQ_VALUE)
	public void gameStart(HawkProtocol protocol, String playerId){
		SubmarineWarGameStartReq req =  protocol.parseProtocol(SubmarineWarGameStartReq.getDefaultInstance());
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.gameStart(playerId,req.getType(), req.getSkillItemId(),protocol.getType());
	}
	
	
	
	//关卡通过
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_STAGE_PASS_REQ_VALUE)
	public void gameStagePass(HawkProtocol protocol, String playerId){
		SubmarineWarGameStagePassReq req =  protocol.parseProtocol(SubmarineWarGameStagePassReq.getDefaultInstance());
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.gameStagePass(playerId,req.getStage());
	}
	
	
	
	
	//购买特定随机道具
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_SKILL_BUY_REQ_VALUE)
	public void skillItemBuy(HawkProtocol protocol, String playerId){
		SubmarineWarSkillItemBuyReq req =  protocol.parseProtocol(SubmarineWarSkillItemBuyReq.getDefaultInstance());
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.skillItemBuy(playerId,req.getItemId(), req.getBuyCount(), protocol.getType());
	}
	
	
	
	//商店兑换
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_SHOP_BUY_REQ_VALUE)
	public void itemExchange(HawkProtocol protocol, String playerId){
		SubmarineWarShopBuyReq req =  protocol.parseProtocol(SubmarineWarShopBuyReq.getDefaultInstance());
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.itemExchange(playerId, req.getShopId(), req.getBuyCount(), protocol.getType());
	}
	
	//游戏次数购买
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_GAME_BUY_REQ_VALUE)
	public void gameCountBuy(HawkProtocol protocol, String playerId){
		SubmarineWarGameBuyReq req =  protocol.parseProtocol(SubmarineWarGameBuyReq.getDefaultInstance());
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.gameCountBuy(playerId, req.getBuyCount(), protocol.getType());
	}
	
	
	//排行榜
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_RANK_REQ_VALUE)
	public void getRankInfo(HawkProtocol protocol, String playerId){
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.getRankInfo(playerId);
	}
	
	
	//弹幕
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_BARRAGE_REQ_VALUE)
	public void getBarrageInfo(HawkProtocol protocol, String playerId){
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		activity.getBarrageInfo(playerId);
	}
	
	
	//战令领奖
	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_ORDER_REWARD_REQ_VALUE)
	public void orderReward(HawkProtocol protocol, String playerId){
		SubmarineWarOrderRewardReq req =  protocol.parseProtocol(SubmarineWarOrderRewardReq.getDefaultInstance());
		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
		int level = req.getLevel();
		if(level == 0){
			activity.orderRewardAll(playerId);
		}else{
			activity.orderReward(playerId, level);
		}
	}
	
	//购买战令等级  策划说不要了
//	@ProtocolHandler(code = HP.code2.SUBMARINE_WAR_ORDER_EXP_BUY_REQ_VALUE)
//	public void orderBuyLevel(HawkProtocol protocol, String playerId){
//		SubmarineWarOrderBuyLevelReq req =  protocol.parseProtocol(SubmarineWarOrderBuyLevelReq.getDefaultInstance());
//		SubmarineWarActivity activity = getActivity(ActivityType.SUBMARINE_WAR);
//		int level = req.getLevel();
//		activity.buyAuthLvlMultiple(playerId, level);
//	}
}
