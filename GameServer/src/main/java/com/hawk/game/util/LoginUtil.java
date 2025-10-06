package com.hawk.game.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.GrayPuidCtrl;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.LoginFlag;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPActiveDevice;
import com.hawk.game.protocol.Login.HPActiveDeviceRet;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Login.HPLoginRet;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.HPHeartBeat;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.GsConst.AuthCheckLevel;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.sdk.SDKConst;
import com.hawk.sdk.SDKManager;
import com.hawk.zoninesdk.ZonineSDK;
import com.hawk.zoninesdk.datamanager.OpDataType;

public class LoginUtil {
	/**
	 * 名字随机次数
	 */
	private static final int NAME_RANDOM_TIMES = 100;
	/**
	 * 失败的openid创建记录
	 */
	private static Set<String> createFailedOpenIds = new ConcurrentHashSet<String>();
	
	/**
	 * 校验服务器和puid信息
	 * 
	 * @param cmd
	 * @param session
	 * @return
	 */
	public static boolean checkLoginServerAndPuid(String serverId, String puid, HawkSession session) {
		// puid校验
		// 账号不存在, 只有机器人模式才进行自动创建
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);
		if (accountInfo == null && HawkOSOperator.isEmptyString(puid) && GsConfig.getInstance().isRobotMode()) {
			return true;
		}

		// 平台id错误
		if (HawkOSOperator.isEmptyString(puid)) {
			return false;
		}
		return true;
	}

	/**
	 * 通过puid获取玩家名
	 * 
	 * @param puid
	 * @param pfToken
	 * @param channel
	 * @return
	 */
	public static String getLoginNameByPuid(String playerId, String puid, String pfToken, String channel, HawkSession session, String openid) {
		// 预设名字
		String preinstallName = RedisProxy.getInstance().getPreinstallName(openid);
		if (!HawkOSOperator.isEmptyString(preinstallName)) {
			RedisProxy.getInstance().removePreinstallName(openid, preinstallName);
			return preinstallName;
		}
		
		String playerName = "";
		String puidProfile = null;
		boolean profileReload = false;
		try {
			// 拉取平台名字
			puidProfile = RedisProxy.getInstance().getPuidProfile(puid);
			
			// 缓存信息中不存在的时候, 通过msdk接口重新拉取
			if (HawkOSOperator.isEmptyString(puidProfile) && !HawkOSOperator.isEmptyString(pfToken) && !GsConfig.getInstance().isRobotMode() && !puid.startsWith("robot")) {
				JSONObject pfInfoJson = JSONObject.parseObject(pfToken);
				Map<String, String> params = new HashMap<String, String>();
				params.put("channel", channel);
				JSONObject json = SDKManager.getInstance().fetchProfile(SDKConst.SDKType.MSDK, params, pfInfoJson, session.getAddress());
				if (json != null) {
					puidProfile = json.toJSONString();
					profileReload = true;
				}
			}

			// 获取平台中对应的名字
			if (!HawkOSOperator.isEmptyString(puidProfile)) {
				JSONObject profileJson = JSON.parseObject(puidProfile);
				
				// QQ为大写, WX为小写
				playerName = profileJson.getString("nickName");
				if (HawkOSOperator.isEmptyString(playerName)) {
					playerName = profileJson.getString("nickname");
				}
				
				// 平台名如果在系统中非法, 重置不采用
				if (GameUtil.checkPlayerNameCode(playerName) != Status.SysError.SUCCESS_OK_VALUE) {
					playerName = "";
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		if (!HawkOSOperator.isEmptyString(playerName)) {
			// 判断之前的名字是否存在
			int index = 0;
			do {
				if (GameUtil.tryOccupyPlayerName(playerId, puid, playerName) == Status.SysError.SUCCESS_OK_VALUE) {
					break;
				}
				playerName = String.format("%s%d", playerName, ++index);
				
				// 合法性检测
				if (GameUtil.checkPlayerNameCode(playerName) != Status.SysError.SUCCESS_OK_VALUE) {
					playerName = "";
					break;
				}
			} while (true);
		}

		// 第三方拉取到的名字不为null或者空串 并且 不包含屏蔽字
		if (HawkOSOperator.isEmptyString(playerName)) {
			playerName = randomPlayerName(playerId, puid);
		}

		// 存储拉取的平台信息
		if (profileReload && !HawkOSOperator.isEmptyString(puidProfile)) {
			RedisProxy.getInstance().updatePuidProfile(puid, puidProfile);
		}
		
		return playerName;
	}
	
	/**
	 * 系统随机名字
	 * 
	 * @param playerId
	 * @param puid
	 * @return
	 */
	public static String randomPlayerName(String playerId, String puid) {
		int randTimes = 0;
		do {
			// 随机生成名字
			String randName = GlobalData.getInstance().randomPlayerName();
			if (GameUtil.tryOccupyPlayerName(playerId, puid, randName) == Status.SysError.SUCCESS_OK_VALUE) {
				return randName;
			}
		} while (randTimes++ < NAME_RANDOM_TIMES);
		
		return HawkUUIDGenerator.genUUID();
	}

	/**
	 * 创建新的玩家实体
	 * 
	 * @param puid
	 * @param playerName
	 * @param serverId
	 * @param cmd
	 * @return
	 */
	public static PlayerEntity createNewPlayer(String playerId, String openid,String puid, String playerName, String serverId, HPLogin cmd) {
		PlayerEntity playerEntity = null;
		if (createFailedOpenIds.contains(openid)) {
			List<PlayerEntity> playerEntitys = HawkDBManager.getInstance().query(
					"from PlayerEntity where puid = ? and serverId = ? and isActive = 1 and invalid = 0", puid, serverId);

			// 移除错误记录
			createFailedOpenIds.remove(openid);
			
			if (playerEntitys != null && playerEntitys.size() > 0) {
				playerEntity = playerEntitys.get(0);
			}
			
			if (playerEntity != null) {
				playerEntity.setName(playerName);
				return playerEntity;
			}
		}
		
		// 创建玩家实体对象
		playerEntity = new PlayerEntity();
		playerEntity.setId(playerId);
		playerEntity.setPuid(puid);
		playerEntity.setOpenid(openid);
		playerEntity.setServerId(serverId);
		playerEntity.setName(playerName);
		playerEntity.setPlatform(cmd.getPlatform());
		playerEntity.setChannel(cmd.getChannel());
		playerEntity.setCountry(cmd.getCountry());
		playerEntity.setDeviceId(cmd.getDeviceId());
		playerEntity.setLang(cmd.getLang());
		playerEntity.setVersion(cmd.getVersion());
		playerEntity.setChannelId(cmd.getChannelId());

		// 创建玩家的db对象
		if (!HawkDBManager.getInstance().create(playerEntity)) {
			// 添加错误记录
			createFailedOpenIds.add(openid);
			HawkLog.errPrintln("create player entity failed, puid: {}, deviceId: {}", puid, cmd.getDeviceId());
			return null;
		}
		
		return playerEntity;
	}

	/**
	 * 初始化登录玩家
	 * 
	 * @param accountInfo
	 * @param loginType
	 * @param cmd
	 * @param session
	 * @return
	 */
	public static boolean initLoginPlayer(AccountInfo accountInfo, int loginType, HPLogin cmd, HawkSession session) {
		// 创建玩家对象进行消息和协议处理
		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, accountInfo.getPlayerId());
		HawkObjBase<HawkXID, HawkAppObj> objBase = GsApp.getInstance().lockObject(xid);
		try {
			// id校验检查
			if (objBase != null && !xid.getUUID().equals(objBase.getObjKey().getUUID())) {
				HawkLog.errPrintln("player uuid deranged, playerId: {}, objId: {}", xid.getUUID(), objBase.getObjKey().getUUID());
				return false;
			}
			
			// 对象不存在即创建
			if (objBase == null || !objBase.isObjValid()) {
				objBase = GsApp.getInstance().createObj(xid);
				if (objBase != null) {
					objBase.lockObj();
					HawkLog.logPrintln("player object create success, playerId: {}, puid: {}, deviceId: {}", accountInfo.getPlayerId(), accountInfo.getPuid(), cmd.getDeviceId());
				}
			}

			if (objBase != null) {
				Player player = (Player) objBase.getImpl();
				
				// 踢出之前的活跃对象
				kickoutActiveRole(cmd);
				
				// 设置平台的token信息
				if (!GameUtil.isWin32Platform(cmd.getPlatform(), cmd.getChannel())) {
					if (!HawkOSOperator.isEmptyString(cmd.getPfToken())) {
						JSONObject pfInfoJson = JSONObject.parseObject(cmd.getPfToken());
						
						// 避免token盗用
						try {
							String openid = (String) pfInfoJson.get("open_id");
							if (!cmd.getPuid().equals(openid)) {
								HawkLog.errPrintln("player login pftoken-openid error, self openid: {}, pfToken openid: {}", cmd.getPuid(), openid);
								return false;
							}
						} catch (Exception e) {
							HawkException.catchException(e);
						}
						
						player.setPfTokenJson(pfInfoJson);
						try {
							String channelId = player.getChannelId();
							if (!HawkOSOperator.isEmptyString(channelId)) {
								RedisProxy.getInstance().getRedisSession().setNx("PlayerRegChannel:" + cmd.getPuid(), channelId);
							}
						} catch (Exception e) {
							HawkException.catchException(e);
						}
						
						if (!pfInfoJson.containsKey("channelId")) {
							HawkLog.errPrintln("player login pftoken channelId error, playerId: {}, pfInfoJson: {}", cmd.getPlayerId(), pfInfoJson);
						}
					} else {
						HawkLog.errPrintln("player login pftoken miss, playerId: {}", cmd.getPlayerId());
					}
				}
				
				// 当前玩家在线的处理
				if (player.getActiveState() == GsConst.PlayerState.ONLINE) {
					// 如果会话一致, 即本玩家已是登录状态, 不处理登录协议
					if (player.getSession() == session) {
						HawkLog.logPrintln("player login request discard, state: {}, playerId: {}, puid: {}, deviceId: {}",
								player.getActiveState(), accountInfo.getPlayerId(), accountInfo.getPuid(), cmd.getDeviceId());
						
						return false;
					}
					
					// 不是同一个会话, 直接踢出之前的连接
					HawkLog.logPrintln("player been kickout by broken connect, playerId: {}", player.getId());
					{
						player.notifyPlayerKickout(Status.SysError.PLAYER_KICKOUT_VALUE, "");
						
						// 通知退出
						GsApp.getInstance().postMsg(player, SessionClosedMsg.valueOf());
						
						// 延迟关闭会话
						HawkSession oldSession = player.getSession();
						oldSession.setAppObject(null);
						GsApp.getInstance().addDelayAction(1000, new HawkDelayAction() {
							@Override
							protected void doAction() {
								oldSession.close();
							}
						});
					}
				}

				player.setFirstLogin(loginType);
				player.setHpLogin(cmd.toBuilder());
				if (session != null) {
					// 绑定会话对象
					session.setAppObject(player);
					// 在回话对象上面设置account信息
					session.setUserObject("account", accountInfo);
				}
				
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			if (objBase != null) {
				objBase.unlockObj();
				HawkLog.logPrintln("player object create unlock, puid: {}", accountInfo.getPuid());
			}
		}
		return false;
	}
	
	/**
	 * 跨服专用.
	 * @param cmd
	 * @param playerId
	 */
	public static void kickoutActiveRoleForCross(HPLogin cmd) {
		String accountOnlineInfo = RedisProxy.getInstance().getOnlineInfo(cmd.getPuid());
		if (HawkOSOperator.isEmptyString(accountOnlineInfo)) {
			return;
		}
		
		String[] serverPlatInfo = accountOnlineInfo.split(":");
		// 同一区分将另一在线角色踢下线
		if (serverPlatInfo[0].equals(GsConfig.getInstance().getServerId())) {
			String onlinePuid = GameUtil.getPuidByPlatform(cmd.getPuid(), serverPlatInfo[1]);
			String playerId = GlobalData.getInstance().getOnlinePlayerByPuid(onlinePuid);
			AccountInfo activeRole = null;
			if (!HawkOSOperator.isEmptyString(playerId)) {
				activeRole = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			}			 
			// 账号初始化踢出在线角色玩家时，没有做正常的游戏退出逻辑处理，因此上面判断这个账号还在线，实际上它的账号信息已被清除了
			if (activeRole == null) {
				HawkLog.errPrintln("cs active role kickout error, puid: {}, serverId: {}", onlinePuid, GsConfig.getInstance().getServerId());
				RedisProxy.getInstance().removeOnlineInfo(cmd.getPuid());
				return;
			}
			
			Player activePlayer = GlobalData.getInstance().getActivePlayer(activeRole.getPlayerId());
			if (activePlayer != null) {
				activePlayer.kickout(Status.SysError.ACCOUNT_DIFF_ROLE_KICKOUT_VALUE, true, null);
			}
			HawkLog.logPrintln("cs account role is online, openid: {}, platform: {}, serverId: {}", cmd.getPuid(), serverPlatInfo[1], cmd.getServerId());			
		} else {
			//把这个任务从跨服线程里面移掉.
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				
				@Override
				public Object run() {
					GmProxyHelper.proxyCall(serverPlatInfo[0], "kickout", "openid=" + cmd.getPuid() + "&serverId=" + GsConfig.getInstance().getServerId(), 2000);
					return null;
				}
			});
		}
	}
	/**
	 * 将在线玩家踢下线
	 * @param player
	 * @param platform
	 */
	public static void kickoutActiveRole(HPLogin cmd) {
		String accountOnlineInfo = RedisProxy.getInstance().getOnlineInfo(cmd.getPuid());
		if (HawkOSOperator.isEmptyString(accountOnlineInfo)) {
			return;
		}
		
		String[] serverPlatInfo = accountOnlineInfo.split(":");
		// 同一区分将另一在线角色踢下线
		if (serverPlatInfo[0].equals(GsConfig.getInstance().getServerId())) {
			String onlinePuid = GameUtil.getPuidByPlatform(cmd.getPuid(), serverPlatInfo[1]);
			String onlinePlayerId = GlobalData.getInstance().getOnlinePlayerByPuid(onlinePuid);
			AccountInfo activeRole = null;
			if (!HawkOSOperator.isEmptyString(onlinePlayerId)) {
				activeRole = GlobalData.getInstance().getAccountInfoByPlayerId(onlinePlayerId);
			}			 
			// 账号初始化踢出在线角色玩家时，没有做正常的游戏退出逻辑处理，因此上面判断这个账号还在线，实际上它的账号信息已被清除了
			if (activeRole == null) {
				HawkLog.errPrintln("active role kickout error, puid: {}, serverId: {}", onlinePuid, GsConfig.getInstance().getServerId());
				RedisProxy.getInstance().removeOnlineInfo(cmd.getPuid());
				return;
			}
			
			//下面这段判断逻辑有几种情况，A服不同平台登录,   A,B两服都有角色, 从A服跨到B服， 原B服角色登录也会走下面的逻辑.
			Player activePlayer = GlobalData.getInstance().getActivePlayer(activeRole.getPlayerId());			
			if (activePlayer != null && (!activePlayer.getPlatform().equals(cmd.getPlatform()) || 
					!activePlayer.getMainServerId().equals(GsConfig.getInstance().getServerId()))) {
				activePlayer.kickout(Status.SysError.ACCOUNT_DIFF_ROLE_KICKOUT_VALUE, true, null);
			}
			HawkLog.logPrintln("account role is online, openid: {}, platform: {}, serverId: {}", cmd.getPuid(), serverPlatInfo[1], cmd.getServerId());			
		} else {
			GmProxyHelper.proxyCall(serverPlatInfo[0], "kickout", "openid=" + cmd.getPuid() + "&serverId=" + GsConfig.getInstance().getServerId(), 2000);
		}
	}

	/**
	 * 平台鉴权
	 * 
	 * @param platform
	 * @param channel
	 * @param puid
	 * @return
	 */
	public static boolean platformAuthCheck(HPLogin cmd, HawkSession session) {
		int authCheckLevel = GsConfig.getInstance().getAuthCheckLevel();
		if (authCheckLevel == AuthCheckLevel.IGNORE) {
			return true;
		}

		// 重连的情况不需要鉴权
		if (cmd.getFlag() == LoginFlag.BROKEN_CONNECT_VALUE) {
			String puid = GameUtil.getPuidByPlatform(cmd.getPuid(), cmd.getPlatform());
			AccountInfo ai = GlobalData.getInstance().getAccountInfo(puid, cmd.getServerId());
			return ai != null;
		}

		// GM渠道不校验
		if (cmd.getChannel().equals(GsConfig.getInstance().getGmChannel())) {
			return true;
		}
				
		// auth已鉴权, 松散模式信赖auth鉴权结果
		if (authCheckLevel == AuthCheckLevel.RELAX) {
			String puid = GameUtil.getPuidByPlatform(cmd.getPuid(), cmd.getPlatform());
			String authToken = RedisProxy.getInstance().getAuthToken(puid);
			if (!HawkOSOperator.isEmptyString(authToken)) {
				return true;
			}
		}
		
		// win32无需鉴权
		if ("android".equals(cmd.getPlatform()) && "guest".equals(cmd.getChannel())) {
			return true;
		}

		// gameserver进行严格鉴权
		try {
			JSONObject pfInfoJson = JSONObject.parseObject(cmd.getPfToken());
			Map<String, String> params = new HashMap<String, String>();
			params.put("channel", cmd.getChannel());
			JSONObject json = SDKManager.getInstance().verifyLogin(params, pfInfoJson, session.getAddress());
			if (json != null && json.getIntValue("ret") == SDKConst.ResultCode.SUCCESS) {
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 设备激活判断
	 * 
	 * @param cmd
	 * @param session
	 * @return
	 */
	public static boolean checkDeviceActive(HPLogin cmd, HawkSession session) {
		// 需要激活设备的情况
		if (GsConfig.getInstance().isDeviceNeedActive()) {
			String puid = cmd.getPuid();
			boolean isRobot = false;
			if (puid != null && puid.startsWith("robot")) {
				isRobot = true;
			}

			if (!isRobot && !RedisProxy.getInstance().isDeviceActived(cmd.getDeviceId())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是否会灰度状态
	 * 
	 * @return
	 */
	public static boolean isPuidCtrl() {
		return GsConfig.getInstance().isPuidCtrl();
	}
	
	/**
	 * 账号激活判断
	 * 
	 * @param puid
	 * @return
	 */
	public static boolean checkPuidCtrl(String puid) {
		// 判断此账号是否在灰度账号列表中
		if (HawkConfigManager.getInstance().getConfigByKey(GrayPuidCtrl.class, puid) != null) {
			return true;
		}

		// 判断此账号是否被脚本命令加入白名单
		if (RedisProxy.getInstance().checkPuidControl(puid)) {
			return true;
		}
		
		HawkLog.logPrintln("puid ctrl interdict, puid: {}",puid);
		
		return false;
	}

	/**
	 * 设备号激活
	 * 
	 * @param protocol
	 * @param session
	 * @return
	 */
	public static boolean doDeviceActive(HawkProtocol protocol, HawkSession session) {
		if (GsConfig.getInstance().isDeviceNeedActive()) {
			HPActiveDevice cmd = protocol.parseProtocol(HPActiveDevice.getDefaultInstance());
			HPActiveDeviceRet.Builder builder = HPActiveDeviceRet.newBuilder();
			builder.setDeviceId(cmd.getDeviceId());
			builder.setCode(Status.DeviceError.ACTIVE_TOKEN_NOT_EXIST_VALUE);
			int result = RedisProxy.getInstance().canUseDeviceActiveToken(cmd.getActiveToken());
			
			if (result > 0) {
				builder.setCode(Status.DeviceError.ACTIVE_TOKEN_BEEN_USED_VALUE);
			} else if (result == 0) {
				RedisProxy.getInstance().activeDevice(cmd.getDeviceId(), cmd.getActiveToken());
				builder.setCode(0);
			}
			
			protocol.response(HawkProtocol.valueOf(HP.code.DEVICE_ACTIVE_S, builder));
		}
		return true;
	}
	
	/**
	 * 判断账号是否是被禁止创建角色的账号
	 * 
	 * @param session
	 * @param openid
	 * @return
	 */
	public static boolean forbidCreateRole(HawkSession session, String openid) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(openid, IDIPBanType.BAN_CREATE_ROLE);
		if (banInfo == null) {
			return false;
		}
		
		HPLoginRet.Builder response = HPLoginRet.newBuilder();
		response.setErrCode(Status.SysError.FORBID_CREATE_ROLE_VALUE);
		session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));

		// 过1秒断开之前的连接
		final HawkSession kickoutSession = session;
		GsApp.getInstance().addDelayAction(1000, new HawkDelayAction() {
			@Override
			protected void doAction() {
				kickoutSession.close();
			}
		});
		
		return true;
	}

	/**
	 * 账号封号检测
	 * 
	 * @param session
	 * @param accountInfo
	 * @param openid
	 */
	public static boolean forbidAccount(HawkSession session, AccountInfo accountInfo, String openid) {
		long currentTime = HawkTime.getMillisecond();
		String banReason = "";
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(openid, IDIPBanType.AREA_BAN_ACCOUNT);  // 保留这一行是为了兼容历史
		if (banInfo == null) {
			banInfo = RedisProxy.getInstance().getIDIPBanInfo(accountInfo.getPuid(), IDIPBanType.AREA_BAN_ACCOUNT);
		}
		
		if (banInfo != null) {
			long banEndTime = banInfo.getEndTime();
			banInfo.setTargetId(accountInfo.getPlayerId());
			banInfo.setBanMsg(banInfo.getBanMsg() + "（解封时间：" + HawkTime.formatTime(banEndTime) + "）");
			banReason = banInfo.getBanMsg();
			RedisProxy.getInstance().addIDIPBanInfo(accountInfo.getPlayerId(), banInfo, IDIPBanType.BAN_ACCOUNT);
			accountInfo = GlobalData.getInstance().updateAccountInfo(accountInfo.getPuid(), accountInfo.getServerId(), accountInfo.getPlayerId(), banEndTime, accountInfo.getPlayerName());
		}
		
		long banEndTime = accountInfo.getForbidenTime();
		// 封禁时间结束了
		if (currentTime > banEndTime) {
			banInfo = RedisProxy.getInstance().getIDIPBanInfo(openid, IDIPBanType.CARE_BAN_ACCOUNT);
			// 没有成长守护平台相关的封禁信息，或当前时间不在封禁时间段范围内
			if (banInfo == null || currentTime < banInfo.getStartTime() || currentTime > banInfo.getEndTime() ) {
				return false;
			}
			
			banEndTime = banInfo.getEndTime();
			banReason = banInfo.getBanMsg();
		}
		
		HPLoginRet.Builder response = HPLoginRet.newBuilder();
		response.setErrCode(Status.SysError.PLAYER_FORBIDDEN_VALUE);
		if (!HawkOSOperator.isEmptyString(banReason)) {
			response.setBanReason(banReason);
		} else {
			banInfo = RedisProxy.getInstance().getIDIPBanInfo(accountInfo.getPlayerId(), IDIPBanType.BAN_ACCOUNT);
			if (banInfo != null) {
				response.setBanReason(banInfo.getBanMsg());
			}
		}
		
		response.setBanTimeDiff(banEndTime - HawkTime.getMillisecond());
		session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));

		// 过1秒断开之前的连接
		final HawkSession kickoutSession = session;
		GsApp.getInstance().addDelayAction(1000, new HawkDelayAction() {
			@Override
			protected void doAction() {
				kickoutSession.close();
			}
		});

		HawkLog.logPrintln("player is forbiden, playerId: {}, puid: {}", accountInfo.getPlayerId(), accountInfo.getPuid());
		
		return true;
	}
	
	/**
	 * 玩家注册相关信息存储
	 * 
	 * @param playerEntity
	 * @param cmd
	 */
	public static void playerRegisterSuccess(PlayerEntity playerEntity, HPLogin cmd) {
		AccountRoleInfo accountRole = AccountRoleInfo.newInstance().openId(playerEntity.getOpenid())
				 .playerId(playerEntity.getId())
				 .playerName(playerEntity.getName())
				 .playerLevel(1)
				 .cityLevel(1)
				 .vipLevel(0)
				 .battlePoint(0)
				 .icon(playerEntity.getIcon())
				 .pfIcon("")
				 .serverId(playerEntity.getServerId())
				 .activeServer(playerEntity.getServerId())
				 .platform(playerEntity.getPlatform())
				 .registerTime(HawkTime.getMillisecond())
				 .loginTime(0)
				 .logoutTime(0);
		RedisProxy.getInstance().batchAddRegisterInfo(accountRole, cmd.getPhoneInfo());
		
		ZonineSDK.getInstance().opDataReport(OpDataType.NEW_USER, playerEntity.getOpenid(), 1);
		
		RelationService.getInstance().beInvitedAccountIntoGame(playerEntity, cmd.getPfToken());
	}
	
	/**
	 * 玩家心跳处理
	 * 
	 * @param player
	 * @param protocol
	 */
	public static boolean onPlayerHeartBeat(HawkProtocol protocol) {
		if (protocol == null || protocol.getSession() == null || protocol.getSession().getAppObject() == null) {
			return false;
		}
		
		try {			
			Player player = (Player) protocol.getSession().getAppObject();
			
			//如果是跨服就扔到跨服那边去处理 之所以倒一次手, 是因为需要通过心跳包来保证player不被清理.
			String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
			if (!HawkOSOperator.isEmptyString(toServerId)) {
				CrossProxy.getInstance().sendNotify(protocol, toServerId, player.getId());
				//return true;
			}
			HPHeartBeat cmd = protocol.parseProtocol(HPHeartBeat.getDefaultInstance());
			
			// 机器人模式下测试账号重置
			if (GsConfig.getInstance().isRobotMode() && cmd.hasResetAccount() && cmd.getResetAccount()) {
				GameUtil.resetAccount(player);
				return true;
			}
						
			// 客户端时间和服务端时间不相上下时，说明客户端传的时间是正确的，才有以下操作
			long clientTime = cmd.getTimeStamp();
			long serverTime = HawkApp.getInstance().getCurrentTime();
			if (serverTime / clientTime < 2) {
				player.getData().setClientServerTimeSub((int) ((clientTime - serverTime)/1000));
			}
			
			// 回复心跳
			HPHeartBeat.Builder builder = HPHeartBeat.newBuilder();
			builder.setTimeStamp(serverTime);
			protocol.response(HawkProtocol.valueOf(HP.sys.HEART_BEAT, builder));
			
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
}
