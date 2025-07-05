package org.telegramIunzhakov.ui;

import android.view.View;

public class ButtonData {
    private final ButtonType buttonType;
    private final int imageResId;
    private final String text;
    private final View.OnClickListener clickListener;

    public ButtonData(ButtonType buttonType, int imageResId, String text, View.OnClickListener clickListener) {
        this.buttonType = buttonType;
        this.imageResId = imageResId;
        this.text = text;
        this.clickListener = clickListener;
    }

    public int getPriority() { return buttonType.getPriority(); }
    public ButtonType getButtonType() { return buttonType; }
    public int getImageResId() { return imageResId; }
    public String getText() { return text; }
    public View.OnClickListener getClickListener() { return clickListener; }
}
