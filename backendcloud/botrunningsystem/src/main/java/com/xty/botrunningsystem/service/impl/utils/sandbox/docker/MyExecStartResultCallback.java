package com.xty.botrunningsystem.service.impl.utils.sandbox.docker;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import java.io.Closeable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyExecStartResultCallback implements ResultCallback<Frame> {
    private final StringBuilder stringBuilder = new StringBuilder();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void onStart(Closeable closeable) {
    }

    @Override
    public void onNext(Frame object) {
        stringBuilder.append(new String(object.getPayload()));
    }

    @Override
    public void onError(Throwable throwable) {
        countDownLatch.countDown();
    }

    @Override
    public void onComplete() {
        countDownLatch.countDown();
    }

    @Override
    public void close() {
    }

    public String awaitCompletion() throws InterruptedException {
        countDownLatch.await(30L, TimeUnit.SECONDS); // Adjust the timeout as necessary
        return stringBuilder.toString();
    }
}
