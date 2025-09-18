package com.hawk.game.lianmengxzq.worldpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengxzq.XZQGift;
import com.hawk.game.lianmengxzq.XZQRedisData;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.XZQTlog;
import com.hawk.game.lianmengxzq.timecontroller.IXZQTimeCfg;
import com.hawk.game.lianmengxzq.worldpoint.data.XZQBuildRecord;
import com.hawk.game.lianmengxzq.worldpoint.data.XZQCommander;
import com.hawk.game.lianmengxzq.worldpoint.data.ZXQPointInfoData;
import com.hawk.game.lianmengxzq.worldpoint.state.IXZQPointState;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.WorldPoint.PointData;
import com.hawk.game.protocol.XZQ.PBXZQBuild;
import com.hawk.game.protocol.XZQ.PBXZQBuildCommander;
import com.hawk.game.protocol.XZQ.PBXZQBuildDetail;
import com.hawk.game.protocol.XZQ.PBXZQBuildGuild;
import com.hawk.game.protocol.XZQ.PBXZQBuildRecord;
import com.hawk.game.protocol.XZQ.PBXZQBuildStatus;
import com.hawk.game.protocol.XZQ.PBXZQQuarterInfoResp;
import com.hawk.game.protocol.XZQ.PBXZQQuarterMarch;
import com.hawk.game.protocol.XZQ.PBXZQStatus;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

public class XZQWorldPoint extends WorldPoint {
	/** 日志 */
	public static final Logger logger = LoggerFactory.getLogger("Server");
	/** 世界点数据*/
	private final ZXQPointInfoData pointInfo;
	/** 当前状态*/
	private IXZQPointState state;
	/** npc驻军*/
	private TemporaryMarch npcMarch;
	/** 报名时间*/
	private Map<String,Long> guildSignTime = new ConcurrentHashMap<>();
	/** 伤害列表*/
	private Map<String,Long> damages = new ConcurrentHashMap<>();
	
	
	public XZQWorldPoint(WorldPointType type) {
		pointInfo = new ZXQPointInfoData();
	}
	
	public XZQWorldPoint(int x, int y, int areaId, int zoneId, int pointType) {
		super(x, y, areaId, zoneId, pointType);
		pointInfo = new ZXQPointInfoData();
		pointInfo.setHasNpc(true);
		pointInfo.setStateOrdinal(PBXZQBuildStatus.XZQ_BUILD_INIT_VALUE);
		state = IXZQPointState.valueOf(this, pointInfo.getStateOrdinal());
		createNpc();
		pointInfo.loadXZQBuildRecord(this.getXzqCfg());
	}

	
	/**
	 * 时钟tick
	 */
	public void ontick() {
		state.ontick();
	}
	
	/**
	 * 创建NPC
	 */
	public void createNpc() {
		XZQPointCfg cfg = this.getXzqCfg();
		if(cfg == null){
			return;
		}
		NpcPlayer ghostplayer = new NpcPlayer(HawkXID.nullXid());
		List<PlayerHero> hero = NPCHeroFactory.getInstance().get(cfg.getHeroIdList());
		ghostplayer.setPlayerId(cfg.getId() + "");
		ghostplayer.setName("");
		ghostplayer.setPlayerPos(getId());
		ghostplayer.setHeros(hero);
		TemporaryMarch asmarch = new TemporaryMarch();
		asmarch.setPlayer(ghostplayer);
		asmarch.setMarchId(ghostplayer.getId());
		asmarch.setMarchType(WorldMarchType.GHOST_STRIKE);
		asmarch.setArmys(cfg.getArmyList());
		asmarch.setHeros(hero);
		npcMarch = asmarch;
		this.pointInfo.setHasNpc(true);
	}
	
	

	/**
	 * 修复重置
	 */
	public void fixClearReset(){
		this.pointInfo.setCommander(null);
		this.pointInfo.setLastControlGuild(null);
		this.pointInfo.getOccupyHistorySet().clear();
		this.pointInfo.getSignupGuillds().clear();
		this.pointInfo.clearXZQBuildRecord(getXzqCfg());
		this.pointInfo.setHasNpc(true);
		this.createNpc();
		this.notifyUpdate();
		logger.info("XZQWorldPoint fixClearReset,point:{},x:{},y:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY());
	}
	
	/**
	 * 合服清除
	 */
	public void mergerServerClear(){
		this.pointInfo.setCommander(null);
		this.pointInfo.setLastControlGuild(null);
		this.pointInfo.getOccupyHistorySet().clear();
		this.pointInfo.getSignupGuillds().clear();
		this.pointInfo.setHasNpc(true);
		this.createNpc();
		this.notifyUpdate();
		logger.info("XZQWorldPoint mergerServerClear delete,point:{},x:{},y:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY());
	}
	
	
	/**
	 * 检查联盟是否存在
	 */
	public void checkGuild(){
		//检查报名
		boolean updateData = false;
		List<String> list = this.getSignupGuilds();
		if(list.size() > 0){
			List<String> dels = new ArrayList<>();
			for(String id : list){
				if(GuildService.getInstance().getGuildInfoObject(id) == null){
					dels.add(id);
				}
			}
			for(String id : dels){
				pointInfo.getSignupGuillds().remove(id);
				updateData = true;
				logger.info("XZQWorldPoint checkGuild delete,point:{},x:{},y:{}, guildId: {}",
						this.getXzqCfg().getId(),this.getX(),this.getY(),id);
			}
		}
		
		//检查控制
		String guildId = this.getGuildControl();
		if(!HawkOSOperator.isEmptyString(guildId) &&
				GuildService.getInstance().getGuildInfoObject(guildId) == null){
			pointInfo.setCommander(null);
			createNpc();
			updateData = true;
			logger.info("XZQWorldPoint checkGuild delete control,point:{},x:{},y:{}, guildId: {}",
					this.getXzqCfg().getId(),this.getX(),this.getY(),guildId);
		}
		if(updateData){
			this.notifyUpdate();
			updateWorldScene();
		}
	}
	
	/**
	 * 被放弃重置
	 */
	public void giveupReset(){
		String controlGuild = this.getGuildControl();
		pointInfo.setCommander(null);
		pointInfo.setHasNpc(true);
		createNpc();
		this.notifyUpdate();
		updateWorldScene();
		logger.info("XZQWorldPoint giveupReset ,point:{},x:{},y:{}, guildId: {}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),controlGuild);
	}
	
	/**
	 * 检查报名时间是否合理
	 * @param guildId
	 * @return
	 */
	public boolean checkSignTimeErr(String guildId){
		if(!this.guildSignTime.containsKey(guildId)){
			return false;
		}
		long time = this.guildSignTime.get(guildId);
		long curTime = HawkTime.getMillisecond();
		if(curTime - time < 5 * 1000){
			return true;
		}
		return false;
	}
	

	
	
	/**
	 * 获取所有报名联盟
	 * @return
	 */
	public List<String> getSignupGuilds(){
		List<String> list = new ArrayList<>();
		list.addAll(this.pointInfo.getSignupGuillds());
		return list;
	}

	
	/**
	 * 获取报名联盟总数
	 * @return
	 */
	public int getSignupCount(){
		return this.pointInfo.getSignupGuillds().size();
	}
	
	
	/**
	 * 清除报名
	 */
	public void clearSignup(){
		this.pointInfo.setSignupGuillds(new CopyOnWriteArrayList<String>());
		this.guildSignTime.clear();
		this.notifyUpdate();
		logger.info("XZQWorldPoint clearSignup,point:{},x:{},y:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY());
	}
	
	/**
	 * 清除伤害统计
	 */
	public void clearDamages(){
		this.damages.clear();
		this.notifyUpdate();
		logger.info("XZQWorldPoint clearDamages,point:{},x:{},y:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY());
	}
	
	/**
	 * 获取小站区保护时间
	 */
	public long calProtectedEndTime(){
		PBXZQBuildStatus state = this.getXZQBuildStatus();
		if(state == PBXZQBuildStatus.XZQ_BUILD_BATTLE){
			return 0;
		}
		return Long.MAX_VALUE;
	}
	
	
	/**
	 * 获取当前状态
	 * @return
	 */
	public PBXZQBuildStatus getXZQBuildStatus(){
		PBXZQBuildStatus statues = PBXZQBuildStatus.valueOf(this.pointInfo.getStateOrdinal());
		return statues;
	}
	
	/**
	 * 更替状态
	 * @param statues
	 */
	public void updateXZQBuildStatus(PBXZQBuildStatus statues){
		this.pointInfo.setStateOrdinal(statues.getNumber());
		this.state = IXZQPointState.valueOf(this, this.pointInfo.getStateOrdinal());
		this.notifyUpdate();
	}
	
	/**
	 * 获取NPC守军
	 * @return
	 */
	public TemporaryMarch getNpcMarch() {
		return npcMarch;
	}
	
	/**
	 * 获取控制者信息
	 * @return
	 */
	public XZQCommander getCommander() {
		return pointInfo.getCommander();
	}
		
	/**
	 * 是否有NPC部队
	 * @return
	 */
	public boolean hasNpc() {
		return pointInfo.isHasNpc();
	}
	
	/**
	 * 是否被攻破过
	 * @return
	 */
	public boolean isInitOccupyed(){
		for(XZQBuildRecord record : this.pointInfo.getXZQBuildRecords()){
			if(record.getOccupyPlayerRecord() != null){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 是否被控制过
	 * @return
	 */
	public boolean isInitControl(){
		for(XZQBuildRecord record : this.pointInfo.getXZQBuildRecords()){
			if(record.getControlGuild() != null){
				return false;
			}
		}
		return true;
	}

	
	
	/**
	 * 清除记录
	 */
	public void clearnOccupuHistory(){
		this.pointInfo.getOccupyHistorySet().clear();
		this.notifyUpdate();
		logger.info("XZQWorldPoint clearnOccupuHistory,point:{},x:{},y:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY());
	}
	
	
	/**
	 * 添加攻占记录
	 * @param guildId
	 */
	public void addOccupuHistory(String guildId) {
		this.pointInfo.getOccupyHistorySet().add(guildId);
		notifyUpdate();
		logger.info("XZQWorldPoint addOccupuHistory,point:{},x:{},y:{},guildId:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),guildId);
	}

	/**
	 * 是否攻占
	 * @param guildId
	 * @return
	 */
	public boolean checkOccupyHistory(String guildId) {
		return this.pointInfo.getOccupyHistorySet().contains(guildId);
	}
	
	

	/**
	 * 添加报名联盟
	 * @param guildId
	 */
	public void signup(String guildId) {
		if (this.pointInfo.getSignupGuillds().contains(guildId)) {
			return;
		}
		long curTime = HawkTime.getMillisecond();
		this.guildSignTime.put(guildId, curTime);
		this.pointInfo.getSignupGuillds().add(guildId);
		notifyUpdate();
		logger.info("XZQWorldPoint signup,point:{},x:{},y:{},guildId:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),guildId);
	}

	/**
	 * 移除报名
	 * @param guildId
	 */
	public void signupRemove(String guildId) {
		this.pointInfo.getSignupGuillds().remove(guildId);
		notifyUpdate();
		logger.info("XZQWorldPoint signupRemove,point:{},x:{},y:{},guildId:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),guildId);
	}

	/**
	 * 联盟是否已经报名
	 * @param guildId
	 * @return
	 */
	public boolean isSignup(String guildId) {
		if (this.pointInfo.getSignupGuillds().contains(guildId)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否被联盟控制
	 * @param guildId
	 * @return
	 */
	public boolean isControl(String guildId) {
		String controlGuild = this.pointInfo.getGuildControl();
		if (HawkOSOperator.isEmptyString(controlGuild)) {
			return false;
		}
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		if (!controlGuild.equals(guildId)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取战前控制联盟ID
	 * @return
	 */
	public String getBeforeControl(){
		return this.pointInfo.getLastControlGuild();
	}
	
	/**
	 * 是否和平
	 * @return
	 */
	public boolean isPeace() {
		return state.isPeace();
	}

	/**
	 * 获取当前控制联盟
	 * @return
	 */
	public String getGuildControl() {
		return this.pointInfo.getGuildControl();
	}

	
	/**
	 * 当前攻占联盟
	 * @return
	 */
	public String getOccupyGuild(){
		return this.state.getOccupyGuild();
	}
	
	
	/**
	 * 更新NPC
	 */
	public void updateNpc(){
		if(this.hasNpc()){
			this.createNpc();
		}
	}
	
	/**
	 * 更新战前控制联盟
	 */
	public void updateControlBefore(){
		String guildId= "null";
		XZQCommander control = this.pointInfo.getCommander();
		if(control == null){
			this.pointInfo.setLastControlGuild(null);
		}else{
			this.pointInfo.setLastControlGuild(control.getPlayerGuildId());
			guildId = control.getPlayerGuildId();
		}
		notifyUpdate();
		logger.info("XZQWorldPoint updateControlBefore,point:{},x:{},y:{},guildId:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),guildId);
	}
	
	
	
	
	

	/**
	 * 更新世界点
	 */
	public void updateWorldScene() {
		XZQService.getInstance().addUpdatePoint(this.getId());
		//刷新世界点
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.XZQ_POINT_UPDATE) {
			@Override
			public boolean onInvoke() {
				//世界更新建筑
				WorldPointService.getInstance().getWorldScene().update(getAoiObjId());
				return true;
			}
		});
	}

	
	
	/**
	 * 战胜NPC
	 * @param playerName
	 * @param guildId
	 */
	public void fightNpcWin(String playerId,String playerName, String guildId) {
		pointInfo.setHasNpc(false);
		this.npcMarch = null;
		this.notifyUpdate();
		logger.info("XZQWorldPoint fightNpcWin,point:{},x:{},y:{},playerId:{},playerName:{},guildId:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),playerId,playerName,guildId);
	}
	
	
	/**
	 * 没有国王(无人占领/报名)结束
	 */
	public void noCommanderOver() {
		int termId = XZQService.getInstance().getXZQTermId();
		//退出驻军
		this.dissolvePointMarchs();
		//重置NPC
		boolean hasNpc = this.hasNpc();
		XZQCommander control = this.getCommander();
		if(!hasNpc && control == null){
			this.createNpc();
		}
		//发奖
		XZQGift.getInstance().sendControlAward(this);
		logger.info("XZQWorldPoint noCommanderOver,point:{},x:{},y:{},termId:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),termId);
	}


	/**
	 * 是否有行军停留
	 * @param viewerId
	 * @return
	 */
	private boolean hasMarchStop(String viewerId) {
		for (String marchId : WorldMarchService.getInstance().getXZQMarchs(getId())) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march.getPlayerId().equals(viewerId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 小战区建筑战斗胜利处理
	 * @param march
	 */
	public void doXZQAttackWin(Player atkLeader, Player defLeader) {
		String leadId = atkLeader.getId();
		String leadName = atkLeader.getName();
		String guild = atkLeader.getGuildId();
	
		String defId = "";
		String defName = "";
		String defGuild = "";
		if(defLeader!= null){
			defId = defLeader.getId();
			defName = defLeader.getName();
			defGuild = defLeader.getGuildId();
		}
		logger.info("XZQWorldPoint doXZQAttackWin,point:{},x:{},y:{},atkPlayerId:{},atkPlayerName:{},atkGuildId:{},"
				+ "defPlayerId:{},defPlayerName:{},defGuildId:{},",
				this.getXzqCfg().getId(),this.getX(),this.getY(),leadId,leadName,guild,defId,defName,defGuild);
	}


	/**
	 * 有占领者情况下结束战斗
	 * @param currTime
	 * @param constCfg
	 */
	public void xzqWinOver(long currTime,int termId) {
		Player leader = WorldMarchService.getInstance().getXZQLeader(getId());
		String guildId = leader.getGuildId();
		// 获取获胜联盟信息
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
		// 上届总司令Id
		XZQCommander commander = getCommander();
		String lastPresidentId = commander == null ? null : commander.getPlayerId();
		// 上届没有司令
		if (commander == null) {
			commander = new XZQCommander();
			pointInfo.setCommander(commander);
		}
		// 设置任职时间
		if (HawkOSOperator.isEmptyString(lastPresidentId) || 
				!leader.getId().equals(lastPresidentId)) {
			commander.setTime(currTime);
		}
		commander.setPlayerId(leader.getId());
		commander.setPlayerName(leader.getName());
		commander.setPlayerGuildId(leader.getGuildId());
		commander.setPlayerGuildName(guildInfo.getName());
		commander.setTermId(termId);
		commander.setIcon(leader.getIcon());
		commander.setPfIcon(leader.getPfIcon());
		
		//转为控制状态
		this.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_CONTROL);
		//发奖
		XZQGift.getInstance().sendControlAward(this);
		//刻字
		this.addFirstControlRecord(guildId);
		//退出驻军
		this.dissolvePointMarchs();
		//颜色计算
		XZQService.getInstance().updateXZQForceColor(true);
		notifyUpdate();
		//跑马灯
		ChatParames msg = ChatParames.newBuilder()
				.setChatType(Const.ChatType.SPECIAL_BROADCAST)
				.setKey(Const.NoticeCfgId.XZQ_CONTROL_SUCC)
				.addParms(getX(), getY(), guildInfo.getTag(), leader.getName())
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(msg);
		// 发放全员邮件
		int pos[] = GameUtil.splitXAndY(getId());
		GuildMailService.getInstance().sendGuildMail(getGuildId(), MailParames.newBuilder()
				.setMailId(MailId.XZQ_HAS_CONTROL)
				.addSubTitles(pos[0], pos[1])
				.addContents(pos[0], pos[1]));
		logger.info("XZQWorldPoint xzqWinOver,point:{},x:{},y:{},termId:{},playerId:{},playerName:{},guild:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),termId,leader.getId(),leader.getName(),leader.getGuildId());
	}


	public void addFirstControlRecord(String guildId){
		if(!this.isInitControl()){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		XZQBuildRecord record = this.pointInfo.getRecord(serverId);
		if(record == null){
			record = new XZQBuildRecord();
			this.pointInfo.addRecord(record);
		}
		if(record.getControlGuild() != null){
			return;
		}
		record.setControlGuild(genPBXZQBuildGuildBuilder(guildId).build());
		record.notifyUpdate();
		logger.info("XZQWorldPoint addFirstControlRecord,point:{},x:{},y:{},guild:{}",
				this.getXzqCfg().getId(),this.getX(),this.getY(),guildId);
	}
	
	
	/**
	 * 添加伤害
	 * @param battleOutcome
	 * @param atkPlayers
	 */
	public void addFirstOccupyDamage(BattleOutcome battleOutcome,List<Player> atkPlayers){
		if (!this.isInitOccupyed()) {
			return;
		}
		Map<String, List<ArmyInfo>> aftArmyMapAtk = battleOutcome.getAftArmyMapAtk();
		for (Entry<String, List<ArmyInfo>> entry : aftArmyMapAtk.entrySet()) {
			String playerId = entry.getKey();
			List<ArmyInfo> armyInfos = entry.getValue();
			long damageCount = 0;
			for (ArmyInfo armyInfo : armyInfos) {
				damageCount += armyInfo.getKillCount();
			}
			if(this.damages.containsKey(playerId)){
				damageCount += this.damages.get(playerId);
			}
			this.damages.put(playerId, damageCount);
		}
		
		
	}

	/**
	 * 攻破刻字
	 * @param battleOutcome
	 * @param atkPlayers
	 */
	public void addFirstOccupyRecord(List<Player> atkPlayers) {
		if (!this.isInitOccupyed()) {
			return;
		}
		//添加首次攻破刻字
		String serverId = GsConfig.getInstance().getServerId();
		XZQBuildRecord record = this.pointInfo.getRecord(serverId);
		if(record == null){
			record = new XZQBuildRecord();
			record.setPointId(this.getXzqCfg().getId());
			record.setServerId(serverId);
			this.pointInfo.addRecord(record);
		}
		int termId = XZQService.getInstance().getXZQTermId();
		//伤害排序
		List<HawkTuple2<String, Long>> hurtlist = new ArrayList<>();
		for (Entry<String,Long> entry : this.damages.entrySet()) {
			String playerId = entry.getKey();
			long damage = entry.getValue();
			hurtlist.add(HawkTuples.tuple(playerId, damage));
		}
		Collections.sort(hurtlist, Comparator.comparingLong(e -> e.second));
		Collections.reverse(hurtlist);
		//攻破伤害前三
		List<PBXZQBuildCommander> damages = new ArrayList<>();
		for (int i = 0; i < 3 && i < hurtlist.size(); i++) {
			String damagePlayerId = hurtlist.get(i).first;
			long damageCount = hurtlist.get(i).second;
			Player damagePlayer = GlobalData.getInstance().makesurePlayer(damagePlayerId);
			if (damagePlayer == null) {
				continue;
			}
			PBXZQBuildCommander.Builder commander = genPBXZQBuildCommanderBuilder(damagePlayerId);
			if(commander != null){
				damages.add(commander.build());
				XZQTlog.XZQRecordDamage(damagePlayer, termId, damagePlayer.getGuildId(),this.getXzqCfg().getId(), damageCount,i);
				logger.info("XZQWorldPoint firstOccupyRecord damage,point:{},x:{},y:{},playerId:{},playerName:{},guild:{},damageCount:{},damageIndex:{}",
						this.getXzqCfg().getId(),this.getX(),this.getY(),damagePlayer.getId(),damagePlayer.getName(),damagePlayer.getGuildId(),damageCount,i);
				
			}
		}
		record.setDamagesRecord(damages);
		//首先攻破
		Player leader = atkPlayers.get(0);
		String guildId = leader.getGuildId();
		PBXZQBuildCommander.Builder occupy =genPBXZQBuildCommanderBuilder(atkPlayers.get(0).getId());
		if(occupy != null){
			record.setOccupyPlayerRecord(occupy.build());
			XZQTlog.XZQRecordFistOccupy(leader, termId,leader.getGuildId(), this.getXzqCfg().getId());
			logger.info("XZQWorldPoint firstOccupyRecord fistOccupy,point:{},x:{},y:{},playerId:{},playerName:{},guild:{}",
					this.getXzqCfg().getId(),this.getX(),this.getY(),leader.getId(),leader.getName(),leader.getGuildId());
		}
		//保存数据
		record.notifyUpdate();
		//发邮件,跑马灯
		if (GuildService.getInstance().isGuildExist(guildId)) {
			GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			//跑马灯
			ChatParames msg = ChatParames.newBuilder()
					.setChatType(Const.ChatType.SPECIAL_BROADCAST)
					.setKey(Const.NoticeCfgId.XZQ_OCCUPY)
					.addParms(getX(), getY(),guildInfo.getTag(),leader.getName())
					.build();
			ChatService.getInstance().addWorldBroadcastMsg(msg);
			//联盟邮件
			GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
					.setMailId(MailId.XZQ_OCCUPY_SUCC)
					.addSubTitles(getX(), getY())
					.addContents(getX(), getY()));
		}
		
	}
	

	

	
	/**
	 * 退出所有建筑内的军队
	 */
	public void dissolvePointMarchs(){
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.XZQ_POINT_UPDATE) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().dissolveAllXZQQuarteredMarchs(getId());
				return true;
			}
		});
	}
	
	
	/**
	 * 遣返行军
	 */
	public boolean repatriateMarch(Player player, String targetPlayerId) {
		Player occupyLeader = WorldMarchService.getInstance().getXZQLeader(getId());
		String occupyGuild = "";
		if(occupyLeader!= null ){
			occupyGuild = occupyLeader.getGuildId();
		}
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild() || !player.getGuildId().equals(occupyGuild)) {
			return false;
		}
		// 队长
		Player leader = WorldMarchService.getInstance().getXZQLeader(getId());
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REPATRIATE_MARCH) {
			@Override
			public boolean onInvoke() {
				List<IWorldMarch> marchs = WorldMarchService.getInstance().getXZQStayMarchs(getId());
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
				sendXZQQuarterInfo(player);
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
	public boolean cheangeQuarterLeader(Player player, String targetPlayerId) {
		// 没有联盟或者不是本联盟占领
		if (!player.hasGuild() || !player.getGuildId().equals(this.getOccupyGuild())) {
			return false;
		}
		// 队长
		Player leader = WorldMarchService.getInstance().getXZQLeader(getId());
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return false;
		}
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().changeXZQMarchLeader(getId(), targetPlayerId);
				sendXZQQuarterInfo(player);
				return true;
			}
		});
		return true;
	}

	/**
	 * 发送超级武器驻军信息
	 * @param player
	 */
	public void sendXZQQuarterInfo(Player player) {
		PBXZQQuarterInfoResp.Builder builder = PBXZQQuarterInfoResp.newBuilder();
		Player occupyLeader = WorldMarchService.getInstance().getXZQLeader(getId());
		String occupyGuild = "";
		if(occupyLeader!= null ){
			occupyGuild = occupyLeader.getGuildId();
		}
		if (!player.hasGuild() || !player.getGuildId().equals(occupyGuild)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_QUARTER_INFO_S, builder));
			return;
		}
		BlockingDeque<String> marchs = WorldMarchService.getInstance().getXZQMarchs(getId());
		for (String marchId : marchs) {
			builder.addQuarterMarch(getXZQQuarterMarch(marchId));
		}
		String leaderId = WorldMarchService.getInstance().getXZQLeaderMarchId(getId());
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderId);
		int maxMassJoinSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getPlayer());
		builder.setMassSoldierNum(maxMassJoinSoldierNum);

		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_QUARTER_INFO_S, builder));
	}

	/**
	 * 驻军
	 * @param marchId
	 * @return
	 */
	public PBXZQQuarterMarch.Builder getXZQQuarterMarch(String marchId) {
		PBXZQQuarterMarch.Builder builder = PBXZQQuarterMarch.newBuilder();

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
		if (Objects.nonNull(ssoldier)) {
			builder.setSsoldier(ssoldier.toPBobj());
		}
		return builder;
	}

	

	/**
	 * 获取建筑配置
	 * @return
	 */
	public XZQPointCfg getXzqCfg() {
		XZQPointCfg cfg = HawkConfigManager.getInstance().getCombineConfig(XZQPointCfg.class, getX(), getY());
		return cfg;
	}
	
	/**
	 * 小站区建筑当前状态
	 * @return
	 */
	public IXZQTimeCfg getXZQTime() {
		int termId = XZQService.getInstance().getXZQTermId();
		PBXZQStatus state = XZQService.getInstance().getState();
		if(state == PBXZQStatus.XZQ_HIDDEN){
			return XZQService.getInstance().getTimeController().getNearlyTimeCfg();
		}
		return XZQService.getInstance().getTimeController().getTimeCfg(termId);
	}
	
	public IXZQTimeCfg getXZQNearlyTime(){
		return XZQService.getInstance().getTimeController().
				getBuildNearlyTimeCfg(this.getXzqCfg().getLevel());
	}


	/**
	 * 小站区建筑详细信息
	 * @return
	 */
	public PBXZQBuildDetail.Builder genPBXZQBuildDetailBuilder(String viewerId) {
		PBXZQBuildDetail.Builder builder = PBXZQBuildDetail.newBuilder();
		builder.setBuidingId(this.getXzqCfg().getId());
		builder.setPosx(this.getX());
		builder.setPosy(this.getY());
		//建筑状态
		PBXZQBuildStatus buildState = this.getXZQBuildStatus();
		builder.setStatus(buildState);
		//时间
		builder.setTime(this.getXZQTime().genPBXZQTimeInfoBuilder());
		builder.setNextTime(this.getXZQNearlyTime().genPBXZQTimeInfoBuilder());
		//控制联盟
		String controlGuild = this.pointInfo.getGuildControl();
		PBXZQBuildGuild.Builder controlBuilder = this.genPBXZQBuildGuildBuilder(controlGuild);
		if (controlBuilder != null) {
			builder.setControl(controlBuilder);
		}
		String controlBefore = this.pointInfo.getLastControlGuild();
		if(!HawkOSOperator.isEmptyString(controlBefore)){
			builder.setControlBefore(controlBefore);
		}
		//刻字
		List<PBXZQBuildRecord>  recordBuilder = this.genPBXZQBuildRecordBuidder();
		if (recordBuilder.size() > 0) {
			builder.addAllRecord(recordBuilder);
		}
		//报名
		List<String> signupGuilds = this.getSignupGuilds();
		if (!signupGuilds.isEmpty()) {
			for (String guildId : signupGuilds) {
				PBXZQBuildGuild.Builder signGuildBuilder = this.genPBXZQBuildGuildBuilder(guildId);
				if (signGuildBuilder != null) {
					builder.addSignupGuilds(signGuildBuilder);
				}
			}
			builder.addAllSignupGuildIds(signupGuilds);
		}
		//NPC
		builder.setHaveNpc(this.hasNpc());
		//攻占状态
		PBXZQBuildGuild.Builder occupyGuildBuilder = this.genPBXZQBuildOccupyGuildBuilder();
		if (occupyGuildBuilder!= null ) {
			builder.setOccupy(occupyGuildBuilder);
			builder.setOccupyStartTime(state.getControlStartTime());
			builder.setOccupyEndTime(state.getControlEndTime());
		}
		//奖励状态
		builder.setInitOccupy(this.isInitOccupyed());
		builder.setInitControl(this.isInitControl());
		//联盟成员数量
		String guildId = GuildService.getInstance().getPlayerGuildId(viewerId);
		int count = GuildService.getInstance().getGuildMemberNum(guildId);
		builder.setGuildMembers(count);
		
		return builder;
	}

	
	/**
	 * 小站区建筑信息
	 * @return
	 */
	public PBXZQBuild.Builder genPBXZQBuildBuilder() {
		PBXZQBuild.Builder builder = PBXZQBuild.newBuilder();
		builder.setBuidingId(this.getXzqCfg().getId());
		builder.setPosx(this.getX());
		builder.setPosy(this.getY());
		//建筑状态
		PBXZQBuildStatus buildState = this.getXZQBuildStatus();
		builder.setStatus(buildState);
		//时间
		builder.setTime(this.getXZQTime().genPBXZQTimeInfoBuilder());
		builder.setNextTime(this.getXZQNearlyTime().genPBXZQTimeInfoBuilder());
		//控制信息
		String controlGuild = this.pointInfo.getGuildControl();
		PBXZQBuildGuild.Builder controlBuilder = this.genPBXZQBuildGuildBuilder(controlGuild);
		if (controlBuilder != null) {
			builder.setControl(controlBuilder);
		}
		String controlBefore = this.pointInfo.getLastControlGuild();
		if(!HawkOSOperator.isEmptyString(controlBefore)){
			builder.setControlBefore(controlBefore);
		}
		//刻字
		List<PBXZQBuildRecord>  recordBuilder = this.genPBXZQBuildRecordBuidder();
		if (recordBuilder.size() > 0) {
			builder.addAllRecord(recordBuilder);
		}
		//报名
		List<String> signupGuilds = this.getSignupGuilds();
		if (!signupGuilds.isEmpty() ) {
			builder.addAllSignupGuildIds(signupGuilds);
		}
		//是否有NPC
		builder.setHaveNpc(this.hasNpc());
		//攻破部队
		PBXZQBuildGuild.Builder occupyGuildBuilder = this.genPBXZQBuildOccupyGuildBuilder();
		if (occupyGuildBuilder!= null ) {
			builder.setOccupy(occupyGuildBuilder);
			builder.setOccupyStartTime(state.getControlStartTime());
			builder.setOccupyEndTime(state.getControlEndTime());
		}
		return builder;
	}

	
	/**
	 * 当前攻破联盟
	 * @return
	 */
	private PBXZQBuildGuild.Builder genPBXZQBuildOccupyGuildBuilder(){
		for (String marchId : WorldMarchService.getInstance().getXZQMarchs(getId())) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			Player player = march.getPlayer();
			if(player == null){
				return null;
			}
			return this.genPBXZQBuildGuildBuilder(player.getGuildId());
		}
		return null;
	}
	
	
	/**
	 * 小站区联盟信息
	 * @param guildId
	 * @return
	 */
	private PBXZQBuildGuild.Builder genPBXZQBuildGuildBuilder(String guildId) {
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return null;
		}
		long battlePoint = GuildService.getInstance().getGuildBattlePoint(guildId);
		PBXZQBuildGuild.Builder builder = PBXZQBuildGuild.newBuilder();
		builder.setGuildId(guild.getId());
		builder.setGuildName(guild.getName());
		builder.setGuildTag(guild.getTag());
		builder.setGuildFlag(guild.getFlagId());
		builder.setGuildPower(battlePoint);
		builder.setLeaderName(guild.getLeaderName());
		return builder;
	}

	/**
	 * 小站区指挥官信息
	 * @param playerId
	 * @return
	 */
	private PBXZQBuildCommander.Builder genPBXZQBuildCommanderBuilder(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return null;
		}
		if (HawkOSOperator.isEmptyString(player.getName())) {
			return null;
		}
		PBXZQBuildCommander.Builder builder = PBXZQBuildCommander.newBuilder();
		builder.setPlayerId(playerId);
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		if(HawkOSOperator.isEmptyString(player.getPfIcon())){
			builder.setPfIcon(player.getPfIcon());
		}
		return builder;
	}

	/**
	 * 小站区刻字
	 * @return
	 */
	private List<PBXZQBuildRecord> genPBXZQBuildRecordBuidder() {
		List<PBXZQBuildRecord> blist = new ArrayList<>();
		List<XZQBuildRecord> list = this.pointInfo.getXZQBuildRecords();
		for(XZQBuildRecord re : list){
			PBXZQBuildRecord.Builder builder = PBXZQBuildRecord.newBuilder();
			builder.setServerId(re.getServerId());
			PBXZQBuildGuild controlBuilder = re.getControlGuild();
			if (controlBuilder != null) {
				builder.setFirstControl(controlBuilder);
			}
			PBXZQBuildCommander firstOccy = re.getOccupyPlayerRecord();
			if (firstOccy != null) {
				builder.setFirstOccupy(firstOccy);
			}
			for (PBXZQBuildCommander comm : re.getDamagesRecord()) {
				builder.addDamages(comm);
			}
			blist.add(builder.build());
			
		}
		return blist;
	}
	
	
	@Override
	public WorldPointPB.Builder toBuilder(WorldPointPB.Builder builder,String viewerId) {
		super.toBuilder(builder,viewerId);
		boolean hasMarchStop = hasMarchStop(viewerId);
		builder.setHasMarchStop(hasMarchStop);
		PBXZQBuild.Builder xzqBuildBuilder = genPBXZQBuildBuilder();
		builder.setXzqBuildInfo(xzqBuildBuilder);
		builder.setProtectedEndTime(calProtectedEndTime());
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewerId);
		boolean hasMarchStop = hasMarchStop(viewerId);
		builder.setHasMarchStop(hasMarchStop);
		PBXZQBuildDetail.Builder xzqDetailBuilder = this.genPBXZQBuildDetailBuilder(viewerId);
		builder.setXzqBuildInfo(xzqDetailBuilder);
		builder.setProtectedEndTime(calProtectedEndTime());
		return builder;
	}

	@Override
	public PointData.Builder buildPointData() {
		PointData.Builder builder = super.buildPointData();
		pointInfo.setStateStr(state.serializ());
		builder.setExtryData(pointInfo.serializ());
		return builder;
	}

	@Override
	public void mergeFromPointData(PointData.Builder builder) {
		super.mergeFromPointData(builder);
		try {
			this.pointInfo.mergeFrom(builder.getExtryData());
			this.state = IXZQPointState.valueOf(this, pointInfo.getStateOrdinal());
			this.state.mergeFrom(pointInfo.getStateStr());
			this.pointInfo.loadXZQBuildRecord(this.getXzqCfg());
			if (hasNpc()) {
				createNpc();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	
	@Deprecated
	public void updateXZQCommander(XZQCommander commander){
		this.pointInfo.setCommander(commander);
		this.npcMarch = null;
		this.pointInfo.setHasNpc(false);
	}
}
