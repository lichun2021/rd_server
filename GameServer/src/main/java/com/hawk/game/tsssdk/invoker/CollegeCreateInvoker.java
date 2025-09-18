package com.hawk.game.tsssdk.invoker;

import org.hawk.log.HawkLog;

import com.hawk.game.invoker.CollegeCreateRpcInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.module.college.cfg.CollegeConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.CREATE_COLLEGE)
public class CollegeCreateInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String collegeName, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		} else {
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(CollegeConstCfg.getInstance().getCreateCostList());
			if (!consume.checkConsume(player)) {
				player.sendError(protocol, Status.Error.ITEM_NOT_ENOUGH_VALUE, 0);
				HawkLog.logPrintln("CollegeCreateInvoker fail item less, playerId: {}, name:{},rlt:{}", player.getId(), collegeName, result);
				return 0;
			}
			player.rpcCall(MsgId.COLLEGE_CREATE, CollegeService.getInstance(), new CollegeCreateRpcInvoker(player, collegeName,consume, 
					HP.code.COLLEGE_CREATE_REQ_C_VALUE));
		}
		
		return 0;
	}

}
