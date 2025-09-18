package com.hawk.game.world.object;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.hawk.app.HawkApp;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.YuriRevengeCfg;
import com.hawk.game.config.YuriRevengeMonsterCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.impl.YuriRevengeMonsterMarch;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;

/**
 * 公会尤里工厂
 * @author zhenyu.shang
 * @since 2017年9月21日
 */
public class GuildYuriFactory {

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 尤里点ID */
	private int pointId;

	/** 下一轮开始时间 */
	private long nextPushTime;
	
	/** 当前轮次 */
	private int round;
	
	/** 当前对应的联盟ID */
	private String guildId;
	
	/** 联盟成员列表 */
	private BlockingQueue<String> playerIds;
	
	/** 尤里复仇行军集合 */
	private Set<IWorldMarch> marchs;
	
	private boolean lastRound;
	
	public GuildYuriFactory(int pointId, String guildId) {
		this.guildId = guildId;
		this.pointId = pointId;
		this.marchs = new ConcurrentHashSet<IWorldMarch>();
		this.round = 0;
		this.lastRound = false;
		this.nextPushTime = HawkApp.getInstance().getCurrentTime() + nextRoundPeriod();
		this.playerIds = new LinkedBlockingQueue<String>();
		Collection<String> guildMembers = GuildService.getInstance().getGuildMembers(guildId);
		playerIds.addAll(guildMembers);
	}
	
	public void heartbeat(){
		//玩家列表为空，结束活动
		if(playerIds.isEmpty()){
			WorldFoggyFortressService.getInstance().closeYuriRevenge(guildId, false);
			return;
		}
		if(this.marchs.size() > 0){
			long curTime = HawkTime.getMillisecond();
			Set<String> delList = new ConcurrentHashSet<String>();
			for (IWorldMarch worldMarch : marchs) {
				if(curTime > (worldMarch.getEndTime()+ 10000)){
					delList.add(worldMarch.getMarchId());
				}
			}
			for(String del : delList){
				this.removePlayerMonsterMarch(del);
			}
		}
		//玩家所有行军到达，且达到最后一波, 活动彻底结束
		if(lastRound && marchs.isEmpty()){
			WorldFoggyFortressService.getInstance().closeYuriRevenge(guildId, false);
			return;
		}
		long now = HawkApp.getInstance().getCurrentTime();
		if(!lastRound && now > nextPushTime){
			//设置轮次加1
			this.round++;
			//获取下次时间
			long next = nextRoundPeriod();
			if(next == -1){
				//所有轮怪物已经出击完毕，不再出怪，等待活动自己结束
				this.nextPushTime = Long.MAX_VALUE;
				//移除尤里工厂，但不处理行军
				this.lastRound = true;
			} else {
				//设置下次心跳时间
				this.nextPushTime = now + next;
			}
			//出兵
			this.pushMonster();
		}
	}
	
	/**
	 * 结束尤里复仇活动
	 */
	public void clearMarch(){
		//清空所有在行动中的怪物
		for (IWorldMarch worldMarch : marchs) {
			WorldMarchService.getInstance().removeMarch(worldMarch);
		}
		marchs.clear();
	}
	
	/**
	 * 移除向某个玩家的所有行军
	 * @param playerId
	 * @param clearPlayer 是否删除玩家，退出联盟或者踢出联盟为true
	 */
	public void removePlayerAllMonsterMarch(String playerId, boolean clearPlayer){
		Iterator<IWorldMarch> it = marchs.iterator();
		while (it.hasNext()) {
			IWorldMarch iWorldMarch = it.next();
			if(iWorldMarch.getMarchEntity().getTargetId().equals(playerId)){
				it.remove();
				WorldMarchService.getInstance().removeMarch(iWorldMarch);
			}
		}
		if(clearPlayer){
			playerIds.remove(playerId);
		}
	}
	
	/**
	 * 移除向某个玩家的其中一个行军
	 * @param playerId
	 */
	public void removePlayerMonsterMarch(String marchId){
		Iterator<IWorldMarch> it = marchs.iterator();
		while (it.hasNext()) {
			IWorldMarch iWorldMarch = it.next();
			if(iWorldMarch.getMarchId().equals(marchId)){
				it.remove();
				WorldMarchService.getInstance().removeMarch(iWorldMarch);
			}
		}
	}
	
	/**
	 * 下一波时间间隔
	 * @return
	 */
	public long nextRoundPeriod(){
		YuriRevengeCfg yuriRevengeCfg = HawkConfigManager.getInstance().getConfigByKey(YuriRevengeCfg.class, this.round + 1) ;
		if(yuriRevengeCfg == null){
			return -1;
		}
		return yuriRevengeCfg.getMonsterInterval() * 1000L;
	}
	
	/**
	 * 发起行军
	 */
	private void pushMonster(){
		int[] day = WorldFoggyFortressService.getInstance().getYuriInOpenTime();
		for (String playerId : playerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(player == null){
				logger.error("player is null, round : {}, playerId : {}", round, playerId);
				continue;
			}
			
						
			if (CrossService.getInstance().isCrossPlayer(playerId)) {
				HawkLog.errPrintln("yuri revenge can not attack a cross player:{}", playerId);
				
				continue;
			}
							
			String mapName = player.getDungeonMap();
			if (!HawkOSOperator.isEmptyString(mapName)) {
				HawkLog.errPrintln("yuri revenge can't attack player in dungeon playerId:{}, mapName:{}", playerId, mapName);
				
				continue;
			}					
			
			YuriRevengeMonsterCfg monsterCfg = HawkConfigManager.getInstance().getCombineConfig(YuriRevengeMonsterCfg.class, round, day[0], day[1]);
			if(monsterCfg == null){
				logger.error("current monsterCfg is null, round : {}, daystart : {}, dayend : {}", round, day[0], day[1]);
				continue;
			}
			List<ArmyInfo> armys = monsterCfg.getArmyList();
			int playerPos = WorldPlayerService.getInstance().getPlayerPos(playerId);
			
			WorldPoint playerWorldPoint = WorldPointService.getInstance().getWorldPoint(playerPos);
			if(playerWorldPoint == null){
				//如果玩家城点被打飞, 或者被清除掉,则不发兵
				logger.error("can not find player point, playerId:{}, playerPos:{}", playerId, playerPos);
				continue;
			}
			//发起行军开始
			YuriRevengeMonsterMarch march = (YuriRevengeMonsterMarch) WorldMarchService.getInstance().startMonsterMarch(guildId, WorldMarchType.YURI_MONSTER_VALUE, pointId, playerPos, null, playerId, armys, round,nextPushTime,lastRound, false);
			//加入活动集合
			this.marchs.add(march);
		}
	}

	public long getNextPushTime() {
		return nextPushTime;
	}

	public int getRound() {
		return round;
	}

	public Set<IWorldMarch> getMarchs() {
		return marchs;
	}

	public String getGuildId() {
		return guildId;
	}

	public boolean isLastRound() {
		return lastRound;
	}
	
}
