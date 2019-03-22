// 使用者類別
class SumClass {
    private long length;
    // 建構子
    public SumClass(long length) {
        this.length = length;
    }
    // 計算總和
    public long sum() {
        long temp = 0;
        for (int i = 1; i <= length; i++) {
            try {  // 暫停一段時間
                Thread.sleep((int)(Math.random()*10));
            }
            catch(InterruptedException e){ }
            temp += i;
        }
        return temp;
    }
}
// 執行緒類別
class SumThread extends SumClass implements Runnable {
    // 建構子
    public SumThread(long length) {
        super(length);
    }
    // 執行執行緒
    public void run() {
        System.out.println(Thread.currentThread() +
                "總和 = " + sum());
    }
}
// 主類別
public class RunnableTest {
    // 主程式
    public static void main(String[] args) {
        System.out.print("執行緒: ");
        System.out.println(Thread.currentThread());
        // 建立執行緒物件
        SumThread st1 = new SumThread(150);
        Thread t1 = new Thread(st1, "執行緒A");
        SumThread st2 = new SumThread(150);
        Thread t2 = new Thread(st2, "執行緒B");
        // 啟動執行緒
        t1.start();
        t2.start();
    }
}