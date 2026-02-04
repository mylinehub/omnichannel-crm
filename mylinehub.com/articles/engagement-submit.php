<?php
include __DIR__ . '/../partials/config.php';
include __DIR__ . '/_helpers.php';

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
  http_response_code(405);
  echo json_encode(['ok'=>false,'message'=>'Method not allowed']);
  exit;
}

function mh_fail($msg, $code=200){
  http_response_code($code);
  echo json_encode(['ok'=>false,'message'=>$msg], JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE);
  exit;
}

$slug = strtolower(trim((string)($_POST['slug'] ?? '')));
$slug = preg_replace('/[^a-z0-9-]/', '', $slug);
if ($slug === '') mh_fail('Validation failed');

$article = mh_find_article($slug);
if (!$article) mh_fail('Validation failed');

// Engagement inputs
$helpful = trim((string)($_POST['helpful'] ?? ''));     // yes/no
$reaction = trim((string)($_POST['reaction'] ?? ''));   // like/insightful/love/confused
$follow = trim((string)($_POST['follow'] ?? ''));       // on (or empty)
$reason = trim((string)($_POST['reason'] ?? ''));       // too_short/unclear/outdated/missing_steps/other

$allowedHelpful = ['yes','no',''];
$allowedReaction = ['like','insightful','love','confused',''];
$allowedFollow = ['on',''];
$allowedReason = ['too_short','unclear','outdated','missing_steps','other',''];

if (!in_array($helpful, $allowedHelpful, true)) mh_fail('Validation failed');
if (!in_array($reaction, $allowedReaction, true)) mh_fail('Validation failed');
if (!in_array($follow, $allowedFollow, true)) mh_fail('Validation failed');
if (!in_array($reason, $allowedReason, true)) mh_fail('Validation failed');

if ($helpful === '' && $reaction === '' && $follow === '' && $reason === '') {
  mh_fail('Validation failed');
}

// If helpful != no, reason should be empty
if ($helpful !== 'no') $reason = '';

$ip = mh_client_ip();
$ipHash = mh_ip_hash($ip);
$ua = mh_ua_short();
$nowIso = gmdate('Y-m-d\TH:i:s\Z');

// File path
$path = __DIR__ . '/../data/engagement/' . $slug . '.json';
$dir = dirname($path);
if (!is_dir($dir)) @mkdir($dir, 0775, true);

// Concurrency lock
$fp = fopen($path, 'c+');
if (!$fp) mh_fail('Server error', 500);
if (!flock($fp, LOCK_EX)) { fclose($fp); mh_fail('Server error', 500); }

$raw = stream_get_contents($fp);
$doc = $raw ? json_decode($raw, true) : null;

if (!is_array($doc)) {
  $doc = [
    'slug' => $slug,
    'lastUpdated' => null,
    'totals' => new stdClass(),
    'byIp' => new stdClass()
  ];
}
if (!isset($doc['totals']) || !is_array($doc['totals'])) $doc['totals'] = [];
if (!isset($doc['byIp']) || !is_array($doc['byIp'])) $doc['byIp'] = [];

if (isset($doc['byIp'][$ipHash])) {
  // Only once per IP per slug
  flock($fp, LOCK_UN);
  fclose($fp);
  echo json_encode(['ok'=>true,'message'=>'Already recorded'], JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE);
  exit;
}

// Store
$item = [
  'ts' => $nowIso,
  'helpful' => $helpful !== '' ? $helpful : null,
  'follow' => $follow !== '' ? $follow : null,
  'reaction' => $reaction !== '' ? $reaction : null,
  'reason' => $reason !== '' ? $reason : null,
  'ua' => $ua
];

$doc['byIp'][$ipHash] = $item;

// Totals increment helper
$inc = function($key) use (&$doc){
  if ($key === '') return;
  $doc['totals'][$key] = (int)($doc['totals'][$key] ?? 0) + 1;
};

if ($helpful === 'yes') $inc('helpful_yes');
if ($helpful === 'no') $inc('helpful_no');
if ($follow === 'on') $inc('follow_on');

if ($reaction !== '') $inc('reaction_' . $reaction);
if ($reason !== '') $inc('reason_' . $reason);

$doc['lastUpdated'] = $nowIso;

// Write back
rewind($fp);
ftruncate($fp, 0);

$json = json_encode($doc, JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE|JSON_PRETTY_PRINT);
$ok = ($json !== false) && (fwrite($fp, $json) !== false);

fflush($fp);
flock($fp, LOCK_UN);
fclose($fp);

if (!$ok) mh_fail('Server error', 500);

echo json_encode(['ok'=>true,'message'=>'Saved'], JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE);
