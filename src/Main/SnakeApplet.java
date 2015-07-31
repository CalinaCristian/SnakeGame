package Main;

import java.applet.Applet;
import java.awt.Dimension;

public class SnakeApplet extends Applet{

	private static final long serialVersionUID = -253453253L;

	//Get an object of the SnakeCanvas class.
	private SnakeCanvas snakeCanvas;
	
	//Initialize the canvas and make it's window.
	public void init(){
		snakeCanvas = new SnakeCanvas();
		snakeCanvas.setPreferredSize(new Dimension(601,650));
		snakeCanvas.setVisible(true);
		snakeCanvas.setFocusable(true);
		this.add(snakeCanvas);
		this.setVisible(true);
		this.setSize(new Dimension(601,650));
	}
}
