package com.hawk.game.lianmengxzq;

import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class XZQForceColor  implements SerializJsonStrAble {

	
	private String guildId;
	
	private int colorId;
	
	private long colorTime;

	
	public boolean inCDTime(){
		long curTime = HawkTime.getMillisecond();
		long cdEndTime = this.getCDEndTime();
		if(curTime  < cdEndTime){
			return true;
		}
		return false;
	}
	
	public long getCDEndTime(){
		return this.colorTime + XZQConstCfg.getInstance().getColorResetTime() * 1000;
	}
	
	
	
	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getColorId() {
		return colorId;
	}

	public void setColorId(int colorId) {
		this.colorId = colorId;
	}

	public long getColorTime() {
		return colorTime;
	}

	public void setColorTime(long colorTime) {
		this.colorTime = colorTime;
	}

	@Override
	public String serializ() {
		JSONObject ser = new JSONObject();
		ser.put("1", this.guildId);
		ser.put("2", this.colorId);
		ser.put("3", this.colorTime);
		return ser.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject ser = JSONObject.parseObject(serialiedStr);
		if(ser.containsKey("1")){
			this.guildId = ser.getString("1");
		}
		if(ser.containsKey("2")){
			this.colorId = ser.getIntValue("2");
		}
		if(ser.containsKey("3")){
			this.colorTime = ser.getLongValue("3");
		}
	}
	
	
	
}
