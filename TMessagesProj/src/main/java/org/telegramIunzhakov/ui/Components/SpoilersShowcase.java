package org.telegramIunzhakov.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegramIunzhakov.ui.ActionBar.BaseFragment;
import org.telegramIunzhakov.ui.Components.spoilers.SpoilerEffect2;

public class SpoilersShowcase extends BaseFragment {

    @Override
    public View createView(Context context) {
        final ViewGroup renderView = new FrameLayout(context) {
            private SpoilerEffect2 spoilerEffect;

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                if (spoilerEffect == null) {
                    spoilerEffect = SpoilerEffect2.getInstance(this);
                }
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                spoilerEffect.draw(canvas, this, getMeasuredWidth(), getMeasuredHeight());
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                if (spoilerEffect != null) {
                    spoilerEffect.detach(this);
                    spoilerEffect = null;
                }
            }
        };

        FrameLayout container = new FrameLayout(context);
        container.addView(renderView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView = container;
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return true;
    }
}
