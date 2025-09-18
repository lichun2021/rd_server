package com.hawk.game.strengthenguide;

import org.hawk.app.HawkAppObj;
import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.xid.HawkXID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.strengthenguide.StrengthenGuideTypedef.SGuideType;

@SuppressWarnings("unused")

public class StrengthenGuideManager {

	static private class Singletion {
		static StrengthenGuideManager instance = new StrengthenGuideManager();
	}

	public StrengthenGuideManager() {
	}

	public static StrengthenGuideManager getInstance() {
		return Singletion.instance;
	}

	public void postMsg(SGMsgInvoker msg) {
		try {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
				@Override
				public Object run() {
					msg.invoke();
					return null;
				}
			},0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public boolean init() {
		LoggerFactory.getLogger("Server").debug("strengthenguide init from redis");
		for (SGuideType eType : SGuideType.values()) {
			eType.loadFromRedis();
		}
		return true;
	}
}
