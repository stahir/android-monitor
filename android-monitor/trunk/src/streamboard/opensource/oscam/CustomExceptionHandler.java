package streamboard.opensource.oscam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.os.Environment;


public class CustomExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler _defaultUEH;
    private String _localPath;
    private File _root;
    
    public CustomExceptionHandler(String localPath) {
    	this._root = Environment.getExternalStorageDirectory();
    	this._localPath = localPath;
    	this._defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();
		String filename = "OscamMonitor.stacktrace";

		if (this._localPath != null) {
			writeToFile(stacktrace, filename);
		}

		this._defaultUEH.uncaughtException(t, e);
	}
	
	private void writeToFile(String stacktrace, String filename) {
		try {
			BufferedWriter bos = new BufferedWriter(new FileWriter(this._root + this._localPath + "/" + filename));
			bos.write(stacktrace);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
