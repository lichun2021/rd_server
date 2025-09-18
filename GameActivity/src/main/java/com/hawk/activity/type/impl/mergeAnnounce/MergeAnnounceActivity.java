package com.hawk.activity.type.impl.mergeAnnounce;

import java.util.Collection;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.mergeAnnounce.cfg.MergeAnnounceKVCfg;
import com.hawk.game.protocol.Activity.MergeAnnounceInfo;
import com.hawk.game.protocol.HP;
/**
 * 可拆分和服通告
 * @author che
 *
 */
public class MergeAnnounceActivity extends ActivityBase{

	
	private int mergeId;
	
	public MergeAnnounceActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.MERGE_ANNOUNCE_ACTIVITY;
	}

	
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MergeAnnounceActivity activity = new MergeAnnounceActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}
	
	
	@Override
	public boolean isActivityClose(String playerId) {
		if(this.mergeId <= 0){
			return true;
		}
		return false;
	}
	
	@Override
	public void onTick() {
		int curId = this.getMergeId();
		if(this.mergeId != curId){
			this.mergeId = curId;
			Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
			for (String playerId : onlinePlayerIds) {
				syncActivityStateInfo(playerId);
				syncActivityDataInfo(playerId);
			}
		}
		
	}
	
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		MergeAnnounceInfo.Builder builder = this.genInfoRespBuilder(playerId);
		if(builder != null){
			PlayerPushHelper.getInstance().pushToPlayer(playerId,
					HawkProtocol.valueOf(HP.code2.MERGE_ANNOUNCE_RESP, builder));
		}
		
	}
	
	
	private int getMergeId(){
		String serverId = this.getDataGeter().getServerId();
		long curTime = HawkTime.getMillisecond();
		MergeAnnounceKVCfg kvcfg = HawkConfigManager.getInstance()
				.getKVInstance(MergeAnnounceKVCfg.class);
		List<MergeServerTimeCfg> list = HawkConfigManager.getInstance()
				.getConfigIterator(MergeServerTimeCfg.class).toList();
		for(MergeServerTimeCfg mergeCfg : list){
			if(mergeCfg.getMergeType() ==0){
				continue;
			}
			if(!mergeCfg.getMergeServerList().contains(serverId)){
				continue;
			}
			if(curTime > mergeCfg.getMergeTimeValue()){
				continue;
			}
			if(curTime < (mergeCfg.getMergeTimeValue() - kvcfg.getNoticeTime()*1000L)){
				continue;
			}
			return mergeCfg.getId();
		}
		return 0;
	}
	
	private MergeAnnounceInfo.Builder genInfoRespBuilder(String playerId){
		MergeServerTimeCfg cfg = HawkConfigManager.getInstance()
				.getConfigByKey(MergeServerTimeCfg.class, this.mergeId);
		if(cfg == null){
			return null;
		}
		int mergeType = cfg.getMergeType();
		if(mergeType == 1){
			//可拆分和服
			String serverId = this.getDataGeter().getServerId();
			MergeAnnounceInfo.Builder builder = MergeAnnounceInfo.newBuilder();
			builder.setMergeType(mergeType);
			builder.setServerId(serverId);
			builder.addMergeServers(cfg.getMasterServer());
			builder.addMergeServers(cfg.getSlaveServerIdList().get(0));
			builder.setMergeTime(cfg.getMergeTimeValue());
			return builder;
		}else if(mergeType == 2){
			//可拆分拆服
			String serverId = this.getDataGeter().getPlayerServerId(playerId);
			MergeAnnounceInfo.Builder builder = MergeAnnounceInfo.newBuilder();
			builder.setMergeType(mergeType);
			builder.setServerId(serverId);
			builder.addMergeServers(cfg.getMasterServer());
			builder.setMergeTime(cfg.getMergeTimeValue());
			return builder;
		}
		return null;
	}
	
	
}
