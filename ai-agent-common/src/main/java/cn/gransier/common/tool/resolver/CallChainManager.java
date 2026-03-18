package cn.gransier.common.tool.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class CallChainManager {

    public static final int DEFAULT_MAX_DEPTH = 10;
    public static final int DEFAULT_MAX_TOOLS_PER_TURN = 5;
    
    private final int maxDepth;
    private final int maxToolsPerTurn;
    
    private final ThreadLocal<LinkedList<ToolCallRecord>> callStack = ThreadLocal.withInitial(LinkedList::new);

    public CallChainManager() {
        this(DEFAULT_MAX_DEPTH, DEFAULT_MAX_TOOLS_PER_TURN);
    }

    public CallChainManager(int maxDepth, int maxToolsPerTurn) {
        this.maxDepth = maxDepth;
        this.maxToolsPerTurn = maxToolsPerTurn;
    }

    public void enter(String toolName, String toolCallId) {
        LinkedList<ToolCallRecord> stack = callStack.get();
        ToolCallRecord record = new ToolCallRecord(toolName, toolCallId);
        stack.addLast(record);
        log.debug("Enter tool: {} (depth: {}/{})", toolName, stack.size(), maxDepth);
    }

    public void exit(String toolCallId) {
        LinkedList<ToolCallRecord> stack = callStack.get();
        if (!stack.isEmpty() && stack.getLast().toolCallId().equals(toolCallId)) {
            stack.removeLast();
        }
        log.debug("Exit tool: {} (depth: {})", toolCallId, stack.size());
    }

    public boolean shouldTerminate() {
        int currentDepth = callStack.get().size();
        return currentDepth >= maxDepth;
    }

    public boolean shouldTerminateForToolCount(int count) {
        return count > maxToolsPerTurn;
    }

    public int getCurrentDepth() {
        return callStack.get().size();
    }

    public List<ToolCallRecord> getCallHistory() {
        return new ArrayList<>(callStack.get());
    }

    public void reset() {
        callStack.get().clear();
        log.debug("Call chain reset");
    }

    public void clear() {
        callStack.remove();
        log.debug("Call chain thread local cleared");
    }

    public record ToolCallRecord(String toolName, String toolCallId, long timestamp) {
        public ToolCallRecord(String toolName, String toolCallId) {
            this(toolName, toolCallId, System.currentTimeMillis());
        }
    }
}
