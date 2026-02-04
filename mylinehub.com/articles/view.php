<?php
include __DIR__ . '/../partials/config.php';
include __DIR__ . '/_helpers.php';

$base = mh_base();

$slug = isset($_GET['slug']) ? trim((string)$_GET['slug']) : '';
$slug = strtolower($slug);
$slug = preg_replace('/[^a-z0-9-]/', '', $slug);

/**
 * ✅ FIX #1: Handle empty slug (avoid trying to load "")
 * - We keep it simple: show the same 404 page block you already have.
 * - No extra dependencies, no removal.
 */
if ($slug === '') {
  http_response_code(404);

  $pageTitle = "Not Found | MYLINEHUB";
  $pageDescription = "The page you are looking for does not exist.";
  $pageCanonical = mh_site() . $base . "articles/";
  $robots = "noindex, follow";
  $pageType = "website";
  $showHero = false;
  $activeNav = 'articles';

  ?>
  <!DOCTYPE html>
  <html class="no-js" lang="en">
  <?php include __DIR__ . '/../partials/head.php'; ?>
  <body>
  <?php include __DIR__ . '/../partials/header.php'; ?>

  <section style="padding:90px 15px; background:linear-gradient(135deg,#f9fafc,#e9f5ff);">
    <div class="container" style="max-width:900px; margin:auto;">
      <div style="background:#fff;border-radius:18px;padding:28px;border:1px solid rgba(148,163,184,0.35);box-shadow:0 12px 30px rgba(15,23,42,0.06); text-align:center;">
        <h1 style="font-size:34px; font-weight:900; color:#0b4b8c;">404 — Page not found</h1>
        <p style="color:#475569; margin:12px 0 0;">
          This article/page doesn’t exist yet. Try the articles list or go back home.
        </p>
        <div style="margin-top:18px; display:flex; gap:12px; justify-content:center; flex-wrap:wrap;">
          <a href="<?= esc($base) ?>articles/" style="padding:10px 16px;border-radius:12px;text-decoration:none;font-weight:800;color:#fff;background:linear-gradient(90deg,#33c8c1,#119bd2);">Browse Articles</a>
          <a href="<?= esc($base) ?>" style="padding:10px 16px;border-radius:12px;text-decoration:none;font-weight:800;color:#0b4b8c;background:#fff;border:1px solid rgba(148,163,184,0.35);">Go Home</a>
        </div>
      </div>
    </div>
  </section>

  <?php include __DIR__ . '/../partials/footer.php'; ?>
  <?php include __DIR__ . '/../partials/scripts.php'; ?>
  </body>
  </html>
  <?php
  exit;
}

$article = mh_find_article($slug);

if (!$article) {
  http_response_code(404);

  $pageTitle = "Not Found | MYLINEHUB";
  $pageDescription = "The page you are looking for does not exist.";
  $pageCanonical = mh_site() . $base . "articles/";
  $robots = "noindex, follow";
  $pageType = "website";
  $showHero = false;
  $activeNav = 'articles';

  ?>
  <!DOCTYPE html>
  <html class="no-js" lang="en">
  <?php include __DIR__ . '/../partials/head.php'; ?>
  <body>
  <?php include __DIR__ . '/../partials/header.php'; ?>

  <section style="padding:90px 15px; background:linear-gradient(135deg,#f9fafc,#e9f5ff);">
    <div class="container" style="max-width:900px; margin:auto;">
      <div style="background:#fff;border-radius:18px;padding:28px;border:1px solid rgba(148,163,184,0.35);box-shadow:0 12px 30px rgba(15,23,42,0.06); text-align:center;">
        <h1 style="font-size:34px; font-weight:900; color:#0b4b8c;">404 — Page not found</h1>
        <p style="color:#475569; margin:12px 0 0;">
          This article/page doesn’t exist yet. Try the articles list or go back home.
        </p>
        <div style="margin-top:18px; display:flex; gap:12px; justify-content:center; flex-wrap:wrap;">
          <a href="<?= esc($base) ?>articles/" style="padding:10px 16px;border-radius:12px;text-decoration:none;font-weight:800;color:#fff;background:linear-gradient(90deg,#33c8c1,#119bd2);">Browse Articles</a>
          <a href="<?= esc($base) ?>" style="padding:10px 16px;border-radius:12px;text-decoration:none;font-weight:800;color:#0b4b8c;background:#fff;border:1px solid rgba(148,163,184,0.35);">Go Home</a>
        </div>
      </div>
    </div>
  </section>

  <?php include __DIR__ . '/../partials/footer.php'; ?>
  <?php include __DIR__ . '/../partials/scripts.php'; ?>
  </body>
  </html>
  <?php
  exit;
}

// Fields
$title         = (string)($article['title'] ?? 'Article');
$desc          = (string)($article['excerpt'] ?? '');
$datePublished = (string)($article['datePublished'] ?? '');
$dateModified  = (string)($article['dateModified'] ?? $datePublished);
$author        = (string)($article['author'] ?? 'MYLINEHUB');
$category      = (string)($article['category'] ?? 'General');
$readingTime   = (string)($article['readingTime'] ?? '');

// contentHtml can be either:
// 1) actual HTML string
// 2) a path like "/data/articles/html/slug.html"
$contentHtml = (string)($article['contentHtml'] ?? '');

/**
 * ✅ FIX #2: Load contentHtml if it’s a file path safely
 * - Keep your feature (string HTML OR file path)
 * - Add traversal guard ("..")
 * - Better path detection (no PHP 8-only str_ends_with/str_starts_with)
 */
if ($contentHtml !== '') {

  $looksLikePath = (strpos($contentHtml, '<') === false) && (
    (substr($contentHtml, -5) === '.html') ||
    (isset($contentHtml[0]) && $contentHtml[0] === '/')
  );

  if ($looksLikePath) {

    // Block traversal
    if (strpos($contentHtml, '..') !== false) {
      $contentHtml = '<p style="color:#64748b;font-weight:800;">Invalid article path.</p>';
    } else {
      $abs = rtrim($_SERVER['DOCUMENT_ROOT'] ?? '', '/') . '/' . ltrim($contentHtml, '/');

      if (is_file($abs)) {
        $contentHtml = (string)file_get_contents($abs);
      } else {
        // fallback: show safe message instead of raw path
        $contentHtml = '<p style="color:#64748b;font-weight:800;">Article content missing. (HTML file not found)</p>';
      }
    }
  }
}

$cover = (string)($article['coverImage'] ?? '');
if ($cover === '') $cover = (string)($article['featuredImage'] ?? '');

/**
 * ✅ FIX #3: Ensure cover URL is clean absolute if relative
 * - Avoid double slashes
 * - Keep remote URLs as-is
 */
if ($cover !== '' && strpos($cover, 'http') !== 0) {
  $cover = rtrim(mh_site(), '/') . '/' . ltrim($cover, '/');
}

$pageCanonical = mh_site() . $base . "articles/" . rawurlencode($slug);

$pageTitle = $title . " | MYLINEHUB";
$pageDescription = $desc !== '' ? $desc : "Read this guide by MYLINEHUB.";
$pageType = "article";
$robots = "index, follow";
$showHero = false;
$activeNav = 'articles';

if ($cover !== '') {
  $ogImage = (strpos($cover, 'http') === 0) ? $cover : (rtrim(mh_site(), '/') . '/' . ltrim($cover, '/'));
}

// TOC (adds ids into h2)
include __DIR__ . '/_toc.php';
$contentHtml = $contentHtmlWithIds;

// Related
$allArticles = mh_load_articles_published();
$limit = 4;
include __DIR__ . '/_related.php';

// Comments initial
$commentsData = mh_comments_read($slug);
$approved = mh_comments_approved_sorted($commentsData['items']);
$commentTotal = count($approved);
$initial = array_slice($approved, 0, 5);

// Engagement totals (optional display)
$engPath = __DIR__ . '/../data/engagement/' . $slug . '.json';

/**
 * ✅ FIX #4: Avoid warnings if engagement file missing
 */
$eng = is_file($engPath) ? mh_read_json_bom($engPath, null) : null;

$totals = (is_array($eng) && isset($eng['totals']) && is_array($eng['totals'])) ? $eng['totals'] : [];
$yes = (int)($totals['helpful_yes'] ?? 0);
$no  = (int)($totals['helpful_no'] ?? 0);

// Schemas
$schemaArticle = [
  "@context" => "https://schema.org",
  "@type" => "Article",
  "headline" => $title,
  "description" => $pageDescription,
  "author" => ["@type"=>"Organization","name"=>$author],
  "publisher" => ["@type"=>"Organization","name"=>"MYLINEHUB"],
  "datePublished" => $datePublished,
  "dateModified" => $dateModified,
  "mainEntityOfPage" => ["@type"=>"WebPage","@id"=>$pageCanonical],
];
if ($cover !== '') $schemaArticle["image"] = [ (strpos($cover,'http')===0) ? $cover : (rtrim(mh_site(),'/').'/'.ltrim($cover,'/')) ];

$schemaBreadcrumb = [
  "@context" => "https://schema.org",
  "@type" => "BreadcrumbList",
  "itemListElement" => [
    ["@type"=>"ListItem","position"=>1,"name"=>"Home","item"=>mh_site().$base],
    ["@type"=>"ListItem","position"=>2,"name"=>"Articles","item"=>mh_site().$base."articles/"],
    ["@type"=>"ListItem","position"=>3,"name"=>$title,"item"=>$pageCanonical],
  ]
];
?>
<!DOCTYPE html>
<html class="no-js" lang="en">
<?php include __DIR__ . '/../partials/head.php'; ?>
<body class="mh-article-page">

<?php include __DIR__ . '/../partials/header.php'; ?>

<link rel="stylesheet" href="<?= esc($base) ?>assets/css/article.css?v=2">

<div class="mh-progress" id="mhProgress"></div>

<main
  class="mh-shell"
  data-mh-article
  data-slug="<?= esc($slug) ?>"
  data-canonical="<?= esc($pageCanonical) ?>"
  data-base="<?= esc($base) ?>"
>
  <div></div>

  <article class="mh-main">
    <div style="margin-bottom:14px;">
      <a href="<?= esc($base) ?>articles/" style="text-decoration:none;font-weight:900;color:#119bd2;">← Back to Articles</a>
    </div>

    <div class="mh-kicker"><?= esc($category) ?></div>
    <h1 class="mh-title"><?= esc($title) ?></h1>

    <div class="mh-meta">
      <span><?= esc($author) ?></span>
      <?php if ($datePublished): ?><span>• <?= esc($datePublished) ?></span><?php endif; ?>
      <?php if ($readingTime): ?><span>• <?= esc($readingTime) ?></span><?php endif; ?>
    </div>

    <?php if ($desc): ?>
      <p style="margin-top:12px;color:#475569;font-size:16px;line-height:1.7;font-weight:700;"><?= esc($desc) ?></p>
    <?php endif; ?>

    <?php if ($cover): ?>
      <div class="mh-cover">
        <!-- ✅ FIX #5: performance -->
        <img src="<?= esc($cover) ?>" alt="<?= esc($title) ?>" loading="lazy" decoding="async">
      </div>
    <?php endif; ?>

    <div class="mh-content" id="mhArticleContent">
      <?= $contentHtml ?>
    </div>

	<?php
	  $ctaWhatsApp = "https://wa.me/919711761156";
	  $ctaYoutube  = "https://www.youtube.com/@mylinehub-wq2mg";
	?>

	<section class="mh-card" style="margin-top:18px;">
	  <div class="mh-kicker" style="margin-bottom:8px;">Try it</div>

	  <p style="margin:0;color:#475569;font-size:15px;line-height:1.7;font-weight:700;">
		Want to see API-driven CRM + Telecom workflows in action? Try the WhatsApp bot or explore the demos.
	  </p>

	  <div style="display:flex;gap:10px;flex-wrap:wrap;margin-top:12px;">
		<a
		  href="<?= esc($ctaWhatsApp) ?>"
		  target="_blank"
		  rel="noopener"
		  class="mh-btn mh-btn-primary"
		  style="text-decoration:none;"
		>
		  💬 Try WhatsApp Bot
		</a>

		<a
		  href="<?= esc($ctaYoutube) ?>"
		  target="_blank"
		  rel="noopener"
		  class="mh-btn"
		  style="text-decoration:none;"
		>
		  ▶️ Watch CRM YouTube Demos
		</a>
	  </div>

	  <div style="margin-top:10px;color:#64748b;font-size:12px;font-weight:800;">
		Tip: Comment <strong>“Try the bot”</strong> on our YouTube videos to see automation in action.
	  </div>
	</section>

    <div class="mh-author">
      <div class="mh-avatar"><?= esc(mb_substr($author, 0, 1)) ?></div>
      <div>
        <div class="mh-author-name"><?= esc($author) ?></div>
        <div class="mh-author-note">
          Published: <?= esc($datePublished) ?>
          <?php if ($dateModified && $dateModified !== $datePublished): ?>
            • Updated: <?= esc($dateModified) ?>
          <?php endif; ?>
        </div>
      </div>
    </div>

    <!-- ✅ Engagement UI -->
    <section class="mh-card" style="margin-top:18px;">
      <div class="mh-kicker" style="margin-bottom:6px;">Quick feedback</div>

      <div style="display:flex;gap:14px;flex-wrap:wrap;align-items:center;justify-content:space-between;">
        <div style="font-weight:900;color:#0f172a;">
          Was this helpful?
          <span style="color:#64748b;font-weight:800;">(Yes <?= $yes ?> • No <?= $no ?>)</span>
        </div>

        <div style="display:flex;gap:10px;flex-wrap:wrap;">
          <button class="mh-btn" type="button" data-mh-helpful="yes">Yes</button>
          <button class="mh-btn" type="button" data-mh-helpful="no">No</button>
          <button class="mh-btn" type="button" data-mh-follow="on">🔔 Follow updates</button>
        </div>
      </div>

      <div style="margin-top:12px;">
        <div style="font-weight:900;color:#0f172a;margin-bottom:8px;">Reaction</div>
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
          <button class="mh-btn" type="button" data-mh-reaction="like">👍 Like</button>
          <button class="mh-btn" type="button" data-mh-reaction="insightful">💡 Insightful</button>
          <button class="mh-btn" type="button" data-mh-reaction="love">❤️ Love</button>
          <button class="mh-btn" type="button" data-mh-reaction="confused">🤔 Confused</button>
        </div>
      </div>

      <div id="mhReasonWrap" style="display:none;margin-top:12px;">
        <div style="font-weight:900;color:#0f172a;margin-bottom:8px;">Why not helpful?</div>
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
          <label style="font-weight:800;color:#334155;"><input type="radio" name="mh_reason" value="too_short"> Too short</label>
          <label style="font-weight:800;color:#334155;"><input type="radio" name="mh_reason" value="unclear"> Unclear</label>
          <label style="font-weight:800;color:#334155;"><input type="radio" name="mh_reason" value="outdated"> Outdated</label>
          <label style="font-weight:800;color:#334155;"><input type="radio" name="mh_reason" value="missing_steps"> Missing steps</label>
          <label style="font-weight:800;color:#334155;"><input type="radio" name="mh_reason" value="other"> Other</label>
        </div>
        <div style="color:#64748b;font-weight:800;font-size:12px;margin-top:8px;">
          Saved once per IP for this article
        </div>
      </div>

      <div id="mhEngToast" class="mh-toast"></div>
    </section>

    <!-- ✅ Comments -->
    <section class="mh-comments" id="mhComments">
      <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;flex-wrap:wrap;">
        <h2 style="margin:0;font-size:22px;border:none;padding:0;font-weight:900;color:#0b4b8c;">
          Comments (<?= (int)$commentTotal ?>)
        </h2>
        <button class="mh-btn mh-btn-primary" id="mhOpenModal">Write a comment</button>
      </div>

      <div id="mhToast" class="mh-toast"></div>

      <div id="mhCommentList">
        <?php if (count($initial) === 0): ?>
          <p style="color:#64748b;margin-top:10px;font-weight:700;">Be the first to comment.</p>
        <?php else: ?>
          <?php foreach ($initial as $c): ?>
            <div class="mh-comment-card">
              <div class="mh-comment-top">
                <div class="mh-comment-name"><?= esc($c['name'] ?? '') ?></div>
                <div class="mh-comment-time"><?= esc($c['createdAt'] ?? '') ?></div>
              </div>
              <div class="mh-comment-text"><?= esc($c['comment'] ?? '') ?></div>
            </div>
          <?php endforeach; ?>
        <?php endif; ?>
      </div>

      <?php if ($commentTotal > 5): ?>
        <div style="margin-top:12px;">
          <button class="mh-btn" id="mhLoadMore">Show more</button>
        </div>
      <?php endif; ?>
    </section>
  </article>

  <aside class="mh-side">
    <div class="mh-card">
      <div class="mh-kicker" style="margin-bottom:8px;">On this page</div>
      <?php if (!empty($tocItems)): ?>
        <div class="mh-toc" id="mhToc">
          <?php foreach ($tocItems as $it): ?>
            <a href="#<?= esc($it['id']) ?>" data-id="<?= esc($it['id']) ?>"><?= esc($it['text']) ?></a>
          <?php endforeach; ?>
        </div>
      <?php else: ?>
        <div style="color:#64748b;font-weight:700;">No sections yet.</div>
      <?php endif; ?>
    </div>

    <div class="mh-card">
      <div class="mh-kicker" style="margin-bottom:8px;">Share</div>
      <div class="mh-share">
        <button class="mh-btn" id="mhCopyLink">Copy link</button>
        <button class="mh-btn" id="mhShareWa">WhatsApp</button>
        <button class="mh-btn" id="mhShareLi">LinkedIn</button>
      </div>
      <div style="margin-top:8px;color:#64748b;font-size:12px;font-weight:800;">Sharing helps the post reach more people.</div>
    </div>

    <div class="mh-card">
      <div class="mh-kicker" style="margin-bottom:8px;">Related posts</div>
      <?php if (!empty($related)): ?>
        <?php foreach ($related as $r): ?>
          <div style="padding:10px 8px;border-top:1px solid rgba(148,163,184,0.35);">
            <a style="text-decoration:none;color:#0f172a;font-weight:900;"
               href="<?= esc($base) ?>articles/<?= esc($r['slug']) ?>">
              <?= esc($r['title'] ?? '') ?>
            </a>
            <div style="color:#64748b;font-size:13px;margin-top:4px;font-weight:700;">
              <?= esc($r['readingTime'] ?? '') ?>
              <?php if (!empty($r['datePublished'])): ?> • <?= esc($r['datePublished']) ?><?php endif; ?>
            </div>
          </div>
        <?php endforeach; ?>
      <?php else: ?>
        <div style="color:#64748b;font-weight:700;">No related posts found.</div>
      <?php endif; ?>
    </div>
  </aside>
</main>

<div class="mh-backdrop" id="mhBackdrop"></div>
<div class="mh-modal" id="mhModal" aria-hidden="true">
  <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;">
    <h3 style="margin:0;">Write a comment</h3>
    <button class="mh-btn" id="mhCloseModal" type="button">Close</button>
  </div>

  <form id="mhCommentForm" autocomplete="off">
    <input type="hidden" name="slug" value="<?= esc($slug) ?>">

    <!-- honeypots -->
    <div style="position:absolute;left:-9999px;top:-9999px;height:0;overflow:hidden;">
      <label>Middle<input type="text" name="hp_mid" tabindex="-1" autocomplete="off"></label>
    </div>

    <div class="mh-field">
      <label>Name *</label>
      <input name="name" required minlength="2" maxlength="60">
    </div>

    <div class="mh-field">
      <label>Email *</label>
      <input name="email" type="email" required>
    </div>

    <div class="mh-field">
      <label>Phone *</label>
      <input name="phone" inputmode="numeric" required>
      <div style="color:#64748b;font-size:12px;font-weight:800;margin-top:6px;">
        Stored masked for safety.
      </div>
    </div>

    <div class="mh-field">
      <label>Comment *</label>
      <textarea name="comment" required minlength="10" maxlength="800"></textarea>
    </div>

    <div style="position:absolute;left:-9999px;top:-9999px;height:0;overflow:hidden;">
      <label>End<input type="text" name="hp_end" tabindex="-1" autocomplete="off"></label>
    </div>

    <div class="mh-actions">
      <button class="mh-btn" type="button" id="mhCancel">Cancel</button>
      <button class="mh-btn mh-btn-primary" type="submit" id="mhSubmitBtn">Post</button>
    </div>
  </form>
</div>

<script type="application/ld+json"><?= json_encode($schemaArticle, JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE) ?></script>
<script type="application/ld+json"><?= json_encode($schemaBreadcrumb, JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE) ?></script>

<?php include __DIR__ . '/../partials/footer.php'; ?>
<?php include __DIR__ . '/../partials/scripts.php'; ?>

<script src="<?= esc($base) ?>assets/js/article.js?v=2"></script>

</body>
</html>
