package com.hawk.game.superweapon.weapon;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.annotation.SerializeField;
import org.hawk.app.HawkApp;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.HawkNetworkManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.OccupySuperWeaponEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.SuperWeaponAwardCfg;
import com.hawk.game.config.SuperWeaponCfg;
import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.config.SuperWeaponSpecialAwardCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.PlayerLockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerLockImageMsg.LockParam;
import com.hawk.game.msg.PlayerLockImageMsg.LockType;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponInfo;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventControlSuperWeapon;
import com.hawk.game.superweapon.SuperWeaponRecord;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.model.Commander;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.SuperWeaponAwardType;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 超级武器抽象类
 * @author zhenyu.shang
 * @since 2018年4月23日
 */
public class AbstractSuperWeapon implements IWeapon {
	
	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 超级武器的坐标 */
	private int pointId;

	/** 超级武器状态 */
	@SerializeField
	private int status;
	
	/** 当前占领联盟 */
	@SerializeField
	private String guildId;
	
	/** 当前占领司令 */
	@SerializeField
	private Commander commander;
	
	/** 加时阶段占领联盟 */
	@SerializeField
	private String overTimeGuildId;
	
	/** 下阶段开始时间 */
	@SerializeField
	private long startTime;
	
	/** 结束时间 */
	@SerializeField
	private long endTime;
	
	/** 占领时间 */
	@SerializeField
	private long occupyTime;
	
	@SerializeField
	private long lastTickTime;
	
	@SerializeField
	private boolean hasNpc;
	
	/** 报名数据 */
	@SerializeField
	private ConcurrentHashSet<String> signUpSet;
	
	/** 自动报名数据 */
	@SerializeField
	private ConcurrentHashSet<String> autoSignUpSet;
	
	/** 攻占的历史联盟 */
	@SerializeField
	private ConcurrentHashSet<String> occupyHistorySet;
	
	private int x;
	
	private int y;
	
	public AbstractSuperWeapon(int pointId) {
		this.pointId = pointId;
		this.signUpSet = new ConcurrentHashSet<String>();
		this.autoSignUpSet = new ConcurrentHashSet<String>();
		this.occupyHistorySet = new ConcurrentHashSet<String>();
		int[] pos = GameUtil.splitXAndY(pointId);
		this.x = pos[0];
		this.y = pos[1];
		this.hasNpc = true;
		this.status = SuperWeaponPeriod.PEACE_VALUE;
	}
	
	@Override
	public void init() throws Exception{
		//初始化加载各个参数值
		Field[] fields = AbstractSuperWeapon.class.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			SerializeField anno = field.getAnnotation(SerializeField.class);
			if(anno != null){
				String key = getSuperWeaponPrefix() + field.getName();
				Object obj = LocalRedis.getInstance().getAbstractSuperWeaponDataByKey(pointId, key, field.getType());
				if(obj != null){
					field.set(this, obj);
				}
			}
		}
		
		//初始化完之后，如果状态为空，则是第一次启服
		if(status == SuperWeaponPeriod.WARFARE_VALUE ){
			//一般情况下，是不会在战争状态和加时状态停服的。如果启服后, 当前是战争状态, 或者是加时状态, 重置开始时间和占领时间
			long offset = HawkTime.getMillisecond() - getLastTickTime();
			setStartTime(startTime + offset);
			if(occupyTime > 0){
				setOccupyTime(occupyTime + offset);
			}
		}
		
		SuperWeaponService.logger.info("super weapon {} init over, status : {} , next startTime : {}", getPointId(), getStatus(), new Date(getStartTime()));		
	}
	
	@Override
	public void tick(long currTime) {
		
		switch (getStatus()) {
		
		// 战争阶段
		case SuperWeaponPeriod.WARFARE_VALUE:
			doWarfarePeriod(currTime);
			break;
			
		default:
			break;
		}
		
	}

	/**
	 * 战争阶段
	 * @param currTime
	 */
	private synchronized void doWarfarePeriod(long currTime) {
		// 设置上次tick时间
		setLastTickTime(currTime);
		
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		
		// 结束时间
		long endTime = getStartTime() + constCfg.getWarfareTime() * 1000L;;
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			endTime = getStartTime() + GameConstCfg.getInstance().getSuperBarrackWarfreTime();
		}
		
		// 战争阶段结束
		if (currTime > endTime) {
			noCommanderOver(currTime, constCfg, SuperWeaponPeriod.WARFARE_VALUE);
			return;
		}
		
		// 无人占领
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		// 占领时间
		long occupyTime = constCfg.getOccupationTime() * 1000L;;
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			occupyTime = GameConstCfg.getInstance().getSuperBarrackOccupyTime();
		}
		
		// 占领足够时长
		if (currTime - getOccupyTime() >= occupyTime && !HawkOSOperator.isEmptyString(guildId)) {
			superWeaponWinOver(currTime, constCfg);
			doWarfareToControl();
		}
	}
	
	/**
	 * 没有国王(无人占领/报名)结束
	 */
	public void noCommanderOver(long currTime, SuperWeaponConstCfg constCfg, int status) {
		// 上一届国王
		String lastPresidentId = this.commander != null ? this.commander.getPlayerId() : null;
		
		// 设置本届国王为null
		this.commander = null;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "commander", null);
		
		// 清除相关记录
		setGuildId(null);
		setOverTimeGuildId(null);
		setOccupyTime(0);
		setEndTime(currTime);
		setStatus(SuperWeaponPeriod.PEACE_VALUE);

		// 添加国王记录
		superWeaponAddCommander(lastPresidentId, "");
		
		//通知超级武器战斗结束
		if(!this.signUpSet.isEmpty() || !this.autoSignUpSet.isEmpty()){
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_WAR_ENDED_NO_WIN, null, x, y);
		}
		
		// 通知阶段改变
		notifySuperWeaponPeriodChanged(status, SuperWeaponPeriod.PEACE_VALUE);
		
		//设置下次开启时间
		long nextStartTime = HawkTime.getNextTimeDayOfWeek(getEndTime(), constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()) - constCfg.getSignUpTime();
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			nextStartTime = getEndTime() + 300 * 1000L;
		}
		setStartTime(nextStartTime);
		
		SuperWeaponService.logger.info("super weapon {} is over, there is no win in this Tenure", pointId);
	}

	
	/**
	 * 战争阶段到控制阶段数据处理
	 */
	@Override
	public void doWarfareToControl() {
		if (commander == null || HawkOSOperator.isEmptyString(commander.getPlayerGuildId())) {
			return;
		}
		
		// 刷新作用号
		for (String playerId : GuildService.getInstance().getGuildMembers(commander.getPlayerGuildId())) {
			
			ActivityManager.getInstance().postEvent(new OccupySuperWeaponEvent(playerId, true));
			
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player == null || !player.isActiveOnline()) {
				continue;
			}
			player.getEffect().resetEffectSuperWeapon(player);
		}
		
		WorldMarchService.getInstance().dissolveAllSuperWeaponQuarteredMarchs(pointId);
		
		Player player = GlobalData.getInstance().makesurePlayer(commander.getPlayerId());
		MissionManager.getInstance().postMsg(player, new EventControlSuperWeapon(pointId));
		
		// 添加赛季积分
		SuperWeaponService.getInstance().addSeasonScore(commander.getPlayerGuildId());
	}
	
	/**
	 * 控制阶段到和平阶段数据处理
	 */
	@Override
	public void doControlToPace() {
		// 阶段切换通知
		notifySuperWeaponPeriodChanged(SuperWeaponPeriod.CONTROL_VALUE, SuperWeaponPeriod.PEACE_VALUE);
		
		this.occupyHistorySet.clear();
		this.signUpSet.clear();
		this.autoSignUpSet.clear();
		this.guildId = null;
		this.overTimeGuildId = null;
		this.hasNpc = true;
		this.status = SuperWeaponPeriod.PEACE_VALUE;
		
		LocalRedis.getInstance().clearAbstractSWData(pointId);
		
		// 添加自动报名
		if (commander != null && !HawkOSOperator.isEmptyString(commander.getPlayerGuildId())) {
			int turnMax = SuperWeaponConstCfg.getInstance().getSeasonPeriodNum();
			int seasonTurn = SuperWeaponService.getInstance().getSeasonTurn();
			if (seasonTurn % turnMax != 0) {
				addAutoSignUp(commander.getPlayerGuildId());
			}
		}
		
		if(commander != null){
			Player lastPresident = GlobalData.getInstance().makesurePlayer(commander.getPlayerId());
			HawkTaskManager.getInstance().postMsg(lastPresident.getXid(), PlayerLockImageMsg.valueOf(LockType.PLAYERSTAT, LockParam.NO_ZHANQUSILING));
		}
	}
	
	/**
	 * 和平阶段到报名阶段数据处理
	 */
	public void doPaceToSignUp() {
	
	}
	
	/**
	 * 有占领者情况下结束战斗
	 * @param currTime
	 * @param constCfg
	 */
	private void superWeaponWinOver(long currTime, SuperWeaponConstCfg constCfg) {
		
		// 获取获胜联盟信息
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
		
		Player leader = WorldMarchService.getInstance().getSuperWeaponLeader(pointId);
		if (leader == null) {
			leader = GlobalData.getInstance().makesurePlayer(guildInfo.getLeaderId());
		}
		
		// 上届总司令Id
		String lastPresidentId = commander == null ? null : commander.getPlayerId();
		
		// 上届没有司令
		if (commander == null) {
			commander = new Commander();
		}
		
		// 设置任职时间
		if (HawkOSOperator.isEmptyString(lastPresidentId) || !leader.getId().equals(lastPresidentId)) {
			commander.setTenure(currTime);
		}
		
		commander.setPlayerId(leader.getId());
		commander.setPlayerName(leader.getName());
		commander.setPlayerGuildId(leader.getGuildId());
		commander.setPlayerGuildName(guildInfo.getName());
		commander.setTenureCount(SuperWeaponService.getInstance().getTurnCount());
		commander.setIcon(leader.getIcon());
		commander.setPfIcon(leader.getPfIcon());
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "commander", commander);
		
		// 跑马灯
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_OCCUPY_SUCC, null, x, y, guildInfo.getTag(), leader.getName());
		
		//发放全员邮件
		int pos[] = GameUtil.splitXAndY(pointId);
		GuildMailService.getInstance().sendGuildMail(getGuildId(), MailParames.newBuilder()
                .setMailId(MailId.SUPER_WEAPON_HAS_CONTROL)
                .addSubTitles(pos[0], pos[1])
                .addContents(pos[0], pos[1]));
		
		
		// 清理攻击状态
		setOccupyTime(0);
		setEndTime(currTime);
		setStatus(SuperWeaponPeriod.CONTROL_VALUE);
		
		//设置下次开启时间
		long startTime = HawkTime.getNextTimeDayOfWeek(getEndTime(), constCfg.getInitday(), constCfg.getInitTime(), constCfg.getInitMinute()) - constCfg.getSignUpTime();
		if(GsConfig.getInstance().isRobotMode() && GsConfig.getInstance().isDebug()){
			startTime = getEndTime() + 300 * 1000L;
		}
		setStartTime(startTime);
		
		// 通知国王改变
		superWeaponAddCommander(lastPresidentId, commander.getPlayerId());
		
		//解锁头像框
		if(lastPresidentId == null){
			Player player = GlobalData.getInstance().makesurePlayer(commander.getPlayerId());
			HawkTaskManager.getInstance().postMsg(player.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.ZHANQUSILING));
		}else{
			if(!lastPresidentId.equals(commander.getPlayerId())){
				//新国王解锁新头像，老国王上锁头像
				Player lastPresident = GlobalData.getInstance().makesurePlayer(lastPresidentId);
				Player curPresident = GlobalData.getInstance().makesurePlayer(commander.getPlayerId());
				if (lastPresident != null) {
					HawkTaskManager.getInstance().postMsg(lastPresident.getXid(), PlayerLockImageMsg.valueOf(LockType.PLAYERSTAT, LockParam.NO_ZHANQUSILING));
				}
				HawkTaskManager.getInstance().postMsg(curPresident.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.ZHANQUSILING));
			}
		}
		
		// 时期切换
		notifySuperWeaponPeriodChanged(SuperWeaponPeriod.WARFARE_VALUE, SuperWeaponPeriod.CONTROL_VALUE);
		
		// 发奖
		sendControlAward();
		
		SuperWeaponService.logger.info("super weapon {} is over, the win guild is {}, the TenureCount is {}", pointId, guildInfo.getId());
	}

	/**
	 * 超级武器状态发生变更时通知
	 * @param lastPeriod
	 * @param currentPeriod
	 */
	protected void notifySuperWeaponPeriodChanged(int lastPeriod, int currPeriod){
		try {
			// 切换为和平时期所有驻军回家
			if (currPeriod == SuperWeaponPeriod.PEACE_VALUE) {
				WorldMarchService.getInstance().dissolveAllSuperWeaponQuarteredMarchs(pointId);
				SuperWeaponService.logger.info("change period to peace dissolve all marchs");
			}
			
			// 广播超级武器状态信息
			broadcastSingleSuperWeaponInfo(null);
			
			int[] pos = GameUtil.splitXAndY(pointId);
			SuperWeaponService.logger.info("super weapon ({}, {}) period changed: old status : {}, new status : {}, currentGuild : {}", pos[0], pos[1], lastPeriod, currPeriod, guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 广播这个超级武器的信息
	 * @param player
	 */
	@Override
	public void broadcastSingleSuperWeaponInfo(Player player){
		try {
			SuperWeaponInfo.Builder infoBuilder = genSuperWeaponInfoBuilder();
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SUPER_WEAPON_SINGLE_INFO_S, infoBuilder);
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
	 * 总司令上任
	 * @param lastPresidentId
	 * @param currPresidentId
	 */
	public void superWeaponAddCommander(String lastPresidentId, String currPresidentId){
		try {
			SuperWeaponService.logger.info("president player changed: lastPresidentId：{} , currPresidentId：{}", lastPresidentId, currPresidentId);
			
			// 总司令上任后的操作
			if (!HawkOSOperator.isEmptyString(currPresidentId)) {
				SuperWeaponRecord.getInstance().addSuperWeaponElectedRecord(currPresidentId, pointId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取超级武器国王ID
	 * @return
	 */
	@Override
	public String getCommanderPlayerId(){
		if(commander != null){
			return commander.getPlayerId();
		}
		return null;
	}
	
	/**
	 * 获取超级武器国王guildId
	 * @return
	 */
	@Override
	public String getCommanderGuildId(){
		if(commander != null){
			return commander.getPlayerGuildId();
		}
		return null;
	}
	
	/**
	 * 生成超级武器信息的协议对象
	 * 
	 * @return
	 */
	public SuperWeaponInfo.Builder genSuperWeaponInfoBuilder() {
		try {
			SuperWeaponInfo.Builder infoBuilder = SuperWeaponInfo.newBuilder();
			infoBuilder.setPeriodType(getStatus());
			infoBuilder.setTurnCount(SuperWeaponService.getInstance().getTurnCount());
			infoBuilder.setPeriodEndTime(getCurrentPeriodEndTime());
			infoBuilder.setWarStartTime(getStartTime());
			infoBuilder.setOccupyTime(getOccupyTime());
			infoBuilder.setHasSignUp(!signUpSet.isEmpty());
			infoBuilder.setHasAutoSignUp(!autoSignUpSet.isEmpty());
			
			int[] pos = GameUtil.splitXAndY(pointId);
			infoBuilder.setX(pos[0]);
			infoBuilder.setY(pos[1]);
			//  国王信息
			if (commander != null) {
				infoBuilder.setCommanderId(commander.getPlayerId());
				infoBuilder.setCommanderName(commander.getPlayerName());
				infoBuilder.setCommanderIcon(commander.getIcon());
				infoBuilder.setTenureTime(commander.getTenure());
				if (!HawkOSOperator.isEmptyString(commander.getPfIcon())) {
					infoBuilder.setCommanderPfIcon(commander.getPfIcon());
				}
				
				// 联盟
				String guildId = commander.getPlayerGuildId();
				infoBuilder.setCommanderGuildId(guildId);
				infoBuilder.setCommanderGuildName(GuildService.getInstance().getGuildName(guildId));
				infoBuilder.setCommanderGuildTag(GuildService.getInstance().getGuildTag(guildId));
				infoBuilder.setCommanderGuildFlagId(GuildService.getInstance().getGuildFlag(guildId));
			}

			String guildId = getGuildId();
			if (!HawkOSOperator.isEmptyString(guildId)) {
				infoBuilder.setLeaderGuildId(guildId);
				infoBuilder.setLeaderGuildTag(GuildService.getInstance().getGuildTag(guildId));
				infoBuilder.setLeaderGuildName(GuildService.getInstance().getGuildName(guildId));
				infoBuilder.setLeaderGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
				// 添加队长信息
				Player leader = WorldMarchService.getInstance().getSuperWeaponLeader(pointId);
				
				// 箭塔没有leader的时候， 取盟主的信息
				if (leader == null) {
					String guildLeaderId = GuildService.getInstance().getGuildLeaderId(guildId);
					leader = GlobalData.getInstance().makesurePlayer(guildLeaderId);
				}
				
				infoBuilder.setLeaderId(leader.getId());
				infoBuilder.setLeaderName(leader.getName());
				infoBuilder.setLeaderIcon(leader.getIcon());
				infoBuilder.setLeaderPfIcon(leader.getPfIcon());
			}
			infoBuilder.setHasNpc(hasNpc);
			return infoBuilder;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	
	/**
	 * 获取当前阶段的结束时间
	 * @return
	 */
	public long getCurrentPeriodEndTime(){
		SuperWeaponConstCfg constCfg = SuperWeaponConstCfg.getInstance();
		if(getStatus() == SuperWeaponPeriod.WARFARE_VALUE){//战争状态
			if(getGuildId() == null){
				return getStartTime() + constCfg.getWarfareTime() * 1000L;
			} else {
				return getOccupyTime() + (constCfg.getOccupationTime() * 1000L);
			}
		} else if(getStatus() == SuperWeaponPeriod.CONTROL_VALUE){//加时状态
			int mainStatus = SuperWeaponService.getInstance().getStatus();
			if (mainStatus == SuperWeaponPeriod.CONTROL_VALUE) {
				return SuperWeaponService.getInstance().getStartTime();
			} else {
				return SuperWeaponService.getInstance().getStartTime() + (constCfg.getControlTime() * 1000L);
			}
		}
		return getStartTime();
	}
	
	/**
	 * 占领超级武器
	 * @param guildId
	 * @param pointId
	 */
	public synchronized void changeOccuption(String leaderName, String afterGuildId) {
		String beforeGuildId = getGuildId();
		//联盟首次攻占，跑马灯显示
		if(afterGuildId != null && occupyHistorySet.isEmpty()){
			GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(afterGuildId);
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST,
					Const.NoticeCfgId.SUPERWEAPON_CHANGE, null, x, y, guildInfo.getTag(), leaderName);
		}
		if (HawkOSOperator.isEmptyString(afterGuildId)) {
			setOccupyTime(0);
		} else if(!afterGuildId.equals(beforeGuildId)){
			setOccupyTime(HawkTime.getMillisecond());
		}
		
		setGuildId(afterGuildId);
		
		broadcastSingleSuperWeaponInfo(null);
		
		int pos[] = GameUtil.splitXAndY(pointId);
		//发送占领者邮件
		if(!HawkOSOperator.isEmptyString(afterGuildId)){
			Object[] content = new Object[] {pos[0], pos[1]};
			GuildMailService.getInstance().sendGuildMail(afterGuildId, MailParames.newBuilder()
	                .setMailId(MailId.SUPER_WEAPON_OCCUPY_SUCC)
	                .addContents(content));
		}
		logger.info("change super weapon occuptaion, beforeGuildId:{}, afterGuildId:{}, occupyTime:{}", beforeGuildId, afterGuildId, getOccupyTime());
	}
	
	/**
	 * 援助超级武器处理
	 * @param massMarchList
	 */
	public void doSuperWeaponAssistance(IWorldMarch march) {
		
	}
	
	/**
	 * 超级武器战斗胜利处理
	 * @param march
	 */
	public void doSuperWeaponAttackWin(Player atkLeader, Player defLeader) {
		int pos[] = GameUtil.splitXAndY(pointId);
		String atkGuildId = atkLeader.getGuildId();
		
		changeOccuption(atkLeader.getName(), atkLeader.getGuildId());
		
		if(defLeader != null){
			String defGuildId = defLeader.getGuildId();
			//发送被占领邮件
			if(!HawkOSOperator.isEmptyString(defGuildId) && !HawkOSOperator.isEmptyString(atkGuildId)){
				GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(atkGuildId);
				Object[] content = new Object[] {pos[0], pos[1], guildInfo.getTag(), guildInfo.getLeaderName()};
				GuildMailService.getInstance().sendGuildMail(defGuildId, MailParames.newBuilder()
		                .setMailId(MailId.SUPER_WEAPON_OCCUPY)
		                .addContents(content));
			}
		}
	}
	
	/**
	 * 超级武器战斗失败处理
	 * @param march
	 */
	public void doSuperWeaponAttackLose(Player atkLeader, Player defLeader) {
		
	}
	
	@Override
	public int getPointId() {
		return pointId;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "status", status);
	}

	@Override
	public String getGuildId() {
		return guildId;
	}

	@Override
	public void setGuildId(String guildId) {
		this.guildId = guildId;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "guildId", guildId);
	}
	
	public String getOverTimeGuildId() {
		return overTimeGuildId;
	}

	public void setOverTimeGuildId(String overTimeGuildId) {
		this.overTimeGuildId = overTimeGuildId;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "overTimeGuildId", overTimeGuildId);
	}

	public void setHasNpc(boolean hasNpc) {
		this.hasNpc = hasNpc;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "hasNpc", hasNpc);
	}
	
	public long getStartTime() {
		return startTime;
	}

	@Override
	public void setStartTime(long startTime) {
		this.startTime = startTime;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "startTime", startTime);
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "endTime", endTime);
	}

	public long getOccupyTime() {
		return occupyTime;
	}

	@Override
	public void setOccupyTime(long occupyTime) {
		this.occupyTime = occupyTime;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "occupyTime", occupyTime);
	}

	public long getLastTickTime() {
		return lastTickTime;
	}

	public void setLastTickTime(long lastTickTime) {
		this.lastTickTime = lastTickTime;
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "lastTickTime", lastTickTime);
	}
	
	@Override
	public boolean checkSignUp(String guildId){
		return this.signUpSet.contains(guildId) || this.autoSignUpSet.contains(guildId);
	}
	
	@Override
	public boolean checkAutoSignUp(String guildId){
		return this.autoSignUpSet.contains(guildId);
	}
	
	@Override
	public void addSignUp(String guildId){
		this.signUpSet.add(guildId);
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "signUpSet", signUpSet);
	}
	
	@Override
	public void removeSignUp(String guildId) {
		this.signUpSet.remove(guildId);
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "signUpSet", signUpSet);
	}
	
	@Override
	public void delSignUp(String guildId){
		this.signUpSet.remove(guildId);
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "signUpSet", signUpSet);
	}
	
	@Override
	public boolean hasSignUpGuild() {
		return !this.signUpSet.isEmpty() || !this.autoSignUpSet.isEmpty();
	}
	
	/**
	 * 添加自动报名
	 * @param guildId
	 */
	private void addAutoSignUp(String guildId) {
		this.autoSignUpSet.add(guildId);
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "autoSignUpSet", autoSignUpSet);
	}
	
	@Override
	public boolean checkOccupyHistory(String guildId) {
		return this.occupyHistorySet.contains(guildId);
	}
	
	@Override
	public void addOccupuHistory(String guildId) {
		// 拦截下，避免多做redis操作
		if (this.occupyHistorySet.contains(guildId)) {
			return;
		}
		this.occupyHistorySet.add(guildId);
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "occupyHistory", occupyHistorySet);
	}
	
	@Override
	public void delOccupuHistory(String guildId){
		if (!this.occupyHistorySet.contains(guildId)) {
			return;
		}
		this.occupyHistorySet.remove(guildId);
		LocalRedis.getInstance().updateAbstractSuperWeaponDataByKey(pointId, getSuperWeaponPrefix() + "occupyHistory", occupyHistorySet);
	}
	
	@Override
	public boolean canAttack(String guildId) {
		return this.signUpSet.contains(guildId) || this.autoSignUpSet.contains(guildId);
	}

	public Commander getCommander() {
		return commander;
	}

	public String getSuperWeaponPrefix(){
		return "SW-" + pointId + "-";
	}
	
	@Override
	public void sendWillStartMail() {
		Set<String> sendSet = new HashSet<>();
		sendSet.addAll(signUpSet);
		sendSet.addAll(autoSignUpSet);
		
		for (String guildId : sendSet) {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					int pos[] = GameUtil.splitXAndY(pointId);
					Object[] content = new Object[] {pos[0], pos[1]};
					GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
			                .setMailId(MailId.SUPER_WEAPON_WAR_START)
			                .addContents(content));
					return null;
				}
			});
		}
	}
	

	@Override
	public void sendWillRefreshMail() {
		if(commander != null){
			String guildId = commander.getPlayerGuildId();
			long startTime = SuperWeaponService.getInstance().getStartTime();
			Object[] content = new Object[] {startTime};
			GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
	                .setMailId(MailId.SUPER_WEAPON_REFRESH)
	                .addContents(content));
		}
	}
	
	@Override
	public void printSignUpLog() {
		int[] pos = GameUtil.splitXAndY(pointId);
		StringBuffer sb = new StringBuffer();
		sb.append("point x:").append(pos[0]).append(", y:").append(pos[1]);
		sb.append("\\r\\n");
		if(signUpSet.isEmpty()){
			sb.append("sign up empty");
		} else {
			for (String guildId : signUpSet) {
				sb.append("guildId:").append(guildId).append(", guildName:").append(GuildService.getInstance().getGuildName(guildId));
				sb.append("\\r\\n");
			}
		}
		SuperWeaponService.logger.info(sb.toString());
	}
	
	/**
	 * 发送占领奖励 (超级武器第二版移除，不发占领奖励)
	 */
	public void sendOccupyTickAward() {
		SuperWeaponCfg superWeaponCfg = AssembleDataManager.getInstance().getSuperWeaponCfg(pointId);
		// 联盟成员奖励
		AwardItems guildAwards = AwardItems.valueOf();
		List<SuperWeaponAwardCfg> guildAwardCfgs = AssembleDataManager.getInstance().getSuperWeaponAwards(pointId, SuperWeaponAwardType.OCCUPY_GUILD_MEMEBER_AWARD);
		for (SuperWeaponAwardCfg cfg : guildAwardCfgs) {
			for (int i = 0; i < cfg.getTotalNumber(); i++) {
				guildAwards.addItemInfos(cfg.getRewardItem());
			}
		}
		int[] pos = GameUtil.splitXAndY(pointId);
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.addContents(superWeaponCfg.getId(), pos[0],pos[1])
					.setMailId(MailId.SUPER_WEAPON_OCCUPY_GUILD_AWARD)
					.setRewards(guildAwards.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
		
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		// 礼包
		List<SuperWeaponAwardCfg> leaderSendAwardCfgs = AssembleDataManager.getInstance().getSuperWeaponAwards(pointId, SuperWeaponAwardType.OCCUPY_LEADER_SEND_AWARD);
		for (SuperWeaponAwardCfg cfg : leaderSendAwardCfgs) {
			String superWeaponGiftInfo = LocalRedis.getInstance().getSuperWeaponGiftInfo(turnCount, pointId, guildId, cfg.getId());
			String afterInfo = null;
			if (HawkOSOperator.isEmptyString(superWeaponGiftInfo)) {
				int sendCount = 0;
				int totalCount = cfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			} else {
				String[] splitInfo = superWeaponGiftInfo.split("_");
				int sendCount = Integer.parseInt(splitInfo[0]);
				int totalCount = Integer.parseInt(splitInfo[1]) + cfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			}
			LocalRedis.getInstance().updateSuperWeaponGiftInfo(turnCount, pointId, guildId, cfg.getId(), afterInfo);
		}
	}
	
	public void sendControlAward() {
		SuperWeaponCfg superWeaponCfg = AssembleDataManager.getInstance().getSuperWeaponCfg(pointId);
		// 联盟成员奖励
		AwardItems guildAwards = AwardItems.valueOf();
		List<SuperWeaponAwardCfg> guildAwardCfgs = AssembleDataManager.getInstance().getSuperWeaponAwards(pointId, SuperWeaponAwardType.CONTROL_GUILD_MEMEBER_AWARD);
		for (SuperWeaponAwardCfg cfg : guildAwardCfgs) {
			for (int i = 0; i < cfg.getTotalNumber(); i++) {
				guildAwards.addItemInfos(cfg.getRewardItem());
			}
		}
		int[] pos = GameUtil.splitXAndY(pointId);
		for (String playerId : GuildService.getInstance().getGuildMembers(commander.getPlayerGuildId())) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(playerId)
					.addContents(superWeaponCfg.getId(), pos[0],pos[1])
					.setMailId(MailId.SUPER_WEAPON_CONTROL_GUILD_AWARD)
					.setRewards(guildAwards.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
		
		int turnCount = SuperWeaponService.getInstance().getTurnCount();
		// 礼包
		SuperWeaponSpecialAwardCfg specialAwardCfg = getSuperWeaponSpecialAward();
		if (specialAwardCfg != null) {
			String superWeaponGiftInfo = LocalRedis.getInstance().getSuperWeaponGiftInfo(turnCount, pointId, commander.getPlayerGuildId(), specialAwardCfg.getId());
			String afterInfo = null;
			if (HawkOSOperator.isEmptyString(superWeaponGiftInfo)) {
				int sendCount = 0;
				int totalCount = specialAwardCfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			} else {
				String[] splitInfo = superWeaponGiftInfo.split("_");
				int sendCount = Integer.parseInt(splitInfo[0]);
				int totalCount = Integer.parseInt(splitInfo[1]) + specialAwardCfg.getTotalNumber();
				afterInfo = String.valueOf(sendCount) + "_" + totalCount;
			}
			LocalRedis.getInstance().updateSuperWeaponGiftInfo(turnCount, pointId, commander.getPlayerGuildId(), specialAwardCfg.getId(), afterInfo);
		}
	}
	
	/**
	 * 获取特殊奖励(控制颁发奖励)
	 * @return
	 */
	private SuperWeaponSpecialAwardCfg getSuperWeaponSpecialAward() {
		// 当前期数
		int currentRound = SuperWeaponService.getInstance().getTurnCount();
		// 最大期数
		int maxConfigRound = getSpecialAwardMaxRound();
		// 查找期数
		int findRound = currentRound <= maxConfigRound ? currentRound : (currentRound % maxConfigRound);
		if (findRound == 0) {
			findRound = maxConfigRound;
		}
		
		int pos[] = GameUtil.splitXAndY(pointId);
		
		ConfigIterator<SuperWeaponSpecialAwardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperWeaponSpecialAwardCfg.class);
		while (configIterator.hasNext()) {
			SuperWeaponSpecialAwardCfg config = configIterator.next();
			if (config.getX() != pos[0] || config.getY() != pos[1]) {
				continue;
			}
			if (config.getRound() != findRound) {
				continue;
			}
			return config;
		}
		
		return null;
	}
	
	/**
	 * 获取特殊奖励最大轮询期数
	 * @return
	 */
	private int getSpecialAwardMaxRound() {
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
	
	public boolean hasNpc() {
		return hasNpc;
	}
	
	public void fightNpcWin(String playerName, String guildId) {
		setHasNpc(false);
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.SUPERWEAPON_HAS_OCCUPY, null, x, y, guildInfo.getName(), playerName);
	}

	@Override
	public int getWeaponLevel() {
		return getWeaponCfg().getClassify();
	}

	@Override
	public SuperWeaponCfg getWeaponCfg() {
		return AssembleDataManager.getInstance().getSuperWeaponCfg(getPointId());
	}
	
	/**
	 * 遣返行军
	 */
	@Override
	public boolean repatriateMarch(Player player, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getSuperWeaponLeader(pointId);
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REPATRIATE_MARCH) {
			@Override
			public boolean onInvoke() {
				List<IWorldMarch> marchs = WorldMarchService.getInstance().getSuperWeaponStayMarchs(pointId);
				for (IWorldMarch iWorldMarch : marchs) {
					if (iWorldMarch.isReturnBackMarch()) {
						continue;
					}
					if (!iWorldMarch.getPlayerId().equals(targetPlayerId)) {
						continue;
					}
					WorldMarchService.logger.info("marchRepatriate, playerId:{}, tarPlayerId:{}, marchId:{}", player.getId(), targetPlayerId, iWorldMarch.getMarchId());
					WorldMarchService.getInstance().onPlayerNoneAction(iWorldMarch, HawkApp.getInstance().getCurrentTime());
				}
				SuperWeaponService.getInstance().sendSuperWeaponQuarterInfo(player, AbstractSuperWeapon.this);
				broadcastSingleSuperWeaponInfo(player);
				return true;
			}
		});

		return true;
	}
	
	/**
	 * 任命队长
	 * @param player
	 * @param targetPlayerId
	 * @return
	 */
	@Override
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild() || !player.getGuildId().equals(getGuildId())) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getSuperWeaponLeader(pointId);
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().changeSuperWeaponMarchLeader(pointId, targetPlayerId);
				SuperWeaponService.getInstance().sendSuperWeaponQuarterInfo(player, AbstractSuperWeapon.this);
				broadcastSingleSuperWeaponInfo(player);
				return true;
			}
		});
		
		return true;
	}
}
