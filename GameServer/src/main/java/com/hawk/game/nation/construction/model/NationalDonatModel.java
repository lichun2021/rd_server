package com.hawk.game.nation.construction.model;

import java.io.Serializable;

import com.hawk.game.GsApp;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.global.LocalRedis;

/**
 * 国家捐献个人记录
 * @author zhenyu.shang
 * @since 2022年3月29日
 */
public class NationalDonatModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String playerId;
	
	private int leftcount;

	private long resumeTime;

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getLeftcount() {
		return leftcount;
	}

	public void setLeftcount(int leftcount) {
		this.leftcount = leftcount;
	}

	public long getResumeTime() {
		return resumeTime;
	}

	public void setResumeTime(long resumeTime) {
		this.resumeTime = resumeTime;
	}
	
	/**
	 * 更新捐献信息
	 * @return
	 */
	public boolean doDonate() {
		if(leftcount <= 0){
			return false;
		}
		// 减次数
		leftcount--;
		// 设置恢复时间
		if(resumeTime == 0) {
			resumeTime = GsApp.getInstance().getCurrentTime() + NationConstCfg.getInstance().getRebuildingRecoveryTime() * 1000L;
		}
		return true;
	}
	
	/**
	 * 检查恢复次数
	 */
	public void checkResumeCount(long now) {
		if(resumeTime > 0 && now > this.resumeTime) {
			int addCount = 1 + (int) ((now - this.resumeTime) / (NationConstCfg.getInstance().getRebuildingRecoveryTime() * 1000L));
			if(addCount < 0){
				// 防止异常溢出
				return;
			}
			// 增加次数并且判断是否到达上限
			this.leftcount += addCount;
			if(this.leftcount > NationConstCfg.getInstance().getRebuildingCountLimit()) {
				this.leftcount = NationConstCfg.getInstance().getRebuildingCountLimit();
				this.resumeTime = 0;
			} else {
				resumeTime = now + NationConstCfg.getInstance().getRebuildingRecoveryTime() * 1000L;
			}
			// 更新redis
			LocalRedis.getInstance().updateNationalDonateInfo(playerId, this);
		}
	}
}
