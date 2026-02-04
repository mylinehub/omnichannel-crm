<?php
// Input: $contentHtml (string)
// Output: $contentHtmlWithIds, $tocItems

$contentHtmlWithIds = $contentHtml ?? '';
$tocItems = [];

if (!is_string($contentHtmlWithIds) || trim($contentHtmlWithIds) === '') return;

libxml_use_internal_errors(true);

$doc = new DOMDocument();
$wrapped =
  '<!doctype html><html><head><meta charset="utf-8"></head><body><div id="root">' .
  $contentHtmlWithIds .
  '</div></body></html>';

if (!$doc->loadHTML($wrapped, LIBXML_HTML_NOIMPLIED | LIBXML_HTML_NODEFDTD)) {
  libxml_clear_errors();
  return;
}

$xpath = new DOMXPath($doc);
$h2s = $xpath->query('//*[@id="root"]//h2');

$used = [];

if ($h2s && $h2s->length) {
  foreach ($h2s as $h2) {
    $text = trim($h2->textContent ?? '');
    if ($text === '') continue;

    $id = $h2->getAttribute('id');
    if (!$id) $id = mh_slugify($text);

    $base = $id;
    $i = 2;
    while (isset($used[$id])) {
      $id = $base . '-' . $i;
      $i++;
    }
    $used[$id] = true;

    $h2->setAttribute('id', $id);
    $tocItems[] = ['id' => $id, 'text' => $text];
  }
}

$root = $xpath->query('//*[@id="root"]')->item(0);
if ($root) {
  $html = '';
  foreach ($root->childNodes as $child) $html .= $doc->saveHTML($child);
  $contentHtmlWithIds = $html;
}

libxml_clear_errors();
