package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 待办事项（标题）检测回调事件
 * 
 * @author lating
 *
 */
public class ScheduleCheckBackMsg extends HawkMsg {
	
	private String uuid;
	private int type;
	private String title;
	private long startTime;
	private int continues;
	private int posX;
	private int posY;

	public ScheduleCheckBackMsg() {
		super(0);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static ScheduleCheckBackMsg valueOf(String uuid, int type, String title, long startTime, int continues, int posX, int posY) {
		ScheduleCheckBackMsg msg = new ScheduleCheckBackMsg();
		msg.uuid = uuid;
		msg.type = type;
		msg.title = title;
		msg.startTime = startTime;
		msg.continues = continues;
		msg.posX = posX;
		msg.posY = posY;
		return msg;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getContinues() {
		return continues;
	}

	public void setContinues(int continues) {
		this.continues = continues;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}
	
}
