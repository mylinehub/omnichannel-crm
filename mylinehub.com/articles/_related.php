<?php
// Input: $article, $allArticles, $limit
// Output: $related

$related = [];
$limit = isset($limit) ? (int)$limit : 4;
if ($limit < 1) $limit = 4;

$slug = $article['slug'] ?? '';
$cat  = $article['category'] ?? '';
$tags = $article['tags'] ?? [];
if (!is_array($tags)) $tags = [];

$scored = [];

foreach ($allArticles as $a) {
  if (!is_array($a)) continue;
  if (($a['slug'] ?? '') === $slug) continue;

  $score = 0;

  if ($cat !== '' && (($a['category'] ?? '') === $cat)) $score += 5;

  $aTags = $a['tags'] ?? [];
  if (!is_array($aTags)) $aTags = [];

  if ($tags && $aTags) {
    $common = array_intersect($tags, $aTags);
    $score += min(5, count($common)) * 2;
  }

  if ($score > 0) $scored[] = ['score'=>$score,'a'=>$a];
}

usort($scored, function($x, $y){
  if ($y['score'] === $x['score']) {
    return strcmp((string)($y['a']['datePublished'] ?? ''), (string)($x['a']['datePublished'] ?? ''));
  }
  return $y['score'] <=> $x['score'];
});

foreach ($scored as $row) {
  $related[] = $row['a'];
  if (count($related) >= $limit) break;
}
