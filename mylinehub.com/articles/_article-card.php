<?php
// expects $a + $base in scope

$slug = (string)($a['slug'] ?? '');
$title = (string)($a['title'] ?? '');
$excerpt = (string)($a['excerpt'] ?? '');
$category = (string)($a['category'] ?? 'General');
$date = (string)($a['datePublished'] ?? '');
$reading = (string)($a['readingTime'] ?? '');
$link = $base . 'articles/' . rawurlencode($slug);

// Optional image (not required for list)
$img = (string)($a['coverImage'] ?? '');
if ($img === '') $img = (string)($a['featuredImage'] ?? '');
?>

<div class="mh-card">
  <div class="mh-badge">
    <?= esc($category) ?>
    <?php if ($reading !== ''): ?><small>• <?= esc($reading) ?></small><?php endif; ?>
  </div>

  <div class="mh-card-title">
    <a href="<?= esc($link) ?>"><?= esc($title) ?></a>
  </div>

  <?php if ($excerpt !== ''): ?>
    <div class="mh-excerpt"><?= esc($excerpt) ?></div>
  <?php else: ?>
    <div class="mh-excerpt">Read this article on MYLINEHUB knowledge hub.</div>
  <?php endif; ?>

  <div class="mh-meta">
    <?php if ($date !== ''): ?><span><?= esc($date) ?></span><?php endif; ?>
    <?php if (!empty($a['tags']) && is_array($a['tags'])): ?>
      <span>• <?= esc(implode(', ', array_slice($a['tags'], 0, 3))) ?></span>
    <?php endif; ?>
  </div>

  <a class="mh-readmore" href="<?= esc($link) ?>">Read article →</a>
</div>
