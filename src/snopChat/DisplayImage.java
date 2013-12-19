package snopChat;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class DisplayImage extends Thread{
	private String mImageName;
	private ImageFrame frame;
	DisplayImage(String imageName){
		this.mImageName=imageName;
	}

	public void run(){
		frame = new ImageFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		
		try {
			Thread.currentThread();
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		frame.setVisible(false);
		File file = new File(this.mImageName);
		file.delete();
	}

	public void off(){
		frame.setVisible(false);
//		try {
//			Thread.currentThread().join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	class ImageFrame extends JFrame{

		ImageFrame(){
			setTitle(mImageName);
			setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

			ImageComponent component = new ImageComponent();
			add(component);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			double width = screenSize.getWidth();
			double height = screenSize.getHeight();
			setLocation((int)width/2-(getWidth()/2), (int)height/2-(getHeight()/2));
		}

		public static final int DEFAULT_WIDTH = 500;
		public static final int DEFAULT_HEIGHT = 500;
	}


	class ImageComponent extends JComponent{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Image image;
		public ImageComponent(){
			try{
				File image2 = new File(mImageName);
				sleep();
				image = ImageIO.read(image2);

			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
		synchronized void sleep() {
			try {this.wait(100);}catch(Exception e){e.printStackTrace();}
		}
		public void paintComponent (Graphics g){
			if(image == null) return;
			g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		}
		public Image getImage(){
			return image;
		}
	}
}
