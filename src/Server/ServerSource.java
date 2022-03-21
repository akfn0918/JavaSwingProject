package Server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ServerSource extends JFrame implements ActionListener {
	
	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea ta = new JTextArea();
	private JButton sstart_btn = new JButton("서버 실행");
	private JButton sstop_btn = new JButton("서버 중지");
	
	//Network자원
	
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector user_vc = new Vector();
	
	ServerSource()
	{
		init(); //화면 생성 메소드
		saction(); //리스너 설정 메소드
	}
	
	private void saction()
	{
		sstart_btn.addActionListener(this);
		sstop_btn.addActionListener(this);
	}
	
	private void init()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 308, 338);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 268, 193);
		contentPane.add(scrollPane);
		
		scrollPane.setViewportView(ta);
		ta.setEditable(false);
		
		JLabel lblNewLabel = new JLabel("포트 번호");
		lblNewLabel.setBounds(12, 213, 77, 15);
		contentPane.add(lblNewLabel);
		
		port_tf = new JTextField();
		port_tf.setBounds(74, 210, 206, 21);
		contentPane.add(port_tf);
		port_tf.setColumns(10);
		
		sstart_btn.setBounds(12, 238, 127, 54);
		contentPane.add(sstart_btn);
		
		sstop_btn.setBounds(153, 238, 127, 54);
		contentPane.add(sstop_btn);
		sstop_btn.setEnabled(false);
		
		this.setVisible(true);
	}

	private void Server_start()
	{
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(server_socket != null) //정상적으로 포트가 열렸을 경우
		{
			Connection();
		}
		
	}
	
	private void Connection()
	{	
		
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(true) {
				
				try {
					ta.append("사용자 대기중...\n");
					socket=server_socket.accept(); //사용자 접속 무한대기
					
					Userinfo user = new Userinfo(socket);
					
					user.start(); //객체의 스레드 실행
					
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null,"서버가 종료되었습니다.","알림",JOptionPane.ERROR_MESSAGE);
					break;
				}
			  } //while문-end
			}
		});
				
		th.start();
		
	}
		
		
	public static void main(String[] args) {
		
		new ServerSource();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == sstart_btn)
		{
			System.out.println("서버 실행버튼 클릭");
			
			sstart_btn.setEnabled(false);
			port_tf.setEditable(false);
			sstop_btn.setEnabled(true);
			
			port = Integer.parseInt(port_tf.getText().trim());
			
			Server_start(); //소켓 생성&사용자 접속 대기
			
			ta.append("서버 가동 완료\n");
		}
		else if(e.getSource() == sstop_btn)
		{
			System.out.println("서버 중지버튼 클릭");
			
			sstart_btn.setEnabled(true);
			port_tf.setEditable(true);
			sstop_btn.setEnabled(false);
			
			ta.append("서버 종료\n");
			
			try {
				server_socket.close();
				user_vc.removeAllElements();
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
		}
		
	} //액션이벤트-end
	
	class Userinfo extends Thread
	{
		
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket user_socket;
		private String Nickname = "";
		
		Userinfo(Socket soc)
		{
			this.user_socket = soc;
			
			UserNetwork();
			
		}
		
		private void UserNetwork()
		{	
			try {
			is = user_socket.getInputStream();
			dis = new DataInputStream(is);
			
			os = user_socket.getOutputStream();
			dos = new DataOutputStream(os);
			
			Nickname = dis.readUTF(); //사용자의 닉네임
			ta.append("["+Nickname+"]님이 접속하셨습니다.\n");
			BroadCast("Uinfo/"+"시스템: ["+Nickname+"]님이 접속하셨습니다.\n");
			
			//기존 사용자들에게 새로운 사용자 알림
			
			int usercnt = user_vc.size()+1;
			
			System.out.println("현재 접속된 사용자 수: "+usercnt);
			
			BroadCast("NewUser/"+Nickname); //기존 사용자에게 자신을 알림
			
			//자신에게 기존 사용자를 받아오는 부분
			
			for(int i=0; i<user_vc.size(); i++)
			{
				Userinfo u = (Userinfo)user_vc.elementAt(i);
				
				send_Message("OldUser/"+u.Nickname);
			}
			
			user_vc.add(this); //사용자에게 알린 후 vector에 추가
			
			}
			catch(IOException e)
			{
				
			}
			
			String msg;
	
		}
		
		public void run() //Thread에서 처리
		{
			
			SimpleDateFormat now = new SimpleDateFormat("HH:mm");
			Date time = new Date();
			
			String time1 = now.format(time);
			
			while(true)
			{
				try
				{
					String msg = dis.readUTF();
					BroadCast("Chat/"+Nickname+"("+time1+"): "+msg+"\n");
					
				} catch (IOException e)
				{
					try
					{
						dos.close();
						dis.close();
						user_socket.close();
						user_vc.remove(this);
						ta.append("["+Nickname+"]님이 퇴장하셨습니다.\n");
						ta.append("사용자 대기중...\n");
						BroadCast("UserOut/"+Nickname);
					}
					catch(IOException e1)
					{
					}
					break;
				}
			}
			
			
				
		} //run 메소드 -end
		
		private void BroadCast(String str) //전체 사용자에게 메세지 전송
		{
			for(int i=0; i<user_vc.size(); i++)
			{
				Userinfo u = (Userinfo)user_vc.elementAt(i);
				
				u.send_Message(str);
			}
		}
		
		private void send_Message(String str) //문자를 받아서 전송
		{
		try {
			dos.writeUTF(str);
		} catch(IOException e) {
			e.printStackTrace();
		}
		}
		
	}

}