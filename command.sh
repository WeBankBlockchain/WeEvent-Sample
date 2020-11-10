#!/bin/bash
current_path=$(pwd)

# check param
if [[ $1 != "open" ]] && [[ $1 != "subscribe" ]] && [[ $1 != "publish" ]] && [[ $1 != "status" ]] && [[ $1 != "getEvent" ]] && [[ $1 != "general" ]] && [[ $1 != "listGroup" ]] && [[ $1 != "sendFile" ]] && [[ $1 != "receiveFile" ]];then
    echo "Usage:"
    echo "    ./command.sh listGroup"
    echo "    ./command.sh open groupId topicName"
    echo "    ./command.sh subscribe groupId topicName"
    echo "    ./command.sh publish groupId topicName content"
    echo "    ./command.sh getEvent groupId eventId(成功publish一个事件后，会返回该事件的eventId)"
    echo "    ./command.sh status groupId topicName"
    echo "    ./command.sh sendFile groupId topicName fileUrl"
    echo "    ./command.sh receiveFile groupId topicName"
    echo "    ./command.sh general groupId"
    exit 1
fi

if [[ $1 == "listGroup" ]];then
    brokerUrl=$(grep "broker.url" ${current_path}/dist/conf/application.properties| head -1 | awk -F'=' '{print $NF}' | sed s/[[:space:]]//g)
    if [[ $? -ne 0 ]];then
        echo "get brokerUrl fail"
        exit 1
    fi
    listGroup_response=$(curl -s "$brokerUrl/admin/listGroup")
    echo "listGroup result: $listGroup_response"
    exit 1
fi

if [[ $1 == "general" ]];then
    brokerUrl=$(grep "broker.url" ${current_path}/dist/conf/application.properties| head -1 | awk -F'=' '{print $NF}' | sed s/[[:space:]]//g)
    if [[ $? -ne 0 ]];then
        echo "get brokerUrl fail"
        exit 1
    fi
    general_response=$(curl -s "$brokerUrl/admin/group/general?groupId=$2")
    echo "general result: $general_response"
    exit 1
fi

if [[ $1 == "open" ]] && [[ $# -ne 3 ]];then
    echo "Usage:"
    echo "    ./command.sh open groupId topicName"
    exit 1
fi

if [[ $1 == "subscribe" ]] && [[ $# -ne 3 ]];then
    echo "Usage:"
    echo "    ./command.sh subscribe groupId topicName"
    exit 1
fi

if [[ $1 == "publish" ]] && [[ $# -ne 4 ]];then
    echo "Usage:"
    echo "    ./command.sh publish groupId topicName content"
    exit 1
fi

if [[ $1 == "status" ]] && [[ $# -ne 3 ]];then
    echo "Usage:"
    echo "    ./command.sh status groupId topicName"
    exit 1
fi

if [[ $1 == "getEvent" ]] && [[ $# -ne 3 ]];then
    echo "Usage:"
    echo "    ./command.sh getEvent groupId eventId"
    exit 1
fi

if [[ $1 == "sendFile" ]] && [[ $# -ne 4 ]];then
    echo "Usage:"
    echo "    ./command.sh sendFile groupId topicName fileUrl"
    exit 1
fi

if [[ $1 == "receiveFile" ]] && [[ $# -ne 3 ]];then
    echo "Usage:"
    echo "    ./command.sh receiveFile groupId topicName"
    exit 1
fi

# Mac OS X || GNU/Linux
if [[ "$(uname)" == "Darwin" ]] || [[ "$(expr substr $(uname -s) 1 5)" == "Linux" ]];then
    if [[ $1 == "publish" ]] && [[ $1 != "sendFile" ]];then
        ${JAVA_HOME}/bin/java -cp dist/conf/:dist/lib/*:dist/apps/* com.webank.weevent.demo.JavaSDKSample $1 $2 $3 $4
    else
        ${JAVA_HOME}/bin/java -cp dist/conf/:dist/lib/*:dist/apps/* com.webank.weevent.demo.JavaSDKSample $1 $2 $3
    fi
# Windows
elif [[ "$(expr substr $(uname -s) 1 6)" == "MINGW" ]];then
    if [[ $1 == "publish" ]] && [[ $1 != "sendFile" ]];then
        ${JAVA_HOME}/bin/java -cp 'dist/conf/;dist/lib/*;dist/apps/*' com.webank.weevent.demo.JavaSDKSample $1 $2 $3 $4
    else
        ${JAVA_HOME}/bin/java -cp 'dist/conf/;dist/lib/*;dist/apps/*' com.webank.weevent.demo.JavaSDKSample $1 $2 $3
    fi
fi
