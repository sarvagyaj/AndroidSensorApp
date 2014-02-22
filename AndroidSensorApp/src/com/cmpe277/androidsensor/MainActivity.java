package com.cmpe277.androidsensor;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView timer;
	Button bStart;
	Button bLightOff;
	Handler timeHandler = new Handler();
	Handler cameraHandler = new Handler();
	Long startTime = 0L;
	Long updatedTimeValue = 0L;
	Camera camera;
	Parameters p;
	boolean isBlinking;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bStart = (Button) findViewById(R.id.startButton);
		timer = (TextView) findViewById(R.id.timerValue);
		bLightOff = (Button) findViewById(R.id.CloseButton);
		camera = Camera.open();

		bStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startTime = SystemClock.uptimeMillis();
				p = camera.getParameters();
				// call the timer thread with 0 sec delay
				timeHandler.postDelayed(updateTimeThread, 0); 
				cameraHandler.postDelayed(startLightThread, 5000);
				cameraHandler.postDelayed(TurnOffLightThread, 10000);
			}
		});

		bLightOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// terminates the whole java machine which is dedicated to
				// running the application and hence all thread associated with
				// it.
				System.exit(0);
			}
		});
	}

	private Runnable updateTimeThread = new Runnable() {

		@Override
		public void run() {
			updatedTimeValue = SystemClock.uptimeMillis() - startTime;
			int millisec = (int) (updatedTimeValue % 1000);
			int sec = (int) (updatedTimeValue / 1000);
			int min = sec / 60;
			sec = sec % 60;
			timer.setText("" + min + ":" + String.format("%02d", sec) + ":"
					+ String.format("%02d", millisec));
			// call itself with 0 millisec delay, so that times is updated with each milliseconds
			timeHandler.postDelayed(updateTimeThread, 0); 
		}
	};

	private Runnable startLightThread = new Runnable() {
		@Override
		public void run() {
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(p);
			camera.startPreview();
			if (isBlinking) {
				cameraHandler.postDelayed(TurnOffLightThread, 500);
			}
		}
	};

	private Runnable TurnOffLightThread = new Runnable() {

		@Override
		public void run() {
			isBlinking = true;
			p.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(p);
			camera.startPreview();
			//call startLight and TurnOffLight recursively to create the blinking effect
			if (isBlinking) {
				cameraHandler.postDelayed(startLightThread, 500);
			}

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
