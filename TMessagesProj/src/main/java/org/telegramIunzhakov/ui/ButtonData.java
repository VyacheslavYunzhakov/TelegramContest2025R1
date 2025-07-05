package org.telegramIunzhakov.ui;

import android.view.View;

public class ButtonData {
    private final int priority;
    private final int imageResId;
    private final String text;
    private final View.OnClickListener clickListener;

    public ButtonData(int priority, int imageResId, String text, View.OnClickListener clickListener) {
        this.priority = priority;
        this.imageResId = imageResId;
        this.text = text;
        this.clickListener = clickListener;
    }

    public int getPriority() { return priority; }
    public int getImageResId() { return imageResId; }
    public String getText() { return text; }
    public View.OnClickListener getClickListener() { return clickListener; }
}
