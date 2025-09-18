package com.hawk.game.recharge.impl;

import org.hawk.log.HawkLog;
import com.hawk.game.GsApp;
import com.hawk.game.config.MarchEmoticonProperty;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.msg.MarchEmoticonBagUnlockMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.util.GameUtil;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 行军表情礼包
 * 
 * @author lating
 *
 */
public class MarchEmotionGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		int emoticon = MarchEmoticonProperty.getInstance().getEmoticonBagByPayGift(giftCfg.getId());
		if (GameUtil.isMarchEmoticonBagUnlocked(player, emoticon)) {
			player.sendError(protocol, Status.Error.MARCH_EMOTICON_UNLOCKED, 0);
			HawkLog.errPrintln("emoticon has already unlocked, playerId: {}, payGiftId: {}, emoticon: {}", player.getId(), giftCfg.getId(), emoticon);
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		GsApp.getInstance().postMsg(player, new MarchEmoticonBagUnlockMsg(giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.MARCH_EMOTICON;
	}

}
