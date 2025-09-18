package com.hawk.game.module.homeland.entity;

import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.collection.ConcurrentHashSet;

import java.util.Set;

public class HLAttrComp implements SerializJsonStrAble {
    private Set<Integer> activeProsperityAttrSet = new ConcurrentHashSet<>();

    /**
     * 序列化
     */
    @Override
    public String serializ() {
        return SerializeHelper.collectionToString(activeProsperityAttrSet, SerializeHelper.BETWEEN_ITEMS);
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        activeProsperityAttrSet = SerializeHelper.stringToSet(Integer.class, serialiedStr, SerializeHelper.BETWEEN_ITEMS);
    }

    public Set<Integer> getActiveProsperityAttrSet() {
        return activeProsperityAttrSet;
    }
}
