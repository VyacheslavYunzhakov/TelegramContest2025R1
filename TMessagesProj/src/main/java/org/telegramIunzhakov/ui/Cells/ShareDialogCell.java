/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegramIunzhakov.ui.Cells;

import static org.telegramIunzhakov.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.Layout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import org.telegramIunzhakov.messenger.AndroidUtilities;
import org.telegramIunzhakov.messenger.ChatObject;
import org.telegramIunzhakov.messenger.ContactsController;
import org.telegramIunzhakov.messenger.DialogObject;
import org.telegramIunzhakov.messenger.Emoji;
import org.telegramIunzhakov.messenger.LocaleController;
import org.telegramIunzhakov.messenger.MessagesController;
import org.telegramIunzhakov.messenger.NotificationCenter;
import org.telegramIunzhakov.messenger.R;
import org.telegramIunzhakov.messenger.UserConfig;
import org.telegramIunzhakov.messenger.UserObject;
import org.telegramIunzhakov.tgnet.ConnectionsManager;
import org.telegramIunzhakov.tgnet.TLRPC;
import org.telegramIunzhakov.tgnet.tl.TL_account;
import org.telegramIunzhakov.ui.ActionBar.SimpleTextView;
import org.telegramIunzhakov.ui.ActionBar.Theme;
import org.telegramIunzhakov.ui.Components.AnimatedFloat;
import org.telegramIunzhakov.ui.Components.AvatarDrawable;
import org.telegramIunzhakov.ui.Components.BackupImageView;
import org.telegramIunzhakov.ui.Components.CheckBox2;
import org.telegramIunzhakov.ui.Components.CubicBezierInterpolator;
import org.telegramIunzhakov.ui.Components.Forum.ForumUtilities;
import org.telegramIunzhakov.ui.Components.LayoutHelper;
import org.telegramIunzhakov.ui.Components.Premium.PremiumGradient;
import org.telegramIunzhakov.ui.Components.RLottieDrawable;
import org.telegramIunzhakov.ui.Components.Text;
import org.telegramIunzhakov.ui.Stars.StarsIntroActivity;

public class ShareDialogCell extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {

    private final BackupImageView imageView;
    private final TextView nameTextView;
    private final SimpleTextView topicTextView;
    private final CheckBox2 checkBox;
    private final AvatarDrawable avatarDrawable;
    private RepostStoryDrawable repostStoryDrawable;
    private TLRPC.User user;
    private final int currentType;

    private float onlineProgress;
    private long lastUpdateTime;
    private long currentDialog;

    private boolean topicWasVisible;

    private final int currentAccount = UserConfig.selectedAccount;
    public final Theme.ResourcesProvider resourcesProvider;

    public static final int TYPE_SHARE = 0;
    public static final int TYPE_CREATE = 2;

    private final AnimatedFloat premiumBlockedT = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private boolean premiumBlocked;
    private final AnimatedFloat starsBlockedT = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private long starsPriceBlocked;

    public boolean isBlocked() {
        return premiumBlocked;
    }

    public long getStarsPrice() {
        return starsPriceBlocked;
    }

    public BackupImageView getImageView() {
        return imageView;
    }

    public ShareDialogCell(Context context, int type, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        avatarDrawable = new AvatarDrawable(resourcesProvider) {
            @Override
            public void invalidateSelf() {
                super.invalidateSelf();
                imageView.invalidate();
            }
        };

        setWillNotDraw(false);
        currentType = type;

        imageView = new BackupImageView(context);
        imageView.setRoundRadius(dp(28));
        if (type == TYPE_CREATE) {
            addView(imageView, LayoutHelper.createFrame(48, 48, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 7, 0, 0));
        } else {
            addView(imageView, LayoutHelper.createFrame(56, 56, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 7, 0, 0));
        }

        nameTextView = new TextView(context) {
            @Override
            public void setText(CharSequence text, BufferType type) {
                text = Emoji.replaceEmoji(text, getPaint().getFontMetricsInt(), false);
                super.setText(text, type);
            }
        };
        NotificationCenter.listenEmojiLoading(nameTextView);
        nameTextView.setTextColor(getThemedColor(premiumBlocked ? Theme.key_windowBackgroundWhiteGrayText5 : Theme.key_dialogTextBlack));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        nameTextView.setMaxLines(2);
        nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        nameTextView.setLines(2);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, currentType == TYPE_CREATE ? 58 : 66, 6, 0));

        topicTextView = new SimpleTextView(context);
        topicTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        topicTextView.setTextSize(12);
        topicTextView.setMaxLines(2);
        topicTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        topicTextView.setAlignment(Layout.Alignment.ALIGN_CENTER);
        addView(topicTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, currentType == TYPE_CREATE ? 58 : 66, 6, 0));

        checkBox = new CheckBox2(context, 21, resourcesProvider);
        checkBox.setColor(Theme.key_dialogRoundCheckBox, Theme.key_dialogBackground, Theme.key_dialogRoundCheckBoxCheck);
        checkBox.setDrawUnchecked(false);
        checkBox.setDrawBackgroundAsArc(4);
        checkBox.setProgressDelegate(progress -> {
            float scale = 1.0f - (1.0f - 0.857f) * checkBox.getProgress();
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
            invalidate();
        });
        addView(checkBox, LayoutHelper.createFrame(24, 24, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 19, currentType == TYPE_CREATE ? -40 : 42, 0, 0));

        setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), dp(2), dp(2)));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.userIsPremiumBlockedUpadted) {
            final TL_account.RequirementToContact r = user != null ? MessagesController.getInstance(currentAccount).isUserContactBlocked(user.id) : null;
            final long starsPrice = currentDialog < 0 ? MessagesController.getInstance(currentAccount).getSendPaidMessagesStars(currentDialog) : DialogObject.getMessagesStarsPrice(r);
            if (premiumBlocked != DialogObject.isPremiumBlocked(r) || starsPriceBlocked != starsPrice) {
                premiumBlocked = DialogObject.isPremiumBlocked(r);
                starsPriceBlocked = starsPrice;
                nameTextView.setTextColor(getThemedColor(premiumBlocked ? Theme.key_windowBackgroundWhiteGrayText5 : Theme.key_dialogTextBlack));
                invalidate();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(dp(currentType == TYPE_CREATE ? 95 : 103), MeasureSpec.EXACTLY));
    }

    protected String repostToCustomName() {
        return LocaleController.getString(R.string.FwdMyStory);
    }

    public void setDialog(long uid, boolean checked, CharSequence name) {
        avatarDrawable.setScaleSize(1f);
        if (uid == Long.MAX_VALUE) {
            nameTextView.setText(repostToCustomName());
            if (repostStoryDrawable == null) {
                repostStoryDrawable = new RepostStoryDrawable(getContext(), imageView, true, resourcesProvider);
            }
            imageView.setImage(null, null, repostStoryDrawable, null);
        } else if (DialogObject.isUserDialog(uid)) {
            user = MessagesController.getInstance(currentAccount).getUser(uid);
            final TL_account.RequirementToContact r = MessagesController.getInstance(currentAccount).isUserContactBlocked(uid);
            premiumBlocked = DialogObject.isPremiumBlocked(r);
            starsPriceBlocked = DialogObject.getMessagesStarsPrice(r);
            nameTextView.setTextColor(getThemedColor(premiumBlocked ? Theme.key_windowBackgroundWhiteGrayText5 : Theme.key_dialogTextBlack));
            premiumBlockedT.force(premiumBlocked);
            starsBlockedT.force(starsPriceBlocked > 0);
            invalidate();
            avatarDrawable.setInfo(currentAccount, user);
            if (currentType != TYPE_CREATE && UserObject.isReplyUser(user)) {
                nameTextView.setText(LocaleController.getString(R.string.RepliesTitle));
                avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_REPLIES);
                imageView.setImage(null, null, avatarDrawable, user);
            } else if (currentType != TYPE_CREATE && UserObject.isUserSelf(user)) {
                nameTextView.setText(LocaleController.getString(R.string.SavedMessages));
                avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_SAVED);
                imageView.setImage(null, null, avatarDrawable, user);
            } else {
                if (name != null) {
                    nameTextView.setText(name);
                } else if (user != null) {
                    nameTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
                } else {
                    nameTextView.setText("");
                }
                imageView.setForUserOrChat(user, avatarDrawable);
            }
            imageView.setRoundRadius(dp(28));
        } else {
            user = null;
            premiumBlocked = false;
            premiumBlockedT.force(0);
            starsPriceBlocked = MessagesController.getInstance(currentAccount).getSendPaidMessagesStars(uid);
            starsBlockedT.force(false);
            TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-uid);
            if (name != null) {
                nameTextView.setText(name);
            } else if (chat != null) {
                if (chat.monoforum) {
                    nameTextView.setText(ForumUtilities.getMonoForumTitle(currentAccount, chat));
                } else {
                    nameTextView.setText(chat.title);
                }
            } else {
                nameTextView.setText("");
            }
            if (ChatObject.isMonoForum(chat)) {
                ForumUtilities.setMonoForumAvatar(currentAccount, chat, avatarDrawable, imageView);
            } else {
                avatarDrawable.setInfo(currentAccount, chat);
                imageView.setForUserOrChat(chat, avatarDrawable);
            }
            imageView.setRoundRadius(chat != null && (chat.forum || chat.monoforum)? dp(16) : dp(28));
        }
        currentDialog = uid;
        checkBox.setChecked(checked, false);
    }

    public long getCurrentDialog() {
        return currentDialog;
    }

    public void setChecked(boolean checked, boolean animated) {
        checkBox.setChecked(checked, animated);
        if (!checked) {
            setTopic(null, true);
        }
    }

    public void setTopic(TLRPC.TL_forumTopic topic, boolean animate) {
        setTopic(topic, false, animate);
    }

    public void setTopic(TLRPC.TL_forumTopic topic, boolean mono, boolean animate) {
        boolean wasVisible = topicWasVisible;
        boolean visible = topic != null;
        if (wasVisible != visible || !animate) {
            SpringAnimation prevSpring = (SpringAnimation) topicTextView.getTag(R.id.spring_tag);
            if (prevSpring != null) {
                prevSpring.cancel();
            }

            if (visible) {
                if (mono) {
                    topicTextView.setText(MessagesController.getInstance(currentAccount).getPeerName(DialogObject.getPeerDialogId(topic.from_id)));
                } else {
                    topicTextView.setText(ForumUtilities.getTopicSpannedName(topic, topicTextView.getTextPaint(), false));
                }
                topicTextView.requestLayout();
            }
            if (animate) {
                SpringAnimation springAnimation = new SpringAnimation(new FloatValueHolder(visible ? 0f : 1000f))
                        .setSpring(new SpringForce(visible ? 1000f : 0f)
                                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                                .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY))
                        .addUpdateListener((animation, value, velocity) -> {
                            value /= 1000f;

                            topicTextView.setAlpha(value);
                            nameTextView.setAlpha(1f - value);

                            topicTextView.setTranslationX((1f - value) * -dp(10));
                            nameTextView.setTranslationX(value * dp(10));
                        })
                        .addEndListener((animation, canceled, value, velocity) -> {
                            topicTextView.setTag(R.id.spring_tag, null);
                        });
                topicTextView.setTag(R.id.spring_tag, springAnimation);
                springAnimation.start();
            } else {
                if (visible) {
                    topicTextView.setAlpha(1f);
                    nameTextView.setAlpha(0f);
                    topicTextView.setTranslationX(0);
                    nameTextView.setTranslationX(dp(10));
                } else {
                    topicTextView.setAlpha(0f);
                    nameTextView.setAlpha(1f);
                    topicTextView.setTranslationX(-dp(10));
                    nameTextView.setTranslationX(0);
                }
            }

            topicWasVisible = visible;
        }
    }

    private PremiumGradient.PremiumGradientTools premiumGradient;
    private Drawable lockDrawable;

    private final Paint priceBackgroundPaint = new Paint();
    private long priceTextValue;
    private Text priceText;

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (child == imageView && currentType != TYPE_CREATE) {
            if (user != null && !MessagesController.isSupportUser(user)) {
                long newTime = SystemClock.elapsedRealtime();
                long dt = newTime - lastUpdateTime;
                if (dt > 17) {
                    dt = 17;
                }
                lastUpdateTime = newTime;

                final float priceT = starsBlockedT.set(starsPriceBlocked > 0);
                if (priceT > 0) {
                    float cx = imageView.getLeft() + imageView.getMeasuredWidth() / 2.0f + dp(18);
                    float cy = imageView.getTop() + imageView.getMeasuredHeight() / 2.0f - dp(20.83f);

                    if (priceText == null || priceTextValue != starsPriceBlocked && starsPriceBlocked > 0) {
                        priceText = new Text(StarsIntroActivity.replaceStars("⭐️" + AndroidUtilities.formatWholeNumber((int) (priceTextValue = starsPriceBlocked), 0), .65f), 9.33f, AndroidUtilities.bold());
                    }
                    final float w = (priceText == null ? 0 : priceText.getCurrentWidth()) + dp(10);
                    final float h = dp(14.33f);

                    AndroidUtilities.rectTmp.set(cx - w / 2.0f, cy - h / 2.0f, cx + w / 2.0f, cy + h / 2.0f);
                    AndroidUtilities.rectTmp.inset(-dp(1.33f), dp(-1.33f));
                    priceBackgroundPaint.setColor(getThemedColor(Theme.key_dialogBackground));
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.rectTmp.height() / 2.0f, AndroidUtilities.rectTmp.height() / 2.0f, priceBackgroundPaint);
                    AndroidUtilities.rectTmp.inset(dp(1.33f), dp(1.33f));
                    priceBackgroundPaint.setColor(getThemedColor(Theme.key_dialogRoundCheckBox));
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.rectTmp.height() / 2.0f, AndroidUtilities.rectTmp.height() / 2.0f, priceBackgroundPaint);

                    if (priceText != null) {
                        priceText.draw(canvas, cx - w / 2.0f + dp(5), cy, Color.WHITE, 1.0f);
                    }
                }
                final float lockT = premiumBlockedT.set(premiumBlocked);
                if (lockT > 0) {
                    int top = imageView.getBottom() - dp(9);
                    int left = imageView.getRight() - dp(9.33f);

                    canvas.save();
                    Theme.dialogs_onlineCirclePaint.setColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    canvas.drawCircle(left, top, dp(12) * lockT, Theme.dialogs_onlineCirclePaint);
                    if (premiumGradient == null) {
                        premiumGradient = new PremiumGradient.PremiumGradientTools(Theme.key_premiumGradient1, Theme.key_premiumGradient2, -1, -1, -1, resourcesProvider);
                    }
                    premiumGradient.gradientMatrix(left - dp(10), top - dp(10), left + dp(10), top + dp(10), 0, 0);
                    canvas.drawCircle(left, top, dp(10) * lockT, premiumGradient.paint);
                    if (lockDrawable == null) {
                        lockDrawable = getContext().getResources().getDrawable(R.drawable.msg_mini_lock2).mutate();
                        lockDrawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
                    }
                    lockDrawable.setBounds(
                        (int) (left - lockDrawable.getIntrinsicWidth() / 2f * .875f * lockT),
                        (int) (top  - lockDrawable.getIntrinsicHeight() / 2f * .875f * lockT),
                        (int) (left + lockDrawable.getIntrinsicWidth() / 2f * .875f * lockT),
                        (int) (top  + lockDrawable.getIntrinsicHeight() / 2f * .875f * lockT)
                    );
                    lockDrawable.setAlpha((int) (0xFF * lockT));
                    lockDrawable.draw(canvas);
                    canvas.restore();
                }

                boolean isOnline = !premiumBlocked && !user.self && !user.bot && (user.status != null && user.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime() || MessagesController.getInstance(currentAccount).onlinePrivacy.containsKey(user.id));
                if (isOnline || onlineProgress != 0) {
                    int top = imageView.getBottom() - dp(6);
                    int left = imageView.getRight() - dp(10);
                    Theme.dialogs_onlineCirclePaint.setColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    canvas.drawCircle(left, top, dp(7) * onlineProgress * (1.0f - lockT) * (1.0f - priceT), Theme.dialogs_onlineCirclePaint);
                    Theme.dialogs_onlineCirclePaint.setColor(getThemedColor(Theme.key_chats_onlineCircle));
                    canvas.drawCircle(left, top, dp(5) * onlineProgress * (1.0f - lockT) * (1.0f - priceT), Theme.dialogs_onlineCirclePaint);
                    if (isOnline) {
                        if (onlineProgress < 1.0f) {
                            onlineProgress += dt / 150.0f;
                            if (onlineProgress > 1.0f) {
                                onlineProgress = 1.0f;
                            }
                            imageView.invalidate();
                            invalidate();
                        }
                    } else {
                        if (onlineProgress > 0.0f) {
                            onlineProgress -= dt / 150.0f;
                            if (onlineProgress < 0.0f) {
                                onlineProgress = 0.0f;
                            }
                            imageView.invalidate();
                            invalidate();
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cx = imageView.getLeft() + imageView.getMeasuredWidth() / 2;
        int cy = imageView.getTop() + imageView.getMeasuredHeight() / 2;
        Theme.checkboxSquare_checkPaint.setColor(getThemedColor(Theme.key_dialogRoundCheckBox));
        Theme.checkboxSquare_checkPaint.setAlpha((int) (checkBox.getProgress() * 255));
        int radius = dp(currentType == TYPE_CREATE ? 24 : 28);
        AndroidUtilities.rectTmp.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawRoundRect(AndroidUtilities.rectTmp, imageView.getRoundRadius()[0], imageView.getRoundRadius()[0], Theme.checkboxSquare_checkPaint);
        super.onDraw(canvas);
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (checkBox.isChecked()) {
            info.setSelected(true);
        }
    }

    public static class RepostStoryDrawable extends Drawable {

        private final LinearGradient gradient;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final RLottieDrawable lottieDrawable;
        private final Drawable drawable;

        public RepostStoryDrawable(Context context, View parentView, boolean animate, Theme.ResourcesProvider resourcesProvider) {
            gradient = new LinearGradient(0, 0, dp(56), dp(56), new int[] {
                Theme.getColor(Theme.key_stories_circle1, resourcesProvider),
                Theme.getColor(Theme.key_stories_circle2, resourcesProvider)
            }, new float[] { 0, 1 }, Shader.TileMode.CLAMP);
            paint.setShader(gradient);

            if (animate) {
                lottieDrawable = new RLottieDrawable(R.raw.story_repost, "story_repost", dp(42), dp(42), true, null);
                lottieDrawable.setMasterParent(parentView);
                AndroidUtilities.runOnUIThread(lottieDrawable::start, 450);
                drawable = null;
            } else {
                lottieDrawable = null;
                drawable = context.getResources().getDrawable(R.drawable.large_repost_story).mutate();
                drawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            }
        }

        int alpha = 0xFF;
        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.save();
            canvas.translate(getBounds().left, getBounds().top);
            AndroidUtilities.rectTmp.set(0, 0, getBounds().width(), getBounds().height());
            paint.setAlpha(alpha);
            float r2 = Math.min(getBounds().width(), getBounds().height()) / 2f * ((float) alpha / 0xFF);
            canvas.drawRoundRect(AndroidUtilities.rectTmp, r2, r2, paint);
            canvas.restore();

            final int r = dp(lottieDrawable != null ? 20 : 15);
            AndroidUtilities.rectTmp2.set(
                getBounds().centerX() - r,
                getBounds().centerY() - r,
                getBounds().centerX() + r,
                getBounds().centerY() + r
            );
            Drawable drawable = lottieDrawable == null ? this.drawable : lottieDrawable;
            if (drawable != null) {
                drawable.setBounds(AndroidUtilities.rectTmp2);
                drawable.setAlpha(alpha);
                drawable.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha;
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getIntrinsicWidth() {
            return dp(56);
        }

        @Override
        public int getIntrinsicHeight() {
            return dp(56);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }
}
