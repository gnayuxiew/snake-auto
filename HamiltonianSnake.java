import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;

public class Main extends Application {
    enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private final int width = 20;
    private final int height = 20;
    private int size = 10;
    private Cell[] cells = new Cell[width * height];
    private Pane pane = new Pane();
    private Scene scene = new Scene(pane, width * size, height * size);

    private Snake snake;
    private int foodLocation;
    private ArrayList<Integer> hamiltonPath = new ArrayList<>();

    private int index = 1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        //初始化细胞
        for (int i = 0; i < width * height; i++) {
            cells[i] = new Cell(i);
        }
        stage.setScene(scene);
        stage.show();
        gameInit();
        //开始新线程
        Thread snakeGo = new Thread(this::snakeGo);
        snakeGo.start();
    }

    private void gameInit() {
        //初始化蛇，生成食物
        snake = new Snake(width * height);
        //生成食物
        generateFood();
        generatePath();
    }

    private void generatePath() {
        for (int i = 0; i < width; i++) {
            hamiltonPath.add(i);
        }
        int direction = -1;
        for (int i = width - 1; i >= 0; i--) {
            ArrayList<Integer> list = new ArrayList<>();
            for (int j = 1; j < height; j++) {
                list.add(i + j * width);
            }
            if (direction == 1) {
                Collections.reverse(list);
            }
            hamiltonPath.addAll(list);
            direction *= -1;
        }



    }

    private void generateFood() {
        //不是蛇身的所有节点，每次生成食物都要清空notSnake
        ArrayList<Integer> notSnake = new ArrayList<>();
        for (int i = 0; i < width * height; i++) {
            notSnake.add(i);
        }
        //在整张地图中去除蛇的占位，得到非蛇(即可生成食物的节点)，注意删除的是Integer对象
        for (int i = 0; i < snake.len; i++) {
            notSnake.remove((Integer) (snake.body[i]));
        }
        if (notSnake.size() != 0) {
            foodLocation = notSnake.get((int) (Math.random() * notSnake.size()));
            cells[foodLocation].setVisible(true);
        } else {
            System.out.println("WIN");
        }
    }

    private void takeShortCut() {
        System.out.println(snake.head + " " + snake.tail + " " + hamiltonPath.indexOf(snake.head) + " " + hamiltonPath.indexOf(snake.tail));

    }

    private void snakeGo() {
        while (true) {
            //判断有没有咬到自己
            if (snake.biteSelf()) {
                cells[snake.head].setFill();
                return;
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            takeShortCut();
            switch (snake.head - hamiltonPath.get(index)) {
                case width:
                    if (snake.d != Direction.DOWN) snake.changeDirection(Direction.UP);
                    break;
                case -width:
                    if (snake.d != Direction.UP) snake.changeDirection(Direction.DOWN);
                    break;
                case -1:
                    if (snake.d != Direction.LEFT) snake.changeDirection(Direction.RIGHT);
                    break;
                case 1:
                    if (snake.d != Direction.RIGHT) snake.changeDirection(Direction.LEFT);
                    break;
            }
            index++;
            if (index == hamiltonPath.size()) {
                index = 0;
            }


            switch (snake.d) {
                case UP:
                    if (snake.head / width == 0) {
                        cells[snake.head].setFill();
                        return;
                    }
                    if (cells[snake.head].getX() == cells[foodLocation].getX() && cells[snake.head].getY() - cells[foodLocation].getY() == size) {
                        snake.eat();
                    } else {
                        cells[snake.tail].setVisible(false);//尾巴消失
                        snake.updateBody();
                        snake.head -= width;
                        snake.updateHeadAndTail();
                    }
                    break;
                case RIGHT:
                    if (snake.head % width == width - 1) {
                        cells[snake.head].setFill();
                        return;
                    }
                    if (cells[snake.head].getY() == cells[foodLocation].getY() && cells[snake.head].getX() - cells[foodLocation].getX() == -size) {
                        snake.eat();
                    } else {
                        cells[snake.tail].setVisible(false);
                        snake.updateBody();
                        snake.head += 1;
                        snake.updateHeadAndTail();
                    }
                    break;
                case DOWN:
                    if (snake.head / width == height - 1) {
                        cells[snake.head].setFill();
                        return;
                    }
                    if (cells[snake.head].getX() == cells[foodLocation].getX() && cells[snake.head].getY() - cells[foodLocation].getY() == -size) {
                        snake.eat();
                    } else {
                        cells[snake.tail].setVisible(false);
                        snake.updateBody();
                        snake.head += width;
                        snake.updateHeadAndTail();
                    }
                    break;
                case LEFT:
                    if (snake.head % width == 0) {
                        cells[snake.head].setFill();
                        return;
                    }
                    if (cells[snake.head].getY() == cells[foodLocation].getY() && cells[snake.head].getX() - cells[foodLocation].getX() == size) {
                        snake.eat();
                    } else {
                        cells[snake.tail].setVisible(false);
                        snake.updateBody();
                        snake.head -= 1;
                        snake.updateHeadAndTail();
                    }
                    break;
            }
            cells[snake.head].setVisible(true);//显示新蛇头

        }

    }

    class Cell {
        int x;
        int y;
        Rectangle rectangle;

        Cell(int i) {
            x = i % width;
            y = i / width;
            rectangle = new Rectangle(x * size, y * size, size, size);
            rectangle.setStroke(Color.WHITE);
            rectangle.setVisible(false);
            pane.getChildren().add(rectangle);
        }

        void setVisible(boolean b) {
            Platform.runLater(() -> rectangle.setVisible(b));//如果在非Fx线程要执行Fx线程相关的任务，必须在Platform.runLater中执行
        }

        double getX() {
            return rectangle.getX();
        }

        double getY() {
            return rectangle.getY();
        }

        void setFill() {
            Platform.runLater(() -> rectangle.setFill(Color.RED));
        }
    }

    class Snake {
        int[] body;//身体容量，包含蛇每一节的位置
        int head;
        int tail;
        int len;
        Direction d = Direction.RIGHT;

        Snake(int size) {
            body = new int[size];
            head = body[0];//一开始只有一个头，位置在最左上角
            Platform.runLater(() -> cells[head].setVisible(true));//如果在非Fx线程要执行Fx线程相关的任务，必须在Platform.runLater中执行
            len = 1;
            tail = body[len - 1];
        }

        void changeDirection(Direction d) {
            this.d = d;
        }

        boolean biteSelf() {
            for (int i = 1; i < len; i++) {
                if (head == body[i]) {
                    return true;
                }
            }
            return false;
        }

        void eat() {
            if (len >= 0) System.arraycopy(body, 0, body, 1, len);
            len++;
            body[0] = foodLocation;
            head = foodLocation;
            generateFood();
        }

        void updateBody() {
            if (len - 1 >= 0) System.arraycopy(body, 0, body, 1, len - 1);
        }

        void updateHeadAndTail() {
            body[0] = head;
            tail = body[len - 1];
        }
    }
}
