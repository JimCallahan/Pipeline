<?php

/* create a temporary plscript to view a Focus node */ 
function viewFocusNodeScript($node_name, $node_version, $node_prefix) 
{
  global $temerity_root;

  $tmpdir = 'tmp'; 
  $subdirs = explode('/', $node_name . '/' . $node_version);
  foreach($subdirs as $sdir) {
    if($sdir != "") {
      $tmpdir .= ('/' . $sdir);
      if(!file_exists($tmpdir)) 
        mkdir($tmpdir);
    }
  }

  $script = ($tmpdir . '/' . $node_prefix . '.php'); 
  if(!file_exists($script)) {
    $handle = fopen($script, "w");
    fwrite($handle,
           '<?php header("Content-type: application/plscript");?>' . "\n" .
           "checked-in --view=" . $node_name . " --version=" . $node_version . "\n"); 
    fclose($handle);
  }

  return $script; 
}

?>
