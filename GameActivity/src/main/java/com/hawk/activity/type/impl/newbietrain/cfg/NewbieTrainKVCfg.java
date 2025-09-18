package com.hawk.activity.type.impl.newbietrain.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.Activity.NoviceTrainType;

/**
 * 新兵作训
 */
@HawkConfigManager.KVResource(file = "activity/newbie_train/%s/newbie_train_cfg.xml", autoLoad=false, loadParams="334")
public class NewbieTrainKVCfg extends HawkConfigBase {
	/**
	 *  基地等级大于等于此等级才开启
	 */
    private final int buildMin;
    /**
     *  基地等于大于等于此等级才开启装备作训
     */
    private final int equipMin;
    /**
     *  开服时间晚于此时间的服务器才开启
     */
    private final String serverOpenTime;
    /**
     *  普通服务器高级招募多少次给1次英雄作训
     */
    private final int normalServerHero;
    /**
     * 专服服务器高级招募多少次给1次英雄作训
     */
    private final int specialServerHero;
    /**
     *  普通服务器装备打造多少次给1次装备作训
     */
    private final int normalServerEquip;
    /**
     *  专服服务器装备打造多少次给1次装备作训
     */
    private final int specialServerEquip;
    
    /**
     * 单次作训最大次数
     */
    private final int trainMax;
    /**
     * 作训记录数据条目上限
     */
    private final int recordMax;

    private long serverOpenTimeValue;

    public NewbieTrainKVCfg(){
        this.buildMin = 0;
        this.equipMin = 0;
        this.serverOpenTime = "";
        this.normalServerHero = 0;
        this.specialServerHero = 0;
        this.normalServerEquip = 0;
        this.specialServerEquip = 0;
        this.trainMax = 10;
        this.recordMax = 50;
    }
    
	public int getBuildMin() {
		return buildMin;
	}

	public int getEquipMin() {
		return equipMin;
	}

	public String getServerOpenTime() {
		return serverOpenTime;
	}

	public int getNormalServerHero() {
		return normalServerHero;
	}

	public int getSpecialServerHero() {
		return specialServerHero;
	}

	public int getNormalServerEquip() {
		return normalServerEquip;
	}

	public int getSpecialServerEquip() {
		return specialServerEquip;
	}
	
	public int getTrainMax() {
		return trainMax;
	}

	public int getRecordMax() {
		return recordMax;
	}

	@Override
    protected boolean assemble() {
		serverOpenTimeValue = HawkTime.parseTime(serverOpenTime);
		if (specialServerHero <= 0 || specialServerEquip <= 0 || normalServerHero <= 0 || normalServerEquip <= 0) {
			return false;
		}
		
		if (trainMax <= 0 || trainMax <= 0) {
			return false;
		}
        return true;
    }

	public long getServerOpenTimeValue() {
		return serverOpenTimeValue;
	}
	
	public int getGacha2TrainTimes(boolean specialServer, int trainType) {
		if (specialServer) {
			return trainType == NoviceTrainType.TYPE_HERO_VALUE ? specialServerHero : specialServerEquip;
		}
		
		return trainType == NoviceTrainType.TYPE_HERO_VALUE ? normalServerHero : normalServerEquip;
	}
}
