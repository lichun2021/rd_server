package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.module.dayazhizhan.marchserver.service.DYZZMatchService;

/**
 * 获取战斗日志
 * http://localhost:8080/script/battlelog?playerName=l0001
 *
 * @author hawk
 */
public class DYZZMatchInfoHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String result = DYZZMatchService.getInstance().getMatchInfo();
		return result;
	}
}