package com.hawk.game.tsssdk.invoker;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.PlayerWorldModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.WORLD_MARCH_PRESET_ADD)
public class WorldMarchPresetAddInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}

		JSONObject json = JSONObject.parseObject(callback);
		int idxValue = json.getIntValue("paramIdx");
		int cacheKey = json.getIntValue("cacheKey");
		PlayerWorldModule module = player.getModule(GsConst.ModuleType.WORLD_MODULE);
		JSONArray arr = module.getPlayerPresetMarchInfo();
		int index = idxValue - 1;
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		module.addWorldMarchPresetInfo(null, arr, obj, index, cacheKey);
		LogUtil.logSecTalkFlow(player, null, LogMsgType.WORLD_MARCH_PRESET, "", name);
		return 0;
	}

}
