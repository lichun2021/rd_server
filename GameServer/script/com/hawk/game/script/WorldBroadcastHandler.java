package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.service.chat.ChatService;

/**
 * 系统广播
 * 
 * localhost:8080/script/broadcast?type=*&guildId=*&args=*'
 * http://10.0.1.125:8080/script/broadcast?type=0&args=hello
 *
 * type : 广播类型
 * args : 广播参数
 * guildId : 联盟id(可选)
 *
 * @author hawk
 *
 */
public class WorldBroadcastHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		//类型
		Const.NoticeCfgId type = null;
		if (!HawkOSOperator.isEmptyString(params.get("type"))) {
			type =Const.NoticeCfgId.valueOf(Integer.valueOf(params.get("type")));
		}

		//参数
		String str = params.get("args");
		Object[] args = null;
		if (!HawkOSOperator.isEmptyString(str)) {
			args = str.split(",");
		} else {
			args = new String[] { "" };
		}

		ChatService.getInstance().addWorldBroadcastMsg(ChatType.SYS_BROADCAST, type, null, args);
		return HawkScript.successResponse(null);
	}
}
