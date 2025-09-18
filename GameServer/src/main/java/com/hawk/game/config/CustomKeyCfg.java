package com.hawk.game.config;

import java.util.LinkedList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.Const;

/**
 * 自定义key
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/custom_keys.xml")
public class CustomKeyCfg extends HawkConfigBase {
	protected final int id;
	@Id
	protected final String des;
	// 是否是idip红点控制开关：0否,非0是
	protected final int idipOpen;

	private static String tutorialKey;
	private static String tutorialCompleteKey;
	private static String newlyDataStateKey;
	private static String trainQuantityAddKey;
	private static String advanceQuantityAddKey;
	private static String laboratoryRouteNameKey;
	
	private static List<CustomKeyCfg> idipRedDotSwitchKeys = new LinkedList<CustomKeyCfg>();
	
	public CustomKeyCfg() {
		id = 0;
		des = "";
		idipOpen = 0;
	}

	public int getId() {
		return id;
	}

	public String getKey() {
		return des;
	}
	
	public int getIdipRedDotType() {
		return idipOpen;
	}

	@Override
	public boolean assemble() {
		switch (id) {
		case 20:
			tutorialCompleteKey = des;
			break;
		case 21:
			tutorialKey = des;
			break;
		case 42:
			newlyDataStateKey = des;
			break;
		case 49:
			trainQuantityAddKey = des;
			break;
		case 62:
			advanceQuantityAddKey = des;
			break;
		case 65:
			laboratoryRouteNameKey = des;
			break;
		} 
		
		if (idipOpen > 0) {
			idipRedDotSwitchKeys.add(this);
		}

		return true;
	}
	
	/**取得单次训练加速key*/
	public static String getQuantityAddKey(int itemType) {
		String key = "";
		if (itemType == Const.ToolType.TRAIN_QUANTITY_ADD_ONCE_VALUE) {
			key = CustomKeyCfg.getTrainQuantityAddKey();
		} else if (itemType == Const.ToolType.ADVANCE_QUANTITY_ADD_ONCE_VALUE) {
			key = CustomKeyCfg.getAdvanceQuantityAddKey();
		}
		return key;
	}

	public static String getTutorialKey() {
		return tutorialKey;
	}

	public static String getTutorialCompleteKey() {
		return tutorialCompleteKey;
	}

	public static String getNewlyDataStateKey() {
		if (HawkOSOperator.isEmptyString(newlyDataStateKey)) {
			newlyDataStateKey = "newlyDataState";
		}

		return newlyDataStateKey;
	}

	public static String getTrainQuantityAddKey() {
		return trainQuantityAddKey;
	}

	public static String getAdvanceQuantityAddKey() {
		return advanceQuantityAddKey;
	}
	
	public static String getLaboratoryRouteNameKey() {
		return laboratoryRouteNameKey;
	}
	
	public static List<CustomKeyCfg> getIdipRedDotSwitchKeys() {
		return idipRedDotSwitchKeys;
	}
}
