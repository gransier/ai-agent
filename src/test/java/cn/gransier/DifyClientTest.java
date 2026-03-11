package cn.gransier;

import cn.gransier.common.DifyStreamListener;
import cn.gransier.domain.query.AgentQuery;
import cn.gransier.util.DifyClient;

public class DifyClientTest {

    public static void main(String[] args) {

        AgentQuery agentQuery = new AgentQuery("What is AI?", "user-123", "");

        new DifyClient().stream("app-kn4j9PtM1SZLC5K7jL148MUm", "/v1/chat-messages", agentQuery, new DifyStreamListener() {
            @Override
            public void onMessage(String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete(String convId) {
                System.out.println("\n✅ Done. Conv ID: " + convId);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
