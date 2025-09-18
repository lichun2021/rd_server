package com.hawk.game.gmscript;

import java.util.Map;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;

/**
 * 给手Q大区redis添加wx gmserver的ip地址
 * 
 * @param serverlist 以英文逗号分隔的wx大区gm服务器ip列表，如30.41.111.144,30.41.110.112,30.41.111.66
 * 
 * @author lating
 */
public class GmServerChangeHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			if (GsConfig.getInstance().getAreaId().equals("1")) {
				return HawkScript.successResponse("wx area skip", null);
			}
			
			String serverlist = params.get("serverlist");
			if (HawkOSOperator.isEmptyString(serverlist)) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "serverlist empty");
			}
			
			String[] ipArray = serverlist.split(",");
			for (String ip : ipArray) {
				if (!ip.equals(ip.trim())) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "ip[" + ip + "] error");
				}
				String[] seg = ip.split("\\.");
				if (seg.length != 4) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "ip[" + ip + "] error");
				}
				for (String str : seg) {
					if (HawkOSOperator.isEmptyString(str.trim())) {
						return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "ip[" + ip + "] error");
					}
				}
			}
			
			RedisProxy.getInstance().getRedisSession().del("gmserver_hosts");
			RedisProxy.getInstance().getRedisSession().sAdd("gmserver_hosts", 0, ipArray);
			
			// 返回区服和额外信息
			return HawkScript.successResponse("", null);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}

}
