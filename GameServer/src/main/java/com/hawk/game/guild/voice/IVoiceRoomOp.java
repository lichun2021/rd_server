package com.hawk.game.guild.voice;

import java.util.Collection;

public interface IVoiceRoomOp {

	boolean joinRoom( String playerId, String roomId );
	
	boolean quitRoom(String playerId, String roomId);
	
	int getRoomMemberCount(String roomId);
	
	boolean isExistRoomMember(String playerId, String roomId);
	
	Collection<?> getRoomMembers(String roomId);

}
