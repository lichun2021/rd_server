package com.hawk.game.playercopy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.service.SysOpService;

/**
 * 玩家复制服务类
 * 
 * lating
 */
public class PlayerCopyService {
    /**
     * db会话
     */
	private PlayerCopySqlSession sqlSession;
	/**
	 * 数据库名字
	 */
	private String dbName;
	/**
	 * 玩家ID到openid,puid的映射
	 */
	private Map<String, HawkTuple2<String, String>> playerOpenidMap = new HashMap<>(); 
	/**
	 * 本次起服后已经复制过的玩家
	 */
	private List<String> alreadySelectPlayer;
	
	/**
	 * 是否初始化
	 */
	boolean initOK = false;
	
	/**
	 * 单例
	 */
	private static PlayerCopyService instance = null;
	
	/**
	 * 获取单例
	 */
	public static PlayerCopyService getInstance() {
		if (instance == null) {
			instance = new PlayerCopyService();
		}
		return instance;
	}
	
	/**
	 * 初始化
	 */
	public boolean init() {
		if (initOK) {
			return true;
		}
		
		List<String> motherPlayerIds = GameConstCfg.getInstance().getMotherPlayerIdList();
		List<String>  sonPlayerIds = SysOpService.getInstance().getSonPlayerIdList();
		List<String>  removePlayerIds = GameConstCfg.getInstance().getRemovePlayerIdList();
		// 如果没有配置，就不开启服务
		if (motherPlayerIds.isEmpty() && sonPlayerIds.isEmpty() && removePlayerIds.isEmpty()) {
			return false;
		}
		
		// 线上环境，如果没有配置本服的号，则不执行初始化逻辑
		if (!GsConfig.getInstance().isDebug()) {
			int playerCount = 0;
			for (String playerId : motherPlayerIds) {
				if (GlobalData.getInstance().isExistPlayerId(playerId)) {
					playerCount++;
					break;
				}
			}
			
			if (playerCount == 0) {
				HawkLog.errPrintln("PlayerSelectService init fail, config no player");
				return false;
			}
		}
		
		try {
			String dbConnUrl = GsConfig.getInstance().getDbConnUrl(), dbUsername = GsConfig.getInstance().getDbUserName(), dbPassword = GsConfig.getInstance().getDbPassWord();
			String dbUrl = String.format("%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true", dbConnUrl);
			if (GsConfig.getInstance().isDebug()) {
				dbUrl = String.format("%s&useSSL=false", dbUrl);
			}
			sqlSession = new PlayerCopySqlSession();
			boolean rlt = sqlSession.init(dbUrl, dbUsername, dbPassword);
			if (!rlt) {
				HawkLog.errPrintln("PlayerSelectService init db connection fail, dbUrl: {}", dbUrl);
				return false;
			}
			
			rlt = sqlSession.getConnection().isValid();
			if(!rlt) {
				HawkLog.errPrintln("PlayerSelectService init db connection fail, connection invalid, dbUrl: {}", dbUrl);
				return false;
			}
			
			alreadySelectPlayer = new ArrayList<String>();
			dbName = dbConnUrl.substring(dbConnUrl.lastIndexOf("/") + 1, dbConnUrl.length());
			initOK = true;
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 拉取玩家信息
	 */
	public void selectMotherPlayer() {
		List<String> motherPlayerIds = GameConstCfg.getInstance().getMotherPlayerIdList();
		if (motherPlayerIds.isEmpty()) {
			close();  // 用完了就关闭服务，释放资源；保证不影响线上环境
			return;
		}
		
		// 如果没有配置本服的号，就不往下走了
		int playerCount = 0;
		for (String playerId : motherPlayerIds) {
			if (GlobalData.getInstance().isExistPlayerId(playerId)) {
				playerCount++;
				break;
			}
		}
		
		if (playerCount == 0) {
			return;
		} 
		
		Thread thread = new Thread() {
			public void run() {
				selectMontherPlayerAsync();
			}
		};
		thread.setName("playercopy-select-motherdata");
		thread.start();
	} 
	
	/**
	 * 异步执行拉取玩家信息的逻辑
	 */
	private void selectMontherPlayerAsync() {
		List<String> motherPlayerIds = GameConstCfg.getInstance().getMotherPlayerIdList();
		try {
			for (String playerId : motherPlayerIds) {
				if (!GlobalData.getInstance().isExistPlayerId(playerId)) {
					HawkLog.logPrintln("select playerdata not local player, playerId: {}", playerId);
					continue;
				}
				
				// 本次起服后已经拉取过数据的号，就不再拉取了
				if (alreadySelectPlayer.contains(playerId)) {
					HawkLog.logPrintln("select playerdata repeated, playerId: {}", playerId);
					continue;
				}
				
				selectMotherPlayerDBData(playerId, false, null, null);
				alreadySelectPlayer.add(playerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			close(); // 用完了就关闭服务，释放资源；保证不影响线上环境
		}
	}
	
	/**
	 * 拉取玩家数据
	 * 
	 * @param playerId
	 * @param playerCopy true表示是在复制账号数据的过程中执行的，false表示单纯拉取玩家数据
	 */
	private void selectMotherPlayerDBData(String playerId, boolean playerCopy, List<String> specialKeyNameTable, List<String> specialKeyIndexTable) {
		List<String> tableNames = sqlSession.selectTableList(dbName, specialKeyNameTable, specialKeyIndexTable);
		for (String tableName : tableNames) {
			boolean playerTable = tableName.equals("player");
			PlayerCopyRunnable task = new PlayerCopyRunnable(sqlSession, tableName, playerId, playerTable, playerCopy);
			try {
				HawkTuple2<String, String> tuple = task.execute();
				if (tuple != null) {
					// 记录下这个信息，有可能在需要拉取redis数据的情况下用到
					playerOpenidMap.put(playerId, tuple);
				}
			} catch (Exception e) {
				HawkException.catchException(e, playerId);
			}
		}
	}
	
	/**
	 * 玩家复制: 复制玩家需要在起服的时候复制！！！
	 * 
	 * <p> 因为需要将现有玩家的数据删了，然后将目标角色的数据插入进去，防止内存数据影响，所以要在起服的时候去复制  <p>
	 */
	public List<String> copyPlayer() {
		// 线上环境，不支持玩家复制
		if (!GsConfig.getInstance().isDebug()) {
			return Collections.emptyList();
		}
		
		// 考虑到内存数据的影响，起服完成后不让复制
		if (GsApp.getInstance().isInitOK()) {
			return Collections.emptyList();
		}
		
		List<String> sonPlayerIds = SysOpService.getInstance().getSonPlayerIdList();
		// 没有配置玩家信息，就不用复制了
		if (sonPlayerIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		String fileName = "tmp/Copy.log";
		List<String> sqls = new ArrayList<>();
		try {
			HawkOSOperator.readTextFileLines(fileName, sqls);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (sqls.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<String> copySuccPlayerIds = new ArrayList<>();
		String motherPlayerId = GameConstCfg.getInstance().getMotherPlayer();
		String redisKey = "copyPlayer:" + GsConfig.getInstance().getServerId();
		for (String sonPlayerId : sonPlayerIds) {
			// 已经复制过的玩家，不要重复复制
			String time = RedisProxy.getInstance().getRedisSession().hGet(redisKey, sonPlayerId);
			if (!HawkOSOperator.isEmptyString(time)) {
				HawkLog.logPrintln("copy player already, sonPlayerId: {}", sonPlayerId);
				continue;
			}
			
			// 对 Copy.cfg中的数据进行判断，如果同一个号存在多份数据，或是没有数据，那么 db中 sonPlayerId 原有的数据不删除
			int count = 0;
			String sonPlayerIdString = "'" + sonPlayerId + "'";
			for (String sql : sqls) {
				if (sql.indexOf(sonPlayerIdString) < 0) {
					continue;
				}
				
				if (sql.indexOf("insert into player(") >= 0) {
					count++;
					if (count > 1) {
						break;
					}
				}
			}
			
			if (count != 1) {
				HawkLog.logPrintln("copy player failed, sonPlayerId: {}, data count: {}", sonPlayerId, count);
				continue;
			}
			
			List<String> specialKeyNameTable = new ArrayList<String>();
			List<String> specialKeyIndexTable = new ArrayList<String>();
			selectMotherPlayerDBData(sonPlayerId, true, specialKeyNameTable, specialKeyIndexTable);  // 这一步主要是将sonPlayer当前的db数据清除掉，后面用新的数据插入
			// 本服不存在该玩家
			if (!GameConstCfg.getInstance().isPlayerCopySkip() && !playerOpenidMap.containsKey(sonPlayerId)) {
				HawkLog.logPrintln("copy player not local player, sonPlayerId: {}", sonPlayerId);
				continue;
			}
			
			if (!specialKeyIndexTable.isEmpty()) {
				HawkLog.errPrintln("copy player contains special key index table, {}", specialKeyIndexTable);
				continue;
			}
			
			boolean success = copyPlayerData(sonPlayerId, sqls, specialKeyNameTable, specialKeyIndexTable);
			if (success) {
				copySuccPlayerIds.add(sonPlayerId);
				HawkLog.logPrintln("copy player success, sonPlayerId: {}", sonPlayerId);
				// 记下一个时间标识，表示sonPlayerId对应的这个玩家已经复制过了
				RedisProxy.getInstance().getRedisSession().hSet(redisKey, sonPlayerId, String.valueOf(HawkTime.getMillisecond()));
				if (!HawkOSOperator.isEmptyString(motherPlayerId)) {
					SysOpService.getInstance().copyPlayerRedis(motherPlayerId, sonPlayerId);
				}
			}
		}
		
		return copySuccPlayerIds;
	}
	
	/**
	 * 给复制成功的子号做一些后续处理
	 * @param playerIds
	 */
	public void copySuccPlayerRefresh(List<String> playerIds) {
		if (playerIds.isEmpty()) {
			return;
		}
		
		for (String playerId : playerIds) {
			GsApp.getInstance().addDelayAction(60000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					Player sonPlayer = GlobalData.getInstance().makesurePlayer(playerId);
					if (sonPlayer == null) {
						HawkLog.logPrintln("player copy service remove city failed, playerId: {}", playerId);
						return;
					}
					
					AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
					accountRoleInfo.cityLevel(sonPlayer.getCityLevel()).playerLevel(sonPlayer.getLevel());
					RedisProxy.getInstance().addAccountRole(accountRoleInfo);
					
					CityManager.getInstance().moveCity(playerId, true);
					
					SysOpService.getInstance().armysCheckAndFix(sonPlayer);
					HawkLog.logPrintln("player copy service remove city, playerId: {}", playerId);
				}
			});
		}
	}
	
	/**
	 * 删除玩家数据
	 */
	public void removePlayerData() {
		// 线上环境，不支持玩家复制
		if (!GsConfig.getInstance().isDebug()) {
			return;
		}
		
		// 考虑到内存数据的影响，起服完成后不让复制
		if (GsApp.getInstance().isInitOK()) {
			return;
		}
		
		List<String> removePlayerIds = GameConstCfg.getInstance().getRemovePlayerIdList();
		if (removePlayerIds.isEmpty()) {
			return;
		}
		
		for (String playerId : removePlayerIds) {
			List<String> specialKeyNameTable = new ArrayList<String>();
			List<String> specialKeyIndexTable = new ArrayList<String>();
			selectMotherPlayerDBData(playerId, true, specialKeyNameTable, specialKeyIndexTable);  // 这一步主要是将sonPlayer当前的db数据清除掉，后面用新的数据插入
			HawkLog.logPrintln("remove playerdata on server start, playerId: {}", playerId);
		}
	}
	
	/**
	 * 将文件中targetPlayerId的玩家数据复制到 sonPlayerId对应的玩家身上
	 * 
	 * @param sonPlayerId
	 */
	private boolean copyPlayerData(String sonPlayerId, List<String> sqls, List<String> specialKeyNameTable, List<String> specialKeyIndexTable) {
		String sonPlayerIdString = "" + sonPlayerId + "";
		for (String sql : sqls) {
			if (HawkOSOperator.isEmptyString(sql.trim())) {
				continue;
			}
			
			if (sql.indexOf(sonPlayerIdString) < 0) {
				continue;
			}
			
			String subString = "insert into ";
			sql = sql.substring(sql.indexOf(subString));
			int startIndex = subString.length(), endIndex = sql.indexOf("(");
			String tableName = sql.substring(startIndex, endIndex);
			if (!tableName.equals("player") && !specialKeyNameTable.contains(tableName)) {
				startIndex = sql.indexOf("values('") + 8;
				endIndex = sql.indexOf("',");
				String oldUuid = sql.substring(startIndex, endIndex);
				String uuid = HawkOSOperator.randomUUID();
				sql = sql.replace(oldUuid, uuid);
			}
			
			PreparedStatement pstmt = null;
			try {
				pstmt = sqlSession.getConnection().prepareStatement(sql);
				pstmt.executeUpdate();
			} catch (SQLException sqlException) {
				HawkException.catchException(sqlException, sql);
			} finally {
				try {
					if (pstmt != null) {
						pstmt.close();
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}	
			}
		}
		
		return true;
	}
	
	/**
	 * 用完了就关闭服务，释放资源，保证不影响线上环境； 测试环境下不关闭，保证在线也能复制玩家数据
	 */
	private void close() {
		// 测试环境下不关闭，保证在线也能拉取玩家数据
		if (GsConfig.getInstance().isDebug()) {
			return;
		}
		
		shutdown();
	}
	
	/**
	 * 停服时一定要关闭
	 */
	public void shutdown() {
		if (!initOK) {
			return;
		}
		
		try {
			if (sqlSession != null) {
				sqlSession.close();
				sqlSession = null;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 拉取玩家的redis数据
	 * 
	 * @param playerId
	 */
	protected void selectPlayerRedisData(String playerId) {
		// RedisProxy.PUID_PROFILE_KEY
		String key = "puid_profile:" + playerOpenidMap.get(playerId).second;
		String puidProfile = RedisProxy.getInstance().getRedisSession().getString(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "string", key, puidProfile);
		
		// RedisProxy.ACCOUNT_ROLE_KEY
		key = "account_role:" + playerOpenidMap.get(playerId).first;
		Map<String, String> accountRoleMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "map", key, accountRoleMap);
		
		// RedisProxy.PUID_PFICON_KEY
		key = "puid_pfIcon:" + playerOpenidMap.get(playerId).second;
		String puidPfIcon = RedisProxy.getInstance().getRedisSession().getString(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "string", key, puidPfIcon);
		
		// RedisProxy.OVERLAY_MISSION_KEY
		key = "overlay_mission:" + playerId;
		Map<String, String> overlayMissionMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "map", key, overlayMissionMap);
		
		// RedisProxy.VIP_BOX_STATUS
		key = "vip_box_status:" + playerId;
		Map<String, String> vipBoxStatusMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "map", key, vipBoxStatusMap);
		
		// RedisProxy.UNRECEIVED_BENEFIT_BOX
		key = "unreceived_vip_box:" + playerId;
		List<String> unrecievedBenefitBoxList = RedisProxy.getInstance().getUnreceivedBenefitBox(playerId);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "list", key, unrecievedBenefitBoxList);
		
		// RedisProxy.VIP_SHOP_BOUGHT_TIMES
		key = "vip_shop_bought_times:" + playerId;
		Map<String, String> vipSopBoughtMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "map", key, vipSopBoughtMap);
		
		// RedisProxy.VIP_SHOP_REFRESH_TIME
		// RedisProxy.VIP_SHOP_IDS
		
		// RedisProxy.TOUCH_PUSH_GIFT_TIMES
		key = "touch_push_gift_times:" + playerId;
		Map<String, String> pushGiftMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "map", key, pushGiftMap);

		// RedisProxy.EQUIP_STAR_SHOW
		key = "equip_star_show:" + playerId;
		String equipStarShow = RedisProxy.getInstance().getRedisSession().getString(key);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, value: {}", playerId, "string", key, equipStarShow);
		
		// RedisProxy.PLAYER_PRESET_MARCH
		key = "world_preset_march";
		String value  = RedisProxy.getInstance().getRedisSession().hGet(key, playerId);
		HawkLog.logPrintln("selectPlayerRedisData, playerId: {}, type: {}, key: {}, field: {}, value: {}", playerId, "map-field", key, playerId, value);
	}
	
}
