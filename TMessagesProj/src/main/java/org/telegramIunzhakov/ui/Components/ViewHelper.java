package org.telegramIunzhakov.ui.Components;

import android.view.View;

import org.telegramIunzhakov.messenger.AndroidUtilities;
import org.telegramIunzhakov.messenger.LocaleController;

public final class ViewHelper {

    private ViewHelper() {
    }

    public static void setPadding(View view, float padding) {
        final int px = padding != 0 ? AndroidUtilities.dp(padding) : 0;
        view.setPadding(px, px, px, px);
    }

    public static void setPadding(View view, float left, float top, float right, float bottom) {
        view.setPadding(AndroidUtilities.dp(left), AndroidUtilities.dp(top), AndroidUtilities.dp(right), AndroidUtilities.dp(bottom));
    }

    public static void setPaddingRelative(View view, float start, float top, float end, float bottom) {
        setPadding(view, LocaleController.isRTL ? end : start, top, LocaleController.isRTL ? start : end, bottom);
    }

    public static int getPaddingStart(View view) {
        return LocaleController.isRTL ? view.getPaddingRight() : view.getPaddingLeft();
    }

    public static int getPaddingEnd(View view) {
        return LocaleController.isRTL ? view.getPaddingLeft() : view.getPaddingRight();
    }
}
