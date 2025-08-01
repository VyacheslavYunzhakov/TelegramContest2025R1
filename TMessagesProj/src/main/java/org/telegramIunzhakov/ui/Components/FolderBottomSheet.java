package org.telegramIunzhakov.ui.Components;

import static org.telegramIunzhakov.messenger.AndroidUtilities.dp;
import static org.telegramIunzhakov.messenger.LocaleController.formatPluralSpannable;
import static org.telegramIunzhakov.messenger.LocaleController.formatPluralString;
import static org.telegramIunzhakov.messenger.LocaleController.formatSpannable;
import static org.telegramIunzhakov.messenger.LocaleController.formatString;
import static org.telegramIunzhakov.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.telegramIunzhakov.messenger.AndroidUtilities;
import org.telegramIunzhakov.messenger.BotWebViewVibrationEffect;
import org.telegramIunzhakov.messenger.ChatObject;
import org.telegramIunzhakov.messenger.DialogObject;
import org.telegramIunzhakov.messenger.Emoji;
import org.telegramIunzhakov.messenger.LocaleController;
import org.telegramIunzhakov.messenger.MessageObject;
import org.telegramIunzhakov.messenger.MessagesController;
import org.telegramIunzhakov.messenger.NotificationCenter;
import org.telegramIunzhakov.messenger.R;
import org.telegramIunzhakov.messenger.UserObject;
import org.telegramIunzhakov.messenger.Utilities;
import org.telegramIunzhakov.tgnet.TLObject;
import org.telegramIunzhakov.tgnet.TLRPC;
import org.telegramIunzhakov.tgnet.Vector;
import org.telegramIunzhakov.tgnet.tl.TL_chatlists;
import org.telegramIunzhakov.ui.ActionBar.AlertDialog;
import org.telegramIunzhakov.ui.ActionBar.BaseFragment;
import org.telegramIunzhakov.ui.ActionBar.INavigationLayout;
import org.telegramIunzhakov.ui.ActionBar.Theme;
import org.telegramIunzhakov.ui.Cells.GroupCreateUserCell;
import org.telegramIunzhakov.ui.Cells.TextInfoPrivacyCell;
import org.telegramIunzhakov.ui.ChatActivity;
import org.telegramIunzhakov.ui.DialogsActivity;
import org.telegramIunzhakov.ui.FilterCreateActivity;
import org.telegramIunzhakov.ui.FiltersSetupActivity;

import java.util.ArrayList;
import java.util.List;

public class FolderBottomSheet extends BottomSheetWithRecyclerListView {

    private String slug;
    private int filterId = -1;
    private TL_chatlists.chatlist_ChatlistInvite invite;
    private TL_chatlists.TL_chatlists_chatlistUpdates updates;
    private boolean deleting;

    private CharSequence title = "";
    private ArrayList<TLRPC.MessageEntity> titleEntities = new ArrayList<>();
    private boolean titleNoanimate;
    private CharSequence escapedTitle = "";
    private ArrayList<TLRPC.Peer> peers;
    private ArrayList<Long> alreadyJoined = new ArrayList<>();
    private ArrayList<Long> selectedPeers = new ArrayList<>();
    private ArrayList<TLRPC.Peer> alreadyPeers;

    private FrameLayout bulletinContainer;

    private Button button;
    private View buttonShadow;
    private TitleCell titleCell;

    private int rowsCount;

    private int titleRow;
    private int sectionRow;
    private int headerRow;
    private int usersStartRow;
    private int usersEndRow;
    private int usersSectionRow;
    private int alreadyHeaderRow;
    private int alreadyUsersStartRow;
    private int alreadyUsersEndRow;
    private int alreadySectionRow;

    private HeaderCell headerCell;

    public static void showForDeletion(final BaseFragment fragment, final int filterId, final Utilities.Callback<Boolean> whenDone) {
        ArrayList<MessagesController.DialogFilter> myFilters = fragment.getMessagesController().dialogFilters;
        MessagesController.DialogFilter f = null;
        if (myFilters != null) {
            for (int i = 0; i < myFilters.size(); ++i) {
                if (myFilters.get(i).id == filterId) {
                    f = myFilters.get(i);
                    break;
                }
            }
        }
        final MessagesController.DialogFilter filter = f;

        Runnable showDeleteAlert = () -> {
            TL_chatlists.TL_chatlists_getLeaveChatlistSuggestions req = new TL_chatlists.TL_chatlists_getLeaveChatlistSuggestions();
            req.chatlist = new TL_chatlists.TL_inputChatlistDialogFilter();
            req.chatlist.filter_id = filterId;
            fragment.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                if (fragment.getParentActivity() == null) {
                    return;
                }
                FolderBottomSheet sheet;
                if (res instanceof Vector) {
                    Vector vector = (Vector) res;
                    ArrayList<Long> suggestions = new ArrayList<>();
                    try {
                        for (int i = 0; i < vector.objects.size(); ++i) {
                            TLRPC.Peer peer = (TLRPC.Peer) vector.objects.get(i);
                            suggestions.add(DialogObject.getPeerDialogId(peer));
                        }
                    } catch (Exception ignore) {
                    }
                    sheet = new FolderBottomSheet(fragment, filterId, suggestions);
                } else {
                    sheet = new FolderBottomSheet(fragment, filterId, (List<Long>) null);
                }
                sheet.setOnDone(whenDone);
                fragment.showDialog(sheet);
            }));
        };

        if (filter != null && filter.isMyChatlist()) {
            AlertDialog alertDialog = new AlertDialog.Builder(fragment.getContext())
                .setTitle(getString(R.string.FilterDelete))
                .setMessage(getString(R.string.FilterDeleteAlertLinks))
                .setNegativeButton(getString(R.string.Cancel), (d, w) -> {
                    if (whenDone != null) {
                        whenDone.run(false);
                    }
                })
                .setPositiveButton(getString(R.string.Delete), (d, w) -> {
                    showDeleteAlert.run();
                })
                .create();
            fragment.showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        } else {
            showDeleteAlert.run();
        }
    }

    public FolderBottomSheet(BaseFragment fragment, int filterId, List<Long> select) {
        super(fragment, false, false);

        this.filterId = filterId;
        this.deleting = true;

        peers = new ArrayList<>();
        selectedPeers.clear();
        if (select != null) {
            selectedPeers.addAll(select);
        }

        ArrayList<MessagesController.DialogFilter> myFilters = fragment.getMessagesController().dialogFilters;
        MessagesController.DialogFilter filter = null;
        if (myFilters != null) {
            for (int i = 0; i < myFilters.size(); ++i) {
                if (myFilters.get(i).id == filterId) {
                    filter = myFilters.get(i);
                    break;
                }
            }
        }
        if (filter != null) {
            title = filter.name;
            titleEntities = filter.entities;
            titleNoanimate = filter.title_noanimate;

            for (int i = 0; i < selectedPeers.size(); ++i) {
                long did = selectedPeers.get(i);
                TLRPC.Peer peer = fragment.getMessagesController().getPeer(did);
                if (peer instanceof TLRPC.TL_peerChat || peer instanceof TLRPC.TL_peerChannel) {
                    peers.add(peer);
                }
            }

            for (int i = 0; i < filter.alwaysShow.size(); ++i) {
                long did = filter.alwaysShow.get(i);
                if (selectedPeers.contains(did)) {
                    continue;
                }
                TLRPC.Peer peer = fragment.getMessagesController().getPeer(did);
                if (peer instanceof TLRPC.TL_peerChat || peer instanceof TLRPC.TL_peerChannel) {
                    TLRPC.Chat chat = fragment.getMessagesController().getChat(-did);
                    if (chat == null || !ChatObject.isNotInChat(chat)) {
                        peers.add(peer);
                    }
                }
            }
        }

        init();
    }

    public FolderBottomSheet(BaseFragment fragment, int filterId, TL_chatlists.TL_chatlists_chatlistUpdates updates) {
        super(fragment, false, false);

        this.filterId = filterId;
        this.updates = updates;

        selectedPeers.clear();
        peers = updates.missing_peers;
        ArrayList<MessagesController.DialogFilter> myFilters = fragment.getMessagesController().dialogFilters;
        if (myFilters != null) {
            for (int i = 0; i < myFilters.size(); ++i) {
                if (myFilters.get(i).id == filterId) {
                    title = myFilters.get(i).name;
                    break;
                }
            }
        }

        init();
    }

    public FolderBottomSheet(BaseFragment fragment, String slug, TL_chatlists.chatlist_ChatlistInvite invite) {
        super(fragment, false, false);

        this.slug = slug;
        this.invite = invite;

        selectedPeers.clear();
        if (invite instanceof TL_chatlists.TL_chatlists_chatlistInvite) {
            TL_chatlists.TL_chatlists_chatlistInvite chatlistInvite = (TL_chatlists.TL_chatlists_chatlistInvite) invite;
            title = chatlistInvite.title.text;
            titleEntities = chatlistInvite.title.entities;
            titleNoanimate = chatlistInvite.title_noanimate;
            peers = chatlistInvite.peers;
        } else if (invite instanceof TL_chatlists.TL_chatlists_chatlistInviteAlready) {
            TL_chatlists.TL_chatlists_chatlistInviteAlready inv = (TL_chatlists.TL_chatlists_chatlistInviteAlready) invite;
            peers = inv.missing_peers;
            alreadyPeers = inv.already_peers;
            this.filterId = inv.filter_id;
            ArrayList<MessagesController.DialogFilter> myFilters = fragment.getMessagesController().dialogFilters;
            if (myFilters != null) {
                for (int i = 0; i < myFilters.size(); ++i) {
                    final MessagesController.DialogFilter filter = myFilters.get(i);
                    if (filter.id == filterId) {
                        title = filter.name;
                        titleEntities = filter.entities;
                        titleNoanimate = filter.title_noanimate;
                        break;
                    }
                }
            }
        }

        init();
    }

    private void init() {
//        escapedTitle = title.replace('*', '✱');
        escapedTitle = AndroidUtilities.replaceCharSequence("*", title, "✱");

        if (peers != null) {
            for (int i = 0; i < peers.size(); ++i) {
                TLRPC.Peer peer = peers.get(i);
                if (peer == null) {
                    continue;
                }
                boolean joined = false;
                long did = 0;
                if (peer instanceof TLRPC.TL_peerUser) {
                    did = peer.user_id;
                } else if (peer instanceof TLRPC.TL_peerChat) {
                    did = -peer.chat_id;
                    joined = !ChatObject.isNotInChat(getBaseFragment().getMessagesController().getChat(-did));
                } else if (peer instanceof TLRPC.TL_peerChannel) {
                    did = -peer.channel_id;
                    joined = !ChatObject.isNotInChat(getBaseFragment().getMessagesController().getChat(-did));
                }
                if (did != 0 && !deleting) {
                    if (joined) {
                        alreadyJoined.add(did);
                    }
                    selectedPeers.add(did);
                }
            }
        }

        rowsCount = 0;
        titleRow = rowsCount++;
        if (peers != null && !peers.isEmpty()) {
            sectionRow = rowsCount++;
            headerRow = rowsCount++;
            usersStartRow = rowsCount;
            usersEndRow = (rowsCount += peers.size());
        } else {
            sectionRow = -1;
            headerRow = -1;
            usersStartRow = -1;
            usersEndRow = -1;
        }
        usersSectionRow = rowsCount++;
        if (alreadyPeers != null && !alreadyPeers.isEmpty()) {
            alreadyHeaderRow = rowsCount++;
            alreadyUsersStartRow = rowsCount;
            alreadyUsersEndRow = (rowsCount += alreadyPeers.size());
            alreadySectionRow = rowsCount++;
        } else {
            alreadyHeaderRow = -1;
            alreadyUsersStartRow = -1;
            alreadyUsersEndRow = -1;
            alreadySectionRow = -1;
        }

        button = new Button(getContext(), "");
        button.setOnClickListener(e -> onJoinButtonClicked());
        containerView.addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 10, 16, 10));

        buttonShadow = new View(getContext());
        buttonShadow.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        containerView.addView(buttonShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1f / AndroidUtilities.density, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 6, 0, 6, 68));
        recyclerListView.setPadding(dp(6), 0, dp(6), dp(button != null ? 68 : 0));

        bulletinContainer = new FrameLayout(getContext());
        containerView.addView(bulletinContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 100, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 6, 0, 6, 68));

        fixNavigationBar(Theme.getColor(Theme.key_dialogBackground));

        updateCount(false);

        actionBar.setTitle(getTitle());
    }

    private int reqId = -1;

    private void onJoinButtonClicked() {
        if (button != null && button.isLoading()) {
            return;
        }
        if (peers == null) {
            dismiss();
            return;
        }
        if (peers.isEmpty() && !deleting) {
            dismiss();
            return;
        }

        if (selectedPeers.isEmpty() && invite instanceof TL_chatlists.TL_chatlists_chatlistInvite) {
            AndroidUtilities.shakeViewSpring(button, shiftDp = -shiftDp);
            BotWebViewVibrationEffect.APP_ERROR.vibrate();
            return;
        }

        final ArrayList<TLRPC.InputPeer> inputPeers = new ArrayList<>();
        for (int i = 0; i < peers.size(); ++i) {
            long did = DialogObject.getPeerDialogId(peers.get(i));
            if (selectedPeers.contains(did)) {
                inputPeers.add(getBaseFragment().getMessagesController().getInputPeer(did));
            }
        }

        final Utilities.Callback<Integer> after;
        TLObject reqObject;
        if (deleting) {
            TL_chatlists.TL_chatlists_leaveChatlist req = new TL_chatlists.TL_chatlists_leaveChatlist();
            req.chatlist = new TL_chatlists.TL_inputChatlistDialogFilter();
            req.chatlist.filter_id = filterId;
            req.peers.addAll(inputPeers);
            reqObject = req;
        } else if (updates != null) {
            if (inputPeers.isEmpty()) {
                TL_chatlists.TL_chatlists_hideChatlistUpdates req = new TL_chatlists.TL_chatlists_hideChatlistUpdates();
                req.chatlist = new TL_chatlists.TL_inputChatlistDialogFilter();
                req.chatlist.filter_id = filterId;
                getBaseFragment().getConnectionsManager().sendRequest(req, null);
                getBaseFragment().getMessagesController().invalidateChatlistFolderUpdate(filterId);
                dismiss();
                return;
            }
            TL_chatlists.TL_chatlists_joinChatlistUpdates req = new TL_chatlists.TL_chatlists_joinChatlistUpdates();
            req.chatlist = new TL_chatlists.TL_inputChatlistDialogFilter();
            req.chatlist.filter_id = filterId;
            req.peers.addAll(inputPeers);
            reqObject = req;
        } else {
            if (invite instanceof TL_chatlists.TL_chatlists_chatlistInviteAlready && inputPeers.isEmpty()) {
                dismiss();
                return;
            }
            TL_chatlists.TL_chatlists_joinChatlistInvite req = new TL_chatlists.TL_chatlists_joinChatlistInvite();
            req.slug = slug;
            req.peers.addAll(inputPeers);
            reqObject = req;
        }

        final INavigationLayout parentLayout = getBaseFragment().getParentLayout();
        if (deleting) {
            if (parentLayout != null) {
                BaseFragment fragment = parentLayout.getLastFragment();
                UndoView undoView = null;
                if (fragment instanceof ChatActivity) {
                    undoView = ((ChatActivity) fragment).getUndoView();
                } else if (fragment instanceof DialogsActivity) {
                    undoView = ((DialogsActivity) fragment).getUndoView();
                } else if (fragment instanceof FiltersSetupActivity) {
                    undoView = ((FiltersSetupActivity) fragment).getUndoView();
                } else if (fragment instanceof FilterCreateActivity) {
                    List<BaseFragment> stack = parentLayout.getFragmentStack();
                    if (stack.size() >= 2 && stack.get(stack.size() - 2) instanceof FiltersSetupActivity) {
                        FiltersSetupActivity setupFragment = (FiltersSetupActivity) stack.get(stack.size() - 2);
                        fragment.finishFragment();
                        undoView = setupFragment.getUndoView();
                    }
                }
                if (undoView == null) {
                    button.setLoading(true);
                    reqId = getBaseFragment().getConnectionsManager().sendRequest(reqObject, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        reqId = -1;
                        BulletinFactory.of(fragment)
                            .createSimpleBulletin(R.raw.ic_delete, formatString(R.string.FolderLinkDeletedTitle, title), formatPluralString("FolderLinkDeletedSubtitle", inputPeers.size()))
                            .setDuration(Bulletin.DURATION_PROLONG)
                            .show();
                        success = true;
                        dismiss();
                        getBaseFragment().getMessagesController().invalidateChatlistFolderUpdate(filterId);
                    }));
                    return;
                }

                ArrayList<Long> dids = new ArrayList<>();
                for (int i = 0; i < inputPeers.size(); ++i) {
                    dids.add(DialogObject.getPeerDialogId(inputPeers.get(i)));
                }

                final Pair<Runnable, Runnable> applyOrUndo = getBaseFragment().getMessagesController().removeFolderTemporarily(filterId, dids);
                undoView.showWithAction(0, UndoView.ACTION_SHARED_FOLDER_DELETED, title, (Integer) inputPeers.size(), () -> {
                    reqId = getBaseFragment().getConnectionsManager().sendRequest(reqObject, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        reqId = -1;
                        applyOrUndo.first.run();
                    }));
                }, applyOrUndo.second);

                success = true;
                dismiss();
                getBaseFragment().getMessagesController().invalidateChatlistFolderUpdate(filterId);
            }
        } else if (parentLayout != null) {
            final Utilities.Callback<BaseFragment> bulletin = (fragment) -> {
                if (updates != null || invite instanceof TL_chatlists.TL_chatlists_chatlistInviteAlready) {
                    BulletinFactory.of(fragment)
                        .createSimpleBulletin(
                            R.raw.folder_in,
                            AndroidUtilities.replaceTags(LocaleController.formatString(R.string.FolderLinkUpdatedTitle, escapedTitle)),
                            inputPeers.size() <= 0 ?
                                formatPluralString("FolderLinkUpdatedSubtitle", alreadyJoined.size()) :
                                formatPluralString("FolderLinkUpdatedJoinedSubtitle", inputPeers.size())
                        )
                        .setDuration(Bulletin.DURATION_PROLONG)
                        .show();
                } else {
                    BulletinFactory.of(fragment)
                        .createSimpleBulletin(
                            R.raw.contact_check,
                            AndroidUtilities.replaceTags(LocaleController.formatString(R.string.FolderLinkAddedTitle, escapedTitle)),
                            formatPluralString("FolderLinkAddedSubtitle", inputPeers.size())
                        )
                        .setDuration(Bulletin.DURATION_PROLONG)
                        .show();
                }
            };
            if (updates != null) {
                after = (fid) -> bulletin.run(parentLayout.getLastFragment());
            } else {
                after = (fid) -> {
                    List<BaseFragment> fragments = parentLayout.getFragmentStack();
                    boolean last = true;
                    BaseFragment lastFragment = null;
                    for (int i = fragments.size() - 1; i >= 0; --i) {
                        lastFragment = fragments.get(i);
                        if (lastFragment instanceof DialogsActivity) {
                            break;
                        }

                        if (last) {
                            lastFragment.finishFragment();
                            last = false;
                        } else {
                            lastFragment.removeSelfFromStack();
                        }
                    }
                    final BaseFragment fragment = lastFragment;
                    if (lastFragment instanceof DialogsActivity) {
                        DialogsActivity dialogsActivity = (DialogsActivity) lastFragment;
                        dialogsActivity.closeSearching();
                        AndroidUtilities.runOnUIThread(() -> {
                            dialogsActivity.scrollToFolder(fid);
                            AndroidUtilities.runOnUIThread(() -> bulletin.run(fragment), 200);
                        }, 80);
                    } else {
                        bulletin.run(fragment);
                    }
                };
            }

            boolean hasNewChatsInArchived = false;
            for (int i = 0; i < inputPeers.size(); ++i) {
                TLRPC.InputPeer peer = inputPeers.get(i);
                long did = DialogObject.getPeerDialogId(peer);
                if (!alreadyJoined.contains(did)) {
                    hasNewChatsInArchived = true;
                    break;
                }
            }

            if (hasNewChatsInArchived) {
                boolean[] folderCreated = new boolean[1];
                getBaseFragment().getMessagesController().ensureFolderDialogExists(1, folderCreated);
                if (folderCreated[0]) {
                    getBaseFragment().getNotificationCenter().postNotificationName(NotificationCenter.dialogsNeedReload);
                }
            }

            button.setLoading(true);
            reqId = getBaseFragment().getConnectionsManager().sendRequest(reqObject, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                reqId = -1;
                if (FilterCreateActivity.processErrors(err, getBaseFragment(), BulletinFactory.of(getBaseFragment())) && res != null) {
                    int foundFilterId = -1;
                    if (res instanceof TLRPC.Updates) {
                        ArrayList<TLRPC.Update> updates = ((TLRPC.Updates) res).updates;
                        if (!updates.isEmpty()) {
                            for (int i = 0; i < updates.size(); ++i) {
                                if (updates.get(i) instanceof TLRPC.TL_updateDialogFilter) {
                                    TLRPC.TL_updateDialogFilter upd = (TLRPC.TL_updateDialogFilter) updates.get(i);
                                    foundFilterId = upd.id;
                                    break;
                                }
                            }
                        } else if (((TLRPC.Updates) res).update instanceof TLRPC.TL_updateDialogFilter) {
                            TLRPC.TL_updateDialogFilter upd = (TLRPC.TL_updateDialogFilter) ((TLRPC.Updates) res).update;
                            foundFilterId = upd.id;
                        }
                    }
                    final int newFilterId = foundFilterId;

                    if (invite instanceof TL_chatlists.TL_chatlists_chatlistInvite) {
                        getBaseFragment().getMessagesController().loadRemoteFilters(true, success -> {
                            FolderBottomSheet.this.success = success;
                            dismiss();
                            after.run(newFilterId);
                        });
                    } else {
                        if (updates != null) {
                            getBaseFragment().getMessagesController().checkChatlistFolderUpdate(filterId, true);
                        }
                        success = true;
                        dismiss();
                        after.run(newFilterId);
                    }
                } else {
                    button.setLoading(false);
                }
            }));
        }
    }

    private boolean success;
    private Utilities.Callback<Boolean> onDone;
    public void setOnDone(Utilities.Callback<Boolean> listener) {
        this.onDone = listener;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (reqId >= 0) {
            getBaseFragment().getConnectionsManager().cancelRequest(reqId, true);
        }
        if (onDone != null) {
            onDone.run(success);
            onDone = null;
        }
    }

    private int shiftDp = -5;
    private long lastClickedDialogId, lastClicked;

    @Override
    public void onViewCreated(FrameLayout containerView) {
        super.onViewCreated(containerView);
        recyclerListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerListView.setPadding(dp(6), 0, dp(6), dp(button != null ? 68 : 0));
        recyclerListView.setOnItemClickListener((view, position) -> {
            if (view instanceof GroupCreateUserCell) {
                int peersPosition = position - 1 - usersStartRow;
                if (peersPosition < 0 || peersPosition >= peers.size()) {
                    return;
                }
                long did = DialogObject.getPeerDialogId(peers.get(peersPosition));
                if (selectedPeers.contains(did)) {
                    if (alreadyJoined.contains(did)) {
                        AndroidUtilities.shakeViewSpring(view, shiftDp = -shiftDp);
                        BotWebViewVibrationEffect.APP_ERROR.vibrate();
                        ArrayList<TLObject> array = new ArrayList<>();
                        String text;
                        if (did >= 0) {
                            array.add(getBaseFragment().getMessagesController().getUser(did));
                            text = "beep boop.";
                        } else {
                            TLRPC.Chat chat = getBaseFragment().getMessagesController().getChat(-did);
                            if (ChatObject.isChannelAndNotMegaGroup(chat)) {
                                text = getString(R.string.FolderLinkAlreadySubscribed);
                            } else {
                                text = getString(R.string.FolderLinkAlreadyJoined);
                            }
                            array.add(chat);
                        }
                        if (lastClickedDialogId != did || System.currentTimeMillis() - lastClicked > Bulletin.DURATION_SHORT) {
                            lastClickedDialogId = did;
                            lastClicked = System.currentTimeMillis();
                            BulletinFactory.of(bulletinContainer, null).createChatsBulletin(array, text, null).setDuration(Bulletin.DURATION_SHORT).show();
                        }
                        return;
                    }

                    selectedPeers.remove(did);
                    ((GroupCreateUserCell) view).setChecked(false, true);
                } else {
                    selectedPeers.add(did);
                    ((GroupCreateUserCell) view).setChecked(true, true);
                }
                updateCount(true);
                updateHeaderCell(true);
                announceSelection(false);
            }
        });
    }

    public void updateCount(boolean animated) {
        int count = selectedPeers.size();
        if (button != null) {
            if (deleting) {
                button.setText(count > 0 ? getString(R.string.FolderLinkButtonRemoveChats) : getString(R.string.FolderLinkButtonRemove), animated);
            } else if (peers == null || peers.isEmpty()) {
                button.setText(getString(R.string.OK), animated);
            } else if (invite instanceof TL_chatlists.TL_chatlists_chatlistInvite) {
                CharSequence title = this.title;
                title = Emoji.replaceEmoji(title, button.getTextPaint().getFontMetricsInt(), false);
                title = MessageObject.replaceAnimatedEmoji(title, titleEntities, button.getTextPaint().getFontMetricsInt());
                button.setText(formatSpannable(R.string.FolderLinkButtonAdd, title), animated);
                button.setEmojiCacheType(titleNoanimate ? AnimatedEmojiDrawable.CACHE_TYPE_NOANIMATE_FOLDER : AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES);
            } else {
                button.setText(count > 0 ? formatPluralString("FolderLinkButtonJoinPlural", count) : getString(R.string.FolderLinkButtonNone), animated);
            }
            button.setCount(count, animated);
            if (invite instanceof TL_chatlists.TL_chatlists_chatlistInvite) {
                button.setEnabled(!selectedPeers.isEmpty());
            }
        }
        if (titleCell != null) {
            titleCell.setSelectedCount(count, animated);
        }
    }

    private static class Button extends FrameLayout {
        Paint paint;
        AnimatedTextView.AnimatedTextDrawable text;
        AnimatedTextView.AnimatedTextDrawable countText;
        float countAlpha;
        AnimatedFloat countAlphaAnimated = new AnimatedFloat(350, CubicBezierInterpolator.EASE_OUT_QUINT);
        private View rippleView;
        private ShapeDrawable background;

        public Button(Context context, String string) {
            super(context);

            rippleView = new View(context);
            rippleView.setBackground(Theme.AdaptiveRipple.rect(Theme.getColor(Theme.key_featuredStickers_addButton), 8));
            addView(rippleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            setBackground(background = (ShapeDrawable) Theme.createRoundRectDrawable(dp(8), Theme.getColor(Theme.key_featuredStickers_addButton)));

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Theme.getColor(Theme.key_featuredStickers_buttonText));

            text = new AnimatedTextView.AnimatedTextDrawable(true, true, false);
            text.setAnimationProperties(.3f, 0, 250, CubicBezierInterpolator.EASE_OUT_QUINT);
            text.setCallback(this);
            text.setTextSize(dp(14));
            text.setTypeface(AndroidUtilities.bold());
            text.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            text.setText(string);
            text.setGravity(Gravity.CENTER_HORIZONTAL);

            countText = new AnimatedTextView.AnimatedTextDrawable(false, false, true);
            countText.setAnimationProperties(.3f, 0, 250, CubicBezierInterpolator.EASE_OUT_QUINT);
            countText.setCallback(this);
            countText.setTextSize(dp(12));
            countText.setTypeface(AndroidUtilities.bold());
            countText.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            countText.setText("");
            countText.setGravity(Gravity.CENTER_HORIZONTAL);

            setWillNotDraw(false);
        }

        public TextPaint getTextPaint() {
            return text.getPaint();
        }

        public void setEmojiCacheType(int cacheType) {
            text.setEmojiCacheType(cacheType);
        }

        public void setText(CharSequence newText, boolean animated) {
            if (animated) {
                text.cancelAnimation();
            }
            text.setText(newText, animated);
            invalidate();
        }

        private float loadingT = 0;
        private boolean loading;
        private ValueAnimator loadingAnimator;
        public void setLoading(boolean loading) {
            if (this.loading != loading) {
                if (loadingAnimator != null) {
                    loadingAnimator.cancel();
                    loadingAnimator = null;
                }

                loadingAnimator = ValueAnimator.ofFloat(loadingT, (this.loading = loading) ? 1 : 0);
                loadingAnimator.addUpdateListener(anm -> {
                    loadingT = (float) anm.getAnimatedValue();
                    invalidate();
                });
                loadingAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingT = loading ? 1 : 0;
                        invalidate();
                    }
                });
                loadingAnimator.setDuration(320);
                loadingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                loadingAnimator.start();
            }
        }

        public boolean isLoading() {
            return loading;
        }

        private float countScale = 1;
        private ValueAnimator countAnimator;
        private void animateCount() {
            if (countAnimator != null) {
                countAnimator.cancel();
                countAnimator = null;
            }

            countAnimator = ValueAnimator.ofFloat(0, 1);
            countAnimator.addUpdateListener(anm -> {
                countScale = Math.max(1, (float) anm.getAnimatedValue());
                invalidate();
            });
            countAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    countScale = 1;
                    invalidate();
                }
            });
            countAnimator.setInterpolator(new OvershootInterpolator(2.0f));
            countAnimator.setDuration(200);
            countAnimator.start();
        }

        private int lastCount;

        public void setCount(int count, boolean animated) {
            if (animated) {
                countText.cancelAnimation();
            }
            if (animated && count != lastCount && count > 0 && lastCount > 0) {
                animateCount();
            }
            lastCount = count;
            countAlpha = count != 0 ? 1f : 0f;
            countText.setText("" + count, animated);
            invalidate();
        }

        private float enabledT = 1;
        private boolean enabled = true;
        private ValueAnimator enabledAnimator;

        @Override
        public void setEnabled(boolean enabled) {
            if (this.enabled != enabled) {
                if (enabledAnimator != null) {
                    enabledAnimator.cancel();
                    enabledAnimator = null;
                }

                enabledAnimator = ValueAnimator.ofFloat(enabledT, (this.enabled = enabled) ? 1 : 0);
                enabledAnimator.addUpdateListener(anm -> {
                    enabledT = (float) anm.getAnimatedValue();
                    invalidate();
//                    background.getPaint().setColor(ColorUtils.blendARGB(
//                        Theme.getColor(Theme.key_checkboxDisabled),
//                        Theme.getColor(Theme.key_featuredStickers_addButton),
//                        enabledT
//                    ));
                });
                enabledAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        if (enabled) {
//                            rippleView.setBackground(Theme.AdaptiveRipple.rect(Theme.getColor(Theme.key_featuredStickers_addButton), 8));
//                        } else {
//                            rippleView.setBackground(Theme.createRadSelectorDrawable(0x0fffffff, 8, 8));
//                        }
                    }
                });
                enabledAnimator.start();
            }
        }

        @Override
        protected boolean verifyDrawable(@NonNull Drawable who) {
            return text == who || countText == who || super.verifyDrawable(who);
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            return false;
        }

        private CircularProgressDrawable loadingDrawable;

        @Override
        protected void onDraw(Canvas canvas) {
            rippleView.draw(canvas);

            if (loadingT > 0) {
                if (loadingDrawable == null) {
                    loadingDrawable = new CircularProgressDrawable(text.getTextColor());
                }
                int y = (int) ((1f - loadingT) * dp(24));
                loadingDrawable.setBounds(0, y, getWidth(), y + getHeight());
                loadingDrawable.setAlpha((int) (0xFF * loadingT));
                loadingDrawable.draw(canvas);
                invalidate();
            }

            if (loadingT < 1) {
                boolean restore = false;
                if (loadingT != 0) {
                    canvas.save();
                    canvas.translate(0, (int) (loadingT * dp(-24)));
                    canvas.scale(1, 1f - .4f * loadingT);
                    restore = true;
                }
                float textWidth = text.getCurrentWidth();
                float countAlpha = countAlphaAnimated.set(this.countAlpha);

                float width = textWidth + (dp(5.66f + 5 + 5) + countText.getCurrentWidth()) * countAlpha;
                AndroidUtilities.rectTmp2.set(
                        (int) ((getMeasuredWidth() - width - getWidth()) / 2f),
                        (int) ((getMeasuredHeight() - text.getHeight()) / 2f - dp(1)),
                        (int) ((getMeasuredWidth() - width + getWidth()) / 2f + textWidth),
                        (int) ((getMeasuredHeight() + text.getHeight()) / 2f - dp(1))
                );
                text.setAlpha((int) (0xFF * (1f - loadingT) * AndroidUtilities.lerp(.5f, 1f, enabledT)));
                text.setBounds(AndroidUtilities.rectTmp2);
                text.draw(canvas);

                AndroidUtilities.rectTmp2.set(
                        (int) ((getMeasuredWidth() - width) / 2f + textWidth + dp(5f)),
                        (int) ((getMeasuredHeight() - dp(18)) / 2f),
                        (int) ((getMeasuredWidth() - width) / 2f + textWidth + dp(5f + 4 + 4) + Math.max(dp(9), countText.getCurrentWidth())),
                        (int) ((getMeasuredHeight() + dp(18)) / 2f)
                );
                AndroidUtilities.rectTmp.set(AndroidUtilities.rectTmp2);

                if (countScale != 1) {
                    canvas.save();
                    canvas.scale(countScale, countScale, AndroidUtilities.rectTmp2.centerX(), AndroidUtilities.rectTmp2.centerY());
                }
                paint.setAlpha((int) (0xFF * (1f - loadingT) * countAlpha * countAlpha));
                canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(10), dp(10), paint);

                AndroidUtilities.rectTmp2.offset(-dp(.3f), -dp(.4f));
                countText.setAlpha((int) (0xFF * (1f - loadingT) * countAlpha));
                countText.setBounds(AndroidUtilities.rectTmp2);
                countText.draw(canvas);
                if (countScale != 1) {
                    canvas.restore();
                }
                if (restore) {
                    canvas.restore();
                }
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName("android.widget.Button");
            info.setContentDescription(text.getText() + (lastCount > 0 ? ", " + formatPluralString("Chats", lastCount) : ""));
        }
    }

    @Override
    protected CharSequence getTitle() {
        if (deleting) {
            return getString(R.string.FolderLinkTitleRemove);
        } else if (invite instanceof TL_chatlists.TL_chatlists_chatlistInvite) {
            return getString(R.string.FolderLinkTitleAdd);
        } else if (peers == null || peers.isEmpty()) {
            return getString(R.string.FolderLinkTitleAlready);
        } else {
            return getString(R.string.FolderLinkTitleAddChats);
        }
    }

    @Override
    protected RecyclerListView.SelectionAdapter createAdapter(RecyclerListView listView) {
        return new RecyclerListView.SelectionAdapter() {

            private static final int
                VIEW_TYPE_TITLE = 0,
                VIEW_TYPE_HINT = 1,
                VIEW_TYPE_USER = 2,
                VIEW_TYPE_HEADER = 3;

            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return holder.getItemViewType() == VIEW_TYPE_USER && holder.getAdapterPosition() >= usersStartRow && holder.getAdapterPosition() <= usersEndRow;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = null;
                if (viewType == VIEW_TYPE_TITLE) {
                    view = titleCell = new TitleCell(getContext(), invite instanceof TL_chatlists.TL_chatlists_chatlistInviteAlready || updates != null, escapedTitle, titleEntities, titleNoanimate);
                } else if (viewType == VIEW_TYPE_HINT) {
                    view = new TextInfoPrivacyCell(getContext());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                } else if (viewType == VIEW_TYPE_USER) {
                    GroupCreateUserCell userCell = new GroupCreateUserCell(getContext(), 1, 0, false);
                    userCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    view = userCell;
                } else if (viewType == VIEW_TYPE_HEADER) {
                    view = new HeaderCell(getContext());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                }
                return new RecyclerListView.Holder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                final int viewType = holder.getItemViewType();
                if (viewType == VIEW_TYPE_USER) {
                    GroupCreateUserCell userCell = (GroupCreateUserCell) holder.itemView;
                    TLRPC.Peer peer = null;
                    if (position >= usersStartRow && position <= usersEndRow) {
                        peer = peers != null ? peers.get(position - usersStartRow) : null;
                    } else if (position >= alreadyUsersStartRow && position <= alreadyUsersEndRow) {
                        peer = alreadyPeers != null ? alreadyPeers.get(position - alreadyUsersStartRow) : null;
                    }

                    TLObject object = null;
                    String name = null, status = null;
                    long did = 0;
                    if (peer != null) {
                        if (peer instanceof TLRPC.TL_peerUser) {
                            did = peer.user_id;
                            TLRPC.User user = getBaseFragment().getMessagesController().getUser(peer.user_id);
                            object = user;
                            name = UserObject.getUserName(user);
                            if (user != null && user.bot) {
                                status = getString(R.string.FilterInviteBot);
                            } else {
                                status = getString(R.string.FilterInviteUser);
                            }
                        } else if (peer instanceof TLRPC.TL_peerChat) {
                            did = -peer.chat_id;
                            object = getBaseFragment().getMessagesController().getChat(peer.chat_id);
                        } else if (peer instanceof TLRPC.TL_peerChannel) {
                            did = -peer.channel_id;
                            object = getBaseFragment().getMessagesController().getChat(peer.channel_id);
                        }
                    }
                    if (object instanceof TLRPC.Chat) {
                        TLRPC.Chat chat = (TLRPC.Chat) object;
                        name = chat.title;
                        if (chat.participants_count != 0) {
                            if (ChatObject.isChannelAndNotMegaGroup(chat)) {
                                status = LocaleController.formatPluralStringComma("Subscribers", chat.participants_count);
                            } else {
                                status = LocaleController.formatPluralStringComma("Members", chat.participants_count);
                            }
                        } else {
                            if (ChatObject.isChannelAndNotMegaGroup(chat)) {
                                status = getString(R.string.ChannelPublic);
                            } else {
                                status = getString(R.string.MegaPublic);
                            }
                        }
                    }
                    userCell.setTag(did);
                    userCell.getCheckBox().getCheckBoxBase().setAlpha(alreadyJoined.contains(did) ? .5f : 1f);
                    userCell.setChecked(selectedPeers.contains(did), false);
                    userCell.setObject(object, name, status);
                } else if (viewType == VIEW_TYPE_HEADER) {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == alreadyHeaderRow) {
                        headerCell.setText(getString(R.string.FolderLinkHeaderAlready), false);
                        headerCell.setAction("", null);
                    } else {
                        FolderBottomSheet.this.headerCell = headerCell;
                        updateHeaderCell(false);
                    }
                } else if (viewType == VIEW_TYPE_HINT) {
                    TextInfoPrivacyCell hintCell = (TextInfoPrivacyCell) holder.itemView;
                    hintCell.setForeground(Theme.getThemedDrawableByKey(getContext(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    if (position == alreadySectionRow || position == sectionRow || peers == null || peers.isEmpty()) {
                        hintCell.setFixedSize(12);
                        hintCell.setText("");
                    } else {
                        hintCell.setFixedSize(0);
                        if (deleting) {
                            hintCell.setText(getString(R.string.FolderLinkHintRemove));
                        } else {
                            hintCell.setText(getString(R.string.FolderLinkHint));
                        }
                    }
                } else if (viewType == VIEW_TYPE_TITLE) {
                    titleCell = (TitleCell) holder.itemView;
                    updateCount(false);
                }
            }

            @Override
            public int getItemViewType(int position) {
                if (position == titleRow) {
                    return VIEW_TYPE_TITLE;
                } else if (position == sectionRow || position == usersSectionRow || position == alreadySectionRow) {
                    return VIEW_TYPE_HINT;
                } else if (position == headerRow || position == alreadyHeaderRow) {
                    return VIEW_TYPE_HEADER;
                }
                return VIEW_TYPE_USER;
            }

            @Override
            public int getItemCount() {
                return rowsCount;
            }
        };
    }

    public static class HeaderCell extends FrameLayout {
        public AnimatedTextView textView;
        public AnimatedTextView actionTextView;
        public HeaderCell(Context context) {
            super(context);

            textView = new AnimatedTextView(context, true, true, false);
            textView.setTextSize(dp(15));
            textView.setTypeface(AndroidUtilities.bold());
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
            textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.BOTTOM, 21, 15, 21, 2));

            actionTextView = new AnimatedTextView(context, true, true, true);
            actionTextView.setAnimationProperties(.45f, 0, 250, CubicBezierInterpolator.EASE_OUT_QUINT);
            actionTextView.setTextSize(dp(15));
            actionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
            actionTextView.setGravity(LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT);
            addView(actionTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 20, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, 21, 15, 21, 2));

            ViewCompat.setAccessibilityHeading(this, true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
        }

        public void setText(CharSequence text, boolean animated) {
            if (animated) {
                textView.cancelAnimation();
            }
            textView.setText(text, animated && !LocaleController.isRTL);
        }

        public void setAction(CharSequence text, Runnable onClick) {
            actionTextView.setText(text, !LocaleController.isRTL);
            actionTextView.setOnClickListener(e -> {
                if (onClick != null) {
                    onClick.run();
                }
            });
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName("android.widget.TextView");
            info.setText(textView.getText());
        }
    }

    private class TitleCell extends FrameLayout {

        private boolean already;
        private CharSequence title;

        private FoldersPreview preview;
        private AnimatedEmojiSpan.TextViewEmojis titleTextView, subtitleTextView;

        public TitleCell(Context context, boolean already, CharSequence title, ArrayList<TLRPC.MessageEntity> titleEntities, boolean titleNoanimate) {
            super(context);
            this.already = already;

            String left2Folder = null,
                   left1Folder = getString(R.string.FolderLinkPreviewLeft),
                   right1Folder = getString(R.string.FolderLinkPreviewRight),
                   right2Folder = null;
            preview = new FoldersPreview(context, left2Folder, left1Folder, title == null ? "" : new SpannableStringBuilder(title), titleEntities, titleNoanimate, right1Folder, right2Folder);
            addView(preview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 17.33f, 0, 0));

            titleTextView = new AnimatedEmojiSpan.TextViewEmojis(context);
            titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            titleTextView.setTypeface(AndroidUtilities.bold());
            titleTextView.setGravity(Gravity.CENTER);
            titleTextView.setLineSpacing(dp(-1), 1.0f);
            this.title = Emoji.replaceEmoji(new SpannableStringBuilder(title), titleTextView.getPaint().getFontMetricsInt(), false, 0.8f);
            this.title = MessageObject.replaceAnimatedEmoji(this.title, titleEntities, titleTextView.getPaint().getFontMetricsInt(), false, 0.8f);
            titleTextView.setText(getTitle());
            titleTextView.setCacheType(titleNoanimate ? AnimatedEmojiDrawable.CACHE_TYPE_NOANIMATE_FOLDER : AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES);
            titleTextView.setEmojiColor(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider));
            addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 32, 78.3f, 32, 0));

            subtitleTextView = new AnimatedEmojiSpan.TextViewEmojis(context);
            subtitleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            subtitleTextView.setLines(2);
            subtitleTextView.setGravity(Gravity.CENTER);
            subtitleTextView.setLineSpacing(0, 1.15f);
            addView(subtitleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 32, 113, 32, 0));

            setSelectedCount(0, false);
        }

        public void setSelectedCount(int count, boolean animated) {
            if (deleting) {
                subtitleTextView.setText(AndroidUtilities.replaceTags(formatSpannable(R.string.FolderLinkSubtitleRemove, title)));
            } else if (already) {
                preview.setCount(peers != null ? peers.size() : 0, false);
                if (peers == null || peers.isEmpty()) {
                    subtitleTextView.setText(AndroidUtilities.replaceTags(formatSpannable(R.string.FolderLinkSubtitleAlready, title)));
                } else {
                    subtitleTextView.setText(AndroidUtilities.replaceTags(formatPluralSpannable("FolderLinkSubtitleChats", peers != null ? peers.size() : 0, title)));
                }
            } else {
                if (peers == null || peers.isEmpty()) {
                    subtitleTextView.setText(AndroidUtilities.replaceTags(formatSpannable(R.string.FolderLinkSubtitleAlready, title)));
                } else {
                    subtitleTextView.setText(AndroidUtilities.replaceTags(formatSpannable(R.string.FolderLinkSubtitle, title)));
                }
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(172), MeasureSpec.EXACTLY)
            );
        }

        private class FoldersPreview extends View {

            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            TextPaint selectedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Path path = new Path();
            float[] radii = new float[8];

            Text leftFolder2;
            Text leftFolder;
            Text middleFolder;
            Text rightFolder;
            Text rightFolder2;

            LinearGradient leftGradient, rightGradient;
            Paint leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG), rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Matrix leftMatrix = new Matrix(), rightMatrix = new Matrix();

            AnimatedTextView.AnimatedTextDrawable countText;

            public FoldersPreview(
                Context context,
                CharSequence left2FolderText,
                CharSequence left1FolderText,
                CharSequence middleFolderText,
                ArrayList<TLRPC.MessageEntity> middleFolderEntities,
                boolean middleFolderNoanimate,
                CharSequence right1FolderText,
                CharSequence right2FolderText
            ) {
                super(context);

                paint.setColor(Theme.multAlpha(Theme.getColor(Theme.key_profile_tabText), .8f));
                paint.setTextSize(dp(15.33f));
                paint.setTypeface(AndroidUtilities.bold());

                selectedTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText2));
                selectedTextPaint.setTextSize(dp(17));
                selectedTextPaint.setTypeface(AndroidUtilities.bold());

                selectedPaint.setColor(Theme.getColor(Theme.key_featuredStickers_unread));

                countText = new AnimatedTextView.AnimatedTextDrawable(false, true, true);
                countText.setAnimationProperties(.3f, 0, 250, CubicBezierInterpolator.EASE_OUT_QUINT);
                countText.setCallback(this);
                countText.setTextSize(dp(11.66f));
                countText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                countText.setTypeface(AndroidUtilities.bold());
                countText.setGravity(Gravity.CENTER_HORIZONTAL);

                final int textColor = Theme.multAlpha(Theme.getColor(Theme.key_profile_tabText), .8f);
                final int selectedTextColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText2);
                if (left2FolderText != null) {
                    leftFolder2 = new Text(normalizeTitle(left2FolderText), 15.33f, AndroidUtilities.bold())
                        .supportAnimatedEmojis(this)
                        .setColor(textColor);
                }
                if (left1FolderText != null) {
                    leftFolder = new Text(normalizeTitle(left1FolderText), 15.33f, AndroidUtilities.bold())
                        .supportAnimatedEmojis(this)
                        .setColor(textColor);
                }
                middleFolderText = normalizeTitle(middleFolderText);
                middleFolder = new Text(middleFolderText, 15.33f, AndroidUtilities.bold())
                    .supportAnimatedEmojis(this)
                    .setColor(selectedTextColor);
                middleFolderText = Emoji.replaceEmoji(middleFolderText, middleFolder.getFontMetricsInt(), false);
                middleFolderText = MessageObject.replaceAnimatedEmoji(middleFolderText, middleFolderEntities, middleFolder.getFontMetricsInt());
                middleFolder.setText(middleFolderText);
                middleFolder.setEmojiCacheType(middleFolderNoanimate ? AnimatedEmojiDrawable.CACHE_TYPE_NOANIMATE_FOLDER : AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES);
                if (right1FolderText != null) {
                    rightFolder = new Text(normalizeTitle(right1FolderText), 15.33f, AndroidUtilities.bold())
                        .supportAnimatedEmojis(this)
                        .setColor(textColor);
                }
                if (right2FolderText != null) {
                    rightFolder2 = new Text(normalizeTitle(right2FolderText), 15.33f, AndroidUtilities.bold())
                        .supportAnimatedEmojis(this)
                        .setColor(textColor);
                }

                radii[0] = radii[1] = radii[2] = radii[3] = dp(3);
                radii[4] = radii[5] = radii[6] = radii[7] = dp(1);

                leftGradient = new LinearGradient(0, 0, dp(80), 0, new int[] {0xffffffff, 0x00ffffff}, new float[] {0, 1}, Shader.TileMode.CLAMP);
                leftPaint.setShader(leftGradient);
                leftPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                rightGradient = new LinearGradient(0, 0, dp(80), 0, new int[] {0x00ffffff, 0xffffffff}, new float[] {0, 1f}, Shader.TileMode.CLAMP);
                rightPaint.setShader(rightGradient);
                rightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            }

            private CharSequence normalizeTitle(CharSequence text) {
                if (text == null || "ALL_CHATS".equals(text.toString())) {
                    text = getString(R.string.FilterAllChats);
                }
                return text;
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                canvas.saveLayerAlpha(0, 0, getMeasuredWidth(), getMeasuredHeight(), 0xFF, Canvas.ALL_SAVE_FLAG);

                float cx = getMeasuredWidth() / 2f;
                float cy = getMeasuredHeight() / 2f;

                float width, cleft;
                if (middleFolder != null) {
                    canvas.save();
                    width = middleFolder.getCurrentWidth() + (isCountEmpty() ? 0 : dp(4.66f + 5.33f + 5.33f) + countText.getCurrentWidth());
                    cleft = cx - width / 2f;
                    canvas.translate(cleft, cy - middleFolder.getHeight() / 2f);
                    middleFolder.draw(canvas);
                    canvas.restore();
                } else {
                    width = 0; cleft = cx;
                }

                if (!isCountEmpty()) {
                    AndroidUtilities.rectTmp2.set(
                        (int) (cleft + middleFolder.getCurrentWidth() + dp(4.66f)),
                        (int) (cy - dp(9)),
                        (int) (cleft + middleFolder.getCurrentWidth() + dp(4.66f + 5.33f + 5.33f) + countText.getCurrentWidth()),
                        (int) (cy + dp(9))
                    );
                    AndroidUtilities.rectTmp.set(AndroidUtilities.rectTmp2);
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(9), dp(9), selectedPaint);

                    AndroidUtilities.rectTmp2.offset(-dp(.33f), -dp(.66f));
                    countText.setBounds(AndroidUtilities.rectTmp2);
                    countText.draw(canvas);
                }

                final float gap = dp(30);

                float x = cleft - gap - leftFolder.getCurrentWidth();
                float minx = x;

                if (leftFolder2 != null && leftFolder.getCurrentWidth() < dp(64)) {
                    minx -= gap + leftFolder2.getCurrentWidth();
                    canvas.save();
                    canvas.translate(minx, cy - leftFolder2.getHeight() / 2f + dp(1));
                    leftFolder2.draw(canvas);
                    canvas.restore();
                }

                if (leftFolder != null) {
                    canvas.save();
                    canvas.translate(x, cy - leftFolder.getHeight() / 2f + dp(1));
                    leftFolder.draw(canvas);
                    canvas.restore();
                }
                x = cleft + width;

                if (rightFolder != null) {
                    canvas.save();
                    canvas.translate(x + gap, cy - rightFolder.getHeight() / 2f + dp(1));
                    rightFolder.draw(canvas);
                    canvas.restore();
                    x += gap + rightFolder.getCurrentWidth();
                }

                if (rightFolder2 != null && rightFolder.getCurrentWidth() < dp(64)) {
                    canvas.save();
                    canvas.translate(x + gap, cy - rightFolder2.getHeight() / 2f + dp(1));
                    rightFolder2.draw(canvas);
                    canvas.restore();
                    x += gap + rightFolder2.getCurrentWidth();
                }

                float y = cy + middleFolder.getHeight() / 2f + dp(12);
                canvas.drawRect(0, y, getMeasuredWidth(), y + 1, paint);

                path.rewind();
                AndroidUtilities.rectTmp.set(cx - width / 2f - dp(4), y - dp(4), cx + width / 2f + dp(4), y);
                path.addRoundRect(AndroidUtilities.rectTmp, radii, Path.Direction.CW);
                canvas.drawPath(path, selectedPaint);

                canvas.save();
                float left = Math.max(dp(8), minx);
                leftMatrix.reset();
                leftMatrix.postTranslate(Math.min(cleft, left + dp(8)), 0);
                leftGradient.setLocalMatrix(leftMatrix);

                float right = Math.min(getMeasuredWidth() - dp(8), x);
                rightMatrix.reset();
                rightMatrix.postTranslate(Math.max(cx + width / 2f, right - dp(80 + 8)), 0);
                rightGradient.setLocalMatrix(rightMatrix);

                canvas.drawRect(0, 0, cx, getMeasuredHeight(), leftPaint);
                canvas.drawRect(cx, 0, getMeasuredWidth(), getMeasuredHeight(), rightPaint);
                canvas.restore();

                canvas.restore();
            }

            @Override
            protected boolean verifyDrawable(@NonNull Drawable who) {
                return who == countText || super.verifyDrawable(who);
            }

            private boolean isCountEmpty() {
                return countText.getText() == null || countText.getText().length() == 0;
            }

            public void setCount(int count, boolean animated) {
                if (animated) {
                    countText.cancelAnimation();
                }
                countText.setText(count > 0 ? "+" + count : "", animated);
                invalidate();
            }
        }
    }

    private void updateHeaderCell(boolean animated) {
        if (headerCell == null) {
            return;
        }

        if (deleting) {
            headerCell.setText(formatPluralString("FolderLinkHeaderChatsQuit", peers.size()), false);
        } else {
            headerCell.setText(formatPluralString("FolderLinkHeaderChatsJoin", peers.size()), false);
        }
        if (peers != null && (peers.size() - alreadyJoined.size()) > 1) {
            final boolean deselect = selectedPeers.size() >= peers.size() - alreadyJoined.size();
            headerCell.setAction(
                    deselect ? getString(R.string.DeselectAll) : getString(R.string.SelectAll), () -> deselectAll(headerCell, deselect)
            );
        } else {
            headerCell.setAction("", null);
        }
    }

    private void announceSelection(boolean buttonSelect) {
        AndroidUtilities.makeAccessibilityAnnouncement(
                formatPluralString("FilterInviteHeaderChats", selectedPeers.size()) + (buttonSelect && headerCell != null ? ", " + headerCell.actionTextView.getText() : "")
        );
    }

    private void deselectAll(HeaderCell headerCell, boolean deselect) {
        selectedPeers.clear();
        selectedPeers.addAll(alreadyJoined);
        if (!deselect) {
            for (int i = 0; i < peers.size(); ++i) {
                long id = DialogObject.getPeerDialogId(peers.get(i));
                if (!selectedPeers.contains(id)) {
                    selectedPeers.add(id);
                }
            }
        }
        updateCount(true);
        headerCell.setAction(deselect ? getString(R.string.SelectAll) : getString(R.string.DeselectAll), () -> deselectAll(headerCell, !deselect));
        announceSelection(true);
        for (int i = 0; i < recyclerListView.getChildCount(); ++i) {
            View child = recyclerListView.getChildAt(i);
            if (child instanceof GroupCreateUserCell) {
                Object tag = child.getTag();
                if (tag instanceof Long) {
                    ((GroupCreateUserCell) child).setChecked(selectedPeers.contains((long) tag),true);
                }
            }
        }
    }
}
