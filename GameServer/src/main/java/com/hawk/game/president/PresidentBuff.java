package com.hawk.game.president;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.log.Action;
import com.hawk.game.protocol.President.PresidentBuffInfoResp;
import com.hawk.game.protocol.President.PresidentBuffMsg;
import com.hawk.game.protocol.President.PresidentResourceInfoSyn;

public class PresidentBuff {	
	/**
	 * {effectId, effectValue}
	 */
	private Map<Integer, Integer> effectMap = new HashMap<>();
	/**
	 * {buffId, endTime}
	 */
	private Map<Integer, Long> buffMap = new ConcurrentHashMap<>();
	
	private static PresidentBuff instance = new PresidentBuff();
	
	private PresidentBuff() {
		 
	}
	
	public static PresidentBuff getInstance() {
		return instance;
	}
	
	public boolean init() {
		Map<Integer, Long> map = LocalRedis.getInstance().getPresidentBuff();
		if (map == null ) {
			buffMap = new ConcurrentHashMap<>();
		} else {
			buffMap = new ConcurrentHashMap<>(map);
		}
		
		this.initEffectMap();
		
		return effectMap != null;
	}	
	
	public void onTick() {
		try {
			long curTime = HawkTime.getMillisecond();			
			int validCount = 0; 
			for (Entry<Integer, Long> entry : buffMap.entrySet()) {
				if (entry.getValue() >= curTime) {
					validCount ++;
				}
			}
			
			//当前生效的buff 和effect 的size 不一样的时候才需要重新初始effect 
			if (validCount != effectMap.size()) {							
				initEffectMap();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void initEffectMap() {		
		Map<Integer, Integer> effectMap = new HashMap<>();
		long curTime = HawkTime.getMillisecond();
		for (Entry<Integer, Long> entry : buffMap.entrySet()) {
			if (curTime > entry.getValue()) {
				continue;
			}
			BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, entry.getKey());
			effectMap.put(buffCfg.getEffect(), buffCfg.getValue());
		}
		
		Set<Integer> oldEffectKey = this.effectMap.keySet();
		Set<Integer> newEffectKey = effectMap.keySet();
		this.effectMap = effectMap;		
		
		Collection<Integer> colls = CollectionUtils.disjunction(oldEffectKey, newEffectKey);
		if (colls.isEmpty()) {
			return;
		}
		
		Integer[] changeEffect = new Integer[colls.size()];
		colls.toArray(changeEffect);
		this.synEffectChange(changeEffect);
	}
	
	
	
	public int getEffect(String playerId, int effectId) {	
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		if (CrossService.getInstance().isCrossPlayer(playerId)) {
			return 0;
		}
		
		return effectMap.getOrDefault(effectId, 0);
	}
	
	
	/**
	 * 
	 * @param playerId
	 * @return 错误码
	 */
	public int onOpenBuff(Player player,  int buffId, int hpCode) {
		String presidentPlayerId = PresidentFightService.getInstance().getPresidentPlayerId();
		if (HawkOSOperator.isEmptyString(presidentPlayerId) || !player.getId().equals(presidentPlayerId)) {
			return Status.Error.PRESIDENT_NOT_KING_VALUE;
		}
		
		PresidentConstCfg presidentCfg = PresidentConstCfg.getInstance();
		
		List<ItemInfo> itemInfos = presidentCfg.getGlobalBuffMap().get(buffId);
		if (CollectionUtils.isEmpty(itemInfos)) {
			Player.logger.warn("playerId:{} received buffId:{} can not found costItemList", player.getId(), buffId);			
			
			return Status.SysError.DATA_ERROR_VALUE;
		}
		
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
		if (buffCfg == null) {
			Player.logger.warn("playerId:{} buffId:{} can not found buffcfg", player.getId(), buffId);
			
			return Status.SysError.DATA_ERROR_VALUE;
		}
		
		long oldTime = buffMap.getOrDefault(buffId, 0L);
		if (oldTime != 0 && buffCfg.getBuffCd() * 1000 > HawkTime.getMillisecond() - (oldTime - buffCfg.getTime() * 1000)) {
			Player.logger.warn("playerId:{} buffId:{} cd time", player.getId(), buffId);
			
			return Status.Error.PRESIDENT_BUFF_CD_VALUE;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(itemInfos);
		int result = consumeItems.checkResult(player);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			return result;
		}
		
		consumeItems.consumeAndPush(player, Action.PRESIDENT_OPEN_BUFF);
		
		buffMap.put(buffId, buffCfg.getTime() * 1000L + HawkTime.getMillisecond());
		if (oldTime <= 0) {
			this.initEffectMap();
		}		
		LocalRedis.getInstance().addPresidentBuff(buffMap);
		
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}   
	
	/**
	 *  同步作用号的改变
	 */
	public void synEffectChange(Integer [] effectIds) {
		Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
		EffType[] effArray = new EffType[effectIds.length];
		for (int i = 0; i < effectIds.length; i ++) {
			effArray[i] = EffType.valueOf(effectIds[i]);
		}
		
		players.stream().forEach(player->player.getPush().syncPlayerEffect(effArray));
	}
	
	public  void synBuff(Player player) {
		PresidentBuffInfoResp.Builder respBuilder = PresidentBuffInfoResp.newBuilder();
		long curTime = HawkTime.getMillisecond();		
		for (Entry<Integer, Long> entry : buffMap.entrySet()) {
			if (entry.getValue() < curTime) {
				continue;
			}
			PresidentBuffMsg.Builder msgBuilder = PresidentBuffMsg.newBuilder();
			msgBuilder.setBuffId(entry.getKey());
			msgBuilder.setEndTime(entry.getValue());
			
			respBuilder.addBuffMsgs(msgBuilder);
		}
		
		//应前端要求组装.
		PresidentResourceInfoSyn.Builder sbuilder = LocalRedis.getInstance().getPresidentResource();
		if (sbuilder != null && sbuilder.hasAttrType()) {
			PresidentBuffMsg.Builder msgBuilder = PresidentBuffMsg.newBuilder();
			msgBuilder.setBuffId(sbuilder.getAttrType());
			msgBuilder.setEndTime((sbuilder.getLastSetTime() + PresidentConstCfg.getInstance().getChangeResCd()) * 1000l);
			respBuilder.addBuffMsgs(msgBuilder);
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PRESIDENT_BUFF_INFO_RESP_VALUE, respBuilder);
		player.sendProtocol(protocol);
	}
}
