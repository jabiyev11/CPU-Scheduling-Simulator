package com.app.service;

import com.app.model.SchedulingAlgorithm;
import java.util.EnumSet;
import javafx.scene.control.CheckBox;

public final class AlgorithmSelector {

  private final CheckBox fcfsCheckBox;
  private final CheckBox priorityCheckBox;
  private final CheckBox roundRobinCheckBox;
  private final CheckBox sjfCheckBox;
  private final CheckBox srtfCheckBox;
  private final CheckBox lotteryCheckBox;
  private final CheckBox guaranteedCheckBox;
  private final CheckBox multilevelQueueCheckBox;

  public AlgorithmSelector(
      CheckBox fcfsCheckBox,
      CheckBox priorityCheckBox,
      CheckBox roundRobinCheckBox,
      CheckBox sjfCheckBox,
      CheckBox srtfCheckBox,
      CheckBox lotteryCheckBox,
      CheckBox guaranteedCheckBox,
      CheckBox multilevelQueueCheckBox
  ) {
    this.fcfsCheckBox = fcfsCheckBox;
    this.priorityCheckBox = priorityCheckBox;
    this.roundRobinCheckBox = roundRobinCheckBox;
    this.sjfCheckBox = sjfCheckBox;
    this.srtfCheckBox = srtfCheckBox;
    this.lotteryCheckBox = lotteryCheckBox;
    this.guaranteedCheckBox = guaranteedCheckBox;
    this.multilevelQueueCheckBox = multilevelQueueCheckBox;
  }

  public EnumSet<SchedulingAlgorithm> selected() {
    EnumSet<SchedulingAlgorithm> selected = EnumSet.noneOf(SchedulingAlgorithm.class);
    if (fcfsCheckBox.isSelected()) selected.add(SchedulingAlgorithm.FCFS);
    if (priorityCheckBox.isSelected()) selected.add(SchedulingAlgorithm.PRIORITY);
    if (roundRobinCheckBox.isSelected()) selected.add(SchedulingAlgorithm.ROUND_ROBIN);
    if (sjfCheckBox.isSelected()) selected.add(SchedulingAlgorithm.SHORTEST_JOB_FIRST);
    if (srtfCheckBox.isSelected()) selected.add(SchedulingAlgorithm.SHORTEST_REMAINING_TIME_FIRST);
    if (lotteryCheckBox.isSelected()) selected.add(SchedulingAlgorithm.LOTTERY);
    if (guaranteedCheckBox.isSelected()) selected.add(SchedulingAlgorithm.GUARANTEED);
    if (multilevelQueueCheckBox.isSelected()) selected.add(SchedulingAlgorithm.MULTILEVEL_QUEUE);
    return selected;
  }

  public void clearAll() {
    fcfsCheckBox.setSelected(false);
    priorityCheckBox.setSelected(false);
    roundRobinCheckBox.setSelected(false);
    sjfCheckBox.setSelected(false);
    srtfCheckBox.setSelected(false);
    lotteryCheckBox.setSelected(false);
    guaranteedCheckBox.setSelected(false);
    multilevelQueueCheckBox.setSelected(false);
  }

  public void addChangeListener(Runnable onChange) {
    fcfsCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
    priorityCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
    roundRobinCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
    sjfCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
    srtfCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
    lotteryCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
    guaranteedCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
    multilevelQueueCheckBox.selectedProperty().addListener((obs, o, n) -> onChange.run());
  }

}
