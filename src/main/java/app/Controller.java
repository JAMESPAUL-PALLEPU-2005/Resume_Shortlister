package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.Application;
import app.Candidate;
import app.SkillWeight;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import rules.AllRules;
import rules.Rule;

/**
 * Main FXML controller for the Resume Classifier UI.
 * Handles directory browsing, skill set management, rule execution, and results display.
 */
public class Controller implements SkillSet {

    private final DirectoryChooser chooser = new DirectoryChooser();
    private Optional<File> file = Optional.empty();
    private final List<SkillSetView> skillSetViews = new ArrayList<>();

    @FXML private Label selectUserInputAlertLabel;
    @FXML private VBox mainVBox;
    @FXML private TextField directoryTextField;
    @FXML private Button execute;
    @FXML private Label executionMsg;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox resultsCard;
    @FXML private TableView<Candidate> resultsTable;
    @FXML private Label resultsCountLabel;

    @FXML private Label previewHeaderLabel;
    @FXML private javafx.scene.text.TextFlow previewTextFlow;

    /**
     * Called automatically after FXML loading. Initializes the first SkillSet.
     */
    public void initialize() {
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        if (resultsCard != null) {
            resultsCard.setVisible(false);
            resultsCard.setManaged(false);
        }
        if (resultsTable != null) {
            setupResultsTable();
        }
        addSkillSet();
    }

    /**
     * Configures the TableView columns, cell factories, row factory, and styling.
     * Uses UNCONSTRAINED resize so columns honour their preferred widths and the
     * table scrolls horizontally when the window is narrow.
     */
    @SuppressWarnings("unchecked")
    private void setupResultsTable() {
        resultsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        resultsTable.setPlaceholder(new Label("No results to display"));

        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showPreview(newSel);
            }
        });

        /* ──────────────────────────────────────────────
         * Row factory — applies top-1/2/3 highlight CSS
         * to entire rows so background bands are visible.
         * ────────────────────────────────────────────── */
        resultsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Candidate item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("top-row-1", "top-row-2", "top-row-3");
                if (!empty && item != null) {
                    int idx = getIndex();
                    if (idx == 0) getStyleClass().add("top-row-1");
                    else if (idx == 1) getStyleClass().add("top-row-2");
                    else if (idx == 2) getStyleClass().add("top-row-3");
                }
            }
        });

        // ── Column 1: Rank ──
        TableColumn<Candidate, Number> rankCol = new TableColumn<>("#");
        rankCol.setMinWidth(52);
        rankCol.setMaxWidth(62);
        rankCol.setPrefWidth(58);
        rankCol.setSortable(false);
        rankCol.setReorderable(false);
        rankCol.getStyleClass().add("rank-column");
        rankCol.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(resultsTable.getItems().indexOf(param.getValue()) + 1));
        rankCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("rank-top1", "rank-top2", "rank-top3");
                } else {
                    int rank = item.intValue();
                    String badge;
                    getStyleClass().removeAll("rank-top1", "rank-top2", "rank-top3");
                    if (rank == 1) {
                        badge = "🥇";
                        getStyleClass().add("rank-top1");
                    } else if (rank == 2) {
                        badge = "🥈";
                        getStyleClass().add("rank-top2");
                    } else if (rank == 3) {
                        badge = "🥉";
                        getStyleClass().add("rank-top3");
                    } else {
                        badge = String.valueOf(rank);
                    }
                    setText(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // ── Column 2: Candidate Name ──
        TableColumn<Candidate, String> nameCol = new TableColumn<>("Candidate Name");
        nameCol.setMinWidth(160);
        nameCol.setPrefWidth(240);
        nameCol.setSortable(false);
        nameCol.setReorderable(false);
        nameCol.getStyleClass().add("name-column");
        nameCol.setCellValueFactory(param ->
                new ReadOnlyStringWrapper(param.getValue().getName()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    setStyle("");
                } else {
                    setText(item);
                    int idx = getIndex();
                    if (idx >= 0 && idx < 3) {
                        setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");
                    } else {
                        setStyle("-fx-font-size: 13px;");
                    }
                    // Tooltip for long names
                    if (item.length() > 22) {
                        Tooltip tip = new Tooltip(item);
                        tip.setShowDelay(Duration.millis(250));
                        tip.setStyle("-fx-font-size: 13px;");
                        setTooltip(tip);
                    }
                }
            }
        });

        // ── Column 3: Score (with inline mini-bar) ──
        TableColumn<Candidate, Double> scoreCol = new TableColumn<>("Score");
        scoreCol.setMinWidth(100);
        scoreCol.setMaxWidth(130);
        scoreCol.setPrefWidth(115);
        scoreCol.setSortable(false);
        scoreCol.setReorderable(false);
        scoreCol.getStyleClass().add("score-column");
        scoreCol.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getScore()));
        scoreCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Build a mini HBox with a score label + colored bar
                    Label scoreLbl = new Label(String.format("%.1f%%", item));
                    scoreLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                    // Determine color based on score
                    String color;
                    if (item >= 70) {
                        color = "#00b894";
                    } else if (item >= 40) {
                        color = "#fdcb6e";
                    } else {
                        color = "#ff6b6b";
                    }
                    scoreLbl.setStyle(scoreLbl.getStyle() + " -fx-text-fill: " + color + ";");

                    // Mini progress bar behind the score
                    Region barBg = new Region();
                    barBg.setPrefHeight(4);
                    barBg.setPrefWidth(50);
                    barBg.setMaxWidth(50);
                    barBg.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 2;");

                    Region barFill = new Region();
                    barFill.setPrefHeight(4);
                    barFill.setPrefWidth(Math.max(2, item / 100.0 * 50));
                    barFill.setMaxWidth(50);
                    barFill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");

                    javafx.scene.layout.StackPane bar = new javafx.scene.layout.StackPane(barBg, barFill);
                    bar.setAlignment(Pos.CENTER_LEFT);
                    bar.setPrefWidth(50);

                    VBox cell = new VBox(3, scoreLbl, bar);
                    cell.setAlignment(Pos.CENTER);
                    cell.setPadding(new Insets(4, 0, 4, 0));

                    setText(null);
                    setGraphic(cell);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // ── Column 4: Matched Skills (FlowPane chips) ──
        TableColumn<Candidate, List<String>> skillsCol = new TableColumn<>("Matched Skills");
        skillsCol.setMinWidth(250);
        skillsCol.setPrefWidth(420);
        skillsCol.setSortable(false);
        skillsCol.setReorderable(false);
        skillsCol.getStyleClass().add("skills-column");
        skillsCol.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getMatchedSkills()));
        skillsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(List<String> skills, boolean empty) {
                super.updateItem(skills, empty);
                if (empty || skills == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else if (skills.isEmpty()) {
                    setText("—");
                    setGraphic(null);
                    setStyle("-fx-text-fill: #555b6e; -fx-font-style: italic;");
                } else {
                    // Render each skill as a chip inside a FlowPane
                    FlowPane flow = new FlowPane();
                    flow.setHgap(6);
                    flow.setVgap(5);
                    flow.setPadding(new Insets(4, 2, 4, 2));

                    for (String skill : skills) {
                        Label chip = new Label(skill.trim());
                        chip.getStyleClass().add("skill-chip");
                        flow.getChildren().add(chip);
                    }

                    setText(null);
                    setGraphic(flow);

                    // Tooltip for full list
                    String joined = String.join(", ", skills);
                    if (joined.length() > 25) {
                        Tooltip tip = new Tooltip(joined);
                        tip.setShowDelay(Duration.millis(250));
                        tip.setMaxWidth(450);
                        tip.setWrapText(true);
                        tip.setStyle("-fx-font-size: 12px;");
                        setTooltip(tip);
                    }
                }
            }
        });

        resultsTable.getColumns().setAll(rankCol, nameCol, scoreCol, skillsCol);
    }

    /**
     * Opens a directory chooser dialog for selecting the resume folder.
     */
    public void browseProfileDirectory(javafx.event.ActionEvent actionEvent) {
        chooser.setTitle("Choose Profiles Folder location");
        File chosenFile = chooser.showDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
        file = Optional.ofNullable(chosenFile);
        directoryTextField.setText(file.map(File::getAbsolutePath).orElse("No Directory Selected"));

        if (file.isPresent()) {
            selectUserInputAlertLabel.setText("");
            selectUserInputAlertLabel.getStyleClass().remove("alert-label");
        }
    }

    /**
     * Executes the classification and scoring on the selected directory.
     * Runs on a background thread to keep the UI responsive.
     */
    public void executeRule() {
        // Validate inputs
        if (file.isEmpty()) {
            showAlert("Please select a directory first", true);
            return;
        }

        // Check ALL skillsets have valid skills
        boolean allSkillsPresent = !skillSetViews.isEmpty();
        for (SkillSetView ssv : skillSetViews) {
            if (!ssv.areSkillsPresent()) {
                allSkillsPresent = false;
                break;
            }
        }

        if (!allSkillsPresent) {
            showAlert("Please add skill details to all skill sets", true);
            return;
        }

        // Build rule and collect skill weights on FX thread (accesses UI controls)
        Rule selectionRule = getSelectionRule();
        List<SkillWeight> skillWeights = collectAllSkillWeights();
        File selectedFolder = file.get();

        // Disable UI during execution
        execute.setDisable(true);
        execute.setText("Processing...");
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        executionMsg.setText("");
        selectUserInputAlertLabel.setText("");

        // Hide previous results
        if (resultsCard != null) {
            resultsCard.setVisible(false);
            resultsCard.setManaged(false);
        }

        // Run classification on background thread
        Task<List<Candidate>> classificationTask = new Task<>() {
            @Override
            protected List<Candidate> call() {
                return new Application().parseProfiles(selectedFolder, selectionRule, skillWeights);
            }
        };

        classificationTask.setOnSucceeded(event -> {
            execute.setDisable(false);
            execute.setText("▶  Execute");
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }

            List<Candidate> results = classificationTask.getValue();
            if (results != null && !results.isEmpty()) {
                showAlert("✓ Classified " + results.size() + " resumes successfully!", false);
                displayResults(results);
            } else {
                showAlert("✓ Complete — no matching resumes found", false);
            }
        });

        classificationTask.setOnFailed(event -> {
            execute.setDisable(false);
            execute.setText("▶  Execute");
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
            showAlert("✗ An error occurred during classification", true);
            classificationTask.getException().printStackTrace();
        });

        Thread thread = new Thread(classificationTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Collects SkillWeight objects from all SkillSetViews.
     */
    private List<SkillWeight> collectAllSkillWeights() {
        List<SkillWeight> allWeights = new ArrayList<>();
        for (SkillSetView ssv : skillSetViews) {
            allWeights.addAll(ssv.getSkillWeights());
        }
        return allWeights;
    }

    /**
     * Populates the TableView with scored candidates.
     */
    private void displayResults(List<Candidate> candidates) {
        if (resultsTable == null || resultsCard == null) return;

        ObservableList<Candidate> data = FXCollections.observableArrayList(candidates);
        resultsTable.setItems(data);

        // Dynamically size the table — rows are taller now (chips + mini-bars)
        int rowCount = candidates.size();
        double estimatedRowHeight = 52;
        double headerHeight = 42;
        double computedHeight = headerHeight + (rowCount * estimatedRowHeight) + 6;
        double maxHeight = 450;
        resultsTable.setPrefHeight(Math.min(computedHeight, maxHeight));

        // Update the count badge
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(String.valueOf(candidates.size()));
        }

        // Show card
        resultsCard.setVisible(true);
        resultsCard.setManaged(true);
    }

    /**
     * Displays a status message with appropriate styling.
     */
    private void showAlert(String message, boolean isError) {
        Platform.runLater(() -> {
            if (isError) {
                selectUserInputAlertLabel.setText(message);
                selectUserInputAlertLabel.getStyleClass().remove("success-label");
                if (!selectUserInputAlertLabel.getStyleClass().contains("alert-label")) {
                    selectUserInputAlertLabel.getStyleClass().add("alert-label");
                }
                executionMsg.setText("");
            } else {
                executionMsg.setText(message);
                executionMsg.getStyleClass().remove("alert-label");
                if (!executionMsg.getStyleClass().contains("success-label")) {
                    executionMsg.getStyleClass().add("success-label");
                }
                selectUserInputAlertLabel.setText("");
            }
        });
    }

    /**
     * Combines rules from all SkillSetViews into a single AllRules.
     */
    private Rule getSelectionRule() {
        ArrayList<Rule> combinedSkillSetRules = new ArrayList<>();
        for (SkillSetView skillSetView : skillSetViews) {
            combinedSkillSetRules.add(skillSetView.createRule());
        }
        return new AllRules(combinedSkillSetRules);
    }

    @Override
    public void addSkillSet() {
        SkillSetView skillSetView = new SkillSetView(this);
        mainVBox.getChildren().add(skillSetView);
        skillSetViews.add(skillSetView);
    }

    @Override
    public void removeSkillSet(SkillSetView skillSetView) {
        if (skillSetViews.size() != 1) {
            mainVBox.getChildren().remove(skillSetView);
            skillSetViews.remove(skillSetView);
        }
    }

    private void showPreview(Candidate candidate) {
        if (previewHeaderLabel == null || previewTextFlow == null) return;
        
        previewHeaderLabel.setText(String.format("%s (%.1f%%)", candidate.getName(), candidate.getScore()));
        previewTextFlow.getChildren().clear();

        String text = candidate.getResumeText();
        if (text == null || text.trim().isEmpty()) {
            javafx.scene.text.Text emptyText = new javafx.scene.text.Text("No preview text available.");
            emptyText.setStyle("-fx-font-style: italic; -fx-fill: #636e72;");
            previewTextFlow.getChildren().add(emptyText);
            return;
        }

        List<String> skills = candidate.getMatchedSkills();
        if (skills == null || skills.isEmpty()) {
            javafx.scene.text.Text normalText = new javafx.scene.text.Text(text);
            previewTextFlow.getChildren().add(normalText);
            return;
        }

        // Highlight matched skills
        String regex = skills.stream()
                .map(java.util.regex.Pattern::quote)
                .reduce((s1, s2) -> s1 + "|" + s2)
                .orElse("");

        if (regex.isEmpty()) {
            previewTextFlow.getChildren().add(new javafx.scene.text.Text(text));
            return;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(" + regex + ")").matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                previewTextFlow.getChildren().add(new javafx.scene.text.Text(text.substring(lastEnd, matcher.start())));
            }
            javafx.scene.text.Text matchedText = new javafx.scene.text.Text(matcher.group());
            matchedText.setStyle("-fx-font-weight: bold; -fx-fill: #16a085; -fx-background-color: #f1c40f;"); // Highlights in green
            previewTextFlow.getChildren().add(matchedText);
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            previewTextFlow.getChildren().add(new javafx.scene.text.Text(text.substring(lastEnd)));
        }
    }
}
