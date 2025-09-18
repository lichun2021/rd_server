package com.hawk.game.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.BuffCfg;
import com.hawk.game.entity.GlobalBuffEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.RefreshEffectMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.SynGlobalBuff;
import com.hawk.game.util.BuilderUtil;
import com.hawk.msg.GlobalBuffAddMsg;
import com.hawk.msg.GlobalBuffRemoveMsg;

/**
 * 全局buff 较少，所以就不支持一次性加多次buff,减少buff 等能减少消息量的做法，做起来麻烦.
 * @author jm
 *
 */
public class BuffService extends HawkAppObj {
	private static Logger logger = LoggerFactory.getLogger("Server"); 
	private static BuffService instance = null;
	
	/**
	 * 写内存
	 */
	private Map<Integer, GlobalBuffEntity> writeMap = new HashMap<>();	
	/**
	 *读取作用号的地方比较频繁, 试试读写分离 
	 */
	private Map<Integer, GlobalBuffEntity> readMap = new HashMap<>();
	
	public BuffService(HawkXID xid) {
		super(xid);
		
		instance = this;
	}
	
	public static BuffService getInstance() {
		return instance;
	}
	
	@MessageHandler
	private void removeBuffEvent(GlobalBuffRemoveMsg msg) {
		logger.info("buffService removeBuff buffList:{}", msg.getBuffIdList());
		List<Integer> buffIdList = msg.getBuffIdList(); 
		for (Integer buffId : buffIdList) {
			BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
			this.removeBuff(buffCfg.getEffect());
		}		
	}
	
	@MessageHandler
	private void addBuffEvent(GlobalBuffAddMsg msg) {
		logger.info("buffService addBuff buffList:{}, buffStartTime:{} buffEndTime:{}", msg.getBuffIdList(), msg.getBuffStartTime(), msg.getBuffEndTime());
		List<Integer> buffIdList = msg.getBuffIdList();
		for (Integer buffId : buffIdList) {
			BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);					
			this.addBuff(buffCfg.getEffect(), buffCfg.getValue(),  msg.getBuffStartTime(), msg.getBuffEndTime(), true);
		}
				
	}
	
	private GlobalBuffEntity addBuff(int effect, int effectValue, long startTime, long endTime, boolean isSyn) {
		GlobalBuffEntity globalBuffEntity = new GlobalBuffEntity();
		globalBuffEntity.setEndTime(endTime);
		globalBuffEntity.setStartTime(startTime);
		globalBuffEntity.setStatusId(effect);
		globalBuffEntity.setValue(effectValue);
		
		writeMap.put(effect, globalBuffEntity);
		
		buffChange(effect);
		
		if (isSyn) {
			this.synGlobalBuffEntity(globalBuffEntity);
			
		}
		
		return globalBuffEntity;
	}
	
	private void synGlobalBuffEntity(GlobalBuffEntity... globalBuffEntitys){
		if (globalBuffEntitys.length > 0) {
			SynGlobalBuff.Builder sbuilder = SynGlobalBuff.newBuilder(); 
			for (GlobalBuffEntity globalBuffEntity : globalBuffEntitys) {
				sbuilder.addStateInfos(BuilderUtil.genStateInfoBuilder(globalBuffEntity));
			}			
			
//			sendAllPlayer(HawkProtocol.valueOf(HP.code.SYN_GLOBAL_BUFF_VALUE, sbuilder.build().toByteArray()));
		}
		
	}
	
	private void sendAllPlayer(HawkProtocol protocol) {
		Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : players) {
			player.sendProtocol(protocol);
		}
	}
	
	private void removeBuff(Integer statusId) {
		GlobalBuffEntity globalBuffEntity = writeMap.remove(statusId);
		
		if (globalBuffEntity != null) {
			buffChange(statusId);
			globalBuffEntity.setEndTime(0);
			this.synGlobalBuffEntity(globalBuffEntity);
		} 		
	}
	
	/**
	 * 写同步给读map, 通知给在线玩家刷新effect
	 */
	private void buffChange(int statusId) {
		Map<Integer, GlobalBuffEntity> map = new HashMap<>(writeMap.size());
		map.putAll(writeMap);
		
		readMap = map;
				
		Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : players) {
			RefreshEffectMsg refreshEffectMsg = new RefreshEffectMsg();
			refreshEffectMsg.setEffectId(statusId);
			HawkApp.getInstance().postMsg(player.getXid(), refreshEffectMsg);
		}
	}
	
	public int getEffectValue(int effectId) {
		GlobalBuffEntity buffEntity = readMap.get(effectId);
		
		return buffEntity == null ? 0 : buffEntity.getValue();
	}
	
	/**
	 * 不可修改
	 * @return
	 */
	public Map<Integer, GlobalBuffEntity> getBuffMap() {
		return readMap;
	}
}
