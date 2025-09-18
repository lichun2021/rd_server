package com.hawk.game.service.warFlag;

import java.util.Collection;

import org.hawk.os.HawkTime;

import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 战旗状态：防御中
 * @author golden
 *
 */
public class FlagDefend implements FlagState {
	
	public IFlag flag;
	
	public FlagDefend(IFlag flag) {
		this.flag = flag;
	}

	@Override
	public void stateTick() {

	}

	@Override
	public void resTick() {
		long period = WarFlagConstProperty.getInstance().getProductResourcePeriod();
		if (HawkTime.getMillisecond() - flag.getLastResourceTick() < period) {
			return;
		}
		flag.setLastResourceTick(HawkTime.getMillisecond());
		WarFlagService.getInstance().addFlagTickResource(flag, flag.getCurrentId());
	}

	@Override
	public boolean hasManor() {
		return true;
	}

	@Override
	public int getCurrBuildLife() {
		return WarFlagConstProperty.getInstance().getMaxBuildLife(flag.isCenter());
	}

	@Override
	public int getCurrOccupyLife() {
		return WarFlagConstProperty.getInstance().getFlagOccupy(flag.isCenter());
	}

	@Override
	public void marchReturn() {
		
	}

	@Override
	public void marchReach(String guildId) {
		if (!guildId.equals(flag.getCurrentId())) {
			flag.setState(FlageState.FLAG_BEINVADED_VALUE);
			flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, true, false));
			flag.setLastBuildTick(HawkTime.getMillisecond());
			flag.setRemoveTime(0L);
			
			long overTime = WarFlagService.getInstance().overTime(flag, true, false);
			
			Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
			for (IWorldMarch worldMarch : marchs) {
				worldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE);
				worldMarch.getMarchEntity().setEndTime(overTime);
				worldMarch.updateMarch();
			}
			
			WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
		} else {
			Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
			for (IWorldMarch worldMarch : marchs) {
				worldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE);
				worldMarch.updateMarch();
			}
			
		}
	}

	@Override
	public boolean isBuildComplete() {
		return true;
	}

	@Override
	public boolean isPlaced() {
		return true;
	}

	@Override
	public boolean canTakeBack() {
		return true;
	}

	@Override
	public boolean canPlace() {
		return false;
	}

	@Override
	public boolean canCenterTick() {
		return true;
	}
}
