package com.hawk.game.nation.tech.skill;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.hawk.activity.constant.ObjType;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.NationTechCfg;
import com.hawk.msg.GlobalBuffAddMsg;

/**
 * 奇珍异宝
 * @author Golden
 *
 */
public class NationTechSkill111 {

	public static void touchSkill() {
		int techId = 11101;
		
		NationTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationTechCfg.class, techId);
		
		// param1 触发buffId
		int buffId = Integer.parseInt(cfg.getParam1());
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
		if (buffCfg == null) {
			return;
		}
		
		long currentTime = HawkTime.getMillisecond();
		GlobalBuffAddMsg addMsg = new GlobalBuffAddMsg(buffId, currentTime, currentTime + buffCfg.getTime() * 1000L);
		HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_GLOBAL_BUFF), addMsg);
	}
}
