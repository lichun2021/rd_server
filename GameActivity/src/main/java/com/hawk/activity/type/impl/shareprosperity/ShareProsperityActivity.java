package com.hawk.activity.type.impl.shareprosperity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.shareprosperity.cfg.ShareProsperityKVCfg;
import com.hawk.activity.type.impl.shareprosperity.cfg.ShareProsperityRewardCfg;
import com.hawk.activity.type.impl.shareprosperity.entity.ShareProsperityEntity;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.protocol.Activity.ShareProperityActivityInfo;
import com.hawk.game.protocol.Activity.ShareProperityRolePB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

import org.hawk.app.HawkApp;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 有福同享 376活动 （新服充值给老服返利）
 * 
 * @author lating
 *
 */
public class ShareProsperityActivity extends ActivityBase {

	/**
	 * 记录触发该活动的账号信息
	 */
	private static final String ACCOUNT_ACTIVITY_KEY = "activity376_account";

	private Map<String, ServerInfo> serverInfoMap = new ConcurrentHashMap<>();
	private AtomicLong serverInfoRefreshTime = new AtomicLong(0);
	
	private Map<String, Long> activityOpenPlayerMap = new ConcurrentHashMap<>();
	

	public ShareProsperityActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SHARE_PROSPERITY_376;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ShareProsperityActivity activity = new ShareProsperityActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ShareProsperityEntity> queryList = HawkDBManager.getInstance().query(
				"from ShareProsperityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && !queryList.isEmpty()) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return new ShareProsperityEntity(playerId, termId);
	}

	@Override
	public boolean isActivityClose(String playerId) {
		return !checkActivityOpen(playerId);
	}

	/**
	 * 判断活动是否开启
	 * 
	 * @param playerId
	 * @return
	 */
	private boolean checkActivityOpen(String playerId) {
		// 判断本服的开服时间
		long serverOpenTime = this.getDataGeter().getServerOpenTime(playerId);
		if (serverOpenTime < ShareProsperityKVCfg.getInstance().getOpenTimeLimitValue()) {
			HawkLog.debugPrintln("ShareProsperityActivity checkActivityOpen serverOpenTime break, playerId: {}", playerId);
			return false;
		}

		Optional<ShareProsperityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		ShareProsperityEntity entity = opEntity.get();
		// 已经触发了
		if (entity.getStartTime() > 0) {
			if (entity.getEndTime() > HawkTime.getMillisecond()) {
				return true;
			}
			HawkLog.debugPrintln("ShareProsperityActivity checkActivityOpen lastTime end break, playerId: {}", playerId);
			return false;
		}

		// 本服角色基地等级条件
		if (this.getDataGeter().getConstructionFactoryLevel(playerId) < ShareProsperityKVCfg.getInstance().getOpenLimit()) {
			HawkLog.debugPrintln("ShareProsperityActivity checkActivityOpen cityLevel break, playerId: {}", playerId);
			return false;
		}

		// 新服创建角色后，在新服持续时间内x天内可触发该活动
		long now = HawkTime.getMillisecond();
		long bornTime = getDataGeter().getPlayerCreateTime(playerId);
		int limitDay = ShareProsperityKVCfg.getInstance().getOpenDay();
		if (now - bornTime > limitDay * HawkTime.DAY_MILLI_SECONDS) {
			HawkLog.debugPrintln("ShareProsperityActivity checkActivityOpen openDay end break, playerId: {}", playerId);
			return false;
		}
		
		int specialServerOpen = ShareProsperityKVCfg.getInstance().getSpecialServerOpen();
		//0=都开,1=专服开，2=非专服开
		if (specialServerOpen > 0) {
			boolean specialServer = this.isProprietary();
			if ((!specialServer && specialServerOpen == 1) || (specialServer && specialServerOpen > 1)) {
				HawkLog.debugPrintln("ShareProsperityActivity specialServerOpen break, playerId: {}", playerId);
				return false;
			}
		}

		// 判断 intervalTime（一个账号下各个角色触发该活动有时间间隔限制）
		String openid = this.getDataGeter().getOpenId(playerId);
		String redisKey = ACCOUNT_ACTIVITY_KEY + ":" + openid;
		String accountInfoStr = ActivityGlobalRedis.getInstance().getRedisSession().getString(redisKey);
		if (!HawkOSOperator.isEmptyString(accountInfoStr)) {
			ActivityAccountInfo accountInfo = JSONObject.parseObject(accountInfoStr, ActivityAccountInfo.class);
			if (now - accountInfo.getTime() < ShareProsperityKVCfg.getInstance().getIntervalTime()) {
				HawkLog.debugPrintln("ShareProsperityActivity checkActivityOpen intervalTime check break, playerId: {}", playerId);
				return false;
			}
		}

		boolean exist = false;
		for (AccountRoleInfo roleInfo : getAccountRoleInfo(entity).values()) {
			if (roleInfo.getPlayerId().equals(playerId)) {
				continue;
			}
			if (checkOldServerRole(roleInfo, bornTime, now) == 0) {
				exist = true;
				break;
			}
		}

		// 不存在满足条件的老号
		if (!exist) {
			HawkLog.debugPrintln("ShareProsperityActivity checkActivityOpen oldserver role check break, playerId: {}", playerId);
			return false;
		}

		entity.setStartTime(now);
		activityOpenPlayerMap.put(playerId, entity.getEndTime());
		
		// 记录一个账号触发该活动的时间信息
		String serverId = this.getDataGeter().getServerId();
		ActivityAccountInfo accountInfo = new ActivityAccountInfo(playerId, serverId, HawkTime.getMillisecond());
		ActivityGlobalRedis.getInstance().getRedisSession().setString(redisKey, JSONObject.toJSONString(accountInfo));
		HawkLog.logPrintln("ShareProsperityActivity checkActivityOpen success, playerId: {}", playerId);
		return true;
	}

	/**
	 * 判断老服角色是否满足条件
	 * 
	 * @param roleInfo
	 * @param currBornTime
	 * @param now
	 * @return
	 */
	private int checkOldServerRole(AccountRoleInfo roleInfo, long currBornTime, long now) {
		//判断老号基地等级
		if (roleInfo.getCityLevel() < ShareProsperityKVCfg.getInstance().getBaseLimit()) {
			HawkLog.debugPrintln("ShareProsperityActivity checkOldServerRole cityLevel error, oldServerPlayerId: {}", roleInfo.getPlayerId());
			return Status.Error.SHARE_PROSP_ROLE_INVALID_VALUE;
		}
		
		//判断老号注册时间
		if (currBornTime - roleInfo.getRegisterTime() <= ShareProsperityKVCfg.getInstance().getTimeLimit() * HawkTime.DAY_MILLI_SECONDS) {
			HawkLog.debugPrintln("ShareProsperityActivity checkOldServerRole registerTime error, oldServerPlayerId: {}", roleInfo.getPlayerId());
			return Status.Error.SHARE_PROSP_ROLE_INVALID_VALUE;
		}
		
		String serverId = roleInfo.getServerId();
		ServerInfo serverInfo = this.getServerInfo(serverId);
		long openTime = HawkTime.parseTime(serverInfo.getOpenTime());
		//判断老号所在服的开服时间
		if (now - openTime < ShareProsperityKVCfg.getInstance().getOldOrNewServer() * HawkTime.DAY_MILLI_SECONDS) {
			HawkLog.debugPrintln("ShareProsperityActivity checkOldServerRole serverOpenTime error, oldServerPlayerId: {}", roleInfo.getPlayerId());
			return Status.Error.SHARE_PROSP_ROLE_INVALID_VALUE;
		}
		
		return 0;
	}

	
	public void onTick() {
		List<String> playerList = new ArrayList<>();
		long now = HawkApp.getInstance().getCurrentTime();
		for (Entry<String, Long> entry : activityOpenPlayerMap.entrySet()) {
			try {
				String playerId = entry.getKey();
				if (!this.getDataGeter().isOnlinePlayer(playerId)) {
					continue;
				}
				//同步活动移除信息
				if (entry.getValue() < now) {
					playerList.add(playerId);
					syncActivityStateInfo(playerId);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		for(String playerId : playerList) {
			activityOpenPlayerMap.remove(playerId);
		}
	}
	
	/**
	 * 登录事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			if (activityOpenPlayerMap.containsKey(playerId)) {
				activityOpenPlayerMap.remove(playerId);
				syncActivityStateInfo(playerId);
			}
			return;
		}
		Optional<ShareProsperityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ShareProsperityEntity entity = opEntity.get();
		syncActivityInfo(entity);
		activityOpenPlayerMap.put(playerId, entity.getEndTime());
	}
	
	/**
	 * 大本升级事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onBuildingLevelUpEvent(BuildingLevelUpEvent event) {
		if (event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE) {
			return;
		}

		String playerId = event.getPlayerId();
		Optional<ShareProsperityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ShareProsperityEntity entity = opEntity.get();
		if (entity.getStartTime() > 0) {
			return;
		}
		if (checkActivityOpen(playerId)) {
			HawkLog.logPrintln("ShareProsperityActivity BuildingLevelUpEvent touch success, playerId: {}", playerId);
			syncActivityInfo(entity);
			syncActivityStateInfo(playerId);
		}
	}

	/**
	 * 充值事件 
	 * @param event
	 */
	@Subscribe
	public void onRechargeEvent(DiamondRechargeEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<ShareProsperityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ShareProsperityEntity entity = opEntity.get();
		if (HawkOSOperator.isEmptyString(entity.getBindOldPlayer())) {
			return;
		}
		
		int rebateCount = entity.getRebateCount();
		if (rebateCount >= ShareProsperityKVCfg.getInstance().getRewardLimitCount()) {
			HawkLog.logPrintln("ShareProsperityActivity DiamondRechargeEvent comin break, playerId: {}, rebeteTotal: {}", playerId, rebateCount);
			return;
		}
		
		boolean specialServer = this.isProprietary();
		int rebateRatio = ShareProsperityKVCfg.getInstance().getRebateRatio(specialServer);
		int count = (int) (event.getDiamondNum() * 1D * rebateRatio / 10000);
		long remain = ShareProsperityKVCfg.getInstance().getRewardLimitCount() - rebateCount;
		int realAdd = (int) Math.min(count, remain);
		entity.setRebateCount(rebateCount + realAdd);
		RewardItem.Builder builder = RewardHelper.toRewardItem(10000, 1000, realAdd);
		
		Object[] mailContent = new Object[5];
		mailContent[0] = this.getDataGeter().getServerId(); //新服区服
		mailContent[1] = this.getDataGeter().getPlayerName(playerId); //新服角色
		mailContent[2] = event.getDiamondNum(); //充值金条数量
		mailContent[3] = realAdd;  //返利数值
		mailContent[4] = builder.getItemId();   //返利货币
		sendMailToPlayer(entity.getBindOldPlayer(), MailId.SHARE_PROSP_OLDSVR_REWARD, null, null, mailContent, Arrays.asList(builder), false);
				
		syncActivityInfo(entity);
		
		HawkLog.logPrintln("ShareProsperityActivity DiamondRechargeEvent comin, playerId: {}, rechargeDiamond: {}, rebeteCount: {}, realAdd: {}, rebeteTotal: {}", 
				playerId, event.getDiamondNum(), count, realAdd, entity.getRebateCount());
	}
	
	@Subscribe
	public void onRechargeEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		String payId = event.getGiftId();
		boolean specialServer = this.isProprietary();
		ShareProsperityRewardCfg rewardCfg = ShareProsperityRewardCfg.getConfig(payId, specialServer);
		if (rewardCfg == null) {
			return;
		}
		
		Optional<ShareProsperityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ShareProsperityEntity entity = opEntity.get();
		if (HawkOSOperator.isEmptyString(entity.getBindOldPlayer())) {
			return;
		}
		
		int rebateCount = entity.getRebateCount();
		if (rebateCount >= ShareProsperityKVCfg.getInstance().getRewardLimitCount()) {
			HawkLog.logPrintln("ShareProsperityActivity PayGiftBuyEvent comin break, playerId: {}, rebeteTotal: {}", playerId, rebateCount);
			return;
		}
		RewardItem.Builder builder = RewardHelper.toRewardItem(rewardCfg.getReward());
		long count = builder.getItemCount();
		long remain = ShareProsperityKVCfg.getInstance().getRewardLimitCount() - rebateCount;
		int realAdd = (int) Math.min(count, remain);
		entity.setRebateCount(rebateCount + realAdd);
		builder.setItemCount(realAdd);
		
		Object[] mailContent = new Object[5];
		mailContent[0] = this.getDataGeter().getServerId(); //新服区服
		mailContent[1] = this.getDataGeter().getPlayerName(playerId); //新服角色
		mailContent[2] = event.getDiamondNum(); //充值金条数量
		mailContent[3] = realAdd;  //返利数值
		mailContent[4] = builder.getItemId();   //返利货币
		sendMailToPlayer(entity.getBindOldPlayer(), MailId.SHARE_PROSP_OLDSVR_REWARD, null, null, mailContent, Arrays.asList(builder), false);
				
		syncActivityInfo(entity);
		
		HawkLog.logPrintln("ShareProsperityActivity PayGiftBuyEvent comin, playerId: {}, rechargeDiamond: {}, rebeteCount: {}, realAdd: {}, rebeteTotal: {}", 
				playerId, event.getDiamondNum(), count, realAdd, entity.getRebateCount());
	}
	

	/**
	 * 绑定老服角色
	 * 
	 * @param playerId
	 * @param oldServerPlayerId
	 * @return
	 */
	public Result<?> bind(String playerId, String oldServerPlayerId) {
		if (HawkOSOperator.isEmptyString(oldServerPlayerId)) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<ShareProsperityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		ShareProsperityEntity entity = opEntity.get();
		if (!HawkOSOperator.isEmptyString(entity.getBindOldPlayer())) {
			return Result.fail(Status.Error.SHARE_PROSP_BIND_ALREADY_VALUE);
		}

		AccountRoleInfo roleInfo = this.getRoleInfo(entity, oldServerPlayerId);
		if (roleInfo == null) {
			HawkLog.errPrintln("ShareProsperityActivity bind error, roleInfo empty, playerId: {}, oldServerPlayerId: {}", playerId, oldServerPlayerId);
			return Result.fail(Status.Error.SHARE_PROSP_ROLE_INVALID_VALUE);
		}

		long bornTime = this.getDataGeter().getPlayerCreateTime(playerId);
		int result = checkOldServerRole(roleInfo, bornTime, HawkTime.getMillisecond());
		if (result > 0) {
			HawkLog.errPrintln("ShareProsperityActivity bind error, condition not match, playerId: {}, oldServerPlayerId: {}", playerId, oldServerPlayerId);
			return Result.fail(result);
		}

		entity.setBindOldPlayer(oldServerPlayerId);
		syncActivityInfo(entity);
		
		HawkLog.logPrintln("ShareProsperityActivity bind success, playerId: {}, oldServerPlayerId: {}, oldServer: {}", playerId, oldServerPlayerId, roleInfo.getServerId());
		return Result.success();
	}

	/**
	 * 同步活动信息
	 */
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<ShareProsperityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ShareProsperityEntity entity = opEntity.get();
		syncActivityInfo(entity);
	}
	
	/**
	 * 同步活动信息
	 * @param entity
	 */
	private void syncActivityInfo(ShareProsperityEntity entity) {
		if (entity.getStartTime() <= 0) {
			return;
		}
		String playerId = entity.getPlayerId();
		ShareProperityActivityInfo.Builder info = ShareProperityActivityInfo.newBuilder();
		info.setRebateCount(entity.getRebateCount());
		info.setOpenTime(entity.getStartTime());
		info.setEndTime(entity.getEndTime());

		if (!HawkOSOperator.isEmptyString(entity.getBindOldPlayer())) {
			AccountRoleInfo roleInfo = getRoleInfo(entity, entity.getBindOldPlayer());
			info.setBindRole(buildRoleInfo(roleInfo));
		}

		long bornTime = this.getDataGeter().getPlayerCreateTime(playerId);
		long now = HawkApp.getInstance().getCurrentTime();
		Map<String, AccountRoleInfo> roleInfoMap = getAccountRoleInfo(entity);
		for (AccountRoleInfo roleInfo : roleInfoMap.values()) {
			if (roleInfo.getServerId().equals(this.getDataGeter().getServerId())) {
				continue;
			}
			if (checkOldServerRole(roleInfo, bornTime, now) > 0) {
				continue;
			}
			info.addRoleInfo(buildRoleInfo(roleInfo));
		}

		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.SHARE_PROS_ACTIVITY_INFO_S, info));
	}

	/**
	 * 构建角色信息
	 * @param roleInfo
	 * @return
	 */
	private ShareProperityRolePB.Builder buildRoleInfo(AccountRoleInfo roleInfo) {
		ShareProperityRolePB.Builder builder = ShareProperityRolePB.newBuilder();
		builder.setPlayerId(roleInfo.getPlayerId());
		builder.setPlayerName(roleInfo.getPlayerName());
		builder.setPlayerLevel(roleInfo.getPlayerLevel());
		builder.setCityLevel(roleInfo.getCityLevel());
		builder.setVipLevel(roleInfo.getVipLevel());
		builder.setServerId(roleInfo.getServerId());
		builder.setIcon(roleInfo.getIcon());
		if (!HawkOSOperator.isEmptyString(roleInfo.getPfIcon())) {
			builder.setPfIcon(roleInfo.getPfIcon());
		}
		return builder;
	}

	/**
	 * 获取角色信息
	 * @param entity
	 * @param playerId
	 * @return
	 */
	private AccountRoleInfo getRoleInfo(ShareProsperityEntity entity, String playerId) {
		return getAccountRoleInfo(entity).get(playerId);
	}

	/**
	 * 获取角色信息
	 * @param entity
	 * @return
	 */
	private Map<String, AccountRoleInfo> getAccountRoleInfo(ShareProsperityEntity entity) {
		if (HawkApp.getInstance().getCurrentTime() - entity.getRoleInfoRefreshTime() < 120000L) {
			return entity.getRoleInfoMap();
		}
		String openid = this.getDataGeter().getOpenId(entity.getPlayerId());
		List<AccountRoleInfo> roleInfos = getDataGeter().getAccountRoleList(openid);
		for (AccountRoleInfo roleInfo : roleInfos) {
			entity.getRoleInfoMap().put(roleInfo.getPlayerId(), roleInfo);
		}
		entity.setRoleInfoRefreshTime(HawkTime.getMillisecond());
		return entity.getRoleInfoMap();
	}

	/**
	 * 获取区服信息
	 * @param serverId
	 * @return
	 */
	private ServerInfo getServerInfo(String serverId) {
		if (serverInfoMap.containsKey(serverId)) {
			return serverInfoMap.get(serverId);
		}
		
		String value = ActivityGlobalRedis.getInstance().hget("server_list", serverId);
		if (!HawkOSOperator.isEmptyString(value)) {
			JSONObject json = JSON.parseObject(value);
			ServerInfo serverInfo = new ServerInfo();
			if (serverInfo.updateFromJson(json)) {
				serverInfoMap.put(serverId, serverInfo);
				return serverInfo;
			}
		}

		return null;
	}

	/**
	 * 判断是否是专服
	 * @return
	 */
	private boolean isProprietary(){
		try {
			String zhuanfu = ActivityGlobalRedis.getInstance().hget("PROPRIETARY_SERVER", getDataGeter().getServerId());
			if(!HawkOSOperator.isEmptyString(zhuanfu) && zhuanfu.equals("1")){
				return true;
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		return false;
	}
	
}
