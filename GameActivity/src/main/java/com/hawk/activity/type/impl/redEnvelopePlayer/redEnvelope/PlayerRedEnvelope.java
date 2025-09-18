package com.hawk.activity.type.impl.redEnvelopePlayer.redEnvelope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.redEnvelope.base.BaseRedEnvelope;
import com.hawk.activity.type.impl.redEnvelope.base.OnceRedEnvelope;
import com.hawk.activity.type.impl.redEnvelope.callback.RecieveCallBack;
import com.hawk.activity.type.impl.redEnvelopePlayer.cfg.RedEnvelopePlayerDetailsCfg;
import com.hawk.activity.type.impl.redEnvelopePlayer.cfg.RedEnvelopePlayerKVCfg;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.RedEnvelopeOnceDetail;
import com.hawk.game.protocol.Activity.RedEnvelopePlayerRecieveDetails;
import com.hawk.game.protocol.Activity.RedEnvelopeSenderInfo;
import com.hawk.game.protocol.Activity.RedEnvelopeState;

/***
 * 玩家红包
 * @author yang.rao
 *
 */
public class PlayerRedEnvelope extends BaseRedEnvelope {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/** 个人发出来的红包过期时间 **/
	private static final int PLAYER_RED_ENVELOPE_EXPIRE = (2 * 86400);
	
	/** 发红包的人 **/
	private String playerId;
	
	/** 如果是发的联盟红包，记录一下发红包的时刻，所在的盟（防止发完了红包退盟了） **/
	private String guildId;
	
	/** 发红包的时间  **/
	private long time;
	
	/** 个人红包的配置id **/
	private int cfgId;
	
	/** 红包类型(0.世界聊天红包  1.联盟聊天红包 ) **/
	private int type = -1;
	
	/** 红包玩家的祝福语 **/
	private String chatInfo;

	public PlayerRedEnvelope(){
		super();
	}
	
	public PlayerRedEnvelope(String id, int count){
		super(id, count);
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getChatInfo() {
		return chatInfo;
	}

	public void setChatInfo(String chatInfo) {
		this.chatInfo = chatInfo;
	}

	@Override
	public void recieve(String playerId, RecieveCallBack callback) {
		//判断红包是否过期
		if(hasExpire()){
			logger.info("RedEnvelopePlayer recieve error, can't recieve expire red_envelope, playerId:{}, redId:{}", playerId, getId());
			callback.call(Status.Error.RED_ENVELOPE_PLAYER_EXPIRE_VALUE, null);
			return;
		}
		//判断是否领取过该红包
		if(hasRecieved(playerId)){
			callback.call(Status.Error.RED_ENVELOPE_ALREADY_RECIEVE_VALUE, null);
			return;
		}
		//判断红包是否被领取完
		if(hasRecievedOver()){
			callback.call(Status.Error.RED_ENVELOPE_DELIVE_OVER_VALUE, null);
			return;
		}
		
		// 判断个人当日领取红包是否达到上限
		if (hasTouchLimit(playerId)) {
			callback.call(Status.Error.RED_ENVELOPE_TOUCH_LIMIT_VALUE, null);
			return;
		}
		
		for(OnceRedEnvelope oneRed : getSpiltList()){
			if(oneRed.getRecieveId() == null){
				//领到了这个红包
				addReciever(playerId);
				oneRed.setRecieveId(playerId);
				save2Redis(null);
				updateReceiveCount(playerId);
				callback.call(0, oneRed.getRewards());
				return;
			}
		}
	}

	@Override
	public void splitBag() {
		RedEnvelopePlayerDetailsCfg config = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopePlayerDetailsCfg.class, cfgId);
		if(config == null){
			logger.error("PlayerRedEnvelope split bag bug error, can't find config:{}", cfgId);
			return;
		}
		List<String> reward = config.getRewards();
		Collections.shuffle(reward);
		for(String re : reward){
			List<String> onceList = new ArrayList<>();
			onceList.add(re);
			OnceRedEnvelope onceRed = OnceRedEnvelope.valueOf(null, onceList);
			addOnceRedEnvelope(onceRed);
		}
	}

	@Override
	public RedEnvelopeState getPlayerState(String playerId) {
		return null;
	}

	@Override
	public OnceRedEnvelope getMyRecieveDetail(String playerId) {
		return null;
	}

	@Override
	public List<OnceRedEnvelope> getRecieveDetails() {
		return null;
	}
	
	public void buildRecieveDetails(RedEnvelopePlayerRecieveDetails.Builder build){
		for(OnceRedEnvelope oneRed : getSpiltList()){
			if(oneRed.getRecieveId() != null){
				String recieveId = oneRed.getRecieveId();
				RedEnvelopeOnceDetail.Builder once = RedEnvelopeOnceDetail.newBuilder();
				String playerName = ActivityManager.getInstance().getDataGeter().getPlayerName(recieveId);
				String pfIcon = ActivityManager.getInstance().getDataGeter().getPfIcon(recieveId);
				int icon = ActivityManager.getInstance().getDataGeter().getIcon(recieveId);
				if(!HawkOSOperator.isEmptyString(pfIcon)){
					once.setPfIcon(pfIcon);
				}
				if(!HawkOSOperator.isEmptyString(playerName)){
					once.setPlayerName(playerName);
				}
				once.setIcon(icon);
				once.setReward(oneRed.getItemCnt());
				once.setPlayerId(recieveId);
				once.setRewardItem(oneRed.getRewardItem());
				build.addDetail(once);
			}
		}
	}
	
	public void buildSenderInfo(RedEnvelopePlayerRecieveDetails.Builder build){
		RedEnvelopeSenderInfo.Builder info = RedEnvelopeSenderInfo.newBuilder();
		ActivityDataProxy proxy = ActivityManager.getInstance().getDataGeter();
		info.setPfIcon(proxy.getPfIcon(playerId));
		info.setName(proxy.getPlayerName(playerId));
		info.setMsg(chatInfo);
		info.setIcon(proxy.getIcon(playerId));
		build.setSender(info);
	}

	/***
	 * 个人红包不要key
	 */
	@Override
	public void save2Redis(String key) {
		String redisKey = String.format(ActivityRedisKey.RED_ENVELIPE_PLAYER, this.getId());
		ActivityLocalRedis.getInstance().set(redisKey, JsonUtils.Object2Json(this), PLAYER_RED_ENVELOPE_EXPIRE);
	}

	/** 是否过期 **/
	public boolean hasExpire(){
		long curTime = HawkTime.getMillisecond();
		RedEnvelopePlayerKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedEnvelopePlayerKVCfg.class);
		if(config == null){
			return true;
		}
		long consistTime = config.getOverdueTime();
		if(curTime - time > consistTime){
			return true;
		}
		return false;
	}
	
	public String getMaxCntPlayer(){
		int cnt = 0;
		String name = null;
		for(OnceRedEnvelope red : getSpiltList()){
			int itemCnt = red.getItemCnt();
			if(itemCnt > cnt){
				cnt = itemCnt;
				if(red.getRecieveId() != null){
					name = ActivityManager.getInstance().getDataGeter().getPlayerName(red.getRecieveId());
				}
			}
		}
		return name;
	}
	
	/**
	 *  判断玩家当日领取该类红包数量是否达上限
	 * @param playerId
	 * @return
	 */
	public boolean hasTouchLimit(String playerId) {
		RedEnvelopePlayerDetailsCfg config = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopePlayerDetailsCfg.class, cfgId);
		if (config.getReceivelimit() <= 0) {
			return false;
		}
		String dayTime = HawkTime.formatNowTime(HawkTime.FORMAT_YMD);
		String redisKey = String.format(ActivityRedisKey.RED_ENVELIPE_PLAYER_RECIEVE, playerId, dayTime, this.getCfgId());
		String count = ActivityLocalRedis.getInstance().get(redisKey);
		if (HawkOSOperator.isEmptyString(count) || Integer.parseInt(count) < config.getReceivelimit()) {
			return false;
		}
		return true;
	}
	
	public void updateReceiveCount(String playerId) {
		RedEnvelopePlayerDetailsCfg config = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopePlayerDetailsCfg.class, cfgId);
		if (config.getReceivelimit() <= 0) {
			return;
		}
		
		String dayTime = HawkTime.formatNowTime(HawkTime.FORMAT_YMD);
		String redisKey = String.format(ActivityRedisKey.RED_ENVELIPE_PLAYER_RECIEVE, playerId, dayTime, this.getCfgId());
		ActivityLocalRedis.getInstance().getRedisSession().increaseBy(redisKey, 1, 86400);
	}
}
