package com.hawk.game.invoker;

import java.util.Set;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;

import com.hawk.game.city.CityManager;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

public class CityBrokenMoveInvoker extends HawkMsgInvoker {
	
	private Player player;
	private Long cityBrokenTime;
	private Set<String> removeCityPlayers;
	private boolean forced;
	
	public CityBrokenMoveInvoker(Player player, Long cityBrokenTime, Set<String> removeCityPlayers, boolean forced) {
		this.player = player;
		this.cityBrokenTime = cityBrokenTime;
		this.removeCityPlayers = removeCityPlayers;
		this.forced = forced;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		if (!forced) {
			//注意，这里再判断一次，是因为之前的判断是在世界线程处理，防止玩家并发操作，这里在玩家队列需要再次判断一下时间
			if(cityBrokenTime == null){
				removeCityPlayers.remove(player.getId());
				return false;
			}
			
			if(HawkTime.getMillisecond() < cityBrokenTime) {
				removeCityPlayers.remove(player.getId());
				return false;
			}
		}
		
		CityManager.getInstance().removeCity(player);
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.SYNC_CITY_DEF) {
			@Override
			public boolean onInvoke() {
				WorldPlayerService.getInstance().moveCity(player);
				return true;
			}
		});
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public Long getCityBrokenTime() {
		return cityBrokenTime;
	}

	public Set<String> getRemoveCityPlayers() {
		return removeCityPlayers;
	}

	public boolean isForced() {
		return forced;
	}
	
}
