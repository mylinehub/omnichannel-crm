<head>
  <meta charset="utf-8" />
  <meta http-equiv="x-ua-compatible" content="ie=edge" />

  <?php
    // ----------------------------
    // SEO defaults (fallbacks)
    // Pages can override by setting these variables BEFORE including head.php:
    // $pageTitle, $pageDescription, $pageCanonical, $robots, $pageType, $ogImage
    // ----------------------------
    $defaultTitle = "MYLINEHUB | WhatsApp, IVR & AI Automation Platform";
    $defaultDescription = "MYLINEHUB helps businesses automate WhatsApp, IVR, calling, CRM follow-ups and AI interviews. Scale sales and hiring faster.";
    $defaultCanonical = "https://mylinehub.com/";
    $defaultRobots = "index, follow";
    $defaultType = "website";

    $finalTitle = isset($pageTitle) && trim((string)$pageTitle) !== '' ? (string)$pageTitle : $defaultTitle;
    $finalDescription = isset($pageDescription) && trim((string)$pageDescription) !== '' ? (string)$pageDescription : $defaultDescription;
    $finalCanonical = isset($pageCanonical) && trim((string)$pageCanonical) !== '' ? (string)$pageCanonical : $defaultCanonical;
    $finalRobots = isset($robots) && trim((string)$robots) !== '' ? (string)$robots : $defaultRobots;
    $finalType = isset($pageType) && trim((string)$pageType) !== '' ? (string)$pageType : $defaultType;

    // Optional OG image (fallback to favicon/logo)
    $fallbackOgImage = (defined('SITE_URL') ? rtrim(SITE_URL, '/') : 'https://mylinehub.com') . "/assets/images/logo/logo-2.png";
    $finalOgImage = isset($ogImage) && trim((string)$ogImage) !== '' ? (string)$ogImage : $fallbackOgImage;
  ?>

  <title><?= esc($finalTitle) ?></title>

  <meta name="description" content="<?= esc($finalDescription) ?>" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <meta name="robots" content="<?= esc($finalRobots) ?>" />

  <link rel="canonical" href="<?= esc($finalCanonical) ?>" />

  <!-- Open Graph (safe + optional, does not break anything) -->
  <meta property="og:title" content="<?= esc($finalTitle) ?>" />
  <meta property="og:description" content="<?= esc($finalDescription) ?>" />
  <meta property="og:url" content="<?= esc($finalCanonical) ?>" />
  <meta property="og:type" content="<?= esc($finalType) ?>" />
  <meta property="og:image" content="<?= esc($finalOgImage) ?>" />

  <!-- Favicon -->
  <link rel="shortcut icon" href="<?php echo BASE_URL; ?>assets/images/favicon.png" type="image/png" />

  <!-- CSS -->
  <link rel="stylesheet" href="<?php echo BASE_URL; ?>assets/css/animate.css?v=1" />
  <link rel="stylesheet" href="<?php echo BASE_URL; ?>assets/css/glightbox.min.css?v=1" />
  <link rel="stylesheet" href="<?php echo BASE_URL; ?>assets/css/lineIcons.css?v=1" />
  <link rel="stylesheet" href="<?php echo BASE_URL; ?>assets/css/bootstrap.min.css?v=1" />
  <link rel="stylesheet" href="<?php echo BASE_URL; ?>assets/css/style.css?v=1" />

  <!-- KEEP YOUR TABLE STYLES (unchanged) -->
  <style>
    table { font-family: arial, sans-serif; border-collapse: collapse; width: 100%; }
    td, th { border: 1px solid #dddddd; text-align: left; padding: 8px; }
    tr:nth-child(even) { background-color: #dddddd; }
  </style>

  <!-- Schema: Organization (unchanged) -->
  <script type="application/ld+json">
  {
    "@context": "https://schema.org",
    "@type": "Organization",
    "name": "MYLINEHUB",
    "url": "https://mylinehub.com/",
    "email": "support@mylinehub.com",
    "sameAs": [
      "https://www.instagram.com/mylinehub/",
      "https://www.facebook.com/profile.php?id=61560429959357"
    ]
  }
  </script>

  <!-- Google Ads Tag (unchanged) -->
  <script async src="https://www.googletagmanager.com/gtag/js?id=AW-11169328845"></script>
  <script>
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());
    gtag('config', 'AW-11169328845');
  </script>
</head>
