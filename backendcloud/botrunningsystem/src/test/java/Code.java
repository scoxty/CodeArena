import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Code {
    public static void main(String[] args) {
        try {
            // 尝试访问网络
            System.out.println("尝试访问外部网络...");
            Runtime.getRuntime().exec("ping -c 4 google.com");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 尝试读写文件系统
            System.out.println("尝试读写文件系统...");
            Runtime.getRuntime().exec("touch /sandbox/testFile.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 尝试执行可能的危险操作
            System.out.println("尝试执行潜在的危险操作...");
            Runtime.getRuntime().exec("rm -rf /");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("测试完成。");
    }
}
