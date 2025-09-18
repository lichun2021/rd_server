package com.hawk.game.yuriStrikes;

import org.hawk.os.HawkTime;

import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.protocol.YuriStrike.YuriStrikeInfo;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

@YuriStrikeState(pbState = YuriState.SCANING)
public class YuriStrikeStateScaning implements IYuriStrikeState {

	@Override
	public YuriStrikeInfo.Builder toPBBuilder(Player player, YuriStrike obj) {
		YuristrikeCfg cfg = obj.getCfg();
		long marchTime = obj.getDbEntity().getMatchTime();
		YuriStrikeInfo.Builder result = YuriStrikeInfo.newBuilder()
				.setState(pbState())
				.setScanStar(marchTime - cfg.getScanningTime() * 1000)
				.setScanEnd(marchTime)
				.setYuriCfgId(obj.getDbEntity().getCfgId());
		return result;
	}

	@Override
	public void tick(Player player, YuriStrike obj) {
		if (HawkTime.getMillisecond() > obj.getDbEntity().getMatchTime()) { // 发启行军
			IWorldMarch march = WorldMarchService.getInstance().startYuriStrikeMarch(player, obj.getCfg());
			obj.getDbEntity().setMarchId(march.getMarchId());
			obj.setState(player, IYuriStrikeState.valueOf(YuriState.MARCH));
		}
	}

}
