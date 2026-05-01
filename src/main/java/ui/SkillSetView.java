package ui;

import java.util.ArrayList;
import java.util.List;

import app.SkillWeight;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import rules.AllRules;
import rules.AnyRule;
import rules.Rule;

/**
 * A visual component representing a set of skills with an AND/OR condition.
 * Contains multiple SkillView children and produces a combined Rule.
 * Also collects SkillWeight objects for resume scoring.
 */
public class SkillSetView extends VBox implements Skill {
    private final List<SkillView> skillViews = new ArrayList<>();
    private final ChoiceBox<String> andOrConditionChoiceBox;
    private Label removeSkillAlertLabel;

    SkillSetView(SkillSet skillSet) {
        super();
        this.getStyleClass().add("skill-set-card");
        this.fillWidthProperty().setValue(true);
        this.setSpacing(12);

        HBox hBox = new HBox();
        hBox.setSpacing(12);
        hBox.getStyleClass().add("skill-set-header");

        // "Select Resume When" label
        Label label = new Label("Select Resume When :");
        label.getStyleClass().add("section-label");
        hBox.getChildren().add(label);

        // AND/OR condition choice box
        andOrConditionChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(
                "Select a condition",
                "All of the Skills are met",
                "Any of the Skills are met"));
        andOrConditionChoiceBox.getStyleClass().add("choice-box");
        andOrConditionChoiceBox.setValue("Select a condition");
        andOrConditionChoiceBox.setOnAction(event -> {
            if (andOrConditionChoiceBox.getSelectionModel().getSelectedIndex() != 0 && skillViews.isEmpty()) {
                addSkill();
            }
        });
        hBox.getChildren().add(andOrConditionChoiceBox);

        // Add New SkillSet button
        Button addNewSkillSetButton = new Button("+ Add SkillSet");
        addNewSkillSetButton.getStyleClass().addAll("btn", "btn-secondary");
        addNewSkillSetButton.setOnAction(event -> {
            if (!skillViews.isEmpty()) {
                skillSet.addSkillSet();
            }
        });
        hBox.getChildren().add(addNewSkillSetButton);

        // Remove SkillSet button
        Button removeSkillSetButton = new Button("✕ Remove");
        removeSkillSetButton.getStyleClass().addAll("btn", "btn-danger");
        removeSkillSetButton.setOnAction(event -> {
            if (skillViews.isEmpty()) {
                skillSet.removeSkillSet(SkillSetView.this);
            } else {
                removeSkillAlertLabel.setText("Remove all skills first");
            }
        });
        hBox.getChildren().add(removeSkillSetButton);

        // Alert label
        removeSkillAlertLabel = new Label();
        removeSkillAlertLabel.getStyleClass().add("alert-label");
        hBox.getChildren().add(removeSkillAlertLabel);

        this.getChildren().add(hBox);
    }

    /**
     * Checks if at least one skill with valid details is present.
     */
    boolean areSkillsPresent() {
        return (!skillViews.isEmpty() && skillViews.get(0).areSkillDetailsPresent());
    }

    /**
     * Creates a combined Rule based on the selected condition and all child skills.
     * Returns an AllRules (always-true) no-op rule if no condition is selected,
     * preventing NullPointerException.
     */
    Rule createRule() {
        ArrayList<Rule> rules = new ArrayList<>();
        for (SkillView view : skillViews) {
            rules.add(view.createRule());
        }

        int selectedIndex = andOrConditionChoiceBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 1) {
            return new AllRules(rules);
        } else if (selectedIndex == 2) {
            return new AnyRule(rules);
        }

        // Default: if no condition selected, treat as "All rules must match"
        return new AllRules(rules);
    }

    /**
     * Collects SkillWeight objects from all child SkillViews for scoring.
     *
     * @return list of SkillWeight with skill names and their weights
     */
    List<SkillWeight> getSkillWeights() {
        List<SkillWeight> weights = new ArrayList<>();
        for (SkillView sv : skillViews) {
            String name = sv.getSkillName();
            int weight = sv.getWeight();
            if (!name.isEmpty() && weight > 0) {
                weights.add(new SkillWeight(name, weight));
            }
        }
        return weights;
    }

    @Override
    public void addSkill() {
        SkillView skillView = new SkillView(this);
        this.getChildren().add(skillView);
        skillViews.add(skillView);
        removeSkillAlertLabel.setText("");
    }

    @Override
    public void removeSkill(SkillView skillView) {
        this.getChildren().remove(skillView);
        skillViews.remove(skillView);
    }
}
