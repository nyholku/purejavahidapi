#!/bin/sh
# Push javadoc files to a website hosted by github <http://pages.github.com/>.
# Before executing this script, generate the javadoc files into build/docs/javadoc/.
git checkout master || exit $?
git checkout gh-pages || exit $?
javadoc -d build/javadoc  -sourcepath src purejavahidapi 
# Clear out the old files:
rm -rf javadoc/* 
# Replace them with new files and commit them:
cp -pR build/javadoc/ javadoc \
&& git add javadoc \
&& git commit -a -m "generated javadoc"
ERROR=$?

git checkout master || exit $?
[ $ERROR -eq 0 ] || exit $ERROR
git push origin master || exit $?
git push origin gh-pages || exit $?