package com.hawk.game.module.lianmengyqzz.march.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.Const.NoticeCfgId;


/**
 * 跨服活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/moon_war_stage.xml")
public class YQZZBattleStageTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int stageId;

	/** 开启时间 */
	private final int stageStartTime;

	/** 结束时间 */
	private final int stageEndTime;

	/** 开启建筑*/
	private final String stageOpenBuild;
	
	private final int send;
	
	
	private List<Integer> stageOpenBuildList;
	

	public YQZZBattleStageTimeCfg() {
		stageId = 0;
		stageStartTime = 0;
		stageEndTime = 0;
		stageOpenBuild = "";
		send = 0;
	}
	
	public int getStageId() {
		return stageId;
	}
	
	public int getStageStartTime() {
		return stageStartTime;
	}
	
	public int getStageEndTime() {
		return stageEndTime;
	}
	
	public List<Integer> getStageOpenBuildList() {
		return stageOpenBuildList;
	}
	
	protected boolean assemble() {
		List<Integer> stageOpenBuilds = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.stageOpenBuild)){
			String[] conArr = this.stageOpenBuild.split(",");
			for(int i=0;i<conArr.length;i++){
				String str =  conArr[i];
				int val = Integer.parseInt(str.trim());
				stageOpenBuilds.add(val);
			}
		}
		this.stageOpenBuildList = ImmutableList.copyOf(stageOpenBuilds);
		return true;
	}

	@Override
	protected boolean checkValid() {
		if(this.stageEndTime < this.stageStartTime){
			return false;
		}
		return true;
	}
	
	public int getSend() {
		return send;
	}

	public NoticeCfgId getNoticeId(){
		switch (this.stageId) {
		case 2: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_2;
		case 3: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_3;
		case 4: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_4;
		case 5: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_5;
		case 6: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_6;
		case 7: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_7;
		case 8: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_8;
		case 9: return NoticeCfgId.YQZZ_BATTLE_STAGE_START_9;
		default:return null;
		}
		
	}
}
