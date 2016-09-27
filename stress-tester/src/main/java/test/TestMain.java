package test;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-07-17
 * Time: 오후 12:37
 * To change this template use File | Settings | File Templates.
 */
public class TestMain {

    public static void main(String[] args){

        System.out.print(System.getProperty("user.dir"));
//        new TestMain().ThreadRun();

    }

    public void ThreadRun(){
        Thread1 thread1 = new Thread1();
        thread1.setName("AAA");
        thread1.start();

        Thread2 thread2 = new Thread2();
        thread2.setName("BBB");
        thread2.start();
    }

    class Thread1 extends Thread{
        @Override
        public void run() {
            for (int i=0; i<10000; i++){
                try {
                    OfflineCache.getInstance().remove("AAA");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Thread2 extends Thread{
        @Override
        public void run() {
            for (int i=0; i<10000; i++){
                try {
                    OfflineCache.getInstance().remove("BBB");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
