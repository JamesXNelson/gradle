#!/usr/bin/env bash
set -e
root=`dirname $(realpath $BASH_SOURCE)`
# installs into local build/dist location that we control
$root/gradlew installAll -x dslHtml -x userguideSinglePage -Pgradle_installPath=$root/build/dist/gradle-X $@
# upon success, move current contents out and new contents in.
if [ -d "$root/build/dist/gradle-X" ]; then
  parent=`dirname $root`
  echo "Removing $parent/gradle-X-bak"
  rm -rf $parent/gradle-X-bak
  echo "Backing up $parent/gradle-X"
  mv -f $parent/gradle-X $parent/gradle-X-bak > /dev/null 2>&1 || true
  echo "Moving $root/build/dist/gradle-X to $parent"
  mv -f $root/build/dist/gradle-X $parent
fi
echo "Done"

