package com.hawk.game.module.obelisk.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;
import org.hawk.util.HawkClassScaner;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashMultimap;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.config.ObeliskConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.obelisk.service.mission.ObeliskMission;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.module.obelisk.service.mission.type.IObeliskMission;
import com.hawk.game.msg.ObeliskMissionRefreshMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.HP.code;
import com.hawk.game.protocol.Obelisk.PBObelisk;
import com.hawk.game.protocol.Obelisk.PBObeliskListSync;
import com.hawk.game.protocol.Obelisk.PBObeliskMissionState;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 方尖碑服务类
 * @author hf
 */
public class ObeliskService extends HawkAppObj {
	static final Logger logger = LoggerFactory.getLogger("Server");

	private static ObeliskService instance;

	/**方尖碑全服类2 key*/
	final String OBELISK_MISSION_SERVER = "obelisk:global_server:";
	/**方尖碑全局数据 key*/
	final String OBELISK_MISSION_ALL = getLocalIdentify() + ":obelisk:all:";
	/**方尖碑期数 key*/
	final String OBELISK_TERM = getLocalIdentify() + ":obelisk:term:";
	/** 方尖碑开放记录*/
	final String OBELISK_INIT_RECORD = "obelisk:init_record";

	/**
	 * 任务的内存数据
	 */
	private Map<Integer, ObeliskMissionItem> obeliskMissionItemMap = new HashMap<>();

	/**
	 * 方尖碑任务上下文
	 */
	private Map<ObeliskMissionType, IObeliskMission> obeliskMission;

	/**
	 * 方尖碑事件对应的任务集合的组装数据
	 */
	private HashMultimap<Class<? extends MissionEvent>, ObeliskMissionType> eventMissionTypeMap;
	/**
	 * tick 时间间隔
	 */
	private static final int tickDuration = 5000;

	/**期数, 每次合服开新一期 */
	private int termId;
	private long termAM0Date;
	/** 跨服盟总的redis中的 field */
	public static final String CROSS_PRESIDENT = "crossPresident";

	public ObeliskService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	public static ObeliskService getInstance() {
		return instance;
	}

	/**
	 * 初始化
	 */
	public boolean init() {
		List<String> serverList = GlobalData.getInstance().getMergeServerList(GsConfig.getInstance().getServerId());
		// 没有合服 或者不在合服列表里面. 取本服.
		if (CollectionUtils.isEmpty(serverList)) {
			serverList = new ArrayList<>();
			serverList.add(GsConfig.getInstance().getServerId());
		}
		//获取开放记录
		int recordTerm = this.getObeliskOpenRecord();
		// 判断和服的依据
		String termMark = SerializeHelper.collectionToString(serverList);
		HawkTuple3<Integer, String, Long> termAndMark = getObeliskTerm();
		// 没开过, 或者发生了合服
		if (termAndMark.first == 0 || !Objects.equals(termAndMark.second, termMark)) {
			//如果有开放记录，则关闭功能
			long curTime = HawkTime.getMillisecond();
			if(recordTerm > 0 && (curTime > GameUtil.getServerOpenTime() + HawkTime.DAY_MILLI_SECONDS * 10)){
				//如果当前时间 大于
				return true;
			}
			// 如果是第一次开, 使用合服数次数计算第几期,否则期数+1
			int mergeCnt = (int) (Math.log(serverList.size()) / Math.log(2));
			termId = termAndMark.first == 0 ? (mergeCnt + 1) : (termAndMark.first + 1);
			if(GameUtil.getServerOpenTime() > ObeliskConstCfg.getInstance().getNewTimeValue()){
				termId = ObeliskConstCfg.getInstance().getNewServerTermId();
			}
			// 第一次上线从开服时间来
			if (termAndMark.first == 0) {
				termAM0Date = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
			} else {
				// 如果开多期, 以当期开始天0点开始记
				termAM0Date = HawkTime.getAM0Date().getTime();
			}
			updateObeliskTerm(termId, termMark, termAM0Date);
		} else {
			termId = termAndMark.first;
			termAM0Date = termAndMark.third;
		}
		
		eventMissionTypeMap = HashMultimap.create();
		obeliskMission = new HashMap<>();
		String packageName = IObeliskMission.class.getPackage().getName();
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, ObeliskMission.class);
		for (Class<?> cls : classList) {
			try {
				IObeliskMission iObeliskMission = (IObeliskMission) cls.newInstance();
				obeliskMission.put(cls.getAnnotation(ObeliskMission.class).missionType(), iObeliskMission);
				if (iObeliskMission.getObeliskMissionType() != null) {
					eventMissionTypeMap.put(iObeliskMission.getEventClassType(), iObeliskMission.getObeliskMissionType());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		obeliskMissionItemMap = getAllObeliskServerMission(termId);
		if (obeliskMissionItemMap.isEmpty()) {
			initObeliskMissionItem();
		}
		addTickable(new HawkPeriodTickable(tickDuration, tickDuration) {
			@Override
			public void onPeriodTick() {
				missionTick();
			}
		});
		//记录初始化数据
		this.saveObeliskOpenRecord(serverList, termId);
		logger.info("ObeliskService init termId:{},termAM0Date:{} ,obeliskSize:{}", termId, termAM0Date, obeliskMissionItemMap.size());
		return true;
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}
	
	/**
	 * 方尖碑功能是否开放
	 * @return
	 */
	public boolean inOpen(){
		return this.termId > 0;
	}
	
	public int getTermId(){
		return this.termId;
	}
	
	/** 获取本地redis标识
	 * 
	 * @return */
	private String getLocalIdentify() {
		String serverIdentify = GsApp.getInstance().getMergeNotChangeIdentify();
		String serverId = GsConfig.getInstance().getServerId();
		return serverId + ":" + serverIdentify;
	}

	/**周期检测 */
	private void missionTick() {
		List<ObeliskMissionItem> missList = new ArrayList<>(obeliskMissionItemMap.values());

		Collections.sort(missList, Comparator.comparingInt((ObeliskMissionItem item) -> item.getState().getNumber()).reversed());

		for (ObeliskMissionItem missionItem : missList) {

			ObeliskCfg cfg = missionItem.getObeliskCfg();
			ObeliskMissionType obeliskMissionType = ObeliskMissionType.valueOf(cfg.getTaskType());
			if (obeliskMissionType == null) {
				logger.error("ObeliskService missionTick obeliskMissionType = null, TaskType:{}", cfg.getTaskType());
			}
			IObeliskMission iObeliskMission = getIObeliskMission(obeliskMissionType);
			if (iObeliskMission == null) {
				continue;
			}

			iObeliskMission.onTick(missionItem);
			if (missionItem.isChanged()) {
				updateObeliskServerMission(termId, missionItem);
			}
		}
	}

	/**
	 * 通知前端 任务有变化 显示红点或重新拉取数据
	 */
	public void noticeNewPoint() {
		// 在线玩家推有红点
		Set<Player> onlinePlayers = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : onlinePlayers) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.OBELISK_MISSION_RED_POINT));
		}
		logger.info("ObeliskService noticeNewPoint onlinePlayers");
	}

	/**
	 * 关服调用,刷新redis
	 * tick刷新任务到redis频率有点太高了,可以考虑不刷,正常停服保存就好
	 */
	public void onClose() {
		onRefreshMissionRedis();
	}

	/**
	 * 初始化全服任务数据
	 */
	public void initObeliskMissionItem() {
		ConfigIterator<ObeliskCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ObeliskCfg.class);
		while (configIterator.hasNext()) {
			ObeliskCfg cfg = configIterator.next();
			if (termId != cfg.getTermId()) {
				continue;
			}
			int cfgId = cfg.getId();
			ObeliskMissionItem obeliskMissionItem = new ObeliskMissionItem(cfgId);
			// 合服时 要取合服时间
			long startTime = cfg.getOpenTime() + termAM0Date;
			obeliskMissionItem.setStartTime(startTime);
			long endTime = startTime + cfg.getDuration();
			obeliskMissionItem.setEndTime(endTime);
			IObeliskMission mission = getIObeliskMission(ObeliskMissionType.valueOf(cfg.getTaskType()));
			mission.initMission(obeliskMissionItem);
			obeliskMissionItemMap.put(cfgId, obeliskMissionItem);

			logger.info("ObeliskService initObeliskMissionItem cfgId:{},startTime:{} ,endTime:{}", cfgId, startTime, endTime);
		}
	}

	/**
	 * 事件关联的任务类型
	 * @param event
	 * @return
	 */
	public Set<ObeliskMissionType> touchMission(MissionEvent event) {
		return eventMissionTypeMap.get(event.getClass());
	}

	/**
	 * 接收事件,计算任务数据
	 * 刷新剧情任务
	 *
	 * @param msg
	 */
	@MessageHandler
	public void onReceiveMissionEvent(ObeliskMissionRefreshMsg msg) {
		Player player = msg.getPlayer();
		if (player != null && player.isCsPlayer()) {
			return;
		}
		MissionEvent event = msg.getEvent();
		// 事件触发任务列表
		Set<ObeliskMissionType> touchMissions = touchMission(event);
		if (touchMissions == null || touchMissions.isEmpty()) {
			return;
		}
		ConfigIterator<ObeliskCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ObeliskCfg.class);

		for (ObeliskCfg cfg : configIterator) {
			ObeliskMissionType missionType = ObeliskMissionType.valueOf(cfg.getTaskType());
			// 不触发此类型任务
			if (!touchMissions.contains(missionType)) {
				continue;
			}
			// 任务实体
			ObeliskMissionItem entityItem = getObeliskMissionItem(cfg.getId());
			if (entityItem == null) {
				continue;
			}
			// 未开启的任务不处理
			if (entityItem.getState() == PBObeliskMissionState.CLOSED) {
				continue;
			}
			// 刷新任务
			IObeliskMission mission = getIObeliskMission(ObeliskMissionType.valueOf(cfg.getTaskType()));
			mission.refreshMission(player, entityItem, event);
		}
	}

	/** 玩家打开界面 拉去任务*/
	public void getMissionList(Player player) {
		PBObeliskListSync.Builder obeliskListBuilder = PBObeliskListSync.newBuilder();
		if (player.isCsPlayer()) {
			player.sendProtocol(HawkProtocol.valueOf(code.OBELISK_MISSION_SYNC, obeliskListBuilder));
			return;
		}
		int termId = this.getTermId();
		boolean open = this.inOpen();
		obeliskListBuilder.setTermId(termId);
		if(open){
			List<ObeliskMissionItem> missList = new ArrayList<>(obeliskMissionItemMap.values());
			for (ObeliskMissionItem missionItem : missList) {
				ObeliskCfg cfg = missionItem.getObeliskCfg();
				ObeliskMissionType obeliskMissionType = ObeliskMissionType.valueOf(cfg.getTaskType());
				if (obeliskMissionType == null) {
					logger.error("ObeliskService getMissionList obeliskMissionType = null, TaskType:{}", cfg.getTaskType());
				}
				IObeliskMission iObeliskMission = getIObeliskMission(obeliskMissionType);
				if (iObeliskMission == null) {
					continue;
				}

				try {
					PBObelisk.Builder builder = iObeliskMission.buildPbToPlayer(player, missionItem);
					if (builder != null) {
						obeliskListBuilder.addObeliskPB(builder);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(code.OBELISK_MISSION_SYNC, obeliskListBuilder));

	}

	/**
	 * 内存刷进redis
	 */
	public void onRefreshMissionRedis() {
		if (obeliskMissionItemMap.isEmpty()) {
			return;
		}
		saveAllObeliskServerMission(termId, obeliskMissionItemMap);
		logger.info("ObeliskService onRefreshMissionRedis Success");
	}

	public IObeliskMission getIObeliskMission(ObeliskMissionType type) {
		return obeliskMission.get(type);
	}

	/**
	 * 根据id获取方尖碑任务数据
	 * @param obeliskId
	 * @return
	 */
	public ObeliskMissionItem getObeliskMissionItem(int obeliskId) {
		return obeliskMissionItemMap.get(obeliskId);
	}

	public long getTermAM0Date() {
		return termAM0Date;
	}
	
	
	/**
	 * 获取当前方尖碑开放记录
	 * @return
	 */
	public int getObeliskOpenRecord(){
		String serverId = GsConfig.getInstance().getServerId();
		StatisManager.getInstance().incRedisKey(OBELISK_INIT_RECORD);
		String record = RedisProxy.getInstance().getRedisSession().hGet(OBELISK_INIT_RECORD, serverId);
		if(HawkOSOperator.isEmptyString(record)){
			return 0;
		}
		return Integer.parseInt(record);
	}
	
	
	/**
	 * 保存当前方尖碑开放记录
	 * @param serverList
	 * @param termId
	 */
	public void saveObeliskOpenRecord(List<String> serverList,int termId){
		StatisManager.getInstance().incRedisKey(OBELISK_INIT_RECORD);
		Map<String,String> map =new HashMap<>();
		for(String serverId : serverList){
			map.put(String.valueOf(serverId), String.valueOf(termId));
		}
		RedisProxy.getInstance().getRedisSession().hmSet(OBELISK_INIT_RECORD, map, 0);
	}
	

	/**
	 * 保存缓存中方尖碑的任务数据
	 * @param obeliskMap
	 */
	public void saveAllObeliskServerMission(int termId, Map<Integer, ObeliskMissionItem> obeliskMap) {
		StatisManager.getInstance().incRedisKey(OBELISK_MISSION_ALL);
		final String key = obelistRedisKey(termId);
		Map<String, String> map = new HashMap<>();
		obeliskMap.forEach((obeliskId, obeliskMissionItem) -> {
			map.put(String.valueOf(obeliskId), obeliskMissionItem.serializ());
		});
		RedisProxy.getInstance().getRedisSession().hmSet(key, map, 0);
	}

	/** 更新缓存中方尖碑的任务数据*/
	public void updateObeliskServerMission(int termId, ObeliskMissionItem missionItem) {
		final String key = obelistRedisKey(termId);
		RedisProxy.getInstance().getRedisSession().hSet(key, String.valueOf(missionItem.getCfgId()), missionItem.serializ());
		missionItem.setChanged(false);
	}

	private String obelistRedisKey(int termId) {
		String serverId = GsConfig.getInstance().getServerId();
		StatisManager.getInstance().incRedisKey(OBELISK_MISSION_ALL);
		final String key = OBELISK_MISSION_ALL + serverId + ":" + termId;
		return key;
	}

	/**
	 * 获取方尖碑redis数据
	 * @return
	 */
	public Map<Integer, ObeliskMissionItem> getAllObeliskServerMission(int termId) {
		StatisManager.getInstance().incRedisKey(OBELISK_MISSION_ALL);
		final String key = obelistRedisKey(termId);

		Map<Integer, ObeliskMissionItem> obeliskMap = new ConcurrentHashMap<>();
		Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		map.forEach((obeliskId, obeliskStr) -> {
			ObeliskMissionItem obeliskItem = new ObeliskMissionItem();
			obeliskItem.mergeFrom(obeliskStr);
			obeliskMap.put(Integer.valueOf(obeliskId), obeliskItem);
		});
		return obeliskMap;
	}

	/**
	 * 获取方尖碑期数相关数据
	 * @return
	 */
	public HawkTuple3<Integer, String, Long> getObeliskTerm() {
		String serverId = GsConfig.getInstance().getServerId();
		StatisManager.getInstance().incRedisKey(OBELISK_TERM);
		final String key = OBELISK_TERM + serverId;
		String jsonStr = RedisProxy.getInstance().getRedisSession().getString(key);
		if (StringUtils.isEmpty(jsonStr)) {
			return HawkTuples.tuple(0, "", 0L);
		}
		JSONObject obj = JSONObject.parseObject(jsonStr);
		return HawkTuples.tuple(obj.getInteger("termId"), obj.getString("termMark"), obj.getLong("am0date"));
	}

	/**
	 * 方尖碑期数相关数据更新
	 * @param termId
	 * @param termMark
	 * @param am0date
	 */
	public void updateObeliskTerm(int termId, String termMark, long am0date) {
		JSONObject obj = new JSONObject();
		obj.put("termId", termId);
		obj.put("termMark", termMark);
		obj.put("am0date", am0date);

		String serverId = GsConfig.getInstance().getServerId();
		StatisManager.getInstance().incRedisKey(OBELISK_TERM);
		final String key = OBELISK_TERM + serverId;
		RedisProxy.getInstance().getRedisSession().setString(key, obj.toJSONString());
	}

	/**
	 * 全球服的方尖碑数据
	 */
	public void setServerObeliskMission(ObeliskMissionType type, String serverId, String field, String val) {
		StatisManager.getInstance().incRedisKey(OBELISK_MISSION_SERVER);
		final String key = OBELISK_MISSION_SERVER + serverId + ":" + type.intValue();
		RedisProxy.getInstance().getRedisSession().hSet(key, field, val);
	}

	/**
	 * 全球服的方尖碑数据,
	 */
	public String getServerObeliskMission(ObeliskMissionType type, String serverId, String field) {
		StatisManager.getInstance().incRedisKey(OBELISK_MISSION_SERVER);
		final String key = OBELISK_MISSION_SERVER + serverId + ":" + type.intValue();
		return RedisProxy.getInstance().getRedisSession().hGet(key, field);
	}
}
