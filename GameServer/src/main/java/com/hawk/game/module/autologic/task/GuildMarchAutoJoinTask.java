package com.hawk.game.module.autologic.task;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.autologic.data.GuildAutoMarchQueueMember;
import com.hawk.game.module.autologic.data.PlayerAutoMarchEnum;
import com.hawk.game.module.autologic.data.PlayerAutoMarchParam;
import com.hawk.game.player.Player;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

public class GuildMarchAutoJoinTask  extends HawkTask{

	//联盟ID
	private String guildId;
	//出征列表
	private List<GuildAutoMarchQueueMember> members;
	//是否完成整体任务
	private boolean finish = false;
	
	public GuildMarchAutoJoinTask(String guildId,List<GuildAutoMarchQueueMember> member) {
		this.guildId = guildId;
		this.members = member;
	}
	
	
	@Override
	public Object run() {
		if(Objects.isNull(this.members) || this.members.isEmpty()){
			this.finish = true;
			return null;
		}
		//检查加入集结
		try {
			GuildAutoMarchQueueMember member = members.remove(0);
			this.checkJoinMarch(member);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		//继续下发任务
		this.enterTask();
		return null;
	}
	
	
	public void enterTask(){
		if(Objects.isNull(this.members) || this.members.isEmpty()){
			this.finish = true;
			return;
		}
		GuildAutoMarchQueueMember member = members.get(0);
		String playerId = member.getPlayerId();
		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
		int threadIdx = xid.getHashThread(HawkTaskManager.getInstance().getThreadNum());
		HawkTaskManager.getInstance().postTask(this,threadIdx);
	}
	
	
	
	public void checkJoinMarch(GuildAutoMarchQueueMember member){
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(this.guildId);
		if(Objects.isNull(guild)){
			return;
		}
		Player player = GlobalData.getInstance().makesurePlayer(member.getPlayerId());
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		String playerGuildId = player.getGuildId();
		if(!this.guildId.equals(playerGuildId)){
			return;
		}
		PlayerAutoMarchParam param = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
		if(!param.inWorking()){
			return;
		}
		Set<Integer> joinSet = param.getJoinSet();
		if(joinSet.isEmpty()){
			return;
		}
		Collection<IWorldMarch> mCollection = WorldMarchService.getInstance().getGuildMarchs(this.guildId);
		for(IWorldMarch march : mCollection){
			//加入集结
			for(int join : joinSet){
				PlayerAutoMarchEnum marchEnum = PlayerAutoMarchEnum.valueOf(join);
				if(Objects.nonNull(marchEnum)){
					marchEnum.checkJoinMarch(march, player);
				}
			}
		}
	}
	
	/**
	 * 任务是否完结
	 * @return
	 */
	public boolean taskFinish(){
		return this.finish;
	}
	
}
