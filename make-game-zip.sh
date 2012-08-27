#!/bin/bash
dir=`mktemp -d lolwatXXXXXX`
curdir=`pwd`
echo $dir
mkdir $dir/octodarwin
cp -r octodarwin* $dir/octodarwin/
cp -r *.bat $dir/octodarwin/
cp -r *.command $dir/octodarwin/
cd $dir
zip -r octodarwin.zip octodarwin
mv octodarwin.zip $curdir
cd $curdir
rm -rf $dir
