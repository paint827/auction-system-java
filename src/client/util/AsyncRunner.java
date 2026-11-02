package client.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.concurrent.Task;

public final class AsyncRunner {

    // Thread daemon: tắt app là tự tắt theo, không treo tiến trình
    private static final ExecutorService POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "client-worker");
        t.setDaemon(true);
        return t;
    });

    private AsyncRunner() { }

    public static <T> void run(Callable<T> work,
                               Consumer<T> onSuccess,
                               Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call(); // chạy ở luồng nền
            }
        };
        // 2 dòng dưới chạy lại ở luồng giao diện -> an toàn để sửa UI
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        POOL.execute(task);
    }
}