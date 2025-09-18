package com.hawk.activity.type.impl.plantSoldierFactory.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.ArrayList;
import java.util.List;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/plant_soldier_factory/plant_soldier_factory_group.xml")
public class PlantSoldierFactoryGroupCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final String connectionLimit;
    private final int group;

    private List<Integer> connectionList = new ArrayList<>();

    public PlantSoldierFactoryGroupCfg(){
        this.id = 0;
        this.connectionLimit = "";
        this.group = 0;
    }

    @Override
    protected boolean assemble() {
        this.connectionList = SerializeHelper.stringToList(Integer.class, this.connectionLimit, SerializeHelper.BETWEEN_ITEMS);
        return true;
    }

    public int getId(){
        return this.id;
    }

    public String getConnectionLimit(){
        return this.connectionLimit;
    }

    public int getGroup(){
        return this.group;
    }

    public List<Integer> getConnectionList() {
        return connectionList;
    }
}