<?php
// Shared helpers ONLY for articles + comments.
// Keeps partials/config.php clean.

/* ---------------------------------------------------------
 * JSON helpers
 * --------------------------------------------------------- */

if (!function_exists('mh_read_json_bom')) {
  function mh_read_json_bom(string $path, $fallback = []) {
    if (!is_file($path) || !is_readable($path)) return $fallback;

    $raw = file_get_contents($path);
    if (!is_string($raw) || $raw === '') return $fallback;

    // Strip UTF-8 BOM
    if (substr($raw, 0, 3) === "\xEF\xBB\xBF") {
      $raw = substr($raw, 3);
    }

    $data = json_decode($raw, true);
    return (json_last_error() === JSON_ERROR_NONE && is_array($data))
      ? $data
      : $fallback;
  }
}

if (!function_exists('mh_write_json_atomic')) {
  function mh_write_json_atomic(string $path, $data): bool {
    $dir = dirname($path);
    if (!is_dir($dir)) {
      if (!@mkdir($dir, 0775, true) && !is_dir($dir)) return false;
    }

    $tmp = $path . '.' . uniqid('tmp_', true);

    $json = json_encode(
      $data,
      JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT
    );

    if ($json === false) return false;
    if (file_put_contents($tmp, $json, LOCK_EX) === false) return false;

    @chmod($tmp, 0664);

    // rename may fail across FS → fallback
    if (@rename($tmp, $path)) return true;

    $ok = @copy($tmp, $path);
    @unlink($tmp);
    return $ok;
  }
}

/* ---------------------------------------------------------
 * String + slug helpers
 * --------------------------------------------------------- */

if (!function_exists('mh_slugify')) {
  function mh_slugify(string $s): string {
    $s = trim($s);
    if ($s === '') return 'section';

    if (function_exists('mb_strtolower')) {
      $s = mb_strtolower($s);
    } else {
      $s = strtolower($s);
    }

    $s = preg_replace('/[^\p{L}\p{N}\s-]/u', '', $s);
    $s = preg_replace('/[\s-]+/', '-', $s);
    $s = trim($s, '-');

    return $s !== '' ? $s : 'section';
  }
}

/* ---------------------------------------------------------
 * Client / request helpers
 * --------------------------------------------------------- */

if (!function_exists('mh_client_ip')) {
  function mh_client_ip(): string {
    if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
      $ips = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
      return trim($ips[0]);
    }
    if (!empty($_SERVER['HTTP_CF_CONNECTING_IP'])) {
      return $_SERVER['HTTP_CF_CONNECTING_IP'];
    }
    return $_SERVER['REMOTE_ADDR'] ?? '0.0.0.0';
  }
}

if (!function_exists('mh_ip_hash')) {
  function mh_ip_hash(string $ip): string {
    return substr(hash('sha256', $ip . '|mh_salt_v1'), 0, 8);
  }
}

if (!function_exists('mh_ua_short')) {
  function mh_ua_short(): string {
    $ua = $_SERVER['HTTP_USER_AGENT'] ?? '';
    if ($ua === '') return 'Other';

    if (stripos($ua, 'Edg') !== false) return 'Edge';
    if (stripos($ua, 'Chrome') !== false) return 'Chrome';
    if (stripos($ua, 'Firefox') !== false) return 'Firefox';
    if (stripos($ua, 'Safari') !== false) return 'Safari';

    return 'Other';
  }
}

/* ---------------------------------------------------------
 * Data masking helpers
 * --------------------------------------------------------- */

if (!function_exists('mh_mask_phone')) {
  function mh_mask_phone(string $phone): string {
    $p = preg_replace('/\D+/', '', $phone);
    $len = strlen($p);

    if ($len < 8) return '******';

    $first = substr($p, 0, 2);
    $last  = substr($p, -2);

    return '+' . $first . str_repeat('*', max(0, $len - 4)) . $last;
  }
}

/* ---------------------------------------------------------
 * URL helpers
 * --------------------------------------------------------- */

if (!function_exists('mh_base')) {
  function mh_base(): string {
    if (!defined('BASE_URL')) return '/';
    return rtrim((string)BASE_URL, '/') . '/';
  }
}

if (!function_exists('mh_site')) {
  function mh_site(): string {
    if (!defined('SITE_URL')) return '';
    return rtrim((string)SITE_URL, '/');
  }
}

/* ---------------------------------------------------------
 * Articles helpers
 * --------------------------------------------------------- */

if (!function_exists('mh_load_articles_published')) {
  function mh_load_articles_published(): array {
    $path = __DIR__ . '/../data/articles.json';
    $all = mh_read_json_bom($path, []);

    if (!is_array($all)) return [];

    $pub = [];
    foreach ($all as $a) {
      if (!is_array($a) || empty($a['slug'])) continue;

      $published = $a['published'] ?? false;
      if (is_string($published)) {
        $published = strtolower(trim($published)) === 'true';
      }
      if ($published !== true) continue;

      $pub[] = $a;
    }

    usort($pub, function ($x, $y) {
      $dx = (string)($x['datePublished'] ?? '');
      $dy = (string)($y['datePublished'] ?? '');
      return strcmp($dy, $dx);
    });

    return $pub;
  }
}

if (!function_exists('mh_find_article')) {
  function mh_find_article(string $slug): ?array {
    $slug = strtolower(trim($slug));
    if ($slug === '' || !preg_match('/^[a-z0-9-]+$/', $slug)) return null;

    foreach (mh_load_articles_published() as $a) {
      if (($a['slug'] ?? '') === $slug) return $a;
    }
    return null;
  }
}

/* ---------------------------------------------------------
 * Comments helpers
 * --------------------------------------------------------- */

if (!function_exists('mh_comments_read')) {
  function mh_comments_read(string $slug): array {
    $path = __DIR__ . '/../data/comments/' . $slug . '.json';

    $data = mh_read_json_bom($path, [
      'slug'        => $slug,
      'total'       => 0,
      'lastUpdated' => null,
      'items'       => []
    ]);

    if (!isset($data['items']) || !is_array($data['items'])) {
      $data['items'] = [];
    }

    return $data;
  }
}

if (!function_exists('mh_comments_approved_sorted')) {
  function mh_comments_approved_sorted(array $items): array {
    $approved = array_filter($items, function ($it) {
      return is_array($it) && (($it['status'] ?? '') === 'approved');
    });

    usort($approved, function ($a, $b) {
      return strcmp(
        (string)($b['createdAt'] ?? ''),
        (string)($a['createdAt'] ?? '')
      );
    });

    return array_values($approved);
  }
}

/* ---------------------------------------------------------
 * Activity (reserved)
 * --------------------------------------------------------- */

if (!function_exists('mh_activity_log_client')) {
  function mh_activity_log_client(string $event, string $path, string $slug = '', array $data = []) {
    // Reserved for future server-side logging
  }
}
