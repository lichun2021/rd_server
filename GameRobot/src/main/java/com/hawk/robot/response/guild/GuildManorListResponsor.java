package com.hawk.robot.response.guild;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.GuildManor.GuildManorBase;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.GuildManor.GuildManorStat;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 *
 * @author zhenyu.shang
 * @since 2017年8月10日
 */
@RobotResponse(code = HP.code.GUILD_MANOR_LIST_S_VALUE)
public class GuildManorListResponsor extends RobotResponsor{

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GuildManorList list = protocol.parseProtocol(GuildManorList.getDefaultInstance());
		String guildId = robotEntity.getGuildId();
		if(guildId != null){
			//判断领地建成, 构建领地占用点
			List<GuildManorBase> manors = list.getAllManorList();
			if(manors != null && !manors.isEmpty()){
				for (int i = 0; i < manors.size(); i++) {
					GuildManorBase current = manors.get(i);
					//判断领地状是否是已经完成，完成则刷新领地范围
					if(isComplete(current.getStat())){
						WorldDataManager.getInstance().addManorPoints(current.getX(), current.getY());
					}
				}
			}
			WorldDataManager.getInstance().refreshGuildManorList(guildId, list);
		}
	}
	
	public boolean isComplete(GuildManorStat stat){
		return stat == GuildManorStat.BREAKING_M || stat == GuildManorStat.DAMAGED_M
				|| stat == GuildManorStat.GARRISON_M || stat == GuildManorStat.REPAIRING_M
				|| stat == GuildManorStat.UNGARRISON_M;
	}

}
