package com.hawk.game.tsssdk.invoker;

import java.util.UUID;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.HPWorldFavoriteSync;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.WORLD_FAVORITE_ADD)
public class WorldFavoriteAddInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String builderValue) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		WorldFavoritePB.Builder favoriteBuilder = WorldFavoritePB.newBuilder();
		try {
			JsonFormat.merge(builderValue, favoriteBuilder);
		} catch (ParseException e) {
			HawkException.catchException(e);
			return 0;
		}
		
		favoriteBuilder.setFavoriteId(UUID.randomUUID().toString().replace("-", ""));
		favoriteBuilder.setUpdateTime(HawkTime.getMillisecond());
		if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
			favoriteBuilder.setPfIcon(player.getPfIcon());
		}
		int expireTime = 0;
		//跨服玩家,活动时间多加上几天, 如果不设置过期会让玩家的数据死在redis里面.
		if (player.isCsPlayer()) {
			expireTime = CrossActivityService.getInstance().getCrossKeyExpireTime();
		}
		LocalRedis.getInstance().addWorldFavorite(player.getId(), favoriteBuilder, expireTime);
		
		// 同步
		HPWorldFavoriteSync.Builder builder = HPWorldFavoriteSync.newBuilder();
		builder.addFavorites(favoriteBuilder);
		builder.setSynType(1);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_FAVORITE_SYNC_S, builder));
		
		// 提示添加成功
		player.responseSuccess(protocol);
		
		LogUtil.logSecTalkFlow(player, null, LogMsgType.WORLD_FAVORITE_ADD, favoriteBuilder.getFavoriteId(), name);
		
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_ADD_FAVORITE, Params.valueOf("tag", favoriteBuilder.getTag()),
				Params.valueOf("type", favoriteBuilder.getType()), Params.valueOf("name", favoriteBuilder.getName()), Params.valueOf("serverId", favoriteBuilder.getServerId()),
				Params.valueOf("posX", favoriteBuilder.getPosX()), Params.valueOf("posY", favoriteBuilder.getPosY()));
		
		return 0;
	}

}
