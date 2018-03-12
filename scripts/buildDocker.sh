#!/usr/bin/env bash

export COMMIT=${TRAVIS_COMMIT::8}

docker login -u $DOCKER_USER -p $DOCKER_PASS
export JAVAREPO=anallytics/bahmni-mart
export TAG=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi)
docker build -f JavaDockerfile -t $JAVAREPO:$COMMIT .
docker tag $JAVAREPO:$COMMIT $JAVAREPO:$TAG
docker tag $JAVAREPO:$COMMIT $JAVAREPO:mart-$TRAVIS_BUILD_NUMBER
docker push $JAVAREPO

export POSTGRESREPO=anallytics/postgres
export TAG=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi)
docker build -f PostgresqlDockerfile -t $POSTGRESREPO:$COMMIT .
docker tag $POSTGRESREPO:$COMMIT $POSTGRESREPO:$TAG
docker tag $POSTGRESREPO:$COMMIT $POSTGRESREPO:postgres-$TRAVIS_BUILD_NUMBER
docker push $POSTGRESREPO

if [ "$?" != 0 ]; then
   exit "$?"
fi