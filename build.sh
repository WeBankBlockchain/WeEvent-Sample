#!/bin/bash

echo "begin to build WeEvent-Sample."

./gradlew clean build
if [[ $? -ne 0 ]];then
	echo "gradle build WeEvent-Sample failed"
	exit 1
fi
echo "build WeEvent-Sample success."

exit 0
