package com.hawk.game.tsssdk.invoker;

import org.hawk.log.HawkLog;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.PlayerOperationModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.PLAYER_CHANGE_NAME)
public class PlayerChangeNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int resultCode, String name, int protocol, String callback) {
		if (resultCode != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		Action action = Action.PLAYER_CHANGE_NAME;
		Const.ItemId itemId = Const.ItemId.ITEM_CHANGE_NAME;
		int count = 1;
		JSONObject json = JSONObject.parseObject(callback);
		boolean useGold = json.getBooleanValue("useGold");
		int checkResult = json.getIntValue("checkResult");
		
		PlayerOperationModule module = player.getModule(GsConst.ModuleType.OPERATION_MODULE);
		int result = Status.SysError.SUCCESS_OK_VALUE;
		if (useGold) {
			result = module.costGoldFromItem(protocol, Const.ShopId.SHOP_CHANGE_NAME, itemId, count, action);
		} else {
			result = module.costItem(protocol, itemId, count, action);
		}
		
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			GameUtil.removePlayerNameInfo(name);
			player.sendError(protocol, result, 0);
			HawkLog.errPrintln("player change name failed, playerId: {}, tarName: {}, result: {}", player.getId(), name, result);
			return 0;
		}
		
		module.changeName(name, action, protocol);
		if (checkResult > 0) {
			RedisProxy.getInstance().removeChangeContentCDTime(player.getId(), ChangeContentType.CHANGE_ROLE_NAME);
		}
		
		LogUtil.logSecTalkFlow(player, null, LogMsgType.CHANGE_NAME, "", name);
		
		return 0;
	}

}
