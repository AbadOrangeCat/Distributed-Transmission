import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class CNDserver {

	public static String s1 = "123";

	static ArrayList<MyFile> myFiles = new ArrayList<>();
	static JFrame frame = new JFrame();
	static JLabel label5 = new JLabel("File processing status:");
	static JLabel label6 = new JLabel("***");
	static JLabel label7 = new JLabel("***");
	static JLabel label8 = new JLabel("  ");
	static ArrayList<String> namelist = new ArrayList<>();
	static ArrayList<String> log = new ArrayList<>();
	static ArrayList<String> fra = new ArrayList<>();
	static ArrayList<String> hash = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		ServerSocket client = new ServerSocket(7999);
		ServerSocket asclient = new ServerSocket(7998);

		JList alist = new JList();

		frame.setTitle("This is the CND server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700, 500);

		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		JButton jb = new JButton("Clear");
		JButton ll = new JButton("Log");

		JPanel panel = new JPanel();
		BoxLayout boxlayout1 = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxlayout1);

		JPanel pane2 = new JPanel();
		BoxLayout boxlayout2 = new BoxLayout(pane2, BoxLayout.Y_AXIS);
		pane2.setLayout(boxlayout2);
		JScrollPane jScrollPane = new JScrollPane(pane2);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JLabel label9 = new JLabel("Current content £¨Click for fragment, may take times): ");

		JPanel pane3 = new JPanel();
		BoxLayout boxlayout3 = new BoxLayout(pane3, BoxLayout.X_AXIS);
		pane3.setLayout(boxlayout3);

		panel.add(label8);
		panel.add(label5);
		panel.add(label6);
		panel.add(label7);

		pane3.add(jb);
		pane3.add(ll);
		pane3.add(label9);

		frame.add(panel);
		frame.add(pane3);
		frame.add(jScrollPane);

		jb.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				namelist = new ArrayList<>();
				myFiles = new ArrayList<>();
				log = new ArrayList<>();
				fra = new ArrayList<>();
				hash = new ArrayList<>();
				label6.setText("***");
				label7.setText("***");
				pane2.removeAll();
				pane2.repaint();
				frame.validate();
			}
		});
		
		ll.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JFrame jfPreview = creatlog();
				jfPreview.setVisible(true);
			}
		});

		System.out.println("This is CND");
		int fileId = 0;
		frame.setVisible(true);

		while (true) {

			try {

				Socket clients = client.accept();
				DataInputStream dataInputStream = new DataInputStream(clients.getInputStream());
				int input = dataInputStream.readInt();
				clients.close();

				Socket server = new Socket("localhost", 8000);
				DataOutputStream datacnd = new DataOutputStream(server.getOutputStream());

				try {
					datacnd.writeInt(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
				server.close();

				try {
					Socket finalsend = new Socket("localhost", 6666);
					Socket asclients = asclient.accept();
					DataInputStream asdataInputStream = new DataInputStream(asclients.getInputStream());
					int namelength = asdataInputStream.readInt();
					byte[] fileNameBytes = new byte[namelength];
					asdataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
					String fileName = new String(fileNameBytes);

					DataOutputStream fineout = new DataOutputStream(finalsend.getOutputStream());
					fineout.writeInt(namelength);
					fineout.write(fileNameBytes);

					DateTimeFormatter time = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
					LocalDateTime now = LocalDateTime.now();
					
					String log1 = "user request: file " + fileName + " at " + time.format(now);
					log.add(log1);

					String sp= " ";
	
					label6.setText("user request: file " + fileName + " at " + time.format(now));
					int fileContentLength = asdataInputStream.readInt();
					
					boolean exist = true;
					
					int hashlength = asdataInputStream.readInt();
					byte[] hashbyte = new byte[hashlength];
					asdataInputStream.readFully(hashbyte, 0, hashbyte.length);
					String hh;
					hh = new BigInteger(1, hashbyte).toString(16);
					
					System.out.println(hash.contains(hh));
					
					if(hash.contains(hh)){
						String log2 = "Already have this file's data";
						label7.setText(log2);
						log.add(log2);
						log.add(sp);
						exist = false;
					}else if(!(namelist.contains(fileName))) {
						namelist.add(fileName);
						String log2 = "Download data from server";
						label7.setText(log2);
						log.add(log2);
						log.add(sp);
					}
					
					
					fineout.writeInt(fileContentLength);
					if (fileContentLength > 0) {
						JPanel jpFileRow = new JPanel();
						
						
                		int times = fileContentLength/2024;
                		int rem = fileContentLength%2024;
                		int start = 0;
                		int bb = 0;
                		byte[] total = new byte[0];
                		for(int i = 0; i<times ; i++) {	
                			byte[] part = new byte[2024];
                			asdataInputStream.readFully(part, 0, part.length);
                			total = connectarray(total, part);
                		}
                		byte[] part = new byte[rem];
                		asdataInputStream.readFully(part, 0, part.length);
                		total = connectarray(total, part);
                		

                		start = 0;
                		bb = 0;
                		for(int i = 0; i<times ; i++) {
                			byte[] newArray = Arrays.copyOfRange(total, bb, bb+2024);
                			fineout.write(newArray);
                			start ++;
                			bb += 2024;
                		}
                		byte[] newArray = Arrays.copyOfRange(total, bb, bb+rem);
                		fineout.write(newArray);

						jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));
						JLabel jlFileName = new JLabel(fileName);
						jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
						jlFileName.setBorder(new EmptyBorder(10, 0, 10, 0));

						jpFileRow.setName((String.valueOf(fileId)));
						
						if (exist) {
							jpFileRow.add(jlFileName);
							myFiles.add(new MyFile(fileId, fileName, total, fileName));
						}
						
                        try {
                        	String sh;
							MessageDigest md = MessageDigest.getInstance("SHA-1");
							for(int i = 0; i<total.length; i++) {
								String xxxx = ""+total[i];
								md.update(xxxx.getBytes("UTF-8"));
							}
							byte[] result = md.digest();
							sh = new BigInteger(1, result).toString(16);
							System.out.println(sh);
							hash.add(sh);
							
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}

						pane2.add(jpFileRow);
						frame.validate();
						jpFileRow.addMouseListener(getMyMouseListener());
						
						fileId++;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	
	static public byte[] connectarray(byte[] diyi, byte[] dier) {
		int fal = diyi.length;        
		int sal = dier.length;  
		byte[] result = new byte[fal + sal];  
		System.arraycopy(diyi, 0, result, 0, fal);  
		System.arraycopy(dier, 0, result, fal, sal);  
		return result;   
	}

	private static void cleardata() {
		label5 = new JLabel("File processing status:");
		label6 = new JLabel("***");
		label7 = new JLabel("***");
		label8 = new JLabel("  ");
		frame.validate();
	}

	public static MouseAdapter getMyMouseListener() {
		return new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JPanel pane2 = (JPanel) e.getSource();
				int fileId = Integer.parseInt(pane2.getName());
				for (MyFile myFile : myFiles) {
					if (myFile.getId() == fileId) {
						JFrame jfPreview = createFrame(myFile.getName(), myFile.getData(), myFile.getFileExtension());
						jfPreview.setVisible(true);
					}
				}
			}

		};
	}
	
	public static JFrame creatlog() {
		JFrame jFrame = new JFrame("Show log");
		jFrame.setSize(500, 500);
		Object[] llog = log.toArray();
		JList alist = new JList();
		alist.setListData(llog);
		JPanel pane1 = new JPanel();
		BoxLayout boxlayout1 = new BoxLayout(pane1, BoxLayout.Y_AXIS);
		JScrollPane jScrollPane1 = new JScrollPane(pane1);
		jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		pane1.setLayout(boxlayout1);
		pane1.add(alist);
		jFrame.add(jScrollPane1);
		return jFrame;

	}

	public static JFrame createFrame(String fileName, byte[] fileData, String fileExtension) {
		JFrame jFrame = new JFrame("Show fragment");
		jFrame.setSize(500, 500);
		
		JPanel panex = new JPanel();
		BoxLayout boxlayout1 = new BoxLayout(panex, BoxLayout.Y_AXIS);
		panex.setLayout(boxlayout1);
		JScrollPane jScrollPane1 = new JScrollPane(panex);
		jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setSize(400, 300);
		
		int x = fileData.length;
		StringBuilder hex = new StringBuilder();
		int times = x/2024;
		int rem = x%2024;
		int start = 0;
		int bb = 0;
		
		JList alist = new JList();
		ArrayList<String> title = new ArrayList<>();
		
		for(int i = 0; i<times ; i++) {
			
			StringBuilder asd = new StringBuilder();
			for (int begin=0;begin<2024;begin++) {
				asd.append(String.format("%02X ", fileData[bb+begin]));
			}
			String getstring = asd.toString();
			fra.add(getstring);
			String fr = "Fragment num" + start + ",  " + bb +" to "+(bb + 2024)+ " Click for detail";
			title.add(fr);
			start ++;
			bb += 2024;
		}
		
		String fr = "Fragment num" + start + ",  " + bb +" to "+(bb + rem)+ " Click for detail";
		StringBuilder asd = new StringBuilder();
		for (int begin=0;begin<rem;begin++) {
			asd.append(String.format("%02X ", fileData[bb+begin]));
		}
		String getstring = asd.toString();
		fra.add(getstring);
		title.add(fr);
		
		Object[] titlel = title.toArray();
		alist.setListData(titlel);
		panex.add(alist);
		
		jFrame.add(jScrollPane1);
		jFrame.validate();
		
		alist.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();
				int index = list.locationToIndex(evt.getPoint());
				JFrame inf = new JFrame("Show detial");
				inf.setSize(500, 500);
				
				Object[] single = fra.toArray();		
				
				JTextArea textArea = new JTextArea(2, 20);
				textArea.setText( single[index].toString());
				textArea.setWrapStyleWord(true);
				textArea.setLineWrap(true);
				textArea.setOpaque(false);
				textArea.setEditable(false);
				textArea.setFocusable(false);
				inf.getContentPane().add(textArea, BorderLayout.CENTER);
				inf.setVisible(true);
			}
		});

		return jFrame;

	}

}
