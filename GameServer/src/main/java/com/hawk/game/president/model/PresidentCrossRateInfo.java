package com.hawk.game.president.model;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.RedisProxy;

/**
 * 跨服盟总推进度条信息
 * @author Golden
 *
 */
public class PresidentCrossRateInfo {

	/**
	 * 进度值
	 */
	private long rate;
	
	/**
	 * 开始占领时间
	 */
	private long startOccupyTime;
	
	/**
	 * 占领速度
	 */
	private int speed;
	
	/**
	 * 是否是攻击方
	 */
	private boolean isAtker;
	
	/**
	 * 构造
	 * @param jsonInfo
	 */
	public PresidentCrossRateInfo(String jsonInfo) {
		if (HawkOSOperator.isEmptyString(jsonInfo)) {
			this.rate = CrossConstCfg.getInstance().getCrossAttackInit();
			return;
		}
		JSONObject json = JSONObject.parseObject(jsonInfo);
		this.rate = json.getLongValue("rate");
		this.startOccupyTime = json.getLongValue("startOccupyTime");
		this.speed = json.getIntValue("speed");
		this.isAtker = json.getBooleanValue("isAtker");
	}
	
	public long getRate() {
		return rate;
	}

	public void setRate(long rate) {
		this.rate = rate;
	}

	public long getStartOccupyTime() {
		return startOccupyTime;
	}

	public void setStartOccupyTime(long startOccupyTime) {
		this.startOccupyTime = startOccupyTime;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public boolean isAtker() {
		return isAtker;
	}

	public void setAtker(boolean isAtker) {
		this.isAtker = isAtker;
	}

	/**
	 * 序列化
	 * @return
	 */
	public String serialize() {
		JSONObject json = new JSONObject();
		json.put("rate", rate);
		json.put("startOccupyTime", startOccupyTime);
		json.put("speed", speed);
		json.put("isAtker", isAtker);
		return json.toJSONString();
	}
	
	/**
	 * 获取当前推条进度
	 */
	public long getCurrentRate() {
		if (startOccupyTime == 0) {
			return rate;
		}
		
		// 占领的时间
		long occupySecond = (HawkTime.getMillisecond() - startOccupyTime) / 1000;
		if (isAtker) {
			return rate +  occupySecond * speed;
		} else {
			return rate -  occupySecond * speed;
		}
	}
	
	/**
	 * 更换占领方
	 * @param speed
	 * @param isAtker
	 */
	public void changeOccupy(int speed, boolean isAtker) {
		this.rate = getCurrentRate();
		this.startOccupyTime = HawkTime.getMillisecond();
		this.speed = speed;
		this.isAtker = isAtker;
		
		String serverId = GsConfig.getInstance().getServerId();
		int termId = CrossActivityService.getInstance().getTermId();
		RedisProxy.getInstance().updateCrossRateInfo(serverId, termId, this);
	}
	
	/**
	 * 是否占领完成
	 * @return
	 */
	public boolean isOccupySuccess() {
		long currentRate = getCurrentRate();
		HawkLog.logPrintln("cross occupy rate, currentRate:{}", currentRate);
		if (currentRate <= 0 || currentRate >= CrossConstCfg.getInstance().getCrossProgressTotal()) {
			return true;
		}
		return false;
	}
}
