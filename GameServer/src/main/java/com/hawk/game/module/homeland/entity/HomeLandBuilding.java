package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.homeland.cfg.HomeLandBuildingCfg;
import com.hawk.game.module.homeland.cfg.HomeLandBuildingTypeCfg;
import com.hawk.game.module.homeland.cfg.HomeLandConstKVCfg;
import com.hawk.game.module.homeland.map.IHomeLandPoint;
import com.hawk.game.protocol.HomeLand.HomeLandBuildingPB;
import com.hawk.game.util.GameUtil;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

public class HomeLandBuilding implements IHomeLandPoint {
    private String uid;           //建筑的唯一实例ID
    private int configId;       //配置ID，对应静态配置表
    private int x;              //网格X坐标
    private int y;              //网格Y坐标
    private long lastHarvestTime; //上次收取资源的时间戳
    private int buildType; //建筑类型
    private int width; //宽
    private int height; //高

    public boolean isMainBuild() {
        HomeLandConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandConstKVCfg.class);
        return buildType == cfg.getMainBuildType();
    }

    public static HomeLandBuilding valueOf(int cfgId) {
        HomeLandBuilding building = new HomeLandBuilding();
        building.setUid(HawkOSOperator.randomUUID());
        building.setConfigId(cfgId);
        building.setLastHarvestTime(HawkTime.getMillisecond());
        HomeLandBuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, cfgId);
        if (buildingCfg != null) {
            building.setBuildType(buildingCfg.getBuildType());
            HomeLandBuildingTypeCfg buildingTypeCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingTypeCfg.class, buildingCfg.getBuildType());
            if (buildingTypeCfg != null) {
                building.setWidth(buildingTypeCfg.getWidth());
                building.setHeight(buildingTypeCfg.getHeight());
            }
        }
        return building;
    }

    public HomeLandBuildingPB toPB() {
        HomeLandBuildingPB.Builder builder = HomeLandBuildingPB.newBuilder();
        builder.setBuildCfgId(configId);
        builder.setUuid(uid);
        builder.setX(x);
        builder.setY(y);
        builder.setLastHarvestTime(lastHarvestTime);
        return builder.build();
    }

    public void mergeFrom(String jsonStr) {
        HomeLandBuilding result = JSON.parseObject(jsonStr, HomeLandBuilding.class);
        this.uid = result.getUid();
        this.lastHarvestTime = result.getLastHarvestTime();
        this.configId = result.getConfigId();
        this.x = result.getX();
        this.y = result.getY();
        if (this.getBuildCfg() != null) {
            this.buildType = this.getBuildCfg().getBuildType();
            HomeLandBuildingTypeCfg buildingTypeCfg = this.getBuildTypeCfg(this.buildType);
            if (buildingTypeCfg != null) {
                this.width = buildingTypeCfg.getWidth();
                this.height = buildingTypeCfg.getHeight();
            }
        }
    }

    /**
     * 序列化
     */
    public String serialize() {
        JSONObject obj = new JSONObject();
        obj.put("uid", this.getUid());
        obj.put("configId", this.getConfigId());
        obj.put("lastHarvestTime", this.getLastHarvestTime());
        obj.put("x", this.getX());
        obj.put("y", this.getY());
        return obj.toString();
    }

    public String getUid() {
        return uid;
    }

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public void setUid(String uuid) {
        this.uid = uuid;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int getPointId() {
        return GameUtil.combineXAndY(x, y);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getLastHarvestTime() {
        return lastHarvestTime;
    }

    public void setLastHarvestTime(long lastHarvestTime) {
        this.lastHarvestTime = lastHarvestTime;
    }

    public HomeLandBuildingCfg getBuildCfg() {
        return HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, this.configId);
    }

    public HomeLandBuildingTypeCfg getBuildTypeCfg(int buildType) {
        return HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingTypeCfg.class, buildType);
    }

    public int getBuildType() {
        return buildType;
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
