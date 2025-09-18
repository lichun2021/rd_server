package com.hawk.game.president;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.OfficerCfg;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.OfficerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.President.PresidentManifestoResp;
import com.hawk.game.util.BuilderUtil;

/**
 * 国王战官职
 * 
 * @author hawk
 *
 */
public class PresidentOfficier {
	/**
	 * 全局实例对象
	 */
	static PresidentOfficier instance = null;
	/**
	 * 协议日志记录器
	 */
	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 所有官职信息:key--官职ID,官职Entity
	 */
	private Map<Integer, OfficerEntity> mOfficerEntity;

	/**
	 * 所有官职对应玩家信息:key--玩家ID,官职
	 */
	private Map<String, Integer> mPlayerOfficer;

	/**
	 * 获取所有官职
	 */
	public List<OfficerEntity> getOfficerEntityList() {
		List<OfficerEntity> list = new ArrayList<OfficerEntity>();
		list.addAll(mOfficerEntity.values());
		return list;
	}

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static PresidentOfficier getInstance() {
		if (instance == null) {
			instance = new PresidentOfficier();
		}
		return instance;
	}

	/**
	 * 构造
	 */
	private PresidentOfficier() {
		mOfficerEntity = new ConcurrentHashMap<Integer, OfficerEntity>();
		mPlayerOfficer = new ConcurrentHashMap<String, Integer>();
	}

	/**
	 * @param officerId
	 * @return
	 */
	public OfficerEntity getEntityById(int officerId) {
		return mOfficerEntity.get(officerId);
	}
	
	/**
	 * 当前的总统是不是临时总统
	 * @return
	 */
	protected boolean isProvisionalPresident() {
		OfficerEntity officerEntity = this.getEntityById(OfficerType.OFFICER_01_VALUE);
		if (officerEntity == null || HawkOSOperator.isEmptyString(officerEntity.getPlayerId())) {
			return false;
		}
		
		return officerEntity.getEndTime() <= 0l;
	}
	/**
	 * 是否是临时总统
	 * @param playerId
	 * @return
	 */
	protected boolean isProvisionalPresident(String playerId) {
		OfficerEntity officerEntity = this.getEntityById(OfficerType.OFFICER_01_VALUE);
		if (officerEntity == null) {
			return false;
		}
		if (!officerEntity.getPlayerId().equals(playerId)) {
			return false;
		}
		//endTime设置过说明没有被任命过.
		if (officerEntity.getEndTime() != 0) {
			return false;
		}
		
		return true;
	}

	/**
	 * @param playerId
	 * @return
	 */
	public OfficerEntity getEntityByPlayerId(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		for (OfficerEntity entity : mOfficerEntity.values()) {
			if (playerId.equals(entity.getPlayerId())) {
				return entity;
			}
		}
		return null;
	}

	/**
	 * 获取玩家官职id
	 * 
	 * @param playerId
	 * @return
	 */
	public int getOfficerId(String playerId) {
		if (!HawkOSOperator.isEmptyString(playerId) && mPlayerOfficer.containsKey(playerId)) {
			return mPlayerOfficer.get(playerId);
		} else {
			return OfficerType.OFFICER_00_VALUE;
		}
	}

	/**
	 * 设置官职
	 * 
	 * @param playerId
	 * @param officerId
	 * @return
	 */
	public boolean setOfficer(String playerId, int officerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
				

		OfficerEntity entity = mOfficerEntity.get(officerId);
		if (entity == null) {
			return false;
		}
				

		OfficerCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OfficerCfg.class, officerId);
		if (cfg == null) {
			return false;
		}
		
		logger.info("set officer oldPlayerId:{}, newPlayerId:{}, officerId:{}", entity.getPlayerId(), playerId, officerId);

		if (!HawkOSOperator.isEmptyString(entity.getPlayerId())) {
			mPlayerOfficer.remove(entity.getPlayerId());
			syncOfficerEffect(entity.getPlayerId(), officerId);
		}

		entity.setPlayerId(playerId);
		//和客户端约定,国王是否设置过通过设置endtime来判定. 那么前端的逻
		if (officerId == OfficerType.OFFICER_01_VALUE) {
			entity.setEndTime(HawkTime.getMillisecond() + PresidentConstCfg.getInstance().getAppointTime());
		} else {
			entity.setEndTime(HawkTime.getMillisecond() + cfg.getCdTime() * 1000);
		}		
		mPlayerOfficer.put(playerId, officerId);
		syncOfficerEffect(entity.getPlayerId(), officerId);
		
		// 给跨服同步下作用号
		if (CrossService.getInstance().isEmigrationPlayer(playerId)) {
			RedisProxy.getInstance().updatePlayerOfficerId(playerId, officerId);
			RedisProxy.getInstance().addPlayerOfficerIdSet(playerId, officerId);
		}
		return true;
	}

	/**
	 * 撤销官职
	 * 
	 * @param playerId
	 * @param officerId
	 * @return
	 */
	public boolean unsetOfficer(String playerId, int officerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
				
		OfficerEntity entity = mOfficerEntity.get(officerId);
		if (entity == null) {
			return false;
		}

		if (!playerId.equals(entity.getPlayerId())) {
			return false;
		}
		
		logger.info("unset officer playerId:{},officerId:{}",  playerId, officerId);

		entity.setPlayerId("");
		mPlayerOfficer.remove(playerId);
		
		syncOfficerEffect(playerId, officerId);
		
		if (CrossService.getInstance().isEmigrationPlayer(playerId)) {
			RedisProxy.getInstance().removePlayerOfficerId(playerId);
			RedisProxy.getInstance().removePlayerOfficerIdSet(playerId, officerId);
		}
		
		return true;
	}

	/**
	 * 获取官职作用号
	 * 
	 * @param playerId
	 * @param effId
	 * @return
	 */
	public int getEffectOfficer(String playerId, int effId) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		
		if (PresidentFightService.getInstance().isFightPeriod()) {
			return 0;
		}

		int officerId = getOfficerId(playerId);
		if (officerId == OfficerType.OFFICER_00_VALUE) {
			return 0;
		}

		OfficerCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OfficerCfg.class, officerId);
		if (cfg == null) {
			return 0;
		}

		return cfg.getEffVal(effId);
	}
	
	/**
	 * 国王战切换的时候同步作用号
	 * 国王产生的时候  会调用 通知作用发生改变@see  onPresidentChanged
	 * @return
	 */
	public boolean synOfficerEffect() {
		for (OfficerEntity entity : mOfficerEntity.values()) {				
			//如果该官职有玩家同步作用号
			if (!HawkOSOperator.isEmptyString(entity.getPlayerId())) {
				syncOfficerEffect(entity.getPlayerId(), entity.getOfficerId());
			}
		}
		
		return true;
	}
	/**
	 * 同步官职作用号数据
	 * 
	 * @param playerId
	 * @param officerId
	 * @return
	 */
	public boolean syncOfficerEffect(String playerId, int officerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		
		OfficerCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OfficerCfg.class, officerId);
		if (cfg == null || cfg.getEffTypes() == null) {
			return false;
		}

		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player != null) {
			player.getPush().syncPlayerEffect(cfg.getEffTypes());
		}
		
		return true;
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		long startTime = HawkTime.getMillisecond();

		// DB数据和和配置数据对比，如果配置文件有db无则创建，反之则删除
		List<OfficerEntity> dbList = HawkDBManager.getInstance().query("from OfficerEntity");
		if (dbList != null && dbList.size() > 0) {
			for (OfficerEntity entity : dbList) {
				OfficerCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OfficerCfg.class, entity.getOfficerId());
				if (cfg != null) {
					mOfficerEntity.put(entity.getOfficerId(), entity);
				} else {
					entity.delete();
				}
			}
		}

		// DB无则创建
		ConfigIterator<OfficerCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(OfficerCfg.class);
		while (iterator.hasNext()) {
			OfficerCfg cfg = iterator.next();
			if (mOfficerEntity.containsKey(cfg.getId())) {
				continue;
			}
			if (cfg.getId() == OfficerType.OFFICER_CROSS_PRESIDENT_VALUE) {
				continue;
			}
			
			OfficerEntity entity = new OfficerEntity();
			entity.setOfficerId(cfg.getId());

			// 创建
			if (!HawkDBManager.getInstance().create(entity)) {
				logger.error("create officer entity failed, officerId: {}", cfg.getId());
				continue;
			}
			mOfficerEntity.put(entity.getOfficerId(), entity);

			logger.info("create officer entity success, officerId: {}", cfg.getId());
		}

		for (OfficerEntity entity : mOfficerEntity.values()) {
			if (!HawkOSOperator.isEmptyString(entity.getPlayerId())) {
				mPlayerOfficer.put(entity.getPlayerId(), entity.getOfficerId());
			}
		}

		// 检查国王官职
		OfficerEntity entity = mOfficerEntity.get(OfficerType.OFFICER_01_VALUE);
		if (!HawkOSOperator.isEmptyString(PresidentFightService.getInstance().getPresidentPlayerId()) &&
				HawkOSOperator.isEmptyString(entity.getPlayerId())) {
			entity.setPlayerId(PresidentFightService.getInstance().getPresidentPlayerId());
			mPlayerOfficer.put(entity.getPlayerId(), entity.getOfficerId());
		}

		logger.info("load officer costtime: {} ", HawkTime.getMillisecond() - startTime);

		return true;
	}
	
	/**
	 * 战争开始的时候清理掉所有的buff.
	 */
	protected void onWarStart() {
		for (OfficerEntity entity : mOfficerEntity.values()) {
			entity.setEndTime(0);
			//国王清理.
			
			if (HawkOSOperator.isEmptyString(entity.getPlayerId())) {
				continue;
			}
			String oldPlayerId = entity.getPlayerId();
			mPlayerOfficer.remove(entity.getPlayerId());					
			entity.setPlayerId("");
			
			//如果该官职有玩家同步作用号
			if (!HawkOSOperator.isEmptyString(oldPlayerId)) {
				syncOfficerEffect(oldPlayerId, entity.getOfficerId());
			}
		}
		
		String serverId = GsConfig.getInstance().getServerId();
		RedisProxy.getInstance().deleteServerKing(serverId);
	}
	/**
	 * 国王变更之后的通知(需要判断两个id是否一致, 连任的情况)
	 */
	protected void onPresidentChanged(String lastPresidentId, String currPresidentId) {
		try {
			if (HawkOSOperator.isEmptyString(currPresidentId)) {
				currPresidentId = "";
			}
			logger.info("onPresidentChanged officer last: {} curr: {}", lastPresidentId, currPresidentId);
	
			// 第一次国王战上界国王为空
			if (HawkOSOperator.isEmptyString(lastPresidentId)) {
				lastPresidentId = "";
			}
			
			//上一届和当前届都没有国王就不做任何处理
			if (lastPresidentId.equals("") && currPresidentId.equals("")) {
				return;
			}
			
			//老国王的作用号不需用同步.
			OfficerEntity entity = mOfficerEntity.get(OfficerType.OFFICER_01_VALUE); 
			if (!currPresidentId.equals(entity.getPlayerId())) {
				entity.setPlayerId(currPresidentId);
				mPlayerOfficer.put(entity.getPlayerId(), entity.getOfficerId());
				syncOfficerEffect(entity.getPlayerId(), entity.getOfficerId());
				//清理上个国王的官职和作用号
				if (!HawkOSOperator.isEmptyString(lastPresidentId)) {
					mPlayerOfficer.remove(lastPresidentId);
					syncOfficerEffect(lastPresidentId, entity.getOfficerId());
				}
			}
			
			//王战结束的时候清理一下跨服国王信息
			RedisProxy.getInstance().deleteCrossServerKing(GsConfig.getInstance().getServerId());
			CrossPlayerStruct playerStruct = null;
			if (!HawkOSOperator.isEmptyString(currPresidentId)) {
				//如果是跨服开启的王战, 设置endTime把国王设置为正式总统, 如果是非本服玩家则写入国王信息.
				if (CrossActivityService.getInstance().isOpen()) {
					entity.setEndTime(HawkTime.getMillisecond() + PresidentConstCfg.getInstance().getAppointTime() + 60 * 1000l);
					if(!GlobalData.getInstance().isLocalPlayer(currPresidentId)) {
						PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
						String guildId = presidentCity.getPresident().getPlayerGuildId();
						playerStruct = RedisProxy.getInstance().getCrossGuildLeaderInfo(guildId);
						if (playerStruct != null) {						
							//插入跨服国王.
							presidentCity.pushCrossKing(playerStruct);
						} else {
							logger.error("can not get cross guild leard info  guildId:{}", guildId);
						} 
					}									
				}
				
				if (playerStruct == null) {
					//尝试当做本服玩家加载
					Player presidentPlayer = GlobalData.getInstance().makesurePlayer(currPresidentId);
					if (presidentPlayer != null) {
						playerStruct = BuilderUtil.buildCrossPlayer(presidentPlayer).build();
					}
				}
				
				if (playerStruct != null) {
					PresidentFightService.getInstance().getPresidentCity().pushKingInfo(playerStruct);
				}
				
			}			
			
			
			
			
			//这里再加载一次跨服国王的缓存吧.
			PresidentFightService.getInstance().getPresidentCity().loadCrossServerKing();			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void notifySaveEntity() {
		Map<Integer, OfficerEntity> officerEntity = this.mOfficerEntity;
		if (officerEntity != null) {
			for (Entry<Integer, OfficerEntity> entry : officerEntity.entrySet()) {
				entry.getValue().notifyUpdate();
			}
		}
		
		logger.info("notfify presidentOfficer save");
	}
	
	/**
	 * 同步总统宣言
	 * @param player
	 */
	public void synPresidentManifesto(Player player) {
		PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
		PresidentManifestoResp.Builder respBuilder = PresidentManifestoResp.newBuilder();
		respBuilder.setManifesto(presidentCity.getManifesto() == null ? "" : presidentCity.getManifesto());
		respBuilder.setUpdateTimes(presidentCity.getUpdateManifestoTimes());
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PRESIDENT_MANIFESTO_RESP_VALUE, respBuilder);
		player.sendProtocol(protocol);
	}
	
	public int onPresidentManifestoUpdate(Player player, String manifesto) {
		PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
		if (presidentCity.getUpdateManifestoTimes() > 0) {
			ConsumeItems consumeItems = ConsumeItems.valueOf();
			consumeItems.addConsumeInfo(PresidentConstCfg.getInstance().getManifestoCostList());
			int checkResult = consumeItems.checkConsumeAndGetResult(player);
			if (checkResult != Status.SysError.SUCCESS_OK_VALUE) {
				return checkResult;
			}
			
			consumeItems.consumeAndPush(player, Action.PRESIDENT_UPDATE_MANIFESTO);
		}
		
		presidentCity.setManifesto(manifesto);
		presidentCity.setUpdateManifestoTimes(presidentCity.getUpdateManifestoTimes() + 1);
		
		this.synPresidentManifesto(player);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 是否是跨服国王。
	 * @param playerId
	 * @return
	 */
	public boolean isCrossKing(String playerId) {
		return PresidentFightService.getInstance().getPresidentCity().isCrossServerKing(playerId);
	}
	
	/**
	 * 是否是八大功臣
	 * @param playerId
	 * @return
	 */
	public boolean isMeritoriousOfficials(String playerId){
		int officerId = getOfficerId(playerId);
		
		return officerId == OfficerType.OFFICER_02_VALUE 
				|| officerId == OfficerType.OFFICER_03_VALUE 
				|| officerId == OfficerType.OFFICER_04_VALUE 
				|| officerId == OfficerType.OFFICER_05_VALUE 
				|| officerId == OfficerType.OFFICER_06_VALUE 
				|| officerId == OfficerType.OFFICER_07_VALUE 
				|| officerId == OfficerType.OFFICER_08_VALUE 
				|| officerId == OfficerType.OFFICER_09_VALUE ;
	}
}
