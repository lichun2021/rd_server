package com.hawk.game.service.warFlag;

import java.util.Collection;

import org.hawk.os.HawkTime;

import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 战旗状态：损毁
 * @author golden
 *
 */
public class FlagDamaged implements FlagState {

	public IFlag flag;
	
	public FlagDamaged(IFlag flag) {
		this.flag = flag;
	}
	
	@Override
	public void stateTick() {
		
	}

	@Override
	public void resTick() {
		
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
		return flag.getOccupyLife();
	}

	@Override
	public void marchReturn() {
		
	}

	@Override
	public void marchReach(String guildId) {
		if (!guildId.equals(flag.getCurrentId())) {
			flag.setState(FlageState.FLAG_BEINVADED_VALUE);
			flag.setRemoveTime(0L);
			flag.setOccupyLife(getCurrOccupyLife());
			flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, true, false));
			flag.setLastBuildTick(HawkTime.getMillisecond());
			
			long overTime = WarFlagService.getInstance().overTime(flag, true, false);
			
			Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
			for (IWorldMarch worldMarch : marchs) {
				worldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE);
				worldMarch.getMarchEntity().setEndTime(overTime);
				worldMarch.updateMarch();
			}
			
			WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
		} else {
			flag.setState(FlageState.FLAG_FIX_VALUE);
			flag.setOccupyLife(getCurrOccupyLife());
			flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, false, false));
			flag.setLastBuildTick(HawkTime.getMillisecond());
			
			long overTime = WarFlagService.getInstance().overTime(flag, false, false);
			
			Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
			for (IWorldMarch worldMarch : marchs) {
				worldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE);
				worldMarch.getMarchEntity().setEndTime(overTime);
				worldMarch.updateMarch();
			}
			
			WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
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