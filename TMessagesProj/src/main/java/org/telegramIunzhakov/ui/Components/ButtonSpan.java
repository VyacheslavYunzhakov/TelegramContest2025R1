package org.telegramIunzhakov.ui.Components;

import static org.telegramIunzhakov.messenger.AndroidUtilities.dp;
import static org.telegramIunzhakov.messenger.AndroidUtilities.dpf2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegramIunzhakov.messenger.AndroidUtilities;
import org.telegramIunzhakov.ui.ActionBar.Theme;

public class ButtonSpan extends ReplacementSpan {

    private final Theme.ResourcesProvider resourcesProvider;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Text text;
    private final Runnable onClickListener;
    private ButtonBounce bounce;

    public ButtonSpan(CharSequence buttonText, Runnable onClick, Theme.ResourcesProvider resourcesProvider) {
        this.resourcesProvider = resourcesProvider;
        this.onClickListener = onClick;
        text = new Text(buttonText, 12);
    }

    public static CharSequence make(CharSequence buttonText, Runnable onClick) {
        return make(buttonText, onClick, null);
    }

    public static CharSequence make(CharSequence buttonText, Runnable onClick, Theme.ResourcesProvider resourcesProvider) {
        SpannableString ss = new SpannableString("btn");
        ButtonSpan span1 = new ButtonSpan(buttonText, onClick, resourcesProvider);
        ss.setSpan(span1, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public int getSize() {
        return (int) (this.text.getCurrentWidth() + dp(14));
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return getSize();
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        final float h = dpf2(17), cy = (top + bottom) / 2f;
        AndroidUtilities.rectTmp.set(x, cy - h / 2f, x + getSize(), cy + h / 2f);
        final float s = bounce == null ? 1f : bounce.getScale(.025f);
        canvas.save();
        canvas.scale(s, s, AndroidUtilities.rectTmp.centerX(), AndroidUtilities.rectTmp.centerY());
        final int color = Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider);
        backgroundPaint.setColor(Theme.multAlpha(color, .15f));
        canvas.drawRoundRect(AndroidUtilities.rectTmp, h / 2, h / 2, backgroundPaint);
        this.text.draw(canvas, x + dp(7), (top + bottom) / 2f, color, 1f);
        canvas.restore();
    }

    public void setPressed(View view, boolean pressed) {
        if (bounce == null) {
            bounce = new ButtonBounce(view);
        }
        bounce.setPressed(pressed);
    }

    public static class TextViewButtons extends LinkSpanDrawable.LinksTextView {

        public TextViewButtons(Context context) {
            super(context);
        }

        public TextViewButtons(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context, resourcesProvider);
        }

        public ButtonSpan findSpan(float x, int y) {
            if (!(getText() instanceof Spanned)) return null;
            Layout layout = getLayout();
            if (layout == null) return null;
            int line = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(line, x);
            Spanned spanned = (Spanned) getText();
            ButtonSpan[] spans = spanned.getSpans(layout.getLineStart(line), layout.getLineEnd(line), ButtonSpan.class);
            for (int i = 0; i < spans.length; ++i) {
                ButtonSpan span = spans[i];
                if (
                    spanned.getSpanStart(span) <= offset && spanned.getSpanEnd(span) >= offset &&
                    layout.getPrimaryHorizontal(spanned.getSpanStart(span)) <= x && layout.getPrimaryHorizontal(spanned.getSpanEnd(span)) >= x
                ) {
                    return span;
                }
            }
            return null;
        }


        private ButtonSpan pressedSpan;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            ButtonSpan span = findSpan(event.getX() - getPaddingLeft(), (int) event.getY() - getPaddingTop());
            if (action == MotionEvent.ACTION_DOWN) {
                pressedSpan = span;
                if (pressedSpan != null) {
                    pressedSpan.setPressed(this, true);
                    return true;
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (pressedSpan != null) {
                    pressedSpan.setPressed(this, false);
                    if (action == MotionEvent.ACTION_UP && pressedSpan.onClickListener != null) {
                        pressedSpan.onClickListener.run();
                    }
                }
                pressedSpan = null;
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (pressedSpan != null && pressedSpan != span) {
                    pressedSpan.setPressed(this, false);
                    pressedSpan = null;
                }
            }
            return pressedSpan != null || super.onTouchEvent(event);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if (buttonToBeAdded != null && getMeasuredWidth() > 0) {
                SpannableString btn = new SpannableString(" btn");
                btn.setSpan(buttonToBeAdded, 1, btn.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                SpannableStringBuilder sb = new SpannableStringBuilder(
                        TextUtils.ellipsize(getText(), getPaint(), getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - buttonToBeAdded.getSize() - dp(4), TextUtils.TruncateAt.END)
                );
                sb.append(btn);
                setText(sb);
                buttonToBeAdded = null;
            }
        }

        ButtonSpan buttonToBeAdded;
        public void addButton(ButtonSpan span) {
            buttonToBeAdded = span;
        }
    }
}