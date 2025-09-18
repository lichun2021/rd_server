package com.hawk.game.module.obelisk.service.mission.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Obelisk.PBObeliskMissionState;
import com.hawk.game.protocol.Rank;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.util.LogUtil;
import com.hawk.serialize.string.SerializeHelper;

/***
 * 配置任务, 包含完成和未完成的. 一条配置对应一个
 * @author hf
 */
public class ObeliskMissionItem implements SerializJsonStrAble {
	/**任务id 和表一一对应 */
	private int cfgId;
	/** 完成数量 */
	private int num;
	/** 状态 0 未开始 1已开始 2已结束*/
	private PBObeliskMissionState state;
	/** 任务开始时间 */
	private long startTime;
	/** 任务完成时间 */
	private long endTime;
	/** 联盟任务数据 key 联盟id, value 联盟完成数量 */
	private Map<String, Integer> guildMap = new HashMap<>();
	/** 排行榜数据 */
	private Rank.HPPushRank rankInfo;

	private JSONObject extryData = new JSONObject();

	private boolean changed;

	public ObeliskMissionItem() {
	}

	public ObeliskMissionItem(int cfgId) {
		this.cfgId = cfgId;
		this.state = PBObeliskMissionState.NOTOPEN;
		this.num = 0;
	}

	public static final String GUILD_MAP = "guildMap";
	public static final String RANK_INFO = "rankInfo";

	public ObeliskCfg getObeliskCfg() {
		ObeliskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ObeliskCfg.class, cfgId);
		if (cfg == null) {
			HawkLog.errPrintln("ObeliskMissionItem getObeliskCfg ObeliskCfg = null, cfgId:{}", cfgId);
		}
		return cfg;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public PBObeliskMissionState getState() {
		return state;
	}

	public void setState(PBObeliskMissionState state) {
		this.state = state;
		LogUtil.logObeliskState(this);
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void addValue(int value) {
		this.num += value;
		setChanged(true);
	}

	public Map<String, Integer> getGuildMap() {
		return guildMap;
	}

	public void setGuildMap(Map<String, Integer> guildMap) {
		this.guildMap = guildMap;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/** 联盟的数据增加 */
	public void addGuildValue(String guildId, int value) {
		int beforeValue = guildMap.getOrDefault(guildId, 0);
		guildMap.put(guildId, value + beforeValue);
		setChanged(true);
	}
	
	/** 联盟的数据增加 */
	public void setGuildValue(String guildId, int value) {
		guildMap.put(guildId, value);
		setChanged(true);
	}

	/** 联盟的数据查询 */
	public int getGuildValue(String guildId) {
		return guildMap.getOrDefault(guildId, 0);
	}

	public Rank.HPPushRank getRankInfo() {
		return rankInfo;
	}

	public void setRankInfo(Rank.HPPushRank rankInfo) {
		this.rankInfo = rankInfo;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("cfgId", cfgId);
		obj.put("num", num);
		obj.put("state", state.getNumber());
		obj.put("startTime", startTime);
		obj.put("endTime", endTime);
		if (!guildMap.isEmpty()) {
			obj.put(GUILD_MAP, SerializeHelper.mapToString(guildMap));
		}
		if (rankInfo != null) {
			JSONArray jsonArray = new JSONArray();
			for (Rank.RankInfo pbRankInfo : rankInfo.getRankInfoList()) {
				jsonArray.add(JsonFormat.printToString(pbRankInfo));
			}
			obj.put(RANK_INFO, jsonArray);
			obj.put("rankType", rankInfo.getRankType().getNumber());
		}
		if (Objects.nonNull(extryData)) {
			obj.put("extryData", extryData);
		}
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.cfgId = obj.getIntValue("cfgId");
		this.num = obj.getIntValue("num");
		this.state = PBObeliskMissionState.valueOf(obj.getIntValue("state"));
		this.startTime = obj.getLongValue("startTime");
		this.endTime = obj.getLongValue("endTime");
		if (obj.containsKey(GUILD_MAP)) {
			this.guildMap = SerializeHelper.stringToMap(obj.getString(GUILD_MAP), String.class, Integer.class);
		}
		try {
			if (obj.containsKey(RANK_INFO)) {
				RankType type = RankType.valueOf(obj.getIntValue("rankType"));
				if (getObeliskCfg().getTaskType() == ObeliskMissionType.GUIlD_POWER_RANK.intValue()) { // 这里写死是因为原做者没有测试. 序列化时丢失信息. 并且 这里rankInfo 为null 跟本不会反序列化成功
					type = RankType.ALLIANCE_FIGHT_KEY;
				} else if (getObeliskCfg().getTaskType() == ObeliskMissionType.PLAYER_POWER_RANK.intValue()) {
					type = RankType.PLAYER_FIGHT_RANK;
				}
				Rank.HPPushRank.Builder rankBuilder = Rank.HPPushRank.newBuilder().setMyRank(0).setMyRankScore(0).setRankType(type);
				JSONArray jsonArray = obj.getJSONArray(RANK_INFO);
				for (Object object : jsonArray) {
					Rank.RankInfo.Builder rankPb = Rank.RankInfo.newBuilder();
					JsonFormat.merge(object.toString(), rankPb);
					rankBuilder.addRankInfo(rankPb);
				}
				this.rankInfo = rankBuilder.build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.extryData = obj.getJSONObject("extryData");
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void putExtryData(String key, String value) {
		if (Objects.isNull(extryData)) {
			extryData = new JSONObject();
		}
		extryData.put(key, value);
	}

	public String getExtryData(String key) {
		if (Objects.isNull(extryData)) {
			return "";
		}
		return extryData.getString(key);
	}

}
