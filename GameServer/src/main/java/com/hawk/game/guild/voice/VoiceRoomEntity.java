package com.hawk.game.guild.voice;

import java.util.HashSet;

import com.hawk.game.protocol.GuildManager.VoiceRoomModel;

public class VoiceRoomEntity {
	
	private HashSet<String> members = new HashSet<String>();
	
	private VoiceRoomModel voiceModel = VoiceRoomModel.LIBERTY;

	public HashSet<String> getMembers() {
		return members;
	}

	public void setMembers(HashSet<String> members) {
		this.members = members;
	}

	public VoiceRoomModel getVoiceModel() {
		return voiceModel;
	}

	public void setVoiceModel(VoiceRoomModel voiceModel) {
		this.voiceModel = voiceModel;
	}
	
}
