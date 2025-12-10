// Handler.java
package com.bilibili.rec_system.service.recommend_filter;

public abstract class Handler {

    private Handler nextHandler;

    public Handler() {
    }

    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    protected abstract void handleRequest(Request request);

    public final void submit(Request request) {
        this.handleRequest(request);

        if (this.nextHandler != null && needFurtherProcess(request)) {
            this.nextHandler.submit(request);
        } else {
            System.out.println("推荐流程结束！");
        }
    }

    protected boolean needFurtherProcess(Request request) {
        return request.getRecommendations() != null && !request.getRecommendations().isEmpty();
    }
}