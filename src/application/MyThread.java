package application;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MyThread extends Thread{

    Socket fir, sec;
    Scanner in;
    String g = "000000000";

    public MyThread(Socket fir, Socket sec) {
        this.fir = fir;
        this.sec = sec;
    }

    static int tot = 0;

    @Override
    public void run() {
        super.run();
        System.out.println("Server thread start!");
        try {
            in = new Scanner(fir.getInputStream());
            in.next();
            in = new Scanner(sec.getInputStream());
            in.next();

            write(fir, "1");
            write(sec, "2");

            while (true) {
                // 在当前玩家落子后，得到当前压缩棋盘信息 g。
                ++tot;
                System.out.println("current rounds: " + tot);
                if ((tot & 1) == 1) in = new Scanner(fir.getInputStream());
                else in = new Scanner(sec.getInputStream());
                g = in.next();
                System.out.println(g);

                // 判定是否终局
                if (hasWin(g)) {
                    if ((tot & 1) == 1) {
                        write(fir, "YouWin!");
                        write(sec, "YouLose!");
                    } else {
                        write(sec, "YouWin!");
                        write(fir, "YouLose!");
                    }
                    Exit();
                } else if (draw(g)) {
                    write(fir, "Draw!");
                    write(sec, "Draw!");

                    Exit();
                }

                write(fir, g);
                write(sec, g);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void Exit() {
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

    private boolean draw(String g) {
        int cnt=0;
        for(int i=0; i<g.length(); i++) if(g.charAt(i)!='0') cnt++;
        return cnt==9;
    }

    private boolean hasWin(String g) {
        for(int i=0; i<=6; i+=3) if(g.charAt(0+i)!='0' && g.charAt(0+i)==g.charAt(1+i) && g.charAt(1+i)==g.charAt(2+i)) return true;
        for(int i=0; i<=2; i++) if(g.charAt(0+i)!='0' && g.charAt(0+i)==g.charAt(3+i) && g.charAt(3+i)==g.charAt(6+i)) return true;
        if(g.charAt(0)!='0' && g.charAt(0)==g.charAt(4) && g.charAt(4)==g.charAt(8)) return true;
        if(g.charAt(4)!='0' && g.charAt(2)==g.charAt(4) && g.charAt(4)==g.charAt(6)) return true;
        return false;
    }

    public void write(Socket target, String str) {
        PrintWriter go;
        try {
            go = new PrintWriter(target.getOutputStream());
            go.println(str);
            go.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
