package org.telegramIunzhakov.ui;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegramIunzhakov.ui.ActionBar.SimpleTextView;
import org.telegramIunzhakov.ui.ActionBar.Theme;

public class ProfileButton extends FrameLayout {
    public ProfileButton(Context context, ButtonData data) {
        super(context);
        init(context, data);
    }

    private void init(Context context, ButtonData data) {

        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(dpToPx(context, 8));
        background.setColor(Theme.isCurrentThemeDark() || Theme.isCurrentThemeNight() ? 0xff597590 : 0xff0E477F);
        background.setAlpha(64);
        setBackground(background);

        // Внутренний вертикальный контейнер
        LinearLayout innerLayout = new LinearLayout(context);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setGravity(Gravity.CENTER);

        LayoutParams innerParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        addView(innerLayout, innerParams);

        ImageView icon = new ImageView(context);
        icon.setImageResource(data.getImageResId());
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(context, 48),
                dpToPx(context, 48)
        );
        iconParams.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        innerLayout.addView(icon, iconParams);

        SimpleTextView textView = new SimpleTextView(context);
        textView.setText(data.getText());
        textView.setTextSize(12);
        textView.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dpToPx(context, 2);
        textParams.gravity = Gravity.CENTER;
        innerLayout.addView(textView, textParams);
        setOnClickListener(data.getClickListener());

        int padding = dpToPx(context, 4);
        setPadding(padding, padding, padding, padding);
    }

    private int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}

