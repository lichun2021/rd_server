const fs = require('fs');
const path = require('path');
const { spawnSync, spawn } = require('child_process');

const SCRIPT_DIR = __dirname;
const CONFIG_FILE = path.join(SCRIPT_DIR, 'cfg', 'mergeDb.cfg');
const MYSQL_BIN = process.env.MYSQL_BIN || 'mysql';
const MYSQLDUMP_BIN = process.env.MYSQLDUMP_BIN || 'mysqldump';
// mysqldump 默认不输出进度，开关：DUMP_VERBOSE=1 会打印正在导出的表（输出到 stderr）
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
  const args = ['-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`, '-N', '-B', '-e', sql];
  const res = spawnSync(MYSQL_BIN, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'inherit'] });
  if (res.status !== 0) {
    console.error(`[ERROR] mysql 执行失败: ${sql}`);
    process.exit(1);
  }
  return res.stdout.trim();
}

function backupOne(srcDb, dstDb, conn) {
  console.log(`[CHECK] ${dstDb} @ ${conn.host}:${conn.port}`);
  const exists = mysqlExec(conn, `SHOW DATABASES LIKE '${dstDb}';`);
  if (exists) {
    console.log(`[SKIP] ${dstDb} 已存在，跳过`);
    return Promise.resolve();
  }

  console.log(`[CREATE] ${dstDb}`);
  // 直接使用反引号，不再转义为 \`，避免 mysql 将 \` 视作未知转义
  mysqlExec(conn, `CREATE DATABASE IF NOT EXISTS \`${dstDb}\` DEFAULT CHARACTER SET utf8mb4;`);

  console.log(`[DUMP] ${srcDb} -> ${dstDb}`);
  return new Promise((resolve, reject) => {
    // 兼容性参数：
    // --column-statistics=0 避免老版 MySQL 无 column_statistics 表
    // --no-tablespaces 避免缺少 PROCESS 权限报错
    const dumpArgs = [
      '-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`,
      // --quick: 逐行读取，减少大表内存/卡顿风险；配合 pipe 更稳
      '--quick',
      '--single-transaction', '--routines', '--events', '--triggers',
      '--column-statistics=0', '--no-tablespaces',
      srcDb,
    ];
    if (DUMP_VERBOSE) dumpArgs.splice(dumpArgs.length - 1, 0, '--verbose');
    const restoreArgs = ['-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`, dstDb];

    const dump = spawn(MYSQLDUMP_BIN, dumpArgs, { stdio: ['ignore', 'pipe', 'inherit'] });
    const restore = spawn(MYSQL_BIN, restoreArgs, { stdio: ['pipe', 'inherit', 'inherit'] });

    dump.stdout.pipe(restore.stdin);

    let dumpDone = false;
    let restoreDone = false;
    let dumpCode = 0;
    let restoreCode = 0;

    const finish = () => {
      if (!dumpDone || !restoreDone) return;
      if (dumpCode === 0 && restoreCode === 0) {
        console.log(`[OK] ${srcDb} -> ${dstDb}`);
        resolve();
      } else {
        reject(new Error(`mysqldump exit ${dumpCode}, mysql exit ${restoreCode}`));
      }
    };

    dump.on('error', reject);
    restore.on('error', reject);

    dump.on('exit', code => {
      dumpDone = true;
      dumpCode = code;
      try { dump.stdout.unpipe(restore.stdin); } catch (_) {}
      try { restore.stdin.end(); } catch (_) {}
      finish();
    });

    restore.on('exit', code => {
      restoreDone = true;
      restoreCode = code;
      try { dump.stdout.unpipe(restore.stdin); } catch (_) {}
      if (dump.exitCode === null) {
        try { dump.kill(); } catch (_) {}
      }
      try { restore.stdin.end(); } catch (_) {}
      finish();
    });
  });
}

(async () => {
  console.log('使用配置:');
  console.log(`  dbHosts=${hostList.join(',')} dbPorts=${portList.join(',')} dbUsernames=${userList.join(',')}`);
  console.log(`  dbNames=${dbNames.join(',')}`);
  console.log('');

  for (let i = 0; i < dbNames.length; i++) {
    const db = dbNames[i];
    const conn = { host: hostList[i], port: portList[i], user: userList[i], pass: passList[i] };
    const dst = `${db}_backup`;
    await backupOne(db, dst, conn);
  }

  console.log('完成');
})().catch(err => {
  console.error('[ERROR]', err.message || err);
  process.exit(1);
});

