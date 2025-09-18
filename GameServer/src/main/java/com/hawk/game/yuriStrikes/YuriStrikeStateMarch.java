package com.hawk.game.yuriStrikes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.hawk.os.HawkTime;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.protocol.YuriStrike.YuriStrikeInfo;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

@YuriStrikeState(pbState = YuriState.MARCH)
public class YuriStrikeStateMarch implements IYuriStrikeState {

	@Override
	public YuriStrikeInfo.Builder toPBBuilder(Player player, YuriStrike obj) {
		IWorldMarch march = WorldMarchService.getInstance().getMarch(obj.getDbEntity().getMarchId());
		if (Objects.isNull(march)) {
			this.yuriMarchReach(player, obj);
			return YuriStrikeInfo.newBuilder();
		}
		YuriStrikeInfo.Builder result = YuriStrikeInfo.newBuilder()
				.setState(pbState())
				.setYuriMarchId(obj.getDbEntity().getMarchId())
				.setYuriCfgId(obj.getDbEntity().getCfgId())
				.setYuriMarchStart(march.getMarchEntity().getStartTime())
				.setYuriMarchEnd(march.getMarchEntity().getEndTime());

		return result;
	}

	@Override
	public void login(Player player, YuriStrike obj) {
		IWorldMarch march = WorldMarchService.getInstance().getMarch(obj.getDbEntity().getMarchId());
		if (Objects.isNull(march)) {
			this.yuriMarchReach(player, obj);
		}
	}

	@Override
	public void yuriMarchReach(Player player, YuriStrike obj) {
		List<Integer> unlockedAreas = new ArrayList<>(player.getData().getPlayerBaseEntity().getUnlockedAreaSet());
		unlockedAreas.sort(Comparator.comparingInt(Integer::intValue));
		int areaId = obj.getDbEntity().getAreaIdLock();
		if (areaId == 0) {
			areaId = unlockedAreas.get(0);
		}
		for (int id : unlockedAreas) {
			if (id > areaId) {
				break;
			}
		}

		obj.getDbEntity().setAreaIdLock(areaId);
		obj.setState(player, IYuriStrikeState.valueOf(YuriState.YURI_HOLD));
	}

	@Override
	public void moveCity(Player player, YuriStrike obj) {
		// 预警 推客户端
		obj.getDbEntity().setMatchTime(HawkTime.getMillisecond() + obj.getCfg().getScanningTime() * 1000);
		obj.setState(player, IYuriStrikeState.valueOf(YuriState.SCANING));
	}

}
