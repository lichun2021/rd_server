package com.hawk.game.yuriStrikes;

import java.util.Objects;

import com.hawk.game.entity.QueueEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.protocol.YuriStrike.YuriStrikeInfo;

@YuriStrikeState(pbState = YuriState.CLEAN)
public class YuriStrikeStateClean implements IYuriStrikeState {

	@Override
	public YuriStrikeInfo.Builder toPBBuilder(Player player, YuriStrike obj) {
		QueueEntity queue = player.getData().getQueueEntity(obj.getDbEntity().getCleanQueueId());
		YuriStrikeInfo.Builder result = YuriStrikeInfo.newBuilder()
				.setState(pbState())
				.setYuriCfgId(obj.getDbEntity().getCfgId())
				.setCleanStart(queue.getStartTime())
				.setCleanEnd(queue.getEndTime());
		return result;
	}

	@Override
	public void login(Player player, YuriStrike obj) {
		QueueEntity queue = player.getData().getQueueEntity(obj.getDbEntity().getCleanQueueId());
		if (Objects.isNull(queue)) { // 预防没有收到消息
			this.cleanOver(player, obj);
		}
	}

	@Override
	public void cleanOver(Player player, YuriStrike obj) {
		obj.getDbEntity().setHasReward(1);
		obj.getDbEntity().setCleanQueueId("");
		obj.setState(player, IYuriStrikeState.valueOf(YuriState.CLEAN_OVER));

	}

}
