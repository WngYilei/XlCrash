/*
 * *   
 *   Created by wyl on 3/13/21 1:09 PM  
 *  project_name:CashDemo     
 *  创建时间：3/13/21 1:09 PM   
 *  修改人：wyl   
 *  修改时间：3/13/21 1:09 PM    
 *  修改备注：
 *  版权所有违法必究   
 *  @version    
 * /
 */
package com.xl.xlcash.utils;

import android.text.TextUtils;
import android.util.Log;

import com.xl.xlcash.config.CashConfig;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class EmailUtils {

    private static class HODLER {
        static EmailUtils emailUtils = new EmailUtils();
    }

    public static EmailUtils getInstance() {
        return HODLER.emailUtils;
    }


    public String toEmail = "";

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();


    public void setEmail(String emailAdress) {
        toEmail = emailAdress;
    }

    public void sendEmail(final String title, final String message) {
        if (TextUtils.isEmpty(toEmail)) {
            return;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    props.put(CashConfig.HOST, CashConfig.SMTP);
                    Session session = Session.getInstance(props, null);

                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(CashConfig.FROM_EMAIL);
                    msg.setRecipients(Message.RecipientType.TO, toEmail);
                    msg.setSubject(title);
                    msg.setSentDate(new Date());
                    msg.setText(message);
                    Transport.send(msg, CashConfig.FROM_EMAIL, CashConfig.PASSWORD);
                    Log.e("TAG", "邮件发送成功");
                } catch (MessagingException e) {
                    e.printStackTrace();
                    Log.e("TAG", "邮件发送失败: " + e);
                }
            }
        });

    }


}
