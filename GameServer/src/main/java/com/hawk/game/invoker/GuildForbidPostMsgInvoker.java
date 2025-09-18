package com.hawk.game.invoker;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.protocol.GuildManager.GuildBBSMessage;


/**
 * 屏蔽玩家留言
 * 
 * @author Jesse
 *
 */
public class GuildForbidPostMsgInvoker extends HawkMsgInvoker {
	/** 屏蔽玩家id */
	private String playerId;

	/** 联盟名称 */
	private String guildId;

	/** 协议Id */
	private int hpCode;

	public GuildForbidPostMsgInvoker(String playerId, String guildId, int hpCode) {
		this.playerId = playerId;
		this.guildId = guildId;
		this.hpCode = hpCode;
	}

	public String getPlayerId() {
		return playerId;
	}

	public String getGuildId() {
		return guildId;
	}

	public int getHpCode() {
		return hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		LocalRedis.getInstance().addForbidPostPlayer(guildId, playerId);
		List<byte[]> remList = new ArrayList<>();
		try {
			for (byte[] message : LocalRedis.getInstance().getGuildBBS(guildId)) {
				GuildBBSMessage.Builder messageBuilder = GuildBBSMessage.newBuilder().mergeFrom(message);
				if (messageBuilder.getPlayerId().equals(playerId)) {
					remList.add(message);
				}
			}
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
			e.printStackTrace();
		}
		if (!remList.isEmpty()) {
			byte[][] remArr = remList.toArray(new byte[remList.size()][]);
			LocalRedis.getInstance().delGuildBBS(guildId, remArr);
		}

		return false;
	}
	
}
