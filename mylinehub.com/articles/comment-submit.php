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

function mh_fail(string $msg, int $code = 200) {
  http_response_code($code);
  echo json_encode(['ok'=>false,'message'=>$msg], JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
  exit;
}

$slug = strtolower(trim((string)($_POST['slug'] ?? '')));
$slug = preg_replace('/[^a-z0-9-]/', '', $slug);

$name = trim((string)($_POST['name'] ?? ''));
$email = trim((string)($_POST['email'] ?? ''));
$phone = trim((string)($_POST['phone'] ?? ''));
$comment = trim((string)($_POST['comment'] ?? ''));

$hp_mid = trim((string)($_POST['hp_mid'] ?? ''));
$hp_end = trim((string)($_POST['hp_end'] ?? ''));

// Honeypots => silent success (spam bots think it posted)
if ($hp_mid !== '' || $hp_end !== '') {
  echo json_encode(['ok'=>true,'message'=>'Comment posted'], JSON_UNESCAPED_SLASHES);
  exit;
}

if ($slug === '') mh_fail('Validation failed');
$article = mh_find_article($slug);
if (!$article) mh_fail('Validation failed');

// Validation
if (mb_strlen($name) < 2 || mb_strlen($name) > 60) mh_fail('Validation failed');
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) mh_fail('Validation failed');

$digits = preg_replace('/\D+/', '', $phone);
if (strlen($digits) < 10 || strlen($digits) > 15) mh_fail('Validation failed');

if (mb_strlen($comment) < 10 || mb_strlen($comment) > 800) mh_fail('Validation failed');

// URLs max 1
$urlCount = 0;
if (preg_match_all('/https?:\/\/|www\./i', $comment, $m)) $urlCount = count($m[0]);
if ($urlCount > 1) mh_fail('Validation failed');

// Spam scoring (strict auto-approve)
$spamScore = 0;
$lc = mb_strtolower($comment);

if (preg_match('/https?:\/\//i', $comment)) $spamScore += 2;

$spamWords = ['casino','loan','crypto','bitcoin','forex','porn','viagra','free money','betting'];
foreach ($spamWords as $w) {
  if (strpos($lc, $w) !== false) $spamScore += 3;
}

if (preg_match('/(.)\1{6,}/u', $comment)) $spamScore += 2;
if (mb_strlen($comment) < 20) $spamScore += 1;

if ($spamScore >= 6) mh_fail('Validation failed');

// Rate limit: 1 per 60s, 5 per day
$ip = mh_client_ip();
$ipHash = mh_ip_hash($ip);

$rlDir = __DIR__ . '/../data/ratelimit';
if (!is_dir($rlDir)) @mkdir($rlDir, 0775, true);

$rlPath = $rlDir . '/' . $ipHash . '.json';
$now = time();
$today = gmdate('Y-m-d');

$rl = mh_read_json_bom($rlPath, ['ipHash'=>$ipHash,'last'=>0,'day'=>$today,'count'=>0]);
if (!is_array($rl)) $rl = ['ipHash'=>$ipHash,'last'=>0,'day'=>$today,'count'=>0];

if (($rl['day'] ?? '') !== $today) {
  $rl['day'] = $today;
  $rl['count'] = 0;
}

$last = (int)($rl['last'] ?? 0);
$count = (int)($rl['count'] ?? 0);

if ($now - $last < 60) mh_fail('Too many requests');
if ($count >= 5) mh_fail('Too many requests');

$rl['last'] = $now;
$rl['count'] = $count + 1;
mh_write_json_atomic($rlPath, $rl);

// Build comment item
$createdAt = gmdate('Y-m-d\TH:i:s\Z');
$id = 'cmt_' . $now . '_' . substr(bin2hex(random_bytes(4)), 0, 4);

$item = [
  'id' => $id,
  'name' => $name,
  'comment' => $comment,
  'createdAt' => $createdAt,
  'status' => 'approved',
  'ipHash' => $ipHash,
  'ua' => mh_ua_short(),
  'emailHash' => hash('md5', mb_strtolower($email)),
  'phoneMasked' => mh_mask_phone($phone),
  'ref' => mh_base() . 'articles/' . $slug
];

// Save to /data/comments/{slug}.json using flock
$commentsPath = __DIR__ . '/../data/comments/' . $slug . '.json';
$dir = dirname($commentsPath);
if (!is_dir($dir)) @mkdir($dir, 0775, true);

$fp = fopen($commentsPath, 'c+');
if (!$fp) mh_fail('Server error', 500);

if (!flock($fp, LOCK_EX)) {
  fclose($fp);
  mh_fail('Server error', 500);
}

$raw = stream_get_contents($fp);
$data = $raw ? json_decode($raw, true) : null;
if (!is_array($data)) $data = ['slug'=>$slug,'total'=>0,'lastUpdated'=>null,'items'=>[]];
if (!isset($data['items']) || !is_array($data['items'])) $data['items'] = [];

array_unshift($data['items'], $item);
if (count($data['items']) > 500) $data['items'] = array_slice($data['items'], 0, 500);

$data['total'] = count($data['items']);
$data['lastUpdated'] = $createdAt;

rewind($fp);
ftruncate($fp, 0);

$json = json_encode($data, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
$ok = ($json !== false) && (fwrite($fp, $json) !== false);

fflush($fp);
flock($fp, LOCK_UN);
fclose($fp);

if (!$ok) mh_fail('Server error', 500);

echo json_encode(['ok'=>true,'message'=>'Comment posted','item'=>$item], JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
