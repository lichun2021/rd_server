package com.hawk.game.lianmengcyb.order;

import java.util.Objects;

import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrder;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrderUseReq;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.LogUtil;

/**
 * 朝指定建筑行军时长
 */
public class CYBORGOrder1001 extends CYBORGOrder {

	private int tarX;// = 2; // 技能生效目标建筑. 对于快速援助
	private int tarY;// = 3;

	public CYBORGOrder1001(CYBORGOrderCollection parent) {
		super(parent);
	}

	@Override
	public int canStartOrder(PBCYBORGOrderUseReq req) {
		ICYBORGWorldPoint point = getParent().getParent().getWorldPoint(req.getTarX(), req.getTarY()).orElse(null);
		if (Objects.isNull(point) || !(point instanceof ICYBORGBuilding)) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		return super.canStartOrder(req);
	}

	@Override
	public CYBORGOrder startOrder(PBCYBORGOrderUseReq req,ICYBORGPlayer player) {
		super.startOrder(req,player);
		this.tarX = req.getTarX();
		this.tarY = req.getTarY();
		
		String pos = String.format("%s_%s", this.getBuildX() ,this.getBuildY());
		String effPos = String.format("%s_%s", this.tarX ,this.tarY);
		// 广播战队
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.CYBORG_CHRONO_SPHERE_ORDER;
		ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(noticeId)
				.addParms(player.getCamp().intValue(),player.getGuildTag(),player.getName(),
						req.getOrderId(), pos, effPos).build();
		getParent().getParent().addWorldBroadcastMsg(parames);
		// 广播战场
		Const.NoticeCfgId noticeIdBroad = Const.NoticeCfgId.CYBORG_CHRONO_SPHERE_BROAD_CAST;
		ChatParames paramesBroad = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(noticeIdBroad)
				.addParms(player.getCamp().intValue(),player.getGuildTag(),player.getName(), req.getOrderId(), pos, effPos).build();
		getParent().getParent().addWorldBroadcastMsg(paramesBroad);
		// Tlog
		String roomId = this.getParent().getParent().getId();
		LogUtil.logCYBORGUseOrder(player, roomId, player.getGuildId(), player.getGuildName(), this.getOrderId(), 0);
		return this;
	}

	@Override
	public PBCYBORGOrder.Builder genPBCYBORGOrderBuilder() {
		PBCYBORGOrder.Builder result = super.genPBCYBORGOrderBuilder();
		if (inEffect()) {
			result.setTarX(tarX);
			result.setTarY(tarY);
		}
		return result;
	}

	public int getTarX() {
		return tarX;
	}

	public void setTarX(int tarX) {
		this.tarX = tarX;
	}

	public int getTarY() {
		return tarY;
	}

	public void setTarY(int tarY) {
		this.tarY = tarY;
	}

}
