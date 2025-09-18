package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigStorage;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.configupdate.ActivityConfigUpdateManager;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.module.ClientCommonModule;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.player.roleexchange.XinyueConst.ServerState;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GameUtil;
import com.hawk.gamelog.GameLog;
import com.hawk.sdk.config.CouponCfg;
import com.hawk.sdk.config.CreditCfg;
import com.hawk.sdk.config.HealthCfg;
import com.hawk.sdk.config.PlatformConstCfg;
import com.hawk.sdk.config.TencentCfg;

/**
 * 配置重新加载
 * 
 * localhost:8080/script/xmlreload
 *
 * @author hawk
 */
public class XmlReloadHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			// 更新的配置列表
			List<String> updateList = new ArrayList<String>();
			
			// 重新加载app的主配置
			if (GsConfig.getInstance().getStorage().checkUpdate()) {				
				HawkConfigStorage cfgStorage = new HawkConfigStorage(GsConfig.class);
				GsConfig appCfg = (GsConfig) cfgStorage.getConfigByIndex(0);
				if (appCfg == null) {
					return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "GsConfig");
				}
				
				updateList.add(cfgStorage.getFilePath());
				HawkTime.setMsOffset(appCfg.getTsOffset() * 1000L);
				
				// 写版本信息
				GsApp.getInstance().flushVersionInfo();
				// 检测更新全服数据
				GameUtil.checkUpdateGlobalData();
			}
			
			// 重新加载tencent的配置
			if (TencentCfg.getInstance() != null && TencentCfg.getInstance().getStorage().checkUpdate()) {				
				HawkConfigStorage cfgStorage = new HawkConfigStorage(TencentCfg.class);
				TencentCfg tencentCfg = (TencentCfg) cfgStorage.getConfigByIndex(0);
				if (tencentCfg == null) {
					return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "TencentCfg");
				}
				updateList.add(cfgStorage.getFilePath());
			}
			
			if (CouponCfg.getInstance() != null && CouponCfg.getInstance().getStorage().checkUpdate()) {				
				HawkConfigStorage cfgStorage = new HawkConfigStorage(CouponCfg.class);
				CouponCfg couponCfg = (CouponCfg) cfgStorage.getConfigByIndex(0);
				if (couponCfg == null) {
					return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "CouponCfg");
				}
				updateList.add(cfgStorage.getFilePath());
			}
			
			// 重新加载health的配置
			if (HealthCfg.getInstance() != null && HealthCfg.getInstance().getStorage().checkUpdate()) {				
				HawkConfigStorage cfgStorage = new HawkConfigStorage(HealthCfg.class);
				HealthCfg healthCfg = (HealthCfg) cfgStorage.getConfigByIndex(0);
				if (healthCfg == null) {
					return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "HealthCfg");
				}
				updateList.add(cfgStorage.getFilePath());
			}
			
			// 重新加载msdk平台接口请求参数常量配置
			if (PlatformConstCfg.getInstance() != null && PlatformConstCfg.getInstance().getStorage().checkUpdate()) {				
				HawkConfigStorage cfgStorage = new HawkConfigStorage(PlatformConstCfg.class);
				PlatformConstCfg platformConstCfg = (PlatformConstCfg) cfgStorage.getConfigByIndex(0);
				if (platformConstCfg == null) {
					return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "PlatformConstCfg");
				}
				updateList.add(cfgStorage.getFilePath());
			}
			
			// credit配置检测
			if (CreditCfg.getInstance() != null && CreditCfg.getInstance().getStorage().checkUpdate()) {				
				HawkConfigStorage cfgStorage = new HawkConfigStorage(CreditCfg.class);
				CreditCfg creditCfg = (CreditCfg) cfgStorage.getConfigByIndex(0);
				if (creditCfg == null) {
					return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "CreditCfg");
				}
				updateList.add(cfgStorage.getFilePath());
			}
			
			// 重新加载logTable相关配置
			if(GameLog.getInstance().reloadConfig()) {
				updateList.add("logTable.xml");
			}
			
			//服务器运行标识（0: 未停服 1: 已停服）
			RoleExchangeService.getInstance().updateServerState(ServerState.SERVER_RUNNING);
			
			// 检测更新逻辑配置
			if (HawkConfigManager.getInstance().updateReload(updateList)) {
				// 通知配置重新加载
				GsApp.getInstance().onCfgReload(updateList);
				
				// 清空配置mds缓存
				ClientCommonModule.onXmlReload();
				
				// 活动相关数据
				ActivityConfigUpdateManager.getInstance().updateConfig(true, updateList);
				
				// 返回成功信息
				return HawkScript.successResponse("xml reload success! config: " + updateList.toString());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}