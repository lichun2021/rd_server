package com.hawk.game.invoker.guard;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Friend.GuardGiftSendResp;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.BuilderUtil;

/**
 * 玩家线程扣完东西之后发到relationService 线程加守护值.
 * @author jm
 *
 */
public class GuardGiftSendInvoker extends HawkMsgInvoker {
	/**
	 * 玩家
	 */
	private Player player;
	/**
	 * 守护礼包ID
	 */
	private int giftId;
	public GuardGiftSendInvoker(Player player, int giftId) {
		this.player = player;
		this.giftId = giftId;
	}
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		RelationService.getInstance().onSendGift(player.getId(), giftId);
		
		//同步放在这里需要等guardValue生效之后.
		GuardGiftSendResp.Builder sbuilder = GuardGiftSendResp.newBuilder();
		sbuilder.setGiftMsg(BuilderUtil.buildGuardGift(giftId, player.getData().getDailyDataEntity().getGuardGiftNum(giftId)));
		sbuilder.setGuardValue(RelationService.getInstance().getGuardValue(player.getId()));
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.GUARD_GIFT_SEND_RESP_VALUE, sbuilder);
		player.sendProtocol(respProtocol);
		
		//客户端判断状态有问题,服务器推送.
		RelationService.getInstance().synGuardHud(player);
		
		return true;
	}

}
