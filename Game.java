import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Game extends JPanel implements ActionListener, KeyListener {

    private Timer timer;
    private final int DELAY = 16; // ~60 FPS

    private int playerX = 815, playerY = 210;
    private int playerSizeX = 146, playerSizeY = 110;
    private double playerSpeed = 8;
    private String playerDirection = "left";
    private boolean playerLost = false;
    private boolean playerWon = false;
    private boolean playerStoppedAtStopSign = false;
    private long stopSignStartTime = 0;
    private double stopElapsedSeconds = 0;

    private int aiX = 0, aiY = 210;
    private int aiSizeX = 146, aiSizeY = 110;
    private double aiSpeed = Math.random() * 5 + 5;
    private String aiDirection = "right";
    private boolean aiPausedAt200 = false;
    private boolean aiTurnedUp = false;
    private long aiStopStartTime = 0;

    private BufferedImage playerSprite, aiSprite, backgroundImage;

    private final int STOP_SIGN_X = 590;
    private final int STOP_SIGN_TOLERANCE = 10;
    private final long STOP_SIGN_REQUIRED_TIME = 2000; // 2 seconds
    private boolean allowMovementAfterStop = false;
    public Game() {
        setFocusable(true);
        setPreferredSize(new Dimension(960, 700));
        addKeyListener(this);
        initGame();
    }

    private void initGame() {
        loadAssets();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        if (!playerLost && !playerWon) {
            updateAI();
            checkCollisions();
            checkStopSign();
            checkWinCondition();
        }
        loadAssets();
    }

    private void updateAI() {
        if (!aiPausedAt200) {
            if (aiX < 200) {
                aiX += aiSpeed;
                aiDirection = "right";
                aiSizeX = 146;
                aiSizeY = 110;
            } else {
                aiPausedAt200 = true;
                aiStopStartTime = System.currentTimeMillis();
            }
        } else {
            if (System.currentTimeMillis() - aiStopStartTime > 1000) {
                if (!aiTurnedUp && aiX < 440) {
                    aiX += aiSpeed;
                    aiDirection = "right";
                    aiSizeX = 146;
                    aiSizeY = 110;
                } else {
                    aiTurnedUp = true;
                }
            }
        }

        if (aiTurnedUp) {
            aiY -= aiSpeed;
            aiDirection = "up";
            aiSizeX = 110;
            aiSizeY = 146;
        }
    }

    private void checkCollisions() {
        if ((playerX < 300 && (playerY < 200 || playerY > 300)) || (playerX > 525 && (playerY < 200 || playerY > 300))) {
            playerLost = true;
        }

        if (playerX < aiX + aiSizeX && playerX + playerSizeX > aiX &&
            playerY < aiY + aiSizeY && playerY + playerSizeY > aiY) {
            playerLost = true;
        }
    }

    private void checkStopSign() {
        boolean inStopZone = Math.abs(playerX - STOP_SIGN_X) <= STOP_SIGN_TOLERANCE;

        if (inStopZone) {
            if (!playerStoppedAtStopSign) {
                stopSignStartTime = System.currentTimeMillis();
                playerStoppedAtStopSign = true;
            } else {
                long elapsed = System.currentTimeMillis() - stopSignStartTime;
                stopElapsedSeconds = elapsed / 1000.0;
                if (elapsed >= STOP_SIGN_REQUIRED_TIME) {
                    allowMovementAfterStop = true;
                }
            }
        } else {
            if (playerStoppedAtStopSign && !allowMovementAfterStop) {
                playerLost = true;
            }

            if (!allowMovementAfterStop) {
                playerStoppedAtStopSign = false;
                stopElapsedSeconds = 0;
                stopSignStartTime = 0;
            }
        }
    }

    private void checkWinCondition() {
        if (playerY >= 505 && allowMovementAfterStop) {
            playerWon = true;
        }
    }

    private void loadAssets() {
        try {
            switch (playerDirection) {
                case "left": playerSprite = resize(ImageIO.read(new File("carleft.png")), playerSizeX, playerSizeY); break;
                case "right": playerSprite = resize(ImageIO.read(new File("carright.png")), playerSizeX, playerSizeY); break;
                case "up": playerSprite = resize(ImageIO.read(new File("carup.png")), playerSizeX, playerSizeY); break;
                case "down": playerSprite = resize(ImageIO.read(new File("cardown.png")), playerSizeX, playerSizeY); break;
            }

            switch (aiDirection) {
                case "left": aiSprite = resize(ImageIO.read(new File("aicarleft.png")), aiSizeX, aiSizeY); break;
                case "right": aiSprite = resize(ImageIO.read(new File("aicarright.png")), aiSizeX, aiSizeY); break;
                case "up": aiSprite = resize(ImageIO.read(new File("aicarup.png")), aiSizeX, aiSizeY); break;
                case "down": aiSprite = resize(ImageIO.read(new File("aicardown.png")), aiSizeX, aiSizeY); break;
            }

            backgroundImage = resize(ImageIO.read(new File("round1bg.png")), 960, 720);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage resize(BufferedImage originalImage, int width, int height) {
        Image scaled = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        if (backgroundImage != null) g.drawImage(backgroundImage, 0, 0, null);
        if (playerSprite != null) g.drawImage(playerSprite, playerX, playerY, null);
        if (aiSprite != null) g.drawImage(aiSprite, aiX, aiY, null);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        g.drawString("Player X: " + playerX + " | Y: " + playerY, 10, 20);

        // Text background for stop sign
        g.setColor(Color.WHITE);
        g.fillRoundRect(230, 15, 500, 30, 20, 20); // Background behind the text

        g.setColor(Color.BLUE);
        if (playerStoppedAtStopSign && !allowMovementAfterStop) {
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g.drawString(String.format("Stopping... %.2f / 2.00 seconds", stopElapsedSeconds), 250, 40);
        } else if (!playerStoppedAtStopSign && !allowMovementAfterStop) {
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g.drawString("Stop at the stop sign for 2 seconds!", 250, 40);
        } else if (allowMovementAfterStop) {
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g.drawString("You may proceed!", 350, 40);
        }

        // Win message with background
        if (playerWon) {
            g.setColor(Color.WHITE);
            g.fillRoundRect(300, 300, 360, 80, 20, 20);
            g.setColor(Color.GREEN);
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
            g.drawString("You Win!", 380, 350);
        }

        // Loss message
        if (playerLost) {
            g.setColor(Color.WHITE);
            g.fillRoundRect(300, 300, 360, 80, 20, 20);
            g.setColor(Color.RED);
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
            g.drawString("You Lost!", 380, 350);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && playerX >= -5) {
            playerX -= playerSpeed;
            playerDirection = "left";
            playerSizeX = 146;
            playerSizeY = 110;
        } else if (key == KeyEvent.VK_RIGHT && playerX <= 820) {
            playerX += playerSpeed;
            playerDirection = "right";
            playerSizeX = 146;
            playerSizeY = 110;
        } else if (key == KeyEvent.VK_UP && playerY >= -10) {
            playerY -= playerSpeed;
            playerDirection = "up";
            playerSizeX = 110;
            playerSizeY = 146;
        } else if (key == KeyEvent.VK_DOWN && playerY <= 500) {
            playerY += playerSpeed;
            playerDirection = "down";
            playerSizeX = 110;
            playerSizeY = 146;
        }

        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Crossroads Game");
        Game game = new Game();
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
