const fs = require('fs');
const path = require('path');
const { spawnSync, spawn } = require('child_process');

/**
 * 将 cfg/mergeDb.cfg 中配置的库，逐个导出为 .sql（可选 .sql.gz）
 *
 * 用法：
 *   node backup_merge_sql.js
 *
 * 可选环境变量：
 *   - MERGE_DB_CFG: 指定配置文件路径（默认：MergeServer/cfg/mergeDb.cfg）
 *   - OUTPUT_DIR: 输出目录（默认：MergeServer/backups）
 *   - MYSQLDUMP_BIN: mysqldump 命令（默认：mysqldump）
 *   - DUMP_VERBOSE=1: 打印正在导出的表（mysqldump --verbose）
 *   - GZIP=1: 输出 gzip 压缩（需要系统有 gzip 命令）
 */

const SCRIPT_DIR = __dirname;
const CONFIG_FILE = process.env.MERGE_DB_CFG || path.join(SCRIPT_DIR, 'cfg', 'mergeDb.cfg');
const OUTPUT_DIR = process.env.OUTPUT_DIR || path.join(SCRIPT_DIR, 'backups');
const MYSQLDUMP_BIN = process.env.MYSQLDUMP_BIN || 'mysqldump';
const DUMP_VERBOSE = String(process.env.DUMP_VERBOSE || '').trim() === '1';
const USE_GZIP = String(process.env.GZIP || '').trim() === '1';

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

function ensureDir(dir) {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

function nowTag() {
  const d = new Date();
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}_${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`;
}

function checkGzipAvailable() {
  if (!USE_GZIP) return;
  const res = spawnSync('gzip', ['--version'], { encoding: 'utf8', stdio: ['ignore', 'ignore', 'ignore'] });
  if (res.status !== 0) {
    console.error('[ERROR] GZIP=1 但系统找不到 gzip 命令，请安装 gzip 或去掉 GZIP=1');
    process.exit(1);
  }
}

async function dumpOne(db, conn, outDir, tag) {
  ensureDir(outDir);
  const baseName = `${db}_${tag}.sql`;
  const outFile = path.join(outDir, USE_GZIP ? `${baseName}.gz` : baseName);

  console.log(`[DUMP] ${db} @ ${conn.host}:${conn.port} -> ${outFile}`);

  const dumpArgs = [
    '-h', conn.host, '-P', conn.port, '-u', conn.user, `-p${conn.pass}`,
    '--quick',
    '--single-transaction', '--routines', '--events', '--triggers',
    '--column-statistics=0', '--no-tablespaces',
    db,
  ];
  if (DUMP_VERBOSE) dumpArgs.splice(dumpArgs.length - 1, 0, '--verbose');

  await new Promise((resolve, reject) => {
    const dump = spawn(MYSQLDUMP_BIN, dumpArgs, { stdio: ['ignore', 'pipe', 'inherit'] });

    let outStream = null;
    let gzip = null;
    try {
      outStream = fs.createWriteStream(outFile, { flags: 'wx' }); // 防止覆盖
    } catch (e) {
      reject(e);
      return;
    }

    if (USE_GZIP) {
      gzip = spawn('gzip', ['-c'], { stdio: ['pipe', 'pipe', 'inherit'] });
      dump.stdout.pipe(gzip.stdin);
      gzip.stdout.pipe(outStream);
    } else {
      dump.stdout.pipe(outStream);
    }

    const onErr = (e) => {
      try { dump.kill(); } catch (_) {}
      try { if (gzip) gzip.kill(); } catch (_) {}
      try { outStream.close(); } catch (_) {}
      reject(e);
    };

    dump.on('error', onErr);
    if (gzip) gzip.on('error', onErr);
    outStream.on('error', onErr);

    let dumpCode = null;
    let gzipCode = null;

    const finish = () => {
      if (dumpCode === null) return;
      if (USE_GZIP && gzipCode === null) return;
      if (dumpCode === 0 && (!USE_GZIP || gzipCode === 0)) {
        console.log(`[OK] ${db} -> ${outFile}`);
        resolve();
      } else {
        reject(new Error(`mysqldump exit ${dumpCode}${USE_GZIP ? `, gzip exit ${gzipCode}` : ''}`));
      }
    };

    dump.on('exit', (code) => {
      dumpCode = code;
      try { dump.stdout.unpipe(); } catch (_) {}
      if (USE_GZIP) {
        try { gzip.stdin.end(); } catch (_) {}
      } else {
        try { outStream.end(); } catch (_) {}
      }
      finish();
    });

    if (USE_GZIP) {
      gzip.on('exit', (code) => {
        gzipCode = code;
        try { outStream.end(); } catch (_) {}
        finish();
      });
    } else {
      outStream.on('finish', () => finish());
    }
  });
}

(async () => {
  checkGzipAvailable();

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
  console.log(`  dbHosts=${hostList.join(',')} dbPorts=${portList.join(',')} dbUsernames=${userList.join(',')}`);
  console.log(`  dbNames=${dbNames.join(',')}`);
  console.log(`  outputDir=${OUTPUT_DIR}`);
  console.log(`  gzip=${USE_GZIP ? 'on' : 'off'} verbose=${DUMP_VERBOSE ? 'on' : 'off'}`);
  console.log('');

  const tag = nowTag();
  for (let i = 0; i < dbNames.length; i++) {
    const db = dbNames[i];
    const conn = { host: hostList[i], port: portList[i], user: userList[i], pass: passList[i] };
    await dumpOne(db, conn, OUTPUT_DIR, tag);
  }

  console.log('完成');
})().catch(err => {
  console.error('[ERROR]', err.message || err);
  process.exit(1);
});

