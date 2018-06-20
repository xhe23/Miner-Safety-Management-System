package edu.uic.cs442.msms.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import edu.uic.cs442.msms.R;
import edu.uic.cs442.msms.manager.DataManager;
import edu.uic.cs442.msms.model.History;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class DataExploreFragment extends DialogFragment {
    private ColumnChartView chart;
    private ColumnChartData data;
    private int Time;
    //private final double[] dangerLevels = {0, 66, 44, 35, 59, 78, 22, 45, 46, 34, 45, 14, 49, 5, 89, 95};
    private final int dangerLevelsSize = 16;

    private DataManager dataManager = DataManager.getInstance();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_data_explore, null);

        chart = v.findViewById(R.id.columnChart);

        Time = dataManager.getHistorySize();

        generateData();

        // Disable viewport recalculations, see toggleCubic() method for more info.
        chart.setViewportCalculationEnabled(false);

        resetViewport();

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }


    private void generateData() {

        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;

        ArrayList<History> dangerLevels = dataManager.getHistories();

        float value;

        for(History history : dangerLevels){
            values = new ArrayList<>();
            value = (float) history.getDangerLevels();

            if(value > 85){
                values.add(new SubcolumnValue((float) history.getDangerLevels(), ChartUtils.COLOR_RED));
            } else {
                values.add(new SubcolumnValue((float) history.getDangerLevels(), ChartUtils.COLOR_BLUE));
            }

            columns.add(new Column(values));
        }

        ColumnChartData data = new ColumnChartData(columns);

        data.setAxisXBottom(new Axis().setName("Time"));
        data.setAxisYLeft(new Axis().setName("Danger Level percentage(%)").setHasLines(true));
        chart.setColumnChartData(data);

    }

    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = Time;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }
}
