#!/usr/bin/env bash
set -e
root=`dirname $(realpath $BASH_SOURCE)`
# installs into local build/dist location that we control
$root/gradlew installAll -x dslHtml -Pgradle_installPath=$root/build/dist/gradle-X $@
# upon success, move current contents out and new contents in.
if [ -d "$root/build/dist/gradle-X" ]; then
  rm -rf $root/build/dist/gradle-X-bak
  parent=`dirname $root`
  mv -f $parent/gradle-X $parent/gradle-X-bak > /dev/null 2>&1 || true
  mv -f $root/build/dist/gradle-X $parent
fi

