package com.hawk.game.tsssdk.invoker;

import org.hawk.net.protocol.HawkProtocol;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.PlayerWorldModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.PlayerPresetMarchInfo;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.WORLD_MARCH_PRESET_UPDATE)
public class WorldMarchPresetChangeInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		JSONArray arr = new JSONArray(GsConst.MAX_PRESET_SIZE);
		String presetMarchStr = RedisProxy.getInstance().getPlayerPresetWorldMarch(player.getId());
		if (presetMarchStr != null) {
			arr.addAll(JSONArray.parseArray(presetMarchStr));
		}

		int idx = Integer.parseInt(callback);
		int index = idx - 1;
		JSONObject obj = new JSONObject();
		if (arr.size() >= idx && arr.getJSONObject(index) != null) {
			obj = arr.getJSONObject(index);
		}
		
		obj.put("name", name);
		arr.set(index, obj);
		RedisProxy.getInstance().addPlayerPresetWorldMarch(player.getId(), arr);
		
		PlayerWorldModule module = player.getModule(GsConst.ModuleType.WORLD_MODULE);
		PlayerPresetMarchInfo.Builder infos = module.makeMarchPresetBuilder();
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MARCH_PRESET_NAME_CHANGE_S, infos));
		
		LogUtil.logSecTalkFlow(player, null, LogMsgType.WORLD_MARCH_PRESET, "", name);
		
		return 0;
	}

}
