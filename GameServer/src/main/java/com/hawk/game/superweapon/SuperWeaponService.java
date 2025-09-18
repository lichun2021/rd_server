package com.hawk.game.superweapon;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.net.HawkNetworkManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsConfig;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.config.SuperWeaponSection;
import com.hawk.game.config.SuperWeaponSectionAwardCfg;
import com.hawk.game.config.SuperWeaponSpecialAwardCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.SuperWeapon.AllSuperWeaponInfo;
import com.hawk.game.protocol.SuperWeapon.SWSeasonRankInfo;
import com.hawk.game.protocol.SuperWeapon.SWSeasonRankResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoResp;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterMarch;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.superweapon.weapon.AbstractSuperWeapon;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 超级武器服务类
 * @author zhenyu.shang
 * @since 2018年4月23日
 */
public class SuperWeaponService extends HawkAppObj {
	
	/**
	 * 日志记录器
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 全局实例对象
	 */
	private static SuperWeaponService instance = null;
	
	/**
	 * 所有超级武器
	 */
	private Map<Integer, IWeapon> allWeapons = new HashMap<Integer, IWeapon>();
	
	/** 
	 * 整体状态(和平, 报名, 报名准备, 开战)
	 */
	private int status;
	
	/** 
	 * 下阶段开始时间 
	 */
	private long startTime;
	
	/**
	 * 超级武器期数
	 */
	private int turnCount;
	
	/** 
	 * 开始公告提示步骤
	 */
	private int warStartNoticeStep = 0;
	
	/**
	 * 开启活动标识
	 */
	public boolean openService;
	
	/**
	 * 是否进入预热期
	 */
	private boolean inPrepareStatus;
	
	/**
	 * 赛季轮次
	 */
	private int seasonTurn;
	
	/**
	 * 赛季排名
	 */
	private Map<String, Integer> seasonRank;
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static SuperWeaponService getInstance() {
		return instance;
	}

	public SuperWeaponService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		try {
			
			Integer statusData = LocalRedis.getInstance().getSuperWeaponDataByKey("status", Integer.class);
			this.status = statusData == null ? 0 : statusData;
			Long startTimeData = LocalRedis.getInstance().getSuperWeaponDataByKey("startTime", Long.class);
			this.startTime = startTimeData == null ? 0 : startTimeData;
			Integer turnCountData = LocalRedis.getInstance().getSuperWeaponDataByKey("turnCount", Integer.class);
			this.turnCount = (turnCountData == null || turnCountData == 0) ? 1 : turnCountData;
			Boolean openService = LocalRedis.getInstance().getSuperWeaponDataByKey("openService", Boolean.class);
			this.openService = openService == null ? false : openService;
			
			logger.info("super weapon service init, status:{}, startTime:{}, turnCount:{}, openService:{}", status, startTime, turnCount, openService);
			
			// 初始化赛季轮次
			seasonTurn = LocalRedis.getInstance().getSWSeasonTurn();
			// 初始化赛季排行榜
			seasonRank = LocalRedis.getInstance().getSWSeasonScore();
			
			//初始化完之后，如果状态为空，则是第一次启服
			if(status == 0 || status == SuperWeaponPeriod.INIT_VALUE || (GsConfig.getInstance().isDebug() && status == SuperWeaponPeriod.PEACE_VALUE)){
				if(status == 0){
					setStatus(SuperWeaponPeriod.INIT_VALUE);
				}
				//设置开始时间
				long serverOpenTime = GameUtil.getServerOpenTime();
				if(serverOpenTime <= 0){
					throw new HawkException("server open time is zero...");
				}
				SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
				// 计算基准时间
				long baseTime = serverOpenTime + (constCfg.getInitPeaceTime() * 1000L);
				baseTime = Math.max(HawkTime.getMillisecond(), baseTime);
				long nexWarStartTime = HawkTime.getNextTimeDayOfWeek(baseTime, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute());
				long nextSignUpTime = nexWarStartTime - constCfg.getSignUpTime();  
				if (nextSignUpTime <= HawkTime.getMillisecond()) {
					setStatus(SuperWeaponPeriod.SIGNUP_VALUE);
					setStartTime(nexWarStartTime);
				} else {
					setStartTime(nextSignUpTime);
				}
			}
			
			//初始化所有超级武器
			for (int pointId : getSuperWeaponPoints()) {
				IWeapon weapon = new AbstractSuperWeapon(pointId);
				HawkAssert.notNull(weapon);
				weapon.init();
				allWeapons.put(pointId, weapon);
			}			
			
			addTickable(new HawkPeriodTickable(1000) {
				@Override
				public void onPeriodTick() {
					if(isOpenService()){
						superWeaponTick();
					}
				}
			});
			
			// 超级武器记录信息
			SuperWeaponRecord.getInstance().init();
			// 超级武器记礼包信息
			SuperWeaponGift.getInstance().init();
			
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 心跳处理
	 */
	private void superWeaponTick(){
		switch (getStatus()) {
		// 初始阶段
		case SuperWeaponPeriod.INIT_VALUE:
			doInitPeriod();
			break;

		// 报名阶段
		case SuperWeaponPeriod.SIGNUP_VALUE:
			doSignUpPeriod();
			break;
			
		// 战斗阶段
		case SuperWeaponPeriod.WARFARE_VALUE:
			doWarfarePeriod();
			break;
			
		// 控制阶段
		case SuperWeaponPeriod.CONTROL_VALUE:
			doControlPeriod();
			break;
			
		// 和平阶段
		case SuperWeaponPeriod.PEACE_VALUE:
			doPeacePeriod();
			break;
			
		default:
			break;
		}
	}

	/**
	 * 和平阶段
	 */
	private void doPeacePeriod() {
		long currTime = HawkTime.getMillisecond();
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		
		// 战争将要开始系统通知
		willStartNotice((int) ((getStartTime() - currTime) / 1000), NoticeCfgId.SUPERWEAPON_SIGNUP_WILL_START, SuperWeaponConstCfg.getInstance().getMailBeforeTimeArray());
		
		// 预热期
		if(!isInPrepareStatus() && currTime > getStartTime() - constCfg.getPrepareTime() * 1000L){
			broadcastSuperWeaponInfo(null);
			setInPrepareStatus(true);
		}
		
		if (currTime < getStartTime()) {
			return;
		}
		
		// 改变当前进入战争
		warStartNoticeStep = 0;
		// 阶段改变
		setStatus(SuperWeaponPeriod.SIGNUP_VALUE);
		// 期数
		setTurnCount(getTurnCount() + 1);
		
		// 每个超级武器各自处理
		for (IWeapon weapon : allWeapons.values()) {
			try {
				weapon.doPaceToSignUp();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		// 超级武器开始跑马灯
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_SIGNUP_STARTED, null);
		// 下期开始时间
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			setStartTime(currTime + GameConstCfg.getInstance().getSuperBarrackSignTime());
		} else {
			setStartTime(currTime + constCfg.getSignUpTime());
		}
		// 阶段切换通知
		notifySuperWeaponPeriodChanged(SuperWeaponPeriod.PEACE_VALUE, SuperWeaponPeriod.SIGNUP_VALUE);
	}

	/**
	 * 控制阶段
	 */
	private void doControlPeriod() {
		long currTime = HawkTime.getMillisecond();
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		
		if (currTime < getStartTime()) {
			return;
		}		
		
		// 各自超级兵营各自处理
		for (IWeapon weapon : allWeapons.values()) {
			try {
				weapon.doControlToPace();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 设置预热期标识
		setInPrepareStatus(false);
		// 改变状态
		setStatus(SuperWeaponPeriod.PEACE_VALUE);
		// 阶段切换：控制 -> 和平
		notifySuperWeaponPeriodChanged(SuperWeaponPeriod.CONTROL_VALUE, SuperWeaponPeriod.PEACE_VALUE);
		// 超级武器进入和平时段
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUER_BARRAKE_CONTROL_TO_PACE, null);
		// 设置下阶段开始时间
		if (GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()) {
			setStartTime(currTime + GameConstCfg.getInstance().getSuperBarrackPaceTime());
		} else {
			setStartTime(HawkTime.getNextTimeDayOfWeek(currTime, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()) - constCfg.getSignUpTime());
		}
		
		int turnMax = SuperWeaponConstCfg.getInstance().getSeasonPeriodNum();
		if (seasonTurn > 0 && seasonTurn % turnMax == 0) {
			seasonRank.clear();
			LocalRedis.getInstance().clearSWSeasonScore();
		}
		
		setSeasonTurn(seasonTurn + 1);
	}
	
	/**
	 * 战斗阶段
	 */
	private void doWarfarePeriod() {
		long currTime = HawkTime.getMillisecond();
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		
		// 战争将要开始系统通知
		willStartNotice((int) ((getStartTime() - currTime) / 1000), NoticeCfgId.SUPER_BARRACK_WARFRE_WILL_OVER, SuperWeaponConstCfg.getInstance().getMailAfterTimeArray());
		
		//所有超级武器的心跳
		boolean allControl = true;
		
		// 各自超级兵营各自处理
		for (IWeapon weapon : allWeapons.values()) {
			try {
				weapon.tick(currTime);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			if(weapon.getStatus() != SuperWeaponPeriod.PEACE_VALUE
					&& weapon.getStatus() != SuperWeaponPeriod.CONTROL_VALUE){
				allControl = false;
			}
		}
		
		//所有的超武都完成争夺
		if(allControl){
			// 改变当前进入战争
			warStartNoticeStep = 0;
			// 设置预热期标识
			setInPrepareStatus(false);
			// 切换阶段
			setStatus(SuperWeaponPeriod.CONTROL_VALUE);
			// 设置下阶段开始时间
			if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
				setStartTime(currTime + GameConstCfg.getInstance().getSuperBarrackControlTime());
			} else {
				setStartTime(currTime + constCfg.getControlTime() * 1000L);
			}
			// 通知阶段改变
			notifySuperWeaponPeriodChanged(SuperWeaponPeriod.WARFARE_VALUE, SuperWeaponPeriod.CONTROL_VALUE);
			
			// 发赛季奖励
			int turnMax = SuperWeaponConstCfg.getInstance().getSeasonPeriodNum();
			if (seasonTurn > 0 && seasonTurn % turnMax == 0) {
				sendSWSectionAward();
			}
		}
	}

	/**
	 * 报名阶段
	 * @param currTime
	 * @param constCfg
	 */
	private void doSignUpPeriod() {
		long currTime = HawkTime.getMillisecond();
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		
		// 战斗将要开始系统通知
		willStartNotice((int) ((getStartTime() - currTime) / 1000), NoticeCfgId.SUPERWEAPON_WAR_WILL_START, SuperWeaponConstCfg.getInstance().getMailBeforeTimeArray());
		
		if (currTime < getStartTime()) {
			return;
		}
		
		// 改变当前进入战争
		warStartNoticeStep = 0;
		
		// 是否没人报名
		boolean haveNoSignUp = true;
		
		for (IWeapon weapon : allWeapons.values()) {
			try {
				if(weapon.hasSignUpGuild()){
					haveNoSignUp = false;
					continue;
				}
				// 如果没报名
				int pos[] = GameUtil.splitXAndY(weapon.getPointId());
				// 跑马灯
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_NO_SIGNUP, null, pos[0], pos[1]);
				// 针对单独超级兵营的状态切换
				weapon.noCommanderOver(currTime, constCfg, SuperWeaponPeriod.SIGNUP_VALUE);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 如果所有超级兵营都没人报名
		if (haveNoSignUp) {
			// 设置预热期标识
			setInPrepareStatus(false);
			// 切换到和平阶段
			setStatus(SuperWeaponPeriod.PEACE_VALUE);
			// 通知状态切换
			notifySuperWeaponPeriodChanged(SuperWeaponPeriod.SIGNUP_VALUE, SuperWeaponPeriod.PEACE_VALUE);
			// 设置下期开始时间
			if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()) {
				setStartTime(currTime + GameConstCfg.getInstance().getSuperBarrackPaceTime());
			} else {
				setStartTime(HawkTime.getNextTimeDayOfWeek(currTime + constCfg.getWarfareTime() * 1000L, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()) - constCfg.getSignUpTime());
			}
			return;
		}
		
		// 超级武器争夺战开始系统通知 
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_WAR_STARTED, null);
		// 设置下阶段开始时间
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			setStartTime(currTime + GameConstCfg.getInstance().getSuperBarrackWarfreTime());
		} else {
			setStartTime(currTime + constCfg.getWarfareTime() * 1000L);
		}
		// 设置预热期标识
		setInPrepareStatus(false);
		// 状态切换
		setStatus(SuperWeaponPeriod.WARFARE_VALUE);
		//设置超级武器的状态为战争状态, 并设置开始时间
		for (IWeapon weapon : allWeapons.values()) {
			try {
				if(!weapon.hasSignUpGuild()){
					continue;
				}
				weapon.setStatus(SuperWeaponPeriod.WARFARE_VALUE);
				weapon.setStartTime(currTime);
				weapon.printSignUpLog();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		// 通知状态切换
		notifySuperWeaponPeriodChanged(SuperWeaponPeriod.SIGNUP_VALUE, SuperWeaponPeriod.WARFARE_VALUE);
	}

	/**
	 * 初始阶段
	 * @param currTime
	 * @param constCfg
	 */
	@SuppressWarnings("deprecation")
	private void doInitPeriod() {
		long currTime = HawkTime.getMillisecond();
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		 
		//预热期开始的时候，广播一遍客户端
		if(!isInPrepareStatus() && currTime > getStartTime() - constCfg.getPrepareTime() * 1000L){
			broadcastSuperWeaponInfo(null);
			setInPrepareStatus(true);
		}
		
		if (currTime < getStartTime()) {
			return;
		}
		
		// 改变状态
		setStatus(SuperWeaponPeriod.SIGNUP_VALUE);
		// 跑马灯：报名开始
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_SIGNUP_STARTED, null);
		// 设置期数
		setTurnCount(1);
		
		setSeasonTurn(1);
		
		long startTime = 0;
		// 设置开始时间
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			startTime = currTime + GameConstCfg.getInstance().getSuperBarrackSignTime();
		} else {
			startTime = currTime + constCfg.getSignUpTime();
		}
		setStartTime(startTime);
		// 阶段切换：初始->报名
		notifySuperWeaponPeriodChanged(SuperWeaponPeriod.INIT_VALUE, SuperWeaponPeriod.SIGNUP_VALUE);
	}
	
	/**
	 * 战争将要开始系统通知
	 * @param remainTime 剩余时间（秒）
	 * */
	private void willStartNotice(int remainTime, NoticeCfgId noticeId, int[] times) {
		try {
			if(remainTime < 0 || times == null || times.length <= 0){
				return;
			}
			for (int i = times.length - 1; i >= 0; i--) {
				if (remainTime > times[i] || warStartNoticeStep >= i + 1) {
					continue;
				}
				//战斗即将开始时发邮件通知所有报名的玩家
				if(warStartNoticeStep == 0 && (noticeId == NoticeCfgId.SUPERWEAPON_WAR_WILL_START || noticeId == NoticeCfgId.SUPERWEAPON_SIGNUP_WILL_START)){
					
					// 报名即将开始
					if(noticeId == NoticeCfgId.SUPERWEAPON_SIGNUP_WILL_START){
						long currentTime = HawkTime.getMillisecond();
						long signUpTime = SuperWeaponConstCfg.getInstance().getSignUpTime();
						SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				                .setMailId(MailId.SUPER_WEAPON_REFRESH)
				                .build()
				                ,currentTime, currentTime + signUpTime + (remainTime * 1000L));
					}
					
					// 战斗即将开始
					if (noticeId == NoticeCfgId.SUPERWEAPON_WAR_WILL_START) {
						for (IWeapon weapon : allWeapons.values()) {
							weapon.sendWillStartMail();
						}
					}
				}
				warStartNoticeStep = i + 1;
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null, times[i] / 60);
				logger.info("super weapon war will start... left Time : {}", times[i] / 60);
				break;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 通知国王战时期改变
	 * 
	 * @param lastPeriod
	 * @param currPeriod
	 */
	protected void notifySuperWeaponPeriodChanged(int lastPeriod, int currPeriod) {
		try {
			// 广播超级武器状态信息
			broadcastSuperWeaponInfo(null);
			logger.info("president period changed: old status : {}, new status : {}", lastPeriod, currPeriod);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 广播超级武器战信息
	 */
	public void broadcastSuperWeaponInfo(Player player) {
		try {
			AllSuperWeaponInfo.Builder builder = AllSuperWeaponInfo.newBuilder();
			builder.setTurnCount(getTurnCount());
			
			if(isOpenService()){
				builder.setPeriodType(getStatus());
			} else {
				builder.setPeriodType(SuperWeaponPeriod.CLOSED_VALUE);
			}
			
			builder.setPeriodEndTime(getStartTime());
			for (IWeapon superWeapon : allWeapons.values() ) {
				builder.addSuperWeaponInfo(superWeapon.genSuperWeaponInfoBuilder());
			}
			
			int turnMax = SuperWeaponConstCfg.getInstance().getSeasonPeriodNum();
			
			builder.setSeasonCount(((seasonTurn - 1) / turnMax) + 1);
			builder.setSeasonTurn(((seasonTurn - 1) % turnMax) + 1);
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SUPER_WEAPON_INFO_S, builder);
			if (player != null) {
				player.sendProtocol(protocol);
			} else {
				HawkNetworkManager.getInstance().broadcastProtocol(protocol, null);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 开启超级武器
	 * @return
	 */
	public void openSuperWeaponService(){
		if(isOpenService()){ // 已经打开无需再执行
			return;
		}
		//首先判断当前时间是否已经超过了开始时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		if(currTime > getStartTime()){
			//自动跳到下一期
			setStatus(SuperWeaponPeriod.PEACE_VALUE);
			if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
				setStartTime(currTime + 300 * 1000L);
			} else {
				setStartTime(HawkTime.getNextTimeDayOfWeek(currTime, constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()) - constCfg.getSignUpTime());
			}
		}
		//设置状态为true
		setOpenService(true);
		//广播
		broadcastSuperWeaponInfo(null);
	}
	
	/**
	 * 关闭超级武器
	 * @return
	 */
	public boolean closeSuperWeaponService(){
		if(getStatus() != SuperWeaponPeriod.INIT_VALUE && getStatus() != SuperWeaponPeriod.PEACE_VALUE){
			return false;
		}
		setOpenService(false);
		//广播
		broadcastSuperWeaponInfo(null);
		return true;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
		LocalRedis.getInstance().updateSuperWeaponDataByKey("status", status);
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
		LocalRedis.getInstance().updateSuperWeaponDataByKey("startTime", startTime);
	}

	public int getTurnCount() {
		return turnCount;
	}

	public void setTurnCount(int turnCount) {
		this.turnCount = turnCount;
		LocalRedis.getInstance().updateSuperWeaponDataByKey("turnCount", turnCount);
	}
	
	public void setSeasonTurn(int seasonTurn) {
		this.seasonTurn = seasonTurn;
		LocalRedis.getInstance().setSWSeasonTurn(seasonTurn);
	}
	
	public int getSeasonTurn() {
		return seasonTurn;
	}

	public IWeapon getWeapon(int pointId){
		return allWeapons.get(pointId);
	}
	
	
	public Map<Integer, IWeapon> getAllWeapons() {
		return allWeapons;
	}

	public Map<Integer, IWeapon> getAllWeapon(){
		return allWeapons;
	}
	
	/**
	 * 报名某个超级武器
	 * @return
	 */
	public boolean signUpWar(int pointId, String guildId){
		IWeapon signWeapon = getWeapon(pointId);
		
		signWeapon.addSignUp(guildId);
		
		int pos[] = GameUtil.splitXAndY(pointId);
		
		//报名成功后发送联盟邮件
		// 发送邮件---国王战开启全服邮件
		long warStartTime = getStartTime() + SuperWeaponConstCfg.getInstance().getWarfareTime();
		Object[] content = new Object[] {pos[0], pos[1], warStartTime};
		GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
                .setMailId(MailId.SUPER_WEAPON_SIGNUP)
                .addContents(content));
		
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player == null) {
				continue;
			}
			signWeapon.broadcastSingleSuperWeaponInfo(player);
		}
		
		ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_7_VALUE, guildId, warStartTime, pos[0], pos[1], String.valueOf(pointId));
		ScheduleService.getInstance().addSystemSchedule(schedule);
		return true;
	}
	
	/**
	 * 检测是否已经报名
	 * @param pointId
	 * @param guildId
	 * @return
	 */
	public boolean checkAlreadySignUp(int pointId, String guildId) {
		BitSet bitSet = new BitSet(2);
		//先获取先前的报名信息
		for (IWeapon weapon : allWeapons.values()) {
			int level = weapon.getWeaponLevel();
			if(weapon.checkSignUp(guildId)){
				bitSet.set(level);
			}
		}
		IWeapon thisWeapon = allWeapons.get(pointId);
		if(bitSet.get(thisWeapon.getWeaponLevel())){
			return true;
		}
		return false;
	}
	
	/**
	 * 检查是否报名过这个等级的超级武器
	 * @param selfLevel
	 * @param guildId
	 * @return
	 */
	public boolean checkSignUpThisLevel(int selfLevel, String guildId){
		//先获取先前的报名信息
		for (IWeapon weapon : allWeapons.values()) {
			int level = weapon.getWeaponLevel();
			if(selfLevel == level && weapon.checkSignUp(guildId)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取联盟控制的超级武器
	 * @param guildId
	 * @return
	 */
	public List<IWeapon> getGuildControlSuperWeapon(String guildId) {
		List<IWeapon> weapons = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return weapons;
		}
		for (IWeapon weapon : allWeapons.values()) {
			if (weapon.getStatus() != SuperWeaponPeriod.CONTROL_VALUE) {
				continue;
			}
			// 是否是和平期间控制中
			boolean paceControl = !HawkOSOperator.isEmptyString(weapon.getCommanderPlayerId()) && GuildService.getInstance().isPlayerInGuild(guildId, weapon.getCommanderPlayerId());
			if (paceControl) {
				weapons.add(weapon);
			}
		}
		return weapons;
	}

	/**
	 * 获取联盟占领的超级武器
	 * @param guildId
	 * @return
	 */
	public List<IWeapon> getGuildOccupySuperWeapon(String guildId) {
		List<IWeapon> weapons = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return weapons;
		}
		for (IWeapon weapon : allWeapons.values()) {
			// 是否是战争期间占领中
			boolean warfareOccupy = !HawkOSOperator.isEmptyString(weapon.getGuildId()) && weapon.getGuildId().equals(guildId);
			// 是否是和平期间控制中
			boolean paceControl = !HawkOSOperator.isEmptyString(weapon.getCommanderGuildId()) && weapon.getCommanderGuildId().equals(guildId);
			if (warfareOccupy || paceControl) {
				weapons.add(weapon);
			}
		}
		return weapons;
	}
	
	public boolean isOpenService() {
		return !SuperWeaponConstCfg.getInstance().isClosed();
	}

	public void setOpenService(boolean openService) {
		this.openService = openService;
		LocalRedis.getInstance().updateSuperWeaponDataByKey("openService", openService);
	}

	public boolean isInPrepareStatus() {
		return inPrepareStatus;
	}

	public void setInPrepareStatus(boolean inPrepareStatus) {
		this.inPrepareStatus = inPrepareStatus;
	}
	
	public List<Integer> getSuperWeaponPoints() {
		return AssembleDataManager.getInstance().getSuperWeaponPoints();
	}
	
	public boolean isSuperWeaponPoints(int x, int y) {
		return getSuperWeaponPoints().contains(GameUtil.combineXAndY(x, y));
	}
	
	public boolean isSuperWeaponPoints(int pointId) {
		return getSuperWeaponPoints().contains(pointId);
	}
	

	/**
	 * 获取特殊奖励最大轮询期数
	 * @return
	 */
	public int getSpecialAwardMaxRound() {
		int maxRound = 0;
		ConfigIterator<SuperWeaponSpecialAwardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperWeaponSpecialAwardCfg.class);
		while (configIterator.hasNext()) {
			SuperWeaponSpecialAwardCfg config = configIterator.next();
			if (config.getRound() <= maxRound) {
				continue;
			}
			maxRound = config.getRound();
		}
		return maxRound;
	}
	
	public boolean isSuperWeaponPriesdent(String playerId){
		if(status != SuperWeaponPeriod.WARFARE_VALUE && status != SuperWeaponPeriod.CONTROL_VALUE){
			return false;
		}
		for(IWeapon weapon : allWeapons.values()){
			if(weapon instanceof AbstractSuperWeapon){
				AbstractSuperWeapon superWeapon = (AbstractSuperWeapon)weapon;
				if(superWeapon.getCommander() != null && superWeapon.getCommanderPlayerId().trim().equals(playerId)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 发送超级武器驻军信息
	 * @param player
	 * @param weapon
	 */
	public void sendSuperWeaponQuarterInfo(Player player, IWeapon weapon) {
		SuperWeaponQuarterInfoResp.Builder builder = SuperWeaponQuarterInfoResp.newBuilder();
		if (!player.hasGuild() || !player.getGuildId().equals(weapon.getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
			return;
		}
		
		BlockingDeque<String> marchs = WorldMarchService.getInstance().getSuperWeaponMarchs(weapon.getPointId());
		for (String marchId : marchs) {
			builder.addQuarterMarch(getSuperWeaponQuarterMarch(marchId));
		}
		
		String leaderId = WorldMarchService.getInstance().getSuperWeaponLeaderMarchId(weapon.getPointId());
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderId);
		int maxMassJoinSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getPlayer());
		builder.setMassSoldierNum(maxMassJoinSoldierNum);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_QUARTER_INFO_S, builder));
	}
	
	public SuperWeaponQuarterMarch.Builder getSuperWeaponQuarterMarch(String marchId) {
		SuperWeaponQuarterMarch.Builder builder = SuperWeaponQuarterMarch.newBuilder();
		
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		String playerId = march.getPlayerId();
		Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
		
		builder.setPlayerId(snapshot.getId());
		builder.setName(snapshot.getName());
		builder.setIcon(snapshot.getIcon());
		builder.setPfIcon(snapshot.getPfIcon());
		builder.setGuildTag(snapshot.getGuildTag());
		builder.setMarchId(marchId);
		
		List<ArmyInfo> armys = march.getMarchEntity().getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmy(army.toArmySoldierPB(snapshot).build());
		}
		for (PlayerHero hero : march.getHeros()) {
			builder.addHeroId(hero.getCfgId());
		}
		List<PlayerHero> heroList = snapshot.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : heroList) {
			builder.addHero(hero.toPBobj());
		}
		SuperSoldier ssoldier = snapshot.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId()).orElse(null);
		if(Objects.nonNull(ssoldier)){
			builder.setSsoldier(ssoldier.toPBobj());
		}
		return builder;
	}
	
	/**
	 * 添加赛季积分
	 */
	public void addSeasonScore(String guildId) {
		int afterScore = 0;
		
		Integer score = seasonRank.get(guildId);
		if (score == null) {
			afterScore = 1;
		} else {
			afterScore = score + 1;
		}
		
		seasonRank.put(guildId, afterScore);
		LocalRedis.getInstance().setSWSeasonScore(guildId, String.valueOf(afterScore));
		
	}
	
	/**
	 * 根据积分获取段位
	 */
	public int getSWSectionScoreRank(int score) {
		int rank = Integer.MAX_VALUE;
		ConfigIterator<SuperWeaponSection> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperWeaponSection.class);
		while (configIterator.hasNext()) {
			SuperWeaponSection config = configIterator.next();
			if (score >= config.getScoreLow() && config.getId() < rank) {
				rank = config.getId();
			}
		}
		
		if (rank == Integer.MAX_VALUE) {
			rank = 0;
		}
		return rank;
	}
	
	/**
	 * 推送战区排行榜
	 */
	public void pushSeasonRank(Player player) {
		
		SWSeasonRankResp.Builder builder = SWSeasonRankResp.newBuilder();
		if (player.hasGuild()) {
			SWSeasonRankInfo.Builder own = SWSeasonRankInfo.newBuilder();
			String guildTag = GuildService.getInstance().getGuildTag(player.getGuildId());
			String guildName = GuildService.getInstance().getGuildName(player.getGuildId());
			Integer score = seasonRank.get(player.getGuildId());
			own.setGuildTag(HawkOSOperator.isEmptyString(guildTag) ? "" : guildTag);
			own.setGuildName(HawkOSOperator.isEmptyString(guildName) ? "" : guildName);
			own.setScore(score == null ? 0 : score);
			own.setSection(score == null ? 0 : getSWSectionScoreRank(score));
			builder.setOwn(own);
		}
		
		if (seasonRank.isEmpty()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_SEASON_RANK_S, builder));
			return;
		}
		
		for (Entry<String, Integer> rank : seasonRank.entrySet()) {
			String guildId = rank.getKey();
			if (HawkOSOperator.isEmptyString(guildId)) {
				continue;
			}
			
			String guildTag = GuildService.getInstance().getGuildTag(guildId);
			if (HawkOSOperator.isEmptyString(guildTag)) {
				continue;
			}
			
			String guildName = GuildService.getInstance().getGuildName(guildId);
			if (HawkOSOperator.isEmptyString(guildName)) {
				continue;
			}
			
			SWSeasonRankInfo.Builder info = SWSeasonRankInfo.newBuilder();
			info.setGuildTag(guildTag);
			info.setGuildName(guildName);
			info.setScore(rank.getValue());
			info.setSection(getSWSectionScoreRank(rank.getValue()));
			builder.addInfo(info);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_SEASON_RANK_S, builder));
	}
	
	/**
	 * 发赛季积分奖励
	 */
	public void sendSWSectionAward() {
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				for (Entry<String, Integer> rank : seasonRank.entrySet()) {
					
					Collection<String> guildMembers = GuildService.getInstance().getGuildMembers(rank.getKey());
					if (guildMembers.isEmpty()) {
						continue;
					}
					
					// 分段
					int section = getSWSectionScoreRank(rank.getValue());
					SuperWeaponSectionAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSectionAwardCfg.class, section);
					if (cfg == null) {
						continue;
					}
					
					MailParames.Builder paramesBuilder = MailParames.newBuilder()
							.setMailId(MailId.SUPER_WEAPON_SEASON_AWARD)
							.setRewards(cfg.getRewardItem())
							.addContents(section)
							.setAwardStatus(MailRewardStatus.NOT_GET);
					
					GuildMailService.getInstance().sendGuildMail(rank.getKey(), paramesBuilder);
				}
				return null;
			}
		});
	}
}
