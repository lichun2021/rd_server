package com.hawk.game.module.lianmengyqzz.battleroom.order;

import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderUseReq;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.GameUtil;

/**
 * 本方加buff
 */
public class YQZZOrder4001 extends YQZZOrder {

	public YQZZOrder4001(YQZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public YQZZOrder startOrder(PBYQZZOrderUseReq req, IYQZZPlayer comdPlayer) {
		super.startOrder(req, comdPlayer);

		for (YQZZGuildBaseInfo baseInfo : getParent().getParent().getParent().getBattleCamps()) {
			if (baseInfo.camp == comdPlayer.getCamp()) {
				baseInfo.declareWarPoint += getConfig().getP1();
			}
		}

		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_LIJIHUIFU)
				.addParms(GameUtil.getPresidentOfficerId(comdPlayer.getId()))
				.addParms(comdPlayer.getName())
				.addParms(getParent().getParent().getX())
				.addParms(getParent().getParent().getY()).build();
		getParent().getParent().getParent().addWorldBroadcastMsg(parames);
		return this;
	}
}
