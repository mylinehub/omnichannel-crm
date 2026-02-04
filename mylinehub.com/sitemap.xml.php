<?php
include __DIR__ . '/partials/config.php';

header('Content-Type: application/xml; charset=utf-8');

// Read articles JSON
$articles = [];
$articlesPath = __DIR__ . '/data/articles.json';

if (file_exists($articlesPath)) {
  $decoded = json_decode(file_get_contents($articlesPath), true);
  if (is_array($decoded)) $articles = $decoded;
}

// XML safe
function xml($s) {
  return htmlspecialchars((string)$s, ENT_XML1 | ENT_QUOTES, 'UTF-8');
}

// normalize BASE_URL to end with /
$base = rtrim((string)BASE_URL, '/') . '/';

// validate lastmod (YYYY-MM-DD only)
function valid_lastmod($date) {
  if (!is_string($date) || $date === '') return '';
  return preg_match('/^\d{4}-\d{2}-\d{2}$/', $date) ? $date : '';
}

echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

  <!-- Homepage -->
  <url>
    <loc><?= xml($base) ?></loc>
    <changefreq>weekly</changefreq>
    <priority>1.0</priority>
  </url>

  <!-- Articles Listing -->
  <url>
    <loc><?= xml($base . 'articles/') ?></loc>
    <changefreq>daily</changefreq>
    <priority>0.8</priority>
  </url>

  <!-- Policies -->
  <url><loc><?= xml($base . 'assets/general-policies.html') ?></loc></url>
  <url><loc><?= xml($base . 'assets/privacy-policy.html') ?></loc></url>
  <url><loc><?= xml($base . 'assets/refund-policy.html') ?></loc></url>
  <url><loc><?= xml($base . 'assets/return-policy.html') ?></loc></url>
  <url><loc><?= xml($base . 'assets/shipping-policy.html') ?></loc></url>
  <url><loc><?= xml($base . 'assets/terms-conditions.html') ?></loc></url>

  <!-- Articles from JSON -->
  <?php foreach ($articles as $a): ?>
    <?php
      if (empty($a['published'])) continue;

      $slug = isset($a['slug']) ? trim((string)$a['slug']) : '';
      if ($slug === '') continue;
      if (!preg_match('/^[a-z0-9-]+$/', $slug)) continue;

      $lastmod = '';
      if (!empty($a['dateModified'])) $lastmod = valid_lastmod($a['dateModified']);
      if ($lastmod === '' && !empty($a['datePublished'])) $lastmod = valid_lastmod($a['datePublished']);
    ?>
    <url>
      <loc><?= xml($base . 'articles/' . $slug) ?></loc>
      <?php if ($lastmod !== ''): ?><lastmod><?= xml($lastmod) ?></lastmod><?php endif; ?>
      <changefreq>monthly</changefreq>
      <priority>0.6</priority>
    </url>
  <?php endforeach; ?>

</urlset>
