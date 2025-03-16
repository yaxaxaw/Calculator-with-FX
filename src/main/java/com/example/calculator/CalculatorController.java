package com.example.calculator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

public class CalculatorController {

    private double number1;
    private String operator = "";
    private boolean start = true;

    @FXML
    private Label output;

    @FXML
    private GridPane upperPanel;

    @FXML
    private void toggleUpperPanel(ActionEvent event) {
        upperPanel.setVisible(!upperPanel.isVisible());
    }

    @FXML
    public void initialize() {
        upperPanel.setVisible(false);
    }

    @FXML
    private void processNumpad(ActionEvent event) {
        if (start) {
            output.setText("");
            start = false;
        }
        String value = ((Button) event.getSource()).getText();

        // Если текущий текст — "0", заменяем его на введенное значение (кроме точки)
        if (output.getText().equals("0") && !value.equals(".")) {
            output.setText(value);
        } else {
            output.setText(output.getText() + value);
        }
    }

    @FXML
    private void processOperator(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        String value = ((Button) event.getSource()).getText();
        if (!value.equals("=")) {
            if (!operator.isEmpty()) {
                return;
            }
            operator = value;
            number1 = Double.parseDouble(output.getText());
            output.setText("");
        } else {
            if (operator.isEmpty()) {
                output.setText("ERROR");
                return;
            }
            if (output.getText().isEmpty()) {
                output.setText("ERROR");
                operator = "";
                start = true;
                return;
            }

            String expression = number1 + " " + operator + " " + output.getText();
            String rpn = infixToRPN(expression);
            if (rpn.equals("ERROR")) {
                output.setText("ERROR");
                operator = "";
                start = true;
                return;
            }

            double result = evaluateRPN(rpn);
            if (Double.isNaN(result)) {
                output.setText("ERROR");
            } else {
                output.setText(String.valueOf(result));
            }
            operator = "";
            start = true;
        }
    }

    @FXML
    private void clearOutput(ActionEvent event) {
        output.setText("0");
        start = true;
        operator = "";
        number1 = 0;
    }

    @FXML
    private void processPower(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        operator = "^";
        number1 = Double.parseDouble(output.getText());
        output.setText("");
    }

    @FXML
    private void processSqrt(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        double number = Double.parseDouble(output.getText());
        if (number < 0) {
            output.setText("ERROR");
        } else {
            output.setText(String.valueOf(Math.sqrt(number)));
        }
        start = true;
    }

    @FXML
    private void processAbs(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        double number = Double.parseDouble(output.getText());
        output.setText(String.valueOf(Math.abs(number)));
        start = true;
    }

    @FXML
    private void processRound(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        double number = Double.parseDouble(output.getText());
        output.setText(String.valueOf(Math.round(number)));
        start = true;
    }

    @FXML
    private void processOpenBracket(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        if (output.getText().equals("0")) {
            output.setText("("); // Удаляем ноль и добавляем скобку
        } else {
            output.setText(output.getText() + "(");
        }
    }

    @FXML
    private void processCloseBracket(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        if (output.getText().equals("0")) {
            output.setText(")"); // Удаляем ноль и добавляем скобку
        } else {
            output.setText(output.getText() + ")");
        }
    }

    @FXML
    private void processDecimalPoint(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        if (!output.getText().contains(".")) {
            if (output.getText().isEmpty() || output.getText().equals("0")) {
                output.setText("0."); // Добавляем "0.", если строка пуста или содержит только ноль
            } else {
                output.setText(output.getText() + ".");
            }
        }
    }

    @FXML
    private void processPercentage(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        try {
            double number = Double.parseDouble(output.getText());
            double result = number / 100.0; // Вычисляем процент
            output.setText(String.valueOf(result));
        } catch (NumberFormatException e) {
            output.setText("ERROR");
        }
    }

    @FXML
    private void processToggleSign(ActionEvent event) {
        if (output.getText().equals("ERROR")) {
            return;
        }
        try {
            double number = Double.parseDouble(output.getText());
            number = -number; // Меняем знак на противоположный
            output.setText(String.valueOf(number));
        } catch (NumberFormatException e) {
            output.setText("ERROR");
        }
    }

    private String infixToRPN(String expression) {
        StringBuilder output = new StringBuilder();
        Stack<Character> operators = new Stack<>();
        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('+', 1);
        precedence.put('-', 1);
        precedence.put('×', 2);
        precedence.put('÷', 2);
        precedence.put('^', 3);

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                output.append(c);
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    output.append(' ').append(operators.pop());
                }
                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop();
                } else {
                    return "ERROR";
                }
            } else if (precedence.containsKey(c)) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != ' ') {
                    output.append(' ');
                }
                while (!operators.isEmpty() && precedence.get(operators.peek()) >= precedence.get(c)) {
                    output.append(' ').append(operators.pop()).append(' ');
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            if (operators.peek() == '(' || operators.peek() == ')') {
                return "ERROR";
            }
            output.append(' ').append(operators.pop());
        }

        return output.toString().trim();
    }

    private double evaluateRPN(String rpn) {
        Stack<Double> stack = new Stack<>();
        String[] tokens = rpn.split(" ");

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (Character.isDigit(token.charAt(0))) {
                stack.push(Double.parseDouble(token));
            } else if (token.length() > 1 && token.charAt(0) == '-' && Character.isDigit(token.charAt(1))) {
                stack.push(Double.parseDouble(token));
            } else {
                if (stack.size() < 2) {
                    return Double.NaN;
                }
                double num2 = stack.pop();
                double num1 = stack.pop();
                switch (token) {
                    case "+":
                        stack.push(num1 + num2);
                        break;
                    case "-":
                        stack.push(num1 - num2);
                        break;
                    case "×":
                        stack.push(num1 * num2);
                        break;
                    case "÷":
                        if (num2 == 0) {
                            return Double.NaN;
                        }
                        stack.push(num1 / num2);
                        break;
                    case "^":
                        stack.push(Math.pow(num1, num2));
                        break;
                    default:
                        return Double.NaN;
                }
            }
        }

        if (stack.size() != 1) {
            return Double.NaN;
        }

        return stack.pop();
    }
}