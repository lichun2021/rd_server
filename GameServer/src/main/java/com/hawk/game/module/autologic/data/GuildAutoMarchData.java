package com.hawk.game.module.autologic.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.global.GlobalData;
import com.hawk.game.module.autologic.cfg.AutoMassJoinCfg;
import com.hawk.game.module.autologic.task.GuildMarchAutoJoinTask;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;

public class GuildAutoMarchData {

	private String guildId;
	//排名
	private AtomicLong order;
	//联盟自动成员
	private Map<String,GuildAutoMarchQueueMember> memebers = new ConcurrentHashMap<>();
	//排序后的队列
	private List<GuildAutoMarchQueueMember> autoList = new ArrayList<>();
	//任务对象
	private GuildMarchAutoJoinTask joinTask;
	
	
	public GuildAutoMarchData(String guildId) {
		this.guildId = guildId;
		this.order = new AtomicLong(1);
	}
	
	
	
	/**
	 *轮询一下 
	 */
	public void onTick(){
		//检查联盟成员是否合格
		this.checkMemeber();
		//排序
		this.fillMemerList();
		//执行
		this.doAutoMarchJoin();
	}
	
	
	
	
	/**
	 * 获取在队伍中的排位
	 * @param playerId
	 * @return
	 */
	public int getAutoQueueOrder(String playerId){
		GuildAutoMarchQueueMember member = this.memebers.get(playerId);
		if(Objects.isNull(member)){
			return 0;
		}
		int order = member.getQueueOrder();
		if(order > 0){
			return order;
		}
		return this.memebers.size();
	}
	
	
	
	
	
	/**
	 * 添加成员
	 * @param playerId
	 */
	public void addGuildAutoMarchMember(String playerId){
		if(this.memebers.containsKey(playerId)){
			return;
		}
		int queueSize = this.memebers.size() +1;
		GuildAutoMarchQueueMember member = new GuildAutoMarchQueueMember(playerId, this.order.incrementAndGet(),queueSize);
		this.memebers.put(playerId, member);
	}
	
	
	
	
	
	/**
	 * 移出成员
	 * @param playerId
	 */
	public void removeGuildAutoMarchMember(String playerId){
		this.memebers.remove(playerId);
	}
	
	
	
	
	
	
	/**
	 * 获取成员信息
	 * @param playerId
	 * @return
	 */
	public GuildAutoMarchQueueMember getGuildAutoMarchQueueMember(String playerId){
		return this.memebers.get(playerId);
	}
	
	
	
	/**
	 * 检查成员状态是否正确并删除
	 */
	public void checkMemeber(){
		Set<String> dels = new HashSet<>();
		for(GuildAutoMarchQueueMember member : this.memebers.values()){
			
			//不在线
			Player player = GlobalData.getInstance().getActivePlayer(member.getPlayerId());
			if (player == null) {
				dels.add(member.getPlayerId());
				continue;
			}
			//跨服中
			if(player.isCsPlayer()){
				dels.add(member.getPlayerId());
				continue;
			}
			//副本中
			if(player.isInDungeonMap()){
				dels.add(member.getPlayerId());
				continue;
			}
			//换联盟了
			if(!this.guildId.equals(player.getGuildId())){
				dels.add(member.getPlayerId());
				continue;
			}
			//超时
			PlayerAutoMarchParam param = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
			if(!param.inWorking()){
				dels.add(member.getPlayerId());
				continue;
			}
			//世界点没有了
			WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(member.getPlayerId());
			if(Objects.isNull(wp)){
				dels.add(member.getPlayerId());
				continue;
			}
		}
		//删除不合适的
		for(String key : dels){
			this.memebers.remove(key);
		}
		
	}
	
	
	
	
	
	/**
	 * 检查是否有合适的集结
	 * @param mCloection
	 * @return
	 */
	public boolean checkMarch(Collection<IWorldMarch> mCloection){
		if(mCloection.isEmpty()){
			return false;
		}
		for(IWorldMarch march : mCloection){
			if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				for(PlayerAutoMarchEnum autoEnum : PlayerAutoMarchEnum.values()){
					boolean rlt = autoEnum.checkWorldMarch(march);
					if(rlt){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	
	
	/**
	 *填充排序列表
	 */
	public void fillMemerList(){
		if(this.memebers.isEmpty()){
			return;
		}
		AutoMassJoinCfg cfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
		List<GuildAutoMarchQueueMember> mlist = new ArrayList<>();
		mlist.addAll(this.memebers.values());
		//排序
		Collections.sort(mlist, new Comparator<GuildAutoMarchQueueMember>() {
			@Override
			public int compare(GuildAutoMarchQueueMember o1,GuildAutoMarchQueueMember o2) {
				if(o1.getOrderParam() != o2.getOrderParam()){
					return o1.getOrderParam() > o2.getOrderParam()?1:-1;
				}
				return o1.getPlayerId().compareTo(o2.getPlayerId());
			}
		});
		int index = 1;
		for(GuildAutoMarchQueueMember member : mlist){
			member.setQueueOrder(index);
			index++;
			if(cfg.needQAlog()){
				HawkLog.logPrintln("GuildAutoMarchData-fillMemerList,guildId:{},playerId:{},order:{}", 
						this.guildId,member.getPlayerId(),member.getQueueOrder());
			}
		}
		this.autoList = mlist;
	}
	
	
	
	
	/**
	 * 开始执行任务
	 */
	public void doAutoMarchJoin(){
		//是否有合适的集结行军
		Collection<IWorldMarch> mCollection = WorldMarchService.getInstance().getGuildMarchs(this.guildId);
		boolean checkMarchRlt = this.checkMarch(mCollection);
		if(!checkMarchRlt){
			return;
		}
		if(this.autoList.isEmpty()){
			return;
		}
		//如果上一次的任务还没有完成，则等上一次的任务完成
		if(Objects.nonNull(this.joinTask) && !this.joinTask.taskFinish()){
			return;
		}
		//开始加入集结
		this.joinTask = new GuildMarchAutoJoinTask(this.guildId,this.autoList);
		this.joinTask.enterTask();
	}
	
	
	
	/**
	 * 已经有队伍加入到集结
	 * @param playerId
	 */
	public void onMassJoin(String playerId){
		GuildAutoMarchQueueMember member = this.getGuildAutoMarchQueueMember(playerId);
		if(Objects.isNull(member)){
			return;
		}
		member.setOrderParam(this.order.incrementAndGet());
	}
}
