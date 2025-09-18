package com.hawk.activity.type.impl.questTreasure;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.QuestTreasureGamePointChooseReq;
import com.hawk.game.protocol.Activity.QuestTreasureGameRandomItemBuyReq;
import com.hawk.game.protocol.Activity.QuestTreasureShopBuyReq;
import com.hawk.game.protocol.HP;


public class QuestTreasureHandler extends ActivityProtocolHandler {
	
	
	//活动整体信息
	@ProtocolHandler(code = HP.code2.QUEST_TREASURE_INFO_REQ_VALUE)
	public void geQuestTreasureInfo(HawkProtocol protocol, String playerId){
		QuestTreasureActivity activity = getActivity(ActivityType.QUEST_TREASURE);
		activity.getPageInfo(playerId,protocol.getType());
	}
	
	
	//设置前进路径
	@ProtocolHandler(code = HP.code2.QUEST_TREASURE_GAME_POINT_CHOOSE_REQ_VALUE)
	public void setGameChoosePoints(HawkProtocol protocol, String playerId){
		QuestTreasureGamePointChooseReq req =  protocol.parseProtocol(QuestTreasureGamePointChooseReq.getDefaultInstance());
		QuestTreasureActivity activity = getActivity(ActivityType.QUEST_TREASURE);
		activity.setGameChoosePoints(playerId,req.getChoosePosList(),protocol.getType());
	}
	
	
	
	//随机前进步数
	@ProtocolHandler(code = HP.code2.QUEST_TREASURE_GAME_WALK_RANDOM_REQ_VALUE)
	public void randomWalk(HawkProtocol protocol, String playerId){
		QuestTreasureActivity activity = getActivity(ActivityType.QUEST_TREASURE);
		activity.randomWalk(playerId,protocol.getType());
	}
	
	
	
	
	//购买特定随机道具
	@ProtocolHandler(code = HP.code2.QUEST_TREASURE_GAME_RANDOM_ITEM_BUY_REQ_VALUE)
	public void itemBuy(HawkProtocol protocol, String playerId){
		QuestTreasureGameRandomItemBuyReq req =  protocol.parseProtocol(QuestTreasureGameRandomItemBuyReq.getDefaultInstance());
		QuestTreasureActivity activity = getActivity(ActivityType.QUEST_TREASURE);
		activity.itemBuy(playerId, req.getBuyCount(), protocol.getType());
	}
	
	
	
	//商店兑换
	@ProtocolHandler(code = HP.code2.QUEST_TREASURE_SHOP_BUY_REQ_VALUE)
	public void itemExchange(HawkProtocol protocol, String playerId){
		QuestTreasureShopBuyReq req =  protocol.parseProtocol(QuestTreasureShopBuyReq.getDefaultInstance());
		QuestTreasureActivity activity = getActivity(ActivityType.QUEST_TREASURE);
		activity.itemExchange(playerId, req.getShopId(), req.getBuyCount(), protocol.getType());
	}

}
