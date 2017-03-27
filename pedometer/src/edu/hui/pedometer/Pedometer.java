/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.hui.pedometer;

import java.text.SimpleDateFormat;

import java.util.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import edu.hui.pedometer.R;
import edu.hui.pedometer.startMapActivity;
import edu.hui.pedometer.tools.Exercise;
import edu.hui.pedometer.tools.Utils;
import edu.hui.pedometer.tools.ExtendAppication;
import edu.hui.pedometer.tools.commandInfo;
import android.view.ViewGroup.LayoutParams;

public class Pedometer extends Activity {
	private static final String TAG = "Pedometer";
	private SharedPreferences mSettings;
	private PedometerSettings mPedometerSettings;
	private Utils mUtils;
	private ExtendAppication ws = null;
	private TextView mStepValueView;
	private TextView mPaceValueView;
	private TextView mDistanceValueView;
	private TextView mSpeedValueView;
	private TextView mCaloriesValueView;
	private TextView mTimerValueView;
	// private TextView serverBack;
	TextView mDesiredPaceView;
	private int mStepValue;// mStepValueView的值
	private int mPaceValue;// mPaceValueView的值
	private float mDistanceValue;// mDistanceValueView的值
	private float mSpeedValue;// mSpeedValueView的值
	private int mCaloriesValue;// mCaloriesValueView的值
	private float mDesiredPaceOrSpeed;//
	private int mMaintain;// 是否是爬山
	private float mMaintainInc;//
	private boolean mQuitting = false; //
	// Set when user selected Quit from menu, can be used by onPause, onStop,
	// onDestroy
	private int currentValue = 0;
	Calendar c = Calendar.getInstance();
	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh");
	Timer timer;
	private int recLen = 0;
	private boolean isPause;

	private XYSeries series;
	private XYMultipleSeriesDataset mDataset;
	private GraphicalView chart;
	private XYMultipleSeriesRenderer renderer;
	private int addX = -1, addY;
	ArrayList<Integer> xv,yv;
	private Context context;
	int steps;

	/**
	 * True, when service is running.
	 */
	private boolean mIsRunning;// 程序是否运行的标志位

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "[ACTIVITY] onCreate");
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		mStepValue = 0;
		mPaceValue = 0;
		setContentView(R.layout.mainlayout);
		ws = (ExtendAppication) this.getApplicationContext();
		ws.setMgr(this);
		mUtils = Utils.getInstance();
		timer = new Timer(true);
		timer.schedule(task, 1000, 1000); // 延时1000ms后执行，1000ms执行一次
		isPause = true;
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
		series = new XYSeries("Pace");
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(series);
		int color = Color.parseColor("#FFA500");
		PointStyle style = PointStyle.CIRCLE;
		renderer = buildRenderer(color, style, true);
		setChartSettings(renderer, "X", "Y", 0, 100, 0, 300, Color.WHITE,
				Color.WHITE);
		chart = ChartFactory.getLineChartView(context, mDataset, renderer);
		layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		xv = new ArrayList<Integer>();
		yv = new ArrayList<Integer>();
		steps = 0;
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String xTitle, String yTitle, double xMin, double xMax,
			double yMin, double yMax, int axesColor, int labelsColor) {
		// 有关对图表的渲染可参看api文档
		renderer.setChartTitle("pace");
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(Color.parseColor("#FFA500"));
		renderer.setLabelsColor(Color.WHITE);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.WHITE);
		renderer.setXLabels(20);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setXTitle("Time");
		renderer.setYTitle("Pace");
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setPointSize((float) 2);
		renderer.setShowLegend(false);
		renderer.setZoomButtonsVisible(true);
		  renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(true, false);
	}

	protected XYMultipleSeriesRenderer buildRenderer(int color,
			PointStyle style, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(color);
		r.setPointStyle(style);
		r.setFillPoints(fill);
		r.setLineWidth(3);
		renderer.addSeriesRenderer(r);

		return renderer;
	}

	// 开始函数，重写该函数，加入日志。
	@Override
	protected void onStart() {
		Log.i(TAG, "[ACTIVITY] onStart");
		super.onStart();
	}

	// 重写回复函数
	@Override
	protected void onResume() {
		Log.i(TAG, "[ACTIVITY] onResume");
		super.onResume();

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mPedometerSettings = new PedometerSettings(mSettings);

		mUtils.setSpeak(mSettings.getBoolean("speak", false));

		// Read from preferences if the service was running on the last onPause
		mIsRunning = mPedometerSettings.isServiceRunning();
		ws.getUdpMgr().send(commandInfo.hello,
				mPedometerSettings.getIpAddress(),8001);
		// Start the service if this is considered to be an application start
		// (last onPause was long ago)
		if (!mIsRunning && mPedometerSettings.isNewStart()) {
			startStepService();
			bindStepService();
			isPause = false;
		} else if (mIsRunning) {
			bindStepService();
			isPause = false;
		}

		mPedometerSettings.clearServiceRunning();

		mStepValueView = (TextView) findViewById(R.id.step_value);
		mPaceValueView = (TextView) findViewById(R.id.pace_value);
		mDistanceValueView = (TextView) findViewById(R.id.distance_value);
		mSpeedValueView = (TextView) findViewById(R.id.speed_value);
		mCaloriesValueView = (TextView) findViewById(R.id.calories_value);
		mDesiredPaceView = (TextView) findViewById(R.id.desired_pace_value);
		mTimerValueView = (TextView) findViewById(R.id.recTime);
		// serverBack = (TextView) findViewById(R.id.TextView01);
		// serverBack.setText(ws.onstart());

		TextPaint tp2 = mStepValueView.getPaint();
		float[] direction = new float[] { 1, 1, 1 };
		// 设置环境光亮度
		float light = 0.4f;
		// 选择要应用的反射等级
		float specular = 6;
		// 向mask应用一定级别的模糊
		float blur = 3.5f;
		EmbossMaskFilter maskfilter = new EmbossMaskFilter(direction, light,
				specular, blur);
		tp2.setMaskFilter(maskfilter);

		mMaintain = mPedometerSettings.getMaintainOption();
		((LinearLayout) this.findViewById(R.id.desired_pace_control))
				.setVisibility(mMaintain != PedometerSettings.M_NONE ? View.VISIBLE
						: View.GONE);
		if (mMaintain == PedometerSettings.M_PACE) {
			mMaintainInc = 5f;
			mDesiredPaceOrSpeed = (float) mPedometerSettings.getDesiredPace();
		} else if (mMaintain == PedometerSettings.M_SPEED) {
			mDesiredPaceOrSpeed = mPedometerSettings.getDesiredSpeed();
			mMaintainInc = 0.1f;
		}
		Button button1 = (Button) findViewById(R.id.button_desired_pace_lower);
		button1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDesiredPaceOrSpeed -= mMaintainInc;
				mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
				displayDesiredPaceOrSpeed();
				setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
			}
		});
		Button button2 = (Button) findViewById(R.id.button_desired_pace_raise);
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDesiredPaceOrSpeed += mMaintainInc;
				mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
				displayDesiredPaceOrSpeed();
				setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
			}
		});
		if (mMaintain != PedometerSettings.M_NONE) {
			((TextView) findViewById(R.id.desired_pace_label))
					.setText(mMaintain == PedometerSettings.M_PACE ? R.string.desired_pace
							: R.string.desired_speed);
		}

		displayDesiredPaceOrSpeed();
	}

	private void displayDesiredPaceOrSpeed() {
		if (mMaintain == PedometerSettings.M_PACE) {
			mDesiredPaceView.setText("" + (int) mDesiredPaceOrSpeed);
		} else {
			mDesiredPaceView.setText("" + mDesiredPaceOrSpeed);
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "[ACTIVITY] onPause");
		if (mIsRunning) {
			unbindStepService();
		}
		if (mQuitting) {
			mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
		} else {
			mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
		}
		isPause = true;
		super.onPause();
		savePaceSetting();

	}

	@Override
	protected void onStop() {
		Log.i(TAG, "[ACTIVITY] onStop");
		isPause = true;
		super.onStop();

	}

	protected void onDestroy() {
		Log.i(TAG, "[ACTIVITY] onDestroy");
		super.onDestroy();
		ws.getMgr().closeDB();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	protected void onRestart() {
		Log.i(TAG, "[ACTIVITY] onRestart");
		super.onDestroy();
	}

	private void setDesiredPaceOrSpeed(float desiredPaceOrSpeed) {
		if (mService != null) {
			if (mMaintain == PedometerSettings.M_PACE) {
				mService.setDesiredPace((int) desiredPaceOrSpeed);
			} else if (mMaintain == PedometerSettings.M_SPEED) {
				mService.setDesiredSpeed(desiredPaceOrSpeed);
			}
		}
	}

	private void savePaceSetting() {
		mPedometerSettings.savePaceOrSpeedSetting(mMaintain,
				mDesiredPaceOrSpeed);
	}

	private StepService mService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((StepService.StepBinder) service).getService();

			mService.registerCallback(mCallback);
			mService.reloadSettings();

		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	private void startStepService() {
		if (!mIsRunning) {
			Log.i(TAG, "[SERVICE] Start");
			mIsRunning = true;
			startService(new Intent(Pedometer.this, StepService.class));
		}
	}

	private void bindStepService() {
		Log.i(TAG, "[SERVICE] Bind");
		bindService(new Intent(Pedometer.this, StepService.class), mConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private void unbindStepService() {
		Log.i(TAG, "[SERVICE] Unbind");
		unbindService(mConnection);
	}

	private void stopStepService() {
		Log.i(TAG, "[SERVICE] Stop");
		if (mService != null) {
			Log.i(TAG, "[SERVICE] stopService");
			stopService(new Intent(Pedometer.this, StepService.class));
		}
		mIsRunning = false;
	}

	private void resetValues(boolean updateDisplay) {
		if (mService != null && mIsRunning) {
			mService.resetValues();
		} else {
			mStepValueView.setText("0");
			mPaceValueView.setText("0");
			mDistanceValueView.setText("0");
			mSpeedValueView.setText("0");
			mCaloriesValueView.setText("0");
			SharedPreferences state = getSharedPreferences("state", 0);
			SharedPreferences.Editor stateEditor = state.edit();
			if (updateDisplay) {
				stateEditor.putInt("steps", 0);
				stateEditor.putInt("pace", 0);
				stateEditor.putFloat("distance", 0);
				stateEditor.putFloat("speed", 0);
				stateEditor.putFloat("calories", 0);

				stateEditor.commit();
			}
		}
	}

	private static final int MENU_SETTINGS = 8;
	private static final int MENU_QUIT = 9;
	private static final int MENU_RECORD = 0;

	private static final int MENU_PAUSE = 1;
	private static final int MENU_RESUME = 2;
	private static final int MENU_RESET = 3;
	private static final int MENU_MAP = 4;

	/* Creates the menu items */
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mIsRunning) {
			menu.add(0, MENU_PAUSE, 0, R.string.pause)
					.setIcon(android.R.drawable.ic_media_pause)
					.setShortcut('1', 'p');
		} else {
			menu.add(0, MENU_RESUME, 0, R.string.resume)
					.setIcon(android.R.drawable.ic_media_play)
					.setShortcut('1', 'p');
		}
		menu.add(0, MENU_RESET, 0, R.string.reset)
				.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
				.setShortcut('2', 'r');

		menu.add(0, MENU_MAP, 0, R.string.map)
				.setIcon(android.R.drawable.ic_dialog_map)
				.setShortcut('4', 'm')
				.setIntent(new Intent(this, startMapActivity.class));

		menu.add(0, MENU_SETTINGS, 0, R.string.settings)
				.setIcon(android.R.drawable.ic_menu_preferences)
				.setShortcut('8', 's')
				.setIntent(new Intent(this, Settings.class));

		menu.add(0, MENU_QUIT, 0, R.string.quit)
				.setIcon(android.R.drawable.ic_lock_power_off)
				.setShortcut('9', 'q');

		menu.add(0, MENU_RECORD, 0, "Record")
				.setIcon(android.R.drawable.ic_input_get).setShortcut('0', 'r')
				.setIntent(new Intent(this, recordList.class));
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PAUSE:
			unbindStepService();
			stopStepService();
			isPause = true;
			return true;
		case MENU_RESUME:
			startStepService();
			bindStepService();
			isPause = false;
			return true;
		case MENU_RESET:
			resetValues(true);
			this.recLen = 0;
			this.mTimerValueView.setText(this.GetRecTime(0));
			 resetChart();
			return true;
		case MENU_QUIT:

			addExercise();

			return true;
		}
		return false;
	}

	// TODO: unite all into 1 type of message
	private StepService.ICallback mCallback = new StepService.ICallback() {
		public void stepsChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
		}

		public void paceChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
		}

		public void distanceChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG,
					(int) (value * 1000), 0));
		}

		public void speedChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG,
					(int) (value * 1000), 0));
		}

		public void caloriesChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG,
					(int) (value), 0));
		}
	};
	TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message();
			message.what = 6;
			mHandler.sendMessage(message);

		}
	};

	private static final int STEPS_MSG = 1;
	private static final int PACE_MSG = 2;
	private static final int DISTANCE_MSG = 3;
	private static final int SPEED_MSG = 4;
	private static final int CALORIES_MSG = 5;
	private static final int time_increase = 6;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STEPS_MSG:
				mStepValue = (int) msg.arg1;
				if (currentValue != mStepValue) {
					currentValue = mStepValue;
					ws.getUdpMgr().send(commandInfo.forward,
							mPedometerSettings.getIpAddress(),8001);
					ws.getUdpMgr().send(commandInfo.detail+mDistanceValue+"|"+mPaceValue+"|"+mSpeedValue+"|"+mCaloriesValue+"|"+mStepValue,
							mPedometerSettings.getIpAddress(),8002);
					
					// ws.getResponse(commandInfo.forward);
					// serverBack.setText(ws.getResponse("F"));
					// serverBack.setText(ws.getResponse((String.valueOf(mStepValue))));
				}
				mStepValueView.setText("" + mStepValue);

				// serverBack.setText(String.valueOf(mStepValue));
				break;
			case PACE_MSG:
				mPaceValue = msg.arg1;
				if (mPaceValue <= 0) {
					mPaceValueView.setText("0");
				} else {
					mPaceValueView.setText("" + (int) mPaceValue);
				}
				break;
			case DISTANCE_MSG:
				mDistanceValue = ((int) msg.arg1) / 1000f;
				if (mDistanceValue <= 0) {
					mDistanceValueView.setText("0");
				} else {
					mDistanceValueView
							.setText(("" + (mDistanceValue + 0.000001f))
									.substring(0, 5));
				}
				break;
			case SPEED_MSG:
				mSpeedValue = ((int) msg.arg1) / 1000f;
				if (mSpeedValue <= 0) {
					mSpeedValueView.setText("0");
				} else {
					mSpeedValueView.setText(("" + (mSpeedValue + 0.000001f))
							.substring(0, 4));
				}
				break;
			case CALORIES_MSG:
				mCaloriesValue = msg.arg1;
				if (mCaloriesValue <= 0) {
					mCaloriesValueView.setText("0");
				} else {
					mCaloriesValueView.setText("" + (int) mCaloriesValue);
				}
				break;
			case time_increase:
				if (!isPause) {
					recLen++;
					updateChart(mPaceValue);
					mTimerValueView.setText(GetRecTime(recLen));

				}

				break;

			default:
				super.handleMessage(msg);
			}
		}

	};

	private void updateChart(int value) {

		// 设置好下一个需要增加的节点
		
		addX = recLen;
	
			addY = value;

		
		// 移除数据集中旧的点集
		mDataset.removeSeries(series);

		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
		int length = series.getItemCount();
		if (length > 100) {
			setChartSettings(renderer, "X", "Y", length-100, length, 0, 300, Color.WHITE,
					Color.WHITE);
		}
		for (int i = 0; i <length; i++) {
			xv.add(i, (int) series.getX(i));
			yv.add(i, (int) series.getY(i));
		
		
		}
		

		// 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
	

		// 点集先清空，为了做成新的点集而准备
		series.clear();

		// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
		// 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
		series.add(addX, addY);
		for (int k = 0; k <length; k++) {
			series.add(xv.get(k), yv.get(k));
		}

		// 在数据集中添加新的点集
		mDataset.addSeries(series);

		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
		chart.invalidate();
		
	}
	private void resetChart() {

		// 设置好下一个需要增加的节点
		
		addX = 0;
			addY = 0;
		// 移除数据集中旧的点集
		mDataset.removeSeries(series);

		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
		
		
			setChartSettings(renderer, "X", "Y", 0, 100, 0, 300, Color.WHITE,
					Color.WHITE);
		
		

		// 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
	

		// 点集先清空，为了做成新的点集而准备
		series.clear();

		// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
		// 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
		
	

		// 在数据集中添加新的点集
		mDataset.addSeries(series);

		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
		chart.invalidate();
		
	}

	public CharSequence GetRecTime(int senconds) {
		if (senconds == 0)
			return "00:00:00";
		int hour = senconds / (60 * 60);
		int minits = (senconds % (60 * 60)) / (60);

		return intToTime(hour) + ":" + intToTime(minits) + ":"
				+ intToTime(senconds % 60);
	}

	public static String intToTime(int time) {
		if (time <= 0)
			return "00";
		if (time < 10)
			return "0" + time;
		else
			return time + "";
	}

	public void addExercise() {
		if (mStepValue != 0) {
			if (saveOnQuite()) {
				Toast.makeText(getApplicationContext(),
						"Successfully save the date.", Toast.LENGTH_SHORT)
						.show();
				Log.i(TAG, "[ACTIVITY] save into database");

			}
		} else {
			exit();
		}

	}

	public void query(View view) {
		List<Exercise> exercises = ws.getMgr().query();
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (Exercise exercise : exercises) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("name", exercise.date);
			map.put("info", exercise.steps + " steps, " + exercise.distance
					+ " miles");
			list.add(map);
		}
		// SimpleAdapter adapter = new SimpleAdapter(this, list,
		// android.R.layout.simple_list_item_2,
		// new String[]{"name", "info"}, new int[]{android.R.id.text1,
		// android.R.id.text2});
		// listView.setAdapter(adapter);
	}

	boolean save = false;

	public boolean saveOnQuite() {

		new AlertDialog.Builder(this)
				.setTitle("Exit")
				.setMessage("Save the data?")
				.setIcon(R.drawable.icon)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								setResult(RESULT_OK);// 确定按钮事件
								Exercise es = new Exercise(
										f.format(c.getTime()), mStepValue,
										mPaceValue, mDistanceValue,
										mSpeedValue, mCaloriesValue, recLen);
								ws.getMgr().add(es);

								save = true;
								exit();

							}
						})
				.setNeutralButton("Don't save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								exit();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// 取消按钮事件

							}
						}).show();

		return save;

	}

	public void exit() {
		ws.getUdpMgr().send("E:", mPedometerSettings.getIpAddress(),8001);
		ws.getUdpMgr().send(commandInfo.bye, mPedometerSettings.getIpAddress(),8001);

		resetValues(false);
		if(!isPause){
			unbindStepService();
			
		}
		stopStepService();
		mQuitting = true;
		finish();
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.saveOnQuite();
			return false;
		}
		return false;
	}

}