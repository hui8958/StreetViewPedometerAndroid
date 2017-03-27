package edu.hui.pedometer.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase db;
	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh");
	
	public DBManager(Context context) {
		helper = new DBHelper(context);
		//因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
		//所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = helper.getWritableDatabase();
	}
	
	/**
	 * add persons
	 * @param persons
	 */
	public void add(Exercise exercise) {
        db.beginTransaction();	//开始事务
        try {
        
        		db.execSQL("INSERT INTO exercise VALUES(NULL, ?, ?, ?, ?, ?, ?, ?)", new Object[]{exercise.date,exercise.steps, exercise.pace, exercise.distance,exercise.speed,exercise.calories,exercise.time});
        	
        	db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
        	db.endTransaction();	//结束事务
        }
	}
	

	public List<Exercise> query() {
		ArrayList<Exercise> exerciselist = new ArrayList<Exercise>();
		Cursor c = queryTheCursor();
        while (c.moveToNext()) {
        	Exercise exercise = new Exercise();
        	exercise._id = c.getInt(c.getColumnIndex("_id"));
        	exercise.steps = c.getInt(c.getColumnIndex("steps"));
        	exercise.pace = c.getInt(c.getColumnIndex("pace"));
        	exercise.distance = c.getFloat(c.getColumnIndex("distance"));
        	exercise.speed = c.getFloat(c.getColumnIndex("speed"));
        	exercise.calories = c.getInt(c.getColumnIndex("calories"));
        	exercise.date = c.getString(c.getColumnIndex("date"));
        	exercise.time = c.getInt(c.getColumnIndex("time"));
        	exerciselist.add(exercise);
        }
        c.close();
        return exerciselist;
	}
	
	/**
	 * query all persons, return cursor
	 * @return	Cursor
	 */
	public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM exercise", null);
        return c;
	}
	
	  public void deleteExercise(int id) {  
	       db.delete("Exercise", "_id = ?", new String[]{String.valueOf(id)});  
	    } 
	
	
	public List<Exercise> queryThisWeek(){
		ArrayList<Exercise> exerciselistweek = new ArrayList<Exercise>();
		
		Cursor c = queryTheCursor();
		int todaysWeek = this.DateToWeek(new Date());
		   while (c.moveToNext()) {
			//   Exercise exercise = new Exercise();
			   Date d = null;
			try {
				d = f.parse(c.getString(c.getColumnIndex("date")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(todaysWeek+"|"+DateToWeek(d));
			   if(todaysWeek == DateToWeek(d)){
				   Exercise exercise = new Exercise();
		        	exercise._id = c.getInt(c.getColumnIndex("_id"));
		        	exercise.steps = c.getInt(c.getColumnIndex("steps"));
		        	exercise.pace = c.getInt(c.getColumnIndex("pace"));
		        	exercise.distance = c.getFloat(c.getColumnIndex("distance"));
		        	exercise.speed = c.getFloat(c.getColumnIndex("speed"));
		        	exercise.calories = c.getInt(c.getColumnIndex("calories"));
		        	exercise.date = c.getString(c.getColumnIndex("date"));
		        	exercise.time = c.getInt(c.getColumnIndex("time"));
		        	exerciselistweek.add(exercise);
			   }
	        }
	        c.close();
		
		
	      return exerciselistweek;
	}
	
	public List<Exercise> queryThisMonth(){
		ArrayList<Exercise> exerciselistMonth = new ArrayList<Exercise>();
		
		Cursor c = queryTheCursor();
		int todaysMonth = this.DateToMonth(new Date());
		   while (c.moveToNext()) {
			//   Exercise exercise = new Exercise();
			   Date d = null;
			try {
				d = f.parse(c.getString(c.getColumnIndex("date")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(todaysMonth+"|"+DateToMonth(d));
			   if(todaysMonth == DateToMonth(d)){
				   Exercise exercise = new Exercise();
		        	exercise._id = c.getInt(c.getColumnIndex("_id"));
		        	exercise.steps = c.getInt(c.getColumnIndex("steps"));
		        	exercise.pace = c.getInt(c.getColumnIndex("pace"));
		        	exercise.distance = c.getFloat(c.getColumnIndex("distance"));
		        	exercise.speed = c.getFloat(c.getColumnIndex("speed"));
		        	exercise.calories = c.getInt(c.getColumnIndex("calories"));
		        	exercise.date = c.getString(c.getColumnIndex("date"));
		        	exercise.time = c.getInt(c.getColumnIndex("time"));
		        	exerciselistMonth.add(exercise);
			   }
	        }
	        c.close();
		
		
	      return exerciselistMonth;
	}
	
	
	/**
	 * close database
	 */
	public void closeDB() {
		db.close();
	}

	public  int DateToWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayIndex = calendar.get(Calendar.WEEK_OF_YEAR);
		
		
		return dayIndex ;
	}
	public int DateToMonth(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayIndex = calendar.get(Calendar.MONTH);
	

		return dayIndex ;
	}
}
