package org.telegramIunzhakov.ui.bots;

import static org.telegramIunzhakov.messenger.LocaleController.getString;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.telegramIunzhakov.messenger.LocaleController;
import org.telegramIunzhakov.messenger.R;
import org.telegramIunzhakov.messenger.UserObject;
import org.telegramIunzhakov.ui.ActionBar.ActionBar;
import org.telegramIunzhakov.ui.ActionBar.BackDrawable;
import org.telegramIunzhakov.ui.ActionBar.BaseFragment;
import org.telegramIunzhakov.ui.ActionBar.Theme;
import org.telegramIunzhakov.ui.AvatarSpan;
import org.telegramIunzhakov.ui.Components.LayoutHelper;
import org.telegramIunzhakov.ui.Components.UItem;
import org.telegramIunzhakov.ui.Components.UniversalAdapter;
import org.telegramIunzhakov.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class BotBiometrySettings extends BaseFragment {

    private UniversalRecyclerView listView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.PrivacyBiometryBots));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout contentView = new FrameLayout(context);
        contentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray, resourceProvider));

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, this::onLongClick);
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        BotBiometry.getBots(getContext(), currentAccount, bots -> {
            biometryBots.clear();
            biometryBots.addAll(bots);
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        });

        return fragmentView = contentView;
    }

    private final ArrayList<BotBiometry.Bot> biometryBots = new ArrayList<>();
    private final HashMap<BotBiometry.Bot, SpannableStringBuilder> botName = new HashMap<>();

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        for (int i = 0; i < biometryBots.size(); ++i) {
            BotBiometry.Bot bot = biometryBots.get(i);
            SpannableStringBuilder name = botName.get(bot);
            if (name == null) {
                name = new SpannableStringBuilder();
                name.append("a   ");
                AvatarSpan avatarSpan = new AvatarSpan(null, currentAccount, 24);
                avatarSpan.setUser(bot.user);
                name.setSpan(avatarSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                name.append(UserObject.getUserName(bot.user));
                botName.put(bot, name);
            }
            items.add(UItem.asCheck(i, name).setChecked(!bot.disabled));
        }
        items.add(UItem.asShadow(LocaleController.getString(R.string.PrivacyBiometryBotsInfo)));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.viewType == UniversalAdapter.VIEW_TYPE_CHECK) {
            if (item.id < 0 || item.id >= biometryBots.size()) return;
            BotBiometry.Bot bot = biometryBots.get(item.id);
            bot.disabled = !bot.disabled;
            BotBiometry.toggleBotDisabled(getContext(), currentAccount, bot.user.id, bot.disabled);
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        }
    }

    private boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

}
