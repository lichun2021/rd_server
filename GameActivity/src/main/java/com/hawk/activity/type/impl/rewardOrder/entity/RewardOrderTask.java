package com.hawk.activity.type.impl.rewardOrder.entity;

/***
 * 悬赏令任务
 * @author yang.rao
 *
 */
public class RewardOrderTask {
	
	/** 任务id **/
	private int id;
	
	private boolean finish;
	
	public RewardOrderTask(){}
	
	public RewardOrderTask(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}
}
