package com.leyou.sms.constants;

public class MQConstants {
    public static final class Exchange {
        // ... 略
        /**
         * 消息服务交换机名称
         */
        public static final String SMS_EXCHANGE_NAME = "ly.sms.exchange";
    }

    public static final class RoutingKey {
        // ... 略
        /**
         * 短信发送的routing-key
         */
        public static final String VERIFY_CODE_KEY = "sms.verify.code";
    }


    public static final class Queue{
        // ... 略
        /**
         * 短信的收发队列
         */
        public static final String SMS_VERIFY_CODE_QUEUE = "sms.verify.code.queue";
    }
}
