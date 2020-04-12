package venn;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class MainFrame implements MouseListener, KeyListener, ActionListener, ListSelectionListener{
	public final static int WIDTH = 1366, HEIGHT = 768; // size of the window/frame
	private final int MIN_CIRCLES = 2, MAX_CIRCLES = 7;
	private static final String MODE_NEW = "New", MODE_OPEN = "Open";
	private final String CIRCLE_TYPE = "Circle", TEXT_TYPE = "Text", PANEL_TYPE = "PANEL"; //PANEL_TYPE is the background jlayeredpanel
	private final String VALUE_DEFAULT = "Default", VALUE_CUSTOMIZE = "Customize";
	private final int SIZE_C_MIN = 100, SIZE_C_MAX = 600, SIZE_C_INIT = 600, SIZE_S_MIN = 0, SIZE_S_MAX = 10, SIZE_S_INIT = 2;
	private final String MODE_AUTO = "Automatic Placement and Resizing", MODE_AUTO_RESIZING = "Automatic Placement and No Resizing";
	private final String MODE_MANUAL = "Manual";
	private JSlider sliderSize, sliderStroke;
	private JFrame frame; // declares frame
	private JTable jtable;
	private JLabel lblForeColor,lblBackColor,lblFont,lblFSize, lblCSize, lblStroke,lblCColor;
	private JPanel panelCustomizeText, panelCustomizeCir, panelCustomizeMsg;
	private JCheckBox checkOpaque;
	private JSpinner addCircleAndText;
	private JButton btnAdd, btnDel, btnFont, btnAddText, btnForeColor,btnBackColor, btnCColor;
	private JTextField txtAddText, txtEditText;
	private JLayeredPane jlpane;
	public static int PANE_INDEX = 2, CINDEX = 0, TINDEX = 0; //CINDEX = circle index, TINDEX = text index
	public static ArrayList<CircleInfo> CI;
	public static ArrayList<TextInfo> TI;
	private JComboBox selectMode, sizeList, fontList;
	private JColorChooser jcc;
	private JFontChooser jfc;
	public static boolean DRAGGING = false;
	public boolean btnForeClicked = true, btnCColorClicked = false, btnFontClicked = false;
	private String savePath="";
	
	public MainFrame() {
		this(MODE_NEW,"");
	}
	
	public MainFrame(String mode, String path) {
		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////FRAME RELATED STUFF/////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
		frame = new JFrame("Customizable Venn Diagram"); // inside "" is the name for the window/frame
		frame.setSize(WIDTH, HEIGHT); // sets the frame to have size of 500 by 500
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closes the frame when x button is clicked (top right															// button)
		frame.setLocationRelativeTo(null);// sets the location of the frame to be in the middle.
		frame.setResizable(false); // makes the frame not resizable (constant size for frame)
		frame.setLayout(null); // sets absolute layout for the frame, so when adding stuff you have to set					// x,y,width, and height.
		//frame.add(this); // adds JPanel to the frame
		frame.addMouseListener(this); // adds mouse listener to the frame so mouse related actions can be performed
		frame.getContentPane().addKeyListener(this);
		//try {
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
			
			
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////MENU BAR RELATED STUFF/////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
			
		JMenuBar jmb = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		JMenu menuHelp = new JMenu("Help");
		
		JMenuItem itemOpen = new JMenuItem();
		Action open = new AbstractAction("Open") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser jfc = new JFileChooser();
				String desktop = System.getProperty("user.home") + "/Desktop";
				
				
				jfc.setDialogTitle("Choose a File to Open");
					
				FileFilter jpegFilter = new CustomFilter(".jpeg", "JPEG");
				FileFilter pngFilter = new CustomFilter(".png", "PNG");
					
				jfc.addChoosableFileFilter(jpegFilter);
				jfc.addChoosableFileFilter(pngFilter);
				jfc.setAcceptAllFileFilterUsed(false);
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.setCurrentDirectory(new File(desktop));
				int response = jfc.showOpenDialog(frame);
				
				if(response == JFileChooser.APPROVE_OPTION) {
					File path = jfc.getSelectedFile();
					
					String name = "";
					
					if(path.getName().indexOf(".jpeg")!= -1) {
						name = path.getName().replaceAll(".jpeg", "");
					}else if(path.getName().indexOf(".png")!= -1){
						name = path.getName().replaceAll(".png", "");
					}else {
						name = path.getName();
					}
					
					System.out.println("Selected File to Open: " + path);
					
					if(new File(path.getParentFile().getAbsolutePath()+"/"+name+"C.txt").exists()) {
						if(new File(path.getParentFile().getAbsolutePath()+"/"+name+"T.txt").exists()) {
							
							int choice = JOptionPane.showConfirmDialog(frame, "Do you want to save the file?", "Save File Confirmation", JOptionPane.YES_NO_OPTION);
							
							if(choice == JOptionPane.YES_OPTION) {
								BufferedImage saveImage = null;
								try {
									saveImage = new Robot().createScreenCapture(jlpane.getBounds());
								} catch (AWTException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								Graphics2D g2d = saveImage.createGraphics();
								g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
								jlpane.paint(g2d);
								
								if(savePath == "") {
									initiateChoosingDirectory(saveImage);
								}else {
									String type = savePath.substring(savePath.lastIndexOf(".")+1);
									File spath = new File(savePath);
									
									saveImage(saveImage,type,spath);
								}
							}
							
							new MainFrame(MODE_OPEN, path.getAbsolutePath());
							
							frame.dispose();

							
							
						}else {
							JOptionPane.showMessageDialog(frame, "Please select any file created by this program!",
									"Error: One or more file doesn't exist!",JOptionPane.ERROR_MESSAGE);
						}
					}else {
						JOptionPane.showMessageDialog(frame, "Please select any file created by this program!",
									"Error: One or more file doesn't exist!",JOptionPane.ERROR_MESSAGE);
					}
					
					
				}

			}
			
		};
		open.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK));

		itemOpen.setAction(open);
		
		JMenuItem itemSave = new JMenuItem();
		Action save = new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				BufferedImage saveImage = null;
				try {
					saveImage = new Robot().createScreenCapture(jlpane.getBounds());
				} catch (AWTException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				Graphics2D g2d = saveImage.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				jlpane.paint(g2d);
				
				if(savePath == "") {
					initiateChoosingDirectory(saveImage);
				}else {
					String type = savePath.substring(savePath.lastIndexOf(".")+1);
					File path = new File(savePath);
					
					saveImage(saveImage,type,path);
				}
			}
			
		};
		save.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		itemSave.setAction(save);
		
		JMenuItem itemSaveAs = new JMenuItem();
		Action saveas = new AbstractAction("SaveAs") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				BufferedImage saveImage = null;
				try {
					saveImage = new Robot().createScreenCapture(jlpane.getBounds());
				} catch (AWTException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				Graphics2D g2d = saveImage.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				jlpane.paint(g2d);
				
				initiateChoosingDirectory(saveImage);
				
			}
			
		};
		saveas.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK+ActionEvent.ALT_MASK));

		itemSaveAs.setAction(saveas);
		
		JMenuItem itemClose = new JMenuItem();
		Action close = new AbstractAction("Close") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
			
		};
		close.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK));

		itemClose.setAction(close);
		
		JMenuItem itemAbout = new JMenuItem();
		Action about = new AbstractAction("About") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
			
		};
		about.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK));

		itemAbout.setAction(about);
		
		JMenuItem itemSearch = new JMenuItem();
		Action search = new AbstractAction("Search") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
			
		};
		search.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK));

		itemSearch.setAction(search);

		
		menuFile.add(itemOpen);
		menuFile.addSeparator();
		menuFile.add(itemSave);
		menuFile.addSeparator();
		menuFile.add(itemSaveAs);
		menuFile.addSeparator();
		menuFile.add(itemClose);
		
		menuHelp.add(itemAbout);
		menuHelp.addSeparator();
		menuHelp.add(itemSearch);

		
		jmb.add(menuFile);
		jmb.add(menuHelp);
		
		frame.setJMenuBar(jmb);


		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////JLAYEREDPANE RELATED STUFF/////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
		CI = new ArrayList<CircleInfo>();
		TI = new ArrayList<TextInfo>();
		
		jcc = new JColorChooser();
		jfc = new JFontChooser();
		
		jlpane = new JLayeredPane();
		jlpane.setBorder(BorderFactory.createLineBorder(Color.black));
		jlpane.setBounds(10, 10, WIDTH-400, HEIGHT-100);
		jlpane.setOpaque(true);
		jlpane.setBackground(Color.white);
		jlpane.addMouseListener(this);
		//jlpane.addMouseMotionListener(this);
		
		frame.add(jlpane);
		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////JTABLE RELATED STUFF/////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
		DefaultTableModel dtm = new DefaultTableModel();
		dtm.addColumn("Name");
		dtm.addColumn("Object Type");
		dtm.addColumn("Pos");
		
		
		
		Object[] o = new Object[17];
		jtable = new JTable(dtm);
		jtable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jtable.getSelectionModel().addListSelectionListener(this);
		jtable.getColumnModel().getColumn(2).setMaxWidth(40);
		
		
		JScrollPane jsp = new JScrollPane(jtable);
		jsp.setBounds(WIDTH-400+20, 10, 300, 300-5);
		jtable.setFillsViewportHeight(true);
		frame.add(jsp);
		
		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////COMPONENTS RELATED STUFF/////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
		btnAdd = new JButton("Add");
		btnAdd.setBounds(WIDTH-400+20, jsp.getY()+jsp.getHeight()+5,112,40);
		btnAdd.addMouseListener(this);
		frame.add(btnAdd);
		
		btnDel = new JButton("Delete");
		btnDel.setBounds(btnAdd.getX()+btnAdd.getWidth(), jsp.getY()+jsp.getHeight()+5,112,40);
		btnDel.addMouseListener(this);
		frame.add(btnDel);
		
//		btnEdit = new JButton("Edit");
//		btnEdit.setBounds(btnDel.getX()+75,jsp.getY()+jsp.getHeight()+20,75,40);
//		btnEdit.addMouseListener(this);
//		frame.add(btnEdit);
		String[] values = {"1", "2", "3", "4", "5", "6", "7" };
		addCircleAndText = new JSpinner(new SpinnerListModel(values));
		addCircleAndText.setBounds(btnDel.getX()+btnDel.getWidth(), btnDel.getY(), 75, 40);
		addCircleAndText.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(((String)addCircleAndText.getValue()).matches("\\d+")) {
					int value = Integer.parseInt((String)addCircleAndText.getValue());
					if(value > 7) {
						addCircleAndText.setValue(new String("7"));
					}else if(value < 1) {
						addCircleAndText.setValue(new String("1"));
					}
				}else {
					addCircleAndText.setValue(new String("1"));
				}
				
			}
		});
		frame.add(addCircleAndText);
		
		txtAddText = new JTextField();
		txtAddText.setBounds(btnAdd.getX(), btnAdd.getY()+40+5,300,40);
		txtAddText.addFocusListener(new FocusListener() {


			@Override
			public void focusGained(FocusEvent e) {
				jtable.clearSelection();
				switchPanel(0);
				setProperties(TEXT_TYPE,VALUE_DEFAULT);
				
				
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		frame.add(txtAddText);
		
//		delAddText = new JTextField();
//		delAddText.setBounds(txtAddText.getX()+230+20, btnAdd.getY()+40+20, 50, 40);
//		frame.add(delAddText);

		btnAddText = new JButton("ADD TEXT");
		btnAddText.setBounds(txtAddText.getX(),txtAddText.getY()+40,txtAddText.getWidth(),40);
		btnAddText.addMouseListener(this);
		frame.add(btnAddText);
		
		JLabel lblMode = new JLabel("Mode: ");
		lblMode.setBounds(btnAddText.getX()+5, btnAddText.getY()+btnAddText.getHeight(), 45, 40);
		frame.add(lblMode);

		String[] modes = {"Manual","Automatic Placement and Resizing","Automatic Placement and No Resizing"};
		selectMode = new JComboBox(modes);
		selectMode.setSelectedIndex(1);
		selectMode.setBounds(lblMode.getX()+lblMode.getWidth(), lblMode.getY(), 250, 40);
		frame.add(selectMode);
		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////CUSTOMIZE TEXT PANEL RELATED STUFF//////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
		
		int panelCusHeight = jlpane.getY()+jlpane.getHeight()-(selectMode.getY()+selectMode.getHeight()+20);
		panelCustomizeText = new JPanel();
		panelCustomizeText.setBounds(lblMode.getX(), selectMode.getY()+selectMode.getHeight()+20,300, panelCusHeight);
		panelCustomizeText.setLayout(null);
		panelCustomizeText.setVisible(false);
		
		txtEditText = new JTextField();
		txtEditText.setBounds(0, 5, 300, 30);
		txtEditText.getDocument().addDocumentListener(new DocumentListener(){
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(jtable.getSelectedRowCount() == 1) {
					int index = Integer.parseInt(jtable.getValueAt(jtable.getSelectedRow(), 2).toString());
					String type = jtable.getValueAt(jtable.getSelectedRow(), 1).toString();
					if(type.equals(TEXT_TYPE) && txtEditText.getText().length() > 0) {
						JLabel lbl = (JLabel) jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index]; 
						lbl.setSize(500, 500);
						lbl.setText(txtEditText.getText());
						lbl.setSize(lbl.getPreferredSize());
						TI.get(index).setText(txtEditText.getText());
					}
				}
				
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(jtable.getSelectedRowCount() == 1) {
					int index = Integer.parseInt(jtable.getValueAt(jtable.getSelectedRow(), 2).toString());
					String type = jtable.getValueAt(jtable.getSelectedRow(), 1).toString();
					if(type.equals(TEXT_TYPE) && txtEditText.getText().length() > 0) {
						JLabel lbl = (JLabel) jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index]; 
						lbl.setSize(500, 500);
						lbl.setText(txtEditText.getText());
						lbl.setSize(lbl.getPreferredSize());
						TI.get(index).setText(txtEditText.getText());
					}else if (type.equals(TEXT_TYPE) && txtEditText.getText().length() == 0) {
						 Runnable doHighlight = new Runnable() {
						        @Override
						        public void run() {
									int index2 = Integer.parseInt(jtable.getValueAt(jtable.getSelectedRow(), 2).toString());
						            txtEditText.setText(TI.get(index2).getText());
						        }
						    };       
						    SwingUtilities.invokeLater(doHighlight);
					}
				}

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				
			}
			
		});
		panelCustomizeText.add(txtEditText);
		
		lblFont = new JLabel("Selected Font: ");
		lblFont.setBounds(txtEditText.getX(), txtEditText.getY()+txtEditText.getHeight(), 100, 30);
		panelCustomizeText.add(lblFont);
		
		btnFont = new JButton("SansSerif, plain, 12");
		btnFont.setBounds(lblFont.getX()+lblFont.getWidth(),lblFont.getY(),200,30);
		btnFont.addActionListener(this);
		panelCustomizeText.add(btnFont);
		
		lblForeColor = new JLabel("Selected Foreground Color: ");
		lblForeColor.setBounds(0,65,200, 30);
		panelCustomizeText.add(lblForeColor);
		
		btnForeColor = new JButton();
		btnForeColor.setBounds(lblForeColor.getX()+lblForeColor.getWidth(), lblForeColor.getY(), 100, 30);
		btnForeColor.setBackground(Color.BLACK);
		btnForeColor.addActionListener(this);
		panelCustomizeText.add(btnForeColor);
		
		lblBackColor = new JLabel("Selected Background Color: ");
		lblBackColor.setBounds(0,lblForeColor.getY()+lblForeColor.getHeight(),200, 30);
		panelCustomizeText.add(lblBackColor);
		
		btnBackColor = new JButton();
		btnBackColor.setBounds(lblBackColor.getX()+lblBackColor.getWidth(), lblBackColor.getY(), 100, 30);
		btnBackColor.setBackground(Color.BLACK);
		btnBackColor.addActionListener(this);
		panelCustomizeText.add(btnBackColor);
				
		checkOpaque = new JCheckBox("Opaque background");
		checkOpaque.setSelected(false);
		checkOpaque.setHorizontalAlignment(JCheckBox.CENTER);
		checkOpaque.setBounds(panelCustomizeText.getWidth()/2-100, btnBackColor.getY()+btnBackColor.getHeight()+5, 200, 30);
		checkOpaque.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(checkOpaque.isSelected()) {
					int[] indexes = jtable.getSelectedRows();
					for(int i = 0; i < indexes.length; i++) {
						int index = Integer.parseInt(jtable.getValueAt(indexes[i], 2).toString());
						String type = jtable.getValueAt(indexes[i], 1).toString();
						
						if(type.equals(TEXT_TYPE)) {
							TI.get(index).setOpaque(true);
							((JLabel)jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index]).setOpaque(true);
							jlpane.repaint();
						}
						
					}
				}else {
					int[] indexes = jtable.getSelectedRows();
					for(int i = 0; i < indexes.length; i++) {
						int index = Integer.parseInt(jtable.getValueAt(indexes[i], 2).toString());
						String type = jtable.getValueAt(indexes[i], 1).toString();
						
						if(type.equals(TEXT_TYPE)) {
							TI.get(index).setOpaque(true);
							((JLabel)jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index]).setOpaque(false);
							jlpane.repaint();
						}
						
					}
				}
			}
			
		});
		panelCustomizeText.add(checkOpaque);
		
		frame.add(panelCustomizeText);
		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////CUSTOMIZE CIRCLE PANEL RELATED STUFF////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
		
		panelCustomizeCir = new JPanel();
		panelCustomizeCir.setBounds(lblMode.getX(), selectMode.getY()+selectMode.getHeight()+20,300, panelCusHeight);
		panelCustomizeCir.setLayout(null);
		panelCustomizeCir.setVisible(false);
		
		lblCColor = new JLabel("Selected Color");
		lblCColor.setBounds(0, 5, 100, 30);
		panelCustomizeCir.add(lblCColor);
		
		btnCColor = new JButton();
		btnCColor.setBounds(lblCColor.getX()+lblCColor.getWidth()+100, lblCColor.getY(), 100, 30);
		btnCColor.setBackground(Color.black);
		btnCColor.addActionListener(this);
		panelCustomizeCir.add(btnCColor);
		

		lblStroke = new JLabel("Selected Stroke Size: ");
		lblStroke.setBounds(0, lblCColor.getY()+lblCColor.getHeight(), 200, 30);
		panelCustomizeCir.add(lblStroke);
		
		sliderStroke = new JSlider(JSlider.HORIZONTAL, SIZE_S_MIN, SIZE_S_MAX, SIZE_S_INIT);
		sliderStroke.setSize(300,30);
		sliderStroke.setMajorTickSpacing(2);
		sliderStroke.setMinorTickSpacing(1);
		sliderStroke.setPaintLabels(true);
		sliderStroke.setPaintTicks(true);
		sliderStroke.setSnapToTicks(true);
		sliderStroke.setBounds(0, lblStroke.getY()+lblStroke.getHeight()-10, 300, 50);
		sliderStroke.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(!sliderStroke.getValueIsAdjusting()) {
					int strokeSize = sliderStroke.getValue();
					int[] indexes = jtable.getSelectedRows();
					for(int i = 0; i < indexes.length; i++) {
						int index = Integer.parseInt(jtable.getValueAt(indexes[i], 2).toString());
						CircleInfo c = CI.get(index);
						c.setStrokeSize(strokeSize);
						c.updateImage();
						CI.set(index, c);
						jlpane.repaint();
					}
				}
			}
			
		});
		panelCustomizeCir.add(sliderStroke);
		
		
		lblCSize = new JLabel("Selected Size: ");
		lblCSize.setBounds(0, sliderStroke.getY()+lblStroke.getHeight()+15, 100, 30);
		panelCustomizeCir.add(lblCSize);
		
		sliderSize = new JSlider(JSlider.HORIZONTAL, SIZE_C_MIN, SIZE_C_MAX, SIZE_C_INIT);
		sliderSize.setMajorTickSpacing(100);
		sliderSize.setMinorTickSpacing(50);
		sliderSize.setPaintLabels(true);
		sliderSize.setPaintTicks(true);
		//sliderSize.setSnapToTicks(true);
		sliderSize.setBounds(0, lblCSize.getY()+lblCSize.getHeight()-10, 300, 50);
		sliderSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("HELLO2");
				// TODO Auto-generated method stub
				if(!sliderSize.getValueIsAdjusting()) {
					int size = sliderSize.getValue();
					int[] indexes = jtable.getSelectedRows();
					for(int i = 0; i < indexes.length; i++) {
						int index = Integer.parseInt(jtable.getValueAt(indexes[i], 2).toString());
						//CI.get(index).setSize(size);
						//CI.get(index).updateImage();
						CircleInfo c = CI.get(index);
						c.setSize(size);
						c.updateImage();
						Draw d = (Draw) jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[index];
						d.size = size;
						d.setOpaque(false);
						d.setBounds(c.getX(), c.getY(), c.getSize(), c.getSize());
						
					}
					jlpane.repaint();
				}
			}
			
		});
		panelCustomizeCir.add(sliderSize);
		
		frame.add(panelCustomizeCir);
		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////CUSTOMIZE MESSAGE PANEL RELATED STUFF////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////

		panelCustomizeMsg = new JPanel();
		panelCustomizeMsg.setBounds(lblMode.getX(), selectMode.getY()+selectMode.getHeight()+20,300, panelCusHeight);
		panelCustomizeMsg.setLayout(null);
		panelCustomizeMsg.setVisible(true);
		
		JLabel lblMsg = new JLabel("Please select any same components to modify!");
		lblMsg.setHorizontalAlignment(JLabel.CENTER);
		lblMsg.setBounds(0, panelCustomizeMsg.getHeight()/2-15, 300, 30);
		panelCustomizeMsg.add(lblMsg);
		
		frame.add(panelCustomizeMsg);
		
		
/////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////INITIATE COMPONENT RELATED STUFF////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
		
		if(mode.equals(MODE_OPEN)) {
			savePath = path;
			
			ReadAndWriteObject rw = new ReadAndWriteObject(new File(savePath));
			rw.read();
			CI = rw.getCI();
			TI = rw.getTI();
			
			PANE_INDEX = CI.size();
			for(int i = 0; i < CI.size(); i++) {
				jlpane.add(new Draw(i,mode),JLayeredPane.DEFAULT_LAYER,i);
			    jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[i].addMouseListener(this);
			    addRow(i,CIRCLE_TYPE);
			}
			
			TINDEX = TI.size();
			for(int i = 0; i < TI.size(); i++) {
				JLabel t = new Text(i);
				if(t.getFont().getStyle() == 2 || t.getFont().getStyle() == 3) {
					Dimension d = t.getPreferredSize();
					t.setSize((int)d.getWidth()+5,(int)d.getHeight());
				}else {
					t.setSize(t.getPreferredSize());
				}
				jlpane.add(t,JLayeredPane.MODAL_LAYER,i);
			    jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[i].addMouseListener(this);
			    addRow(i,TEXT_TYPE);

			}
			
		}else {
			
			//init stuff
		    savePath = "";
		    
			jlpane.add(new Draw(CINDEX),JLayeredPane.DEFAULT_LAYER,CINDEX);
		    jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[CINDEX].addMouseListener(this);
		    addRow(CINDEX,CIRCLE_TYPE);
			System.out.println("SIZE: "+ CI.size() + "CC: " + jlpane.getComponentCountInLayer(0));
		
			CINDEX++;
		    jlpane.add(new Draw(CINDEX),JLayeredPane.DEFAULT_LAYER,CINDEX);
		    jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[CINDEX].addMouseListener(this);
		    addRow(CINDEX,CIRCLE_TYPE);
			System.out.println("SIZE: "+ CI.size() + "CC: " + jlpane.getComponentCountInLayer(0));
		
		}
		
		frame.setVisible(true);		
	
	
	}
	
	
	
	public void initiateChoosingDirectory(BufferedImage saveImage) {
		

		JFileChooser jfc = new JFileChooser();
		String desktop = System.getProperty("user.home") + "/Desktop";
		
		
		jfc.setDialogTitle("Choose a destination directory");
			
		FileFilter jpegFilter = new CustomFilter(".jpeg", "JPEG");
		FileFilter pngFilter = new CustomFilter(".png", "PNG");
			
		jfc.addChoosableFileFilter(jpegFilter);
		jfc.addChoosableFileFilter(pngFilter);
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setCurrentDirectory(new File(desktop));
		int response = jfc.showSaveDialog(frame);
			
			
		if(response == JFileChooser.APPROVE_OPTION) {
			File path = jfc.getSelectedFile();
			String type = (jfc.getFileFilter() == jpegFilter)? "jpeg" : "png";
			//create a path so that files are inside "VennDiagramImages" folder
			File dirCreate = new File(path.getParentFile().getAbsolutePath()+"/VennDiagramImages");
			if(!dirCreate.exists()) {
				dirCreate.mkdir();
			}
			savePath = dirCreate.getAbsolutePath()+"/"+path.getName()+"."+type;
			System.out.println(jfc.getSelectedFile().getPath() + " (*." + type + ")");
			saveImage(saveImage,type,new File(savePath));
		}
	}
	
	
	public void saveImage(BufferedImage saveImage,String type, File dest) {
		try {
						
			ImageIO.write(saveImage,type,dest);
			
			ReadAndWriteObject rw = new ReadAndWriteObject(dest);
			rw.write();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	public static int[] generateCircleBounds() {
		//jlayeredpane starts at x = 10 and ends at x = 10 + (WIDTH-400)
		//jlayeredpane starts at y = 10 and ends at y = 10 + (HEIGHT-100)
		//median of x is at x = (WIDTH-400)/2
		//median of y is at y = (HEIGHT-100)/2
		//where [(WIDTH-400)/2, (HEIGHT-100)/2] is middle point in jlayeredpane
		
		
		int endX = ((WIDTH-400)/2) + (int)((Draw.SIZE/4)*Math.cos( (2*Math.PI/MainFrame.PANE_INDEX)*MainFrame.CINDEX ));
		int endY = ((HEIGHT-100)/2) - (int)((Draw.SIZE/4)*Math.sin( (2*Math.PI/MainFrame.PANE_INDEX)*MainFrame.CINDEX));  // Note "-"
		return new int[] {endX,endY};
	}
	
	public void updateIndexes(int startIndex) {
		for(int i = startIndex; i < jlpane.getComponentCountInLayer(JLayeredPane.DEFAULT_LAYER);i++) {
			Draw d = (Draw) jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[startIndex];
			d.index -= 1;
		}
	}

	public void updateBounds(String mode) { 
		if(mode != MODE_MANUAL) {
			for(CINDEX=0; CINDEX < jlpane.getComponentCountInLayer(JLayeredPane.DEFAULT_LAYER); CINDEX++) {
				int[] xy = updateCircle(CINDEX, mode);
				Draw d = (Draw) jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[CINDEX];
				if(mode == MODE_AUTO) {
					d.size = Draw.SIZE;
				}
				d.setBounds(xy[0], xy[1],d.size, d.size);
			}
		}else {
			CINDEX = jlpane.getComponentCountInLayer(JLayeredPane.DEFAULT_LAYER);
		}
		
	}
	
	
	public int[] updateCircle(int index, String mode) {
		CircleInfo prevElem = CI.get(index);
		if(mode == MODE_AUTO) {
			prevElem.setSize(Draw.SIZE);
		}
		
		prevElem.updateImage();
		int[] bounds = generateCircleBounds();
		int newX = bounds[0]-(Draw.SIZE/2);
		int newY = bounds[1]-(Draw.SIZE/2);
		prevElem.setX(newX);
		prevElem.setY(newY);
		CI.set(index, prevElem);
		return new int[] {newX,newY};
	}
	
	public void addRow(int index, String type) {
		DefaultTableModel dtm = (DefaultTableModel) jtable.getModel();
		if(type.equals(CIRCLE_TYPE)) {
			dtm.addRow(new Object[]{"Circle"+index, type, index});
		}else {
			dtm.addRow(new Object[] {"Text"+index,type,index});
		}
	}
	
	
	public void removeRow(int index) {
		DefaultTableModel dtm = (DefaultTableModel) jtable.getModel();
		dtm.removeRow(index);
	}
	
	public boolean isSameType(String t) {
		String type = t;
		for(int index : jtable.getSelectedRows()) {
			if(type != jtable.getValueAt(index, 1)) {
				return false;
			}
		}
		return jtable.getSelectedRowCount()>0;
	}
	
	public String[] fontToString(Font f) {
		String name;
		if(f.getFontName().indexOf(".") == -1) {
			name = f.getFontName();
		}else {
			name = f.getFontName().substring(0,f.getFontName().indexOf("."));
		}
		
		String style;
		switch(f.getStyle()) {
		case 0: //plain
			style = "Plain";
			break;
		case 1: //bold
			style = "Bold";
			break;
		case 2: //Italic
			style = "Italic";
			break;
		case 3: //BoldItalic
			style = "BoldItalic";
			break;
		default:
			style = "Plain";
		}
		Integer size = f.getSize();
		return new String[] {name, style,""+size};
	}
	
	public Font stringToFont(String text) {
		String[] arr = text.split(", ");
		String name = arr[0];
		int style = Font.PLAIN;
		
		switch(arr[1]) {
		case "Plain": style = Font.PLAIN;
		break;
		case "Bold": style = Font.BOLD;
		break;
		case "Italic": style = Font.ITALIC;
		break;
		case "BoldItalic": style = Font.BOLD + Font.ITALIC;
		break;
		default:
			style = Font.PLAIN;
		}
		
		int size = Integer.parseInt(arr[2]);
		
		return stringToFont(name,style,size);
	}
	
	public Font stringToFont(String name, int style, int size) {
		return new Font(name,style,size);
	}
	
	public void setProperties(String t, String v) {
		if(v.equals(VALUE_DEFAULT)) {
			if(t.equals(CIRCLE_TYPE)) {
				btnCColor.setBackground(Color.black);
				sliderSize.setValueIsAdjusting(true);
				sliderStroke.setValueIsAdjusting(true);
				sliderStroke.setValue(2);
				sliderSize.setValue(600);
				System.out.println("HELLO");
			}else if(t.equals(TEXT_TYPE)) {
				txtEditText.setEnabled(false);
				txtEditText.setText("");
				btnForeColor.setBackground(Color.black);
				btnBackColor.setBackground(Color.black);
				Font f = new Font("SansSerif",Font.PLAIN,12);
				String[] arr = fontToString(f);
				btnFont.setText(arr[0]+", "+arr[1]+", " + Integer.parseInt(arr[2]));
				checkOpaque.setSelected(false);
			}
		}else {
			int index = Integer.parseInt(jtable.getValueAt(jtable.getSelectedRow(), 2).toString());

			if(t.equals(CIRCLE_TYPE)) {
				btnCColor.setBackground(CI.get(index).getColor());
				sliderSize.setValueIsAdjusting(true);
				sliderStroke.setValueIsAdjusting(true);
				sliderStroke.setValue(CI.get(index).getStrokeSize());
				sliderSize.setValue(CI.get(index).getSize());
			}else if(t.equals(TEXT_TYPE)) {
				txtEditText.setText(TI.get(index).getText());
				btnForeColor.setBackground(TI.get(index).getForeColor());
				btnBackColor.setBackground(TI.get(index).getBackColor());
				Font f = TI.get(index).getFont();
				String[] arr = fontToString(f);
				btnFont.setText(arr[0]+", "+arr[1]+", " + Integer.parseInt(arr[2]));
				checkOpaque.setSelected(TI.get(index).isOpaque());
			}
		}
	}
	
	public void updateTable() {
		int ccounter = 0, tcounter=0;
		for(int i = 0; i < jtable.getRowCount(); i++) {
			if(jtable.getValueAt(i, 1).equals(CIRCLE_TYPE)) {
				jtable.setValueAt(""+ccounter, i, 2);
				ccounter++;
			}else if(jtable.getValueAt(i, 1).equals(TEXT_TYPE)) {
				jtable.setValueAt(""+tcounter, i, 2);
				tcounter++;
			}
		}
	}
	
	public void switchPanel(int state) {
		//state: 0 - panelCustomizeText is visible, 1 - panelCustomizeCir is visible, 2 - panelCustomizeMsg is visible
		
		if(state == 0) {
			panelCustomizeCir.setVisible(false);
			panelCustomizeMsg.setVisible(false);
			panelCustomizeText.setVisible(true);
			for(Component c : panelCustomizeText.getComponents()) {
				c.setEnabled(true);
			}
		}else if(state == 1) {
			panelCustomizeMsg.setVisible(false);
			panelCustomizeText.setVisible(false);
			panelCustomizeCir.setVisible(true);
			for(Component c : panelCustomizeCir.getComponents()) {
				c.setEnabled(true);
			}
		}else if(state == 2) {
			panelCustomizeCir.setVisible(false);
			panelCustomizeText.setVisible(false);
			panelCustomizeMsg.setVisible(true);
			for(Component c : panelCustomizeMsg.getComponents()) {
				c.setEnabled(true);
			}
		}else {
			//Undefined state
		}
		
	}
	
	public int getAddValue() {
		String value = (String)addCircleAndText.getValue();
		return Integer.parseInt(value);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
		
		if(arg0.getSource() == btnAdd) {
			for(int i = 0; i < getAddValue(); i++) {
				if(PANE_INDEX < MAX_CIRCLES) {
					PANE_INDEX++;
					if(PANE_INDEX == 3 || PANE_INDEX == 4) {
						Draw.SIZE -= 145;
					}
					updateBounds(selectMode.getSelectedItem().toString());
					Draw d = new Draw(CINDEX);
					//if(selectMode.getSelectedItem().toString().equals(MODE_MANUAL)){
						d.size = Draw.SIZE;
					//}
					jlpane.add(d,JLayeredPane.DEFAULT_LAYER,CINDEX);
				    jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[CINDEX].addMouseListener(this);
					DefaultTableModel dtm = (DefaultTableModel) jtable.getModel();
					addRow(CINDEX,CIRCLE_TYPE);
				}
			}
			jlpane.repaint();
			
		}else if(arg0.getSource() == btnDel) {
			if(!jtable.getSelectionModel().isSelectionEmpty()) {
				int[] selIndexes = jtable.getSelectedRows();
				
				for(int i = selIndexes.length-1; i >= 0; i--) {
					if(jtable.getValueAt(selIndexes[i], 1).toString().equals(CIRCLE_TYPE)) {
						System.out.println("CIRCLE_TYPE");
						if(PANE_INDEX > MIN_CIRCLES) {
							PANE_INDEX--;
							if(PANE_INDEX == 2 || PANE_INDEX == 3) {
								Draw.SIZE += 145;
							}
							int index = Integer.parseInt(jtable.getValueAt(selIndexes[i], 2).toString());
							System.out.println("CI SIZE: " + index);
							
							if(jlpane.getComponentCountInLayer(JLayeredPane.DEFAULT_LAYER) != index+1) {
								updateIndexes(index+1);
							}
							Component c = jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[index];
							jlpane.remove(c);

							CI.remove(index);

							updateBounds(selectMode.getSelectedItem().toString());
							removeRow(selIndexes[i]);
						}
					}else if(jtable.getValueAt(selIndexes[i], 1).toString().equals(TEXT_TYPE)) {
						System.out.println("TEXT_TYPE");
						TINDEX--;
						int index = Integer.parseInt(jtable.getValueAt(selIndexes[i], 2).toString());
						TI.remove(index);
						Component c = jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index];
						jlpane.remove(c);
						removeRow(selIndexes[i]);
					}
				}
				updateTable();
				jlpane.repaint();
			}
		}else if(arg0.getSource() == btnAddText) {
			if(txtAddText.getText().length() > 0) {
				for(int i = 0; i < getAddValue(); i++) {
					
					addRow(TINDEX,TEXT_TYPE);
					
					Font f = stringToFont(btnFont.getText());
					Object[] data = {f,btnForeColor.getBackground(), btnBackColor.getBackground(),
							checkOpaque.isSelected(), txtAddText.getText()};
					
					JLabel t= new Text(TINDEX,data);
					System.out.println(t.getFont().toString());
					t.setSize(t.getPreferredSize());
					jlpane.add(t,JLayeredPane.MODAL_LAYER);
				    jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[TINDEX].addMouseListener(this);
					TINDEX++;
				}
				txtAddText.setText("");
				switchPanel(2);
				setProperties(TEXT_TYPE,VALUE_DEFAULT);
			}
			
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		for(int k = 0; k < jlpane.getComponentCountInLayer(JLayeredPane.DEFAULT_LAYER); k++) {
			if(jlpane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[k] == arg0.getSource()) {
				for(int j = 0; j < jtable.getRowCount(); j++) {
					int index = Integer.parseInt(jtable.getValueAt(j, 2).toString());
					String type = jtable.getValueAt(j, 1).toString();
					
					if(k == index && type == CIRCLE_TYPE) {
						jtable.setRowSelectionInterval(j, j);
						break;
					}
				}
			}
		}
		
		for(int k = 0; k < jlpane.getComponentCountInLayer(JLayeredPane.MODAL_LAYER); k++) {
			if(jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[k] == arg0.getSource()) {
				for(int j = 0; j < jtable.getRowCount(); j++) {
					int index = Integer.parseInt(jtable.getValueAt(j, 2).toString());
					String type = jtable.getValueAt(j, 1).toString();
					
					if(k == index && type == TEXT_TYPE) {
						jtable.setRowSelectionInterval(j, j);
						break;
					}
				}
			}
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainFrame();
            }
        });
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		if(btnFont == e.getSource()) {
			Font f = stringToFont(btnFont.getText());
			jfc.setSelectedFont(f);
			int option = jfc.showDialog(frame);
			if(option == jfc.OK_OPTION) {
				Font newF = jfc.getSelectedFont();
				String[] arr = fontToString(newF);
				btnFont.setText(arr[0] + ", " + arr[1] + ", " + Integer.parseInt(arr[2]));
				if(jtable.getSelectedRowCount() == 1) {
					int index = Integer.parseInt(jtable.getValueAt(jtable.getSelectedRow(), 2).toString());
					String type = jtable.getValueAt(jtable.getSelectedRow(), 1).toString();
					
					if(type.equals(TEXT_TYPE)) {
						JLabel lbl = (JLabel) jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index];
						lbl.setSize(500, 500);
						lbl.setFont(newF);
						if(newF.getStyle() == 2 || newF.getStyle() == 3) {
							Dimension d = lbl.getPreferredSize();
							lbl.setSize((int)d.getWidth()+5,(int)d.getHeight());
						}else {
							lbl.setSize(lbl.getPreferredSize());
						}
						
						TI.get(index).setFont(newF);
					}
				}
			}
			
		}else if(e.getSource() == btnForeColor) {
			btnForeClicked = true;
			btnCColorClicked = false;
			btnFontClicked = false;
			jcc.setColor(btnForeColor.getBackground());
			JDialog dialog = JColorChooser.createDialog(frame, "Select a Color", true, jcc,this,null);
			dialog.setVisible(true);
		}else if(e.getSource() == btnBackColor){
			btnForeClicked = false;
			btnCColorClicked = false;
			btnFontClicked = false;
			jcc.setColor(btnBackColor.getBackground());
			JDialog dialog = JColorChooser.createDialog(frame, "Select a Color", true, jcc,this,null);
			dialog.setVisible(true);
		}else if(e.getSource() == btnCColor) {
			btnCColorClicked = true;
			btnFontClicked = false;
			jcc.setColor(btnCColor.getBackground());
			JDialog dialog = JColorChooser.createDialog(frame, "Select a Color", true, jcc,this,null);
			dialog.setVisible(true);
		}else {
			if(btnCColorClicked) {
				Color c = jcc.getColor();
				btnCColor.setBackground(c);
				int[] indexes = jtable.getSelectedRows();
				for(int i = 0; i < indexes.length; i++) {
					int index = Integer.parseInt(jtable.getValueAt(indexes[i], 2).toString());
					String type = jtable.getValueAt(indexes[i], 1).toString();
					
					if(type.equals(CIRCLE_TYPE)) {
						CI.get(index).setColor(c);
						CI.get(index).updateImage();
						jlpane.repaint();
					}
					
				}
			}else if(btnForeClicked) {
				Color c = jcc.getColor();
				btnForeColor.setBackground(c);
				int[] indexes = jtable.getSelectedRows();
				for(int i = 0; i < indexes.length; i++) {
					int index = Integer.parseInt(jtable.getValueAt(indexes[i], 2).toString());
					String type = jtable.getValueAt(indexes[i], 1).toString();
					
					if(type.equals(TEXT_TYPE)) {
						TI.get(index).setForeColor(c);
						jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index].setForeground(c);
						jlpane.repaint();
					}
					
				}

			}else {
				Color c = jcc.getColor();
				btnBackColor.setBackground(c);
				int[] indexes = jtable.getSelectedRows();
				for(int i = 0; i < indexes.length; i++) {
					int index = Integer.parseInt(jtable.getValueAt(indexes[i], 2).toString());
					String type = jtable.getValueAt(indexes[i], 1).toString();
					
					if(type.equals(TEXT_TYPE)) {
						TI.get(index).setBackColor(c);
						jlpane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)[index].setBackground(c);
						jlpane.repaint();
					}
					
				}
			}
		}
	}



	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	
		
	}



	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int codeOpen = KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK).getKeyCode();
		int codeSave = KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK).getKeyCode();
		int codeSaveAs = KeyStroke.getKeyStroke(KeyEvent.VK_A,ActionEvent.CTRL_MASK).getKeyCode();
		int codeClose = KeyStroke.getKeyStroke(KeyEvent.VK_W,ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK).getKeyCode();
		int codeAbout = KeyStroke.getKeyStroke(KeyEvent.VK_A,ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK).getKeyCode();
		int codeSearch = KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK).getKeyCode();
		
		System.out.println("keycode: "+ e.getKeyCode() + " keystrokecode: " + codeClose);
		
		if(e.getKeyCode() == codeOpen) {
			
		}else if(e.getKeyCode() == codeSave) {
			
		}else if(e.getKeyCode() == codeSaveAs) {
			
		}else if(e.getKeyCode() == codeClose) {
			System.exit(0);
		}else if(e.getKeyCode() == codeAbout) {
			
		}else if(e.getKeyCode() == codeSearch) {
			
		}
	}



	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
	
	}
	
	

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()) {			
			if(e.getSource() == jtable.getSelectionModel()) {
			
				if(isSameType(CIRCLE_TYPE)) {
					if(jtable.getSelectedRowCount() > 1) {
						setProperties(CIRCLE_TYPE, VALUE_DEFAULT);
					}else {
						setProperties(CIRCLE_TYPE, VALUE_CUSTOMIZE);
					}
					switchPanel(1);
				}else if(isSameType(TEXT_TYPE)){
					if(jtable.getSelectedRowCount() > 1) {
						setProperties(TEXT_TYPE, VALUE_DEFAULT);
					}else {
						setProperties(TEXT_TYPE, VALUE_CUSTOMIZE);
					}
					switchPanel(0);
				}else if(isSameType(PANEL_TYPE)) {
					
				}else {
					//UNDEFINED TYPE...ERROR
					switchPanel(2);
				}
			}
		}
	}

}
