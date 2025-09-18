package com.hawk.game.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.data.ScoreBatchInfo;
import com.hawk.game.player.Player;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.sdk.SDKManager;

public class QQScoreBatch {
	/**
	 * 等待任务数量
	 */
	AtomicInteger waitCount = null;
	/**
	 * 任务执行线程池
	 */
	HawkThreadPool threadPool = null;
	
	/**
	 * 全局实例对象
	 */
	private static QQScoreBatch instance = null;
	public static QQScoreBatch getInstance() {
		if (instance == null) {
			instance = new QQScoreBatch();
		}
		return instance;
	}
	
	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		if (threadPool == null) {
			waitCount = new AtomicInteger();
			threadPool = new HawkThreadPool("QQScoreBatch");
			threadPool.initPool(GsConfig.getInstance().getExtraThreads()/2, true);
			threadPool.start();
		}
		
		return true;
	}

	/**
	 * 关闭
	 */
	public void close() {
		if (threadPool != null) {
			threadPool.close(true);
			threadPool = null;
		}
	}
	
	/**
	 * 上报成就积分信息
	 * 
	 * @param scoreType 成就积分类型
	 * @param value  成就值
	 * @param bcover 与排行榜有关的数据bcover=0，其他bcover=1。游戏中心排行榜与游戏排行榜保持一致;
	 * @param expires   unix时间戳，单位s，表示哪个时间点数据过期，0时标识永不超时
	 * 
	 */
	public void scoreBatch(Player player, ScoreType scoreType, Object value, int bcover, String expires) {
		try {
			if (threadPool == null || !GameUtil.isScoreBatchEnable(player)) {
				return;
			}
			
			if (waitCount.get() >= GameConstCfg.getInstance().getScoreBatchMaxWait()) {
				HawkLog.errPrintln("qqscore batch cache task count overflow");
				return;
			}
			
			threadPool.addTask(new HawkTask() {
				@Override
				public Object run() {
					waitCount.decrementAndGet();
					try {
						JSONArray scoreBatchData = getScoreBatchData(player);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("type", scoreType.intValue());
						jsonObject.put("data", String.valueOf(value));
						jsonObject.put("bcover", bcover);
						jsonObject.put("expires", expires);
						scoreBatchData.add(jsonObject);
						
						HawkLog.debugPrintln("score batch single, playerId: {}, scoreType: {}, data: {}", 
								player.getId(), scoreType.intValue(), scoreBatchData.toJSONString());
						
						SDKManager.getInstance().qqScoreBatch(player.getChannel(), (int)HawkApp.getInstance().getCurrentTime()/1000, 
								player.getOpenId(), player.getAccessToken(), scoreBatchData);
						
					} catch (Exception e) {
						HawkException.catchException(e);
					}
					return null;
				}
			}.setMustRun(false));
			
			waitCount.incrementAndGet();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 上报成就积分信息: 批量上报成就项
	 * @param scoreBatchObjs
	 */
	public void scoreBatch(Player player, ScoreBatchInfo... scoreBatchObjs) {
		try {
			if (threadPool == null || !GameUtil.isScoreBatchEnable(player)) {
				return;
			}
			
			if (waitCount.get() >= GameConstCfg.getInstance().getScoreBatchMaxWait()) {
				HawkLog.errPrintln("qqscore batch cache task count overflow");
				return;
			}
			
			threadPool.addTask(new HawkTask() {
				@Override
				public Object run() {
					waitCount.decrementAndGet();
					try {
						JSONArray scoreBatchData = getScoreBatchData(player);
						for (ScoreBatchInfo obj : scoreBatchObjs) {
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("type", obj.getScoreType().intValue());
							jsonObject.put("data", String.valueOf(obj.getValue()));
							jsonObject.put("bcover", obj.getBcover());
							jsonObject.put("expires", obj.getExpires());
							scoreBatchData.add(jsonObject);
						}
						
						HawkLog.debugPrintln("score batch, playerId: {}, data: {}", player.getId(), scoreBatchData.toJSONString());
						
						SDKManager.getInstance().qqScoreBatch(player.getChannel(), (int)HawkApp.getInstance().getCurrentTime()/1000, 
								player.getOpenId(), player.getAccessToken(), scoreBatchData);
						
					} catch (Exception e) {
						HawkException.catchException(e);
					}
					return null;
				}
			}.setMustRun(false));
			
			waitCount.incrementAndGet();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
    
	/**
	 * 获取等待任务数
	 * 
	 * @return
	 */
	public long getWaitCount() {
		if (threadPool != null) {
			return threadPool.getWaitTaskCount();
		}
		return 0;
	}
	
    /**
     * 获取积分上报基础公共字段信息
     * 
     * @param player
     * @return
     */
    public JSONArray getScoreBatchData(Player player) {
    	JSONArray jsonArray = getScoreBatchData(player.getPlatId(), GsConfig.getInstance().getAreaId(), player.getServerId(), player.getId(), player.getName());
    	String guildId = player.getGuildId();
    	if (!HawkOSOperator.isEmptyString(guildId)) {
    		JSONObject jsonObject = new JSONObject();
    		ScoreType scoreType = ScoreType.GUILD_ID;
    		jsonObject.put("type", scoreType.intValue());
    		jsonObject.put("data", guildId);
    		jsonObject.put("bcover", scoreType.bcoverVal());
    		jsonObject.put("expires", scoreType.expiresVal());
    		jsonArray.add(jsonObject);
    	}
    	return jsonArray;
    }
    
    /**
     * 获取积分上报基础公共字段信息
     * 
     * @param object
     * @return
     */
    private static JSONArray getScoreBatchData(Object... object) {
    	JSONArray jsonArray = new JSONArray();
    	int index = 0;
    	for (ScoreType scoreType : GsConst.COMMON_SCORE_TYPE) {
    		JSONObject jsonObject = new JSONObject();
    		jsonObject.put("type", scoreType.intValue());
    		jsonObject.put("data", String.valueOf(object[index++]));
    		jsonObject.put("bcover", scoreType.bcoverVal());
    		jsonObject.put("expires", scoreType.expiresVal());
    		jsonArray.add(jsonObject);
    		if (index == object.length) {
    			break;
    		}
    	}
    	return jsonArray;
    }
}
