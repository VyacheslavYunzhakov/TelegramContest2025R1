package org.telegramIunzhakov.ui.Charts;

import android.animation.Animator;

import org.telegramIunzhakov.ui.Charts.data.ChartData;
import org.telegramIunzhakov.ui.Charts.view_data.StackLinearViewData;

public class PieChartViewData extends StackLinearViewData {

    float selectionA;
    float drawingPart;
    Animator animator;

    public PieChartViewData(ChartData.Line line) {
        super(line);
    }
}
