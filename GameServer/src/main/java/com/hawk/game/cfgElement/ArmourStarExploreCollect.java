package com.hawk.game.cfgElement;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ArmourStarExploreCollectCfg;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Armour;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmourStarExploreCollect {
    private int collectId;
    private int upCount;

    private Map<Integer, Integer> fixAttrMap = new HashMap<>();
    private Map<Integer, Integer> randomAttrMap = new HashMap<>();
    private Map<Integer, Integer> tmpAttrMap = new HashMap<>();

    public JSONObject serialize() {
        JSONObject obj = new JSONObject();
        obj.put("collectId", collectId);
        obj.put("upCount", upCount);

        JSONArray fixAttr = new JSONArray();
        for (Map.Entry<Integer, Integer> info : fixAttrMap.entrySet()) {
            JSONObject infoObj = new JSONObject();
            infoObj.put("attrId", info.getKey());
            infoObj.put("attrVal", info.getValue());
            fixAttr.add(infoObj);
        }
        obj.put("fixAttr", fixAttr);

        JSONArray randomAttr = new JSONArray();
        for (Map.Entry<Integer, Integer> info : randomAttrMap.entrySet()) {
            JSONObject infoObj = new JSONObject();
            infoObj.put("attrId", info.getKey());
            infoObj.put("attrVal", info.getValue());
            randomAttr.add(infoObj);
        }
        obj.put("randomAttr", randomAttr);

        JSONArray tmpAttr = new JSONArray();
        for (Map.Entry<Integer, Integer> info : tmpAttrMap.entrySet()) {
            JSONObject infoObj = new JSONObject();
            infoObj.put("attrId", info.getKey());
            infoObj.put("attrVal", info.getValue());
            tmpAttr.add(infoObj);
        }
        obj.put("tmpAttr", tmpAttr);
        return obj;
    }

    public static ArmourStarExploreCollect unSerialize(JSONObject json) {
        ArmourStarExploreCollect collect = new ArmourStarExploreCollect();
        collect.setCollectId(json.getIntValue("collectId"));
        collect.setUpCount(json.getIntValue("upCount"));

        Map<Integer, Integer> fixAttrMap = new HashMap<>();
        JSONArray fixAttr = json.getJSONArray("fixAttr");
        for (int i = 0; i < fixAttr.size(); i++) {
            JSONObject infoObj = fixAttr.getJSONObject(i);
            int attrId = infoObj.getIntValue("attrId");
            int attrVal = infoObj.getIntValue("attrVal");
            fixAttrMap.put(attrId, attrVal);
        }
        collect.setFixAttrMap(fixAttrMap);

        Map<Integer, Integer> randomAttrMap = new HashMap<>();
        JSONArray randomAttr = json.getJSONArray("randomAttr");
        for (int i = 0; i < randomAttr.size(); i++) {
            JSONObject infoObj = randomAttr.getJSONObject(i);
            int attrId = infoObj.getIntValue("attrId");
            int attrVal = infoObj.getIntValue("attrVal");
            randomAttrMap.put(attrId, attrVal);
        }
        collect.setRandomAttrMap(randomAttrMap);

        Map<Integer, Integer> tmpAttrMap = new HashMap<>();
        if(json.containsKey("tmpAttr")){
            JSONArray tmpAttr = json.getJSONArray("tmpAttr");
            for (int i = 0; i < tmpAttr.size(); i++) {
                JSONObject infoObj = tmpAttr.getJSONObject(i);
                int attrId = infoObj.getIntValue("attrId");
                int attrVal = infoObj.getIntValue("attrVal");
                tmpAttrMap.put(attrId, attrVal);
            }
        }
        collect.setTmpAttrMap(tmpAttrMap);
        return collect;
    }

    public Armour.ArmourStarExploreCollectInfo.Builder toPB() {
        Armour.ArmourStarExploreCollectInfo.Builder builder = Armour.ArmourStarExploreCollectInfo.newBuilder();
        builder.setId(collectId);
        for (Map.Entry<Integer, Integer> attrInfo : fixAttrMap.entrySet()) {
            Armour.ArmourStarExploreAttrInfo.Builder attrBuilder = Armour.ArmourStarExploreAttrInfo.newBuilder();
            attrBuilder.setAttrIndex(attrInfo.getKey());
            attrBuilder.setAttrValue(attrInfo.getValue());
            builder.addFixedInfo(attrBuilder);
        }
        for (Map.Entry<Integer, Integer> attrInfo : randomAttrMap.entrySet()) {
            Armour.ArmourStarExploreAttrInfo.Builder attrBuilder = Armour.ArmourStarExploreAttrInfo.newBuilder();
            attrBuilder.setAttrIndex(attrInfo.getKey());
            attrBuilder.setAttrValue(attrInfo.getValue());
            builder.addRandomInfo(attrBuilder);
        }
        for (Map.Entry<Integer, Integer> attrInfo : tmpAttrMap.entrySet()) {
            Armour.ArmourStarExploreAttrInfo.Builder attrBuilder = Armour.ArmourStarExploreAttrInfo.newBuilder();
            attrBuilder.setAttrIndex(attrInfo.getKey());
            attrBuilder.setAttrValue(attrInfo.getValue());
            builder.addTmpInfo(attrBuilder);
        }
        return builder;
    }

    public int getCollectId() {
        return collectId;
    }

    public void setCollectId(int collectId) {
        this.collectId = collectId;
    }

    public int getUpCount() {
        return upCount;
    }

    public void setUpCount(int upCount) {
        this.upCount = upCount;
    }

    public Map<Integer, Integer> getFixAttrMap() {
        return fixAttrMap;
    }

    public void setFixAttrMap(Map<Integer, Integer> fixAttrMap) {
        this.fixAttrMap = fixAttrMap;
    }

    public Map<Integer, Integer> getRandomAttrMap() {
        return randomAttrMap;
    }

    public void setRandomAttrMap(Map<Integer, Integer> randomAttrMap) {
        this.randomAttrMap = randomAttrMap;
    }

    public Map<Integer, Integer> getTmpAttrMap() {
        return tmpAttrMap;
    }

    public void setTmpAttrMap(Map<Integer, Integer> tmpAttrMap) {
        this.tmpAttrMap = tmpAttrMap;
    }

    public ArmourStarExploreCollectCfg getCfg(){
        return HawkConfigManager.getInstance().getConfigByKey(ArmourStarExploreCollectCfg.class, collectId);
    }

    public static ArmourStarExploreCollect creat(int collectId){
        ArmourStarExploreCollect collect = new ArmourStarExploreCollect();
        collect.setCollectId(collectId);
        ArmourStarExploreCollectCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarExploreCollectCfg.class, collectId);
        collect.getFixAttrMap().put(cfg.getFixAttr().first, cfg.getFixAttr().second);
        collect.getRandomAttrMap().put(cfg.getRandomAttr().first, cfg.getRandomAttr().second);
        collect.getTmpAttrMap().put(cfg.getRandomAttr().first, cfg.getRandomAttr().second);
        return collect;
    }

    public void jump(Player player){
        ArmourStarExploreCollectCfg cfg = getCfg();
        if(cfg == null){
            return;
        }
        
        List<ItemInfo> resItems = ItemInfo.valueListOf(cfg.getConsume());
		final int XINGNENG = 802005; //星能id
		GameUtil.reduceByEffect(resItems, XINGNENG, player.getEffect().getEffValArr(EffType.EFF_367818));
        ConsumeItems consume = ConsumeItems.valueOf();
        consume.addConsumeInfo(resItems);
        if (!consume.checkConsume(player)) {
            return;
        }
        consume.consumeAndPush(player, Action.ARMOUR_STAR_EXPLORE_JUMP);
        upCount++;

        int fixedAttr = fixAttrMap.getOrDefault(cfg.getFixAttr().first, cfg.getFixAttr().second);
        fixedAttr += cfg.getFixAttrGrow().second;
        fixedAttr = Math.min(fixedAttr, cfg.getFixAttrLimit().second);
        fixAttrMap.put(cfg.getFixAttr().first, fixedAttr);

        if(upCount >= cfg.getRandomSkillFinal()){
            randomAttrMap.put(cfg.getRandomAttr().first, cfg.getRandomAttrRange().third);
            tmpAttrMap.put(cfg.getRandomAttr().first, cfg.getRandomAttrRange().third);
        }else {
            int randomAttr = randomAttrMap.getOrDefault(cfg.getRandomAttr().first, cfg.getRandomAttr().second);
            int tmp = HawkRand.randInt(cfg.getRandomAttrRange().second, cfg.getRandomAttrRange().third);
            tmp = Math.min(tmp, cfg.getRandomAttrRange().third);
            tmpAttrMap.put(cfg.getRandomAttr().first, tmp);
            if(tmp > randomAttr){
                randomAttr = tmp;
            }
            randomAttr = Math.min(randomAttr, cfg.getRandomAttrRange().third);
            randomAttrMap.put(cfg.getRandomAttr().first, randomAttr);
        }
    }

    public Map<Integer, Integer> getEffectMap(){
        Map<Integer, Integer> effMap = new HashMap<>();
        MapUtil.mergeMap(effMap, fixAttrMap);
        MapUtil.mergeMap(effMap, randomAttrMap);
        return effMap;
    }
}
