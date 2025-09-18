package com.hawk.game.yuriStrikes;

import java.util.Collections;

import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.service.QueueService;
import com.hawk.game.util.GsConst;

/**
 * 预备净化.
 * 
 * @author lwt
 * @date 2018年7月31日
 */
@YuriStrikeState(pbState = YuriState.PRE_CLEAN)
public class YuriStrikeStatePreClean implements IYuriStrikeState {

	@Override
	public void startClean(Player player, YuriStrike obj) {
		YuristrikeCfg cfg = obj.getCfg();
		long costtime = cfg.getPurifyTime() * 1000;
		QueueEntity queue = QueueService.getInstance().addReusableQueue(player, QueueType.YURISTRIKE_CLEAN_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE, "",
				0, costtime, Collections.emptyList(), GsConst.QueueReusage.YURISTRIKE_CLEAN);
		obj.getDbEntity().setCleanQueueId(queue.getId());
		obj.setState(player, IYuriStrikeState.valueOf(YuriState.CLEAN));
	}

}
