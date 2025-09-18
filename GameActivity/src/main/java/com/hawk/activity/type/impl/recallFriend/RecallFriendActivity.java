package com.hawk.activity.type.impl.recallFriend;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityAccountRoleInfo;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GuildQuiteEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.event.impl.RecallFriendEvent;
import com.hawk.activity.event.impl.RecallGuildFriendEvent;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendCfg;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendGuildTaskCfg;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendTaskCfg;
import com.hawk.activity.type.impl.recallFriend.data.RecalDataContent;
import com.hawk.activity.type.impl.recallFriend.data.RecalPlayer;
import com.hawk.activity.type.impl.recallFriend.entity.RecallFriendEntity;
import com.hawk.activity.type.impl.recallFriend.parser.IGuildAchieveParser;
import com.hawk.activity.type.impl.recallFriend.task.GuildAchieveContext;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.RecallFriendInfoResp;
import com.hawk.game.protocol.Activity.RecallFriendPlayerMsg;
import com.hawk.game.protocol.Activity.RecallFriendResp;
import com.hawk.game.protocol.Activity.RecallFriendState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Player.ImageSource;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RecallFriendActivity extends ActivityBase implements AchieveProvider{
	static Logger logger = LoggerFactory.getLogger("Server");
	
	private RecalDataContent dataContent;

	Map<String, Map<String, RecallFriendPlayerMsg.Builder>> recallFriednCacheMap = new ConcurrentHashMap<>();

	public RecallFriendActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RECALL_FRIEND_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RecallFriendActivity recallFriendActivity = new RecallFriendActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(recallFriendActivity);
		return  recallFriendActivity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RecallFriendEntity> queryList = HawkDBManager.getInstance()
				.query("from RecallFriendEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RecallFriendEntity activityRecallFriendEntity = new RecallFriendEntity();
		activityRecallFriendEntity.setPlayerId(playerId);
		activityRecallFriendEntity.setTermId(termId);
		activityRecallFriendEntity.setLastResetTime(HawkTime.getMillisecond());
		
		return activityRecallFriendEntity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	@Subscribe
	public void onCrossDayEevent(ContinueLoginEvent event) {
		if (!event.isCrossDay()) {
			return;
		}
		if (!this.isAllowOprate(event.getPlayerId())) {
			return;
		}
		
		Optional<RecallFriendEntity> opEntity = this.getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}

		RecallFriendEntity entity = opEntity.get();

		/**
		 * 登录天数累计,并跨天重置每天召回玩家的数据
		 */
		entity.recordLoginDay();

		if (HawkTime.isSameDay(entity.getLastResetTime(), HawkTime.getMillisecond())) {
			return;
		}
		clearData(entity);
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (!this.isAllowOprate(playerId)) {
			return;
		}
		
		Optional<RecallFriendEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		RecallFriendEntity entity = opEntity.get();
		if (!HawkTime.isSameDay(entity.getLastResetTime(), HawkTime.getMillisecond())) {
			clearData(entity);
		}
		/**
		 * 登录天数累计,并跨天重置每天召回玩家的数据
		 */
		entity.recordLoginDay();
		//校验是否符合联盟召回中,回流玩家
		checkComeBackGuildFlow(playerId);
		/////////////////////

		//判断是否是回归玩家.
		if (isLostPlayerByPlayerId(playerId)) {
			onPlayerComeBack(playerId);
		}
		//重新登录之后才会清理该信息.
		recallFriednCacheMap.remove(playerId);
		try {
			Map<String, RecallInfo> recallStatteMap = this.getRecallStateMap(playerId);
			if (recallStatteMap == null) {
				return;
			}
			
			int count = 0;
			//主堡等级对应的人数
			Map<Integer, Integer> recallFacLvMap = new HashMap<>();
			for (RecallInfo recallInfo : recallStatteMap.values()) {
				//已经召回的玩家.
				if (recallInfo.getState() == RecallFriendState.RECALL_FRIEND_RECALLED_VALUE) {
					count ++;
					//统计已经召回的玩家的主堡等级数据
					int facLv = recallInfo.getFacLv();
					int beforeNum = recallFacLvMap.getOrDefault(facLv, 0);
					recallFacLvMap.put(recallInfo.getFacLv(), beforeNum + 1);
				}
			}


			if (count <= 0) {
				return;
			}
			
			Optional<AchieveItems> opItems = this.getAchieveItems(playerId);
			if (!opItems.isPresent()) {
				return;
			}			
			
			List<AchieveItem> aiList = new ArrayList<>();
			//此种修复只能针对单个做处理.
			AchieveItem recalledReset = null; 
			for (AchieveItem ai : opItems.get().getItems()) {
				//已经完成的不处理.
				if (ai.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
					continue;
				}
				AchieveConfig ac = this.getAchieveCfg(ai.getAchieveId());
				if (ac == null) {
					continue;
				}
				//成就是已召回好友.
				if (ac.getAchieveType() == AchieveType.RECALLED_FIREND) {					
					RecallFriendTaskCfg taskCfg = (RecallFriendTaskCfg)ac;
					if (taskCfg.getReset() != 0) {
						recalledReset = ai;
						continue;
					}
					if (count >= ac.getConditionValue(0)) {
						ai.setValue(0, ac.getConditionValue(0));
						ai.setState(AchieveState.NOT_REWARD_VALUE);
						aiList.add(ai);
					} else if (count > ai.getValue(0)) {
						ai.setValue(0, count);
						aiList.add(ai);
					} 
				}
				/**
				 * 今日发出召唤好友基地等级多少级
				 */
				if (ac.getAchieveType() == AchieveType.RECALL_FRIEND_LEVEL) {
					int conditionLv = ac.getConditionValue(0);
					int conditionNum = ac.getConditionValue(1);
					int facNum = getGreatAndThen(recallFacLvMap, conditionLv);
					if (facNum >= conditionNum) {
						ai.setValue(0, conditionNum);
						ai.setState(AchieveState.NOT_REWARD_VALUE);
						aiList.add(ai);
					} else if (facNum > ai.getValue(0)) {
						ai.setValue(0, facNum);
						aiList.add(ai);
					}
				}
			}
			
			if (aiList != null && !aiList.isEmpty()) {
				if (recalledReset != null) {
					recalledReset.setValue(0, 1);
					recalledReset.setState(AchieveState.NOT_REWARD_VALUE);
					aiList.add(recalledReset);
				}
				entity.notifyUpdate();
				AchievePushHelper.pushAchieveUpdate(playerId, aiList);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/**
	 * 计算大于条件值的召回玩家的数量
	 * @param recallFacLvMap
	 * @param conditionLv
	 * @return
	 */
	public int getGreatAndThen(Map<Integer, Integer> recallFacLvMap, int conditionLv){
		int sum = 0;
		for (Map.Entry<Integer, Integer> entry : recallFacLvMap.entrySet()) {
			int lv = entry.getKey();
			int value = entry.getValue();
			if (lv >= conditionLv){
				sum += value;
			}
		}
		return sum;
	}
	/**
	 * 玩家召回.
	 * @param playerId
	 */
	private void onPlayerComeBack(String playerId) {
		String openId = this.getDataGeter().getOpenId(playerId);
		int facLv = this.getDataGeter().getConstructionFactoryLevel(playerId);
		if (HawkOSOperator.isEmptyString(openId)) {
			return;
		}
		
		Map<String, String> players = this.getRecallPlayers(openId);
		if (players == null || players.isEmpty()) {
			return;
		}
		List<Entry<String, String>> entryList = new ArrayList<>(players.entrySet());
		this.clearRecallPlayers(openId);
		Collections.sort(entryList, new Comparator<Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> entry1, Entry<String, String> entry2) {
				if (entry1.getValue().equals(entry2.getValue())) {
					return entry1.getKey().compareTo(entry2.getKey());
				} else {
					return entry1.getValue().compareTo(entry2.getValue());
				}
			}
		});
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				HawkLog.logPrintln("player login back openId:{}, notfityPlayerIds:{}", openId, players);
				int num = 0;
				RecallFriendCfg cfg = RecallFriendCfg.getInstance();
				for (Entry<String, String> entry : entryList) {
					num++;
					try {
						String serverId = entry.getKey();						
						String[] strings = serverId.split(":");						
						if (num > cfg.getTriggerRecallNumLimit()) {
							RecallFriendActivity.this.addRecallState(strings[1], openId, RecallInfo.valueOf(RecallFriendState.RECALL_FRIEND_MISS_VALUE, facLv));
							continue;
						} else {
							RecallFriendActivity.this.addRecallState(strings[1], openId, RecallInfo.valueOf(RecallFriendState.RECALL_FRIEND_RECALLED_VALUE, facLv));
						}
						RecallFriendActivity.this.getDataGeter().proxyCall(strings[0], "recalledFriendBack", "playerId=" + strings[1] + "&" + "openId=" + openId+ "&" + "facLv=" + facLv, 2000);
						
						//记录自己是被哪个服上的哪个角色召回的
						Map<String, Object> params = new HashMap<>(); 
						params.put("fromServer", strings[0]);
						params.put("fromPlayerId", strings[1]);
						RecallFriendActivity.this.getDataGeter().logActivityCommon(playerId, LogInfoType.friend_back, params);
					} catch (Exception e) {
						HawkException.catchException(e);
					}					
				}
				
				return null;
			}
		});
	}

	public void clearData(RecallFriendEntity entity) {
		super.logger.info("player:{}, termId:{} reset activity recall friend data", entity.getPlayerId(), entity.getTermId());
		entity.setRecallNum(0);
		entity.setLastResetTime(HawkTime.getMillisecond());
		for (AchieveItem achieveItem  : entity.getAchieveItemList()) {
			AchieveConfig ac = this.getAchieveCfg(achieveItem.getAchieveId());
			if (ac == null) {
				super.logger.error("can not find achieve config achieveId:{}", achieveItem.getAchieveId());
				continue;
			}
			RecallFriendTaskCfg recallFriendTaskCfg = HawkConfigManager.getInstance().getConfigByKey(RecallFriendTaskCfg.class, ac.getAchieveId());
			if (recallFriendTaskCfg != null){
				RecallFriendTaskCfg taskCfg = (RecallFriendTaskCfg)ac;
				//0位不需要
				if (taskCfg.getReset() != 0) {
					super.logger.error("recall friend clear achieve playerId:{}, data:{}", entity.getPlayerId(), achieveItem);
					achieveItem.reset();
				}
			}else{
				RecallFriendGuildTaskCfg recallFriendGuildTaskCfg = HawkConfigManager.getInstance().getConfigByKey(RecallFriendGuildTaskCfg.class, ac.getAchieveId());
				if (recallFriendGuildTaskCfg != null){
					RecallFriendGuildTaskCfg taskCfg = (RecallFriendGuildTaskCfg)ac;
					//联盟的任务不能有重置,,错误日志打印
					if (taskCfg.getReset() != 0) {
						super.logger.error("recall friend guild clear achieve is error playerId:{}, data:{}", entity.getPlayerId(), achieveItem);
						achieveItem.reset();
					}
				}
			}


		}
		
		entity.notifyUpdate();
		//同步成就
		AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), entity.getAchieveItemList());		
		
	}
	
	/**
	 * 获取请求召回好友的信息
	 * @param playerId
	 */
	public void recallFriendInfo(String playerId) {
		Map<String, RecallFriendPlayerMsg.Builder> map = recallFriednCacheMap.get(playerId);
		RecallFriendCfg cfg = RecallFriendCfg.getInstance();
		List<RecallFriendPlayerMsg> playerMsgList = new ArrayList<>();
		Optional<RecallFriendEntity> opEntity = this.getPlayerDataEntity(playerId);
		RecallFriendEntity entity = opEntity.get();
		Map<String, RecallInfo> recallStatteMap = this.getRecallStateMap(playerId);
		//尽力减少redis的访问.
		if (map == null) {
			JSONObject platformObj = this.getDataGeter().fetchPlatformFriendList(playerId);
			if (platformObj != null) {
				JSONArray friendList = platformObj.getJSONArray("lists");
				Map<String, JSONObject> friendInfoMap = new HashMap<String, JSONObject>();		
				String myOpenId = this.getDataGeter().getOpenId(playerId);
				for (int i = 0; i < friendList.size(); i++) {
					 JSONObject friendInfo = friendList.getJSONObject(i);
					 String fopenid = friendInfo.getString("openid");
					 if (myOpenId.indexOf(fopenid) >= 0) {
						 continue;
					 }				 
					 friendInfoMap.put(fopenid, friendInfo);
				}
				
				List<ActivityAccountRoleInfo> roleList = ActivityGlobalRedis.getInstance().getLostAccoutRoleList(friendInfoMap.keySet(), cfg.getLostTime() * 1000l, 
						cfg.getBuildlevel(), cfg.getLevel(), cfg.getVip());
				map = new HashMap<>();
				for (ActivityAccountRoleInfo role : roleList) {
					//过滤掉非正式服
					if (Integer.parseInt(role.getServerId()) % 10000 > 9000) {
						continue;
					}
					JSONObject jsonObject = friendInfoMap.get(role.getOpenId());
					RecallFriendPlayerMsg.Builder playerBuilder = RecallFriendPlayerMsg.newBuilder();
					playerBuilder.setPlayerId(role.getPlayerId());
					playerBuilder.setServerId(role.getServerId());
					playerBuilder.setIcon(role.getIcon());
					if (HawkOSOperator.isEmptyString(role.getPfIcon())) {
						playerBuilder.setPfIcon("");
					} else {
						String[] icons = role.getPfIcon().split("_");						 
						 if (icons[0].equals(ImageSource.FROMIM_VALUE+"")) {
							 playerBuilder.setPfIcon(icons[0] + "_" + 
									 this.getDataGeter().getPfIconFromRedis(role.getOpenId(), role.getPlatform()) + "_" + icons[2]); 
						 } else {
							 playerBuilder.setPfIcon(role.getPfIcon());
						 }	 
	
					}
					playerBuilder.setPlayerName(role.getPlayerName());
					playerBuilder.setNickName(jsonObject.getString("nickName"));				
					playerBuilder.setOpenid(role.getOpenId());
					RecallInfo recallInfo = recallStatteMap.get(role.getOpenId());
					int state;
					if (recallInfo == null) {
						state = RecallFriendState.RECALL_FRIEND_NONE_VALUE;
					}else{
						 state = recallInfo.getState();
					}
					playerBuilder.setRecalled(RecallFriendState.valueOf(state));
					playerMsgList.add(playerBuilder.build());
					map.put(role.getOpenId(), playerBuilder);
				}					
				recallFriednCacheMap.put(playerId, map);
			}
			
		} else {
			for (RecallFriendPlayerMsg.Builder builder : map.values()) {
				RecallInfo recallInfo = recallStatteMap.get(builder.getOpenid());
				int state;
				if (recallInfo == null) {
					state = RecallFriendState.RECALL_FRIEND_NONE_VALUE;
				}else{
					state = recallInfo.getState();
				}
				
				builder.setRecalled(RecallFriendState.valueOf(state));				
				playerMsgList.add(builder.build());
			}
		}
		
		RecallFriendInfoResp.Builder sbuilder = RecallFriendInfoResp.newBuilder();
		sbuilder.setRecalledNum(entity.getRecallNum());
		sbuilder.addAllMsg(playerMsgList);
		//自己拉回的好友openid
		for (Entry<String, RecallInfo> entry : recallStatteMap.entrySet()) {
		 	if (entry.getValue().getState() == RecallFriendState.RECALL_FRIEND_RECALLED_VALUE) {
		 		sbuilder.addSelfRecalledOpenId(entry.getKey());
		 	}
		}
		
		HawkProtocol hawkProtocl = HawkProtocol.valueOf(HP.code.RECALL_FRIEND_INFO_RESP_VALUE, sbuilder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, hawkProtocl);
	}
	
	/**
	 * 请求召回好友
	 * @param playerId
	 * @return
	 */
	public int recallFriendReq(String playerId, String recalledFriendOpenId) {
		if (HawkOSOperator.isEmptyString(recalledFriendOpenId)) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (playerId.equals(recalledFriendOpenId)) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Map<String, RecallFriendPlayerMsg.Builder> map = this.recallFriednCacheMap.get(playerId);
		if (map == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		RecallFriendPlayerMsg.Builder builder = map.get(recalledFriendOpenId);
		if (builder == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<RecallFriendEntity> opEntity = this.getPlayerDataEntity(playerId);
		RecallFriendEntity entity = opEntity.get();
		RecallFriendCfg cfg = RecallFriendCfg.getInstance();
		if (cfg.getRecallNumLimit() <= entity.getRecallNum()) {
			return Status.Error.RECALL_FRIEND_NUM_NOT_ENOUGH_VALUE;
		} 
		
		int state = this.getRecallState(playerId, recalledFriendOpenId);
		if (state != RecallFriendState.RECALL_FRIEND_NONE_VALUE) {
			 return Status.Error.RECALL_FRIEND_ALEARDY_REQ_VALUE;
		}
		
		if (!this.isLostPlayerByOpenId(recalledFriendOpenId, false)) {
			//从缓存里面删除,同步一次好友信息.
			map.remove(recalledFriendOpenId);
			this.recallFriendInfo(playerId);
			return Status.Error.RECALL_FRIEND_NOT_VALID_VALUE;
		}
		
		entity.setRecallNum(entity.getRecallNum() + 1);
		String serverId = this.getDataGeter().getPlayerServerId(playerId);
		this.addRecallPlayer(recalledFriendOpenId, serverId, playerId);
		this.addRecallState(playerId, recalledFriendOpenId, RecallInfo.valueOf(RecallFriendState.RECALL_FRIEND_RECALLING_VALUE, 0));
		//抛出事件,发出了召唤.
		ActivityManager.getInstance().postEvent(new RecallFriendEvent(playerId));
		
		Map<String, RecallInfo> recallStatteMap = this.getRecallStateMap(playerId);
		RecallFriendResp.Builder resp = RecallFriendResp.newBuilder();
		builder.setRecalled(RecallFriendState.RECALL_FRIEND_RECALLING);
		resp.setPlayerMsg(builder);
		//自己拉回的好友openid
		for (Entry<String, RecallInfo> entry : recallStatteMap.entrySet()) {
		 	if (entry.getValue().getState() == RecallFriendState.RECALL_FRIEND_RECALLED_VALUE) {
		 		resp.addSelfRecalledOpenId(entry.getKey());
		 	}
		}
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.RECALL_FRIEND_RESP_VALUE, resp);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, respProtocol);
		
		super.logger.info("recall friend req playerId:{}, targetOpenId:{}", playerId, recalledFriendOpenId);
		
		Map<String, Object> param = new HashMap<>();
        param.put("targetOpenId", recalledFriendOpenId);
		this.getDataGeter().logActivityCommon(playerId, LogInfoType.friend_recall, param);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 保存到活动结束 加一天.
	 * @return
	 */
	public int getKeyExpireTime() {
		long endTime = this.getTimeControl().getHiddenTimeByTermId(this.getActivityTermId());
		long curTime = HawkTime.getMillisecond();
		int remainTime = (int)((endTime - curTime) / 1000) ;
		
		return remainTime + 86400;
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return this.isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return true;
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<RecallFriendEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		
		RecallFriendEntity playerDataEntity = opPlayerDataEntity.get();
		if (playerDataEntity.getAchieveItemList().isEmpty()) {
			this.initAchieve(playerDataEntity);
		}
		List<AchieveItem> list = playerDataEntity.getAchieveItemList();

		List<AchieveItem> allAchieveList = new ArrayList<>();
		allAchieveList.addAll(list);

		String guildId = this.getDataGeter().getGuildId(playerId);
		/**
		 * 联盟召回成就
		 */
		if (!StringUtils.isEmpty(guildId)){
			List<AchieveItem> guildAchieveItem = dataContent.getGuildAchieveItem(guildId);
			allAchieveList.addAll(guildAchieveItem);
		}
		AchieveItems items = new AchieveItems(allAchieveList, playerDataEntity);

		return Optional.of(items);
	}
	
	@Override
	public void onOpen() {
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String id : onlinePlayers){
			callBack(id, GameConst.MsgId.ACHIEVE_INIT_RECALL_FRIEDN, ()->{
				initAchieve(id);
			});
		}
		// 活动开启的时候初始化
		init();
	}
	
	private void initAchieve(String playerId) {
		if (!this.isAllowOprate(playerId)) {
			return;
		}
		
		Optional<RecallFriendEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		this.initAchieve(opPlayerDataEntity.get());
		/**
		 * 优化版,,登录就记录天数次数据已经排除重复
		 */
		RecallFriendEntity entity = opPlayerDataEntity.get();
		entity.recordLoginDay();
	}
	
	private void initAchieve(RecallFriendEntity entity) {
		// 成就已初始化
		if (!entity.getAchieveItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<RecallFriendTaskCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RecallFriendTaskCfg.class);
		while (configIterator.hasNext()) {
			RecallFriendTaskCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getAchieveItemList()), true);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig achieveConfig = HawkConfigManager.getInstance().getConfigByKey(RecallFriendTaskCfg.class, achieveId);
		if (achieveConfig == null){
			achieveConfig = HawkConfigManager.getInstance().getConfigByKey(RecallFriendGuildTaskCfg.class, achieveId);
		}
		return achieveConfig;
	}

	@Override
	public Action takeRewardAction() {
		return Action.RECALL_FRIEND_ACHIEVE_AWARD;
	}
	
	public boolean isLostPlayerByPlayerId(String playerId) {
		String openId = this.getDataGeter().getOpenId(playerId);
		return isLostPlayerByOpenId(openId, true);
	}
	/**
	 * 是否是回归玩家.
	 * @return
	 */
	public boolean isLostPlayerByOpenId(String openId, boolean isSelf) {		
		List<ActivityAccountRoleInfo> roleList = ActivityGlobalRedis.getInstance().getActivityAccountRoleList(openId);
		long curTime = HawkTime.getMillisecond();
		RecallFriendCfg cfg = RecallFriendCfg.getInstance();
		long leaveTime = cfg.getLostTime() * 1000l;
		boolean isComeBack = false;
		for (ActivityAccountRoleInfo role : roleList) {
			//时间不达标 自己的话可能logintime被写入了
			if (isSelf) {
				if (curTime - role.getLogoutTime() < leaveTime) {
					return false;
				} 			
			} else {
				long realLeavTime = Math.max(role.getLoginTime(), role.getLogoutTime());
				if (curTime - realLeavTime < leaveTime) {
					return false;
				}
			}
			
			//基地等级不达标
			if (role.getCityLevel() < cfg.getBuildlevel()) {
				continue;
			}
			//玩家等级.
			if (role.getPlayerLevel() < cfg.getLevel()) {
				continue;
			}
			//贵族等级不够
			if (role.getVipLevel() < cfg.getVip()) {
				continue;
			}
			
			isComeBack = true;
		}
		
		return isComeBack;
	}
	
	/**
	 * 记录谁对该玩家进行了召唤
	 * @return
	 */
	public String getRecallPlayerKey() {
		int termId = this.getActivityTermId();
		return "recall_player:" + termId;
	}
	
	/**
	 * 记录此玩家都召唤了谁
	 * @return
	 */
	public String getRecallStateKey() {
		int termId = this.getActivityTermId();
		return "recall_state:" + termId;
	}
	
	public Map<String, RecallInfo> getRecallStateMap(String playerId) {
		String key = getRecallStateKey() + ":" + playerId;
		Map<String, String> map = ActivityGlobalRedis.getInstance().hgetAll(key);
		Map<String, RecallInfo> resultMap = new HashMap<>();
		for (Entry<String, String> entry : map.entrySet()) {
			RecallInfo recallInfo = new RecallInfo();
			recallInfo.mergeFrom(entry.getValue());
			resultMap.put(entry.getKey(), recallInfo);
		}
		
		return resultMap;
	}
	
	/**
	 * 获取对某个账号的召唤状态.
	 * @param playerId
	 * @param openId
	 * @return
	 */
	public Integer getRecallState(String playerId, String openId) {
		String key = getRecallStateKey() + ":" + playerId;
		String info = ActivityGlobalRedis.getInstance().hget(key, openId);
		if (StringUtils.isEmpty(info)){
			return RecallFriendState.RECALL_FRIEND_NONE_VALUE;
		}
		RecallInfo recallInfo = new RecallInfo();
		recallInfo.mergeFrom(info);
		int state = recallInfo.getState();
		return state;
	}
	/**
	 * 主动添加玩家.
	 * @param targetOpenId
	 */
	public void addRecallState(String playerId, String targetOpenId, RecallInfo recallInfo) {
		String key = getRecallStateKey() + ":" + playerId;
		String info = recallInfo.serialize();
		ActivityGlobalRedis.getInstance().hset(key, targetOpenId, info , getKeyExpireTime());
	}
	
	/**
	 * 添加召唤者列表
	 * @param openId
	 * @param playerId
	 */
	public void addRecallPlayer(String openId, String serverId, String playerId) {
		String key = getRecallPlayerKey() + ":" + openId;
		int curTime = HawkTime.getSeconds();
		ActivityGlobalRedis.getInstance().hset(key, serverId + ":" + playerId, curTime + "", this.getKeyExpireTime());
	}
	
	/**
	 * 获取所有对此玩家召唤过的玩家.
	 * @param openId
	 * @return
	 */
	public Map<String, String> getRecallPlayers(String openId) {
		String key = getRecallPlayerKey() + ":" + openId;
		return ActivityGlobalRedis.getInstance().hgetAll(key);
	}
	
	/**
	 * 删除谁召唤了我的信息
	 * @param openId
	 */
	public void clearRecallPlayers(String openId) {
		String key = getRecallPlayerKey() + ":" + openId;
		ActivityGlobalRedis.getInstance().del(key);
	}


	///////////////////////////////////////////联盟回流/////////////////////////////////////
	/**
	 * 是否经过初始化
	 */
	private boolean isInit;

	private long lastTickTime;
	@Override
	public void onTick() {
		if(!isInit){
			init();
		}
		long nowTime = HawkTime.getMillisecond();
		long refreshTime = RecallFriendCfg.getInstance().getAllianceRefreshTime();
		//间隔10分钟一刷新
		if (nowTime - lastTickTime >= refreshTime){
			//刷新联盟可召回玩家
			dataContent.refreshGuildCanRecallPlayer();
			dataContent.flushToRedis();
			lastTickTime = nowTime;
		}

	}
	/**
	 * 初始化 联盟召回相关数据
	 */
	private void init() {
		dataContent = new RecalDataContent(getActivityTermId());
		dataContent.loadData();
		dataContent.refreshGuildCanRecallPlayer();
		isInit = true;
		logger.info("RecallFriendActivity init readRedisRecallInfo success ");
	}

	@Override
	public void shutdown() {
		if (dataContent != null){
			dataContent.flushToRedis();
			//所有联盟成就数据写redis
			logger.info("RecallFriendActivity shutdown flushRedis success ");
		}
	}
	

	/**
	 *校验是否符合联盟召回中,回流玩家
	 * @param playerId
	 */
	public void checkComeBackGuildFlow(String playerId){
		//判断是否是回流玩家
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer != null){
			logger.info("RecallFriendActivity onPlayerLogin backFlowPlayer is true, playerId:{}", playerId);
			long backTime = backFlowPlayer.getBackTimeStamp();
			int termId = this.getActivityTermId();
			long actStartTime = this.getTimeControl().getStartTimeByTermId(termId);
			long endStartTime = this.getTimeControl().getEndTimeByTermId(termId);
			//回流时间是否在活动期间
			boolean isTimeValid = (backTime >= actStartTime) && (endStartTime >= backTime);
			if(isTimeValid){
				logger.info("RecallFriendActivity onPlayerLogin isTimeValid is true, playerId:{}", playerId);
				//是否进过总回流数据(即判断是否第一次登陆)
				RecalPlayer backPlayer = dataContent.getRecalPlayer(playerId);
				if (Objects.isNull(backPlayer)){
					logger.info("RecallFriendActivity onPlayerLogin isFirst login is true, playerId:{}", playerId);
					//第一次进联盟回流玩家数据
					RecalPlayer recalPlayer = dataContent.newReclPlayer(playerId);
					String guildId = this.getDataGeter().getGuildId(playerId);
					logger.info("RecallFriendActivity onPlayerLogin prepare add backFlow, playerId:{}, guildId:{}", playerId, guildId);
					//无联盟 添加联盟回流玩家数据
					recalPlayer.setInitGuildId(guildId);
				}
			}
		}
	}

	/**
	 * 获取联盟成就配置表数据
	 * @param achieveId
	 * @return
	 */
	public AchieveConfig getGuildAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(RecallFriendGuildTaskCfg.class, achieveId);
	}

	/**
	 * 联盟任务完成
	 * @param playerId
	 * @param achieveItem
	 * @return
	 */
	public Result<?> onGuildAchieveFinished(String playerId, AchieveItem achieveItem) {
		//记录打点
		//完成发奖
		String guildId = this.getDataGeter().getGuildId(playerId);
		if (StringUtils.isEmpty(guildId)){
			return Result.fail(Status.Error.GUILD_NOT_EXIST_VALUE);
		}
		Collection<String> guildMemberIds = this.getDataGeter().getGuildMemberIds(guildId);
		
		String serverId = this.getDataGeter().getServerId();
		int termId = this.getActivityTermId();
		String key = serverId + ":recall_guild_achieve_reward:" + termId;
		Map<String,List<String>> playerRecord = new HashMap<>();
		//取出所有玩家领取的奖励记录
		String [] pidArr = guildMemberIds.toArray(new String[guildMemberIds.size()]);
		List<String> rlts = ActivityGlobalRedis.getInstance().getRedisSession().hmGet(key, pidArr);
		for(String str : rlts){
			List<String> strlist = SerializeHelper.stringToList(String.class, str);
			if(strlist.size() > 0){
				playerRecord.put(strlist.get(0), strlist);
			}
		}
		
		Map<String,String> updateRecorde = new HashMap<>();
		for (String guildPlayerId:guildMemberIds) {
			try {
				List<String> record = playerRecord.get(guildPlayerId);
				if(Objects.nonNull(record) && 
						record.contains(String.valueOf(achieveItem.getAchieveId()))){
					continue;
				}
				int achieveId = achieveItem.getAchieveId();
				AchieveConfig achieveConfig =  getGuildAchieveCfg(achieveId);
				if (achieveConfig == null){
					logger.info("RecallFriendActivity onGuildAchieveFinished playerId:{}, achieveId:{}",playerId, achieveId);
					continue;
				}
				if(Objects.isNull(record)){
					record = new ArrayList<>();
					record.add(guildPlayerId);
				}
				record.add(String.valueOf(achieveItem.getAchieveId()));
				String recordStr = SerializeHelper.collectionToString(record, SerializeHelper.ELEMENT_DELIMITER);
				updateRecorde.put(guildPlayerId, recordStr);
				RecallFriendGuildTaskCfg achieveCfg = (RecallFriendGuildTaskCfg)achieveConfig;
				List<Reward.RewardItem.Builder> rewardList = achieveCfg.getRewardList();
				//邮件发奖
				sendReward(guildPlayerId, rewardList, achieveId);
				logger.info("RecallFriendActivity onAchieveFinished sendReward success guildPlayerId:{}, guildId:{},triggerPlayerId:{}",guildPlayerId, guildId, playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		achieveItem.setState(AchieveState.TOOK_VALUE);
		ActivityGlobalRedis.getInstance().getRedisSession().hmSet(key, updateRecorde,  (int)TimeUnit.DAYS.toSeconds(30));
		return Result.success();
	}
	/**
	 * 成就统一事件监听
	 * @param event
	 */
	@Subscribe
	public void onEvent(ActivityEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		String playerId = event.getPlayerId();
		List<IGuildAchieveParser<?>> parsers = GuildAchieveContext.getParser(event.getClass());
		if (parsers == null || parsers.isEmpty()) {
			logger.info("RecallFriendActivity on ActivityEvent parsers is empty playerId:{}", playerId);
			return;
		}

		String guildId = this.getDataGeter().getGuildId(playerId);
		if (StringUtils.isEmpty(guildId)){
			return;
		}
		//总召回池,不包含
		RecalPlayer recalPlayer = dataContent.getRecalPlayer(playerId);
		if(Objects.isNull(recalPlayer)){
			logger.info("RecallFriendActivity on ActivityEvent allBackFlowPlayerIdList not contain, playerId:{}", playerId);
			return;
		}
		//不满足做任务条件
		if(!recalPlayer.isValidDoEvent(guildId)){
			logger.info("RecallFriendActivity on ActivityEvent guildId is not isValidDoEvent, playerId:{},guildId:{}", playerId, guildId);
			return;
		}

		long eventTime = HawkTime.getMillisecond();
		int cycleCnt = 0;

		List<AchieveItem> needPush = new ArrayList<>();

		for (IGuildAchieveParser<?> parser : parsers) {
				if (!this.isProviderActive(playerId)) {
					cycleCnt++;
					continue;
				}
				//联盟成就数据
				List<AchieveItem> guildAchieveItemList = dataContent.getGuildAchieveItem(guildId);
				if (guildAchieveItemList.isEmpty()){
					super.logger.error("RecallFriendActivity guild achieve cache not found, guildId: {}", guildId);
					return;
				}
				boolean update = false;
				for (AchieveItem achieveItem : guildAchieveItemList) {
					// 联盟成就 更新具体成就数值和状态
					AchieveConfig achieveConfig = getGuildAchieveCfg(achieveItem.getAchieveId());
					if (achieveConfig == null) {
						super.logger.error("RecallFriendActivity guild achieve config not found, achieveId: {}", achieveItem.getAchieveId());
						cycleCnt++;
						continue;
					}
					if (achieveConfig.getAchieveType() != parser.geAchieveType()) {
						cycleCnt++;
						continue;
					}
					boolean temp = parser.updateAchieveData(achieveItem, achieveConfig, event, needPush);
					update = update || temp;
					cycleCnt++;
					// 此活动成就变更默认打点
					if (temp){
						// 记录打点日志
						PlayerDataHelper.getInstance().getDataGeter().logActivityAchieve(playerId, getActivityId(),
								getActivityTermId(), achieveItem.getAchieveId(), achieveItem.getState(),
								SerializeHelper.collectionToString(achieveItem.getDataList(), SerializeHelper.BETWEEN_ITEMS));
					}
				}
		}
		//push 联盟所有在线玩家
		if (!needPush.isEmpty()) {
			List<String> guildPlayerIds = this.getDataGeter().getOnlineGuildMemberIds(guildId);
			for (String guildPlayerId: guildPlayerIds) {
				AchievePushHelper.pushAchieveUpdate(guildPlayerId, needPush);
				logger.info("RecallFriend pushAchieveUpdate, playerId: {},guildPlayerId:{}, eventClass: {}, needPushSize:{}", playerId,guildPlayerId, event.getClass().getSimpleName(), needPush.size());

				//push 联盟召回的信息
				pushRecallGuildInfo(guildPlayerId, guildId);
				logger.info("RecallFriend achieveUpdate pushRecallGuildInfo guildPlayerId:{}, guildId:{}",guildPlayerId, guildId);
			}
		}
		if (eventTime >= ActivityConst.EVENT_TIMEOUT) {
			logger.info("RecallFriend AchieveDealEvent timeout, playerId: {}, eventClass: {}, cycleCnt:{}, costTime: {}", playerId, event.getClass().getSimpleName(), cycleCnt, eventTime);
		}
	}

	@Subscribe
	public void onEvent(JoinGuildEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		//联盟已经召回盟友列表
		String guildId = this.getDataGeter().getGuildId(playerId);
		//进入联盟有限推送下成就,,推送联盟回流活动成就数据
		List<AchieveItem> guildAchieveItem = dataContent.getGuildAchieveItem(guildId);
		AchievePushHelper.pushAchieveUpdate(playerId, guildAchieveItem);

		RecalPlayer recalPlayer = dataContent.getRecalPlayer(playerId);
		if(dataContent.getRecalPlayer(playerId)== null){
			logger.info("RecallFriendActivity on JoinGuildEvent allBackFlowPlayerIdList not contains, playerId:{}", playerId);
			return;
		}
		if (StringUtils.isNotEmpty(recalPlayer.getJoinGuildId()) || StringUtils.isNotEmpty(recalPlayer.getInitGuildId())) {
			logger.info("RecallFriendActivity on JoinGuildEvent isHasRecalled, playerId:{}, JoinGuildId:{}", playerId, recalPlayer.getJoinGuildId());
			return;
		}
		int facLv = this.getDataGeter().getConstructionFactoryLevel(playerId);
		//一旦进过联盟,则标记下
		recalPlayer.setJoinGuildId(guildId);
		recalPlayer.setJoinGuildIdFacLv(facLv);
		//满足做任务条件
		if (recalPlayer.isValidDoEvent(guildId)){
			//是回流玩家
			//联盟好友召回成功事件
			ActivityManager.getInstance().postEvent(new RecallGuildFriendEvent(playerId));
			logger.info("RecallFriendActivity on JoinGuildEvent facLv is limit, playerId:{}, facLv:{}", playerId, facLv);
		}

		logger.info("RecallFriendActivity on JoinGuildEvent success, playerId:{}", playerId);

		//push
		pushRecallGuildInfo(playerId, guildId);

	}
	/**
	 * 联盟退出
	 * @param event
	 */
	@Subscribe
	public void onGuildQuite(GuildQuiteEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		RecalPlayer recalPlayer = dataContent.getRecalPlayer(playerId);
		if(Objects.nonNull(recalPlayer) && Objects.equals(event.getGuildId(), recalPlayer.getJoinGuildId())){
			recalPlayer.setQuitJoinguild(true);
		}
		//退盟,联盟任务成就,push 删除
		List<AchieveItem> guildAchieveItem = dataContent.getGuildAchieveItem(event.getGuildId());
		for (AchieveItem achieve : guildAchieveItem) {
			AchievePushHelper.pushAchieveDelete(playerId, achieve);
		}
	}

	/**
	 * 联盟召回回流玩家
	 * @param playerId
	 * @param targetPlayerId
	 * @return
	 */
	public Result<?> recallGuildBackFlowPlayerReq(String playerId, String targetPlayerId) {
		if (playerId.equals(targetPlayerId)){
			return Result.fail(Status.Error.RECALL_FRIEND_GUILD_IS_MYSELF_VALUE);
		}
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RecallFriendEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RecallFriendEntity entity = opEntity.get();
		//各种校验
		String targetGuildId = this.getDataGeter().getGuildId(targetPlayerId);
		if (!StringUtils.isEmpty(targetGuildId)){
			return Result.fail(Status.Error.GUILD_ALREADYJOIN_VALUE);
		}
		//玩家已经发送过召回请求的玩家
		List<String> hasRecallPlayerIds = entity.getRecallPlayerList();
		if (hasRecallPlayerIds.contains(targetPlayerId)){
			//已经召回过
			return Result.fail(Status.Error.RECALL_FRIEND_GUILD_HAS_RECALL_VALUE);
		}

		RecalPlayer recalPlayer = dataContent.getRecalPlayer(targetPlayerId);
		//目标玩家今日已经被召回的次数
		int beRecallTimes = recalPlayer.getTodayCalCnt();
		int receiveLimit = RecallFriendCfg.getInstance().getReceiveLimit();
		if(beRecallTimes >= receiveLimit){
			//被召回次数已达上限
			return Result.fail(Status.Error.RECALL_FRIEND_GUILD_TARGET_LIMIT_VALUE);
		}
		//邀请入盟
		this.getDataGeter().invitePlayerJoinGuild(playerId, targetPlayerId);
		//添加召回记录
		entity.addRecallPlayerList(targetPlayerId);

		//添加被召回次数记录
		recalPlayer.incDayCalCnt();

		String guildId = this.getDataGeter().getGuildId(playerId);
		if (StringUtils.isEmpty(guildId)){
			//push
			pushRecallGuildInfo(playerId, guildId);
		}
		logger.info("RecallFriendActivity recallGuildBackFlowPlayerReq playerId:{}, targetPlayerId:{}", playerId, targetPlayerId);
		return Result.success();
	}

	/**
	 * 可召回盟友列表
	 * @param playerId
	 * @return
	 */
	public Result<?> getGuildBackFlowPlayerInfoReq(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RecallFriendEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		String guildId = this.getDataGeter().getGuildId(playerId);
		//联盟不存在
		if (StringUtils.isEmpty(guildId)){
			return Result.fail(Status.Error.GUILD_NOT_EXIST_VALUE);
		}
		//push
		pushRecallGuildInfo(playerId, guildId);

		logger.info("RecallFriendActivity getGuildBackFlowPlayerInfoReq playerId:{}, guildId:{}", playerId, guildId);
		return Result.success();

	}

	/**
	 * 推送联盟召回相关信息
	 * @param playerId
	 * @param guildId
	 */
	public void pushRecallGuildInfo(String playerId, String guildId){
		Optional<RecallFriendEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return ;
		}
		RecallFriendEntity entity = opEntity.get();

		Activity.RecallGuildBackFlowInfoResp.Builder builder = Activity.RecallGuildBackFlowInfoResp.newBuilder();
		//联盟可召回盟友列表
		List<String> canRecallPlayerIdList = dataContent.getGuildCanRecallPlayer(guildId);
		if (!canRecallPlayerIdList.isEmpty()){
			List<Activity.RecallGuildBackFlowMsg> recallPb = genPbRecallGuildBackFlowMsg(playerId, entity, canRecallPlayerIdList, true);
			builder.addAllCanRecallMsg(recallPb);
		}
		//联盟已经召回盟友列表
		List<String> hasRecallPlayerIdList = dataContent.getGuildHasRecallPlayer(guildId);
		if (!hasRecallPlayerIdList.isEmpty()){
			List<Activity.RecallGuildBackFlowMsg> hasRecallPb = genPbRecallGuildBackFlowMsg(playerId, entity, hasRecallPlayerIdList, false);
			builder.addAllHasRecallMsg(hasRecallPb);
		}
		//联盟成就任务
		List<Activity.RecallGuildAchieveInfo> guildAchieveInfoList = genGuildAchieveItemsNum(guildId);
		if (!guildAchieveInfoList.isEmpty()){
			builder.addAllGuildAchieve(guildAchieveInfoList);
		}
		//推送消息
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.RECALL_GUILD_BACKFLOW_INFO_RESP_VALUE, builder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, respProtocol);

		logger.info("RecallFriendActivity pushRecallGuildInfo playerId:{}, guildId:{}", playerId, guildId);
	}
	/**
	 * 成就完成数量,通过成就任务计算
	 * @param guildId
	 * @return
	 */
	public List<Activity.RecallGuildAchieveInfo> genGuildAchieveItemsNum(String guildId){
		List<Activity.RecallGuildAchieveInfo> list  = new ArrayList<>();
		List<AchieveItem> achieveItemList = dataContent.getGuildAchieveItem(guildId);
		if (achieveItemList == null){
			return list;
		}
		Map<Integer, Integer> achieveTypeNumMap = new HashMap<>();
		for (AchieveItem guildAchieveItem : achieveItemList) {
			int achieveId = guildAchieveItem.getAchieveId();
			RecallFriendGuildTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RecallFriendGuildTaskCfg.class, achieveId);
			int achieveType = cfg.getAchieveType().getValue();
			int value = guildAchieveItem.getValue(0);

			if (achieveTypeNumMap.containsKey(achieveType)){
				int before = achieveTypeNumMap.get(achieveType);
				if (value > before){
					achieveTypeNumMap.put(achieveType, value);
				}
			}else{
				achieveTypeNumMap.put(achieveType, value);
			}
		}
		for (Map.Entry<Integer, Integer> entry : achieveTypeNumMap.entrySet()) {
			int achieveType = entry.getKey();
			int value = entry.getValue();
			Activity.RecallGuildAchieveInfo.Builder builder = Activity.RecallGuildAchieveInfo.newBuilder();
			builder.setType(achieveType);
			builder.setNum(value);
			list.add(builder.build());
		}
		return list;
	}

	/**
	 * 构建消息体
	 * @param playerId
	 * @param playerIdList
	 * @param needIsRecall
	 * @return
	 */
	public List<Activity.RecallGuildBackFlowMsg> genPbRecallGuildBackFlowMsg(String playerId,RecallFriendEntity entity, List<String> playerIdList, boolean needIsRecall){
		List<Activity.RecallGuildBackFlowMsg> pbList = new ArrayList<>();
		for (String recallPlayerId : playerIdList) {
			Activity.RecallGuildBackFlowMsg.Builder builder = Activity.RecallGuildBackFlowMsg.newBuilder();
			builder.setPlayerId(recallPlayerId);
			String name = this.getDataGeter().getPlayerName(recallPlayerId);
			int icon = this.getDataGeter().getIcon(recallPlayerId);
			String pfIcon = this.getDataGeter().getPfIcon(recallPlayerId);
			int facLv = this.getDataGeter().getConstructionFactoryLevel(recallPlayerId);
			boolean isOnline = this.getDataGeter().isOnlinePlayer(recallPlayerId);
			builder.setNickName(name);
			builder.setIcon(icon);
			builder.setPfIcon(pfIcon);
			builder.setFacLv(facLv);
			builder.setIsOnline(isOnline);
			//是否需要此此字段,,已经召回的不需要
			if (needIsRecall){
				boolean isRecalled = entity.getRecallPlayerList().contains(recallPlayerId);
				if (isRecalled){
					builder.setRecallState(Activity.RecallState.RECALL_COMPLETE);
				}else{
					builder.setRecallState(Activity.RecallState.RECALL_NONE);
				}
			}
			pbList.add(builder.build());
		}
		return pbList;
	}

	/**
	 * 发奖励
	 * @param rewardData
	 */
	private void sendReward(String playerId, List<Reward.RewardItem.Builder> rewardData, int achieveId) {
		MailConst.MailId mailId = MailConst.MailId.RECALL_FRIEND_GUIULD_ACHIEVE_REWARD;
		//邮件发送奖励
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		Object[] content = new Object[0];
		// 邮件发送奖励
		sendMailToPlayer(playerId, mailId, title, subTitle, content, rewardData);
		HawkLog.logPrintln("RecallFriendActivity guildAchieve finish sendReward success, playerId: {}, achieveId:{}", playerId, achieveId);
	}

}
