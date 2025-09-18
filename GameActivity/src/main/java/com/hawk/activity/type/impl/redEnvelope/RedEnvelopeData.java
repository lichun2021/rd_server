package com.hawk.activity.type.impl.redEnvelope;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.redEnvelope.base.BaseRedEnvelope;
import com.hawk.activity.type.impl.redEnvelope.base.CurRedEnvelopeState;
import com.hawk.activity.type.impl.redEnvelope.base.OnceRedEnvelope;
import com.hawk.activity.type.impl.redEnvelope.base.SystemRedEnvelope;
import com.hawk.activity.type.impl.redEnvelope.callback.RecieveCallBack;
import com.hawk.activity.type.impl.redEnvelope.cfg.RedEnvelopeAchieveCfg;
import com.hawk.game.protocol.Activity.RedEnvelopeState;
import com.hawk.game.protocol.Status;

/***
 * 红包数据
 * @author yang.rao
 *
 */
public class RedEnvelopeData {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/** 当前红包配置 **/
	private RedEnvelopeAchieveCfg curStage = null;
	
	/** 当前阶段红包 **/
	private BaseRedEnvelope redEnvelope;
	
	private CurRedEnvelopeState state;
	
	private int termId;
	
	public RedEnvelopeData(int termId){
		this.termId = termId;
	}

	public RedEnvelopeAchieveCfg getCurStage() {
		return curStage;
	}

	public void setCurStage(RedEnvelopeAchieveCfg curStage) {
		this.curStage = curStage;
	}

	public BaseRedEnvelope getRedEnvelope() {
		return redEnvelope;
	}

	public void setRedEnvelope(BaseRedEnvelope redEnvelope) {
		this.redEnvelope = redEnvelope;
	}

	public CurRedEnvelopeState getState() {
		return state;
	}

	public void setState(CurRedEnvelopeState state) {
		this.state = state;
	}

	/***
	 * 是否到了一个新的红包阶段
	 * @return
	 */
	public RedEnvelopeAchieveCfg hasNewRedEnvelope(long curTime){
		ConfigIterator<RedEnvelopeAchieveCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RedEnvelopeAchieveCfg.class);
		while(ite.hasNext()){
			RedEnvelopeAchieveCfg cfg = ite.next();
			if(cfg.getShow(termId) <= curTime && cfg.getEnd(termId) > curTime){ 
				if(cfg != curStage){
					return cfg;
				}
			}
		}
		return null;
	}
	
	public boolean checkState(long curTime){
		if(curStage == null){
			return false;
		}
		if(curStage.getStart(termId) <= curTime && curStage.getEnd(termId) > curTime && state == CurRedEnvelopeState.SHOW){
			state = CurRedEnvelopeState.START;
			return true;
		}
		return false;
	}
	
	public boolean hasPushStartTask(long curTime){
		if(curStage == null){
			return false;
		}
		ConfigIterator<RedEnvelopeAchieveCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RedEnvelopeAchieveCfg.class);
		while(ite.hasNext()){
			RedEnvelopeAchieveCfg cfg = ite.next();
			if(cfg.getStart(termId) <= curTime && cfg.getEnd(termId) > curTime){
				
			}
		}
		return false;
	}
	
	public void initRedEnvelope(){
		if(curStage == null){
			return;
		}
		//先从redis初始化红包，如果没有则手动构建
		String key = getKey();
		if(key == null){
			return;
		}
		String msg = ActivityLocalRedis.getInstance().get(key);
		if(msg != null){
			SystemRedEnvelope systemRedEnvelope = JsonUtils.String2Object(msg, SystemRedEnvelope.class);
			this.redEnvelope = systemRedEnvelope;
		}else{
			BaseRedEnvelope red = new SystemRedEnvelope(String.valueOf(curStage.getStageID()), curStage.getNum());
			red.splitBag();
			this.redEnvelope = red;
		}
	}
	
	private String getKey(){
		if(curStage == null){
			return null;
		}
		return String.format(ActivityRedisKey.RED_ENVELOPE, termId, curStage.getStageID());
	}
	
	public RedEnvelopeState getState(String playerId){
		if(curStage == null){
			return null;
		}
		if(isShow()){
			return RedEnvelopeState.ONT_START;
		}else if(isStart()){
			return redEnvelope.getPlayerState(playerId);
		}else{
			return RedEnvelopeState.ALREADY_OVER; //已经结束
		}
	}
	
	/***
	 * 抢红包
	 * @param playerId
	 */
	public void recivevRedEnvelope(String playerId, RecieveCallBack callback){
		if(!isStart()){
			callback.call(Status.Error.RED_ENVELOPE_NOT_START_VALUE, null);
			return;
		}
		redEnvelope.recieve(playerId, callback);
	}
	
	public OnceRedEnvelope getRecieveDetails(int stageID, String playerId){
		if(stageID != curStage.getStageID()){
			return null;
		}
		return redEnvelope.getMyRecieveDetail(playerId);
	}
	
	/***
	 * 保存红包到redis
	 */
	public void saveRedEnvelope(){
		String key = getKey();
		if(key == null){
			logger.error("save red envelope, but redis key is null.");
			return;
		}
		redEnvelope.save2Redis(key);
	}
	
	/***
	 * 添加一次记录
	 * @param once
	 */
	public void addOnceRedEnvelope(OnceRedEnvelope once){
		redEnvelope.addOnceRedEnvelope(once);
		redEnvelope.addReciever(once.getRecieveId());
	}
	
	private boolean isStart(){
		long curTime = HawkTime.getMillisecond();
		if(curStage == null){
			return false;
		}
		if(curStage.getStart(termId) <= curTime && curStage.getEnd(termId) > curTime){
			return true;
		}
		return false;
	}
	
	/***
	 * 粗略判断能否抢红包
	 * @param stageId
	 * @return
	 */
	public boolean canRecieve(int stageId){
		if(!isStart()){
			return false;
		}
		if(stageId != curStage.getStageID()){
			return false;
		}
		return true;
	}
	
	private boolean isShow(){
		long curTime = HawkTime.getMillisecond();
		if(curStage == null){
			return false;
		}
		if(curStage.getShow(termId) <= curTime && curStage.getStart(termId) > curTime){
			return true;
		}
		return false;
	}
}
