package org.telegramIunzhakov.ui;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.Keep;

import org.telegramIunzhakov.messenger.Utilities;

@Keep
public abstract class IUpdateButton extends FrameLayout {
    @Keep
    public IUpdateButton(Context context) {
        super(context);
    }
    @Keep
    public void onTranslationUpdate(Utilities.Callback<Float> onTranslationUpdate) {}
    @Keep
    public void update(boolean animated) {}
}
