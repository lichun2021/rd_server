package com.hawk.game.module.college.entity;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class CollegeStatisticsEntity {
	
	/**
	 * 每天登录过的学员
	 */
	private Set<String> loginMembers = new ConcurrentHashSet<>();
	
	/**
	 * 活跃积分
	 */
	private int weekScore;
	
	/**
	 * 活跃积分刷新周
	 */
	private int refresWeek;
	
	/**
	 * 刷新时间
	 */
	private int refreshDay;
	
	
	
	
	
	public Set<String> getLoginMembers() {
		int today = HawkTime.getYyyyMMddIntVal();
		if(today == this.refreshDay){
			return this.loginMembers;
		}
		return null;
	}
	
	
	public int getLoginMemberCount(){
		Set<String> set = this.getLoginMembers();
		if(Objects.isNull(set)){
			return 0;
		}
		return set.size();
	}
	
	
	public int getWeekScore(){
		int toWeek = HawkTime.getYearWeek();
		if(toWeek == this.refresWeek){
			return this.weekScore;
		}
		return 0;
	}
	
	public boolean addLoginMember(Collection<String> members){
		if(members.size() <= 0){
			return false;
		}
		int today = HawkTime.getYyyyMMddIntVal();
		if(today != this.refreshDay){
			this.loginMembers.clear();
			this.refreshDay = today;
		}
		int size = this.loginMembers.size();
		this.loginMembers.addAll(members);
		if(this.loginMembers.size() != size){
			return true;
		}
		return false;
	}
	
	public boolean refreshWeekScore(){
		int toWeek = HawkTime.getYearWeek();
		if(toWeek != this.refresWeek){
			this.weekScore =0;
			this.refresWeek = 0;
			return true;
		}
		return false;
	}
	
	public boolean refreshLoginMember(){
		int today = HawkTime.getYyyyMMddIntVal();
		if(today != this.refreshDay){
			this.loginMembers.clear();
			this.refreshDay = today;
			return true;
		}
		return false;
	}
	
	
	public boolean addWeekScore(int score){
		if(score <= 0){
			return false;
		}
		int toWeek = HawkTime.getYearWeek();
		if(toWeek != this.refresWeek){
			this.weekScore =0;
			this.refresWeek = toWeek;
		}
		this.weekScore += score;
		return true;
	}
	
	
	
	
	
	
	
	public void setRefresWeek(int refresWeek) {
		this.refresWeek = refresWeek;
	}
	
	public void setRefreshDay(int refreshDay) {
		this.refreshDay = refreshDay;
	}
	
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		if(this.loginMembers.size() >= 0){
			JSONArray memberArr = new JSONArray();
			for(String str:this.loginMembers){
				memberArr.add(str);
			}
			obj.put("loginMembers", memberArr.toJSONString());
		}
		obj.put("weekScore", this.weekScore);
		obj.put("refresWeek", this.refresWeek);
		obj.put("refreshDay", this.refreshDay);
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		Set<String> loginMembersTemp = new ConcurrentHashSet<>();
		if(obj.containsKey("loginMembers")){
			String loginMembersStr = obj.getString("loginMembers");
			if(!HawkOSOperator.isEmptyString(loginMembersStr)){
				JSONArray memberArr = JSONArray.parseArray(loginMembersStr);
				for(int i=0;i<memberArr.size();i++){
					loginMembersTemp.add(memberArr.getString(i));
				}
			}
		}
		this.loginMembers = loginMembersTemp;
		this.weekScore = obj.getIntValue("weekScore");
		this.refresWeek = obj.getIntValue("refresWeek");
		this.refreshDay = obj.getIntValue("refreshDay");
	}
	
	

}
