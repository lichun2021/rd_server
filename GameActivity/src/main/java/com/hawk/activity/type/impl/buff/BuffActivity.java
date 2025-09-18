package com.hawk.activity.type.impl.buff;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.buff.cfg.ActivityBuffCfg;
import com.hawk.msg.GlobalBuffAddMsg;
import com.hawk.msg.GlobalBuffRemoveMsg;

public class BuffActivity extends ActivityBase {

	public BuffActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
		this.reset();
	}

	/**
	 * 当前阶段.对应ActivityBuffCfg 里面的stageBuffList stageTimeList的下标
	 */
	private volatile int stage;
	/**
	 * 当前阶段的状态
	 */
	private volatile boolean isOpen;
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.BUFF_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new BuffActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {

	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	@Override
	public void onOpen() {
		this.reset();
	}

	@Override
	public void onEnd() {
		forceCloseCurStage();
		this.reset();
	}

	private void reset() {
		this.stage = -1;
		this.isOpen = false;
	}

	@Override
	public void onTick() {
		//只有在活动开启的情况下进行tick
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		
		int termId = getActivityTermId();
		ActivityBuffCfg activityBufCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityBuffCfg.class, termId);
		if (activityBufCfg == null) {
			return;
		}

		if (isOpen) {
			tryCloseCurStage(activityBufCfg, this.stage);
			// 关闭失败的时候说明还处在当前阶段.
			if (isOpen) {
				return;
			}
		} else {
			int nextStage = this.stage + 1;
			boolean needCheckNextStage = this.tryOpenStage(activityBufCfg, nextStage);
			while(needCheckNextStage) {
				nextStage = nextStage + 1;
				if (nextStage >= 0 && nextStage < activityBufCfg.getStageTimeList().size()) {
					this.tryOpenStage(activityBufCfg, nextStage);
				} else {
					break;
				}
			}			
		}		

	}

	/**
	 * 
	 * @param activityBufCfg
	 * @param curStage
	 * @return need check nextStage
	 */
	private boolean tryOpenStage(ActivityBuffCfg activityBufCfg, int curStage) {
		long now = HawkTime.getMillisecond();
		List<Integer> timeList = activityBufCfg.getStageTimeList().get(curStage);
		int termId = getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		if (startTime + timeList.get(0) <= now && now < startTime + timeList.get(1)) {
			this.isOpen = true;
			this.stage = curStage;
			GlobalBuffAddMsg addMsg = new GlobalBuffAddMsg(
					activityBufCfg.getStageBuffList().get(curStage), startTime + timeList.get(0), startTime +timeList.get(1));
			HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_GLOBAL_BUFF), addMsg);
			logger.info("buffActivity openStage curStage:{}, termId:{}", curStage, termId);
			return false;
		} else {
			if (now > startTime + timeList.get(1)) {
				return true;
			}
			
			return false;
		}		
	}

	private void tryCloseCurStage(ActivityBuffCfg activityBufCfg, int curStage) {
		long now = HawkTime.getMillisecond();
		List<Integer> timeList = activityBufCfg.getStageTimeList().get(curStage);
		int termId = getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		if (startTime + timeList.get(1) < now) {
			this.isOpen = false;
			GlobalBuffRemoveMsg removeMsg = new GlobalBuffRemoveMsg(activityBufCfg.getStageBuffList().get(curStage));
			HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_GLOBAL_BUFF), removeMsg);
			
			logger.info("buffActivity closeStage curStage:{}, termId:{}", curStage, termId);
		}
	}
	
	private void forceCloseCurStage() {
		int termId = this.getActivityEntity().getTermId();
		ActivityBuffCfg activityBufCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityBuffCfg.class, termId);
		if (this.isOpen) {
			List<Integer> buffIdList = activityBufCfg.getStageBuffList().get(stage);
			GlobalBuffRemoveMsg removeMsg = new GlobalBuffRemoveMsg(buffIdList);
			HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_GLOBAL_BUFF), removeMsg);
			
			logger.info("buffActivity forcecloseStage curStage:{}, termId:{}, buffIdList:{}", stage, termId, buffIdList);
		} 
	}
}
