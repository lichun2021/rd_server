package com.hawk.activity.type.impl.hotBloodWar.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.serialize.string.SerializeHelper;

/**
 * @author richard
 */
@HawkConfigManager.XmlResource(file = "activity/hot_blood_war/%s/hot_blood_war_point.xml", autoLoad=false, loadParams="378")
public class HotBloodWarPointCfg extends HawkConfigBase {
    /**
     * 唯一ID 
     * <data Id="37800001" type="1" target="100" point="1" addPointSoldierID="100101,100301,100501,100701" />
     */
    @Id
    private final int id;

    /**
     * 1击杀敌军 2自损
     */
    private final int type;
    /**
     * 兑换获得物品
     */
    private final String addPointSoldierID;

    /**
     * 数量
     */
    private final int target;
    
    /**
     * 积分
     */
    private final int point;
    
    //类型-兵ID-配置ID
    private static Table<Integer,Integer,Integer> pointTab = HashBasedTable.create();
   

    public HotBloodWarPointCfg() {
        id = 0;
        type = 0;
        addPointSoldierID = "";
        target = 0;
        point = 0;
    }

    @Override
    protected boolean assemble() {
    	List<Integer> idList = SerializeHelper.stringToList(Integer.class, this.addPointSoldierID,SerializeHelper.BETWEEN_ITEMS);
    	for(int sid : idList){
    		pointTab.put(this.type, sid, this.id);
    	}
    	return true;
    }
    
    public int getId() {
        return id;
    }


    
    public int getType() {
		return type;
	}
    
    public int getTarget() {
		return target;
	}
    
    public int getPoint() {
		return point;
	}
    

    
    @Override
    protected final boolean checkValid() {
        return super.checkValid();
    }
    
    public static int getPointConfigId(int type,int sid){
    	if(pointTab.contains(type, sid)){
    		return pointTab.get(type, sid);
    	}
    	return 0;
    }

}
