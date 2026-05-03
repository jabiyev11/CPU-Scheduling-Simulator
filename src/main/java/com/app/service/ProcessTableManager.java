package com.app.service;

import com.app.model.Process;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

public final class ProcessTableManager {

  private final TableView<Process> table;
  private final TableColumn<Process, Integer> pidColumn;
  private final TableColumn<Process, Integer> arrivalColumn;
  private final TableColumn<Process, Integer> burstColumn;
  private final TableColumn<Process, Integer> ioBurstColumn;
  private final TableColumn<Process, Integer> priorityColumn;
  private final ObservableList<Process> processes;

  private Runnable onEdit = () -> {};

  public ProcessTableManager(
      TableView<Process> table,
      TableColumn<Process, Integer> pidColumn,
      TableColumn<Process, Integer> arrivalColumn,
      TableColumn<Process, Integer> burstColumn,
      TableColumn<Process, Integer> ioBurstColumn,
      TableColumn<Process, Integer> priorityColumn,
      ObservableList<Process> processes
  ) {
    this.table = table;
    this.pidColumn = pidColumn;
    this.arrivalColumn = arrivalColumn;
    this.burstColumn = burstColumn;
    this.ioBurstColumn = ioBurstColumn;
    this.priorityColumn = priorityColumn;
    this.processes = processes;
  }

  public void setOnEdit(Runnable onEdit) {
    this.onEdit = onEdit;
  }

  public void configure() {
    table.setItems(processes);
    table.setEditable(true);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    pidColumn.setCellValueFactory(
        c -> new SimpleIntegerProperty(c.getValue().getPid()).asObject());
    arrivalColumn.setCellValueFactory(
        c -> new SimpleIntegerProperty(c.getValue().getArrivalTime()).asObject());
    burstColumn.setCellValueFactory(
        c -> new SimpleIntegerProperty(c.getValue().getBurstTime()).asObject());
    ioBurstColumn.setCellValueFactory(
        c -> new SimpleIntegerProperty(c.getValue().getIoBurstTime()).asObject());
    priorityColumn.setCellValueFactory(
        c -> new SimpleIntegerProperty(c.getValue().getPriority()).asObject());

    pidColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    arrivalColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    burstColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    ioBurstColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    priorityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

    pidColumn.setOnEditCommit(e -> {
      e.getRowValue().setPid(e.getNewValue());
      refresh();
    });
    arrivalColumn.setOnEditCommit(e -> {
      e.getRowValue().setArrivalTime(e.getNewValue());
      refresh();
    });
    // Keep remaining time aligned with edited burst time for reruns
    burstColumn.setOnEditCommit(e -> {
      Process p = e.getRowValue();
      p.setBurstTime(e.getNewValue());
      p.setRemainingTime(e.getNewValue());
      refresh();
    });
    ioBurstColumn.setOnEditCommit(e -> {
      e.getRowValue().setIoBurstTime(e.getNewValue());
      refresh();
    });
    priorityColumn.setOnEditCommit(e -> {
      e.getRowValue().setPriority(e.getNewValue());
      refresh();
    });
  }

  private void refresh() {
    table.refresh();
    onEdit.run();
  }

}
