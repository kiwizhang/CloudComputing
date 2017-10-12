import java.sql.Timestamp;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TimeZone;


public class BankUserMultiThreaded {

    private static int balance;

    private static PriorityQueue<String> operations = new PriorityQueue<String>();

    public static Boolean authenticate() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println("ERROR: ABORT OPERATION, Authentication failed");
            return false;
        }
        return true;
    }

    private static Long get_time() {
        return System.nanoTime();
    }

    private static void wait_50ms() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            return;
        }
    }

    public static void withdraw(final int amt) {
        final String timestamp = new Timestamp(System.currentTimeMillis()
                + TimeZone.getTimeZone("EST").getRawOffset()).toString();
        System.out.print(timestamp + " of withdraw" + "\n");
        Thread t = new Thread(new Runnable() {
            public void run() {
                acquire_lock(timestamp);
                // 出这句话以后,queue已经不被sync了,所以可以往里加,只是不在peak而已
                System.out.print("I'm done.");
                int holdings = balance;
                if (!authenticate()) {
                    return;
                }
                if (holdings < amt) {
                    System.out.println("Overdraft Error: You have insufficient funds for this withdrawal. Balance = " + Integer.toString(holdings) + ". Amt = " + Integer.toString(amt));
                    release_lock();
                    return;
                }
                balance = holdings - amt;
                release_lock();
                System.out.println("Withdrew " + Integer.toString(amt) + " from funds. Now at " + Integer.toString(balance));
            }
        });
        t.start();
    }

    public static void deposit(final int amt) {
		/* TODO: After lunch, fill in this method to handle deposit operations
		 * Note to self: do NOT call authenticate here
		 */
        final String timestamp = new Timestamp(System.currentTimeMillis()
                + TimeZone.getTimeZone("EST").getRawOffset()).toString();
        System.out.print(timestamp + " of deposit" + "\n");
        Thread t = new Thread(new Runnable() {
            public void run() {
                acquire_lock(timestamp);
                balance = balance + amt;
                System.out.println("deposited " + Integer.toString(amt) + " to funds. Now at " + Integer.toString(balance));
                release_lock();
            }
        });
        t.start();
    }


    private static void acquire_lock(String timestamp) {
		/* TODO: Optional placeholder for acquiring the locks for handling concurrency
		 * I should use the operations field, or some other data structure here
		 */
        synchronized (operations) {
            operations.add(timestamp);

            Iterator it = operations.iterator();
            while (it.hasNext()){
                System.out.println ( "Value: "+ it.next());
            }
            System.out.println ( "Over");

            while (!timestamp.equals(operations.peek())) {
                try {
                    operations.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
    }

    private static void release_lock() {
		/* TODO: Optional placeholder for releasing the locks for handling concurrency
		 * Let's not forget to update the data structure before completion
		 */
        synchronized (operations) {
            operations.remove();
            operations.notifyAll();
        }
    }

    public static void test_case0() {
        balance = 0;
        deposit(100);
        wait_50ms();
        deposit(200);
        wait_50ms();
        deposit(700);
        wait_50ms();
        if (balance == 1000) {
            System.out.println("Test passed!");
        } else {
            System.out.println("Test failed!");
        }
    }

    public static void test_case1() {
        balance = 0;
        deposit(100);
        deposit(200);
        deposit(700);
    }

    public static void test_case2() {
        balance = 0;
        deposit(1000);
        withdraw(1000);
        withdraw(1000);
        withdraw(1000);
        withdraw(1000);
        withdraw(1000);
    }

    public static void test_case3() {
        balance = 0;
        withdraw(1000);
        deposit(500);
        deposit(500);
        withdraw(500);
        withdraw(500);
        withdraw(1000);
    }

    public static void test_case4() {
        balance = 0;
        deposit(2000);
        withdraw(500);
        withdraw(1000);
        withdraw(1500);
        deposit(4000);
        withdraw(2000);
        withdraw(2500);
        withdraw(3000);
        deposit(5000);
        withdraw(3500);
        withdraw(4000);
    }

    public static void main(String[] args) {
        // Uncomment Tests Cases as you go
        //test_case0();
        //test_case1();
        test_case2();
        //test_case3();
        //test_case4();
    }

}