package com.webank.weevent.demo;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.file.IWeEventFileClient;
import com.webank.weevent.file.service.FileChunksMeta;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaSDKSample {
    private static IWeEventClient weEventClient;
    private static List<String> subscribeIdList = new ArrayList<>();

    private static String localReceivePath = "./received";
    // chunk size 1MB
    private static int fileChunkSize = 1048576;

    public static void main(String[] args) throws InterruptedException {
        log.info("args = {}", Arrays.toString(args));

        if (args.length < 3) {
            log.error("args missing");
            System.exit(1);
        }
        String command = args[0];
        String groupId = args[1];
        String topicName;
        String content;
        String filePath;
        String eventId;

        String brokerUrl = PropertiesUtils.getProperty("broker.url");

        try {
            // build Client by default groupId and configured brokerUrl
            weEventClient = IWeEventClient.builder().brokerUrl(brokerUrl).groupId(groupId).build();
        } catch (BrokerException e) {
            log.error("build WeEventClient failed, brokerUrl:[{}], exception:{}", brokerUrl, e);
            System.out.println("build WeEventClient failed, brokerUrl:[" + brokerUrl + "], exception:" + e);
            System.exit(1);
        }

        try {
            switch (command) {
                case "open":
                    topicName = args[2];
                    openTopic(topicName);
                    break;
                case "subscribe":
                    topicName = args[2];
                    subscribe(topicName);
                    break;
                case "publish":
                    topicName = args[2];
                    content = args[3];
                    System.out.println("content:" + content);
                    publish(topicName, content);
                    break;
                case "status":
                    topicName = args[2];
                    status(topicName);
                    break;
                case "getEvent":
                    eventId = args[2];
                    getEvent(eventId);
                    break;
                case "sendFile":
                    topicName = args[2];
                    filePath = args[3];
                    System.out.println("filePath:" + filePath);
                    sendFile(groupId, topicName, filePath);
                    break;
                case "receiveFile":
                    topicName = args[2];
                    receiveFile(groupId, topicName);
                    break;
            }
        } catch (BrokerException e) {
            log.error("execute {} failed, exception:{}", command, e);
            System.out.println("execute " + command + " failed, exception:" + e);
            System.exit(1);
        }

        if (command.equals("subscribe")) {
            // subscribe will be auto unSubscribed after 1 hour (just for test)
            Thread.sleep(60 * 60 * 1000L);
            subscribeIdList.forEach(subscribeId -> {
                try {
                    unSubscribe(subscribeId);
                } catch (BrokerException e) {
                    log.error("unsubscribe topic failed, subscribeId:{}", subscribeId);
                }
            });
        }
        System.exit(0);
    }

    private static void openTopic(String topicName) throws BrokerException {
        boolean result = weEventClient.open(topicName);
        if (!result) {
            log.error("open topic:[{}] failed.", topicName);
            System.out.println("open topic:[" + topicName + "] failed.");
        } else {
            log.info("open topic:{} success.", topicName);
            System.out.println("open topic:[" + topicName + "] success.");
        }
    }

    private static void subscribe(String topicName) throws BrokerException {
        Map<String, String> ext = new HashMap<>();

        String subscribeId = weEventClient.subscribe(topicName, WeEvent.OFFSET_LAST, ext, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info("receive event:{}", event);
                System.out.println("received event: " + event);
                System.out.println("event content:" + new String(event.getContent(), StandardCharsets.UTF_8));
            }

            @Override
            public void onException(Throwable e) {
                log.error("onException:", e);
            }
        });
        log.info("subscribe topic:[{}] success, subscribeId:[{}]", topicName, subscribeId);
        System.out.println("subscribe topic:[" + topicName + "] success, subscribeId:" + subscribeId);
        subscribeIdList.add(subscribeId);
    }

    private static void publish(String topicName, String content) throws BrokerException {
        WeEvent event = new WeEvent(topicName, content.getBytes(StandardCharsets.UTF_8));
        SendResult sendResult = weEventClient.publish(event);
        if (!sendResult.getStatus().equals(SendResult.SendResultStatus.SUCCESS)) {
            log.error("publish event by topic:[{}] failed, sendResult:{}.", topicName, sendResult);
            System.out.println("publish event by topic:[" + topicName + "] failed, sendResult:" + sendResult);
        } else {
            log.info("publish event to:{} success, sendResult:{}.", topicName, sendResult);
            System.out.println("publish event by topic:[" + topicName + "] success, sendResult:" + sendResult);
        }
    }

    private static void unSubscribe(String subscriptionId) throws BrokerException {
        boolean unSubscribe = weEventClient.unSubscribe(subscriptionId);
        if (!unSubscribe) {
            log.error("unSubscribe subscriptionId:{} failed.", subscriptionId);
            System.out.println("unSubscribe failed, subscriptionId:" + subscriptionId);
        } else {
            log.info("unSubscribe subscriptionId:{} success.", subscriptionId);
            System.out.println("unSubscribe subscriptionId:" + subscriptionId + " success.");
        }

    }

    private static void status(String topicName) throws BrokerException {
        if (!weEventClient.exist(topicName)) {
            log.error("get topic info failed due to topic not exist. topic:{}", topicName);
            System.out.println("get topic info failed due to topic not exist. topic:{" + topicName + "}");
            System.exit(1);
        }
        TopicInfo topicInfo = weEventClient.state(topicName);
        log.info("get topic info success, topicInfo:{}", JsonHelper.object2Json(topicInfo));
        System.out.println("get topic info success, topicInfo:" + JsonHelper.object2Json(topicInfo));
    }

    private static void getEvent(String eventId) throws BrokerException {
        WeEvent event = weEventClient.getEvent(eventId);
        log.info("getEvent success, event:{}", event);
        System.out.println("getEvent success, event:" + event);
    }

    private static void sendFile(String groupId, String topicName, String filePath) throws BrokerException {
        try {
            IWeEventFileClient weEventFileClient = getIWeEventFileClient(groupId);
            weEventFileClient.openTransport4Sender(topicName);
            FileChunksMeta fileChunksMeta = weEventFileClient.publishFile(topicName, new File(filePath).getAbsolutePath(), true);
            log.info("sendFile success, fileChunksMeta:{}", JsonHelper.object2Json(fileChunksMeta));
            System.out.println("sendFile success, fileChunksMeta:" + JsonHelper.object2Json(fileChunksMeta));
        } catch (Exception e) {
            log.error("send file failed.", e);
            System.out.println("send file failed." + e);
        }
    }

    private static void receiveFile(String groupId, String topicName) throws BrokerException {
        IWeEventFileClient.FileListener fileListener = new IWeEventFileClient.FileListener() {
            @Override
            public void onFile(String topicName, String fileName) {
                log.info("+++++++topic name: {}, file name: {}", topicName, fileName);
                System.out.println(new File(localReceivePath + "/" + fileName).getPath());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        };
        try {
            IWeEventFileClient weEventFileClient = getIWeEventFileClient(groupId);
            weEventFileClient.openTransport4Receiver(topicName, fileListener);
            Thread.sleep(1000 * 60 * 5);
        } catch (Exception e) {
            log.error("receive file failed.", e);
            System.out.println("receive file failed." + e);
        }
    }

    private static IWeEventFileClient getIWeEventFileClient(String groupId) {
        FiscoConfig fiscoConfig = new FiscoConfig();
        fiscoConfig.load("");
        IWeEventFileClient weEventFileClient = IWeEventFileClient.build(groupId, localReceivePath, fileChunkSize, fiscoConfig);
        return weEventFileClient;
    }
}
