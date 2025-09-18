package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.db.HawkDBManager;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.service.GuildService;
import com.hawk.game.world.proxy.WorldPointProxy;

/**
 * 数据库立即落地
 * 
 * localhost:8080/script/dbland
 *
 * @author hawk
 *
 */
public class LandImmediately extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		// mysql落地
		HawkDBManager.getInstance().landImmediately();
		
		// 世界地图数据落地
		WorldPointProxy.getInstance().flush();
		// 联盟帮助
		GuildService.getInstance().saveGuildHelpInfoTable();
		// 联盟任务
		GuildService.getInstance().updateGuildTaskList();
		
		return HawkScript.successResponse(null);
	}
}