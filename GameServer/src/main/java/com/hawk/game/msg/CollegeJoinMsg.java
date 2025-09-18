package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 加入学院
 * 
 * @author Jesse
 *
 */
public class CollegeJoinMsg extends HawkMsg {
	
	/**
	 * 学院Id
	 */
	String collegeId;
	/**
	 * 教官id
	 */
	String coachId;
	/**
	 * 教官姓名
	 */
	String coachName;
	/**
	 * 是否创建
	 */
	boolean isCreate;
	/**
	 * 是否被邀请
	 */
	boolean isInvite;

	public void setCollegeId(String collegeId, String coachId, String coachName, boolean isCreate, boolean isInvite) {
		this.collegeId = collegeId;
		this.coachId = coachId;
		this.coachName = coachName;
		this.isCreate = isCreate;
		this.isInvite = isInvite;
	}
	
	public String getCollegeId() {
		return collegeId;
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

	public void setCollegeId(String collegeId) {
		this.collegeId = collegeId;
	}

	public boolean isCreate() {
		return isCreate;
	}

	public void setCreate(boolean isCreate) {
		this.isCreate = isCreate;
	}

	public boolean isInvite() {
		return isInvite;
	}

	public void setInvite(boolean isInvite) {
		this.isInvite = isInvite;
	}

	public CollegeJoinMsg() {
		super(MsgId.COLLEGE_JOIN);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static CollegeJoinMsg valueOf(String collegeId, String coachId, String coachName, boolean isCreate, boolean isInvite) {
		CollegeJoinMsg msg = new CollegeJoinMsg();
		msg.collegeId = collegeId;
		msg.coachId = coachId;
		msg.coachName = coachName;
		msg.isCreate = isCreate;
		msg.isInvite = isInvite;
		return msg;
	}
}
