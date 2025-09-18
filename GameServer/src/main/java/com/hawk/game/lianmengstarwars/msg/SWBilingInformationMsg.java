package com.hawk.game.lianmengstarwars.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.lianmengstarwars.SWConst.SWOverType;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.SW.PBSWGameInfoSync;
import com.hawk.game.protocol.SW.PBSWGuildInfo;
import com.hawk.game.protocol.SW.PBSWPlayerInfo;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;
import org.hawk.tuple.HawkTuple3;

/***
 * 结算信息
 */
public class SWBilingInformationMsg extends HawkMsg implements SerializJsonStrAble {
	private SWOverType overType;
	private String winGuild;
	private String roomId;
	private SWWarType warType;
	private PBSWGameInfoSync lastSyncpb;

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("overType", overType);
		obj.put("winGuild", winGuild);
		obj.put("roomId", roomId);
		obj.put("warType", warType);
		obj.put("lastSyncpb", JsonFormat.printToString(lastSyncpb));
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		overType = SWOverType.valueOf(obj.getString("overType"));
		winGuild = obj.getString("winGuild");
		roomId = obj.getString("roomId");
		warType = SWWarType.valueOf(obj.getString("warType"));
		try {
			PBSWGameInfoSync.Builder lastSyncpbBul = PBSWGameInfoSync.newBuilder();
			JsonFormat.merge(obj.getString("lastSyncpb"), lastSyncpbBul);
			lastSyncpb = lastSyncpbBul.build();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	public SWOverType getOverType() {
		return overType;
	}

	public void setOverType(SWOverType overType) {
		this.overType = overType;
	}

	public String getWinGuild() {
		return winGuild;
	}

	public void setWinGuild(String winGuild) {
		this.winGuild = winGuild;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public SWWarType getWarType() {
		return warType;
	}

	public void setWarType(SWWarType warType) {
		this.warType = warType;
	}

	public PBSWGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBSWGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	/**
	 * 联盟总分
	 */
	public long getGuildHonor(String guildid) {
		List<PBSWGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBSWGuildInfo ginfo : guildList) {
			if (Objects.equal(guildid, ginfo.getGuildId())) {
				return ginfo.getHonor();
			}
		}
		return 0;
	}

	public HawkTuple3<Integer, Integer, Integer> getKillHonor(String playerId) {
		if (lastSyncpb == null) {
			return new HawkTuple3<>(0,0,0);
		}
		for (PBSWPlayerInfo pinfo : lastSyncpb.getPlayerInfoList()) {
			if (Objects.equal(pinfo.getPlayerId(), playerId)) {
				return new HawkTuple3<>(pinfo.getKillHonor(), pinfo.getKillPower(), pinfo.getDeadPower());
			}
		}
		return new HawkTuple3<>(0,0,0);
	}

}
