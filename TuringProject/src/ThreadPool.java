import java.util.concurrent.*;

public class ThreadPool {

	private ThreadPoolExecutor executor;

	public ThreadPool(){
		executor=(ThreadPoolExecutor)Executors.newCachedThreadPool();
	}

	//metodo per l'esecuzione del task passato al threadpool
	public void Task(Worker t){ 
		executor.execute(t); 
	}

	//metodo per la terminazione del thread pool
	public void endServer() { 
		executor.shutdown();
	}
}