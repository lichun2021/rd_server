const fs = require('fs');
const path = require('path');
const { spawnSync, spawn } = require('child_process');

const SCRIPT_DIR = __dirname;
const CONFIG_FILE = path.join(SCRIPT_DIR, 'cfg', 'mergeDb.cfg');
const MYSQL_BIN = process.env.MYSQL_BIN || 'mysql';
const MYSQLDUMP_BIN = process.env.MYSQLDUMP_BIN || 'mysqldump';
// 仅回滚主库（dbNames 第一个）：RESTORE_SCOPE=master
const RESTORE_SCOPE = (process.env.RESTORE_SCOPE || 'all').trim().toLowerCase(); // all | master
// 备份库后缀，默认 _backup（如需用 _backup2：BACKUP_SUFFIX=_backup2）
const BACKUP_SUFFIX = (process.env.BACKUP_SUFFIX || '_backup').trim();
// 只打印，不执行：DRY_RUN=1
const DRY_RUN = String(process.env.DRY_RUN || '').trim() === '1';
// mysqldump 输出表级进度（stderr）：DUMP_VERBOSE=1
const DUMP_VERBOSE = String(process.env.DUMP_VERBOSE || '').trim() === '1';

if (!fs.existsSync(CONFIG_FILE)) {
  console.error(`[ERROR] 找不到配置文件: ${CONFIG_FILE}`);
  process.exit(1);
}

function parseCfg(file) {
  const cfg = {};
  const lines = fs.readFileSync(file, 'utf8').split(/\r?\n/);
  for (const raw of lines) {
    const line = raw.trim();
    if (!line || line.startsWith('#') || !line.includes('=')) continue;
    const [k, v] = line.split('=', 2).map(s => s.trim());
    cfg[k] = v;
  }
  return cfg;
}

const cfg = parseCfg(CONFIG_FILE);
if (!cfg.dbNames) {
  console.error('[ERROR] 配置项 dbNames 为空');
  process.exit(1);
}

const dbNames = cfg.dbNames.split(',').map(s => s.trim()).filter(Boolean);

function toList(raw, len, defVal) {
  const arr = (raw || '').split(',').map(s => s.trim()).filter(Boolean);
  if (arr.length === 0) return Array(len).fill(defVal);
  if (arr.length === 1 && len > 1) return Array(len).fill(arr[0]);
  if (arr.length !== len) {
    console.error(`[ERROR] 配置长度不匹配，需要 ${len} 个，当前 ${arr.length} 个: ${raw}`);
    process.exit(1);
  }
  return arr;
}

const hostList = toList(cfg.dbHosts, dbNames.length, '127.0.0.1');
const portList = toList(cfg.dbPorts, dbNames.length, '3306');
const userList = toList(cfg.dbUsernames, dbNames.length, 'root');
const passList = toList(cfg.dbPasswords, dbNames.length, '');

function mysqlExec(conn, sql) {
  if (DRY_RUN) {
    console.log(`[DRY] mysql -h ${conn.host} -P ${conn.port} -u ${conn.user} -p*** -e "${sql}"`);
    return '';
  }
  const args = ['-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`, '-N', '-B', '-e', sql];
  const res = spawnSync(MYSQL_BIN, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'inherit'] });
  if (res.status !== 0) {
    console.error(`[ERROR] mysql 执行失败: ${sql}`);
    process.exit(1);
  }
  return res.stdout.trim();
}

function restoreOne(db, conn) {
  const backup = `${db}${BACKUP_SUFFIX}`;
  console.log(`[CHECK] ${backup} @ ${conn.host}:${conn.port}`);
  const exists = mysqlExec(conn, `SHOW DATABASES LIKE '${backup}';`);
  if (!exists) {
    console.error(`[ERROR] 找不到备份库 ${backup}`);
    process.exit(1);
  }

  console.log(`[DROP] ${db}`);
  mysqlExec(conn, `DROP DATABASE IF EXISTS \`${db}\`;`);

  console.log(`[CREATE] ${db}`);
  mysqlExec(conn, `CREATE DATABASE IF NOT EXISTS \`${db}\` DEFAULT CHARACTER SET utf8mb4;`);

  console.log(`[RESTORE] ${backup} -> ${db}`);
  return new Promise((resolve, reject) => {
    const dumpArgs = [
      '-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`,
      '--quick',
      '--single-transaction', '--routines', '--events', '--triggers',
      '--column-statistics=0', '--no-tablespaces',
      backup,
    ];
    if (DUMP_VERBOSE) dumpArgs.splice(dumpArgs.length - 1, 0, '--verbose');
    const restoreArgs = ['-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`, db];

    if (DRY_RUN) {
      console.log(`[DRY] ${MYSQLDUMP_BIN} ${dumpArgs.join(' ')} | ${MYSQL_BIN} ${restoreArgs.join(' ')}`);
      resolve();
      return;
    }

    const dump = spawn(MYSQLDUMP_BIN, dumpArgs, { stdio: ['ignore', 'pipe', 'inherit'] });
    const restore = spawn(MYSQL_BIN, restoreArgs, { stdio: ['pipe', 'inherit', 'inherit'] });

    dump.stdout.pipe(restore.stdin);

    let dumpDone = false, restoreDone = false;
    let dumpCode = 0, restoreCode = 0;

    const finish = () => {
      if (!dumpDone || !restoreDone) return;
      if (dumpCode === 0 && restoreCode === 0) {
        console.log(`[OK] ${backup} -> ${db}`);
        resolve();
      } else {
        reject(new Error(`mysqldump exit ${dumpCode}, mysql exit ${restoreCode}`));
      }
    };

    dump.on('error', reject);
    restore.on('error', reject);

    dump.on('exit', code => { dumpDone = true; dumpCode = code; try { restore.stdin.end(); } catch (_) {} finish(); });
    restore.on('exit', code => { restoreDone = true; restoreCode = code; finish(); });
  });
}

(async () => {
  console.log('使用配置:');
  console.log(`  dbHosts=${hostList.join(',')} dbPorts=${portList.join(',')} dbUsernames=${userList.join(',')}`);
  console.log(`  dbNames=${dbNames.join(',')}`);
  console.log(`  restoreScope=${RESTORE_SCOPE} backupSuffix=${BACKUP_SUFFIX} dryRun=${DRY_RUN ? 'on' : 'off'} verbose=${DUMP_VERBOSE ? 'on' : 'off'}`);
  console.log('');

  const max = RESTORE_SCOPE === 'master' ? 1 : dbNames.length;
  for (let i = 0; i < max; i++) {
    const db = dbNames[i];
    const conn = { host: hostList[i], port: portList[i], user: userList[i], pass: passList[i] };
    await restoreOne(db, conn);
  }

  console.log('完成');
})().catch(err => {
  console.error('[ERROR]', err.message || err);
  process.exit(1);
});

