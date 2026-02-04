<?php
include __DIR__ . '/../partials/config.php';
include __DIR__ . '/_helpers.php';

$base = mh_base();

// Load published
$articlesAll = mh_load_articles_published();

// Query
$q    = isset($_GET['q']) ? trim((string)$_GET['q']) : '';
$cat  = isset($_GET['cat']) ? trim((string)$_GET['cat']) : '';
$sort = isset($_GET['sort']) ? trim((string)$_GET['sort']) : 'newest';
if (!in_array($sort, ['newest','oldest','read'], true)) $sort = 'newest';

// Category counts from ALL published
$catCounts = [];
foreach ($articlesAll as $a) {
  $c = (string)($a['category'] ?? 'General');
  $catCounts[$c] = ($catCounts[$c] ?? 0) + 1;
}
ksort($catCounts);
$categories = array_keys($catCounts);

// Featured (latest 3 from ALL published)
$featured = array_slice($articlesAll, 0, 3);

// Apply filters to list
$articles = $articlesAll;

if ($cat !== '') {
  $articles = array_values(array_filter($articles, function($a) use ($cat){
    return ((string)($a['category'] ?? 'General') === $cat);
  }));
}

if ($q !== '') {
  $qLower = mb_strtolower($q);
  $articles = array_values(array_filter($articles, function($a) use ($qLower){
    $hay = mb_strtolower(
      ((string)($a['title'] ?? '')) . ' ' .
      ((string)($a['excerpt'] ?? '')) . ' ' .
      ((string)($a['contentHtml'] ?? ''))
    );
    return (mb_strpos($hay, $qLower) !== false);
  }));
}

// Sort
if ($sort === 'newest') {
  usort($articles, fn($a,$b)=> strcmp((string)($b['datePublished'] ?? ''), (string)($a['datePublished'] ?? '')));
} elseif ($sort === 'oldest') {
  usort($articles, fn($a,$b)=> strcmp((string)($a['datePublished'] ?? ''), (string)($b['datePublished'] ?? '')));
} else { // read time
  $toMin = function($rt){
    $rt = (string)$rt;
    if (preg_match('/(\d+)/', $rt, $m)) return (int)$m[1];
    return 0;
  };
  usort($articles, function($a,$b) use ($toMin){
    return $toMin($a['readingTime'] ?? '') <=> $toMin($b['readingTime'] ?? '');
  });
}

// Pagination
$perPage = 9;
$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;

$total = count($articles);
$totalPages = max(1, (int)ceil($total / $perPage));
if ($page > $totalPages) $page = $totalPages;

$start = ($page - 1) * $perPage;
$paged = array_slice($articles, $start, $perPage);

// SEO
$baseAbs = rtrim((string)SITE_URL, '/') . rtrim($base, '/');
if ($cat !== '' && $q === '') {
  $pageTitle = $cat . " Articles | MYLINEHUB";
  $pageDescription = "Browse " . $cat . " guides and tutorials by MYLINEHUB.";
  $pageCanonical = $baseAbs . "/articles/?cat=" . urlencode($cat);
} elseif ($q !== '') {
  $pageTitle = "Search: " . $q . " | MYLINEHUB Articles";
  $pageDescription = "Search results for “" . $q . "” on MYLINEHUB Articles.";
  $pageCanonical = $baseAbs . "/articles/?q=" . urlencode($q) . ($cat !== '' ? "&cat=" . urlencode($cat) : "");
} else {
  $pageTitle = "Articles | MYLINEHUB";
  $pageDescription = "Read tutorials and guides on WhatsApp automation, IVR, CRM, AI and sales workflows by MYLINEHUB.";
  $pageCanonical = $baseAbs . "/articles/";
}
$pageType = "website";
$robots = "index, follow";
$showHero = false;
$activeNav = 'articles';
?>
<!DOCTYPE html>
<html class="no-js" lang="en">
<?php include __DIR__ . '/../partials/head.php'; ?>
<body class="mh-articles-page">

<?php include __DIR__ . '/../partials/header.php'; ?>

<link rel="stylesheet" href="<?= esc($base) ?>assets/css/articles.css?v=2">

<section class="mh-hero">
  <div class="mh-wrap" data-mh-articles data-base="<?= esc($base) ?>">

    <div class="mh-headline">
      <div class="bar"></div>
      <h1>Knowledge Hub</h1>
      <p>Practical guides and playbooks for WhatsApp automation, IVR, CRM, AI hiring, and performance marketing.</p>
    </div>

    <!-- Featured + Sort -->
    <div class="mh-featured">
      <div class="mh-featured-head">
        <div class="mh-featured-title">Latest & Featured</div>

        <div class="mh-sort">
          <label for="mhSort">Sort</label>
          <select id="mhSort" name="sort" form="mhFilterForm">
            <option value="newest" <?= $sort==='newest'?'selected':'' ?>>Newest</option>
            <option value="oldest" <?= $sort==='oldest'?'selected':'' ?>>Oldest</option>
            <option value="read"   <?= $sort==='read'?'selected':'' ?>>Reading time</option>
          </select>
        </div>
      </div>

      <div class="mh-featured-grid">
        <?php foreach ($featured as $f): ?>
          <?php
            $fs = (string)($f['slug'] ?? '');
            $ft = (string)($f['title'] ?? '');
            $fd = (string)($f['datePublished'] ?? '');
            $fr = (string)($f['readingTime'] ?? '');
            $fl = $base . 'articles/' . rawurlencode($fs);
          ?>
          <div class="mh-featured-item">
            <a href="<?= esc($fl) ?>"><?= esc($ft) ?></a>
            <div class="mh-featured-meta"><?= esc($fd) ?><?= $fr?(' • '.esc($fr)) : '' ?></div>
          </div>
        <?php endforeach; ?>
      </div>
    </div>

    <!-- Search + Filters -->
    <form id="mhFilterForm" method="get" class="mh-toolbar" style="margin-top:14px;">
      <div class="mh-search">
        <input
          type="text"
          name="q"
          value="<?= esc($q) ?>"
          placeholder="Search articles…"
          data-mh-articles-search
        />
        <?php if ($cat !== ''): ?>
          <input type="hidden" name="cat" value="<?= esc($cat) ?>" />
        <?php endif; ?>
        <input type="hidden" name="sort" value="<?= esc($sort) ?>">
        <button type="submit">Search</button>
      </div>

      <div class="mh-chips">
        <a class="mh-chip <?= $cat===''?'active':'' ?>" href="<?= esc($base) ?>articles/?<?= http_build_query(['sort'=>$sort]) ?>">
          All (<?= (int)count($articlesAll) ?>)
        </a>

        <?php foreach ($categories as $c): ?>
          <?php
            $isActive = ($cat === $c);
            $url = $base . 'articles/?' . http_build_query([
              'cat' => $c,
              'sort' => $sort,
              'q' => $q
            ]);
          ?>
          <a class="mh-chip <?= $isActive?'active':'' ?>" href="<?= esc($url) ?>">
            <?= esc($c) ?> (<?= (int)($catCounts[$c] ?? 0) ?>)
          </a>
        <?php endforeach; ?>
      </div>
    </form>

    <!-- Results meta -->
    <div class="mh-results">
      <div>
        Showing <b><?= count($paged) ?></b> of <b><?= $total ?></b> results
        <?php if ($cat !== ''): ?> in <b><?= esc($cat) ?></b><?php endif; ?>
        <?php if ($q !== ''): ?> for “<b><?= esc($q) ?></b>”<?php endif; ?>
      </div>

      <?php if ($q !== '' || $cat !== ''): ?>
        <a class="mh-clear" href="<?= esc($base) ?>articles/?<?= http_build_query(['sort'=>$sort]) ?>">Clear filters</a>
      <?php endif; ?>
    </div>

    <!-- Grid -->
    <?php if (count($paged) === 0): ?>
      <div class="mh-empty" style="margin-top:18px;">
        <h3 style="margin:0 0 6px;color:#0b4b8c;font-weight:950;">No articles found</h3>
        <p style="margin:0;color:#475569;font-weight:650;">Try a different keyword or category.</p>
      </div>
    <?php else: ?>
      <div class="mh-grid">
        <?php foreach ($paged as $a): ?>
          <?php include __DIR__ . '/_article-card.php'; ?>
        <?php endforeach; ?>
      </div>
    <?php endif; ?>

    <!-- Pagination -->
    <?php if ($totalPages > 1): ?>
      <div class="mh-pagination">
        <?php
          $qs = [];
          if ($q !== '') $qs['q'] = $q;
          if ($cat !== '') $qs['cat'] = $cat;
          if ($sort !== '') $qs['sort'] = $sort;

          $mkUrl = function($p) use ($base, $qs) {
            $qs2 = $qs;
            $qs2['page'] = $p;
            return $base . 'articles/?' . http_build_query($qs2);
          };
        ?>

        <a class="mh-pg <?= $page<=1?'disabled':'' ?>" href="<?= $page<=1?'#':$mkUrl($page-1) ?>">← Prev</a>

        <?php
          $startP = max(1, $page-2);
          $endP   = min($totalPages, $page+2);

          if ($startP > 1) echo '<span class="mh-pg dots">…</span>';

          for ($p=$startP; $p<=$endP; $p++){
            $active = ($p === $page) ? 'active' : '';
            echo '<a class="mh-pg '.$active.'" href="'.$mkUrl($p).'">'.$p.'</a>';
          }

          if ($endP < $totalPages) echo '<span class="mh-pg dots">…</span>';
        ?>

        <a class="mh-pg <?= $page>=$totalPages?'disabled':'' ?>" href="<?= $page>=$totalPages?'#':$mkUrl($page+1) ?>">Next →</a>
      </div>
    <?php endif; ?>

  </div>
</section>

<?php include __DIR__ . '/../partials/footer.php'; ?>
<?php include __DIR__ . '/../partials/scripts.php'; ?>

<script src="<?= esc($base) ?>assets/js/articles.js?v=2"></script>
</body>
</html>
