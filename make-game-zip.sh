#!/bin/bash
dir=`mktemp -d lolwatXXXXXX`
curdir=`pwd`
echo $dir
mkdir $dir/octodarwin
find octodarwin* *.bat *.command | xargs -L 1 -I@ cp -r @ $dir/octodarwin/
cd $dir
zip -r octodarwin.zip octodarwin
mv octodarwin.zip $curdir
cd $curdir
rm -rf $dir
