package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONObject;

/**
 * 活动公告
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/activity_billboard.xml")
public class ActivityBillboard extends HawkConfigBase implements Comparable<ActivityBillboard>{
	
	/**
	 * 活动id
	 */
	@Id
	protected final int activityId;
	
	/**
	 * 公告类型
	 */
	protected final int type;
	
	/**
	 * 公告标题
	 */
	protected final String title;
	
	/**
	 * 公告内容
	 */
	protected final String content;
	
	/**
	 * 跳转url
	 */
	protected final String redirect;
	
	/**
	 * 优先级
	 */
	protected final int priority;
	
	public ActivityBillboard() {
		activityId = 0;
		type = 0;
		title = "";
		content = "";
		redirect = "";
		priority = 0;
	}

	public int getActivityId() {
		return activityId;
	}

	public int getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getRedirect() {
		return redirect;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * 转换为显示json
	 * 
	 * @return
	 */
	public JSONObject toShow() {
		JSONObject json = new JSONObject();
		json.put("type", type);
		json.put("title", title);
		json.put("content", content);
		json.put("redirect", redirect);
		return json;
	}

	@Override
	public int compareTo(ActivityBillboard o) {
		return this.getPriority() <= o.priority ? 1 : -1;
	}
}
