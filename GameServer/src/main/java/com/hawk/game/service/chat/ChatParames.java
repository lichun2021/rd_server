package com.hawk.game.service.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;

public class ChatParames {
	private ChatType chatType;
	private Const.NoticeCfgId key;
	private Player player;
	private List<String> parms;
	private String guildId;
	private String serverId;
	
	public ChatParames() {
	}

	public ChatMsg toPBMsg() {
		ChatType chatType = this.getChatType();
		Const.NoticeCfgId key = this.getKey();
		Player player = this.getPlayer();
		List<String> parms = this.getParms();
		String content = "";
		ChatMsg.Builder chatMsgInfo = ChatMsg.newBuilder();
		chatMsgInfo.setMsgId(HawkUUIDGenerator.genUUID());
		if (null != player) {
			chatMsgInfo = ChatService.getInstance().createMsgObj(player);
		}
		if(StringUtils.isNotEmpty(guildId)){
			chatMsgInfo.setAllianceId(guildId);
		}
		if(StringUtils.isNotEmpty(serverId)){
			chatMsgInfo.setServerId(serverId);
		}
		chatMsgInfo.setMsgTime(HawkTime.getMillisecond());
		chatMsgInfo.setType(chatType.getNumber());
		// 包含超链接
		if (Objects.nonNull(key)) {
			JSONArray array = new JSONArray();
			parms.forEach(array::add);
			content = array.toJSONString();
			chatMsgInfo.setNoticeId(key.getNumber());
		} else if (!parms.isEmpty()) {
			content = parms.get(0);
		}

		chatMsgInfo.setChatMsg(content);
		return chatMsgInfo.build();
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private ChatType chatType;
		private Const.NoticeCfgId key;
		private Player player;
		private List<String> parms;
		private String guildId;
		private String serverId;
		private Builder() {
			this.parms = new ArrayList<>();
		}

		public ChatParames build() {
			ChatParames result = new ChatParames();
			result.setChatType(chatType);
			result.setKey(key);
			result.setPlayer(player);
			result.setParms(parms);
			result.setGuildId(guildId);
			result.setServerId(serverId);
			return result;
		}

		public ChatType getChatType() {
			return chatType;
		}

		public Builder setChatType(ChatType chatType) {
			this.chatType = chatType;
			return this;
		}

		public Const.NoticeCfgId getKey() {
			return key;
		}

		public Builder setKey(Const.NoticeCfgId key) {
			this.key = key;
			return this;
		}

		public Player getPlayer() {
			return player;
		}

		public Builder setPlayer(Player player) {
			this.player = player;
			return this;
		}

		public List<String> getParms() {
			return parms;
		}

		public Builder setGuildId(String guildId) {
			this.guildId = guildId;
			return this;
		}
		
		public Builder setServerId(String serverId) {
			this.serverId = serverId;
			return this;
		}

		public Builder addParms(Object... parms) {
			for (Object p : parms) {
				String str = Objects.isNull(p) ? "" : Objects.toString(p);
				this.parms.add(str);
			}
			return this;
		}
	}

	public String getGuildId() {
		return guildId;
	}

	public ChatType getChatType() {
		return chatType;
	}

	public Const.NoticeCfgId getKey() {
		return key;
	}

	public Player getPlayer() {
		return player;
	}

	public List<String> getParms() {
		return parms;
	}

	public void setChatType(ChatType chatType) {
		this.chatType = chatType;
	}

	public void setKey(Const.NoticeCfgId key) {
		this.key = key;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setParms(List<String> parms) {
		this.parms = parms;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
}
