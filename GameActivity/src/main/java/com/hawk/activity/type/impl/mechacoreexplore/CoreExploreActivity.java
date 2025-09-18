package com.hawk.activity.type.impl.mechacoreexplore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreConstCfg;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreItemCfg;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreRewardCfg;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreShopCfg;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreTechCfg;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreZoneCfg;
import com.hawk.activity.type.impl.mechacoreexplore.entity.BoxItem;
import com.hawk.activity.type.impl.mechacoreexplore.entity.CoreExploreEntity;
import com.hawk.activity.type.impl.mechacoreexplore.entity.PickItem;
import com.hawk.activity.type.impl.mechacoreexplore.entity.SpecialItem;
import com.hawk.activity.type.impl.mechacoreexplore.entity.StoneItem;
import com.hawk.game.protocol.Activity.AutoPickResp;
import com.hawk.game.protocol.Activity.CEObstacleRemoveType;
import com.hawk.game.protocol.Activity.CEObstacleType;
import com.hawk.game.protocol.Activity.CERemoveObstacleResp;
import com.hawk.game.protocol.Activity.CEZoneLinePB;
import com.hawk.game.protocol.Activity.CoreExploreActivityInfo;
import com.hawk.game.protocol.Activity.PWBShopInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

/***
 * （机甲）核心勘探活动
 * 
 * @author lating
 */
public class CoreExploreActivity extends ActivityBase implements IExchangeTip<CoreExploreShopCfg> {
	/**
	 * 第一列和最后一列的序号
	 */
	private static final int first_column = 1;
	private static final int last_column  = 6;
	/**
	 * 可见行数
	 */
	private static final int view_line_count = 7;
	/**
	 * 隐藏的行数
	 */
	private static final int hide_line_count = 2;
	
	
	public CoreExploreActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.MECHA_CORE_EXPLORE;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CoreExploreActivity activity = new CoreExploreActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CoreExploreEntity> queryList = HawkDBManager.getInstance().query("from CoreExploreEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CoreExploreEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CoreExploreEntity entity = new CoreExploreEntity(playerId, termId);
		return entity;
	}

	 /**
     * 判断活动是否开启
     */
    public boolean isActivityClose(String playerId) {
    	int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
    	int vipLevel = this.getDataGeter().getVipLevel(playerId);
    	if (cityLevel < CoreExploreConstCfg.getInstance().getBaseLimit() 
    		|| vipLevel < CoreExploreConstCfg.getInstance().getVipLimit()) {
    		return true;
    	}
    	
    	return false;
    }
    
	@Override
	public void onOpen() {
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			if (!isOpening(playerId)) {
				continue;
			}
			callBack(playerId, GameConst.MsgId.CORE_EXPLORE_INIT, ()->{
				initActivityData(playerId, null);
				pushActivityInfo(playerId);
			});
		}
	}
	
	@Override
	public void onEnd() {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				Set<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
				for (String playerId : onlinePlayerIds) {
					clearConsumeItems(playerId);
				} 
				return null;
			}
		});
	}
	
	/**
	 * 活动结束清空消耗类道具（免费矿镐、炸弹、钻机、矿石）
	 * @param playerId
	 */
	private void clearConsumeItems(String playerId) {
		try {
			if(this.getDataGeter().isPlayerCrossIngorePlayerObj(playerId)) {
				return;
			}
			
			List<RewardItem.Builder> consumeList = new ArrayList<>();
			for (int itemId : CoreExploreConstCfg.getInstance().getClearItemList()) {
				int itemCount = getDataGeter().getItemNum(playerId, itemId);
				if (itemCount > 0) {
					RewardItem.Builder consumeItem = RewardHelper.toRewardItem(30000, itemId, itemCount);
					consumeList.add(consumeItem);
				}
			}
			
			if (!consumeList.isEmpty()) {
				this.getDataGeter().consumeItems(playerId, consumeList, 0, Action.CORE_EXPLORE_END_CLEAR);
			}
		} catch (Exception e) {
			HawkException.catchException(e, playerId);
		}
	}
	
	/**
	 * 初始化数据
	 * @param playerId
	 */
	private boolean initActivityData(String playerId, CoreExploreEntity entity) {
		if (entity == null) {
			Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return false;
			}
			entity = opEntity.get();
		}
		if (!entity.getLineList().isEmpty()) {
			return false;
		}
		
		clearConsumeItems(playerId);
		
		CoreExploreConstCfg constCfg = CoreExploreConstCfg.getInstance();
		List<RewardItem.Builder> rewardItems = new ArrayList<>();
		if (constCfg.getFreeBoomNum() > 0) {
			RewardItem.Builder rewardItem = RewardHelper.toRewardItem(30000, constCfg.getBoomItemId(), constCfg.getFreeBoomNum());
			rewardItems.add(rewardItem);
		}
		if (constCfg.getFreeRigNum() > 0) {
			RewardItem.Builder rewardItem = RewardHelper.toRewardItem(30000, constCfg.getRigItemId(), constCfg.getFreeRigNum());
			rewardItems.add(rewardItem);
		}
		if (!rewardItems.isEmpty()) {
			this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.CORE_EXPLORE_SEND_OPEN, false);
		}
		
		entity.getFreePickData().setLastSendTime(HawkTime.getMillisecond());
		entity.setCurrLine(view_line_count);
		for (int line = 1; line <= entity.getCurrLine() + hide_line_count; line++) {
			newLineData(entity, line, false);
		}
		entity.notifyUpdate();
		CoreExploreHelper.refreshConnectedData(entity);
		return true;
	}
	
	/**
	 * 移除现有行数据，同时生成新一行数据
	 * @param entity
	 * @param newLineForce
	 * @param builder
	 */
	private void removeAndNewLine(CoreExploreEntity entity, boolean newLineForce, CERemoveObstacleResp.Builder builder) {
		removeLine(entity, builder);
		List<Integer> list = newLineData(entity, newLineForce);
		entity.currLineInc();
		HawkLog.logPrintln("CoreExploreActivity new line, playerId: {}, currLine: {}, line count: {}, newline: {}", entity.getPlayerId(), entity.getCurrLine(), entity.getLineList().size(), list);
	}
	
	/**
	 * 生成新的一行数据
	 * @param entity
	 * @param force
	 * @return
	 */
	private List<Integer> newLineData(CoreExploreEntity entity, boolean force) {
		int newLine = entity.getTotalLine() + 1;
		return newLineData(entity, newLine, force);
	}
	
	/**
	 * 生成新的一行数据
	 * @param entity
	 * @param newLine
	 */
	private List<Integer> newLineData(CoreExploreEntity entity, int newLine, boolean force) {
		List<Integer> columnList = new ArrayList<>();
		entity.addNewLine(columnList);
		CoreExploreZoneCfg zoneCfg = CoreExploreZoneCfg.getConfig(force ? 0 : newLine);
		for (int column = first_column; column <= last_column; column++) {
			List<Integer> obstacleList = zoneCfg.getColumnObstacleList(column);
			List<Integer> weightList = zoneCfg.getColumnWeightList(column);
			int clickTimes = 0, obstacle = HawkRand.randomWeightObject(obstacleList, weightList);
			columnList.add(obstacle);
			if (obstacle == CEObstacleType.CE_BOX_VALUE) {
				BoxItem box = BoxItem.valueOf(newLine, column, CoreExploreConstCfg.getInstance().getBoxClickRandTimes());
				entity.addBox(box);
				clickTimes = box.getTotalTimes();
			}
			if (obstacle == CEObstacleType.CE_STONE_VALUE || obstacle == CEObstacleType.CE_STONE_ORE_VALUE) {
				StoneItem stone = StoneItem.valueOf(newLine, column);
				entity.addStone(stone);
			}
			if (obstacle == CEObstacleType.CE_SANDY_SOIL_ITEM_VALUE) {
				String itemStr = CoreExploreItemCfg.getRewardStr(newLine);
				SpecialItem item = SpecialItem.valueOf(newLine, column, itemStr);
				entity.addSpecialItem(item);
			} 
			
			if (obstacle == CEObstacleType.CE_STONE_ORE_VALUE || obstacle == CEObstacleType.CE_SANDY_SOIL_ORE_VALUE) {
				String itemStr = CoreExploreRewardCfg.getRewardStr(newLine);
				SpecialItem item = SpecialItem.valueOf(newLine, column, itemStr);
				entity.addOreItem(item);
			}
			
			//刷出带奖励的矿点
			if (obstacle == CEObstacleType.CE_BOX_VALUE || obstacle == CEObstacleType.CE_SANDY_SOIL_ORE_VALUE 
					|| obstacle == CEObstacleType.CE_STONE_ORE_VALUE || obstacle == CEObstacleType.CE_SANDY_SOIL_ITEM_VALUE) {
				Map<String, Object> param = new HashMap<>();
		        param.put("lineNum", newLine);      //所在行
		        param.put("colNum", column);        //所在列
		        param.put("mineType", obstacle);    //矿点类型
		        param.put("clickTimes", clickTimes);//可点击次数
		        getDataGeter().logActivityCommon(entity.getPlayerId(), LogInfoType.core_explore_new_mine, param);
			}
		}	
		return columnList;
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			clearConsumeItems(playerId);
			return;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		CoreExploreEntity entity = opEntity.get();
		if (entity.getLineList().isEmpty()) {
			initActivityData(playerId, entity);
		}
		
		long time = HawkTime.getMillisecond();
		if (!HawkTime.isSameDay(time, entity.getDayTime())) {
			entity.setDayTime(time);
			PickItem freePick = entity.getFreePickData();
			freePick.setDailySendYet(0);
			freePick.setSendPickDaily(0);
		}
		
		autoPickBreakLogin(entity);
		syncActivityInfo(playerId, entity);
	}
	
	/**
	 * 登陆时检测自动挖矿是否关闭
	 * @param entity
	 */
	private void autoPickBreakLogin(CoreExploreEntity entity) {
		if (entity.getAutoPick() <= 0) {
			return;
		}
		
		try {
			entity.setAutoPick(0);
			if (!entity.getAutoPickRewardList().isEmpty()) {
				sendMailToPlayer(entity.getPlayerId(), MailId.CORE_EXPLORE_END_NOTIFY, null, null, null, entity.getAutoPickRewardList(), true);
				entity.getAutoPickRewardList().clear();
				entity.getAutoPickConsumeList().clear();
				entity.notifyUpdate();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 个人tick
	 * @param playerId
	 */
	public void playerTick(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		CoreExploreEntity entity = opEntity.get();
		//主要是用于检测玩家从不满足大本等级、vip等级条件，到满足条件过渡
		if (initActivityData(playerId, entity)) {
			ActivityManager.getInstance().syncAllActivityInfo(playerId);
			syncActivityInfo(playerId, entity);
		}
		
		PickItem freePick = entity.getFreePickData();
		int freePickLimit = getFreePickLimit(entity);
		if (this.getFreePickCount(playerId) >= freePickLimit) {
			return;
		}
		
		boolean sync = false;
		if (freePick.getDailySendYet() <= 0) {
			sendPick(playerId, freePick, freePickLimit, CoreExploreConstCfg.getInstance().getDailyFreePick());
			freePick.setDailySendYet(1);
			entity.setDayTime(HawkTime.getMillisecond());
			entity.notifyUpdate();
			sync = true;
			if (this.getFreePickCount(playerId) >= freePickLimit) {
				syncActivityInfo(playerId, entity);
				return;
			}
		}
		
		long now = HawkApp.getInstance().getCurrentTime();
		if (freePick.getLastSendTime() <= 0) {
			freePick.setLastSendTime(now);
			entity.notifyUpdate();
			return;
		}
		
		long period = getRecoverPeriod(entity);
		if (now - freePick.getLastSendTime() >= period) {
			int count = (int) ((now - freePick.getLastSendTime()) / period);
			sendPick(playerId, freePick, freePickLimit, CoreExploreConstCfg.getInstance().getFreePickNum() * count);
			freePick.setLastSendTime(freePick.getLastSendTime() + period * count);
			entity.notifyUpdate();
			sync = true;
		}
		if (sync) {
			syncActivityInfo(playerId, entity);
		}
	}
	
	/**
	 * 检测是否已有【免费矿镐上限增加】的作用号
	 * @param entity
	 * @return
	 */
	private int getFreePickLimit(CoreExploreEntity entity) {
		int effect = 0;
		int freePickLimit = CoreExploreConstCfg.getInstance().getPickLimit();
		for (int techId : entity.getCompleteTechs()) {
			CoreExploreTechCfg techCfg = HawkConfigManager.getInstance().getConfigByKey(CoreExploreTechCfg.class, techId);
			if (techCfg != null && techCfg.getTechType() == CoreExploreConst.TECH_EFF_3) {
				effect += Integer.parseInt(techCfg.getTechEffect());
			}
		}
		
		return freePickLimit + effect;
	}
	
	/**
	 * 检测是否已有【免费矿镐恢复时间减少 x%】的作用号
	 * @param entity
	 * @return
	 */
	private long getRecoverPeriod(CoreExploreEntity entity) {
		int effect = 0;
		int period = CoreExploreConstCfg.getInstance().getFreePickCd();
		for (int techId : entity.getCompleteTechs()) {
			CoreExploreTechCfg techCfg = HawkConfigManager.getInstance().getConfigByKey(CoreExploreTechCfg.class, techId);
			if (techCfg != null && techCfg.getTechType() == CoreExploreConst.TECH_EFF_2) {
				effect += Integer.parseInt(techCfg.getTechEffect());
			}
		}
		
		if (effect > 0) {
			period = (int) Math.ceil(period * (1 - effect * 1d / 10000));
		} 
		return period * 1000L;
	}
	
	/**
	 * 赠送矿镐
	 * @param playerId
	 * @param freePick
	 * @param sendNum
	 */
	private void sendPick(String playerId, PickItem freePick, int freePickLimit, int sendNum) {
		int freePickCount = this.getFreePickCount(playerId);
		int sendCount = Math.min(sendNum, freePickLimit - freePickCount);
		//赠送矿镐
		int freePickId = CoreExploreConstCfg.getInstance().getFreePickId();
		RewardItem.Builder rewardItem = RewardHelper.toRewardItem(30000, freePickId, sendCount);
		this.getDataGeter().takeReward(playerId, Arrays.asList(rewardItem), 1, Action.CORE_EXPLORE_SEND_PICK, false);
		freePick.setSendPickDaily(freePick.getSendPickDaily() + sendCount);
		freePick.setSendPickTotal(freePick.getSendPickTotal() + sendCount);
	}
	
	/**
	 * 检测是否已有【获得矿石数量*x】的作用号
	 * @param entity
	 * @param rewardList
	 */
	private void checkTechEff2Ore(CoreExploreEntity entity, List<RewardItem.Builder> rewardList) {
		int itemId = CoreExploreConstCfg.getInstance().getMineralItemId();
		Optional<RewardItem.Builder> op = rewardList.stream().filter(e -> e.getItemId() == itemId).findAny();
		if (!op.isPresent()) {
			return;
		}
		
		RewardItem.Builder rewardItem = op.get();
		int effect = 0;
		for (int techId : entity.getCompleteTechs()) {
			CoreExploreTechCfg techCfg = HawkConfigManager.getInstance().getConfigByKey(CoreExploreTechCfg.class, techId);
			if (techCfg != null && techCfg.getTechType() == CoreExploreConst.TECH_EFF_1) {
				effect += Integer.parseInt(techCfg.getTechEffect());
			}
		}
		
		if (effect > 0) {
			int newCount = (int) Math.floor(rewardItem.getItemCount() * (1 + effect * 1d / 10000));
			rewardItem.setItemCount(newCount);
		} 
	}
	
	/**
	 * 开启或关闭自动挖矿
	 * @param playerId
	 * @param autoPick 1开启，0关闭
	 * @return
	 */
	public int autoPick(String playerId, int autoPick) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		if (autoPick != 0 && autoPick != 1) {
			autoPick = 1;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		CoreExploreEntity entity = opEntity.get();
		if (entity.getAutoPick() == autoPick) {
			HawkLog.logPrintln("CoreExploreActivity autoPick set break, playerId: {}, autoPick: {}", playerId, autoPick);
			return 0;
		}
		
		if (autoPick > 0) {
			entity.getAutoPickRewardList().clear();
			entity.getAutoPickConsumeList().clear();
		}
		entity.setAutoPick(autoPick);
		syncActivityInfo(playerId, entity);
		
		AutoPickResp.Builder respBuilder = AutoPickResp.newBuilder();
		respBuilder.setAutoPick(autoPick);
		if (autoPick == 0) {
			entity.getAutoPickRewardList().forEach(e -> respBuilder.addAwards(e));
			entity.getAutoPickRewardList().clear();
			entity.getAutoPickConsumeList().forEach(e -> respBuilder.addConsumes(e));
			entity.getAutoPickConsumeList().clear();
			entity.notifyUpdate();
		}
		pushToPlayer(playerId, HP.code2.CORE_EXPLORE_AUTO_PICK_S_VALUE, respBuilder);
		HawkLog.logPrintln("CoreExploreActivity autoPick set, playerId: {}, autoPick: {}", playerId, autoPick);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 消除格子上的障碍物
	 * @param playerId
	 * @param line
	 * @param column
	 * @param type
	 */
	public int removeObstacle(String playerId, int line, int column, CEObstacleRemoveType type) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		CoreExploreEntity entity = opEntity.get();
		if (line > entity.getCurrLine() || line < entity.getFirstLine() || column < first_column || column > last_column) {
			HawkLog.logPrintln("CoreExploreActivity param err, playerId: {}, line: {}, column: {}, type: {}, currLine: {}, firstLine: {}", 
					playerId, line, column, type, entity.getCurrLine(), entity.getFirstLine());
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Integer grid = entity.getGridObstacle(line, column);
		HawkTuple2<Boolean, Integer> tuple = null;
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		if (type == CEObstacleRemoveType.REMOVE_BY_PICK) {
			tuple = removeObsByPick(entity, line, column, rewardList); //矿镐
		} else if (type == CEObstacleRemoveType.REMOVE_BY_BOOM) {
			tuple = removeObsByBoom(entity, line, column, rewardList); //炸弹
		} else {
			tuple = removeObsByRig(entity, line, column, rewardList);  //钻机
		}
		
		if (!tuple.first) {
			return tuple.second;
		}
		
		//发奖
		int newLine = tuple.second;
		if (!rewardList.isEmpty()) {
			rewardList = RewardHelper.mergeRewardItem(rewardList);
			checkTechEff2Ore(entity, rewardList);
			this.getDataGeter().takeReward(playerId, rewardList, 1, Action.CORE_EXPLORE_REMOVE_OBS, false);
			entity.addAutoPickRewards(rewardList);
		}
		
		removeObstacleResponse(playerId, entity, newLine, grid, type.getNumber(), line, column);
		HawkLog.logPrintln("CoreExploreActivity removeObstacle, playerId: {}, line: {}, column: {}, type: {}, newLineCount: {}", playerId, line, column, type, newLine);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 移除障碍返还
	 * @param playerId
	 * @param entity
	 * @param newLine
	 */
	private void removeObstacleResponse(String playerId, CoreExploreEntity entity, int newLine, int mineType, int toolType, int lineNum, int columnNum) {
		int maxLineBef = entity.getTotalLine(), removeLineCount = newLine;
		CERemoveObstacleResp.Builder respBuilder = CERemoveObstacleResp.newBuilder();
		while (newLine > 0) {
			newLine--;
			removeAndNewLine(entity, false, respBuilder);
			CoreExploreHelper.checkConnected(entity, entity.getTotalLine());
		}
		
		int count = CoreExploreConstCfg.getInstance().getBlockLineNum();
		while (count > 0) {
			int addLineCount = 0;
			for(int line = entity.getCurrLine(); line <= entity.getTotalLine(); line++) {
				boolean connectedLine = false;
				for (int col = first_column; col <= last_column; col++) {
					if (entity.isConnectedGrids(line, col)) {
						addLineCount++;
						connectedLine = true;
						break;
					}
				}
				if(!connectedLine) {
					break;
				}
			}
			
			if (addLineCount == 0) {
				break;
			}
			for(int i = 0; i < addLineCount; i++) {
				removeAndNewLine(entity, count<=3, respBuilder);
				CoreExploreHelper.checkConnected(entity, entity.getTotalLine());
				count--;
			}
			entity.notifyUpdate();
		}
		
		//不管是否新生成了行数据，都需要检测连通性
		CoreExploreHelper.refreshConnectedData(entity);
		
		//挖矿打点：矿点坐标所在行，矿点坐标所在列，矿点类型，挖矿工具，挖矿前最大行数，挖矿后最大行数，理论消除行数，实际消除行数
		int realRemoveCount = entity.getTotalLine() - maxLineBef;
		Map<String, Object> param = new HashMap<>();
		param.put("lineNum", lineNum);                    //矿点坐标所在行
		param.put("colNum", columnNum);                   //矿点坐标所在列
        param.put("mineType", mineType);                  //矿点类型
        param.put("toolType", toolType);                  //挖矿工具
        param.put("maxLineBefore", maxLineBef);           //挖矿前最大行数
        param.put("maxLineAfter", entity.getTotalLine()); //挖矿后最大行数
        param.put("removeLines1", removeLineCount);       //理论消除行数
        param.put("removeLines2", realRemoveCount);       //实际消除行数
        getDataGeter().logActivityCommon(playerId, LogInfoType.core_explore_mining, param);
		
        int freePickCount = this.getFreePickCount(playerId);
		respBuilder.setCurrLine(entity.getCurrLine());
		respBuilder.setFreePick(entity.getFreePickData().toBuilder(freePickCount));
		entity.getBoxList().forEach(box -> respBuilder.addZoneBox(box.toBuilder()));
		entity.getStoneList().forEach(stone -> respBuilder.addZoneStone(stone.toBuilder()));
		entity.getSpecialItemList().forEach(item -> respBuilder.addZoneItem(item.toBuilder()));
		entity.getOreItemList().forEach(item -> respBuilder.addOreItem(item.toBuilder()));
		
		for (int line = entity.getFirstLine(); line <= entity.getTotalLine(); line++) {
			CEZoneLinePB.Builder builder = CEZoneLinePB.newBuilder();
			builder.setLine(line);
			entity.getColumnList(line).forEach(e -> builder.addColObstacle(e));
			respBuilder.addZoneLine(builder);
		}
		pushToPlayer(playerId, HP.code2.CORE_EXPLORE_REMOVE_OBSTACLE_S_VALUE, respBuilder);
	}
	
	/**
	 * 移除行数据
	 * @param entity
	 * @param respBuilder
	 */
	private void removeLine(CoreExploreEntity entity, CERemoveObstacleResp.Builder builder) {
		int firstLine = entity.getFirstLine();
		List<Integer> removeCols = entity.getColumnList(firstLine); 
		if (builder != null) {
			CEZoneLinePB.Builder lineBuilder = CEZoneLinePB.newBuilder();
			lineBuilder.setLine(firstLine);
			removeCols.forEach(e -> lineBuilder.addColObstacle(e));
			builder.addRemovedLine(lineBuilder);
		}
		
		//注意：要把石头、宝箱、通路格子数据清理掉
		for (int i = 1; i <= removeCols.size(); i++) {
			int gridVal = removeCols.get(i - 1);
			if (gridVal == CEObstacleType.CE_BOX_VALUE) {
				entity.removeBox(firstLine, i);
			}
			if (gridVal == CEObstacleType.CE_STONE_VALUE || gridVal == CEObstacleType.CE_STONE_ORE_VALUE) {
				entity.removeStone(firstLine, i);
			}
			if (gridVal == CEObstacleType.CE_SANDY_SOIL_ITEM_VALUE) {
				entity.removeSpecialItem(firstLine, i);
			}
			if (gridVal == CEObstacleType.CE_STONE_ORE_VALUE || gridVal == CEObstacleType.CE_SANDY_SOIL_ORE_VALUE) {
				entity.removeOreItem(firstLine, i);
			}
		}
		
		//移除第一行之前，需要设置第二行格子的状态
		int index = 0, secondLine = firstLine + 1;
		List<Integer> secondLineCols = entity.getColumnList(secondLine);
		int emptyVal = CEObstacleType.CE_EMPTY_VALUE;
		for (int gridVal : removeCols) {
			int column = index + 1;
			if (gridVal != emptyVal && secondLineCols.get(index) == emptyVal && !entity.isConnectedGrids(secondLine, column)) {
				secondLineCols.set(index, CEObstacleType.CE_SPECIAL_EMPTY_VALUE);
			}
			index++;
		}
		
		HawkLog.logPrintln("CoreExploreActivity remove line, playerId: {}, currLine: {}, removeLine: {}", entity.getPlayerId(), entity.getCurrLine(), firstLine);
		entity.removeFirstLine();
		entity.removeFirstLineConnect();
	}
	
	/**
	 * 通过钻机来清除障碍
	 * @param entity
	 * @param line
	 * @param column
	 * @return
	 */
	private HawkTuple2<Boolean, Integer> removeObsByRig(CoreExploreEntity entity, int line, int column, List<RewardItem.Builder> rewardList) {
		Integer grid = entity.getGridObstacle(line, column);
		if (grid.intValue() != CEObstacleType.CE_EMPTY_VALUE) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.CORE_EXPLORE_GRID_OBSTACLE_VALUE);
		}
		
		if (!entity.isConnectedGrids(line, column)) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.CORE_EXPLORE_GRID_CONNECTED_ERR_VALUE);
		}
		
		int rigItemId = CoreExploreConstCfg.getInstance().getRigItemId();
		RewardItem.Builder consume = RewardHelper.toRewardItem(30000, rigItemId, 1);
		boolean flag = this.getDataGeter().cost(entity.getPlayerId(), Arrays.asList(consume), 1, Action.CORE_EXPLORE_REMOVE_OBS, false);
		if (!flag) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.addAutoPickConsumes(Arrays.asList(consume));
		
		for (int tmpLine = entity.getFirstLine(); tmpLine <= entity.getCurrLine(); tmpLine++) {
			if (tmpLine == entity.getCurrLine()) {
				clearObstacle(tmpLine, entity, rewardList, column - 1, column, column + 1);
			} else {
				clearObstacle(tmpLine, entity, rewardList, column);
			}
		}
		
		this.addConnectedGrid(entity, line, column, true);
		entity.notifyUpdate();
		int newLine = getNewLineCount(entity);
		return new HawkTuple2<Boolean, Integer>(true, newLine);
	}
	
	/**
	 * 通过炸弹来清除障碍
	 * @param entity
	 * @param line
	 * @param column
	 * @return
	 */
	private HawkTuple2<Boolean, Integer> removeObsByBoom(CoreExploreEntity entity, int line, int column, List<RewardItem.Builder> rewardList) {
		Integer grid = entity.getGridObstacle(line, column);
		if (grid.intValue() != CEObstacleType.CE_EMPTY_VALUE) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.CORE_EXPLORE_GRID_OBSTACLE_VALUE);
		}
		
		if (!entity.isConnectedGrids(line, column)) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.CORE_EXPLORE_GRID_CONNECTED_ERR_VALUE);
		}
		
		int boomItemId = CoreExploreConstCfg.getInstance().getBoomItemId();
		RewardItem.Builder consume = RewardHelper.toRewardItem(30000, boomItemId, 1);
		boolean flag = this.getDataGeter().cost(entity.getPlayerId(), Arrays.asList(consume), 1, Action.CORE_EXPLORE_REMOVE_OBS, false);
		if (!flag) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.addAutoPickConsumes(Arrays.asList(consume));
		
		clearObstacle(line-2, entity, rewardList, column);
		clearObstacle(line-1, entity, rewardList, column-1, column, column+1);
		clearObstacle(line,   entity, rewardList, column-2, column-1, column, column+1, column+2);
		clearObstacle(line+1, entity, rewardList, column-1, column, column+1);
		clearObstacle(line+2, entity, rewardList, column);
		this.addConnectedGrid(entity, line, column, true);
		
		entity.notifyUpdate();
		int newLine = getNewLineCount(entity);
		return new HawkTuple2<Boolean, Integer>(true, newLine);
	}
	
	/**
	 * 通过矿镐来清除障碍
	 * @param entity
	 * @param line
	 * @param column
	 */
	private HawkTuple2<Boolean, Integer> removeObsByPick(CoreExploreEntity entity, int line, int column, List<RewardItem.Builder> rewardList) {
		Integer grid = entity.getGridObstacle(line, column);
		//已经是空格子
		if (grid.intValue() == CEObstacleType.CE_EMPTY_VALUE) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.CORE_EXPLORE_GRID_EMPTY_VALUE);
		}
		
		if (grid.intValue() == CEObstacleType.CE_BOX_VALUE) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.CORE_EXPLORE_GRID_BOX_VALUE); 
		}
		
		if (!entity.isConnectedGrids(line, column - 1) && !entity.isConnectedGrids(line - 1, column)
				&& !entity.isConnectedGrids(line, column + 1) && !entity.isConnectedGrids(line + 1, column)) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.CORE_EXPLORE_GRID_CONNECTED_ERR_VALUE);
		}
		
		int freeCount = this.getFreePickCount(entity.getPlayerId());
		// - 格子包括：空格、沙土、沙土附带矿石、石头、石头附带矿石、宝箱
		List<RewardItem.Builder> consumes = null;
		if (grid.intValue() == CEObstacleType.CE_SANDY_SOIL_VALUE || grid.intValue() == CEObstacleType.CE_SANDY_SOIL_ORE_VALUE
				|| grid.intValue() == CEObstacleType.CE_SANDY_SOIL_ITEM_VALUE) {
			consumes = getConsumeItems(entity.getPlayerId(), CoreExploreConstCfg.getInstance().getSandCost());
		} else {
			consumes = getConsumeItems(entity.getPlayerId(), CoreExploreConstCfg.getInstance().getStoneCost());
		}
		
		boolean flag = this.getDataGeter().cost(entity.getPlayerId(), consumes, 1, Action.CORE_EXPLORE_REMOVE_OBS, false);
		if (!flag) {
			return new HawkTuple2<Boolean, Integer>(false, Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.addAutoPickConsumes(consumes);
		
		int freePickLimit = getFreePickLimit(entity);
		int newCount = this.getFreePickCount(entity.getPlayerId());
		if (freeCount >= freePickLimit && newCount < freePickLimit) {
			entity.getFreePickData().setLastSendTime(HawkTime.getMillisecond());
			entity.notifyUpdate();
		}
		
		boolean remove = true;
		StoneItem stone = entity.getStone(line, column);
		if (stone == null || stone.getClickTimes() <= 1) {
			clearObstacle(line, entity, rewardList, column);
			this.addConnectedGrid(entity, line, column, true);
		} else {
			remove = false;
			stone.setClickTimes(stone.getClickTimes() - 1);
		}
		
		entity.notifyUpdate();
		if (!remove) {
			return new HawkTuple2<Boolean, Integer>(true, 0);
		}
		
		int newLine = getNewLineCount(entity);
		return new HawkTuple2<Boolean, Integer>(true, newLine);
	}
	
	/**
	 * 获取矿镐消耗
	 * @param playerId
	 * @param itemStr
	 * @return
	 */
	private List<RewardItem.Builder> getConsumeItems(String playerId, String itemStr) {
		int freePickNum = this.getFreePickCount(playerId);
		List<RewardItem.Builder> consumes = new ArrayList<>();
		List<RewardItem.Builder> builderList = RewardHelper.toRewardItemImmutableList(itemStr);
		if (freePickNum > 0) {
			int freePickId = CoreExploreConstCfg.getInstance().getFreePickId();
			int count = (int) Math.min(freePickNum, builderList.get(0).getItemCount());
			RewardItem.Builder consumeBuilder = RewardHelper.toRewardItem(30000, freePickId, count);
			consumes.add(consumeBuilder);
		}
		if (freePickNum < builderList.get(0).getItemCount()) {
			RewardItem.Builder consumeBuilder = RewardHelper.toRewardItem(30000, builderList.get(1).getItemId(), builderList.get(0).getItemCount() - freePickNum);
			consumes.add(consumeBuilder);
		}
		return consumes;
	}
	
	/**
	 * 清除障碍物，同时获得奖励
	 * @param line
	 * @param entity
	 * @param rewardList
	 * @param columns
	 */
	private void clearObstacle(int line, CoreExploreEntity entity, List<RewardItem.Builder> rewardList, int... columns) {
		List<Integer> columnList = entity.getColumnList(line);
		if (columnList.isEmpty()) {
			HawkLog.errPrintln("CoreExploreActivity clearObstacle error, empty line, playerId: {}, line: {}", entity.getPlayerId(), line);
			return;
		}
		
		for (int column : columns) {
			if (column < first_column || column > last_column) {
				continue;
			}
			int gridVal = columnList.get(column - 1);
			if (gridVal == CEObstacleType.CE_EMPTY_VALUE) {
				continue;
			}
			if (gridVal != CEObstacleType.CE_BOX_VALUE) {
				columnList.set(column - 1, CEObstacleType.CE_EMPTY_VALUE);
				this.addConnectedGrid(entity, line, column, false);
			}
			if (gridVal == CEObstacleType.CE_STONE_VALUE || gridVal == CEObstacleType.CE_STONE_ORE_VALUE) {
				StoneItem stone = entity.getStone(line, column);
				entity.removeStone(stone);
			}
			if (gridVal == CEObstacleType.CE_SANDY_SOIL_ITEM_VALUE) {
				SpecialItem item = entity.getSpecialItem(line, column);
				rewardList.addAll(RewardHelper.toRewardItemImmutableList(item.getItem()));
				entity.removeSpecialItem(item);
			}
			if (gridVal == CEObstacleType.CE_STONE_ORE_VALUE || gridVal == CEObstacleType.CE_SANDY_SOIL_ORE_VALUE) {
				SpecialItem item = entity.getOreItem(line, column);
				rewardList.addAll(RewardHelper.toRewardItemImmutableList(item.getItem()));
				entity.removeOreItem(item);
			}
		}
	}
	
	/**
	 * 将指定格子加进通路
	 * @param line
	 * @param column
	 */
	private void addConnectedGrid(CoreExploreEntity entity, int line, int column, boolean checkConnect) {
		entity.addConnectedGrid(line, column);
		if (checkConnect) {
			Set<String> unconnectedEmptyGrids = entity.getUnconnectedEmptyGrids();
			CoreExploreHelper.checkConnected(entity, unconnectedEmptyGrids);
		}
	}
	
	/**
	 * 获取要新刷出的行数
	 * @param entity
	 * @return
	 */
	private int getNewLineCount(CoreExploreEntity entity) {
		int newLine = 0;
		for (int line = entity.getCurrLine(); line <= entity.getTotalLine(); line++) {
			if (entity.isConnectedLine(line)) {
				newLine++;
			}
		}
		return newLine;
	}
	
	/**
	 *  领取宝箱奖励
	 * @param playerId
	 * @param line
	 * @param column
	 * @param count
	 */
	public int receiveBoxAward(String playerId, int line, int column, int count) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		CoreExploreEntity entity = opEntity.get();
		if (count <= 0) {
			HawkLog.logPrintln("CoreExploreActivity recieve box param error, playerId: {}, line: {}, column: {}, count: {}", playerId, line, column, count);
			count = 1;
		}
		BoxItem box = entity.getBox(line, column);
		if (box == null) {
			HawkLog.logPrintln("CoreExploreActivity recieve box not exist, playerId: {}, line: {}, column: {}", playerId, line, column);
			return Status.Error.CORE_EXPLORE_BOX_NOT_EXIST_VALUE;
		}
		
		if (box.getRecieveTimes() >= box.getTotalTimes()) {
			HawkLog.logPrintln("CoreExploreActivity recieve box times limit, playerId: {}, line: {}, column: {}, recieveTimes: {}, totalTimes: {}", playerId, line, column, box.getRecieveTimes(), box.getTotalTimes());
			return Status.Error.CORE_EXPLORE_BOX_REWARD_LIMIT_VALUE;
		}
		
		int realCount = Math.min(count, box.getTotalTimes() - box.getRecieveTimes());
		//发奖
		int rewardId = CoreExploreConstCfg.getInstance().getBoxClickAward();
		List<RewardItem.Builder> boxAwardItems = this.getDataGeter().takeRewardReturnItemlist(playerId, rewardId, realCount, Action.CORE_EXPLORE_BOX_AWARD, true);
		box.setRecieveTimes(box.getRecieveTimes() + realCount);
		entity.notifyUpdate();
		
		entity.addAutoPickRewards(boxAwardItems);
		
		int newLine = 0;
		if (box.recieveEnd()) {
			entity.removeBox(box);
			entity.setGridEmpty(line, column);
			this.addConnectedGrid(entity, line, column, true);
			newLine = getNewLineCount(entity);
		}

		removeObstacleResponse(playerId, entity, newLine, CEObstacleType.CE_BOX_VALUE, 0, line, column);
		HawkLog.logPrintln("CoreExploreActivity recieve boxAward, playerId: {}, line: {}, column: {}, count: {}, received: {}, totalCount: {}", playerId, line, column, count, box.getRecieveTimes(), box.getTotalTimes());
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 *  购买矿镐
	 * @param playerId
	 * @param count
	 */
	public int buyPick(String playerId, int count) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		CoreExploreEntity entity = opEntity.get();
		int alreadyCount = entity.getPickBuyTimes();
		int limitCount = CoreExploreConstCfg.getInstance().getBuyPickLimit();
		if (alreadyCount >= limitCount) {
			HawkLog.logPrintln("CoreExploreActivity buyPick error, playerId: {}, count: {}, boughtCount: {}, limitCount: {}", playerId, count, alreadyCount, limitCount);
			return Status.Error.CORE_EXPLORE_BUY_PICK_LIMIT_VALUE;
		}
		
		if (count <= 0) {
			HawkLog.logPrintln("CoreExploreActivity buyPick param error, playerId: {}, count: {}", playerId, count);
			count = 1;
		}
		int realCount = Math.min(count, limitCount - alreadyCount);
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(CoreExploreConstCfg.getInstance().getPickPrice());
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, realCount, Action.CORE_EXPLORE_BUY_PICK, false);
		if (!flag) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		int pickId = CoreExploreConstCfg.getInstance().getBuyPickId();
		RewardItem.Builder builder = RewardHelper.toRewardItem(30000, pickId, realCount);
		this.getDataGeter().takeReward(playerId, Arrays.asList(builder), 1, Action.CORE_EXPLORE_BUY_PICK, true, RewardOrginType.ACTIVITY_REWARD);
		entity.setPickBuyTimes(alreadyCount + realCount);
		syncActivityInfo(playerId, entity);
		
		HawkLog.logPrintln("CoreExploreActivity buyPick, playerId: {}, count: {}, totalCount: {}", playerId, count, entity.getPickBuyTimes());
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 *  科技提升
	 * @param playerId
	 * @param techId
	 */
	public int techOper(String playerId, int techId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		CoreExploreTechCfg techCfg = HawkConfigManager.getInstance().getConfigByKey(CoreExploreTechCfg.class, techId);
		if (techCfg == null) {
			HawkLog.errPrintln("CoreExploreActivity techOper config error, playerId: {}, techId: {}", playerId, techId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		CoreExploreEntity entity = opEntity.get();
		if (entity.getCompleteTechs().contains(techId)) {
			HawkLog.errPrintln("CoreExploreActivity techOper repeated error, playerId: {}, techId: {}, complete techs: {}", playerId, techId, entity.getCompleteTechs());
			return Status.Error.CORE_EXPLORE_TECH_COMPLETE_VALUE;
		}
		
		for (int condition : techCfg.getConditionList()) {
			if (!entity.getCompleteTechs().contains(condition)) {
				HawkLog.errPrintln("CoreExploreActivity techOper condition error, playerId: {}, techId: {}, pretech: {}, complete techs: {}", playerId, techId, condition, entity.getCompleteTechs());
				return Status.Error.CORE_EXPLORE_TECH_PRE_UNREACH_VALUE;
			}
		}
		
		if (!HawkOSOperator.isEmptyString(techCfg.getTechCost())) {
			List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(techCfg.getTechCost());
			boolean flag = this.getDataGeter().cost(playerId, consumeItems, 1, Action.CORE_EXPLORE_TECH_CONSUME, false);
			if (!flag) {
				return Status.Error.ITEM_NOT_ENOUGH_VALUE;
			}
		}
		
		int freePickLimit = techCfg.getTechType() != CoreExploreConst.TECH_EFF_2 ? 0 : this.getFreePickLimit(entity);
		
		entity.addCompleteTech(techId);
		if (techCfg.getTechType() == CoreExploreConst.TECH_EFF_4 && !HawkOSOperator.isEmptyString(techCfg.getTechEffect())) {
			List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(techCfg.getTechEffect());
			this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.CORE_EXPLORE_TECH_AWARD, true, RewardOrginType.ACTIVITY_REWARD);
		}
		
		if (freePickLimit > 0) {
			int freeCount = this.getFreePickCount(playerId);
			int newLimit = this.getFreePickLimit(entity);
			if (freeCount >= freePickLimit && freeCount < newLimit) {
				entity.getFreePickData().setLastSendTime(HawkTime.getMillisecond());
				entity.notifyUpdate();
			}
		}
		
		//科技研究打点：科技id，科技类型
		Map<String, Object> param = new HashMap<>();
        param.put("techId", techId);                  //科技id
        param.put("techType", techCfg.getTechType()); //科技类型
        getDataGeter().logActivityCommon(playerId, LogInfoType.core_explore_tech, param);
		
		syncActivityInfo(playerId, entity);
		HawkLog.logPrintln("CoreExploreActivity techOper, playerId: {}, techId: {}", playerId, techId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 *  商店兑换
	 * @param playerId
	 * @param shopId
	 * @param count
	 * @return
	 */
	public int shopExchange(String playerId, int shopId, int count) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		CoreExploreShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CoreExploreShopCfg.class, shopId);
		if (cfg == null) {
			HawkLog.errPrintln("CoreExploreActivity shop buy error, playerId: {}, shopId: {}", playerId, shopId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		if (count <= 0) {
			HawkLog.errPrintln("CoreExploreActivity shop buy error, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		CoreExploreEntity entity = opEntity.get();
		boolean shopUnlock = false, newTimesUnlock = false, newPriceUnlock = false;
		for (int techId : entity.getCompleteTechs()) {
			CoreExploreTechCfg techCfg = HawkConfigManager.getInstance().getConfigByKey(CoreExploreTechCfg.class, techId);
			if (techCfg == null) {
				continue;
			}
			if (techCfg.getTechType() == CoreExploreConst.TECH_EFF_5) {  /** 科技效果5： 解锁兑换商店 */
				shopUnlock = true;
			} else if (techCfg.getTechType() == CoreExploreConst.TECH_EFF_6) { /** 科技效果6： 兑换商店价格降低、（读取新的一列价格配置） */
				newPriceUnlock = true;
			} else if (techCfg.getTechType() == CoreExploreConst.TECH_EFF_7) { /** 科技效果7： 兑换商店商品兑换次数增加n次。（读取新的一列限购次数配置） */
				newTimesUnlock = true;
			}
		}
		
		//判断兑换商店是否解锁
		if (!shopUnlock) {
			HawkLog.errPrintln("CoreExploreActivity shop buy error, shop not unlock, playerId: {}, shopId: {}", playerId, shopId);
			return Status.Error.CORE_EXPLORE_SHOP_LOCK_VALUE;
		} 
		
		if (cfg.getTechId() > 0 && !entity.getCompleteTechs().contains(cfg.getTechId())) {
			HawkLog.errPrintln("CoreExploreActivity shop buy error, playerId: {}, shopId: {}, unreach techId: {}", playerId, shopId, cfg.getTechId());
			return Status.Error.CORE_EXPLORE_SHOP_ITEM_LOCK_VALUE;
		}
		
		int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
		int newCount = boughtCount + count;
		
		//判断是否有兑换次数增加的科技效果
		int timesLimit = newTimesUnlock ? cfg.getTime2() : cfg.getTimes();
		if (newCount > timesLimit) {
			HawkLog.errPrintln("CoreExploreActivity shop buy error, playerId: {}, shopId: {}, oldCount: {}, newCount: {}, limit: {}", playerId, shopId, boughtCount, newCount, timesLimit);
			return Status.Error.CORE_EXPLORE_SHOP_EXCHANGE_LIMIT_VALUE;
		}
		
		//判断是否有消耗降低的科技效果
		String price = newPriceUnlock ? cfg.getNeedItem2() : cfg.getNeedItem();
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(price);
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, count, Action.CORE_EXPLORE_SHOP_EXCHANGE, false);
		if (!flag) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getGainItem());
		this.getDataGeter().takeReward(playerId, rewardItems, count, Action.CORE_EXPLORE_SHOP_EXCHANGE, true, RewardOrginType.ACTIVITY_REWARD);
		entity.getShopItemMap().put(shopId, boughtCount + count);
		entity.notifyUpdate();
		
		syncActivityInfo(playerId, entity);
		HawkLog.logPrintln("CoreExploreActivity shopExchange, playerId: {}, shopId: {}, count: {}", playerId, shopId, count);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	public void syncActivityDataInfo(String playerId){
		pushActivityInfo(playerId);
	}

	/**
	 * 同步活动信息
	 * @param playerId
	 */
	public void pushActivityInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		syncActivityInfo(playerId, opEntity.get());
	}
	
	
	/**
	 * 同步活动信息
	 * 
	 * @param playerId
	 */
	private void syncActivityInfo(String playerId, CoreExploreEntity entity){
		if (entity.getLineList().isEmpty()) {
			HawkLog.logPrintln("CoreExploreActivity syncActivityInfo break, line list empty, playerId: {}", playerId);
			return;
		}
		CoreExploreActivityInfo.Builder activityInfo = CoreExploreActivityInfo.newBuilder();
		activityInfo.setCurrLine(entity.getCurrLine());
		activityInfo.addAllCompleteTech(entity.getCompleteTechs());
		activityInfo.setAutoPick(entity.getAutoPick());
		
		entity.getBoxList().forEach(box -> activityInfo.addZoneBox(box.toBuilder()));
		entity.getStoneList().forEach(stone -> activityInfo.addZoneStone(stone.toBuilder()));
		entity.getSpecialItemList().forEach(item -> activityInfo.addZoneItem(item.toBuilder()));
		entity.getOreItemList().forEach(item -> activityInfo.addOreItem(item.toBuilder()));
		
		for (int line = entity.getFirstLine(); line <= entity.getTotalLine(); line++) {
			CEZoneLinePB.Builder builder = CEZoneLinePB.newBuilder();
			builder.setLine(line);
			entity.getColumnList(line).forEach(e -> builder.addColObstacle(e));
			activityInfo.addZoneLine(builder);
		}
		
		int freePickCount = this.getFreePickCount(playerId);
		activityInfo.setPickBuyTimes(entity.getPickBuyTimes());
		activityInfo.setFreePick(entity.getFreePickData().toBuilder(freePickCount));
		for (Entry<Integer, Integer> entry : entity.getShopItemMap().entrySet()) {
			PWBShopInfo.Builder builder = PWBShopInfo.newBuilder();
			builder.setShopId(entry.getKey());
			builder.setCount(entry.getValue());
			activityInfo.addShopInfo(builder);
		}
		activityInfo.addAllTips(getTips(CoreExploreShopCfg.class, entity.getTipSet()));
		pushToPlayer(playerId, HP.code2.CORE_EXPLORE_ACTIVITY_INFO_S_VALUE, activityInfo);
	}
	
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	private int getFreePickCount(String playerId) {
		return getDataGeter().getItemNum(playerId, CoreExploreConstCfg.getInstance().getFreePickId());
	}
	
	public void skipToLineGM(String playerId, int line) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<CoreExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		CoreExploreEntity entity = opEntity.get();
		if (entity.getTotalLine() >= line) {
			HawkLog.logPrintln("CoreExploreActivity skipLineGM break, playerId: {}, totalLine: {}, skipToLine: {}", playerId, entity.getTotalLine(), line);
			return;
		}
		
		try {
			int skipLineCount = line - entity.getTotalLine();
			if (skipLineCount >= view_line_count + hide_line_count) {
				entity.getLineList().clear();
				entity.getConnectedGrids().clear();
				entity.getUnconnectedEmptyGrids().clear();
				entity.getStoneList().clear();
				entity.getBoxList().clear();
				
				entity.setCurrLine(line - 2);
				for (int i = entity.getFirstLine(); i <= entity.getTotalLine(); i++) {
					List<Integer> columnList = newLineData(entity, i, i>=entity.getCurrLine());
					if (i == entity.getFirstLine() || i == entity.getCurrLine() - 2) {
						columnList.set(HawkRand.randInt(0, 5), CEObstacleType.CE_EMPTY_VALUE);
						columnList.set(HawkRand.randInt(0, 5), CEObstacleType.CE_EMPTY_VALUE);
					}
				}
				
			} else {
				while (skipLineCount > 0) {
					skipLineCount--;
					removeAndNewLine(entity, skipLineCount < 3, null);
					List<Integer> columnList = entity.getColumnList(entity.getFirstLine());
					columnList.set(HawkRand.randInt(0, 5), CEObstacleType.CE_EMPTY_VALUE);
					columnList.set(HawkRand.randInt(0, 5), CEObstacleType.CE_EMPTY_VALUE);
					columnList.set(HawkRand.randInt(0, 5), CEObstacleType.CE_EMPTY_VALUE);
					CoreExploreHelper.checkConnected(entity, entity.getTotalLine());
				}
			}
			
			entity.notifyUpdate();
			CoreExploreHelper.refreshConnectedData(entity);
			this.pushActivityInfo(playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
