package com.hawk.game.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hawk.db.HawkDBEntity;
import org.hawk.msg.HawkMsg;
import org.hawk.task.HawkFuture;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.helper.PlayerActivityData;
import com.hawk.activity.helper.PlayerActivityDataSerialize;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsApp;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.msg.ImmigratePlayerMsg;
import com.hawk.game.msg.MigrateOutPlayerMsg;
import com.hawk.game.msg.RemoveObjectMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerSerializer;
import com.hawk.game.protocol.Const.GuildAuthority;
import com.hawk.game.protocol.Player.PlayerStatus;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 玩家迁服
 * @author jm
 *
 */
public class MigrateService {
	private static MigrateService instance = new MigrateService();
	private static Logger logger = LoggerFactory.getLogger("Server");
	
	public static MigrateService getInstance() {
		return instance;
	}
	
	
	/**
	 * 检测玩家是否可迁出
	 * @param playerId
	 * @return
	 */
	public HawkTuple2<Boolean, Integer> checkImmigrate(String playerId, String targetServerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return new HawkTuple2<Boolean, Integer>(Boolean.FALSE, 1);
		}
		
		int auth = GuildService.getInstance().getPlayerGuildAuthority(playerId);
		//盟主不让迁
		if (auth == GuildAuthority.L5_VALUE) {
			return new HawkTuple2<Boolean, Integer>(Boolean.FALSE, 2);																																																																																																																																																																																																																											
		}
		
		
		
		return new HawkTuple2<Boolean, Integer>(Boolean.TRUE, 0);
	}
	
	/**
	 * 是否可以迁城
	 * 检测目标服是否有角色
	 * 检测当前服是否是盟主之类的
	 * @param playerId
	 * @param targetServerId
	 * @return
	 */
	public HawkTuple3<Integer, Player, ServerInfo> isCanMigrate(String playerId, String targetServerId) {
		
		//检测目标服务器是否可用
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(targetServerId);
		 if (serverInfo == null) {
			 return new HawkTuple3<Integer, Player, ServerInfo>(Status.SysError.SERVER_NOT_EXIST_VALUE, null, null);
		 }
		 boolean isValid = GameUtil.testServerWebService(serverInfo);
		 if (!isValid) {
			 return new HawkTuple3<Integer, Player, ServerInfo>(Status.SysError.SERVER_INVALID_VALUE, null, null);
		 }
		 
		//角色是否存在
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return new HawkTuple3<Integer, Player, ServerInfo>(Status.SysError.ACCOUNT_NOT_EXIST_VALUE, null, null); 
		}
		
		//目标服是否存在角色.
		AccountRoleInfo roleInfo = RedisProxy.getInstance().getAccountRole(targetServerId, player.getPlatform(), player.getOpenId());
		if (roleInfo != null) {
			return new HawkTuple3<Integer, Player, ServerInfo>(Status.SysError.PLAYER_EXIST_VALUE, null, null);
		}
		
		if (GuildService.getInstance().isGuildLeader(playerId)) {
			return new HawkTuple3<Integer, Player, ServerInfo>(Status.Error.MIGRATE_GUILD_LEADER_VALUE, null, null);
		}
		
		return new HawkTuple3<Integer, Player, ServerInfo>(Status.SysError.SUCCESS_OK_VALUE, player, serverInfo);
	}
	
	/**
	 * 迁入玩家
	 * @targetPlayer 目标玩家
	 * @param playerId
	 * @return
	 */
	public int immigrateInPlayer(String playerId, String targetServerId) {
		logger.info("start immigrateInPlayer playerId: {}, targetServerId: {}", playerId, targetServerId);
		//从缓存读取玩家json数据
		JSONObject dataJson = RedisProxy.getInstance().getPlayerData(playerId);
		if (dataJson == null) {
			logger.error("immigrate in player get json data failed, playerId: {}", playerId);
			return Status.Error.MIGRATE_NO_DATA_VALUE;
		}
		
		List<HawkDBEntity> allEntityList = new ArrayList<>();
		// playerdata数据反序列化
		JSONObject playerJson = dataJson.getJSONObject("playerData");		
		List<HawkDBEntity> playerDataList = new LinkedList<HawkDBEntity>();
		PlayerSerializer.unserializeHawkDBEntityList(playerJson, playerDataList);
		if (playerDataList.isEmpty()) {
			logger.error("immigrate in player unserialize data failed, playerId: {}", playerId);
			return Status.Error.MIGRATE_UNSERIAL_ERROR_VALUE;
		}		
		allEntityList.addAll(playerDataList);
				
		//活动数据
		JSONObject activityData = dataJson.getJSONObject("activityData");
		List<HawkDBEntity> activityList = new LinkedList<HawkDBEntity>();
		PlayerActivityData playerActivityData = PlayerActivityDataSerialize.unserializePlayerActivityData(activityData, activityList);
		if (playerActivityData == null) {
			logger.error("immigrate in player unserialize data failed, playerId: {}", playerId);
			return Status.Error.MIGRATE_UNSERIAL_ERROR_VALUE;
		}
		
		allEntityList.addAll(activityList);
		
		PlayerEntity playerEntity = null;
		PlayerBaseEntity playerBaseEntity = null;
		for (HawkDBEntity entity : playerDataList) {
			if (entity instanceof PlayerEntity) {
				playerEntity = (PlayerEntity)entity;
			} else if (entity instanceof PlayerBaseEntity) {
				playerBaseEntity = (PlayerBaseEntity)entity;
			}
		}
		
		if (playerEntity == null || playerBaseEntity == null) {
			logger.error("immigrate in player can not find PlayerEntity or PlayerBaseEntity, playerId: {}", playerId);
			return Status.Error.MIGRATE_UNSERIAL_ERROR_VALUE;
		}

		if (!HawkDBEntity.batchCreate(allEntityList)) {
			logger.error("immigrate in player create db entity sqls failed, playerId: {}", playerId);
			return Status.Error.MIGRATE_SAVE_DB_ERROR_VALUE;
		}

		logger.info("immigrate in player create db entity sqls success, playerId: {}", playerId);

		// 更新到内存中(参考GsApp中创建新账号的流程)
		boolean rlt = GlobalData.getInstance().tryOccupyOrUpdatePlayerName(playerEntity.getId(), playerEntity.getName());
		if (!rlt) {
			//加上区服ID和特殊字母
			playerEntity.setName(playerEntity.getName() + "#" + targetServerId);
			GlobalData.getInstance().tryOccupyOrUpdatePlayerName(playerEntity.getId(), playerEntity.getName());
		}
		
		GlobalData.getInstance().updateAccountInfo(playerEntity.getPuid(), playerEntity.getServerId(), playerId, 0, playerEntity.getName());
		
		AccountRoleInfo roleInfo = RedisProxy.getInstance().getAccountRole(playerEntity.getServerId(), playerEntity.getPlatform(), playerEntity.getOpenid());
		roleInfo.playerName(playerEntity.getName()).logoutTime(playerEntity.getLogoutTime()).serverId(playerEntity.getServerId());
		GlobalData.getInstance().addOrUpdateAccountRoleInfo(roleInfo);
				
		//添加最近登录
		RedisProxy.getInstance().updateRecentServer(playerEntity.getServerId(), playerEntity.getOpenid(), playerEntity.getPlatform());
		// 添加实时在线上报视角的本服注册人数
		GlobalData.getInstance().addRegister(playerEntity.getChannel(), 1);
			
		ImmigratePlayerMsg msg = ImmigratePlayerMsg.valueOf(playerId);
		HawkTaskManager.getInstance().postFutureMsg(ActivityService.getInstance().getXid(), msg);
		
		return Status.SysError.SUCCESS_OK_VALUE;		
	}
	
	/**
	 * 在本服迁出玩家数据(添加迁移记录, 删除数据库实体对象, 移除玩家城点, 删除玩家排行榜信息, 从playerdata缓存移除, 减总玩家数)
	 * 暂时没有移除内存中账号信息
	 * 先往各个模块抛出消息 再序列化数据
	 * 
	 * @param playerId
	 */
	public int migrateOutPlayer(Player player, String targetServerId) {		
		//锁住玩家
		player.lockPlayer();
		String playerId = player.getId();
		
		//清理好友 无需等待
		MigrateOutPlayerMsg msg = MigrateOutPlayerMsg.valueOf(player);
		HawkTaskManager.getInstance().postFutureMsg(RelationService.getInstance().getXid(), msg);
		
		//世界
		WorldTask worldTask = new WorldTask(GsConst.WorldTaskType.MIGRATE_OUT_PLAYER) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().mantualMoveCityProcessMarch(player);
				int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
				WorldPointService.getInstance().removeWorldPoint(playerPos[0], playerPos[1]);
				return false;
			}
		};
		
		WorldThreadScheduler.getInstance().postWorldTask(worldTask);
		worldTask.blockThread(1000l);
		
		
		//退出工会
		msg = MigrateOutPlayerMsg.valueOf(player);
		HawkTaskManager.getInstance().postFutureMsg(GuildService.getInstance().getXid(), msg);
		
		//活动相关
		msg = MigrateOutPlayerMsg.valueOf(player);
		HawkFuture<Boolean> activityFuture = HawkTaskManager.getInstance().postFutureMsg(ActivityService.getInstance().getXid(), msg);
		Boolean activiytResult = activityFuture.getResult(1000l);
		if (activiytResult == null || !activiytResult.booleanValue()) {
			logger.error("migrate out actiivty handle fail playerId:{}, resutle:{}", player.getId(), activiytResult);
			
			return Status.Error.MIGRATE_SERIAL_ERROR_VALUE;
		}
		
		
		// 记录跨服迁城记录
		List<HawkDBEntity> serializeEntities = new LinkedList<HawkDBEntity>();
		JSONObject palyerAndActivity = new JSONObject();
		JSONObject dataJson = PlayerSerializer.serializePlayerData(player.getData(), serializeEntities);
		if (dataJson == null) {
			logger.error("migrate out serial playerData fail playerId:{}", player.getId());
			return Status.Error.MIGRATE_SERIAL_ERROR_VALUE;
		}
		palyerAndActivity.put("playerData", dataJson);
		
		//序列化活动的数据
		PlayerActivityData activityData = PlayerDataHelper.getInstance().getPlayerData(playerId);
		if (activityData == null) {
			activityData = new PlayerActivityData(player.getId());
		}
		
		List<HawkDBEntity> entityList = new ArrayList<>();
		JSONObject activityJson = PlayerActivityDataSerialize.serializePlayerActivityData(activityData, entityList);
		palyerAndActivity.put("activityData", activityJson);
		
		//存入redis中
		RedisProxy.getInstance().updatePlayerData(playerId, palyerAndActivity);
		RedisProxy.getInstance().addImmigrateRecord(playerId, targetServerId, palyerAndActivity);
		// 日志记录
		logger.info("immigrate out player data success, playerId:{}", playerId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	/**
	 * a玩家从A 服 迁往B服，B服有角色b. 在玩家a迁入完成之后 b会受到此协议
	 * @param playerId
	 */
	public void immigrateFinish(Player player) {
		if (player == null) {
			return;
		}
		
		//玩家置为无效.
		player.getData().getPlayerBaseEntity().setInvalid(true);
		GameUtil.migrateOutPlayer(player.getId(), player.getName());
		
		//通知客户端
		player.synPlayerStatus(PlayerStatus.FINISH);
		// 通知管理器删除对象
		HawkMsg msg = RemoveObjectMsg.valueOf(HawkXID.valueOf(GsConst.ObjType.PLAYER, player.getId()));
		GsApp.getInstance().postMsg(GsApp.getInstance().getXid(), msg);
		// 从缓存中移除
		GlobalData.getInstance().uncachePlayerData(player.getId());
		
		player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "迁服完成");
	}
	
	/**
	 * 迁服务失败,尝试恢复数据
	 * @param player
	 */
	public void migrateOutError(Player player, int errorCode) {
		player.unLockPlayer(errorCode);
	}
	/**
	 *  迁移的玩家在整个过程结束之后调用该方法
	 * @param player 
	 */
	public void migrateOutFinish(Player player) {
		//解锁玩家
		player.unLockPlayer();		
		//player 置为无效无法登陆即可
		player.getData().getPlayerBaseEntity().setInvalid(true);;
		player.getData().getPlayerBaseEntity().notifyUpdate();
		
		//清除玩家名字
		GameUtil.migrateOutPlayer(player.getId(), player.getName());		
		// 从排行榜中清理
		LocalRedis.getInstance().deletePlayerRanks(player.getId());			
		
		GlobalData globalData = GlobalData.getInstance(); 
		//玩家在线就走一遍kickout 然后再删除数据 否则直接删除数据和管理对象
		if (globalData.isOnline(player.getId())) {
			player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "migrate finish");
		} else {
			// 通知管理器删除对象
			HawkMsg msg = RemoveObjectMsg.valueOf(HawkXID.valueOf(GsConst.ObjType.PLAYER, player.getId()));
			GsApp.getInstance().postMsg(GsApp.getInstance().getXid(), msg);			
			//删除没有工会的玩家
			globalData.removeNoGuildPlayer(player.getId());
			// 从缓存中移除
			GlobalData.getInstance().uncachePlayerData(player.getId());
		}		
	}
}
