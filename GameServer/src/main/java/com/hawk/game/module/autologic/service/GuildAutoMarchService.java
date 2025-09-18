package com.hawk.game.module.autologic.service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.module.autologic.PlayerAutoMassJoinModule;
import com.hawk.game.module.autologic.data.GuildAutoMarchData;
import com.hawk.game.module.autologic.data.GuildAutoMarchQueueMember;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst;

public class GuildAutoMarchService extends HawkAppObj {
	
	
	private Map<String,GuildAutoMarchData> autoMassJoin = new ConcurrentHashMap<>();
	private long lastTime;
	
	
	private static GuildAutoMarchService instance;
	public static GuildAutoMarchService getInstance() {
		return instance;
	}

	public GuildAutoMarchService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	public boolean init(){
		return true;
	}
	

	@Override
	public boolean onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime - this.lastTime < 5000){
			return true;
		}
		this.lastTime = curTime;
		for(GuildAutoMarchData data : autoMassJoin.values()){
			data.onTick();
		}
		return true;
	}
	
	
	/**
	 * 获取在队列中的信息
	 * @param player
	 * @return
	 */
	public GuildAutoMarchQueueMember getGuildAutoMarchQueueMember(Player player){
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return null;
		}
		GuildAutoMarchData data = autoMassJoin.get(guildId);
		if(Objects.isNull(data)){
			return null;
		}
		return data.getGuildAutoMarchQueueMember(player.getId());
	}
	


	/**
	 * 添加到自动集结
	 * @param player
	 */
	public void addGuildAutoMarchQueueMember(Player player){
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		if(!player.isActiveOnline()){
			return;
		}
		if(!autoMassJoin.containsKey(guildId)){
			GuildAutoMarchData data = new GuildAutoMarchData(guildId);
			autoMassJoin.putIfAbsent(guildId, data);
		}
		GuildAutoMarchData data = autoMassJoin.get(guildId);
		data.addGuildAutoMarchMember(player.getId());
	}
	
	
	/**
	 * 移出自动集结
	 * @param player
	 */
	public void removeGuildAutoMarchQueueMember(Player player){
		String guildId = player.getGuildId();
		GuildAutoMarchData data = autoMassJoin.get(guildId);
		if(Objects.isNull(data)){
			return;
		}
		data.removeGuildAutoMarchMember(player.getId());
	}
	
	/**
	 * 是否已经在队列中
	 * @return
	 */
	public int getGuildAutoQueueOrder(Player player){
		GuildAutoMarchData guildData = this.autoMassJoin.get(player.getGuildId());
		if(Objects.isNull(guildData)){
			return 0;
		}
		return guildData.getAutoQueueOrder(player.getId());
	}
	
	/**
	 * 加入集结
	 * @param player
	 */
	public void onAutoMassJoin(Player player){
		if(player.isCsPlayer()){
			return;
		}
		GuildAutoMarchData data = autoMassJoin.get(player.getGuildId());
		if(Objects.isNull(data)){
			return;
		}
		data.onMassJoin(player.getId());
	}
	
	/**
	 * 发送玩家错误邮件
	 * @param player
	 * @param mailId
	 * @param marchType
	 * @param param
	 */
	public void addMissMail(Player player,int reason,String... param){
		PlayerAutoMassJoinModule module = player.getModule(GsConst.ModuleType.AUTO_MASS_JOIN);
		module.addMissMail(reason,param);
	}

}
