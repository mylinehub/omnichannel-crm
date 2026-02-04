<?php
/**
 * BASE_URL: used for assets + internal links
 * - If hosted at root: '/'
 * - If hosted in subfolder: '/mylinehub/'
 */
if (!defined('BASE_URL')) {
  define('BASE_URL', '/');
}

/**
 * SITE_URL: absolute URL used for canonical/schema/sitemap
 */
if (!defined('SITE_URL')) {
  define('SITE_URL', 'https://mylinehub.com');
}

/**
 * Safe HTML escape helper (define only once)
 */
if (!function_exists('esc')) {
  function esc($v) {
    return htmlspecialchars((string)$v, ENT_QUOTES, 'UTF-8');
  }
}
