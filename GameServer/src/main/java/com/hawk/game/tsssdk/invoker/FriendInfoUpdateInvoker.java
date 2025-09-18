package com.hawk.game.tsssdk.invoker;

import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.UPDATE_FRIEND_INFO)
public class FriendInfoUpdateInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String remark, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		String friendPlayerId = callback;
		PlayerRelationEntity playerRelationEntity = RelationService.getInstance().getPlayerRelationEntity(player.getId(), friendPlayerId);
		playerRelationEntity.setRemark(remark);
		player.responseSuccess(protocol);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.FRIEND_REMARK_CHANGE, "", remark);
		
		return 0;
	}

}
