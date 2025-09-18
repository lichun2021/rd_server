package com.hawk.game.crossactivity.season;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.crossactivity.season.state.CrossSeasonStateHidden;
import com.hawk.game.crossactivity.season.state.ICrossSeasonState;
import com.hawk.game.global.RedisProxy;

public class CrossSeaonStateData {
	
	public int season = 0;

	public ICrossSeasonState state = new CrossSeasonStateHidden();
	
	
	
	public void onTick(){
		state.update();
	}
	
	/**
	 * 变更状态
	 * @param toState
	 */
	public void changeState(CrossSeasonStateEnum toState){
		this.state = toState.createSeasonState();
		try {
			this.state.start();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		//保存数据
		this.saveData();
		//同步状态
		CrossActivitySeasonService.getInstance().syncSeasonDataOnlinePlayer();
		HawkLog.logPrintln("CrossActivitySeasonService-CrossSeaonStateData-change:{},{},{}",
				this.season,this.state.getStateEnum().name(),GsConfig.getInstance().getServerId());
		
	}
	
	
	
	
	
	/**
	 * 加载数据
	 */
	public void loadData(){
		String serverId = GsConfig.getInstance().getServerId();
		String value = RedisProxy.getInstance().getRedisSession().hGet(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_STATE,serverId);
        if(HawkOSOperator.isEmptyString(value)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(value);
        season = obj.getIntValue("season");
        this.state = CrossSeasonStateEnum.seasonStateOf(obj.getIntValue("state")).createSeasonState();
	}
	
	
	/**
	 * 保存数据
	 */
	public void saveData(){
        String serverId = GsConfig.getInstance().getServerId();
        JSONObject obj = new JSONObject();
        obj.put("season", this.season);
        obj.put("state", this.state.getStateEnum().getNum());
        RedisProxy.getInstance().getRedisSession().hSet(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_STATE, serverId, obj.toJSONString());
	}
	
	
	
	
	

	public int getSeason() {
		return season;
	}
	
	public void setSeason(int season) {
		this.season = season;
	}

	public ICrossSeasonState getState() {
		return state;
	}

	
	

}
