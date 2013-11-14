package com.example.helisalvesti;

import java.io.File;
import java.io.IOException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Main extends Activity implements OnClickListener,
		OnCompletionListener {

	Button lindista, l6petaLindistus, esita, esitlusloend, l6petaK6ik, j2tka,
			paus, stop;
	TextView statusTextView, amplituud, klipiPikkus;
	MediaRecorder recorder;
	LinearLayout ll;
	MediaPlayer player;
	File audioFile;
	String salvestuskaust;
	RecordAmplitude recordAmplitude;
	boolean isRecording = false;
	long startAeg, stopAeg;
	int xpaus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SharedPreferences getPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		salvestuskaust = getPrefs.getString("salvestuskaust",
				"/HeliSalvesti/esitlusloend/");

		statusTextView = (TextView) this.findViewById(R.id.tvStaatus);
		l6petaLindistus = (Button) this.findViewById(R.id.bL6peta);
		l6petaK6ik = (Button) this.findViewById(R.id.bTyhista);
		lindista = (Button) this.findViewById(R.id.bREC);
		esitlusloend = (Button) this.findViewById(R.id.bEsitlusloend);
		esita = (Button) this.findViewById(R.id.bEsita);
		j2tka = (Button) this.findViewById(R.id.bJ2tka);
		paus = (Button) this.findViewById(R.id.bPaus);
		stop = (Button) this.findViewById(R.id.bStop);
		ll = (LinearLayout)this.findViewById(R.id.llAmpBar);
		lindista.setOnClickListener(this);
		esitlusloend.setOnClickListener(this);
		l6petaLindistus.setOnClickListener(this);
		l6petaK6ik.setOnClickListener(this);
		esita.setOnClickListener(this);
		esita.setVisibility(View.GONE);
		l6petaLindistus.setVisibility(View.GONE);
		l6petaK6ik.setVisibility(View.GONE);
		j2tka.setVisibility(View.GONE);
		paus.setVisibility(View.GONE);
		stop.setVisibility(View.GONE);
		statusTextView.setText("Vajuta REC");

		klipiPikkus = (TextView) this.findViewById(R.id.tvPikkus);
		klipiPikkus.setVisibility(View.GONE);
		startAeg = 0;

		amplituud = (TextView) this.findViewById(R.id.amplitudeTextView);
		amplituud.setVisibility(View.GONE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			Intent i = new Intent("com.example.helisalvesti.ABOUT");
			startActivity(i);

			break;
		case R.id.action_settings:
			Intent p = new Intent("com.example.helisalvesti.PREFS");
			startActivity(p);
			break;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		esita.setVisibility(View.VISIBLE);
		j2tka.setVisibility(View.GONE);
		paus.setVisibility(View.GONE);
		stop.setVisibility(View.GONE);
		l6petaLindistus.setVisibility(View.GONE);
		lindista.setEnabled(true);
		statusTextView.setText("Valmis");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == lindista) {
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			startAeg = System.currentTimeMillis();
			l6petaLindistus.setVisibility(View.VISIBLE);
			l6petaK6ik.setVisibility(View.VISIBLE);
			esita.setVisibility(View.GONE);
			esitlusloend.setVisibility(View.GONE);
			amplituud.setVisibility(View.VISIBLE);

			// Pathi seadistamine ja faili lindistamine
			File path = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + salvestuskaust);
			path.mkdirs();
			try {
				audioFile = File.createTempFile("recording", ".3gpp", path);
				recorder.setOutputFile(audioFile.getAbsolutePath());
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			isRecording = true;
			recordAmplitude = new RecordAmplitude();
			recordAmplitude.execute();

			statusTextView.setText("Lindistab");
			lindista.setEnabled(false);
		} else if (v == l6petaK6ik) {
			recordAmplitude.cancel(true);
			isRecording = false;
			amplituud.setVisibility(View.GONE);
			startAeg = 0;
			klipiPikkus.setVisibility(View.GONE);

			recorder.stop();
			recorder.release();

			statusTextView.setText("Vajuta REC");
			lindista.setEnabled(true);
			l6petaK6ik.setVisibility(View.GONE);
			l6petaLindistus.setVisibility(View.GONE);
			esitlusloend.setVisibility(View.VISIBLE);
			
			parameetridtagasi();
		} else if (v == l6petaLindistus) {
			recordAmplitude.cancel(true);
			isRecording = false;
			amplituud.setVisibility(View.GONE);

			stopAeg = System.currentTimeMillis();
			if (startAeg != 0) {
				long result = stopAeg - startAeg;
				int millis = (int) result;
				int second = (int) result / 1000;
				int minut = second / 60;
				millis = millis % 100;
				second = second % 60;
				klipiPikkus.setText(String.format("%d:%02d:%02d", minut,
						second, millis));
				startAeg = 0;
			} else {
				klipiPikkus.setVisibility(View.GONE);
			}

			recorder.stop();
			recorder.release();
			
			player = new MediaPlayer();
			player.setOnCompletionListener(this);

			try {
				player.setDataSource(audioFile.getAbsolutePath());
				player.prepare();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			statusTextView.setText("Valmis Esitama");
			esita.setVisibility(View.VISIBLE);
			esitlusloend.setVisibility(View.VISIBLE);
			lindista.setEnabled(true);
			l6petaK6ik.setVisibility(View.GONE);
			l6petaLindistus.setVisibility(View.GONE);
			parameetridtagasi();
		} else if (v == esitlusloend) {
			Intent i = new Intent(Main.this, Esitlusloend.class);
			startActivity(i);
		} else if (v == esita) {
			player.start();
			statusTextView.setText("Esitab");
			lindista.setEnabled(false);
			l6petaLindistus.setVisibility(View.GONE);
			esita.setVisibility(View.GONE);
			paus.setVisibility(View.VISIBLE);
			stop.setVisibility(View.VISIBLE);
		} else if(v == j2tka){
			player.seekTo(xpaus);
			player.start();
			j2tka.setVisibility(View.GONE);
			paus.setVisibility(View.VISIBLE);
			stop.setVisibility(View.VISIBLE);
		} else if(v == paus){
			xpaus = player.getCurrentPosition();
			player.pause();
			Log.v("log",""+xpaus);
			j2tka.setVisibility(View.VISIBLE);
			paus.setVisibility(View.GONE);
			stop.setVisibility(View.VISIBLE);
		} else if(v == stop){
			statusTextView.setText("Esitamine Peatatud");
			player.stop();
			paus.setVisibility(View.GONE);
			stop.setVisibility(View.GONE);
		}
	}

	public void parameetridtagasi() {
		// TODO Auto-generated method stub
		LayoutParams paramsBack = ll.getLayoutParams();
		paramsBack.height = 0;
		paramsBack.width = 0;
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	private class RecordAmplitude extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onProgressUpdate(Integer... progress) {

			if (progress[0] > 0) {
				int amp = progress[0]/120;
				int vamp = 40;
				LayoutParams params = ll.getLayoutParams();
				params.height = vamp;
				params.width = amp;
				
				amplituud.setTextColor(Color.WHITE);
				amplituud.setText(progress[0].toString());
			}

			stopAeg = System.currentTimeMillis();
			if (startAeg != 0) {
				long result = stopAeg - startAeg;
				int millis = (int) result;
				int second = (int) result / 1000;
				int minut = second / 60;
				millis = millis % 100;
				second = second % 60;
				klipiPikkus.setText(String.format("%d:%02d:%02d", minut,
						second, millis));
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			while (isRecording == true) {

				publishProgress(recorder.getMaxAmplitude());
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
