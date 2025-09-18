package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 退出联盟
 * 
 * @author Jesse
 *
 */
public class CollegeQuitMsg extends HawkMsg {
	/**
	 * 是否被踢出
	 */
	boolean isKick;
	/**
	 * 是否解散
	 */
	boolean isDismiss;
	/**
	 * 学院Id
	 */
	String collegeId;
	
	/**
	 * 教官id
	 */
	String coachId;
	/**
	 * 教官名字
	 */
	String coachName;
	
	public boolean isKick() {
		return isKick;
	}

	public void setKick(boolean isKick) {
		this.isKick = isKick;
	}
	
	public boolean isDismiss() {
		return isDismiss;
	}

	public void setDismiss(boolean isDismiss) {
		this.isDismiss = isDismiss;
	}

	public String getCollegeId() {
		return collegeId;
	}

	public void setCollegeId(String collegeId) {
		this.collegeId = collegeId;
	}

	public String getCoachId() {
		return coachId;
	}

	public void setCoachId(String coachId) {
		this.coachId = coachId;
	}

	public String getCoachName() {
		return coachName;
	}

	public void setCoachName(String coachName) {
		this.coachName = coachName;
	}

	public CollegeQuitMsg() {
		super(MsgId.COLLEGE_QUIT_MSG);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static CollegeQuitMsg valueOf(String collegeId, String coachId, String coachName, boolean isKick, boolean isDismiss) {
		CollegeQuitMsg msg = new CollegeQuitMsg();
		msg.collegeId = collegeId;
		msg.coachId = coachId;
		msg.coachName = coachName;
		msg.isKick = isKick;
		msg.isDismiss = isDismiss;
		return msg;
	}
}
