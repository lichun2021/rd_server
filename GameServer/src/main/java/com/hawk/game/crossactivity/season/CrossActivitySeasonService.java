package com.hawk.game.crossactivity.season;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;

import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossServerListCfg;
import com.hawk.game.crossactivity.CrossServerInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonInfoResp;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonScoreDataResp;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonScoreRankResp;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonStateData;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;


public class CrossActivitySeasonService  extends HawkAppObj{

	/** 赛季状态数据*/
	private CrossSeaonStateData stateData;
	/** 赛季排行榜数据*/
	private CrossSeasonScoreRank scoreRank;
	
	/** GM命令处理*/
	private CrossSeasonGM gm;

	
	/** 单例*/
	private static CrossActivitySeasonService instance = null;
	public static CrossActivitySeasonService getInstance() {
		return instance;
	}
	public CrossActivitySeasonService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	
	
	/**
	 * 初始化
	 */
	public boolean init(){
		//初始化状态数据
		this.stateData = new CrossSeaonStateData();
		this.stateData.loadData();
		//初始化排行榜数据
		this.scoreRank = new CrossSeasonScoreRank();
		this.scoreRank.initSeason(this.stateData.getSeason());
		//GM
		this.gm = new CrossSeasonGM();
		//检查合服
		this.checkMerge();
		//状态更新
		this.addTickable(new HawkPeriodTickable("CrossActivitySeasonService-stateTick",2 * 1000) {
			@Override
			public void onPeriodTick() {
				stateData.onTick();
			}
		});
		//排行榜更新，因为可能随时会有新服数据进来  5分钟的tick
		this.addTickable(new HawkPeriodTickable("CrossActivitySeasonService-rankTick",60 * 1000 * 5) {
			@Override
			public void onPeriodTick() {
				scoreRank.onTick();
				//检查一下匹配失败发奖
				checkMatchFailReward();
			}
		});
		return true;
	}
	
	
	/**
	 * 匹配
	 * @param crossTermId
	 */
	public void doMatch(int crossTermId) {
		HawkLog.logPrintln("CrossActivitySeasonService-doMatch-start:{},{}",
				crossTermId,GsConfig.getInstance().getServerId());
		//必须两两匹配，直接写死
		CrossConstCfg constCfg = CrossConstCfg.getInstance();
		Map<String, CrossServerInfo> serverMap = RedisProxy.getInstance().getCrossMatchServerBattleMap(crossTermId);
		if (serverMap.isEmpty() || serverMap.size() < 2) {
			return;
		}
		
		long curTime = HawkTime.getMillisecond();
		AtomicInteger atoCrossId = new AtomicInteger(0);
		int seasonTerm = this.stateData.getSeason();
		Map<Integer, String> crossServerListMap = new HashMap<>();
		Map<String,CrossSeasonServerData> seasonMap = CrossSeasonServerData.loadAllData(seasonTerm);
		List<CrossServerInfo> slist = new ArrayList<>();
		slist.addAll(serverMap.values());

		Collections.sort(slist,new Comparator<CrossServerInfo>() {
			@Override
			public int compare(CrossServerInfo o1, CrossServerInfo o2) {
				CrossSeasonServerData sdata1 = seasonMap.get(o1.getServerId());
				CrossSeasonServerData sdata2 = seasonMap.get(o2.getServerId());
				int serverScore1 = 0;
				int serverScore2 = 0;
				if(Objects.nonNull(sdata1)){
					serverScore1 = sdata1.getScore();
				}
				if(Objects.nonNull(sdata2)){
					serverScore2 = sdata2.getScore();
				}
				if(serverScore1 != serverScore2){
					return serverScore1 - serverScore2 > 0?-1 : 1;
				}else if(o1.getBattleValue() != o2.getBattleValue()){
					return o1.getBattleValue() - o2.getBattleValue() > 0? -1 : 1;
				}else if (o1.getOpenServerTime() == o2.getOpenServerTime()) {
					return o1.getServerId().compareTo(o2.getServerId());
				} else {
					return o1.getOpenServerTime() - o2.getOpenServerTime() > 0 ? 1 : -1;
				}
			}
		});
		
		int cnt = slist.size();
		int matchRandomOffset = constCfg.getSeasonMatchOffset();
		for(int i=0;i<= cnt;i++){
			//如果少于配
			if(slist.size() < 2){
				break;
			}
			//取第一名的国家与后第2~X名的国家，进行匹配，由此循环。
			int rmax = Math.min(matchRandomOffset, slist.size() -1);
			rmax = Math.max(1, rmax);
			int index2 = HawkRand.randInt(1,rmax);
			int index1 = 0;
			CrossServerInfo s1 = slist.get(index1);
			CrossServerInfo s2 = slist.get(index2);
			
			slist.remove(s1);
			slist.remove(s2);
		
			StringJoiner sj = new StringJoiner("_");
			sj.add(s1.getServerId());
			sj.add(s2.getServerId());
			
			int crossId = atoCrossId.incrementAndGet();
			crossServerListMap.put(crossId, sj.toString());
		}
		
		RedisProxy.getInstance().addCrossMatchList(crossTermId, crossServerListMap,
				GsConfig.getInstance().getPlayerRedisExpire());
		//Tlog
		for(Entry<Integer, String> entry : crossServerListMap.entrySet()){
			int cId = entry.getKey();
			String servers = entry.getValue();
			LogUtil.logCrossActivityMatch(crossTermId, cId, servers);
			HawkLog.logPrintln("CrossActivitySeasonService-doMatch-battle:{},{}",cId,servers);
		}
		if(slist.size() > 0){
			for(CrossServerInfo cs : slist){
				CrossSeasonServerData sdata = seasonMap.get(cs.getServerId());
				if(Objects.nonNull(sdata)){
					//添加匹配失败发奖记录
					this.addMatchFailReward(cs.getServerId(), seasonTerm, crossTermId);
				}
				HawkLog.logPrintln("CrossActivitySeasonService-doMatch-fail,serverId:{},power:{},openTime:{},openDays:{}", 
						cs.getServerId(),cs.getBattleValue(),cs.getOpenServerTime(),cs.getOpenServerDays(curTime));
			}
		}
		HawkLog.logPrintln("CrossActivitySeasonService-doMatch-end:{},{}",
				crossTermId,GsConfig.getInstance().getServerId());
	}
	
	/**
	 * 添加匹配失败奖励
	 * @param serverId
	 * @param season
	 * @param crossId
	 * @param cnt
	 */
	public void addMatchFailReward(String serverId,int season,int crossId){
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_MATCH_FAIL_REWARD , season, serverId);
		RedisProxy.getInstance().getRedisSession().setString(key, String.valueOf(crossId));
	}
	
	/**
	 * 检查匹配失败发奖
	 */
	public void checkMatchFailReward(){
		if(!this.seasonInOpening()){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		int season = this.stateData.getSeason();
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_MATCH_FAIL_REWARD , season, serverId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(val)){
			return;
		}
		//删除
		RedisProxy.getInstance().getRedisSession().del(key);
		
		int crossTerm = Integer.parseInt(val);
		HawkLog.logPrintln("CrossActivitySeasonService-checkMatchFailReward-start:{},{}",
				crossTerm,GsConfig.getInstance().getServerId());
		try {
			CrossSeasonServerData data = CrossSeasonServerData.loadData(this.stateData.getSeason(), serverId);
			if(Objects.isNull(data)){
				return;
			}
			boolean hasSend = this.hasBattleRewardSendRecord(crossTerm);
			if(hasSend){
				return;
			}
			this.addBattleRewardSendRecord(crossTerm,data.getScore());
			//航海结束发送奖励
			int itemCnt = data.getScore();
			int itemId = CrossConstCfg.getInstance().getSeasonStarItem();
			if(itemCnt > 0 && itemId > 0){
				long curTime = HawkTime.getMillisecond();
				long experiTime = curTime + HawkTime.DAY_MILLI_SECONDS * 7;
				List<ItemInfo> list = new ArrayList<>();
				ItemInfo item = new ItemInfo(ItemType.TOOL_VALUE, itemId, itemCnt);
				list.add(item);
				SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
						.setMailId(MailConst.MailId.CROSS_ACTIVITY_SEASON_MATCH_FAIL_MAIL)
						.setRewards(list)
						.setAwardStatus(Const.MailRewardStatus.NOT_GET)
						.build(), curTime, experiTime);
			}
			HawkLog.logPrintln("CrossActivitySeasonService-checkMatchFailReward-end:{},{},{}",
					crossTerm,itemCnt,GsConfig.getInstance().getServerId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 计算分数
	 * @param crossTermId
	 */
	public void calBattleRlt(int crossTermId){
		if(!this.seasonInOpening()){
			return;
		}
		boolean take = CrossActivitySeasonService.getInstance()
				.takeLock(CrossActivitySeasonConst.LockType.CROSS_SEASON_FIGHT_RLT+":"+crossTermId);
		if(!take){
			return;
		}
		HawkLog.logPrintln("CrossActivitySeasonService-calBattleRlt-start:{},{}",
				crossTermId,GsConfig.getInstance().getServerId());
		
		//全部胜利服务
		Map<String, String> winMap = RedisProxy.getInstance().getCrossWinServer(crossTermId);
		if(Objects.isNull(winMap) || winMap.isEmpty()){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		//全量赛季积分数据
		int seasonId = this.stateData.getSeason();
		Map<String,CrossSeasonServerData> seasonMap = CrossSeasonServerData.loadAllData(seasonId);
		Map<Integer, String> serverMap = RedisProxy.getInstance().getCrossServerList(crossTermId);
		for (Entry<Integer, String> entry : serverMap.entrySet()) {
			CrossServerListCfg serverCfg = new CrossServerListCfg(entry.getKey(), entry.getValue());
			serverCfg.assemble();
			int crossId = serverCfg.getId();
			//王战服-也就是 防守方
			String defendServer = RedisProxy.getInstance().getCrossPresidentServer(crossTermId, crossId);
			String winner = null;
			List<String> loseList = new ArrayList<>();
			for(String serverId : serverCfg.getServerList()){
				if(winMap.containsKey(serverId)){
					winner = serverId;
				}else{
					loseList.add(serverId);
				}
			}
			if(HawkOSOperator.isEmptyString(winner) || loseList.isEmpty()){
				continue;
			}
			//败者给胜者添加积分
			CrossSeasonServerData winServerData = seasonMap.get(winner);
			if(Objects.isNull(winServerData)){
				continue;
			}
			//胜方 是否是防守方
			boolean defenderWin = Objects.equals(winServerData.getServerId(), defendServer);
			
			for(String lose : loseList){
				CrossSeasonServerData loseServerData = seasonMap.get(lose);
				if(Objects.nonNull(loseServerData)){
					//败方 是否是防守方
					boolean defenderLose =  Objects.equals(loseServerData.getServerId(), defendServer);
					int scoreBefLose = loseServerData.getScore();
					int scoreAftLose = loseServerData.getScore();
					int scoreLose = 0;
					//如果失败方，是防守方，则计算减分
					if(defenderLose){
						scoreLose = scoreBefLose * CrossConstCfg.getInstance().getSeasonScoreLosePer() / 100;
						scoreLose = Math.max(scoreLose, 0);
						scoreAftLose = scoreBefLose - scoreLose;
						scoreAftLose = Math.max(scoreAftLose, 0);
						loseServerData.setScore(scoreAftLose);
						loseServerData.saveData();
					}
					CrossSeasonScoreRecord recordLose = new CrossSeasonScoreRecord(seasonId, lose, winner, scoreLose, scoreBefLose,
							scoreAftLose, 0, curTime, crossTermId, crossId ,defenderLose);
					recordLose.saveData();
					
					int scoreBefWin = winServerData.getScore();
					int scoreAftWin = scoreBefWin + scoreLose;
					if(scoreLose > 0){
						winServerData.setScore(scoreAftWin);
						winServerData.saveData();
					}
					CrossSeasonScoreRecord recordWin = new CrossSeasonScoreRecord(seasonId, winner, lose, scoreLose, scoreBefWin,
							scoreAftWin, 1, curTime, crossTermId, crossId, defenderWin);
					recordWin.saveData();
					
					//败方
					CrossSeasonLog.logCrossSeasonScore(lose, winner, 2, scoreLose, scoreBefLose, scoreAftLose, seasonId, crossTermId, crossId, 2);
					//胜方
					CrossSeasonLog.logCrossSeasonScore(winner, lose, 1, scoreLose, scoreBefWin, scoreAftWin, seasonId, crossTermId, crossId, 2);
					//添加日志
					HawkLog.logPrintln("CrossActivitySeasonService-calBattleRlt-battleId:{},{},{},{},{},{},{},{}",
							crossId,lose,scoreLose,scoreBefLose,scoreAftLose,winner,scoreBefWin,scoreAftWin);
				}
			}
		}
		HawkLog.logPrintln("CrossActivitySeasonService-calBattleRlt-end:{},{}",
				crossTermId,GsConfig.getInstance().getServerId());
	}
	
	/**
	 * 检查合服继承积分
	 */
	public void checkMerge(){
		long curTime = HawkTime.getMillisecond();
		String serverId = GsConfig.getInstance().getServerId();
		Long mergeServerTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		if(Objects.isNull(mergeServerTime)){
			return;
		}
		if(curTime < mergeServerTime){
			return;
		}
		List<String> serverList = AssembleDataManager.getInstance().getMergedServerList(serverId);
		if (Objects.isNull(serverList) || serverList.isEmpty()) {
			return;
		}
		CrossSeasonServerData data = CrossSeasonServerData.loadData(this.stateData.getSeason(), serverId);
		if(Objects.isNull(data)){
			return;
		}
		if(data.getMergeTime() >= mergeServerTime){
			return;
		}
		int scoreBef = data.getScore();
		for(String sid : serverList){
			if(sid.equals(serverId)){
				continue;
			}
			CrossSeasonServerData inheriteData = CrossSeasonServerData.loadData(this.stateData.getSeason(), sid);
			if(Objects.isNull(inheriteData)){
				continue;
			}
			if(inheriteData.getInherited() > 0){
				continue;
			}
			//已经被继承过
			inheriteData.setInherited(1);
			inheriteData.saveData();
			if(data.getScore() < inheriteData.getScore()){
				data.setScore(inheriteData.getScore());
				data.setInheritServer(inheriteData.getServerId());
				data.setInheritScore(inheriteData.getScore());
			}
		}
		data.setMergeTime(mergeServerTime);
		data.saveData();
		//日志
		CrossSeasonLog.logCrossSeasonScore(serverId, data.getInheritServer(), 0, 0, scoreBef, data.getScore(), data.getSeason(), 0, 0, 3);
	}
	
	
	
	/**
	 * 发放每期奖励
	 */
	public void sendCrossTermAward(int crossTerm){
		if(!this.seasonInOpening()){
			return;
		}
		HawkLog.logPrintln("CrossActivitySeasonService-sendCrossTermAward-start:{},{}",
				crossTerm,GsConfig.getInstance().getServerId());
		try {
			String serverId = GsConfig.getInstance().getServerId();
			CrossSeasonServerData data = CrossSeasonServerData.loadData(this.stateData.getSeason(), serverId);
			if(Objects.isNull(data)){
				return;
			}
			boolean hasSend = this.hasBattleRewardSendRecord(crossTerm);
			if(hasSend){
				return;
			}
			this.addBattleRewardSendRecord(crossTerm,data.getScore());
			//航海结束发送奖励
			int itemCnt = data.getScore();
			int itemId = CrossConstCfg.getInstance().getSeasonStarItem();
			if(itemCnt > 0 && itemId > 0){
				long curTime = HawkTime.getMillisecond();
				long experiTime = curTime + HawkTime.DAY_MILLI_SECONDS * 7;
				List<ItemInfo> list = new ArrayList<>();
				ItemInfo item = new ItemInfo(ItemType.TOOL_VALUE, itemId, itemCnt);
				list.add(item);
				SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
						.setMailId(MailConst.MailId.CROSS_ACTIVITY_SEASON_BATTLE_REWARD)
						.addContents(data.getScore())
						.setRewards(list)
						.setAwardStatus(Const.MailRewardStatus.NOT_GET)
						.build(), curTime, experiTime);
			}
			
			HawkLog.logPrintln("CrossActivitySeasonService-sendCrossTermAward-end:{},{},{}",
					crossTerm,itemCnt,GsConfig.getInstance().getServerId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		
	}
	
	
	/**
	 * 获取航海战斗发奖记录
	 * @param crossTerm
	 * @return
	 */
	public boolean hasBattleRewardSendRecord(int crossTerm){
		int season = this.stateData.getSeason();
		String serverId = GsConfig.getInstance().getServerId();
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_BATTLE_REWARD,season,serverId);
		String rlt = RedisProxy.getInstance().getRedisSession().hGet(key, String.valueOf(crossTerm));
		if(HawkOSOperator.isEmptyString(rlt)){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 添加航海战斗发奖记录
	 * @param crossTerm
	 * @param cnt
	 */
	public void addBattleRewardSendRecord(int crossTerm,int cnt){
		int season = this.stateData.getSeason();
		String serverId = GsConfig.getInstance().getServerId();
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_BATTLE_REWARD,season,serverId);
		RedisProxy.getInstance().getRedisSession().hSet(key, String.valueOf(crossTerm),String.valueOf(cnt));
	}
	
	
	
	/**
	 * 获取计算锁
	 * @param lockTime
	 */
	public boolean takeLock(String type){
		String serverId = GsConfig.getInstance().getServerId();
		int season = this.stateData.getSeason();
		String lockKey = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_LOCK ,season);
		long rlt = ActivityGlobalRedis.getInstance().getRedisSession(). hSetNx(lockKey, type, serverId);
		if(rlt > 0){
			HawkLog.logPrintln("CrossActivitySeasonService-takeLock-type-{},{}"+type,serverId);
			return true;
		}
		return false;
	}
	
	
	/**
	 * 获取排行榜对象
	 * @return
	 */
	public CrossSeasonScoreRank getRank(){
		return this.scoreRank;
	}
	
	/**
	 * 获取状态对象
	 * @return
	 */
	public CrossSeaonStateData getCrossSeaonStateData(){
		return this.stateData;
	}
	
	
	/**
	 * 是否正在开启中
	 * @return
	 */
	public boolean seasonInOpening(){
		if(this.stateData.getState().getStateEnum() 
				== CrossSeasonStateEnum.OPEN){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 排行榜信息
	 * @param player
	 */
	public void syncRankData(Player player){
		CrossActivitySeasonScoreRankResp.Builder builder = this.scoreRank.buildRank(player.getMainServerId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_ACTIVITY_SEASON_RANK_RESP_VALUE, builder));
	}
	
	
	/**
	 * 积分数据
	 * @param player
	 */
	public void syncScoreData(Player player){
		String serverId = player.getMainServerId();
		CrossSeasonServerData data = CrossSeasonServerData.loadData(this.stateData.getSeason(), serverId);
		List<CrossSeasonScoreRecord> rlist = CrossSeasonScoreRecord.loadAllData(this.stateData.getSeason(), serverId);
		CrossActivitySeasonScoreDataResp.Builder builder = CrossActivitySeasonScoreDataResp.newBuilder();
		long mergerTime = 0;
		if(Objects.nonNull(data)){
			builder.setScore(data.getScore());
			mergerTime = data.getMergeTime();
		}else{
			builder.setScore(0);
		}
		for(CrossSeasonScoreRecord record : rlist){
			if(mergerTime > record.getTime()){
				continue;
			}
			builder.addRecords(record.build());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_ACTIVITY_SEASON_SCORE_RESP_VALUE, builder));
	}
	
	
	/**
	 * 同步数据给玩家
	 * @param player
	 */
	public void syncSeasonData(Player player){
		CrossActivitySeasonInfoResp.Builder builder = CrossActivitySeasonInfoResp.newBuilder();
		CrossActivitySeasonStateData.Builder stateBuilder = CrossActivitySeasonStateData.newBuilder();
		this.stateData.getState().buildState(stateBuilder);
		builder.setStateData(stateBuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_ACTIVITY_SEASON_DATA_RESP_VALUE, builder));
	}
	
	/**
	 * 广播线上玩家
	 */
	public void syncSeasonDataOnlinePlayer(){
		for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
			syncSeasonData(player);
		}
	}
	
	
	/**
	 * GM命令
	 * @param map
	 * @return
	 */
	public String doGm(Map<String, String> map){
		 //只有测试环境可以使用
        if (!GsConfig.getInstance().isDebug()) {
            return "不是测试环境";
        }
        //要执行的gm指令
        return this.gm.onCMD(map);
	}
}
