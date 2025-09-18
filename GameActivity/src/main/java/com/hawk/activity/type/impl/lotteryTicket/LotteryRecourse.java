package com.hawk.activity.type.impl.lotteryTicket;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketKVCfg;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistApply;

public class LotteryRecourse {
	//唯一ID
	private String id;
	
	//求助者ID
	private String sourceId;
	
	//帮助者ID
	private String assistId;
	
	//数量
	private int ticketCount;
	
	//帮刮数量
	private int lotteryCount;
	
	//发起时间
	private long applyTime;
	
	//拒绝时间
	private long refuseTime;

	//刮奖结果列表
	private List<LotteryRlt> rlts = new CopyOnWriteArrayList<>();
	
	//是否已经结束
	private long finishTime;
	
	
	public LotteryRecourse() {
		
	}
	
	public LotteryRecourse(String id,String sourceId,String assistId,int ticketCount,long applyTime) {
		this.id = id;
		this.sourceId = sourceId;
		this.assistId = assistId;
		this.ticketCount = ticketCount;
		this.applyTime = applyTime;
		
	}
	
	
	public String getId() {
		return id;
	}
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getAssistId() {
		return assistId;
	}

	public void setAssistId(String assistId) {
		this.assistId = assistId;
	}

	public int getTicketCount() {
		return ticketCount;
	}

	public void setTicketCount(int ticketCount) {
		this.ticketCount = ticketCount;
	}

	public int getLotteryCount() {
		return lotteryCount;
	}

	public void setLotteryCount(int lotteryCount) {
		this.lotteryCount = lotteryCount;
	}
	
	public long getApplyTime() {
		return applyTime;
	}
	
	public void setApplyTime(long applyTime) {
		this.applyTime = applyTime;
	}
	
	public long getRefuseTime() {
		return refuseTime;
	}
	
	public void setRefuseTime(long refuseTime) {
		this.refuseTime = refuseTime;
	}
	
	
	public List<LotteryRlt> getRlts() {
		return rlts;
	}
	
	public void setRlts(List<LotteryRlt> rlts) {
		this.rlts = rlts;
	}
	
	public long getFinishTime() {
		return finishTime;
	}
	
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}
	
	public int hasBigReward(){
		for(LotteryRlt rlt :this.rlts){
			if(rlt.hasBigReward() > 0){
				return 1;
			}
		}
		return 0;
	}
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("id", this.id);
		obj.put("sourceId", this.sourceId);
		obj.put("assistId", this.assistId);
		obj.put("ticketCount", this.ticketCount);
		obj.put("lotteryCount", this.lotteryCount);
		obj.put("refuseTime", this.refuseTime);
		obj.put("applyTime", this.applyTime);
		obj.put("finishTime", this.finishTime);
		
		if(Objects.nonNull(this.rlts) && !this.rlts.isEmpty()){
			JSONArray arr = new JSONArray();
			this.rlts.forEach(r->arr.add(r.serializ()));
			obj.put("rlts", arr.toJSONString());
		}
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.id = obj.getString("id");
		this.sourceId = obj.getString("sourceId");
		this.assistId = obj.getString("assistId");
		this.ticketCount = obj.getIntValue("ticketCount");
		this.lotteryCount = obj.getIntValue("lotteryCount");
		this.refuseTime = obj.getLongValue("refuseTime");
		this.applyTime = obj.getLongValue("applyTime");
		this.finishTime = obj.getLongValue("finishTime");
		this.rlts.clear();
		if(obj.containsKey("rlts")){
			String rltStr = obj.getString("rlts");
			JSONArray arr = JSONArray.parseArray(rltStr);
			for(int i =0;i<arr.size();i++){
				String str = arr.getString(i);
				LotteryRlt lotteryRlt = new LotteryRlt();
				lotteryRlt.mergeFrom(str);
				this.rlts.add(lotteryRlt);
			}
		}
	}
	

	public PBLotteryTicketAssistApply genAssistApplyBuilder(){
		
		String sourceName = ActivityManager.getInstance().getDataGeter().getPlayerName(this.sourceId);
		String sourceGuildName = ActivityManager.getInstance().getDataGeter().getGuildNameByByPlayerId(this.sourceId);
		String sourceGuildTag = ActivityManager.getInstance().getDataGeter().getGuildTagByPlayerId(this.sourceId);
		int sourceIcon = ActivityManager.getInstance().getDataGeter().getIcon(this.sourceId);
		String sourcePfIcon = ActivityManager.getInstance().getDataGeter().getPfIcon(this.sourceId);
		
		
		String assistName = ActivityManager.getInstance().getDataGeter().getPlayerName(this.assistId);
		String assistGuildName = ActivityManager.getInstance().getDataGeter().getGuildNameByByPlayerId(this.assistId);
		String assistGuildTag = ActivityManager.getInstance().getDataGeter().getGuildTagByPlayerId(this.assistId);
		int assistIcon = ActivityManager.getInstance().getDataGeter().getIcon(this.assistId);
		String assistPfIcon = ActivityManager.getInstance().getDataGeter().getPfIcon(this.assistId);
		
		
		LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
		long outTime = this.applyTime + config.getAssistOutTime() * 1000;
		if(this.finishTime > 0){
			outTime = this.finishTime;
		}
		
		PBLotteryTicketAssistApply.Builder builder = PBLotteryTicketAssistApply.newBuilder();
		builder.setApplyId(this.id);
		
		builder.setSenderId(this.sourceId);
		if(!HawkOSOperator.isEmptyString(sourceName)){
			builder.setSenderName(sourceName);
		}
		if(!HawkOSOperator.isEmptyString(sourceGuildName)){
			builder.setSenderGuildName(sourceGuildName);
		}
		if(!HawkOSOperator.isEmptyString(sourceGuildTag)){
			builder.setSenderGuildTag(sourceGuildTag);
		}
		if(!HawkOSOperator.isEmptyString(sourcePfIcon)){
			builder.setSenderPfIcon(sourcePfIcon);
		}
		builder.setSenderIcon(sourceIcon);
		
		builder.setAssistId(this.assistId);
		if(!HawkOSOperator.isEmptyString(assistName)){
			builder.setAssistName(assistName);
		}
		if(!HawkOSOperator.isEmptyString(assistGuildName)){
			builder.setAssistGuildName(assistGuildName);
		}
		if(!HawkOSOperator.isEmptyString(assistGuildTag)){
			builder.setAssistGuildTag(assistGuildTag);
		}
		if(!HawkOSOperator.isEmptyString(assistPfIcon)){
			builder.setAssistPfIcon(assistPfIcon);
		}
		builder.setAssistIcon(assistIcon);
		
		builder.setLotteryTicketCount(this.ticketCount);
		builder.setLotteryTicketUseCount(this.lotteryCount);
		builder.setApplyTime(this.applyTime);
		builder.setOutTime(outTime);
		builder.setRefuseTime(this.refuseTime);
		builder.setSendReward(this.hasBigReward());
		
		return builder.build();
	}


}
