package com.hawk.game.tsssdk.invoker;

import java.util.List;

import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.invoker.CynorgCreateTeamRpcInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.CREATE_CYBOR_TEAM)
public class CreateCyborTeamNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		} else {
			List<ItemInfo> needItems = CyborgConstCfg.getInstance().getTeamCreateCostItem();
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(needItems);
			if (!consume.checkConsume(player)) {
				return 0;
			}
			player.rpcCall(MsgId.CYBOGR_CREATE_TEAM, CyborgWarService.getInstance(), 
					new CynorgCreateTeamRpcInvoker(player, name, consume, protocol));
		}
		
		return 0;
	}

}
