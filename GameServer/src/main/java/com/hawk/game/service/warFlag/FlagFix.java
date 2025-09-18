package com.hawk.game.service.warFlag;

import java.util.Collection;
import org.hawk.os.HawkTime;

import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 战旗状态：修复中
 * @author golden
 *
 */
public class FlagFix implements FlagState {

	public IFlag flag;
	
	public FlagFix(IFlag flag) {
		this.flag = flag;
	}
	
	@Override
	public void stateTick() {

		// 建造值是否达到最大
		int buildLife = getCurrOccupyLife();
		if (buildLife < WarFlagConstProperty.getInstance().getFlagOccupy(flag.isCenter())) {
			return;
		}
		
		// 解散行军
		WarFlagService.getInstance().dissolveFlagPointMarch(flag);

		// 通知旗帜变为完成状态
		notifyToDefence(flag, flag.getCurrentId());
		
		// 通知周边旗帜点状态刷新
		WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
		
		// 通知领地变更范围内玩家刷新领地buff
		GuildManorService.getInstance().notifyManorBuffChange(flag.getPointId());
		
		// 日志
		log();
	}

	/**
	 * 日志
	 */
	private void log() {
		String guildName = GuildService.getInstance().getGuildName(flag.getCurrentId());
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		getLogger().info("flagFixComplete, flagId:{}, owner:{}, curr:{}, posX:{}, posY:{}, guildName{}, placeTime:{}, state:{}, speed:{}, life:{}",
				flag.getFlagId(), flag.getOwnerId(), flag.getOwnerId(), pos[0], pos[1], guildName, flag.getPlaceTime(), flag.getState(), flag.getSpeed(), flag.getLife());
	}
	
	/**
	 * 通知旗帜变为完成状态
	 */
	public void notifyToDefence(IFlag flag, String currGuildId) {
		flag.setCurrentId(currGuildId);
		flag.setState(FlageState.FLAG_DEFEND_VALUE);
		flag.setLife(WarFlagConstProperty.getInstance().getMaxBuildLife(flag.isCenter()));
		flag.setOccupyLife(WarFlagConstProperty.getInstance().getFlagOccupy(flag.isCenter()));
		flag.setLastBuildTick(HawkTime.getMillisecond());
		flag.setCompleteTime(HawkTime.getMillisecond());
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
		int life = flag.getOccupyLife();
		double speed = flag.getSpeed();
		long lastTickTime = flag.getLastBuildTick();
		long tickTime = HawkTime.getMillisecond() - lastTickTime;
		return life + (int)(speed * (tickTime / 1000));
	}

	@Override
	public void marchReturn() {
		flag.setOccupyLife(getCurrOccupyLife());
		flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, false, false));
		flag.setLastBuildTick(HawkTime.getMillisecond());
		flag.setState(FlageState.FLAG_FIX_VALUE);
		
		Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		if (!marchs.isEmpty()) {
			long overTime = WarFlagService.getInstance().overTime(flag, false, false);
			for (IWorldMarch worldMarch : marchs) {
				worldMarch.getMarchEntity().setEndTime(overTime);
				worldMarch.updateMarch();
			}
		} else {
			flag.setState(FlageState.FLAG_DAMAGED_VALUE);
		}
		
		WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
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
