package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.configupdate.ActivityConfigUpdateManager;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ServerGroupCfg;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 遍历检测基础+所有分组配置
 * 
 * localhost:8080/script/groupDataCheck
 *
 * @author hawk
 */
public class GroupDataCheckHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		boolean result = true;
		List<String> updateList = new ArrayList<String>();
		try {
			// reload基础目录下配置
			HawkConfigManager.getInstance().setConfigRoot("");
			if (!reloadCfg(updateList)) {
				result = false;
			}

			// 遍历reload各分组的配置
			ConfigIterator<ServerGroupCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ServerGroupCfg.class);
			for (ServerGroupCfg cfg : iterator) {
				HawkConfigManager.getInstance().setConfigRoot("groupData/" + cfg.getGroupId());
				if (!reloadCfg(updateList)) {
					result = false;
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			// 检测完成后,恢复配置文件根目录
			String serverId = GsConfig.getInstance().getServerId();
			String groupId = ServerGroupCfg.getServerGroupId(serverId);
			if (!HawkOSOperator.isEmptyString(groupId)) {
				HawkConfigManager.getInstance().setConfigRoot("groupData/" + groupId);
				HawkLog.logPrintln("server group info, serverId: {}, groupId: {}", serverId, groupId);
			} else {
				HawkConfigManager.getInstance().setConfigRoot("");
			}
			if (!reloadCfg(updateList)) {
				result = false;
			}
		}
		if (result) {
			// 返回成功信息
			return HawkScript.successResponse("group config check success! config: " + updateList.toString());
		}
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}

	private boolean reloadCfg(List<String> updateList) {
		boolean result = true;
		// 检测更新逻辑配置
		if (HawkConfigManager.getInstance().updateReload(updateList)) {
			// 更新黑白名单
			result &= GsApp.getInstance().updateIpControl();

			// 更新安全协议
			GsApp.getInstance().updateSecProto();

			// 重新初始化屏蔽字库
			result &= GsApp.getInstance().updateWords();
			result &= GsApp.getInstance().updateSlienceWords();

			// 活动相关数据
			ActivityConfigUpdateManager.getInstance().updateConfig(true, updateList);

		} else {
			result = false;
		}

		return result;
	}

}