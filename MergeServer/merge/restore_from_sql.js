const fs = require('fs');
const path = require('path');
const { spawnSync, spawn } = require('child_process');

/**
 * 从 backup_merge_sql.js 导出的 .sql / .sql.gz 文件回灌数据库。
 *
 * 默认行为：回灌 cfg/mergeDb.cfg 里的所有 dbNames（会 DROP 原库再重建）
 * 常用：只回灌主库（dbNames 第一个）：
 *   RESTORE_SCOPE=master node restore_from_sql.js
 *
 * 可选环境变量：
 *   - MERGE_DB_CFG: 配置文件路径（默认：MergeServer/merge/cfg/mergeDb.cfg）
 *   - INPUT_DIR: SQL 文件目录（默认：MergeServer/backups）
 *   - SQL_FILE: 直接指定要导入的 SQL 文件（仅对 master 生效，省得自动找最新）
 *   - RESTORE_SCOPE=master|all: 默认 all
 *   - DRY_RUN=1: 只打印不执行
 *   - MYSQL_BIN: mysql 命令（默认 mysql）
 */

const SCRIPT_DIR = __dirname;
const CONFIG_FILE = process.env.MERGE_DB_CFG || path.join(SCRIPT_DIR, 'cfg', 'mergeDb.cfg');
const INPUT_DIR = process.env.INPUT_DIR || path.join(SCRIPT_DIR, '..', 'backups');
const MYSQL_BIN = process.env.MYSQL_BIN || 'mysql';

const RESTORE_SCOPE = (process.env.RESTORE_SCOPE || 'all').trim().toLowerCase(); // all | master
const DRY_RUN = String(process.env.DRY_RUN || '').trim() === '1';
const SQL_FILE = (process.env.SQL_FILE || '').trim();
// 导入进度（默认开启）。关闭：PROGRESS=0
const SHOW_PROGRESS = String(process.env.PROGRESS || '1').trim() !== '0';
const PROGRESS_INTERVAL_MS = Number(process.env.PROGRESS_INTERVAL_MS || '5000');

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

function listCandidateSqlFiles(db) {
  if (!fs.existsSync(INPUT_DIR)) return [];
  const files = fs.readdirSync(INPUT_DIR);
  // 支持：game_10001_YYYYMMDD_HHMMSS.sql / .sql.gz
  return files
    .filter(f => f.startsWith(`${db}_`) && (f.endsWith('.sql') || f.endsWith('.sql.gz')))
    .map(f => path.join(INPUT_DIR, f))
    .sort((a, b) => fs.statSync(b).mtimeMs - fs.statSync(a).mtimeMs); // newest first
}

function ensureGzipAvailable() {
  const res = spawnSync('gzip', ['--version'], { encoding: 'utf8', stdio: ['ignore', 'ignore', 'ignore'] });
  if (res.status !== 0) {
    console.error('[ERROR] 需要导入 .sql.gz 但系统找不到 gzip 命令，请安装 gzip 或使用 .sql 文件');
    process.exit(1);
  }
}

function fmtBytes(bytes) {
  if (!Number.isFinite(bytes)) return String(bytes);
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let n = bytes;
  let i = 0;
  while (n >= 1024 && i < units.length - 1) {
    n /= 1024;
    i++;
  }
  return `${n.toFixed(i === 0 ? 0 : 2)}${units[i]}`;
}

function startProgressLogger({ totalBytes, getBytes, label }) {
  if (!SHOW_PROGRESS) return { stop: () => {} };
  let lastBytes = 0;
  let lastTs = Date.now();
  const timer = setInterval(() => {
    const now = Date.now();
    const cur = getBytes();
    const dt = Math.max(1, now - lastTs);
    const delta = Math.max(0, cur - lastBytes);
    const speed = (delta * 1000) / dt;
    const pct = totalBytes > 0 ? ((cur / totalBytes) * 100).toFixed(2) : '??';
    console.log(`[PROGRESS] ${label} ${fmtBytes(cur)} / ${totalBytes > 0 ? fmtBytes(totalBytes) : 'unknown'} (${pct}%) speed=${fmtBytes(speed)}/s`);
    lastBytes = cur;
    lastTs = now;
  }, PROGRESS_INTERVAL_MS);
  return { stop: () => clearInterval(timer) };
}

function importSqlFile(conn, db, filePath) {
  console.log(`[IMPORT] ${filePath} -> ${db}`);
  if (DRY_RUN) {
    console.log(`[DRY] DROP/CREATE 已执行完毕后，导入命令将会运行：${filePath.endsWith('.gz') ? `gzip -dc "${filePath}" | ${MYSQL_BIN} ... ${db}` : `${MYSQL_BIN} ... ${db} < "${filePath}"`}`);
    return Promise.resolve();
  }

  return new Promise((resolve, reject) => {
    const mysqlArgs = ['-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`, db];

    if (filePath.endsWith('.gz')) {
      ensureGzipAvailable();
      const gunzip = spawn('gzip', ['-dc', filePath], { stdio: ['ignore', 'pipe', 'inherit'] });
      const mysql = spawn(MYSQL_BIN, mysqlArgs, { stdio: ['pipe', 'inherit', 'inherit'] });

      // gzip 解压后的总字节无法提前得知，这里只显示已处理字节与速度
      let bytes = 0;
      gunzip.stdout.on('data', chunk => { bytes += chunk.length; });
      const prog = startProgressLogger({ totalBytes: 0, getBytes: () => bytes, label: `${db} (gunzip->mysql)` });

      gunzip.stdout.pipe(mysql.stdin);

      let gunzipDone = false, mysqlDone = false;
      let gunzipCode = 0, mysqlCode = 0;

      const finish = () => {
        if (!gunzipDone || !mysqlDone) return;
        prog.stop();
        if (gunzipCode === 0 && mysqlCode === 0) resolve();
        else reject(new Error(`gzip exit ${gunzipCode}, mysql exit ${mysqlCode}`));
      };

      gunzip.on('error', reject);
      mysql.on('error', reject);
      gunzip.on('exit', code => {
        gunzipDone = true; gunzipCode = code;
        try { mysql.stdin.end(); } catch (_) {}
        finish();
      });
      mysql.on('exit', code => {
        mysqlDone = true; mysqlCode = code;
        if (gunzip.exitCode === null) {
          try { gunzip.kill(); } catch (_) {}
        }
        finish();
      });
    } else {
      const inStream = fs.createReadStream(filePath);
      const mysql = spawn(MYSQL_BIN, mysqlArgs, { stdio: ['pipe', 'inherit', 'inherit'] });

      const totalBytes = (() => {
        try { return fs.statSync(filePath).size; } catch (_) { return 0; }
      })();
      let bytes = 0;
      inStream.on('data', chunk => { bytes += chunk.length; });
      const prog = startProgressLogger({ totalBytes, getBytes: () => bytes, label: `${db} (file->mysql)` });

      inStream.pipe(mysql.stdin);

      mysql.on('error', reject);
      inStream.on('error', reject);
      mysql.on('exit', code => {
        prog.stop();
        if (code === 0) resolve();
        else reject(new Error(`mysql exit ${code}`));
      });
    }
  });
}

async function restoreOne(db, conn, filePath) {
  if (!filePath) {
    console.error(`[ERROR] 找不到 ${db} 的 SQL 备份文件。请检查 INPUT_DIR=${INPUT_DIR} 或设置 SQL_FILE`);
    process.exit(1);
  }
  console.log(`[DROP] ${db}`);
  mysqlExec(conn, `DROP DATABASE IF EXISTS \`${db}\`;`);
  console.log(`[CREATE] ${db}`);
  mysqlExec(conn, `CREATE DATABASE IF NOT EXISTS \`${db}\` DEFAULT CHARACTER SET utf8mb4;`);
  await importSqlFile(conn, db, filePath);
  console.log(`[OK] ${db} 已从 SQL 回灌完成`);
}

(async () => {
  const cfg = parseCfg(CONFIG_FILE);
  if (!cfg.dbNames) {
    console.error('[ERROR] 配置项 dbNames 为空');
    process.exit(1);
  }

  const dbNames = cfg.dbNames.split(',').map(s => s.trim()).filter(Boolean);
  const hostList = toList(cfg.dbHosts, dbNames.length, '127.0.0.1');
  const portList = toList(cfg.dbPorts, dbNames.length, '3306');
  const userList = toList(cfg.dbUsernames, dbNames.length, 'root');
  const passList = toList(cfg.dbPasswords, dbNames.length, '');

  console.log('使用配置:');
  console.log(`  cfg=${CONFIG_FILE}`);
  console.log(`  inputDir=${INPUT_DIR}`);
  console.log(`  restoreScope=${RESTORE_SCOPE} dryRun=${DRY_RUN ? 'on' : 'off'}`);
  console.log(`  dbHosts=${hostList.join(',')} dbPorts=${portList.join(',')} dbUsernames=${userList.join(',')}`);
  console.log(`  dbNames=${dbNames.join(',')}`);
  if (SQL_FILE) console.log(`  SQL_FILE=${SQL_FILE}`);
  console.log('');

  const max = RESTORE_SCOPE === 'master' ? 1 : dbNames.length;
  for (let i = 0; i < max; i++) {
    const db = dbNames[i];
    const conn = { host: hostList[i], port: portList[i], user: userList[i], pass: passList[i] };

    let filePath = '';
    if (i === 0 && SQL_FILE) {
      filePath = path.isAbsolute(SQL_FILE) ? SQL_FILE : path.join(process.cwd(), SQL_FILE);
    } else {
      const candidates = listCandidateSqlFiles(db);
      filePath = candidates[0] || '';
    }

    await restoreOne(db, conn, filePath);
  }

  console.log('完成');
})().catch(err => {
  console.error('[ERROR]', err.message || err);
  process.exit(1);
});

