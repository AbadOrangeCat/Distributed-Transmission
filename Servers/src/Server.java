import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.security.NoSuchAlgorithmException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Server {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(8000);

		boolean notend = true;

		System.out.println("This is Server");

		while (true) {
			try {
				Socket s = ss.accept();
				DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
				int input = dataInputStream.readInt();

				if (input == -1) {
					ArrayList<String> filename = getname();
					ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
					objectOutput.writeObject(filename);

				} else {
					ArrayList<String> filename = getname();
					Object[] namefile = filename.toArray();
					System.out.println("Client want to download " + namefile[input]);
					
					
                    try {
                    	Socket client = new Socket("localhost", 7998);
                    	String filepath = "./image/" + namefile[input];
                        FileInputStream fileInputStream = new FileInputStream(filepath);
                        File file = new File(filepath);
                        DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                        String outfile = file.getName();
                        byte[] fileNameBytes = outfile.getBytes();
                        byte[] fileBytes = new byte[(int)file.length()];
                        fileInputStream.read(fileBytes);
                        
                        int namelenth = fileNameBytes.length;

                        dataOutputStream.writeInt(namelenth);
                        dataOutputStream.write(fileNameBytes);
                        
                        dataOutputStream.writeInt(fileBytes.length);
                        
                        
                        try {
                        	String sh;
							MessageDigest md = MessageDigest.getInstance("SHA-1");
							for(int i = 0; i<fileBytes.length; i++) {
								String xxxx = ""+fileBytes[i];
								md.update(xxxx.getBytes("UTF-8"));
							}
							byte[] result = md.digest();
							sh = new BigInteger(1, result).toString(16);
							dataOutputStream.writeInt(result.length);
							dataOutputStream.write(result);
							
							
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
                        
                        

                        int filelength = fileBytes.length;
                		int times = filelength/2024;
                		int rem = filelength%2024;
                		int start = 0;
                		int bb = 0;
                		for(int i = 0; i<times ; i++) {
                			byte[] newArray = Arrays.copyOfRange(fileBytes, bb, bb+2024);
                			dataOutputStream.write(newArray);
                			start ++;
                			bb += 2024;
                		}
                		byte[] newArray = Arrays.copyOfRange(fileBytes, bb, bb+rem);
                		dataOutputStream.write(newArray);
                        
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static ArrayList<String> getname() {
		String path = "./image";
		File folder = new File(path);
		File[] files = folder.listFiles();
		ArrayList<String> filename = new ArrayList<String>();
		for (File file : files) {
			filename.add(file.getName());
		}
		return filename;
	}

}
