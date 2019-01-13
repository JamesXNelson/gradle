#!/usr/bin/env bash
set -e
root=`dirname $(realpath $BASH_SOURCE)`
$root/gradlew installAll -Pgradle_installPath=$root/build/dist/gradle-X
if [ -d "$root/build/dist/gradle-X" ]; then
  rm -rf $root/build/dist/gradle-X-bak
  parent=`dirname $root`
  mv $parent/gradle-X $parent/gradle-X-bak > /dev/null 2>&1 || true
  mv $root/build/dist/gradle-X $parent
fi

