package Main;

//This makes an application from the applet.
public class StartClass {
	
	public static void main(String [] args)
	{
		// Create an object of type SnakeApplet which is the main applet class
		SnakeApplet theApplet = new SnakeApplet();
		theApplet.init();   // Invoke the applet's init() method
		theApplet.start();  // Starts the applet
 
		// Create a window (JFrame) and make applet the content pane.
		javax.swing.JFrame window = new javax.swing.JFrame("CristySnake");
		window.setContentPane(theApplet);
		window.pack();              // Arrange the components.
		window.setVisible(true);    // Make the window visible.
	}
}