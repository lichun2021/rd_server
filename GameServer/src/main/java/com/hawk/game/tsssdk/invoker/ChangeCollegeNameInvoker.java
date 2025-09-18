package com.hawk.game.tsssdk.invoker;

import java.util.Objects;

import org.hawk.log.HawkLog;

import com.hawk.game.invoker.CollegeChangeNameRpcInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.CHANGE_COLLEGE_NAME)
public class ChangeCollegeNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String guildName, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		} else {
			ItemInfo cost = CollegeService.getInstance().getReNameCost(player.getCollegeId());
			if(Objects.nonNull(cost)){
				ConsumeItems consume = ConsumeItems.valueOf();
				consume.addConsumeInfo(cost, false);
				if(!consume.checkConsume(player)){
					player.sendError(protocol, Status.Error.ITEM_NOT_ENOUGH_VALUE,0);
					HawkLog.errPrintln("player change guild name tsssdk invoker, playerId: {}", player.getId());
					return 0;
				}
			}
			player.rpcCall(MsgId.CHANGE_COLLEGE_NAME, CollegeService.getInstance(),
					new CollegeChangeNameRpcInvoker(player, guildName, protocol));
		}
		return 0;
	}

}
