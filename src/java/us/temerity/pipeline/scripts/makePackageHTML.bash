#!/bin/bash

path=`pwd`


if [[ "$path" =~ .*us/temerity/pipeline/plugin.* ]]
then
  version=`basename $path`
  temp=`dirname $path`
  pluginName=`basename $temp`


  if [[ "$version" =~ v[0-9]+_[0-9]+_[0-9]+ ]]
  then

   dotVersion=${version//_/.}
   dotVersion=${dotVersion//v/}

(
    cat <<EOL
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <meta content="text/html; charset=ISO-8859-1" http-equiv="content-type">
  <title>us.temerity.pipeline.plugin.${pluginName}.${version}</title>
</head>
<body>
Provides the classes which make up specific version of a Pipline plugin.
<br>
<h2><span style="font-weight: bold;"></span>Package Specification</h2>
The classes in this package are used to build a JAR archive file which 
is used to dynamically install the (${dotVersion}) of the $pluginName plugin using 
the plplugin(1) tool.
<BR>
</body>
</html>
EOL
) > package.html
  else
    echo Not a valid plugin directory
  fi
else
  echo "Not in the Pipeline code directory"
fi