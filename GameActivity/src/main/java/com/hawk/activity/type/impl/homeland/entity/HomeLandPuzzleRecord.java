package com.hawk.activity.type.impl.homeland.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.ActivityHomeLandPuzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeLandPuzzleRecord {
    //获得时间
    private long time;
    //普通记录
    Map<Integer, HomeLandPuzzleRecordItem> normal = new HashMap<>();
    //组合记录
    List<HomeLandPuzzleRecordItem> combine = new ArrayList<>();
    //大奖记录
    List<HomeLandPuzzleRecordItem> prize = new ArrayList<>();

    public void addNewRecord(HomeLandPuzzlePoolItem poolItem, int combines, int prizes, String reward) {
        HomeLandPuzzleRecordItem recordItem = null;
        switch (poolItem.getType()) {
            case NORMAL:
                recordItem = normal.computeIfAbsent(poolItem.getId(), k -> HomeLandPuzzleRecordItem.valueOf(poolItem.getId(), 0));
                break;
            case COMBINATION:
                recordItem = HomeLandPuzzleRecordItem.valueOf(poolItem.getId(), 0);
                combine.add(recordItem);
                break;
            case GRAND_PRIZE:
                recordItem = HomeLandPuzzleRecordItem.valueOf(poolItem.getId(), 0);
                prize.add(recordItem);
                break;
            default:
                break;
        }
        recordItem.setItemCount(recordItem.getItemCount() + 1);
        recordItem.setCombines(combines);
        recordItem.setPrize(prizes);
        recordItem.setReward(reward);
    }

    public static HomeLandPuzzleRecord valueOf(long time) {
        return new HomeLandPuzzleRecord(time);
    }

    public HomeLandPuzzleRecord(long time) {
        this.time = time;
    }

    public HomeLandPuzzleRecord() {
        this.time = 0;
    }

    public void mergeFrom(String jsonStr) {
        JSONObject aa = JSONObject.parseObject(jsonStr);
        if (aa == null) {
            return;
        }
        this.time = aa.getLong("time");
        JSONArray combine = aa.getJSONArray("combine");
        if (combine != null) {
            combine.forEach(str -> {
                HomeLandPuzzleRecordItem item = new HomeLandPuzzleRecordItem();
                item.mergeFrom(str.toString());
                this.combine.add(item);
            });
        }
        JSONArray normal = aa.getJSONArray("normal");
        if (normal != null) {
            normal.forEach(str -> {
                HomeLandPuzzleRecordItem item = new HomeLandPuzzleRecordItem();
                item.mergeFrom(str.toString());
                this.normal.put(item.getCfgId(), item);
            });
        }
        JSONArray prize = aa.getJSONArray("prize");
        if (prize != null) {
            prize.forEach(str -> {
                HomeLandPuzzleRecordItem item = new HomeLandPuzzleRecordItem();
                item.mergeFrom(str.toString());
                this.prize.add(item);
            });
        }
    }

    /**
     * 序列化
     */
    public Object serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("time", getTime());
        JSONArray combine = new JSONArray();
        this.combine.stream().map(HomeLandPuzzleRecordItem::serialize).forEach(combine::add);
        if (!combine.isEmpty()) {
            jsonObject.put("combine", combine);
        }
        JSONArray normal = new JSONArray();
        this.normal.values().stream().map(HomeLandPuzzleRecordItem::serialize).forEach(normal::add);
        if (!normal.isEmpty()) {
            jsonObject.put("normal", normal);
        }
        JSONArray prize = new JSONArray();
        this.prize.stream().map(HomeLandPuzzleRecordItem::serialize).forEach(prize::add);
        if (!prize.isEmpty()) {
            jsonObject.put("prize", prize);
        }
        return jsonObject.toJSONString();
    }

    public long getTime() {
        return time;
    }

    public ActivityHomeLandPuzzle.HomeLandPuzzleRecordPB buildRecord() {
        ActivityHomeLandPuzzle.HomeLandPuzzleRecordPB.Builder builder = ActivityHomeLandPuzzle.HomeLandPuzzleRecordPB.newBuilder();
        builder.setTime(this.time);
        normal.values().forEach(v -> builder.addNormal(v.buildItem()));
        combine.forEach(v -> builder.addCombine(v.buildItem()));
        prize.forEach(v -> builder.addPrize(v.buildItem()));
        return builder.build();
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Map<Integer, HomeLandPuzzleRecordItem> getNormal() {
        return normal;
    }

    public List<HomeLandPuzzleRecordItem> getCombine() {
        return combine;
    }

    public List<HomeLandPuzzleRecordItem> getPrize() {
        return prize;
    }
}
