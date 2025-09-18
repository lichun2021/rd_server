package com.hawk.game.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import com.hawk.game.GsConfig;
import com.hawk.game.config.MergeIgnoreConfig;
import com.hawk.game.config.MergeServerConfig;
import com.hawk.game.config.SeparateActivityCfg;
import com.hawk.game.config.SeparateIgnoreCfg;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;


/**
 * 
 * @author jm
 *
 */
public class DBUtil {
	
	/**
	 *检测代码里面的字段和数据库是不是对的上,
	 *无法检测字段类型, 因为现在的类型定义的不严格,
	 *比如 mysql里面定义int 然后代码里面定义成boolean.
	 *如果需要支持此种太复杂,且场景较小，只需校验字段是不是对的上即可。
	 */
	public static void checkCodeAndDbColumns() {
		List<String> tableNameList = selectTablesName();
		boolean found = false;
		StringBuilder sb = new StringBuilder();
		StringBuilder mysqlColumn = new StringBuilder();
		for (Entry<String, String> entry : HawkDBEntity.getEntityTableNames().entrySet()) {
			//worldPoint在redis模式下不检测
			if (entry.getValue().equalsIgnoreCase("world_point") && GsConfig.getInstance().getWorldPointProxy() > 0) {
				continue;
			}
			found = false;
			for (String tableName : tableNameList) {
				if (tableName.equalsIgnoreCase(entry.getValue())) {
					found = true;
					break;
				}
			}
			
			List<String> columnsNameList = HawkDBEntity.getColumnNames(entry.getKey());
			Collections.sort(columnsNameList);
			if(found) {				
				List<TableDescript> tableDescriptList = selectTableColumns(entry.getValue());
				List<String> dbColumnsList = new ArrayList<>();
				tableDescriptList.stream().forEach(td->{
					dbColumnsList.add(td.getColumeName());
				});
				Collections.sort(dbColumnsList);
				if (!containsAll(dbColumnsList, columnsNameList)) {					
					sb.append(entry.getKey()+"={");
					sb.append("db="+dbColumnsList+",");
					sb.append("code="+columnsNameList);
					sb.append("},");
				}
				
				StringBuilder tmpSB = new StringBuilder();
				tableDescriptList.stream().forEach(td->{
					if (((!td.isAllowNull()) || td.isUnique() || td.isPrimaryKey()) && td.getDefFaultValue() == null) {						
						if (!columnsNameList.contains(td.getColumeName())) {
							if (tmpSB.length() == 0) {
								tmpSB.append(entry.getKey()+"={");
								tmpSB.append("unused DbColumn="+td.getColumeName());
							} else {
								tmpSB.append(","+td.getColumeName());
							}
						}												
					}
				});
				
				if (tmpSB.length() > 0) {					
					tmpSB.append("}");
					mysqlColumn.append(tmpSB+",");
				}
			} else {
				sb.append(entry.getKey()+"={");
				sb.append("db=[],");
				sb.append("code=");
				sb.append(columnsNameList);
				sb.append("},");
			}						
		}
		
		if (sb.length() > 0 || mysqlColumn.length() > 0) {
			if (sb.length() > 0) {
				HawkLog.errPrintln(sb.toString());
			}
			if (mysqlColumn.length() > 0) {
				HawkLog.errPrintln(mysqlColumn.toString());
			}			
			throw new RuntimeException("代码字段和数据库字段不匹配");			
		}
	}
	
	//db 一定要包含 code
	//code 不必包含db
	public static boolean containsAll(List<String> dbList, List<String> codeList) {
		for (String str : codeList) {
			boolean isContains = false;
			for (String db : dbList) {
				if (db.equalsIgnoreCase(str)) {
					isContains = true;
				}
			}
			
			if (!isContains) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 代码更新sql机制
	 */
	public static void useCodeUpdateDb()  {
		String sql = null;
		List<String> sqlList = new ArrayList<>();
		List<String> tableNameList = selectTablesName();
		boolean found = false, exceptionFlag = false;
		for (Entry<String, String> entry : HawkDBEntity.getEntityTableNames().entrySet()) {
			String entityClass = entry.getKey();
			String table = entry.getValue();
			found = false;
			for (String tableName : tableNameList) {
				if (tableName.equalsIgnoreCase(table)) {
					found = true;
					break;
				}
			}
			
			List<Field> fieldList = HawkDBEntity.getColumnFileds(entityClass);
			try {
				if (found) {
					List<TableDescript> tableDescriptList = selectTableColumns(table);														
					sql = genUpdateSql(table, fieldList, tableDescriptList);
					if (!HawkOSOperator.isEmptyString(sql)) {
						sqlList.add(sql);
					}					
				} else {
					sql = genCreateSql(table, fieldList);
					MergeServerConfig mergeCfg = HawkConfigManager.getInstance().getKVInstance(MergeServerConfig.class);
					if (table.startsWith(mergeCfg.getActivityPrefix())) {
						MergeIgnoreConfig mergeIgnore = HawkConfigManager.getInstance().getConfigByKey(MergeIgnoreConfig.class, table);
						if (!mergeCfg.getSaveActivityTableList().contains(table) && mergeIgnore == null) {
							exceptionFlag = true;
							throw new RuntimeException("merge_config.cfg -> saveActivityTable miss new table: " + table);
						}
						SeparateActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeparateActivityCfg.class, table);
						SeparateIgnoreCfg separateIgnore = HawkConfigManager.getInstance().getConfigByKey(SeparateIgnoreCfg.class, table);
						if (cfg == null && separateIgnore == null) {
							exceptionFlag = true;
							throw new RuntimeException("separateActivity.xml miss new table: " + table);
						}
					}
					
					sqlList.add(sql);
				}
			}catch (Exception e) {
				HawkException.catchException(e, "sql oeration error tableName=>" + table + " entity=>" + entityClass);
				if (exceptionFlag) {
					break;
				}
			}
		}
		
		if (exceptionFlag) {
			throw new RuntimeException("merge_config.cfg or separateActivity.xml miss new table");
		}
		
		AtomicInteger atomic = new AtomicInteger(0);
		int times = 0;
		while (atomic.get() == 0 && times < 3) {
			times++;
			//更新sql语句
			Session session = HawkDBManager.getInstance().getSession();
			session.beginTransaction();
			session.doWork(new Work() {
				@Override
				public void execute(Connection connection) throws SQLException {
					try{
						Statement stmt = connection.createStatement();
						for (String sql : sqlList) {
							stmt.addBatch(sql);
						}				
						stmt.executeBatch();
						atomic.set(1);
					} catch(SQLException e) {
						HawkLog.errPrintln("execute sql error:{}", sqlList.toString());
						HawkLog.errPrintln("SQLException msg: {}", e.getMessage());
						if (e.getMessage().indexOf("Row size too large") < 0){
							atomic.set(1);
							throw e;
						} else {
							for (int i= 0; i < sqlList.size(); i++) {
								if (sqlList.get(i).indexOf("varchar(512)") > 0) {
									String newSql = sqlList.get(i).replace("varchar(512)", "varchar(128)");
									sqlList.set(i, newSql);
								}
							}
						}
					}
				}
			});
			session.getTransaction().commit();
			session.close();	
		}
		
		if (times >= 3) {
			throw new RuntimeException("useCodeUpdateDb failed");
		}
	}
	

	/**
	 * 生成表更新的sql
	 * 
	 * @param tableName
	 * @param fieldList
	 * @param tableDescriptList
	 * @return
	 * @throws Exception
	 */
	public static String genUpdateSql(String tableName, List<Field> fieldList, List<TableDescript> tableDescriptList) throws Exception {
		StringBuilder sb = new StringBuilder();
		String alterTable = "ALTER TABLE " + tableName + " ";
		boolean appendFlag = false;
		List<String> modifyList = new ArrayList<>();
		List<String> addList = new ArrayList<>();
		List<String> dropList = new ArrayList<>();
		Map<String, Boolean> fieldMap = new HashMap<>();
		fieldList.stream().forEach(f->{
			if (f.getType() == String.class) {
				fieldMap.put(f.getName(), false);
			}			
			});		
		String classNmae = fieldList.get(0).getDeclaringClass().getName();
		findFiledVistor(classNmae, fieldMap);
		List<String> changedColumns = new ArrayList<>();
		for (Field field : fieldList) {
			Column column = field.getAnnotation(Column.class);
			Id id = field.getAnnotation(Id.class);
			boolean found = false;
			Class<?> javaType = field.getType();
			boolean isText = fieldMap.getOrDefault(field.getName(), false);
			for (TableDescript tableDescript : tableDescriptList) {
				if (column.name().equalsIgnoreCase(tableDescript.getColumeName())) {
					found = true;
					if (!changedColumns.contains(column.name())){
						//类的字段和数据的字段类型对不上，以类的为准.
						if (!isSameType(javaType, tableDescript.getType(), isText)) {
							changedColumns.add(column.name());
							modifyList.add(" MODIFY COLUMN " + genMysqlTypeString(column.name(), javaType, column, id != null, isText));												
						}
						
						if (id == null && tableDescript.isPrimaryKey()) {
							changedColumns.add(column.name());
							dropList.add("DROP PRIMARY KEY ");
						}
						if (id != null && !tableDescript.isPrimaryKey()) {
							changedColumns.add(column.name());
							addList.add("ADD PRIMARY KEY (`"+column.name()+"`)");
						}									
					}
				}
			}
			
			//类字段在数据库找不到,就新增数据库的字段.
			if (!found) {
				if (!changedColumns.contains(column.name())) {
					changedColumns.add(column.name());
					addList.add("ADD COLUMN " + genMysqlTypeString(column.name(), javaType, column, id != null, isText));
				}
				if (id != null && !changedColumns.contains(column.name())) {
					changedColumns.add(column.name());
					addList.add("ADD PRIMARY KEY (`"+column.name()+"`)");
				}
			}
		}
		
		//查找在库中，但是不在java中的字段.
		boolean foundInJava = false;
		for (TableDescript td : tableDescriptList) {
			foundInJava = false;
			for (Field field : fieldList) {
				Column column = field.getAnnotation(Column.class);
				if (column != null && column.name().equalsIgnoreCase(td.getColumeName())) {
					foundInJava = true;
					break;
				}
			}
			
			if (!foundInJava && !changedColumns.contains(td.getColumeName())) {
				changedColumns.add(td.getColumeName());
				dropList.add(" DROP COLUMN `"+td.getColumeName()+"`");
			}
		}
		
		 
		for (String str : dropList) {
			if (appendFlag) {
				sb.append(",");
			}
			sb.append(str);
			appendFlag = true;
		}
		
		for (String str : modifyList) {
			if (appendFlag) {
				sb.append(",");
			}
			sb.append(str);
			appendFlag = true;
		}
		
		for (String str : addList) {
			if (appendFlag) {
				sb.append(",");
			}			
			sb.append(str);
			appendFlag = true;
		}
		
		if (sb.length() > 0) {
			return alterTable + sb.toString() +";";
		} else {
			return "";
		}
	}

	/**
	 * 判断类型
	 * 
	 * @param typeName
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private static boolean isSameType(Class<?> typeName, int type, boolean isText) throws Exception {
		if (isText && type != Types.VARCHAR && type != Types.LONGVARCHAR) {
			return false;
		}
		if (typeName == Date.class && Types.TIMESTAMP == type) {
			return true;
		} else if (typeName == byte[].class  && Types.LONGVARBINARY == type) {
			return true;
		} else if ((typeName == boolean.class || typeName == Boolean.class) &&  (Types.TINYINT == type || Types.BIT == type || Types.INTEGER == type)) {
			return true;
		} else if ((typeName == int.class || typeName == Integer.class) && Types.INTEGER == type) {
			return true;
		} else if ((typeName == long.class || typeName == Long.class) && Types.BIGINT == type) {
			return true;
		} else if ((typeName == float.class || typeName == Float.class || 
				typeName == double.class || typeName == Double.class) && Types.DECIMAL == type) {
			return true;
		} else if ((typeName == short.class || typeName == Short.class) && Types.SMALLINT == type) {
			return true;
		} else if (typeName == String.class && (Types.VARCHAR == type || Types.LONGVARCHAR == type)) {
			return true;
		}
		
		return false;
	} 
	
	/**
	 * 生成删除表的sql
	 * 
	 * @param tableName
	 * @return
	 */
	public static String genDropTableSql(String tableName) {
		return "DROP TABLE " + tableName + ";";
	}

	/**
	 * 不支持索引，不支持联合主键
	 *
	 * @param tableName
	 * @param fieldList
	 * @return
	 * @throws Exception
	 */
	public static String genCreateSql(String tableName, List<Field> fieldList) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE `" + tableName + "` (");
		boolean flag = false;
		String columnStr = null;
		String priKey = null;
		Map<String, Boolean> fieldMap = new HashMap<>();
		fieldList.stream().forEach(f->{
			if (f.getType() == String.class) {
				fieldMap.put(f.getName(), false);
			}			
			});		
		String classNmae = fieldList.get(0).getDeclaringClass().getName();
		findFiledVistor(classNmae, fieldMap);
		for (Field field : fieldList) {
			Column column = field.getAnnotation(Column.class);
			if (column == null) {
				continue;
			}
			
			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				priKey = column.name();
			}
			
			if (flag){
				sb.append(",");
			}
			
			columnStr = genMysqlTypeString(column.name(), field.getType(), column, id != null, fieldMap.getOrDefault(field.getName(), false));
			flag = true;
			sb.append(columnStr);
		}
		
		if (priKey != null) {
			sb.append(", PRIMARY KEY (`"+priKey+"`)");
		}
		
		sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

		return sb.toString();
	}

	public static String genMysqlTypeString(String columnName, Class<?> type, Column column, boolean priKey, boolean isText) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("`").append(columnName).append("` ").append(java2MysqlType(type, column.length(), isText));
		int length = column.length();
		int typeInt = 0;
		if (type == boolean.class || type == Boolean.class) {
			sb.append("(1) ");
			typeInt = Types.BOOLEAN;
		} else if (type == String.class) {
			if (isText) {
				typeInt = Types.LONGVARCHAR;
			} else {
				if (priKey || columnName.equals("playerId")) {
					length = 64;
				} else {
					length = 512; //在建表的时候若指定ROW_FORMAT为COMPACT的情况下，length=512可能会报错，得减小长度，在后面处理
				}
				typeInt = Types.NVARCHAR;
				sb.append("(" + length + ") ");
			}
		} else if (type == short.class || type == Short.class) {
			sb.append("(6)");
		} else if (type == int.class || type == Integer.class) {
			sb.append("(11)");
		} else if (type == long.class || type == Long.class) {
			sb.append("(20)");
		} else if (type == float.class || type == Float.class || type == double.class || type == Double.class) {
			int precision = 6;
			if (length == 0 || length > 65) {
				length = 20;
			}
			sb.append("(" + length + "," + precision + ")");
		} else if (type == byte[].class) {
			typeInt = Types.LONGVARBINARY;  // byte数组
		}

		if (!column.nullable()) {
			sb.append(" NOT NULL");
		}
		// 可以为空
		if (column.nullable() && typeInt == Types.NVARCHAR) {
			sb.append(" DEFAULT NULL"); 
		} else {
			// 默认值
			if (typeInt == Types.NVARCHAR) {
				sb.append(" default \'\'");
			} else if (typeInt == Types.LONGVARCHAR || typeInt == Types.BLOB) {
				// 没有默认值，不处理
			} else {
				sb.append(" default 0 ");
			}
		}

		return sb.toString();
	}
	 
	public static String java2MysqlType(Class<?> type, int length, boolean isText) throws Exception {
		if (isText) {
			return "text";
		}
		if (type == Date.class) {
			return "datetime";
		} else if (type == byte[].class) {
			return "longblob";
		} else if (type == boolean.class || type == Boolean.class || type == byte.class || type == Byte.class) {
			return "tinyint";
		} else if (type == int.class || type == Integer.class) {
			return "int";
		} else if (type == long.class || type == Long.class) {
			return "bigint";
		} else if (type == float.class || type == Float.class || type == double.class || type == Double.class) {
			return "decimal";
		} else if (type == short.class || type == Short.class) {
			return "smallint";
		} else if (type == String.class) {
			if (length >= 1024) {
				return "text";
			} else {
				return "varchar";
			}
		} else {
			throw new Exception("cant transfer java typ 2 mysql type javaType=" + type);
		}

	}
	public static List<String> selectTablesName() {
		List<String> tableNameList = new ArrayList<>();
		Session session = HawkDBManager.getInstance().getSession();
		session.beginTransaction();
		session.doWork(new Work() {

			@Override
			public void execute(Connection conn) throws SQLException {
				String dbName = conn.getCatalog();
				Statement stmt = conn.createStatement();
				
				ResultSet rs = stmt.executeQuery("select table_name from information_schema.`TABLES`  where TABLE_SCHEMA = '"+dbName+"';");
				while(rs.next()){
					tableNameList.add(rs.getString("table_name"));
				}
			}
		});
		
		session.getTransaction().commit();

		return tableNameList;
	}
	
	public static List<TableDescript> selectTableColumns(String tableName) {
		List<TableDescript> tableDescript = new ArrayList<>();
		Session session = HawkDBManager.getInstance().getSession();
		session.beginTransaction();
		session.doWork(new Work() {

			@Override
			public void execute(Connection conn) throws SQLException {
				DatabaseMetaData dmd = conn.getMetaData();
				//String dbName = conn.getSchema();
				String dbName = conn.getCatalog();
				ResultSet primaryKeySet = dmd.getPrimaryKeys(null, dbName, tableName);
				List<String> primaryKeyList = new ArrayList<>();
				while (primaryKeySet.next()) {
					primaryKeyList.add(primaryKeySet.getString("COLUMN_NAME"));
				}
				
				ResultSet uniqueKeyResult = dmd.getIndexInfo(null, dbName, tableName, true, false);
				List<String> uniqueKeyList = new ArrayList<>();
				while (uniqueKeyResult.next()) {
					uniqueKeyList.add(uniqueKeyResult.getString("COLUMN_NAME"));
				}
				
				ResultSet rs = dmd.getColumns(dbName, dbName, tableName, null);
				while (rs.next()) {
					TableDescript td = new TableDescript();
					td.setColumeName(rs.getString("COLUMN_NAME"));
					td.setAllowNull(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
					td.setDefFaultValue(rs.getString("COLUMN_DEF"));
					td.setType(rs.getInt("DATA_TYPE"));
					td.setCommonts(rs.getString("REMARKS"));
					td.setLength(rs.getInt("COLUMN_SIZE"));
					td.setTypeName(rs.getString("TYPE_NAME"));
					td.setPrimaryKey(primaryKeyList.contains(td.getColumeName()));
					td.setUnique(uniqueKeyList.contains(td.getColumeName()));

					tableDescript.add(td);
				}

				rs.close();
			}
		});
		
		session.getTransaction().commit();
		session.close();

		return tableDescript;
	}
	
	static class MyClassVistor extends ClassVisitor {
		String clsName = null;
		//纯粹是为了传给MethodVistor
		private Map<String, Boolean> fieldMap;	
		public MyClassVistor(String clsName, Map<String, Boolean> fieldMap) {
            super(327680);
            this.fieldMap = fieldMap;
            this.clsName = clsName.replaceAll("\\.", "/");
        }
		
		 public MethodVisitor visitMethod(int access, String name, String desc,  String signature, String[] exceptions) {
			 if (name.equals("beforeWrite") || name.equals("afterRead")) {
	                return new MyMethodVistor(clsName, fieldMap);
	            } else {
	                return null;
	            }
	     }
	} 
	
	public static void findFiledVistor(String fullClassName, Map<String, Boolean> fieldMap) {
		try {
			ClassReader cr = new ClassReader(fullClassName);
			MyClassVistor mcv = new MyClassVistor(fullClassName, fieldMap);
			cr.accept(mcv, ClassReader.EXPAND_FRAMES);
		} catch (Exception e) {
			HawkException.catchException(e);			
		}		
	}
	
	 static class MyMethodVistor extends MethodVisitor {
	 	String clsName = null; 
		private Map<String, Boolean> fieldMap;
        public MyMethodVistor(String clsName, Map<String, Boolean> fieldMap) {
            super(327680);
            this.clsName = clsName;
            this.fieldMap = fieldMap;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (owner.equals(clsName) && fieldMap.containsKey(name)) {
            	fieldMap.put(name, true);
            }
        }
    }
}
