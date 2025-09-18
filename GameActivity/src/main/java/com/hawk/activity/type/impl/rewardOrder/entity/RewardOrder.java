package com.hawk.activity.type.impl.rewardOrder.entity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderCfg;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderTaskCfg;
import com.hawk.game.protocol.Activity.orderState;

/***
 * 悬赏令
 * @author yang.rao
 *
 */
public class RewardOrder {
	
	public static final int NOT_RECEIVE = 1;
	
	public static final int TAKE = 2; //未完成
	
	public static final int FINISH = 3; //已经完成
	
	public static final int FAIL = 4; //失败
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 悬赏令id **/
	private int id;
	
	/** 领取任务时间 **/
	private long beginTime;
	
	/** 悬赏令任务 **/
	private List<RewardOrderTask> tasks;
	
	/** 悬赏令配置 **/
	@JSONField(serialize=false)
	private RewardOrderCfg config;
	
	/** 悬赏令状态 **/
	private int state;
	
	/** 是否领取奖励 1为已经领取，0为未领取 **/
	private boolean takeRewar;
	
	public RewardOrder(){}
	
	public RewardOrder(int id, RewardOrderCfg config){
		this.id = id;
		this.config = config;
		this.tasks = new ArrayList<RewardOrderTask>();
		this.state = RewardOrder.NOT_RECEIVE;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}

	public RewardOrderCfg getConfig() {
		return config;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public List<RewardOrderTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<RewardOrderTask> tasks) {
		this.tasks = tasks;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public boolean receive(){
		return state == TAKE;
	}
	
	public boolean fail(){
		return state == FAIL;
	}
	
	public boolean finish(){
		return state == FINISH;
	}
	
	public boolean isTakeRewar() {
		return takeRewar;
	}

	public void setTakeRewar(boolean takeRewar) {
		this.takeRewar = takeRewar;
	}
	
	public void setConfig(RewardOrderCfg config) {
		this.config = config;
	}

	public void initTask(List<RewardOrderTaskCfg> list){
		if(list == null || list.isEmpty()){
			return;
		}
		this.tasks.clear();
		for(RewardOrderTaskCfg cfg : list){
			RewardOrderTask task = new RewardOrderTask(cfg.getAchieveId());
			this.tasks.add(task);
		}
	}

	public void onTick(long nowTime, RewardOrderEntity entity){
		//如果超时，设置state
		if((nowTime - beginTime >= config.getConsistTime() * 1000l) && this.state == TAKE){
			logger.info("rewardOrder fail. playerId:" + entity.getPlayerId() + ", msg:" + entity);
			this.state = FAIL;
		}
	}
	
	public long calEndTime(){
		return beginTime + config.getConsistTime() * 1000l;
	}
	
	public void achieveFinish(AchieveItem item, RewardOrderEntity entity){
		boolean orderFinish = true;
		for(RewardOrderTask task : tasks){
			if(task.getId() == item.getAchieveId()){
				task.setFinish(true);
			}
			if(!task.isFinish()){
				orderFinish = false;
			}
		}
		if(orderFinish){
			state = FINISH;
			logger.info("rewardOrder finish. orderId is:"+ id + ",playerId:" + entity.getPlayerId() + ", msg:" + entity);
		}
	}
	
	public boolean canFresh(){
		return state == NOT_RECEIVE;
	}
	
	public orderState calOrderStateProto(){
		switch (state) {
		case 1:
			return orderState.NOT_RECEIVED;
		case 2:
			return orderState.RECEIVED;
		case 3:
			return orderState.FINISH;
		case 4:
			return orderState.FAIL;
		default:
			return null;
		}
	}
}
