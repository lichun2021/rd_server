package com.hawk.game.service.starwars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.list.CursorableLinkedList;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.common.ServerInfo;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.StarWarsConstCfg;
import com.hawk.game.config.StarWarsGiftCfg;
import com.hawk.game.config.StarWarsOfficerCfg;
import com.hawk.game.config.StarWarsPartCfg;
import com.hawk.game.config.StarWarsTimeCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftRecordResp;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftRecordStruct;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftResp;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftStruct;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsKingRecordResp;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsKingRecordStruct;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerResp;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerStruct;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerTeamStruct;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.starwars.StarWarsConst.SWActivityState;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 星球大战官职任命和礼包
 * 霸主 的part 为0
 * 三军统帅 的 team为0
 * 统帅的part 和team 不为0.
 * @author jm
 *
 */
public class StarWarsOfficerService {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	static StarWarsOfficerService instance = new StarWarsOfficerService();

	/**
	 * {part, {team, player}}
	 */
	Map<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> partOfficerMap = new ConcurrentHashMap<>();
	/**
	 * {playerId, {part, team}}
	 */
	Map<String, List<HawkTuple2<Integer, Integer>>> playerOfficerMap = new ConcurrentHashMap<>();
	/**
	 * 定时拉取官职信息.
	 */
	int officerPullTickNum = 0;
	/**
	 * 定时更新官职信息
	 */
	int officerPushTickNum = 0;
	/**
	 * 定时拉取世界的信息.
	 */
	int worldPullTickNum = 0;
	/**
	 * 清除标志.
	 */
	private boolean hasClean;
	/**
	 * 最后的读取时间.
	 */
	private AtomicInteger lastReadTime = new AtomicInteger();
	
	private StarWarsOfficerService() {

	}

	public static StarWarsOfficerService getInstance() {
		return instance;
	}

	public boolean init() {
		handleMergeServerUnsetOfficer();
		loadOrReloadOfficer();
		return true;
	}
	
	/**
	 * 处理合服卸载官职.
	 */
	private void handleMergeServerUnsetOfficer() {
		Map<String, Integer> map = RedisProxy.getInstance().loadAllMergeServerUnsetOfficerInfo();
		Map<String, Integer> handleMap = new HashMap<>();
		//字符串
		 
		for (Entry<String, Integer> entry : map.entrySet()) {
			int bitValue = entry.getValue();
			boolean handled = false;
			if (!GlobalData.getInstance().isExistPlayerId(entry.getKey())) {
				continue;
			}
			
			//已经处理过了
			if ((bitValue & GsConst.StarWarsConst.UNSET_OFFICER_ALREADY_HANDLED) > 0) {
				continue;
			}
			
			//因为分区
			if ((bitValue & GsConst.StarWarsConst.UNSET_OFFICER_PART_KING) > 0) {
				handled = true;
				MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(entry.getKey()).
						setMailId(MailId.SW_PART_MERGE).build());
			}
			
			if ((bitValue & GsConst.StarWarsConst.UNSET_OFFICER_WORLD_KING) > 0) {
				handled = true;
				MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(entry.getKey()).
						setMailId(MailId.SW_WORLD_MERGE).build());
			}
			
			if ((bitValue & GsConst.StarWarsConst.UNSET_OFFICER_COMMON) > 0 && !handled) {
				handled = true;
				MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(entry.getKey()).
						setMailId(MailId.SW_SELF_MERGE).build());
			}
			
			if (handled) {
				handleMap.put(entry.getKey(), entry.getValue() & GsConst.StarWarsConst.UNSET_OFFICER_ALREADY_HANDLED);
			}
		}
		
		if (!handleMap.isEmpty()) {
			logger.info("handle merge server unset officer handleMap:{}", handleMap);
			RedisProxy.getInstance().updateMergeServerUnsetOfficerInfo(handleMap);
		}
	}	

	/**
	 * 最开始有考虑做分区加载,玩家的数据有多条，修改数据可能会并发异常，所以就采取了全量加载的方式，也就是一条redis操作变成9条。不过不是很频繁.
	 * 
	 * @param partId
	 */
	public void loadOrReloadOfficer() {
		try {
			Set<Integer> partSet = AssembleDataManager.getInstance().getStarWarsPartSet();	
			Map<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> partOfficerMap = new ConcurrentHashMap<>();
			Map<String, List<HawkTuple2<Integer, Integer>>> playerPartOfficerMap = new ConcurrentHashMap<>();
			Map<String, List<HawkTuple2<Integer, Integer>>> copyPlayerPartOfficerMap = this.playerOfficerMap;
			for (Integer part : partSet) {
				Map<Integer, StarWarsOfficerStruct.Builder> builderMap = RedisProxy.getInstance().getAllStarWarsOfficer(part);
				partOfficerMap.put(part, builderMap);
				for (Entry<Integer, StarWarsOfficerStruct.Builder> builderEntry : builderMap.entrySet()) {
					if (builderEntry.getValue().hasPlayerInfo()) {
						String playerId = builderEntry.getValue().getPlayerInfo().getPlayerId();
						List<HawkTuple2<Integer, Integer>> tupleList = playerPartOfficerMap.get(playerId);
						if (tupleList == null) {
							tupleList = new ArrayList<>();
							playerPartOfficerMap.put(playerId, tupleList);
						}						
						tupleList.add(new HawkTuple2<Integer, Integer>(part, builderEntry.getKey()));
					}
				}
			}

			this.partOfficerMap = partOfficerMap;
			this.playerOfficerMap = playerPartOfficerMap;

			// 计算两次map玩家的官职变化.
			checkPlayerOfficerChange(copyPlayerPartOfficerMap, playerPartOfficerMap);
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}
	
	private List<Integer> tupleList2OfficerList(List<HawkTuple2<Integer, Integer>> tuples) {
		List<Integer> idList = new ArrayList<>();
		for (HawkTuple2<Integer, Integer> tuple : tuples) {
			StarWarsOfficerCfg cfg = this.getOfficerCfg(tuple.first, tuple.second);
			if (cfg != null) {
				idList.add(cfg.getId());
			}
		}
		
		return idList;
	}
	private void checkPlayerOfficerChange(Map<String, List<HawkTuple2<Integer, Integer>>> copyPlayerPartOfficerMap,
			Map<String, List<HawkTuple2<Integer, Integer>>> playerPartOfficerMap) {
		// 同步已经删除的和发生变化的.
		for (Entry<String, List<HawkTuple2<Integer, Integer>>> sourceEntry : copyPlayerPartOfficerMap.entrySet()) {
			List<HawkTuple2<Integer, Integer>> tupleList = playerPartOfficerMap.get(sourceEntry.getKey());
			//说明是官职没了,走这里.
			if (tupleList == null) {
				List<Integer> idList = this.tupleList2OfficerList(sourceEntry.getValue());
				synEffect(sourceEntry.getKey(), idList);
			} else {
				if (tupleList.equals(sourceEntry.getValue())) {
					continue;
				} else {
					List<Integer> officerList = new ArrayList<>();
					officerList.addAll(this.tupleList2OfficerList(sourceEntry.getValue()));
					officerList.addAll(this.tupleList2OfficerList(tupleList));
					synEffect(sourceEntry.getKey(), officerList);
				}
			}
		}

		for (Entry<String, List<HawkTuple2<Integer, Integer>>> targetEntry : playerPartOfficerMap.entrySet()) {
			// 新加的
			if (copyPlayerPartOfficerMap.containsKey(targetEntry.getKey())) {
				continue;
			}
			
			List<Integer> idList = this.tupleList2OfficerList(targetEntry.getValue());
			synEffect(targetEntry.getKey(), idList);
		}

	}
	
	/**
	 * 战斗结束.
	 * @param player
	 * @param part
	 */
	public void onFighterOver(String playerId, int termId, int part, int team) {
		logger.info("onFighterOver playerId:{}, termId:{}, part:{}, team:{}", playerId, termId, part, team);
		if (HawkOSOperator.isEmptyString(playerId)) {
			logger.error("the part:{} does not has king", part);			
			return;
		}
		
		CrossPlayerStruct.Builder cps = RedisProxy.getInstance().getCrossPlayerStruct(playerId);
		if (cps == null) {
			logger.error("the part:{}  playerId:{} can not find CrossPlayerStruct", part, playerId);			
			return; 
		}
		try {
			this.sendSpecialAward(cps.getPlayerId(), part, team);
			this.onGenerateKing(cps, termId, part, team);
		} catch (Exception e) {
			HawkException.catchException(e);
		}								
	}
	
	/**
	 * 
	 * @param playerId
	 * @param part
	 * @param team
	 */
	private void sendSpecialAward(String playerId, int part, int team) {
		int level = this.getOfficerLevel(part, team);
		StarWarsConstCfg constCfg = StarWarsConstCfg.getInstance(); 
		if (level == GsConst.StarWarsConst.SECOND_LEVEL) {
			MailParames.Builder mailParamesBuilder = MailParames.newBuilder();
			mailParamesBuilder.setPlayerId(playerId);
			mailParamesBuilder.addRewards(constCfg.getSwGenerRewardList());
			mailParamesBuilder.setAwardStatus(MailRewardStatus.NOT_GET);
			mailParamesBuilder.setMailId(MailId.SW_PART_KING_PERSONAL_AWARD);
			SystemMailService.getInstance().sendMail(mailParamesBuilder.build());
		} else if (level == GsConst.StarWarsConst.THIRD_LEVEL){
			MailParames.Builder mailParamesBuilder = MailParames.newBuilder();
			mailParamesBuilder.setPlayerId(playerId);
			mailParamesBuilder.addRewards(constCfg.getSwKingRewardList());
			mailParamesBuilder.setMailId(MailId.SW_WORLD_KING_PERSONAL_AWARD);
			mailParamesBuilder.setAwardStatus(MailRewardStatus.NOT_GET);
			SystemMailService.getInstance().sendMail(mailParamesBuilder.build());
		}
	}

	/**
	 * 产生王
	 */
	public void onGenerateKing(CrossPlayerStruct.Builder player, int termId, int part, int team) {
		//这里调用的地方是战斗结束,这个时候需要对世界霸主做个处理
		if (part != GsConst.StarWarsConst.WORLD_PART) {
			createStarWarsKingRecord(player, termId, part, team);
		}		
		this.createStarWarsKingOfficer(player, termId, part, team);
	}

	public void createStarWarsKingOfficer(CrossPlayerStruct.Builder player, int termId, int part, int team) {
		int kingOfficerId = this.getKingOfficerIdByPart(part, team);
		int expireTime = this.getKeyExpireTime();
		StarWarsOfficerStruct.Builder officerBuilder = this.buildStarWarsOfficerStruct(player, part, kingOfficerId);
		RedisProxy.getInstance().updateStarWarsOfficer(part, team, officerBuilder.build(), expireTime);
	}

	/**
	 * 清理所有的信息
	 */
	public void onClearEvent() {
		logger.info("clear all star wars info");
		clearOfficerInfo();
		clearGiftInfo();		
		clearJoinGuildLeaderInfo();			
	}
	
	private void clearJoinGuildLeaderInfo() {
		logger.info("clear join guild leader info");
		try {
			RedisProxy.getInstance().deleteStarWarsJoinGuildLeader();
		} catch (Exception e) {
			HawkException.catchException(e);
		} 
		
	}

	/**
	 * 清理缓存中的信息.
	 */
	public void onClearMemInfo() {
		try {
			logger.info("clear star wars memory info");
			this.partOfficerMap = new ConcurrentHashMap<>();
			this.playerOfficerMap = new ConcurrentHashMap<>();
			//todo 通知玩家.
			Map<String, List<HawkTuple2<Integer, Integer>>> copyPlayerOfficerMap = this.playerOfficerMap;
			clearEffect(copyPlayerOfficerMap);			
			//减这个数防止清理之后立马拉取.
			this.officerPullTickNum = this.officerPullTickNum - 2;
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}

	/**
	 * 删除礼包记录,和礼包的颁发信息
	 */
	private void clearGiftInfo() {
		try {
			Set<Integer> parts = AssembleDataManager.getInstance().getStarWarsPartSet();
			for (Integer part : parts) {
				RedisProxy.getInstance().deleteStarWarsGiftRecord(part);
				RedisProxy.getInstance().deleteStarWarsGiftSendInfo(part);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		
	}

	/**
	 * 清除所有的官职.
	 */
	private void clearOfficerInfo() {
		try {
			// 重置分区官职信息.
			Map<String, List<HawkTuple2<Integer, Integer>>> copyPlayerOfficerMap = playerOfficerMap;
			partOfficerMap = new ConcurrentHashMap<>();
			playerOfficerMap = new ConcurrentHashMap<>();			
			// 通知玩家.
			clearEffect(copyPlayerOfficerMap);
			
			Set<Integer> parts = AssembleDataManager.getInstance().getStarWarsPartSet();
			for (Integer part : parts) {
				RedisProxy.getInstance().deleteStarWarsOfficer(part);
			}
		} catch (Exception e) {
			HawkException.catchException(e);			
		}	
	} 

	private void clearEffect(Map<String, List<HawkTuple2<Integer, Integer>>> copyPlayerPartOfficerMap) {
		for (Entry<String, List<HawkTuple2<Integer, Integer>>> entry : copyPlayerPartOfficerMap.entrySet()) {

			// 只处理在线玩家,同时也过滤了非本服玩家.
			Player player = GlobalData.getInstance().getActivePlayer(entry.getKey());
			if (player == null) {
				continue;
			}			
			player.getEffect().syncEffect(player);
		}
	}

	/**
	 * @param player
	 * @param officerIds
	 */
	private void synEffect(String playerId, Collection<Integer> officerIds) {
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player == null) {
			return;
		}

		this.synEffect(player, officerIds);
	}

	public void synEffect(Player player, Collection<Integer> officerIds) {
		Set<EffType> effSet = new HashSet<>();
		officerIds.stream().forEach(officerId -> {
			StarWarsOfficerCfg officerCfg = HawkConfigManager.getInstance().getConfigByKey(StarWarsOfficerCfg.class,
					officerId);
			if (officerCfg != null) {
				for (EffType effType : officerCfg.getEffTypes()) {
					effSet.add(effType);
				}
			}
		});

		player.getEffect().syncEffect(player, effSet.toArray(new EffType[effSet.size()]));
	}

	/**
	 * 定时拉取一些信息(官职,因为需要计算作用号) 战斗的时候可以稍微拉取频繁一点, 战斗结束之后就没有必要了||有值的时候就拉取的频率慢一点,
	 * 没有就拉取的频繁一点.
	 */
	public void onTick() {
		// 假定十秒执行一次. 1800秒,半个小时更新一次信息,
		boolean needPull = false;
		officerPushTickNum ++;
		officerPullTickNum ++;
		worldPullTickNum ++;
		
		if (officerPushTickNum >= 180) {
			officerPushTickNum = 0;		
			this.pushPlayerInfo();
		}		
		

		//五分钟读取一次
		if(playerOfficerMap == null || playerOfficerMap.isEmpty()) {
			 if (officerPullTickNum >= 30) {
				 needPull = true;
			 }				
		} else {
			if (officerPullTickNum >= 180) {
				needPull = true;
			}
		} 
		
		if (needPull) {
			officerPullTickNum = 0;
			this.loadOrReloadOfficer();
		}
		
		if (worldPullTickNum >= 180) {
			worldPullTickNum = 0;
		}
		//自动设置国王.
		autoSetKing();
		checkClean();
	}
	
	private void checkClean() {
		try {					
			if (hasClean) {
				return;
			}			
			//只有在未开始的时候才会去做清理.
			if (StarWarsActivityService.getInstance().getActivityData().getState() != SWActivityState.NOT_OPEN) {
				return;
			} 
			
			StarWarsTimeCfg timeCfg = null;
			ConfigIterator<StarWarsTimeCfg>  timeIterator= HawkConfigManager.getInstance().getConfigIterator(StarWarsTimeCfg.class);
			long curTimeMill = HawkTime.getMillisecond();
			while (timeIterator.hasNext()) {
				StarWarsTimeCfg tmpCfg = timeIterator.next();
				if (tmpCfg.getSignStartTimeValue() > curTimeMill) {
					break;
				}
				timeCfg = tmpCfg;
			}
			int endTime = 0;
			if (timeCfg != null) {
				endTime = ((int)(timeCfg.getEndTimeValue() / 1000)) + StarWarsConstCfg.getInstance().getSwClearTime();
			} else {
				return;
			}
			//在结束阶段多次清理是没有关系的,这里就不记录是否清理过了.
			int curTime = HawkTime.getSeconds();
			if (endTime < curTime) {
				//if (StarWarsActivityService.getInstance().isBattleServer()) {
				this.onClearMemInfo();
				if (StarWarsActivityService.getInstance().isCalServer()) {
					this.clearOfficerInfo();
				} 
				hasClean = true;
			}		
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/**
	 * 截止国王的设置时间必须在本期之内.
	 */
	private void autoSetKing() {
		Map<Integer,  StarWarsOfficerStruct.Builder> officerMap = this.partOfficerMap.get(GsConst.StarWarsConst.WORLD_PART);
		if (officerMap == null) {
			return;
		}
		
		StarWarsOfficerStruct.Builder sbuilder = officerMap.get(GsConst.StarWarsConst.TEAM_NONE);
		if (sbuilder == null) {
			return;
		}
		//已经修改了.
		if (sbuilder.getState() == GsConst.StarWarsConst.UPDATED) {
			return;
		}
		if (sbuilder.getEndSetTime() > HawkTime.getSeconds()) {
			return;
		}
		if (!sbuilder.hasPlayerInfo()) {
			return;
		}
		CrossPlayerStruct crossPlayerStruct = sbuilder.getPlayerInfo();
		//不是本服.
		if (!GlobalData.getInstance().isLocalPlayer(crossPlayerStruct.getPlayerId())) {
			return;
		}
		logger.info("auto set king playerId:{}", crossPlayerStruct.getPlayerId());
		sbuilder.setState(GsConst.StarWarsConst.UPDATED);
		RedisProxy.getInstance().updateStarWarsOfficer(GsConst.StarWarsConst.WORLD_PART, 
				GsConst.StarWarsConst.TEAM_NONE, sbuilder.build(), this.getKeyExpireTime());
		
		//todo 这里需要修改下邮件的id和公告的id.
		Player player = GlobalData.getInstance().makesurePlayer(crossPlayerStruct.getPlayerId());		
		//发送邮件.
		String mainId = GlobalData.getInstance().getMainServerId(player.getServerId());
		MailParames.Builder mailParames = MailParames.newBuilder();
		mailParames.setMailId(MailId.SW_WORLD_KING_NOTICE);
		mailParames.addContents(mainId, player.getGuildTag(), player.getName());
		SystemMailService.getInstance().addGlobalMail(mailParames.build(), HawkTime.getMillisecond(), HawkTime.DAY_MILLI_SECONDS * 10);
		
		ChatParames.Builder chatBuilder = ChatParames.newBuilder();
		chatBuilder.setKey(NoticeCfgId.CHANGE_STAR_WARS_KING);
		chatBuilder.setChatType(ChatType.SPECIAL_BROADCAST);
		chatBuilder.addParms(mainId);
		chatBuilder.addParms(player.getGuildTag());
		chatBuilder.addParms(player.getName());
		ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());
		
		this.createStarWarsKingRecord(BuilderUtil.buildCrossPlayer(player), StarWarsActivityService.activityInfo.getTermId(), GsConst.StarWarsConst.WORLD_PART,
				GsConst.StarWarsConst.TEAM_NONE);
	}

	/**
	 * 定时更新信息到redis. 
	 */
	public void pushPlayerInfo() {		
		//todo 有官职在身的玩家.
		Set<String> playerIds = new HashSet<>();
		int expireTime = 86400;
		for (String playerId : playerIds) {
			try {
				//不是本区的玩家．
				if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
					continue;
				}
				
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if (player == null) {
					continue;
				}					
				CrossPlayerStruct.Builder crossBuilder = BuilderUtil.buildCrossPlayer(player); 
				RedisProxy.getInstance().updateCrossPlayerStruct(playerId, crossBuilder.build(), expireTime);
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}							
		}
	}

	/**
	 * redis key的过期时间.
	 * 
	 * @return
	 */
	private int getKeyExpireTime() {
		return 0;
	}

	/**
	 * 
	 * @return
	 */
	private int getTermId() {
		return StarWarsActivityService.getInstance().getTermId(); 
	}

	/**
	 * 获取作用号的值.
	 * 
	 * @param playerId
	 * @param effectType
	 * @return
	 */
	public int getEffectValue(String playerId, int effectType) {		
	List<HawkTuple2<Integer, Integer>> partOfficerList = playerOfficerMap.get(playerId);
		int count = 0;
		if (partOfficerList != null) {
			StarWarsOfficerCfg oldOfficerCfg = null;
			for (HawkTuple2<Integer, Integer> tuple: partOfficerList) {
				int officerId = this.getKingOfficerIdByPart(tuple.first, tuple.second);
				StarWarsOfficerCfg officerCfg = HawkConfigManager.getInstance().getConfigByKey(StarWarsOfficerCfg.class,
						officerId);
				if (oldOfficerCfg == null) {
					oldOfficerCfg = officerCfg;
				} else {
					if (officerCfg != null) {
						//leve越高代表的越是后面的场次.
						if (officerCfg.getLevel() > oldOfficerCfg.getLevel()) {
							return officerCfg.getLevel();
						}
					}
				}
			}
			if (oldOfficerCfg != null) {
				count = oldOfficerCfg.getEffVal(effectType);
			}
		} 				
		return count;
	}

	public void createStarWarsKingRecord(CrossPlayerStruct.Builder player, int termId, int part, int team) {
		HawkLog.logPrintln("starWarsOfficer create king record playerId:{}, termId:{}, part:{}", player.getPlayerId(), termId, part);		
		StarWarsKingRecordStruct.Builder kingRecord = this.buildStarWarsKingRecordStruct(player, termId);
		RedisProxy.getInstance().addStarWarsKingRecord(part, team, kingRecord.build());
	}

	/**
	 * 产生国王.
	 * 
	 * @param player
	 * @param termId
	 * @param part
	 * @return
	 */
	public StarWarsOfficerStruct.Builder buildStarWarsOfficerStruct(CrossPlayerStruct.Builder player, int part,
			int officerId) {
		StarWarsOfficerStruct.Builder officerBuilder = StarWarsOfficerStruct.newBuilder();
		if (part == GsConst.StarWarsConst.WORLD_PART) {
			officerBuilder.setEndSetTime(HawkTime.getSeconds() + StarWarsConstCfg.getInstance().getAppointTime());
		} else {
			officerBuilder.setEndSetTime(0);
		}		
		officerBuilder.setOfficerId(officerId);
		officerBuilder.setPlayerInfo(player);

		return officerBuilder;
	}

	/**
	 * 创建国王记录。
	 * 
	 * @param struct
	 * @return
	 */
	public StarWarsKingRecordStruct.Builder buildStarWarsKingRecordStruct(CrossPlayerStruct.Builder player, int termId) {
		StarWarsKingRecordStruct.Builder recordBuilder = StarWarsKingRecordStruct.newBuilder();
		recordBuilder.setCreateTime(HawkTime.getSeconds());
		recordBuilder.setTurnCount(termId);
		recordBuilder.setPlayerInfo(player);

		return recordBuilder;
	}
	
	/**
	 * 获得不同类型的国王officerId
	 * @param type
	 * @return
	 */
	public int getKingOfficerIdByPart(int part, int team) {
		int type = this.getOfficerLevel(part, team);
		ConfigIterator<StarWarsOfficerCfg> officerCfg = HawkConfigManager.getInstance()
				.getConfigIterator(StarWarsOfficerCfg.class);
		Optional<StarWarsOfficerCfg> cfgOptional = officerCfg.stream()
				.filter(cfg -> cfg.getLevel() == type ).findAny();
		// 这里必须不为空,配置校验那里会做校验.
		return cfgOptional.get().getId();
	}

	/**
	 * 获取玩家的{part, officerId}
	 * 
	 * @param playerId
	 * @return
	 */
	public List<HawkTuple2<Integer, Integer>> getPlayerPartOfficer(String playerId) {
		return this.playerOfficerMap.get(playerId);
	}
	
	/**
	 * 获取
	 * @param playerId
	 * @return 没有官职返回 0
	 */
	public Integer getPlayerOfficerId(String playerId) {
		List<HawkTuple2<Integer, Integer>> partOfficerList = this.getPlayerPartOfficer(playerId);
		if (CollectionUtils.isEmpty(partOfficerList)) {
			return 0;
		} else {
			//level 越大说明官职越高.
			StarWarsOfficerCfg oldOfficerCfg = null;
			for (HawkTuple2<Integer, Integer> tuple : partOfficerList) {
				StarWarsOfficerCfg officerCfg = this.getOfficerCfg(tuple.first, tuple.second);
				if (oldOfficerCfg == null) {
					oldOfficerCfg = officerCfg;
				} else {
					if (officerCfg != null) {
						if (officerCfg.getLevel() > oldOfficerCfg.getLevel()) {
							oldOfficerCfg = officerCfg;
						}
					}
				}
			}
			
			if (oldOfficerCfg != null) {
				return oldOfficerCfg.getId();
			} else {
				return 0;
			}			
		}			
	}
	
	/**
	 * 判断这个人是不是赛区或者世界的统治者。
	 * @param playerId
	 * @return
	 */
	public boolean isKing(String playerId) {		
		Map<String, List<HawkTuple2<Integer, Integer>>> localMap = playerOfficerMap;
		return localMap.containsKey(playerId);
	}
	/**
	 * 判断是不是某个分区的王.
	 * 
	 * @param playerId
	 * @param part
	 * @return
	 */
	public boolean isKing(String playerId, int part, int teamNumber) {
		List<HawkTuple2<Integer, Integer>> playerPartOfficerList = this.getPlayerPartOfficer(playerId);
		if (CollectionUtils.isEmpty(playerPartOfficerList)) {
			return false;
		}
		
		for (HawkTuple2<Integer, Integer> tuple : playerPartOfficerList) {
			if (tuple.first == part && tuple.second == teamNumber) {
				return true;
			}
		}
		
		return false;
	}
	
	public StarWarsOfficerCfg getOfficerCfg(int part, int team) {
		int type = this.getOfficerLevel(part, team);
		return AssembleDataManager.getInstance().getStarWarsOfficerCfg(type);
	}
	/**
	 * {playerId, {part, officerId}}
	 * 
	 * @return
	 */
	public Map<String, List<HawkTuple2<Integer, Integer>>> getPlayerPartOfficerMap() { 
		return playerOfficerMap;
	}
	
	/**转让世界霸主.
	 * @param player
	 * @param struct
	 * @param officerId
	 */
	public int onSetWorldKing(Player player, String targetPlayerId) {
		logger.info("StarWarsOfficer change world king playerId:{}, targetPlayerId:{}", player.getId(), targetPlayerId);		
		int expireTime = this.getKeyExpireTime();
		int termId = this.getTermId();
		// 不能任命自己.
		if (player.getId().equals(targetPlayerId)) {
			return Status.Error.STAR_WARS_SET_SELF_VALUE;
		}
		
		if (!this.isKing(player.getId(), GsConst.StarWarsConst.WORLD_PART, GsConst.StarWarsConst.TEAM_NONE)) {
			return Status.Error.STAR_WARS_NOT_KING_VALUE;
		}
		
		//不是同一个联盟.
		if (!GuildService.getInstance().isInTheSameGuild(player.getId(), targetPlayerId)) {
			return Status.Error.GUILD_NOT_SAME_VALUE;
		}
				
		if (!GlobalData.getInstance().isLocalPlayer(targetPlayerId)) {
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetPlayerId);
		if (targetPlayer == null) {
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}
		StarWarsOfficerStruct king = this.getKing(GsConst.StarWarsConst.WORLD_PART, GsConst.StarWarsConst.TEAM_NONE);
		if (king == null) {
			logger.error("id:{} not found workd king", player.getId());
			return Status.Error.STAR_WARS_NOT_KING_VALUE;
		}
		
		//已经设置过了,或者时间到期了.
		if (king.getState() == GsConst.StarWarsConst.UPDATED) {			
			logger.error("id:{} change timeout", player.getId());
			return Status.Error.SW_CHANGE_TIMEOUT_VALUE;
		}
		if (king.getEndSetTime()  < HawkTime.getSeconds()) {
			logger.error("id:{} has changed", player.getId());			
			return Status.SysError.PARAMS_INVALID_VALUE; 
		}
		
		int part = GsConst.StarWarsConst.WORLD_PART;
		int team = GsConst.StarWarsConst.TEAM_NONE;
		int worldKingOfficerId =this.getKingOfficerIdByPart(part, team);
		CrossPlayerStruct.Builder crossPlayerBuilder = BuilderUtil.buildCrossPlayer(targetPlayer);
		StarWarsOfficerStruct.Builder officerBuilder = this.buildStarWarsOfficerStruct(crossPlayerBuilder, part, worldKingOfficerId);
		//这里是二次设置时间了,所以这里我们修改一下时间
		officerBuilder.setEndSetTime(HawkTime.getSeconds() - 1);
		officerBuilder.setState(GsConst.StarWarsConst.UPDATED);
		RedisProxy.getInstance().updateStarWarsOfficer(part, team, officerBuilder.build(), expireTime);
		// 重新reload
		this.loadOrReloadOfficer();
		this.createStarWarsKingRecord(crossPlayerBuilder, termId, part, team);
		String mainId = GlobalData.getInstance().getMainServerId(targetPlayer.getServerId());
		MailParames.Builder mailParames = MailParames.newBuilder();
		mailParames.setMailId(MailId.SW_WORLD_KING_CHANGE);
		mailParames.addContents(mainId, targetPlayer.getGuildTag(), targetPlayer.getName());
		SystemMailService.getInstance().addGlobalMail(mailParames.build(), HawkTime.getMillisecond(), HawkTime.DAY_MILLI_SECONDS * 10);
		
		this.synStarWarsOfficer(player);
		this.synStarWarsOfficer(targetPlayer);
		ChatParames.Builder chatBuilder = ChatParames.newBuilder();
		chatBuilder.setKey(NoticeCfgId.CHANGE_STAR_WARS_KING);
		chatBuilder.setChatType(ChatType.SPECIAL_BROADCAST);
		chatBuilder.addParms(mainId);
		chatBuilder.addParms(targetPlayer.getGuildTag());
		chatBuilder.addParms(targetPlayer.getName());
		ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());			
		this.synEffect(player, Arrays.asList(worldKingOfficerId));
		this.synEffect(targetPlayer, Arrays.asList(worldKingOfficerId));
		
		return Status.SysError.SUCCESS_OK_VALUE;

	}

	/**
	 * 调用远程发奖励.
	 * @param targetServerId
	 * @param playerSet
	 * @param giftId
	 */
	public void callRemoteSendGift(String targetServerId, Set<String> playerSet, int part, int giftId) {
		String playerIds = SerializeHelper.collectionToString(playerSet, SerializeHelper.ATTRIBUTE_SPLIT);
		GmProxyHelper.proxyCall(targetServerId, "starWarsSendGift", "playerIds=" + playerIds + "&giftId=" + giftId + "&part="+part,
				2000);
	}

	/**
	 * 获得官职名字
	 * 
	 * @param officerId
	 * @return
	 */
	public String getOfficerName(int officerId) {
		StarWarsOfficerCfg officerCfg = HawkConfigManager.getInstance().getConfigByKey(StarWarsOfficerCfg.class,
				officerId);

		return officerCfg.getOfficeName();
	}

	/**
	 * 同步国王记录
	 * 
	 * @param player
	 * @param part
	 */
	public void synStarWarsKingRecord(Player player, int part, int team) {
		List<StarWarsKingRecordStruct> structList = RedisProxy.getInstance().
				getStarWarsKingRecord(part, team, StarWarsConstCfg.getInstance().getMaxHistoryCount());
		StarWarsKingRecordResp.Builder sbuilder = StarWarsKingRecordResp.newBuilder();
		sbuilder.addAllRecord(structList);
		sbuilder.setPart(part);
		sbuilder.setTeam(team);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.STAR_WARS_KING_RECORD_RESP_VALUE, sbuilder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步分区礼包信息.
	 * 
	 * @param player
	 * @param part
	 */
	public void synStarWarsGiftRecord(Player player, int part) {
		List<StarWarsGiftRecordStruct> structList = RedisProxy.getInstance().getStarWarsGiftRecord(part, StarWarsConstCfg.getInstance().getMaxGiftHistoryCount());
		StarWarsGiftRecordResp.Builder sbuilder = StarWarsGiftRecordResp.newBuilder();
		sbuilder.setPart(part);
		sbuilder.addAllRecors(structList);

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.STAR_WARS_GIFT_RECORD_RESP_VALUE, sbuilder);
		player.sendProtocol(protocol);
	}

	/**
	 * 是否是一个合法的分区.
	 * 
	 * @param part
	 * @return
	 */
	public boolean isValidPart(int part) {
		return AssembleDataManager.getInstance().getStarWarsPartSet().contains(part);
	}

	/**
	 * 同步官职信息	 
	 * @param player
	 * @param part
	 */
	public void synStarWarsOfficer(Player player) {
		Map<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> map = this.partOfficerMap;
		StarWarsOfficerResp.Builder sbuilder = StarWarsOfficerResp.newBuilder();		
		if (!MapUtils.isEmpty(map)) {							
			for (Entry<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> partEntry : map.entrySet()) {
				for (Entry<Integer, StarWarsOfficerStruct.Builder> teamEntry : partEntry.getValue().entrySet()) {
					StarWarsOfficerTeamStruct.Builder teamStruct = StarWarsOfficerTeamStruct.newBuilder();					
					teamStruct.setLevel(this.getOfficerLevel(partEntry.getKey(), teamEntry.getKey()));
					teamStruct.setOfficer(teamEntry.getValue());
					if (teamEntry.getKey() == 0 || partEntry.getKey() == 0) {
						String kingServerId = GlobalData.getInstance().getMainServerId(teamEntry.getValue().getPlayerInfo().getServerId());
						StarWarsPartCfg partCfg = AssembleDataManager.getInstance().getServerPartCfg(kingServerId);						
						//客户端说做不了.
						teamStruct.setTeam(partCfg.getTeam());
						teamStruct.setPart(partCfg.getZone());
					} else {
						teamStruct.setTeam(teamEntry.getKey());
						teamStruct.setPart(partEntry.getKey());
					}					
					
					sbuilder.addTeamStructs(teamStruct);
				}
			}
		}
		
		StarWarsTimeCfg timeCfg = StarWarsActivityService.activityInfo.getTimeCfg();
		int endTime = 0;
		if (timeCfg != null) {
			endTime = ((int)(timeCfg.getEndTimeValue() / 1000)) + StarWarsConstCfg.getInstance().getSwClearTime();
		} 
		sbuilder.setClearTime(endTime);

		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.STAR_WARS_OFFICER_RESP_VALUE, sbuilder);
		player.sendProtocol(hawkProtocol);
	}
	
	/**
	 * 1 是16强
	 * 2 是4强
	 * 3 是世界霸主.
	 * @param part
	 * @param team
	 * @return
	 */
	private int getOfficerLevel(int part, int team) {
		if (team != 0) {
			return GsConst.StarWarsConst.FIRST_LEVEL;
		} else {
			if (part == 0) {
				return GsConst.StarWarsConst.THIRD_LEVEL;
			} else {
				return GsConst.StarWarsConst.SECOND_LEVEL;
			}
		}
	}

	/**
	 * 同步礼包的信息
	 * 
	 * @param player
	 * @param part
	 */
	public void synStarWarsGiftInfo(Player player, int part) {
		Map<Integer, Set<String>> map = RedisProxy.getInstance().getStarWarsGiftSendInfo(part);
		this.synStarWarsGiftInfo(player, part, map);
	}

	/**
	 * 同步礼包的信息
	 * 
	 * @param player
	 * @param part
	 */
	public void synStarWarsGiftInfo(Player player, int part, Map<Integer, Set<String>> map) {
		StarWarsGiftResp.Builder sbuilder = StarWarsGiftResp.newBuilder();
		if (!MapUtils.isEmpty(map)) {
			for (Entry<Integer, Set<String>> entry : map.entrySet()) {
				sbuilder.addGifts(this.buildStarWarsGiftStruct(entry.getKey(), entry.getValue()));
			}
		}
		sbuilder.setPart(part);

		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.STAR_WARS_GIFT_RESP_VALUE, sbuilder);
		player.sendProtocol(hawkProtocol);
	}

	/**
	 * 礼包的信息
	 * 
	 * @param giftId
	 * @param playerIds
	 * @return
	 */
	public StarWarsGiftStruct.Builder buildStarWarsGiftStruct(int giftId, Set<String> playerIds) {
		StarWarsGiftStruct.Builder structBuilder = StarWarsGiftStruct.newBuilder();
		structBuilder.addAllPlayerIdSet(playerIds);
		structBuilder.setGiftId(giftId);

		return structBuilder;
	}

	/**
	 * 发送礼包
	 * 
	 * @param player
	 * @param giftId
	 * @param playerIds
	 * @return
	 */
	public int onStarWarsSendGift(Player player, int part, int giftId, List<String> playerIds) {
		logger.info("StarWarsSendGift playerId:{},part:{},giftId:{},playerids:{}", player.getId(), part, giftId,
				playerIds);
		int expireTime = this.getKeyExpireTime();		
		StarWarsGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(StarWarsGiftCfg.class, giftId);
		if (giftCfg == null) {
			return Status.Error.STAR_WARS_INCORRECT_GIFTID_VALUE;
		}
		
		int level = this.getOfficerLevel(part, GsConst.StarWarsConst.TEAM_NONE);
		if (giftCfg.getLevel() != level) {
			return Status.Error.STAR_WARS_INCORRECT_GIFTID_VALUE;
		}
		
		if (!this.isKing(player.getId(), part, GsConst.StarWarsConst.TEAM_NONE)) {
			return Status.Error.STAR_WARS_NOT_KING_VALUE;
		}
		
		Set<String> setPlayerIds = new HashSet<>(playerIds);
		if (setPlayerIds.size() != playerIds.size()) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		// 账号不存在。
		GlobalData globalData = GlobalData.getInstance();			
		for (String playerId : setPlayerIds) {
			if (!globalData.isExistPlayerId(playerId) && RedisProxy.getInstance().getCrossPlayerStruct(playerId) == null) {
				return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
			}
		}

		Map<Integer, Set<String>> setMap = RedisProxy.getInstance().getStarWarsGiftSendInfo(part);
		Set<String> alreadySentSet = setMap.get(giftId);
		if (alreadySentSet == null) {
			alreadySentSet = new HashSet<>();
			setMap.put(giftId, alreadySentSet);
		}
		Set<String> allPlayerIdSet = new HashSet<>();
		for (Set<String> idSet : setMap.values()) {
			allPlayerIdSet.addAll(idSet);
		}
		
		// 判断个数.
		if (setPlayerIds.size() + alreadySentSet.size() > giftCfg.getTotalNumber()) {
			return Status.Error.STAR_WARS_GIFT_NOT_ENOUGH_VALUE;
		}
		
		if (!Collections.disjoint(allPlayerIdSet, setPlayerIds)) {
			return Status.Error.STAR_WARS_ALREADY_SEND_GIFT_VALUE;
		}

		alreadySentSet.addAll(setPlayerIds);
		RedisProxy.getInstance().updateStarWarsGiftSendInfo(part, giftId, alreadySentSet, expireTime);
		// 生成礼包颁发记录。
		addGiftRecor(player, part, giftId, setPlayerIds);
		//分发发送礼包请求
		this.dispatchSendGift(setPlayerIds, part, giftId);

		logger.info("StarWarsSendGift playerId:{},part:{},giftId:{},playerids:{} success ", player.getId(), part,
				giftId, playerIds);
		this.synStarWarsGiftInfo(player, part, setMap);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 创建记录.
	 * 
	 * @param king
	 * @param termId
	 * @param part
	 * @param giftId
	 * @param playerIds
	 */
	private void addGiftRecor(Player king, int part, int giftId, Set<String> playerIds) {
		logger.info("StarWarsSendGift create record playerId:{},part:{},giftId:{},playerids:{} ", king.getId(), part,
				giftId, playerIds);
		StarWarsGiftRecordStruct.Builder builder = null;
		List<StarWarsGiftRecordStruct> structList = new ArrayList<>();
		Player player = null;
		CrossPlayerStruct struct = null;
		int createTime = HawkTime.getSeconds();
		for (String playerId : playerIds) {
			builder = StarWarsGiftRecordStruct.newBuilder();
			builder.setKingName(king.getName());
			builder.setKingGuildTag(king.getGuildTag());
			builder.setGiftId(giftId);
			builder.setCreateTime(createTime);
			builder.setKingServerId(king.getServerId());
			if (GlobalData.getInstance().isExistPlayerId(playerId)) {
				player = GlobalData.getInstance().makesurePlayer(playerId);
				builder.setPlayerName(player.getName());
				builder.setPlayerGuildTag(player.getGuildTag());
				builder.setServerId(player.getServerId());
			} else {
				struct = RedisProxy.getInstance().getCrossPlayerStruct(playerId).build();
				builder.setPlayerGuildTag(struct.getGuildTag());
				builder.setPlayerName(struct.getName());
				builder.setServerId(struct.getServerId());
			}
			structList.add(builder.build());
		}

		RedisProxy.getInstance().addStarWarsGiftRecord(part, structList);
	}

	public void dispatchSendGift(Set<String> playerIdSet, int part, int giftId) {
		Map<String, Set<String>> setMap = new HashMap<>();
		GlobalData globalData = GlobalData.getInstance();
		AccountInfo ai = null;
		String mainServerId = null;
		CrossPlayerStruct struct = null;
		Set<String> playerSet = null;
		// 其实也可以通过id解析出serverId但是不靠谱
		for (String playerId : playerIdSet) {
			ai = globalData.getAccountInfoByPlayerId(playerId);
			if (ai != null) {
				mainServerId = globalData.getMainServerId(ai.getServerId());
				playerSet = setMap.get(mainServerId);
				if (playerSet == null) {
					playerSet = new HashSet<>();
					setMap.put(mainServerId, playerSet);
				}

				playerSet.add(playerId);

				continue;
			}

			struct = RedisProxy.getInstance().getCrossPlayerStruct(playerId).build();
			mainServerId = globalData.getMainServerId(struct.getServerId());
			playerSet = setMap.get(mainServerId);
			if (playerSet == null) {
				playerSet = new HashSet<>();
				setMap.put(mainServerId, playerSet);
			}

			playerSet.add(playerId);
		}

		// 收集到每个区服的玩家.
		for (Entry<String, Set<String>> entry : setMap.entrySet()) {
			if (globalData.isLocalServer(entry.getKey())) {
				onSendGift(entry.getValue(), part, giftId);
			} else {
				callRemoteSendGift(entry.getKey(), entry.getValue(), part, giftId);
			}
		}
	}

	/**
	 * 实际颁发礼包.
	 * 
	 * @param playerIds
	 * @param giftId
	 */
	public void onSendGift(Set<String> playerIds, int part, int giftId) {
		logger.info("star wars send gift playerids:{}", playerIds);
		StarWarsGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(StarWarsGiftCfg.class, giftId);
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, giftCfg.getAwardId());
		boolean isWorld = giftCfg.getLevel() == GsConst.StarWarsConst.THIRD_LEVEL;
		//最早的时候策划说是大陆是世界的，后面说不是
		MailId mailid = isWorld ? MailId.SW_PART_KING_GIFT : MailId.SW_WORLD_KING_GIFT;
		String kingName = "";
		String kingServerId = "";
		String tag = "";
		StarWarsOfficerStruct starWarsOfficerStruct = this.getKing(part, GsConst.StarWarsConst.TEAM_NONE);
		if (starWarsOfficerStruct != null && starWarsOfficerStruct.hasPlayerInfo()) {
			kingName = starWarsOfficerStruct.getPlayerInfo().getName();
			tag = starWarsOfficerStruct.getPlayerInfo().getGuildTag();
			kingServerId = starWarsOfficerStruct.getPlayerInfo().getServerId();
		}
		NoticeCfgId noticeId = part == GsConst.StarWarsConst.WORLD_PART ? Const.NoticeCfgId.WORLD_KING_SEND_GIFT : Const.NoticeCfgId.PART_KING_SEND_GIFT; 
		ChatParames.Builder chatBuilder = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST).
				setKey(noticeId).setPlayer(null);
		chatBuilder.addParms(kingServerId);
		chatBuilder.addParms(tag);
		chatBuilder.addParms(kingName);
		chatBuilder.addParms(giftCfg.getGiftName());		
		for (String playerId : playerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				logger.error("makesure player fail playerId:{}", playerId);				
				continue;
			}
			chatBuilder.addParms(player.getServerId());
			chatBuilder.addParms(player.getGuildTag());
			chatBuilder.addParms(player.getName());
			SystemMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(mailid)
							.setAwardStatus(MailRewardStatus.NOT_GET).setRewards(awardCfg.getRandomAward().getAwardItems())
							.addContents(giftCfg.getGiftName())
							.build());			
		}		
		
		ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());
	}

	/**
	 * 获取所有区的king
	 * 国王的playerInfo是不可能清空的。
	 * @return
	 */
	public Map<Integer, StarWarsOfficerStruct.Builder> getAllPartKing() {
		Map<Integer, StarWarsOfficerStruct.Builder> map = new HashMap<>();
		Map<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> localPartOfficerMap = partOfficerMap;
		Set<String> playerIdSet = new HashSet<>();
		for (Entry<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> entry : localPartOfficerMap.entrySet()) {
			StarWarsOfficerStruct.Builder kingBuilder = entry.getValue().get(GsConst.StarWarsConst.TEAM_NONE);
			if (kingBuilder != null) {
				map.put(entry.getKey(), kingBuilder);
				playerIdSet.add(kingBuilder.getPlayerInfo().getPlayerId());
			}
		}
		
		//做一次批量查询,浪费IO可耻.
		if (!playerIdSet.isEmpty()) {
			Map<String, CrossPlayerStruct.Builder> playerStructMap = RedisProxy.getInstance().getCrossPlayerStructs(playerIdSet);
			map.values().forEach(builder->{
				CrossPlayerStruct.Builder playerBuilder = playerStructMap.get(builder.getPlayerInfo().getPlayerId());
				if (playerBuilder != null) {
					builder.setPlayerInfo(playerBuilder);
				}
			});
		}		
		
		
		return map;
	}
	
	/**
	 * 获取分区的统治者
	 * @param part
	 * @return
	 */
	public StarWarsOfficerStruct getKing(int part, int team) {
		Map<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> localPartOfficerMap = partOfficerMap;
		Map<Integer, StarWarsOfficerStruct.Builder> partMap = localPartOfficerMap.get(part);
		if (partMap == null) {
			return null;
		}
				
		StarWarsOfficerStruct.Builder struct = partMap.get(team);
		if (struct == null) {
			return null;
		} else {
			return struct.build(); 
		}
	}
	
	/**
	 * 获取世界的统治者.
	 * @return
	 */
	public StarWarsOfficerStruct getWorldKing() {
		return this.getKing(GsConst.StarWarsConst.WORLD_PART, GsConst.StarWarsConst.TEAM_NONE);
	}
	
	public String getPlayerNameWithGuildTag(String guildTag, String playerName) {
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			return "[" + guildTag + "]" + playerName;
		} else {
			return playerName;
		}
	}

	public Map<Integer, Map<Integer, StarWarsOfficerStruct.Builder>> getPartOfficerMap() {
		return partOfficerMap;
	}
	
	public void reset() {
		hasClean = false;
	}
	
	/**
	 * 是否需要读取
	 * @return
	 */
	public boolean checkNeedReload() {
		if (StarWarsActivityService.getInstance().isClose()) {
			return false;
		}
		int curTime = HawkTime.getSeconds();
		int lastTime = lastReadTime.getAndIncrement();
		int cdTime = StarWarsConstCfg.getInstance().getLoadOfficerCdTime();
		if (curTime - lastTime < cdTime) {
			return false;
		}
		return lastReadTime.compareAndSet(lastTime, curTime);
	}
	
	
	/**
	 * 尝试 
	 */
	public void tryReloadOfficer() {
		if (checkNeedReload()) {
			this.loadOrReloadOfficer();
		}
	}	
}
