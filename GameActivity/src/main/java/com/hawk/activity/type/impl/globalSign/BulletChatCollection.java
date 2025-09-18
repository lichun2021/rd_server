package com.hawk.activity.type.impl.globalSign;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.game.protocol.Activity.GlobalSignInBulletChatResp;
import com.hawk.game.protocol.HP;

import redis.clients.jedis.Tuple;

public class BulletChatCollection {
	/** 缓存弹幕*/
	private LinkedList<BulletChat> chats = new LinkedList<>();
	/** 缓存列表长度*/
	private int cacheCount;
	/** 刷新时间*/
	private long lastRefreshTime;
	/** 刷新间隔*/
	private long refreshInterval;
	/** 期数*/
	private int termId;
	
	public BulletChatCollection(int cacheCount,long refreshInterval) {
		this.cacheCount = cacheCount;
		this.refreshInterval = refreshInterval;
	}
	
	/**
	 * 更新
	 */
	public void update(int curtermId){
		long curTime = HawkTime.getMillisecond();
		if(this.termId != curtermId){
			this.termId = curtermId;
			this.lastRefreshTime = curTime;
			this.load();
			return;
		}
		if(curTime - this.lastRefreshTime < this.refreshInterval){
			return;
		}
		this.lastRefreshTime = curTime;
		if(this.chats.size() <= this.cacheCount){
			return;
		}
		HawkTask task = new HawkTask(){
			@Override
			public Object run() {
				//只要前cacheCount
				int size = chats.size();
				if(size <= cacheCount){
					return null;
				}
				int dropSize = size - cacheCount;
				for(int i=0;i<dropSize;i++){
					chats.removeLast();
				}
				//清除过时记录
				String key = getRedisKey(termId);
				ActivityGlobalRedis.getInstance().getRedisSession().zRemrangeByRank(key, 0, -cacheCount);
				return null;
			}};
		this.addTask(task, "global_sign_update_bullet_chat");
	}
	
	
	/**
	 * 添加弹幕
	 * @param termId
	 * @param chat
	 */
	public void addBulletChat(BulletChat chat){
		HawkTask task = new HawkTask(){
			@Override
			public Object run() {
				chats.addFirst(chat);
				long curTime = HawkTime.getMillisecond();
				String key = getRedisKey(termId);
				String str = JSONObject.toJSONString(chat);
				ActivityLocalRedis.getInstance().zaddWithExpire(key, curTime, str,(int)TimeUnit.DAYS.toSeconds(30));
				return null;
			}
		};
		this.addTask(task, "global_sign_add_bullet_chat");
	}
	
	
	
	/**
	 * 获取弹幕
	 * @param time
	 * @return
	 */
	public void sendChat(String playerId,long time,int maxCount){
		HawkTask task = new HawkTask(){
			@Override
			public Object run() {
				GlobalSignInBulletChatResp.Builder builder = GlobalSignInBulletChatResp.newBuilder();
				for(BulletChat chat : chats){
					if(builder.getChatsCount() >= maxCount){
						break;
					}
					if(chat.getChatTime() < time){
						break;
					}
					builder.addChats(chat.createBuilder());
				}
				PlayerPushHelper.getInstance().pushToPlayer(playerId,
						HawkProtocol.valueOf(HP.code.GLOBAL_SIGN_PLAYER_CHAT_RESP_VALUE, builder));
				return null;
			}
		};
		this.addTask(task, "global_sign_get_bullet_chat");
	}
	
	
	/**
	 * 加载
	 */
	public void load(){
		String key = this.getRedisKey(this.termId);
		Set<Tuple> set = ActivityGlobalRedis.getInstance().getRedisSession().
				zRevrangeWithScores(key,0, this.cacheCount,(int)TimeUnit.DAYS.toSeconds(30));
		if(set == null || set.size() <= 0){
			return;
		}
		for(Tuple tuple : set){
			String data = tuple.getElement();
			BulletChat chat = JSONObject.parseObject(data, BulletChat.class);
			this.chats.add(chat);
		}
		//正序排列
		Collections.sort(this.chats,new Comparator<BulletChat>() {
			@Override
			public int compare(BulletChat o1, BulletChat o2) {
				if(o1.getChatTime() > o2.getChatTime()){
					return -1;
				}
				return 1;
			}
		});
	}
	
	
	
	public void addTask(HawkTask task,String taskName){
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName(taskName);
			taskPool.addTask(task, 0, false);
		}
	}
	
	
	
	public String getRedisKey(int termId){
		return ActivityRedisKey.GLOBAL_SIGN_BULLET_CHAT+":"+ termId;
	}
	
}
