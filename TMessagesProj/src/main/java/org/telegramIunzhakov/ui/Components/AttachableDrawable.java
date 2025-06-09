package org.telegramIunzhakov.ui.Components;

import android.view.View;

import org.telegramIunzhakov.messenger.ImageReceiver;

public interface AttachableDrawable {
    void onAttachedToWindow(ImageReceiver parent);
    void onDetachedFromWindow(ImageReceiver parent);

    default void setParent(View view) {}
}
