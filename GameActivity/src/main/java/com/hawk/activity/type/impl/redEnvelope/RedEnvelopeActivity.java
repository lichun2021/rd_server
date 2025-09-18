package com.hawk.activity.type.impl.redEnvelope;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.redEnvelope.base.CurRedEnvelopeState;
import com.hawk.activity.type.impl.redEnvelope.base.OnceRedEnvelope;
import com.hawk.activity.type.impl.redEnvelope.cfg.RedEnvelopeAchieveCfg;
import com.hawk.activity.type.impl.redEnvelope.entity.RedEnvelopeEntity;
import com.hawk.activity.type.impl.redEnvelope.history.PlayerRedEnvelopeHistory;
import com.hawk.game.protocol.Activity.RedEnvelopeActivityInfo;
import com.hawk.game.protocol.Activity.RedEnvelopeDetail;
import com.hawk.game.protocol.Activity.RedEnvelopeHistory;
import com.hawk.game.protocol.Activity.RedEnvelopeHistoryInfo;
import com.hawk.game.protocol.Activity.RedEnvelopeInfo;
import com.hawk.game.protocol.Activity.RedEnvelopePersonState;
import com.hawk.game.protocol.Activity.RedEnvelopeRecieveDetail;
import com.hawk.game.protocol.Activity.RedEnvelopeState;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;

public class RedEnvelopeActivity extends ActivityBase {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/** 个人红包历史记录，过期时间 **/
	private final int PLAYER_HISTORY_EXPIRE = (86400 * 30);
	
	/** 当前红包数据**/
	private RedEnvelopeData data;
	
	public RedEnvelopeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RED_ENVELOPE_ACTIVITY;
	}
	
	@Override
	public void onOpen() {
		data = new RedEnvelopeData(this.getActivityTermId());
		syncInfo2OnlinePlayers();
	}

	@Override
	public void onEnd() {
		int termId = getActivityTermId();
		//清理缓存数据
		ConfigIterator<RedEnvelopeAchieveCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RedEnvelopeAchieveCfg.class);
		while(ite.hasNext()){
			RedEnvelopeAchieveCfg cfg = ite.next();
			String key = String.format(ActivityRedisKey.RED_ENVELOPE, termId, cfg.getStageID());
			ActivityLocalRedis.getInstance().del(key); //删除缓存红包数据
		}
	}

	@Override
	public void onTick() {
		RedEnvelopeAchieveCfg newStageCfg = null;
		long curTime = HawkTime.getMillisecond();
		if((newStageCfg = data.hasNewRedEnvelope(curTime)) != null){
			logger.info("RedEnvelope exchange, current RedEnvelopeAchieveCfg is:{}", newStageCfg.getStageID());
			data.setCurStage(newStageCfg);
			data.initRedEnvelope();
			data.setState(CurRedEnvelopeState.SHOW);
			syncInfo2OnlinePlayers();
		}
		if(data.checkState(curTime)){
			syncInfo2OnlinePlayers(); //同步开始状态
		}
	}
	
	private void syncInfo2OnlinePlayers(){
		Set<String> onlines = getDataGeter().getOnlinePlayers();
		for(String playerId : onlines){
			callBack(playerId, MsgId.RED_ENVELOPE_SYNC_INFO, ()->{
				syncActivityInfo(playerId);
			});
		}
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			syncActivityInfo(playerId);
		}
	}
	
	/***
	 * 抢红包
	 * @param playerId
	 * @param stageId 红包id
	 * @return
	 */
	public Result<?> onPlayerRecieveRedEnvelope(String playerId, int stageId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RedEnvelopeEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RedEnvelopeEntity entity = opEntity.get();
		//判断是否领取过
		if(entity.isRecieve(stageId)){
			return Result.fail(Status.Error.RED_ENVELOPE_ALREADY_RECIEVE_VALUE);
		}
		//判断该阶段是否开启
		if(!data.canRecieve(stageId)){
			return Result.fail(Status.Error.RED_ENVELOPE_NOT_START_VALUE);
		}
		addTask(new HawkTask() {
			@Override
			public Object run() {
				data.recivevRedEnvelope(playerId, (code, rewards) -> {
					if(code == 0){
						logger.info("RedEnvelopeActivity player:{}, recieve sus, rewards:{}", playerId, rewards);
						OnceRedEnvelope red = OnceRedEnvelope.valueOf(playerId, rewards);
						//插入自己的历史记录LPUSH
						lpushRedEnvelopeHistory(playerId, rewards, stageId);
						data.addOnceRedEnvelope(red);
						//判断是否需要广播
						checkBroadCast(playerId, rewards, stageId);
						entity.addRecieve(stageId);
						entity.notifyUpdate();
						data.saveRedEnvelope();
						//再同步一次消息
						syncActivityInfo(playerId);
						//告诉客户端领取了哪些东西，并设置状态
						syncRecieveState(stageId, playerId, RedEnvelopePersonState.PERSON_RECEIVED);
					}else{
						//返回错误码
						logger.info("RedEnvelopeActivity player:{}, recieve fail, errorCode:{}", playerId, code);
						if(code == Status.Error.RED_ENVELOPE_DELIVE_OVER_VALUE){
							//告诉客户端被领取完了
							syncRecieveState(stageId, playerId, RedEnvelopePersonState.PERSON_RECEIVED_END);
						}else{
							PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.RED_ENVELOPE_RECIEVE_VALUE, code);
						}
					}
				});
				return null;
			}
		});
		return null;
	}
	
	private void syncRecieveState(int stageID, String playerId, RedEnvelopePersonState state){
		if(!isOpening(playerId)){
			return;
		}
		OnceRedEnvelope detail = data.getRecieveDetails(stageID, playerId);
		RedEnvelopeRecieveDetail.Builder build = RedEnvelopeRecieveDetail.newBuilder();
		if(detail != null){
			if(state == RedEnvelopePersonState.PERSON_RECEIVED){
				for(String src : detail.getRewards()){
					build.addReward(src);
				}
			}
		}
		build.setState(state);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RED_ENVELOPE_RECIEVE_S_VALUE, build));
	}
	
	public void pushPlayerRedEnvelopeHistory(String playerId){
		if(!isOpening(playerId)){
			logger.info("RedEnvelope player req history, but activity not open:{}", playerId);
			return;
		}
		String key = String.format(ActivityRedisKey.RED_ENVELOPE_PLAYER_HISTORY, playerId);
		List<String> history = ActivityLocalRedis.getInstance().lall(key);
		RedEnvelopeHistoryInfo.Builder build = RedEnvelopeHistoryInfo.newBuilder();
		if(history != null && history.size() > 0){
			for(String src : history){
				PlayerRedEnvelopeHistory redHis = JsonUtils.String2Object(src, PlayerRedEnvelopeHistory.class);
				RedEnvelopeHistory.Builder hisBuild = RedEnvelopeHistory.newBuilder();
				hisBuild.setTime(redHis.getTime());
				for(String reward : redHis.getRewards()){
					hisBuild.addRewards(reward);
				}
				hisBuild.setStageId(redHis.getStageId());
				build.addHistory(hisBuild);
			}
		}
		pushToPlayer(playerId, HP.code.RED_ENVELOPE_HISTORY_INFO_S_VALUE, build);
	}
	
	/***
	 * 查看红包领取详情
	 * @param stageID
	 * @param playerId
	 */
	public void playerReqRedEnvelopeDetail(int stageID, String playerId){
		if(!isOpening(playerId)){
			return;
		}
		OnceRedEnvelope detail = data.getRecieveDetails(stageID, playerId);
		RedEnvelopeDetail.Builder build = RedEnvelopeDetail.newBuilder();
		if(detail != null){
			for(String src : detail.getRewards()){
				build.addReward(src);
			}
		}
		pushToPlayer(playerId, HP.code.RED_ENVELOPE_RECIEVE_DETAIL_S_VALUE, build);
	}
	
	/***
	 * 检查该奖励是否需要广播
	 * @param playerId
	 * @param rewards
	 */
	private void checkBroadCast(String playerId, List<String> rewards, int stageID){
		RedEnvelopeAchieveCfg config = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopeAchieveCfg.class, stageID);
		if(config == null){
			return;
		}
		try {
			String playerName = getDataGeter().getPlayerName(playerId);
			for(String reward : rewards){
				if (config.getBroadcastItemID().contains(reward)) {
					if(HawkOSOperator.isEmptyString(playerName)){
						sendBroadcast(NoticeCfgId.RED_ENVELOPE_SYSTEM, null,  reward);
					}else{
						sendBroadcast(NoticeCfgId.RED_ENVELOPE_SYSTEM, null, playerName, reward);
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/***
	 * 插入历史记录
	 * @param playerId
	 * @param rewards
	 */
	private void lpushRedEnvelopeHistory(String playerId, List<String> rewards, int stageID){
		String key = String.format(ActivityRedisKey.RED_ENVELOPE_PLAYER_HISTORY, playerId);
		PlayerRedEnvelopeHistory history = new PlayerRedEnvelopeHistory();
		history.setPlayerId(playerId);
		history.setRewards(rewards);
		history.setStageId(stageID);
		history.setTime(HawkTime.getMillisecond());
		ActivityLocalRedis.getInstance().lpush(key,PLAYER_HISTORY_EXPIRE,JsonUtils.Object2Json(history));
	}
	
	private void addTask(HawkTask task) {
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("RED_ENVELOPE_ACTIVITY");
			taskPool.addTask(task, 0, false);
		}
	}
	
	/****
	 * 同步活动信息
	 */
	public void syncActivityInfo(String playerId){
		RedEnvelopeActivityInfo.Builder build = RedEnvelopeActivityInfo.newBuilder();
		ConfigIterator<RedEnvelopeAchieveCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RedEnvelopeAchieveCfg.class);
		while(ite.hasNext()){
			RedEnvelopeAchieveCfg cfg = ite.next();
			RedEnvelopeInfo.Builder info = RedEnvelopeInfo.newBuilder();
			info.setId(cfg.getStageID());
			info.setShowTime(cfg.getShow(getActivityTermId()));
			info.setStartTime(cfg.getStart(getActivityTermId()));
			info.setEndTime(cfg.getEnd(getActivityTermId()));
			if(cfg == data.getCurStage()){
				info.setState(data.getState(playerId));
			}else{
				info.setState(getState(cfg));
			}
			build.addRedEnvelope(info);
		}
		pushToPlayer(playerId, HP.code.RED_ENVELOPE_INFO_S_VALUE, build);
	}
	
	/***
	 * 获取非当前红包阶段的状态
	 * @param cfg
	 * @return
	 */
	private RedEnvelopeState getState(RedEnvelopeAchieveCfg cfg){
		long curTime = HawkTime.getMillisecond();
		if(curTime >= cfg.getEnd(getActivityTermId())){
			return RedEnvelopeState.ALREADY_OVER;
		}
		return RedEnvelopeState.ONT_START;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RedEnvelopeActivity activity = new RedEnvelopeActivity(config.getActivityId(), activityEntity);
		activity.data = new RedEnvelopeData(activityEntity.getTermId());
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RedEnvelopeEntity> queryList = HawkDBManager.getInstance()
				.query("from RedEnvelopeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RedEnvelopeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RedEnvelopeEntity entity = new RedEnvelopeEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
