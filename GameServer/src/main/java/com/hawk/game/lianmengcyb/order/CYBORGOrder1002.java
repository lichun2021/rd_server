package com.hawk.game.lianmengcyb.order;

import java.util.List;

import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGBuildState;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrderUseReq;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.LogUtil;

/**
 * 立即占领正在占领中的建筑. 不需要持续时间. 立即生效
 * @author lwt
 * @date 2022年4月8日
 */
public class CYBORGOrder1002 extends CYBORGOrder {

	public CYBORGOrder1002(CYBORGOrderCollection parent) {
		super(parent);
	}
	
	@Override
	public int canStartOrder(PBCYBORGOrderUseReq req) {
		boolean canUse = false;
		List<ICYBORGBuilding> buildList = getParent().getParent().getCYBORGBuildingList();
		for (ICYBORGBuilding build : buildList) {
			if (build.getState() == CYBORGBuildState.ZHAN_LING_ZHONG 
					&& build.getCamp() == getParent().getCamp()) {
				canUse = true;
				break;
			}
		}
		if(!canUse){
			return Status.CYBORGError.CYBORG_ORDER_USE_LIMIT_NO_BUILDING_VALUE;
		}
		return super.canStartOrder(req);
	}

	
	@Override
	public CYBORGOrder startOrder(PBCYBORGOrderUseReq req,ICYBORGPlayer player) {
		super.startOrder(req,player);

		StringBuilder effPosBuilder = new StringBuilder();
		List<ICYBORGBuilding> buildList = getParent().getParent().getCYBORGBuildingList();
		for (ICYBORGBuilding build : buildList) {
			if (build.getState() == CYBORGBuildState.ZHAN_LING_ZHONG && build.getCamp() == getParent().getCamp()) {
				build.setZhanLingJieShu(getParent().getParent().getCurTimeMil());
				String pos = String.format("%s_%s", build.getX(),build.getY());
				if(effPosBuilder.length() > 0){
					effPosBuilder.append(",");
				}
				effPosBuilder.append(pos.toString());
			}
		}
		
		
		String pos = String.format("%s_%s", this.getBuildX() ,this.getBuildY());
		String effPos = effPosBuilder.toString();
		// 广播战队
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.CYBORG_WEATHER_ORDER;
		ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(noticeId)
				.addParms(player.getCamp().intValue(),player.getGuildTag(),player.getName(), req.getOrderId(), pos, effPos).build();
		getParent().getParent().addWorldBroadcastMsg(parames);
		// 广播战场
		Const.NoticeCfgId noticeIdBroad = Const.NoticeCfgId.CYBORG_WEATHER_ORDER_BROAD_CAST;
		ChatParames paramesBroad = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(noticeIdBroad)
				.addParms(player.getCamp().intValue(),player.getGuildTag(),player.getName(), req.getOrderId(), pos, effPos).build();
		getParent().getParent().addWorldBroadcastMsg(paramesBroad);
		// Tlog
		String roomId = this.getParent().getParent().getId();
		LogUtil.logCYBORGUseOrder(player, roomId, player.getGuildId(), player.getGuildName(), this.getOrderId(), 0);

		return this;
	}
}
