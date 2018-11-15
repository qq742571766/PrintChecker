package hardware.print;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class printBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Toast.makeText(arg0, arg1.getAction(), Toast.LENGTH_SHORT).show();
		if(arg1.getAction()=="com.printer.printerror"){
			Log.d("print", "out of paper");
		}
	}

}
