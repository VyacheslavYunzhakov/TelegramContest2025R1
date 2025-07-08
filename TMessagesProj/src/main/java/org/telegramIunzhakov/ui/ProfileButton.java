package org.telegramIunzhakov.ui;


import static org.telegramIunzhakov.messenger.AndroidUtilities.density;
import static org.telegramIunzhakov.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegramIunzhakov.ui.ActionBar.SimpleTextView;
import org.telegramIunzhakov.ui.ActionBar.Theme;

public class ProfileButton extends FrameLayout {

    private float baseIconSize;
    private float baseTextSize;
    private float basePadding;
    private float MAX_HEIGHT;
    private ImageView icon;
    private SimpleTextView textView;

    public ProfileButton(Context context, ButtonData data, float maxHeight ) {
        super(context);
        MAX_HEIGHT = maxHeight;
        baseIconSize = 26;
        baseTextSize = 13;
        basePadding = 8;
        init(context, data);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height > 0) {
            updateScale(height);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updateScale(int containerHeight) {
        float scale = containerHeight / (MAX_HEIGHT * density);

        LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
        iconParams.width = iconParams.height = (int) (baseIconSize * scale * density);
        icon.setLayoutParams(iconParams);

        textView.setTextSize((int) (baseTextSize * scale));

        int padding = (int) (basePadding * scale * density);
        setPadding(0, padding, 0, padding);

        GradientDrawable bg = (GradientDrawable) getBackground();
        bg.setCornerRadius(10 * scale * density);
    }

    private void init(Context context, ButtonData data) {

        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(dp(10));
        background.setColor(Theme.isCurrentThemeDark() || Theme.isCurrentThemeNight() ? 0xff465C72 : 0xff2B6296);
        background.setAlpha(102);
        setBackground(background);

        // Внутренний вертикальный контейнер
        LinearLayout innerLayout = new LinearLayout(context);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setGravity(Gravity.TOP);

        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        addView(innerLayout, innerParams);

        ImageView icon = new ImageView(context);
        icon.setImageResource(data.getImageResId());
        icon.setScaleType(ImageView.ScaleType.FIT_XY);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp(baseIconSize),
                dp(baseIconSize)
        );
        iconParams.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        innerLayout.addView(icon, iconParams);
        this.icon = icon;

        SimpleTextView textView = new SimpleTextView(context);
        textView.setText(data.getText());
        textView.setTextSize((int) baseTextSize);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dp(4);
        textParams.gravity = Gravity.CENTER;
        innerLayout.addView(textView, textParams);
        setOnClickListener(data.getClickListener());
        this.textView = textView;
    }
}

