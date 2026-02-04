<?php
include __DIR__ . '/../partials/config.php';
include __DIR__ . '/_helpers.php';

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
  http_response_code(405);
  echo json_encode(['ok'=>false,'message'=>'Method not allowed'], JSON_UNESCAPED_SLASHES);
  exit;
}

function mh_act_fail($msg, $code=200){
  http_response_code($code);
  echo json_encode(['ok'=>false,'message'=>$msg], JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE);
  exit;
}

$raw = file_get_contents('php://input');
if (!is_string($raw) || $raw === '') mh_act_fail('Bad request');

$payload = json_decode($raw, true);
if (!is_array($payload)) mh_act_fail('Bad request');

$event = (string)($payload['event'] ?? '');
$path  = (string)($payload['path'] ?? '');
$slug  = (string)($payload['slug'] ?? '');
$data  = $payload['data'] ?? null;

$event = trim($event);
$path  = trim($path);
$slug  = trim($slug);

if ($event === '' || strlen($event) > 40) mh_act_fail('Validation failed');
if ($path === '' || strlen($path) > 160) mh_act_fail('Validation failed');
if ($slug !== '' && !preg_match('/^[a-z0-9-]+$/', $slug)) $slug = '';

$allowed = [
  'page_view','toc_click',
  'share_copy','share_whatsapp','share_linkedin',
  'comment_modal_open','comment_post_ok',
  'scroll_25','scroll_50','scroll_75','scroll_90',
  'time_10s','time_30s','time_60s'
];
if (!in_array($event, $allowed, true)) mh_act_fail('Validation failed');

// Rate limit activity logs (per IP hash): max 1 event per second, max 200 per day
$ip = mh_client_ip();
$ipHash = mh_ip_hash($ip);

$rlDir = __DIR__ . '/../data/ratelimit-activity';
if (!is_dir($rlDir)) @mkdir($rlDir, 0775, true);

$rlPath = $rlDir . '/' . $ipHash . '.json';
$now = time();
$today = gmdate('Y-m-d');

$rl = mh_read_json_bom($rlPath, ['ipHash'=>$ipHash,'last'=>0,'day'=>$today,'count'=>0]);
if (!is_array($rl)) $rl = ['ipHash'=>$ipHash,'last'=>0,'day'=>$today,'count'=>0];

if (($rl['day'] ?? '') !== $today) { $rl['day'] = $today; $rl['count'] = 0; }

$last = (int)($rl['last'] ?? 0);
$count = (int)($rl['count'] ?? 0);

if ($now - $last < 1) mh_act_fail('Too many requests');
if ($count >= 200) mh_act_fail('Too many requests');

$rl['last'] = $now;
$rl['count'] = $count + 1;
mh_write_json_atomic($rlPath, $rl);

// Write activity to /data/activity/YYYY-MM-DD.json
$actDir = __DIR__ . '/../data/activity';
if (!is_dir($actDir)) @mkdir($actDir, 0775, true);

$dayFile = $actDir . '/' . $today . '.json';
$createdAt = gmdate('Y-m-d\TH:i:s\Z');

$item = [
  'ts' => $createdAt,
  'event' => $event,
  'path' => $path,
  'slug' => $slug,
  'ipHash' => $ipHash,
  'ua' => mh_ua_short(),
  'data' => is_array($data) ? $data : null
];

// Keep file bounded (max ~5000 events/day)
$fp = fopen($dayFile, 'c+');
if (!$fp) mh_act_fail('Server error', 500);
if (!flock($fp, LOCK_EX)) { fclose($fp); mh_act_fail('Server error', 500); }

$existingRaw = stream_get_contents($fp);
$doc = $existingRaw ? json_decode($existingRaw, true) : null;
if (!is_array($doc)) $doc = ['date'=>$today,'total'=>0,'items'=>[]];
if (!isset($doc['items']) || !is_array($doc['items'])) $doc['items'] = [];

$doc['items'][] = $item;
if (count($doc['items']) > 5000) $doc['items'] = array_slice($doc['items'], -5000);

$doc['total'] = count($doc['items']);
$doc['lastUpdated'] = $createdAt;

rewind($fp);
ftruncate($fp, 0);

$json = json_encode($doc, JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE|JSON_PRETTY_PRINT);
$ok = ($json !== false) && (fwrite($fp, $json) !== false);

fflush($fp);
flock($fp, LOCK_UN);
fclose($fp);

if (!$ok) mh_act_fail('Server error', 500);

echo json_encode(['ok'=>true], JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE);
