package com.hawk.game.service;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.timer.HawkTimerEntry;
import org.hawk.timer.HawkTimerListener;
import org.hawk.timer.HawkTimerManager;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsApp;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.msg.CrossDayBeforeZeroMsg;
import com.hawk.game.msg.TimerEventMsg;
import com.hawk.game.nation.NationService;
import com.hawk.game.player.Player;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.TimerEventEnum;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

public class GlobalTimerService extends HawkAppObj {
	
	private static GlobalTimerService instance = null; 
	public GlobalTimerService(HawkXID xid) {
		super(xid);
		
		instance = this;
	}
	
	public static GlobalTimerService getInstance() {
		return instance;
	}
	
	@MessageHandler
	private void onTimerEventMessage(TimerEventMsg timeEventMsg){
		switch(timeEventMsg.getEventEnum()) {
		case ZERO_CLOCK:
			doZeroClockEvent();
			break;
		case THREEE_CLOCK:
			doThreeClockEvent();
			break;
		case FOUR_CLOCK:
			doFourClockEvent();
			break;
		case FIVE_CLOCK:
			doFiveClockEvent();
			break; 
		default:
			break;
		}
	}
	
	private void doThreeClockEvent() {
		RelationService.getInstance().logStatistics();		
	}

	@MessageHandler
	private void onCrossDayBeforeZeroMessage(CrossDayBeforeZeroMsg msg) {
		Set<Player> playerSet = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : playerSet) {
			CrossDayBeforeZeroMsg playerEventMsg = new CrossDayBeforeZeroMsg();
			HawkApp.getInstance().postMsg(player.getXid(), playerEventMsg);
		}
	}
	
	/**
	 * 多个事件之间的调用，尽量互不影响 用catch包住
	 */
	private void doZeroClockEvent() {
		//更新联盟清理资源点次数
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.RESET_CLEAR_RESOURCE_NUM) {
			@Override
			public boolean onInvoke() {
				GuildService.getInstance().resetClearResNum();
				return false;
			}
		});
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_BUILDING_RESET) {
			@Override
			public boolean onInvoke() {
				// 检查国家是否开启
				NationService.getInstance().checkOpenTime();
				// 重置国家建筑重建值
				NationService.getInstance().resetNationBuildVal();
				// 重置飞船每日上限
				NationService.getInstance().resetShipAssistLimit();
				return true;
			}
		});
		
		// 服务器统计日志
		GameUtil.dailyLog();
		
		// 排行榜信息上报
		GameUtil.dailyRankLog(false);
	}
	
	private void doFourClockEvent() {
		ScheduleService.getInstance().removeScheduleDaily();
	}

	/**
	 * 多个事件之间的调用，尽量互不影响 用catch包住
	 */
	private void doFiveClockEvent() {
		//long now = HawkTime.getMillisecond();
		//线上个别服磁盘空间吃紧，暂且不执行这个操作了，避免影响服务器运行
		//SysOpService.getInstance().grepErrorLog(now - HawkTime.DAY_MILLI_SECONDS, now);
	}

	public void init() {
		// 玩家的每日事件调度
		TimerEventEnum[] enums = TimerEventEnum.values();
		for (int i = 0; i < enums.length; i++) {
			for (int j = i + 1; j < enums.length; j++) {
				if (enums[i].getClock() == enums[j].getClock()) {
					throw new InvalidParameterException("PlayerTimeEventEnum clock dumplicate");
				}
			}
		}
		
		for (TimerEventEnum eventEnum : enums) {
			try {
				HawkTimerManager.getInstance().addAlarm("TimeEvent-" + eventEnum.name(), new int[] { 1 },
						new int[] { 0 }, new int[] { eventEnum.getClock() }, new int[] { -1 }, new int[] { -1 },
						new HawkTimerListener() {
							@Override
							protected void handleAlarm(HawkTimerEntry entry) {
								TimerEventMsg eventMsg = TimerEventMsg.valueOf(eventEnum);
								GsApp.getInstance().postMsg(GlobalTimerService.getInstance().getXid(), eventMsg);
								
								if (eventEnum.isNoticePlayer()) {
									Set<Player> playerSet = GlobalData.getInstance().getOnlinePlayers();
									for (Player player : playerSet) {
										TimerEventMsg playerEventMsg = TimerEventMsg.valueOf(eventEnum);
										HawkApp.getInstance().postMsg(player.getXid(), playerEventMsg);
									}
								}
								
							}
						});
			} catch (HawkException e) {
				HawkException.catchException(e);
			}
		}
		
		//刷新超值礼包,保证每天的礼包重置是在礼包刷新的前面
		try {
			HawkTimerManager.getInstance().addAlarm("crossDayBeforeZero", new int[]{1}, new int[]{55}, new int[]{23}, new int[]{-1}, new int[]{-1},
					new HawkTimerListener() {
						
						@Override
						protected void handleAlarm(HawkTimerEntry entry) {
							CrossDayBeforeZeroMsg msg = new CrossDayBeforeZeroMsg();
							HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TIMER), msg);
						}
					});
		} catch (HawkException e) {
			HawkException.catchException(e, "init crossDayBeforeZero error");
		}

		// 世界区块更新
		try {
			int areaUpdateClock = WorldMapConstProperty.getInstance().getAreaUpdateClock();
			long refreshTime = WorldMapConstProperty.getInstance().getAreaUpdateRefreshTime();
			HawkTimerManager.getInstance().addAlarm("AreaUpdateTimer-" + areaUpdateClock, new int[] { 1 }, new int[] { 0 }, new int[] { areaUpdateClock }, new int[] { -1 }, new int[] { -1 },
					new HawkTimerListener() {
						@Override
						protected void handleAlarm(HawkTimerEntry entry) {
							for (AreaObject area : WorldPointService.getInstance().getAreaVales()) {
								long delayTime = refreshTime / WorldPointService.getInstance().getAreaSize() * area.getId();
								WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.AREA_UPDATE, delayTime, delayTime, 1) {
									@Override
									public boolean onInvoke() {
										WorldPointService.getInstance().notifyAreaUpdate(area, 0);
										return true;
									}
								});
							}
						}
					});
		} catch (HawkException e) {
			HawkException.catchException(e, "init AreaUpdateTimer error");
		}
		
		// 世界机甲
		List<Integer> gundamRefreshArr = WorldMapConstProperty.getInstance().getGoundamRefreshTimeArr();
		for (int refreshClock : gundamRefreshArr) {
			try {
				HawkTimerManager.getInstance().addAlarm("GundamRefresh-" + refreshClock, new int[] { 1 },
						new int[] { 0 }, new int[] { refreshClock }, new int[] { -1 }, new int[] { -1 },
						new HawkTimerListener() {
							@Override
							protected void handleAlarm(HawkTimerEntry entry) {
								WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.GUNDAM_REFRESH) {
									@Override
									public boolean onInvoke() {
										WorldGundamService.getInstance().refreshGundam();
										return true;
									}
								});
							}
						});
			} catch (HawkException e) {
				HawkException.catchException(e, "init GundamRefresh");
			}
		}
		
		// 年兽
		List<Integer> nianRefreshArr = WorldMapConstProperty.getInstance().getNianRefreshTimeArr();
		for (int refreshClock : nianRefreshArr) {
			try {
				HawkTimerManager.getInstance().addAlarm("NianRefresh-" + refreshClock, new int[] { 1 },
						new int[] { 0 }, new int[] { refreshClock }, new int[] { -1 }, new int[] { -1 },
						new HawkTimerListener() {
							@Override
							protected void handleAlarm(HawkTimerEntry entry) {
								WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NIAN_REFRESH) {
									@Override
									public boolean onInvoke() {
										WorldNianService.getInstance().refreshNian();
										return true;
									}
								});
							}
						});
			} catch (HawkException e) {
				HawkException.catchException(e, "init NianRefresh");
			}
		}
		
		//圣诞boss
		List<Integer> christmasRefreshArr = WorldMapConstProperty.getInstance().getChristmasRefreshTimeList();
		for (int refreshClock : christmasRefreshArr) {
			try {
				HawkTimerManager.getInstance().addAlarm("ChristmaRefresh-" + refreshClock, new int[] { 1 },
						new int[] { 0 }, new int[] { refreshClock }, new int[] { -1 }, new int[] { -1 },
						new HawkTimerListener() {
							@Override
							protected void handleAlarm(HawkTimerEntry entry) {
								WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHRISTMAS_REFRESH) {
									@Override
									public boolean onInvoke() {
										WorldChristmasWarService.getInstance().refreshBoss();
										
										return true;
									}
								});
							}
						});
			} catch (HawkException e) {
				HawkException.catchException(e, "init christmas refresh");
			}
		}
	}
}
