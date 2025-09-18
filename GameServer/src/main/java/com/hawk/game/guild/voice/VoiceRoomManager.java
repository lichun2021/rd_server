package com.hawk.game.guild.voice;

import java.util.Collection;
import java.util.HashMap;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.GuildVoiceRoomInfoNtfResp;
import com.hawk.game.protocol.GuildManager.VoiceRoomModel;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;

public class VoiceRoomManager implements IVoiceRoomOp {

	static private class Singletion {
		static VoiceRoomManager instance = new VoiceRoomManager();
	}

	public VoiceRoomManager() {
	}

	public static VoiceRoomManager getInstance() {
		return Singletion.instance;
	}

	HashMap<String, VoiceRoomEntity> roomAll = new HashMap<String, VoiceRoomEntity>();

	public boolean onPlayerJoin(String playerId) {
		String roomId = getPlayerVoiceRoomId(playerId);
		if (!HawkOSOperator.isEmptyString(roomId)) {
			return joinRoom(playerId, roomId);
		}
		return false;
	}

	public boolean onPlayerQuit(String playerId) {
		String roomId = getPlayerVoiceRoomId(playerId);
		if (!HawkOSOperator.isEmptyString(roomId)) {
			return quitRoom(playerId, roomId);
		}
		return false;
	}

	@Override
	public boolean joinRoom(String playerId, String roomId) {
		if (!HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(roomId)) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(null == player){
				return false;
			}
			synchronized (Singletion.instance) {
				VoiceRoomEntity entity = roomAll.get(roomId);
				if (null == entity ) {
					entity = new VoiceRoomEntity();
					roomAll.put(roomId, entity);
				}
				entity.getMembers().add(playerId);
			}
			String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			if(!HawkOSOperator.isEmptyString(guildId)){
				LogUtil.logVoiceRoom(player, guildId, 1);
				notifyWithRoomId(guildId, roomId);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean quitRoom(String playerId, String roomId) {
		if (!HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(roomId)) {
			boolean isQuitOk = false;
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(null == player){
				return false;
			}
			String guildId =  GuildService.getInstance().getPlayerGuildId(playerId);
			synchronized (Singletion.instance) {
				VoiceRoomEntity entity = roomAll.get(roomId);
				if (null != entity) {
					entity.getMembers().remove(playerId);
					isQuitOk = true;

				}
			}
			if (isQuitOk) {
				if(!HawkOSOperator.isEmptyString(guildId)){
					LogUtil.logVoiceRoom(player, guildId, 2 );
					notifyWithRoomId(guildId, roomId);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int getRoomMemberCount(String roomId) {
		if (!HawkOSOperator.isEmptyString(roomId)) {
			VoiceRoomEntity entity = roomAll.get(roomId);
			if(null != entity){
				return entity.getMembers().size();
			}
		}
		return 0;
	}

	@Override
	public boolean isExistRoomMember(String playerId, String roomId) {
		if (!HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(roomId)) {
			VoiceRoomEntity entity = roomAll.get(roomId);
			if(null != entity){
				return entity.getMembers().contains(playerId);
			}
		}
		return false;
	}

	@Override
	public Collection<?> getRoomMembers(String roomId) {
		if (!HawkOSOperator.isEmptyString(roomId)) {
			VoiceRoomEntity entity = roomAll.get(roomId);
			if(null != entity){
				return entity.getMembers();
			}
		}
		return null;
	}

	/**
	 * 聊天室模式变更
	 * 
	 * @param player
	 * @param model
	 */
	public void onChangeRoomType(Player player, VoiceRoomModel model) {
		String roomId = getPlayerVoiceRoomId(player.getId());
		synchronized(Singletion.instance){
			VoiceRoomEntity entity = roomAll.get(roomId);
			if(null != entity){
				entity.setVoiceModel(model);
			}
		}
		notifyWithRoomId(player.getGuildId(), roomId);
	}

	private void notifyWithRoomId(String guildId, String roomId) {
		if (!HawkOSOperator.isEmptyString(roomId)) {
			VoiceRoomEntity entity = roomAll.get(roomId);
			if(null == entity){
				return;
			}
			GuildVoiceRoomInfoNtfResp.Builder builder = GuildVoiceRoomInfoNtfResp.newBuilder();
			builder.setMemberCount(entity.getMembers().size());
			builder.setModel(entity.getVoiceModel());
			for (Object memberPlayerId : entity.getMembers()) {
				Player player = GlobalData.getInstance().makesurePlayer((String) memberPlayerId);
				if (null != player) {
					player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_VOICE_ROOM_NOTIFY_S, builder));
				}
			}
		}
	}

	private String getPlayerVoiceRoomId(String playerId) {
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			return guildId + ":" + GsConfig.getInstance().getServerId();
		}
		return null;
	}
}
