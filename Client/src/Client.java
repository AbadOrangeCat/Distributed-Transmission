import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class Client {

	static ArrayList<Otherfile> myFiles = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		ServerSocket last = new ServerSocket(6666);

		JFrame frame = new JFrame();
		JList alist = new JList();
		frame.setTitle("This is the client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 600);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		JButton jb = new JButton("List the file name in the server");
		JPanel pane1 = new JPanel();
		BoxLayout boxlayout1 = new BoxLayout(pane1, BoxLayout.Y_AXIS);
		pane1.setLayout(boxlayout1);
		JScrollPane jScrollPane1 = new JScrollPane(pane1);
		jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setSize(400, 300);
		JLabel title = new JLabel("Download file (Click to show image): ");
		JPanel pane2 = new JPanel();
		BoxLayout boxlayout2 = new BoxLayout(pane2, BoxLayout.Y_AXIS);
		pane2.setLayout(boxlayout2);
		JScrollPane jScrollPane = new JScrollPane(pane2);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setSize(400, 300);

		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					Socket s = new Socket("localhost", 8000);
					DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
					dataOutputStream.writeInt(-1);

					ArrayList<String> filename = new ArrayList<String>();
					try {
						ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream()); // Error Line!
						try {
							Object object = objectInput.readObject();
							filename = (ArrayList<String>) object;
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					Object[] namefile = filename.toArray();

					alist.setListData(namefile);
					pane1.add(alist);
					s.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		alist.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				try {
					JList list = (JList) evt.getSource();
					if (evt.getClickCount() == 2) {
						Socket s = new Socket("localhost", 7999);
						DataOutputStream datacnd = new DataOutputStream(s.getOutputStream());
						int index = list.locationToIndex(evt.getPoint());
						try {
							datacnd.writeInt(index);
						} catch (IOException e) {
							e.printStackTrace();
						}
						s.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		frame.add(jb, BorderLayout.NORTH);
		frame.add(jScrollPane1);
		frame.add(title);
		frame.add(jScrollPane);
		frame.setVisible(true);

		int fileId = 0;

		while (true) {
			try {
				Socket asclients = last.accept();
				DataInputStream asdataInputStream = new DataInputStream(asclients.getInputStream());
				int namelength = asdataInputStream.readInt();
				byte[] fileNameBytes = new byte[namelength];

				asdataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
				String fileName = new String(fileNameBytes);

				int fileContentLength = asdataInputStream.readInt();

				if (fileContentLength > 0) {
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
					
					
					
					JPanel jpFileRow = new JPanel();

					jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));

					JLabel jlFileName = new JLabel(fileName);
					jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
					jlFileName.setBorder(new EmptyBorder(10, 0, 10, 0));

					jpFileRow.setName((String.valueOf(fileId)));

					jpFileRow.add(jlFileName);
					pane2.add(jpFileRow);

					frame.validate();
					myFiles.add(new Otherfile(fileId, fileName, total, fileName));
					jpFileRow.addMouseListener(getMyMouseListener());
					fileId++;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static JFrame createFrame(String fileName, byte[] fileData) {
		JFrame jFrame = new JFrame("Show image");
		jFrame.setSize(500, 500);
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
		JLabel jlFileContent = new JLabel();
		jlFileContent.setAlignmentX(Component.CENTER_ALIGNMENT);

		
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(fileData);
			Image image = ImageIO.read(bais);
			jlFileContent.setIcon(new ImageIcon(image));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		jPanel.add(jlFileContent);
		jFrame.add(jPanel);
		return jFrame;
	}

	public static MouseAdapter getMyMouseListener() {
		return new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				JPanel pane2 = (JPanel) e.getSource();

				int fileId = Integer.parseInt(pane2.getName());
				for (Otherfile myFile : myFiles) {
					if (myFile.getId() == fileId) {
						JFrame jfPreview = createFrame(myFile.getName(), myFile.getData());
						jfPreview.setVisible(true);
					}
				}
			}

		};
	}
	static public byte[] connectarray(byte[] diyi, byte[] dier) {
		int fal = diyi.length;        
		int sal = dier.length;  
		byte[] result = new byte[fal + sal];  
		System.arraycopy(diyi, 0, result, 0, fal);  
		System.arraycopy(dier, 0, result, fal, sal);  
		return result;   
	}

}
