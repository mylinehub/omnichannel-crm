<!DOCTYPE html>
<html class="no-js" lang="en">

<?php include __DIR__ . '/partials/config.php'; ?>
<?php include __DIR__ . '/partials/head.php'; ?>

<body>

<?php
$showHero = true; // homepage only
include __DIR__ . '/partials/header.php';
?>

<?php include __DIR__ . '/sections/services.php'; ?>
<?php include __DIR__ . '/sections/easyOnboard.php'; ?>
<?php include __DIR__ . '/sections/brand.php'; ?>
<?php include __DIR__ . '/sections/offers.php'; ?>
<?php include __DIR__ . '/sections/features.php'; ?>
<?php include __DIR__ . '/sections/pricing.php'; ?>
<?php include __DIR__ . '/sections/whatsapp.php'; ?>
<?php include __DIR__ . '/sections/facts.php'; ?>
<?php include __DIR__ . '/sections/policies.php'; ?>

<?php include __DIR__ . '/partials/footer.php'; ?>
<?php include __DIR__ . '/partials/scripts.php'; ?>

</body>
</html>
