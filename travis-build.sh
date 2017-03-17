#!/bin/bash
set -e
rm -rf *.zip

# Set Gradle daemon JVM args
mkdir -p ~/.gradle
echo "org.gradle.jvmargs=-XX\:MaxPermSize\=512m -Xmx1024m" >> ~/.gradle/gradle.properties
echo "org.gradle.daemon=true" >> ~/.gradle/gradle.properties

./gradlew clean check assemble
./gradlew —-stop

EXIT_STATUS=0
echo "Publishing archives for branch $TRAVIS_BRANCH"
if [[ -n $TRAVIS_TAG ]] || [[ $TRAVIS_BRANCH =~ ^master|[12]\..\.x$ && $TRAVIS_PULL_REQUEST == 'false' ]]; then

  echo "Publishing archives"

  if [[ -n $TRAVIS_TAG ]]; then
      ./gradlew bintrayUpload || EXIT_STATUS=$?
  else
      ./gradlew publish || EXIT_STATUS=$?
  fi
  ./gradlew —-stop

  ./gradlew views-docs:docs || EXIT_STATUS=$?
  ./gradlew —-stop

  git config --global user.name "$GIT_NAME"
  git config --global user.email "$GIT_EMAIL"
  git config --global credential.helper "store --file=~/.git-credentials"
  echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

  git clone https://${GH_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git -b gh-pages gh-pages --single-branch > /dev/null
  cd gh-pages

  # If this is the master branch then update the snapshot
  if [[ $TRAVIS_BRANCH =~ ^master|[12]\..\.x$ ]]; then
     mkdir -p snapshot
     cp -r ../docs/build/docs/. ./snapshot/

     git add snapshot/*
  fi

  # If there is a tag present then this becomes the latest
  if [[ -n $TRAVIS_TAG ]]; then
      mkdir -p latest
      cp -r ../docs/build/docs/. ./latest/
      git add latest/*

      version="$TRAVIS_TAG"
      version=${version:1}
      majorVersion=${version:0:4}
      majorVersion="${majorVersion}x"

      mkdir -p "$version"
      cp -r ../docs/build/docs/. "./$version/"
      git add "$version/*"

      mkdir -p "$majorVersion"
      cp -r ../docs/build/docs/. "./$majorVersion/"
      git add "$majorVersion/*"

  fi

  git commit -a -m "Updating docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
  git push origin HEAD
  cd ..
  rm -rf gh-pages
fi

exit $EXIT_STATUS