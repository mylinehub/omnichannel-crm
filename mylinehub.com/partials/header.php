<?php
// ---------------------------------------------------------------------
// partials/header.php
// ---------------------------------------------------------------------

$base = rtrim((string)BASE_URL, '/') . '/';
$showHero = $showHero ?? false;

$script = $_SERVER['SCRIPT_NAME'] ?? '';
$isHome = (trim($script, '/') === 'index.php');

function navHref($id, $base, $isHome) {
  return $isHome ? ("#" . $id) : ($base . "index.php#" . $id);
}

/**
 * ✅ Logo path handling
 * - Use absolute-from-webroot so it works from /articles/... pages too.
 * - If your site is installed in a subfolder (e.g. /mylinehub/),
 *   BASE_URL already points there, so also compute a BASE_URL logo fallback.
 */
$logoWebRoot = "/assets/images/logo/logo-2.png";          // works for root installs
$logoBaseUrl  = $base . "assets/images/logo/logo-2.png";  // works for subfolder installs

// Prefer BASE_URL form for hrefs, but keep src absolute to avoid /articles/ relative issues.
// If you are 100% sure you are root install, you can just use $logoWebRoot for both.
$logoSrc = $logoWebRoot;
?>

<style>
  .navbar-brand { display:flex; align-items:center; min-height:40px; }
  .navbar-brand img.site-logo{
    height: 40px;
    width: auto;
    max-width: 240px;
    display: block;
    object-fit: contain;
  }
  @media (max-width: 420px){
    .navbar-brand img.site-logo{
      height: 32px;
      max-width: 180px;
    }
  }
</style>

<!--[if IE]>
  <p class="browserupgrade">
    You are using an <strong>outdated</strong> browser. Please
    <a href="https://browsehappy.com/">upgrade your browser</a> to improve
    your experience and security.
  </p>
<![endif]-->

<!--====== PRELOADER PART START ======-->
<div class="preloader">
  <div class="loader">
    <div class="spinner">
      <div class="spinner-container">
        <div class="spinner-rotator">
          <div class="spinner-left">
            <div class="spinner-circle"></div>
          </div>
          <div class="spinner-right">
            <div class="spinner-circle"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<!--====== PRELOADER PART ENDS ======-->

<!--====== HEADER PART START ======-->
<header class="header-area">

  <div class="navbar-area">
    <div class="container">
      <div class="row">
        <div class="col-lg-12">
          <nav class="navbar navbar-expand-lg">

            <a class="navbar-brand" href="<?= esc($base) ?>index.php" aria-label="MYLINEHUB Home">
              <img
                class="site-logo"
                src="<?= esc($logoSrc) ?>"
                alt="MYLINEHUB"
                decoding="async"
                loading="eager"
                draggable="false"
                onerror="this.onerror=null; this.src='<?= esc($logoBaseUrl) ?>';"
              />
            </a>

            <button
              class="navbar-toggler collapsed"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarSupportedContent"
              aria-controls="navbarSupportedContent"
              aria-expanded="false"
              aria-label="Toggle navigation"
            >
              <span class="toggler-icon"></span>
              <span class="toggler-icon"></span>
              <span class="toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse sub-menu-bar" id="navbarSupportedContent">
              <ul id="nav" class="navbar-nav ms-auto">

                <li class="nav-item">
                  <a class="<?= $isHome ? 'page-scroll active' : '' ?>" href="<?= esc(navHref('home', $base, $isHome)) ?>">Home</a>
                </li>

                <li class="nav-item">
                  <a class="<?= $isHome ? 'page-scroll' : '' ?>" href="<?= esc(navHref('offers', $base, $isHome)) ?>">Features</a>
                </li>

                <li class="nav-item">
                  <a class="<?= $isHome ? 'page-scroll' : '' ?>" href="<?= esc(navHref('pricing', $base, $isHome)) ?>">Pricing</a>
                </li>

                <li class="nav-item">
                  <a class="<?= $isHome ? 'page-scroll' : '' ?>" href="<?= esc(navHref('whatsapp', $base, $isHome)) ?>">Product Videos</a>
                </li>

                <li class="nav-item">
                  <a class="<?= $isHome ? 'page-scroll' : '' ?>" href="<?= esc(navHref('policies', $base, $isHome)) ?>">Policies</a>
                </li>

                <li class="nav-item">
                  <a href="<?= esc($base) ?>articles/">Articles</a>
                </li>

              </ul>
            </div>

          </nav>
        </div>
      </div>
    </div>
  </div>

  <?php if ($showHero): ?>
    <?php include __DIR__ . '/../sections/hero.php'; ?>
  <?php endif; ?>

</header>
<!--====== HEADER PART ENDS ======-->
