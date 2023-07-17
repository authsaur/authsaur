package com.deepoove.authsaur.notifications;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AliyunSmsSender implements SmsSender {

    private static String accessKey = "accessKey";
    private static String accessKeySecret = "accessKeySecret";
    private String templateCode = "SMS_154950909";
    private String signName = "阿里云短信测试";

    private boolean canSend = true;

    @Override
    public boolean send(String from, String to, String message) {
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder().accessKeyId(accessKey).accessKeySecret(accessKeySecret).build());
        AsyncClient client = AsyncClient.builder().region("cn-hangzhou") // Region ID
                .credentialsProvider(provider).overrideConfiguration(
                        ClientOverrideConfiguration.create().setEndpointOverride("dysmsapi.aliyuncs.com")).build();
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder().signName(signName).templateCode(templateCode)
//                .phoneNumbers("15967192892")
                .phoneNumbers(to).templateParam("{\"code\":\"" + message + "\"}").build();
        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        SendSmsResponse resp = null;
        try {
            resp = response.get();
//            System.out.println(new Gson().toJson(resp));
            client.close();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean canSend() {
        return canSend;
    }

    public void setCanSend(boolean canSend) {
        this.canSend = canSend;
    }
}
