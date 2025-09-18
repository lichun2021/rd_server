package com.hawk.game.gmscript;

import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MergeService;

/**
 * 拆服完成,服务器启动以后执行此脚本
 * @author Golden
 *
 */
public class SeparateAfterHandler extends HawkScript {
	
	@Override
	public String action(Map<String, String> map, HawkScriptHttpInfo scriptInfo) {
		doAction();
		return HawkScript.successResponse("ok");
	}
	 
	private void doAction() {
		// 清除空联盟
		MergeService.clearEmptyGuild();
		
		// 检测 联盟盟主&联盟领地
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		guildIds.forEach(id -> {
			try {
				GuildService.getInstance().checkLeaderExist(id);
				GuildManorService.getInstance().checkGuildManor(id);
			} catch (Exception e){
				HawkException.catchException(e);
			}
		});
		
		// 更新合服信息，给idip中转服（GM服）提供信息
		MergeService.updateMergeServerInfo();
		
		// 创建世界点
		MergeService.createPoint(15000, false);
	}
}
