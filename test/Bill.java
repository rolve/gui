import static java.lang.Math.atan2;
import static java.lang.Math.max;

import java.util.Random;

public class Bill {
    
    private static final int width = 658;
    private static final int height = 432;
    private static final int billSize = 64;
    private static final int coinWidth = 16;
    private static final int coinHeight = 25;
    
    private static Random random = new Random();
    
    public static void main(String[] args) {
        Gui gui = new Gui("Bill", width, height);
        gui.setResizable(false);
        gui.setFontSize((int) (gui.getFontSize() * 1.5));
        gui.open();
        
        int t = 0;
        
        double x = width/2;
        double y = height/2;
        double vx = 0;
        double vy = 0;
        double acc = 0.15;
        
        int coinX = randomX();
        int coinY = randomY();
        
        int score = 0;
        int highscore = 0;
        int lives = 3;
        
        while(gui.isOpen()) {
            if(gui.isKeyPressed("w") || gui.isKeyPressed("up"))
                vy -= acc;
            if(gui.isKeyPressed("s") || gui.isKeyPressed("down"))
                vy += acc;
            if(gui.isKeyPressed("a") || gui.isKeyPressed("left"))
                vx -= acc;
            if(gui.isKeyPressed("d") || gui.isKeyPressed("right"))
                vx += acc;
            
            if(gui.wasKeyTyped("space"))
                lives = -1;

            if(x < billSize/2 || x + billSize/2 >= width) {
                vx = -vx;
                lives--;
            }
            if(y < billSize/2 || y + billSize/2 >= height) {
                vy = -vy;
                lives--;
            }
            
            x += vx;
            y += vy;
            
            double coinDist = (x - coinX) * (x - coinX) + (y - coinY) * (y - coinY);
            if(coinDist < (billSize + coinWidth) * (billSize + coinWidth) / 4) {
                score++;
                coinX = randomX();
                coinY = randomY();
            }
            
            if(lives < 0) {
                highscore = max(highscore, score);
                score = 0;
                lives = 3;
                x = width/2;
                y = height/2;
                vx = vy = 0;
            }
            
            int coinSprite = max(0, t / 3 % 40 - 35);
            gui.drawImage("background.png", 0, 0);
            gui.drawImage("coin" + coinSprite + ".png", coinX - coinWidth/2, coinY - coinHeight/25);
            gui.drawImage("bill.png", (int) (x - billSize/2), (int) (y - billSize/2), atan2(vy, vx));

            gui.setColor(255, 255, 255);
            gui.drawString("Score: " + score + "  Lives: " + lives, 10, 30);
            gui.setColor(200, 200, 0);
            gui.drawString("Highscore: " + highscore, 10, 60);
            
            gui.refresh(20);
            t++;
        }
    }
    
    private static int randomX() {
        return random.nextInt(width - 2 * billSize) + billSize;
    }
    
    private static int randomY() {
        return random.nextInt(height - 2 * billSize) + billSize;
    }
}
