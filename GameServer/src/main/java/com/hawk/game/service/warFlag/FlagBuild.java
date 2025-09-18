package com.hawk.game.service.warFlag;

import java.util.Collection;

import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AddBannerEvent;
import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.LogConst.WarFlagOwnChangeType;

/**
 * 战旗状态：建造中
 * @author golden
 *
 */
public class FlagBuild implements FlagState {

	public IFlag flag;
	
	public FlagBuild(IFlag flag) {
		this.flag = flag;
	}
	
	@Override
	public void stateTick() {
		
		// 建造值是否达到最大
		int buildLife = getCurrBuildLife();
		if (buildLife < WarFlagConstProperty.getInstance().getMaxBuildLife(flag.isCenter())) {
			return;
		}
		
		// 解散行军
		WarFlagService.getInstance().dissolveFlagPointMarch(flag);

		// 通知旗帜变为完成状态
		notifyToDefence(flag, flag.getCurrentId());
		
		// 活动事件&Tlog
		postEnent();
		
		// 通知周边旗帜点状态刷新
		WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
		
		// 发邮件
		sendMail();
		
		// 发公告
		sendNotice();
		
		// 通知领地变更范围内玩家刷新领地buff
		GuildManorService.getInstance().notifyManorBuffChange(flag.getPointId());
		
		// 日志
		log();
		
		// 检测母旗数量
		WarFlagService.getInstance().checkCenterFlagCreate(flag.getOwnerId());
	}

	/**
	 * 日志
	 */
	private void log() {
		String guildName = GuildService.getInstance().getGuildName(flag.getCurrentId());
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		getLogger().info("flagBuildComplete, flagId:{}, owner:{}, curr:{}, posX:{}, posY:{}, guildName{}, placeTime:{}, state:{}, speed:{}, life:{}",
				flag.getFlagId(), flag.getOwnerId(), flag.getOwnerId(), pos[0], pos[1], guildName, flag.getPlaceTime(), flag.getState(), flag.getSpeed(), flag.getLife());
	}

	/**
	 * 发公告
	 */
	private void sendNotice() {
		String guildName = GuildService.getInstance().getGuildName(flag.getCurrentId());
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WAR_FLAG_BUILD_COMPLATE, null, guildName);
	}

	/**
	 * 活动时间事件&Tlog
	 */
	private void postEnent() {
		ActivityManager.getInstance().postEvent(new AddBannerEvent(flag.getOwnerId(), FlagCollection.getInstance().getGuildCompFlagCount(flag.getOwnerId())), true);
		LogUtil.logWarFlag(flag, flag.getOwnerId(), WarFlagOwnChangeType.PLACE);
	}

	/**
	 * 发邮件
	 */
	private void sendMail() {
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		
		if (!flag.isCenter()) {
			GuildMailService.getInstance().sendGuildMail(flag.getCurrentId(), MailParames.newBuilder()
					.setMailId(MailId.WAR_FLAG_BUILD_COMPLATE)
					.addContents(pos[0], pos[1]));
		} else {
			GuildMailService.getInstance().sendGuildMail(flag.getCurrentId(), MailParames.newBuilder()
					.setMailId(MailId.WAR_FLAG_BUILD_COMPLATE_GUILD)
					.addContents(pos[0], pos[1]));
		}
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
		flag.setSpeed(0.0d);
		
		// 设置母旗tick时间
		if (flag.isCenter()) {
			long calcCentNextTickTime = WarFlagService.getInstance().calcCentNextTickTime(flag);
			flag.setCenterNextTickTime(calcCentNextTickTime);
			
			int centerActiveCount = WarFlagService.getInstance().getCenterActiveCount();
			if (centerActiveCount < WarFlagConstProperty.getInstance().getBigFalgActiveCount()) {
				
				int[] flagPos = GameUtil.splitXAndY(flag.getPointId());
				int kingPointId = WorldMapConstProperty.getInstance().getCenterPointId();
				int[] kingPos = GameUtil.splitXAndY(kingPointId);
				
				int kingRadius = WorldMapConstProperty.getInstance().getKingPalaceRange()[0];
				int bigFlagRadius = WarFlagConstProperty.getInstance().getBigFlagRadius();
				
				if (Math.abs(kingPos[0] - flagPos[0]) + Math.abs(kingPos[1] - flagPos[1]) <= kingRadius + bigFlagRadius) {
					flag.setCenterAvtive(true);
				}
			}
		}
	}
	
	@Override
	public void resTick() {
		
	}

	@Override
	public boolean hasManor() {
		return false;
	}

	@Override
	public int getCurrBuildLife() {
		int life = flag.getLife();
		double speed = flag.getSpeed();
		long lastTickTime = flag.getLastBuildTick();
		long tickTime = HawkTime.getMillisecond() - lastTickTime;
		return life + (int)(speed * (tickTime / 1000));
	}

	@Override
	public int getCurrOccupyLife() {
		return 0;
	}

	@Override
	public void marchReturn() {
		flag.setLife(flag.getCurrBuildLife());
		flag.setLastBuildTick(HawkTime.getMillisecond());
		flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, false, true));
		
		Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		if (!marchs.isEmpty()) {
			long overTime = WarFlagService.getInstance().overTime(flag, false, true);
			for (IWorldMarch worldMarch : marchs) {
				worldMarch.getMarchEntity().setEndTime(overTime);
				worldMarch.updateMarch();
			}
		} else {
			flag.setState(FlageState.FLAG_PLACED_VALUE);
		}
		
		WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
	}

	@Override
	public void marchReach(String guildId) {
		Collection<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
		if (!marchs.isEmpty()) {
			double afterSpeed = WarFlagService.getInstance().getCurrentSpeed(flag, false, true);
			flag.setLife(flag.getCurrBuildLife());
			flag.setLastBuildTick(HawkTime.getMillisecond());
			flag.setSpeed(afterSpeed);
			long overTime = WarFlagService.getInstance().overTime(flag, false, true);
			for (IWorldMarch worldMarch : marchs) {
				worldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE);
				worldMarch.getMarchEntity().setEndTime(overTime);
				worldMarch.updateMarch();
			}
		} else {
			flag.setState(FlageState.FLAG_PLACED_VALUE);
		}
		WarFlagService.getInstance().notifyArroundPointFlagUpdate(flag);
	}

	@Override
	public boolean isBuildComplete() {
		return false;
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
		return false;
	}
}
