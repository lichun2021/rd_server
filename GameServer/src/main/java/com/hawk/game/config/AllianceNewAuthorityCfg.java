package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.GuildManager.AuthId;

/**
 * 联盟权限配置(新)
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_new_authority.xml")
public class AllianceNewAuthorityCfg extends HawkConfigBase {
	//<data id="33" initAuthority="1_2_3_4_5" amendAuthority="1_2" />
	@Id
	private final int id;
	// 初始权限
	private final String initAuthority;
	
	// 权限修改范围
	private final String amendAuthority;
	
	// 初始权限等级列表
	private List<Integer> initList;
	
	// 可被修改的权限列表
	private List<Integer> canEditList;
	

	public AllianceNewAuthorityCfg() {
		id = 0;
		initAuthority = "";
		amendAuthority = "";
	}

	public int getId() {
		return id;
	}
	
	/**
	 * 获取初始化权限等级列表
	 * @return
	 */
	public List<Integer> getInitList() {
		List<Integer> copy = new ArrayList<>();
		copy.addAll(this.initList);
		return copy;
	}
	
	/**
	 * 获取可修改等级范围
	 * @return
	 */
	public List<Integer> getCanEditList() {
		List<Integer> copy = new ArrayList<>();
		copy.addAll(this.canEditList);
		return copy;
	}

	@Override
	protected boolean assemble() {
		List<Integer> initLst = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.initAuthority)) {
			for (String lvl : this.initAuthority.split("_")) {
				initLst.add(Integer.valueOf(lvl));
			}
		}
		initList = initLst;

		List<Integer> editList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.amendAuthority)) {
			for (String lvl : this.amendAuthority.split("_")) {
				editList.add(Integer.valueOf(lvl));
			}
		}
		canEditList = editList;
		return true;
	}


	@Override
	protected boolean checkValid() {
		AuthId authId = AuthId.valueOf(this.id);
		if(authId == null){
			HawkLog.errPrintln("alliance_new_authority check error, auth is not exist, id; {}", this.id);
			return false;
		}
		return true;
	}

}
