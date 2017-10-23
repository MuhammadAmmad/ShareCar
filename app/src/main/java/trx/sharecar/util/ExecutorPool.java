package trx.sharecar.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorPool {
    private static ExecutorService executor;
    public static ExecutorPool executorPool = new ExecutorPool();

    private ExecutorPool (){
        executor = Executors.newCachedThreadPool();
    }

    public void startThread(Runnable r){
        executor.execute(r);
    }

}
