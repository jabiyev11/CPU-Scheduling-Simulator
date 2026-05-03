package com.app.service;

import com.app.model.MetricExtremes;
import com.app.model.MetricType;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public final class ComparisonPanelManager {

  private final DecimalFormat metricFormat = new DecimalFormat("0.000");

  private final TableView<SchedulerResult> comparisonTable = new TableView<>();
  private final Label verdictLabel = new Label("Run comparison to get a verdict.");
  private final BarChart<String, Number> waitingBarChart =
      createBarChart("Average Waiting Time", "Avg Waiting");

  private Tab comparisonTab;
  private MetricExtremes metricExtremes = MetricExtremes.empty();

  public void buildTab() {
    configureTableColumns();

    VBox root = new VBox(10);
    root.setPadding(new Insets(12));

    Label title = new Label("Algorithm Comparison");
    title.getStyleClass().add("section-label");
    verdictLabel.getStyleClass().add("verdict-label");

    comparisonTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    comparisonTable.setPrefHeight(220);
    waitingBarChart.setPrefHeight(280);

    root.getChildren().addAll(
        title,
        comparisonTable,
        waitingBarChart,
        verdictLabel
    );
    comparisonTab = new Tab("Comparison", root);
    comparisonTab.setClosable(false);
  }

  public Tab getTab() {
    return comparisonTab;
  }

  public void populate(List<SchedulerResult> results) {
    comparisonTable.getItems().setAll(results);
    metricExtremes = MetricExtremes.from(results);
    comparisonTable.refresh();
    updateWaitingChart(results);
    updateVerdict(results);
  }

  public void clear() {
    comparisonTable.getItems().clear();
    waitingBarChart.getData().clear();
    verdictLabel.setText("Run comparison to get a verdict.");
    metricExtremes = MetricExtremes.empty();
  }

  private void configureTableColumns() {
    TableColumn<SchedulerResult, String> algorithmCol = new TableColumn<>("Algorithm");
    algorithmCol.setCellValueFactory(
        c -> new ReadOnlyStringWrapper(c.getValue().algorithm().getLabel()));

    comparisonTable.getColumns().setAll(
        algorithmCol,
        metricColumn("Avg Waiting", "Average waiting time per process. Lower is better.",
            SchedulerResult::avgWaitingTime, MetricType.AVG_WAITING),
        metricColumn("Avg Turnaround", "Average completion - arrival time. Lower is better.",
            SchedulerResult::avgTurnaroundTime, MetricType.AVG_TURNAROUND),
        metricColumn("Avg Response", "Average first-run delay from arrival. Lower is better.",
            SchedulerResult::avgResponseTime, MetricType.AVG_RESPONSE),
        metricColumn("CPU Util%",
            "CPU busy percentage during simulation interval. Higher is better.",
            SchedulerResult::cpuUtilization, MetricType.CPU_UTIL),
        metricColumn("Throughput", "Completed processes per unit time. Higher is better.",
            SchedulerResult::throughput, MetricType.THROUGHPUT),
        metricColumn("Jain",
            "Jain's fairness index over per-process service share (burst/turnaround). Higher is better.",
            SchedulerResult::jainsFairness, MetricType.JAINS_FAIRNESS),
        metricColumn("Gini",
            "Gini coefficient over per-process service share (burst/turnaround). Lower is better.",
            SchedulerResult::giniCoefficient, MetricType.GINI),
        metricColumn("Min-Max Gap",
            "Max-min gap of per-process service share (burst/turnaround). Lower is better.",
            SchedulerResult::minMaxFairnessGap, MetricType.MIN_MAX_GAP)
    );
  }

  private TableColumn<SchedulerResult, Number> metricColumn(
      String header,
      String tooltip,
      MetricExtractor extractor,
      MetricType metricType
  ) {
    TableColumn<SchedulerResult, Number> col = new TableColumn<>();
    Label label = new Label(header);
    label.setTooltip(new Tooltip(tooltip));
    col.setGraphic(label);
    col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractor.extract(c.getValue())));
    col.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
          return;
        }
        SchedulerResult row = getTableRow().getItem();
        setText(metricFormat.format(item.doubleValue()));
        // Highlight best and worst values so tradeoffs are easy to compare
        if (metricExtremes.isBest(metricType, row.algorithm())) {
          setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #14532d;");
        } else if (metricExtremes.isWorst(metricType, row.algorithm())) {
          setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #7f1d1d;");
        } else {
          setStyle("");
        }
      }
    });
    return col;
  }

  private void updateWaitingChart(List<SchedulerResult> results) {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Avg Waiting");
    for (SchedulerResult r : results) {
      XYChart.Data<String, Number> data =
          new XYChart.Data<>(shortName(r.algorithm()), r.avgWaitingTime());
      series.getData().add(data);
    }
    waitingBarChart.getData().setAll(series);
    Platform.runLater(() -> {
      for (SchedulerResult r : results) {
        XYChart.Data<String, Number> dataPoint = series.getData().stream()
            .filter(d -> d.getXValue().equals(shortName(r.algorithm())))
            .findFirst()
            .orElse(null);
        if (dataPoint != null && dataPoint.getNode() != null) {
          Tooltip.install(
              dataPoint.getNode(),
              new Tooltip(
                  r.algorithm().getLabel()
                      + System.lineSeparator()
                      + "Avg Waiting: "
                      + metricFormat.format(r.avgWaitingTime())
              )
          );
        }
      }
    });
  }

  private void updateVerdict(List<SchedulerResult> results) {
    if (results.isEmpty()) {
      verdictLabel.setText("Best overall: N/A");
      return;
    }
    // Lowest waiting time first, then lowest turnaround
    SchedulerResult best = results.stream()
        .min(Comparator.comparingDouble(SchedulerResult::avgWaitingTime)
            .thenComparingDouble(SchedulerResult::avgTurnaroundTime))
        .orElse(results.get(0));
    verdictLabel.setText(
        "Best overall: " + best.algorithm().getLabel() +
            " (lowest avg waiting, turnaround tie-break)."
    );
  }

  private static BarChart<String, Number> createBarChart(String title, String yLabel) {
    CategoryAxis xAxis = new CategoryAxis();
    xAxis.setLabel("Algorithm");
    xAxis.setTickLabelRotation(0);
    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel(yLabel);
    yAxis.setForceZeroInRange(false);
    yAxis.setAutoRanging(true);
    BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
    chart.setTitle(title);
    chart.setLegendVisible(false);
    chart.setAnimated(false);
    return chart;
  }

  private static String shortName(SchedulingAlgorithm algorithm) {
    return switch (algorithm) {
      case FCFS -> "FCFS";
      case PRIORITY -> "PRIO";
      case ROUND_ROBIN -> "RR";
      case SHORTEST_JOB_FIRST -> "SJF";
      case SHORTEST_REMAINING_TIME_FIRST -> "SRTF";
      case LOTTERY -> "LOT";
      case GUARANTEED -> "GUA";
      case MULTILEVEL_QUEUE -> "MLQ";
    };
  }
}
