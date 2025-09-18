package com.hawk.game.service.warFlag;

import java.util.Collection;
import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AddBannerEvent;
import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst.WarFlagOwnChangeType;

/**
 * 战旗状态：占领中
 * @author golden
 *
 */
public class FlagBeinvaded implements FlagState {

	public IFlag flag;
	
	public FlagBeinvaded(IFlag flag) {
		this.flag = flag;
	}
	
	@Override
	public void stateTick() {
		
		// 占领值值是否为0
		int occupyLife = getCurrOccupyLife();
		if (occupyLife > 0) {
			return;
		}

		// 更换前联盟id
		String oldGuildId = flag.getCurrentId();
		
		String afterGuildId = "";
		List<IWorldMarch> flagPointMarch = WarFlagService.getInstance().getFlagPointMarch(flag);
		if (flagPointMarch != null && !flagPointMarch.isEmpty()) {
			afterGuildId = flagPointMarch.get(0).getPlayer().getGuildId();
		}
		
		// 发母旗奖励
		WarFlagService.getInstance().sendCenterFlagAward(flag);
		
		// 解散行军
		WarFlagService.getInstance().dissolveFlagPointMarch(flag);

		WarFlagService.getInstance().rmFlagTargetMarch(flag);
		
		// 删除点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point != null && point.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			LogUtil.logWarFlag(flag, flag.getCurrentId(), WarFlagOwnChangeType.DESTROY, point.getZoneId());
			WorldPointService.getInstance().removeWorldPoint(flag.getPointId());
		}

		// 设置已解锁
		flag.setState(FlageState.FLAG_UNLOCKED_VALUE);
		flag.setCompleteTime(0);
		flag.setLife(0);
		flag.clearSignUpInfo();
		
		// 通知周边旗帜点状态刷新
		WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
		
		// 拆旗子，隔壁方回行军
		List<IFlag> arroundCompFlags = WarFlagService.getInstance().getArroundCompFlags(flag.getPointId(), flag.isCenter());
		for (IFlag arrFlag : arroundCompFlags) {
			if (!WarFlagService.getInstance().inOtherManorGuildIds(flag.getCurrentId(), flag.getPointId(), flag.isCenter()).isEmpty()) {
				continue;
			}
			WarFlagService.getInstance().dissolveFlagPointMarch(arrFlag);
		}
			
		// 联盟旗帜添加事件
		postEvent(oldGuildId);

		// 发邮件
		sendMail(afterGuildId);

		// 发公告
		sendNotice(afterGuildId);

		// 通知领地变更范围内玩家刷新领地buff
		GuildManorService.getInstance().notifyManorBuffChange(flag.getPointId());

		// 日志
		log(oldGuildId);
	}

	/**
	 * 日志
	 */
	private void log(String oldGuildId) {
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		String atkGuildName = GuildService.getInstance().getGuildName(flag.getCurrentId());
		String defGuildName = GuildService.getInstance().getGuildName(oldGuildId);
		getLogger().info(
				"flagDestroyComplete, flagId:{}, owner:{}, curr:{}, posX:{}, posY:{}, placeTime:{}, state:{}, speed:{}, life:{}, oldGuildId:{}, atkGuildName:{}, defGuildName:{}",
				flag.getFlagId(), flag.getOwnerId(), flag.getOwnerId(), pos[0], pos[1], flag.getPlaceTime(), flag.getState(), flag.getSpeed(), flag.getLife(), oldGuildId,
				atkGuildName, defGuildName);
	}
	
	/**
	 * 抛活动事件&Tlog
	 */
	private void postEvent(String oldGuildId) {
		ActivityManager.getInstance().postEvent(new AddBannerEvent(oldGuildId, FlagCollection.getInstance().getGuildCompFlagCount(oldGuildId)), true);
		ActivityManager.getInstance().postEvent(new AddBannerEvent(flag.getCurrentId(), FlagCollection.getInstance().getGuildCompFlagCount(flag.getCurrentId())), true);
		LogUtil.logWarFlag(flag, oldGuildId, WarFlagOwnChangeType.LOSE);
	}

	/**
	 * 发送邮件
	 */
	private void sendMail(String afterGuildId) {
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		Player atkPlayer = null;
		List<IWorldMarch> march = WarFlagService.getInstance().getFlagPointMarch(flag);
		if (!march.isEmpty()) {
			atkPlayer = march.get(0).getPlayer();
		}
		
		if (!flag.isCenter()) {
			
			GuildMailService.getInstance().sendGuildMail(flag.getOwnerId(), MailParames.newBuilder()
					.setMailId(MailId.WAR_FLAG_BE_OCCUPY)
					.addContents(pos[0], pos[1], GuildService.getInstance().getGuildName(afterGuildId), atkPlayer == null ? "" : atkPlayer.getName()));
		} else {
			GuildMailService.getInstance().sendGuildMail(flag.getOwnerId(), MailParames.newBuilder()
					.setMailId(MailId.WAR_FLAG_BE_OCCUPY_GUILD)
					.addContents(pos[0], pos[1], GuildService.getInstance().getGuildName(afterGuildId), atkPlayer == null ? "" : atkPlayer.getName()));			
		}
	}
	
	/**
	 * 发公告
	 */
	private void sendNotice(String afterGuildId) {
		String atkGuildName = GuildService.getInstance().getGuildName(afterGuildId);
		String defGuildName = GuildService.getInstance().getGuildName(flag.getOwnerId());
		if (flag.getCurrentId().equals(flag.getOwnerId())) {
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WAR_FLAG_TAKE_BACK, null, atkGuildName, defGuildName);
		} else {
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WAR_FLAG_BE_OCCUPY_NOTICE, null, atkGuildName, defGuildName);
		}
	}
	
	/**
	 * 通知旗帜变为完成状态
	 */
	public void notifyFlagToComplete(IFlag flag, String currGuildId) {
		flag.setCurrentId(currGuildId);
		flag.setState(FlageState.FLAG_DEFEND_VALUE);
		flag.setLife(WarFlagConstProperty.getInstance().getMaxBuildLife(flag.isCenter()));
		flag.setOccupyLife(WarFlagConstProperty.getInstance().getFlagOccupy(flag.isCenter()));
		flag.setLastBuildTick(HawkTime.getMillisecond());
		flag.setCompleteTime(HawkTime.getMillisecond());
		WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
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
		return life - (int)(speed * (tickTime / 1000));
	}

	@Override
	public void marchReturn() {
		flag.setOccupyLife(getCurrOccupyLife());
		flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, true, false));
		flag.setLastBuildTick(HawkTime.getMillisecond());
		
		Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		if (!marchs.isEmpty()) {
			long overTime = WarFlagService.getInstance().overTime(flag, true, false);
			for (IWorldMarch worldMarch : marchs) {
				if (worldMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE) {
					continue;
				}
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
			flag.setOccupyLife(getCurrOccupyLife());
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
			flag.setState(FlageState.FLAG_FIX_VALUE);
			flag.setOccupyLife(getCurrOccupyLife());
			flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, false, false));
			flag.setLastBuildTick(HawkTime.getMillisecond());
			
			long overTime =  WarFlagService.getInstance().overTime(flag, false, false);
			
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
		return false;
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
