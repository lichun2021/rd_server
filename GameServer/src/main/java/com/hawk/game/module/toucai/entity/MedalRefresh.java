package com.hawk.game.module.toucai.entity;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.toucai.cfg.MedalFactoryConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryTanXunList;

public class MedalRefresh {
	private List<String> rand = new ArrayList<>();
	private List<String> enemy = new ArrayList<>();
	private List<String> friend = new ArrayList<>();

	public void mergeFrom(String string) {
		try {

			JSONObject obj = JSONObject.parseObject(string);
			for (Object str : obj.getJSONArray("rand")) {
				rand.add(str.toString());
			}
			for (Object str : obj.getJSONArray("enemy")) {
				enemy.add(str.toString());
			}

			for (Object str : obj.getJSONArray("friend")) {
				friend.add(str.toString());
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("rand", rand);
		obj.put("enemy", enemy);
		obj.put("friend", friend);
		return obj.toJSONString();
	}

	public List<String> getRand() {
		return rand;
	}

	public void setRand(List<String> rand) {
		this.rand = rand;
	}

	public List<String> getEnemy() {
		return enemy;
	}

	public void setEnemy(List<String> enemy) {
		this.enemy = enemy;
	}

	public List<String> getFriend() {
		return friend;
	}

	public void setFriend(List<String> friend) {
		this.friend = friend;
	}

	public boolean isEmpty() {
		return rand.isEmpty() && enemy.isEmpty() && friend.isEmpty();
	}

	public void sync(Player player) {
		HPMedalFactoryTanXunList.Builder resp = HPMedalFactoryTanXunList.newBuilder();
		for (String pid : getRand()) {
			try {
				Player tar = GlobalData.getInstance().makesurePlayer(pid);
				if (tar != null) {
					resp.addRand(tar.getData().getMedalEntity().getFactoryObj().toHP());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		for (String pid : getEnemy()) {
			try {
				Player tar = GlobalData.getInstance().makesurePlayer(pid);
				if (tar != null) {
					resp.addEnemy(tar.getData().getMedalEntity().getFactoryObj().toHP());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		for (String pid : getFriend()) {
			try {
				Player tar = GlobalData.getInstance().makesurePlayer(pid);
				if (tar != null) {
					resp.addFriend(tar.getData().getMedalEntity().getFactoryObj().toHP());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		MedalFactoryConstCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MedalFactoryConstCfg.class);
		MedalEntity entity = player.getData().getMedalEntity();
		resp.setRefresh(kvCfg.getRefreshNum() - entity.getDailyRefresh());
		resp.setRefreshCool(entity.getRefreshCool());

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TANXUN_S, resp));
	}

}
