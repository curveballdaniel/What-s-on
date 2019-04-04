import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoLPanel extends JPanel implements SportsFrameInterface {
	
	JFrame parent;
	Font titleFont = new Font("Elephant", Font.PLAIN, 55);
	Font matchupFont = new Font("Georgia", Font.PLAIN, 32);
	
	// main panel
	JPanel matchesBelow;
	
	// set icon sizes
	int iconWidth = 128;
	int iconHeight = 128;
	
	// background image
	BufferedImage backgroundImage;
	
	// api access token
	private final String PANDA_SCORE_TOKEN = "W0e0u3xQomzT3q8dRgSEBnLqXpN-ydqsY3W8N3cmACvU4orHMAU";
	
	public LoLPanel(JFrame parent) {
		// set parent, object layout
		this.parent = parent;
		parent.getContentPane().add(this);
		this.setLayout(new BoxLayout(this, 1));
	}
	
	public void initializePanel() throws IOException {
		// panel title
		JPanel titleSection = new JPanel();
		titleSection.setLayout(new FlowLayout());
		
		JLabel lblUpcomingLol = new JLabel("Upcoming League of Legends:"); // , SwingConstants.CENTER
		lblUpcomingLol.setFont(titleFont);
		titleSection.add(lblUpcomingLol);
		
		this.add(titleSection);
		
		matchesBelow = new JPanel();
		matchesBelow.setLayout(new BoxLayout(matchesBelow, 1));
		
		this.add(matchesBelow);
		
		refreshInfo();
	}
	
	public void refreshInfo() throws IOException {
		// obtained from
		// https://api.pandascore.co/lol/matches/upcoming
		// https://api.pandascore.co/
		
		if (matchesBelow.getComponents().length > 0) { // if exists, clear, and add again
			matchesBelow.removeAll();
		}
		
		OkHttpClient client = new OkHttpClient();

		// grabs an aggregated average on CryptoCompare - can add '&e=Coinbase' to specify a single exchange
		Request request = new Request.Builder()
				.url("https://api.pandascore.co/lol/matches/upcoming")
				.addHeader("Authorization", "Bearer " + PANDA_SCORE_TOKEN)
				.build();

		Response response = client.newCall(request).execute();

		// JSON interior (needs to get parsed through for vars)
		String JSONstring = response.body().string();
		
		JSONArray upcomingMatches = new JSONArray(JSONstring);
		
		for (int i = 0; i < upcomingMatches.length(); i++) {
			JSONObject leagueObject = upcomingMatches.getJSONObject(i);
			String league = leagueObject.getJSONObject("league").getString("name");
			
			if ((league.equals("LCK") || league.equals("LEC") || league.equals("LCS"))) {
				JPanel individualMatchup = new JPanel();
				individualMatchup.setLayout(new BoxLayout(individualMatchup, 1)); // add a panel to hold time, then Daily Matchup, then Separator
				
				// obtain league image url
				String leagueImg = leagueObject.getJSONObject("league").getString("image_url");
				
				// obtain teams informations
				JSONArray match = leagueObject.getJSONArray("opponents");
				
				// opponents information
				String opponentOneName = "unknown";
				String opponentTwoName = "unknown";
				String opponentOneImg = null;
				String opponentTwoImg = null;
				
				String matchName = "unknown";
				//String liveTime = "unknown";
				String matchBegin = "unknown";
				
				matchName = leagueObject.getString("name");
				//liveTime = leagueObject.getJSONObject("live").getString("opens_at");
				matchBegin = leagueObject.getString("begin_at");
				
				if (match.length() == 2) {
					JSONObject opponentOne = match.optJSONObject(0).getJSONObject("opponent");
					JSONObject opponentTwo = match.optJSONObject(1).getJSONObject("opponent");
					opponentOneName = opponentOne.getString("name"); // get opponent 1 name
					opponentTwoName = opponentTwo.getString("name"); // get opponent 2 name
					
					// get opponents image urls:
					opponentOneImg = opponentOne.getString("image_url");
					opponentTwoImg = opponentTwo.getString("image_url");
					
				} else if (match.length() == 1){
					JSONObject opponentOne = match.optJSONObject(0).getJSONObject("opponent");
					opponentOneName = opponentOne.getString("name");
					opponentTwoName = "*winner";
					
					// get img urls
					opponentOneImg = opponentOne.getString("image_url");
					
				} else { // if match.length() = 0, meaning no opponents are known yet
					
				}
				
				Date matchDate = returnFormattedDate(matchBegin, "yyyy-MM-dd'T'HH:mm:ssZ"); // obtain formatted date

				// 'title' panel for league (holds league info)
				JPanel titlePanel = new JPanel(); 
				titlePanel.setLayout(new FlowLayout());
				addImages(titlePanel, leagueImg, matchName, "", null, "", iconWidth, iconHeight); // add league img, name
				individualMatchup.add(titlePanel); // add title panel to matchups
				
				// 'time' panel for league (holds beginning time)
				JPanel timePanel = new JPanel(); 
				timePanel.setLayout(new FlowLayout());
				JLabel timeLabel = new JLabel("Match starting at: " + turnDateIntoString(matchDate), SwingConstants.CENTER);
				timeLabel.setFont(matchupFont);
				timePanel.add(timeLabel); // add live time
				individualMatchup.add(timePanel); // add time panel to matchups
				
				// 'middle' panel for league (holds matchups)
				JPanel middlePanel = new JPanel(); 
				middlePanel.setLayout(new FlowLayout());
				
				try {
					addImages(middlePanel, opponentOneImg, opponentOneName, " vs. ", opponentTwoImg, opponentTwoName, iconWidth, iconHeight); // add images to panel
				} catch (IOException e) {
					e.printStackTrace();
				} 
				
				individualMatchup.add(middlePanel);
				
				// print console ease of sight
				System.out.println(matchName);
				System.out.println(league + ": " + opponentOneName + " vs. " + opponentTwoName);
				
				// create separator panel
				JPanel separatorPanel = new JPanel();
				separatorPanel.setLayout(new FlowLayout());
				JLabel separatorLabel = new JLabel("--------------------------------------------------------------------------------", SwingConstants.CENTER);
				
				separatorPanel.add(separatorLabel); // add label to panel
				individualMatchup.add(separatorPanel); // add panel to main to separate from next day

				matchesBelow.add(individualMatchup); // add to main panel
			}
		}
	}
	
	public void addImages(JPanel matchesBelow, String teamOneURL, String teamOneName, String attacherString, String teamTwoURL, String teamTwoName, int iconWidth, int iconHeight) throws IOException {
		JPanel matchup = new JPanel();
		matchup.setLayout(new FlowLayout());
		
		if (teamOneURL != null) {
			URL imageOne = new URL(teamOneURL);
			BufferedImage pulledImageOne = ImageIO.read(imageOne);
			JLabel picLabelAway = new JLabel();
			picLabelAway.setIcon(new ImageIcon(new ImageIcon(pulledImageOne).getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT)));
			matchup.add(picLabelAway);
		}
		
		JLabel vsLabel = new JLabel(teamOneName + attacherString + teamTwoName);
		vsLabel.setFont(matchupFont);
		matchup.add(vsLabel);
		
		if (teamTwoURL != null) {
			URL imageTwo = new URL(teamTwoURL);
			BufferedImage pulledImageTwo = ImageIO.read(imageTwo);
			JLabel picLabelHome = new JLabel();
			picLabelHome.setIcon(new ImageIcon(new ImageIcon(pulledImageTwo).getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT)));		
			matchup.add(picLabelHome);
		}
		
		// add everything to panel
		matchesBelow.add(matchup);
		
		//this.add(matchesBelow);
	}
	
	public JPanel returnPanel() {
		return this;
	}
	
	public void setBackground() {
		try {
			backgroundImage = ImageIO.read(new URL("https://wallpaperspot.net/images/2018/11/26/league-of-legends-wallpaper-2018-3.jpg"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Date returnFormattedDate(String unformattedDate, String formatStyle) {
		SimpleDateFormat formatter = new SimpleDateFormat(formatStyle);
		try {
            Date formattedDate = formatter.parse(unformattedDate.replaceAll("Z$", "+0000"));
            return formattedDate;
        } catch (ParseException e) {
        	System.out.println("Input String format doesn't follow input format style");
            e.printStackTrace();
            return null;
        }
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(backgroundImage, 0, 0, null);
	}

	@Override
	public String turnDateIntoString(Date input) {
		DateFormat df = new SimpleDateFormat("EEEE, HH:ss, MM/dd/yyyy");
		return df.format(input).toString();
	}

}
