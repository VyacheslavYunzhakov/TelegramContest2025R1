package org.telegramIunzhakov.ui.Charts.view_data;

import android.graphics.Paint;

import org.telegramIunzhakov.ui.Charts.BaseChartView;
import org.telegramIunzhakov.ui.Charts.data.ChartData;

public class StackLinearViewData extends LineViewData {

    public StackLinearViewData(ChartData.Line line) {
        super(line, false);
        paint.setStyle(Paint.Style.FILL);
        if (BaseChartView.USE_LINES) {
            paint.setAntiAlias(false);
        }
    }
}
