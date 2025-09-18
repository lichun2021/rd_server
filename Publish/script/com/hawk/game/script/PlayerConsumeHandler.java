package com.hawk.game.script;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;

/**
 * 消耗资源
 * 
 * localhost:8080/script/consume?playerName=l0001&items=type_id_count,type_id_count&isGold=true
 *
 * playerId: 玩家Id
 * playerName: 玩家名字
 * 
 * --------------------以下参数可选--------------------
 * 属性数100 :  10000_id_100
 * 道具数100 :  30000_id_100
 * 兵种数100 :  70000_id_100
 * 
 * @author lating
 *
 */
public class PlayerConsumeHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			String items = params.get("items");
			if (HawkOSOperator.isEmptyString(items)) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "items set error");
			}

			// 给玩家发奖
			AwardItems awardItems = AwardItems.valueOf(items);
			List<ItemInfo> itemInfos = awardItems.getAwardItems();

			// 过滤不合法数据
			Iterator<ItemInfo> iterator = itemInfos.iterator();
			while (iterator.hasNext()) {
				ItemInfo info = iterator.next();
				if (info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.PLAYER_ATTR_VALUE
						&& info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.TOOL_VALUE
						&& info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.SOLDIER_VALUE
						&& info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.EQUIP_VALUE
						|| info.getCount() < 0) {
					return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "item info error " + info.toString());
				}
			}
			
			boolean isGold = false;
			if (params.containsKey("isGold")) {
				isGold = "true".equals(params.get("isGold"));
			}

			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(itemInfos, isGold);
			int result = consume.checkResult(player);
			if (result == 0) {
				consume.consumeAndPush(player, Action.GM_EXPLOIT);
			} else {
				return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, Status.Error.valueOf(result).name().toLowerCase());
			}

			return HawkScript.successResponse(null);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
