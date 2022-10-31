import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SnakeGame implements Runnable {

    public static void main(String[] arg) {
        SwingUtilities.invokeLater(new SnakeGame());
    }
    private final GamePanel gamePanel;
    private final JButton restartButton;
    private final SnakeModel model;
    public SnakeGame() {
        this.model = new SnakeModel();
        this.restartButton = new JButton("Start Game");
        this.gamePanel = new GamePanel(model);
    }
    @Override
    public void run() {
        JFrame frame = new JFrame("Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(createButtonPanel(), BorderLayout.SOUTH);

        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(Color.black);

        restartButton.addActionListener(new ButtonListener(this, model));
        panel.add(restartButton);
        restartButton.setBackground(Color.DARK_GRAY);
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);

        return panel;
    }
    public JButton getRestartButton() {
        return restartButton;
    }
    public void repaint() {
        gamePanel.repaint();
    }
    public static class GamePanel extends JPanel {

        @Serial
        private static final long serialVersionUID = 1L;

        private final int margin;
        private final int scoreAreaHeight;
        private final int unitSize;

        private final SnakeGame.SnakeModel model;

        public GamePanel(SnakeGame.SnakeModel model) {
            this.model = model;
            this.margin = 10;
            this.unitSize = 25;
            this.scoreAreaHeight = 15 + margin;
            new Random();
            this.setBackground(Color.black);

            Dimension gameArea = model.getGameArea();
            int width = gameArea.width * unitSize + 2 * margin;
            int height = gameArea.height * unitSize + 2 * margin + scoreAreaHeight;
            this.setPreferredSize(new Dimension(width, height));
            setKeyBindings();
        }
        private void setKeyBindings() {
            InputMap inputMap = this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = this.getActionMap();

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");

            actionMap.put("up", new SnakeGame.MovementAction(model, 'U', 'D'));
            actionMap.put("down", new SnakeGame.MovementAction(model, 'D', 'U'));
            actionMap.put("left", new SnakeGame.MovementAction(model, 'L', 'R'));
            actionMap.put("right", new SnakeGame.MovementAction(model, 'R', 'L'));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Dimension gameArea = model.getGameArea();
            drawSnake(g);
            drawScore(g, gameArea);

            if (model.isGameOver) {
                drawGameOver(g);
            } else {
                drawApple(g);
            }
        }

        private void drawApple(Graphics g) {
            // Draw apple
            g.setColor(Color.WHITE);
            Point point = model.getAppleLocation();
            if (point != null) {
                int a = point.x * unitSize + margin + 1;
                int b = point.y * unitSize + margin + scoreAreaHeight + 1;
                g.fillOval(a, b, unitSize - 2, unitSize - 2);
            }
        }

        private void drawScore(Graphics g, Dimension gameArea) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Century Gothic", Font.BOLD, 20));
            FontMetrics metrics = getFontMetrics(g.getFont());
            int width = 2 * margin + gameArea.width * unitSize;
            String text = "SCORE: " + model.getApplesEaten();
            int textWidth = metrics.stringWidth(text);
            g.drawString(text, (width - textWidth) / 2, g.getFont().getSize());
        }

        private void drawSnake(Graphics g) {
            // Draw snake
            SnakeGame.Snake snake = model.getSnake();
            List<Point> cells = snake.getCells();
            Point cell = cells.get(0);
            drawSnakeCell(g, cell, Color.green);
            for (int index = 1; index < cells.size(); index++) {
                Color color = new Color(45, 180, 0);
                cell = cells.get(index);
                drawSnakeCell(g, cell, color);
            }
        }

        private void drawSnakeCell(Graphics g, Point point, Color color) {
            int x = margin + point.x * unitSize;
            int y = margin + scoreAreaHeight + point.y * unitSize;
            if (point.y >= 0) {
                g.setColor(color);
                g.fillRect(x, y, unitSize, unitSize);
            }
        }

        private void drawGameOver(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Century Gothic", Font.BOLD, 64));
            FontMetrics metrics = getFontMetrics(g.getFont());
            String text = "Game Over";
            int textWidth = metrics.stringWidth(text);
            g.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
        }

    }
    public static class ButtonListener implements ActionListener {
        private final SnakeGame.SnakeModel model;
        private final Timer timer;
        public ButtonListener(SnakeGame view, SnakeGame.SnakeModel model) {
            int delay = 80;
            this.model = model;
            this.timer = new Timer(delay, new SnakeGame.TimerListener(view, model));

        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JButton button = (JButton) event.getSource();
            String text = button.getText();

            if (text.equals("Start Game")) {
                button.setText("Restart Game");
            }

            button.setEnabled(false);
            model.initialize();
            timer.restart();
        }
    }
    public static class TimerListener implements ActionListener {
        private final SnakeGame view;
        private final SnakeModel model;

        public TimerListener(SnakeGame view, SnakeModel model) {
            this.view = view;
            this.model = model;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            moveSnake();
            checkApple();
            model.checkCollisions();
            if (model.isGameOver()) {
                Timer timer = (Timer) event.getSource();
                timer.stop();
                model.setRunning(false);
                view.getRestartButton().setEnabled(true);
            }
            view.repaint();
        }

        private void moveSnake() {
            Snake snake = model.getSnake();
            Point head = (Point) snake.getHead().clone();

            switch (snake.getDirection()) {
                case 'U' -> head.y--;
                case 'D' -> head.y++;
                case 'L' -> head.x--;
                case 'R' -> head.x++;
            }

            snake.removeTail();
            snake.addHead(head);
        }

        private void checkApple() {
            Point appleLocation = model.getAppleLocation();
            Snake snake = model.getSnake();
            Point head = snake.getHead();
            Point tail = (Point) snake.getTail().clone();

            if (head.x == appleLocation.x && head.y == appleLocation.y) {
                model.incrementApplesEaten();
                snake.addTail(tail);
                model.generateRandomAppleLocation();
            }
        }
    }
    public static class MovementAction extends AbstractAction {

        @Serial
        private static final long serialVersionUID = 1L;

        private final char newDirection, oppositeDirection;

        private final SnakeModel model;

        public MovementAction(SnakeModel model, char newDirection,
                              char oppositeDirection) {
            this.model = model;
            this.newDirection = newDirection;
            this.oppositeDirection = oppositeDirection;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isRunning()) {
                Snake snake = model.getSnake();
                char direction = snake.getDirection();
                if (direction != oppositeDirection && direction != newDirection) {
                    snake.setDirection(newDirection);
                }
            }
        }
    }
    public static class SnakeModel {
        public boolean isGameOver;
        private boolean isRunning;
        private int applesEaten;
        private final Dimension gameArea;
        private Point appleLocation;
        private final Random random;
        private final Snake snake;
        public SnakeModel() {
            this.random = new Random();
            this.snake = new Snake();
            this.gameArea = new Dimension(24, 24);
        }

        public void initialize() {
            this.isRunning = true;
            this.isGameOver = false;
            this.snake.initialize();
            this.applesEaten = 0;

            Point point = generateRandomAppleLocation();
            // Make sure first apple isn't under snake
            int y = (point.y == 0) ? 1 : point.y;
            this.appleLocation = new Point(point.x, y);
        }
        public void checkCollisions() {
            Point head = snake.getHead();

            // Check for snake going out of the game area
            // if out of area, continue
            if (head.x < 0) {
                snake.getHead().x = gameArea.width - 1;
            } else if (head.x > gameArea.width - 1) {
                snake.getHead().x = 0;
            }

            if (head.y < 0) {
                snake.getHead().y = gameArea.height - 1;
            } else if (head.y > gameArea.height - 1) {
                snake.getHead().y = 0;
            }

            // Check for snake touching itself
            List<Point> cells = snake.getCells();
            for (int index = 1; index < cells.size(); index++) {
                Point cell = cells.get(index);
                if (head.x == cell.x && head.y == cell.y) {
                    isGameOver = true;
                    return;
                }
            }
        }
        public Point generateRandomAppleLocation() {
            int x = random.nextInt(gameArea.width);
            int y = random.nextInt(gameArea.height);
            this.appleLocation = new Point(x, y);
            return getAppleLocation();
        }
        public void incrementApplesEaten() {
            this.applesEaten++;
        }
        public boolean isRunning() {
            return isRunning;
        }
        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }
        public boolean isGameOver() {
            return isGameOver;
        }
        public Dimension getGameArea() {
            return gameArea;
        }
        public int getApplesEaten() {
            return applesEaten;
        }
        public Point getAppleLocation() {
            return appleLocation;
        }
        public Snake getSnake() {
            return snake;
        }
    }
    public static class Snake {
        private char direction;
        private final List<Point> cells;
        public Snake() {
            this.cells = new ArrayList<>();
            initialize();
        }
        public void initialize() {
            this.direction = 'R';
            cells.clear();
            for (int x = 5; x >= 0; x--) {
                cells.add(new Point(x, 0));
            }
        }
        public void addHead(Point head) {
            cells.add(0, head);
        }
        public void addTail(Point tail) {
            cells.add(tail);
        }
        public void removeTail() {
            cells.remove(cells.size() - 1);
        }
        public Point getHead() {
            return cells.get(0);
        }
        public Point getTail() {
            return cells.get(cells.size() - 1);
        }
        public char getDirection() {
            return direction;
        }
        public void setDirection(char direction) {
            this.direction = direction;
        }
        public List<Point> getCells() {
            return cells;
        }
    }
}