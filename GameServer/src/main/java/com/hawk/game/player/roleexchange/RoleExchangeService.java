package com.hawk.game.player.roleexchange;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.callback.HawkCallback;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkCallbackTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.IDIPBanInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.RoleExchangeProtoCfg;
import com.hawk.game.data.PlatTransferInfo;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.RechargeDailyEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeState;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.log.LogConst.Platform;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 心悦角色交易服务类
 * 
 * @author lating
 */
public class RoleExchangeService {
	/**
	 * 单例
	 */
	private static RoleExchangeService instance = new RoleExchangeService();
	
	/**
	 * 获取单例
	 */
	public static RoleExchangeService getInstance() {
		return instance;
	}
	
	/**
	 * 获取心悦角色转移过程状态的rediskey（玩家登录控制，在转移处理过程中不让登录）
	 * @param openid
	 * @param playerId
	 * @return
	 */
	public String getRoleExchangeStateKey(String openid, String playerId) {
		return RedisKey.ROLE_EXCHANGE_DOING + ":" + openid + ":" + playerId;
	}
	
	/**
	 * 记录卖方玩家在心悦角色交易过程的各个阶段状态
	 * @return
	 */
	public String getRoleExchangeStatusKey(String openid, String playerId) {
		return RedisKey.ROLE_EXCHANGE_STATUS + ":" + openid + ":" + playerId;
	}
	
	/**
	 * 对同一个角色进行转移的各个openid存储
	 * @param playerId
	 * @return
	 */
	private String getRoleExchangeAccountKey(String playerId) {
		return RedisKey.ROLE_EXCHANGE_ACCOUNT + ":" + playerId;
	}
	
	/**
	 * 获取心悦角色交易-角色数据存储的rediskey
	 * @param playerId
	 * @return
	 */
	public String getRoleExchangeKey(String playerId) {
		String redisKey = RedisKey.ROLE_EXCHANGE_DATA + ":" + playerId;
		return redisKey;
	}
	
	
	/**
	 * 心悦角色交易回滚
	 * @param buyerPlayer
	 * @param sellerOpenid
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean roleExchangeRollback(Player buyerPlayer, String sellerOpenid) {
		String buyerOpenid = buyerPlayer.getOpenId();
		String playerId = buyerPlayer.getId();
		String serverId = buyerPlayer.getServerId();
		String platform = buyerPlayer.getPlatform();
		
		String redisKey = getRoleExchangeStateKey(buyerOpenid, playerId);
		RedisProxy.getInstance().getRedisSession().setString(redisKey, String.valueOf(XinyueRoleExchangeState.EXCHANGE_DOING), 180);
		// 此处要判断玩家是否在线，在线的话要先踢下线，然后抛到玩家线程中处理
		if (buyerPlayer.isActiveOnline()) {
			HawkLog.logPrintln("roleExchange rollback kickout buyer, sellerOpenid: {}, buyerOpenid: {}, roleid: {}", sellerOpenid, buyerOpenid, playerId);
			buyerPlayer.kickout(Status.IdipMsgCode.IDIP_EXCHANGE_ROLE_OFFLINE_VALUE, true, null);
		}
		
		//数据还原，此时seller和buyer其实指向的都是同一个player，等还原完数据后，再修改其openid和puid等信息
		
		//此时要判断卖方是否已经创建了新的角色，如果是需要将其先清除掉，然后再还原
		Player sellerPlayer = null;
		String puid = GameUtil.getPuidByPlatform(sellerOpenid, platform);
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);
		if (accountInfo != null) {
			sellerPlayer = GlobalData.getInstance().makesurePlayer(accountInfo.getPlayerId());
		}
		
		//卖方还未创建新角色
		if (sellerPlayer == null) {
			roleExchangeRollbackDirect(buyerPlayer, sellerOpenid);
			return true;
		}
		
		//卖方已创建角色
		String redisKey1 = getRoleExchangeStateKey(sellerOpenid, sellerPlayer.getId());
		RedisProxy.getInstance().getRedisSession().setString(redisKey1, String.valueOf(XinyueRoleExchangeState.EXCHANGE_DOING), 180);
		if (sellerPlayer.isActiveOnline()) {
			HawkLog.logPrintln("roleExchange rollback kickout seller, sellerOpenid: {}, buyerOpenid: {}, roleid: {}, new sellerId: {}", sellerOpenid, buyerOpenid, playerId, sellerPlayer.getId());
			sellerPlayer.kickout(Status.IdipMsgCode.IDIP_EXCHANGE_ROLE_OFFLINE_VALUE, true, null);
		}
		
		//卖方已经创建了新角色的情况下，要先将新角色重置再进行回滚操作
		Player newSellerRole = sellerPlayer;
		HawkCallbackTask task = HawkCallbackTask.valueOf(new HawkTask() {
			@Override
			public Object run() {
				boolean restoreSucc = sellerDataUnserialize(buyerPlayer);
				HawkLog.logPrintln("roleExchange rollback restore end, sellerOpenid: {}, buyerOpenid: {}, playerId: {}, restoreSucc: {}", sellerOpenid, buyerOpenid, playerId, restoreSucc);
				if (!restoreSucc) {
					return null;
				}
				HawkLog.logPrintln("roleExchange rollback reset new seller, sellerOpenid: {}, buyerOpenid: {}, new sellerId: {}, old playerId: {}, serverId: {}", sellerOpenid, buyerOpenid, newSellerRole.getId(), playerId, serverId);
				GameUtil.resetAccount(newSellerRole, 0);
				return "success";
			}
		}, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				if (args == null) {
					HawkLog.errPrintln("roleExchange rollback seller unserialize failed, sellerOpenid: {}, buyerOpenid: {}, new roleid: {}, old roleid: {}", sellerOpenid, buyerOpenid, newSellerRole.getId(), playerId);
					return -1;
				}
				HawkLog.logPrintln("roleExchange rollback reset new seller finish, sellerOpenid: {}, buyerOpenid: {}, new roleid: {}, old roleid: {}, serverId: {}", sellerOpenid, buyerOpenid, newSellerRole.getId(), playerId, serverId);
				roleExchangeRollbackSafe(buyerPlayer, sellerOpenid, newSellerRole);
				return 0;
			}
		});
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		int threadIdx = Math.abs(sellerPlayer.getXid().hashCode() % threadPool.getThreadNum());
		task.setTypeName("roleexchange-rollback");
		task.setTaskOwner(sellerPlayer.getXid().hashCode());
		threadPool.addTask(task, threadIdx, false);
		return true;
	}
	
	/**
	 * 卖方玩家还没有创建新角色的情况下，直接回滚
	 * @param buyerPlayer
	 * @param sellerOpenid
	 */
	private void roleExchangeRollbackDirect(Player buyerPlayer, String sellerOpenid) {
		String buyerOpenid = buyerPlayer.getOpenId();
		String playerId = buyerPlayer.getId();
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					boolean restoreSucc = sellerDataUnserialize(buyerPlayer);
					HawkLog.logPrintln("roleExchange rollback restore end, sellerOpenid: {}, buyerOpenid: {}, playerId: {}, restoreSucc: {}", sellerOpenid, buyerOpenid, playerId, restoreSucc);
					if (restoreSucc) {
						roleExchangeRollbackSafe(buyerPlayer, sellerOpenid, null);
					}
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("roleexchange-rollback-direct");
			int threadIndex = Math.abs(buyerPlayer.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
			return;
		}
		
		boolean restoreSucc = sellerDataUnserialize(buyerPlayer);
		HawkLog.logPrintln("roleExchange rollback restore end, sellerOpenid: {}, buyerOpenid: {}, playerId: {}, restoreSucc: {}", sellerOpenid, buyerOpenid, playerId, restoreSucc);
		if (restoreSucc) {
			roleExchangeRollbackSafe(buyerPlayer, sellerOpenid, null);
		}
	}
	
	/**
	 * 心悦角色交易回滚
	 * @param buyerPlayer
	 * @param sellerOpenid
	 * @param sellerPlayer
	 */
	@SuppressWarnings("deprecation")
	private void roleExchangeRollbackSafe(Player buyerPlayer, String sellerOpenid, Player sellerPlayer) {
		String buyerOpenid = buyerPlayer.getOpenId();
		String serverId = buyerPlayer.getServerId();
		String playerId = buyerPlayer.getId();
		String platform = buyerPlayer.getPlatform();
		
		HawkLog.logPrintln("roleExchange rollback start, sellerOpenid: {}, buyerOpenid: {}, roleId: {}, platform: {}", sellerOpenid, buyerOpenid, playerId, platform);
		//recentServer信息处理
		RedisProxy.getInstance().updateRecentServer(serverId, sellerOpenid, platform);
		RedisProxy.getInstance().deleRecentServer(serverId, buyerOpenid, platform);
		
		//accountRoleInfo信息处理
		AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
		accountRoleInfo.setOpenId(sellerOpenid);
		RedisProxy.getInstance().removeAccountRole(buyerOpenid, serverId, platform);
		RedisProxy.getInstance().addAccountRole(accountRoleInfo);
		
		//内存数据accountInfo处理
		GlobalData.getInstance().removeAccountInfoOnly(playerId);
		String newPuid = GameUtil.getPuidByPlatform(sellerOpenid, platform);
		GlobalData.getInstance().updateAccountInfo(newPuid, serverId, playerId, 0, buyerPlayer.getName());
		
		//db数据处理
		buyerPlayer.getEntity().setOpenid(sellerOpenid);
		buyerPlayer.getEntity().setPuid(newPuid);
		
		//充值相关数据还原
		rechargeRollback(sellerOpenid, playerId);
		
		//擦除关于卖方和买方信息的数据记录
		RedisProxy.getInstance().getRedisSession().hDel(RedisKey.ROLE_EXCHANGE_RECORD, sellerOpenid + ":" + playerId);
		RedisProxy.getInstance().getRedisSession().lRem(getRoleExchangeAccountKey(playerId), 1, sellerOpenid);
		String newRoleId = sellerPlayer == null ? "" : sellerPlayer.getId();
		HawkLog.logPrintln("roleExchange rollback success, sellerOpenid: {}, buyerOpenid: {}, roleId: {}, new roleId: {}", sellerOpenid, buyerOpenid, playerId, newRoleId);
	}
	
	/**
	 * 充值数据还原
	 * @param sellerOpenid
	 * @param playerId
	 */
	private void rechargeRollback(String sellerOpenid, String playerId) {
		List<RechargeInfo> rechargeInfoBackList = RedisProxy.getInstance().getAllRechargeInfoBack(sellerOpenid, playerId);
		String key = RedisKey.RECHARGE_INFO + ":" + sellerOpenid;
		int index = 0, totalCount = rechargeInfoBackList.size();
		while (index < totalCount) {
			int count = 0;
			// 这里通过pipline批量操作，减少redis的QPS压力
			try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
				for (;index < totalCount; index++) {
					RechargeInfo recharge = rechargeInfoBackList.get(index);
					if (recharge.getType() == 0) {
						pip.hset(RedisKey.ROLE_RECHARGE_TOTAL + ":" + sellerOpenid, playerId, String.valueOf(recharge.getCount()));
					} else {
						pip.lpush(key, JSONObject.toJSONString(recharge));
					}
					if (++count > 200) {
						break;
					}
				}
				pip.sync();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		//删除备份数据
		String key1 = RedisKey.RECHARGE_INFO_BACK + ":" + sellerOpenid + ":" + playerId;
		RedisProxy.getInstance().getRedisSession().del(key1);
	}
	
	/**
	 * 心悦角色交易--角色转换
	 * @param sellerPlayer
	 * @param buyerOpenid
	 */
	public int roleExchange(Player sellerPlayer, String buyerOpenid) {
		int resultCode = roleExchangeConditionCheck(sellerPlayer, buyerOpenid);
		if (resultCode != XinyueRoleExchangeFailReason.ERROR_0_SUCCESS) {
			return resultCode;
		}
		
		String sellerOpenid = sellerPlayer.getOpenId();
		String playerId = sellerPlayer.getId();
		String redisKey = getRoleExchangeStateKey(sellerOpenid, playerId);
		RedisProxy.getInstance().getRedisSession().setString(redisKey, String.valueOf(XinyueRoleExchangeState.EXCHANGE_DOING), 180);
		
		//卖方玩家在线情况下，要先踢下线再操作
		if (sellerPlayer.isActiveOnline()) {
			HawkLog.logPrintln("roleExchange kickout seller, sellerOpenid: {}, buyerOpenid: {}, roleid: {}", sellerOpenid, buyerOpenid, playerId);
			sellerPlayer.kickout(Status.IdipMsgCode.IDIP_EXCHANGE_ROLE_OFFLINE_VALUE, true, null);
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		HawkCallbackTask task = HawkCallbackTask.valueOf(new HawkTask() {
			@Override
			public Object run() {
				HawkLog.logPrintln("roleExchange seller serialize start, sellerOpenid: {}, buyerOpenid: {}, roleid: {}", sellerOpenid, buyerOpenid, playerId);
				//卖方数据备份
				boolean flushSucc = sellerDataSerialize(sellerPlayer.getData().getDataCache());
				if (!flushSucc) {
					checkResult.set(1);
					RedisProxy.getInstance().getRedisSession().del(redisKey);
					return null;
				}
				checkResult.set(0);
				return "success";
			}
		}, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				if (args == null) {
					HawkLog.errPrintln("roleExchange seller serialize failed, sellerOpenid: {}, buyerOpenid: {}, roleid: {}", sellerOpenid, buyerOpenid, playerId);
					return -1;
				}
				HawkLog.logPrintln("roleExchange seller serialize succ, sellerOpenid: {}, buyerOpenid: {}, roleid: {}", sellerOpenid, buyerOpenid, playerId); 
				roleExchangeSafe(sellerPlayer, buyerOpenid);
				return 0;
			}
		});
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		int threadIdx = Math.abs(sellerPlayer.getXid().hashCode() % threadPool.getThreadNum());
		task.setTypeName("roleexchange");
		task.setTaskOwner(sellerPlayer.getXid().hashCode());
		threadPool.addTask(task, threadIdx, false);
		
		int count = 0;
		//等待数据备份结果
		do {
			HawkOSOperator.osSleep(50L);
			if (checkResult.get() > 0) {
				return XinyueRoleExchangeFailReason.ERROR_1015; //数据备份异常
			}
			count++;
		} while (checkResult.get() < 0 && count <= 10); // 如果等了一定时长还没有结束，那就不等了，就当是备份成功了
		
		return 0;
	}
	
	/**
	 * 角色转移
	 * @param sellerPlayer
	 * @param buyerOpenid
	 */
	@SuppressWarnings("deprecation")
	private void roleExchangeSafe(Player sellerPlayer, String buyerOpenid) {
		String serverId = sellerPlayer.getServerId();
		String sellerOpenid = sellerPlayer.getOpenId();
		String playerId = sellerPlayer.getId();
		String platform = sellerPlayer.getPlatform();
		
		HawkLog.logPrintln("roleExchange start, sellerOpenid: {}, buyerOpenid: {}, roleId: {}, platform: {}", sellerOpenid, buyerOpenid, playerId, platform);
		
		//卖方+买方recentServer信息处理
		RedisProxy.getInstance().updateRecentServer(serverId, buyerOpenid, platform);
		RedisProxy.getInstance().deleRecentServer(serverId, sellerOpenid, platform);
		
		//卖方+买方accountRoleInfo信息处理
		AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
		accountRoleInfo.setOpenId(buyerOpenid);
		RedisProxy.getInstance().removeAccountRole(sellerOpenid, serverId, platform);
		RedisProxy.getInstance().addAccountRole(accountRoleInfo);
		
		//内存数据accountInfo处理
		GlobalData.getInstance().removeAccountInfoOnly(playerId);
		String newPuid = GameUtil.getPuidByPlatform(buyerOpenid, platform);
		GlobalData.getInstance().updateAccountInfo(newPuid, serverId, playerId, 0, sellerPlayer.getName());
		
		//卖方db数据处理：TODO 需要确认是否还有其它字段信息待修改，比如平台头像信息（依赖openid或puid获取信息存储的，都需要过一遍）
		sellerPlayer.getEntity().setOpenid(buyerOpenid);
		sellerPlayer.getEntity().setPuid(newPuid);
		
		//卖方充值数据相关处理：rechargeEntity 和 redis中的rechargeInfo数据要清空
		backRechargeData(sellerPlayer, sellerOpenid);
		
		String redisKey = getRoleExchangeStatusKey(sellerOpenid, playerId);
		RedisProxy.getInstance().getRedisSession().del(redisKey);
		
		//记录下一条关于卖方和买方信息的数据记录
		String currServerId = GsConfig.getInstance().getServerId();
		RoleExchangeInfo info = new RoleExchangeInfo(playerId, sellerOpenid, buyerOpenid, serverId, currServerId, sellerPlayer.getChannel(), platform, HawkTime.getMillisecond());
		//记录角色转换具体信息
		RedisProxy.getInstance().getRedisSession().hSet(RedisKey.ROLE_EXCHANGE_RECORD, sellerOpenid + ":" + playerId, JSONObject.toJSONString(info));
		//记录当前这个角色经过哪些玩家账号转换
		RedisProxy.getInstance().getRedisSession().lPush(getRoleExchangeAccountKey(playerId), 0, sellerOpenid);
		
		HawkLog.logPrintln("roleExchange success, sellerOpenid: {}, buyerOpenid: {}, roleId: {}", sellerOpenid, buyerOpenid, playerId);
	}
	
	/**
	 * 获取不可交易原因码列表（当该角色不可交易时）原因码需要与需求中⼀⼀对应
	 * @return
	 */
	public List<Integer> getRoleExchangeFailCodeList(Player player) {
		List<Integer> codeList = new ArrayList<>();
		sellerRoleLaunchCheck(player, true, codeList);
		checkRoleExchangeSelf(player, true, codeList);
		return codeList;
	}
	
	/**
	 * 条件判断
	 * 
	 * 卖方条件：1.卖方账号上金条数量 <【100】; 2.开服时间大于【180】天; 3.非福利账号; 4.距离上次出售在30天以上
	 * 买方条件：1.在本服没有角色
	 * 
	 * @param sellerPlayer
	 * @param buyerOpenid
	 * @return
	 */
	public int roleExchangeConditionCheck(Player sellerPlayer, String buyerOpenid) {
		int retCode = sellerRoleLaunchCheck(sellerPlayer);
		if (retCode > 0) {
			return retCode;
		}
		
		retCode = checkRoleExchangeSelf(sellerPlayer, false, null);
		if (retCode > 0) {
			return retCode;
		} 

		if (!HawkOSOperator.isEmptyString(buyerOpenid)) {
			retCode = buyerCheck(buyerOpenid, false, null);
			if (retCode > 0) {
				return retCode;
			}
		}
		
		return XinyueRoleExchangeFailReason.ERROR_0_SUCCESS;
	}
	
	private int checkRoleExchangeSelf(Player sellerPlayer, boolean checkAll, List<Integer> codeList) {
		if (codeList == null) {
			codeList = new ArrayList<>();
		}
		String playerId = sellerPlayer.getId();
		String sellerOpenid = sellerPlayer.getOpenId();
		long now = HawkTime.getMillisecond();
		List<String> openidList = RedisProxy.getInstance().getRedisSession().lRange(getRoleExchangeAccountKey(playerId), 0, -1, 0);
		for (String openid : openidList) {
			if (codeList.contains(XinyueRoleExchangeFailReason.ERROR_1009)) {
				break;
			}
			String value = RedisProxy.getInstance().getRedisSession().hGet(RedisKey.ROLE_EXCHANGE_RECORD, openid + ":" + playerId);
			if (HawkOSOperator.isEmptyString(value)) {
				continue;
			}
			RoleExchangeInfo record = JSONObject.parseObject(value, RoleExchangeInfo.class);
			if (now - record.getExchangeTime() > HawkTime.DAY_MILLI_SECONDS * 30) {
				continue;
			}
			
			HawkLog.errPrintln("roleExchange check exchange time break, sellerOpenid: {}, roleId: {}, last sellerOpenid: {}, time: {}", sellerOpenid, playerId, record.getSellerOpenid(), record.getExchangeTime());
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1009; //角色距离上次交易未满30天
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1009);
		}
		
		String redisKey = getRoleExchangeStatusKey(sellerOpenid, playerId);
		String status = RedisProxy.getInstance().getRedisSession().getString(redisKey);
		int state = HawkOSOperator.isEmptyString(status) ? 0 : Integer.parseInt(status);
		if (state == XinyueRoleExchangeState.EXCHANGE_NOTICE) {
			HawkLog.errPrintln("roleExchange check exchange state break, sellerOpenid: {}, roleId: {}", sellerOpenid, playerId);
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1023;  //该角色正处于公示期，当前阶段无法购买
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1023);
		}
		
		return 0;
	}
	
	/**
	 * 充值数据备份处理
	 * @param sellerPlayer
	 * @param openid
	 */
	@SuppressWarnings("deprecation")
	private void backRechargeData(Player sellerPlayer, String sellerOpenid) {
		String buyerOpenid = sellerPlayer.getOpenId();
		HawkLog.logPrintln("roleExchange back recharge db data, playerId: {}, sellerOpenid: {}, buyerOpenid: {}", sellerPlayer.getId(), sellerOpenid, buyerOpenid);
		
		List<RechargeEntity> rechargeEntities = sellerPlayer.getData().getPlayerRechargeEntities();
		rechargeEntities.forEach(e -> e.delete(true));
		rechargeEntities.clear();
		
		List<RechargeDailyEntity> rechargeDailyEntities = sellerPlayer.getData().getPlayerRechargeDailyEntities();
		rechargeDailyEntities.forEach(e -> e.delete(true));
		rechargeDailyEntities.clear();
		
		sellerPlayer.getPlayerBaseEntity().setSaveAmt(0);
		sellerPlayer.getPlayerBaseEntity().setSaveAmtTotal(0);
		sellerPlayer.getPlayerBaseEntity()._setChargeAmt(0);
		sellerPlayer.getPlayerBaseEntity().setRechargeTotal(0);
		
		HawkLog.logPrintln("roleExchange back recharge redis data, playerId: {}, sellerOpenid: {}, buyerOpenid: {}", sellerPlayer.getId(), sellerOpenid, buyerOpenid);
		List<RechargeInfo> rechargeInfos = RedisProxy.getInstance().getAllRechargeInfoByOpenid(sellerOpenid);
		
		String tarServer = sellerPlayer.getServerId();
		String playerId = sellerPlayer.getId();
		String sourceServer = GlobalData.getInstance().getImmgrationSource(playerId, tarServer);
		String platform = PlatTransferInfo.getSourcePlatform(playerId, sellerPlayer.getPlatform());
		List<RechargeInfo> rechargeInfoBackList = new ArrayList<>();
		for(RechargeInfo info : rechargeInfos){
			// rechargeInfo中的playerId字段是在2024年3月份才添加的，之前没有playerId信息只能通过serverId+platform来匹配，后面就可以直接通过playerId匹配了
			if (!HawkOSOperator.isEmptyString(info.getPlayerId()) && info.getPlayerId().equals(playerId)) {
				rechargeInfoBackList.add(info);
				continue;
			}
			
			String rechargePlatform = Platform.valueOf(info.getPlatId()).strLowerCase();
			if (!rechargePlatform.equals(platform)) {
				continue;
			}
			
			String rechargeServer = info.getServer();
			if (rechargeServer.equals(tarServer) || rechargeServer.equals(sourceServer)) {
				rechargeInfoBackList.add(info);
			}
		}
		
		//从原来的数据中清除，同时添加到备份数据中去 
		String key1 = RedisKey.RECHARGE_INFO + ":" + sellerOpenid;
		String key2 = RedisKey.RECHARGE_INFO_BACK + ":" + sellerOpenid + ":" + playerId;
		int index = 0, totalCount = rechargeInfoBackList.size();
		while (index < totalCount) {
			int count = 0;
			// 这里通过pipline批量操作，减少redis的QPS压力
			try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
				for (;index < totalCount; index++) {
					RechargeInfo recharge = rechargeInfoBackList.get(index);
					String info = JSONObject.toJSONString(recharge);
					pip.lrem(key1, 1, info);
					pip.lpush(key2, info);
					if (++count > 200) {
						break;
					}
				}
				pip.sync();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		//设置过期时间
		RedisProxy.getInstance().getRedisSession().expire(key2, GsConst.DAY_SECONDS * 90);
		
		//角色充值总额roleRechargeTotal数据也要清除，清除前先备份
		String roleRechargeTotal = RedisProxy.getInstance().getRedisSession().hGet(RedisKey.ROLE_RECHARGE_TOTAL + ":" + sellerOpenid, playerId);
		if (!HawkOSOperator.isEmptyString(roleRechargeTotal)) {
			RechargeInfo rechargeInfo = new RechargeInfo(sellerOpenid, playerId, 0, tarServer, HawkTime.getSeconds(), Integer.parseInt(roleRechargeTotal), 0);
			RedisProxy.getInstance().addRechargeInfoBack(playerId, rechargeInfo);
			RedisProxy.getInstance().getRedisSession().hDel(RedisKey.ROLE_RECHARGE_TOTAL + ":" + sellerOpenid, playerId);
		}
		
		HawkLog.logPrintln("roleExchange back recharge data finish, playerId: {}, sellerOpenid: {}, buyerOpenid: {}", sellerPlayer.getId(), sellerOpenid, buyerOpenid);
	}
	
	/**
	 * 状态切换
	 * @param player
	 * @param nextStatus
	 */
	public void roleExchangeStateSwitch(Player player, int nextStatus) {
		try {
			String redisKey = getRoleExchangeStatusKey(player.getOpenId(), player.getId());
			RedisProxy.getInstance().getRedisSession().setString(redisKey, String.valueOf(nextStatus));
			if (player.isActiveOnline()) {
				int offlineReason = 0;
				if (nextStatus == XinyueRoleExchangeState.EXCHANGE_NOTICE) {
					offlineReason = Status.IdipMsgCode.IDIP_EXCHANGE_NOTICE_OFFLINE_VALUE;
				} else if (nextStatus == XinyueRoleExchangeState.EXCHANGE_LAUNCH) {
					offlineReason = Status.IdipMsgCode.IDIP_EXCHANGE_LAUNCH_OFFLINE_VALUE;
				} else if(nextStatus == XinyueRoleExchangeState.EXCHANGE_INSPECTION_FAILED) {
					offlineReason = Status.IdipMsgCode.IDIP_INSPECTION_FAILED_OFFLINE_VALUE;
				}
				player.setRoleExchangeState(nextStatus);
				// 玩家在线的话，将玩家踢下线
				if (offlineReason > 0) {
					player.kickout(offlineReason, true, null);
				}
			}
			
			//进入考察期，开5天的保护罩
			if (nextStatus == XinyueRoleExchangeState.EXCHANGE_INSPECTION 
					|| nextStatus == XinyueRoleExchangeState.EXCHANGE_NOTICE
					|| nextStatus == XinyueRoleExchangeState.EXCHANGE_LAUNCH) {
				addProtectBuff(player);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 进入特定时期，加保护罩
	 * @param player
	 */
	private void addProtectBuff(Player player) {
		long newShieldTime = HawkTime.getMillisecond() + HawkTime.DAY_MILLI_SECONDS * 5;
		long oldShieldTime = player.getData().getCityShieldTime();
		if (newShieldTime > oldShieldTime) {
			StatusDataEntity addStatusBuff = player.getData().addStatusBuff(EffType.CITY_SHIELD_VALUE, newShieldTime);
			if (addStatusBuff != null) {
				WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), addStatusBuff.getEndTime());
				player.getPush().syncPlayerStatusInfo(false, addStatusBuff);
			}
		}
	}
	
	/**
	 * 角色上架判断
	 * @param player
	 * @return
	 */
	public int sellerRoleLaunchCheck(Player player) {
		return sellerRoleLaunchCheck(player, false, null);
	}
	
	/**
	 * 角色上架判断
	 * @param player
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public int sellerRoleLaunchCheck(Player player, boolean checkAll, List<Integer> codeList) {
		String playerServerId = player.getServerId();
		long serverOpenTime = GlobalData.getInstance().getServerOpenTime(playerServerId);
		if (codeList == null) {
			codeList = new ArrayList<>();
		}
		
		if (HawkTime.getMillisecond() - serverOpenTime < HawkTime.DAY_MILLI_SECONDS * 30) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1008; //角色所处区服开服时间未达到30天
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1008);
		}
		
		if (player.getDiamonds() >= 100) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1007; //角色所拥有的金条数过多
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1007);
		}
				
		if (player.getCityLevel() < 15) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1004; //角色建筑工厂等级未达到15级，不能上架
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1004);
		}
		if (player.hasGuild()) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1013; //角色有联盟，不能上架
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1013);
		}
		if (RelationService.getInstance().hasGuarder(player.getId())) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1014; //角色有守护，不能上架
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1014);
		}
		
		if (GlobalData.getInstance().isPlayerInAccountCancelState(player.getId())) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1019; //卖家处于注销状态，无法进行角色交易， 不能上架
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1019);
		}
		
		if (player.isCsPlayer() || player.isInDungeonMap()) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1010; //角色正处于航海远征等跨服玩法或泰伯利亚之战、赛博之战、联合军演等副本、跨服玩法中。
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1010);
		}
		
		String passwd = RedisProxy.getInstance().readSecPasswd(player.getId());
		if (!HawkOSOperator.isEmptyString(passwd)) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1016; //有二级密码，不能上架
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1016);
		}
		
		Map<String, String> roleInfoMap = RedisProxy.getInstance().getAccountRole(player.getOpenId());
		for (String value : roleInfoMap.values()) {
			AccountRoleInfo roleInfo = JSONObject.parseObject(value, AccountRoleInfo.class);
			if (roleInfo.getPlayerId().equals(player.getId())) {
				continue;
			}
			
			if (!codeList.contains(XinyueRoleExchangeFailReason.ERROR_1001)) {
				String key = getRoleExchangeStatusKey(player.getOpenId(), roleInfo.getPlayerId());
				String roleStatus = RedisProxy.getInstance().getRedisSession().getString(key);
				int roleStatusVal = HawkOSOperator.isEmptyString(roleStatus) ? 0 : Integer.parseInt(roleStatus);
				if (roleStatusVal == XinyueRoleExchangeState.EXCHANGE_LAUNCH) {
					if (!checkAll) {
						return XinyueRoleExchangeFailReason.ERROR_1001; //角色所属账号下已有其他角色上架，不能上架
					}
					codeList.add(XinyueRoleExchangeFailReason.ERROR_1001);
					continue;
				}
			}
			
			if (!codeList.contains(XinyueRoleExchangeFailReason.ERROR_1011)) {
				String info = RedisProxy.getInstance().getRedisSession().hGet(RedisKey.ROLE_EXCHANGE_RECORD, player.getOpenId() + ":" + roleInfo.getPlayerId());
				if (HawkOSOperator.isEmptyString(info)) {
					continue;
				}
				RoleExchangeInfo record = JSONObject.parseObject(value, RoleExchangeInfo.class);
				if (HawkTime.getMillisecond() - record.getExchangeTime() > HawkTime.DAY_MILLI_SECONDS * 30) {
					continue;
				}
				
				if (!checkAll) {
					return XinyueRoleExchangeFailReason.ERROR_1011; //角色所属账号30天内已有其他角色交易成功
				}
				codeList.add(XinyueRoleExchangeFailReason.ERROR_1011);
			}
		}
		
		return 0;
	}
	
	/**
	 * 买方账号判断
	 * @param buyerOpenid
	 * @return
	 */
	public int buyerCheck(String buyerOpenid, boolean checkAll, List<Integer> codeList) {
		if (codeList == null) {
			codeList = new ArrayList<>();
		}
		
		String serverId = GsConfig.getInstance().getServerId();
		Map<String, String> roleInfoMap = RedisProxy.getInstance().getAccountRole(buyerOpenid);
		for (String value : roleInfoMap.values()) {
			AccountRoleInfo roleInfo = JSONObject.parseObject(value, AccountRoleInfo.class);
			if (!codeList.contains(XinyueRoleExchangeFailReason.ERROR_1018) && 
					GlobalData.getInstance().isPlayerInAccountCancelState(roleInfo.getPlayerId())) {
				if (!checkAll) {
					return XinyueRoleExchangeFailReason.ERROR_1018; //买家在该区服也有就角色，且处于注销状态，无法购买
				}
				codeList.add(XinyueRoleExchangeFailReason.ERROR_1018);
			}
			
			if (!codeList.contains(XinyueRoleExchangeFailReason.ERROR_1017) && 
					serverId.equals(GlobalData.getInstance().getMainServerId(roleInfo.getServerId()))) {
				if (!checkAll) {
					return XinyueRoleExchangeFailReason.ERROR_1017; // 买家在该区服已有角色，无法购买
				}
				codeList.add(XinyueRoleExchangeFailReason.ERROR_1017);
			}
		}
		
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(buyerOpenid, IDIPBanType.AREA_BAN_ACCOUNT);
		if (banInfo != null && banInfo.getEndTime() > HawkTime.getMillisecond()) {
			if (!checkAll) {
				return XinyueRoleExchangeFailReason.ERROR_1022; //买家处于封禁状态，不能交易
			}
			codeList.add(XinyueRoleExchangeFailReason.ERROR_1022);
		}
	
		return 0;
	}
	
	/**
	 * 获取角色摘要信息
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public JSONObject getXinyuePlayerAbstractInfo(Player player) {
		String serverId = player.getServerId();
		String platform = player.getPlatform();
		JSONObject obj = new JSONObject();
		obj.put("channelID", player.getChannel());  //string：账号渠道类型（qq: qq渠道, wx: 微信渠道）
		obj.put("platformID", platform.substring(0, 3)); //string：平台渠道类型（ios: ios平台, and: 安卓平台, ia: 安卓IOS互通(这里的互通指的是同⼀个角色能在安卓和IOS上都能登录。而不是安卓和IOS能在⼀起玩那种)）
		obj.put("partitionID", serverId);      //string：区服ID
		obj.put("roleID", player.getId());       //string：角色ID
		obj.put("playerName", player.getName()); //string：玩家名称
		obj.put("itemName", "");      //string：该角色作为物品是的名称(将用于站内信，短信，支付物品名称中)
		
		//心悦那边说这里不需要返回错误码
//		List<Integer> codeList = getRoleExchangeFailCodeList(player);
//		if (codeList.isEmpty()) {
//			obj.put("isTradable", true);
//		} else {
//			obj.put("isTradable", false);  //bool：该角色是否可交易
//			obj.put("cwList", codeList.toArray(new Integer[codeList.size()])); //int数组： 当该角色不可交易时，不可交易原因码列表, 原因码需要与需求中⼀⼀对应
//		}
		
		JSONObject json = new JSONObject();
		json.put("platform", platform);
		json.put("areaId", GsConfig.getInstance().getAreaId());
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(serverId);
		json.put("bornServerId", serverId);
		json.put("bornServerName", serverInfo.getName());
		serverInfo = RedisProxy.getInstance().getServerInfo(GlobalData.getInstance().getMainServerId(serverId));
		json.put("mainServerId", serverInfo.getId());
		json.put("mainServerName", serverInfo.getName());
		json.put("cityLevel", player.getCityLevel());
		json.put("commanderLevel", player.getLevel());
		json.put("vipLevel", player.getVipLevel());
		json.put("totalPower", player.getPower());
		json.put("excludeArmyPower", player.getNoArmyPower());
		fetchMainSoldierType(player, json);
		obj.put("gameSummary", json);
		return obj;
	}
	
	/**
	 * 取主力兵种信息
	 * 
	 * 1）取最高阶的泰能战士。若有多个尉官5阶的，则都为主力兵种。
	 * 2）若有相同阶的，或者无泰能战士，则取11级及以上兵，数量最多的兵种。
	 * 3）若无11级及以上兵种，则主力兵种为空
	 * 
	 * @param player
	 * @param json
	 */
	private void fetchMainSoldierType(Player player, JSONObject json) {
		List<Integer> armyIds = new ArrayList<>();
		int strengthLevel = 1;
		PlantSoldierSchool school = player.getData().getPlantSoldierSchoolEntity().getPlantSchoolObj();
		for (SoldierStrengthen crack : school.getStrengthens()) {
			int level = crack.getPlantStrengthLevel();
			if (level == strengthLevel) {
				armyIds.add(crack.getCfgId());
			} else if (level > strengthLevel) {
				strengthLevel = level;
				armyIds.clear();
				armyIds.add(crack.getCfgId());
			}
		}
		
		JSONArray array = new JSONArray();
		json.put("mainSoldierId", array);
		if (armyIds.isEmpty()) { //无泰能兵
			int armyCount = 0, selectArmyId = 0;
			for (ArmyEntity entity : player.getData().getArmyEntities()) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
				//无泰能战士，则取11级及以上兵，数量最多的兵种
				if (cfg == null || cfg.getLevel() < 11) {
					continue;
				}
				int count = entity.getTotal() - entity.getWoundedCount() - entity.getCureCount();
				if (count > armyCount) {
					armyCount = count;
					selectArmyId = entity.getArmyId();
				}
			}
			if (selectArmyId > 0) {
				array.add(selectArmyId);
			}
		} else if (strengthLevel < 5) { //没有5阶泰能兵
			int armyCount = 0, selectArmyId = armyIds.get(0);
			for (int armyId : armyIds) {
				ArmyEntity entity = player.getData().getArmyEntity(armyId);
				if (entity == null) {
					continue;
				}
				int count = entity.getTotal() - entity.getWoundedCount() - entity.getCureCount();
				if (count > armyCount) {
					armyCount = count;
					selectArmyId = armyId;
				}
			}
			array.add(selectArmyId);
		} else {
			array.addAll(armyIds);
		}
	}
	
	/**
	 * 心悦角色交易交易期限制检测
	 * @param protocol
	 * @return
	 */
	public boolean checkProtocol(HawkProtocol protocol, Player player) {
		if (player.getRoleExchangeState() != XinyueRoleExchangeState.EXCHANGE_INSPECTION) {
			return true;
		}
		
		//指定协议不让操作
		RoleExchangeProtoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RoleExchangeProtoCfg.class, protocol.getType());
		if (cfg != null && cfg.getBan() != 0) {
			player.sendError(protocol.getType(), Status.SysError.ROLE_EXCHANGE_FORBIDDEN_REQ_VALUE, 0);
			return false;
		}
		
		return true;
	}

	/**
	 * 心悦角色交易限制期：角色不能使用资源（含金币、金条这些资源）
	 * @param player
	 * @return
	 */
	public int consumeCheck(Player player) {
		if (player.getRoleExchangeState() == XinyueRoleExchangeState.EXCHANGE_INSPECTION) {
			return Status.Error.ROLE_EXCHANGE_CONSUME_FORBIDDEN_VALUE;
		}
		return 0;
	}
	
	/**
	 * 登录检测
	 * @param openid
	 * @param playerId
	 * @return
	 */
	public boolean loginCheck(String openid, String playerId) {
		String redisKey = getRoleExchangeStatusKey(openid, playerId);
		String status = RedisProxy.getInstance().getRedisSession().getString(redisKey);
		int statusVal = HawkOSOperator.isEmptyString(status) ? 0 : Integer.parseInt(status);
		//心悦角色交易限制检测
		if (statusVal == XinyueRoleExchangeState.EXCHANGE_NOTICE 
				|| statusVal == XinyueRoleExchangeState.EXCHANGE_LAUNCH 
				|| statusVal == XinyueRoleExchangeState.EXCHANGE_INSPECTION_FAILED) {
			HawkLog.errPrintln("player is forbidden login by role exchange status, playerId: {}, openid: {}, status: {}", playerId, openid, statusVal);
			return false;
		}
		
		redisKey = getRoleExchangeStateKey(openid, playerId);
		String exchangeDoingState = RedisProxy.getInstance().getRedisSession().getString(redisKey);
		if (!HawkOSOperator.isEmptyString(exchangeDoingState)) {
			HawkLog.errPrintln("player is forbidden login by role exchange doing, playerId: {}, openid: {}, status: {}", playerId, openid, statusVal);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 更新服务器状态
	 * @param state
	 */
	public void updateServerState(int state) {
		String serverId = GsConfig.getInstance().getServerId();
		List<String> serverList = AssembleDataManager.getInstance().getMergedServerList(serverId);
		if (serverList == null || serverList.isEmpty()) {
			RedisProxy.getInstance().getRedisSession().setString(RedisKey.SERVER_STATE + ":" + serverId, String.valueOf(state));
		} else {
			for (String svrid : serverList) {
				RedisProxy.getInstance().getRedisSession().setString(RedisKey.SERVER_STATE + ":" + svrid, String.valueOf(state));
			}
		}
	}
	
	/**
	 * 刷新状态
	 * @param player
	 */
	public void refreshStatus(Player player) {
		String redisKey = getRoleExchangeStatusKey(player.getOpenId(), player.getId());
		String status = RedisProxy.getInstance().getRedisSession().getString(redisKey);
		if (!HawkOSOperator.isEmptyString(status)) {
			player.setRoleExchangeState(Integer.parseInt(status));
		}
	}
	
	/**
	 * 卖方数据序列化
	 * @param dataCache
	 * @return
	 */
	public boolean sellerDataSerialize(PlayerDataCache dataCache) {
		long startTime = HawkTime.getMillisecond();
		try {
			for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
				if(!serialize(dataCache, key)) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			RedisProxy.getInstance().getRedisSession().expire(getRoleExchangeKey(dataCache.getPlayerId()), GsConst.DAY_SECONDS * 60); //60天过期
			HawkLog.logPrintln("sellerPlayer data serialize to redis finish, playerId: {}, costtime: {}", dataCache.getPlayerId(), HawkTime.getMillisecond() - startTime);
		}		
		
		return false;
	}
	
	/**
	 * 数据序列化
	 * @param dataCache
	 * @param key
	 * @return
	 */
	private boolean serialize(PlayerDataCache dataCache, PlayerDataKey key) {
		boolean flushFlag = dataCache.removeEntityFlag(key);
		if (!flushFlag || dataCache.isLockKey(key)) {
			return true;
		}
		
		//每次写一个key拿一次jedis pipeline 主要是怕一次异常导致全跪.
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pipeline = jedis.pipelined();){
			byte[] bytes = PlayerDataSerializer.serializeData(key, dataCache.makesureDate(key));
			String fieldKey = key.name();
			if (bytes != null) {
				pipeline.hset(getRoleExchangeKey(dataCache.getPlayerId()).getBytes(), fieldKey.getBytes(), bytes);
			} else {
				pipeline.hdel(getRoleExchangeKey(dataCache.getPlayerId()), fieldKey);
			}
			pipeline.sync();
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		HawkLog.errPrintln("seller player data flush to redis loop failed, playerId: {}, dataKey: {}", dataCache.getPlayerId(), key.name());
		return false;
	}
	
	/**
	 * 卖方数据反序列化
	 * @param player
	 * @return
	 */
	public boolean sellerDataUnserialize(Player player) {
		long startTime = HawkTime.getMillisecond();
		synchronized (player.getSyncObj()) {
			for (PlayerDataKey dataKey : EnumSet.allOf(PlayerDataKey.class)) {
				unserialize(dataKey, player);
			}
		}
		HawkLog.logPrintln("sellerPlayer data unserialize from redis finish, playerId: {}, costtime: {}", player.getId(), HawkTime.getMillisecond() - startTime);
		return true;
	}
	
	/**
	 * 数据反序列化
	 * @param dataKey
	 * @param player
	 */
	private void unserialize(PlayerDataKey dataKey, Player player) {
		try {
			// 从redis读取数据
			byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes(getRoleExchangeKey(player.getId()), dataKey.name());
			
			// 反序列化
			Object data = PlayerDataSerializer.unserializeData(dataKey, bytes, false);
			if (data == null) {
				HawkLog.errPrintln("sellerPlayer data unserialize error, playerId: {}, dataKey: {}", player.getId(), dataKey.name());
				return;
			}
			
			// 数据合并处理
			boolean mergeSucc = PlayerDataSerializer.mergePlayerData(player.getData().getDataCache(), dataKey, data, false);
			if (!mergeSucc) {
				HawkLog.errPrintln("sellerPlayer data unserialize failed, playerId: {}, dataKey: {}", player.getId(), dataKey.name());
			}
			
			//player身上挂了一个cityLevel.
			if (dataKey == PlayerDataKey.BuildingEntities) {
				player.setCityLevel(0);
			}
		} catch (Exception e) {
			HawkLog.errPrintln("sellerPlayer data unserialize exception, playerId: {}, dataKey: {}", player.getId(), dataKey.name());
			HawkException.catchException(e);				
		}
	}
	
}
