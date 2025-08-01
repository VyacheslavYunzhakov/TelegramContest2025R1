package org.telegramIunzhakov.ui.Cells;

import static org.telegramIunzhakov.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegramIunzhakov.messenger.AndroidUtilities;
import org.telegramIunzhakov.messenger.LocaleController;
import org.telegramIunzhakov.messenger.NotificationCenter;
import org.telegramIunzhakov.messenger.R;
import org.telegramIunzhakov.messenger.UserConfig;
import org.telegramIunzhakov.tgnet.TLRPC;
import org.telegramIunzhakov.ui.ActionBar.Theme;
import org.telegramIunzhakov.ui.Components.AnimatedEmojiSpan;
import org.telegramIunzhakov.ui.Components.AvatarsImageView;
import org.telegramIunzhakov.ui.Components.BackupImageView;
import org.telegramIunzhakov.ui.Components.BlurredFrameLayout;
import org.telegramIunzhakov.ui.Components.LayoutHelper;
import org.telegramIunzhakov.ui.Components.LinkSpanDrawable;
import org.telegramIunzhakov.ui.Components.SizeNotifierFrameLayout;

import java.util.ArrayList;

public class DialogsHintCell extends BlurredFrameLayout {

    private final LinearLayout parentView;
    private final LinearLayout contentView;
    public final AnimatedEmojiSpan.TextViewEmojis titleView;
    public final LinkSpanDrawable.LinksTextView messageView;
    private final ImageView chevronView;
    private final ImageView closeView;
    public final BackupImageView imageView;
    private final AvatarsImageView avatarsImageView;

    public boolean titleIsError;

    public DialogsHintCell(@NonNull Context context, SizeNotifierFrameLayout parentFrameLayout) {
        super(context, parentFrameLayout);

        setWillNotDraw(false);
        setPadding(dp(9), dp(8), dp(9), dp(8));

        avatarsImageView = new AvatarsImageView(context, false);
        avatarsImageView.setStepFactor(46f / 81f);
        avatarsImageView.setVisibility(View.GONE);
        avatarsImageView.setCount(0);

        imageView = new BackupImageView(context);
        imageView.setVisibility(View.GONE);

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setPadding(LocaleController.isRTL ? dp(24) : 0, 0, LocaleController.isRTL ? 0 : dp(24), 0);

        titleView = new AnimatedEmojiSpan.TextViewEmojis(context);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setSingleLine();
        contentView.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP));

        messageView = new LinkSpanDrawable.LinksTextView(context);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        messageView.setEllipsize(TextUtils.TruncateAt.END);
        contentView.addView(messageView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, Gravity.TOP));

        NotificationCenter.getGlobalInstance().listenGlobal(this, NotificationCenter.emojiLoaded, args -> {
            if (titleView != null) {
                titleView.invalidate();
            }
            if (messageView != null) {
                messageView.invalidate();
            }
        });

        parentView = new LinearLayout(context);
        parentView.setOrientation(LinearLayout.HORIZONTAL);
        if (LocaleController.isRTL) {
            parentView.addView(contentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 7, 0, 7, 0));
            parentView.addView(avatarsImageView, LayoutHelper.createFrame(0, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 2, 0, 8, 0));
            parentView.addView(imageView, LayoutHelper.createFrame(36, 36, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 2, 1, 0, 0));
        } else {
            parentView.addView(imageView, LayoutHelper.createFrame(36, 36, Gravity.CENTER_VERTICAL | Gravity.LEFT, 0, 1, 2, 0));
            parentView.addView(avatarsImageView, LayoutHelper.createFrame(0, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 0, 0, 2, 0));
            parentView.addView(contentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 7, 0, 7, 0));
        }
        addView(parentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        chevronView = new ImageView(context);
        chevronView.setImageResource(R.drawable.arrow_newchat);
        addView(chevronView, LayoutHelper.createFrame(16, 16, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL));

        closeView = new ImageView(context);
        closeView.setImageResource(R.drawable.msg_close);
        closeView.setPadding(dp(6), dp(6), dp(6), dp(6));
        addView(closeView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, LocaleController.isRTL ? -15 + 7 : 0, 0, LocaleController.isRTL ? 0 : -15 + 7, 0));
        closeView.setVisibility(GONE);
        setClipToPadding(false);
        updateColors();
    }

    public void setCompact(boolean compact) {
        setPadding(dp(9), dp(compact ? 4 : 8), dp(9), dp(8));
    }

    public void updateColors() {
        titleView.setTextColor(Theme.getColor(titleIsError ? Theme.key_text_RedBold : Theme.key_windowBackgroundWhiteBlackText));
        messageView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        messageView.setLinkTextColor(Theme.getColor(Theme.key_chat_messageLinkIn));
        chevronView.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), PorterDuff.Mode.SRC_IN);
        closeView.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), PorterDuff.Mode.SRC_IN);
        closeView.setBackground(Theme.AdaptiveRipple.filledCircle());
        setBackground(Theme.AdaptiveRipple.filledRect());
    }

    public void setAvatars(int currentAccount, ArrayList<TLRPC.User> users) {
        final int count = Math.min(3, users == null ? 0 : users.size());
        final boolean updated = count != avatarsImageView.avatarsDrawable.count;
        if (count <= 1) {
            avatarsImageView.setAvatarsTextSize(dp(20));
            avatarsImageView.setSize(dp(32));
        } else {
            avatarsImageView.setAvatarsTextSize(dp(18));
            avatarsImageView.setSize(dp(27));
        }
        avatarsImageView.setCount(count);
        avatarsImageView.setVisibility(count <= 0 ? View.GONE : View.VISIBLE);
        avatarsImageView.getLayoutParams().width = count <= 1 ? dp(32) : dp(27 + 16 * (count - 1));
        if (updated) parentView.requestLayout();
        if (users != null) {
            for (int i = 0; i < 3; ++i) {
                avatarsImageView.setObject(i, currentAccount, i >= users.size() ? null : users.get(i));
            }
        }
        avatarsImageView.commitTransition(false);
    }

    public void clear() {
        setCompact(false);
        setAvatars(UserConfig.selectedAccount, null);
        imageView.setVisibility(View.GONE);
        imageView.clearImage();
    }

    public void showImage() {
        imageView.setVisibility(View.VISIBLE);
    }

    public void setText(CharSequence title, CharSequence subtitle) {
        setText(title, subtitle, true, false);
    }
    public void setText(CharSequence title, CharSequence subtitle, boolean showChevron, boolean error) {
        titleIsError = error;
        titleView.setVisibility(TextUtils.isEmpty(title) ? GONE : VISIBLE);
        titleView.setText(title);
        titleView.setCompoundDrawables(null, null, null, null);
        messageView.setText(subtitle);
        chevronView.setVisibility(showChevron ? VISIBLE : GONE);
        closeView.setVisibility(GONE);
        final int padding = showChevron ? dp(24) : 0;
        contentView.setPadding(LocaleController.isRTL ? padding : 0, 0, LocaleController.isRTL ? 0 : padding, 0);
        updateColors();
    }

    public void setOnCloseListener(OnClickListener closeListener) {
        chevronView.setVisibility(INVISIBLE);
        closeView.setVisibility(VISIBLE);
        closeView.setOnClickListener(closeListener);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(v -> {
            if (getAlpha() > .5f && l != null)
                l.onClick(v);
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getAlpha() < .5f) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawRect(0, getHeight() - 1, getWidth(), getHeight(), Theme.dividerPaint);
    }

    private int height;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width <= 0) {
            width = AndroidUtilities.displaySize.x;
        }
        contentView.measure(
                MeasureSpec.makeMeasureSpec(width - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.y, MeasureSpec.AT_MOST)
        );
        this.height = contentView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom() + 1;
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public int height() {
        if (getVisibility() != View.VISIBLE) {
            return 0;
        }
        if (height <= 0) {
            height = dp(72) + 1;
        }
        return height;
    }
}
