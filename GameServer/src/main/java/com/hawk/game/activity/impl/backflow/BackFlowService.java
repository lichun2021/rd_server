package com.hawk.game.activity.impl.backflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkAppObj;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple3;
import org.hawk.util.JsonUtils;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowAccount;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.Friend.PlatformFriendInfo;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.PushService;
import com.hawk.game.service.RelationService;


/**
 * 老玩家回流数据
 * 
 * @author che
 *
 */
public class BackFlowService extends HawkAppObj {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 单例对象
	 */
	private static BackFlowService instance = null;
	
	/**
	 * 回归玩家角色信息
	 */
	private Map<String, BackFlowPlayer> backFlowPlayers = new ConcurrentHashMap<>();
	
	/**
	 * 符合流失推送的玩家信息
	 */
	private Set<String> lossPush = new ConcurrentHashSet<String>();
	
	
	/**
	 * 符合流失推荐的玩家信息
	 */
	private Set<String> lossRecommends = new ConcurrentHashSet<String>();
	
	/**
	 * 单独任务线程
	 */
	private ScheduledExecutorService scheduled;
	

	/**
	 * 下一次的推送时间
	 */
	private long nextPushTime;
	
	
	
	
	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static BackFlowService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public BackFlowService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}

	public boolean init() {
		
		scheduled = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName("BackFlowServiceTask");
				thread.setDaemon(true);
				return thread;
		}});
		scheduled.scheduleAtFixedRate(() -> {
			try {
				this.sendServerLossPushMessage();
			} catch (Exception e) {
				this.lossPush.clear();
				HawkException.catchException(e, "ThreadName:" + 
						Thread.currentThread().getName() + ": scheduled sendServerLossPushMessage error!");
				
			}
		}, 10000, 10000, TimeUnit.MILLISECONDS);
		scheduled.scheduleAtFixedRate(() -> {
			try {
				this.checkRecommendLoss();
			} catch (Exception e) {
				HawkException.catchException(e, "ThreadName:" + 
						Thread.currentThread().getName() + ": scheduled checkRecommendLoss error!");
			}
		}, 10000, 10000, TimeUnit.MILLISECONDS);
		return true;
	}
	
	/**
	 * 是否开放
	 * @return
	 */
	public boolean inWork(){
		int workFlag = ConstProperty.getInstance().getPushWorkFlag();
		if(workFlag == 0){
			return false;
		}
		List<String> list = ConstProperty.getInstance().getPushServerList();
		if(list.size() == 0){
			return true;
		}
		boolean inList = false;
		for(String serverId : list){
			boolean isLocal = GlobalData.getInstance().isLocalServer(serverId);
			if(isLocal){
				inList = true;
				break;
			}
		}
		return inList;
	}
	


	
	/**
	 *  玩家登录
	 * @param accountInfo
	 */
	public void onPlayerLogin(Player player, AccountInfo accountInfo) {
		this.checkBackFlowPlayer(player,accountInfo);
	}
	
	/***
	 * 跟运营同学确定的逻辑:
	 * 账号下所有角色满足回流条件才算回流，
	 * 一旦触发回流，所有角色都有回流活动，
	 * 去新服创建的角色没有回流活动。
	 * 
	 * @param openId
	 * @return
	 */
	private void checkBackFlowPlayer(Player player,AccountInfo accountInfo){
		String openId = player.getOpenId();
		BackFlowPlayer backPlayerData = this.loadBackFlowPlayer(player.getId());
		if(backPlayerData != null){
			this.backFlowPlayers.put(backPlayerData.getPlayerId(), backPlayerData);
			logger.info("checkBackFlowPlayer backPlayerData not null,openId: {},playerId:{},backTimeStamp:{} ",
					openId,player.getId(),backPlayerData.getBackTimeStamp());
		}
		long time = HawkTime.getMillisecond();
		long constTime = getLostTimeConst();
		long lastOutTime = accountInfo.getLogoutTime();
		long lossBegin =  this.getLossBeginTime(lastOutTime);
		//新建角色
		if(lastOutTime <= 0){
			return;
		}
		//玩家本服帐号离线时间不满足条件
		if (time - lossBegin < constTime) {
			logger.info("checkBackFlowPlayer lossTime less ,openId: {},playerId:{},lastOutTime:{} ",
					openId,player.getId(),lastOutTime);
			return;
		}
		int backTimes = 0;
		BackFlowAccount baccount = this.loadBackFlowAccount(openId);
		if(baccount != null){
			backTimes = baccount.getBackCount();
			logger.info("checkBackFlowPlayer BackFlowAccount not null ,openId: {},playerId:{},roleId:{},backCount:{},backTime:{}",
					openId,player.getId(),baccount.getPlayerId(),backTimes,baccount.getBackTime());
		}
		//是否是再次回流
		boolean backFlow = true;
		List<AccountRoleInfo> roleList = getPlayerAccountInfos(openId);
		for(AccountRoleInfo info : roleList){
			if(info.getLogoutTime() == 0L){
				backFlow = false;
				logger.info("checkBackFlowPlayer AccountRoleInfo new ,openId: {},playerId:{},backCount:{},newRole:{},newRoleServer:{}",
						openId,player.getId(),backTimes,info.getPlayerId(),info.getServerId());
				continue; //新注册的帐号进来
			}
			long roleLossBegin =  this.getLossBeginTime(info.getLogoutTime());
			if(time - roleLossBegin < constTime){
				logger.info("checkBackFlowPlayer back fail,openId: {},playerId:{},failRoleId:{},lastLogoutTime:{} ",
						openId,player.getId(),info.getPlayerId(),info.getLogoutTime());
				backFlow = false;
			}
		}
		//如果再次回流，则增加回流次数
		if(backFlow){
			backTimes ++;
			baccount = this.addPlayerBackFlowTimes(baccount,player,backTimes);
			if(baccount != null){
				logger.info("checkBackFlowPlayer BackFlowAccount backTimes add ,openId: {},playerId:{},backCount:{},backTime:{}",
						openId,player.getId(),backTimes,baccount.getBackTime());
			}
			
		}
		//未产生回流
		if(backTimes == 0){
			logger.info("checkBackFlowPlayer BackFlowAccount backTimes is zero ,openId: {},playerId:{}",
					openId,player.getId());
			return;
		}
		//对比回流次数，不一致则更新
		BackFlowPlayer backPlayer = this.getBackFlowPlayer(player.getId());
		if(backPlayer == null || backPlayer.getBackCount() < backTimes){
			BackFlowPlayer newBackPlayer = this.createBackPlayer(player,backTimes,time);
			this.backFlowPlayers.put(newBackPlayer.getPlayerId(), newBackPlayer);
			this.updateBackFlowPlayerRedis(newBackPlayer);
			logger.info("checkBackFlowPlayer BackFlowPlayer back sucess,openId: {},playerId:{},backCount:{},lastLogoutTime:{},backTime:{},vip:{}",
					openId,player.getId(),newBackPlayer.getBackCount(),newBackPlayer.getLogoutTime(),newBackPlayer.getBackTimeStamp(),newBackPlayer.getVipLevel());
		}
	}
	
	
	
	/**
	 * 创建回流角色
	 * @param player
	 * @param backTimes
	 * @param backTimeStamp
	 * @return
	 */
	public BackFlowPlayer createBackPlayer(Player player,int backTimes,long backTimeStamp){
		BackFlowPlayer bfplayer = new BackFlowPlayer();
		bfplayer.setOpenId(player.getOpenId());
		bfplayer.setPlayerId(player.getId());
		bfplayer.setPlayerLevel(player.getLevel());
		bfplayer.setVipLevel(player.getVipLevel());
		bfplayer.setCityLevel(player.getCityLevel());
		bfplayer.setBattlePoint(player.getPower());
		bfplayer.setServerId(player.getServerId());
		bfplayer.setPlatform(player.getPlatform());
		bfplayer.setLoginTime(player.getLoginTime());
		bfplayer.setLogoutTime(player.getLogoutTime());
		bfplayer.setBackCount(backTimes);
		bfplayer.setBackTimeStamp(backTimeStamp);
		bfplayer.setPushBackMessageTime(0L);
		return bfplayer;
		
	}
	
	/**
	 * 获取玩家所有同平台帐号列表
	 * @param accountInfo
	 * @return
	 */
	public List<AccountRoleInfo> getPlayerAccountInfos(String openId) {
		Map<String, String> map = RedisProxy.getInstance().getAccountRole(openId);
		List<AccountRoleInfo> list = new ArrayList<>();
		for (String value : map.values()) {
			AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
			list.add(roleInfoObj);
		}
		return list;
	}
	
	/**
	 * 添加回流次数
	 * @param openId
	 * @return
	 */
	public BackFlowAccount addPlayerBackFlowTimes(BackFlowAccount account,Player player,int backTimes){
		if(account == null){
			account = new BackFlowAccount();
		}
		account.setOpenId(player.getOpenId());
		account.setPlayerId(player.getId());
		account.setBackCount(backTimes);
		account.setBackTime(HawkTime.getMillisecond());
		String rlt = JSONObject.toJSONString(account);
		RedisProxy.getInstance().savePlayerBackFlowAccount(account.getOpenId(), rlt);
		return account;
	}
	
	/**
	 * 获取此账号的回流次数
	 * @param openId
	 * @return
	 */
	public BackFlowAccount loadBackFlowAccount(String openId){
		String accountInfo = RedisProxy.getInstance().getPlayerBackFlowAccount(openId);
		if(!HawkOSOperator.isEmptyString(accountInfo)){
			BackFlowAccount baccount = JSONObject.parseObject(accountInfo, BackFlowAccount.class);
			return baccount;
		}
		return null;
	}
	
	/**
	 * 从redis中加载数据
	 * @param openId
	 * @return
	 */
	private  BackFlowPlayer loadBackFlowPlayer(String playerId){
		String roleInfo = RedisProxy.getInstance().getPlayerBackFlow(playerId);
		if (HawkOSOperator.isEmptyString(roleInfo)) {
			return null;
		}
		BackFlowPlayer backFlowPlayer = JSONObject.parseObject(roleInfo, BackFlowPlayer.class);
		return backFlowPlayer;
	}
	
	
	/**
	 * 更新数据到redis
	 * @param backPlayer
	 */
	private void updateBackFlowPlayerRedis(BackFlowPlayer backPlayer){
		if(backPlayer == null){
			return;
		}
		String rlt = JsonUtils.Object2Json(backPlayer);
		RedisProxy.getInstance().savePlayerBackFlow(backPlayer.getPlayerId(), rlt);
	}
	
	/***
	 * 获取流失时间长度(ms)
	 * @return
	 */
	private  long getLostTimeConst(){
		return HawkTime.DAY_MILLI_SECONDS * 
				ConstProperty.getInstance().getPlayerLossDays();
	}
	
	
	
	/**
	 * 获取老玩家信息
	 * @param openId
	 * @return
	 */
	public BackFlowPlayer getBackFlowPlayer(String playerId){
		BackFlowPlayer blayer = this.backFlowPlayers.get(playerId);
		return  blayer;
	}
	
	
	/**
	 * 重置推荐好友
	 */
	public void checkRecommendLoss(){
		if(!this.inWork()){
			logger.info("checkRecommendLoss not inwork...");
			return;
		}
		if(this.lossRecommends.size() <= 0){
			this.loadRecommends();
		}
	}
	
	
	/**
	 * 
	 * 获取流失角色推荐
	 * @return
	 */
	public void loadRecommends(){
		long curTime = HawkTime.getMillisecond();
		Set<String> lossSet = new ConcurrentHashSet<String>();
		Set<String>  playerIds = GlobalData.getInstance().getAllPlayerIds();
		for(String pid : playerIds){
			if(GlobalData.getInstance().isOnline(pid)){
				continue;
			}
			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(pid);
			if(account == null){
				continue;
			}
			long roleLossBegin =  this.getLossBeginTime(account.getLogoutTime());
			int lossDays = (int) ((curTime - roleLossBegin)/HawkTime.DAY_MILLI_SECONDS);
			if(lossDays <=0 ){
				continue;
			}
			if(this.getPushNoticeId(lossDays) != null){
				lossSet.add(pid);
			}
		}
		this.lossRecommends = lossSet;
		logger.info("loadRecommends ,lossRecommends size :{}",this.lossRecommends.size());
	}
	
	
	
	/**
	 * 获取流失推荐
	 * @param playerId
	 * @return
	 */
	public Set<String> getLossRecommendRoles(String playerId){
		int num = ConstProperty.getInstance().getPlayerLossRecommendNum();
		Set<String> recommendSet = new HashSet<String>();
		Set<String> delSet = new HashSet<String>();
		long curTime = HawkTime.getMillisecond();
		if(this.lossRecommends.size() <= 0){
			return recommendSet;
		}
		for(String pId : this.lossRecommends){
			if(num == 0){
				break;
			}
			if(pId.equals(playerId)){
				delSet.add(pId);
				continue;
			}
			if (RelationService.getInstance().isFriend(playerId, pId)) {
				continue;
			}
			if (RelationService.getInstance().isAlreadyApply(playerId,pId)) {
				continue;
			}
			if (RelationService.getInstance().isAlreadyApply(pId, playerId)) {
				continue;
			}
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(pId);
			if(accountInfo == null){
				continue;
			}
			long lossBegin = this.getLossBeginTime(accountInfo.getLogoutTime());
			int lossDays = (int) ((curTime - lossBegin)/HawkTime.DAY_MILLI_SECONDS);
			if(this.getPushNoticeId(lossDays) == null){
				delSet.add(pId);
				continue;
			}
			recommendSet.add(pId);
			delSet.add(pId);
			num--;
		}
		//删除过期的和已经被选中的
		this.lossRecommends.removeAll(delSet);
		return recommendSet;
	}
	

	/**
	 * 发送全服推送
	 */
	public void sendServerLossPushMessage(){
		if(!this.inWork()){
			logger.info("sendServerLossPushMessage not in work...");
			return;
		}
		//加载推送玩家
		this.loadLossPush();
		//列表中是否有数据，如果没有则退出
		logger.info("sendServerLossPushMessage loadLossPushSize: {} ",this.lossPush.size());
		if(this.lossPush.size() <= 0){
			return;
		}
		//每次只处理50条流失玩家
		Set<String> pushSet = new HashSet<String>();
		Map<String,Long> savePushTimes = new HashMap<String,Long>();
		Map<String,String> openIds = new HashMap<String,String>();
		long curTime = HawkTime.getMillisecond();
		for(String playerId : this.lossPush){
			pushSet.add(playerId);
			if(pushSet.size() >= 50){
				break;
			}
		}
		if(pushSet.size() > 0){
			this.lossPush.removeAll(pushSet);
		}
		this.serverLossPushStageOne(pushSet,openIds);
		this.serverLossPushStageTwo(openIds);
		this.serverLossPushStageThree(openIds, savePushTimes);
		if(savePushTimes.size() > 0){
			RedisProxy.getInstance().savePlayerBackMessagePush(savePushTimes);
		}
		long useTime = HawkTime.getMillisecond() - curTime;
		logger.info("sendServerLossPushMessage use time ,lossRecommends size :{}",useTime);
		//如果占用时间过长，则清除
		if(useTime > 3000){
			this.lossPush.clear();
		}
	}
	
	
	/**
	 * 过滤掉不到发送时间的账号
	 * @param ids
	 * @param saveMap
	 */
	public void serverLossPushStageOne(Set<String> ids,Map<String,String> openIds){
		if(ids == null || ids.size() <= 0){
			return;
		}
		for(String playerId : ids){
			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(account == null){
				logger.info("sendServerLossPushMessage AccountInfo null ,playerId:{}",playerId);
				continue;
			}
			if(GlobalData.getInstance().isOnline(playerId)){
				logger.info("sendServerLossPushMessage player Onlie ,playerId:{}",playerId);
				continue;
			}
			String[] puidArr = account.getPuid().split("#");
			String openId = puidArr[0];
			openIds.put(playerId, openId);
		}
	}
	
	/**
	 * 查看是否已经发送过
	 * @param ids
	 * @param saveMap
	 */
	public void serverLossPushStageTwo(Map<String,String> openIds){
		if(openIds == null || openIds.size() <= 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		Set<String> openIdset = new HashSet<String>(openIds.values());
		Map<String,Long> timeMap = RedisProxy.getInstance().getPushTimeMap(openIdset);
		if(timeMap == null){
			return;
		}
		Set<String> dels = new HashSet<String>();
		for(Entry<String, String> entry : openIds.entrySet() ){
			String playerId = entry.getKey();
			String openId = entry.getValue();
			if(!timeMap.containsKey(openId)){
				dels.add(playerId);
				continue;
			}
			long time = timeMap.get(openId);
			if(curTime < time){
				//不到推送时间
				dels.add(playerId);
				logger.info("sendPushMessage curTime < pushTime ,load from redis,openId: {} playerId: {}, nextPush:{}",
						openId,playerId,HawkTime.formatTime(time));
			}
		}
		//删除不需要推送的
		for(String del :dels ){
			openIds.remove(del);
		}
	}
	
	
	public void serverLossPushStageThree(Map<String,String> openIds,Map<String,Long> saveMap){
		if(openIds == null || openIds.size() <= 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		Set<String> openIdset = new HashSet<String>(openIds.values());
		Map<String,List<AccountRoleInfo>> rolesMap = RedisProxy.getInstance().getAccountRoleMap(openIdset);
		if(rolesMap == null){
			return;
		}
		logger.info("sendPushMessage rolesMap size,openIdSize: {} rolesMapSize: {}",
				openIds.size(),rolesMap.size());
		for(Entry<String, String> entry : openIds.entrySet()){
			String playerId = entry.getKey();
			String openId = entry.getValue();
			if(!rolesMap.containsKey(openId)){
				continue;
			}
			List<AccountRoleInfo> roleList = rolesMap.get(openId);
			if(roleList == null){
				logger.info("sendPushMessage roleList null,openId: {} playerId: {}",
						openId,playerId);
				continue;
			}
			if(roleList.size() <= 0){
				logger.info("sendPushMessage roleList size 0,openId: {} playerId: {}",
						openId,playerId);
				continue;
			}
			
			long lastLoginTime = 0;
			String otherRolePlayerId = "";
			String otherRoleServerId = "";
			boolean isNew = false;
			for(AccountRoleInfo info : roleList){
				//新注册的帐号进来,直接放进列表,检查时间点为流失限制天数后
				if(info.getLogoutTime() == 0L){
					logger.info("sendPushMessage info.getLogoutTime() == 0,openId: {} playerId: {}, accountRoleId:{},serverId:{}",
							openId,playerId,info.getPlayerId(),info.getServerId());
					isNew = true;
					break;
				}
				//获取最近的登出时间
				if(lastLoginTime < info.getLogoutTime()){
					lastLoginTime = info.getLogoutTime();
					otherRolePlayerId = info.getPlayerId();
					otherRoleServerId = info.getServerId();
				}
			}
			if(isNew){
				continue;
			}
			long lossBegin = this.getLossBeginTime(lastLoginTime);
			int lossDays = (int) ((curTime - lossBegin)/HawkTime.DAY_MILLI_SECONDS);
			PushMsgType nid = this.getPushNoticeId(lossDays);
			if(nid != null){
				PushService.getInstance().pushMsg(playerId, nid.getNumber());
				long pushTime = HawkTime.getAM0Date().getTime() + 
						HawkTime.DAY_MILLI_SECONDS * ConstProperty.getInstance().getPlayerLossPushCD();
				saveMap.put(openId, pushTime);
				logger.info("sendPushMessage sucess,openId: {} playerId: {}, accountRoleId:{},serverId:{},logoutTime:{},nextPush:{}",
						openId,playerId,otherRolePlayerId,otherRoleServerId,HawkTime.formatTime(lastLoginTime),HawkTime.formatTime(pushTime));
			}
		}
	}
	
	
	
	/**
	 * 加载推送
	 * @param playerIds
	 * @param curTime
	 */
	public void loadLossPush(){
		long curTime = HawkTime.getMillisecond();
		int pushHour = ConstProperty.getInstance().getLossPushTime();
		if(this.nextPushTime == 0){
			int pushRadomAdd = HawkRand.randInt(60);
			this.nextPushTime = HawkTime.getAM0Date().getTime() + pushHour * HawkTime.HOUR_MILLI_SECONDS;
			if(curTime >= this.nextPushTime){
				this.nextPushTime = HawkTime.getNextAM0Date() + pushHour * HawkTime.HOUR_MILLI_SECONDS;
			}
			this.nextPushTime += pushRadomAdd * HawkTime.MINUTE_MILLI_SECONDS;
			logger.info("sendServerLossPushMessage init,nextPushTime: {} ",HawkTime.formatTime(this.nextPushTime));
			return;
		}
		if(curTime < this.nextPushTime){
			return;
		}
		int pushRadomAdd = HawkRand.randInt(60);
		this.nextPushTime = HawkTime.getNextAM0Date() + pushHour * HawkTime.HOUR_MILLI_SECONDS
				+ pushRadomAdd * HawkTime.MINUTE_MILLI_SECONDS;
		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
		logger.info("sendServerLossPushMessage loadLossPush,nextPushTime: {} ",HawkTime.formatTime(this.nextPushTime));
		this.lossPush.clear();
		Set<String> pushs = new ConcurrentHashSet<String>();
		for(String playerId : playerIds){
			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(account.getLogoutTime() == 0){
				continue;
			}
			//如果在本服的角色未登陆天数超过限制，则加入推送列表
			long lastLogoutTime = account.getLogoutTime();
			long lossBegin = this.getLossBeginTime(lastLogoutTime);
			int lossDays = (int) ((curTime - lossBegin)/HawkTime.DAY_MILLI_SECONDS);
			PushMsgType nid = this.getPushNoticeId(lossDays);
			if(nid != null){
				pushs.add(playerId);
			}
		}
		this.lossPush = pushs;
	}
	
	
	/**
	 * 获取流失开始时间(登出当天的第二天零点)
	 * @param time
	 * @return
	 */
	public long getLossBeginTime(long time){
		return HawkTime.getAM0Date(new Date(time)).getTime() +
				HawkTime.DAY_MILLI_SECONDS;
		
	}
	
	/**
	 * 获取推送消息ID
	 * @param lossDays
	 * @return
	 */
	public PushMsgType getPushNoticeId(int lossDays){
		List<HawkTuple3<Integer, Integer, List<Integer>>> timeArr = ConstProperty.getInstance().
				getPlayerLossPushNoticeList();
		for(HawkTuple3<Integer, Integer, List<Integer>> tuple : timeArr){
			int lossBegin = tuple.first;
			int lossEnd = tuple.second;
			if(lossBegin<=lossDays && lossDays<=lossEnd){
				List<Integer> nids = tuple.third;
				int ran = HawkRand.randInt(nids.size() -1);
				int nid = nids.get(ran);
				return PushMsgType.valueOf(nid);
			}
		}
		return null;
	}
	
	
	
	/**
	 * 获取推送最低限制
	 * @return
	 */
	public int getLossPushMinLimit(int defaut){
		List<HawkTuple3<Integer, Integer, List<Integer>>> timeArr = ConstProperty.getInstance().
				getPlayerLossPushNoticeList();
		if(timeArr.size() <= 0){
			return defaut;
		}
		return timeArr.get(0).first;
	}
	
	/**
	 * 获取推送最低限制
	 * @return
	 */
	public int getLossPushMaxLimit(int defaut){
		List<HawkTuple3<Integer, Integer, List<Integer>>> timeArr = ConstProperty.getInstance().
				getPlayerLossPushNoticeList();
		if(timeArr.size() <= 0){
			return defaut;
		}
		int index = timeArr.size() -1 ;
		return timeArr.get(index).second;
	}
	
	
	
	/**
	 * 好友申请，推送
	 * @param targetList
	 */
	public void sendPushOnAddFriend(List<String> targetList){
		if(!this.inWork()){
			logger.info("sendPushOnAddFriend not in work...");
			return;
		}
		HawkTask task = new HawkTask() {
			public Object run() {
				long curTime = HawkTime.getMillisecond();
				int configLimitDay = ConstProperty.getInstance().getPlayerFriendLossDaysPush();
				for(String playerId : targetList){
					AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
					if(account == null){
						continue;
					}
					String[] puidArr = account.getPuid().split("#");
					String openId = puidArr[0];
					long lossBegin = getLossBeginTime(account.getLogoutTime());
					if(curTime - lossBegin  < configLimitDay * HawkTime.DAY_MILLI_SECONDS){
						logger.info("sendPushOnAddFriend fail,curTime - lossBegin,openId: {} playerId: {},LogoutTime:{}",openId,playerId,account.getLogoutTime());
						continue;
					}
					long sendCount = RedisProxy.getInstance().getSendFriendPushTimesToday(openId);
					if(sendCount >= ConstProperty.getInstance().getPlayerFriendLossPushReceiveNum()){
						logger.info("sendPushOnAddFriend fail,sendCount > config,openId: {} playerId: {},send:{}",openId,playerId,sendCount);
						continue;
					}
					List<AccountRoleInfo> roleList = getPlayerAccountInfos(openId);
					if(!friendIsLoss(roleList)){
						logger.info("sendPushOnAddFriend fail,friendIsLoss not loss,openId: {} playerId: {},send:{}",openId,playerId,sendCount);
						continue;
					}
					PushService.getInstance().pushMsg(playerId, PushMsgType.LOSS_FRIEND_APPLY_PUSH_VALUE);
					logger.info("sendPushOnAddFriend sucess,openId: {} playerId: {}",openId,playerId);
					Set<String> openIds = new HashSet<String>();
					openIds.add(openId);
					RedisProxy.getInstance().saveSendFriendPushTimesToday(openIds);
				}
				return null;
			}
		};
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("SEND_PUSH_ON_ADD_FRIEND_APPLY");
			taskPool.addTask(task);
		}
		
	}
	
	
	/**
	 * 好友推送
	 * 
	 * @param playerIdMap
	 */
	public void sendFriendPush(Player player,boolean isPlatformFriend, Map<String, String> playerIdMap) {
		if(!this.inWork()){
			logger.info("sendFriendPush not inwork: {}",player.getId());
			return;
		}
		Map<String, String> pmap = new HashMap<>();
		pmap.putAll(playerIdMap);
		HawkTask task = new HawkTask() {
			public Object run() {
				if(isPlatformFriend){
					sendPlatformFriendsPush(player, pmap);
				} else {
					sendGameFriendsPush(player.getId(), pmap);
				}
				return null;
			}
		};
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("SEND_FRIEND_PUSH");
			taskPool.addTask(task);
		}
	}
	
	/**
	 * 平台好友推送
	 * @param player
	 * @param playerIdMap
	 */
	public void sendPlatformFriendsPush(Player player,Map<String, String> playerIdMap){
		logger.info("sendPlatformFriendsPush begin,player: {}",player.getId());
		long curTime = HawkTime.getMillisecond();
		List<PlatformFriendInfo.Builder>  platformFriendList = RelationService.getInstance().getPlatformFriendList(player);
		Map<String,PlatformFriendInfo.Builder> lastLoginTimeMap = new HashMap<>();
		for(PlatformFriendInfo.Builder builder : platformFriendList){
			lastLoginTimeMap.put(builder.getPlayerId(), builder);
		}
		Map<String,String> openIds = new HashMap<String,String>();
		Set<String> saveSet = new HashSet<String>();
		int configLimitMinDay = ConstProperty.getInstance().getPlayerFriendLossDaysPush();
		logger.info("sendPlatformFriendsPush configLimitMinDay,player: {},configLimitMinDay:{}",
				player.getId(),configLimitMinDay);
		int configLimitMaxDay = this.getLossPushMaxLimit(100);
		logger.info("sendPlatformFriendsPush configLimitMaxDay,player: {},configLimitMaxDay:{}",
				player.getId(),configLimitMaxDay);
		for (Entry<String, String> entry : playerIdMap.entrySet()) {
			String friendPlayerId = entry.getKey();
			PlatformFriendInfo.Builder builder = lastLoginTimeMap.get(friendPlayerId);
			if(builder == null){
				continue;
			}
			long lossBegin = getLossBeginTime(builder.getLogoutTime() );
			if(curTime - lossBegin  < configLimitMinDay * HawkTime.DAY_MILLI_SECONDS){
				logger.info("sendPlatformFriendsPush loss less,player: {},configLimitMaxDay:{},lossTime:{}",
						player.getId(),friendPlayerId,lossBegin);
				continue;
			}
			if(curTime - lossBegin > configLimitMaxDay *  HawkTime.DAY_MILLI_SECONDS){
				logger.info("sendPlatformFriendsPush loss great,player: {},configLimitMaxDay:{},lossTime:{}",
						player.getId(),friendPlayerId,lossBegin);
				continue;
			}
			openIds.put(builder.getOpenid(),friendPlayerId);
		}
		Map<String,Long> pushCountMap = RedisProxy.getInstance().
				getFriendsPushCountMapToday(openIds.keySet());
		long sendCountLimit = ConstProperty.getInstance().getPlayerFriendLossPushReceiveNum();
		for (Entry<String, Long> entry : pushCountMap.entrySet()) {
			String openId = entry.getKey();
			long sendCount = entry.getValue();
			if(sendCount > sendCountLimit){
				logger.info("sendPlatformFriendsPush frind get greater,player: {},sendCountLimit:{},sendCount:{}",
						player.getId(),sendCountLimit,sendCount);
				continue;
			}
			String friendId = openIds.get(openId);
			String friendServerId =  playerIdMap.get(friendId);
			saveSet.add(openId);
			boolean sameServer = GlobalData.getInstance().isLocalServer(friendServerId);
			if (sameServer) {
				PushService.getInstance().pushMsg(friendId, PushMsgType.LOSS_PLATFORM_FRIEND_PUSH_VALUE);
				logger.info("sendPlatformFriendsPush sucess,friendId: {}",friendId);
			}else{
				GmProxyHelper.proxyCall(friendServerId, "sendFriendLossPush", "playerId=" + friendId, 2000);
				logger.info("sendPlatformFriendsPush sucess,friendId: {}",friendId);
			}
			
		}
		if(saveSet.size() > 0){
			RedisProxy.getInstance().saveSendFriendPushTimesToday(saveSet);
		}
		
		
			
	}
	
	public void sendGameFriendsPush(String playerId,Map<String, String> playerIdMap){
		Map<String,String> openIds = new HashMap<String,String>();
		Set<String> saveSet = new HashSet<String>();
		sendGameFriendsPushOne(playerId,playerIdMap,openIds);
		sendGameFriendsPushTwo(playerId,openIds);
		sendGameFriendsPushThree(playerId,openIds,saveSet);
		if(saveSet.size() > 0){
			RedisProxy.getInstance().saveSendFriendPushTimesToday(saveSet);
		}
	}
	
	
	/**
	 * 过滤掉不到发送时间的账号
	 * @param ids
	 * @param saveMap
	 */
	public void sendGameFriendsPushOne(String senderId,Map<String,String> ids,Map<String,String> openIds){
		if(ids == null || ids.size() <= 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		int configLimitMinDay = ConstProperty.getInstance().getPlayerFriendLossDaysPush();
		int configLimitMaxDay = this.getLossPushMaxLimit(100);
		
		for(Entry<String,String> entry : ids.entrySet() ){
			String playerId = entry.getKey();
			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(account == null){
				continue;
			}
			if(account.getLogoutTime() == 0){
				logger.info("sendGameFriendsPushOne new,player: {},friend:{},configLimitMinDay:{},configLimitMaxDay:{},lossTime:{}",
						senderId,playerId,configLimitMinDay,configLimitMaxDay,0);
				continue;
			}
			long lossBegin = getLossBeginTime(account.getLogoutTime());
			if(curTime - lossBegin  < configLimitMinDay * HawkTime.DAY_MILLI_SECONDS){
				logger.info("sendGameFriendsPushOne loss less,player: {},friend:{},configLimitMinDay:{},configLimitMaxDay:{},lossTime:{}",
						senderId,playerId,configLimitMinDay,configLimitMaxDay,lossBegin);
				continue;
			}
			if(curTime - lossBegin > configLimitMaxDay *  HawkTime.DAY_MILLI_SECONDS){
				logger.info("sendGameFriendsPushOne loss greater,player: {},friend:{},configLimitMinDay:{},configLimitMaxDay:{},lossTime:{}",
						senderId,playerId,configLimitMinDay,configLimitMaxDay,lossBegin);
				continue;
			}
			String[] puidArr = account.getPuid().split("#");
			String openId = puidArr[0];
			openIds.put(openId, playerId);
		}
	}
	
	/**
	 * 查看是否已经发送过
	 * @param ids
	 * @param saveMap
	 */
	public void sendGameFriendsPushTwo(String senderId,Map<String,String> openIds){
		if(openIds == null || openIds.size() <= 0){
			return;
		}
		Map<String,Long> pushCountMap = RedisProxy.getInstance().getFriendsPushCountMapToday(openIds.keySet());
		if(pushCountMap == null){
			logger.info("sendGameFriendsPushOne pushCountMap null,player:{}",senderId);
			return;
		}
		int limitCount = ConstProperty.getInstance().getPlayerFriendLossPushReceiveNum();
		Set<String> dels = new HashSet<String>();
		for(Entry<String,String> entry : openIds.entrySet() ){
			String openId = entry.getKey();
			String playerId = entry.getValue();
			if(!pushCountMap.containsKey(openId)){
				dels.add(openId);
				logger.info("sendGameFriendsPushOne pushCountMap not contain,player:{},friend: {}",senderId,playerId);
				continue;
			}
			long pushCount = pushCountMap.get(openId);
			if(pushCount >= limitCount){
				dels.add(openId);
				logger.info("sendPushMessage pushCount >= limitCount ,load from redis,player:{},friend: {},openId: {}",senderId,playerId,openId);
			}
		}
		//删除不需要推送的
		for(String del : dels){
			openIds.remove(del);
		}
	}
	
	
	public void sendGameFriendsPushThree(String senderId,Map<String,String> openIds,Set<String> saveSet){
		if(openIds == null || openIds.size() <= 0){
			return;
		}
		Map<String,List<AccountRoleInfo>> rolesMap = RedisProxy.getInstance().
				getAccountRoleMap(openIds.keySet());
		if(rolesMap == null){
			logger.info("sendGameFriendsPushThree rolesMap null,player:{}",senderId);
			return;
		}
		for(String openId : openIds.keySet()){
			String playerId = openIds.get(openId);
			if(!rolesMap.containsKey(openId)){
				logger.info("sendGameFriendsPushThree rolesMap !containsKey,player:{},friend:{}",senderId,playerId);
				continue;
			}
			List<AccountRoleInfo> rlist = rolesMap.get(openId);
			boolean send = this.friendIsLoss(rlist);
			logger.info("sendGameFriendsPushThree friendIsLoss,player:{},friend:{}",senderId,playerId);
			if(send){
				saveSet.add(openId);
				PushService.getInstance().pushMsg(playerId, PushMsgType.LOSS_GAME_FRIEND_PUSH_VALUE);
				logger.info("sendGameFriendsPush sucess,playerId: {},friend:{}",senderId,playerId);
			}
		}
	}
	
	/**
	 * 好友是否流失
	 * @param openId
	 * @return
	 */
	public boolean friendIsLoss(List<AccountRoleInfo> roleList){
		long curTime = HawkTime.getMillisecond();
		int configLimitMinDay = ConstProperty.getInstance().getPlayerFriendLossDaysPush();
		int configLimitMaxDay = this.getLossPushMaxLimit(100);
		long lossBegin = 0;
		for(AccountRoleInfo info : roleList){
			if(info.getLogoutTime() == 0L){
				return false; //新注册的帐号进来
			}
			long infoLossBegin = getLossBeginTime(info.getLogoutTime());
			if(infoLossBegin > lossBegin){
				lossBegin = infoLossBegin;
			}
		}
		if(curTime - lossBegin  < configLimitMinDay * HawkTime.DAY_MILLI_SECONDS){
			return false;
		}
		if(curTime - lossBegin > configLimitMaxDay *  HawkTime.DAY_MILLI_SECONDS){
			return false;
		}
		return true;
	}
	
	
	
	
	
}
