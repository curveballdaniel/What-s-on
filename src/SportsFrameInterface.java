import java.io.IOException;
import java.util.Date;

public interface SportsFrameInterface {
	// refresh all information shown on respective frame
	public void refreshInfo() throws IOException;
	// take an unformatted date in input format (i.e. xxxx-xx-xxTxx:xxZ), and return the String in a Date object
	public Date returnFormattedDate(String unformattedDate, String formatStyle);
	// take a date object, and return the appropriate string format for the frame
	public String turnDateIntoString(Date input);
}
