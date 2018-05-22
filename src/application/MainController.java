package application;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainController implements Initializable
{
	@FXML private Button bConnect;
	@FXML private TextField tfAddress;
	@FXML private Label lStatus;
	
	private Stage stage;
	private String host = "";
	private int port = 0;
	private SocketThread socketThread = new SocketThread();
	
	private GameController gc = GameController.getInstance();
	
	private Runnable readyCheck = new Runnable()
	{
		@Override
		public void run()
		{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Are you ready?");
			alert.setHeaderText("Are you ready to play?");

			Optional<ButtonType> result = alert.showAndWait();
			try
			{
				if (result.get() == ButtonType.OK)
				    socketThread.write("ready");
				else Platform.exit();
			}
			catch(IOException e) { e.printStackTrace(); }
		}
	};
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initialized");
		
		socketThread.setOnEmit((b) -> onSocketEmits(b));
	}

	void setStage(Stage s)
	{
		stage = s;
	}
	
	private void parseAddress()
	{
		if(tfAddress.getText().matches(".*?:\\d+"))
		{
			String[] parts = tfAddress.getText().split(":");
			host = parts[0];
			port = Integer.parseInt(parts[1]);
			socketThread.host = host;
			socketThread.port = port;
		}
	}
	
	private void getConnected() { bConnect.setDisable(true); }
	
	public void bConnectClick(ActionEvent e)
	{
		parseAddress();
		socketThread.setOnConnected((o) -> {
			getConnected();
			return null;
		});
		socketThread.start();
	}
	
	private Void onSocketEmits(byte[] bytes)
	{
		String s = new String(bytes).trim();
		System.out.println("Read: " + s);
		if(s.matches("poll:(O|X)"))
		{
			String side = s.split(":")[1];
			gc.setSide(side);
			Platform.runLater(new Runnable() {	
				@Override public void run() { stage.setTitle("Tic Tac Toe " + side); }
			});
			Platform.runLater(readyCheck);
		}
		else if(s.matches("start:(O|X)"))
		{
			gc.start();
			gc.setTurn(s.split(":")[1]);
			Platform.runLater(new Runnable() {
				 @Override public void run() { resetUi(); setTurnStatus(); }
			});
		}
		else if(s.matches("restart:\\w*"))
		{
			String whoWon = s.split(":")[1];
			System.out.println(whoWon.equalsIgnoreCase("draw") ? whoWon : whoWon + " win!");
			Platform.runLater(new Runnable() {
				 @Override public void run() { resetUi(); setTurnStatus(); }
			});
			Platform.runLater(readyCheck);
		}
		else if(s.matches("\\w\\d:(O|X)"))
		{
			String[] parts = s.split(":");
			gc.setTurn(s.split(":")[1].equals("X") ? "O" : "X");
			Platform.runLater(new Runnable()
			{
				@Override public void run() { setButton(parts[0], parts[1]); setTurnStatus(); }
			});
		}
		return null;
	}
	
	private void resetUi()
	{
		for(int i=0; i<9; i++)
		{
			Button b = (Button)stage.getScene().lookup("#b"+i);
			b.setDisable(false);
			b.setText("");
		}
	}
	
	private void setTurnStatus() { lStatus.setText(gc.yourTurn() ? "YOUR TURN" : "NOT YOUR TURN"); }
	
	private void setButton(String buttonId, String sign)
	{
		Button b = (Button)stage.getScene().lookup("#"+buttonId);
		b.setText(sign);
		b.setDisable(true);
	}
	
	public void bFieldClick(ActionEvent e)
	{
		if(!gc.getStarted() || !gc.yourTurn()) return;
		
		try { socketThread.write(((Button)e.getSource()).getId() + ":" + gc.getSide()); }
		catch (IOException e1) { System.out.println("Cannot send data"); }
	}
	
}
