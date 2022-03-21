package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ClientSource extends JFrame implements ActionListener{
	
	//로그인 gui
	private JFrame Login_GUI = new JFrame();
	private JPanel login_Pane;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JTextField id_tf;
	private JButton login_btn = new JButton("접 속");
	
	//메인gui
	private JPanel contentPane;
	private JTextField textField;
	JButton send_btn = new JButton("전송");
	JButton dbsend = new JButton("대화내용 저장");
	JList pslist = new JList();
	JTextArea chat_ta = new JTextArea();
	
	//네트워크를 위한 자원 변수
	
	private Socket socket;
	private String ip; //자신을 나타내는 ip
	private int port;
	private String id = "";
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	//그외 변수들
	Vector user_list = new Vector();
	StringTokenizer st;
	
	ClientSource()
	{
		Login_init();
		Main_init();
		saction();
	}
	
	private void saction()
	{
		login_btn.addActionListener(this);
		send_btn.addActionListener(this);
		dbsend.addActionListener(this);
	}
	
	private void Login_init()
	{
		Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Login_GUI.setBounds(100, 100, 339, 320);
		login_Pane = new JPanel();
		login_Pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		Login_GUI.setContentPane(login_Pane);
		login_Pane.setLayout(null);
		
		JLabel lblServerIp = new JLabel("서버 IP");
		lblServerIp.setBounds(53, 36, 57, 15);
		login_Pane.add(lblServerIp);
		
		JLabel lblServerPort = new JLabel("서버 포트");
		lblServerPort.setBounds(53, 83, 73, 15);
		login_Pane.add(lblServerPort);
		
		JLabel lblId = new JLabel("닉네임");
		lblId.setBounds(53, 129, 57, 15);
		login_Pane.add(lblId);
		
		ip_tf = new JTextField();
		ip_tf.setBounds(155, 33, 116, 21);
		login_Pane.add(ip_tf);
		ip_tf.setColumns(10);
		
		port_tf = new JTextField();
		port_tf.setBounds(155, 80, 116, 21);
		login_Pane.add(port_tf);
		port_tf.setColumns(10);
		
		id_tf = new JTextField();
		id_tf.setBounds(155, 126, 116, 21);
		login_Pane.add(id_tf);
		id_tf.setColumns(10);
		
		login_btn.setBounds(91, 201, 145, 48);
		login_Pane.add(login_btn);
		
		Login_GUI.setVisible(true);
	}
	
	private void Main_init()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 463);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("접속자");
		lblNewLabel.setBounds(51, 10, 80, 21);
		contentPane.add(lblNewLabel);
		
		pslist.setBounds(12, 34, 119, 348);
		contentPane.add(pslist);
		
		chat_ta.setBounds(143, 10, 279, 372);
		contentPane.add(chat_ta);
		chat_ta.setEditable(false);
		
		textField = new JTextField();
		textField.setBounds(144, 393, 191, 21);
		contentPane.add(textField);
		textField.setColumns(10);
		
		send_btn.setBounds(346, 392, 76, 23);
		contentPane.add(send_btn);
		
		dbsend.setBounds(12, 392, 119, 23);
		contentPane.add(dbsend);
		
		this.setVisible(false);
	}

	private void Network()
	{
		try {
			socket = new Socket(ip, port);
			
			if(socket !=null)
			{
				Connection();
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void Connection() //연결부
	{
		try {
		is = socket.getInputStream();
		dis = new DataInputStream(is);
		
		os = socket.getOutputStream();
		dos = new DataOutputStream(os);
		}
		catch(IOException e)
		{
			
		}
		
		this.setVisible(true);
		Login_GUI.setVisible(false);
		
		//처음 접속시에 ID 전송
		send_message(id);
		
		//pslist에 사용자 추가
		user_list.add(id);
		pslist.setListData(user_list);
		
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(true)
				{
					
					try {
						String msg = dis.readUTF();
						System.out.println("서버로부터 수신된 메세지: " + msg);
						inmessage(msg);
					} catch (IOException e) {
						
					}
					
				}
				
			}
		});
		
		th.start();
		
	}
	
	private void inmessage(String str) //서버로부터 들어오는 모든 메세지
	{
		
		st = new StringTokenizer(str,"/");
		
		String protocol = st.nextToken();
		String Message = st.nextToken();
		
		System.out.println("프로토콜: "+protocol);
		System.out.println("내용: "+Message);
		
		if(protocol.equals("NewUser"))
		{
			user_list.add(Message);
			pslist.setListData(user_list);
		}
		else if(protocol.equals("OldUser"))
		{
			user_list.add(Message);
			pslist.setListData(user_list);
		}
		else if(protocol.equals("Chat"))
		{
			chat_ta.append(Message);
		}
		else if(protocol.equals("Uinfo"))
		{
			chat_ta.append(Message);
		}
		else if(protocol.equals("UserOut"))
		{
			user_list.remove(Message);
			pslist.setListData(user_list);
			chat_ta.append("시스템: ["+Message+"]님이 퇴장하셨습니다.\n");
		}
	}
	
	private void send_message(String str) // 서버에게 메세지를 보냄
	{
		try {
			dos.writeUTF(str);
		}catch(IOException e) {
			
		}
		
	}
	
	public static void main(String[] args) {
		
		new ClientSource();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == login_btn)
		{
			System.out.println("로그인 버튼 클릭");
			ip = ip_tf.getText().trim();
			port = Integer.parseInt(port_tf.getText().trim());
			id = id_tf.getText().trim();
			
			Network();
			
			chat_ta.append("시스템: 접속 완료! 채팅을 시작해보세요!\n");
		}
		else if(e.getSource() == send_btn)
		{
			send_message(textField.getText().trim());
			textField.setText("");
			
			System.out.println("전송 버튼 클릭");
		}
		else if(e.getSource() == dbsend)
		{
			System.out.println("저장 버튼 클릭");
		}
		
	}

}