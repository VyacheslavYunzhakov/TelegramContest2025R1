package org.telegramIunzhakov.ui.Stories;

import android.view.View;

import org.telegramIunzhakov.messenger.AndroidUtilities;
import org.telegramIunzhakov.messenger.ChatObject;
import org.telegramIunzhakov.messenger.MessagesController;
import org.telegramIunzhakov.messenger.MessagesStorage;
import org.telegramIunzhakov.messenger.NotificationCenter;
import org.telegramIunzhakov.messenger.UserConfig;
import org.telegramIunzhakov.messenger.support.LongSparseLongArray;
import org.telegramIunzhakov.tgnet.ConnectionsManager;
import org.telegramIunzhakov.tgnet.TLRPC;
import org.telegramIunzhakov.tgnet.Vector;
import org.telegramIunzhakov.tgnet.tl.TL_stories;
import org.telegramIunzhakov.ui.Cells.DialogCell;
import org.telegramIunzhakov.ui.Cells.UserCell;
import org.telegramIunzhakov.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class UserListPoller {

    private static UserListPoller[] istances = new UserListPoller[UserConfig.MAX_ACCOUNT_COUNT];

    final int currentAccount;

    private UserListPoller(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    public static UserListPoller getInstance(int account) {
        if (istances[account] == null) {
            istances[account] = new UserListPoller(account);
        }
        return istances[account];
    }

    LongSparseLongArray userPollLastTime = new LongSparseLongArray();
    ArrayList<Long> dialogIds = new ArrayList<>();
    ArrayList<Long> collectedDialogIds = new ArrayList<>();

    ArrayList<Integer> runningRequests = new ArrayList<>();

    Runnable requestCollectedRunnables = new Runnable() {
        @Override
        public void run() {
            if (!collectedDialogIds.isEmpty()) {
                ArrayList<Long> dialogsFinal = new ArrayList<>(collectedDialogIds);
                collectedDialogIds.clear();
                TL_stories.TL_stories_getPeerMaxIDs request = new TL_stories.TL_stories_getPeerMaxIDs();
                for (int i = 0; i < dialogsFinal.size(); i++) {
                    request.id.add(MessagesController.getInstance(currentAccount).getInputPeer(dialogsFinal.get(i)));
                }
                ConnectionsManager.getInstance(currentAccount).sendRequest(request, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (response instanceof Vector) {
                        Vector vector = (Vector) response;
                        ArrayList<TLRPC.User> usersToUpdate = new ArrayList<>();
                        ArrayList<TLRPC.Chat> chatsToUpdate = new ArrayList<>();
                        for (int i = 0; i < vector.objects.size(); i++) {
                            if (dialogsFinal.get(i) > 0) {
                                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(dialogsFinal.get(i));
                                if (user == null) {
                                    continue;
                                }
                                user.stories_max_id = ((Vector.Int) vector.objects.get(i)).value;
                                if (user.stories_max_id != 0) {
                                    user.flags2 |= 32;
                                } else {
                                    user.flags2 &= ~32;
                                }
                                usersToUpdate.add(user);
                            } else {
                                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(dialogsFinal.get(i));
                                if (chat == null) {
                                    continue;
                                }
                                chat.stories_max_id = ((Vector.Int) vector.objects.get(i)).value;
                                if (chat.stories_max_id != 0) {
                                    chat.flags2 |= 16;
                                } else {
                                    chat.flags2 &= ~16;
                                }
                                chatsToUpdate.add(chat);
                            }
                        }
                        MessagesStorage.getInstance(currentAccount).putUsersAndChats(usersToUpdate, chatsToUpdate, true, true);
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, 0);
                    }
                }));
            }
        }
    };

    public void checkList(RecyclerListView recyclerListView) {
        long currentTime = System.currentTimeMillis();
        dialogIds.clear();
        for (int i = 0; i < recyclerListView.getChildCount(); i++) {
            View child = recyclerListView.getChildAt(i);
            long dialogId = 0;
            if (child instanceof DialogCell) {
                dialogId = ((DialogCell) child).getDialogId();
            } else if (child instanceof UserCell) {
                dialogId = ((UserCell) child).getDialogId();
            }

            if (dialogId > 0) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(dialogId);
                if (user != null && !user.bot && !user.self && !user.contact && user.status != null && !(user.status instanceof TLRPC.TL_userStatusEmpty)) {
                    long lastPollTime = userPollLastTime.get(dialogId, 0);
                    if (currentTime - lastPollTime > 60 * 60 * 1000) {
                        userPollLastTime.put(dialogId, currentTime);
                        dialogIds.add(dialogId);
                    }
                }
            } else {
                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-dialogId);
                if (ChatObject.isChannel(chat) && !ChatObject.isMonoForum(chat)) {
                    long lastPollTime = userPollLastTime.get(dialogId, 0);
                    if (currentTime - lastPollTime > 60 * 60 * 1000) {
                        userPollLastTime.put(dialogId, currentTime);
                        dialogIds.add(dialogId);
                    }
                }
            }
        }
        if (!dialogIds.isEmpty()) {
            collectedDialogIds.addAll(dialogIds);
            AndroidUtilities.cancelRunOnUIThread(requestCollectedRunnables);
            AndroidUtilities.runOnUIThread(requestCollectedRunnables, 300);
        }
    }
}
