package activitystreamer.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@SuppressWarnings("serial")
public class TextFrame extends JFrame implements ActionListener {
	private static final Logger log = LogManager.getLogger();
	private JTextArea inputText;
	private static JTextArea outputText;
	private JButton sendButton;
	private JButton disconnectButton;
	private JButton clearButton;
	private JSONParser parser = new JSONParser();
	
	public TextFrame(){
		setTitle("ActivityStreamer Text I/O");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1,2));
		JPanel inputPanel = new JPanel();
		JPanel outputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());
		outputPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),"JSON input, to send to server");
		inputPanel.setBorder(lineBorder);
		lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),"JSON output, received from server");
		outputPanel.setBorder(lineBorder);
		outputPanel.setName("Text output");
		
		inputText = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(inputText);
		inputPanel.add(scrollPane,BorderLayout.CENTER);
		
		JPanel buttonGroup = new JPanel();
		sendButton = new JButton("Send");
		disconnectButton = new JButton("Disconnect");
		buttonGroup.add(sendButton);
		buttonGroup.add(disconnectButton);
		inputPanel.add(buttonGroup,BorderLayout.SOUTH);
		sendButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		
		
		outputText = new JTextArea();
		scrollPane = new JScrollPane(outputText);
		outputPanel.add(scrollPane,BorderLayout.CENTER);

		JPanel buttonGroup1 = new JPanel();
		clearButton = new JButton("Clear");
//		disconnectButton = new JButton("Disconnect");
		buttonGroup1.add(clearButton);
		outputPanel.add(buttonGroup1,BorderLayout.SOUTH);
		clearButton.addActionListener(this);
//		disconnectButton.addActionListener(this);

		
		mainPanel.add(inputPanel);
		mainPanel.add(outputPanel);
		add(mainPanel);
		
		setLocationRelativeTo(null); 
//		setSize(1280,768);
		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public static void setOutputText(final JSONObject obj){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(obj.toJSONString());
		String prettyJsonString = gson.toJson(je).replaceAll("\\\\","");
//		outputText.setText(prettyJsonString);
		outputText.append(prettyJsonString);
		outputText.append("\n");
		outputText.revalidate();
		outputText.repaint();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==sendButton){
			String msg = inputText.getText().trim().replaceAll("\r","").replaceAll("\n","").replaceAll("\t", "");
			JSONObject obj;
//			JSONObject errorObj = null;
			try {
				obj = (JSONObject) parser.parse(msg);
				ClientSkeleton.getInstance().sendActivityObject(obj);
//				System.out.println("----------"+obj.get("command"));
//				if(obj.get("command") == null){
//					ClientSkeleton.getInstance().sendActivityObject(obj);
//				}
//				else{
//					log.error("the received message did not contain a command, data not sent");
//					ClientSkeleton.getInstance().sendInvalidInfoObj("NO_COMMAND");
//				}
			} catch (ParseException e1) {
//                ClientSkeleton.getInstance().sendInvalidInfoObj("JSON_PARSE_ERROR");
				log.error("invalid JSON object entered into input text field, data not sent");

				JSONObject errorObj = new JSONObject();
				errorObj.put("command", "INVALID_MESSAGE");
				errorObj.put("info", "JSON parse error while parsing message");
				setOutputText(errorObj);

			}
			
		} else if(e.getSource()==disconnectButton){
			ClientSkeleton.getInstance().disconnect();
			setVisible(false);
			System.exit(0);
//			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		} else if(e.getSource()==clearButton){
			outputText.setText("");
		}
	}
}
