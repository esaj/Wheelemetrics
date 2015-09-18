package com.github.gadgetfactory.wheelemetrics.ui.graph;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * @author esaj
 */
public class GraphDataControl
{
    private GraphView graph;
    private LineGraphSeries<DataPoint> dataSeries;

    public GraphDataControl(GraphView graph)
    {
        graph.removeAllSeries();
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        GridLabelRenderer labelRenderer = graph.getGridLabelRenderer();
        labelRenderer.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        labelRenderer.setGridColor(Color.argb(255, 0, 128, 0));
        labelRenderer.setHighlightZeroLines(true);
        labelRenderer.setVerticalLabelsColor(Color.argb(255, 0, 192, 0));
        labelRenderer.setHorizontalLabelsVisible(false);

        this.graph = graph;
        dataSeries = new LineGraphSeries<DataPoint>();
        dataSeries.setThickness(1);
        dataSeries.setColor(Color.argb(255, 0, 255, 0));
        this.graph.addSeries(dataSeries);

        clearGraph();
    }

    public synchronized void addDataPoint(double x, double y, boolean scrollToEnd)
    {
        dataSeries.appendData(new DataPoint(x, y), scrollToEnd, 500);
    }

    public synchronized void clearGraph()
    {
        graph.getViewport().setScalable(false);
        graph.getViewport().setScrollable(false);
        this.graph.getViewport().setMinX(0);
        this.graph.getViewport().setMaxX(200);
        this.graph.computeScroll();
        dataSeries.resetData(new DataPoint[0]);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
    }
}

