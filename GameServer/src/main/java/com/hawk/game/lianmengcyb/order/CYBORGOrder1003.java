package com.hawk.game.lianmengcyb.order;

import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrderUseReq;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.LogUtil;

/**
 * 本方加buff
 */
public class CYBORGOrder1003 extends CYBORGOrder {

	public CYBORGOrder1003(CYBORGOrderCollection parent) {
		super(parent);
	}

	@Override
	public CYBORGOrder startOrder(PBCYBORGOrderUseReq req,ICYBORGPlayer player) {
		super.startOrder(req,player);
		
		String pos = String.format("%s_%s", this.getBuildX() ,this.getBuildY());
		// 广播战队
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.CYBORG_IRON_ORDER;
		ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(noticeId)
				.addParms(player.getCamp().intValue(),player.getGuildTag(),player.getName(), req.getOrderId(), pos).build();
		getParent().getParent().addWorldBroadcastMsg(parames);
		// 广播战场
		Const.NoticeCfgId noticeIdBroad = Const.NoticeCfgId.CYBORG_IRON_ORDER_BROAD_CAST;
		ChatParames paramesBroad = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(noticeIdBroad)
				.addParms(player.getCamp().intValue(),player.getGuildTag(),player.getName(), req.getOrderId(), pos).build();
		getParent().getParent().addWorldBroadcastMsg(paramesBroad);
		// Tlog
		String roomId = this.getParent().getParent().getId();
		LogUtil.logCYBORGUseOrder(player, roomId, player.getGuildId(), player.getGuildName(), this.getOrderId(), 0);
		return this;
	}
}
