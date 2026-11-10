import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener{
    class Block{
        int x,y, width,height;
        Image image;
 
        int startX, startY;
        char direction = 'U'; //R = right, L = left, U = up, D = down;
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height){
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            startX = x;
            startY = y;
        }

        void updateDirection(char direction){
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();

            this.x += this.velocityX;
            this.y += this.velocityY;
            for(Block wall : walls){
                if(checkCollision(this, wall)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                    break;
                }
            }
        }

        public void updateVelocity(){
            if(direction == 'U'){
                velocityX = 0;
                velocityY = -tileSize/4;
            }
            else if(direction == 'D'){
                velocityX = 0;
                velocityY = tileSize/4;
            }
            else if(direction == 'L'){
                velocityX = -tileSize/4;
                velocityY = 0;
            }
            else if(direction == 'R'){
                velocityX = tileSize/4;
                velocityY = 0;
            }
        }

        void reset(){
            this.x = startX;    
            this.y = startY;

        }
    }
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image orangeGhost;
    private Image pinkGhost;
    private Image redGhost;
    private Image blueGhost;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    HashSet<Block> walls;
    HashSet<Block> ghosts;
    HashSet<Block> foods;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random ranadom = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    PacMan(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true); //to make sure that only this panel can be focused and receive key events


        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        orangeGhost = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhost = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhost = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        blueGhost = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
    
        loadMap();
        for(Block ghost : ghosts){
            ghost.updateDirection(directions[ranadom.nextInt(4)]);
        }
        //how long it takes to start timer, milliseconds gone between frames
        // 1 second = 1000 milliseconds
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();

    }

    public void loadMap(){
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for(int r = 0; r < rowCount; r++){
            for(int c = 0; c < columnCount; c++){
                char tileMapChar = tileMap[r].charAt(c);

                int x = c * tileSize; //how many tiles from the left
                int y = r * tileSize; //how many tiles from the top

                if(tileMapChar == 'X'){ //block wall
                    Block wall = new Block(wallImage,x,y,tileSize,tileSize);
                    walls.add(wall);
                    //walls.add(new Block(x,y,tileSize,tileSize));
                }
                else if(tileMapChar == ' '){
                    foods.add(new Block(null, x + 14 , y + 14, 4,  4));
                }
                else if(tileMapChar == 'P'){ //Player
                    pacman = new Block(pacmanRightImage,x,y,tileSize,tileSize);
                }
                else if(tileMapChar == 'b'){ //blue ghost
                    ghosts.add(new Block(blueGhost,x,y,tileSize,tileSize));
                }
                else if(tileMapChar == 'o'){ //orange ghost
                    ghosts.add(new Block(orangeGhost,x,y,tileSize,tileSize));
                }
                else if(tileMapChar == 'p'){ //pink ghost
                    ghosts.add(new Block(pinkGhost,x,y,tileSize,tileSize));
                }
                else if(tileMapChar == 'r'){ //red ghost
                    ghosts.add(new Block(redGhost,x,y,tileSize,tileSize));
                }
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for(Block ghost : ghosts){
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for(Block wall : walls){
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for(Block food : foods){
            g.setColor(Color.YELLOW);
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        //score
        g.setFont(new Font("Arial", Font.BOLD, 20));
        if(gameOver){
            g.drawString("Game Over" + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else{
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
    }

    public void move(){
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //check for collisions with walls
        for(Block wall : walls){
            if(checkCollision(pacman, wall)){
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check for collision with ghosts
        for(Block ghost : ghosts){
            if(checkCollision(pacman, ghost)){
                lives--;
                if(lives == 0){
                    gameOver = true;
                    return;
                }
                pacman.reset();
                for(Block g : ghosts){
                    g.reset();
                }
            }

            if(ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D'){
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for(Block wall : walls){
                if(checkCollision(ghost, wall) ||ghost.x <= 0 || ghost.x + ghost.width >= boardWidth){
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    ghost.updateDirection(directions[ranadom.nextInt(4)]);
                    break;
                }
            }
        }

        //check for collision with food
        Block foodEaten = null;
        for(Block food : foods){
            if(checkCollision(pacman, food)){
                foodEaten = food;
                score += 10;
                break;
            }
        }
        foods.remove(foodEaten);

        if(foods.isEmpty()){
            loadMap();
            resetPosition();
        }
    }

    public boolean checkCollision(Block block1, Block block2){
        Rectangle rect1 = new Rectangle(block1.x, block1.y, block1.width, block1.height);
        Rectangle rect2 = new Rectangle(block2.x, block2.y, block2.width, block2.height);

        return rect1.intersects(rect2);
    }

    public void resetPosition(){
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;   
        for(Block ghost : ghosts){
            ghost.reset();
            ghost.updateDirection(directions[ranadom.nextInt(4)]);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameOver){
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if(gameOver){
            loadMap();
            resetPosition();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }

        //System.out.println("Key released: " + e.getKeyCode());
        if(e.getKeyCode() == KeyEvent.VK_UP){
            pacman.updateDirection('U');
        }
        else if(e.getKeyCode() == KeyEvent.VK_DOWN){
            pacman.updateDirection('D');
        }
        else if(e.getKeyCode() == KeyEvent.VK_LEFT){
            pacman.updateDirection('L');
        }
        else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            pacman.updateDirection('R');
        }
    
        if(pacman.direction == 'U'){
            pacman.image = pacmanUpImage;
        }
        else if(pacman.direction == 'D'){
            pacman.image = pacmanDownImage;
        }
        else if(pacman.direction == 'L'){
            pacman.image = pacmanLeftImage;
        }
        else if(pacman.direction == 'R'){
            pacman.image = pacmanRightImage;
        }
    }
}
