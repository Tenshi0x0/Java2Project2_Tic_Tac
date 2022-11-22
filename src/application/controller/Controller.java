package application.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    int color;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;

    private static volatile boolean TURN = false;

    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    String g = "000000000";

    Socket socket = null;
    Scanner res;
    PrintWriter req;

    boolean fl = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        try {
            socket = new Socket("127.0.0.1", 8888);
        } catch (Exception e) {
            System.err.println("cannot connect");
        }

        try {
            res = new Scanner(socket.getInputStream());
            req = new PrintWriter(socket.getOutputStream());
            req.println(g);
            req.flush();


            new Thread(() -> {
                if (res.next().equals("1")) {
                    TURN = true;
                    color = 1;
                } else {
                    TURN = false;
                    color = 2;
                }

                while (true) {
                    if (res.hasNext()) {
                        g = res.next();
                    }
                    if (Character.isDigit(g.charAt(0))) {
                        TURN = true;
                        g2Board();
                        Platform.runLater(this::drawChess);
                    } else {
                        if (!fl) {
                            fl = true;
                            System.out.println(g);
                        }
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                System.exit(0);
                            }
                        }, 2000);
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            if (TURN && refreshBoard(x, y)) {
                TURN = false;
                Board2g();
                req.println(g);
                req.flush();
                drawChess();
            }
        });
    }

    private void Board2g() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                sb.append(chessBoard[i][j]);
            }
        g = sb.toString();
    }

    private void g2Board() {
        for (int i = 0; i < 9; i++) {
            char c = g.charAt(i);
            int x = i / 3, y = i % 3;
            chessBoard[x][y] = c - '0';
        }
    }

    private boolean refreshBoard(int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
//            chessBoard[x][y] = !TURN ? PLAY_1 : PLAY_2;
            chessBoard[x][y] = color;
            drawChess();
            return true;
        }
        return false;
    }

    private void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
}
