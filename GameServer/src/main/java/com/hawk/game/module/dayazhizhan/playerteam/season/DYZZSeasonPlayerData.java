package com.hawk.game.module.dayazhizhan.playerteam.season;

import java.util.List;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DYZZGradeEvent;
import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonGradeCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;

public class DYZZSeasonPlayerData implements SerializJsonStrAble{
	
	private int termId;
	
	private String serverId;
	
	private String platform;
	
	private String openId;
	
	private String playerId;
	
	private int score;
	
	private int continueScore;
	
	private int sendScoreReward;
	
	private int battleCount;
	/**
	 * 发放奖励
	 */
	public boolean doSendScoreReward(){
		if(this.sendScoreReward > 0){
			return false;
		}
		DYZZSeasonGradeCfg gradeCfg = DYZZSeasonService.getInstance().getGrade(this.score);
		if(gradeCfg == null){
			return false;
		}
		this.sendScoreReward = gradeCfg.getId();
		List<ItemInfo> items = gradeCfg.getSettlementRewardItems();
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.DYZZ_SEASON_GRADE_REWARD)
				.setPlayerId(playerId)
				.addContents(this.termId, this.score)
				.setRewards(items)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		ActivityManager.getInstance().postEvent(new DYZZGradeEvent(playerId, gradeCfg.getId(), termId));
		return true;
	}
		
	/**
	 * 获取段位
	 * @return
	 */
	public int getGrade(){
		DYZZSeasonGradeCfg cfg = DYZZSeasonService.getInstance().getGrade(this.score);
		if(cfg == null){
			return 0;
		}
		return cfg.getId();
	}
	
	/**
	 * 添加积分
	 * @param addScore
	 * @return
	 */
	public int addScore(int addScore){
		this.score += addScore;
		this.score = Math.max(this.score, 0);
		return this.score;
	}
	
	/**
	 * 承接积分
	 * @return
	 */
	public void continueScoreFrom(int score){
		DYZZSeasonCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		if(score > cfg.getContineScoreParam1()){
//			int continueScoreTemp = (int) Math.max(cfg.getContineScoreParam1(),
//					cfg.getContineScoreParam2() * score);
			int continueScoreTemp = (int) Math.max(cfg.getContineScoreParam1(),
					Math.pow(score, cfg.getContineScoreParam2()) + cfg.getContineScoreParam3());
					this.score = continueScoreTemp;
			this.continueScore = continueScoreTemp;
		}else{
//			int continueScoreTemp = (int) Math.max(cfg.getSeasonScoreInit(), score);
			int continueScoreTemp = score;
			this.score = continueScoreTemp;
			this.continueScore = continueScoreTemp;
		}
	}

	public void addBattleCount(){
		this.battleCount++;
	}
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", termId);
		obj.put("serverId", serverId);
		obj.put("platform", platform);
		obj.put("openId", openId);
		obj.put("playerId", playerId);
		obj.put("score", score);
		obj.put("continueScore", continueScore);
		obj.put("sendScoreReward", sendScoreReward);
		obj.put("battleCount", battleCount);
		return obj.toJSONString();
		
		
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.serverId = obj.getString("serverId");
		this.platform = obj.getString("platform");
		this.openId = obj.getString("openId");
		this.playerId = obj.getString("playerId");
		this.score = obj.getIntValue("score");
		this.continueScore = obj.getIntValue("continueScore");
		this.sendScoreReward = obj.getIntValue("sendScoreReward");
		this.battleCount = obj.getIntValue("battleCount");
	}
	

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public String getPlatform() {
		return platform;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getOpenId() {
		return openId;
	}
	
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	
	public long getContinueScore() {
		return continueScore;
	}
	
	public int getSendScoreReward() {
		return sendScoreReward;
	}

	public int getBattleCount() {
		return battleCount;
	}
}
