package edu.hui.pedometer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import edu.hui.pedometer.tools.Exercise;
import edu.hui.pedometer.tools.ExtendAppication;

public class recordList extends Activity implements OnItemClickListener {

	private ListView listv;

	private ExtendAppication ws = null;
	SimpleAdapter adapter =null;
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.recordlist);
		listv = (ListView) findViewById(R.id.recordlist);

		ws = (ExtendAppication) this.getApplicationContext();

	
		listv.setAdapter(getAdapter());
		listv.setOnItemClickListener(this);

		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem helpMItm = menu.add(1, 1, 1, "Total of this week");
	helpMItm.setIcon(android.R.drawable.ic_menu_view);

		MenuItem exitMItm = menu.add(1, 2, 2, "Total of this month");
		exitMItm.setIcon(android.R.drawable.ic_menu_view);
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {

		int steps =0;
		int pace = 0;
		float distance = 0;
		float speed = 0;
		int calories = 0;
		int time = 0;
		switch (item.getItemId()) {
		case 1:
			List<Exercise> thisweek = ws.getMgr().queryThisWeek();
			
			for( Exercise e : thisweek){
				System.out.println("steps: "+e.steps);
				steps +=e.steps;
				pace +=e.pace;
				distance +=e.distance;
				speed +=e.speed;
				calories +=e.calories;
				time +=e.time;
			}
		
			this.showAlertDialog("Total", "This week", steps, distance, pace, speed, calories, time);
			
			break;
		case 2:
			
			List<Exercise> thismonth = ws.getMgr().queryThisMonth();
			
			for( Exercise e : thismonth){
				System.out.println("steps: "+e.steps);
				steps +=e.steps;
				pace +=e.pace;
				distance +=e.distance;
				speed +=e.speed;
				calories +=e.calories;
				time+=e.time;
			}
		
			this.showAlertDialog("Total", "This Month", steps, distance, pace, speed, calories, time);
			
			
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}


		@SuppressWarnings("unchecked")
		@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		 ListView listView = (ListView) arg0;
		HashMap<String, String> map = (HashMap<String, String>) listView
				.getItemAtPosition(arg2);
		final String _id = map.get("_id");
		String steps = map.get("steps");
		String pace = map.get("pace");
		String distance = map.get("distance");
		String speed = map.get("speed");
		String calories = map.get("calories");
		String date = map.get("date");
		String time = String.valueOf(GetRecTime(Integer.parseInt(map.get("time"))));
		new AlertDialog.Builder(this).setTitle("Detail")
        .setMessage("Date: "+ date+"\n"
        		+"Steps: "+steps+"\n"
        		+"Distance(Km) : "+distance+"\n"
        		+"Steps / Minutes: "+pace+"\n"
        		+"Km / hour: "+speed+"\n"
        		+"Calories Burned: "+calories+"\n"
        		+"Time: "+time
        		)
        .setIcon(R.drawable.icon)
        .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	ws.getMgr().deleteExercise(Integer.parseInt(_id));
            	listv.setAdapter(getAdapter());
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //������������

            }
        })
        .show();
	
	}	
		public void showAlertDialog(String title, String date, int steps, float distance, int pace, float speed,int calories,int time){
		
			if(title.equals("Total")){
				new AlertDialog.Builder(this).setTitle(title)
		        .setMessage(date+"\n"
		        		+"Steps: "+steps+"\n"
		        		+"Distance(km) : "+distance+"\n"
		        		+"Steps / Minutes: "+pace+"\n"
		        		+"Km / hour: "+speed+"\n"
		        		+"Calories Burned: "+calories+"\n"
		        		+"Time: "+this.GetRecTime(time))
		        .setIcon(R.drawable.icon)
		        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

		            public void onClick(DialogInterface dialog, int whichButton) {
		                setResult(RESULT_OK);//������������
		                
		            }
		        })
		        .show();
			
			
			
			}else{
			new AlertDialog.Builder(this).setTitle(title)
	        .setMessage(date+"\n"
	        		+"Steps: "+steps+"\n"
	        		+"Distance(km) : "+distance+"\n"
	        		+"Steps / Minutes: "+pace+"\n"
	        		+"Km / hour: "+speed+"\n"
	        		+"calories Burned: "+calories+"\n"
	        		+"Time: "+this.GetRecTime(time))
	        .setIcon(R.drawable.icon)
	        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface dialog, int whichButton) {
	                setResult(RESULT_OK);//������������
	                
	            }
	        })
	        .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	           	 
	            
	            }
	        })
	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                //������������

	            }
	        })
	        .show();
			}
		
		
		
		}
		
		public SimpleAdapter getAdapter(){
			
			List<Exercise> exercises = ws.getMgr().query();
			ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
			for (Exercise exercise : exercises) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("_id", String.valueOf(exercise._id));
				map.put("name", exercise.date);
				map.put("info", exercise.steps + " steps, " + exercise.distance
						+ " miles");
				map.put("steps", String.valueOf(exercise.steps));
				map.put("pace", String.valueOf(exercise.pace));
				map.put("distance", String.valueOf(exercise.distance));
				map.put("speed", String.valueOf(exercise.speed));
				map.put("calories", String.valueOf(exercise.calories));
				map.put("date", String.valueOf(exercise.date));
				map.put("time", String.valueOf(exercise.time));
				list.add(map);
				
			}
			SimpleAdapter sadapter =  new SimpleAdapter(this, list,
					android.R.layout.simple_list_item_2, new String[] { "name",
			"info" }, new int[] { android.R.id.text1,
			android.R.id.text2 });
			return sadapter;
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

}
