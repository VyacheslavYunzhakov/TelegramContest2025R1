package org.telegramIunzhakov.ui.Stories;

import android.text.TextUtils;

import org.telegramIunzhakov.messenger.ChatObject;
import org.telegramIunzhakov.messenger.MessagesController;
import org.telegramIunzhakov.tgnet.TLRPC;

public class ChannelBoostUtilities {
    public static String createLink(int currentAccount, long dialogId) {
        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-dialogId);
        String username = ChatObject.getPublicUsername(chat);
        if (!TextUtils.isEmpty(username)) {
            return "https://t.me/boost/" + ChatObject.getPublicUsername(chat);
        } else {
            return "https://t.me/boost/?c=" + -dialogId;
        }
    }
}
