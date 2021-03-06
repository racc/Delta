package io.leocad.delta;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.WindowManager;

public class Delta<T> {
	
	private BenchmarkResult<T> mResult;

	public void onPreExecute() {
		//Override me
	}
	public void onPostExecute(BenchmarkResult<T> result) {
		//Override me
	}

	public void benchmark(final Activity activity, final Class<? extends BenchmarkTask<T>> classType, final long numCycles, final long numWarmupCycles) {

		lockOrientationChanges(activity);
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		onPreExecute();

		new Thread() {
			@Override
			public void run() {

				try {
					BenchmarkTask<T> taskInstance = classType.newInstance();
					mResult = taskInstance.execute(numCycles, numWarmupCycles);

				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();

				} finally {

					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							onPostExecute(mResult);
							mResult = null; //Release
						}
					});
				}
			};
		}.start();
	}
	private void lockOrientationChanges(Activity activity) {
		//Lock orientation changes, to avoid the benchmark to start over if the device rotates
		int activityCurrentOrientation = activity.getResources().getConfiguration().orientation;
		
		if (activityCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
}
