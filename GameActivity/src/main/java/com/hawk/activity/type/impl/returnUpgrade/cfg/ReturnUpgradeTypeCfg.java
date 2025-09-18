package com.hawk.activity.type.impl.returnUpgrade.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/return_upgrade/return_upgrade_type.xml")
public class ReturnUpgradeTypeCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int duration;
    private final String lossDays;
    private final String vip;

    private int lossDayMin;
    private int lossDayMax;

    private int vipMin;
    private int vipMax;

    public ReturnUpgradeTypeCfg(){
        this.id = 0;
        this.duration = 0;
        this.lossDays = "";
        this.vip = "";
    }

    @Override
    protected boolean assemble() {
        String [] lossDayArr = this.lossDays.split("_");
        String [] vipArr = this.vip.split("_");
        this.lossDayMin = Integer.valueOf(lossDayArr[0]);
        this.lossDayMax = Integer.valueOf(lossDayArr[1]);
        this.vipMin = Integer.valueOf(vipArr[0]);
        this.vipMax = Integer.valueOf(vipArr[1]);
        return true;
    }

    public int getId(){
        return this.id;
    }

    public int getDuration(){
        return this.duration;
    }

    public String getLossDays(){
        return this.lossDays;
    }

    public String getVip(){
        return this.vip;
    }

    public int getLossDayMin() {
        return lossDayMin;
    }

    public int getLossDayMax() {
        return lossDayMax;
    }

    public int getVipMin() {
        return vipMin;
    }

    public int getVipMax() {
        return vipMax;
    }

    public boolean isAdapt(BackFlowPlayer bplayer){
        int lossDays = bplayer.getLossDays();
        int vipLevel = bplayer.getVipLevel();
        if(vipLevel >= this.getVipMin() &&
                vipLevel <= this.getVipMax() &&
                lossDays >= this.getLossDayMin() &&
                lossDays <= this.getLossDayMax()){
            return true;
        }
        return false;
    }
}