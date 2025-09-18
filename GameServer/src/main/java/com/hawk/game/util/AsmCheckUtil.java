package com.hawk.game.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.config.HawkConfigBase;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.allianceCarnival.AllianceCarnivalActivity;
import com.hawk.activity.type.impl.bannerkill.BannerKillActivity;
import com.hawk.activity.type.impl.bountyHunter.BountyHunterActivity;
import com.hawk.activity.type.impl.domeExchange.DomeExchangeActivity;
import com.hawk.activity.type.impl.domeExchangeTwo.DomeExchangeTwoActivity;
import com.hawk.activity.type.impl.doubleGift.DoubleGiftActivity;
import com.hawk.activity.type.impl.heroBackExchange.HeroBackExchangeActivity;
import com.hawk.activity.type.impl.inherit.InheritActivity;
import com.hawk.activity.type.impl.inheritNew.InheritNewActivity;
import com.hawk.activity.type.impl.lotteryDraw.LotteryDrawActivity;
import com.hawk.activity.type.impl.midAutumn.MidAutumnActivity;
import com.hawk.activity.type.impl.monthcard.MonthCardActivity;
import com.hawk.activity.type.impl.powercollect.rank.impl.GuildRank;
import com.hawk.activity.type.impl.recallFriend.RecallFriendActivity;
import com.hawk.activity.type.impl.recallFriend.data.RecalDataContent;
import com.hawk.activity.type.impl.rechargeWelfare.RechargeWelfareActivity;
import com.hawk.activity.type.impl.redkoi.RedkoiActivity;
import com.hawk.activity.type.impl.resourceDefense.ResourceDefenseActivity;
import com.hawk.activity.type.impl.rewardOrder.RewardOrderActivity;
import com.hawk.activity.type.impl.roulette.RouletteActivity;
import com.hawk.activity.type.impl.sendFlower.SendFlowerActivity;
import com.hawk.activity.type.impl.strongestGuild.cache.GuildData;
import com.hawk.activity.type.impl.strongestGuild.rank.impl.StrongestGuildTotalGuildRank;
import com.hawk.activity.type.impl.timeLimitBuy.TimeLimitBuyActivity;
import com.hawk.activity.type.impl.virtualLaboratory.VirtualLaboratoryActivity;
import com.hawk.activity.type.impl.warFlagTwo.WarFlagTwoActivity;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogTableCfg;
import com.hawk.log.Action;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

/**
 * 用于在起服的时候,通过遍历字节码,检测一些问题.
 * @author jm
 *
 */
public class AsmCheckUtil {
	
	public static void checkActivityCreateEntity() {
		List<Class<?>> clazzList = HawkClassScaner.getAllClasses("com.hawk.activity.type.impl");
		
		//!!!!!!此列表仅用于兼容老活动,新增活动禁止在此处添加白名单,entity创建函数仅提供进行entity创建!!!!!
		Set<Class<?>> whiteList = new HashSet<>(
				Arrays.asList(DomeExchangeTwoActivity.class, 
						MidAutumnActivity.class, 
						RedkoiActivity.class, 
						LotteryDrawActivity.class,
						ResourceDefenseActivity.class, 
						InheritNewActivity.class, 
						DomeExchangeActivity.class, 
						HeroBackExchangeActivity.class, 
						MonthCardActivity.class,
						BountyHunterActivity.class, 
						WarFlagTwoActivity.class, 
						DoubleGiftActivity.class, 
						RouletteActivity.class, 
						InheritActivity.class, 
						RecallFriendActivity.class,
						RechargeWelfareActivity.class, 
						RewardOrderActivity.class, 
						VirtualLaboratoryActivity.class, 
						AllianceCarnivalActivity.class, 
						BannerKillActivity.class));
		
		Set<Class<?>> synchronizedWhiteList = new HashSet<>(
				Arrays.asList(GuildRank.class, 
						StrongestGuildTotalGuildRank.class, 
						GuildData.class, 
						SendFlowerActivity.class, 
						RecalDataContent.class,
						AllianceCarnivalActivity.class,
						TimeLimitBuyActivity.class
				));
		
		Set<String> failedCls = new HashSet<>();
		for (Class<?> clazz : clazzList) {
			if (!synchronizedWhiteList.contains(clazz)) {
				for (Method method : clazz.getDeclaredMethods()) {
					String modifiers = Modifier.toString(method.getModifiers());
					if (modifiers.contains("synchronized")) {
						HawkLog.logPrintln("asm check method modifier synchronized, method: {}", method.toGenericString());
						throw new RuntimeException("activity class method synchronized detected");
					}
				}
			}
			
			if (whiteList.contains(clazz)) {
				continue;
			}
			
			if (!ActivityBase.class.isAssignableFrom(clazz) || clazz == ActivityBase.class) {
				continue;
			}
			
			boolean rlt = checkActivityCreateEntity(clazz.getName());
			if (!rlt) {
				failedCls.add(clazz.getName());
			}
		}
		
		if (!failedCls.isEmpty()) {
			throw new RuntimeException("ActivityBase createDataEntity  call method is unsupport:  " + failedCls);
		}
	}
	
	public static boolean checkActivityCreateEntity(String className) {
		AtomicInteger cnt = new AtomicInteger();
		try {
			ClassVisitor clsVisitor = new ClassVisitor(Opcodes.ASM5){
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {										
					if (name.equals("createDataEntity")) {
						return new CheckActivityCreateEntityMethodVistor(cnt);
					} 
						
					return super.visitMethod(access, name, desc, signature, exceptions);
				}
			};
			
			ClassReader clsReader = new ClassReader(className);
			clsReader.accept(clsVisitor, Opcodes.ASM5);
		} catch (Exception e) {
			HawkException.catchException(e);
		}				
		
		return cnt.get() <= 0;
	}
	
	static class CheckActivityCreateEntityMethodVistor extends MethodVisitor{
		AtomicInteger cnt;
		
		CheckActivityCreateEntityMethodVistor(AtomicInteger cnt) {
			super(Opcodes.ASM5);
			this.cnt = cnt;
		}
		
		/**
		 * 遍历方法调用.
		 */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			if (opcode == Opcodes.INVOKEDYNAMIC || 
				opcode == Opcodes.INVOKEINTERFACE || 
				opcode == Opcodes.INVOKEVIRTUAL || 
				opcode == Opcodes.INVOKESTATIC) {
				
				try {
					Class<?> ownerClass = Class.forName(owner.replace("/", "."));
					if (!HawkDBEntity.class.isAssignableFrom(ownerClass) && !HawkConfigBase.class.isAssignableFrom(ownerClass)) {
						cnt.incrementAndGet();
					}
				} catch (ClassNotFoundException e) {
					HawkException.catchException(e);
				}				
			}
		}			
	}
	
	
	/**
	 * playerDataKey做返回值校验
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void checkFunctionReturn() {
		EnumSet<PlayerDataKey> enumSet = EnumSet.allOf(PlayerDataKey.class);
		for (PlayerDataKey key : enumSet) {
			boolean isList = false;			
			Class<? extends HawkDBEntity> entityType;
			try {
				Method entityTypeMethod = null;
				Method listModeMethod = null;				
				for (Method method : key.getClass().getDeclaredMethods()) {
					if (method.getName().equals("entityType")) {
						entityTypeMethod = method; 
					} else if (method.getName().equals("listMode")) {
						listModeMethod = method;
					}
					
				}
				
				//如果entityType为空则不会被序列化
				if (entityTypeMethod == null) {
					continue;
				} else {
					entityTypeMethod.setAccessible(true);
					Object obj = entityTypeMethod.invoke(key);
					if (obj == null) {
						continue;
					}
					entityType = (Class<? extends HawkDBEntity>)obj;
				}			
				
				if (listModeMethod == null) {
					continue;
				} else {
					listModeMethod.setAccessible(true);
					Object obj = listModeMethod.invoke(key);
					isList = ((Boolean)obj).booleanValue();					
				}	
				
				checkFunctionReturn(key, isList, entityType);
			} catch (Exception e) {
				HawkException.catchException(e);
				throw new RuntimeException(e.getMessage());
			}			
		}
	}
	
	public static void checkFunctionReturn(PlayerDataKey key, boolean isList, Class<? extends HawkDBEntity> clazz) throws IOException {
		final ClassReader reader = new ClassReader(key.getClass().getName());
        final ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);
        
        String desc = "";
        if (isList) {
        	desc = Type.getDescriptor(List.class);
        } else {
        	desc = Type.getDescriptor(clazz);
        }
        
        for (final MethodNode mn : (List<MethodNode>)classNode.methods) {
        	if (!mn.name.equalsIgnoreCase("load")) {
        		continue;
        	}
        	
            List<LocalVariableNode> localVariableNodeList =  mn.localVariables;
            Map<Integer, LocalVariableNode> map = new HashMap<>();
            for (LocalVariableNode lvn : localVariableNodeList) {
                map.put(lvn.index, lvn);
            }
            
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
            	if (ain.getOpcode() != Opcodes.ARETURN) {
            		continue;
            	}
                
                AbstractInsnNode pre = ain;
                while (true) {
                    pre = pre.getPrevious();
                    if (pre == null) {
                        break;
                    }
                    
                    //碰见推送null到栈上的指令.
                    if (pre.getOpcode() == Opcodes.ACONST_NULL) {
                        break;
                    }
                    
                    //这里还有常量指令,所以也需要处理,只需要找到上一个aload即可.	                            
                    if (pre.getOpcode() == Opcodes.ALOAD ) {
                        int localIndex = ((VarInsnNode)pre).var;	                                
                        LocalVariableNode localLvn = map.get(localIndex);
                        if (!localLvn.desc.equals(desc)) {
                            throw new RuntimeException(String.format("playerDatakey %s method load return type %s, but it must be reutrn %s ", 
                            		key.name(), localLvn.desc, desc));	                                    
                        }
                        
                        break;
                    }
                }
            }
        }
	}
	
	public static List<String> showMethod(String className) {			
		List<String> strList = new ArrayList<>();
		try {
			ClassReader cr = new ClassReader(className);
			ClassVisitor cv = new ClassVisitor(Opcodes.ASM5){
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					strList.add("access="+access+", name="+name+", desc="+desc+", signature="+signature+",exceptions="+Arrays.deepToString(exceptions));
					
					return super.visitMethod(access, name, desc, signature, exceptions);
				}
			};
			cr.accept(cv, Opcodes.ASM5);
		} catch (Exception e) {
			HawkException.catchException(e);
		}				
		
		return strList;
	}
	
	public static  Set<String> checkUseEnumValues() {
		List<String> baseList = Arrays.asList("com.hawk.game", "com.hawk.activity");
		Set<String> useEnumClassSet = new HashSet<>();
		for (String basePath : baseList) {
			List<Class<?>> clazzList = HawkClassScaner.getAllClasses(basePath);			
			for (Class<?> clazz : clazzList) {
				try {
					if (clazz.getName().contains("com.hawk.game.protocol")) {
						continue;
					}
					
					//编译器自动生成的我们不管.
					if ((clazz.getModifiers() & Modifier.SYNCHRONIZED) > 0) {
						continue;
					}
					
					ClassReader cr = new ClassReader(clazz.getName());					
					ClassVisitor cv = new ClassVisitor(Opcodes.ASM5) {
						@Override
						public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
							if ((access & Opcodes.ACC_SYNTHETIC) > 0) {
								return null;
							} else {
								return new CheckUseEnumVisitor(clazz.getName() + "="+ name, useEnumClassSet);
							}							
						}
					};
					
					cr.accept(cv, ClassReader.EXPAND_FRAMES);
				} catch (Exception e) {
					HawkException.catchException(e);
					return null;
				}
			}			
		}
		
		return useEnumClassSet;
	}
	
	static class CheckUseEnumVisitor extends MethodVisitor {
		private Set<String> set = null;
		String className = null;
		public CheckUseEnumVisitor(String className, Set<String> set){
			super(Opcodes.ASM5);
			this.className = className;
			this.set = set;			
		}
		
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			if (!name.equals("values")) {
				return;
			}
			
			try {
				Class<?> clazz = Class.forName(owner.replaceAll("/", "."));
				if (clazz.isEnum() && (!clazz.getName().contains("com.hawk.game.protocol"))) {
					Method method = clazz.getDeclaredMethod("values");
					method.setAccessible(true);
					Object[] arraySize = (Object[])method.invoke(null);
					set.add(className+"="+owner+"."+name +" " +" size==" + arraySize.length);
				}				
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
		}
	}
	
	/***************************************************************************************/
	/*****************************   redis链接关闭检测 TODO     ************************************/
	/***************************************************************************************/
	
	/**
	 * jedis连接关闭检测
	 * @param classNameList
	 * @return
	 */
	public static boolean checkJedisUnclose(List<String> classNameList) {
		Set<String> set = new HashSet<>();
		try {			
			for (String className : classNameList) {
				ClassReader cr = new ClassReader(className);
				CheckJedisClassVisitor mcv = new CheckJedisClassVisitor(className, set);
				cr.accept(mcv, ClassReader.EXPAND_FRAMES);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}			
		
		if (!set.isEmpty()) {
			HawkLog.errPrintln("{} get redis but not return", set);
			return false;
		} else {
			return true;
		}		
	}
	
	static class CheckJedisClassVisitor extends ClassVisitor {
		private String className = null;
		Set<String> set;
		public CheckJedisClassVisitor(String className, Set<String> set) {			
			super(Opcodes.ASM5);			
			this.className = className;
			this.set = set;
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return new CheckJedisMethodVisitor(this.className, name, set);
		}
	}
	
	static class CheckJedisMethodVisitor extends MethodVisitor{
		String className;
		String methodName;
		Set<String> set;
		private boolean getJedis;
		private boolean returnJedis;
		
		CheckJedisMethodVisitor(String className, String methodName, Set<String> set) {
			super(Opcodes.ASM5);
			this.className = className;
			this.methodName = methodName;
			this.set = set;
		}
		
		/**
		 * 遍历方法调用.
		 */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			if (owner.contains("HawkRedisSession") && name.equals("getJedis")) {
				getJedis = true;
			}
			
			if (owner.contains("Jedis") && name.equals("close")) {
				returnJedis = true;
			}
		}
		
		/**
		 * 方法遍历到最后.
		 */
		@Override
		public void visitEnd() {
			if (getJedis ^ returnJedis) {
				set.add(String.format("className:%s methodName:%s", className, methodName));
			} 
		}
	}	
	
	/***************************************************************************************/
	/**************************   活动entity内部重复调用set方法检测 TODO     ************************/
	/***************************************************************************************/
	
	public static boolean checkActivityDBEntity() {
		List<Class<?>> clazzList = HawkClassScaner.getAllClasses("com.hawk.activity.type.impl");
		Set<String> set = new HashSet<>();
		for (Class<?> clazz : clazzList) {
			try {			
				Class<?> superClass = clazz.getSuperclass();
				if (superClass == null) {
					continue;
				}
				
				boolean dbEntity = superClass.getName().equals("org.hawk.db.HawkDBEntity") || superClass.getName().equals("com.hawk.activity.AchieveActivityEntity");
				if (!dbEntity) {
					continue;
				}
				String className = clazz.getName();
				ClassReader cr = new ClassReader(className);
				CheckActivityDBEntityClassVisitor mcv = new CheckActivityDBEntityClassVisitor(clazz.getSimpleName(), set);
				cr.accept(mcv, ClassReader.EXPAND_FRAMES);
			} catch (Exception e) {
				HawkException.catchException(e);
				return false;
			}			
		}
		
		if (!set.isEmpty()) {
			HawkLog.errPrintln("{} call set method in beforeWrite method", set);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 检测活动dbentity的beforeWrite方法内是否调用了set方法
	 */
	static class CheckActivityDBEntityClassVisitor extends ClassVisitor {
		private String className = null;
		Set<String> set;
		Set<String> callSetMethodSet;  // 用于记录直接或间接调用了set方法的方法名称
		Set<String> beforeWriteCallMethodSet; // 用于记录被beforeWrite直接或间接调用的内部方法名称
		public CheckActivityDBEntityClassVisitor(String className, Set<String> set) {			
			super(Opcodes.ASM5);			
			this.className = className;
			this.set = set;
			this.callSetMethodSet = new HashSet<>();
			this.beforeWriteCallMethodSet = new HashSet<>();
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return new CheckSetMethodCallVisitor(this.className, name, set, callSetMethodSet, beforeWriteCallMethodSet);
		}
	}
	
	/**
	 * 检测活动dbentity的beforeWrite方法内是否调用了set方法
	 */
	static class CheckSetMethodCallVisitor extends MethodVisitor{
		String className;
		String methodName;
		Set<String> set;
		Set<String> callSetMethodSet;  // 用于记录直接或间接调用了set方法的方法名称
		Set<String> beforeWriteCallMethodSet; // 用于记录被beforeWrite直接或间接调用的内部方法名称
		CheckSetMethodCallVisitor(String className, String methodName, Set<String> set, Set<String> callSetMethodSet, Set<String> beforeWriteCallMethodSet) {
			super(Opcodes.ASM5);
			this.className = className;
			this.methodName = methodName;
			this.set = set;
			this.callSetMethodSet = callSetMethodSet;
			this.beforeWriteCallMethodSet = beforeWriteCallMethodSet;
		}
		
		/**
		 * 遍历方法调用.
		 */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			// 如果调用的不是本类中的方法，直接返回
			if (!owner.contains(className)) {
				return;
			}
			
			// 调用了本类中的set方法，直接将方法名记录下来
			if (name.startsWith("set")) {
				// beforeWriteCallMethodSet 中包含的方法调用了set，就相当于 beforeWrite调用了 set
				if (methodName.equals("beforeWrite") || beforeWriteCallMethodSet.contains(methodName)) {
					// 情形1：beforeWrite方法中直接调用了set方法
					set.add(className);
				} else {
					// 情形2：其它方法中调用了set方法，调用者方法有可能也被 beforeWrite调用了, 所以要将中间方法记录下来
					callSetMethodSet.add(methodName);  // methodName直接调用了set方法, 将其加入到 callSetMethodSet中
				}
			}
			
			// 这个类已经记录下来了，就不再分析了
			if (set.contains(className)) {
				return;
			}
			// 这个类还没有被记录，判断是否存在中间方法调用set；比如方法A中调用了set方法，B调用了A方法，C调用了B方法，beforeWrite调用了A或B或C
			
			if (methodName.equals("beforeWrite") || beforeWriteCallMethodSet.contains(methodName)) {
				if (callSetMethodSet.contains(name)) {  // name方法直接或间接调用了set方法， beforeWrite中调用了name方法
					set.add(className);
				} else {
					beforeWriteCallMethodSet.add(name); // beforeWrite中调用的其它方法也要记录下来，因为有可能先扫描beforeWrite，再扫描的中间方法
				}
			} else if (callSetMethodSet.contains(name)) {
				// name方法直接或间接调用了set方法, 将其调用者 methodName 也加到 callSetMethodSet 中
				callSetMethodSet.add(methodName);
			}
		}
	}	
	
	
	/***************************************************************************************/
	/*****************************  检测活动类中的action引用  TODO   ************************************/
	/***************************************************************************************/
	
	public static void checkActionRef() {
		try {
			List<String> classNameList = new ArrayList<>();
			List<Class<?>> classList = HawkClassScaner.getAllClasses("com.hawk.activity");
			classList.forEach(e -> classNameList.add(e.getName()));
			checkActionRef(classNameList);
			classNameList.clear();
			
			List<Class<?>> gameClassList = HawkClassScaner.getAllClasses("com.hawk.game");
			gameClassList.forEach(e -> classNameList.add(e.getName()));
			checkActionRef(classNameList);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * j检测活动类中的action引用
	 * @param classNameList
	 * @return
	 */
	public static boolean checkActionRef(List<String> classNameList) {
		JSONObject json = new JSONObject();
		try {			
			for (String className : classNameList) {
				ClassReader cr = new ClassReader(className);
				CheckActionRefClassVisitor mcv = new CheckActionRefClassVisitor(className, json);
				cr.accept(mcv, ClassReader.EXPAND_FRAMES);
				if (json.getString("reason") != null) {
					HawkLog.logPrintln("check action reference, activityType: {}, reason: {}, className: {}, methodName: {}", json.get("activityType"), json.getString("reason"), className, json.getString("methodName"));
				}
				json.clear();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}			
		
		return true;
	}
	
	static class CheckActionRefClassVisitor extends ClassVisitor {
		private String className = null;
		private JSONObject json;
		public CheckActionRefClassVisitor(String className, JSONObject json) {			
			super(Opcodes.ASM5);			
			this.className = className;
			this.json = json;
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return new CheckActionRefMethodVisitor(this.className, name, json);
		}
	}
	
	static class CheckActionRefMethodVisitor extends MethodVisitor{
		String className;
		String methodName;
		private JSONObject json;
		CheckActionRefMethodVisitor(String className, String methodName, JSONObject json) {
			super(Opcodes.ASM5);
			this.className = className;
			this.methodName = methodName;
			this.json = json;
			json.put("methodName", methodName);
		}
		
	    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
	    	try {
	    		if (owner.contains("com/hawk/activity/type/ActivityType")) {
	    			if (name == null || name.equals("timeControl") || name.equals("dbEntity") || name.equals("handler") || name.equals("activity") || name.equals("value") || name.startsWith("ENUM")) {
	    				return;
	    			}
	    			ActivityType type = ActivityType.valueOf(name);
	    			json.put("activityType", type == null ? name : type.intValue());
	    		}
	    		
	    		if (owner.contains("com/hawk/log/Action")) {
	    			JSONArray array = json.getJSONArray("reason");
	    			if (array == null) {
	    				array = new JSONArray();
	    				json.put("reason", array);
	    			}
	    			Action action = Action.valueOf(name);
	    			if (!array.contains(action.intItemVal())) {
	    				array.add(action.intItemVal());
	    			}
	    		}
	    	} catch (Exception e) {
	    		HawkException.catchException(e, name);
	    	}
	    }
	}
	
	
	
	/***************************************************************************************/
	/*****************************  检测活动类中的打点上报 TODO    ************************************/
	/***************************************************************************************/
	
	public static void checkTlogOfActivity() {
		List<String> classNameList = new ArrayList<>();
		//GameActivityDataProxy中method和LogUtil中method方法的映射
		Map<String, String> proxy2ReportMethodMap = new HashMap<>();
		//LogUtil中method和logType的映射
		Map<String, String> utilMethod2LogTypeMap = new HashMap<>();
		try {
			List<Class<?>> gameClassList = HawkClassScaner.getAllClasses("com.hawk.game");
			gameClassList.forEach(e -> classNameList.add(e.getName()));
			checkTlogOfGameActivity(classNameList, proxy2ReportMethodMap, utilMethod2LogTypeMap);
			
			classNameList.clear();
			List<JSONObject> jsonList = new ArrayList<>();
			List<Class<?>> classList = HawkClassScaner.getAllClasses("com.hawk.activity");
			classList.forEach(e -> classNameList.add(e.getName()));
			Map<String, List<String>> commonMethodLogTypeMap = new HashMap<>();
			checkTlogOfActivity(classNameList, proxy2ReportMethodMap, jsonList, commonMethodLogTypeMap);
			
			for (JSONObject json : jsonList) {
				int activityType = json.getIntValue("activityType");
				String className = json.getString("className");
				JSONArray array = json.getJSONArray("methodName");
				List<String> logTableList = new ArrayList<>();
				for (int i = 0; i < array.size(); i++) {
					String methodName = "m", logUtilMethod= "logm", logType="t";
					try {
						methodName = array.getString(i);
						logUtilMethod = proxy2ReportMethodMap.get(methodName);
						logType = utilMethod2LogTypeMap.get(logUtilMethod);
						if (logType == null) {
							continue;
						}
						LogTableCfg cfg = GameLog.getInstance().getLogTable(logType);
						logTableList.add(cfg.getTableName());
					} catch (Exception e) {
						HawkException.catchException(e, methodName + "-" + logUtilMethod + "-" + logType);
					}
				}
				
				List<String> list = commonMethodLogTypeMap.get(className);
				if (list != null) {
					for (String type : list) {
						LogTableCfg cfg = GameLog.getInstance().getLogTable(type);
						logTableList.add(cfg.getTableName());
					}
				}
				HawkLog.logPrintln("tlog table of report of activity, activityType: {}, tableList: {}, calssName: {}", activityType, logTableList, className);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		HawkLog.logPrintln("tlog table of report of activity check end");
	}
	
	public static boolean checkTlogOfActivity(List<String> classNameList, Map<String, String> proxy2LogUtilMethodMap, List<JSONObject> jsonList, Map<String, List<String>> commonMethodLogTypeMap) {
		try {			
			for (String className : classNameList) {
				JSONObject json = new JSONObject();
				ClassReader cr = new ClassReader(className);
				CheckTlogOfActivityClassVisitor mcv = new CheckTlogOfActivityClassVisitor(className, json, proxy2LogUtilMethodMap, commonMethodLogTypeMap);
				cr.accept(mcv, ClassReader.EXPAND_FRAMES);
				if (json.containsKey("activityType") && json.containsKey("methodName")) {
					jsonList.add(json);
					HawkLog.logPrintln("check tlog report in activity class, info: {}", json.toJSONString());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}			
		
		return true;
	}
	
	public static boolean checkTlogOfGameActivity(List<String> classNameList, Map<String, String> proxy2ReportMethodMap, Map<String, String> utilMethod2LogTypeMap) {
		JSONObject json = new JSONObject();
		try {			
			for (String className : classNameList) {
				if (className.contains("GameActivityDataProxy")) {
					json.clear();
					ClassReader cr = new ClassReader(className);
					CheckTlogOfActivityClassVisitor mcv = new CheckTlogOfActivityClassVisitor(className, json, proxy2ReportMethodMap, new HashMap<>());
					cr.accept(mcv, ClassReader.EXPAND_FRAMES);
					
					if (json.containsKey("GameActivityDataProxyMethod")) {
						HawkLog.logPrintln("check logUtil method called by activityDataProxy, info: {}", json.toJSONString());
						JSONArray array = json.getJSONArray("GameActivityDataProxyMethod");
						for (int i=0; i<array.size(); i++) {
							JSONObject obj = array.getJSONObject(i);
							proxy2ReportMethodMap.put(obj.getString("methodName"), obj.getString("logUtilMethod"));
						}
					}
					continue;
				}
				
				if (className.equals("com.hawk.game.util.LogUtil")) {
					json.clear();
					ClassReader cr = new ClassReader(className);
					CheckTlogOfActivityClassVisitor mcv = new CheckTlogOfActivityClassVisitor(className, json, proxy2ReportMethodMap,  new HashMap<>());
					cr.accept(mcv, ClassReader.EXPAND_FRAMES);
					HawkLog.logPrintln("check logType to method, info: {}", json.toJSONString());
					
					for (String key : json.keySet()) {
						JSONArray array = json.getJSONArray(key);
						if (array.size() == 1) {
							utilMethod2LogTypeMap.put(array.getString(0), key);
						} else {
							HawkLog.logPrintln("check logType to method, logType: {}, methods: {}", key, array);
						}
					}
					if (json.containsKey("GameActivityDataProxyMethod")) {
						JSONArray array = json.getJSONArray("GameActivityDataProxyMethod");
						for (int i=0; i<array.size(); i++) {
							JSONObject obj = array.getJSONObject(i);
							proxy2ReportMethodMap.put(obj.getString("methodName"), obj.getString("logUtilMethod"));
						}
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}			
		
		return true;
	}
	
	static class CheckTlogOfActivityClassVisitor extends ClassVisitor {
		private String className = null;
		private JSONObject json;
		private Map<String, String> proxy2LogUtilMethodMap;
		private Map<String, List<String>> commonMethodLogTypeMap;
		public CheckTlogOfActivityClassVisitor(String className, JSONObject json, Map<String, String> proxy2LogUtilMethodMap, Map<String, List<String>> commonMethodLogTypeMap) {			
			super(Opcodes.ASM5);			
			this.className = className;
			this.json = json;
			this.proxy2LogUtilMethodMap = proxy2LogUtilMethodMap;
			this.commonMethodLogTypeMap = commonMethodLogTypeMap;
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return new CheckTlogOfActivityMethodVisitor(this.className, name, json, proxy2LogUtilMethodMap, commonMethodLogTypeMap);
		}
	}
	
	static class CheckTlogOfActivityMethodVisitor extends MethodVisitor{
		String className;
		String methodName;
		private JSONObject json;
		private Map<String, String> proxy2LogUtilMethodMap;
		private Map<String, List<String>> commonMethodLogTypeMap;
		CheckTlogOfActivityMethodVisitor(String className, String methodName, JSONObject json, Map<String, String> proxy2LogUtilMethodMap, Map<String, List<String>> commonMethodLogTypeMap) {
			super(Opcodes.ASM5);
			this.className = className;
			this.methodName = methodName;
			this.json = json;
			this.proxy2LogUtilMethodMap = proxy2LogUtilMethodMap;
			this.commonMethodLogTypeMap = commonMethodLogTypeMap;
		}
		
	    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
	    	try {
	    		if (owner.contains("com/hawk/activity/type/ActivityType")) {
	    			if (name == null || name.equals("timeControl") || name.equals("dbEntity") || name.equals("handler") || name.equals("activity") || name.equals("value") || name.startsWith("ENUM")) {
	    				return;
	    			}
	    			ActivityType type = ActivityType.valueOf(name);
	    			json.put("activityType", type == null ? name : type.intValue());
	    			json.put("className", className);
	    		}
	    		
	    		if (className.contains("activity.type.impl") && owner.contains("LogInfoType")) {
	    			List<String> list = commonMethodLogTypeMap.get(className);
	    			if (list == null) {
	    				list = new ArrayList<>();
	    				commonMethodLogTypeMap.put(className, list);
	    			}
	    			list.add(name);
	    		}
	    		
	    		if (className.contains("LogUtil") && owner.contains("LogInfoType")) {
	    			JSONArray array = json.getJSONArray(name);
	    			if (array == null) {
	    				array = new JSONArray();
	    				json.put(name, array);
	    			}
	    			
	    			array.add(methodName);
	    		}
	    	} catch (Exception e) {
	    		HawkException.catchException(e, name);
	    	}
	    }
	    
	    /**
		 * 遍历方法调用.
		 */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			if (className.endsWith("GameActivityDataProxy") && owner.endsWith("LogUtil")) {
				JSONArray array = json.getJSONArray("GameActivityDataProxyMethod");
    			if (array == null) {
    				array = new JSONArray();
    				json.put("GameActivityDataProxyMethod", array);
    			}
    			
    			JSONObject obj = new JSONObject();
    			obj.put("methodName", methodName);
    			obj.put("logUtilMethod", name);
    			array.add(obj);
				return;
			}
			
			if (!proxy2LogUtilMethodMap.containsKey(name)) {
				return;
			}
			
			if (owner.contains("ActivityDataProxy") || owner.contains("GameActivityDataProxy")) {
				JSONArray array = json.getJSONArray("methodName");
    			if (array == null) {
    				array = new JSONArray();
    				json.put("methodName", array);
    			}
    			if (!array.contains(name)) {
    				array.add(name);
    			}
    		}
		}
	}
	
}
