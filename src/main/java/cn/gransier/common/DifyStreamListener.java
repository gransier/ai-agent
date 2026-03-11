package cn.gransier.common;

public interface DifyStreamListener {

    void onMessage(String answer);

    void onComplete(String conversationId);

    void onError(Throwable error);
}
