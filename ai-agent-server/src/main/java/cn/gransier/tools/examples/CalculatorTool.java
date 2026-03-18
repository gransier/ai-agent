package cn.gransier.tools.examples;

import cn.gransier.common.tool.annotation.Tool;
import cn.gransier.common.tool.annotation.ToolService;
import cn.gransier.common.tool.domain.BaseTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
@ToolService
public class CalculatorTool implements BaseTool {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    @Override
    public String getName() {
        return "calculate";
    }

    @Override
    public String getDescription() {
        return "执行数学计算，支持加减乘除和简单的数学函数";
    }

    @Override
    public Map<String, ToolParamDefinition> getParameters() {
        return Map.of(
                "expression", ToolParamDefinition.builder()
                        .name("expression")
                        .type("string")
                        .description("数学表达式，如 2+3*4, sqrt(16), pow(2,3)")
                        .required(true)
                        .build()
        );
    }

    @Override
    public Object invoke(Map<String, Object> params) {
        String expression = (String) params.get("expression");
        
        log.info("Calculating expression: {}", expression);
        
        try {
            double result = evaluateExpression(expression);
            Map<String, Object> successResult = new HashMap<>();
            successResult.put("expression", expression);
            successResult.put("result", result);
            successResult.put("success", true);
            return successResult;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("expression", expression);
            errorResult.put("result", null);
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    private double evaluateExpression(String expression) {
        expression = expression.replace(" ", "").toLowerCase();
        
        if (expression.startsWith("sqrt(") && expression.endsWith(")")) {
            String numStr = expression.substring(5, expression.length() - 1);
            return Math.sqrt(Double.parseDouble(numStr));
        }
        
        if (expression.startsWith("pow(") && expression.endsWith(")")) {
            String[] args = expression.substring(4, expression.length() - 1).split(",");
            return Math.pow(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
        }
        
        if (expression.startsWith("abs(") && expression.endsWith(")")) {
            String numStr = expression.substring(4, expression.length() - 1);
            return Math.abs(Double.parseDouble(numStr));
        }
        
        if (expression.startsWith("sin(") && expression.endsWith(")")) {
            String numStr = expression.substring(4, expression.length() - 1);
            return Math.sin(Math.toRadians(Double.parseDouble(numStr)));
        }
        
        if (expression.startsWith("cos(") && expression.endsWith(")")) {
            String numStr = expression.substring(4, expression.length() - 1);
            return Math.cos(Math.toRadians(Double.parseDouble(numStr)));
        }
        
        if (expression.startsWith("log(") && expression.endsWith(")")) {
            String numStr = expression.substring(4, expression.length() - 1);
            return Math.log(Double.parseDouble(numStr));
        }
        
        return evalSimpleExpression(expression);
    }

    private double evalSimpleExpression(String expr) {
        String exprWithoutParens = expr.replace("(", "").replace(")", "");
        if (NUMBER_PATTERN.matcher(exprWithoutParens).matches()) {
            return Double.parseDouble(exprWithoutParens);
        }
        
        String[] terms = expr.split("(?=[+-])");
        double result = 0;
        
        for (String term : terms) {
            if (term.contains("*") || term.contains("/")) {
                result += evalTerm(term);
            } else {
                result += Double.parseDouble(term);
            }
        }
        
        return result;
    }

    private double evalTerm(String term) {
        String[] factors = term.split("(?=[*/])");
        double result = parseNumber(factors[0]);
        
        for (int i = 1; i < factors.length; i += 2) {
            char op = factors[i].charAt(0);
            double value = parseNumber(factors[i + 1]);
            if (op == '*') {
                result *= value;
            } else if (op == '/') {
                result /= value;
            }
        }
        
        return result;
    }

    private double parseNumber(String num) {
        return Double.parseDouble(num.trim());
    }
}
