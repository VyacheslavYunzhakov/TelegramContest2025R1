/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegramIunzhakov.ui.Cells;

import static org.telegramIunzhakov.messenger.AndroidUtilities.dp;
import static org.telegramIunzhakov.messenger.LocaleController.formatPluralStringComma;
import static org.telegramIunzhakov.messenger.LocaleController.formatString;
import static org.telegramIunzhakov.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegramIunzhakov.messenger.AndroidUtilities;
import org.telegramIunzhakov.messenger.ApplicationLoader;
import org.telegramIunzhakov.messenger.BuildVars;
import org.telegramIunzhakov.messenger.ChatObject;
import org.telegramIunzhakov.messenger.ChatThemeController;
import org.telegramIunzhakov.messenger.DialogObject;
import org.telegramIunzhakov.messenger.DocumentObject;
import org.telegramIunzhakov.messenger.DownloadController;
import org.telegramIunzhakov.messenger.Emoji;
import org.telegramIunzhakov.messenger.FileLoader;
import org.telegramIunzhakov.messenger.FileLog;
import org.telegramIunzhakov.messenger.ImageLoader;
import org.telegramIunzhakov.messenger.ImageLocation;
import org.telegramIunzhakov.messenger.ImageReceiver;
import org.telegramIunzhakov.messenger.LocaleController;
import org.telegramIunzhakov.messenger.MediaDataController;
import org.telegramIunzhakov.messenger.MessageObject;
import org.telegramIunzhakov.messenger.MessageSuggestionParams;
import org.telegramIunzhakov.messenger.MessagesController;
import org.telegramIunzhakov.messenger.NotificationCenter;
import org.telegramIunzhakov.messenger.R;
import org.telegramIunzhakov.messenger.SharedConfig;
import org.telegramIunzhakov.messenger.SvgHelper;
import org.telegramIunzhakov.messenger.UserConfig;
import org.telegramIunzhakov.messenger.UserObject;
import org.telegramIunzhakov.messenger.Utilities;
import org.telegramIunzhakov.messenger.browser.Browser;
import org.telegramIunzhakov.messenger.utils.tlutils.TlUtils;
import org.telegramIunzhakov.tgnet.ConnectionsManager;
import org.telegramIunzhakov.tgnet.TLObject;
import org.telegramIunzhakov.tgnet.TLRPC;
import org.telegramIunzhakov.tgnet.tl.TL_stories;
import org.telegramIunzhakov.ui.ActionBar.ActionBar;
import org.telegramIunzhakov.ui.ActionBar.BaseFragment;
import org.telegramIunzhakov.ui.ActionBar.Theme;
import org.telegramIunzhakov.ui.AvatarSpan;
import org.telegramIunzhakov.ui.ChannelAdminLogActivity;
import org.telegramIunzhakov.ui.ChatBackgroundDrawable;
import org.telegramIunzhakov.ui.Components.AnimatedEmojiDrawable;
import org.telegramIunzhakov.ui.Components.AnimatedEmojiSpan;
import org.telegramIunzhakov.ui.Components.AnimatedFloat;
import org.telegramIunzhakov.ui.Components.AvatarDrawable;
import org.telegramIunzhakov.ui.Components.ButtonBounce;
import org.telegramIunzhakov.ui.Components.ColoredImageSpan;
import org.telegramIunzhakov.ui.Components.CubicBezierInterpolator;
import org.telegramIunzhakov.ui.Components.Forum.ForumUtilities;
import org.telegramIunzhakov.ui.Components.ImageUpdater;
import org.telegramIunzhakov.ui.Components.LoadingDrawable;
import org.telegramIunzhakov.ui.Components.MediaActionDrawable;
import org.telegramIunzhakov.ui.Components.Premium.StarParticlesView;
import org.telegramIunzhakov.ui.Components.RLottieDrawable;
import org.telegramIunzhakov.ui.Components.RadialProgress2;
import org.telegramIunzhakov.ui.Components.RadialProgressView;
import org.telegramIunzhakov.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegramIunzhakov.ui.Components.RecyclerListView;
import org.telegramIunzhakov.ui.Components.ScaleStateListAnimator;
import org.telegramIunzhakov.ui.Components.Text;
import org.telegramIunzhakov.ui.Components.TopicSeparator;
import org.telegramIunzhakov.ui.Components.TypefaceSpan;
import org.telegramIunzhakov.ui.Components.URLSpanNoUnderline;
import org.telegramIunzhakov.ui.Components.spoilers.SpoilerEffect;
import org.telegramIunzhakov.ui.Gifts.GiftSheet;
import org.telegramIunzhakov.ui.GradientClip;
import org.telegramIunzhakov.ui.LaunchActivity;
import org.telegramIunzhakov.ui.PhotoViewer;
import org.telegramIunzhakov.ui.ProfileActivity;
import org.telegramIunzhakov.ui.Stars.StarGiftSheet;
import org.telegramIunzhakov.ui.Stars.StarGiftUniqueActionLayout;
import org.telegramIunzhakov.ui.Stars.StarsIntroActivity;
import org.telegramIunzhakov.messenger.utils.tlutils.AmountUtils;
import org.telegramIunzhakov.ui.Stories.StoriesUtilities;
import org.telegramIunzhakov.ui.Stories.UploadingDotsSpannable;
import org.telegramIunzhakov.ui.Stories.recorder.HintView2;
import org.telegramIunzhakov.ui.Stories.recorder.PreviewView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

public class ChatActionCell extends BaseCell implements DownloadController.FileDownloadProgressListener, NotificationCenter.NotificationCenterDelegate {
    private final static boolean USE_PREMIUM_GIFT_LOCAL_STICKER = false;
    private final static boolean USE_PREMIUM_GIFT_MONTHS_AS_EMOJI_NUMBERS = false;

    private static Map<Integer, String> monthsToEmoticon = new HashMap<>();

    static {
        monthsToEmoticon.put(1, 1 + "\u20E3");
        monthsToEmoticon.put(3, 2 + "\u20E3");
        monthsToEmoticon.put(6, 3 + "\u20E3");
        monthsToEmoticon.put(12, 4 + "\u20E3");
        monthsToEmoticon.put(24, 5 + "\u20E3");
    }

    private int backgroundRectHeight;
    private int backgroundButtonTop;
    private ButtonBounce bounce = new ButtonBounce(this);
    private LoadingDrawable loadingDrawable;

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.startSpoilers) {
            setSpoilersSuppressed(false);
        } else if (id == NotificationCenter.stopSpoilers) {
            setSpoilersSuppressed(true);
        } else if (id == NotificationCenter.didUpdatePremiumGiftStickers || id == NotificationCenter.starGiftsLoaded || id == NotificationCenter.didUpdateTonGiftStickers) {
            MessageObject messageObject = currentMessageObject;
            if (messageObject != null) {
                setMessageObject(messageObject, true);
            }
        } else if (id == NotificationCenter.diceStickersDidLoad) {
            if (Objects.equals(args[0], UserConfig.getInstance(currentAccount).premiumGiftsStickerPack)) {
                MessageObject messageObject = currentMessageObject;
                if (messageObject != null) {
                    setMessageObject(messageObject, true);
                }
            }
        }
    }

    public void setSpoilersSuppressed(boolean s) {
        for (SpoilerEffect eff : spoilers) {
            eff.setSuppressUpdates(s);
        }
    }

    private boolean canDrawInParent;
    private View invalidateWithParent;

    public void setInvalidateWithParent(View viewToInvalidate) {
        invalidateWithParent = viewToInvalidate;
    }

    public boolean hasButton() {
        return currentMessageObject != null && isButtonLayout(currentMessageObject) && giftPremiumButtonLayout != null;
    }

    public interface ChatActionCellDelegate {
        default void didClickImage(ChatActionCell cell) {
        }

        default void didClickButton(ChatActionCell cell) {
        }

        default void didOpenPremiumGift(ChatActionCell cell, TLRPC.TL_premiumGiftOption giftOption, String slug, boolean animateConfetti) {
        }

        default void didOpenPremiumGiftChannel(ChatActionCell cell, String slug, boolean animateConfetti) {
        }

        default boolean didLongPress(ChatActionCell cell, float x, float y) {
            return false;
        }

        default void needOpenUserProfile(long uid) {
        }

        default void didPressBotButton(MessageObject messageObject, TLRPC.KeyboardButton button) {
        }

        default void didPressReplyMessage(ChatActionCell cell, int id) {
        }

        default void didPressTaskLink(ChatActionCell cell, int id, int taskId) {
        }

        default void didPressReaction(ChatActionCell cell, TLRPC.ReactionCount reaction, boolean longpress, float x, float y) {
        }

        default void needOpenInviteLink(TLRPC.TL_chatInviteExported invite) {
        }

        default void needShowEffectOverlay(ChatActionCell cell, TLRPC.Document document, TLRPC.VideoSize videoSize) {
        }

        default void onTopicClick(ChatActionCell cell) {

        }

        default BaseFragment getBaseFragment() {
            return null;
        }

        default long getDialogId() {
            return 0;
        }

        default long getTopicId() {
            return 0;
        }

        default boolean canDrawOutboundsContent() {
            return true;
        }

        default void forceUpdate(ChatActionCell cell, boolean anchorScroll) {}
    }

    public interface ThemeDelegate extends Theme.ResourcesProvider {

        int getCurrentColor();
    }

    private int TAG;

    private URLSpan pressedLink;
    private SpoilerEffect spoilerPressed;
    private boolean textPressed;
    private boolean isSpoilerRevealing;
    private int currentAccount = UserConfig.selectedAccount;
    private ImageReceiver imageReceiver;
    private Drawable wallpaperPreviewDrawable;
    private Path clipPath;
    private AvatarDrawable avatarDrawable;
    private StaticLayout textLayout;
    private int textWidth;
    private int textHeight;
    private StaticLayout titleLayout;
    private int titleHeight;
    private int textX;
    private int textY;
    private int textXLeft;
    private int titleXLeft;
    private int previousWidth;
    private boolean imagePressed;
    private boolean giftButtonPressed;
    RadialProgressView progressView;
    float progressToProgress;
    StoriesUtilities.AvatarStoryParams avatarStoryParams = new StoriesUtilities.AvatarStoryParams(false);
    public boolean isAllChats;
    public boolean isForum;
    public boolean isMonoForum;
    public boolean isSideMenued;
    public boolean isSideMenuEnabled;
    public float sideMenuAlpha;
    public int sideMenuWidth;
    public boolean firstInChat;
    private int topicSeparatorTopPadding;
    public boolean showTopicSeparator = true;
    public TopicSeparator topicSeparator;

    public void setShowTopic(boolean show) {
        if (showTopicSeparator != show) {
            showTopicSeparator = show;
            invalidateOutbounds();
            invalidate();
        }
    }

    private RectF giftButtonRect = new RectF();

    public List<SpoilerEffect> spoilers = new ArrayList<>();
    private Stack<SpoilerEffect> spoilersPool = new Stack<>();
    private AnimatedEmojiSpan.EmojiGroupedSpans animatedEmojiStack;

    TextPaint textPaint;

    private float viewTop;
    private float viewTranslationX;
    private int backgroundHeight;
    private boolean visiblePartSet;

    private ImageLocation currentVideoLocation;

    private float lastTouchX;
    private float lastTouchY;

    private boolean wasLayout;

    private boolean hasReplyMessage;

    public final ReactionsLayoutInBubble reactionsLayoutInBubble = new ReactionsLayoutInBubble(this);
    public float starGiftLayoutX, starGiftLayoutY;
    public final StarGiftUniqueActionLayout starGiftLayout;

    private MessageObject currentMessageObject;
    private int customDate;
    private CharSequence customText;

    private int overrideBackground = -1;
    private int overrideText = -1;
    private Paint overrideBackgroundPaint;
    private TextPaint overrideTextPaint;
    private int overrideColor;
    private ArrayList<Integer> lineWidths = new ArrayList<>();
    private ArrayList<Integer> lineHeights = new ArrayList<>();
    private Path backgroundPath = new Path();
    private int backgroundLeft, backgroundRight;
    private RectF rect = new RectF();
    private boolean invalidatePath = true;
    private boolean invalidateColors = false;

    private ChatActionCellDelegate delegate;
    private Theme.ResourcesProvider themeDelegate;

    private int stickerSize;
    private int giftRectSize;
    private boolean giftRectEmpty;
    private StaticLayout giftPremiumTitleLayout;
    private StaticLayout giftPremiumSubtitleLayout;
    private boolean giftPremiumTextUncollapsed = false;
    private boolean giftPremiumTextCollapsed = false;
    private int giftPremiumTextCollapsedHeight;
    private AnimatedFloat giftPremiumTextExpandedAnimated = new AnimatedFloat(this, 0, 320, CubicBezierInterpolator.EASE_OUT_QUINT);

    private GradientClip giftPremiumTextClip;
    private TextLayout giftPremiumText;
    private int giftPremiumTextMoreX, giftPremiumTextMoreY, giftPremiumTextMoreH;
    private Text giftPremiumTextMore;
    
    class TextLayout {
        public float x, y;
        public int width;
        public StaticLayout layout;
        public TextPaint paint;
        public List<SpoilerEffect> spoilers = new ArrayList<>();
        public final AtomicReference<Layout> patchedLayout = new AtomicReference<>();
        public AnimatedEmojiSpan.EmojiGroupedSpans emoji;

        public void setText(CharSequence text, TextPaint textPaint, int width) {
            this.paint = textPaint;
            this.width = width;
            layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.1f, 0.0f, false);
            if (currentMessageObject == null || !currentMessageObject.isSpoilersRevealed) {
                SpoilerEffect.addSpoilers(ChatActionCell.this, layout, -1, width, null, spoilers);
            } else if (spoilers != null) {
                spoilers.clear();
            }
            attach();
        }

        public void attach() {
            emoji = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatActionCell.this, false, emoji, layout);
        }

        public void detach() {
            AnimatedEmojiSpan.release(ChatActionCell.this, emoji);
        }
    }
    
    private StaticLayout giftPremiumButtonLayout;
    private boolean buttonClickableAsImage = true;
    TextPaint settingWallpaperPaint;
    private StaticLayout settingWallpaperLayout;
    private float settingWallpaperProgress;
    private StaticLayout settingWallpaperProgressTextLayout;
    private float giftPremiumButtonWidth;

    private TextPaint giftTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint giftTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint giftSubtitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private TLRPC.Document giftSticker;
    private TLRPC.VideoSize giftEffectAnimation;
    private RadialProgress2 radialProgress = new RadialProgress2(this);
    private int giftPremiumAdditionalHeight;
    private boolean forceWasUnread;
    private RectF backgroundRect;
    private ImageReceiver.ImageReceiverDelegate giftStickerDelegate = (imageReceiver1, set, thumb, memCache) -> {
        if (set) {
            RLottieDrawable drawable = imageReceiver.getLottieAnimation();
            if (drawable != null) {
                MessageObject messageObject = currentMessageObject;
                if (messageObject != null && !messageObject.playedGiftAnimation) {
                    messageObject.playedGiftAnimation = true;
                    drawable.setCurrentFrame(0, false);
                    AndroidUtilities.runOnUIThread(drawable::restart);

                    if (messageObject != null && messageObject.wasUnread || forceWasUnread) {
                        forceWasUnread = messageObject.wasUnread = false;

                        try {
                            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        } catch (Exception ignored) {}

                        if (getContext() instanceof LaunchActivity) {
                            ((LaunchActivity) getContext()).getFireworksOverlay().start();
                        }

                        if (giftEffectAnimation != null && delegate != null) {
                            delegate.needShowEffectOverlay(ChatActionCell.this, giftSticker, giftEffectAnimation);
                        }
                    }
                } else if (drawable.getCurrentFrame() < 1) {
                    drawable.stop();
                    drawable.setCurrentFrame(drawable.getFramesCount() - 1, false);
                }
            }
        }
    };

    private boolean giftRibbonPaintFilterDark;
    private ColorMatrixColorFilter giftRibbonPaintFilter;
    private CornerPathEffect giftRibbonPaintEffect;
    private Path giftRibbonPath;
    private Text giftRibbonText;

    private View rippleView;

    private Path starsPath = new Path();
    private StarParticlesView.Drawable starParticlesDrawable;
    private int starsSize;

    public ChatActionCell(Context context) {
        this(context, false, null);
    }

    public ChatActionCell(Context context, boolean canDrawInParent, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        avatarStoryParams.drawSegments = false;
        this.canDrawInParent = canDrawInParent;
        this.themeDelegate = resourcesProvider;
        imageReceiver = new ImageReceiver(this);
        imageReceiver.setRoundRadius(AndroidUtilities.roundMessageSize / 2);
        avatarDrawable = new AvatarDrawable();
        TAG = DownloadController.getInstance(currentAccount).generateObserverTag();

        starGiftLayout = new StarGiftUniqueActionLayout(currentAccount, this, resourcesProvider);

        giftTitlePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
        giftSubtitlePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));
        giftTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));

        rippleView = new View(context);
        rippleView.setBackground(Theme.createSelectorDrawable(Theme.multAlpha(Color.BLACK, .1f), Theme.RIPPLE_MASK_ROUNDRECT_6DP, dp(16)));
        rippleView.setVisibility(GONE);
        addView(rippleView);

        starParticlesDrawable = new StarParticlesView.Drawable(10);
        starParticlesDrawable.type = 100;
        starParticlesDrawable.isCircle = false;
        starParticlesDrawable.roundEffect = true;
        starParticlesDrawable.useRotate = false;
        starParticlesDrawable.useBlur = true;
        starParticlesDrawable.checkBounds = true;
        starParticlesDrawable.size1 = 1;
        starParticlesDrawable.k1 = starParticlesDrawable.k2 = starParticlesDrawable.k3 = 0.98f;
        starParticlesDrawable.paused = false;
        starParticlesDrawable.speedScale = 0f;
        starParticlesDrawable.minLifeTime = 750;
        starParticlesDrawable.randLifeTime = 750;
        starParticlesDrawable.init();
    }

    public void setDelegate(ChatActionCellDelegate delegate) {
        this.delegate = delegate;
    }

    public ChatActionCellDelegate getDelegate() {
        return delegate;
    }

    public void setCustomDate(int date, boolean scheduled, boolean inLayout) {
        if (customDate == date || customDate / 3600 == date / 3600) {
            return;
        }
        CharSequence newText;
        if (scheduled) {
            if (date == 0x7ffffffe) {
                newText = getString("MessageScheduledUntilOnline", R.string.MessageScheduledUntilOnline);
            } else {
                newText = formatString("MessageScheduledOn", R.string.MessageScheduledOn, LocaleController.formatDateChat(date));
            }
        } else {
            newText = LocaleController.formatDateChat(date);
        }
        customDate = date;
        if (customText != null && TextUtils.equals(newText, customText)) {
            return;
        }
        customText = newText;
        accessibilityText = null;
        updateTextInternal(inLayout);
    }

    private void updateTextInternal(boolean inLayout) {
        if (getMeasuredWidth() != 0) {
            createLayout(customText, getMeasuredWidth());
            invalidate();
        }
        if (!wasLayout) {
            if (inLayout) {
                AndroidUtilities.runOnUIThread(this::requestLayout);
            } else {
                requestLayout();
            }
        } else {
            buildLayout();
        }
    }

    public void setCustomText(CharSequence text) {
        customText = text;
        if (customText != null) {
            updateTextInternal(false);
        }
    }

    public void setOverrideColor(int background, int text) {
        overrideBackground = background;
        overrideText = text;
    }

    public void setMessageObject(MessageObject messageObject) {
        setMessageObject(messageObject, false);
    }

    public void setMessageObject(MessageObject messageObject, boolean force) {
        if (messageObject == null) return;
        if (currentMessageObject == messageObject && (textLayout == null || TextUtils.equals(textLayout.getText(), messageObject.messageText)) && (hasReplyMessage || messageObject.replyMessageObject == null) && !force && messageObject.type != MessageObject.TYPE_SUGGEST_PHOTO && !messageObject.forceUpdate) {
            return;
        }
        if (BuildVars.DEBUG_PRIVATE_VERSION && Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
            FileLog.e(new IllegalStateException("Wrong thread!!!"));
        }
        accessibilityText = null;
        boolean messageIdChanged = currentMessageObject == null || currentMessageObject.stableId != messageObject.stableId;
        if (currentMessageObject != null) {
            messageObject.playedGiftAnimation = currentMessageObject.playedGiftAnimation;
        }
        currentMessageObject = messageObject;
        messageObject.forceUpdate = false;
        hasReplyMessage = messageObject.replyMessageObject != null;
        DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
        previousWidth = 0;
        isSpoilerRevealing = false;
        if (giftPremiumText != null && messageIdChanged) {
            giftPremiumText.detach();
            giftPremiumText = null;
            giftPremiumTextUncollapsed = false;
        }
        if (messageIdChanged || messageObject.reactionsChanged) {
            messageObject.reactionsChanged = false;
            final boolean isTag = messageObject.messageOwner != null && messageObject.messageOwner.reactions != null && messageObject.messageOwner.reactions.reactions_as_tags;
            if (messageObject.shouldDrawReactions()) {
                final boolean isSmall = !messageObject.shouldDrawReactionsInLayout();
                reactionsLayoutInBubble.setMessage(messageObject, isSmall, isTag, themeDelegate);
            } else {
                reactionsLayoutInBubble.setMessage(null, false, false, themeDelegate);
            }
        }
        starGiftLayout.set(messageObject, !messageIdChanged);
        imageReceiver.setAutoRepeatCount(0);
        imageReceiver.clearDecorators();
        if (messageObject.type != MessageObject.TYPE_ACTION_WALLPAPER) {
            wallpaperPreviewDrawable = null;
        }
        if (messageObject.actionDeleteGroupEventId != -1) {
            ScaleStateListAnimator.apply(this, .02f, 1.2f);
            overriddenMaxWidth = Math.max(dp(250), HintView2.cutInFancyHalf(messageObject.messageText, (TextPaint) getThemedPaint(Theme.key_paint_chatActionText)));
            ProfileActivity.ShowDrawable showDrawable = ChannelAdminLogActivity.findDrawable(messageObject.messageText);
            if (showDrawable != null) {
                showDrawable.setView(this);
            }
        } else {
            ScaleStateListAnimator.reset(this);
            overriddenMaxWidth = 0;
        }
        if (messageObject.isStoryMention()) {
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(messageObject.messageOwner.media.user_id);
            avatarDrawable.setInfo(currentAccount, user);
            TL_stories.StoryItem storyItem = messageObject.messageOwner.media.storyItem;
            if (storyItem != null && storyItem.noforwards) {
                imageReceiver.setForUserOrChat(user, avatarDrawable, null, true, 0, true);
            } else {
                StoriesUtilities.setImage(imageReceiver, storyItem);
            }
            imageReceiver.setRoundRadius((int) (stickerSize / 2f));
        } else if (messageObject.type == MessageObject.TYPE_ACTION_WALLPAPER) {
            TLRPC.PhotoSize strippedPhotoSize = null;
            if (messageObject.strippedThumb == null) {
                for (int a = 0, N = messageObject.photoThumbs.size(); a < N; a++) {
                    TLRPC.PhotoSize photoSize = messageObject.photoThumbs.get(a);
                    if (photoSize instanceof TLRPC.TL_photoStrippedSize) {
                        strippedPhotoSize = photoSize;
                        break;
                    }
                }
            }
            TLRPC.WallPaper wallPaper = null;
            if (messageObject.currentEvent != null && messageObject.currentEvent.action instanceof TLRPC.TL_channelAdminLogEventActionChangeWallpaper) {
                wallPaper = ((TLRPC.TL_channelAdminLogEventActionChangeWallpaper) messageObject.currentEvent.action).new_value;
            } else if (messageObject.messageOwner != null && messageObject.messageOwner.action != null) {
                TLRPC.MessageAction action = messageObject.messageOwner.action;
                wallPaper = action.wallpaper;
            }
            if (!TextUtils.isEmpty(ChatThemeController.getWallpaperEmoticon(wallPaper))) {
                final boolean isDark = themeDelegate != null ? themeDelegate.isDark() : Theme.isCurrentThemeDark();
                imageReceiver.clearImage();
                wallpaperPreviewDrawable = PreviewView.getBackgroundDrawableFromTheme(currentAccount, ChatThemeController.getWallpaperEmoticon(wallPaper), isDark, false);
                if (wallpaperPreviewDrawable != null) {
                    wallpaperPreviewDrawable.setCallback(this);
                }
            } else if (wallPaper != null && wallPaper.uploadingImage != null) {
                imageReceiver.setImage(ImageLocation.getForPath(wallPaper.uploadingImage), "150_150_wallpaper" + wallPaper.id + ChatBackgroundDrawable.hash(wallPaper.settings), null, null, ChatBackgroundDrawable.createThumb(wallPaper), 0, null, wallPaper, 1);
                wallpaperPreviewDrawable = null;
            } else if (wallPaper != null) {
                TLRPC.Document document = null;
                if (messageObject.photoThumbsObject instanceof TLRPC.Document) {
                    document = (TLRPC.Document) messageObject.photoThumbsObject;
                } else if (wallPaper != null) {
                    document = wallPaper.document;
                }
                imageReceiver.setImage(ImageLocation.getForDocument(document), "150_150_wallpaper" + wallPaper.id + ChatBackgroundDrawable.hash(wallPaper.settings), null, null, ChatBackgroundDrawable.createThumb(wallPaper), 0, null, wallPaper, 1);
                wallpaperPreviewDrawable = null;
            } else {
                wallpaperPreviewDrawable = null;
            }
            imageReceiver.setRoundRadius((int) (stickerSize / 2f));

            float uploadingInfoProgress = getUploadingInfoProgress(messageObject);
            if (uploadingInfoProgress == 1f) {
                radialProgress.setProgress(1f, !messageIdChanged);
                radialProgress.setIcon(MediaActionDrawable.ICON_NONE, !messageIdChanged, !messageIdChanged);
            } else {
                radialProgress.setIcon(MediaActionDrawable.ICON_CANCEL, !messageIdChanged, !messageIdChanged);
            }
        } else if (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO) {
            imageReceiver.setRoundRadius((int) (stickerSize / 2f));
            imageReceiver.setAllowStartLottieAnimation(true);
            imageReceiver.setDelegate(null);
            TLRPC.TL_messageActionSuggestProfilePhoto action = (TLRPC.TL_messageActionSuggestProfilePhoto) messageObject.messageOwner.action;

            TLRPC.VideoSize videoSize = FileLoader.getClosestVideoSizeWithSize(action.photo.video_sizes, 1000);
            ImageLocation videoLocation;
            if (action.photo.video_sizes != null && !action.photo.video_sizes.isEmpty()) {
                videoLocation = ImageLocation.getForPhoto(videoSize, action.photo);
            } else {
                videoLocation = null;
            }
            TLRPC.Photo photo = messageObject.messageOwner.action.photo;
            TLRPC.PhotoSize strippedPhotoSize = null;
            if (messageObject.strippedThumb == null) {
                for (int a = 0, N = messageObject.photoThumbs.size(); a < N; a++) {
                    TLRPC.PhotoSize photoSize = messageObject.photoThumbs.get(a);
                    if (photoSize instanceof TLRPC.TL_photoStrippedSize) {
                        strippedPhotoSize = photoSize;
                        break;
                    }
                }
            }
            TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 1000);
            if (photoSize != null) {
                if (videoSize != null) {
                    imageReceiver.setImage(videoLocation, ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForPhoto(photoSize, photo), "150_150", ImageLocation.getForObject(strippedPhotoSize, messageObject.photoThumbsObject), "50_50_b", messageObject.strippedThumb, 0, null, messageObject, 0);
                } else {
                    imageReceiver.setImage(ImageLocation.getForPhoto(photoSize, photo), "150_150", ImageLocation.getForObject(strippedPhotoSize, messageObject.photoThumbsObject), "50_50_b", messageObject.strippedThumb, 0, null, messageObject, 0);
                }
            }

            imageReceiver.setAllowStartLottieAnimation(false);
            ImageUpdater imageUpdater = MessagesController.getInstance(currentAccount).photoSuggestion.get(messageObject.messageOwner.local_id);
            if (imageUpdater == null || imageUpdater.getCurrentImageProgress() == 1f) {
                radialProgress.setProgress(1f, !messageIdChanged);
                radialProgress.setIcon(MediaActionDrawable.ICON_NONE, !messageIdChanged, !messageIdChanged);
            } else {
                radialProgress.setIcon(MediaActionDrawable.ICON_CANCEL, !messageIdChanged, !messageIdChanged);
            }
        } else if (messageObject.type == MessageObject.TYPE_GIFT_STARS || messageObject.type == MessageObject.TYPE_GIFT_PREMIUM || messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL) {
            imageReceiver.setRoundRadius(0);

            if (USE_PREMIUM_GIFT_LOCAL_STICKER) {
                forceWasUnread = messageObject.wasUnread;
                imageReceiver.setAllowStartLottieAnimation(false);
                imageReceiver.setDelegate(giftStickerDelegate);
                imageReceiver.setImageBitmap(new RLottieDrawable(R.raw.premium_gift, messageObject.getId() + "_" + R.raw.premium_gift, dp(160), dp(160)));
            } else {
                TLRPC.TL_messages_stickerSet set = null;
                TLRPC.Document document = null;
                String packName = null;
                Object parentObject = null;

                if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGift) {
                    parentObject = messageObject;
                    final TLRPC.TL_messageActionStarGift action = (TLRPC.TL_messageActionStarGift) messageObject.messageOwner.action;
                    if (action.gift != null) {
                        document = action.gift.sticker;
                    }
                } else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGiftUnique && ((TLRPC.TL_messageActionStarGiftUnique) messageObject.messageOwner.action).refunded) {
                    parentObject = messageObject;
                    final TLRPC.TL_messageActionStarGiftUnique action = (TLRPC.TL_messageActionStarGiftUnique) messageObject.messageOwner.action;
                    if (action.gift != null) {
                        document = action.gift.getDocument();
                    }
                } else {
                    if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftTon) {
                        packName = UserConfig.getInstance(currentAccount).premiumTonStickerPack;
                        if (packName == null) {
                            MediaDataController.getInstance(currentAccount).checkTonGiftStickers();
                            return;
                        }
                    } else {
                        packName = UserConfig.getInstance(currentAccount).premiumGiftsStickerPack;
                        if (packName == null) {
                            MediaDataController.getInstance(currentAccount).checkPremiumGiftStickers();
                            return;
                        }
                    }

                    set = MediaDataController.getInstance(currentAccount).getStickerSetByName(packName);
                    if (set == null) {
                        set = MediaDataController.getInstance(currentAccount).getStickerSetByEmojiOrName(packName);
                    }
                    if (set != null) {
                        parentObject = set;
                        int months = messageObject.messageOwner.action.months;
                        String monthsEmoticon;
                        if (messageObject.type == MessageObject.TYPE_GIFT_STARS) {
                            String emoji;
                            if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftTon) {
                                emoji = StarsIntroActivity.getTonGiftEmoji(messageObject.messageOwner.action.cryptoAmount);
                            } else {
                                final long stars;
                                if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftStars) {
                                    stars = ((TLRPC.TL_messageActionGiftStars) messageObject.messageOwner.action).stars;
                                } else {
                                    stars = ((TLRPC.TL_messageActionPrizeStars) messageObject.messageOwner.action).stars;
                                }
                                if (stars <= 1000) {
                                    emoji = "2⃣";
                                } else if (stars < 2500) {
                                    emoji = "3⃣";
                                } else {
                                    emoji = "4⃣";
                                }
                            }
                            for (int i = 0; i < set.packs.size(); ++i) {
                                TLRPC.TL_stickerPack pack = set.packs.get(i);
                                if (TextUtils.equals(pack.emoticon, emoji) && !pack.documents.isEmpty()) {
                                    long documentId = pack.documents.get(0);
                                    for (int j = 0; j < set.documents.size(); ++j) {
                                        TLRPC.Document d = set.documents.get(j);
                                        if (d != null && d.id == documentId) {
                                            document = d;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        } else {
                            if (USE_PREMIUM_GIFT_MONTHS_AS_EMOJI_NUMBERS) {
                                StringBuilder monthsEmoticonBuilder = new StringBuilder();
                                while (months > 0) {
                                    monthsEmoticonBuilder.insert(0, (months % 10) + "\u20E3");
                                    months /= 10;
                                }
                                monthsEmoticon = monthsEmoticonBuilder.toString();
                            } else {
                                monthsEmoticon = monthsToEmoticon.get(months);
                            }
                            for (TLRPC.TL_stickerPack pack : set.packs) {
                                if (Objects.equals(pack.emoticon, monthsEmoticon)) {
                                    for (long id : pack.documents) {
                                        for (TLRPC.Document doc : set.documents) {
                                            if (doc.id == id) {
                                                document = doc;
                                                break;
                                            }
                                        }
                                        if (document != null) {
                                            break;
                                        }
                                    }
                                }
                                if (document != null) {
                                    break;
                                }
                            }
                        }
                        if (document == null && !set.documents.isEmpty()) {
                            document = set.documents.get(0);
                        }
                    }
                }

                forceWasUnread = messageObject.wasUnread;
                giftSticker = document;
                if (document != null) {
                    imageReceiver.setAllowStartLottieAnimation(true);
                    imageReceiver.setDelegate(giftStickerDelegate);

                    giftEffectAnimation = null;
                    for (int i = 0; i < document.video_thumbs.size(); i++) {
                        if ("f".equals(document.video_thumbs.get(i).type)) {
                            giftEffectAnimation = document.video_thumbs.get(i);
                            break;
                        }
                    }
                    if (messageIdChanged || messageObject == null || messageObject.type != MessageObject.TYPE_GIFT_PREMIUM) {
                        SvgHelper.SvgDrawable svgThumb = DocumentObject.getSvgThumb(document, Theme.key_windowBackgroundGray, 0.3f);
                        imageReceiver.setAutoRepeat(0);
                        imageReceiver.setImage(ImageLocation.getForDocument(document), String.format(Locale.US, "%d_%d_nr_messageId=%d", 160, 160, messageObject.stableId), svgThumb, "tgs", parentObject, 1);
                    }
                } else if (packName != null) {
                    MediaDataController.getInstance(currentAccount).loadStickersByEmojiOrName(packName, false, set == null);
                }
            }
        } else if (messageObject.type == MessageObject.TYPE_ACTION_PHOTO) {
            imageReceiver.setAllowStartLottieAnimation(true);
            imageReceiver.setDelegate(null);
            imageReceiver.setRoundRadius(AndroidUtilities.roundMessageSize / 2);
            imageReceiver.setAutoRepeatCount(1);
            long id = messageObject.getDialogId();
            avatarDrawable.setInfo(id, null, null);
            if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto) {
                imageReceiver.setImage(null, null, avatarDrawable, null, messageObject, 0);
            } else {
                TLRPC.PhotoSize strippedPhotoSize = null;
                if (messageObject.strippedThumb == null) {
                    for (int a = 0, N = messageObject.photoThumbs.size(); a < N; a++) {
                        TLRPC.PhotoSize photoSize = messageObject.photoThumbs.get(a);
                        if (photoSize instanceof TLRPC.TL_photoStrippedSize) {
                            strippedPhotoSize = photoSize;
                            break;
                        }
                    }
                }
                TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 640);
                if (photoSize != null) {
                    TLRPC.Photo photo = messageObject.messageOwner.action.photo;
                    TLRPC.VideoSize videoSize = null;
                    if (!photo.video_sizes.isEmpty() && SharedConfig.isAutoplayGifs()) {
                        videoSize = FileLoader.getClosestVideoSizeWithSize(photo.video_sizes, 1000);
                        if (!messageObject.mediaExists && !DownloadController.getInstance(currentAccount).canDownloadMedia(DownloadController.AUTODOWNLOAD_TYPE_VIDEO, videoSize.size)) {
                            currentVideoLocation = ImageLocation.getForPhoto(videoSize, photo);
                            String fileName = FileLoader.getAttachFileName(videoSize);
                            DownloadController.getInstance(currentAccount).addLoadingFileObserver(fileName, messageObject, this);
                            videoSize = null;
                        }
                    }
                    if (videoSize != null) {
                        imageReceiver.setImage(ImageLocation.getForPhoto(videoSize, photo), ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForObject(strippedPhotoSize, messageObject.photoThumbsObject), "50_50_b", messageObject.strippedThumb, 0, null, messageObject, 1);
                    } else {
                        imageReceiver.setImage(ImageLocation.getForObject(photoSize, messageObject.photoThumbsObject), "150_150", ImageLocation.getForObject(strippedPhotoSize, messageObject.photoThumbsObject), "50_50_b", messageObject.strippedThumb, 0, null, messageObject, 1);
                    }
                } else {
                    imageReceiver.setImageBitmap(avatarDrawable);
                }
            }
            imageReceiver.setVisible(!PhotoViewer.isShowingImage(messageObject), false);
        } else {
            imageReceiver.setAllowStartLottieAnimation(true);
            imageReceiver.setDelegate(null);
            imageReceiver.setImageBitmap((Bitmap) null);
        }
        if (firstInChat && isAllChats && isSideMenued && (isForum || isMonoForum)) {
            topicSeparatorTopPadding = dp(33);
            if (topicSeparator == null) {
                topicSeparator = new TopicSeparator(currentAccount, this, themeDelegate, true);
                topicSeparator.setOnClickListener(() -> {
                    if (delegate != null) {
                        delegate.onTopicClick(this);
                    }
                });
            }
            if (!topicSeparator.update(currentMessageObject)) {
                topicSeparator.detach();
                topicSeparator = null;
                topicSeparatorTopPadding = 0;
            } else if (attachedToWindow) {
                topicSeparator.attach();
            }
        } else {
            if (topicSeparator != null) {
                topicSeparator.detach();
                topicSeparator = null;
            }
            topicSeparatorTopPadding = 0;
        }
        if (getPaddingTop() != topicSeparatorTopPadding) {
            setPadding(0, topicSeparatorTopPadding, 0, 0);
        }
        rippleView.setVisibility(isButtonLayout(messageObject) && !starGiftLayout.has() ? VISIBLE : GONE);
        ForumUtilities.applyTopicToMessage(messageObject);
        requestLayout();
    }

    private float getUploadingInfoProgress(MessageObject messageObject) {
        try {
            if (messageObject != null && messageObject.type == MessageObject.TYPE_ACTION_WALLPAPER) {
                MessagesController messagesController = MessagesController.getInstance(currentAccount);
                if (messagesController.uploadingWallpaper != null && TextUtils.equals(messageObject.messageOwner.action.wallpaper.uploadingImage, messagesController.uploadingWallpaper)) {
                    return messagesController.uploadingWallpaperInfo.uploadingProgress;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return 1;
    }

    public MessageObject getMessageObject() {
        return currentMessageObject;
    }

    public ImageReceiver getPhotoImage() {
        return imageReceiver;
    }

    public void setVisiblePart(float visibleTop, int parentH) {
        visiblePartSet = true;
        backgroundHeight = parentH;
        viewTop = visibleTop;
        viewTranslationX = 0;
    }

    private float dimAmount;
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public void setVisiblePart(float visibleTop, float tx, int parentH, float dimAmount) {
        visiblePartSet = true;
        backgroundHeight = parentH;
        viewTop = visibleTop;
        viewTranslationX = tx;

        this.dimAmount = dimAmount;
        dimPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, (int) (0xFF * dimAmount)));
        invalidate();
    }

    @Override
    protected boolean onLongPress() {
        if (delegate != null) {
            return delegate.didLongPress(this, lastTouchX, lastTouchY);
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        rippleView.layout((int) giftButtonRect.left, (int) giftButtonRect.top, (int) giftButtonRect.right, (int) giftButtonRect.bottom);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
        DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
        imageReceiver.onDetachedFromWindow();
        setStarsPaused(true);
        wasLayout = false;
        AnimatedEmojiSpan.release(this, animatedEmojiStack);
        if (giftPremiumText != null) {
            giftPremiumText.detach();
        }

        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didUpdatePremiumGiftStickers);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didUpdateTonGiftStickers);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.starGiftsLoaded);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.diceStickersDidLoad);
        avatarStoryParams.onDetachFromWindow();

        transitionParams.onDetach();
        starGiftLayout.detach();
        reactionsLayoutInBubble.onDetachFromWindow();
        if (topicSeparator != null) {
            topicSeparator.detach();
        }
    }

    private boolean attachedToWindow;
    public boolean isCellAttachedToWindow() {
        return attachedToWindow;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        imageReceiver.onAttachedToWindow();
        setStarsPaused(false);

        animatedEmojiStack = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, this, canDrawInParent && (delegate != null && !delegate.canDrawOutboundsContent()), animatedEmojiStack, textLayout);
        if (giftPremiumText != null) {
            giftPremiumText.attach();
        }
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didUpdatePremiumGiftStickers);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didUpdateTonGiftStickers);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.starGiftsLoaded);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.diceStickersDidLoad);

        if (currentMessageObject != null && currentMessageObject.type == MessageObject.TYPE_SUGGEST_PHOTO) {
            setMessageObject(currentMessageObject, true);
        }
        starGiftLayout.attach();
        reactionsLayoutInBubble.onAttachToWindow();
        if (topicSeparator != null) {
            topicSeparator.attach();
        }
    }

    private void setStarsPaused(boolean paused) {
        if (paused == starParticlesDrawable.paused) {
            return;
        }
        starParticlesDrawable.paused = paused;
        if (paused) {
            starParticlesDrawable.pausedTime = System.currentTimeMillis();
        } else {
            for (int i = 0; i < starParticlesDrawable.particles.size(); i++) {
                starParticlesDrawable.particles.get(i).lifeTime += System.currentTimeMillis() - starParticlesDrawable.pausedTime;
            }
            invalidate();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        MessageObject messageObject = currentMessageObject;
        if (messageObject == null) {
            return super.onTouchEvent(event);
        }

        if (topicSeparator != null && topicSeparator.onTouchEvent(event, false)) {
            return true;
        }

        if (starGiftLayout.has() && starGiftLayout.onTouchEvent(starGiftLayoutX, starGiftLayoutY, event)) {
            return true;
        }

        if (reactionsLayoutInBubble.checkTouchEvent(event)) {
            return true;
        }

        float x = lastTouchX = event.getX() - sideMenuWidth / 2f;
        float y = lastTouchY = event.getY() + getPaddingTop();

        boolean result = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate != null) {
                if ((messageObject.type == MessageObject.TYPE_ACTION_PHOTO || isButtonLayout(messageObject)) && imageReceiver.isInsideImage(x, y)) {
                    imagePressed = true;
                    result = true;
                }
                if (radialProgress.getIcon() == MediaActionDrawable.ICON_NONE && (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO || messageObject.type == MessageObject.TYPE_ACTION_WALLPAPER) && backgroundRect.contains(x, y)) {
                    imagePressed = true;
                    result = true;
                }
                if (giftPremiumText != null && giftPremiumTextCollapsed) {
                    AndroidUtilities.rectTmp.set(giftPremiumText.x, giftPremiumText.y, giftPremiumText.x + giftPremiumText.layout.getWidth(), giftPremiumText.y + giftPremiumText.layout.getHeight());
                    if (AndroidUtilities.rectTmp.contains(x, y)) {
                        textPressed = true;
                        result = true;
                    }
                }
                if (isButtonLayout(messageObject) && giftPremiumButtonLayout != null && (giftButtonRect.contains(x, y) || buttonClickableAsImage && backgroundRect.contains(x, y))) {
                    rippleView.setPressed(giftButtonPressed = true);
                    bounce.setPressed(true);
                    result = true;
                }
                if (!result && isMessageActionSuggestedPostApproval()) {
                    textPressed = true;
                    result = true;
                }
                if (!result && TlUtils.isInstance(currentMessageObject != null && currentMessageObject.messageOwner != null ? currentMessageObject.messageOwner.action : null, TLRPC.TL_messageActionSuggestedPostRefund.class, TLRPC.TL_messageActionSuggestedPostSuccess.class)) {
                    textPressed = true;
                    result = true;
                }
                if (result) {
                    startCheckLongPress();
                }
            }
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (textPressed) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        rippleView.setPressed(textPressed = false);
                        bounce.setPressed(false);
                        if (delegate != null && messageObject != null && messageObject.replyMessageObject != null && messageObject.messageOwner != null && (TlUtils.isInstance(messageObject.messageOwner.action, TLRPC.TL_messageActionTodoAppendTasks.class, TLRPC.TL_messageActionTodoCompletions.class, TLRPC.TL_messageActionSuggestedPostApproval.class, TLRPC.TL_messageActionSuggestedPostRefund.class, TLRPC.TL_messageActionSuggestedPostSuccess.class))) {
                            delegate.didPressReplyMessage(this, currentMessageObject.getReplyMsgId());
                        } else if (giftPremiumTextCollapsed && !giftPremiumTextUncollapsed && giftPremiumText != null) {
                            int dy = giftPremiumText.layout.getHeight() - giftPremiumTextCollapsedHeight;
                            giftPremiumTextUncollapsed = true;
                            if (delegate != null) {
                                delegate.forceUpdate(this, false);
                                if (getParent() instanceof RecyclerListView) {
                                    ((RecyclerListView) getParent()).smoothScrollBy(0, dy + dp(24));
                                }
                            }
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        textPressed = false;
                        bounce.setPressed(false);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (giftPremiumText == null || !giftPremiumTextCollapsed) {
                            textPressed = false;
                            result = true;
                        } else {
                            AndroidUtilities.rectTmp.set(giftPremiumText.x, giftPremiumText.y, giftPremiumText.x + giftPremiumText.layout.getWidth(), giftPremiumText.y + giftPremiumText.layout.getHeight());
                            if (!AndroidUtilities.rectTmp.contains(x, y)) {
                                textPressed = false;
                                result = true;
                            }
                        }
                        break;
                }
            } else if (giftButtonPressed) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        imagePressed = false;
                        rippleView.setPressed(giftButtonPressed = false);
                        bounce.setPressed(false);
                        if (delegate != null) {
                            if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL) {
                                playSoundEffect(SoundEffectConstants.CLICK);
                                openPremiumGiftChannel();
                            } else if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM) {
                                playSoundEffect(SoundEffectConstants.CLICK);
                                openPremiumGiftPreview();
                            } else if (messageObject.type == MessageObject.TYPE_GIFT_STARS) {
                                playSoundEffect(SoundEffectConstants.CLICK);
                                openStarsGiftTransaction();
                            } else if (messageObject.messageOwner != null && messageObject.messageOwner.action instanceof TLRPC.TL_messageActionSuggestedPostApproval && ((TLRPC.TL_messageActionSuggestedPostApproval) messageObject.messageOwner.action).balance_too_low) {
                                playSoundEffect(SoundEffectConstants.CLICK);
                                openStarsNeedSheet();
                            } else {
                                ImageUpdater imageUpdater = MessagesController.getInstance(currentAccount).photoSuggestion.get(messageObject.messageOwner.local_id);
                                if (imageUpdater == null) {
                                    if (buttonClickableAsImage) {
                                        delegate.didClickImage(this);
                                    } else {
                                        delegate.didClickButton(this);
                                    }
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        imagePressed = false;
                        rippleView.setPressed(giftButtonPressed = false);
                        bounce.setPressed(false);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!(isButtonLayout(messageObject) && (giftButtonRect.contains(x, y) || backgroundRect.contains(x, y)))) {
                            rippleView.setPressed(giftButtonPressed = false);
                            bounce.setPressed(false);
                        }
                        break;
                }
            } else if (imagePressed) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        imagePressed = false;
                        if (giftPremiumTextCollapsed && !giftPremiumTextUncollapsed && giftPremiumText != null) {
                            int dy = giftPremiumText.layout.getHeight() - giftPremiumTextCollapsedHeight;
                            giftPremiumTextUncollapsed = true;
                            if (delegate != null) {
                                delegate.forceUpdate(this, false);
                                if (getParent() instanceof RecyclerListView) {
                                    ((RecyclerListView) getParent()).smoothScrollBy(0, dy + dp(16));
                                }
                            }
                            return true;
                        }
                        if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL) {
                            openPremiumGiftChannel();
                        } else if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM) {
                            openPremiumGiftPreview();
                        } else if (messageObject.type == MessageObject.TYPE_GIFT_STARS) {
                            openStarsGiftTransaction();
                        } else if (delegate != null) {
                            boolean consumed = false;
                            if (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO) {
                                ImageUpdater imageUpdater = MessagesController.getInstance(currentAccount).photoSuggestion.get(messageObject.messageOwner.local_id);
                                if (imageUpdater != null) {
                                    consumed = true;
                                    imageUpdater.cancel();
                                }
                            }
                            if (!consumed) {
                                delegate.didClickImage(this);
                                playSoundEffect(SoundEffectConstants.CLICK);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        imagePressed = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isNewStyleButtonLayout()) {
                            if (!backgroundRect.contains(x, y)) {
                                imagePressed = false;
                            }
                        } else {
                            if (!imageReceiver.isInsideImage(x, y)) {
                                imagePressed = false;
                            }
                        }
                        break;
                }
            }
        }
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || (pressedLink != null || spoilerPressed != null) && event.getAction() == MotionEvent.ACTION_UP) {
                if (giftPremiumText != null && giftPremiumText.spoilers != null && !giftPremiumText.spoilers.isEmpty() && !isSpoilerRevealing) {
                    for (SpoilerEffect eff : giftPremiumText.spoilers) {
                        if (eff.getBounds().contains((int) (x - giftPremiumText.x), (int) (y - giftPremiumText.y))) {
                            pressedLink = null;
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                spoilerPressed = eff;
                                result = true;
                            } else {
                                if (eff == spoilerPressed) {
                                    isSpoilerRevealing = true;
                                    spoilerPressed.setOnRippleEndCallback(() -> post(() -> {
                                        isSpoilerRevealing = false;
                                        getMessageObject().isSpoilersRevealed = true;
                                        if (giftPremiumText.spoilers != null) {
                                            giftPremiumText.spoilers.clear();
                                        }
                                        invalidate();
                                    }));
                                    float width = giftPremiumText.layout.getWidth(), height = giftPremiumText.layout.getHeight();
                                    float rad = (float) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
                                    spoilerPressed.startRipple((int) (x - giftPremiumText.x), (int) (y - giftPremiumText.y), rad);
                                    invalidate();
                                }
                                result = true;
                            }
                            break;
                        }
                    }
                }
                if (!result && textLayout != null && x >= textX && y >= textY && x <= textX + textWidth && y <= textY + textHeight) {
                    y -= textY;
                    x -= textXLeft;

                    if (!result) {
                        final int line = textLayout.getLineForVertical((int) y);
                        final int off = textLayout.getOffsetForHorizontal(line, x);
                        final float left = textLayout.getLineLeft(line);
                        if (left <= x && left + textLayout.getLineWidth(line) >= x && messageObject.messageText instanceof Spannable) {
                            Spannable buffer = (Spannable) messageObject.messageText;
                            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

                            if (link.length != 0) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    pressedLink = link[0];
                                    result = true;
                                } else {
                                    if (link[0] == pressedLink) {
                                        openLink(pressedLink);
                                        result = true;
                                    }
                                }
                            } else {
                                pressedLink = null;
                            }
                        } else {
                            pressedLink = null;
                        }
                    }
                } else {
                    pressedLink = null;
                }
            }
        }

        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }

    private void openPremiumGiftChannel() {
        if (delegate != null) {
            TLRPC.TL_messageActionGiftCode gifCodeAction = (TLRPC.TL_messageActionGiftCode) currentMessageObject.messageOwner.action;
            AndroidUtilities.runOnUIThread(() -> delegate.didOpenPremiumGiftChannel(ChatActionCell.this, gifCodeAction.slug, false));
        }
    }

    private boolean isSelfGiftCode() {
        if (currentMessageObject != null && (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftCode || currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftStars)) {
            if (currentMessageObject.messageOwner.from_id instanceof TLRPC.TL_peerUser) {
                return UserObject.isUserSelf(MessagesController.getInstance(currentAccount).getUser(currentMessageObject.messageOwner.from_id.user_id));
            }
        }
        return false;
    }

    private boolean isGiftCode() {
        return currentMessageObject != null && currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftCode;
    }

    private void openPremiumGiftPreview() {
        final TLRPC.TL_premiumGiftOption giftOption = new TLRPC.TL_premiumGiftOption();
        final TLRPC.MessageAction action = currentMessageObject.messageOwner.action;
        giftOption.amount = action.amount;
        giftOption.months = action.months;
        giftOption.currency = action.currency;
        String slug;
        if (isGiftCode()) {
            slug = isSelfGiftCode() ? null : ((TLRPC.TL_messageActionGiftCode) currentMessageObject.messageOwner.action).slug;
        } else {
            slug = null;
        }
        if (delegate != null) {
            AndroidUtilities.runOnUIThread(() -> delegate.didOpenPremiumGift(ChatActionCell.this, giftOption, slug, false));
        }
    }

    private void openStarsGiftTransaction() {
        if (currentMessageObject == null || currentMessageObject.messageOwner == null) return;
        if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftStars) {
            StarsIntroActivity.showTransactionSheet(getContext(), currentAccount, currentMessageObject.messageOwner.date, currentMessageObject.messageOwner.from_id, currentMessageObject.messageOwner.peer_id, (TLRPC.TL_messageActionGiftStars) currentMessageObject.messageOwner.action, avatarStoryParams.resourcesProvider);
        } else if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionPrizeStars) {
            StarsIntroActivity.showTransactionSheet(getContext(), currentAccount, currentMessageObject.messageOwner.date, currentMessageObject.messageOwner.from_id, currentMessageObject.messageOwner.peer_id, (TLRPC.TL_messageActionPrizeStars) currentMessageObject.messageOwner.action, avatarStoryParams.resourcesProvider);
        } else if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftTon) {
            StarsIntroActivity.showTransactionSheet(getContext(), currentAccount, currentMessageObject.messageOwner.date, currentMessageObject.messageOwner.from_id, currentMessageObject.messageOwner.peer_id, (TLRPC.TL_messageActionGiftTon) currentMessageObject.messageOwner.action, avatarStoryParams.resourcesProvider);
        } else if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGift) {
            final TLRPC.TL_messageActionStarGift action = (TLRPC.TL_messageActionStarGift) currentMessageObject.messageOwner.action;
            if (action.forceIn) return;
//            StarsIntroActivity.showActionGiftSheet(getContext(), currentAccount, currentMessageObject.getDialogId(), currentMessageObject.isOutOwner(), currentMessageObject.messageOwner.date, currentMessageObject.getId(), action, themeDelegate);
            new StarGiftSheet(getContext(), currentAccount, currentMessageObject.getDialogId(), themeDelegate)
                .set(currentMessageObject)
                .show();
        } else if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGiftUnique) {
            new StarGiftSheet(getContext(), currentAccount, currentMessageObject.getDialogId(), themeDelegate)
                .set(currentMessageObject)
                .show();
        }
    }

    private void openStarsNeedSheet() {
        final MessageSuggestionParams params = currentMessageObject.obtainSuggestionOffer();
        if (params.amount == null || params.amount.currency != AmountUtils.Currency.STARS) {
            return;
        }

        new StarsIntroActivity.StarsNeededSheet(getContext(), themeDelegate, params.amount.asDecimal(), StarsIntroActivity.StarsNeededSheet.TYPE_PRIVATE_MESSAGE, ForumUtilities.getMonoForumTitle(currentAccount, currentMessageObject.getDialogId(), true), null)
            .show();
    }

    private void openLink(CharacterStyle link) {
        if (delegate != null && link instanceof URLSpan) {
            String url = ((URLSpan) link).getURL();
            if (url.startsWith("task")) {
                final int taskId = Integer.parseInt(url.substring(5));
                delegate.didPressTaskLink(this, currentMessageObject.getReplyMsgId(), taskId);
            } else if (url.startsWith("topic") && pressedLink instanceof URLSpanNoUnderline) {
                URLSpanNoUnderline spanNoUnderline = (URLSpanNoUnderline) pressedLink;
                TLObject object = spanNoUnderline.getObject();
                if (object instanceof TLRPC.TL_forumTopic) {
                    TLRPC.TL_forumTopic forumTopic = (TLRPC.TL_forumTopic) object;
                    ForumUtilities.openTopic(delegate.getBaseFragment(), -delegate.getDialogId(), forumTopic, 0);
                }
            } else if (url.startsWith("invite") && pressedLink instanceof URLSpanNoUnderline) {
                URLSpanNoUnderline spanNoUnderline = (URLSpanNoUnderline) pressedLink;
                TLObject object = spanNoUnderline.getObject();
                if (object instanceof TLRPC.TL_chatInviteExported) {
                    TLRPC.TL_chatInviteExported invite = (TLRPC.TL_chatInviteExported) object;
                    delegate.needOpenInviteLink(invite);
                }
            } else if (url.startsWith("game")) {
                delegate.didPressReplyMessage(this, currentMessageObject.getReplyMsgId());
                /*TLRPC.KeyboardButton gameButton = null;
                MessageObject messageObject = currentMessageObject.replyMessageObject;
                if (messageObject != null && messageObject.messageOwner.reply_markup != null) {
                    for (int a = 0; a < messageObject.messageOwner.reply_markup.rows.size(); a++) {
                        TLRPC.TL_keyboardButtonRow row = messageObject.messageOwner.reply_markup.rows.get(a);
                        for (int b = 0; b < row.buttons.size(); b++) {
                            TLRPC.KeyboardButton button = row.buttons.get(b);
                            if (button instanceof TLRPC.TL_keyboardButtonGame && button.game_id == currentMessageObject.messageOwner.action.game_id) {
                                gameButton = button;
                                break;
                            }
                        }
                        if (gameButton != null) {
                            break;
                        }
                    }
                }
                if (gameButton != null) {
                    delegate.didPressBotButton(messageObject, gameButton);
                }*/
            } else if (url.startsWith("http")) {
                Browser.openUrl(getContext(), url);
            } else {
                delegate.needOpenUserProfile(Long.parseLong(url));
            }
        }
    }

    private int overriddenMaxWidth;
    public void setOverrideTextMaxWidth(int width) {
        overriddenMaxWidth = width;
    }

    private boolean isMessageActionSuggestedPostApproval() {
        return currentMessageObject != null
            && currentMessageObject.messageOwner != null
            && currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionSuggestedPostApproval;
    }

    private void createLayout(CharSequence text, int width) {
        int maxWidth = width - dp(30);
        if (isSideMenued) {
            maxWidth -= dp(64);
        }
        if (isMessageActionSuggestedPostApproval()) {
            maxWidth -= dp(isSideMenued ? 28 : 82);
            maxWidth = Math.min(maxWidth, dp(272));
        }
        if (maxWidth < 0) {
            return;
        }
        if (overriddenMaxWidth > 0) {
            maxWidth = Math.min(overriddenMaxWidth, maxWidth);
        }
        invalidatePath = true;
        TextPaint paint;
        if (isMessageActionSuggestedPostApproval()) {
            paint = (TextPaint) getThemedPaint(Theme.key_paint_chatActionText3);
        } else if (currentMessageObject != null && currentMessageObject.drawServiceWithDefaultTypeface) {
            paint = (TextPaint) getThemedPaint(Theme.key_paint_chatActionText2);
        } else {
            paint = (TextPaint) getThemedPaint(Theme.key_paint_chatActionText);
        }
        paint.linkColor = paint.getColor();

        if (isMessageActionSuggestedPostApproval()) {
            if (text instanceof Spannable) {
                Spannable spannable = (Spannable) text;
                Emoji.EmojiSpan[] spans = spannable.getSpans(0, spannable.length(), Emoji.EmojiSpan.class);
                for (Object span : spans) {
                    spannable.removeSpan(span);
                }
            }

            text = Emoji.replaceEmoji(text, paint.getFontMetricsInt(), false, null,
                DynamicDrawableSpan.ALIGN_BOTTOM, 0.85f);
        }

        textLayout = new StaticLayout(text, paint, maxWidth,
            isMessageActionSuggestedPostApproval() ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_CENTER,
        1.0f, 0.0f, false);

        titleLayout = null;

        if (currentMessageObject != null
            && currentMessageObject.messageOwner != null
            && currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionSuggestedPostApproval
            && !((TLRPC.TL_messageActionSuggestedPostApproval) currentMessageObject.messageOwner.action).rejected
            && !((TLRPC.TL_messageActionSuggestedPostApproval) currentMessageObject.messageOwner.action).balance_too_low
        ) {
            final CharSequence title = Emoji.replaceEmoji(
                AndroidUtilities.replaceTags(LocaleController.getString(R.string.SuggestionAgreementReached)),
                paint.getFontMetricsInt(), false, null, DynamicDrawableSpan.ALIGN_BOTTOM, 1);

            titleLayout = new StaticLayout(title, paint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        }

        animatedEmojiStack = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, this, canDrawInParent && (delegate != null && !delegate.canDrawOutboundsContent()), animatedEmojiStack, textLayout);

        textHeight = 0;
        textWidth = 0;
        titleHeight = 0;
        if (titleLayout != null) {
            titleHeight = titleLayout.getHeight();
            titleHeight += dp(12);
        }

        if (currentMessageObject == null || !currentMessageObject.isRepostPreview) {
            try {
                int linesCount = textLayout.getLineCount();
                for (int a = 0; a < linesCount; a++) {
                    float lineWidth;
                    try {
                        lineWidth = textLayout.getLineWidth(a);
                        if (lineWidth > maxWidth) {
                            lineWidth = maxWidth;
                        }
                        textHeight = (int) Math.max(textHeight, Math.ceil(textLayout.getLineBottom(a)));
                    } catch (Exception e) {
                        FileLog.e(e);
                        return;
                    }
                    textWidth = (int) Math.max(textWidth, Math.ceil(lineWidth));
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        textX = (width - textWidth) / 2;
        textY = dp(7);

        if (titleLayout != null) {
            textY += titleHeight + dp(11);
        }

        textXLeft = isMessageActionSuggestedPostApproval() ? ((width - textWidth) / 2) : ((width - textLayout.getWidth()) / 2);
        titleXLeft = (width - maxWidth) / 2;

        spoilersPool.addAll(spoilers);
        spoilers.clear();
        if (text instanceof Spannable) {
            SpoilerEffect.addSpoilers(this, textLayout, textX, textX + textWidth, (Spannable) text, spoilersPool, spoilers, null);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        MessageObject messageObject = currentMessageObject;
        if (messageObject == null && customText == null) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), topicSeparatorTopPadding + textHeight + dp(14));
            return;
        }
        if (isButtonLayout(messageObject)) {
            giftRectSize = Math.min((int) (AndroidUtilities.isTablet() ? AndroidUtilities.getMinTabletSide() * 0.6f : AndroidUtilities.displaySize.x * 0.62f - dp(34)), AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.statusBarHeight - dp(64));
            if (!AndroidUtilities.isTablet() && (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM || messageObject.type == MessageObject.TYPE_GIFT_STARS || isMessageActionSuggestedPostApproval())) {
                giftRectSize = (int) (giftRectSize * 1.2f);
            }
            stickerSize = giftRectSize - dp(106);
            if (isNewStyleButtonLayout()) {
                imageReceiver.setRoundRadius(stickerSize / 2);
            } else {
                imageReceiver.setRoundRadius(0);
            }
        }
        int width = Math.max(dp(30), MeasureSpec.getSize(widthMeasureSpec));
        if (previousWidth != width) {
            wasLayout = true;
            previousWidth = width;
            buildLayout();
        }
        int additionalHeight = 0;
        if (messageObject != null) {
            if (messageObject.type == MessageObject.TYPE_ACTION_PHOTO) {
                additionalHeight = AndroidUtilities.roundMessageSize + dp(10);
            } else if (isButtonLayout(messageObject)) {
                additionalHeight = giftRectSize + dp(12);
            }
        }

        int exactlyHeight = 0;
        if (starGiftLayout.has()) {
            if (!starGiftLayout.repost) {
                exactlyHeight += textY + textHeight + dp(4 + 6 + 6);
            }
            exactlyHeight += (int) starGiftLayout.getHeight() + dp(4 + 4);
            if (!reactionsLayoutInBubble.isEmpty) {
                reactionsLayoutInBubble.totalHeight = reactionsLayoutInBubble.height + dp(8);
                exactlyHeight += reactionsLayoutInBubble.totalHeight;
            }
        } else if (isButtonLayout(messageObject)) {
            boolean isGiftChannel = isGiftChannel(messageObject);
            int imageSize = getImageSize(messageObject);
            float y;
            if (isNewStyleButtonLayout()) {
                y = textY + textHeight + dp(4) + (imageSize > 0 ? (dp(16) * 2 + imageSize) : dp(16)) + (giftPremiumText == null ? 0 : giftPremiumText.layout.getHeight() + dp(4));
            } else {
                y = textY + textHeight + giftRectSize * 0.075f + imageSize + dp(4) + (giftPremiumText == null ? 0 : giftPremiumText.layout.getHeight() + dp(4));
            }
            giftPremiumAdditionalHeight = 0;
            if (giftPremiumTitleLayout != null) {
                y += giftPremiumTitleLayout.getHeight();
                if (giftPremiumTitleLayout.getLineCount() > 1) {
                    giftPremiumAdditionalHeight += giftPremiumTitleLayout.getHeight() - giftPremiumTitleLayout.getLineTop(1);
                }
                y += dp(isGiftChannel ? 6 : 0);
                if (giftPremiumSubtitleLayout != null) {
                    y += giftPremiumSubtitleLayout.getHeight() + dp(9);
                }
            } else {
                y -= dp(12);
                giftPremiumAdditionalHeight -= dp(30);
            }

            int giftTextHeight = giftPremiumText == null ? 0 : giftPremiumText.layout.getHeight();
            if (giftPremiumText == null) {
                giftPremiumAdditionalHeight = 0;
            } else if (giftPremiumSubtitleLayout != null) {
                giftPremiumAdditionalHeight += giftTextHeight + dp(10);
            } else if (currentMessageObject.type == MessageObject.TYPE_GIFT_PREMIUM || currentMessageObject.isStarGiftAction()) {
                giftPremiumAdditionalHeight += giftTextHeight - dp(giftPremiumButtonLayout == null ? 0 : 10);
            } else if (currentMessageObject.type == MessageObject.TYPE_GIFT_STARS) {
                giftPremiumAdditionalHeight += giftTextHeight - dp(20);
            } else if (giftPremiumTextCollapsed) {
                giftPremiumAdditionalHeight += giftTextHeight;
            } else if (giftPremiumText.layout.getLineCount() > 2) {
                giftPremiumAdditionalHeight += (giftPremiumText.layout.getLineBottom(0) - giftPremiumText.layout.getLineTop(0)) * giftPremiumText.layout.getLineCount() - 2;
            }

            giftPremiumAdditionalHeight -= dp(isGiftChannel ? 14 : 0);

            additionalHeight += giftPremiumAdditionalHeight;

            int h = textHeight + additionalHeight + dp(14);

            if (giftPremiumButtonLayout != null) {
                y += (h - y - (giftPremiumButtonLayout != null ? giftPremiumButtonLayout.getHeight() : 0) - dp(8)) / 2f;
                if (currentMessageObject.isStarGiftAction()) {
                    y += dp(4);
                }
                float rectX = (previousWidth - giftPremiumButtonWidth) / 2f;
                giftButtonRect.set(rectX - dp(18), y - dp(8), rectX + giftPremiumButtonWidth + dp(18), y + (giftPremiumButtonLayout != null ? giftPremiumButtonLayout.getHeight() : 0) + dp(8));
            } else {
                additionalHeight -= dp(40);
                giftPremiumAdditionalHeight -= dp(40);
                if (currentMessageObject != null && currentMessageObject.messageOwner != null && currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGift) {
                    additionalHeight -= dp(8);
                    giftPremiumAdditionalHeight -= dp(8);
                }
            }
            int sizeInternal = getMeasuredWidth() << 16 + getMeasuredHeight();
            starParticlesDrawable.rect.set(giftButtonRect);
            starParticlesDrawable.rect2.set(giftButtonRect);
            if (starsSize != sizeInternal) {
                starsSize = sizeInternal;
                starParticlesDrawable.resetPositions();
            }

            if (isNewStyleButtonLayout()) {
                exactlyHeight = textY + textHeight + dp(4);
                backgroundRectHeight = 0;
                backgroundRectHeight += (imageSize > 0 ? (dp(16) * 2 + imageSize) : dp(16));
                if (giftPremiumSubtitleLayout != null) {
                    backgroundRectHeight += giftPremiumSubtitleLayout.getHeight() + dp(10);
                }
                backgroundRectHeight += giftTextHeight;
                float rectX = (previousWidth - giftPremiumButtonWidth) / 2f;
                if (giftPremiumButtonLayout != null) {
                    backgroundButtonTop = exactlyHeight + backgroundRectHeight + dp(10);
                    giftButtonRect.set(rectX - dp(18), backgroundButtonTop, rectX + giftPremiumButtonWidth + dp(18), backgroundButtonTop + giftPremiumButtonLayout.getHeight() + dp(8) * 2);
                    backgroundRectHeight += dp(10) + giftButtonRect.height();
                } else {
                    if (!isMessageActionSuggestedPostApproval()) {
                        giftButtonRect.set(rectX - dp(18), backgroundButtonTop, rectX + giftPremiumButtonWidth + dp(18), backgroundButtonTop + dp(17) + dp(8) * 2);
                        backgroundRectHeight += dp(17);
                    }
                }
                backgroundRectHeight += dp(16);
                exactlyHeight += backgroundRectHeight;
                exactlyHeight += dp(6);
                if (!reactionsLayoutInBubble.isEmpty) {
                    reactionsLayoutInBubble.totalHeight = reactionsLayoutInBubble.height + dp(8);
                    exactlyHeight += reactionsLayoutInBubble.totalHeight;
                }
            }
        }
        if (currentMessageObject != null && !reactionsLayoutInBubble.isEmpty) {
            reactionsLayoutInBubble.totalHeight = reactionsLayoutInBubble.height + dp(8);
            additionalHeight += reactionsLayoutInBubble.totalHeight;
        }

        if (isMessageActionSuggestedPostApproval()) {
            additionalHeight += titleHeight + dp(24);
        }

        if (messageObject != null && isNewStyleButtonLayout()) {
            setMeasuredDimension(width, topicSeparatorTopPadding + exactlyHeight);
        } else {
            setMeasuredDimension(width, topicSeparatorTopPadding + textHeight + additionalHeight + dp(14));
        }
        reactionsLayoutInBubble.y = getMeasuredHeight() - getPaddingTop() - reactionsLayoutInBubble.totalHeight;
    }

    private boolean isNewStyleButtonLayout() {
        return starGiftLayout.has()
            || currentMessageObject.type == MessageObject.TYPE_SUGGEST_PHOTO
            || currentMessageObject.type == MessageObject.TYPE_ACTION_WALLPAPER
            || currentMessageObject.isStoryMention()
            || (currentMessageObject.messageOwner != null && currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionSuggestedPostApproval && (((TLRPC.TL_messageActionSuggestedPostApproval) currentMessageObject.messageOwner.action).balance_too_low || ((TLRPC.TL_messageActionSuggestedPostApproval) currentMessageObject.messageOwner.action).rejected));
    }

    private int getImageSize(MessageObject messageObject) {
        int imageSize = stickerSize;
        if (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO || isNewStyleButtonLayout()) {
            imageSize = dp(78);//Math.max(, (int) (stickerSize * 0.7f));
        }
        if (isMessageActionSuggestedPostApproval()) {
            imageSize = 0;
        }
        return imageSize;
    }

    private ColoredImageSpan upgradeIconSpan;

    private void buildLayout() {
        giftRectEmpty = false;

        CharSequence text = null;
        MessageObject messageObject = currentMessageObject;
        if (messageObject != null) {
            if (messageObject.isExpiredStory()) {
                long dialogId = messageObject.messageOwner.media.user_id;
                if (dialogId != UserConfig.getInstance(currentAccount).getClientUserId()) {
                    text = StoriesUtilities.createExpiredStoryString(true, R.string.ExpiredStoryMention);
                } else {
                    text = StoriesUtilities.createExpiredStoryString(true, R.string.ExpiredStoryMentioned, MessagesController.getInstance(currentAccount).getUser(messageObject.getDialogId()).first_name);
                }
            } else if (delegate != null && delegate.getTopicId() == 0 && MessageObject.isTopicActionMessage(messageObject)) {
                TLRPC.TL_forumTopic topic = MessagesController.getInstance(currentAccount).getTopicsController().findTopic(-messageObject.getDialogId(), MessageObject.getTopicId(currentAccount, messageObject.messageOwner, true));
                text = ForumUtilities.createActionTextWithTopic(topic, messageObject);
            }
            if (text == null) {
                if (messageObject.messageOwner != null && messageObject.messageOwner.media != null && messageObject.messageOwner.media.ttl_seconds != 0) {
                    if (messageObject.messageOwner.media.photo != null) {
                        text = getString(R.string.AttachPhotoExpired);
                    } else if (messageObject.messageOwner.media.document instanceof TLRPC.TL_documentEmpty || messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument && messageObject.messageOwner.media.document == null) {
                        if (messageObject.messageOwner.media.voice) {
                            text = getString(R.string.AttachVoiceExpired);
                        } else if (messageObject.messageOwner.media.round) {
                            text = getString(R.string.AttachRoundExpired);
                        } else {
                            text = getString(R.string.AttachVideoExpired);
                        }
                    } else {
                        text = AnimatedEmojiSpan.cloneSpans(messageObject.messageText);
                    }
                } else {
                    text = AnimatedEmojiSpan.cloneSpans(messageObject.messageText);
                }
            }
        } else {
            text = customText;
        }
        if (currentMessageObject != null && currentMessageObject.isRepostPreview) {
            text = "";
        }
        if (currentMessageObject != null && currentMessageObject.messageOwner != null && currentMessageObject.messageOwner.action != null) {
            int iconResId = 0;
            if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionTodoAppendTasks) {
                iconResId = R.drawable.mini_checklist_add;
            } else if (currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionTodoCompletions) {
                final TLRPC.TL_messageActionTodoCompletions action = (TLRPC.TL_messageActionTodoCompletions) currentMessageObject.messageOwner.action;
                if (action.incompleted.size() > action.completed.size()) {
                    iconResId = R.drawable.mini_checklist_undone;
                } else {
                    iconResId = R.drawable.mini_checklist_done;
                }
            }
            if (iconResId != 0) {
                text = new SpannableStringBuilder(text);
                ((SpannableStringBuilder) text).insert(0, "i ");
                ((SpannableStringBuilder) text).setSpan(new ColoredImageSpan(iconResId), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        createLayout(text, previousWidth);
        if (messageObject != null) {
            if (messageObject.messageOwner != null && messageObject.messageOwner.action instanceof TLRPC.TL_messageActionSuggestedPostApproval && ((TLRPC.TL_messageActionSuggestedPostApproval) messageObject.messageOwner.action).balance_too_low) {
                final TLRPC.TL_messageActionSuggestedPostApproval approval = (TLRPC.TL_messageActionSuggestedPostApproval) messageObject.messageOwner.action;
                final String action = (!ChatObject.canManageMonoForum(currentAccount, messageObject.getDialogId())) ?
                    getString(R.string.StarsBuy) : null;

                createGiftPremiumLayouts(null, null, text, false, action, 11, null, giftRectSize, false, true);
                textLayout = null;
                textHeight = 0;
                titleLayout = null;
                titleHeight = 0;
                textY = 0;
                giftRectEmpty = true;
            } else if (messageObject.messageOwner != null && messageObject.messageOwner.action instanceof TLRPC.TL_messageActionSuggestedPostApproval && ((TLRPC.TL_messageActionSuggestedPostApproval) messageObject.messageOwner.action).rejected) {
                final TLRPC.TL_messageActionSuggestedPostApproval approval = (TLRPC.TL_messageActionSuggestedPostApproval) messageObject.messageOwner.action;

                createGiftPremiumLayouts(null, null, text, false, null, 11, null, giftRectSize, false, true);
                textLayout = null;
                textHeight = 0;
                titleLayout = null;
                titleHeight = 0;
                textY = 0;
                giftRectEmpty = true;

            } else if (messageObject.type == MessageObject.TYPE_ACTION_PHOTO) {
                imageReceiver.setImageCoords((previousWidth - AndroidUtilities.roundMessageSize) / 2f, textHeight + dp(19), AndroidUtilities.roundMessageSize, AndroidUtilities.roundMessageSize);
            } else if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL) {
                createGiftPremiumChannelLayouts();
            } else if (messageObject.type == MessageObject.TYPE_GIFT_STARS) {
                final TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(currentMessageObject.getDialogId());
                final long stars;
                if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftStars) {
                    stars = ((TLRPC.TL_messageActionGiftStars) messageObject.messageOwner.action).stars;
                    createGiftPremiumLayouts(
                        formatPluralStringComma("ActionGiftStarsTitle", (int) stars),
                        null,
                        AndroidUtilities.replaceTags(currentMessageObject.isOutOwner() ? formatString(R.string.ActionGiftStarsSubtitle, UserObject.getForcedFirstName(user)) : getString(R.string.ActionGiftStarsSubtitleYou)),
                        false, getString(R.string.ActionGiftStarsView),
                            11, null, giftRectSize,
                        true,
                            false);
                } else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGiftUnique && ((TLRPC.TL_messageActionStarGiftUnique) messageObject.messageOwner.action).refunded) {
                    final long selfId = UserConfig.getInstance(currentAccount).getClientUserId();
                    final TLRPC.TL_messageActionStarGiftUnique action = (TLRPC.TL_messageActionStarGiftUnique) messageObject.messageOwner.action;
                    final long fromId = messageObject.isOutOwner() == !action.upgrade ? selfId : messageObject.getDialogId();
                    final TLRPC.User fromUser = MessagesController.getInstance(currentAccount).getUser(fromId);
                    final SpannableStringBuilder sb = new SpannableStringBuilder();
                    sb.append(LocaleController.getString(R.string.Gift2ActionTitle)).append(" ");
                    if (fromUser != null && fromUser.photo != null) {
                        sb.append("a ");
                        final AvatarSpan avatarSpan = new AvatarSpan(this, currentAccount, 18);
                        avatarSpan.setUser(fromUser);
                        sb.setSpan(avatarSpan, sb.length() - 2, sb.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    sb.append(UserObject.getForcedFirstName(fromUser));
                    createGiftPremiumLayouts(
                        sb,
                        null,
                        getString(R.string.Gift2ActionUpgradeRefundedText),
                        false,
                        getString(R.string.ActionGiftStarsView),
                        12, getString(R.string.Gift2UniqueRibbon),
                        giftRectSize,
                        true,
                            false);
                } else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGift) {
                    final TLRPC.TL_messageActionStarGift action = (TLRPC.TL_messageActionStarGift) messageObject.messageOwner.action;
                    stars = action.convert_stars;
                    final long selfId = UserConfig.getInstance(currentAccount).getClientUserId();
                    final boolean isForChannel = action.peer != null;
                    final boolean self = messageObject.getDialogId() == selfId && !isForChannel;
                    long fromId = messageObject.getFromChatId();
                    if (action.from_id != null) {
                        fromId = DialogObject.getPeerDialogId(action.from_id);
                    }
                    final SpannableStringBuilder sb = new SpannableStringBuilder();
                    final TLObject sender = MessagesController.getInstance(currentAccount).getUserOrChat(fromId);
                    final boolean freeUpgrade = action.can_upgrade && !action.converted && action.upgrade_stars > 0 && !action.upgraded;
                    if (self) {
                        sb.append(getString(R.string.Gift2ActionSelfTitle));
                    } else {
                        sb.append(LocaleController.getString(R.string.Gift2ActionTitle)).append(" ");
                        if (DialogObject.hasPhoto(sender)) {
                            sb.append("a ");
                            final AvatarSpan avatarSpan = new AvatarSpan(this, currentAccount, 18);
                            avatarSpan.setObject(sender);
                            sb.setSpan(avatarSpan, sb.length() - 2, sb.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        sb.append(DialogObject.getShortName(sender));
                    }
                    final int within = MessagesController.getInstance(currentAccount).stargiftsConvertPeriodMax - (ConnectionsManager.getInstance(currentAccount).getCurrentTime() - messageObject.messageOwner.date);
                    final boolean canConvert = (messageObject.isOutOwner() && !self || !action.converted) && action.convert_stars > 0 && within > 0 && !action.refunded;
                    CharSequence title;
                    if (action.refunded) {
                        title = getString(R.string.Gift2ActionConvertRefundedText);
                    } else if (action.message != null && !TextUtils.isEmpty(action.message.text)) {
                        title = new SpannableStringBuilder(action.message.text);
                        giftTextPaint.setTextSize(dp(13));
                        MessageObject.addEntitiesToText(title, action.message.entities, false, false, true, true);
                        title = Emoji.replaceEmoji(title, giftTextPaint.getFontMetricsInt(), false, null);
                        title = MessageObject.replaceAnimatedEmoji(title, action.message.entities, giftTextPaint.getFontMetricsInt());
                    } else if (isForChannel) {
                        if (action.converted) {
                            title = formatPluralStringComma("Gift2ActionConvertedInfo", (int) stars);
                        } else if (canConvert && stars > 0) {
                            title = AndroidUtilities.replaceTags(formatPluralStringComma("Gift2ActionInfoChannel", (int) stars));
                        } else {
                            title = AndroidUtilities.replaceTags(getString(R.string.Gift2ActionInfoChannelNoConvert));
                        }
                    } else if (self) {
                        if (action.converted && stars > 0) {
                            title = formatPluralStringComma("Gift2ActionConvertedInfo", (int) stars);
                        } else if (action.can_upgrade) {
                            title = AndroidUtilities.replaceTags(getString(R.string.Gift2ActionSelfInfoUpgrade));
                        } else {
                            title = AndroidUtilities.replaceTags(getString(R.string.Gift2ActionSelfInfoNoConvert));
                        }
                    } else if (freeUpgrade) {
                        title = AndroidUtilities.replaceTags(messageObject.isOutOwner() ? formatString(R.string.Gift2ActionUpgradeOut, UserObject.getForcedFirstName(user)) : getString(R.string.Gift2ActionUpgrade));
                    } else if (messageObject.isOutOwner()) {
                        if (canConvert && stars > 0) {
                            title = AndroidUtilities.replaceTags(formatPluralStringComma("Gift2ActionOutInfo", (int) stars, UserObject.getForcedFirstName(user)));
                        } else if (action.can_upgrade) {
                            title = AndroidUtilities.replaceTags(formatString(R.string.Gift2ActionOutInfoUpgrade, UserObject.getForcedFirstName(user)));
                        } else {
                            title = AndroidUtilities.replaceTags(formatString(R.string.Gift2ActionOutInfoNoConvert, UserObject.getForcedFirstName(user)));
                        }
                    } else {
                        if (action.converted) {
                            title = formatPluralStringComma("Gift2ActionConvertedInfo", (int) stars);
                        } else if (action.saved) {
                            if (!canConvert) {
                                title = getString(R.string.Gift2ActionBotSavedInfo);
                            } else {
                                title = getString(R.string.Gift2ActionSavedInfo);
                            }
                        } else if (!canConvert) {
                            title = getString(R.string.Gift2ActionBotInfo);
                        } else {
                            title = AndroidUtilities.replaceTags(formatPluralStringComma("Gift2ActionInfo", (int) stars));
                        }
                    }
                    CharSequence ribbon = null;
                    if (action.gift != null && action.gift.limited) {
                        ribbon = LocaleController.formatString(R.string.Gift2Limited1OfRibbon, action.gift.availability_total > 1500 ? AndroidUtilities.formatWholeNumber(action.gift.availability_total, 0) : action.gift.availability_total);
                    }
                    CharSequence buttonText = getString(R.string.ActionGiftStarsView);
                    if (!messageObject.isOutOwner() || action.forceIn || freeUpgrade) {
                        if (!messageObject.isOutOwner() && freeUpgrade) {
                            SpannableStringBuilder ssb = new SpannableStringBuilder();
                            ssb.append("^  ");
                            ColoredImageSpan span = new ColoredImageSpan(R.drawable.gift_unpack);
                            span.setScale(.8f, .8f);
                            ssb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ssb.append(LocaleController.getString(R.string.Gift2Unpack));
                            buttonText = ssb;
                        }
                    }
                    createGiftPremiumLayouts(
                        sb,
                        null,
                        title,
                        false,
                        buttonText,
                        11, ribbon,
                        giftRectSize,
                        true,
                            false);
                } else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftTon) {
                    createGiftPremiumLayouts(
                        getString(R.string.ActionGiftTonTitle),
                        null,
                        currentMessageObject.messageText,
                        false, getString(R.string.ActionGiftStarsView),
                        11, null,
                        giftRectSize,
                        true,
                        false);
                    textLayout = null;
                    textHeight = 0;
                    titleLayout = null;
                    titleHeight = 0;
                    textY = 0;
                } else {
                    createGiftPremiumLayouts(
                        getString(R.string.ActionStarGiveawayPrizeTitle),
                        null,
                        currentMessageObject.messageText,
                        false, getString(R.string.ActionGiftStarsView),
                            11, null,
                        giftRectSize,
                        true,
                            false);
                    textLayout = null;
                    textHeight = 0;
                    titleLayout = null;
                    titleHeight = 0;
                    textY = 0;
                }
            } else if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM) {
                int months;
                TLRPC.TL_textWithEntities textWithEntities = null;
                if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftPremium) {
                    textWithEntities = ((TLRPC.TL_messageActionGiftPremium) messageObject.messageOwner.action).message;
                } else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionGiftCode) {
                    textWithEntities = ((TLRPC.TL_messageActionGiftCode) messageObject.messageOwner.action).message;
                }
                CharSequence messageText = null;
                if (textWithEntities != null && !TextUtils.isEmpty(textWithEntities.text)) {
                    messageText = new SpannableStringBuilder(textWithEntities.text);
                    giftTextPaint.setTextSize(dp(13));
                    MessageObject.addEntitiesToText(messageText, textWithEntities.entities, false, false, true, true);
                    messageText = Emoji.replaceEmoji(messageText, giftTextPaint.getFontMetricsInt(), false, null);
                    messageText = MessageObject.replaceAnimatedEmoji(messageText, textWithEntities.entities, giftTextPaint.getFontMetricsInt());
                }
                if (messageText == null) {
                    messageText = LocaleController.getString(R.string.ActionGiftPremiumText);
                }
                String actionName = getString(isGiftCode() && !isSelfGiftCode() ? R.string.GiftPremiumUseGiftBtn : R.string.ActionGiftPremiumView);
                createGiftPremiumLayouts(formatPluralStringComma("ActionGiftPremiumTitle2", messageObject.messageOwner.action.months), null, messageText, true, actionName, 11, null, giftRectSize, false, false);
            } else if (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO) {
                TLRPC.TL_messageActionSuggestProfilePhoto actionSuggestProfilePhoto = (TLRPC.TL_messageActionSuggestProfilePhoto) messageObject.messageOwner.action;
                String description;
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(messageObject.isOutOwner() ? 0 : messageObject.getDialogId());
                boolean isVideo = actionSuggestProfilePhoto.video || (actionSuggestProfilePhoto.photo != null && actionSuggestProfilePhoto.photo.video_sizes != null && !actionSuggestProfilePhoto.photo.video_sizes.isEmpty());
                if (user.id == UserConfig.getInstance(currentAccount).clientUserId) {
                    TLRPC.User user2 = MessagesController.getInstance(currentAccount).getUser(messageObject.getDialogId());
                    if (isVideo) {
                        description = formatString(R.string.ActionSuggestVideoFromYouDescription, user2.first_name);
                    } else {
                        description = formatString(R.string.ActionSuggestPhotoFromYouDescription, user2.first_name);
                    }
                } else {
                    if (isVideo) {
                        description = formatString(R.string.ActionSuggestVideoToYouDescription, user.first_name);
                    } else {
                        description = formatString(R.string.ActionSuggestPhotoToYouDescription, user.first_name);
                    }
                }
                String action;
                if (actionSuggestProfilePhoto.video || (actionSuggestProfilePhoto.photo.video_sizes != null && !actionSuggestProfilePhoto.photo.video_sizes.isEmpty())) {
                    action = getString(R.string.ViewVideoAction);
                } else {
                    action = getString(R.string.ViewPhotoAction);
                }
                createGiftPremiumLayouts(null, null, description, false, action, 11, null, giftRectSize, true, false);
                textLayout = null;
                textHeight = 0;
                titleLayout = null;
                titleHeight = 0;
                textY = 0;
            } else if (messageObject.type == MessageObject.TYPE_ACTION_WALLPAPER) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(messageObject.isOutOwner() ? 0 : messageObject.getDialogId());
                CharSequence description;
                String action = null;
                boolean actionClickableAsImage = true;
                if (messageObject.getDialogId() < 0) {
                    description = messageObject.messageText;
                } else if (!messageObject.isOutOwner() && messageObject.isWallpaperForBoth() && messageObject.isCurrentWallpaper()) {
                    description = messageObject.messageText;
                    action = getString(R.string.RemoveWallpaperAction);
                    actionClickableAsImage = false;
                } else if (user != null && user.id == UserConfig.getInstance(currentAccount).clientUserId) {
                    description = messageObject.messageText;
                } else {
                    description = messageObject.messageText;
                    action = getString(R.string.ViewWallpaperAction);
                }
                createGiftPremiumLayouts(null, null, description, false, action, 11, null, giftRectSize, actionClickableAsImage, false);
                textLayout = null;
                textHeight = 0;
                titleLayout = null;
                titleHeight = 0;
                textY = 0;
            } else if (messageObject.isStoryMention()) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(messageObject.messageOwner.media.user_id);
                CharSequence description;
                String action = null;

                if (user.self) {
                    TLRPC.User user2 = MessagesController.getInstance(currentAccount).getUser(messageObject.getDialogId());
                    description = AndroidUtilities.replaceTags(formatString("StoryYouMentionedTitle", R.string.StoryYouMentionedTitle, user2.first_name));
                } else {
                    description = AndroidUtilities.replaceTags(formatString("StoryMentionedTitle", R.string.StoryMentionedTitle, user.first_name));
                }
                action = getString(R.string.StoryMentionedAction);

                createGiftPremiumLayouts(null, null, description, false, action, 11, null, giftRectSize, true, false);
                textLayout = null;
                textHeight = 0;
                titleLayout = null;
                titleHeight = 0;
                textY = 0;
            }
        }
        reactionsLayoutInBubble.x = dp(12);
        reactionsLayoutInBubble.measure(previousWidth - dp(24), Gravity.CENTER_HORIZONTAL);
    }

    private void createGiftPremiumChannelLayouts() {
        int width = giftRectSize;
        width -= dp(16);
        giftTitlePaint.setTextSize(dp(14));
        giftTextPaint.setTextSize(dp(13));
        TLRPC.TL_messageActionGiftCode gifCodeAction = (TLRPC.TL_messageActionGiftCode) currentMessageObject.messageOwner.action;
        int months = gifCodeAction.months;
        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-DialogObject.getPeerDialogId(gifCodeAction.boost_peer));
        String from = chat == null ? null : chat.title;
        boolean isPrize = gifCodeAction.via_giveaway;
        CharSequence title = gifCodeAction.unclaimed ?
                getString("BoostingUnclaimedPrize", R.string.BoostingUnclaimedPrize)
                : getString("BoostingCongratulations", R.string.BoostingCongratulations);
        SpannableStringBuilder subtitle;
        CharSequence monthsStr = months == 12 ? LocaleController.formatPluralString("BoldYears", 1) : LocaleController.formatPluralString("BoldMonths", months);
        if (isPrize) {
            if (gifCodeAction.unclaimed) {
                subtitle = new SpannableStringBuilder(AndroidUtilities.replaceTags(formatString("BoostingYouHaveUnclaimedPrize", R.string.BoostingYouHaveUnclaimedPrize, from)));
                subtitle.append("\n\n");
                subtitle.append(AndroidUtilities.replaceTags(formatString("BoostingUnclaimedPrizeDuration", R.string.BoostingUnclaimedPrizeDuration, monthsStr)));
            } else {
                subtitle = new SpannableStringBuilder(AndroidUtilities.replaceTags(formatString("BoostingReceivedPrizeFrom", R.string.BoostingReceivedPrizeFrom, from)));
                subtitle.append("\n\n");
                subtitle.append(AndroidUtilities.replaceTags(formatString("BoostingReceivedPrizeDuration", R.string.BoostingReceivedPrizeDuration, monthsStr)));
            }
        } else {
            subtitle = new SpannableStringBuilder(AndroidUtilities.replaceTags(from == null ? getString("BoostingReceivedGiftNoName", R.string.BoostingReceivedGiftNoName) : formatString("BoostingReceivedGiftFrom", R.string.BoostingReceivedGiftFrom, from)));
            subtitle.append("\n\n");
            subtitle.append(AndroidUtilities.replaceTags(formatString("BoostingReceivedGiftDuration", R.string.BoostingReceivedGiftDuration, monthsStr)));
        }

        String btnText = getString("BoostingReceivedGiftOpenBtn", R.string.BoostingReceivedGiftOpenBtn);

        SpannableStringBuilder titleBuilder = SpannableStringBuilder.valueOf(title);
        titleBuilder.setSpan(new TypefaceSpan(AndroidUtilities.bold()), 0, titleBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        giftPremiumTitleLayout = new StaticLayout(titleBuilder, giftTitlePaint, width, Layout.Alignment.ALIGN_CENTER, 1.1f, 0.0f, false);
        giftPremiumSubtitleLayout = null;

        if (giftPremiumText != null) {
            giftPremiumText.detach();
        }
        giftPremiumText = new TextLayout();
        giftPremiumText.setText(subtitle, giftTextPaint, width);
        SpannableStringBuilder buttonBuilder = SpannableStringBuilder.valueOf(btnText);
        buttonBuilder.setSpan(new TypefaceSpan(AndroidUtilities.bold()), 0, buttonBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        giftPremiumTextCollapsed = false;
        giftPremiumTextCollapsedHeight = 0;
        giftPremiumTextMore = null;

        giftPremiumButtonLayout = new StaticLayout(buttonBuilder, (TextPaint) getThemedPaint(Theme.key_paint_chatActionText), width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        buttonClickableAsImage = true;
        giftPremiumButtonWidth = measureLayoutWidth(giftPremiumButtonLayout);
    }

    private void createGiftPremiumLayouts(CharSequence title, CharSequence subtitle, CharSequence text, boolean allowCollapsing, CharSequence button, int ribbonTextDp, CharSequence ribbon, int width, boolean buttonClickableAsImage, boolean hideImage) {
        width -= dp(16);
        if (currentMessageObject != null && currentMessageObject.type == MessageObject.TYPE_GIFT_STARS) {
            width -= dp(16);
        }
        if (title != null) {
            if (currentMessageObject != null && currentMessageObject.type == MessageObject.TYPE_GIFT_STARS) {
                giftTitlePaint.setTextSize(dp(14));
            } else {
                giftTitlePaint.setTextSize(dp(16));
            }
            SpannableStringBuilder titleBuilder = SpannableStringBuilder.valueOf(title);
            titleBuilder.setSpan(new TypefaceSpan(AndroidUtilities.bold()), 0, titleBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            giftPremiumTitleLayout = new StaticLayout(titleBuilder, giftTitlePaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        } else {
            giftPremiumTitleLayout = null;
        }
        if (subtitle != null) {
            giftSubtitlePaint.setTextSize(dp(13));
            giftPremiumSubtitleLayout = new StaticLayout(subtitle, giftSubtitlePaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        } else {
            giftPremiumSubtitleLayout = null;
        }
        if (currentMessageObject != null && (isNewStyleButtonLayout() || currentMessageObject.type == MessageObject.TYPE_GIFT_STARS || currentMessageObject.type == MessageObject.TYPE_GIFT_PREMIUM)) {
            giftTextPaint.setTextSize(dp(13));
        } else {
            giftTextPaint.setTextSize(dp(15));
        }
        int textWidth = width - dp(12);
        if (currentMessageObject != null && (currentMessageObject.type == MessageObject.TYPE_ACTION_WALLPAPER && currentMessageObject.getDialogId() >= 0)) {
            final int recommendedWidthForTwoLines = HintView2.cutInFancyHalf(text, giftTextPaint);
            if (recommendedWidthForTwoLines < textWidth && recommendedWidthForTwoLines > textWidth / 5f) {
                textWidth = recommendedWidthForTwoLines;
            }
        }
        if (text == null) {
            if (giftPremiumText != null) {
                giftPremiumText.detach();
                giftPremiumText = null;
            }
            giftPremiumTextCollapsed = false;
        } else {
            if (giftPremiumText == null) {
                giftPremiumText = new TextLayout();
            }
            try {
                text = Emoji.replaceEmoji(text, giftTextPaint.getFontMetricsInt(), false);
            } catch (Exception ignore) {
            }
            giftPremiumText.setText(text, giftTextPaint, textWidth);
            if (allowCollapsing && giftPremiumText.layout.getLineCount() > 3) {
                giftPremiumTextCollapsed = !giftPremiumTextUncollapsed;
                giftPremiumTextCollapsedHeight = giftPremiumText.layout.getLineBottom(2);
                giftPremiumTextMore = new Text(LocaleController.getString(R.string.Gift2CaptionMore), giftTextPaint.getTextSize() / AndroidUtilities.density, AndroidUtilities.bold());
                giftPremiumTextMoreY = giftPremiumText.layout.getLineBottom(2);
                giftPremiumTextMoreH = giftPremiumTextMoreY - giftPremiumText.layout.getLineTop(2);
                giftPremiumTextMoreX = (int) giftPremiumText.layout.getLineRight(2);
            } else {
                giftPremiumTextCollapsed = false;
                giftPremiumTextExpandedAnimated.set(true, true);
                giftPremiumTextCollapsedHeight = 0;
            }
            if (giftPremiumTextCollapsed) {
                int index = giftPremiumText.layout.getLineEnd(2) - 1;
                giftPremiumText.setText(text.subSequence(0, index), giftTextPaint, textWidth);
            }
        }
        if (button != null) {
            SpannableStringBuilder buttonBuilder = SpannableStringBuilder.valueOf(button);
            buttonBuilder.setSpan(new TypefaceSpan(AndroidUtilities.bold()), 0, buttonBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            giftPremiumButtonLayout = new StaticLayout(buttonBuilder, (TextPaint) getThemedPaint(Theme.key_paint_chatActionText), width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            this.buttonClickableAsImage = buttonClickableAsImage && !giftPremiumTextCollapsed;
            giftPremiumButtonWidth = measureLayoutWidth(giftPremiumButtonLayout);
        } else {
            giftPremiumButtonLayout = null;
            this.buttonClickableAsImage = false;
            giftPremiumButtonWidth = 0;
        }
        if (ribbon != null) {
            if (giftRibbonPaintEffect == null) {
                giftRibbonPaintEffect = new CornerPathEffect(dp(5));
            }
            if (giftRibbonPath == null) {
                giftRibbonPath = new Path();
                GiftSheet.RibbonDrawable.fillRibbonPath(giftRibbonPath, 1.35f);
            }
            giftRibbonText = new Text(ribbon, ribbonTextDp, AndroidUtilities.bold());
            giftRibbonText.ellipsize(dp(62));
        } else {
            giftRibbonPath = null;
            giftRibbonText = null;
        }
    }

    private float measureLayoutWidth(Layout layout) {
        float maxWidth = 0;
        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineWidth = (int) Math.ceil(layout.getLineWidth(i));
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }
        return maxWidth;
    }

    public boolean showingCancelButton() {
        return radialProgress != null && radialProgress.getIcon() == MediaActionDrawable.ICON_CANCEL;
    }

    public int getCustomDate() {
        return customDate;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(sideMenuWidth / 2f, getPaddingTop());

        MessageObject messageObject = currentMessageObject;
        final float expanded = giftPremiumTextExpandedAnimated.set(!giftPremiumTextCollapsed);
        int imageSize = stickerSize;
        if (!starGiftLayout.has() && isButtonLayout(messageObject)) {
            stickerSize = giftRectSize - dp(106);
            if (isNewStyleButtonLayout()) {
                imageSize = getImageSize(messageObject);
                int top = textY + textHeight + dp(4) + dp(16);
                float x = (previousWidth - imageSize) / 2f;
                float y = top;
                if (messageObject.isStoryMention()) {
                    avatarStoryParams.storyItem = messageObject.messageOwner.media.storyItem;
                }
                avatarStoryParams.originalAvatarRect.set(x, y, x + imageSize, y + imageSize);
                imageReceiver.setImageCoords(x, y, imageSize, imageSize);
            } else if (messageObject.type == MessageObject.TYPE_ACTION_PHOTO) {
                imageReceiver.setImageCoords((previousWidth - stickerSize) / 2f, textY + textHeight + giftRectSize * 0.075f, stickerSize, stickerSize);
            } else if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL) {
                imageSize = (int) (stickerSize * (AndroidUtilities.isTablet() ? 1.0f : 1.2f));
                imageReceiver.setImageCoords((previousWidth - imageSize) / 2f, textY + textHeight + giftRectSize * 0.075f - dp(22), imageSize, imageSize);
            } else if (messageObject.isStarGiftAction()) {
                imageReceiver.setImageCoords((previousWidth - imageSize) / 2f, textY + textHeight + giftRectSize * 0.075f + dp(2), imageSize, imageSize);
            } else if (messageObject.type == MessageObject.TYPE_GIFT_STARS) {
                imageSize = (int) (stickerSize * 1.1f);
                if (messageObject.messageOwner != null && !(messageObject.messageOwner.action instanceof TLRPC.TL_messageActionStarGift)) {
                    imageReceiver.setImageCoords((previousWidth - imageSize) / 2f, textY + textHeight + giftRectSize * 0.075f - dp(22), imageSize, imageSize);
                } else {
                    imageReceiver.setImageCoords((previousWidth - imageSize) / 2f, textY + textHeight + giftRectSize * 0.075f - dp(12), imageSize, imageSize);
                }
            } else {
                imageSize = (int) (stickerSize * 1f);
                imageReceiver.setImageCoords((previousWidth - imageSize) / 2f, textY + textHeight + giftRectSize * 0.075f - dp(4), imageSize, imageSize);
            }
            textPaint = (TextPaint) getThemedPaint(Theme.key_paint_chatActionText);
            if (textPaint != null) {
                if (giftTitlePaint != null && giftTitlePaint.getColor() != textPaint.getColor()) {
                    giftTitlePaint.setColor(textPaint.getColor());
                }
                if (giftSubtitlePaint != null && giftSubtitlePaint.getColor() != textPaint.getColor()) {
                    giftSubtitlePaint.setColor(textPaint.getColor());
                    giftSubtitlePaint.linkColor = textPaint.getColor();
                }
                if (giftTextPaint != null && giftTextPaint.getColor() != textPaint.getColor()) {
                    giftTextPaint.setColor(textPaint.getColor());
                    giftTextPaint.linkColor = textPaint.getColor();
                }
            }
        }

        drawBackground(canvas, false);

        if (starGiftLayout.has()) {
            canvas.save();
            canvas.translate(starGiftLayoutX = (getWidth() - starGiftLayout.getWidth()) / 2.0f, starGiftLayoutY = starGiftLayout.repost ? dp(4) : textY + textHeight + dp(4 + 6 + 6));
            starGiftLayout.draw(canvas);
            if (delegate == null || delegate.canDrawOutboundsContent()) {
                starGiftLayout.drawOutbounds(canvas);
            }
            canvas.restore();
        } else if (isButtonLayout(messageObject) || (messageObject != null && messageObject.type == MessageObject.TYPE_ACTION_PHOTO)) {
            if (wallpaperPreviewDrawable != null) {
                canvas.save();
                canvas.translate(imageReceiver.getImageX(), imageReceiver.getImageY());
                if (clipPath == null) {
                    clipPath = new Path();
                } else {
                    clipPath.rewind();
                }
                clipPath.addCircle(imageReceiver.getImageWidth() / 2f, imageReceiver.getImageHeight() / 2f, imageReceiver.getImageWidth() / 2f, Path.Direction.CW);
                canvas.clipPath(clipPath);
                wallpaperPreviewDrawable.setBounds(0, 0, (int) imageReceiver.getImageWidth(), (int) imageReceiver.getImageHeight());
                wallpaperPreviewDrawable.draw(canvas);
                canvas.restore();
            } else if (messageObject.isStoryMention()) {
                long dialogId = messageObject.messageOwner.media.user_id;
                avatarStoryParams.storyId = messageObject.messageOwner.media.id;
                StoriesUtilities.drawAvatarWithStory(dialogId, canvas, imageReceiver, avatarStoryParams);
             //   imageReceiver.draw(canvas);
            } else {
                imageReceiver.draw(canvas);
            }
            radialProgress.setProgressRect(
                    imageReceiver.getImageX(),
                    imageReceiver.getImageY(),
                    imageReceiver.getImageX() + imageReceiver.getImageWidth(),
                    imageReceiver.getImageY() + imageReceiver.getImageHeight()
            );
            if (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO) {
                ImageUpdater imageUpdater = MessagesController.getInstance(currentAccount).photoSuggestion.get(messageObject.messageOwner.local_id);
                if (imageUpdater != null) {
                    radialProgress.setProgress(imageUpdater.getCurrentImageProgress(), true);
                    radialProgress.setCircleRadius((int) (imageReceiver.getImageWidth() * 0.5f) + 1);
                    radialProgress.setMaxIconSize(dp(24));
                    radialProgress.setColorKeys(Theme.key_chat_mediaLoaderPhoto, Theme.key_chat_mediaLoaderPhotoSelected, Theme.key_chat_mediaLoaderPhotoIcon, Theme.key_chat_mediaLoaderPhotoIconSelected);
                    if (imageUpdater.getCurrentImageProgress() == 1f) {
                        radialProgress.setIcon(MediaActionDrawable.ICON_NONE, true, true);
                    } else {
                        radialProgress.setIcon(MediaActionDrawable.ICON_CANCEL, true, true);
                    }
                }
                radialProgress.draw(canvas);
            } else if (messageObject.type == MessageObject.TYPE_ACTION_WALLPAPER) {
                float progress = getUploadingInfoProgress(messageObject);
                radialProgress.setProgress(progress, true);
                radialProgress.setCircleRadius(dp(26));
                radialProgress.setMaxIconSize(dp(24));
                radialProgress.setColorKeys(Theme.key_chat_mediaLoaderPhoto, Theme.key_chat_mediaLoaderPhotoSelected, Theme.key_chat_mediaLoaderPhotoIcon, Theme.key_chat_mediaLoaderPhotoIconSelected);
                if (progress == 1f) {
                    radialProgress.setIcon(MediaActionDrawable.ICON_NONE, true, true);
                } else {
                    radialProgress.setIcon(MediaActionDrawable.ICON_CANCEL, true, true);
                }
                radialProgress.draw(canvas);
            }
        }

        if (textPaint != null && textLayout != null) {
            canvas.save();
            canvas.translate(textXLeft, textY);
            if (textLayout.getPaint() != textPaint) {
                buildLayout();
            }
            canvas.save();
            SpoilerEffect.clipOutCanvas(canvas, spoilers);
            SpoilerEffect.layoutDrawMaybe(textLayout, canvas);
            if (delegate == null || delegate.canDrawOutboundsContent()) {
                AnimatedEmojiSpan.drawAnimatedEmojis(canvas, textLayout, animatedEmojiStack, 0, spoilers, 0, 0, 0, 1f, textLayout == null ? null : getAdaptiveEmojiColorFilter(textLayout.getPaint().getColor()));
            }
            canvas.restore();

            for (SpoilerEffect eff : spoilers) {
                eff.setColor(textLayout.getPaint().getColor());
                eff.draw(canvas);
            }

            canvas.restore();
        }

        if (textPaint != null && titleLayout != null) {
            canvas.save();
            canvas.translate(titleXLeft, textY - titleHeight);
            if (titleLayout.getPaint() != textPaint) {
                buildLayout();
            }
            canvas.save();
            SpoilerEffect.clipOutCanvas(canvas, spoilers);
            SpoilerEffect.layoutDrawMaybe(titleLayout, canvas);
            if (delegate == null || delegate.canDrawOutboundsContent()) {
                AnimatedEmojiSpan.drawAnimatedEmojis(canvas, titleLayout, animatedEmojiStack, 0, spoilers, 0, 0, 0, 1f, textLayout == null ? null : getAdaptiveEmojiColorFilter(textLayout.getPaint().getColor()));
            }
            canvas.restore();

            for (SpoilerEffect eff : spoilers) {
                eff.setColor(titleLayout.getPaint().getColor());
                eff.draw(canvas);
            }

            canvas.restore();
        }

        if (!starGiftLayout.has() && isButtonLayout(messageObject)) {
            canvas.save();
            float x = (previousWidth - giftRectSize) / 2f;
            if (messageObject.type != MessageObject.TYPE_ACTION_WALLPAPER) {
                x += dp(8);
            }
            float y;
            if (isNewStyleButtonLayout()) {
                float top = backgroundRect != null ? backgroundRect.top : (textY + textHeight + dp(4));
                y = top + (imageSize > 0 ? (dp(16) * 2 + imageSize) : dp(16));
            } else {
                y = textY + textHeight + giftRectSize * 0.075f + (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO ? imageSize : stickerSize) + dp(4);
                if (messageObject.type == MessageObject.TYPE_SUGGEST_PHOTO) {
                    y += dp(16);
                }
                if (messageObject.isStarGiftAction()) {
                    y += dp(12);
                } else if (messageObject.type == MessageObject.TYPE_GIFT_STARS && !messageObject.isStarGiftAction()) {
                    y -= dp(3.66f);
                }
            }

            canvas.translate(x, y);
            if (giftPremiumTitleLayout != null) {
                canvas.save();
                canvas.translate((giftRectSize - dp(16) - giftPremiumTitleLayout.getWidth()) / 2f, 0);
                giftPremiumTitleLayout.draw(canvas);
                canvas.restore();
                y += giftPremiumTitleLayout.getHeight();
                if (giftPremiumSubtitleLayout != null) {
                    canvas.save();
                    canvas.translate((giftRectSize - dp(16) - giftPremiumSubtitleLayout.getWidth()) / 2f, giftPremiumTitleLayout.getHeight() + dp(4));
                    giftPremiumSubtitleLayout.draw(canvas);
                    canvas.restore();
                    y += giftPremiumSubtitleLayout.getHeight() + dp(10);
                }
                y += dp(messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL ? 6 : 0);
            } else {
                y -= dp(4);
            }
            canvas.restore();

            y += dp(4);
            if (messageObject.type == MessageObject.TYPE_GIFT_PREMIUM) {
                y += dp(2);
            }
            canvas.save();
            canvas.translate(x, y);
            if (messageObject.type == MessageObject.TYPE_ACTION_WALLPAPER) {
                if (radialProgress.getTransitionProgress() != 1f || radialProgress.getIcon() != MediaActionDrawable.ICON_NONE) {
                    if (settingWallpaperLayout == null) {
                        settingWallpaperPaint = new TextPaint();
                        settingWallpaperPaint.setTextSize(dp(13));
                        SpannableStringBuilder cs = new SpannableStringBuilder(getString(R.string.ActionSettingWallpaper));
                        int index = cs.toString().indexOf("..."), len = 3;
                        if (index < 0) {
                            index = cs.toString().indexOf("…");
                            len = 1;
                        }
                        if (index >= 0) {
                            SpannableString loading = new SpannableString("…");
                            UploadingDotsSpannable loadingDots = new UploadingDotsSpannable();
                            loadingDots.fixTop = true;
                            loadingDots.setParent(ChatActionCell.this, false);
                            loading.setSpan(loadingDots, 0, loading.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            cs.replace(index, index + len, loading);
                        }
                        settingWallpaperLayout = new StaticLayout(cs, settingWallpaperPaint, giftPremiumText == null ? 1 : giftPremiumText.width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
                    }
                    float progressLocal = getUploadingInfoProgress(messageObject);
                    if (settingWallpaperProgressTextLayout == null || settingWallpaperProgress != progressLocal) {
                        settingWallpaperProgress = progressLocal;
                        settingWallpaperProgressTextLayout = new StaticLayout((int) (progressLocal * 100) + "%", giftTextPaint, giftPremiumText == null ? 1 : giftPremiumText.width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
                    }

                    settingWallpaperPaint.setColor(giftTextPaint.getColor());
                    if (radialProgress.getIcon() == MediaActionDrawable.ICON_NONE) {
                        float p = radialProgress.getTransitionProgress();
                        int oldColor = giftTextPaint.getColor();
                        settingWallpaperPaint.setAlpha((int) (Color.alpha(oldColor) * (1f - p)));
                        giftTextPaint.setAlpha((int) (Color.alpha(oldColor) * p));
                        giftTextPaint.linkColor = giftTextPaint.getColor();

                        float s;
                        if (giftPremiumText != null) {
                            s = 0.8f + 0.2f * p;
                            canvas.save();
                            canvas.scale(s, s, giftRectSize / 2f, giftPremiumText.layout.getHeight() / 2f);
                            canvas.translate((giftRectSize - giftPremiumText.layout.getWidth()) / 2f, 0);
                            giftPremiumText.x = x + (giftRectSize - giftPremiumText.layout.getWidth()) / 2f;
                            giftPremiumText.y = y;
                            SpoilerEffect.renderWithRipple(this, false, giftTextPaint.getColor(), 0, giftPremiumText.patchedLayout, 1, giftPremiumText.layout, giftPremiumText.spoilers, canvas, false);
                            AnimatedEmojiSpan.drawAnimatedEmojis(canvas, giftPremiumText.layout, giftPremiumText.emoji, 0, null, 0, 0, 0, 1f, getAdaptiveEmojiColorFilter(giftTextPaint.getColor()));
                            canvas.restore();
                        }

                        giftTextPaint.setAlpha((int) (Color.alpha(oldColor) * (1f - p)));
                        giftTextPaint.linkColor = giftTextPaint.getColor();
                        s = 0.8f + 0.2f * (1f - p);
                        canvas.save();
                        canvas.scale(s, s, giftRectSize / 2f, settingWallpaperLayout.getHeight() / 2f);
                        canvas.translate((giftRectSize - settingWallpaperLayout.getWidth()) / 2f, 0);
                        SpoilerEffect.layoutDrawMaybe(settingWallpaperLayout, canvas);
                        canvas.restore();

                        canvas.save();
                        canvas.translate(0, settingWallpaperLayout.getHeight() + dp(4));
                        canvas.scale(s, s, giftRectSize / 2f, settingWallpaperProgressTextLayout.getHeight() / 2f);
                        canvas.translate((giftRectSize - settingWallpaperProgressTextLayout.getWidth()) / 2f, 0);
                        SpoilerEffect.layoutDrawMaybe(settingWallpaperProgressTextLayout, canvas);
                        canvas.restore();


                        giftTextPaint.setColor(oldColor);
                        giftTextPaint.linkColor = oldColor;
                    } else {
                        canvas.save();
                        canvas.translate((giftRectSize - settingWallpaperLayout.getWidth()) / 2.f, 0.0f);
                        settingWallpaperLayout.draw(canvas);
                        canvas.restore();

                        canvas.save();
                        canvas.translate((giftRectSize - settingWallpaperProgressTextLayout.getWidth()) / 2.f, settingWallpaperLayout.getHeight() + dp(4));
                        SpoilerEffect.layoutDrawMaybe(settingWallpaperProgressTextLayout, canvas);
                        canvas.restore();
                    }
                } else if (giftPremiumText != null) {
                    canvas.save();
                    canvas.translate((giftRectSize - giftPremiumText.layout.getWidth()) / 2f, 0);
                    giftPremiumText.x = x + (giftRectSize - giftPremiumText.layout.getWidth()) / 2f;
                    giftPremiumText.y = y;
                    SpoilerEffect.renderWithRipple(this, false, giftTextPaint.getColor(), 0, giftPremiumText.patchedLayout, 1, giftPremiumText.layout, giftPremiumText.spoilers, canvas, false);
                    AnimatedEmojiSpan.drawAnimatedEmojis(canvas, giftPremiumText.layout, giftPremiumText.emoji, 0, null, 0, 0, 0, 1f, getAdaptiveEmojiColorFilter(giftTextPaint.getColor()));
                    canvas.restore();
                }
            } else if (giftPremiumText != null) {
                float h = giftPremiumText.layout.getHeight();
                if (expanded < 1) {
                    h = AndroidUtilities.lerp(giftPremiumTextCollapsedHeight, h, expanded);
                    AndroidUtilities.rectTmp.set(0, -dp(20), getWidth(), h);
                    canvas.saveLayerAlpha(AndroidUtilities.rectTmp, 0xFF, Canvas.ALL_SAVE_FLAG);
                } else {
                    canvas.save();
                }
                canvas.translate((giftRectSize - dp(16) - giftPremiumText.layout.getWidth()) / 2f, 0);
                giftPremiumText.x = x + (giftRectSize - dp(16) - giftPremiumText.layout.getWidth()) / 2f;
                giftPremiumText.y = y;
                SpoilerEffect.renderWithRipple(this, false, giftPremiumText.paint.getColor(), 0, giftPremiumText.patchedLayout, 1, giftPremiumText.layout, giftPremiumText.spoilers, canvas, false);
                AnimatedEmojiSpan.drawAnimatedEmojis(canvas, giftPremiumText.layout, giftPremiumText.emoji, 0, null, 0, 0, 0, 1f, getAdaptiveEmojiColorFilter(giftTextPaint.getColor()));
                if (expanded < 1 && giftPremiumTextMore != null) {
                    canvas.save();

                    if (giftPremiumTextClip == null) {
                        giftPremiumTextClip = new GradientClip();
                    }
                    canvas.translate(-(giftRectSize - dp(16) - giftPremiumText.layout.getWidth()) / 2f, 0);
                    AndroidUtilities.rectTmp.set(giftPremiumTextMoreX - giftPremiumTextMore.getCurrentWidth() + dp(8), giftPremiumTextMoreY - giftPremiumTextMoreH - dp(6), giftPremiumTextMoreX + dp(6), giftPremiumTextMoreY);
                    giftPremiumTextClip.clipOut(canvas, AndroidUtilities.rectTmp, 1f - expanded);
                    AndroidUtilities.rectTmp.set(giftPremiumTextMoreX - giftPremiumTextMore.getCurrentWidth() - dp(16), giftPremiumTextMoreY - giftPremiumTextMoreH - dp(6), giftPremiumTextMoreX - giftPremiumTextMore.getCurrentWidth() + dp(8), giftPremiumTextMoreY);
                    giftPremiumTextClip.draw(canvas, AndroidUtilities.rectTmp, GradientClip.RIGHT,  1f - expanded);

                    AndroidUtilities.rectTmp.set(0, h - dp(12), getWidth(), h);
                    float expX = 1f - expanded;
                    giftPremiumTextClip.draw(canvas, AndroidUtilities.rectTmp, GradientClip.BOTTOM,  4 * expX * (1 - expX));

                    canvas.restore();
                }
                canvas.restore();

                if (expanded < 1 && giftPremiumTextMore != null) {
                    giftPremiumTextMore.draw(canvas, giftPremiumTextMoreX - giftPremiumTextMore.getCurrentWidth() + dp(5), giftPremiumTextMoreY - giftPremiumTextMoreH / 2f - dp(1), giftPremiumText.paint.getColor(), 1f - expanded);
                }
            }
            canvas.restore();

            if (giftPremiumTitleLayout == null) {
                y -= dp(8);
            }

            if (giftPremiumText != null) {
                y += AndroidUtilities.lerp(giftPremiumTextCollapsedHeight, giftPremiumText.layout.getHeight(), expanded);
            }
            int buttonH = giftPremiumButtonLayout != null ? giftPremiumButtonLayout.getHeight() : 0;
            y += (getHeight() - y - buttonH - dp(8)) / 2f;

            if (themeDelegate != null) {
                themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
            } else {
                Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
            }

            final float S = bounce.getScale(0.02f);
            canvas.save();
            canvas.scale(S, S, giftButtonRect.centerX(), giftButtonRect.centerY());

            if (giftPremiumButtonLayout != null) {
                Paint backgroundPaint = getThemedPaint(Theme.key_paint_chatActionBackgroundSelected);
                canvas.drawRoundRect(giftButtonRect, dp(16), dp(16), backgroundPaint);
                if (hasGradientService()) {
                    canvas.drawRoundRect(giftButtonRect, dp(16), dp(16), getThemedPaint(Theme.key_paint_chatActionBackgroundDarken));
                }
                if (dimAmount > 0) {
                    canvas.drawRoundRect(giftButtonRect, dp(16), dp(16), dimPaint);
                }

                if (getMessageObject().type != MessageObject.TYPE_SUGGEST_PHOTO && getMessageObject().type != MessageObject.TYPE_ACTION_WALLPAPER && getMessageObject().type != MessageObject.TYPE_STORY_MENTION) {
                    starsPath.rewind();
                    starsPath.addRoundRect(giftButtonRect, dp(16), dp(16), Path.Direction.CW);
                    canvas.save();
                    canvas.clipPath(starsPath);

                    starParticlesDrawable.onDraw(canvas);
                    if (!starParticlesDrawable.paused) {
                        invalidate();
                    }
                    canvas.restore();
                } else {
                    invalidate();
                }
            }

            if (messageObject.settingAvatar && progressToProgress != 1f) {
                progressToProgress += 16 / 150f;
            } else if (!messageObject.settingAvatar && progressToProgress != 0) {
                progressToProgress -= 16 / 150f;
            }
            progressToProgress = Utilities.clamp(progressToProgress, 1f, 0f);
            if (progressToProgress != 0) {
                if (progressView == null) {
                    progressView = new RadialProgressView(getContext());
                }
                int rad = dp(16);
                canvas.save();
                canvas.scale(progressToProgress, progressToProgress, giftButtonRect.centerX(), giftButtonRect.centerY());
                progressView.setSize(rad);
                progressView.setProgressColor(Theme.getColor(Theme.key_chat_serviceText));
                progressView.draw(canvas, giftButtonRect.centerX(), giftButtonRect.centerY());
                canvas.restore();
            }
            if (progressToProgress != 1f && giftPremiumButtonLayout != null) {
                canvas.save();
                float s = 1f - progressToProgress;
                canvas.scale(s, s, giftButtonRect.centerX(), giftButtonRect.centerY());
                canvas.translate(x, giftButtonRect.top + dp(8));
                canvas.translate((giftRectSize - dp(16) - giftPremiumButtonLayout.getWidth()) / 2f, 0);
                giftPremiumButtonLayout.draw(canvas);
                canvas.restore();
            }

            if (messageObject.flickerLoading) {
                if (loadingDrawable == null) {
                    loadingDrawable = new LoadingDrawable(themeDelegate);
                    loadingDrawable.setGradientScale(2f);
                    loadingDrawable.setAppearByGradient(true);
                    loadingDrawable.setColors(
                        Theme.multAlpha(Color.WHITE, .08f),
                        Theme.multAlpha(Color.WHITE, .2f),
                        Theme.multAlpha(Color.WHITE, .2f),
                        Theme.multAlpha(Color.WHITE, .7f)
                    );
                    loadingDrawable.strokePaint.setStrokeWidth(dp(1));
                }
                loadingDrawable.resetDisappear();
                loadingDrawable.setBounds(giftButtonRect);
                loadingDrawable.setRadiiDp(16);
                loadingDrawable.draw(canvas);
            } else if (loadingDrawable != null) {
                loadingDrawable.setBounds(giftButtonRect);
                loadingDrawable.setRadiiDp(16);
                loadingDrawable.disappear();
                loadingDrawable.draw(canvas);
                if (loadingDrawable.isDisappeared()) {
                    loadingDrawable.reset();
                }
            }

            canvas.restore();

            if (backgroundRect != null && giftRibbonPath != null && giftRibbonText != null) {
                final Paint backgroundPaint = getThemedPaint(Theme.key_paint_chatActionBackground);
                final Paint darkenBackgroundPaint = getThemedPaint(Theme.key_paint_chatActionBackgroundDarken);
                final float tx = backgroundRect.right - dp(65) + dp(2);
                final float ty = backgroundRect.top - dp(2);
                if (themeDelegate != null) {
                    themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX + tx, viewTop + dp(4) + ty);
                } else {
                    Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX + tx, viewTop + dp(4) + ty);
                }
                canvas.save();
                canvas.translate(tx, ty);

                ColorFilter wasColorFilter = backgroundPaint.getColorFilter();
                PathEffect wasPathEffect = backgroundPaint.getPathEffect();
                final boolean isDark = themeDelegate != null ? themeDelegate.isDark() : Theme.isCurrentThemeDark();
                if (giftRibbonPaintFilter == null || giftRibbonPaintFilterDark != isDark) {
                    ColorMatrix colorMatrix = new ColorMatrix();
                    if (backgroundPaint.getColorFilter() instanceof ColorMatrixColorFilter && Build.VERSION.SDK_INT >= 26) {
                        ((ColorMatrixColorFilter) backgroundPaint.getColorFilter()).getColorMatrix(colorMatrix);
                    }
                    AndroidUtilities.adjustBrightnessColorMatrix(colorMatrix, isDark ? +.10f : -.08f);
                    AndroidUtilities.adjustSaturationColorMatrix(colorMatrix, isDark ? +.15f : +.10f);
                    giftRibbonPaintFilter = new ColorMatrixColorFilter(colorMatrix);
                    giftRibbonPaintFilterDark = isDark;
                }
                backgroundPaint.setColorFilter(giftRibbonPaintFilter);
                backgroundPaint.setPathEffect(giftRibbonPaintEffect);
                canvas.drawPath(giftRibbonPath, backgroundPaint);
                backgroundPaint.setColorFilter(wasColorFilter);
                backgroundPaint.setPathEffect(wasPathEffect);

                if (hasGradientService()) {
                    wasPathEffect = darkenBackgroundPaint.getPathEffect();
                    darkenBackgroundPaint.setPathEffect(giftRibbonPaintEffect);
                    canvas.drawPath(giftRibbonPath, darkenBackgroundPaint);
                    darkenBackgroundPaint.setPathEffect(wasPathEffect);
                }
                canvas.rotate(45, dp(40.43f), dp(24.56f));
                giftRibbonText.draw(canvas, dp(40.43f) - giftRibbonText.getCurrentWidth() / 2f, dp(26f), 0xFFFFFFFF, 1f);
                canvas.restore();
            }
        }

        drawReactions(canvas, false, null);

        transitionParams.recordDrawingState();
        canvas.restore();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child == rippleView) {
            final float S = bounce.getScale(0.02f);
            canvas.save();
            canvas.scale(S, S, child.getX() + child.getMeasuredWidth() / 2f, child.getY() + child.getMeasuredHeight() / 2f);
            final boolean r = super.drawChild(canvas, child, drawingTime);
            canvas.restore();
            return r;
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    private void checkLeftRightBounds() {
        backgroundLeft = (int) Math.min(backgroundLeft, rect.left);
        backgroundRight = (int) Math.max(backgroundRight, rect.right);
    }

    public void drawBackground(Canvas canvas, boolean fromParent) {
        if (canDrawInParent) {
            if (hasGradientService() && !fromParent) {
                return;
            }
            if (!hasGradientService() && fromParent) {
                return;
            }
        }
        Paint backgroundPaint = getThemedPaint(Theme.key_paint_chatActionBackground);
        Paint darkenBackgroundPaint = getThemedPaint(Theme.key_paint_chatActionBackgroundDarken);
        textPaint = (TextPaint) getThemedPaint(Theme.key_paint_chatActionText);
        if (overrideBackground >= 0) {
            int color = getThemedColor(overrideBackground);
            if (overrideBackgroundPaint == null) {
                overrideBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                overrideBackgroundPaint.setColor(color);
                overrideTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                overrideTextPaint.setTypeface(AndroidUtilities.bold());
                overrideTextPaint.setTextSize(dp(Math.max(16, SharedConfig.fontSize) - 2));
                overrideTextPaint.setColor(getThemedColor(overrideText));
            }
            backgroundPaint = overrideBackgroundPaint;
            textPaint = overrideTextPaint;
        }
        if (invalidatePath) {
            invalidatePath = false;
            backgroundLeft = getWidth();
            backgroundRight = 0;
            lineWidths.clear();
            final int count = textLayout == null ? 0 : textLayout.getLineCount();
            final int corner = dp(11);
            final int cornerIn = dp(8);

            int prevLineWidth = 0;
            for (int a = 0; a < count; a++) {
                int lineWidth = (int) Math.ceil(textLayout.getLineWidth(a));
                if (a != 0) {
                    int diff = prevLineWidth - lineWidth;
                    if (diff > 0 && diff <= 1.5f * corner + cornerIn) {
                        lineWidth = prevLineWidth;
                    }
                }
                lineWidths.add(lineWidth);
                prevLineWidth = lineWidth;
            }
            for (int a = count - 2; a >= 0; a--) {
                int lineWidth = lineWidths.get(a);
                int diff = prevLineWidth - lineWidth;
                if (diff > 0 && diff <= 1.5f * corner + cornerIn) {
                    lineWidth = prevLineWidth;
                }
                lineWidths.set(a, lineWidth);
                prevLineWidth = lineWidth;
            }

            int y = dp(4);
            int x = getMeasuredWidth() / 2;
            int previousLineBottom = 0;

            final int cornerOffset = dp(3);
            final int cornerInSmall = dp(6);
            final int cornerRest = corner - cornerOffset;

            lineHeights.clear();
            backgroundPath.reset();
            backgroundPath.moveTo(x, y);

            for (int a = 0; a < count; a++) {
                int lineWidth = lineWidths.get(a);
                int lineBottom = textLayout.getLineBottom(a);
                int nextLineWidth = a < count - 1 ? lineWidths.get(a + 1) : 0;

                int height = lineBottom - previousLineBottom;
                if (a == 0 || lineWidth > prevLineWidth) {
                    height += dp(3);
                }
                if (a == count - 1 || lineWidth > nextLineWidth) {
                    height += dp(3);
                }

                previousLineBottom = lineBottom;

                float startX = x + lineWidth / 2.0f;

                int innerCornerRad;
                if (a != count - 1 && lineWidth < nextLineWidth && a != 0 && lineWidth < prevLineWidth) {
                    innerCornerRad = cornerInSmall;
                } else {
                    innerCornerRad = cornerIn;
                }

                if (a == 0 || lineWidth > prevLineWidth) {
                    rect.set(startX - cornerOffset - corner, y, startX + cornerRest, y + corner * 2);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, -90, 90);
                } else if (lineWidth < prevLineWidth) {
                    rect.set(startX + cornerRest, y, startX + cornerRest + innerCornerRad * 2, y + innerCornerRad * 2);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, -90, -90);
                }
                y += height;
                int yOffset = y;
                if (a != count - 1 && lineWidth < nextLineWidth) {
                    y -= dp(3);
                    height -= dp(3);
                }
                if (a != 0 && lineWidth < prevLineWidth) {
                    y -= dp(3);
                    height -= dp(3);
                }
                lineHeights.add(height);

                if (a == count - 1 || lineWidth > nextLineWidth) {
                    rect.set(startX - cornerOffset - corner, y - corner * 2, startX + cornerRest, y);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, 0, 90);
                } else if (lineWidth < nextLineWidth) {
                    rect.set(startX + cornerRest, y - innerCornerRad * 2, startX + cornerRest + innerCornerRad * 2, y);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, 180, -90);
                }

                prevLineWidth = lineWidth;
            }
            for (int a = count - 1; a >= 0; a--) {
                prevLineWidth = a != 0 ? lineWidths.get(a - 1) : 0;
                int lineWidth = lineWidths.get(a);
                int nextLineWidth = a != count - 1 ? lineWidths.get(a + 1) : 0;
                int lineBottom = textLayout.getLineBottom(a);
                float startX = x - lineWidth / 2;

                int innerCornerRad;
                if (a != count - 1 && lineWidth < nextLineWidth && a != 0 && lineWidth < prevLineWidth) {
                    innerCornerRad = cornerInSmall;
                } else {
                    innerCornerRad = cornerIn;
                }

                if (a == count - 1 || lineWidth > nextLineWidth) {
                    rect.set(startX - cornerRest, y - corner * 2, startX + cornerOffset + corner, y);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, 90, 90);
                } else if (lineWidth < nextLineWidth) {
                    rect.set(startX - cornerRest - innerCornerRad * 2, y - innerCornerRad * 2, startX - cornerRest, y);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, 90, -90);
                }

                y -= lineHeights.get(a);

                if (a == 0 || lineWidth > prevLineWidth) {
                    rect.set(startX - cornerRest, y, startX + cornerOffset + corner, y + corner * 2);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, 180, 90);
                } else if (lineWidth < prevLineWidth) {
                    rect.set(startX - cornerRest - innerCornerRad * 2, y, startX - cornerRest, y + innerCornerRad * 2);
                    checkLeftRightBounds();
                    backgroundPath.arcTo(rect, 0, -90);
                }
            }
            backgroundPath.close();

            if (isMessageActionSuggestedPostApproval() && !isNewStyleButtonLayout()) {
                rect.left = x - textWidth / 2f - dp(17);
                rect.top = y;
                rect.right = x + textWidth / 2f + dp(17);
                rect.bottom = y + textHeight + titleHeight + dp(28);

                backgroundPath.reset();
                backgroundPath.addRoundRect(rect, dp(15), dp(15), Path.Direction.CW);
                backgroundPath.close();
            }
        }
        if (!visiblePartSet) {
            ViewGroup parent = (ViewGroup) getParent();
            backgroundHeight = parent.getMeasuredHeight();
        }
        if (themeDelegate != null) {
            themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
        } else {
            Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
        }

        int oldAlpha = -1;
        int oldAlpha2 = -1;
        if (fromParent && (getAlpha() != 1f || isFloating())) {
            oldAlpha = backgroundPaint.getAlpha();
            oldAlpha2 = darkenBackgroundPaint.getAlpha();
            backgroundPaint.setAlpha((int) (oldAlpha * getAlpha() * (isFloating() ? .75f : 1f)));
            darkenBackgroundPaint.setAlpha((int) (oldAlpha2 * getAlpha() * (isFloating() ? .75f : 1f)));
        } else if (isFloating()) {
            oldAlpha = backgroundPaint.getAlpha();
            oldAlpha2 = darkenBackgroundPaint.getAlpha();
            backgroundPaint.setAlpha((int) (oldAlpha * (isFloating() ? .75f : 1f)));
            darkenBackgroundPaint.setAlpha((int) (oldAlpha2 * (isFloating() ? .75f : 1f)));
        }
        if (currentMessageObject == null || !currentMessageObject.isRepostPreview) {
            canvas.drawPath(backgroundPath, backgroundPaint);
            if (hasGradientService()) {
                canvas.drawPath(backgroundPath, darkenBackgroundPaint);
            }
            if (dimAmount > 0) {
                int wasAlpha = dimPaint.getAlpha();
                if (fromParent) {
                    dimPaint.setAlpha((int) (wasAlpha * getAlpha()));
                }
                canvas.drawPath(backgroundPath, dimPaint);
                dimPaint.setAlpha(wasAlpha);
            }
        }

        MessageObject messageObject = currentMessageObject;
        if (starGiftLayout.has()) {
            float w = starGiftLayout.getWidth() + dp(4 + 4);
            float x = (getWidth() - w) / 2f;
            float y = starGiftLayout.repost ? 0 : textY + textHeight + dp(12);
            AndroidUtilities.rectTmp.set(x, y, x + w, y + starGiftLayout.getHeight() + dp(4 + 4));
            if (backgroundRect == null) {
                backgroundRect = new RectF();
            }
            backgroundRect.set(AndroidUtilities.rectTmp);
            canvas.drawRoundRect(backgroundRect, dp(16), dp(16), backgroundPaint);

            if (hasGradientService()) {
                canvas.drawRoundRect(backgroundRect, dp(16), dp(16), darkenBackgroundPaint);
            }
        } else if (isButtonLayout(messageObject)) {
            float x = (getWidth() - giftRectSize) / 2f;
            float y = textY + textHeight;
            if (isNewStyleButtonLayout()) {
                y += dp(4);
                AndroidUtilities.rectTmp.set(x, y, x + giftRectSize, y + backgroundRectHeight);
            } else {
                y += dp(12);
                AndroidUtilities.rectTmp.set(x, y, x + giftRectSize, y + giftRectSize + giftPremiumAdditionalHeight);
            }
            if (messageObject != null && messageObject.type == MessageObject.TYPE_GIFT_PREMIUM && !giftPremiumTextCollapsed && giftPremiumText != null && giftPremiumTextCollapsedHeight > 0) {
                AndroidUtilities.rectTmp.bottom -= (giftPremiumText.layout.getHeight() - giftPremiumTextCollapsedHeight) * (1f - giftPremiumTextExpandedAnimated.get());
            }
            if (backgroundRect == null) {
                backgroundRect = new RectF();
            }
            backgroundRect.set(AndroidUtilities.rectTmp);
            canvas.drawRoundRect(backgroundRect, dp(16), dp(16), backgroundPaint);

            if (hasGradientService()) {
                canvas.drawRoundRect(backgroundRect, dp(16), dp(16), darkenBackgroundPaint);
            }
        }

        if (oldAlpha >= 0) {
            backgroundPaint.setAlpha(oldAlpha);
            darkenBackgroundPaint.setAlpha(oldAlpha2);
        }
    }

    public void drawReactions(Canvas canvas, boolean fromParent, Integer only) {
        if (canDrawInParent) {
            if (hasGradientService() && !fromParent) {
                return;
            }
            if (!hasGradientService() && fromParent) {
                return;
            }
        }
        drawReactionsLayout(canvas, fromParent, only);
    }

    public void drawReactionsLayout(Canvas canvas, boolean fromParent, Integer only) {
        final float alpha = fromParent ? getAlpha() : 1.0f;
        if (themeDelegate != null) {
            themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
        } else {
            Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
        }
        if (currentMessageObject != null && currentMessageObject.shouldDrawReactions() && (!reactionsLayoutInBubble.isSmall || transitionParams.animateChange && reactionsLayoutInBubble.animateHeight)) {
            reactionsLayoutInBubble.drawServiceShaderBackground = 1.0f;
            if (alpha < 1) {
                canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), (int) (0xFF * alpha), Canvas.ALL_SAVE_FLAG);
            }
            reactionsLayoutInBubble.draw(canvas, transitionParams.animateChange ? transitionParams.animateChangeProgress : 1f, only);
            if (alpha < 1) {
                canvas.restore();
            }
        }
    }

    public void drawReactionsLayoutOverlay(Canvas canvas, boolean fromParent) {
        final float alpha = fromParent ? getAlpha() : 1.0f;
        if (themeDelegate != null) {
            themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
        } else {
            Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
        }
        if (currentMessageObject != null && currentMessageObject.shouldDrawReactions() && (!reactionsLayoutInBubble.isSmall || transitionParams.animateChange && reactionsLayoutInBubble.animateHeight)) {
            reactionsLayoutInBubble.drawServiceShaderBackground = 1.0f;
            if (alpha < 1) {
                canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), (int) (0xFF * alpha), Canvas.ALL_SAVE_FLAG);
            }
            reactionsLayoutInBubble.drawOverlay(canvas, transitionParams.animateChange ? transitionParams.animateChangeProgress : 1f);
            if (alpha < 1) {
                canvas.restore();
            }
        }
    }

    @Override
    public int getBoundsLeft() {
        if (starGiftLayout.has()) {
            final int giftLayoutLeft = (int) (getWidth() - (starGiftLayout.getWidth() + dp(8))) / 2;
            if (starGiftLayout.repost) {
                return giftLayoutLeft;
            }
            return Math.min(backgroundLeft, giftLayoutLeft);
        }
        if (isButtonLayout(currentMessageObject)) {
            return sideMenuWidth / 2 + (getWidth() - giftRectSize) / 2;
        }
        int left = backgroundLeft;
        if (imageReceiver != null && imageReceiver.getVisible()) {
            left = Math.min((int) imageReceiver.getImageX(), left);
        }
        return sideMenuWidth / 2 + left;
    }

    @Override
    public int getBoundsRight() {
        if (starGiftLayout.has()) {
            final int giftLayoutRight = (int) (getWidth() + (starGiftLayout.getWidth() + dp(8))) / 2;
            if (starGiftLayout.repost) {
                return giftLayoutRight;
            }
            return Math.max(backgroundRight, giftLayoutRight);
        }
        if (isButtonLayout(currentMessageObject)) {
            return sideMenuWidth / 2 + (getWidth() + giftRectSize) / 2;
        }
        int right = backgroundRight;
        if (imageReceiver != null && imageReceiver.getVisible()) {
            right = Math.max((int) imageReceiver.getImageX2(), right);
        }
        return sideMenuWidth / 2 + right;
    }

    public boolean hasGradientService() {
        return overrideBackgroundPaint == null && (themeDelegate != null ? themeDelegate.hasGradientService() : Theme.hasGradientService());
    }

    @Override
    public void onFailedDownload(String fileName, boolean canceled) {

    }

    @Override
    public void onSuccessDownload(String fileName) {
        MessageObject messageObject = currentMessageObject;
        if (messageObject != null && messageObject.type == MessageObject.TYPE_ACTION_PHOTO) {
            TLRPC.PhotoSize strippedPhotoSize = null;
            for (int a = 0, N = messageObject.photoThumbs.size(); a < N; a++) {
                TLRPC.PhotoSize photoSize = messageObject.photoThumbs.get(a);
                if (photoSize instanceof TLRPC.TL_photoStrippedSize) {
                    strippedPhotoSize = photoSize;
                    break;
                }
            }
            imageReceiver.setImage(currentVideoLocation, ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForObject(strippedPhotoSize, messageObject.photoThumbsObject), "50_50_b", avatarDrawable, 0, null, messageObject, 1);
            DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
        }
    }

    @Override
    public void onProgressDownload(String fileName, long downloadSize, long totalSize) {

    }

    @Override
    public void onProgressUpload(String fileName, long downloadSize, long totalSize, boolean isEncrypted) {

    }

    @Override
    public int getObserverTag() {
        return TAG;
    }

    private SpannableStringBuilder accessibilityText;

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        MessageObject messageObject = currentMessageObject;
        if (TextUtils.isEmpty(customText) && messageObject == null) {
            return;
        }
        if (accessibilityText == null) {
            CharSequence text = !TextUtils.isEmpty(customText) ? customText : messageObject.messageText;
            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            CharacterStyle[] links = sb.getSpans(0, sb.length(), ClickableSpan.class);
            for (CharacterStyle link : links) {
                int start = sb.getSpanStart(link);
                int end = sb.getSpanEnd(link);
                sb.removeSpan(link);

                ClickableSpan underlineSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        if (delegate != null) {
                            openLink(link);
                        }
                    }
                };
                sb.setSpan(underlineSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            accessibilityText = sb;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            info.setContentDescription(accessibilityText.toString());
        } else {
            info.setText(accessibilityText);
        }
        info.setEnabled(true);
    }

    public void setInvalidateColors(boolean invalidate) {
        if (invalidateColors == invalidate) {
            return;
        }
        invalidateColors = invalidate;
        invalidate();
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, themeDelegate);
    }

    protected Paint getThemedPaint(String paintKey) {
        Paint paint = themeDelegate != null ? themeDelegate.getPaint(paintKey) : null;
        return paint != null ? paint : Theme.getThemePaint(paintKey);
    }

    public void drawOutboundsContent(Canvas canvas) {
        canvas.save();
        canvas.translate(sideMenuWidth / 2f, getPaddingTop());

        canvas.save();
        canvas.translate(textXLeft, textY);
        AnimatedEmojiSpan.drawAnimatedEmojis(canvas, textLayout, animatedEmojiStack, 0, spoilers, 0, 0, 0, 1f, textLayout != null ? getAdaptiveEmojiColorFilter(textLayout.getPaint().getColor()) : null);
        canvas.restore();

        if (starGiftLayout.has()) {
            canvas.save();
            canvas.translate((getWidth() - starGiftLayout.getWidth()) / 2.0f, starGiftLayout.repost ? dp(4) : textY + textHeight + dp(4 + 6 + 6));
            starGiftLayout.drawOutbounds(canvas);
            canvas.restore();
        }
        canvas.restore();

        if (topicSeparator != null) {
            final float alpha = getAlpha(); // transitionParams.ignoreAlpha ? timeAlpha : getAlpha();
            final float top = 0;//- topicSeparatorTopPadding + (getTopicSeparatorTopPadding() - topicSeparatorTopPadding);;
            if (themeDelegate != null) {
                themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + top);
            } else {
                Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + top);
            }
            topicSeparator.draw(canvas, getWidth(), sideMenuWidth, top, 1.0f, alpha, showTopicSeparator);
        }
    }

    private boolean isButtonLayout(MessageObject messageObject) {
        return messageObject != null && (messageObject.type == MessageObject.TYPE_GIFT_STARS || messageObject.type == MessageObject.TYPE_GIFT_PREMIUM || messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL || isNewStyleButtonLayout());
    }

    private boolean isGiftChannel(MessageObject messageObject) {
        return messageObject != null && messageObject.type == MessageObject.TYPE_GIFT_PREMIUM_CHANNEL;
    }

    private boolean invalidatesParent;
    public void setInvalidatesParent(boolean value) {
        invalidatesParent = value;
    }

    private Runnable invalidateListener;
    public void setInvalidateListener(Runnable listener) {
        invalidateListener = listener;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (invalidateWithParent != null) {
            invalidateWithParent.invalidate();
        }
        if (invalidateListener != null) {
            invalidateListener.run();
        }
        if (invalidatesParent && getParent() != null) {
            View parent = (View) getParent();
            if (parent.getParent() != null) {
                parent.invalidate();
                parent = (View) parent.getParent();
                parent.invalidate();
            }
        }
    }

    public void invalidateOutbounds() {
        if (delegate == null || !delegate.canDrawOutboundsContent()) {
            if (getParent() instanceof View) {
                ((View) getParent()).invalidate();
            }
        } else {
            super.invalidate();
        }
    }

    @Override
    public void invalidate(Rect dirty) {
        super.invalidate(dirty);
        if (invalidateWithParent != null) {
            invalidateWithParent.invalidate();
        }
        if (invalidatesParent && getParent() != null) {
            View parent = (View) getParent();
            if (parent.getParent() != null) {
                parent.invalidate();
                parent = (View) parent.getParent();
                parent.invalidate();
            }
        }
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        if (invalidateWithParent != null) {
            invalidateWithParent.invalidate();
        }
        if (invalidatesParent && getParent() != null) {
            View parent = (View) getParent();
            if (parent.getParent() != null) {
                parent.invalidate();
                parent = (View) parent.getParent();
                parent.invalidate();
            }
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == wallpaperPreviewDrawable || super.verifyDrawable(who);
    }

    public boolean isFloating() {
        return false;
    }

    private ColorFilter adaptiveEmojiColorFilter;
    private int adaptiveEmojiColor;
    private ColorFilter getAdaptiveEmojiColorFilter(int color) {
        if (color != adaptiveEmojiColor || adaptiveEmojiColorFilter == null) {
            adaptiveEmojiColorFilter = new PorterDuffColorFilter(adaptiveEmojiColor = color, PorterDuff.Mode.SRC_IN);
        }
        return adaptiveEmojiColorFilter;
    }

    public int measuredWidth() {
        return getMeasuredWidth();
    }

    public ReactionsLayoutInBubble.ReactionButton getReactionButton(ReactionsLayoutInBubble.VisibleReaction visibleReaction) {
        return reactionsLayoutInBubble.getReactionButton(visibleReaction);
    }

    public final TransitionParams transitionParams = new TransitionParams();
    public class TransitionParams {

        public boolean wasDraw;

        public boolean animateChange;
        public float animateChangeProgress = 1f;

        public void recordDrawingState() {
            wasDraw = true;
            reactionsLayoutInBubble.recordDrawingState();
        }

        public boolean animateChange() {
            if (!wasDraw) {
                return false;
            }
            boolean changed = false;
            if (reactionsLayoutInBubble.animateChange()) {
                changed = true;
            }
            return changed;
        }

        public void onDetach() {
            wasDraw = false;
        }

        public boolean supportChangeAnimation() {
            return true;
        }

        public void resetAnimation() {
            animateChange = false;
            animateChangeProgress = 1f;
        }
    }

    public TransitionParams getTransitionParams() {
        return transitionParams;
    }


    public void setScrimReaction(Integer scrimViewReaction) {
        reactionsLayoutInBubble.setScrimReaction(scrimViewReaction);
    }

    public void drawScrimReaction(Canvas canvas, Integer scrimViewReaction, float progress, boolean direction) {
        if (!reactionsLayoutInBubble.isSmall) {
            if (themeDelegate != null) {
                themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
            } else {
                Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
            }
            reactionsLayoutInBubble.setScrimProgress(progress, direction);
            reactionsLayoutInBubble.draw(canvas, transitionParams.animateChangeProgress, scrimViewReaction);
        }
    }

    public void drawScrimReactionPreview(View view, Canvas canvas, int offset, Integer scrimViewReaction, float progress) {
        if (!reactionsLayoutInBubble.isSmall) {
            if (themeDelegate != null) {
                themeDelegate.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
            } else {
                Theme.applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, viewTranslationX, viewTop + dp(4));
            }
            reactionsLayoutInBubble.setScrimProgress(progress);
            reactionsLayoutInBubble.drawPreview(view, canvas, offset, scrimViewReaction);
        }
    }

    public boolean checkUnreadReactions(float clipTop, int clipBottom) {
        if (!reactionsLayoutInBubble.hasUnreadReactions) {
            return false;
        }
        float y = getY() + reactionsLayoutInBubble.y;
        if (y > clipTop && y + reactionsLayoutInBubble.height - AndroidUtilities.dp(16) < clipBottom) {
            return true;
        }
        return false;
    }

    public void markReactionsAsRead() {
        reactionsLayoutInBubble.hasUnreadReactions = false;
        if (currentMessageObject == null) {
            return;
        }
        currentMessageObject.markReactionsAsRead();
    }
}
