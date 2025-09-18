package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.FriendInviteTaskCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GuardianAttributeCfg;
import com.hawk.game.config.GuardianConstConfig;
import com.hawk.game.config.GuardianGiftCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.data.FriendInviteInfo;
import com.hawk.game.data.FriendInviteInfo.TaskAttrInfo;
import com.hawk.game.data.UnregFriendTaskInfo;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerGuardInviteEntity;
import com.hawk.game.entity.PlayerRelationApplyEntity;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.msg.CheckGuardDressMsg;
import com.hawk.game.msg.ClearRelationApplyMsg;
import com.hawk.game.msg.MigrateOutPlayerMsg;
import com.hawk.game.msg.UpdateGuardDressMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Friend.BlackListMsg;
import com.hawk.game.protocol.Friend.CanInvitePlayerMsg;
import com.hawk.game.protocol.Friend.FriendApplyMsg;
import com.hawk.game.protocol.Friend.FriendMsg;
import com.hawk.game.protocol.Friend.GuardCanInvitePlayersResp;
import com.hawk.game.protocol.Friend.GuardDressItemInfo;
import com.hawk.game.protocol.Friend.GuardDressResp;
import com.hawk.game.protocol.Friend.GuardHudSyn;
import com.hawk.game.protocol.Friend.GuardInfoResp;
import com.hawk.game.protocol.Friend.GuardInfoUpdateSyn;
import com.hawk.game.protocol.Friend.GuardInviteDeleteSyn;
import com.hawk.game.protocol.Friend.GuardInviteListResp;
import com.hawk.game.protocol.Friend.GuardInvitePlayerResp;
import com.hawk.game.protocol.Friend.GuardPlayerMsg;
import com.hawk.game.protocol.Friend.GuardRelationCreateSyn;
import com.hawk.game.protocol.Friend.HateInfoMsg;
import com.hawk.game.protocol.Friend.InviteState;
import com.hawk.game.protocol.Friend.InvitedFriendData;
import com.hawk.game.protocol.Friend.InvitedFriendInfoPB;
import com.hawk.game.protocol.Friend.OperationFrom;
import com.hawk.game.protocol.Friend.OperationType;
import com.hawk.game.protocol.Friend.PlatformFriendInfo;
import com.hawk.game.protocol.Friend.StrangerMsg;
import com.hawk.game.protocol.Friend.SynBlackList;
import com.hawk.game.protocol.Friend.SynDeleteBlackList;
import com.hawk.game.protocol.Friend.SynDeleteFriend;
import com.hawk.game.protocol.Friend.SynFriendApplyUpdate;
import com.hawk.game.protocol.Friend.SynFriendInfo;
import com.hawk.game.protocol.Friend.SyncHateRankList;
import com.hawk.game.protocol.Friend.UnRegFriendInfoPB;
import com.hawk.game.protocol.Friend.UnRegFriendsPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.protocol.Player.ImageSource;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.PlayerCommon;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.RelationType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.SDKManager;
import redis.clients.jedis.Tuple;

/**
 * 判断好友关系的时候都是在线判断的,而玩家在线基本上数据都加载了。
 * @author jm
 *
 */
public class RelationService extends HawkAppObj {
	
	private static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 存储玩家所有的列表
	 */
	private Map<String, Map<String, PlayerRelationEntity>> relationMap;
	/**
	 *好友数量统计 
	 */
	private Map<String, AtomicInteger> friendNumMap;
	
	/**
	 * 黑名单数量统计
	 */
	private Map<String, AtomicInteger> blacklistNumMap;
	/**
	 *申请列表
	 */
	private Map<String, Map<String, PlayerRelationApplyEntity>> relationApplyMap;
	
	/**
	 * 平台好友信息 <playerId, friendInfoSet>
	 */
	Cache<String,List<PlatformFriendInfo.Builder>> platformFriendCache;
	
	/**
	 * 微信或QQ密友邀请信息
	 */
	Map<String, FriendInviteInfo> friendInviteInfoMap;
	/**
	 * 未注册好友信息<playerId, UnregFriendTaskInfo>
	 */
	Map<String, UnregFriendTaskInfo> unregFriendInfoCache;
	
	/**
	 * 跨服的
	 * {TargetPlayerId, guardValue, dressId}
	 */
	Map<String, HawkTuple3<String, Integer, Integer>> csGuardMap = new ConcurrentHashMap<>();
	
	/**
	 * 记录玩家和守护值, 冗余缓存为了提升效率， 这个值涉及到属性的加成，属性调用太频繁了.
	 */
	Map<String, PlayerRelationEntity> playerGuardMap = new ConcurrentHashMap<>();
	
	/**
	 * 玩家的守护邀请.
	 */
	Map<String, PlayerGuardInviteEntity> guardInviteMap = new ConcurrentHashMap<>();
	
	/**
	 * 离线对玩家的装扮tick
	 */
	Map<String, Map<Integer, Integer>> offlinePlayer = new ConcurrentHashMap<>();
	
	/**
	 * 单例
	 */
	private static RelationService instance = null;
	
	public RelationService(HawkXID xid) {
		super(xid);	
		
		instance = this;
	}
	
	public static RelationService getInstance(){
		return instance;
	}
	
	public boolean init(){
		relationMap = new ConcurrentHashMap<>(2000);
		friendNumMap = new ConcurrentHashMap<>(2000);
		blacklistNumMap = new ConcurrentHashMap<>(2000);
		relationApplyMap = new ConcurrentHashMap<>(2000);
		// 写入后半小时自动过期
		platformFriendCache = CacheBuilder.newBuilder().expireAfterWrite(120, TimeUnit.SECONDS).build();
		friendInviteInfoMap = new ConcurrentHashMap<String, FriendInviteInfo>();
		unregFriendInfoCache = new ConcurrentHashMap<String, UnregFriendTaskInfo>();
		
		//加载守护邀请
		loadGuardInvite();
		
		//加载部分玩家的好友信息(有守护关系的好友)
		preloadGuard();
		
		//加载跨服的数据.
		loadAllCsPlayerGuard();
		
		addTickable(new HawkPeriodTickable(3000, 3000){
			@Override
			public void onPeriodTick() {
				if (!GsApp.getInstance().isInitOK()) {
					return;
				}
				checkInviteTimeOut();
				checkGuardDressTimeOut();
			}		
		});
		
		return true;
	}
	
	
	/**
	 * 加载守护的玩家信息
	 */
	private void preloadGuard() {					
		List<PlayerRelationEntity> entitys = HawkDBManager.getInstance().query("from PlayerRelationEntity where guard = true and invalid = 0");
		if (CollectionUtils.isEmpty(entitys)) {
			return;
		}
		
		//加载守护信息
		for (PlayerRelationEntity relationEntity : entitys) {			
			playerGuardMap.put(relationEntity.getPlayerId(), relationEntity);
		}  
	}
	

	/**
	 * 加载守护邀请信息.
	 * @return
	 */
	public void loadGuardInvite() {
		Map<String, PlayerGuardInviteEntity> map = LocalRedis.getInstance().loadAllGuardInvite();
		guardInviteMap.putAll(map);
		return;
	}
	
	public void loadData(String playerId, boolean isLogin) {
		this.loadPlayerRelationApplyData(playerId, isLogin);
		this.loadPlayerRelationData(playerId, isLogin);		
	}
	
	/**
	 * 上线加载数据到内存
	 * @param playerId
	 */
	public void loadData(String playerId) {
		this.loadData(playerId, false);
	}
	
	/**
	 * 获取好友关系
	 * @param playerId
	 * @param targetId
	 * @return
	 */
	public PlayerRelationEntity getPlayerRelationEntity(String playerId, String targetId){
		return getPlayerRelationMap(playerId).get(targetId);
	}
	
	/**
	 * 获取玩家的好友关系列表
	 * @param playerId
	 * @return
	 */
	public Map<String, PlayerRelationEntity> getPlayerRelationMap(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return new HashMap<>();
		}
		Map<String, PlayerRelationEntity> map = relationMap.get(playerId);
		if(map == null) {
			return loadPlayerRelationData(playerId, false);
		}
		
		return map;
	}
	
	/**
	 * 获取不同的关系列表
	 * @param playerId
	 * @param type
	 * @return
	 */
	public List<PlayerRelationEntity> getPlayerRelationList(String playerId, int type){
		Map<String, PlayerRelationEntity> entityMap = getPlayerRelationMap(playerId);
		List<PlayerRelationEntity> entityList = new ArrayList<>(entityMap.size());
		for(PlayerRelationEntity entity : entityMap.values()) {
			if(entity.getType() == type){
				entityList.add(entity);
			}			
		}
		
		return entityList;
	}
	
	/**
	 * 获取不同的关系id列表
	 * @param playerId
	 * @param type
	 * @return
	 */
	public List<String> getPlayerRelationIdList(String playerId, int type){
		Map<String, PlayerRelationEntity> entityMap = getPlayerRelationMap(playerId);
		List<String> relationIds = new ArrayList<>(entityMap.size());
		for (Entry<String, PlayerRelationEntity> entry : entityMap.entrySet()) {
			if(entry.getValue().getType() == type){
				relationIds.add(entry.getKey());
			}
		}
		return relationIds;
	}
	
	/**
	 * 假如有多个线程执行到此,也只会多读取一次mysql，并不会产生影响
	 * @param playerId
	 */
	public Map<String, PlayerRelationEntity> loadPlayerRelationData(String playerId, boolean isLogin) {
		Map<String, PlayerRelationEntity> map = relationMap.get(playerId);
		if(map == null){
			map = new ConcurrentHashMap<>();
			
			AtomicInteger friendNum = new AtomicInteger(0);
			AtomicInteger blacklistNum = new AtomicInteger(0);	
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if (accountInfo != null && !accountInfo.isNewly()) {	
				List<PlayerRelationEntity> entitys = HawkDBManager.getInstance().query("from PlayerRelationEntity where playerId=? and invalid = 0", playerId);
				if(entitys != null) {
					for (PlayerRelationEntity entity : entitys) {
						if(entity.getType() == GsConst.RelationType.FRIEND) {
							friendNum.incrementAndGet();
						}else if (entity.getType() == GsConst.RelationType.BLACKLIST){
							blacklistNum.incrementAndGet();
						}
						
						map.put(entity.getTargetPlayerId(), entity);
					}
				}
			}
			
			//不管有多少个线程同时调用这个方法，保证最终返回去的map是同一个.
			Map<String, PlayerRelationEntity> tmp = relationMap.putIfAbsent(playerId, map);
			if (tmp != null) {
				map = tmp;
			}
			friendNumMap.putIfAbsent(playerId, friendNum);
			blacklistNumMap.putIfAbsent(playerId, blacklistNum);
		}
		
		return map;
	}
	
	/**
	 * 获取好友申请
	 * @param targetId 被申请的那个人
	 * @param reqId    申请的那个人
	 * @return
	 */
	public PlayerRelationApplyEntity getPlayerRelationApply(String targetId, String reqId) {
		return this.getPlayerRelationApplyMap(targetId).get(reqId);
	}
	
	
	public int getPlayerRelationApplySize(String playerId) {
		return this.getPlayerRelationApplyMap(playerId).size();
	}
	/**
	 * 获取好友申请的map
	 * @param id
	 * @return
	 */
	public Map<String, PlayerRelationApplyEntity> getPlayerRelationApplyMap(String id) {
		Map<String, PlayerRelationApplyEntity> map = relationApplyMap.get(id);
		if (map == null) {
			return loadPlayerRelationApplyData(id, false);
		} else {
			return map;
		}
	}

	/**
	 * 加载申请列表信息  注意并发问题.
	 * 
	 * @return
	 */
	public Map<String, PlayerRelationApplyEntity> loadPlayerRelationApplyData(String id, boolean isLogin) {
		Map<String, PlayerRelationApplyEntity> map = relationApplyMap.get(id);
		if (map == null) {
			map = new ConcurrentHashMap<>();
			//这里的加载不能保证都是在同一个线程，只能用这种方式来保证
			Map<String, PlayerRelationApplyEntity> redisMap = LocalRedis.getInstance().getAllRelationApplies(id);
			map.putAll(redisMap);
			Map<String, PlayerRelationApplyEntity> curValue = relationApplyMap.putIfAbsent(id, map);
			if (curValue != null) {
				map = curValue;
			}								
		}
		
		//检测过期
		int expireTime = GameConstCfg.getInstance().getRelaitonApplyExpireTime();
		int curTime = HawkTime.getSeconds();
		for (PlayerRelationApplyEntity prae : map.values()) {
			if (prae.getApplyTime() + expireTime < curTime) {
				if (isLogin) {
					this.clearRelationApply(id);
				} else {
					GsApp.getInstance().postMsg(ActivityService.getInstance().getXid(), new ClearRelationApplyMsg(id));
				}				
				break;
			}
		}
		
		return map;
	}
	
	public void clearRelationApply(String playerId) {
		Map<String, PlayerRelationApplyEntity> map = this.getPlayerRelationApplyMap(playerId);
		List<String> deleteList = new ArrayList<>();
		int curTime = HawkTime.getSeconds();
		int expireTime = GameConstCfg.getInstance().getRelaitonApplyExpireTime();
		for (PlayerRelationApplyEntity prae : map.values()) {
			if (prae.getApplyTime() + expireTime < curTime) {
				deleteList.add(prae.getPlayerId());
			}
		}
		
		if (deleteList.isEmpty()) {
			logger.error("relation apply nothing delete playerId:{}", playerId);
		} else {
			this.deleteApply(playerId, deleteList);
		}
	}
	
	/**
	 * 是否是好友 做了双向判定
	 * A是否是B的好友.
	 * B是否是A的好友.
	 * @param id
	 * @param targetId
	 * @return
	 */
	public boolean isFriend(String playerId, String targetId) {
		return isRelation(playerId, targetId, GsConst.RelationType.FRIEND) || isRelation(targetId, 
				playerId, GsConst.RelationType.FRIEND);		
	}
	
	/**
	 * 
	 * @param playerId 发起申请的人
	 * @param targetId 被申请的人
	 * @return true 已经申请, false 没有申请
	 */
	public boolean isAlreadyApply(String playerId, String targetId) {
		return this.getPlayerRelationApply(targetId, playerId) == null ? this.getPlayerRelationApply(playerId, targetId) != null : true;
	}
	
	private boolean isRelation(String playerId, String targetId, int type) {
		PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(playerId, targetId);
		return playerRelationEntity == null ? false : playerRelationEntity.getType() == type;
	}
	
	/**
	 * playerId 拉黑了targetId
	 * @param playerId
	 * @param targetId
	 * @return
	 */
	public boolean isBlacklist(String playerId, String targetId) {
		return isRelation(playerId, targetId, GsConst.RelationType.BLACKLIST);
	}
	
	public int getFriendNum(String playerId) {
		AtomicInteger ai = friendNumMap.get(playerId);
		if(ai == null) {
			 this.loadData(playerId);
			 ai = friendNumMap.get(playerId);
		}
		
		return ai.get();
	}
	
	public int getBlacklistNum(String playerId) {
		AtomicInteger ai = blacklistNumMap.get(playerId);
		if (ai != null) {
			return ai.get();
		} else {
			logger.warn("relation blacklistNumMapIsNull playerId:{}", playerId);
			return 0;
		}		
	}
	
	public int addFriendNum(String playerId, int num) {
		return friendNumMap.get(playerId).addAndGet(num);
	}
	
	public int addBlacklistNum(String playerId, int num) {
		return blacklistNumMap.get(playerId).addAndGet(num);
	} 
	
	/**
	 * 请求添加好友
	 * @param reqId
	 * @param targetId
	 * @param content
	 * @return
	 */
	public int friendAddReq(String reqId, List<String> targetIds, String content) {
		int friendNum = this.getFriendNum(reqId);
		
		if(friendNum + targetIds.size() > ConstProperty.getInstance().getFriendUpperLimit()){
			return Status.Error.RELATION_FRIEND_MAX_VALUE;
		}
		
		int maxApplySize = ConstProperty.getInstance().getFriendApplyLimit();
		
		//黑名单可以发起申请
		Optional<String> rlt = targetIds.stream().filter(id->this.isFriend(reqId, id)).findAny();
		if(rlt.isPresent()) {
			return Status.Error.RELATION_FRIEND_ALEARDY_EXIST_VALUE;
		}
		
		//申请
		PlayerRelationApplyEntity playerRelationApplyEntity = null; 
		int targetApplySize = 0;
		String clearId = null;
		List<PlayerRelationApplyEntity> createRelationApplies = new ArrayList<>();
		List<String> pushList = new ArrayList<String>();
		for(String targetId : targetIds){
			clearId = null;
		
			if(this.isAlreadyApply(reqId, targetId)) {
				this.synNotice(reqId, HP.code.FRIEND_ADD_REQ_VALUE, Status.Error.RELATION_ALEARDY_APPLY_VALUE);
				continue;
			}
			
			targetApplySize = this.getPlayerRelationApplySize(targetId);
			if(targetApplySize >= maxApplySize) {
				playerRelationApplyEntity = this.getOldAplly(targetId);
				this.deleteApply(targetId, Arrays.asList(playerRelationApplyEntity.getPlayerId()));
				clearId = playerRelationApplyEntity.getPlayerId();
			}
			
			playerRelationApplyEntity = this.createPlayerRelationApplyEntity(reqId, targetId, content);
			createRelationApplies.add(playerRelationApplyEntity);
			pushList.add(targetId);
			synPlayerRelationApplyInfo(targetId, playerRelationApplyEntity, clearId == null ? null : Arrays.asList(clearId));
			LocalRedis.getInstance().createRelationApply(playerRelationApplyEntity);
		}
		if(pushList.size() > 0){
			BackFlowService.getInstance().sendPushOnAddFriend(pushList);
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 *找到最早申请的请求
	 * @param playerId
	 * @return
	 */
	private PlayerRelationApplyEntity getOldAplly(String playerId) {
		PlayerRelationApplyEntity entity = null;
		Map<String, PlayerRelationApplyEntity> map = this.getPlayerRelationApplyMap(playerId);
		for (PlayerRelationApplyEntity tmp : map.values()) {
			if (entity == null) {
				entity = tmp;
			} else {
				if(entity.getApplyTime() > tmp.getApplyTime()) {
					entity = tmp;
				}
			}
		}
		
		return entity;
	}
	/**
	 * 删除好友
	 * @param reqId
	 * @param targetId
	 * @return
	 */
	public int friendDelete(String reqId, String targetId) {
		PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(reqId, targetId);
		if (playerRelationEntity == null) {
			return Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE;
		}
		PlayerRelationEntity targetRelationEntity = this.getPlayerRelationEntity(targetId, reqId);
		if (targetRelationEntity == null) {
			logger.error("targetId:{}, reqId:{} relation not found", targetId, reqId);
			return Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE;
		}
		
		if (playerRelationEntity.isGuard()) {
			return Status.Error.RELATION_CAN_NOT_DELTEE_ON_GUARD_VALUE;
		}
		
		//删除自己
		deleteFriend(reqId, targetId);
		//删除对方
		deleteFriend(targetId, reqId);
		
		this.synDeletePlayerFriend(targetId, reqId, OperationFrom.BE_HANDLER);
		this.synDeletePlayerFriend(reqId, targetId, OperationFrom.HANDLER);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	private void deleteFriend(String reqId, String targetId) {
		logger.info("delete friend reqId:{}, targetId:{}", reqId, targetId);
		PlayerRelationEntity pre = this.getPlayerRelationMap(reqId).remove(targetId);
		this.addFriendNum(reqId, -1);
		if (pre != null) {
			pre.delete();
		} else {
			logger.error("delete empty relation reqId:{}, targetId:{}", reqId, targetId);
		}
		
		PlayerGuardInviteEntity pgie = this.guardInviteMap.get(reqId);
		if (pgie != null && pgie.getTargetPlayerId().equals(targetId)) {
			this.removeGuardInvite(reqId);
			this.returnPlayerInviteCost(pgie);
			this.synGuardHud(targetId);
		}
	}
	
	private void friendChange2BlackList(String reqId, String targetId) {
		PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(reqId, targetId);
		playerRelationEntity.setType(GsConst.RelationType.BLACKLIST);
		playerRelationEntity.setLove(0);
		this.addFriendNum(reqId, -1);
		this.addBlacklistNum(reqId, 1);
		
		this.synDeletePlayerFriend(reqId, targetId, OperationFrom.FROMCOMMON);
		
		logger.info("friendChange2BlackList  reqId:{}, targetId:{}", reqId, targetId);
	}
	
	/**
	 * 添加黑名单
	 * @param reqId
	 * @param targetId
	 * @return
	 */
	public int blacklistAdd(String reqId, String targetId) {
		int blacklistNum = this.getBlacklistNum(reqId);
		if(blacklistNum >= ConstProperty.getInstance().getFriendUpperLimit()){
			return Status.Error.RELATION_BLACKLIST_MAX_VALUE;
		}
		PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(reqId, targetId);
		if(playerRelationEntity != null) {
			if(playerRelationEntity.getType() == GsConst.RelationType.BLACKLIST) {
				return Status.Error.RELATION_BLACKLIST_ALEARDY_EXIST_VALUE;
			}
			
			if (playerRelationEntity.isGuard()) {
				return Status.Error.RELATION_CAN_NOT_DELTEE_ON_GUARD_VALUE;
			}
			
			PlayerRelationEntity targetRelatioEntity = this.getPlayerRelationEntity(targetId, reqId);
			if (targetRelatioEntity == null) {
				logger.error("targetId:{}, reqId:{} relation not found", targetId, reqId);
				return Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE;
			}
			//自己的从好友变成黑名单
			friendChange2BlackList(reqId, targetId);
			
			//对方删除
			this.deleteFriend(targetId, reqId);
			this.synDeletePlayerFriend(targetId, reqId, OperationFrom.BE_HANDLER);
		} else {
			playerRelationEntity = this.createPlayerRelationEntity(reqId, targetId, GsConst.RelationType.BLACKLIST);
		}
		
		this.synBlackList(playerRelationEntity);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 删除黑名单
	 * @param reqId
	 * @param targetId
	 * @param type
	 * @return
	 */
	public int blacklistDelete(String reqId, String targetId) {
		if(!isBlacklist(reqId, targetId)) {
			return Status.Error.RELATION_BLACKLIST_NOT_EXIST_VALUE;
		}
		
		deleteBlacklist(reqId, targetId);		
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	private void deleteBlacklist(String reqId, String targetId) {
		logger.info("delete blackList reqId:{}, targetId:{}", reqId, targetId);
		PlayerRelationEntity pre = this.getPlayerRelationMap(reqId).remove(targetId);
		this.addBlacklistNum(reqId, -1);
		this.synDeleteBlackList(reqId, targetId);
		if (pre != null) {
			pre.delete();
		} else {
			logger.info("delete blackList error reqId:{}, targetId:{}", reqId, targetId);
		}		
		
	}
	
	/**
	 * 处理玩家请求
	 * @param reqId
	 * @param oper
	 * @param playerId
	 * @return
	 */
	public int handleApply(String reqId, OperationType oper, List<String> playerIds){
		GlobalData globalData = GlobalData.getInstance();
		if (playerIds.isEmpty()) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		int rlt = 0;
		if (oper == OperationType.AGREEE) {
			for (String id : playerIds) {
				if (!globalData.isExistPlayerId(id)) {
					rlt = Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
					break;
				}
				
				if (this.getPlayerRelationApply(reqId, id) == null) {
					rlt = Status.Error.RELATION_APPLY_NOT_EXIST_VALUE;
					break;
				}
			}
		}		
		
		if(rlt != 0) {
			return rlt;
		}
			
		List<String> handledIds = null;
		if (oper == OperationType.AGREEE) {
			handledIds = agreeApply(reqId, playerIds);
			//有可能出现为空的情况
			if (!handledIds.isEmpty()) {
				deleteApply(reqId, handledIds);
			}			
		} else {
			deleteApply(reqId, playerIds);
			handledIds = playerIds;
		}
		
		this.synPlayerRelationApplyInfo(reqId, null, handledIds);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	private void deleteApply(String reqId, List<String> playerIds) {
		logger.info("delete apply info reqId:{}, playerIds:{}", reqId, playerIds);
		playerIds.forEach(id->{
				PlayerRelationApplyEntity prae = this.getPlayerRelationApplyMap(reqId).remove(id);
				if (prae == null) {
					logger.error("delete empty apply info reqId:{}, playerId:{}", reqId, id);
				}			
			});
		
		LocalRedis.getInstance().deleteRelationApplies(reqId, playerIds);
	}

	private List<String> agreeApply(String reqId, List<String> playerIds) {
		PlayerRelationEntity playerRelationEntity = null;
		int maxFriendNum = ConstProperty.getInstance().getFriendUpperLimit();
		int friendNum = 0;
		int targetFriendNum = 0;
		List<String> handledIds = new ArrayList<String>();
		List<PlayerRelationEntity> friendList = new ArrayList<>();
		for (String playerId : playerIds) {
			friendNum = this.getFriendNum(reqId);
			if(friendNum >= maxFriendNum) {
				this.synNotice(reqId, HP.code.HANDLE_FRIEND_APPLY_REQ_VALUE, Status.Error.RELATION_FRIEND_MAX_VALUE);
				return handledIds;
			}
			
			//通过调用该方法触发对方的数据加载方法
			if(this.isFriend(playerId, reqId)){
				handledIds.add(playerId);
				logger.error("agree apply is aleardy friend playerId:{}, targetPlayerId:{}", reqId, playerId);
				continue;
			}
			targetFriendNum = this.getFriendNum(playerId);
			if(targetFriendNum >= maxFriendNum) {
				handledIds.add(playerId);
				if(playerIds.size() == 1) {
					this.synNotice(reqId, HP.code.HANDLE_FRIEND_APPLY_REQ_VALUE, Status.Error.RELATION_APPLYER_FIREND_MAX_VALUE);
				}
				continue;
			}
			
			playerRelationEntity = addFriend(reqId, playerId, false);
			if (playerRelationEntity != null) {
				friendList.add(playerRelationEntity);
			}			
			
			addFriend(playerId, reqId, true);
			handledIds.add(playerId);
		}
		
		if (!friendList.isEmpty()) {
			this.synAddFriendInfo(reqId, friendList, OperationFrom.HANDLER);
		}		
		
		return handledIds;
	}
	
	private PlayerRelationEntity addFriend(String reqId, String targetId, boolean isSyn){
		PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(reqId, targetId);
		if(playerRelationEntity != null) {
			if (playerRelationEntity.getType() == GsConst.RelationType.FRIEND) {
				playerRelationEntity = null;
			} else {
				this.synDeleteBlackList(reqId, targetId);
				playerRelationEntity.setType(GsConst.RelationType.FRIEND);
				this.addBlacklistNum(reqId, -1);
				this.addFriendNum(reqId, 1);
				
				logger.info("blacklist change 2 friend reqId:{}, targetId:{}, ", reqId, targetId);
			}			
		} else {
			playerRelationEntity = this.createPlayerRelationEntity(reqId, targetId, GsConst.RelationType.FRIEND);
		}
		
		if (isSyn && playerRelationEntity != null) {
			this.synAddFriendInfo(reqId, Arrays.asList(playerRelationEntity), OperationFrom.BE_HANDLER);
		}		
		
		return playerRelationEntity;
	}
	
	
	/**
	 * 同步删除好友
	 * @param playerId
	 * @param targetPlayer
	 */
	private void synDeletePlayerFriend(String playerId, String targetPlayerId, OperationFrom from){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("relation service synDeletePlayerFriend, makesure player null: {}, targetPlayerId: {}", playerId, targetPlayerId);
			return;
		}
		SynDeleteFriend.Builder builder = SynDeleteFriend.newBuilder();
		builder.setPlayerId(targetPlayerId);
		builder.setFrom(from);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SYN_DELETE_FRIEND_VALUE, builder);
		player.sendProtocol(protocol);
	}
	
	/**
	 * 同步好友申请给被申请的玩家
	 * @param playerId
	 * @param playerRelationApplyEntity
	 */
	private void synPlayerRelationApplyInfo(String playerId, PlayerRelationApplyEntity playerRelationApplyEntity, List<String> handledIds) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player != null) {
			SynFriendApplyUpdate.Builder sbuilder = SynFriendApplyUpdate.newBuilder(); 
			if(playerRelationApplyEntity != null) {
				FriendApplyMsg.Builder builder = buildFriendApplyMsg(playerRelationApplyEntity);
				sbuilder.addApply(builder);
			}
			
			if(handledIds != null && !handledIds.isEmpty()) {
				sbuilder.addAllPlayerIds(handledIds);
			}
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SYN_FRIEND_APPLY_UPDATE_VALUE, sbuilder);
			player.sendProtocol(protocol);
			if(playerRelationApplyEntity != null) {
				player.getPush().syncFriendBuildStatus();
			}
		}
	}
	
	/**
	 * 创建好友申请对象
	 * 申请的时候把请求放到被申请人列表里面
	 * @param reqId
	 * @param targetId
	 * @param content
	 * @return
	 */
	private PlayerRelationApplyEntity createPlayerRelationApplyEntity(String reqId, String targetId, String content){
		PlayerRelationApplyEntity prae = new PlayerRelationApplyEntity();
		prae.setApplyTime(HawkTime.getSeconds());
		prae.setPlayerId(reqId);
		prae.setTargetPlayerId(targetId);
		prae.setContent(content == null ? "" : content);		
		this.getPlayerRelationApplyMap(targetId).put(reqId, prae);
		
		return prae;
	}
	
	/**
	 * 创建对象
	 * @param reqId
	 * @param targetId
	 * @param type
	 * @return
	 */
	private PlayerRelationEntity createPlayerRelationEntity(String reqId, String targetId, int type){
		PlayerRelationEntity entity = new PlayerRelationEntity();
		entity.setPlayerId(reqId);
		entity.setTargetPlayerId(targetId);
		entity.setType(type);
		entity.setLove(0);
		entity.setRemark("");
		if(type == GsConst.RelationType.BLACKLIST) {
			this.addBlacklistNum(reqId, 1);
		} else {
			this.addFriendNum(reqId, 1);
		}
		
		this.getPlayerRelationMap(reqId).put(targetId, entity);
		
		logger.info("create friend relation reqId:{}, targetId:{}, type:{}", reqId, targetId, type);
		
		entity.setId(HawkUUIDGenerator.genUUID());
		entity.create(true);
				
		return entity;
	}
	
	private void synDeleteBlackList(String playerId, String targetId) {
		this.synDeleteBlackList(playerId, Arrays.asList(targetId));
	}
	
	private void synDeleteBlackList(String playerId, List<String> targetIds) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(player != null) {
			SynDeleteBlackList.Builder sbuilder = SynDeleteBlackList.newBuilder();
			sbuilder.addAllPlayerIds(targetIds);
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SYN_DELETE_BLACKLIST_VALUE, sbuilder);
			player.sendProtocol(protocol);
		}
	}
	
	private void synBlackList(PlayerRelationEntity playerRelationEntity) {
		SynBlackList.Builder builder = SynBlackList.newBuilder();
		builder.setBlackList(this.buildBlacklistMsg(playerRelationEntity));
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SYN_BLACKLIST_VALUE, builder.build().toByteArray());
		Player player = GlobalData.getInstance().makesurePlayer(playerRelationEntity.getPlayerId());
		if (player != null) {
			player.sendProtocol(protocol);
		}
	}
	
	
	/**
	 * 构建好友pb对象
	 * @param entity
	 * @return
	 */
	public FriendMsg.Builder buildFriendMsg(PlayerRelationEntity entity, List<String> gaveList) {
		FriendMsg.Builder builder = FriendMsg.newBuilder();
		builder.setPlayerId(entity.getTargetPlayerId());

		Player targetPlayer = GlobalData.getInstance().makesurePlayer(entity.getTargetPlayerId());
		if (targetPlayer == null) {
			return null;
		}
		builder.setBattleValue(targetPlayer.getPower());
		// 预留需要接口
		builder.setGave(gaveList.contains(entity.getTargetPlayerId()) ? 1 : 0);
		builder.setLove(entity.getLove());
		builder.setIcon(targetPlayer.getIcon());
		builder.setPfIcon(targetPlayer.getPfIcon() == null ? "" : targetPlayer.getPfIcon());
		if (GlobalData.getInstance().isOnline(entity.getTargetPlayerId())) {
			builder.setLively(targetPlayer.getData().isLively());
		} else {
			builder.setLively(GameUtil.isLiveLy(targetPlayer.getData()));
		}

		builder.setVipLevel(targetPlayer.getVipLevel());
		builder.setLevel(targetPlayer.getLevel());
		builder.setPlayerName(targetPlayer.getName());
		builder.setCityLevel(targetPlayer.getCityLevel());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(targetPlayer));
		builder.setRemark(entity.getRemark());
		String guildTag = getGuildTag(entity.getTargetPlayerId());
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildName(guildTag);
		}
		
		CollegeMemberEntity member = targetPlayer.getData().getCollegeMemberEntity();
		if (member != null) {
			builder.setCollegeAuth(CollegeAuth.valueOf(member.getAuth()));
		}else{
			builder.setCollegeAuth(CollegeAuth.NOJOIN);
		}

		return builder;
	}
	
	/**
	 * 构建好友申请PB对象
	 * @param entity
	 * @return
	 */
	public FriendApplyMsg.Builder buildFriendApplyMsg(PlayerRelationApplyEntity entity) {
		FriendApplyMsg.Builder builder = FriendApplyMsg.newBuilder();
		builder.setPlayerId(entity.getPlayerId());
		PlayerData playerData = GlobalData.getInstance().getPlayerData(entity.getPlayerId(), true);
		builder.setPlayerName(playerData.getPlayerEntity().getName());
		builder.setContent(entity.getContent());
		builder.setGuildName(getGuildTag(entity.getPlayerId()));
		builder.setIcon(playerData.getPlayerEntity().getIcon());
		builder.setPfIcon(playerData.getPfIcon() == null ? "" : playerData.getPfIcon());
		builder.setVipLevel(playerData.getPlayerEntity().getVipLevel());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(playerData));
		
		return builder;
	}
	
	private String getGuildTag(String playerId) {
		return Optional.ofNullable(GuildService.getInstance().getGuildTag(getGuildId(playerId))).orElse("");
	}
	
	private String getGuildId(String playerId) {
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		return guild == null ? null : guild.getId();
	}
	
	/**
	 * 构建黑名单pb对象
	 * @param entity
	 * @return
	 */
	public BlackListMsg.Builder buildBlacklistMsg(PlayerRelationEntity entity) {
		BlackListMsg.Builder builder = BlackListMsg.newBuilder();
		builder.setPlayerId(entity.getTargetPlayerId());
		Player player = GlobalData.getInstance().makesurePlayer(entity.getTargetPlayerId());
		if (player == null) {
			return null;
		} else {
			builder.setPlayerName(player.getName());
			builder.setIcon(player.getIcon());
			builder.setPfIcon(player.getPfIcon() == null ? "" : player.getPfIcon());
			builder.setGuildName(this.getGuildTag(entity.getTargetPlayerId()));
			builder.setVipLevel(player.getVipLevel());
			builder.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
			builder.setBattleValue(player.getPower());
		}
		return builder;
	}
	
	public StrangerMsg.Builder buildStrangerMsg(String reqId, String playerId) {
		StrangerMsg.Builder builder = StrangerMsg.newBuilder();
		PlayerData playerData = GlobalData.getInstance().getPlayerData(playerId, true);
		builder.setGuildName(this.getGuildTag(playerId));
		builder.setPlayerId(playerId);
		builder.setPlayerName(playerData.getPlayerEntity().getName());
		builder.setIcon(playerData.getPlayerEntity().getIcon());
		builder.setPfIcon(playerData.getPfIcon() == null ? "" : playerData.getPfIcon());
		builder.setBattleValue(playerData.getPlayerEntity().getBattlePoint());
		builder.setVipLevel(playerData.getPlayerEntity().getVipLevel());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(playerData));
		boolean isAleardyApply = this.isAlreadyApply(reqId, playerId);
		int state = 0;
		//1已经申请，2是好友，3是黑名单.
		if (isAleardyApply) {
			state = 1;
		} else {
			PlayerRelationEntity pre = this.getPlayerRelationEntity(reqId, playerId);
			if (pre != null) {
				if (pre.getType() == RelationType.BLACKLIST) {
					state = 3;
				} else {
					state = 2;
				}
			}			
		}		
		builder.setApplyState(state);
		return builder;
		
	}
	
	private void synNotice(String id, int hpCode, int errorCode) {
		Player player = GlobalData.getInstance().makesurePlayer(id);
		if (player != null) {
			player.sendError(hpCode, errorCode, 0);
		}		
	}
	
	/**
	 * 获取亲密度
	 * @param playerId
	 * @param friend
	 * @param love
	 */
	public int getLove(String playerId, String friendId) {
		int love = getPlayerRelationEntity(playerId, friendId).getLove();
		return love;
	}
	
	/**
	 * 添加亲密度
	 * @param playerId
	 * @param friend
	 * @param love
	 */
	public void addLove(String playerId, String friendId, int addLove) {
		Player friend = GlobalData.getInstance().makesurePlayer(friendId);
		if (friend == null || !isFriend(playerId, friendId) || !isFriend(friendId, playerId)) {
			return;
		}
		int beforeLove = getPlayerRelationEntity(friendId, playerId).getLove();
		
		addLove(getPlayerRelationEntity(playerId, friendId), addLove);
		addLove(getPlayerRelationEntity(friendId, playerId), addLove);
		
		int afterLove = getPlayerRelationEntity(friendId, playerId).getLove();;
		
		friend.getPush().loveAddPush(playerId, afterLove - beforeLove);
	}
	
	private void addLove(PlayerRelationEntity playerRelationEntity, int addLove) {
		int beforeLove = playerRelationEntity.getLove();
		int afterLove = beforeLove + addLove;
		int maxLove = ConstProperty.getInstance().getFriendIntimacyLimit();
		
		afterLove = afterLove > maxLove ? maxLove : afterLove;
		playerRelationEntity.setLove(afterLove);
	}
	
	private void synAddFriendInfo(String id, List<PlayerRelationEntity> entityList, OperationFrom from) {
		Player player = GlobalData.getInstance().makesurePlayer(id);
		if ( player != null) {
			List<String> gaveList = LocalRedis.getInstance().getFriendPresentGift(id);
			SynFriendInfo.Builder sbuilder = SynFriendInfo.newBuilder();
			for (PlayerRelationEntity entity : entityList) {
				sbuilder.addFriend(this.buildFriendMsg(entity, gaveList));
			}
			
			sbuilder.setFrom(from);
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SYN_FRIEND_INFO_VALUE, sbuilder);
			player.sendProtocol(protocol);
		}
	}
	
	@MessageHandler
	private void migratePlayer(MigrateOutPlayerMsg msg) {
		msg.setResult(Boolean.FALSE);
		logger.info("relation migratePlayer playerId:{}", msg.getPlayer().getId());		
		Map<String, PlayerRelationEntity> relationMap = this.getPlayerRelationMap(msg.getPlayer().getId());
		for (PlayerRelationEntity entity : relationMap.values()) {
			if (entity.getType() == RelationType.BLACKLIST) {
				this.deleteBlacklist(entity.getPlayerId(), entity.getTargetPlayerId());
			} else {
				this.deleteFriend(entity.getPlayerId(), entity.getTargetPlayerId());
				this.deleteFriend(entity.getTargetPlayerId(), entity.getPlayerId());
			}
			
		}
		
		msg.setResult(Boolean.TRUE);
	}
	
	/**
	 * 更新装扮信息， 有的
	 * @param msg
	 */
	@MessageHandler
	private void onUpdateGuardDess(UpdateGuardDressMsg msg) {
		logger.info("update guard dress playerId:{}, remainDressId:{}", msg.getPlayerId(), msg.getHasDressIdSet());
		PlayerRelationEntity relationEntity = this.getGuardRelationEntity(msg.getPlayerId());
		if (relationEntity == null) {
			return;
		}
		
		//这里都是第一个,
		int oldDressId = relationEntity.getDressId();
		if (!AssembleDataManager.getInstance().isGuardDressId(oldDressId)) {
			return;
		}
		boolean isSingle = AssembleDataManager.getInstance().isSingleGuardDressId(oldDressId);
		if(isSingle){
			return;
		}
		List<Integer> idList = AssembleDataManager.getInstance().getGuardDressList(oldDressId);
		if (idList.get(1) == oldDressId) {
			oldDressId = idList.get(0);
		}
		
		if (msg.getHasDressIdSet().contains(oldDressId)) {
			return;
		}
		
		relationEntity.setDressId(0);
		PlayerRelationEntity targetPlayerRelationEntity = this.getGuardRelationEntity(relationEntity.getTargetPlayerId());
		if (targetPlayerRelationEntity != null) {
			relationEntity.setDressId(0);
		}			
		
	}
	
	/**
	 * 是否有黑名单.
	 * 慎用该接口,
	 * 使用该接口的前提是玩家好友数据已经被加载了。
	 * 该接口不会去db加载玩家数据，只取内存值.
	 * @param playerId
	 * @return
	 */
	public boolean hasBlacklist(String playerId) {
		return this.getBlacklistNum(playerId) > 0;
	}
	
	/**
	 * 判断一个玩家是不是自己的平台好友
	 * 
	 * @param playerId
	 * @param friendPlayerId
	 * @return <是否是平台好友，好友所在服Id>
	 * 
	 */
	public HawkTuple2<Boolean, String> isPlatformFriend(Player player, String friendPlayerId) {
		if (player.isRobot()) {
			return new HawkTuple2<Boolean, String>(false, null);
		}
		
		List<PlatformFriendInfo.Builder> friendInfoList = platformFriendCache.getIfPresent(player.getId());
		if (friendInfoList == null) {
			getPlatformFriendList(player);
		}
		
		friendInfoList = platformFriendCache.getIfPresent(player.getId());
		if (friendInfoList == null || friendInfoList.isEmpty()) {
			return new HawkTuple2<Boolean, String>(false, null);
		}
		
		for (PlatformFriendInfo.Builder friendInfo : friendInfoList) {
			if (friendInfo.getPlayerId().equals(friendPlayerId)) {
				return new HawkTuple2<Boolean, String>(true, friendInfo.getServerId());
			}
		}
		
		return new HawkTuple2<Boolean, String>(false, null);
	}
	
	/**
	 * 判断是不是同服平台好友
	 * 
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public boolean isInGamePlatformFriend(Player player, String friendPlayerId) {
		if (player.isRobot()) {
			return true;
		}
		
		if (GameUtil.isWin32Platform(player)) {
			return true;
		}
		
		HawkTuple2<Boolean, String> tuple = isPlatformFriend(player, friendPlayerId);
		if (tuple.first) {
			String friendServerId = tuple.second;
			String serverId = CrossService.getInstance().getImmigrationPlayerServerId(player.getId());
			if (HawkOSOperator.isEmptyString(serverId)) {
				serverId = GsConfig.getInstance().getServerId();
			}
			
			GlobalData globalData = GlobalData.getInstance();
			String owerMainServerId = globalData.getMainServerId(serverId);
			String friendMainServerId = globalData.getMainServerId(friendServerId);
			if (!owerMainServerId.equals(friendMainServerId)) {
				return false;
			}
		} 
		
		return true;
	}
	
	/**
	 * 获取平台好友列表
	 * 
	 * @param params
	 * @return
	 */
	public List<PlatformFriendInfo.Builder> getPlatformFriendList(Player player) {
		return getPlatformFriendList(player, false);
	}

	/**
	 * 获取平台好友列表
	 * @param params doFlush 为true时表示强制刷新
	 * @return
	 */
	private List<PlatformFriendInfo.Builder> getPlatformFriendList(Player player, boolean doFlush) {
		// 先从本地缓存中获取，本地缓存没有获取到则通过sdk请求好友信息
		List<PlatformFriendInfo.Builder> friendInfoBuilders = platformFriendCache.getIfPresent(player.getId());
		if (!doFlush && friendInfoBuilders != null && !friendInfoBuilders.isEmpty()) {
			List<String> gaveList = LocalRedis.getInstance().getFriendPresentGift(player.getId());
			for (PlatformFriendInfo.Builder friendInfo : friendInfoBuilders) {
				 String accountOnlineInfo = RedisProxy.getInstance().getOnlineInfo(friendInfo.getOpenid());
				 // 不在线返回实际的退出时间，在线时则返回0
				 if (HawkOSOperator.isEmptyString(accountOnlineInfo)) {
					 friendInfo.setLogoutTime(friendInfo.getLogoutTime());
				 } else {
					 friendInfo.setLogoutTime(0);
				 }
				 
				 friendInfo.setGave(gaveList.contains(friendInfo.getPlayerId()) ? 1 : 0);
			}
			
			return friendInfoBuilders;
		}
		
		friendInfoBuilders = new CopyOnWriteArrayList<PlatformFriendInfo.Builder>();
		platformFriendCache.put(player.getId(), friendInfoBuilders);
		
		HawkLog.debugPrintln("fetch platform firend from remote, playerId: {}, playerChannel: {}", player.getId(), player.getChannel());
		
		try {
			HawkLog.logPrintln("RelationService fetch platform friend, step 1. playerId: {}", player.getId());
			JSONObject friendInfos = fetchPlatformFriendList(player);
			if (friendInfos == null || !friendInfos.containsKey("lists")) {
				HawkLog.logPrintln("fetch platform firend failed, openid: {}, playerId:{}, result: {}", player.getOpenId(), player.getId(), friendInfos);
				 return friendInfoBuilders;
			}
			
			HawkLog.logPrintln("RelationService fetch platform friend, step 2. playerId: {}", player.getId());
			fetchFriendRoleInfo(player, friendInfos, friendInfoBuilders);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return friendInfoBuilders;
	}
	
	/**
	 * 拉取平台好友游戏内信息
	 * 
	 * @param player
	 * @param friendInfos
	 * @param friendInfoList
	 */
	private void fetchFriendRoleInfo(Player player, JSONObject friendInfos, List<PlatformFriendInfo.Builder> friendInfoList) {
		JSONArray friendList = friendInfos.getJSONArray("lists");
		Map<String, JSONObject> friendInfoMap = new HashMap<String, JSONObject>();
		
		String openid = player.getOpenId();
		
		for (int i = 0; i < friendList.size(); i++) {
			 JSONObject friendInfo = friendList.getJSONObject(i);
			 String fopenid = friendInfo.getString("openid");
			 if (openid.indexOf(fopenid) >= 0) {
				 continue;
			 }
			 
			 friendInfoMap.put(fopenid, friendInfo);
		}
		
		//这里考虑到friend太多，一次性批量请求redis会给redis服务器造成压力，所以当friendInfoMap的size大于100的时候，要分段获取
		List<AccountRoleInfo> accountRoleList = new ArrayList<>();
		if (friendInfoMap.size() <= 100) {
			List<AccountRoleInfo> tmpList = RedisProxy.getInstance().batchGetAccountRole(friendInfoMap.keySet());
			accountRoleList.addAll(tmpList);
		} else {
			long startTime = HawkTime.getMillisecond();
			int count = 0;
			Set<String> openids = new HashSet<>();
			for(Entry<String, JSONObject> entry : friendInfoMap.entrySet()) {
				openids.add(entry.getKey());
				count++;
				if (count >= 100) {
					count = 0;
					List<AccountRoleInfo> tmpList = RedisProxy.getInstance().batchGetAccountRole(openids);
					accountRoleList.addAll(tmpList);
					openids.clear();
				}
			}
			
			if (count > 0) {
				List<AccountRoleInfo> tmpList = RedisProxy.getInstance().batchGetAccountRole(openids);
				accountRoleList.addAll(tmpList);
			}
			long costtime = HawkTime.getMillisecond() - startTime;
			if (costtime > 60) {
				HawkLog.logPrintln("RelationService fetch platform friend from redis costtime: {}, playerId: {}, fcount: {}", costtime, player.getId(), friendInfoMap.size());
			}
		}
		
		HawkLog.logPrintln("RelationService fetch platform friend, step 3. playerId: {}", player.getId());
		if (accountRoleList.isEmpty()) {
			return;
		}
		
		List<String> gaveList = LocalRedis.getInstance().getFriendPresentGift(player.getId());
		for (AccountRoleInfo accountRole : accountRoleList) {
			JSONObject friendInfo = friendInfoMap.get(accountRole.getOpenId());
			PlatformFriendInfo.Builder roleInfoBuilder = buildFriendRoleInfo(player.getId(), accountRole, friendInfo, gaveList);
			if (roleInfoBuilder != null) {
				friendInfoList.add(roleInfoBuilder);
			}
		}
		
		HawkLog.logPrintln("RelationService fetch platform friend, step 4. playerId: {}", player.getId());
	}
	
	/**
	 * 构建好友角色信息数据
	 * @param playerId
	 * @param accountRole
	 * @param friendInfo
	 * @return
	 */
	private PlatformFriendInfo.Builder buildFriendRoleInfo(String playerId, AccountRoleInfo accountRole, JSONObject friendInfo, List<String> gaveList) {
		// 玩家战力、指挥官等级、基地等级，头像，游戏内名称，昵称，在线情况
		 try {
			 PlatformFriendInfo.Builder roleInfoBuilder = PlatformFriendInfo.newBuilder();
			 roleInfoBuilder.setPlayerId(accountRole.getPlayerId());
			 roleInfoBuilder.setOpenid(accountRole.getOpenId());
			 if (HawkOSOperator.isEmptyString(accountRole.getPlayerName())) {
				 HawkLog.logPrintln("fetch platform firend, friend name empty, friendInfo: {}", accountRole);
				 roleInfoBuilder.setPlayerName(friendInfo.getString("nickName"));
			 } else {
				 roleInfoBuilder.setPlayerName(accountRole.getPlayerName());
			 }
			 
			 roleInfoBuilder.setNickName(friendInfo.getString("nickName"));
			 roleInfoBuilder.setBattleValue(accountRole.getBattlePoint());
			 roleInfoBuilder.setGave(gaveList.contains(accountRole.getPlayerId()) ? 1 : 0);
			 roleInfoBuilder.setIcon(accountRole.getIcon());
			 //如果是本服走三段式,否则走redis
			 String[] icons = accountRole.getPfIcon().split("_");
			 if (icons.length >= 3) {
				 if (icons[0].equals(ImageSource.FROMIM_VALUE+"")) {
					 roleInfoBuilder.setPfIcon(icons[0] + "_" + 
							 RedisProxy.getInstance().getPfIcon(GameUtil.getPuidByPlatform(accountRole.getOpenId(), 
									 accountRole.getPlatform())) + "_" + icons[2]); 
				 } else {
					 roleInfoBuilder.setPfIcon(accountRole.getPfIcon());
				 }				 
			 } else {
				 roleInfoBuilder.setPfIcon(accountRole.getPfIcon());
			 }						
			 roleInfoBuilder.setLevel(accountRole.getPlayerLevel());
			 roleInfoBuilder.setCityLevel(accountRole.getCityLevel());
			 roleInfoBuilder.setVipLevel(accountRole.getVipLevel());
			 roleInfoBuilder.setServerId(accountRole.getServerId());
			 
			 PlayerCommon.Builder playerCommon = PlayerCommon.newBuilder();
			 playerCommon.setLoginWay(LoginWay.valueOf(accountRole.getLoginWay()));
			 int qqVipLevel = accountRole.getQqSVIPLevel(); 
			 if (qqVipLevel > 0) {
				 playerCommon.setSvipLevel(qqVipLevel);    // QQ超级会员等级
			 } else if (qqVipLevel < 0) {
				 roleInfoBuilder.setQQVIPLevel(0 - qqVipLevel);  // QQ普通会员等级
			 }
			 playerCommon.setServerId(accountRole.getServerId());
			 
			 roleInfoBuilder.setCommon(playerCommon);
			 
			 String accountOnlineInfo = RedisProxy.getInstance().getOnlineInfo(accountRole.getOpenId());
			 // 不在线返回实际的退出时间，在线时则返回0
			 if (HawkOSOperator.isEmptyString(accountOnlineInfo)) {
				 roleInfoBuilder.setLogoutTime(accountRole.getLogoutTime());
			 } else {
				 roleInfoBuilder.setLogoutTime(0);
			 }
			 
			 return roleInfoBuilder;
		 } catch (Exception e) {
			 HawkException.catchException(e);
		 }
		 
		 return null;
	} 
	
	/**
	 * 获取好友列表
	 * 
	 * @param params
	 * @return
	 */
	public JSONObject fetchPlatformFriendList(Player player) {
		// 平台好友关系链授权已解除
		if (GlobalData.getInstance().isPfRelationCancel(player.getOpenId())) {
			return null;
		}
		
		if (player.getPfTokenJson() != null) {
			try {
				JSONObject friendProfile = SDKManager.getInstance().getFriendsProfile(player.getChannel(), player.getPfTokenJson(), 2);
				return friendProfile;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return null;
	}
	
	/**
	 * 移除平台好友信息
	 * 
	 * @param playerId
	 */
	public void removeCachePlatfromFriend(String playerId) {
		platformFriendCache.invalidate(playerId);
	}
	
	/**
	 * 加载密友（微信或QQ）邀请信息
	 * 
	 * @param playerId
	 */
	public void loadFriendInviteInfo(String playerId) {
		Map<String, String> inviteSuccFriends = RedisProxy.getInstance().getAllInviteSuccFriends(playerId);
		// 加载邀请未成功的好友
		Map<String, String> notSuccInviteTimes = RedisProxy.getInstance().getAllInviteTime(playerId);
		if (inviteSuccFriends.isEmpty() && notSuccInviteTimes.isEmpty()) {
			return;
		}
		
		FriendInviteInfo friendInviteInfo = new FriendInviteInfo();
		friendInviteInfoMap.put(playerId, friendInviteInfo);
		
		int now = (int) (HawkApp.getInstance().getCurrentTime() / 1000);
		Map<String, Integer> inviteNotSuccFriends = friendInviteInfo.getInviteNotSuccFriends();
		for (Entry<String, String> entry : notSuccInviteTimes.entrySet()) {
			int inviteTime = Integer.parseInt(entry.getValue());
			if (now - inviteTime >= ConstProperty.getInstance().getFriendInviteExpireTime()) {
				continue;
			}
			
			String friendOpenid = entry.getKey();
			inviteNotSuccFriends.put(friendOpenid, inviteTime);
		}
		
		if (inviteSuccFriends.isEmpty()) {
			return;
		}
		
		try {
			Map<String, TaskAttrInfo> inviteSuccFriendMap = friendInviteInfo.getInviteSuccFriends();
			for (Entry<String, String> entry : inviteSuccFriends.entrySet()) {
				String friendOpenid = entry.getKey();
				String inviteTaskAttrInfo = entry.getValue();
				TaskAttrInfo obj = JSONObject.parseObject(inviteTaskAttrInfo, TaskAttrInfo.class);
				inviteSuccFriendMap.put(friendOpenid, obj);
			}
			
			Set<Integer> rewardTaskIdSet = friendInviteInfo.getHasRewardTaskIds();
			Set<String> rewardTaskIds = RedisProxy.getInstance().getFriendInviteRewardTask(playerId);
			for (String taskId : rewardTaskIds) {
				rewardTaskIdSet.add(Integer.valueOf(taskId));
			}
			
			Set<Integer> finishedTaskIdSet = friendInviteInfo.getFinishedTaskIds();
			Set<String> finishedTaskIds = RedisProxy.getInstance().getFinishedInviteTask(playerId);
			for (String taskId : finishedTaskIds) {
				finishedTaskIdSet.add(Integer.valueOf(taskId));
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取密友（微信或QQ）邀请信息
	 * 
	 * @param playerId
	 * @return
	 */
	public FriendInviteInfo getFriendInviteInfo(String playerId) {
		FriendInviteInfo friendInviteInfo = friendInviteInfoMap.get(playerId);
		if (friendInviteInfo == null) {
			friendInviteInfo = new FriendInviteInfo();
			friendInviteInfoMap.put(playerId, friendInviteInfo);
		}
		
		return friendInviteInfo;
	}
	
	/**
	 * 判断此密友（微信或QQ）是否已成功邀请
	 * 
	 * @param playerId
	 * @param friendOpenid
	 * @return
	 */
	public boolean isFriendsInvited(String playerId, String friendOpenid) {
		FriendInviteInfo friendInviteInfo = friendInviteInfoMap.get(playerId);
		if (friendInviteInfo == null) {
			return false;
		}
		
		Map<String, TaskAttrInfo> inviteInfoMap  = friendInviteInfo.getInviteSuccFriends();
		if (inviteInfoMap.containsKey(friendOpenid)) {
			return true;
		}
		
		return false;
	}
	

	/**
	 * 构建未注册好友信息builder
	 * 
	 * @param player
	 * @param friendInfos
	 * @param count 未注册好友展示数量
	 * @return
	 */
	public UnRegFriendsPB.Builder buildUnregFriendInfo(Player player, JSONObject[] friendInfos, int count, String openidKey) {
		FriendInviteInfo friendInviteInfo = getFriendInviteInfo(player.getId());
		// 邀请成功的好友
		Set<String> invitedFriendsSOpenid = friendInviteInfo.getInvitedFriendsSOpenid();
		
		// 邀请过但未邀请成功的好友
		Map<String, Integer> inviteTimeMap = friendInviteInfo.getInviteNotSuccFriends();
		
		int nowTime = (int) (HawkApp.getInstance().getCurrentTime() / 1000);
		int expireTime = ConstProperty.getInstance().getFriendInviteExpireTime();
		
		// 未邀请过的好友
		UnregFriendTaskInfo info = new UnregFriendTaskInfo();
		List<UnRegFriendInfoPB> unregFriendList = info.getUnregFriendList();
		
		int userType = UserType.getByChannel(player.getChannel());
		boolean isWxTaskId = openidKey.equals("sopenid");
		
		for (JSONObject json : friendInfos) {
			String sopenid = json.getString(openidKey);
			// 已邀请成功的好友
			if (invitedFriendsSOpenid.contains(sopenid)) {
				continue;
			}
			
			UnRegFriendInfoPB.Builder friendBuilder = UnRegFriendInfoPB.newBuilder();
			friendBuilder.setOpenid(sopenid);
			// 手Q密友接口，或微信task模型
			if (userType == UserType.QQ || isWxTaskId) {
				friendBuilder.setNickName(json.getString("nick_name"));
				friendBuilder.setHeadImgUrl(json.containsKey("head_img_url") ? json.getString("head_img_url") : "");
			} else {
				// 微信拉取全量
				friendBuilder.setNickName(json.getString("nickName"));
				friendBuilder.setHeadImgUrl(json.getString("picture") + "/96");
			}
			
			Integer inviteTime = inviteTimeMap.get(sopenid);
			if (inviteTime != null && nowTime - inviteTime < expireTime) {
				friendBuilder.setInviteTime(inviteTime);
			} else {
				friendBuilder.setInviteTime(0);
			}
			
			unregFriendList.add(friendBuilder.build());
		}
		
		// 分页
		int index = Math.min(count, unregFriendList.size());
		info.setIndex(index);
		unregFriendInfoCache.put(player.getId(), info);
		
		UnRegFriendsPB.Builder friendsData = UnRegFriendsPB.newBuilder();
		friendsData.addAllUnRegFriend(unregFriendList.subList(0, index));
		
		return friendsData;
	}
	
	/**
	 * 从缓存中获取未注册好友
	 * @param player
	 * @param count
	 * @return
	 */
	public UnRegFriendsPB.Builder getUnregFriendList(String playerId, int count) {
		UnregFriendTaskInfo info = unregFriendInfoCache.get(playerId);
		if (info == null || info.getIndex() == 0) {
			return null;
		}
		
		List<UnRegFriendInfoPB> unregFriendList = info.getUnregFriendList();
		if (unregFriendList.isEmpty()) {
			return null;
		}
		
		int start = info.getIndex();
		int total = unregFriendList.size(), end = start + count;  // end 小于等于 total
		
		// 好友总数小于等于count
		if (start >= total) {
			start = 0;
			end = Math.min(count, total);
		} else if (end > total) {  // end 大于 total
			end = total;
		} 
		
		info.setIndex(end);
		UnRegFriendsPB.Builder friendsData = UnRegFriendsPB.newBuilder();
		friendsData.addAllUnRegFriend(unregFriendList.subList(start, end));
		return friendsData;
	}
	
	/**
	 * 从缓存中获取密友数据
	 * 
	 * @param playerId
	 * @return
	 */
	public UnregFriendTaskInfo getUnregFriendInfo(String playerId) {
		return unregFriendInfoCache.get(playerId);
	}
	
	/**
	 * 添加邀请成功好友信息
	 * 
	 * @param playerId
	 * @param friendOpenid
	 * @param taskAttr
	 */
	public void addInviteSuccFriend(Player player, String friendOpenid, String sopenid, String serverId, String platform) {
		TaskAttrInfo taskAttr = new TaskAttrInfo(1, serverId, friendOpenid, sopenid);
		taskAttr.setPlatform(platform);
		taskAttr.setInviteTime(HawkTime.getMillisecond());
		// 删除邀请关系，添加邀请成功好友信息
		RedisProxy.getInstance().removeInviteInfo(player.getId(), sopenid, taskAttr);
		UnregFriendTaskInfo unregFriendInfo = unregFriendInfoCache.get(player.getId());
		if (unregFriendInfo != null) {
			Iterator<UnRegFriendInfoPB> iter = unregFriendInfo.getUnregFriendList().iterator();
			while(iter.hasNext()) {
				UnRegFriendInfoPB friendInfo = iter.next();
				if (friendInfo.getOpenid().equals(sopenid)) {
					iter.remove();
					break;
				}
 			}
		}
		
		FriendInviteInfo friendInviteInfo = getFriendInviteInfo(player.getId());
		// 更新邀请未成功的好友信息
		Map<String, Integer> inviteNotSuccFriends = friendInviteInfo.getInviteNotSuccFriends();
		inviteNotSuccFriends.remove(sopenid);
		
		Map<String, TaskAttrInfo> inviteInfoMap  = friendInviteInfo.getInviteSuccFriends();
		inviteInfoMap.put(friendOpenid, taskAttr);
		
		HawkLog.logPrintln("addInviteSuccFriend, playerId: {}, friendOpenid: {}", player.getId(), friendOpenid);
		
		AccountRoleInfo friendRoleInfo = RedisProxy.getInstance().getAccountRole(serverId, platform, friendOpenid);
		if (friendRoleInfo == null) {
			HawkLog.errPrintln("addInviteSuccFriend, fetch friend accountRoleInfo failed, playerId: {}, friendOpenid: {}", player.getId(), friendOpenid);
			return;
		}
		
		// 推送新邀请成功的好友信息
		List<PlatformFriendInfo.Builder> platFriendInfoList = getPlatformFriendList(player, true);
		PlatformFriendInfo.Builder platFriendInfoBuilder = null;
		for (PlatformFriendInfo.Builder builder : platFriendInfoList) {
			if (builder.getOpenid().equals(friendOpenid)) {
				platFriendInfoBuilder = builder;
				break;
			}
		}
		
		InvitedFriendInfoPB.Builder friendInfoBuilder = InvitedFriendInfoPB.newBuilder();
		friendInfoBuilder.setOpenid(friendOpenid);
		friendInfoBuilder.setPlayerId(friendRoleInfo.getPlayerId());
		friendInfoBuilder.setPlayerName(friendRoleInfo.getPlayerName());
		friendInfoBuilder.setCityLevel(friendRoleInfo.getCityLevel());
		friendInfoBuilder.setServerId(serverId);
		friendInfoBuilder.setInviteTime(taskAttr.getInviteTime());
		if (platFriendInfoBuilder != null) {
			friendInfoBuilder.setNickName(platFriendInfoBuilder.getNickName());
			friendInfoBuilder.setIcon(platFriendInfoBuilder.getIcon());
			friendInfoBuilder.setPfIcon(platFriendInfoBuilder.getPfIcon());
		} else {
			friendInfoBuilder.setNickName(friendRoleInfo.getPlayerName());
			friendInfoBuilder.setIcon(friendRoleInfo.getIcon());
			friendInfoBuilder.setPfIcon(String.valueOf(friendRoleInfo.getIcon()));
			HawkLog.errPrintln("addInviteSuccFriend, fetch platform friend failed, playerId: {}, friendOpenid: {}", player.getId(), friendOpenid);
		}
		
		InvitedFriendData.Builder friendData = InvitedFriendData.newBuilder();
		friendData.addInvitedFriend(friendInfoBuilder);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.INVITED_FRIEND_PUSH, friendData));		

		logFirstFinishedTask(player, FriendInviteTaskCfg.getPlayerCountTypeTasks(), friendInviteInfo.getFinishedTaskIds());
	}
	
	/**
	 * 记录首次完成的任务
	 * 
	 * @param player
	 * @param allTaskIdSet
	 * @param finishedTaskIds
	 */
	private void logFirstFinishedTask(Player player, Set<Integer> allTaskIdSet, Set<Integer> finishedTaskIds) {
		try {
			for (Integer taskId : allTaskIdSet) {
				if (finishedTaskIds.contains(taskId) || !isTaskFinished(player, taskId)) {
					continue;
				}
				
				RedisProxy.getInstance().addFinishedInviteTask(player.getId(), taskId);
				finishedTaskIds.add(taskId);
				LogUtil.logFinishedInviteTask(player, taskId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 被邀请人通知邀请人，城堡升级了
	 * 
	 * @param player
	 * @param friendOpenId
	 * @param cityLevel
	 * @param serverId
	 */
	public void refreshInviteFriendInfo(Player player, String friendOpenId) {
		FriendInviteInfo friendInviteInfo = getFriendInviteInfo(player.getId());
		if (friendInviteInfo.getInviteSuccFriends().isEmpty()) {
			HawkLog.errPrintln("refreshInviteFriendCityLevel, player has no invited friends, playerId: {}, friendOpenid: {}", player.getId(), friendOpenId);
			return;
		}
		
		Map<String, TaskAttrInfo> inviteInfoMap  = friendInviteInfo.getInviteSuccFriends();
		if (inviteInfoMap.isEmpty() || !inviteInfoMap.containsKey(friendOpenId)) {
			HawkLog.errPrintln("refreshInviteFriendCityLevel, player not invite this friend, playerId: {}, friendOpenid: {}", player.getId(), friendOpenId);
			return;
		}
		
		TaskAttrInfo taskAttr = inviteInfoMap.get(friendOpenId);
		// 推送更新后的好友数据
		AccountRoleInfo friendRoleInfo = RedisProxy.getInstance().getAccountRole(taskAttr.getServerId(), taskAttr.getPlatform(), friendOpenId);
		if (friendRoleInfo == null) {
			HawkLog.errPrintln("refreshInviteFriendCityLevel, fetch friend accountRoleInfo failed, playerId: {}, friendOpenid: {}", player.getId(), friendOpenId);
			return;
		}
		
		HawkLog.logPrintln("refreshInviteFriendCityLevel, playerId: {}, friendOpenid: {}", player.getId(), friendOpenId);
		
		taskAttr.setCityLevel(friendRoleInfo.getCityLevel());
		RedisProxy.getInstance().updateInviteSuccFriendInfo(player.getId(), friendOpenId, taskAttr);
		
		// 推送新邀请成功的好友信息
		List<PlatformFriendInfo.Builder> platFriendInfoList = getPlatformFriendList(player);
		PlatformFriendInfo.Builder platFriendInfoBuilder = null;
		for (PlatformFriendInfo.Builder builder : platFriendInfoList) {
			if (builder.getOpenid().equals(friendOpenId)) {
				platFriendInfoBuilder = builder;
				break;
			}
		}
		
		InvitedFriendInfoPB.Builder friendInfoBuilder = InvitedFriendInfoPB.newBuilder();
		friendInfoBuilder.setOpenid(friendOpenId);
		friendInfoBuilder.setPlayerId(friendRoleInfo.getPlayerId());
		friendInfoBuilder.setPlayerName(friendRoleInfo.getPlayerName());
		friendInfoBuilder.setCityLevel(friendRoleInfo.getCityLevel());
		friendInfoBuilder.setServerId(taskAttr.getServerId());
		if (taskAttr.getInviteTime() > 0) {
			friendInfoBuilder.setInviteTime(taskAttr.getInviteTime());
		}
		
		if (platFriendInfoBuilder != null) {
			friendInfoBuilder.setNickName(platFriendInfoBuilder.getNickName());
			friendInfoBuilder.setIcon(platFriendInfoBuilder.getIcon());
			friendInfoBuilder.setPfIcon(platFriendInfoBuilder.getPfIcon());
		} else {
			friendInfoBuilder.setNickName(friendRoleInfo.getPlayerName());
			friendInfoBuilder.setIcon(friendRoleInfo.getIcon());
			friendInfoBuilder.setPfIcon(String.valueOf(friendRoleInfo.getIcon()));
			HawkLog.errPrintln("refreshInviteFriendCityLevel, fetch platform friend failed, playerId: {}, friendOpenid: {}", player.getId(), friendOpenId);
		}
		
		InvitedFriendData.Builder friendData = InvitedFriendData.newBuilder();
		friendData.addInvitedFriend(friendInfoBuilder);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.INVITED_FRIEND_PUSH, friendData));	
		
		logFirstFinishedTask(player, FriendInviteTaskCfg.getCityLevelTypeTasks(), friendInviteInfo.getFinishedTaskIds());
	}
	
	/**
	 * 被邀请玩家首次进入游戏时，通知邀请人
	 * 
	 * @param playerEntity
	 */
	public void beInvitedAccountIntoGame(PlayerEntity playerEntity, String pfToken) {
		if (!GameUtil.isFriendInviteEnable(playerEntity.getChannel())) {
			return;
		}
		
		if (UserType.getByChannel(playerEntity.getChannel()) == UserType.QQ) {
			beInvitedAccountIntoGame(playerEntity.getOpenid(), playerEntity);
		} else {
			String key = "wx_friends_sopenid:" + playerEntity.getOpenid();
			String sopenid = RedisProxy.getInstance().getRedisSession().getString(key);
			if (HawkOSOperator.isEmptyString(sopenid)) {
				HawkLog.logPrintln("login fetch wx_friends_sopenid failed, playerId: {}, openid: {}", playerEntity.getId(), playerEntity.getOpenid());
				return;
			}
			
			HawkLog.logPrintln("login fetch wx_friends_sopenid success, playerId: {}, openid: {}", playerEntity.getId(), playerEntity.getOpenid());
			
			beInvitedAccountIntoGame(sopenid, playerEntity);
		}
	}
	
	/**
	 * 被邀请人进入游戏
	 * @param sopenid  wx是sopenid，qq是openid
	 * @param playerEntity
	 */
	private void beInvitedAccountIntoGame(String sopenid, PlayerEntity playerEntity) {
		Map<String, String> inviteMeFriends = RedisProxy.getInstance().getBeInviteFriend(sopenid);
		if (inviteMeFriends.isEmpty()) {
			HawkLog.debugPrintln("beInvitedAccountIntoGame, invite me friends empty, playerId: {}, openid: {}, sopenid: {}, channel: {}", 
					playerEntity.getId(), playerEntity.getOpenid(), sopenid, playerEntity.getChannel());
			return;
		}
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				RedisProxy.getInstance().removeBeInviteFriend(sopenid);
				if (inviteMeFriends.size() > ConstProperty.getInstance().getCloseFriendNumLimit()) {
					nofityInvitor(sopenid, playerEntity, inviteMeFriends);
				} else {
					// 大本升级时还要通知邀请人更新任务数据
					Map<String, String> validInviteMeFriends = new HashMap<>();
					int now = (int) (HawkApp.getInstance().getCurrentTime() / 1000);
					int expireTime = ConstProperty.getInstance().getFriendInviteExpireTime();
					for (Entry<String, String> entry : inviteMeFriends.entrySet()) {
						String playerId = entry.getKey();           // 谁邀请的
						String[] arr = entry.getValue().split(":");
						int inviteTime = Integer.parseInt(arr[0]);   // 什么时候邀请的
						if (now - inviteTime > expireTime) {
							HawkLog.errPrintln("beInvitedAccountIntoGame, invite time expire, openid: {}, playerId: {}, invitor playerId: {}, inviteTime: {}", 
									playerEntity.getOpenid(), playerEntity.getId(), playerId, inviteTime);
							continue;
						}
						
						playerEntity.setBeInvited(true);
						String serverId = arr[1];  // 哪个服的玩家邀请的
						validInviteMeFriends.put(playerId, serverId);
						
						String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
						GmProxyHelper.proxyCall(mainServerId, "inviteSuccNotify", 
								"playerId=" + playerId + "&friendOpenid=" + playerEntity.getOpenid() + "&sopenid=" + sopenid 
								+ "&serverId=" + playerEntity.getServerId() + "&platform=" + playerEntity.getPlatform(), 2000);
					}
					
					RedisProxy.getInstance().refreshBeInviteFriends(playerEntity.getId(), validInviteMeFriends);
				}
				
				return null;
			}
		});
	}
	
	/**
	 * 被邀请人进入游戏时通知一定数量的邀请人
	 * 
	 * @param sopenid
	 * @param playerEntity
	 * @param inviteMeFriends
	 */
	private void nofityInvitor(String sopenid, PlayerEntity playerEntity, Map<String, String> inviteMeFriends) {
		int now = (int) (HawkApp.getInstance().getCurrentTime() / 1000);
		int expireTime = ConstProperty.getInstance().getFriendInviteExpireTime();
		List<JSONObject> invitorList = new ArrayList<JSONObject>();
		for (Entry<String, String> entry : inviteMeFriends.entrySet()) {
			String playerId = entry.getKey();           // 谁邀请的
			String[] arr = entry.getValue().split(":");
			int inviteTime = Integer.parseInt(arr[0]);   // 什么时候邀请的
			if (now - inviteTime > expireTime) {
				HawkLog.errPrintln("beInvitedAccountIntoGame, invite time expire, openid: {}, playerId: {}, invitor playerId: {}, inviteTime: {}", 
						playerEntity.getOpenid(), playerEntity.getId(), playerId, inviteTime);
				continue;
			}
			
			String serverId = arr[1];  // 哪个服的玩家邀请的
			JSONObject json = new JSONObject();
			json.put("playerId", playerId);
			json.put("inviteTime", inviteTime);
			json.put("serverId", serverId);
			invitorList.add(json);
		}
		
		if (invitorList.size() > ConstProperty.getInstance().getCloseFriendNumLimit()) {
			Collections.sort(invitorList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					return o1.getIntValue("inviteTime") - o2.getIntValue("inviteTime");
				}
			});
			
			invitorList = invitorList.subList(0, ConstProperty.getInstance().getCloseFriendNumLimit());
		}
		
		// 大本升级时还要通知邀请人更新任务数据
		Map<String, String> validInviteMeFriends = new HashMap<>();
		for (JSONObject json : invitorList) {
			String playerId = json.getString("playerId");
			String serverId = json.getString("serverId");
			validInviteMeFriends.put(playerId, serverId);
			playerEntity.setBeInvited(true);
			
			String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
			GmProxyHelper.proxyCall(mainServerId, "inviteSuccNotify", 
					"playerId=" + playerId + "&friendOpenid=" + playerEntity.getOpenid() + "&sopenid=" + sopenid 
					+ "&serverId=" + playerEntity.getServerId() + "&platform=" + playerEntity.getPlatform(), 2000);
		}
		
		RedisProxy.getInstance().refreshBeInviteFriends(playerEntity.getId(), validInviteMeFriends);
	}
	
	/**
	 * 通知邀请人城堡升级了
	 * 
	 * @param player
	 * @param cityLevel
	 */
	public void notifyInvitorCityLevelUp(Player player, int cityLevel) {
		if (!GameUtil.isFriendInviteEnable(player.getChannel())) {
			return;
		}
		
		Map<String, String> invitorInfo = RedisProxy.getInstance().getBeInviteFriend(player.getId());
		for (Entry<String, String> entry : invitorInfo.entrySet()) {
			String playerId = entry.getKey();
			String serverId = entry.getValue();
			String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
			GmProxyHelper.proxyCall(mainServerId, "notifyInvitor", "playerId=" + playerId + "&friendOpenid=" + player.getOpenId(), 2000);
		}
	}
	
	
	/**
	 * 判断是否时自己的微信密友
	 * 
	 * @param openid
	 * @param sopenid
	 */
	public boolean isUnregFriend(String playerId, String sopenid) {
		UnregFriendTaskInfo info = unregFriendInfoCache.get(playerId);
		if (info == null) {
			return false;
		}
		
		List<UnRegFriendInfoPB> unregFriends = info.getUnregFriendList();
		for (UnRegFriendInfoPB friendInfo : unregFriends) {
			if (friendInfo.getOpenid().equals(sopenid)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 判断邀请任务是否完成
	 * 
	 * @param player
	 * @param taskId
	 * @return
	 */
	private boolean isTaskFinished(Player player, int taskId) {
		FriendInviteTaskCfg taskCfg = HawkConfigManager.getInstance().getConfigByKey(FriendInviteTaskCfg.class, taskId);
		if (taskCfg == null) {
			return false;
		}
		
		FriendInviteInfo friendInviteInfo = RelationService.getInstance().getFriendInviteInfo(player.getId());
		Map<String, TaskAttrInfo> inviteSuccFriends = friendInviteInfo.getInviteSuccFriends();
		int count = 0;
		int cityLevel = taskCfg.getCityLevel();
		if (cityLevel == 0) {
			count = inviteSuccFriends.size();
		} else {
			for (TaskAttrInfo taskAttr : inviteSuccFriends.values()) {
				if (taskAttr.getCityLevel() >= cityLevel) {
					count ++;
				}
			}
		}
		
		// 任务未完成
		if (count < taskCfg.getCount()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 删除守护关系缓存， 该操作不频繁，加点日志.防止数据有问题.
	 * @param playerId
	 * @param targetPlayerId
	 */
	public void deleteGuardCacheInfo(String playerId, String targetPlayerId) {		
		PlayerRelationEntity oldRelationEntity = playerGuardMap.remove(playerId);
		String removePlayerId;
		if (oldRelationEntity == null) {
			removePlayerId = "NULL";
		} else {
			removePlayerId = oldRelationEntity.getTargetPlayerId();
		}
		logger.info("playerId:{} delete guardCacheInfo targetPlayerId:{}", playerId, removePlayerId);
		
		
		oldRelationEntity = playerGuardMap.remove(targetPlayerId);
		if (oldRelationEntity == null) {
			removePlayerId = "NULL";
		} else {
			removePlayerId = oldRelationEntity.getTargetPlayerId();
		}
		logger.info("playerId:{} delete guardCacheInfo targetPlayerId:{}", targetPlayerId, removePlayerId);
	}
	
	/**
	 * 更新缓存的信息
	 * 没有也走更新的.
	 */
	public void updateGuardCacheInfo(PlayerRelationEntity relationEntity, PlayerRelationEntity targetRelationEntity) {
		logger.info("playerId:{} targetPlayer:{} updateGuardCache addValue:{}", relationEntity.getPlayerId(), relationEntity.getTargetPlayerId(), 
				relationEntity.getGuardValue());	
		if (relationEntity.getGuardValue() != targetRelationEntity.getGuardValue()) {
			if (relationEntity.getGuardValue() > targetRelationEntity.getGuardValue()) {
				targetRelationEntity.setGuardValue(relationEntity.getGuardValue());
			} else {
				relationEntity.setGuardValue(targetRelationEntity.getGuardValue());
			}
			logger.error("fixRelationValue playerId:{} targetPlayerId{}  relationEntityvalue:{} targetRelationEntity:{}", 
					relationEntity.getPlayerId(), relationEntity.getTargetPlayerId(), relationEntity.getGuardValue(),
					targetRelationEntity.getGuardValue());
		}
		
		playerGuardMap.put(relationEntity.getPlayerId(), relationEntity);
						
		playerGuardMap.put(targetRelationEntity.getPlayerId(), targetRelationEntity);
	}
	
	/**
	 * 是否有守护者.
	 * @param playerId
	 * @return
	 */
	public boolean hasGuarder(String playerId) {
		return playerGuardMap.containsKey(playerId);
	}
	/**
	 * 获取玩家的守护值, 先取本地,本地没有再取跨服.
	 * 小于0说明是守护的人
	 * @param playerId
	 * @return
	 */
	public int getGuardValue(String playerId) {
		//先取一次本服的.
		PlayerRelationEntity localGuardValue = this.playerGuardMap.get(playerId);
		if (localGuardValue != null) {
			return localGuardValue.getGuardValue();
		}
		
		//取跨服的.
		int guardValue = -1; 
		HawkTuple2<String, Integer> playerGuardTuple = this.csGuardMap.get(playerId);
		if (playerGuardTuple != null) {
			guardValue = playerGuardTuple.second;
		}
		
		return guardValue;
	}
		
	/**
	 * 先取本地的,没有就再取跨服的.
	 * @param playerId
	 * @return
	 */
	public String getGuardPlayer(String playerId) {
		PlayerRelationEntity miniRelationEntity = playerGuardMap.get(playerId);
		if (miniRelationEntity != null) {
			return miniRelationEntity.getTargetPlayerId();
		}
		
		HawkTuple3<String, Integer, Integer> tuple3 = csGuardMap.get(playerId);
		if (tuple3 != null) {
			return tuple3.first;
		}
		
		return null;
	}
	
	/**
	 * 获取玩家的守护信息
	 * @param playerId
	 * @return
	 */
	public PlayerGuardInviteEntity getGuardInvite(String playerId) {
		return guardInviteMap.get(playerId);
	}
	
	
	/**
	 * 加载所有的跨服守护关系
	 */
	public void loadAllCsPlayerGuard() {
		Map<String, HawkTuple3<String, Integer, Integer>> redisGuardMap = RedisProxy.getInstance().loadCrossGuard(GsConfig.getInstance().getServerId());
		csGuardMap = new ConcurrentHashMap<String, HawkTuple3<String, Integer, Integer>>(redisGuardMap);
	}
	
	public PlayerRelationEntity getGuardRelationEntity(String playerId) {
		String guardPlayerId = this.getGuardPlayer(playerId);
		if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
			return this.getPlayerRelationEntity(playerId, guardPlayerId);
		} 
		
		return null;
	}
	
	/**
	 * 玩家从本服跨服出去,在本服执行.
	 * @param playerId
	 */
	public void onPlayerCross(String toCrossServerId, String playerId) {
		PlayerRelationEntity playerRelationEntity = this.getGuardRelationEntity(playerId);
		if (playerRelationEntity != null) {
			RedisProxy.getInstance().addCrossGuard(toCrossServerId, playerId, new HawkTuple3<String, Integer, Integer>(playerRelationEntity.getTargetPlayerId(),
					playerRelationEntity.getGuardValue(), playerRelationEntity.getDressId()), CrossActivityService.getInstance().getCrossKeyExpireTime());
		}
	}
	
	/**
	 * 在目标服退出跨服
	 */
	public void onPlayerExitCross(String playerId) {
		HawkTuple3<String, Integer, Integer> tuple3 = this.csGuardMap.remove(playerId);
		if (tuple3 != null) {
			RedisProxy.getInstance().deleteCrossGuard(GsConfig.getInstance().getServerId(), 
					playerId);
		}
	}
	
	/**
	 * 跨服玩家第一次登陆.
	 * @param playerId
	 */
	public void onCsPlayerFirstLoginIn(String playerId) {
		HawkTuple3<String, Integer, Integer> tuple3 = RedisProxy.getInstance().getCrossGuard(GsConfig.getInstance().getServerId(), playerId);
		if (tuple3 != null) {
			this.csGuardMap.put(playerId, tuple3);
		}		
	}
	
	/**
	 * 检测邀请是否到期
	 */
	private void checkInviteTimeOut() {
		int curTime = HawkTime.getSeconds();
		for (PlayerGuardInviteEntity entity : guardInviteMap.values()) {
			try {
				if (entity.getEndTime() > curTime) {
					continue;
				}
				this.removeGuardInvite(entity.getPlayerId());				
				//返还玩家消耗
				returnPlayerInviteCost(entity);
			} catch (Exception e) {
				HawkException.catchException(e);
			}				
		}		
	}
	
	public void removeGuardInvite(String playerId) {
		guardInviteMap.remove(playerId);
		LocalRedis.getInstance().deleteGuardInvite(playerId);		
	}
	
	/**
	 * 获取守护的作用号
	 * @param playerId
	 * @param effectId
	 * @return
	 */
	public int getEffectValue(String playerId, int effectId) {
		int guardValue = this.getGuardValue(playerId);
		// 小于0 说明没有建立守护关系
		if (guardValue < 0) {
			return 0;
		}		
		GuardianAttributeCfg guardianAttributeCfg = AssembleDataManager.getInstance().getGuardianAttribute(guardValue);
		
		return guardianAttributeCfg.getEffectSeverMap().getOrDefault(effectId, 0);
	}
	
	/**
	 * 构建守护系统玩家.
	 * @param playerId
	 * @return
	 */
	public GuardPlayerMsg.Builder builderGuardPlayer(PlayerRelationEntity relationEntity, boolean showPos, boolean showOfflineTime) {
		Player player = GlobalData.getInstance().makesurePlayer(relationEntity.getTargetPlayerId());
		GuardPlayerMsg.Builder builder = GuardPlayerMsg.newBuilder();
		builder.setPlayerId(relationEntity.getTargetPlayerId());
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(player));		
		if (showPos) {
			int[] posArray = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
			builder.setX(posArray[0]);
			builder.setY(posArray[1]);
		} else {
			builder.setX(-1);
			builder.setY(-1);
		}		
		builder.setGuildTag(this.getGuildTag(player.getId()));
		if (showOfflineTime) {
			builder.setOfflineTime(player.isActiveOnline() ? 0 : player.getLogoutTime());	
		} else {
			builder.setOfflineTime(-1);
		}		
		
		return builder;
	}
	
	/**
	 * 玩家的守护信息.
	 * @param player
	 */
	public void synGuardInfo(Player player) {
		String guardPlayerId = this.getGuardPlayer(player.getId());
		
		GuardInfoResp.Builder sbuilder = GuardInfoResp.newBuilder();
		if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
			PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(player.getId(), guardPlayerId);		
						
			if (playerRelationEntity != null) {
				Player guardPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
				GuardPlayerMsg.Builder playerMsg = this.builderGuardPlayer(playerRelationEntity, 
						guardPlayer.getData().checkFlagSet(PlayerFlagPosition.GUARD_POSITION), 
						guardPlayer.getData().checkFlagSet(PlayerFlagPosition.GUARD_ONLINE_STATUS));
				sbuilder.setPlayerInfo(playerMsg);
				sbuilder.setGuardValue(playerRelationEntity.getGuardValue());
				sbuilder.setWarFeverEndTime(guardPlayer.getData().getPlayerBaseEntity().getWarFeverEndTime());
				WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
				if (wp != null) {
					if (guardPlayer.getData().checkFlagSet(PlayerFlagPosition.GUARD_PROTECTED_TIME)) {
						StatusDataEntity sde = guardPlayer.getData().getStatusById(Const.EffType.CITY_SHIELD_VALUE);
						if (sde != null) {
							if (sde.getEndTime() >= wp.getShowProtectedEndTime()) {
								sbuilder.setProtectedStartTime(sde.getStartTime());
							}
						}							
						sbuilder.setProtectedEndTime(wp.getShowProtectedEndTime());						
					} else {						
						sbuilder.setProtectedEndTime(-1);											
					}
				} else {
					sbuilder.setProtectedEndTime(0);
					sbuilder.setProtectedStartTime(0);
				}
				sbuilder.setFireEndTime(guardPlayer.getData().getPlayerBaseEntity().getOnFireEndTime());
			}			
		} else {
			sbuilder.setProtectedEndTime(-1);
			sbuilder.setGuardValue(-1);
			sbuilder.setFireEndTime(-1);
			sbuilder.setWarFeverEndTime(-1);
		}							
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.GUARD_INFO_RESP_VALUE, sbuilder);
		
		player.sendProtocol(hawkProtocol);
	}
	
	/**
	 * 拉取申请的列表
	 * @param player
	 */
	public void synGuardInviteList(Player player) {
		Map<String, PlayerGuardInviteEntity> inviteMap = this.guardInviteMap;
		GuardInviteListResp.Builder respBuilder = GuardInviteListResp.newBuilder(); 
		for (PlayerGuardInviteEntity invite : inviteMap.values()) {
			if (!invite.getTargetPlayerId().equals(player.getId())) {
				continue;
			}
			PlayerRelationEntity pre = this.getPlayerRelationEntity(player.getId(), invite.getPlayerId());
			if (pre == null) {
				logger.error("playerId:{} want to guard targetPlayerId:{} but they are not friend", invite.getPlayerId(), invite.getTargetPlayerId());
				continue;
			}
			
			GuardPlayerMsg.Builder playerBuilder = this.builderGuardPlayer(pre, false, false);
			respBuilder.addMsg(playerBuilder);
		}
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.GUARD_INVITE_LIST_RESP_VALUE, respBuilder);
		player.sendProtocol(hawkProtocol);
	}
	
	/**
	 * 邀请玩家.
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	public int onGuardInvitePlayer(Player player, String targetPlayerId) {
		logger.info("playerId:{} invite guard targetPlayerId:{}", player.getId(), targetPlayerId);
		if (!HawkOSOperator.isEmptyString(this.getGuardPlayer(player.getId()))) {
			return Status.Error.GUARD_ALEARDY_CREATED_VALUE;
		}
		
		if (!HawkOSOperator.isEmptyString(this.getGuardPlayer(targetPlayerId))) {
			return Status.Error.GUARD_ALEARDY_CREATED_VALUE;
		}
		
		PlayerGuardInviteEntity guardInviteEntity = guardInviteMap.get(player.getId());
		if (guardInviteEntity != null) {
			return Status.Error.GUARD_ALEARDY_INVITED_VALUE;
		}
		
		if (!this.isFriend(player.getId(), targetPlayerId)) {
			return Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE;
		}			
		
		GuardianConstConfig constCfg = GuardianConstConfig.getInstance();
		guardInviteEntity = new PlayerGuardInviteEntity();
		guardInviteEntity.setPlayerId(player.getId());
		guardInviteEntity.setTargetPlayerId(targetPlayerId);
		guardInviteEntity.setEndTime(HawkTime.getSeconds() + constCfg.getValidityTime());
		
		guardInviteMap.put(player.getId(), guardInviteEntity);
		LocalRedis.getInstance().addGuardInvite(guardInviteEntity);
		
		//记录tlog
		LogUtil.logGuardRelation(player, targetPlayerId, GsConst.GuardType.INVITE);	
		//给对方同步一条HUD
		this.synGuardHud(targetPlayerId);
		
		PlayerRelationEntity pre = this.getPlayerRelationEntity(player.getId(), targetPlayerId);
		CanInvitePlayerMsg.Builder playerBuilder = this.buildCanInvitePlayer(pre);
		GuardInvitePlayerResp.Builder respBuilder = GuardInvitePlayerResp.newBuilder();
		respBuilder.setMsg(playerBuilder);
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.GUARD_INVITE_PLAYER_RESP_VALUE, respBuilder);
		player.sendProtocol(hawkProtocol);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 邀请列表里面的玩家.
	 * @param relationEntity
	 * @return
	 */
	public CanInvitePlayerMsg.Builder buildCanInvitePlayer(PlayerRelationEntity relationEntity) {
		Player player = GlobalData.getInstance().makesurePlayer(relationEntity.getTargetPlayerId());
		PlayerGuardInviteEntity inviteEntity = this.getGuardInvite(relationEntity.getPlayerId());
		CanInvitePlayerMsg.Builder builder = CanInvitePlayerMsg.newBuilder();
		builder.setRemark(relationEntity.getRemark() == null ? "" : relationEntity.getRemark());
		builder.setPlayerId(relationEntity.getTargetPlayerId());
		builder.setName(player.getName());
		builder.setGuildTag(this.getGuildTag(relationEntity.getTargetPlayerId()));
		builder.setIcon(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		builder.setBattleValue(player.getPower());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		if (this.hasGuarder(relationEntity.getTargetPlayerId())) {
			builder.setState(InviteState.GUARD);
		} else {
			if (inviteEntity != null && inviteEntity.getTargetPlayerId().equals(relationEntity.getTargetPlayerId())) {
				builder.setState(InviteState.INVITED);
				builder.setInviteEndTime(inviteEntity.getEndTime());
			} else {
				builder.setState(InviteState.NONE);
			}
		}
		
		return builder;
	}
	
	/**
	 * 拉取在邀请列表里面的玩家.
	 * @param player
	 */
	public void synGuardCanInvitePlayers(Player player) {
		List<PlayerRelationEntity> friendList = this.getPlayerRelationList(player.getId(), GsConst.RelationType.FRIEND);
		GuardCanInvitePlayersResp.Builder sbuilder = GuardCanInvitePlayersResp.newBuilder(); 
		for (PlayerRelationEntity entity : friendList) {
			try {
                Player tarPlayer = GlobalData.getInstance().makesurePlayer(entity.getTargetPlayerId());
				if (tarPlayer == null) {
					HawkLog.errPrintln("relationService synGuardCanInvitePlayers tarPlayer null error, playerId: {}, tarPlayerId: {}", player.getId(), entity.getTargetPlayerId());
					continue;
				}
				sbuilder.addPlayerMsg(this.buildCanInvitePlayer(entity));	
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.GUARD_CAN_INVITE_PLAYERS_RESP_VALUE, sbuilder);
		player.sendProtocol(hawkProtocol);
	}
	
	/**
	 * 该函数肯定是不能在跨服下调用的.
	 * @param playerId
	 * @param giftId
	 */
	public void onSendGift(String playerId, int giftId) {
		logger.info("playerId:{} guard send giftId:{}", playerId, giftId);
		String guardPlayerId = this.getGuardPlayer(playerId);
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			logger.error("playerId:{} has no guardPlayer giftId:{}", playerId, giftId);
			
			return;
		}
		GuardianGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GuardianGiftCfg.class, giftId);
		PlayerRelationEntity playerRelationEnitty = this.getPlayerRelationEntity(playerId, guardPlayerId);
		PlayerRelationEntity targetPlayerEntity = this.getPlayerRelationEntity(guardPlayerId, playerId);
		if (playerRelationEnitty == null || targetPlayerEntity == null) {
			logger.error("playerId:{} guardPlayerId:{} are not friend", playerId, guardPlayerId);
			
			return;
		}
		
		addGuardExp(playerId, guardPlayerId, giftCfg.getGuardianValue(), playerRelationEnitty, targetPlayerEntity);		
	}

	public void addGuardExp(String playerId, String guardPlayerId, int addValue,
			PlayerRelationEntity playerRelationEnitty, PlayerRelationEntity targetPlayerEntity) {
		long curTime = HawkTime.getMillisecond();
		GuardianAttributeCfg oldCfg = AssembleDataManager.getInstance().getGuardianAttribute(playerRelationEnitty.getGuardValue());
		playerRelationEnitty.setGuardValue(playerRelationEnitty.getGuardValue() +  addValue);
		targetPlayerEntity.setGuardValue(targetPlayerEntity.getGuardValue() + addValue);
		playerRelationEnitty.setOperationTime(curTime);
		targetPlayerEntity.setOperationTime(curTime);
		
		//更新缓存消息.
		this.updateGuardCacheInfo(playerRelationEnitty, targetPlayerEntity);
		//更新排行榜
		RankService.getInstance().addGuardRank(playerId, guardPlayerId, playerRelationEnitty.getGuardValue(), curTime);
		
		//记录守护值
		Player operatePlayer = GlobalData.getInstance().makesurePlayer(playerId);
		if (operatePlayer != null) {
			LogUtil.logGuardValue(operatePlayer, playerRelationEnitty.getGuardValue());
		}
		
		Player operatedPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
		if (operatedPlayer != null) {
			LogUtil.logGuardValue(operatedPlayer, playerRelationEnitty.getGuardValue());
		}
		
		GuardianAttributeCfg attributeCfg = AssembleDataManager.getInstance().getGuardianAttribute(playerRelationEnitty.getGuardValue());
		if (attributeCfg.getId() != oldCfg.getId()) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player.isActiveOnline()) {
				player.getPush().syncPlayerEffect(attributeCfg.getEffTypeArray()); 
			}
			
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
			if (targetPlayer.isActiveOnline()) {
				targetPlayer.getPush().syncPlayerEffect(attributeCfg.getEffTypeArray());
			}
			
			MailParames.Builder builder = MailParames.newBuilder()
					.setPlayerId(player.getId()).setMailId(MailId.GUARD_UPGRADE);
			builder.addContents( targetPlayer.getName());
			SystemMailService.getInstance().sendMail(builder.build());
			
			builder = MailParames.newBuilder()
					.setPlayerId(targetPlayer.getId()).setMailId(MailId.GUARD_UPGRADE);
			builder.addContents(player.getName());
			
			SystemMailService.getInstance().sendMail(builder.build());
		}
	}
	
	/**
	 * 关系建立和关系解除的时候同步作用号
	 * @param player
	 */
	public void synEffect(Player player) {
		ConfigIterator<GuardianAttributeCfg> attributeCfgIterator = HawkConfigManager.getInstance().getConfigIterator(GuardianAttributeCfg.class);
		GuardianAttributeCfg attributeCfg = null;
		while (attributeCfgIterator.hasNext()) {
			GuardianAttributeCfg tmpCfg = attributeCfgIterator.next();
			if (attributeCfg == null) {
				attributeCfg = tmpCfg;
			} else {
				if (tmpCfg.getId() >= attributeCfg.getId()) {
					attributeCfg = tmpCfg;
				}
			}
		}
		
		if (attributeCfg != null) {
			player.getPush().syncPlayerEffect(attributeCfg.getEffTypeArray());
		}		
	}

	public void onGuardDelete(String playerId) {
		String guardPlayerId = this.getGuardPlayer(playerId);
		logger.info("guard delete playerId:{} guardPlayerId:{}", playerId, guardPlayerId);
		if (guardPlayerId == null) {
			logger.info("guard delete playerId:{} guardPlayerId:{} is null", playerId, guardPlayerId);
			
			return;
		}		
		
		//清除数据.
		GuardianConstConfig constCfg = GuardianConstConfig.getInstance(); 
		PlayerRelationEntity playerEntity = this.getPlayerRelationEntity(playerId, guardPlayerId);
		PlayerRelationEntity guardEntity = this.getPlayerRelationEntity(guardPlayerId, playerId);
		playerEntity.setGuard(false);
		playerEntity.setGuardValue((int)(playerEntity.getGuardValue() * 1L * constCfg.getKeepRate() / GsConst.RANDOM_MYRIABIT_BASE));
		playerEntity.setDressId(0);
		guardEntity.setGuard(false);
		guardEntity.setGuardValue((int)(guardEntity.getGuardValue() * 1L * constCfg.getKeepRate() / GsConst.RANDOM_MYRIABIT_BASE));
		guardEntity.setDressId(0);
					
		//删除排行榜
		RankService.getInstance().deleteGuardRank(playerId, guardPlayerId);
		//清除缓存
		this.deleteGuardCacheInfo(playerId, guardPlayerId);
		//同步
		this.synGuardDelete(playerId);
		this.synGuardDelete(guardPlayerId);
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logGuardRelation(player, guardPlayerId, GsConst.GuardType.DELETE);
		
		Player tmpPlayer = GlobalData.getInstance().makesurePlayer(playerId);
		MailParames.Builder builder = MailParames.newBuilder()
				.setPlayerId(guardPlayerId).setMailId(MailId.GUARD_DELETE);
		builder.addContents(tmpPlayer.getName());
		builder.addSubTitles(tmpPlayer.getName());
		SystemMailService.getInstance().sendMail(builder.build());
		this.synEffect(tmpPlayer);
		LogUtil.logGuardValue(tmpPlayer, guardEntity.getGuardValue());
		
		
		tmpPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
		builder = MailParames.newBuilder()
				.setPlayerId(playerId).setMailId(MailId.GUARD_DELETE);
		builder.addContents(tmpPlayer.getName());
		builder.addSubTitles(tmpPlayer.getName());
		SystemMailService.getInstance().sendMail(builder.build());
		this.synEffect(tmpPlayer);
		LogUtil.logGuardValue(tmpPlayer, playerEntity.getGuardValue());
		
		//删除之后同步一遍世界点的信息.
		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
		if (wp != null) {
			WorldPointService.getInstance().getWorldScene().update(wp.getAoiObjId());
		}
		
		wp = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		if (wp != null) {
			WorldPointService.getInstance().getWorldScene().update(wp.getAoiObjId());
		}
				
	}
	
	/**
	 * 同步守护请求删除.
	 * @param playerId
	 */
	public void synGuardInviteDelete(String playerId, String targetPlayerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return;
		}
		GuardInviteDeleteSyn.Builder sbuilder = GuardInviteDeleteSyn.newBuilder();
		sbuilder.setPlayerId(targetPlayerId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUARD_INVITE_DELETE_SYN_VALUE, sbuilder);
		player.sendProtocol(protocol);
		
	}
	
	/**
	 * 同步守护删除.
	 * @param playerId
	 */
	public void synGuardDelete(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return;
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUARD_RELATION_DELETE_RESP_VALUE);
		player.sendProtocol(protocol);
	}
	
	/**
	 * 处理申请.
	 * @param playerId
	 * @param reqPlayerId
	 * @param oper
	 */
	public int onGuardInvieteHandle(String playerId, String reqPlayerId, OperationType oper) {
		logger.info("guardInviteHandle playerId:{}, reqPlayerId:{} oper:{}", playerId, reqPlayerId, oper);			
		if (oper == OperationType.AGREEE) {
			PlayerGuardInviteEntity inviteEntity = guardInviteMap.get(reqPlayerId);
			if (inviteEntity == null) {
				return Status.Error.GUARD_INVITE_NOT_EXIST_VALUE;
			}
			return agreeGuardInvite(playerId, reqPlayerId);
		} else {
			if (!HawkOSOperator.isEmptyString(reqPlayerId)) {
				PlayerGuardInviteEntity inviteEntity = guardInviteMap.get(reqPlayerId);
				if (inviteEntity == null) {
					return Status.Error.GUARD_INVITE_NOT_EXIST_VALUE;
				}
				//删除申请.
				this.removeGuardInvite(reqPlayerId);
				this.returnPlayerInviteCost(inviteEntity);
				synGuardInviteDelete(playerId, reqPlayerId);
			} else {
				this.deleteAllGuardInvite(playerId);
				synGuardInviteDelete(playerId, "");
			}
			
			return Status.SysError.SUCCESS_OK_VALUE;
		}
	}
	
	 
	
	/**
	 * 删除所有对playerId 发出的邀请
	 * @param playerId
	 */
	public void deleteAllGuardInvite(String playerId) {
		Player.logger.info("delete all guard invite playerId:{}", playerId);
		for (PlayerGuardInviteEntity pgie : guardInviteMap.values()) {
			if (!pgie.getTargetPlayerId().equals(playerId)) {
				continue;
			} 
			
			this.removeGuardInvite(pgie.getPlayerId());
			this.returnPlayerInviteCost(pgie);
		}
	} 
	
	/**
	 * 初始化设置相关.
	 * @param playerId
	 */
	public void initGuardSetting(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null || player.getData().checkFlagSet(PlayerFlagPosition.GUARD_INIT)) {
			return;
		}
		
		player.getData().setFlag(PlayerFlagPosition.GUARD_ONLINE_STATUS, 1);
		player.getData().setFlag(PlayerFlagPosition.GUARD_POSITION, 1);
		player.getData().setFlag(PlayerFlagPosition.GUARD_PROTECTED_TIME, 1);
		player.getData().setFlag(PlayerFlagPosition.GUARD_RECEIVE_RADER, 1);
		player.getData().setFlag(PlayerFlagPosition.GUARD_INIT, 1);
		
		player.getPush().synPlayerFlag();
	} 
	
	@SuppressWarnings("deprecation")
	public int agreeGuardInvite(String playerId, String reqPlayerId) {
		if (this.hasGuarder(playerId) || this.hasGuarder(reqPlayerId)) {
			return Status.Error.GUARD_ALEARDY_CREATED_VALUE;
		}
		
		PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(playerId, reqPlayerId);
		PlayerRelationEntity reqPlayerRelationEntity = this.getPlayerRelationEntity(reqPlayerId, playerId);
		if (playerRelationEntity == null || reqPlayerRelationEntity == null) {
			return Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE;
		}
		long curTime = HawkTime.getMillisecond();
		playerRelationEntity.setGuard(true);
		reqPlayerRelationEntity.setGuard(true);
		playerRelationEntity.setOperationTime(curTime);
		reqPlayerRelationEntity.setOperationTime(curTime);
		
		//删除请求.
		this.removeGuardInvite(reqPlayerId);
		
		this.updateGuardCacheInfo(playerRelationEntity, reqPlayerRelationEntity);
		
		this.synGuardCreate(playerRelationEntity);
		
		//发送建立邮件.
		for (PlayerRelationEntity entity : Arrays.asList(playerRelationEntity, reqPlayerRelationEntity)) {
			Player tmpPlayer = GlobalData.getInstance().makesurePlayer(entity.getTargetPlayerId());
			MailParames.Builder builder = MailParames.newBuilder()
					.setPlayerId(entity.getPlayerId()).setMailId(MailId.GUARD_CREATE);
			builder.addContents(tmpPlayer.getName());
			SystemMailService.getInstance().sendMail(builder.build());
		}
		
		RankService.getInstance().addGuardRank(playerId, reqPlayerId, playerRelationEntity.getGuardValue(), curTime);
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logGuardRelation(player, reqPlayerId, GsConst.GuardType.CREATE);
		//删除其余的申请.
		this.deleteAllGuardInvite(playerId);
		this.deleteAllGuardInvite(reqPlayerId);
		synGuardInviteDelete(playerId, "");
		synGuardInviteDelete(reqPlayerId, "");
		this.initGuardSetting(playerId);
		this.initGuardSetting(reqPlayerId);
		this.synGuardHud(playerId);
		this.synGuardHud(reqPlayerId);
		Player reqPlayer = GlobalData.getInstance().makesurePlayer(reqPlayerId);
		this.synGuardInfo(reqPlayer);
		
		this.synEffect(player);
		this.synEffect(reqPlayer);
		
		//跑马灯
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, NoticeCfgId.GUARD_CREATE, null,
				GameUtil.getPlayerNameWithGuildTag(reqPlayer.getGuildId(), reqPlayer.getName()),
				GameUtil.getPlayerNameWithGuildTag(player.getGuildId(), player.getName())
				);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 同步创建.
	 */
	private void synGuardCreate(PlayerRelationEntity relationEntity) {
		Player player = GlobalData.getInstance().makesurePlayer(relationEntity.getPlayerId());
		if (player != null ) {
			boolean showPos = player.getData().checkFlagSet(PlayerFlagPosition.GUARD_POSITION);
			boolean showTime = player.getData().checkFlagSet(PlayerFlagPosition.GUARD_ONLINE_STATUS);
			
			GuardPlayerMsg.Builder guardBuilder = this.builderGuardPlayer(relationEntity, showPos, showTime);
			GuardRelationCreateSyn.Builder sbuilder = GuardRelationCreateSyn.newBuilder();
			sbuilder.setPlayerInfo(guardBuilder);
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUARD_RELATION_CREATE_SYN_VALUE, sbuilder);
			player.sendProtocol(protocol);
		}			
	}
	
	public void returnPlayerInviteCost(PlayerGuardInviteEntity entity) {
		Player.logger.info("playerId:{} return guardInviteCost", entity.getPlayerId());
		GuardianConstConfig constCfg = GuardianConstConfig.getInstance(); 
		MailParames.Builder builder = MailParames.newBuilder()
				.setPlayerId(entity.getPlayerId()).setMailId(MailId.GUARD_INVITE_RETURN).setRewards(constCfg.getInviteCostList()).setAwardStatus(MailRewardStatus.NOT_GET);
		Player player = GlobalData.getInstance().makesurePlayer(entity.getTargetPlayerId());
		builder.addContents(player.getName());
		
		SystemMailService.getInstance().sendMail(builder.build());
	}

	public Map<String, PlayerRelationEntity> getPlayerGuardMap() {
		return playerGuardMap;
	}	
	
	public int getDressId(String playerId) {
		PlayerRelationEntity relationEntity = this.playerGuardMap.get(playerId);
		if (relationEntity != null) {
			return relationEntity.getDressId();
		} else {
			HawkTuple3<String, Integer, Integer> csTuple3 = this.csGuardMap.get(playerId);
			if (csTuple3 != null) {
				return csTuple3.third;
			}
		}
		
		return 0;
	}
	
	/**
	 * 同步穿戴ID
	 * @param playerId
	 */
	public void synGuardDressId(String playerId) {
		int dressId = this.getDressId(playerId);
		GuardDressResp.Builder sbuilder = GuardDressResp.newBuilder();
		sbuilder.setDressId(dressId);
		PlayerRelationEntity playerRelationEntity = this.getGuardRelationEntity(playerId);
		GuardDressItemInfo.Builder dressItemBuilder = GuardDressItemInfo.newBuilder();
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		
		Map<Integer, Integer> ownMap = player.getData().getPlayerOtherEntity().getDressItemInfoMap();
		List<KeyValuePairInt> ownList = BuilderUtil.buildKeyValuePairIntInt(ownMap);
		dressItemBuilder.addAllOwnerDressItemInfos(ownList);		
		
		if (playerRelationEntity != null) {
			Player tPlayer = GlobalData.getInstance().makesurePlayer(playerRelationEntity.getTargetPlayerId());
			if (tPlayer != null) {
				Map<Integer, Integer> tMap = tPlayer.getData().getPlayerOtherEntity().getDressItemInfoMap();
				List<KeyValuePairInt> tList = BuilderUtil.buildKeyValuePairIntInt(tMap);
				dressItemBuilder.addAllGuardDressItemInfos(tList);
			}			
		}
		
		sbuilder.setDressItemInfo(dressItemBuilder);		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUARD_DRESS_RESP_VALUE, sbuilder);				
		player.sendProtocol(protocol);		
	}
	
	/**
	 * 修改基地特效
	 * @param playerId
	 * @param dressId
	 */
	public int onGuardDressUpdate(String playerId, int dressId) {
		String guardPlayerId = this.getGuardPlayer(playerId);
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			return Status.Error.GUARD_NOT_CREATED_VALUE;
		}
		PlayerRelationEntity playerRelationEntity = this.getPlayerRelationEntity(playerId, guardPlayerId);
		PlayerRelationEntity guardPlayerRelationEntity = this.getPlayerRelationEntity(guardPlayerId, playerId);
		boolean isGuardDressId = AssembleDataManager.getInstance().isGuardDressId(dressId);
		if (!isGuardDressId ) {
			//这里走之前的老逻辑.
			GuardianAttributeCfg attributeCfg = AssembleDataManager.getInstance().getGuardianAttribute(playerRelationEntity.getGuardValue());
			if (attributeCfg == null) {
				HawkLog.errPrintln("guardDressUpdate fail playerId:{}, can not found attributeCfg guardValue:{}", 
						playerId, playerRelationEntity.getGuardValue());
				
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			
			if (!attributeCfg.getUnlockDressIdSeverList().contains(dressId)) {
				return Status.Error.GUARD_LOVE_NOT_ENOUGH_VALUE;
			}
		} else {
			boolean isSingle = AssembleDataManager.getInstance().isSingleGuardDressId(dressId);
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			boolean hasDress = false;
			//如果是单人的特效.
			if (isSingle) {
				hasDress = player.getData().getPlayerOtherEntity().containDressItem(dressId);
				if (!hasDress) {
					return Status.Error.GUARD_DRESS_NOT_HAVE_VALUE;
				}
			} else {
				//找到第二个
				List<Integer> idList = AssembleDataManager.getInstance().getGuardDressList(dressId);
				int singleDressId = idList.get(0);
				hasDress = player.getData().getPlayerOtherEntity().containDressItem(singleDressId);
				if (!hasDress) {
					return Status.Error.GUARD_DRESS_NOT_HAVE_VALUE;
				}
				Player targetPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
				hasDress = targetPlayer.getData().getPlayerOtherEntity().containDressItem(singleDressId);
				if (!hasDress) {
					return Status.Error.GUARD_DRESS_GUARDER_NOT_HAVE_VALUE;
				}
			}
		}		
		
		playerRelationEntity.setDressId(dressId);
		guardPlayerRelationEntity.setDressId(dressId);
		//更新缓存中的信息.
		this.updateGuardCacheInfo(playerRelationEntity, guardPlayerRelationEntity);
		
		this.synGuardDressId(playerId);
		this.synGuardDressId(guardPlayerId);
		
		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
		if (wp != null) {
			WorldPointService.getInstance().getWorldScene().update(wp.getAoiObjId());
		}
		
		wp = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		if (wp != null) {
			WorldPointService.getInstance().getWorldScene().update(wp.getAoiObjId());
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 每天的统计信息
	 */
	public void logStatistics() {
		Map<String, PlayerRelationEntity> logMap = this.playerGuardMap;
		Set<String> alreadyCount = new HashSet<>();
		Map<Integer, Integer> numMap = new HashMap<>();
		for (Entry<String, PlayerRelationEntity> entry : logMap.entrySet()) {
			PlayerRelationEntity relationEntity = entry.getValue();
			if (alreadyCount.contains(entry.getKey()) || alreadyCount.contains(relationEntity.getTargetPlayerId())) {
				continue;
			}
			alreadyCount.add(entry.getKey());
			alreadyCount.add(relationEntity.getTargetPlayerId());
			
			GuardianAttributeCfg cfg = AssembleDataManager.getInstance().getGuardianAttribute(relationEntity.getGuardValue());
			MapUtil.appendIntValue(numMap, cfg.getId(), 1);
		}
		
		for (Entry<Integer, Integer> entry : numMap.entrySet()) {
			LogUtil.logGuardPlayerLevel(entry.getKey(), entry.getValue());
		}
	}
	
	public void synGuardHud(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);		
		synGuardHud(player);
	}
	
	public void synGuardHud(Player player) {
		GuardHudSyn.Builder sbuilder = GuardHudSyn.newBuilder();
		for (PlayerGuardInviteEntity entity : this.guardInviteMap.values()) {
			if (entity.getTargetPlayerId().equals(player.getId())) {
				sbuilder.setInvite(1);
				break;
			}
		}
		
		if (this.hasGuarder(player.getId())) {
			ConfigIterator<GuardianGiftCfg> giftIterator = HawkConfigManager.getInstance().getConfigIterator(GuardianGiftCfg.class);
			Optional<GuardianGiftCfg> gift = giftIterator.stream().filter(giftCfg->giftCfg.isFree()).findAny();
			if (gift.isPresent()) {
				int boughtNum = player.getData().getDailyDataEntity().getGuardGiftNum(gift.get().getGiftId());
				sbuilder.setGift(boughtNum <= 0 ? 1 : 0);
			}
		} else {
			sbuilder.setGift(0);
		}
		
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.GUARD_HUD_SYN_VALUE, sbuilder);
		player.sendProtocol(hawkProtocol);
	}

	public Map<String, PlayerGuardInviteEntity> getGuardInviteMap() {
		return guardInviteMap;
	}	
	
	/**
	 * 
	 * @param sourcePlayer 观察者
	 * @param guardPlayerId 被观察者。
	 */
	public void synGuardUpdate(String sourcePlayerId, String guardPlayerId) {
		Player sourcePlayer = GlobalData.getInstance().makesurePlayer(sourcePlayerId);
		Player guardPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
		GuardInfoUpdateSyn.Builder sbuilder = GuardInfoUpdateSyn.newBuilder();
		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
		if (wp != null) {
			long endTime = wp.getShowProtectedEndTime();
			if (!guardPlayer.getData().checkFlagSet(PlayerFlagPosition.GUARD_PROTECTED_TIME)) {
				sbuilder.setProtectedEndTime(-1);
			} else {
				StatusDataEntity sde = guardPlayer.getData().getStatusById(Const.EffType.CITY_SHIELD_VALUE);
				if (sde != null) {
					if (sde.getEndTime() >= wp.getShowProtectedEndTime()) {
						sbuilder.setProtectedStartTime(sde.getStartTime());
					}
				}
				sbuilder.setProtectedEndTime(endTime);
			}
		} else {
			sbuilder.setProtectedEndTime(0);
			sbuilder.setProtectedStartTime(0);
		}
		sbuilder.setFireEndTime(guardPlayer.getData().getPlayerBaseEntity().getOnFireEndTime());
		sbuilder.setWarFeverEndTime(guardPlayer.getData().getPlayerBaseEntity().getWarFeverEndTime());
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.GUARD_INFO_UPDATE_SYN, sbuilder);
		sourcePlayer.sendProtocol(respProtocol);
	}
	
	/**添加仇恨值
	 * @param atkPlayerId 玩家1
	 * @param defPlayerId 玩家2
	 * @param killAtkPower 玩家1死的战力
	 * @param killDefPower 玩家2死的战力
	 */
	public void addEnemyHateValue(String atkPlayerId, String defPlayerId, long killAtkPower, long killDefPower){
		//加一些限制，之前榜单有数据，则不限制(存是双方一起存的,取的时候取一方的就行)
		long beforeKillTotalPower = RedisProxy.getInstance().getPlayerHateTotal(atkPlayerId, defPlayerId);
		long totalPower = killAtkPower + killDefPower;
		if (beforeKillTotalPower == 0 && totalPower < ConstProperty.getInstance().getHatePowerLoseLimit()) {
			return;
		}
		//总榜
		RedisProxy.getInstance().updatePlayerHateTotal(atkPlayerId, totalPower, defPlayerId);
		RedisProxy.getInstance().updatePlayerHateTotal(defPlayerId, totalPower, atkPlayerId);
		//双方互相杀掉的战力数据存储
		RedisProxy.getInstance().updatePlayerHateSingle(atkPlayerId, killDefPower, defPlayerId);
		RedisProxy.getInstance().updatePlayerHateSingle(defPlayerId, killAtkPower, atkPlayerId);
		logger.info("RelationService addEnemyHateValue atkPlayerId:{}, defPlayerId:{}, killAtkPower:{}, killDefPower:{} , beforeKillTotalPower:{}",atkPlayerId ,defPlayerId, killAtkPower, killDefPower, beforeKillTotalPower );
		//判断玩家是否在线
		Player atkPlayer = GlobalData.getInstance().getActivePlayer(atkPlayerId);
		if (atkPlayer != null) {
			long rank = RedisProxy.getInstance().getHateRank(atkPlayerId, defPlayerId);
			if (rank <= ConstProperty.getInstance().getHateRankShowNum()) {
				//push 排行榜信息
				syncHateRankList(atkPlayer);
			}
		}
		Player defPlayer = GlobalData.getInstance().getActivePlayer(defPlayerId);
		if (defPlayer != null) {
			long rank = RedisProxy.getInstance().getHateRank(defPlayerId, atkPlayerId);
			if (rank <= ConstProperty.getInstance().getHateRankShowNum()) {
				//push 排行榜信息
				syncHateRankList(defPlayer);
			}
		}
	}
	
	/**同步玩家仇恨名单
	 * @param player
	 */
	public void syncHateRankList(Player player){
		SyncHateRankList.Builder builder = SyncHateRankList.newBuilder();
		Set<Tuple> rankList = RedisProxy.getInstance().getPlayerHateTotalRankList(player.getId(), ConstProperty.getInstance().getHateRankShowNum());
		for (Tuple tuple : rankList) {
			Player toPlayer = GlobalData.getInstance().makesurePlayer(tuple.getElement());
			if (toPlayer == null) {
				continue;
			} else {
				HateInfoMsg.Builder msgBuilder = HateInfoMsg.newBuilder();
				msgBuilder.setPlayerId(toPlayer.getId());
				msgBuilder.setPlayerName(toPlayer.getName());
				msgBuilder.setGuildName(this.getGuildTag(toPlayer.getId()));
				//击杀该玩家战力
				long killPower = RedisProxy.getInstance().getPlayerHateSingle(player.getId(), toPlayer.getId());
				//双方总击杀战力 tuple.getScore()
				long losePower = (long) (tuple.getScore() - killPower > 0 ? tuple.getScore() - killPower : 0);
				msgBuilder.setKillPower(killPower);
				msgBuilder.setLosePower(losePower);
				builder.addHateList(msgBuilder);
			}
		}
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.SYNC_HATE_RANK_INFO, builder);
		player.sendProtocol(respProtocol);
	}
	

	/**
	 * 添加离线玩家.
	 * @param player
	 */
	public void addOffinePlayer(Player player) {
		//跨服玩家不做tick
		if (player.isCsPlayer()) {
			return;
		}
		
		//没有守护
		if (!this.hasGuarder(player.getId())) {
			return;
		}
		Map<Integer, Integer> dressMap = player.getData().getPlayerOtherEntity().getDressItemInfoMap();
		if (dressMap.isEmpty()) {
			return;
		}
		
		Map<Integer, Integer> validTimeDressMap = new HashMap<>();
		for (Entry<Integer, Integer> entry : dressMap.entrySet()) {
			if (entry.getValue() < 0) {
				continue;
			}
			validTimeDressMap.put(entry.getKey(), entry.getValue());
		}
		//没有有效期时间的加入.
		if (validTimeDressMap.isEmpty()) {
			return;
		}
		
		logger.info("add offline player id:{}", player.getId());
		offlinePlayer.put(player.getId(), validTimeDressMap);
	}
	
	/**
	 * 移除离线玩家.
	 * @param player
	 */
	public void removeOffinePlayer(Player player) {
		boolean removeResult = offlinePlayer.remove(player.getId()) != null;
		if (removeResult) {
			logger.info("remove offline player id:{}", player.getId());
		}		
	}
	
	public void checkGuardDressTimeOut() {
		int curTime = HawkTime.getSeconds();
		for (Entry<String, Map<Integer, Integer>> entry : offlinePlayer.entrySet()) {
			try {
				Set<Integer> removeDressIdSet = new HashSet<>();
				for (Entry<Integer, Integer> dressIdTime : entry.getValue().entrySet()) {
					if (dressIdTime.getValue() < curTime) {
						removeDressIdSet.add(dressIdTime.getKey());
					}
				}
				
				if (!removeDressIdSet.isEmpty()) {
					for (Integer removeId : removeDressIdSet) {
						entry.getValue().remove(removeId);
					}
					
					//通知玩家修改数据。
					Player player = GlobalData.getInstance().makesurePlayer(entry.getKey());
					HawkTaskManager.getInstance().postMsg(player.getXid(), new CheckGuardDressMsg());
				}
				
				//不再进行循环了.
				if (entry.getValue().isEmpty()) {
					offlinePlayer.remove(entry.getKey());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 删除玩家.
	 * @param playerId
	 */
	public void deletePlayer(String playerId) {
		logger.info("delete player playerId:{}", playerId);
		//删除好友的是会返还守护.
		Map<String, PlayerRelationEntity> map = this.getPlayerRelationMap(playerId);
		for (Entry<String, PlayerRelationEntity> entry : map.entrySet()) {
			PlayerRelationEntity pre = entry.getValue();
			if (pre.getType() == RelationType.FRIEND) {
				this.friendDelete(pre.getPlayerId(), pre.getTargetPlayerId());
			} else {
				this.blacklistDelete(pre.getPlayerId(), pre.getTargetPlayerId());
			}
		}
		
		Set<String> keySet = relationMap.keySet();
		for (String reqId : keySet) {
			//这里必须先做一个判断,因为删除黑名单本身会有打印.
			if (this.isBlacklist(reqId, playerId)) {
				this.deleteBlacklist(reqId, playerId);
			}			
		}
		
		//删除别人向我发出的申请
		List<String> playerIdList = new ArrayList<>();
		this.getPlayerRelationApplyMap(playerId).keySet().stream().forEach(playerIdList::add);	
		if (!playerIdList.isEmpty()) {			
			this.deleteApply(playerId, playerIdList);					
		}
		Set<String> playerIdSet = new HashSet<>();
		relationApplyMap.keySet().stream().forEach(playerIdSet::add);
		for (String applyedPlayerId : playerIdSet) {
			if (getPlayerRelationApply(applyedPlayerId, playerId) != null) {
				this.deleteApply(applyedPlayerId, Arrays.asList(applyedPlayerId));
			}
		}			
	}
}
 