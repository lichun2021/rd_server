package com.hawk.game.lianmengxzq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.redis.HawkRedisSession;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.game.GsConfig;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;

public class XZQRedisData {
	
	
	
	
	private static XZQRedisData instance = null;
	public static XZQRedisData getInstance() {
		if (instance == null) {
			instance = new XZQRedisData();
		}
		return instance;
	}
	private XZQRedisData() {
	}

	
	/**
	 * 获取小站区开服表示
	 * @return
	 */
	private String getXZQFixKey(){
		String key = "xzq_fix_flag";
		StatisManager.getInstance().incRedisKey(key);
		return key +":"+GsConfig.getInstance().getServerId();
	}
	
	
	/**
	 * 获取小站区开服表示
	 * @return
	 */
	private String getXZQOpenKey(){
		String key =  LocalRedis.getInstance().getLocalIdentify() + ":xzq_open_flag";
		StatisManager.getInstance().incRedisKey(key);
		return key;
	}
	
	
	/**
	 * 获取小站区开服表示
	 * @return
	 */
	private String getXZQGlobalOpenKey(){
		String key ="xzq_open_flag";
		StatisManager.getInstance().incRedisKey(key);
		return key+":"+GsConfig.getInstance().getServerId();
	}
	/**
	 * 活动阶段信息KEY
	 * @return
	 */
	private String getXZQInfoKey() {
		String key =  LocalRedis.getInstance().getLocalIdentify() + ":xzq_info";
		StatisManager.getInstance().incRedisKey(key);
		return key;
	}
	/**
	 * 建筑控制信息KEY
	 * @return
	 */
	public String getXZQControlKey(){
		String infoKey = LocalRedis.getInstance().getLocalIdentify() + ":xzq_info:control";
		StatisManager.getInstance().incRedisKey(infoKey);
		return infoKey;
	}
	
	
	/**
	 * 染色信息KEY
	 * @return
	 */
	public String getXZQForceColorlKey(){
		String infoKey = LocalRedis.getInstance().getLocalIdentify() + ":xzq_info:forceColor";
		StatisManager.getInstance().incRedisKey(infoKey);
		return infoKey;
	}
	
	
	/**
	 * 建筑刻字记录KEY
	 * @param serverId
	 * @param pointId
	 * @return
	 */
	public String getBuildRecordKey(String serverId,int pointId){
		String key = "xzq_build_records:";
		String key2 = key + serverId + ":" + pointId;
		StatisManager.getInstance().incRedisKey(key);
		return key2;
	}
	/**
	 * 和服处理KEY
	 * @return
	 */
	public String getXZQMergeKey(){
		String infoKey = "xzq_merge:";
		StatisManager.getInstance().incRedisKey(infoKey);
		return infoKey;
	}
	
	
	public String getXZQTicketKey(int termId,int day){
		String key1 = LocalRedis.getInstance().getLocalIdentify() + ":xzq_gift_num:";
		String key2 = key1 + termId + ":"+ day;
		StatisManager.getInstance().incRedisKey(key1);
		return key2;
	}
	
	/**
	 * 奖励信息KEY
	 * @param guild
	 * @param termId
	 * @return
	 */
	public String getXZQGiftNumKey(int termId,String guild){
		String key1 = LocalRedis.getInstance().getLocalIdentify() + ":xzq_gift_num:";
		String key2 = key1 + guild+":" + termId;
		StatisManager.getInstance().incRedisKey(key1);
		return key2;
	}
	
	public String getXZQGiftSendKey(int termId,String guildId){
		String key1 = LocalRedis.getInstance().getLocalIdentify() + ":xzq_gift_send:";
		String key2 = key1 + ":" + termId + ":" + guildId;
		StatisManager.getInstance().incRedisKey(key1);
		return key2;
	}
	
	public String getXZQGiftPlayerReceiveCountKey(int termId,int pointId,int giftId){
		String key1 = LocalRedis.getInstance().getLocalIdentify() + ":xzq_gift_player_count:";
		String key2 = key1 + termId+":" + pointId+":"+giftId;
		StatisManager.getInstance().incRedisKey(key1);
		return key2;
	}
	
	
	
	/**
	 * 获取修正标识
	 * @return
	 */
	public String getXZQFixFlag(){
		String key = getXZQFixKey();
		String str = RedisProxy.getInstance().getRedisSession().getString(key);
		return str;
	}
	
	
	/**
	 * 更新修正标识
	 * @param str
	 */
	public void updateXZQFixFlag(String str){
		String key = getXZQFixKey();
		RedisProxy.getInstance().getRedisSession().setString(key, str);
	}
	
	
	

	/**
	 * 获取开放标识
	 * @return
	 */
	public String getXZQOpenFlag(){
		String key = getXZQOpenKey();
		String str = LocalRedis.getInstance().getRedisSession().getString(key);
		if(!HawkOSOperator.isEmptyString(str)){
			return str;
		}
		String gkey = getXZQGlobalOpenKey();
		String gstr = RedisProxy.getInstance().getRedisSession().getString(gkey);
		return gstr;
	}
	
	
	/**
	 * 更新开放标识
	 * @param str
	 */
	public void updateXZQOpenFlag(String str){
		String key = getXZQGlobalOpenKey();
		RedisProxy.getInstance().getRedisSession().setString(key, str);
	}
	

	/**
	 * 更新小战区刻字
	 * @param serverId
	 * @param pointId
	 * @param data
	 */
	public void updateXZQBuildRecord(String serverId,int pointId,byte[] data){
		String key = getBuildRecordKey(serverId,pointId);
		RedisProxy.getInstance().getRedisSession().setBytes(key, data);
	}
	
	/**
	 * 获取建筑刻字
	 * @param serverId
	 * @param pointId
	 * @return
	 */
	public byte[] getXZQBuildRecord(String serverId,int pointId){
		String key = getBuildRecordKey(serverId,pointId);
		byte[] byteData = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
		return byteData;
	}
	
	/**
	 * 删除建筑刻字
	 * @param serverId
	 * @param pointId
	 */
	public void deleteXZQBuildRecord(String serverId,int pointId){
		String key = getBuildRecordKey(serverId,pointId);
		RedisProxy.getInstance().getRedisSession().del(key);
	}
	
	
	/**
	 * 删除染色
	 */
	public void delAllXZQForceColor(){
		String key = getXZQForceColorlKey();
		LocalRedis.getInstance().getRedisSession().del(key);
	}
	
	/**
	 * 更新染色
	 * @param color
	 */
	public void updateXZQForceColor(XZQForceColor color){
		String key = getXZQForceColorlKey();
		String field = color.getGuildId();
		LocalRedis.getInstance().getRedisSession().hSet(key, field, color.serializ());
	}
	
	/**
	 * 删除染色
	 * @param guildId
	 */
	public void delXZQForceColor(String guildId){
		String key = getXZQForceColorlKey();
		LocalRedis.getInstance().getRedisSession().hDel(key, guildId);
	}
	
	/**
	 * 获取染色
	 * @return
	 */
	public Map<String,XZQForceColor> loadXZQFoceColor(){
		Map<String,XZQForceColor> map = new ConcurrentHashMap<>();
		String key = getXZQForceColorlKey();
		Map<String,String> data = LocalRedis.getInstance().getRedisSession().hGetAll(key);
		for(String val : data.values()){
			XZQForceColor color = new XZQForceColor();
			color.mergeFrom(val);
			map.put(color.getGuildId(), color);
		}
		return map;
	}
	
	/**
	 * 获取战区阶段信息
	 * @return
	 */
	public XZQServiceInfoData loadXZQServiceInfo() {
		HawkRedisSession redisSession = LocalRedis.getInstance().getRedisSession();
		String str = redisSession.getString(getXZQInfoKey());
		XZQServiceInfoData result = new XZQServiceInfoData();
		if (Objects.nonNull(str)) {
			try {
				result.mergeFrom(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	/**
	 * 更新战区阶段信息
	 * @param info
	 */
	public void updateXZQServiceInfo(XZQServiceInfoData info) {
		LocalRedis.getInstance().getRedisSession().setString(getXZQInfoKey(), info.serializ());
	}
	
	
	/**
	 * 更新控制信息
	 * @param map
	 */
	public void updateControl(Map<String,String> map){
		String infoKey = getXZQControlKey();
		LocalRedis.getInstance().getRedisSession().hmSet(infoKey, map,0);
	}
	
	/**
	 * 删除控制信息
	 */
	public void delAllControlInfo(){
		String infoKey = getXZQControlKey();
		LocalRedis.getInstance().getRedisSession().del(infoKey);
	}
	/**
	 * 获取控制信息
	 * 
	 * @return
	 */
	public Table<String, Integer, Integer> loadControlInfo() {
		String infoKey = getXZQControlKey();
		Map<String, String> rlt = LocalRedis.getInstance().getRedisSession().hGetAll(infoKey);
		Table<String, Integer, Integer> controls = HashBasedTable.create();
		for (Entry<String, String> entry : rlt.entrySet()) {
			String key = entry.getKey();
			String guildId = entry.getValue();
			int cfgId = Integer.parseInt(key);
			XZQPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, cfgId);
			if (cfg == null) {
				continue;
			}

			int level = cfg.getLevel();
			int count = 0;
			if (controls.contains(guildId, level)) {
				count = controls.get(guildId, level);
			}
			count += 1;
			controls.put(guildId, level, count);
		}
		return controls;
	}
	
	/**
	 * 获取和服信息
	 * @return
	 */
	public String getMegerServerData(){
		String serverId = GsConfig.getInstance().getServerId();
		String key = getXZQMergeKey() + serverId;
		String data = LocalRedis.getInstance().getRedisSession().getString(key);
		return data;
	}
	
	/**
	 * 更新和服信息
	 * @param data
	 */
	public void updateMegerServerData(String data){
		String serverId = GsConfig.getInstance().getServerId();
		String key = getXZQMergeKey() + serverId;
		LocalRedis.getInstance().getRedisSession().setString(key,data);
	}
	
	
	/**
	 * 门票数量
	 * @param termId
	 * @param day
	 * @param playerIds
	 * @return
	 */
	public Map<String,Integer> getPlayerTicketNum(int termId,int day,List<String> playerIds){
		String key = this.getXZQTicketKey(termId, day);
		Map<String,Integer> rlt = new HashMap<>();
		String[] playerArr = playerIds.toArray(new String[playerIds.size()]);
		List<String> counts= LocalRedis.getInstance().getRedisSession().hmGet(key, playerArr);
		for (int i = 0; i < playerArr.length; i++) {
			if (!HawkOSOperator.isEmptyString(counts.get(i))) {
				rlt.put(playerArr[i], Integer.parseInt(counts.get(i)));
			}
		}
		return rlt;
	}


	/**
	 * 更新门票
	 * @param termId
	 * @param day
	 * @param tickets
	 */
	public void updatePlayerTicketNum(int termId,int day,Map<String,String> tickets){
		String key = this.getXZQTicketKey(termId, day);
		LocalRedis.getInstance().getRedisSession().hmSet(key, tickets,  (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	
	/** 小战区礼包个数更新
	 * 
	 * @param giftId
	 * @param number
	 * @return 
	 * */
	public void updateXZQGiftInfo(int termId,  String guildId, int pointId,int giftId, int sendNum, int totalNum) {
		String number = String.valueOf(sendNum) + "_" + String.valueOf(totalNum);
		updateXZQGiftInfo(termId,guildId,pointId, giftId, number);
	}
	
	/** 小战区礼包个数更新
	 * 
	 * @param giftId
	 * @param number
	 * @return */
	public void updateXZQGiftInfo(int termId, String guildId,int pointId, int giftId, String info) {
		String key1 = this.getXZQGiftNumKey(termId, guildId);
		String key2 = pointId + ":" + String.valueOf(giftId);
		LocalRedis.getInstance().getRedisSession().hSet(key1, key2, info,  (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	/** 获取小战区礼包信息 */
	public String getXZQGiftInfo(int termId,String guildId, int pointId, int giftId) {
		String key1 = this.getXZQGiftNumKey(termId, guildId);
		String key2 = pointId + ":" + String.valueOf(giftId);
		return LocalRedis.getInstance().getRedisSession().hGet(key1, key2);
	}
	
	/**
	 * 获取小战区建筑联盟所有礼包信息
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public Map<String,String> getAllXZQGiftInfo(int termId,String guildId){
		String key1 = this.getXZQGiftNumKey(termId, guildId);
		Map<String,String> gifts = LocalRedis.getInstance().getRedisSession().hGetAll(key1);
		return gifts;
	}
	
	/**
	 * 获取小战区礼包所有玩家接收次数
	 * @param termId
	 * @param pointId
	 * @param giftId
	 * @return
	 */
	public Map<String,Integer> getXZQPlayerReceiveCounts(int termId, int pointId,int giftId){
		Map<String,Integer> counts = new HashMap<>();
		String key1 = this.getXZQGiftPlayerReceiveCountKey(termId, pointId,giftId);
		Map<String,String> rlt = LocalRedis.getInstance().getRedisSession().hGetAll(key1);
		for(Entry<String, String> entry : rlt.entrySet()){
			String key = entry.getKey();
			String val = entry.getValue();
			counts.put(key, Integer.parseInt(val));
		}
		return counts;
	}
	
	/**
	 * 获取小战区礼包所有玩家接收次数
	 * @param termId
	 * @param pointId
	 * @param giftId
	 * @return
	 */
	public void updateXZQPlayerReceiveCounts(int termId, int pointId,int giftId,Map<String,String> counts){
		String key1 = this.getXZQGiftPlayerReceiveCountKey(termId, pointId,giftId);
		LocalRedis.getInstance().getRedisSession().hmSet(key1, counts, (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	/** 获取小战区礼包接收次数
	 * 
	 * @param pointId
	 * @param playerId
	 * @param count */
	public int getXZQPlayerReceiveCount(int termId, int pointId, String playerId, int giftId) {
		String key1 = this.getXZQGiftPlayerReceiveCountKey(termId, pointId,giftId);
		String key2 = playerId;
		String count =  LocalRedis.getInstance().getRedisSession().hGet(key1, key2);
		if (HawkOSOperator.isEmptyString(count)) {
			return 0;
		}
		return Integer.parseInt(count);
	}
	
	/**
	 * 获取小战区礼包接收次数
	 * @param termId
	 * @param pointId
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public Map<String, String> getAllXZQPlayerReceiveCount(int termId, int pointId, int giftId) {
		String key1 = this.getXZQGiftPlayerReceiveCountKey(termId, pointId,giftId);
		Map<String, String> receiveMap =  LocalRedis.getInstance().getRedisSession().hGetAll(key1);
		if (receiveMap == null) {
			return new HashMap<>();
		}
		return receiveMap;
	}
	
	/** 更新小战区礼包接收次数
	 * 
	 * @param pointId
	 * @param playerId
	 * @param count */
	public void updateXZQPlayerReceiveCount(int termId, int pointId, String playerId, int giftId, int count) {
		String key1 = this.getXZQGiftPlayerReceiveCountKey(termId, pointId,giftId);
		String key2 = playerId;
		LocalRedis.getInstance().getRedisSession().hSet(key1, key2, String.valueOf(count));
	}
	
	
	
	/** 
	 * 获取小战区礼包发送记录数据
	 * @param termId
	 * @param pointId
	 * @param guildId
	 * @return
	 */
	public List<String> getAllXZQGiftSend(int termId, String guildId) {
		String key = this.getXZQGiftSendKey(termId, guildId);
		return LocalRedis.getInstance().getRedisSession().lRange(key, 0, -1, 0);
	}

	/** 小战区礼包发送记录数据更新
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public void addXZQGiftSend(int termId, String guildId,String dataJson) {
		String key = this.getXZQGiftSendKey(termId, guildId);
		LocalRedis.getInstance().getRedisSession().lPush(key, (int)TimeUnit.DAYS.toSeconds(30), dataJson);
	}
	
	
}
