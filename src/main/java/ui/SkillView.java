package ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import rules.MinimumWordCount;
import rules.Rule;

/**
 * A visual component for a single skill entry (skill name + min count + weight).
 * Creates a MinimumWordCount rule and provides weight for scoring.
 */
class SkillView extends HBox {
    private final TextField enterSkillTextField;
    private final TextField enterWordCountTextField;
    private final TextField enterWeightTextField;
    private Label alertLabel;

    SkillView(Skill skill) {
        super();
        this.getStyleClass().add("skill-row");
        this.setSpacing(10);

        // Skill label
        Label skillLabel = new Label("Skill :");
        skillLabel.getStyleClass().add("field-label");
        this.getChildren().add(skillLabel);

        // Skill name input
        enterSkillTextField = new TextField();
        enterSkillTextField.setPromptText("e.g. Java");
        enterSkillTextField.getStyleClass().add("text-input");
        enterSkillTextField.setPrefWidth(120);
        this.getChildren().add(enterSkillTextField);

        // Min count label
        Label countLabel = new Label("Min Count :");
        countLabel.getStyleClass().add("field-label");
        this.getChildren().add(countLabel);

        // Word count input
        enterWordCountTextField = new TextField();
        enterWordCountTextField.setPromptText("e.g. 2");
        enterWordCountTextField.getStyleClass().add("text-input");
        enterWordCountTextField.setPrefWidth(60);
        enterWordCountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericField(newValue, "Count must be a number");
        });
        this.getChildren().add(enterWordCountTextField);

        // Weight label
        Label weightLabel = new Label("Weight :");
        weightLabel.getStyleClass().add("field-label");
        this.getChildren().add(weightLabel);

        // Weight input
        enterWeightTextField = new TextField();
        enterWeightTextField.setPromptText("e.g. 30");
        enterWeightTextField.getStyleClass().add("text-input");
        enterWeightTextField.setPrefWidth(60);
        enterWeightTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericField(newValue, "Weight must be a number");
        });
        this.getChildren().add(enterWeightTextField);

        // Add Another Skill button
        Button addBtn = new Button("+ Skill");
        addBtn.getStyleClass().addAll("btn", "btn-small");
        addBtn.setOnAction(event -> skill.addSkill());
        this.getChildren().add(addBtn);

        // Remove Skill button
        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().addAll("btn", "btn-danger", "btn-small");
        removeBtn.setOnAction(event -> skill.removeSkill(SkillView.this));
        this.getChildren().add(removeBtn);

        // Validation alert label
        alertLabel = new Label();
        alertLabel.getStyleClass().add("alert-label");
        this.getChildren().add(alertLabel);
    }

    /**
     * Validates that the given value is numeric, showing an alert if not.
     */
    private void validateNumericField(String value, String errorMsg) {
        if (value == null || value.isEmpty()) {
            alertLabel.setText("");
        } else if (!value.matches("\\d+")) {
            alertLabel.setText(errorMsg);
        } else {
            alertLabel.setText("");
        }
    }

    /**
     * Returns true if skill name, a valid min count, and a valid weight are provided.
     */
    boolean areSkillDetailsPresent() {
        String skill = enterSkillTextField.getText();
        String count = enterWordCountTextField.getText();
        String weight = enterWeightTextField.getText();
        return (skill != null && !skill.trim().isEmpty())
                && (count != null && !count.trim().isEmpty() && count.trim().matches("\\d+"))
                && (weight != null && !weight.trim().isEmpty() && weight.trim().matches("\\d+"));
    }

    /**
     * Creates a MinimumWordCount rule from the entered skill name and count.
     */
    Rule createRule() {
        String word = enterSkillTextField.getText();
        String countText = enterWordCountTextField.getText().trim();

        if (word == null) word = "";

        int count = 1;
        if (countText != null && !countText.isEmpty() && countText.matches("\\d+")) {
            count = Integer.parseInt(countText);
        } else {
            alertLabel.setText("Count must be a number");
        }

        return new MinimumWordCount(word, count);
    }

    /**
     * Returns the skill name entered by the user.
     */
    String getSkillName() {
        String text = enterSkillTextField.getText();
        return (text != null) ? text.trim() : "";
    }

    /**
     * Returns the weight entered by the user, defaulting to 1 if invalid.
     */
    int getWeight() {
        String weightText = enterWeightTextField.getText();
        if (weightText != null && !weightText.trim().isEmpty() && weightText.trim().matches("\\d+")) {
            return Integer.parseInt(weightText.trim());
        }
        return 1;
    }
}