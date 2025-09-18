package com.hawk.ms;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigStorage;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.xid.HawkXID;
import com.hawk.ms.common.Constants;
import com.hawk.ms.service.MergeServerService;

public class MergeServerApp extends HawkApp {
	public MergeServerApp() {
		super(HawkXID.valueOf(Constants.ObjType.MANAGER, Constants.ObjId.APP));
	}

	public boolean init(String string, boolean isContinue) {
		// 应用程序主体配置
		MergeServerConfig appCfg = null;		
		try {
			HawkConfigStorage cfgStorage = new HawkConfigStorage(MergeServerConfig.class);
			appCfg = (MergeServerConfig) cfgStorage.getConfigByIndex(0);
			appCfg.checkValid();
			
			if (!HawkConfigManager.getInstance().init("com.hawk.ms.cfg")) {
				HawkLog.logPrintln("config init fail");
				return false;
			}			
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		if (!HawkOSOperator.installLibPath()) {
			HawkLog.errPrintln("install lib path fail");
			return false;
		}
		
		appCfg.setArgContinue(isContinue);
		if (!MergeServerService.getInstance().init(appCfg)) {
			return false;
		}
		
		
		return true;
	}

	
	@Override
	public boolean run() {
		long startTime = System.nanoTime();
		boolean result = MergeServerService.getInstance().run();
		if (result) {
			HawkLog.logPrintln("merge server success");
		} else {
			HawkLog.logPrintln("merge server fail");
		}
		long endTime = System.nanoTime();
		HawkLog.logPrintln("merge erver cost time:{}", (endTime - startTime) / 1_000_000_000);
		
		return result;
	}

}
