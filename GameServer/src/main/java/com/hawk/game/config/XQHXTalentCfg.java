package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.entity.PlayerXQHXTalentEntity;
import com.hawk.game.protocol.Const;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/xqhx_talent.xml")
public class XQHXTalentCfg extends HawkConfigBase {
    @Id
    private final int id;
    private final String frontTalent;

    
    private Map<Integer, Integer> frontMapAnd = new HashMap<>();
    private Map<Integer, Integer> frontMapOr = new HashMap<>();
    
    public XQHXTalentCfg(){
        this.id = 0;
        this.frontTalent = "";
    }

    @Override
    protected boolean assemble() {
    	 Map<Integer, Integer> frontMapTmp = new HashMap<>();
    	 String split = SerializeHelper.SEMICOLON_ITEMS;
         if (!HawkOSOperator.isEmptyString(this.frontTalent)) {
        	 if(!this.frontTalent.contains(split)){
        		 split = SerializeHelper.ELEMENT_SPLIT;
        	 }
             String[] array = this.frontTalent.split(split);
             for (String val : array) {
                 String[] info = val.split(SerializeHelper.ATTRIBUTE_SPLIT);
                 frontMapTmp.put(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
             }
         }
         if(SerializeHelper.SEMICOLON_ITEMS.equals(split)){
        	  this.frontMapAnd = ImmutableMap.copyOf(frontMapTmp);
         }else{
        	 this.frontMapOr = ImmutableMap.copyOf(frontMapTmp);
         }
        return true;
    }

    public int getId() {
        return id;
    }

    public String getFrontTalent() {
        return frontTalent;
    }
    
    
    public boolean checkFront(List<PlayerXQHXTalentEntity> talents){
    	if(this.frontMapAnd.isEmpty() && this.frontMapOr.isEmpty()){
    		return true;
    	}
    	Map<Integer,Integer> tmap = new HashMap<>();
    	talents.forEach(t->tmap.put(t.getTalentId(), t.getLevel()));
    	//且  只要有一个不行  就直接返回false
    	if(this.frontMapAnd.size() >0){
    		for(Map.Entry<Integer,Integer> front : this.frontMapAnd.entrySet()){
        		int ft = front.getKey();
        		int fl = front.getValue();
        		int cur = tmap.getOrDefault(ft, 0);
        		if(cur < fl){
        			return false;
        		}
        	}
    		return true;
    	}
    	//或
    	if(this.frontMapOr.size() > 0){
    		for(Map.Entry<Integer,Integer> front : this.frontMapOr.entrySet()){
        		int ft = front.getKey();
        		int fl = front.getValue();
        		int cur = tmap.getOrDefault(ft, 0);
        		if(cur >= fl){
        			return true;
        		}
        	}
    		return false;
    	}
    	return true;
    }
}
