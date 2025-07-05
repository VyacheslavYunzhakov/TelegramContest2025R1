package org.telegramIunzhakov.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileButton extends FrameLayout {
    public ProfileButton(Context context, ButtonData data) {
        super(context);
        init(context, data);
    }

    private void init(Context context, ButtonData data) {
        // Настройка фона с закруглёнными углами
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(dpToPx(context, 8));
        background.setColor(Color.WHITE); // Цвет фона
        setBackground(background);

        // Внутренний вертикальный контейнер
        LinearLayout innerLayout = new LinearLayout(context);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setGravity(Gravity.CENTER);

        // Параметры для размещения внутри FrameLayout
        LayoutParams innerParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        addView(innerLayout, innerParams);

        // Иконка
        ImageView icon = new ImageView(context);
        icon.setImageResource(data.getImageResId());
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(context, 48), // Размер иконки
                dpToPx(context, 48)
        );
        iconParams.gravity = Gravity.CENTER;
        innerLayout.addView(icon, iconParams);

        // Текст
        TextView textView = new TextView(context);
        textView.setText(data.getText());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK); // Цвет текста
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dpToPx(context, 4); // Отступ от иконки
        textParams.gravity = Gravity.CENTER;
        innerLayout.addView(textView, textParams);

        // Обработчик клика
        setOnClickListener(data.getClickListener());

        // Отступы внутри кнопки
        int padding = dpToPx(context, 16);
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

