package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GameUtil;

/**
 * 修改自定义数据脚本
 *
 * localhost:8080/script/customdata?key=?&value=?&arg=?
 *
 * key: 键
 * value: 值
 * arg: 参数(可选)
 *
 * @author hawk
 *
 */

public class CustomDataHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}

		String key = params.get("key");

		int value = 0;
		if (params.containsKey("value")) {
			value = Integer.valueOf(params.get("value"));
		}

		String arg = null;
		if (params.containsKey("arg")) {
			arg = params.get("arg");
		}
		
		if (GameUtil.isCustomKey(key)) {
			CustomDataEntity entity = player.getData().getCustomDataEntity(key);
			if (entity == null) {
				entity = player.getData().createCustomDataEntity(key, value, arg);
			} else {
				entity.setValue(value);
			}
			return HawkScript.successResponse(null);
		}

		return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "key is not exist : " + key);
	}
}
