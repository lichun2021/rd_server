package com.hawk.robot;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.protocol.HawkProtocolManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotEntity;
import com.hawk.game.protocol.Army.ArmyInfoPB;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.ItemInfo;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Mission.MissionPB;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Talent.TalentInfo;
import com.hawk.robot.config.BuildLimitCfg;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.data.ActivityData;
import com.hawk.robot.data.AnchorData;
import com.hawk.robot.data.BasicData;
import com.hawk.robot.data.CityData;
import com.hawk.robot.data.GameRobotData;
import com.hawk.robot.data.GuildData;
import com.hawk.robot.data.WorldData;
import com.hawk.robot.util.WorldUtil;

public class GameRobotEntity extends HawkRobotEntity {
	/**
	 * 机器人标识
	 */
	protected String puid;
	/**
	 * 玩家id
	 */
	protected String playerId;
	/**
	 * 机器人状态
	 */
	protected volatile RobotState state;
	/**
	 * 机器人玩家数据
	 */
	protected GameRobotData robotData;
	/**
	 * 会话
	 */
	protected GameRobotSession session;
	/**
	 * 最近一次登录时间
	 */
	protected long loginTime;
	
	/**
	 * 下线时间，大于0时时间到了强制下线
	 */
	protected long offlineTime;
	
	protected int waitLoginTimes;
	
	protected int worldViewPosition = 0;
	
	protected AnchorRobotSession anchorSession;
	
	/**
	 * 是否在世界上
	 */
	protected boolean isInWorld;
	
	/**
	 * 机器人生命周期状态
	 * @author admin
	 *
	 */
	public enum RobotState {
		INIT, CONNECTED, WAIT_LOGIN, ASSEMBLE_FINISH, OFFLINE
	}
	
	/**
	 * 构造
	 */
	public GameRobotEntity() {
		session = new GameRobotSession(this, GameRobotApp.getInstance().getBootstrap());
		anchorSession = new AnchorRobotSession(this, GameRobotApp.getInstance().getAnchorBootstrap());
		robotData = new GameRobotData();
		state = RobotState.INIT;
	}
	
	/**
	 * 开启服务器连接
	 */
	public boolean init(String puid) {
		this.puid = puid;
		
		// 初始化会话
		String ip = RobotAppConfig.getInstance().getServerIp();
		int port = RobotAppConfig.getInstance().getPort();
		long connTimeout = RobotAppConfig.getInstance().getTimeout();
		long heartbeatTime = RobotAppConfig.getInstance().getHeartbeat();
		// 初始化协议标识符
		HawkProtocolManager.getInstance().setProtocolIdentify(0x00EFBBBF);
		if (!session.init(ip, port, (int) connTimeout, (int) heartbeatTime)) {
			HawkLog.errPrintln("robot connect server failed, ip: {}, port: {}", ip, port);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 连接主播服务器
	 * @param ip
	 * @param port
	 * @return
	 */
	public boolean initAnchorServer(String ip, int port){
		long connTimeout = RobotAppConfig.getInstance().getTimeout();
		long heartbeatTime = RobotAppConfig.getInstance().getHeartbeat();
		if (!anchorSession.init(ip, port, (int) connTimeout, (int) heartbeatTime)) {
			HawkLog.errPrintln("robot connect anchor server failed, ip: {}, port: {}", ip, port);
			return false;
		}
		return true;
	}
	
	public boolean isAnchorSessionConnect() {
		return anchorSession.isActive();
	}
	

	public AnchorRobotSession getAnchorSession() {
		return anchorSession;
	}

	/**
	 * 单个机器人连接
	 * @param puid
	 */
	public void connect() {
		GameRobotApp.getInstance().randRobotActions(this);
		GameRobotApp.getInstance().addRobot(this);
	}
	
	/**
	 * 发送登录协议
	 * @return
	 */
	public synchronized boolean doLogin(){
		if (StatisticHelper.isLoginAccount(getPuid())) {
			return false;
		}
		
		HawkProtocol loginProtocol = HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, genLoginBuilder());
		if (sendProtocol(loginProtocol)) {
			StatisticHelper.addLogin(getPuid());
			loginTime = HawkTime.getMillisecond();
			HawkLog.logPrintln("send login protocol complete, puid: {}", this.getPuid());
		}
		
		return true;
	}
	
	/**
	 * 处于等待登录状态时，重新发起的连接
	 * @return
	 */
	public synchronized boolean doLoginAgain() {
		if (state != RobotState.WAIT_LOGIN) {
			return false;
		}
		
		this.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_WAIT_C_VALUE, genLoginBuilder()));
		waitLoginTimesAdd();
		HawkLog.logPrintln("send login protocol again, puid: {}", this.getPuid());
		return true;
	}
	
	/**
	 * 机器人登陆成功
	 */
	public void loginSuccess(GameRobotEntity robotEntity) {
		robotEntity.setState(RobotState.ASSEMBLE_FINISH);
		StatisticHelper.incrOnlineCnt();
	}
	

	/**
	 * 关闭session时移除机器人
	 */
	public synchronized void doLogout() {
		if(state == RobotState.OFFLINE) {
			return;
		}
		
		loginTime = 0;
		String stateStr = state.name();
		WorldUtil.worldMarchCallBack(this);
		GameRobotApp.getInstance().removeRobot(this);
		WorldDataManager.getInstance().removeRobot(this.getPlayerId());
		state = RobotState.OFFLINE;
		this.closeSession();
		
		HawkLog.logPrintln("robot session closed, puid: {}, state: {}", getPuid(), stateStr);
		
		if (!stateStr.equals(RobotState.ASSEMBLE_FINISH.name())) {
			RobotAppHelper.getInstance().robotReconnect(getPuid());
		}
	}
	
	public long getLoginTime() {
		return loginTime;
	}

	/**
	 * 获取玩家id
	 */
	public String getPlayerId() {
		return playerId;
	}
	
	/**
	 * 设置玩家id
	 * @param playerId
	 */
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	/**
	 * 获取玩家“设备”ID
	 */
	public String getPuid() {
		return puid;
	}
	
	/**
	 * 设置“设备”ID
	 * @param puid
	 */
	public void setPuid(String puid) {
		this.puid = puid;
	}
	
	/**
	 * 获取机器人名字
	 * @return
	 */
	public String getName() {
		return robotData.getBasicData().getPlayerInfo().getName();
	}
	
	/**
	 * 获取机器人体力值
	 * @return
	 */
	public int getVit() {
		return robotData.getBasicData().getPlayerInfo().getVit();
	}
	
	/**
	 * 给改机器人分配“设备”ID
	 * @return
	 */
	public int getPuidNum() {
		String puidPrefix = RobotAppConfig.getInstance().getRobotPuidPrefix();
		int index = puid.indexOf(puidPrefix) + puidPrefix.length();
		try {
			return Integer.valueOf(puid.substring(index));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return 0;
	}
	
	/**
	 * 获取机器人所在的联盟ID
	 * @return
	 */
	public String getGuildId() {
		return robotData.getGuildData().getGuildId();
	}
	
	/**
	 * 设置状态信息
	 * @param state
	 */
	public void setState(RobotState state) {
		this.state = state;
	}
	
	/**
	 * 获取状态信息
	 * @return
	 */
	public RobotState getState() {
		return state;
	}
	
	/**
	 * 获取玩家离线时长
	 * @return
	 */
	public long getOfflineTime() {
		return offlineTime;
	}

	/**
	 * 设置离线时长
	 * @param offlineTime
	 */
	public void setOfflineTime(long offlineTime) {
		this.offlineTime = offlineTime;
	}
	
	/**
	 * 等待登录状态下的已发送登录请求的次数增加
	 */
	public void waitLoginTimesAdd() {
		waitLoginTimes += 1;
	}
	
	/**
	 * 获取等待登录状态下的已发送登录请求的次数
	 * @return
	 */
	public int getWaitLoginTimes() {
		return waitLoginTimes;
	}
	
	/**
	 * 等待登录状态下的已发送登录请求的次数清零
	 */
	public void clearWaitLoginTimes() {
		waitLoginTimes = 0;
	}

	/**
	 * 获取机器人的数据
	 * @return
	 */
	public GameRobotData getData() {
		return robotData;
	}
	
	/**
	 * 判断机器人是否在线
	 */
	public boolean isOnline(){
		return state == RobotState.ASSEMBLE_FINISH;
	}
	
	/**
	 * 判断机器人是否已连接到服务器
	 */
	public boolean isConnected(){
		if(session == null) {
			return false;
		}
		
		return session.isActive() && state != RobotState.INIT && state != RobotState.OFFLINE;
	}
	
	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @return
	 */
	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		if (session != null && session.isActive()) {
			StatisticHelper.incProtocolSendCnt(protocol.getType());
			boolean result = session.sendProtocol(protocol);
			if (!result) {
				session.onSessionClosed();
				HawkLog.logPrintln("send protocol failed after send operation, puid: {},  protocol: {}", puid, protocol.getType());
			}
			
			return result;
		} 
		
		if (session != null) {
			session.onSessionClosed();
			//session = null;
			HawkLog.logPrintln("send protocol failed, session not active, puid: {}, protocol: {}", puid, protocol.getType());
		} else {
			doLogout();
			HawkLog.logPrintln("send protocol failed, session is null, puid: {}, protocol: {}", puid, protocol.getType());
		}
		
		return false;
	}
	
	
	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean sendAnchorProtocol(HawkProtocol protocol) {
		if (anchorSession != null && anchorSession.isActive()) {
			StatisticHelper.incProtocolSendCnt(protocol.getType());
			return anchorSession.sendProtocol(protocol);
		} else {
			if (anchorSession != null) {
				anchorSession.onSessionClosed();
				//session = null;
			}
			HawkLog.logPrintln("send protocol failed, {}, protocol: {}, puid: {}", session != null ? "session not active" : "session is null", protocol.getType(), puid);
		}
		return false;
	}
	
	/**
	 * 关闭会话
	 */
	@Override
	public boolean closeSession() {
		if(session != null) {
			HawkLog.logPrintln("circle closed session, puid:{}, playerId:{}", puid, playerId);
			session.close();
		}
		return true;
	}
	
	/**
	 * 构建登录协议发送信息
	 * @return
	 */
	private HPLogin.Builder genLoginBuilder() {
		HPLogin.Builder builder = HPLogin.newBuilder();
		builder.setFlag(0);
		builder.setPfToken("");
		builder.setCountry("cn");
		builder.setLang("zh-CN");
		builder.setChannel("guest");
		builder.setVersion("1.0.0.0");
		builder.setPlatform("android");
		builder.setPuid(this.getPuid());
		builder.setDeviceId(this.getPuid());
		builder.setServerId(RobotAppConfig.getInstance().getServerId());
		builder.setPhoneInfo("{\"deviceMode\":\"win32\",\"mobileNetISP\":\"0\",\"mobileNetType\":\"0\"}\n");
		return builder;
	} 
	
	/**
	 * 获取玩家建筑相关数据
	 * @return
	 */
	public List<BuildingPB> getBuildingObjects() {
		return new ArrayList<BuildingPB>(robotData.getCityData().getBuildingObjects().values());
	}
	
	/**
	 * 获取玩家队列数据
	 * @return
	 */
	public List<QueuePB> getQueueObjects() {
		return new ArrayList<QueuePB>(robotData.getBasicData().getQueueObjects().values());
	}
	
	/**
	 * 获取玩家兵种相关数据
	 * @return
	 */
	public List<ArmyInfoPB> getArmyObjects() {
		return new ArrayList<ArmyInfoPB>(robotData.getCityData().getArmyObjects().values());
	}
	
	/**
	 * 获取玩家任务相关数据
	 * @return
	 */
	public List<MissionPB> getMissionObjects() {
		return new ArrayList<MissionPB>(robotData.getActivityData().getMissionObjects().values());
	}
	
	/**
	 * 获取物品道具相关数据
	 * @return
	 */
	public List<ItemInfo> getItemObjects() {
		return new ArrayList<ItemInfo>(robotData.getBasicData().getItemObjects().values());
	}
	
	/**
	 * 获取天赋相关数据
	 * @return
	 */
	public List<TalentInfo> getTalentObjects() {
		return new ArrayList<TalentInfo>(robotData.getBasicData().getTalentObjects().values());
	}
	
	/**
	 * 获取玩家活动相关数据
	 * @return
	 */
	public ActivityData getActivityData() {
		return robotData.getActivityData();
	}
	
	/**
	 * 获取玩家基础数据
	 * @return
	 */
	public BasicData getBasicData() {
		return robotData.getBasicData();
	}
	
	/**
	 * 获取玩家城建相关数据
	 * @return
	 */
	public CityData getCityData() {
		return robotData.getCityData();
	}
	
	/**
	 * 获取玩家联盟相关数据
	 * @return
	 */
	public GuildData getGuildData() {
		return robotData.getGuildData();
	}
	
	/**
	 * 获取玩家世界相关数据
	 * @return
	 */
	public WorldData getWorldData() {
		return robotData.getWorldData();
	}
	
	/**
	 * 主播信息
	 * @return
	 */
	public AnchorData getAnchorData(){
		return robotData.getAnchorData();
	}
	
	/**
	 * 获取大本等级
	 * @return
	 */
	public int getCityLevel() {
		return getCityData().getConstructionBuildLevel();
	}
	
	/**
	 * 根据类型获取建筑对象
	 * @param type
	 * @return
	 */
	public List<BuildingPB> getBuildingByType(int type) {
		return robotData.getCityData().getBuildingByType(type);
	}
	
	/**
	 * 通过限制类型获取建筑数据
	 * @param limitTypes
	 * @return
	 */
	public List<BuildingPB> getBuildingListByLimitType(LimitType... limitTypes) {
		return robotData.getCityData().getBuildingListByLimitType(limitTypes);
	}
	
	/**
	 * 获取某类型最大等级的建筑
	 * @param type
	 * @return
	 */
	public BuildingPB getMaxLevelBuilding(int type) {
		return robotData.getCityData().getMaxLevelBuilding(type);
	}
	
	/**
	 * 根据限制类型获取可造建筑数量上限
	 * @param limitType
	 * @return
	 */
	public int getBuildingNumLimit(int limitType) {
		//获取大本数据等级
		int level = getBuildingMaxLevel(BuildingType.CONSTRUCTION_FACTORY_VALUE);
		BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, limitType);
		if (cfg == null) {
			return 0;
		}

		return cfg.getLimit(level);
	}
	
	/**
	 * 根据建筑类型获取同类建筑中最大等级
	 * @param type
	 * @return
	 */
	public int getBuildingMaxLevel(int type) {
		BuildingPB building = getMaxLevelBuilding(type);
		if (building == null) {
			return 0;
		}
		
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
		return buildingCfg.getLevel();
	}
	
	/**
	 * 获取军衔等级
	 * @return
	 */
	public int getMilitaryLevel() {
		return getBasicData().getPlayerInfo().getMilitaryLevel();
	}

	public int getWorldViewPosition() {
		return worldViewPosition;
	}

	public void setWorldViewPosition(int worldViewPosition) {
		this.worldViewPosition = worldViewPosition;
	}

	public boolean isInWorld() {
		return isInWorld;
	}

	public void setInWorld(boolean isInWorld) {
		this.isInWorld = isInWorld;
	}
}
