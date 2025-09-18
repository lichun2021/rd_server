package com.hawk.game.lianmengxzq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.HawkNetworkManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.hawk.game.GsConfig;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildScienceEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.lianmengxzq.timecontroller.IXZQController;
import com.hawk.game.lianmengxzq.timecontroller.IXZQTimeCfg;
import com.hawk.game.lianmengxzq.timecontroller.XZQOpenServerTimeController;
import com.hawk.game.lianmengxzq.timecontroller.XZQTimeController;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.XZQ.PBXZQBuild;
import com.hawk.game.protocol.XZQ.PBXZQBuildStatus;
import com.hawk.game.protocol.XZQ.PBXZQBuildSyncResp;
import com.hawk.game.protocol.XZQ.PBXZQCancelSignupReq;
import com.hawk.game.protocol.XZQ.PBXZQForceColor;
import com.hawk.game.protocol.XZQ.PBXZQForceColorSetResp;
import com.hawk.game.protocol.XZQ.PBXZQGuildWarInfo;
import com.hawk.game.protocol.XZQ.PBXZQInfo;
import com.hawk.game.protocol.XZQ.PBXZQPageShowSyncResp;
import com.hawk.game.protocol.XZQ.PBXZQSignupReq;
import com.hawk.game.protocol.XZQ.PBXZQStage;
import com.hawk.game.protocol.XZQ.PBXZQStatus;
import com.hawk.game.protocol.XZQ.PBXZQTicketsSyncResp;
import com.hawk.game.protocol.XZQ.PBXZQTimeInfo;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResourceService;
import com.hawk.game.world.service.WorldStrongPointService;
import com.hawk.game.world.service.WorldTreasureHuntService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.serialize.string.SerializeHelper;

public class XZQService extends HawkAppObj {
	/** 日志 */
	public static final Logger logger = LoggerFactory.getLogger("Server");
	/** 小站区建筑列表 */
	private Map<Integer, XZQWorldPoint> pointsMap = new ConcurrentHashMap<>();
	/** 小站区建筑列世界点ID */
	private List<Integer> cfgPointIds = new ArrayList<>();
	/** 小站区服务数据 */
	private XZQServiceInfoData serviceInfo = new XZQServiceInfoData();
	/** 小站区增益 */
	private Map<String, XZQEffect> effects = new ConcurrentHashMap<>();
	/** 小战区势力颜色*/
	private Map<String,XZQForceColor> forceColors =  new ConcurrentHashMap<>(); 
	/** 联盟推荐 */
	private Map<String, Integer> signupRecommends =  new ConcurrentHashMap<>();
	/** 阶段检查 */
	private long stage;
	/** 和服时间*/
	private long mergeServerTime;
	/** 通知检查 tick */
	private long noticeCheckTime;
	/** 联盟检查tick */
	private long guildCheckTickTime;
	/** 建筑变化检查tick */
	private long pointUpdateTickTime;
	/** 报名推荐tick */
	private long pointRecommendTickTime;
	/** 变化点 */
	private ConcurrentHashSet<Integer> updatePoints = new ConcurrentHashSet<>();

	public XZQService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	private static XZQService instance = null;
	public static XZQService getInstance() {
		return instance;
	}

	/**
	 * 数据初始化
	 * 
	 * @return
	 */
	public boolean init() {
		//**小战区功能强行关闭了，家了下面3句关闭代码 XZQConstCfg.getInstance().isOpen() 这个方法中加了一个 systemOpenFlag 字段**
		if(!XZQConstCfg.getInstance().isOpen()){
			logger.info("XZQ init, XZQ closing ...");
			return true;
		}
		//*******************小战区功能关闭了，下面是关闭前的代码保留了**********************************//
		String openFlag = XZQRedisData.getInstance().getXZQOpenFlag();
		if(!XZQConstCfg.getInstance().isOpen()){
			if(!HawkOSOperator.isEmptyString(openFlag)){
				//如果开放过，则报错，不支持先开放后关闭
				logger.info("XZQ init err, XZQ has opened...");
				return false;
			}
			return true;
		}
		long openTime = 0;
		if(HawkOSOperator.isEmptyString(openFlag)){
			long time = HawkTime.getMillisecond();
			XZQRedisData.getInstance().updateXZQOpenFlag(String.valueOf(time));
			openTime = time;
		}else{
			openTime = Long.parseLong(openFlag);
			XZQRedisData.getInstance().updateXZQOpenFlag(openFlag);
		}
		serviceInfo = XZQRedisData.getInstance().loadXZQServiceInfo();
		forceColors = XZQRedisData.getInstance().loadXZQFoceColor();
		effects = initZXQEffectControls();
		stage = this.serviceInfo.getXZQStage().getNumber();
		String serverId = GsConfig.getInstance().getServerId();
		Long serverMergeTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		if(serverMergeTime != null){
			this.mergeServerTime = serverMergeTime.longValue();
		}
		XZQOpenServerTimeController.getInstance().init();
		XZQTimeController.getInstance().init();
		XZQGift.getInstance().init();
		// 检查错误点
		this.checkErrorPoint();
		// 创建点
		this.checkAndCreateXZQPointInWorld();
		// 合服检查
		this.checkServerMerge();
		//数据修复
		this.fixXZQ(openTime);
		// service线程tick
		this.addTickable(new HawkPeriodTickable(1000) {
			@Override
			public void onPeriodTick() {
				serviceTick();
			}
		});
		return true;
	}

	//数据修复
	private void fixXZQ(long openTime){
		if(XZQConstCfg.getInstance().getXzqFixTimeValue() > 0){
			String fix = XZQRedisData.getInstance().getXZQFixFlag();
			if(HawkOSOperator.isEmptyString(fix)){
				long curTime = HawkTime.getMillisecond();
				XZQRedisData.getInstance().updateXZQFixFlag(String.valueOf(curTime));
				if(openTime < XZQConstCfg.getInstance().getXzqFixTimeValue()){
					for (XZQWorldPoint point : pointsMap.values()) {	
						point.fixClearReset();
					}
					// 检查控制变动
					this.forceColors.clear();
					XZQRedisData.getInstance().delAllXZQForceColor();
					// 记录控制信息
					this.effects.clear();
					XZQRedisData.getInstance().delAllControlInfo();
				}
			}
		}
	}
	
	/**
	 * 检查创建建筑
	 */
	private void checkAndCreateXZQPointInWorld() {
		Map<Integer, XZQPointCfg> map = new HashMap<>();
		ConfigIterator<XZQPointCfg> pit = HawkConfigManager.getInstance().getConfigIterator(XZQPointCfg.class);
		for (XZQPointCfg pcfg : pit) {
			int pointId = GameUtil.combineXAndY(pcfg.getX(), pcfg.getY());
			map.put(pointId, pcfg);
		}
		cfgPointIds = ImmutableList.copyOf(map.keySet());
		List<Integer> xzqPoints = new ArrayList<>(getXZQPoints());
		// 1 加载所有点已创建点
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.XIAO_ZHAN_QU);
		for (WorldPoint xzq : points) {
			if (!xzqPoints.contains(xzq.getId())) { // 如果配置中没有 删除
				WorldPointService.getInstance().removeWorldPoint(xzq.getId());
				logger.info("XZQ checkAndCreateXZQPointInWorld, delete,x:{},y:{}",xzq.getX(),xzq.getY());
			} else { // 已经创建的
				xzqPoints.remove(Integer.valueOf(xzq.getId()));
			}
		}
		// 2 有没创建的创建一下
		for (int pointId : xzqPoints) {
			int[] pos = GameUtil.splitXAndY(pointId);
			int areaId = WorldUtil.getAreaId(pos[0], pos[1]);
			int resZoneId = WorldUtil.getPointResourceZone(pos[0], pos[1]);
			XZQWorldPoint worldPoint = new XZQWorldPoint(pos[0], pos[1], areaId, resZoneId,
					WorldPointType.XIAO_ZHAN_QU_VALUE);
			WorldPointService.getInstance().createWorldPoint(worldPoint);
			logger.info("XZQ checkAndCreateXZQPointInWorld, cfgId:{},pointId:{},create,x:{},y:{}",worldPoint.getXzqCfg().getId(),pointId,
					worldPoint.getX(),worldPoint.getY());
		}
		List<WorldPoint> servicePoints = WorldPointService.getInstance()
				.getWorldPointsByType(WorldPointType.XIAO_ZHAN_QU);
		for (WorldPoint point : servicePoints) {
			XZQWorldPoint xzqPoint = (XZQWorldPoint) point;
			pointsMap.put(xzqPoint.getId(), xzqPoint);
			logger.info("XZQ checkAndCreateXZQPointInWorld, point load, cfgId:{},pointId:{},create,x:{},y:{}",xzqPoint.getXzqCfg().getId(),xzqPoint.getId(),
					xzqPoint.getX(),xzqPoint.getY());
		}
	}

	/**
	 * 所有小战区点id
	 */
	public List<Integer> getXZQPoints() {
		return cfgPointIds;
	}

	/**
	 * 时钟tick
	 */
	private void worldPointTick() {
		for (XZQWorldPoint point : pointsMap.values()) {
			point.ontick();
		}
	}

	/**
	 * 变化更新时钟
	 */
	private void serviceTick() {
		// 检查活动时间
		this.checkTimeTick();
		// 检查联盟变动
		this.upateGuildDismiss();
		// 检查建筑变动
		this.checkPointUpdate();
		// 检查推荐
		this.checkPointRecommend();
		// 检查通知
		this.checkNotice();
		// 建筑
		this.worldPointTick();
	}

	/**
	 * 检查通知
	 */
	public void checkNotice() {
		long curTime = HawkTime.getMillisecond();
		if (this.noticeCheckTime <= 0) {
			this.noticeCheckTime = HawkTime.getMillisecond();
			return;
		}
		if (curTime - this.noticeCheckTime < 10 * 1000) {
			return;
		}
		long lastTime = this.noticeCheckTime;
		this.noticeCheckTime = curTime;
		PBXZQStatus state = this.getState();
		int termId = this.getXZQTermId();
		IXZQTimeCfg timeCfg = this.getTimeController().getTimeCfg(termId);
		if(timeCfg == null){
			return;
		}
		if (state == PBXZQStatus.XZQ_WAIT_OPEN || state == PBXZQStatus.XZQ_HIDDEN) {
			// 争夺前一小时,等待开战 和 结束阶段都可以直接进入战斗阶段
			long noticeTime = timeCfg.getStartTimeValue() - HawkTime.HOUR_MILLI_SECONDS;
			if (lastTime < noticeTime && curTime >= noticeTime) {
				Const.NoticeCfgId noticeId = Const.NoticeCfgId.XZQ_OPEN_HOUR;
				ChatParames chatParams = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST)
						.setKey(noticeId).build();
				ChatService.getInstance().addWorldBroadcastMsg(chatParams);
			}
		}
		if (state == PBXZQStatus.XZQ_SIGNUP) {
			// 报名前一小时
			long noticeTime = timeCfg.getSignupEndTimeValue() - HawkTime.HOUR_MILLI_SECONDS;
			if (lastTime < noticeTime && curTime >= noticeTime) {
				Const.NoticeCfgId noticeId = Const.NoticeCfgId.XZQ_SIGNUP_HOUR;
				ChatParames chatParams = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST)
						.setKey(noticeId).build();
				ChatService.getInstance().addWorldBroadcastMsg(chatParams);
			}
		}

	}

	/**
	 * 是否结束战斗
	 * 
	 * @return
	 */
	public boolean checkBattleOver() {
		for (int id : this.cfgPointIds) {
			XZQWorldPoint point = this.pointsMap.get(id);
			if (point.getXZQBuildStatus() != PBXZQBuildStatus.XZQ_BUILD_INIT) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检查推荐更新
	 */
	private void checkPointRecommend() {
		long curTime = HawkTime.getMillisecond();
		if (this.pointRecommendTickTime == 0) {
			this.pointRecommendTickTime = curTime;
			return;
		}
		if (curTime - this.pointRecommendTickTime > HawkTime.MINUTE_MILLI_SECONDS * 10) {
			this.pointRecommendTickTime = curTime;
			this.signupRecommends.clear();
			logger.info("XZQ checkPointRecommend, clear....");
		}
	}

	/**
	 * 时间检查
	 */
	public void checkTimeTick() {
		IXZQController timeController = getTimeController();
		timeController.updateState();
		if (this.serviceInfo.getXZQStage().getNumber() != this.stage) {
			this.stage = this.serviceInfo.getXZQStage().getNumber();
			this.broadcastXZQBuildInfo();
		}
		String serverId = GsConfig.getInstance().getServerId();
		Long serverMergeTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		if(serverMergeTime!= null && serverMergeTime.longValue() != this.mergeServerTime){
			this.mergeServerTime = serverMergeTime.longValue();
			this.broadcastXZQBuildInfo();
		}
	}

	
	/**
	 * 添加变更的建筑
	 * 
	 * @param pointId
	 */
	public void addUpdatePoint(int pointId) {
		this.updatePoints.add(pointId);
	}

	/**
	 * 检查同步变更建筑
	 */
	public void checkPointUpdate() {
		long curTime = HawkTime.getMillisecond();
		if (this.pointUpdateTickTime == 0) {
			this.pointUpdateTickTime = curTime;
			return;
		}
		if (curTime - this.pointUpdateTickTime > 2 * 1000) {
			this.pointUpdateTickTime = curTime;
			if (this.updatePoints.size() <= 0) {
				return;
			}
			Set<Integer> update = this.updatePoints;
			this.updatePoints = new ConcurrentHashSet<>();
			PBXZQBuildSyncResp.Builder builder = PBXZQBuildSyncResp.newBuilder();
			for (int id : update) {
				XZQWorldPoint point = this.pointsMap.get(id);
				if (point != null) {
					builder.addBuilds(point.genPBXZQBuildBuilder());
				}
			}
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.XZQ_BUILD_SYNC_S_VALUE, builder);
			HawkNetworkManager.getInstance().broadcastProtocol(protocol, null);
		}
	}

	/**
	 * 检查联盟解散
	 * 
	 * @param guildId
	 */
	public void upateGuildDismiss() {
		long curTime = HawkTime.getMillisecond();
		if (this.guildCheckTickTime == 0) {
			this.guildCheckTickTime = curTime;
			return;
		}
		// 战斗阶段不管
		if (this.getState() == PBXZQStatus.XZQ_OPEN) {
			return;
		}
		if (curTime - this.guildCheckTickTime > 10 * 1000) {
			this.guildCheckTickTime = curTime;
			for (int pid : this.cfgPointIds) {
				XZQWorldPoint point = this.pointsMap.get(pid);
				if (point == null) {
					continue;
				}
				point.checkGuild();
			}
		}
	}

	

	/**
	 * 初始化建筑控制作用号
	 */
	private Map<String, XZQEffect> initZXQEffectControls() {
		Map<String, XZQEffect> map = new ConcurrentHashMap<>();
		Table<String, Integer, Integer> table = XZQRedisData.getInstance().loadControlInfo();
		for (String guildId : table.rowKeySet()) {
			Map<Integer, Integer> levels = table.row(guildId);
			XZQEffect eff = new XZQEffect(guildId);
			eff.updateEffect(levels, false);
			map.put(guildId, eff);
		}
		return map;
	}

	/**
	 * 统计建筑控制数量
	 * 
	 * @return
	 */
	public Table<String, Integer, Integer> getZXQGuildControls() {
		Table<String, Integer, Integer> controls = HashBasedTable.create();
		if(this.pointsMap == null){
			return controls;
		}
		List<XZQWorldPoint> points = new ArrayList<>(this.pointsMap.values());
		for (XZQWorldPoint point : points) {
			String guildControl = point.getGuildControl();
			if (HawkOSOperator.isEmptyString(guildControl)) {
				continue;
			}
			int level = point.getXzqCfg().getLevel();
			int count = 0;
			if (controls.contains(guildControl, level)) {
				count = controls.get(guildControl, level);
			}
			count += 1;
			controls.put(guildControl, level, count);
		}
		return controls;
	}

	/**
	 * 获取作用号值
	 * 
	 * @param playerId
	 * @param effId
	 * @return
	 */
	public int getXZQEffectVal(String playerId, int effId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0;
		}
		if (this.effects == null) {
			return 0;
		}
		XZQEffect effect = this.effects.get(guildId);
		if (effect == null) {
			return 0;
		}
		return effect.getEffectVal(effId);
	}


	
	/**
	 * 同步小站区作用号
	 * 
	 * @param player
	 */
	public void syncXZQEffectVal(Player player) {
		boolean open = XZQConstCfg.getInstance().isOpen();
		if(!open){
			return;
		}
		String guildId = player.getGuildId();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			XZQEffect effect = this.effects.get(guildId);
			if (effect != null) {
				effect.onPlayerSync(player);
			}
		}
	}

	/**
	 * 同步小站区作用号
	 * 
	 * @param player
	 */
	public void syncJoinGuildXZQEffectVal(Player player, String guildId) {
		if (!HawkOSOperator.isEmptyString(guildId)) {
			XZQEffect effect = this.effects.get(guildId);
			if (effect != null) {
				effect.onPlayerJoinGuildSync(player);
			}
		}
	}

	/**
	 * 同步小站区作用号
	 * 
	 * @param player
	 */
	public void syncQuitGuildXZQEffectVal(Player player, String guildId) {
		if (!HawkOSOperator.isEmptyString(guildId)) {
			XZQEffect effect = this.effects.get(guildId);
			if (effect != null) {
				effect.onPlayerQuitGuildSync(player);
			}
		}
	}

	/**
	 * 获取时间控制器
	 * 
	 * @return
	 */
	public IXZQController getTimeController() {
		PBXZQStage stage = this.serviceInfo.getXZQStage();
		switch (stage) {
		case XZQ_CHOOSE:
			return XZQOpenServerTimeController.getInstance();
		case XZQ_CIRCLE:
			return XZQTimeController.getInstance();
		}
		return null;
	}

	/**
	 * 获取建筑
	 * 
	 * @param pointId
	 * @return
	 */
	public XZQWorldPoint getXZQPoint(int pointId) {
		return pointsMap.getOrDefault(pointId, null);
	}

	/**
	 * 报名阶段开启
	 */
	public void onSignup() {
		long time = HawkTime.getMillisecond();
		int termId = this.getTimeController().getActivityTermId(time);
		serviceInfo.setTermId(termId);
		serviceInfo.setState(PBXZQStatus.XZQ_SIGNUP);
		XZQRedisData.getInstance().updateXZQServiceInfo(serviceInfo);
		// 报名广播
		this.onSignupNotice();
		//建筑可以报名
		this.onSignupBuild();
		//广播建筑
		this.broadcastXZQBuildInfo();
		logger.info("XZQ onSignup, termId: {}",termId);
	}

	/**
	 * 等待争夺节点
	 */
	public void onWaiteopen() {
		serviceInfo.setState(PBXZQStatus.XZQ_WAIT_OPEN);
		XZQRedisData.getInstance().updateXZQServiceInfo(serviceInfo);
		//建筑开战等待
		this.onWaitOpenBuild();
		// 清理推荐列表
		this.clearRecommends();
		//广播建筑
		this.broadcastXZQBuildInfo();
		logger.info("XZQ onWaiteopen, termId: {}",this.getXZQTermId());
	}

	/**
	 * 争夺阶段开启
	 */
	public void onOpen() {
		serviceInfo.setState(PBXZQStatus.XZQ_OPEN);
		XZQRedisData.getInstance().updateXZQServiceInfo(serviceInfo);
		// 开战提示
		this.onOpenNotice();
		// 建筑开启争夺
		this.onOpenBuild();
		//广播
		this.broadcastXZQBuildInfo();
		// 数据打点
		this.onOpenTog();
		logger.info("XZQ onOpen, termId: {}",this.getXZQTermId());

	}

	/**
	 * 结束阶段
	 */
	public void onHiden() {
		serviceInfo.setState(PBXZQStatus.XZQ_HIDDEN);
		XZQRedisData.getInstance().updateXZQServiceInfo(serviceInfo);
		//关闭建筑
		this.onHiddenBuild();
		// 重新算buff
		this.updateZXQEffect();
		// 记录控制信息
		this.updateControlInfo();
		// 清除联盟门票
		this.clearGuildTicket();
		// 检查控制变动
		this.updateXZQForceColor(false);
		// 同步建筑
		this.broadcastXZQBuildInfo();
		logger.info("XZQ onHiden, termId: {}",this.getXZQTermId());
	}

	
	
	/**
	 * 报名广播
	 */
	public void onSignupNotice() {
		// 全服系统公告
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.XZQ_SIGNUP_START;
		ChatParames chatParams = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST)
				.setKey(noticeId).build();
		ChatService.getInstance().addWorldBroadcastMsg(chatParams);
		// 联盟有小站区报名权限的管理发邮件
		List<String> guilds = GuildService.getInstance().getGuildIds();
		for (String guildId : guilds) {
			GuildMailService.getInstance().sendGuildMail(guildId, AuthId.XZQ_SIGN_UP,
					MailParames.newBuilder().setMailId(MailId.XZQ_SIGNUP_NOTICE));
		}
	}
	
	/**
	 * 建筑转换到报名状态
	 */
	public void onSignupBuild(){
		int termId = this.getXZQTermId();
		IXZQTimeCfg timeCfg = this.getTimeController().getTimeCfg(termId);
		if(timeCfg == null){
			return;
		}
		for (XZQWorldPoint point : pointsMap.values()) {	
			if(timeCfg.getOpenBuildLevels().contains(point.getXzqCfg().getLevel())){
				point.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_SIGNUP);
				//重置报名
				point.clearSignup();
				logger.info("XZQ onSignupBuild, termId: {},pointId:{},x:{},y:{},level:{}",
						termId,point.getXzqCfg().getId(),point.getX(),point.getY(),point.getXzqCfg().getLevel());
				continue;
			}
			point.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_INIT);
		}
	}
	

	/**
	 * 清除推荐
	 */
	private void clearRecommends() {
		this.signupRecommends.clear();
		logger.info("XZQ onWaitOpenClearRecommends, termId: {}",this.getXZQTermId());
	}
	
	private void onWaitOpenBuild(){
		int termId = this.getXZQTermId();
		IXZQTimeCfg timeCfg = this.getTimeController().getTimeCfg(termId);
		if(timeCfg == null){
			return;
		}
		for (XZQWorldPoint point : pointsMap.values()) {
			if(timeCfg.getOpenBuildLevels().contains(point.getXzqCfg().getLevel())){
				point.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_WAIT_OPEN);
				logger.info("XZQ onWaitOpenBuild, termId: {},pointId:{},x:{},y:{},level:{}",
						termId,point.getXzqCfg().getId(),point.getX(),point.getY(),point.getXzqCfg().getLevel());
				continue;
			}
			point.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_INIT);
		}
	}
	


	/**
	 * 争夺开始系统提示
	 */
	private void onOpenNotice() {
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.XZQ_BATTLE_START;
		ChatParames chatParams = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST).setKey(noticeId)
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(chatParams);
	}

	
	private void onOpenBuild(){
		int termId = this.getXZQTermId();
		IXZQTimeCfg timeCfg = this.getTimeController().getTimeCfg(termId);
		if(timeCfg == null){
			return;
		}
		for (XZQWorldPoint point : pointsMap.values()) {
			if(timeCfg.getOpenBuildLevels().contains(point.getXzqCfg().getLevel())){
				point.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_BATTLE);
				point.updateNpc();
				point.updateControlBefore();
				point.clearnOccupuHistory();
				point.clearDamages();
				logger.info("XZQ onOpenBuild, termId: {},pointId:{},x:{},y:{},level:{}",
						termId,point.getXzqCfg().getId(),point.getX(),point.getY(),point.getXzqCfg().getLevel());
				continue;
			}
			point.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_INIT);
		}
	}
	
	/**
	 * 开始争夺数据打点
	 */
	private void onOpenTog() {
		int termId = this.getXZQTermId();
		Set<String> signupGuilds = new HashSet<>();
		Set<String> controlGuilds = new HashSet<>();
		for (int id : this.cfgPointIds) {
			XZQWorldPoint point = this.pointsMap.get(id);
			signupGuilds.addAll(point.getSignupGuilds());
			String controlGuild = point.getGuildControl();
			if (!HawkOSOperator.isEmptyString(controlGuild)) {
				controlGuilds.add(controlGuild);
			}
		}
		for (String guildId : signupGuilds) {
			if (!GuildService.getInstance().isGuildExist(guildId)) {
				continue;
			}
			int count = GuildService.getInstance().getGuildMemberNum(guildId);
			XZQTlog.XZQGuildParticipate(termId, guildId, count, 1);
		}
		for (String guildId : controlGuilds) {
			if (!GuildService.getInstance().isGuildExist(guildId)) {
				continue;
			}
			int count = GuildService.getInstance().getGuildMemberNum(guildId);
			XZQTlog.XZQGuildParticipate(termId, guildId, count, 2);
		}

	}

	/**
	 * 争夺结束清理联盟门票
	 */
	private void clearGuildTicket() {
		int termId = this.getXZQTermId();
		List<String> guilds = GuildService.getInstance().getGuildIds();
		for (String guildId : guilds) {
			GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
			guild.clearZQTickets();
			int ticketCount = guild.getXZQTickets();
			// 更新推送
			PBXZQTicketsSyncResp.Builder builder = PBXZQTicketsSyncResp.newBuilder();
			builder.setGuildId(guildId);
			builder.setTicketCount(ticketCount);
			this.syncGuildTicketCount(guildId, ticketCount);
			logger.info("XZQ onHidenClearGuildTicket, termId: {},guildId:{}",termId,guildId);
		}
	}

	
	
	/**
	 * 检查颜色设置
	 * 
	 * @param table
	 */
	public void updateXZQForceColor(boolean board) {
		Table<String, Integer, Integer> table = this.getZXQGuildControls();
		Set<String> dels = new HashSet<>();
		int needCount = XZQConstCfg.getInstance().getAllianceColorNeedPointNum();
		for (String guildId : this.forceColors.keySet()) {
			int count = 0;
			Map<Integer, Integer> controls = table.row(guildId);
			if (controls != null) {
				for (int num : controls.values()) {
					count += num;
				}
			}
			if (count < needCount) {
				dels.add(guildId);
			}
		}
		if (dels.size() > 0) {
			for (String del : dels) {
				this.removeForceColor(del);
			}
		}
		if(board){
			// 同步颜色
			PBXZQForceColorSetResp.Builder builder = PBXZQForceColorSetResp.newBuilder();
			builder.addAllColors(this.genXZQForceColorBuilder());
			HawkProtocol proto = HawkProtocol.valueOf(HP.code.XZQ_MAP_FORCE_COLOR_SET_S_VALUE, builder);
			HawkNetworkManager.getInstance().broadcastProtocol(proto, null);
		}
		logger.info("XZQ updateXZQForceColor,termId:{}",this.getXZQTermId());

	}
	
	
	/**
	 * 争夺结束建筑关闭
	 */
	private void onHiddenBuild() {
		for (XZQWorldPoint point : pointsMap.values()) {
			if(point.getXZQBuildStatus() == PBXZQBuildStatus.XZQ_BUILD_BATTLE ){
				//还在进行中的建筑，结束争夺
				point.noCommanderOver();
			}
			//重置状态
			point.updateXZQBuildStatus(PBXZQBuildStatus.XZQ_BUILD_INIT);
			//清楚报名信息
			point.clearSignup();
			//清楚攻破信息
			point.clearnOccupuHistory();
			//清除伤害统计
			point.clearDamages();
			
		}
	}

	/**
	 * 检查建筑控制
	 * 
	 * @param table
	 */
	public void updateZXQEffect() {
		Table<String, Integer, Integer> table = this.getZXQGuildControls();
		// 查看变化
		for (String guildId : table.rowKeySet()) {
			XZQEffect xzqEffect = this.effects.get(guildId);
			Map<Integer, Integer> levels = table.row(guildId);
			if (xzqEffect == null) {
				xzqEffect = new XZQEffect(guildId);
				this.effects.put(guildId, xzqEffect);
			}
			xzqEffect.updateEffect(levels, true);
		}
		// 查询删除
		Set<String> dels = new HashSet<>();
		for (String guildId : this.effects.keySet()) {
			if (table.containsRow(guildId)) {
				continue;
			}
			XZQEffect xzqEffect = this.effects.get(guildId);
			xzqEffect.updateEffect(new HashMap<>(), true);
			dels.add(guildId);
		}
		for (String guildId : dels) {
			this.effects.remove(guildId);
		}
		logger.info("XZQ onHidenUpdateZXQEffect,termId:{}",this.getXZQTermId());
	}

	/**
	 * 小战区信息
	 */
	private void broadcastXZQBuildInfo(){
		//同步小站区信息
		this.broadcastXZQInfo(null);
		//刷新世界点
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.XZQ_POINT_UPDATE) {
			@Override
			public boolean onInvoke() {
				//世界更新建筑
				for (XZQWorldPoint point : pointsMap.values()) {
					WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
				}
				return true;
			}
		});
		
	}

	public PBXZQInfo.Builder genPBXZQInfoBuilder(){
		PBXZQInfo.Builder builder = PBXZQInfo.newBuilder();
		//阶段
		PBXZQStage stage = this.getStage();
		PBXZQStatus state = this.getState();
		builder.setStage(stage);
		builder.setStatus(state);
		//时间配置
		int termId = this.getXZQTermId();
		IXZQTimeCfg timeCfg = null;
		if (state == PBXZQStatus.XZQ_HIDDEN) {
			timeCfg = XZQService.getInstance().getTimeController().getNearlyTimeCfg();
		} else {
			timeCfg = XZQService.getInstance().getTimeController().getTimeCfg(termId);
		}
		builder.setTime(timeCfg.genPBXZQTimeInfoBuilder());
		//选拔期
		if (stage == PBXZQStage.XZQ_CHOOSE) {
			List<PBXZQTimeInfo> chooseTimeList = XZQOpenServerTimeController.getInstance().genPBXZQTimeInfoList();
			builder.addAllChooseStageTimes(chooseTimeList);
		}
		//势力颜色
		List<PBXZQForceColor> colors = this.genXZQForceColorBuilder();
		if (!colors.isEmpty()) {
			builder.addAllColors(colors);
		}
		//建筑
		List<PBXZQBuild> builds = new ArrayList<>();
		for (int id : this.cfgPointIds) {
			XZQWorldPoint point = this.pointsMap.get(id);
			if (point != null) {
				builds.add(point.genPBXZQBuildBuilder().build());
			}
		}
		builder.addAllBuilds(builds);
		return builder;
	}
	
	/**
	 * 广播
	 * @param player
	 */
	public void broadcastXZQInfo(Player player) {
		PBXZQInfo.Builder builder = this.genPBXZQInfoBuilder();
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.XZQ_INFO_S_VALUE, builder);
		if (player != null) {
			player.sendProtocol(protocol);
		} else {
			HawkNetworkManager.getInstance().broadcastProtocol(protocol, null);
		}
	}

	
	/**
	 * 获取展示信息
	 * 
	 * @param player
	 */
	@SuppressWarnings("deprecation")
	public void onShowPage(Player player) {
		boolean open = XZQConstCfg.getInstance().isOpen();
		PBXZQPageShowSyncResp.Builder builder = PBXZQPageShowSyncResp.newBuilder();
		builder.setOpen(open);
		if(open){
			builder.setServerId(player.getServerId());
			builder.setXzqInfo(this.genPBXZQInfoBuilder());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_PAGE_SHOW_INFO_S_VALUE, builder));
	}

	

	
	/**
	 * 报名建筑
	 * 
	 * @param player
	 * @param buildId
	 */
	public void onPlayerSignup(Player player, int buildId, PBXZQSignupReq req) {
		String playerId = player.getId();
		String guildId = player.getGuildId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		
		XZQWorldPoint point = this.pointsMap.get(buildId);
		if (point == null) {
			return;
		}
		//不在报名阶段
		if(point.getXZQBuildStatus() != PBXZQBuildStatus.XZQ_BUILD_SIGNUP){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_NOT_SIGN_UP_PERIOD_VALUE, 0);
			return;
		}
		//没有联盟
		if(Objects.isNull(guild)){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_LIMIT_NO_GUILD_VALUE, 0);
			return;
		}
		//权限不足
		boolean checkGuildAuthority =  GuildService.getInstance().
				checkGuildAuthority(playerId, AuthId.XZQ_SIGN_UP);
		if(!checkGuildAuthority){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_NOT_SIGN_UP_AUTH_LIMIT_VALUE, 0);
			return;
		}
		//联盟人数不足
		int guildMemberCount = GuildService.getInstance().getGuildMemberNum(guildId);
		if(guildMemberCount < point.getXzqCfg().getGuildMemberNeed()){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_SIGN_UP_GUILD_MEMBER_LIMIT_VALUE, 0);
			return;
		}
		//已经报名
		if(point.isSignup(guildId)){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_SIGN_UP_ALREADY_VALUE, 0);
			return;
		}
		//时间间隔
		if(point.checkSignTimeErr(guildId)){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_SIGN_UP_FREQUENTLY_VALUE, 0);
			return;
		}
		int termId = this.getXZQTermId();
		IXZQTimeCfg timeCfg = this.getTimeController().getTimeCfg(termId);
		List<Integer> controlBuilds = this.getControlList(guildId);
		//没有控制建筑的情况
		if(controlBuilds.size() <= 0){
			int signupCount = this.getSignupList(guildId).size();
			//只能报名1级建筑
			if(point.getXzqCfg().getLevel() >2){
				return;
			}
			//报名数量达到上限
			if(signupCount >= timeCfg.getSignupPointNumLimit()){
				player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_SIGN_UP_COUNT_LIMIT_VALUE, 0);
				return;
			}
			//门票不足
			int xzqTickets = guild.getXZQTickets();
			int costTickets = point.getXzqCfg().getNeedTicketNum();
			if(xzqTickets < costTickets){
				player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_SIGN_UP_NO_TICKETS_VALUE, 0);
				return;
			}
			int ticketBefore = guild.getXZQTickets();
			guild.costXZQTickets(costTickets);
			int ticketCount = guild.getXZQTickets();
			// 更新推送
			this.syncGuildTicketCount(guildId, ticketCount);
			// 添加报名
			point.signup(guildId);
			// 更新世界点
			point.updateWorldScene();
			// 保存
			point.notifyUpdate();
			//同步
			player.responseSuccess(HP.code.XZQ_WAR_SIGN_UP_C_VALUE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_WAR_SIGN_UP_S_VALUE, point.genPBXZQBuildDetailBuilder(playerId)));
			
			PBXZQBuildSyncResp.Builder builder = PBXZQBuildSyncResp.newBuilder();
			builder.addBuilds(point.genPBXZQBuildBuilder());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_BUILD_SYNC_S_VALUE,builder ));
			// 发全联盟邮件
			GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
					.setMailId(MailId.XZQ_SIGNUP_SUCESS).addContents(point.getX(), point.getY()));
			// Tlog
			XZQTlog.XZQLogSignup(player, termId, guildId, point.getXzqCfg().getId());
			logger.info("XZQ player signup, termId: {},playerId: {},guildId: {},build:{},ticketBefore:{},ticketCost:{},ticketlast:{}",
					termId,player.getId(),guildId,buildId,ticketBefore,costTickets,ticketCount);
		}else{
			//是否已经达到上限
			int eff = player.getEffect().getEffVal(EffType.GUILD_XZQ_BUILD_CONTROL_VAL);
			int limit = eff + XZQConstCfg.getInstance().getAlliancePointUpperLimitBase();
			int signupCount = this.getSignupList(guildId).size();
			if(controlBuilds.size() + signupCount >= limit){
				//科技没有满级
				int scienceId = XZQConstCfg.getInstance().getXZQSignupScience();
				GuildScienceEntity sentity = GuildService.getInstance().getGuildScience(guildId, scienceId);
				if(sentity != null && !GuildService.getInstance().isScienceMaxLvl(sentity)){
					player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_SIGN_UP_COUNT_LIMIT_SCIENCE_VALUE, 0);
					return;
				}
				//到达上限
				player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_SIGN_UP_COUNT_LIMIT_VALUE, 0);
				return;
			}
			//有控制建筑的情况
			List<Integer> perList = point.getXzqCfg().getPerBuilds();
			boolean hasPre = controlBuilds.removeAll(perList);
			if(!hasPre){
				return;
			}
			// 添加报名
			point.signup(guildId);
			// 更新世界点
			point.updateWorldScene();
			// 保存
			point.notifyUpdate();
			//同步状态
			player.responseSuccess(HP.code.XZQ_WAR_SIGN_UP_C_VALUE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_WAR_SIGN_UP_S_VALUE,point.genPBXZQBuildDetailBuilder(playerId) ));
			
			PBXZQBuildSyncResp.Builder builder = PBXZQBuildSyncResp.newBuilder();
			builder.addBuilds(point.genPBXZQBuildBuilder());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_BUILD_SYNC_S_VALUE,builder ));
			
			// 发全联盟邮件
			GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
					.setMailId(MailId.XZQ_SIGNUP_SUCESS).addContents(point.getX(), point.getY()));
			// Tlog
			XZQTlog.XZQLogSignup(player, termId, guildId, point.getXzqCfg().getId());
			logger.info("XZQ player signup has builds, termId: {},playerId: {},guildId: {},build:{}",
					termId,player.getId(),guildId,buildId);
		}
		
	}

	/**
	 * 取消报名
	 * 
	 * @param player
	 * @param buildId
	 */
	public void onCancelSingup(Player player, int buildId, PBXZQCancelSignupReq req) {
		String playerId = player.getId();
		String guildId = player.getGuildId();
		XZQWorldPoint point = this.pointsMap.get(buildId);
		if (point == null) {
			return;
		}
		//不在报名阶段
		if(point.getXZQBuildStatus() != PBXZQBuildStatus.XZQ_BUILD_SIGNUP){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_NOT_SIGN_UP_PERIOD_VALUE, 0);
			return;
		}
		//没有联盟
		if(!GuildService.getInstance().isGuildExist(guildId)){
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_C_VALUE, Status.XZQError.XZQ_LIMIT_NO_GUILD_VALUE, 0);
			return;
		}
		//权限不够
		boolean checkGuildAuthority = GuildService.getInstance().checkGuildAuthority(playerId, AuthId.XZQ_SIGN_UP);
		if (!checkGuildAuthority) {
			player.sendError(HP.code.XZQ_WAR_SIGN_UP_CANCEL_C_VALUE,
					Status.XZQError.XZQ_CANCEL_SIGN_UP_AUTH_LIMIT_VALUE, 0);
			return;
		}
		int termId = this.serviceInfo.getTermId();
		//没有报名
		if (!point.isSignup(guildId)) {
			return;
		}
		// 取消报名
		point.signupRemove(guildId);
		// 跟新世界点
		point.updateWorldScene();
		// 保存
		point.notifyUpdate();
		//控制列表
		List<Integer> controlBuilds = this.getControlList(guildId);
		if(controlBuilds.size() <= 0){
			// 归还门票
			int costTickets = point.getXzqCfg().getNeedTicketNum();
			GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
			int ticketBefore = guild.getXZQTickets();
			guild.addXZQTickets(costTickets);
			int ticketCount = guild.getXZQTickets();
			// 更新推送
			this.syncGuildTicketCount(guildId, ticketCount);
			logger.info("XZQ player onCancelSingup no build, termId: {},playerId: {},guildId: {},build:{},ticketBefore: {},ticketCost:{},ticketlast:{}",
					termId,player.getId(),guildId,buildId,ticketBefore,costTickets,ticketCount);
		}
		//同步状态
		player.responseSuccess(HP.code.XZQ_WAR_SIGN_UP_CANCEL_C_VALUE);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_WAR_SIGN_UP_CANCEL_S_VALUE, point.genPBXZQBuildDetailBuilder(playerId)));
		
		PBXZQBuildSyncResp.Builder builder = PBXZQBuildSyncResp.newBuilder();
		builder.addBuilds(point.genPBXZQBuildBuilder());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_BUILD_SYNC_S_VALUE,builder ));
		// 发全联盟邮件
		GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
				.setMailId(MailId.XZQ_SIGNUP_CANCEL).addContents(point.getXzqCfg().getX(), point.getXzqCfg().getY()));
		// Tlog
		XZQTlog.XZQGuildSignupCancel(player, termId, guildId, point.getXzqCfg().getId());
		logger.info("XZQ player onCancelSingup, termId: {},playerId: {},guildId: {},build:{}",
						termId,player.getId(),guildId,buildId);
	}

	/**
	 * 放弃小站区建筑
	 * @param player
	 * @param buildId
	 */
	public void onGiveupPoint(Player player, int buildId){
		String playerId = player.getId();
		String guildId = player.getGuildId();
		XZQWorldPoint point = this.pointsMap.get(buildId);
		if (point == null) {
			return;
		}
		//不在循环期
		if(this.getStage() != PBXZQStage.XZQ_CIRCLE){
			player.sendError(HP.code.XZQ_GIVE_UP_C_VALUE, Status.XZQError.XZQ_GIVE_UP_STATUE_LIMIT_VALUE, 0);
			return;
		}
		//不在结束阶段
		if(this.getState() != PBXZQStatus.XZQ_HIDDEN){
			player.sendError(HP.code.XZQ_GIVE_UP_C_VALUE, Status.XZQError.XZQ_GIVE_UP_STATUE_LIMIT_VALUE, 0);
			return;
		}
		//没有联盟
		if(!GuildService.getInstance().isGuildExist(guildId)){
			player.sendError(HP.code.XZQ_GIVE_UP_C_VALUE, Status.XZQError.XZQ_LIMIT_NO_GUILD_VALUE, 0);
			return;
		}
		//权限不够
		boolean checkGuildAuthority = GuildService.getInstance().checkGuildAuthority(playerId, AuthId.XZQ_SIGN_UP);
		if (!checkGuildAuthority) {
			player.sendError(HP.code.XZQ_GIVE_UP_C_VALUE,
					Status.XZQError.XZQ_CANCEL_SIGN_UP_AUTH_LIMIT_VALUE, 0);
			return;
		}
		String controlGuild = point.getGuildControl();
		if(!Objects.equals(controlGuild, guildId)){
			player.sendError(HP.code.XZQ_GIVE_UP_C_VALUE, Status.XZQError.XZQ_GIVE_UP_CONTROL_NUM_LIMIT_VALUE, 0);
			return;
		}
		//控制数量不足
		int controlCount = this.getControlList(guildId).size();
		if(controlCount <= 1){
			player.sendError(HP.code.XZQ_GIVE_UP_C_VALUE, Status.XZQError.XZQ_GIVE_UP_CONTROL_NUM_LIMIT_VALUE, 0);
			return;
		}
		//还有奖励
		int termId = XZQService.getInstance().getXZQTermId();
		Map<String,String> gfitInfo = XZQRedisData.getInstance().getAllXZQGiftInfo(termId, guildId);
		for(Entry<String, String> entry : gfitInfo.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			String[] keyArr = key.split(":");
			String[] valArr = value.split("_");
			int pointId = Integer.parseInt(keyArr[0]);
			int residueNumber = Integer.parseInt(valArr[1]) - Integer.parseInt(valArr[0]);
			if(pointId == point.getXzqCfg().getId() && residueNumber > 0){
				//  还有奖励没有发放
				player.sendError(HP.code.XZQ_GIVE_UP_C_VALUE, Status.XZQError.XZQ_GIVE_UP_GIFT_LIMIT_VALUE, 0);
				return;
			}
		}
		//放弃重置
		point.giveupReset();
		// 重新算buff
		this.updateZXQEffect();
		// 记录控制信息
		this.updateControlInfo();
		// 检查控制变动
		this.updateXZQForceColor(true);
		//返回成功
		player.responseSuccess(HP.code.XZQ_GIVE_UP_C_VALUE);
		//刷新点信息
		PBXZQBuildSyncResp.Builder builder = PBXZQBuildSyncResp.newBuilder();
		builder.addBuilds(point.genPBXZQBuildBuilder());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_BUILD_SYNC_S_VALUE, builder));
		
		ChatParames msg = ChatParames.newBuilder()
				.setChatType(Const.ChatType.SPECIAL_BROADCAST)
				.setKey(Const.NoticeCfgId.XZQ_CONTROL_GIVE_UP)
				.addParms(point.getX(), point.getY(), GuildService.getInstance().getGuildTag(guildId),
						GuildService.getInstance().getGuildName(guildId) )
				.build();
		// 跑马灯
		ChatService.getInstance().addWorldBroadcastMsg(msg);
		
	}
	
	
	
	/**
	 * 玩家是否可以攻击小战区建筑
	 * 
	 * @param player
	 * @param point
	 * @return
	 */
	public int canAttackXZQWorldPoint(Player player, XZQWorldPoint point) {
		String guildId = player.getGuildId();
		if(player.isCsPlayer()){
			return Status.XZQError.XZQ_ATK_LIMIT_NOT_BATTLE_TIME_VALUE;
		}
		if(!GuildService.getInstance().isGuildExist(guildId)){
			return Status.XZQError.XZQ_LIMIT_NO_GUILD_VALUE;
		}
		if(this.getState() != PBXZQStatus.XZQ_OPEN){
			return Status.XZQError.XZQ_ATK_LIMIT_NOT_BATTLE_TIME_VALUE;
		}
		if (point.getXZQBuildStatus() != PBXZQBuildStatus.XZQ_BUILD_BATTLE) {
			return Status.XZQError.XZQ_ATK_LIMIT_NOT_BATTLE_TIME_VALUE;
		}
		if(!Objects.equals(guildId, point.getBeforeControl()) &&
				!point.isSignup(guildId)){
			return Status.XZQError.XZQ_ATK_LIMIT_NOT_BATTLE_TIME_VALUE;
		}
		return 0;
	}

	/**
	 * 联盟战争界面
	 * 
	 * @param guildId
	 */
	public void onXZQGuildWarInfo(Player player) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		int pointId = this.getSignupRecommends(player);
		PBXZQGuildWarInfo.Builder builder = PBXZQGuildWarInfo.newBuilder();
		builder.addRecommends(pointId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_GUILD_WAR_DTAIL_S_VALUE, builder));

	}


	/**
	 * 获取报名建筑个数
	 * 
	 * @param guildId
	 * @return
	 */
	public List<Integer> getSignupList(String guildId) {
		List<Integer> signupList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return signupList;
		}
		for (XZQWorldPoint point : this.pointsMap.values()) {
			if (point.isSignup(guildId)) {
				signupList.add(point.getXzqCfg().getId());
			}
		}
		return signupList;
	}

	/**
	 * 获取控制列表
	 * 
	 * @param guildId
	 * @return
	 */
	public List<Integer> getControlList(String guildId) {
		List<Integer> controlList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return controlList;
		}
		for (XZQWorldPoint point : this.pointsMap.values()) {
			if (point.isControl(guildId)) {
				controlList.add(point.getXzqCfg().getId());
			}
		}
		return controlList;
	}


	/**
	 * 设定势力颜色
	 * 
	 * @param player
	 * @param colorId
	 */
	public void setGuildForceColor(Player player, int colorId) {
		String guildId = player.getGuildId();
		String playerId = player.getId();
		String guildLeader = GuildService.getInstance().getGuildLeaderId(guildId);
		if (HawkOSOperator.isEmptyString(guildLeader)) {
			return;
		}
		if (!playerId.equals(guildLeader)) {
			return;
		}
		if(colorId == 0){
			return;
		}
		if(colorId > XZQConstCfg.getInstance().getAllianceColorList().size()){
			return;
		}
		boolean inCD = this.colorSetInCD(guildId);
		if(inCD){
			player.sendError(HP.code.XZQ_MAP_FORCE_COLOR_SET_C_VALUE, 
					Status.XZQError.XZQ_FORCE_COLOR_IN_CD_VALUE, 0);
			return;
		}
		boolean inUse = this.colorInUser(colorId);
		if(inUse){
			player.sendError(HP.code.XZQ_MAP_FORCE_COLOR_SET_C_VALUE, 
					Status.XZQError.XZQ_FORCE_COLOR_IN_USE_VALUE, 0);
			return;
		}
		int controlCount = this.getControlList(guildId).size();
		if (controlCount < XZQConstCfg.getInstance().getAllianceColorNeedPointNum()) {
			player.sendError(HP.code.XZQ_MAP_FORCE_COLOR_SET_C_VALUE, 
					Status.XZQError.XZQ_FORCE_COLOR_CONTROL_LIMIT, 0);
			return;
		}
		this.setForceColor(guildId, colorId);
		// 同步颜色
		player.responseSuccess(HP.code.XZQ_MAP_FORCE_COLOR_SET_C_VALUE);
		PBXZQForceColorSetResp.Builder builder = PBXZQForceColorSetResp.newBuilder();
		builder.addAllColors(this.genXZQForceColorBuilder());
		HawkProtocol proto = HawkProtocol.valueOf(HP.code.XZQ_MAP_FORCE_COLOR_SET_S_VALUE, builder);
		HawkNetworkManager.getInstance().broadcastProtocol(proto, null);
	}
	
	/**
	 * 势力颜色是否被占用
	 * @param color
	 * @return
	 */
	public boolean colorInUser(int color){
		for(XZQForceColor fc: this.forceColors.values()){
			if(fc.getColorId() == color){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 势力颜色设置是否CD
	 * @param guild
	 * @return
	 */
	public boolean colorSetInCD(String guild){
		XZQForceColor fc = this.forceColors.get(guild);
		if(fc == null){
			return false;
		}
		return fc.inCDTime();
	}
	
	
	/**
	 * 记录势力颜色
	 * @param guildId
	 * @param color
	 */
	public void setForceColor(String guildId,int color){
		XZQForceColor fc = this.forceColors.get(guildId);
		if(fc == null){
			fc = new XZQForceColor();
			fc.setGuildId(guildId);
			this.forceColors.put(guildId, fc);
		}
		fc.setColorId(color);
		fc.setColorTime(HawkTime.getMillisecond());
		XZQRedisData.getInstance().updateXZQForceColor(fc);
	}
	
	/**
	 * 移除势力颜色
	 * @param guildId
	 */
	public void removeForceColor(String guildId){
		this.forceColors.remove(guildId);
		XZQRedisData.getInstance().delXZQForceColor(guildId);
	}
	
	
	/**
	 * 构建势力颜色PB列表
	 * @return
	 */
	public List<PBXZQForceColor> genXZQForceColorBuilder(){
		List<PBXZQForceColor> list = new ArrayList<>();
		for(Entry<String, XZQForceColor> entry : this.forceColors.entrySet()){
			PBXZQForceColor.Builder builder = PBXZQForceColor.newBuilder();
			XZQForceColor color = entry.getValue();
			builder.setGuildId(color.getGuildId());
			builder.setColor(color.getColorId());
			builder.setCdTime(color.getCDEndTime());
			list.add(builder.build());
		}
		return list;
	}

	
	/**
	 * 计算推荐
	 * 
	 * @param guildId
	 * @return
	 */
	public int getSignupRecommends(Player player) {
		String guildId = player.getGuildId();
		if (!GuildService.getInstance().isGuildExist(guildId)) {
			return 0;
		}
		if (this.getState() != PBXZQStatus.XZQ_SIGNUP) {
			return 0;
		}
		if (this.signupRecommends.containsKey(guildId)) {
			int recommend = this.signupRecommends.get(guildId);
			if (recommend > 0) {
				return recommend;
			}
		}
		
		int termId = this.getXZQTermId();
		IXZQTimeCfg timeCfg = this.getTimeController().getTimeCfg(termId);
		List<Integer> controls = this.getControlList(guildId);
		//没有控制建筑的情况
		if(controls.size() <= 0){
			int signupCount = this.getSignupList(guildId).size();
			//报名数量达到上限
			if(signupCount >= timeCfg.getSignupPointNumLimit()){
				return 0;
			}
		}else{
			//是否已经达到上限
			int eff = player.getEffect().getEffVal(EffType.GUILD_XZQ_BUILD_CONTROL_VAL);
			int limit = eff + XZQConstCfg.getInstance().getAlliancePointUpperLimitBase();
			int signupCount = this.getSignupList(guildId).size();
			if(controls.size() + signupCount >= limit){
				return 0;
			}
		}
		List<Integer> signups = new ArrayList<>();
		for (int id : timeCfg.getBuilds()) {
			XZQPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, id);
			if(controls.size() <= 0 && cfg.getLevel() <= 2){
				signups.add(cfg.getId());
				continue;
			}
			if(controls.size() > 0 && !controls.contains(cfg.getId()) && cfg.hasPre(controls)){
				signups.add(cfg.getId());
			}
		}
		if(signups.isEmpty()){
			return 0;
		}
		int signupCount = Integer.MAX_VALUE;
		int pointLevel = 0;
		int pointId = 0;
		HawkRand.randomOrder(signups);
		for (int id : signups) {
			XZQPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, id);
			if (cfg == null) {
				continue;
			}
			int pid = GameUtil.combineXAndY(cfg.getX(), cfg.getY());
			XZQWorldPoint point = this.pointsMap.get(pid);
			if (point == null) {
				continue;
			}
			if (cfg.getLevel() < pointLevel) {
				continue;
			}
			int scount = point.getSignupCount();
			if(cfg.getLevel() > pointLevel){
				pointId = cfg.getId();
				signupCount = scount;
				pointLevel = cfg.getLevel();
				continue;
			}
			if (scount < signupCount) {
				pointId = cfg.getId();
				signupCount = scount;
				pointLevel = cfg.getLevel();
			}
		}
		this.signupRecommends.put(guildId, pointId);
		return pointId;
	}


	/**
	 * 更新记录
	 * @param termId
	 * @param state
	 */
	public void updateXZQTermInfo(int termId, PBXZQStatus state) {
		this.serviceInfo.setTermId(termId);
		this.serviceInfo.setState(state);
		XZQRedisData.getInstance().updateXZQServiceInfo(serviceInfo);
	}

	/**
	 * 获取期数
	 * 
	 * @return
	 */
	public int getXZQTermId() {
		if(this.serviceInfo == null){
			return 0;
		}
		return this.serviceInfo.getTermId();
	}

	/**
	 * 获取当前战区状态
	 * 
	 * @return
	 */
	public PBXZQStatus getState() {
		if(this.serviceInfo == null){
			return PBXZQStatus.XZQ_HIDDEN;
		}
		return serviceInfo.getState();
	}

	/**
	 * 获取小战区阶段
	 * 
	 * @return
	 */
	public PBXZQStage getStage() {
		return serviceInfo.getXZQStage();
	}

	/**
	 * 同步小站门票个数
	 * 
	 * @param guildId
	 * @param ticketCount
	 */
	private void syncGuildTicketCount(String guildId, int ticketCount) {
		PBXZQTicketsSyncResp.Builder builder = PBXZQTicketsSyncResp.newBuilder();
		builder.setGuildId(guildId);
		builder.setTicketCount(ticketCount);
		HawkProtocol proto = HawkProtocol.valueOf(HP.code.XZQ_TICKETS_SYNC_S_VALUE, builder);
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			Player member = GlobalData.getInstance().getActivePlayer(playerId);
			if (member == null) {
				continue;
			}
			member.sendProtocol(proto);
		}
	}

	/**
	 * 获取伤兵比例
	 * 
	 * @return
	 */
	public int getHurtRate() {
		int termId = this.getXZQTermId();
		IXZQTimeCfg cfg = this.getTimeController().getTimeCfg(termId);
		if(cfg != null){
			return cfg.getHurtRate();
		}
		return GsConst.RANDOM_MYRIABIT_BASE;
	}

	

	/**
	 * 更新控制信息
	 * 
	 * @param controls
	 */
	private void updateControlInfo() {
		Map<String, String> controls = new HashMap<>();
		for (int id : this.cfgPointIds) {
			XZQWorldPoint point = this.pointsMap.get(id);
			if (point == null) {
				continue;
			}
			String guildControl = point.getGuildControl();
			if (HawkOSOperator.isEmptyString(guildControl)) {
				continue;
			}
			controls.put(String.valueOf(point.getXzqCfg().getId()), guildControl);
		}
		XZQRedisData.getInstance().delAllControlInfo();
		if (controls.size() > 0) {
			XZQRedisData.getInstance().updateControl(controls);
		}
	}

	
	/**
	 * 合服操作
	 */
	public void checkServerMerge() {
		String serverId = GsConfig.getInstance().getServerId();
		List<String> serverList = GlobalData.getInstance().getMergeServerList(serverId);
		//没有合服 或者不在合服列表里面. 取本服.
		if (CollectionUtils.isEmpty(serverList)) {
			return;
		}
		// 判断和服的依据
		String termMark = SerializeHelper.collectionToString(serverList);
		String data = XZQRedisData.getInstance().getMegerServerData();
		if (!HawkOSOperator.isEmptyString(data) && data.equals(termMark)) {
			return;
		}
		XZQRedisData.getInstance().updateMegerServerData(termMark);
		for (int id : this.cfgPointIds) {
			XZQWorldPoint point = this.pointsMap.get(id);
			if (point == null) {
				continue;
			}
			point.mergerServerClear();
		}
		// 检查控制变动
		this.forceColors.clear();
		XZQRedisData.getInstance().delAllXZQForceColor();
		// 记录控制信息
		this.effects.clear();
		XZQRedisData.getInstance().delAllControlInfo();
	}

	
	
	
	/**
	 * 检测错误点(由于航海要塞是后加的常驻建筑，所以要把占用范围内的点清掉)
	 */
	public void checkErrorPoint() {
		//延展清除，使周围不互相叠压
		int clearExt = 3;
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.XIAO_ZHAN_QU);
		Set<Integer> pset = new HashSet<>();
		for(WorldPoint point : points){
			pset.add(point.getId());
		}
		List<XZQPointCfg> plist = HawkConfigManager.getInstance().getConfigIterator(XZQPointCfg.class).toList();
		for (XZQPointCfg point : plist) {
			//已经落下的点不处理
			if(pset.contains(GameUtil.combineXAndY(point.getX(), point.getY()))){
				continue;
			}
			List<Integer> pointIds = getAroundPointId(point.getX(), point.getY(),
					point.getGridCnt() + clearExt);
			for (Integer pointId : pointIds) {
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
				if (worldPoint == null) {
					continue;
				}
				if (worldPoint.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
					continue;
				}
				removeErrorPoint(worldPoint);
			}
		}
	}

	
	public void removeErrorPoint(WorldPoint point) {
		switch (point.getPointType()) {
		// 玩家城点
		case WorldPointType.PLAYER_VALUE:
			WorldPlayerService.getInstance().removeCity(point.getPlayerId(), true);
			XZQTlog.XZQInitRemove(point.getId(),WorldPointType.PLAYER_VALUE, point.getPlayerId(), "", 0);
			logger.info("XZQ removeErrorPoint playerCity,pointx: {},pointy: {}, playerId: {}",
					point.getX(),point.getY(),point.getPlayerId());
			break;
		//驻扎
		case WorldPointType.QUARTERED_VALUE:
			if (!HawkOSOperator.isEmptyString(point.getMarchId())) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(point.getMarchId());
				if (march != null) {
					march.onMarchCallback(HawkTime.getMillisecond(), point);
				} else {
					logger.info("XZQ removeErrorPoint playerQuartered, playerId: {}, marchId: {}", point.getPlayerId(), point.getMarchId());
				}
				XZQTlog.XZQInitRemove(point.getId(),WorldPointType.QUARTERED_VALUE, point.getPlayerId(), "", 0);
				logger.info("XZQ removeErrorPoint playerQuartered,pointx: {},pointy: {}, playerId: {}",
						point.getX(),point.getY(),point.getPlayerId());
			}
			break;
		//资源宝库
		case WorldPointType.RESOURC_TRESURE_VALUE:
			WorldPointService.getInstance().removeWorldPoint(point.getX(), point.getY());
			XZQTlog.XZQInitRemove(point.getId(),WorldPointType.RESOURC_TRESURE_VALUE, point.getPlayerId(),point.getGuildId(), 0);
			logger.info("XZQ removeErrorPoint resourceTresure,pointx: {},pointy: {}, playerId: {},guildId:{},resourceId: {}",
					point.getX(),point.getY(),point.getPlayerId(),point.getGuildId(),point.getResourceId());
			break;
		// 资源点
		case WorldPointType.RESOURCE_VALUE:
			String marchId = "";
			String playerId = "";
			if (!HawkOSOperator.isEmptyString(point.getMarchId())) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(point.getMarchId());
				if (march != null) {
					march.onMarchCallback(HawkTime.getMillisecond(), point);
					marchId = march.getMarchId();
					playerId = march.getPlayerId();
				} else {
					logger.info("XZQ removeErrorPoint resourcePoint, playerId: {}, marchId: {}", point.getPlayerId(), point.getMarchId());
				}
			}
			WorldResourceService.getInstance().removeResourcePoint(point, true);
			logger.info("XZQ removeErrorPoint resourcePoint,pointx: {},pointy: {},resourceId: {},marchId: {},playerId: {}",
					point.getX(),point.getY(),point.getResourceId(),marchId,playerId);
			break;
		//寻宝怪
		case WorldPointType.TH_MONSTER_VALUE:
			WorldTreasureHuntService.getInstance().notifyMonsterRemove(point.getId(), null, null);
			logger.info("XZQ removeErrorPoint thMonster,pointx: {},pointy: {}",
					point.getX(),point.getY());
			break;
		//寻宝资源点
		case WorldPointType.TH_RESOURCE_VALUE:
			String thMarchId = "";
			String thPlayerId = "";
			if (!HawkOSOperator.isEmptyString(point.getMarchId())) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(point.getMarchId());
				if (march != null) {
					march.onMarchCallback(HawkTime.getMillisecond(), point);
					thMarchId = march.getMarchId();
					thPlayerId = march.getPlayerId();
				} else {
					logger.info("XZQ removeErrorPoint thResourcePoint, playerId: {}, marchId: {}", point.getPlayerId(), point.getMarchId());
				}
			}
			WorldTreasureHuntService.getInstance().notifyResRemove(point.getId(), null, null);
			logger.info("XZQ removeErrorPoint thResourcePoint,pointx: {},pointy: {},resourceId: {},marchId: {},playerId: {}",
					point.getX(),point.getY(),point.getResourceId(),thMarchId,thPlayerId);
			break;
		// 野怪点
		case WorldPointType.MONSTER_VALUE:
			// 野怪配置
			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class,
					point.getMonsterId());
			// 单打的怪
			if (monsterCfg.getType() == MonsterType.TYPE_1_VALUE || monsterCfg.getType() == MonsterType.TYPE_2_VALUE) {
				WorldMonsterService.getInstance().notifyMonsterKilled(point);
				logger.info("XZQ removeErrorPoint monster,pointx: {},pointy: {},monster: {}",
						point.getX(),point.getY(),point.getMonsterId());
				// 集结的怪
			} else {
				WorldPointService.getInstance().removeWorldPoint(point.getId());
				AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
				area.removeMonsterBoss(point.getId());
				logger.info("XZQ removeErrorPoint monsterBoss,pointx: {},pointy: {},monster: {}",
						point.getX(),point.getY(),point.getMonsterId());
			}
			break;
		// 迷雾要塞
		case WorldPointType.FOGGY_FORTRESS_VALUE:
			WorldFoggyFortressService.getInstance().notifyFoggyFortressKilled(point.getId());
			logger.info("XZQ removeErrorPoint foggyFortress,pointx: {},pointy: {},monster: {}",
					point.getX(),point.getY(),point.getMonsterId());
			break;
		// 据点
		case WorldPointType.STRONG_POINT_VALUE:
			if (!HawkOSOperator.isEmptyString(point.getMarchId())) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(point.getMarchId());
				if (march != null) {
					march.onMarchCallback(HawkTime.getMillisecond(), point);
				} else {
					logger.info("XZQ removeErrorPoint Strongpoint, playerId: {}, marchId: {}", point.getPlayerId(), point.getMarchId());
				}
			}
			WorldStrongPointService.getInstance().removeStrongpoint(point, true);
			logger.info("XZQ removeErrorPoint Strongpoint,pointx: {},pointy: {},monster: {}",
					point.getX(),point.getY(),point.getMonsterId());
			break;
		case WorldPointType.GUILD_TERRITORY_VALUE:
			// 玩家联盟
			int buildingId = point.getBuildingId();
			String guildBuildId = point.getGuildBuildId();
			String guildId = point.getGuildId();
			TerritoryType type = TerritoryType.valueOf(buildingId);
			int x = point.getX();
			int y = point.getY();
			switch (type) {
			case GUILD_BASTION:
				GuildManorObj obj = GuildManorService.getInstance().getGuildManorByPoint(point);
				if (obj == null) {
					WorldPointService.getInstance().removeWorldPoint(x, y);
					logger.info("XZQ removeErrorPoint GUILD_BASTION GuildManorObj null guildBuild,pointx: {},pointy: {},guildId:{},type:{}",
							point.getX(),point.getY(),guildId,TerritoryType.GUILD_BASTION_VALUE);
					break;
				}
				//日志记录
				rmGuildManorTlog(guildId, obj.getEntity().getManorId());
				// 先调用地图点移除
				GuildManorService.getInstance().rmGuildManor(guildId, obj.getEntity().getManorId());
				// 再修改领地本身的影响
				obj.onMonorRemove();
				break;
			case GUILD_BARTIZAN:
			case GUILD_MINE:
			case GUILD_STOREHOUSE:
				String[] keys = guildBuildId.split("_");
				int idex = Integer.parseInt(keys[2]);
				IGuildBuilding building = GuildManorService.getInstance().getBuildingByTypeAndIdx(guildId, idex, type);
				if (building == null) {
					WorldPointService.getInstance().removeWorldPoint(x, y);
					logger.info("XZQ removeErrorPoint IGuildBuilding null guildBuild,pointx: {},pointy: {},guildId:{},type:{}",
							point.getX(),point.getY(),guildId,guildBuildId);
					break;
				}
				GuildManorService.getInstance().removeManorBuilding(building);
				XZQTlog.XZQInitRemove(point.getId(),WorldPointType.GUILD_TERRITORY_VALUE, "", guildId, building.getBuildType().getNumber());
				logger.info("XZQ removeErrorPoint guildBuild,pointx: {},pointy: {},guildId:{},type:{},index:{}",
						point.getX(),point.getY(),guildId,type,guildBuildId);
				break;
			default:
				WorldPointService.getInstance().removeWorldPoint(point.getX(), point.getY());
				XZQTlog.XZQInitRemove(point.getId(),point.getPointType(), point.getPlayerId(), point.getGuildId(), 0);
				logger.info("XZQ removeErrorPoint default,pointx: {},pointy: {}, playerId: {},guildId: {},guildBuild:{}",
						point.getX(),point.getY(),point.getPlayerId(),point.getGuildId(),point.getGuildBuildId());
				break;
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 移除联盟领地记录
	 * 
	 * @param guildId
	 * @param manorId
	 */
	public void rmGuildManorTlog(String guildId, String manorId) {
		// 联盟领地(大本)pointId
		int pointId = GuildManorService.getInstance().getManorPostion(manorId);

		WorldPoint wp = WorldPointService.getInstance().getWorldPoint(pointId);
		if (wp == null || !wp.getGuildId().equals(guildId)) {
			return;
		}
		if (wp.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE) {
			return;
		}
		List<IGuildBuilding> builds = GuildManorService.getInstance().getGuildBuildings(guildId);
		// 移除其它建筑
		for (IGuildBuilding build : builds) {
			if (build.isPlaceGround() && 
					GuildManorService.getInstance().isGuildBuildCanRmove(build, guildId, manorId)) {
				XZQTlog.XZQInitRemove(wp.getId(),WorldPointType.GUILD_TERRITORY_VALUE, "", guildId, build.getBuildType().getNumber());
				logger.info("XZQ removeErrorPoint guildBuild,pointx: {},pointy: {},guildId:{},type:{}",
						wp.getX(),wp.getY(),guildId,wp.getGuildBuildId());
			}
		}
		// 移除大本
		XZQTlog.XZQInitRemove(wp.getId(),WorldPointType.GUILD_TERRITORY_VALUE, "", guildId,TerritoryType.GUILD_BASTION_VALUE);
		logger.info("XZQ removeErrorPoint guildBastion,pointx: {},pointy: {},guildId:{},type:{}",
				wp.getX(),wp.getY(),guildId,wp.getGuildBuildId());
	}

	public List<Integer> getAroundPointId(int centerX, int centerY, int radius) {
		List<Integer> pointIds = new ArrayList<>();
		pointIds.add(GameUtil.combineXAndY(centerX, centerY));
		// 取x轴上的点
		for (int i = 1; i <= radius - 1; i++) {
			int x1 = centerX + i;
			int x2 = centerX - i;
			pointIds.add(GameUtil.combineXAndY(x1, centerY));
			pointIds.add(GameUtil.combineXAndY(x2, centerY));
		}
		// 取y轴上的点
		for (int i = 1; i <= radius - 1; i++) {
			int y1 = centerY + i;
			int y2 = centerY - i;
			pointIds.add(GameUtil.combineXAndY(centerX, y1));
			pointIds.add(GameUtil.combineXAndY(centerX, y2));
		}
		// 取其它点
		for (int i = 0; i <= radius - 1; i++) {
			for (int j = 0; j <= radius - 1 - i; j++) {
				if (i == 0 || j == 0) {
					continue;
				}
				int x1 = centerX + i;
				int x2 = centerX - i;
				int y1 = centerY + j;
				int y2 = centerY - j;
				pointIds.add(GameUtil.combineXAndY(x1, y1));
				pointIds.add(GameUtil.combineXAndY(x1, y2));
				pointIds.add(GameUtil.combineXAndY(x2, y1));
				pointIds.add(GameUtil.combineXAndY(x2, y2));
			}
		}
		return pointIds;
	}
}
