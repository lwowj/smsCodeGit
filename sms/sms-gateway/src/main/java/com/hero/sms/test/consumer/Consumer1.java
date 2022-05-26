package com.hero.sms.test.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

public class Consumer1 {

    public void start() throws MQClientException {
        String consumerGroup = "test_group";
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr("localhost:9876");
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.subscribe("test_topic","tagA");
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            public ConsumeConcurrentlyStatus consumeMessage(
                    List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    String topic = msg.getTopic();
                    String tags = msg.getTags();
                    System.out.println("收到消息：" + " topic :" + topic + " ,tags : " + tags + " ,msg : " +new String(msg.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
    public static void main(String[] args) {
            Consumer1 consumer1 = new Consumer1();
        try {
            consumer1.start();
            System.out.println("消费者1启动成功");
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }
}
