package com.hawk.game.guild.manor.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GuildDragonAttackScoreEvent;
import com.hawk.activity.type.impl.guildDragonAttack.GuildDragonAttackActivity;
import com.hawk.activity.type.impl.guildDragonAttack.cfg.GuildDragonAttackKVCfg;
import com.hawk.activity.type.impl.guildDragonAttack.cfg.GuildDragonAttackMemberRewardCfg;
import com.hawk.activity.type.impl.guildDragonAttack.cfg.GuildDragonAttackTotalRewardCfg;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonTrapData;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.GuildDragonAttackDamage;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.GuildDragonTrapState;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.PBDamageRank;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.GuildManor.GuildDragonTrapBase;
import com.hawk.game.protocol.GuildManor.GuildManorList.Builder;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.serialize.string.SerializeHelper;

public class GuildDragonTrap extends AbstractGuildBuilding{
	
	public final static int RADIUS = 2;
	//期数
	private long termId;
	//状态 0 关闭   1开启  2 发奖
	private int trapState;
	//预约开启时间
	private long appointmentTime;
	//开启时间
	private long openTime;
	//开放冷却
	private long openTimeRecord;
	//是否发奖
	private boolean award;
	//总伤害
	private long damageTotal;
	//伤害列表
	private Map<String,GuildDragonAttackDamage> damages = new ConcurrentHashMap<>();

	
	
	public GuildDragonTrap(GuildBuildingEntity entity, TerritoryType buildType) {
		super(entity, buildType);
	}


	@Override
	public int getBuildingUpLimit() {
		return 0;
	}

	@Override
	public int getbuildLimtUp() {
		return 0;
	}
	
	@Override
	public boolean tryChangeBuildStat(int stat) {
		GuildBuildingStat toState = GuildBuildingStat.valueOf(stat);
		//巨龙陷阱没有建造过程，直接完成
		if(toState == GuildBuildingStat.UNCOMPELETE){
			boolean rlt = super.tryChangeBuildStat(GuildBuildingStat.UNCOMPELETE.getIndex());
			if(!rlt){
				HawkLog.logPrintln("GuildDragonTrap-tryChangeBuildStat-fail,UNCOMPELETE, guildId: {}, state: {}",this.getGuildId(),this.getbuildStat());
				return false;
			}
			rlt = super.tryChangeBuildStat(GuildBuildingStat.BUILDING.getIndex());
			if(!rlt){
				HawkLog.logPrintln("GuildDragonTrap-tryChangeBuildStat-fail,BUILDING, guildId: {}, state: {}",this.getGuildId(),this.getbuildStat());
				return false;
			}
			return super.tryChangeBuildStat(GuildBuildingStat.COMPELETE.getIndex());
		}
		return super.tryChangeBuildStat(stat);
	}
	
	
	@Override
	public void tick() {
		try {
			//检查建筑状态
			this.checkBuildState();
		} catch (Exception e) {
			HawkLog.logPrintln("GuildDragonTrap-tick-checkBuildState-ERR, guildId: {}, state: {}",
					this.getGuildId(),this.getbuildStat());
		}
		
		try {
			//检查世界点对活动的影响
			this.checkWorldPoint();
		} catch (Exception e) {
			HawkLog.logPrintln("GuildDragonTrap-tick-checkWorldPoint-ERR, guildId: {}, state: {}",
					this.getGuildId(),this.getbuildStat());
		}
		try {
			//检查活动状态
			this.checkFightState();
		} catch (Exception e) {
			HawkLog.logPrintln("GuildDragonTrap-tick-checkFightState-ERR, guildId: {}, state: {}",
					this.getGuildId(),this.getbuildStat());
			
		}
	}
	
	
	/**
	 * 操作建筑
	 * @param player
	 * @param action
	 * @param params
	 */
	public void guildDragonTrapOp(Player player,int action,String... params){
		switch (action) {
		case 1:
			int hp1 = Integer.parseInt(params[0]);
			this.openImmediately(player,hp1);
			break;
		case 2:
			long time = Long.parseLong(params[0]);
			int hp2 = Integer.parseInt(params[1]);
			this.openAppointmentTime(player,time,hp2);
			break;
		default:
			break;
		}
	}
	
	
	
	/**
	 * 检查建筑开放
	 */
	public void checkBuildState(){
		if(this.getBuildStat() != GuildBuildingStat.LOCKED){
			return;
		}
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		long curTime = HawkTime.getMillisecond();
		if(curTime <= serverOpenAm0 + cfg.getUnlockTime() * 1000){
			return;
		}
		
		boolean hasManor = GuildManorService.getInstance().hasManorComplete(this.getGuildId());
		if(!hasManor){
			return;
		}
		boolean canEnter = this.canEnterState(GuildBuildingStat.OPENED.getIndex());
		if(canEnter){
			this.tryEnterState(GuildBuildingStat.OPENED.getIndex());
		}
		HawkLog.logPrintln("GuildDragonTrap-checkBuildState-open-guildId-{}, state: {}",this.getGuildId(),this.getbuildStat());
	}
	
	/**
	 * 检查世界点
	 */
	public void checkWorldPoint(){
		//检查世界点
		WorldPoint wp = this.getPoint();
		if(Objects.nonNull(wp)){
			return;
		}
		//如果不是和平状态
		if(!this.inPeace()){
			return;
		}
		//没有预约开启
		if(this.appointmentTime <=0 &&
				this.openTime <= 0){
			return;
		}
		this.appointmentTime = 0;
		this.openTime = 0;
		this.getEntity().notifyUpdate();
		//同步数据
		this.syncDragonAttackData();
		HawkLog.logPrintln("GuildDragonTrap-checkWorldPoint-null-guildId-{}, state: {}",this.getGuildId(),this.getbuildStat());
	}
	
	
	
	
	/**
	 * 状态检查
	 */
	public void checkFightState(){
		GuildDragonTrapState curState = this.calState();
		if(this.trapState != curState.getVal()){
			GuildDragonTrapState stateEnum = GuildDragonTrapState.valueOf(this.trapState);
			this.trapState = stateEnum.getNext().getVal();
			this.getEntity().notifyUpdate();
			if(this.trapState == GuildDragonTrapState.FIGHT.getVal()){
				this.onOPenFight();
			}else if(this.trapState == GuildDragonTrapState.WAIT.getVal()){
				//更新世界点
				this.updateBuildWorldPoint();
				//同步活动数据
				this.syncDragonAttackData();
				LogUtil.logGuildDragonAttackEnd(this.getGuildId(), this.damageTotal, 0);
			}else if(this.trapState == GuildDragonTrapState.PEACE.getVal()){
				this.onAwardSend();
				this.clearData();
			}
			
			
		}
	}
	
	
	//立刻开始
	public void openImmediately(Player player,int hp){
		//5阶  盟主
		boolean checkGuildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.GUILD_DRAGON_ATK_OPEN);
		if (!checkGuildAuthority) {
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_AUTH_ERR, 0);
			return;
		}
		//检查建筑状态
		if(this.getBuildStat() != GuildBuildingStat.COMPELETE){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_UNLOCK, 0);
			return;
		}
		//检查世界点
		WorldPoint wp = this.getPoint();
		if(Objects.isNull(wp)){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_NO_POINT, 0);
			return;
		}
		//判断世界点类型
		if(wp.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE || 
				wp.getBuildingId() != TerritoryType.GUILD_DRAGON_TRAP_VALUE){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_NO_POINT, 0);
			return;
		}
		//判断联盟ID是否一致
		if(!this.getGuildId().equals(wp.getGuildId())){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_NO_POINT, 0);
			return;
		}
		//开放冷却中
		long curTime = HawkTime.getMillisecond();
		if(curTime < this.getOpenTimeLimit()){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_IN_CD, 0);
			return;
		}
		//不在和平状态
		if(!this.inPeace()){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_STATE_LIMIT, 0);
			return;
		}
		//是否在开放时间内
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		if(!cfg.openTimeVertify(curTime)){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_TIME_ERR, 0);
			return;
		}
		//当前时间减少2S
		this.openTime = curTime - 1000;
		this.getEntity().notifyUpdate();
		
		//统一返回成功
		player.responseSuccess(hp);
		LogUtil.logGuildDragonAttackOpen(this.getGuildId(), this.appointmentTime, this.openTime);
		HawkLog.logPrintln("GuildDragonTrap-openImmediately-guildId-{},playerId:{}",this.getGuildId(),player.getId());
	}
	
	//预约开始
	public void openAppointmentTime(Player player,long appointmentTime,int hp){
		//5阶  盟主
		boolean checkGuildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.GUILD_DRAGON_ATK_OPEN);
		if (!checkGuildAuthority) {
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_AUTH_ERR, 0);
			return;
		}
		//检查建筑状态
		if(this.getBuildStat() != GuildBuildingStat.COMPELETE){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_UNLOCK, 0);
			return;
		}
		//检查世界点
		WorldPoint wp = this.getPoint();
		if(Objects.isNull(wp)){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_NO_POINT, 0);
			return;
		}
		//判断世界点类型
		if(wp.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE || 
				wp.getBuildingId() != TerritoryType.GUILD_DRAGON_TRAP_VALUE){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_NO_POINT, 0);
			return;
		}
		//判断联盟ID是否一致
		if(!this.getGuildId().equals(wp.getGuildId())){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_BUILD_NO_POINT, 0);
			return;
		}
		//不在和平状态
		if(this.inFight()){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_STATE_LIMIT, 0);
			return;
		}
		//开放冷却中
		if(appointmentTime < this.getOpenTimeLimit()){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_IN_CD, 0);
			return;
		}
		long curTime = HawkTime.getMillisecond();
		if(curTime > appointmentTime){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_TIME_ERR, 0);
			return;
		}
		//马上要开放了，不让修改了
		if(this.getAppointmentLimit(curTime)){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_TIME_LESS, 0);
			return;
		}
		//是否在开放时间内
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		if(!cfg.openTimeVertify(appointmentTime)){
			player.sendError(hp, Status.Error.GUILD_DRAGON_ACTTACK_OPEN_TIME_ERR, 0);
			return;
		}
		this.appointmentTime = appointmentTime;
		this.openTime = appointmentTime;
		this.getEntity().notifyUpdate();
		//发邮件
		GuildMailService.getInstance().sendGuildMail(this.getGuildId(), MailParames.newBuilder()
				.setMailId(MailId.GUILD_DRAGON_ATTCK_APPOINTMENT)
				.addContents(player.getName(), appointmentTime));
		//同步数据
		this.syncDragonAttackData();
		//统一返回成功
		player.responseSuccess(hp);
		
		ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_6_VALUE, this.getGuildId(), this.openTime, wp.getX(), wp.getY());
 		ScheduleService.getInstance().addSystemSchedule(schedule);
 		
		LogUtil.logGuildDragonAttackAppoint(this.getGuildId(), this.appointmentTime, this.openTime);
		HawkLog.logPrintln("GuildDragonTrap-openAppointmentTime-guildId-{},playerId:{}",this.getGuildId(),player.getId());
	}
	
	
	/**
	 * 计算当前状态
	 * @return
	 */
	public GuildDragonTrapState calState(){
		//没有设置开始时间，和平状态
		if(this.openTime <= 0){
			return GuildDragonTrapState.PEACE;
		}
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		long time = HawkTime.getMillisecond();
		long fightEndTime = this.getFightEndTime();
		long waitTime = fightEndTime + Math.min(120, cfg.getAllianceCd()) * 1000;
		if(this.openTime <= time && time < fightEndTime){
			//战斗状态
			return GuildDragonTrapState.FIGHT;
		}else if(fightEndTime <= time && time < waitTime){
			//等待结算
			return GuildDragonTrapState.WAIT;
		}
		return GuildDragonTrapState.PEACE;
	}
	

	
	/**
	 * 战斗开始
	 */
	public void onOPenFight(){
		this.termId = this.openTime;
		this.openTimeRecord = this.openTime;
		this.damages = new ConcurrentHashMap<>();
		this.damageTotal = 0;
		this.award = false;
		this.getEntity().notifyUpdate();
		//同步玩家活动新
		this.syncDragonAttackData();
		//更新世界点
		this.updateBuildWorldPoint();
		HawkLog.logPrintln("GuildDragonTrap-onOPenFight-guildId-{},termId:{}, state: {}",this.getGuildId(),this.getbuildStat());
	}
	
	
	
	/**
	 * 添加伤害
	 * @param playerId
	 * @param damage
	 */
	public void addDamage(String playerId,int damage){
		long curTime = HawkTime.getMillisecond();
		GuildDragonAttackDamage damageData = this.damages.get(playerId);
		if(Objects.isNull(damageData)){
			damageData  = new GuildDragonAttackDamage(playerId, 0, curTime);
			this.damages.put(playerId,damageData);
		}
		damageData.setDamage(damageData.getDamage() + damage);
		damageData.setTime(curTime);
		this.damageTotal += damage;
		this.getEntity().notifyUpdate();
		ActivityManager.getInstance().postEvent(new GuildDragonAttackScoreEvent(playerId, (int)damageData.getDamage()));
		HawkLog.logPrintln("GuildDragonTrap-addDamage-guildId-{},playerId:{},add:{},totlal:{},guildTotal:{}",this.getGuildId(),playerId,damage,damageData.getDamage(),this.damageTotal);
	}
	
	
	/**
	 * 发奖
	 */
	public void onAwardSend(){
		if(this.award){
			return;
		}
		//设置已经发奖
		this.award = true;
		this.getEntity().notifyUpdate();
		//发奖
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		long curTime = HawkTime.getMillisecond();
		List<GuildDragonAttackDamage> dlist = new ArrayList<>();
		dlist.addAll(this.damages.values());
		String guildId = this.getGuildId();
		//总积分
		//联盟伤害奖励
		GuildDragonAttackTotalRewardCfg totlaReward = this.getGuildDragonAttackTotalRewardCfg(this.damageTotal);
		//发奖个人奖励
		for(GuildDragonAttackDamage playerDamage : dlist){
			String playerId = playerDamage.getPlayerId();
			GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
			if(Objects.isNull(member)){
				HawkLog.logPrintln("GuildDragonTrap-onAwardSend-member-null-guildId-{},playerId:{}",this.getGuildId(),playerId);
				continue;
			}
			if(!guildId.equals(member.getGuildId())){
				HawkLog.logPrintln("GuildDragonTrap-onAwardSend-memberguild-err-guildId-{},playerId:{}",this.getGuildId(),playerId);
				continue;
			}
			
			long playeAwardTime = member.getDragonAwardTime();
			if(curTime < playeAwardTime + cfg.getPlayerCd() * 1000){
				LogUtil.logGuildDragonAttackReward(this.getGuildId(), playerId, playerDamage.getDamage(), this.damageTotal, 1, 0, 0);
				HawkLog.logPrintln("GuildDragonTrap-onAwardSend-playeAwardTime-err-guildId-{},playerId:{},playeAwardTime:{}",this.getGuildId(),playerId,playeAwardTime);
				continue;
			}
			//个人奖励
			GuildDragonAttackMemberRewardCfg memberRward = this.getGuildDragonAttackMemberRewardCfg(playerDamage.getDamage());
			if(Objects.nonNull(totlaReward) || Objects.nonNull(memberRward)){
				member.setDragonAwardTime(curTime);
			}
			int guildRewardId = 0;
			int playerRewardId = 0;
			//发联盟奖励
			if(Objects.nonNull(totlaReward)){
				guildRewardId = totlaReward.getId();
				AwardItems award = AwardItems.valueOf(totlaReward.getReward());
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		                .setPlayerId(playerId)
		                .setMailId(MailId.GUILD_DRAGON_ATTCK_GUILD_DAMAGE_REWARD)
		                .addContents(this.damageTotal)
		                .setRewards(award.getAwardItems())
		                .setAwardStatus(MailRewardStatus.NOT_GET)
		                .build());
				HawkLog.logPrintln("GuildDragonTrap-onAwardSend-guildAward-guildId-{},playerId:{},cfg:{},damage:{}",
						this.getGuildId(),playerId,totlaReward.getId(),this.damageTotal);
				
			}else{
			     SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			    		 .setPlayerId(playerId)
			    		 .setMailId(MailConst.MailId.GUILD_DRAGON_ATTCK_GUILD_DAMAGE_NO_REWARD)
			    		 .addContents(this.damageTotal)
			    		 .build());
			     HawkLog.logPrintln("GuildDragonTrap-onAwardSend-guildAwardNON-guildId-{},playerId:{},damage:{}",
			    		 this.getGuildId(),playerId,this.damageTotal);
			}
			//发个人奖励
			if(Objects.nonNull(memberRward)){
				playerRewardId = memberRward.getId();
				AwardItems award = AwardItems.valueOf(memberRward.getReward());
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		                .setPlayerId(playerId)
		                .setMailId(MailId.GUILD_DRAGON_ATTCK_PLAYER_DAMAGE_REWARD)
		                .addContents(playerDamage.getDamage())
		                .setRewards(award.getAwardItems())
		                .setAwardStatus(MailRewardStatus.NOT_GET)
		                .build());
				HawkLog.logPrintln("GuildDragonTrap-onAwardSend-MEMBERAward-guildId-{},playerId:{},CFG:{},damage:{}",
						this.getGuildId(),playerId,memberRward.getId(),playerDamage.getDamage());
			}
			
			LogUtil.logGuildDragonAttackReward(this.getGuildId(), playerId, playerDamage.getDamage(), this.damageTotal, 0, playerRewardId,guildRewardId);
			
		}
		
		
		if(Objects.nonNull(totlaReward)){
			LogUtil.logGuildDragonAttackEnd(this.getGuildId(), this.damageTotal, totlaReward.getId());
		}
	}
	
	/**
	 * 清下数据
	 */
	public void clearData(){
		this.termId = 0;
		this.openTime = 0;
		this.appointmentTime = 0;
		this.getEntity().notifyUpdate();
		HawkLog.logPrintln("GuildDragonTrap-clearData-guildId-{}",this.getGuildId());
	}
	

	/**
	 * 更新建筑世界点
	 */
	public void updateBuildWorldPoint(){
		//更新世界点
		WorldPoint point = this.getPoint();
		if(Objects.nonNull(point)){
			WorldPointService.getInstance().notifyPointUpdate(point.getX(),point.getY());
		}
	}
	
	
	
	/**
	 * 同步活动数据
	 */
	public void syncDragonAttackData(){
		Optional<ActivityBase> opPlantActivity  = ActivityManager.getInstance().
				getActivity(ActivityType.GUILD_DRAGON_ATTACK_VALUE);
		if (!opPlantActivity.isPresent()) {
			return;
		}
		ActivityBase activity = opPlantActivity.get();
		GuildDragonAttackActivity act = (GuildDragonAttackActivity) activity;
		String guildId = this.getGuildId();
		List<String>  onlines = GuildService.getInstance().getOnlineMembers(guildId);
		for(String playerId : onlines){
			act.syncActivityStateInfo(playerId);
		}
	}
	
	
	
	
	/**
	 * 是否战斗中
	 * @return
	 */
	public boolean inFight(){
		if(this.trapState == GuildDragonTrapState.FIGHT.getVal()){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 是否战斗中
	 * @return
	 */
	public boolean inPeace(){
		if(this.trapState == GuildDragonTrapState.PEACE.getVal()){
			return true;
		}
		return false;
	}

	/**
	 * 获取战斗结束时间
	 * @return
	 */
	public long getFightEndTime(){
		if(this.openTime == 0){
			return 0;
		}
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		return this.openTime + cfg.getDuration() * 1000;
	}
	

	
	/**
	 * 预约操作限制时间点
	 * @return
	 */
	public boolean getAppointmentLimit(long time){
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		long limit = this.appointmentTime - cfg.getDisableTime() * 1000;
		if(limit <= time && time <= this.appointmentTime){
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * 获取开放限定时间
	 * @return
	 */
	public long getOpenTimeLimit(){
		if(this.openTimeRecord == 0){
			return 0;
		}
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		return this.openTimeRecord + cfg.getDuration() * 1000 + cfg.getAllianceCd() * 1000;
	}
	
	
	public long getAppointmentTime() {
		return appointmentTime;
	}
	
	public long getOpenTime() {
		return openTime;
	}
	
	
	public GuildDragonAttackMemberRewardCfg getGuildDragonAttackMemberRewardCfg(long damage){
		ConfigIterator<GuildDragonAttackMemberRewardCfg> itr = HawkConfigManager.getInstance()
				.getConfigIterator(GuildDragonAttackMemberRewardCfg.class);
		GuildDragonAttackMemberRewardCfg rlt = null;
		for(GuildDragonAttackMemberRewardCfg cfg : itr){
			if(cfg.getDamagePoint() > damage){
				continue;
			}
			if(Objects.isNull(rlt) ||
					cfg.getDamagePoint() > rlt.getDamagePoint()){
				rlt = cfg;
			}
		}
		return rlt;
	}
	
	public GuildDragonAttackTotalRewardCfg getGuildDragonAttackTotalRewardCfg(long damage){
		ConfigIterator<GuildDragonAttackTotalRewardCfg> itr = HawkConfigManager.getInstance()
				.getConfigIterator(GuildDragonAttackTotalRewardCfg.class);
		GuildDragonAttackTotalRewardCfg rlt = null;
		for(GuildDragonAttackTotalRewardCfg cfg : itr){
			if(cfg.getAllianceDamagePoint() > damage){
				continue;
			}
			if(Objects.isNull(rlt) ||
					cfg.getAllianceDamagePoint() > rlt.getAllianceDamagePoint()){
				rlt = cfg;
			}
		}
		return rlt;
	}
	
	
	
	@Override
	public void addProtocol2Builder(Builder builder) {
		GuildDragonTrapBase.Builder trapBuilder = GuildDragonTrapBase.newBuilder();
		trapBuilder.setStat(GuildBuildingNorStat.valueOf(getEntity().getBuildingStat()));
		trapBuilder.setX(getEntity().getPosX());
		trapBuilder.setY(getEntity().getPosY());
		builder.setDragonTrap(trapBuilder);
	}

	@Override
	public void parseBuildingParam(String buildParam) {
		if(HawkOSOperator.isEmptyString(buildParam)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(buildParam);
		this.termId = obj.getLongValue("termId");
		this.trapState = obj.getIntValue("trapState");
		this.appointmentTime = obj.getLongValue("appointmentTime");
		this.openTime = obj.getLongValue("openTime");
		this.openTimeRecord = obj.getLongValue("openTimeRecord");
		this.award = obj.getBooleanValue("award");
		this.damageTotal = obj.getLongValue("damageTotal");
		if(obj.containsKey("damages")){
			Map<String,GuildDragonAttackDamage> damagesTemp = new ConcurrentHashMap<>();
			String damage = obj.getString("damages");
			String[] darr = damage.split(SerializeHelper.BETWEEN_ITEMS);
			for(String dstr : darr){
				GuildDragonAttackDamage dataData = new GuildDragonAttackDamage();
				dataData.unSerializ(dstr);
				damagesTemp.put(dataData.getPlayerId(), dataData);
			}
			this.damages = damagesTemp;
		}
	
	}

	@Override
	public String genBuildingParamStr() {
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("trapState", this.trapState);
		obj.put("appointmentTime", this.appointmentTime);
		obj.put("openTime", this.openTime);
		obj.put("openTimeRecord", this.openTimeRecord);
		obj.put("award", this.award);
		obj.put("damageTotal", this.damageTotal);
		if(!this.damages.isEmpty()){
			List<String> dlist = new ArrayList<>();
			for(GuildDragonAttackDamage damage : this.damages.values()){
				dlist.add(damage.serializ());
			}
			String damgeStr = Joiner.on(SerializeHelper.BETWEEN_ITEMS).join(dlist);
			obj.put("damages", damgeStr);
		}
		return obj.toJSONString();
	}
	
	
	/**
	 * 获取陷阱数据
	 * @param playerId
	 * @return
	 */
	public GuildDragonTrapData getGuildDragonTrapData(String playerId){
		GuildDragonTrapData data = new GuildDragonTrapData();
		data.openId = this.termId;
		data.inFight = this.inFight();
		data.appointmentTime = this.appointmentTime;
		data.openTimeLimit = this.getOpenTimeLimit();
		data.guildDamage = this.damageTotal;
		data.endTime = this.getFightEndTime();
		data.worldPosx = this.getEntity().getPosX();
		data.worldPosy = this.getEntity().getPosY();
		if(HawkOSOperator.isEmptyString(playerId)){
			GuildDragonAttackDamage damageData = this.damages.get(playerId);
			if(Objects.isNull(damageData)){
				data.playerDamage = damageData.getDamage();
			}
		}
		return data;
	}
	
	
	
	
	public List<PBDamageRank> getRankData(){
		String buildGuild = this.getGuildId();
		List<PBDamageRank> rankList = new ArrayList<>();
		List<GuildDragonAttackDamage> damageList = new ArrayList<>();
		for(GuildDragonAttackDamage damage : this.damages.values()){
			String playerId = damage.getPlayerId();
			String playerGuild = GuildService.getInstance().getPlayerGuildId(playerId);
			if(buildGuild.equals(playerGuild)){
				damageList.add(damage);
			}
		}
		Collections.sort(damageList);
		for(int i=0;i<damageList.size();i++){
			GuildDragonAttackDamage damage = damageList.get(i);
			String playerId = damage.getPlayerId();
			String playerName = GlobalData.getInstance().getPlayerNameById(playerId);
			PBDamageRank.Builder rbuilder = PBDamageRank.newBuilder();
			rbuilder.setRank(i+1);
			rbuilder.setPlayerName(playerName);
			rbuilder.setScore(damage.getDamage());
			rbuilder.setPlayerId(playerId);
			rankList.add(rbuilder.build());
		}
		return rankList;
	}
	
	
	public List<ArmyInfo> getTarpArmy(){
		GuildDragonAttackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildDragonAttackKVCfg.class);
		List<ArmyInfo> armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(cfg.getSoldier())) {
			for (String army : Splitter.on("|").split(cfg.getSoldier())) {
				String[] armyStrs = army.split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(armyStrs[0]), Integer.parseInt(armyStrs[1])));
			}
		}
		return armyList;
	}
	
	public long getTermId() {
		return termId;
	}
}
