package Main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JOptionPane;

public class SnakeCanvas extends Canvas implements Runnable, KeyListener{

	private static final long serialVersionUID = -324142412L;
	
	private final int BOX_HEIGHT = 20;
	private final int BOX_WIDTH = 20;
	private final int GRID_WIDTH = 30;
	private final int GRID_HEIGHT = 30;
	
	private LinkedList<Point> snake = null;
	private Point fruit = null;
	private Point goldFruit = null;
	
	//Boolean to check if the golden fruit should still be drawn.
	private boolean drawGoldFruit = false;
	//Keeps the position of the golden fruit in case the normal fruit is eaten.
	private int goldFruitx;
	private int goldFruity;
	
	//Same for the normal fruit.
	private boolean drawFruit = false;
	private int fruitx;
	private int fruity;
	
	//Draws the menus.
	private boolean Menu = true;
	private Image menuImage = null;
	private boolean Ended = false;
	private Image EndedImage = null;
	private boolean Won = false;
	private Image WonImage = null;
	private boolean Pause = false;
	private Image PauseImage = null;
	
	//Score and high score.
	private int score = 0;
	private int scoreRaise = 125;
	private String highScore = "Nobody:0";

	//Speed of the snake.
	public int difficulty = 100;

	//Timer for golden fruit (depends on blocks not actual time).
	private int timer = 0;
	
	//Check if the inputOptions are wanted every time or only once.
	private boolean inputOptionsDone = false;
	private boolean firstInputOptionsTime = false;
	private boolean keepSettings = false;
	
	//Keys pressed (needed for the bug in which two keys are pressed in the same time.
	private LinkedList<Integer> keys;
	
	//Checks if that bug exists (used for Thread.sleep to make it look normal).
	boolean bug = false;
	
	//Name for the user with high score.
	private String name;
	//Path for the high score file(to make it not that easy to find).
	private String path = "_CommonRedist" + File.separator + "vcredist" + File.separator + "reslists" + File.separator + "oh" + 
			File.separator + "no" + File.separator + "please" + File.separator +"stop" + File.separator + "don't!" + File.separator + "FINE";
	
	//Initially , the snake does not move.
	private Direction direction = Direction.NO_DIRECTION;
	
	private Thread runThread;
	
	//The function which paints the screen (called with repaint())
	public void paint(Graphics g){
		
		//If the thread didn't start yet , starts it.
		if (runThread == null){
			this.addKeyListener(this);
			runThread = new Thread(this);
			Menu = true;
			Ended = false;
			Pause = false;
			Won = false;
			runThread.start();
		}
		
		//If the Menu is chosen , draws it.
		if (Menu == true){
			DrawMenu(g);
		}
		//if Ended menu is chosen , draws it.
		else if (Ended == true){
			//If user won , draws Won screen.
			if (Won == true){
				DrawWonGame(g);
			}
			//Else Draws "game over" screen.
			else{
				DrawEndGame(g);
			}
		}
		//If Pause menu is chosen , draws it.
		else if (Pause == true){
			DrawPauseMenu(g);
		}
		//Otherwise , draw the game.
		else
		{
			//If the snake was not drawn , initialize it and set it's default position.
			//Also "keys" is initialized and we set the first fruit and golden fruit position
			//(Even though the goldenFruit will not be drawn or taken into consideration).
			if (snake == null){
				snake = new LinkedList<Point>();
				keys = new LinkedList<Integer>();
				DefaultSnake();
				placeFruit();
				placeGoldenFruit();
			}
		
			//If high score is default , we get it from the file.
			if (highScore.equals("Nobody:0")){
				highScore = this.GetHighScore();
			}
		
			//Call the drawing functions (to draw the screen with every element)
			//Order is important!
			ColorBackground(g);
			DrawSnake(g);
			DrawFruit(g);
			if ((( score % (scoreRaise * 10) == 0) && (score != 0)) || (drawGoldFruit == true)){
				DrawGoldenFruit(g);
			}
			DrawGrid(g);
			DrawScore(g);
		}
	}
	
	//This function will update the screen using double buffering method.
	//Graphics are drawn off screen on an image and after that it is put on
	//the game screen.
	public void update (Graphics g){
		Graphics offScreenGraphics;
		Dimension dim = this.getSize();
	
		BufferedImage offScreen = new BufferedImage(dim.width,dim.height,BufferedImage.TYPE_INT_ARGB);
		offScreenGraphics = offScreen.getGraphics();
		offScreenGraphics.setColor(this.getBackground());
		offScreenGraphics.fillRect(0, 0, dim.width, dim.height);
		offScreenGraphics.setColor(this.getForeground());
		paint(offScreenGraphics); //Paints everything on the off screen
		
		//Puts the drawn image on current screen.
		g.drawImage(offScreen, 0, 0, this);

	}
	
	//Default snake clears the snake , score and timer and sets it's default points and no movement.
	public void DefaultSnake(){
		score = 0;
		timer = 0;
		snake.clear();
		snake.add(new Point(2,0));
		snake.add(new Point(1,0));
		snake.add(new Point(0,0));
		direction = Direction.NO_DIRECTION;
	}
	
	//The options that are displayed under the game when playing.
	public void DrawScore(Graphics g){
		g.drawString("Score: " + score, 0, BOX_HEIGHT * GRID_HEIGHT + 15);
		g.drawString("HighScore: " + highScore, 0, BOX_HEIGHT * GRID_HEIGHT + 30);
		g.drawString("Bonus Timer: " + (int) Math.round(timer*2/10), 0, BOX_HEIGHT * GRID_HEIGHT + 45);
		g.drawString("Controls: ", 250, BOX_HEIGHT * GRID_HEIGHT + 15);
		g.drawString("Movement: Up / W  Down / S  Left / A  Right / D", 325, BOX_HEIGHT * GRID_HEIGHT + 15);
		g.drawString("Pause/Resume: Esc", 325, BOX_HEIGHT * GRID_HEIGHT + 30);
		g.drawString("Reset Game: R", 325, BOX_HEIGHT * GRID_HEIGHT + 45);
	}
	
	//Draws the Won menu.
	public void DrawWonGame(Graphics g){
		Image WonImage = null;
		if (this.WonImage == null){
			try{
				URL imagePath = SnakeCanvas.class.getResource("Won.png");
				WonImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			}
			catch (Exception e){
				//Image does not exist
				e.printStackTrace();
			}
			g.drawImage(WonImage, 0, 0, 601, 650, this);
		}
	}
	
	//Draws the End menu.
	public void DrawEndGame(Graphics g){
		Image endImage = null;
		if (this.EndedImage == null){
			try{
				URL imagePath = SnakeCanvas.class.getResource("GameOver.png");
				endImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			}
			catch (Exception e){
				//Image does not exist
				e.printStackTrace();
			}
			g.drawImage(endImage, 0, 0, 601, 650, this);
		}
	}
	
	//Draws the Pause menu.
	public void DrawPauseMenu(Graphics g){
		Image pauseImage = null;
		if (this.PauseImage == null){
			try{
				URL imagePath = SnakeCanvas.class.getResource("PauseMenu.jpg");
				pauseImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			}
			catch (Exception e){
				//Image does not exist
				e.printStackTrace();
			}
			g.drawImage(pauseImage, 0, 0, 601, 650, this);
		}
	}
	
	//Draws the beginning menu.
	public void DrawMenu (Graphics g){
		Image menuImage = null;
		if (this.menuImage == null){
			try{
				URL imagePath = SnakeCanvas.class.getResource("Menu.jpg");
				menuImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			}
			catch (Exception e){
				//Image does not exist
				e.printStackTrace();
			}
			g.drawImage(menuImage, 0, 0, 601, 650, this);
		}
	}
	
	//Colors the background black (used before drawing everything else).
	public void ColorBackground(Graphics g){
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, GRID_WIDTH * BOX_WIDTH, GRID_HEIGHT * BOX_HEIGHT);
	}
	
	//Draws the grid (vertical lines and horizontal lines).
	public void DrawGrid(Graphics g){
		g.drawRect(0, 0, GRID_WIDTH * BOX_WIDTH, GRID_HEIGHT * BOX_HEIGHT);
		g.setColor(Color.DARK_GRAY);
		
		//Drawing the vertical lines
		for (int x = BOX_WIDTH ; x < GRID_WIDTH * BOX_WIDTH ; x += BOX_WIDTH){
			g.drawLine(x, 0, x, BOX_HEIGHT * GRID_HEIGHT);
		}
		//Drawing the horizontal lines
		for (int y = BOX_HEIGHT ; y < GRID_HEIGHT * BOX_HEIGHT ; y += BOX_HEIGHT){
			g.drawLine(0,y,GRID_WIDTH * BOX_WIDTH , y);
		}
		g.setColor(Color.BLACK);
	}
	
	//Draws the snake green using it's list of points.
	public void DrawSnake(Graphics g){
		g.setColor(Color.GREEN);
		for (Point p : snake){
			g.fillRect(p.x * BOX_WIDTH , p.y * BOX_HEIGHT, BOX_WIDTH-1 ,BOX_HEIGHT-1);
		}
	}
	
	//Draws the fruit red using it's point coordinates.
	public void DrawFruit(Graphics g){
		g.setColor(Color.RED);
		g.fillOval(fruit.x * BOX_WIDTH, fruit.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
		g.setColor(Color.BLACK);
	}
	
	//Draws the golden fruit using it's point coordinates (if it has to be drawn).
	public void DrawGoldenFruit (Graphics g){
		if (drawGoldFruit == true){
			g.setColor(Color.YELLOW);
			g.fillOval( goldFruit.x * BOX_WIDTH, goldFruit.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
			g.setColor(Color.BLACK);
		}
	}
	
	//Sets the red fruit coordinates using random positions that are not on the snake's position.
	//If the golden fruit was taken , it keeps the red fruit position so that it does not move.
	public void placeFruit(){
		if (drawFruit == true){
			fruit.x = fruitx;
			fruit.y = fruity;
		}
		else{
			Random r = new Random();
			Point newPlace = new Point(r.nextInt(GRID_WIDTH),r.nextInt(GRID_HEIGHT));
			while (snake.contains(newPlace)){
				newPlace.x = r.nextInt(GRID_WIDTH);
				newPlace.y = r.nextInt(GRID_HEIGHT);
			}
			fruit = newPlace;
			fruitx = fruit.x;
			fruity = fruit.y;
		}
	}
	
	//Sets the golden fruit coordinates using random positions that are not on the snake's position or the red fruit.
	//If the red fruit was taken , it keeps the golden fruit position so that it does not move.
	public void placeGoldenFruit(){
		if (drawGoldFruit == true){
			goldFruit.x = goldFruitx;
			goldFruit.y = goldFruity;
		}
		else{
			Random r = new Random();
			Point newPlace = new Point(r.nextInt(GRID_WIDTH),r.nextInt(GRID_HEIGHT));
			while ((snake.contains(newPlace)) || (newPlace == fruit)){
				newPlace.x = r.nextInt(GRID_WIDTH);
				newPlace.y = r.nextInt(GRID_HEIGHT);
			}
			goldFruit = newPlace; 
			goldFruitx = goldFruit.x;
			goldFruity = goldFruit.y;
		}
	}
	
	//This function is useful when two keys are pressed in the same time and the snake
	//collides with itself because of them. It executes both of the moves in the same time
	//so that the snake turns instead of colliding with itself.
	public void shitHappened(){
		for (int i = 0 ; i < keys.size() ; i ++){
			switch (keys.get(i)){
			case KeyEvent.VK_UP:
				direction = Direction.NORTH;
				break;
			case KeyEvent.VK_DOWN:
				direction = Direction.SOUTH;
				break;
			case KeyEvent.VK_LEFT:
				direction = Direction.WEST;
				break;
			case KeyEvent.VK_RIGHT:
				direction = Direction.EAST;
				break;
			}
			repaint();
			Move();
			//Sleeps so that the movement seems natural.
			Thread.currentThread();
			try {
				Thread.sleep(difficulty);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//The movement function.
	public void Move(){
		Point head = snake.peekFirst();
		Point newHead = head;
		
		//Using the direction which is set when a key is pressed , 
		//this sets the coordinates of the new head.
		switch (direction){
		case EAST:
			newHead = new Point(head.x + 1, head.y);
			break;
		case WEST:
			newHead = new Point(head.x - 1, head.y);
			break;
		case NORTH:
			newHead = new Point(head.x,head.y - 1);
			break;
		case SOUTH:
			newHead = new Point(head.x, head.y + 1);
			break;
		default:
			break;
		}
		
		//Deletes the tail of the snake
		Point oldLast = snake.removeLast();
		
		//If the snake is as big as the map, the Won menu is called.
		if (snake.size() == GRID_WIDTH * GRID_HEIGHT - 1){
			Ended = true;
			Won = true;
			DefaultSnake();
			placeFruit();
			inputOptionsDone = false;
			return;
		}
		//If the new head hits a fruit
		if (newHead.equals(fruit)){
			drawFruit = false;
			//We keep the tail , because we grow the snake with 1 box.
			snake.addLast(oldLast);
			//We calculate the new positions for the red and golden fruits.
			placeFruit();
			placeGoldenFruit();
			//We add to the score.
			score += scoreRaise;
			//If the score is multiple of scoreRaise and the timer is zero 
			//We set the timer and draw the set the golden fruit draw value to true.
			if (((score % (scoreRaise*10) == 0) && (score != 0)) && (timer == 0)){
				timer = 56;
				drawGoldFruit = true;
			}
			//We decrement the timer of the golden fruit if it is greater than zero.
			if (timer > 0){
				timer--;
			}
			//And if it is zero, we set the draw golden fruit value to false.
			if (timer == 0){
				drawGoldFruit = false;
			}
		}
		//If the new head hits a golden fruit
		if (newHead.equals(goldFruit)){
			//If the score is multiple of scoreRaise or the draw golden fruit value is true
			if ((( score % (scoreRaise*10) == 0) && (score != 0)) || (drawGoldFruit == true)){
				//We add to the score if the timer is greater than 0 and keep the tail.
				if (timer > 0){
					snake.addLast(oldLast);
					score += scoreRaise * 3;
				}
				//And set the timer to 0 , we don't draw the golden fruit anymore and
				//draw the red fruit in the same position that it was before.
				//We also call the functions that calculate the new coordinates of fruits.
				timer = 0;
				drawGoldFruit = false;
				drawFruit = true;
				placeFruit();
				placeGoldenFruit();
			}
		}
		//The next two conditions are if we hit the walls 
		//and they reset the game , check the score and 
		//place the fruit (the golden fruit will not be placed anyway 
		//so we don't call it's function).
		if (newHead.x < 0 || newHead.x >= GRID_WIDTH){
			checkScore();
			Ended = true;
			DefaultSnake();
			placeFruit();
			inputOptionsDone = false;
			return;
		}
		if (newHead.y < 0 || newHead.y >= GRID_HEIGHT){
			checkScore();
			Ended = true;
			DefaultSnake();
			placeFruit();
			inputOptionsDone = false;
			return;
		}
		//If the snake collides with itself
		if (snake.contains(newHead)){
			//If the snake is moving (otherwise the new head is the same as the head)
			if (direction != Direction.NO_DIRECTION){		
				//This is the bug where two keys are pressed in the same time and makes
				//it move in the opposite direction that it is currently moving.
				if (newHead.equals(snake.get(1))){
					//We add the tail because we will return from this condition
					//and will not add the new head.
					snake.addLast(oldLast);
					bug = true;
					//This function will call the Move function inside it for both
					//the keys that are pressed.
					shitHappened();	
					return;
				}
				//If the bug didn't happen, we check the score, display the end menu and reset the game.
				else{
					checkScore();
					Ended = true;
					DefaultSnake();
					placeFruit();
					inputOptionsDone = false;
					return;
				}
			}
			//If the snake doesn't move (beginning of the game) , we set it as Default each time
			//so it doesn't delete itself.
			else{
				DefaultSnake();
			}
		}
		
		//If the snake is moving , we add the new head.
		if (direction != Direction.NO_DIRECTION){
			snake.addFirst(newHead);
		}
		//If the timer is 0 , we set the draw golden fruit value to false.
		if (timer == 0){
			drawGoldFruit = false;
		}
		//Otherwise we decrement the timer.
		else {
			timer--;
		}
	}

	//This is the run function of the thread. It runs infinitely.
	public void run() {
		while (true){
			//If we are in the game (not menus) , we move and clear the keys pressed.
			if (((!Menu)&&(!Ended))&&(!Pause)){
				Move();
				keys.clear();
			}
			//repaint() creates a new Thread which calls the update(g) function.
			repaint();
			
			//If there is a bug the thread already slept.
			if (!bug){
				//Otherwise we sleep as long as the difficulty is set (this sets the speed of the snake)
				try {
					Thread.currentThread();
					Thread.sleep(difficulty);
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
			//Set the bug value to false.
			bug = false;
		}
	}

	//This function asks for the difficulty(meaning the snake speed) of the game between 1 and 5.
	//It also asks the user if he wants the session to remember the setting so that the first question is 
	//never asked again. This second question won't be displayed again no matter what the answer is.
	public void inputOptions(){
		if (!keepSettings){
			String inputdiff = JOptionPane.showInputDialog("Please select difficulty 1-5! Higher speed equals more points per fruit! (Default is 3)");
			if (inputdiff != null){
				if (inputdiff.equals("1")){
					difficulty = 200;
					scoreRaise = 100;
				}
				else if (inputdiff.equals("2")){
					difficulty = 150;
					scoreRaise = 110;
				}
				else if (inputdiff.equals("3")){
					difficulty = 100;
					scoreRaise = 125;
				}
				else if (inputdiff.equals("4")){
					difficulty = 75;
					scoreRaise = 145; 
				}
				else if (inputdiff.equals("5")){
					difficulty = 50;
					scoreRaise = 170;
				}
				else {
					difficulty = 100;
					scoreRaise = 125;
				}
			}
			else {
				difficulty = 100;
			}
		}
		if (!firstInputOptionsTime){
			String once = JOptionPane.showInputDialog("Do you want to keep that difficulty during this session?(y/n) You will be not asked this question again.");
			if (once != null){
				if ((once.contains("y")) || (once.contains("Y"))){
					keepSettings = true;
				}
				else if ((once.contains("n")) || (once.contains("N"))){
					keepSettings = false;
				}
			}
			else {
				keepSettings = false;
			}
		}
		inputOptionsDone = true;
		firstInputOptionsTime = true;
	}
	
	//This treats the keys.
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()){
		//Accepts both WASD and arrow keys.
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			if ((!Ended) && (!Menu) && (!Pause) && (direction != Direction.SOUTH) && ((inputOptionsDone == true) || (keepSettings == true))){
				direction = Direction.NORTH;
				keys.add(KeyEvent.VK_UP);
			}
			if ((!Ended) && (!Menu) && (!Pause) && (direction == Direction.NO_DIRECTION)){
				inputOptions();
			}
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			if ((!Ended) && (!Menu) && (!Pause) && (direction != Direction.NORTH) && ((inputOptionsDone == true) || (keepSettings == true))){
				direction = Direction.SOUTH;
				keys.add(KeyEvent.VK_DOWN);
			}
			if ((!Ended) && (!Menu) && (!Pause) && (direction == Direction.NO_DIRECTION)){
				inputOptions();
			}
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			if ((!Ended) && (!Menu) && (!Pause) && (direction != Direction.EAST) && (direction != Direction.NO_DIRECTION)  && 
					((inputOptionsDone == true) || (keepSettings == true))){
				direction = Direction.WEST;
				keys.add(KeyEvent.VK_LEFT);
			}
			if ((!Ended) && (!Menu) && (!Pause) && (direction == Direction.NO_DIRECTION)){
				inputOptions();
			}
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			if ((!Ended) && (!Menu) && (!Pause) && (direction != Direction.WEST) && ((inputOptionsDone == true) || (keepSettings == true))){
				direction = Direction.EAST;
				keys.add(KeyEvent.VK_RIGHT);
			}
			if ((!Ended) && (!Menu) && (!Pause) && (direction == Direction.NO_DIRECTION)){
				inputOptions();
			}
			break;
		case KeyEvent.VK_SPACE:
			if (Menu == true){
				Menu = false;
				Ended = false;
				Pause = false;
			}
			else if (Ended == true){
				Ended = false;
				if (Won == true){
					Won = false;
				}
				Menu = false;
				Pause = false;
			}
			else if (Pause == true){
				Pause = false;
				Menu = true;
				Ended = false;
				DefaultSnake();
				placeFruit();
				inputOptionsDone = false;
			}
			else {
				Ended = false;
				Menu = false;
				Pause = false;
			}
			repaint();
			break;
		case KeyEvent.VK_ESCAPE:
			//If in the game, we pause the game.
			if (!Menu && !Ended){
				//If we're in the game , pause.
				if (Pause == false){
					Pause = true;
				}
				//Else, resume it.
				else {
					Pause = false;
				}
			}
			break;
		case KeyEvent.VK_R:
			//Resets the snake.
			DefaultSnake();
			placeFruit();
		}
	}
	
	//unimplemented methods from keyListener
	public void keyReleased(KeyEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		
	}
	
	//This function gets the high score from the high score file.
	//If the file doesn't exist , it returns "Nobody:0" which is default.
	public String GetHighScore() {
		//format: Nume:Scor
		FileReader readFile = null;
		BufferedReader reader = null;
		try {
			readFile = new FileReader(path + File.separator + "DeleteAndLoseHighScore.dat");
			reader = new BufferedReader(readFile);
			return reader.readLine();
		} catch (Exception e) {
			return "Nobody:0";
		}
		//closes the reader (this finally executes even after the function returns a value).
		finally{
			try {
				if (reader != null){
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//This function checks if the user made a new high score and puts it in the high score file.
	public void checkScore(){
		if (score > Integer.parseInt(highScore.split(":")[1])){
			//Asks the user for his name and puts "Anonymous" in case there's no user given.
			name = JOptionPane.showInputDialog("New HighScore! Congratiulations! What is your name?");
			if (name != null){
				if ((name.equals("")||(name.equals(" ")))){
					name = "Anonymous";
				}
			}
			else {
				name = "Anonymous";
			}
			highScore = name + ":" + score;
			
			//Creates the directories for the file.
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			
			//Creates the file if it doesn't exist.
			File scoreFile = new File(path + File.separator + "DeleteAndLoseHighScore.dat");
			if (!scoreFile.exists()){
				try {
					scoreFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//Creates the writer file and writes the score in the file with a message on the next line.
			FileWriter writeFile = null;
			BufferedWriter writer = null;
			try {
				writeFile = new FileWriter(scoreFile);
				writer = new BufferedWriter(writeFile);
				writer.write(highScore);
				writer.write("\n");
				writer.write("Don't cheat! It's no fun!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//closes the writer if it different than null.
			finally{
				if (writer != null){
					try {
						writer.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}
