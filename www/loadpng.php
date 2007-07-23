<?php
header("Content-type: image/png");

$file = $_GET["File"];

$bg = $_GET["Bg"];
$red = ("0x" . substr($bg, 0, 2)); $red += 0;
$grn = ("0x" . substr($bg, 2, 2)); $grn += 0;
$blu = ("0x" . substr($bg, 4, 2)); $blu += 0;

list($width, $height, $type, $attr) = getImageSize($file);
$bgimg = imageCreateTrueColor($width, $height); 

$bg = imageColorAllocate($bgimg, $red, $grn, $blu);
imageFill($bgimg, 0, 0, $bg);

$fgimg = imageCreateFromPNG($file);
imageCopy($bgimg, $fgimg, 0, 0, 0, 0, $width, $height);

imagePNG($bgimg);
?>
