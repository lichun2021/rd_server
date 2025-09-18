package com.hawk.game.module.autologic;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.module.autologic.cfg.AutoMassJoinCfg;
import com.hawk.game.module.autologic.data.GuildAutoMarchQueueMember;
import com.hawk.game.module.autologic.data.PlayerAutoMarchEnum;
import com.hawk.game.module.autologic.data.PlayerAutoMarchParam;
import com.hawk.game.module.autologic.service.GuildAutoMarchService;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP.code2;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.AutoMassJoinSettingReq;
import com.hawk.game.protocol.World.AutoMassJoinSettingResp;
import com.hawk.game.protocol.World.AutoMassJoinType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;

public class PlayerAutoMassJoinModule  extends PlayerModule {

	
	private Map<Integer,Long> missMail = new ConcurrentHashMap<>();
	private long tickTime;
	
	
	public PlayerAutoMassJoinModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerLogin() {
		this.syncAutoData();
		return true;
	}
	
	@Override
	public boolean onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime < tickTime + 5000){
			return false;
		}
		this.tickTime = curTime;
		this.addToGuildQueue();
		return true;
	}
	
	/**
	 * 请求信息
	 * @param protocol
	 */
	@ProtocolHandler(code = code2.AUTO_MASS_JOIN_DATA_REQ_VALUE)
	public void autoMassJoinInfo(HawkProtocol protocol){
		this.syncAutoData();
	}
	
	
	
	/**
	 * 开始
	 * @param protocol
	 */
	@ProtocolHandler(code = code2.AUTO_MASS_JOIN_START_REQ_VALUE)
	public void startAutoMassJoin(HawkProtocol protocol){
		AutoMassJoinSettingReq req = protocol.parseProtocol(AutoMassJoinSettingReq.getDefaultInstance());
	    int marchCnt = req.getMarchCnt();
	    int soldierPer = req.getSoldierPer();
	    List<AutoMassJoinType> joins = req.getTypesList();
	    if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
	    if(!player.hasGuild()){
			return;
		}
	    int maxMarchCnt = player.getMaxMarchNum();
	    if(marchCnt > maxMarchCnt){
	    	return;
	    }
	    if(soldierPer <=0 || soldierPer > 100){
	    	return;
	    }
	    if(joins.isEmpty()){
	    	return;
	    }
	    for(AutoMassJoinType type : joins){
	    	PlayerAutoMarchEnum autoEnum = PlayerAutoMarchEnum.valueOf(type.getNumber());
	    	if(Objects.isNull(autoEnum)){
	    		return;
	    	}
	    }
	    long curTime = HawkTime.getMillisecond();
	    PlayerAutoMarchParam paramData = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
	    paramData.setMarchCount(marchCnt);
	    paramData.setMarchSoldierPer(soldierPer);
	    paramData.getJoinSet().clear();
	    for(AutoMassJoinType type : joins){
	    	PlayerAutoMarchEnum autoEnum = PlayerAutoMarchEnum.valueOf(type.getNumber());
	    	paramData.getJoinSet().add(autoEnum.getVal());
	    }
	    paramData.setOpenTime(curTime);
		paramData.notifyUpdate();
		this.addToGuildQueue();
		this.syncAutoData();
		player.responseSuccess(protocol.getType());
		//日志
		LogUtil.logAutoMassJionOpen(player, paramData.serialize());
	}
	
	
	
	/**
	 * 结束
	 * @param protocol
	 */
	@ProtocolHandler(code = code2.AUTO_MASS_JOIN_STOP_REQ_VALUE)
	public void stopAutoMassJoin(HawkProtocol protocol){
		PlayerAutoMarchParam paramData = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
		paramData.setOpenTime(0);
		paramData.notifyUpdate();
		GuildAutoMarchService.getInstance().removeGuildAutoMarchQueueMember(player);
		this.syncAutoData();
		player.responseSuccess(protocol.getType());
	}

	
	
	/**
	 * 添加进联盟队列
	 */
	public void addToGuildQueue(){
		if(player.isInDungeonMap()){
			return;
		}
		if(player.isCsPlayer()){
			return;
		}
		
		if(!player.hasGuild()){
			return;
		}
		//世界点没有了
		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(this.player.getId());
		if(Objects.isNull(wp)){
			return;
		}
		PlayerAutoMarchParam param = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
		//已经不在工作中了
		if(!param.inWorking()){
			return;
		}
		//已经自排队了
		if(this.inGuildAutoQueue()){
			return;
		}
		//添加进入队列
		GuildAutoMarchService.getInstance()
			.addGuildAutoMarchQueueMember(this.player);
	}
	
	
	
	@MessageHandler
	public boolean onGuildQuitMsg(GuildQuitMsg msg) {
		PlayerAutoMarchParam paramData = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
		paramData.setOpenTime(0);
		paramData.notifyUpdate();
		GuildAutoMarchService.getInstance().removeGuildAutoMarchQueueMember(player);
		this.syncAutoData();
		return true;
	}
	
	
	/**
	 * 同步数据
	 */
	public void syncAutoData(){
		PlayerAutoMarchParam paramData = player.getData().getPlayerOtherEntity().getPlayerAutoMarchParam();
		AutoMassJoinSettingResp.Builder builder = AutoMassJoinSettingResp.newBuilder();
		builder.setMarchCnt(paramData.getMarchCount());
		builder.setSoldierPer(paramData.getMarchSoldierPer());
		builder.setStartTime(paramData.getOpenTime());
		builder.setEndTime(paramData.getEndTime());
		builder.setMemberIndex(GuildAutoMarchService.getInstance().getGuildAutoQueueOrder(this.player));
		for(int type : paramData.getJoinSet()){
			AutoMassJoinType join = AutoMassJoinType.valueOf(type);
			builder.addTypes(join);
	    }
		this.player.sendProtocol(HawkProtocol.valueOf(code2.AUTO_MASS_JOIN_DATA_RESP_VALUE, builder));
	}
	
	
	
	
	
	/**
	 * 是否已经在队列中
	 * @return
	 */
	public boolean inGuildAutoQueue(){
		GuildAutoMarchQueueMember member = GuildAutoMarchService.getInstance()
				.getGuildAutoMarchQueueMember(player);
		if(Objects.isNull(member)){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 发送错误邮件
	 * 距离远参数 ：类型（1）  ，行军类型（幽灵集结，怪物首领集结） ，怪物ID   , 队长名字   
	 * 不在线参数: 类型（2）  ,  行军类型（幽灵集结，怪物首领集结） ,  0  , 队长名字
	 */
	public void addMissMail(int reason,String... param){
		AutoMassJoinCfg cfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
		long curTime = HawkTime.getMillisecond();
		long lastTime = this.missMail.getOrDefault(reason, 0l);
		if(curTime > lastTime + cfg.getMissMailTime() * 1000){
			return;
		}
		this.missMail.put(reason, curTime);
		
		MailParames.Builder mailBuilder= MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.AUTO_MASS_JOIN_MISS)
				.addSubTitles(reason)
				.addContents(String.valueOf(reason));
		for(String str : param){
			mailBuilder.addContents(str);
		}
		SystemMailService.getInstance().sendMail(mailBuilder.build());
	}
	
	
	
	

}
