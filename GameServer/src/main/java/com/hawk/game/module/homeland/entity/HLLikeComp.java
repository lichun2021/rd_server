package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.HomeLand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HLLikeComp implements SerializJsonStrAble {
    private final Map<String, HomeLandDailyLikes> dailyLikeList = new ConcurrentHashMap<>();

    /**
     * 序列化
     */
    @Override
    public String serializ() {
        JSONArray arr = new JSONArray();
        dailyLikeList.values().stream().map(HomeLandDailyLikes::serializ).forEach(arr::add);
        return arr.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        JSONArray arr = JSONArray.parseArray(serialiedStr);
        if (arr == null) {
            return;
        }
        arr.forEach(str -> {
            HomeLandDailyLikes like = new HomeLandDailyLikes();
            like.mergeFrom(str.toString());
            dailyLikeList.put(like.getPlayerId(), like);
        });
    }

    public HomeLand.HomeLandThemeLikePush.Builder buildLikePush(boolean liked, int like) {
        HomeLand.HomeLandThemeLikePush.Builder builder = HomeLand.HomeLandThemeLikePush.newBuilder();
        builder.setLikeInfo(buildLikePb(liked, like));
        return builder;
    }

    public HomeLand.HomeLandThemeLikePB.Builder buildLikePb(boolean liked, int like) {
        HomeLand.HomeLandThemeLikePB.Builder builder = HomeLand.HomeLandThemeLikePB.newBuilder();
        builder.setLikes(like);
        builder.setLiked(liked);
        return builder;
    }

    public Map<String, HomeLandDailyLikes> getDailyLikeList() {
        return dailyLikeList;
    }
}
