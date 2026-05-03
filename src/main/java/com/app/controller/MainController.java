package com.app.controller;

import com.app.model.Process;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;
import com.app.service.AlgorithmSelector;
import com.app.service.ComparisonPanelManager;
import com.app.service.ProcessTableManager;
import com.app.service.ProcessValidator;
import com.app.service.SimulationService;
import com.app.util.GanttChartRenderer;
import com.app.util.RunLogWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public final class MainController {

  @FXML
  private Spinner<Integer> processCountSpinner;
  @FXML
  private Spinner<Integer> quantumSpinner;
  @FXML
  private ChoiceBox<String> inputModeChoiceBox;
  @FXML
  private Button initializeManualButton;
  @FXML
  private Button randomizeButton;
  @FXML
  private Button runButton;

  @FXML
  private CheckBox fcfsCheckBox;
  @FXML
  private CheckBox priorityCheckBox;
  @FXML
  private CheckBox roundRobinCheckBox;
  @FXML
  private CheckBox sjfCheckBox;
  @FXML
  private CheckBox srtfCheckBox;
  @FXML
  private CheckBox lotteryCheckBox;
  @FXML
  private CheckBox guaranteedCheckBox;
  @FXML
  private CheckBox multilevelQueueCheckBox;

  @FXML
  private TableView<Process> processTable;
  @FXML
  private TableColumn<Process, Integer> pidColumn;
  @FXML
  private TableColumn<Process, Integer> arrivalColumn;
  @FXML
  private TableColumn<Process, Integer> burstColumn;
  @FXML
  private TableColumn<Process, Integer> ioBurstColumn;
  @FXML
  private TableColumn<Process, Integer> priorityColumn;

  @FXML
  private VBox processDataPanel;
  @FXML
  private VBox ganttPanel;
  @FXML
  private TabPane resultsTabPane;
  @FXML
  private Label logPathLabel;

  private final SimulationService simulationService = new SimulationService();
  private final GanttChartRenderer chartRenderer = new GanttChartRenderer();

  private final ObservableList<Process> processes = FXCollections.observableArrayList();
  private final ProcessValidator validator = new ProcessValidator();
  private final ComparisonPanelManager comparison = new ComparisonPanelManager();
  private AlgorithmSelector algorithmSelector;
  private ProcessTableManager tableManager;

  private int defaultProcessCount;
  private int defaultQuantum;
  private boolean comparisonAutoRefresh;
  // Baseline width preserved across tab content updates
  private double lockedProcessPanelWidth = 430;

  @FXML
  public void initialize() {
    defaultProcessCount = simulationService.randomProcessCount();
    defaultQuantum = simulationService.randomQuantum();

    processCountSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, defaultProcessCount));
    quantumSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, defaultQuantum));
    processCountSpinner.setEditable(true);
    quantumSpinner.setEditable(true);

    inputModeChoiceBox.setItems(FXCollections.observableArrayList("Manual", "Randomized"));
    inputModeChoiceBox.setValue("Manual");

    algorithmSelector = new AlgorithmSelector(
        fcfsCheckBox, priorityCheckBox, roundRobinCheckBox, sjfCheckBox,
        srtfCheckBox, lotteryCheckBox, guaranteedCheckBox, multilevelQueueCheckBox);

    tableManager = new ProcessTableManager(
        processTable, pidColumn, arrivalColumn, burstColumn, ioBurstColumn, priorityColumn, processes);
    tableManager.setOnEdit(this::refreshComparisonIfEnabled);
    tableManager.configure();

    // Build and display the default comparison tab before any run
    comparison.buildTab();
    resultsTabPane.getTabs().add(comparison.getTab());
    resultsTabPane.getSelectionModel().select(comparison.getTab());

    configureModeBehavior();
    registerLiveUpdateHooks();
    lockInitialPanelWidth();
  }

  @FXML
  private void onInitializeManual() {
    // Initialize editable rows with minimal valid defaults for manual entry
    int count = processCountSpinner.getValue();
    List<Process> seed = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      seed.add(new Process(i, 0, 1, 1, 0));
    }
    processes.setAll(seed);
    refreshComparisonIfEnabled();
  }

  @FXML
  private void onRandomizeProcesses() {
    // Replace current rows with randomized input data
    processes.setAll(simulationService.randomizeProcesses(processCountSpinner.getValue()));
    refreshComparisonIfEnabled();
  }

  @FXML
  private void onRunSimulation() {
    try {
      // Validate UI input, run selected algorithms, then refresh result tabs
      List<Process> valid = validator.validateAndCopy(processes);
      int quantum = quantumSpinner.getValue();
      List<SchedulerResult> results =
          simulationService.runAlgorithms(valid, quantum, algorithmSelector.selected());
      renderResultTabs(results);
      comparison.populate(results);
      writeRunLog("Run Simulation", quantum, valid, results);
    } catch (IllegalArgumentException ex) {
      showError(ex.getMessage());
    } catch (Exception ex) {
      showError("Unexpected error: " + ex.getMessage());
    }
  }

  @FXML
  private void onResetAll() {
    // Reset controls, data, and result tabs to startup state
    comparisonAutoRefresh = false;

    processCountSpinner.getValueFactory().setValue(defaultProcessCount);
    quantumSpinner.getValueFactory().setValue(defaultQuantum);
    processCountSpinner.getEditor().setText(String.valueOf(defaultProcessCount));
    quantumSpinner.getEditor().setText(String.valueOf(defaultQuantum));

    inputModeChoiceBox.setValue("Manual");
    algorithmSelector.clearAll();
    processes.clear();
    comparison.clear();
    logPathLabel.setText("Not generated yet");

    resultsTabPane.getTabs().clear();
    resultsTabPane.getTabs().add(comparison.getTab());
    resultsTabPane.getSelectionModel().select(comparison.getTab());

    updateModeButtons("Manual");
  }

  private void renderResultTabs(List<SchedulerResult> results) {
    // Rebuild tabs from scratch so each run reflects current algorithm selection
    resultsTabPane.getTabs().clear();
    resultsTabPane.getTabs().add(comparison.getTab());
    for (SchedulerResult result : results) {
      Node chart = chartRenderer.render(
          result.scheduleLog(),
          result.algorithm().getLabel() + " Scheduling (Gantt Chart)"
      );
      Tab tab = new Tab(result.algorithm().getLabel());
      tab.setClosable(false);
      tab.setContent(chart);
      resultsTabPane.getTabs().add(tab);
    }
    enforcePanelLayout();
  }

  private void configureModeBehavior() {
    // Keep manual/randomized action buttons in sync with input mode
    updateModeButtons(inputModeChoiceBox.getValue());
    inputModeChoiceBox.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldMode, newMode) -> updateModeButtons(newMode));
  }

  private void updateModeButtons(String mode) {
    boolean manual = "Manual".equalsIgnoreCase(mode);
    initializeManualButton.setDisable(!manual);
    randomizeButton.setDisable(manual);
  }

  private void registerLiveUpdateHooks() {
    // Refresh comparison view whenever user edits key simulation inputs
    processCountSpinner.valueProperty().addListener((obs, o, n) -> refreshComparisonIfEnabled());
    quantumSpinner.valueProperty().addListener((obs, o, n) -> refreshComparisonIfEnabled());
    algorithmSelector.addChangeListener(this::refreshComparisonIfEnabled);
    processes.addListener(
        (ListChangeListener<? super Process>) change ->
            refreshComparisonIfEnabled());

    configureSpinnerInput(processCountSpinner);
    configureSpinnerInput(quantumSpinner);
  }

  private void lockInitialPanelWidth() {
    // Capture the post layout width so later content cannot collapse the left pane
    Platform.runLater(() -> {
      if (processDataPanel != null && processDataPanel.getWidth() > 0) {
        lockedProcessPanelWidth = processDataPanel.getWidth();
      }
      enforcePanelLayout();
    });
  }

  private void enforcePanelLayout() {
    // Reapply width constraints after dynamic tab/chart updates
    Platform.runLater(() -> {
      if (processDataPanel != null) {
        processDataPanel.setMinWidth(lockedProcessPanelWidth);
        processDataPanel.setPrefWidth(lockedProcessPanelWidth);
      }
      if (ganttPanel != null) {
        ganttPanel.setMinWidth(0);
      }
      if (resultsTabPane != null) {
        resultsTabPane.setMinWidth(0);
      }
    });
  }

  private void configureSpinnerInput(Spinner<Integer> spinner) {
    spinner.getEditor().setOnAction(event -> commitSpinnerEditor(spinner));
    spinner.focusedProperty().addListener((obs, hadFocus, hasFocus) -> {
      if (Boolean.TRUE.equals(hadFocus) && !hasFocus) {
        commitSpinnerEditor(spinner);
      }
    });
  }

  private void commitSpinnerEditor(Spinner<Integer> spinner) {
    SpinnerValueFactory<Integer> vf = spinner.getValueFactory();
    if (vf == null) {
      return;
    }
    try {
      Integer value = vf.getConverter().fromString(spinner.getEditor().getText());
      if (value != null) {
        vf.setValue(value);
      }
    } catch (Exception ignored) {
      spinner.getEditor().setText(vf.getValue().toString());
    }
  }

  private void refreshComparisonIfEnabled() {
    if (!comparisonAutoRefresh) {
      return;
    }
    try {
      List<Process> valid = validator.validateAndCopy(processes);
      List<SchedulerResult> results = simulationService.runAlgorithms(
          valid, quantumSpinner.getValue(), EnumSet.allOf(SchedulingAlgorithm.class));
      renderResultTabs(results);
      comparison.populate(results);
    } catch (Exception ignored) {
      // Keeping existing UI state while user is mid-edit
    }
  }

  private void showError(String message) {
    // Surface runtime errors to the user in a modal dialog
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText("Simulation error");
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void writeRunLog(String runType, int quantum, List<Process> validProcesses,
                           List<SchedulerResult> results) {
    try {
      Path path = RunLogWriter.writeRunLog(runType, quantum, validProcesses, results);
      if (logPathLabel != null) {
        logPathLabel.setText("Last log: " + path.toAbsolutePath());
      }
    } catch (Exception ex) {
      if (logPathLabel != null) {
        logPathLabel.setText("Log write failed: " + ex.getMessage());
      }
    }
  }
}

