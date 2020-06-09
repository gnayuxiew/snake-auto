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
    private Snake snakeWhenTakeAStep;
    private int foodLocation;
    private ArrayList<Integer> path;

    private boolean noPath;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        for (int i = 0; i < width * height; i++) {
            cells[i] = new Cell(i);
        }
        stage.setScene(scene);
        stage.show();
        gameInit();
        Thread snakeGo = new Thread(this::snakeGo);
        snakeGo.start();
    }

    private void gameInit() {
        snake = new Snake(width * height);
        generateFood();
    }

    private void generateFood() {
        ArrayList<Integer> notSnake = new ArrayList<>();//不是蛇身的所有节点，每次生成食物都要清空notSnake
        for (int i = 0; i < width * height; i++) {
            notSnake.add(i);
        }
        for (int i = 0; i < snake.len; i++) {
            notSnake.remove((Integer) (snake.body[i]));//在整张地图中去除蛇的占位，得到非蛇(即可生成食物的节点)，注意删除的是Integer对象
        }
        if (notSnake.size() != 0) {
            foodLocation = notSnake.get((int) (Math.random() * notSnake.size()));

            cells[foodLocation].setVisible(true);
        } else {
            System.out.println("WIN");
        }

    }

    private void bfsGo() {
        int[] to = new int[width * height];//从起点到一个已知路径上的最后一个顶点
        path = new ArrayList<>();
        LinkedList<Integer> queue = new LinkedList<>();
        int source = snake.head;
        int target = foodLocation;

        for (int i = 0; i < snake.len; i++) {
            cells[snake.body[i]].visited = true;//蛇的身体设成以访问
        }
        queue.offer(source);

        while (!queue.isEmpty()) {

            int current = queue.remove();
            for (Integer i : neighbors(current)) {
                if (noPath) {
                    System.out.println(cells[i].visited);
                }
                if (!cells[i].visited) {
                    queue.offer(i);
                    cells[i].visited = true;
                    to[i] = current;
                }
            }
        }


        if (to[target] == 0) {
            noPath = true;
            snake.checkDanger();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Cell cell : cells) {//清空所有访问标志位，防止每次bfs的累积
                cell.visited = false;
            }
        } else {
            noPath = false;
            for (int i = target; i != source; i = to[i]) {
                path.add(i);
            }
            for (Cell cell : cells) {//清空所有访问标志位，防止每次bfs的累积
                cell.visited = false;
            }
        }


    }

    private ArrayList<Integer> neighbors(int x) {
        ArrayList<Integer> neighbors = new ArrayList<>();
        if (x / width != 0) {
            neighbors.add(x - width);
        }
        if (x / width != height - 1) {
            neighbors.add(x + width);
        }
        if (x % width != 0) {
            neighbors.add(x - 1);
        }
        if (x % width != width - 1) {
            neighbors.add(x + 1);
        }
        return neighbors;
    }


    private void snakeGo() {
        while (true) {
            bfsGo();
            if (snake.biteSelf()) {//判断有没有咬到自己
                cells[snake.head].setFill();
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!noPath) {
                switch (snake.head - path.get(path.size() - 1)) {//path的最后一个为与蛇头相邻的节点，最后一个为与target相邻的节点，每走一步就会更新path数组
                    case width:
                        snake.changeDirection(Direction.UP);
                        break;
                    case -width:
                        snake.changeDirection(Direction.DOWN);
                        break;
                    case -1:
                        snake.changeDirection(Direction.RIGHT);
                        break;
                    case 1:
                        snake.changeDirection(Direction.LEFT);
                        break;
                }
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
        boolean visited;
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

        void checkDanger() {
            ArrayList<Integer> neighbors = neighbors(head);
            for (int i = 0; i < len; i++) {
                neighbors.remove((Integer) body[i]);
            }
            if (neighbors.size() != 0) {
                switch (head - neighbors.get(0)) {
                    case width:
                        if (d != Direction.DOWN) changeDirection(Direction.UP);
                        break;
                    case -width:
                        if (d != Direction.UP) changeDirection(Direction.DOWN);
                        break;
                    case -1:
                        if (d != Direction.LEFT) changeDirection(Direction.RIGHT);
                        break;
                    case 1:
                        if (d != Direction.RIGHT) changeDirection(Direction.LEFT);
                        break;
                }
            }
        }
    }
}
