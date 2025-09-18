package com.hawk.game.service;

import java.util.List;
import java.util.TreeSet;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.msg.ActivityDoubleRechargeMsg;
import com.hawk.activity.msg.ActivityStateChangeMsg;
import com.hawk.activity.msg.YuriRevengeStateChangeMsg;
import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.config.ActivityBillboard;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.YuriRevengeStageChangeMsgInvoker;
import com.hawk.game.msg.ImmigratePlayerMsg;
import com.hawk.game.msg.MigrateOutPlayerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityState;
import com.hawk.game.recharge.RechargeManager;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;

public class ActivityService extends HawkAppObj {

	private static ActivityService instance = null;

	public static Logger logger = LoggerFactory.getLogger("Server");
	
	public ActivityService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	public static ActivityService getInstance() {
		return instance;
	}
	
	@MessageHandler
	private void onDoubleRechargeChange(ActivityDoubleRechargeMsg msg){
		String playerId = msg.getPlayerId();
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		RechargeManager.getInstance().syncRechargeInfo(player);
	}
	
	
	/**
	 * 活动状态切换消息监听
	 * @param msg
	 */
	@MessageHandler
	public void handleActivityStateChange(ActivityStateChangeMsg msg) {
		switch (msg.getActivityType()) {
		case MACHINE_AWAKE_ACTIVITY:
			onMachineAwakeStateChange(msg.getState());
			break;
		case MACHINE_AWAKE_TWO_ACTIVITY:
			onMachineAwakeTwoStateChange(msg.getState());
			break;
		case CHRISTMAS_WAR_ACTIVITY:
			onCHristmasWarStateChange(msg.getState());
			break;
		default:
			break;
		}
	}

	private void onCHristmasWarStateChange(ActivityState state) {
		switch (state) {
		case END:
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHRISTMAS_REFRESH) {
				
				@Override
				public boolean onInvoke() {
					WorldChristmasWarService.getInstance().removeAllMonster();
					return true;
				}
			});
			break;
		default:
			break;
		}
		
	}

	/**
	 * 机甲觉醒活动状态切换
	 * @param activityState
	 */
	private void onMachineAwakeStateChange(ActivityState activityState) {
		switch (activityState) {
		case END:
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.GUNDAM_REFRESH) {
				@Override
				public boolean onInvoke() {
					WorldGundamService.getInstance().removeAllGundam();
					HawkLog.logPrintln("MACHINE_AWAKE_ACTIVITY on end remove boss");
					return true;
				}
			});
			break;
	
		default:
			break;
		}
		
	}
	
	/**
	 * 机甲觉醒2(年兽)活动状态切换
	 * @param activityState
	 */
	private void onMachineAwakeTwoStateChange(ActivityState activityState) {
		switch (activityState) {
		case END:
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NIAN_REFRESH) {
				@Override
				public boolean onInvoke() {
					WorldNianService.getInstance().removeAllNian();
					HawkLog.logPrintln("MACHINE_AWAKE_TWO_ACTIVITY on end remove boss");
					return true;
				}
			});
			break;
			
		default:
			break;
		}
		
	}

	/**
	 * 尤里复仇活动状态切换
	 * @param msg
	 */
	@MessageHandler
	public void handleRevengeStateChange(YuriRevengeStateChangeMsg msg) {
		ActivityState state = msg.getState();
		int termId = msg.getTermId();
		YuriRevengeService.getInstance().dealMsg(MsgId.YUNI_REVENGE_STATE_CHANGE, new YuriRevengeStageChangeMsgInvoker(state, termId));
	}
	
	
	@MessageHandler
	private void migrateOoutPlayer(MigrateOutPlayerMsg msg){
		msg.setResult(Boolean.FALSE);
		
		String playerId = msg.getPlayer().getId();
		ActivityManager.getInstance().onPlayerMigrate(playerId);
		
		msg.setResult(Boolean.TRUE);
	}
	
	@MessageHandler
	private void immigratePlayer(ImmigratePlayerMsg msg){
		msg.setResult(Boolean.FALSE);
		
		String playerId = msg.getPlayerId();		
		ActivityManager.getInstance().onImmigrateInPlayer(playerId);
		
		msg.setResult(Boolean.TRUE);
	}
	
	/**
	 * 获取活动公告
	 * @return
	 */
	public JSONArray getActivityBillboard(String playerId) {
		// 需要显示的活动公告
		JSONArray showBillboard = new JSONArray();
		try {
			// 需要显示公告活动id列表
			List<Integer> showActivity = ActivityManager.getInstance().getBillBoardActivitys(playerId);
			
			TreeSet<ActivityBillboard> shwBillboardTree = new TreeSet<>();
			for (Integer activityId : showActivity) {
				ActivityBillboard activityBillBoard = HawkConfigManager.getInstance().getConfigByKey(ActivityBillboard.class, activityId);
				if (activityBillBoard == null) {
					logger.error("activityBillBoard is null, activityId:{}", activityId);
					continue;
				}
				shwBillboardTree.add(activityBillBoard);
			}
			
			for (ActivityBillboard billboard : shwBillboardTree) {
				showBillboard.add(billboard.toShow());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return showBillboard;
	}
}
