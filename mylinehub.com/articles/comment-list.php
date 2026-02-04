<?php
include __DIR__ . '/../partials/config.php';
include __DIR__ . '/_helpers.php';

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

$slug = $_GET['slug'] ?? '';
$slug = preg_replace('/[^a-z0-9-]/', '', strtolower($slug));

$page = (int)($_GET['page'] ?? 1);
$perPage = (int)($_GET['perPage'] ?? 5);
if ($page < 1) $page = 1;
if ($perPage < 1 || $perPage > 50) $perPage = 5;

if ($slug === '') {
  echo json_encode(['ok'=>false,'message'=>'Missing slug'], JSON_UNESCAPED_SLASHES);
  exit;
}

$article = mh_find_article($slug);
if (!$article) {
  echo json_encode(['ok'=>false,'message'=>'Article not found'], JSON_UNESCAPED_SLASHES);
  exit;
}

$data = mh_comments_read($slug);
$approved = mh_comments_approved_sorted($data['items']);

$total = count($approved);
$offset = ($page - 1) * $perPage;
$items = array_slice($approved, $offset, $perPage);

echo json_encode([
  'ok' => true,
  'total' => $total,
  'page' => $page,
  'perPage' => $perPage,
  'items' => $items
], JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
