package org.telegramIunzhakov.ui;

import android.view.View;

public class ButtonData {
    private final ButtonType buttonType;
    private final int imageResId;
    private final int textId;
    private final View.OnClickListener clickListener;

    public ButtonData(ButtonType buttonType, int imageResId, int textId, View.OnClickListener clickListener) {
        this.buttonType = buttonType;
        this.imageResId = imageResId;
        this.textId = textId;
        this.clickListener = clickListener;
    }

    public int getPriority() { return buttonType.getPriority(); }
    public ButtonType getButtonType() { return buttonType; }
    public int getImageResId() { return imageResId; }
    public int getTextId() { return textId; }
    public View.OnClickListener getClickListener() { return clickListener; }
}
