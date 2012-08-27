#!/bin/bash
dir=`mktemp -d`
curdir=`pwd`
echo $dir
mkdir $dir/octodarwin
find octodarwin* *.bat *.command | xargs -l1 -I@ cp -r @ $dir/octodarwin/
cd $dir
zip octodarwin.zip octodarwin
mv octodarwin.zip $curdir
cd $curdir
rm -rf $dir
