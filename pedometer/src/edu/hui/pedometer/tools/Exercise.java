package edu.hui.pedometer.tools;

public class Exercise {
	public int _id;  
    public String date;  
	public int steps;
	public int pace;
	public float distance;
	public float speed;
	public int calories;
	public int time;

	public Exercise() {

	}

	public Exercise(String date, int steps, int pace, float distance,
			float speed, int calories, int time) {
		this.date = date;
		this.steps = steps;
		this.pace = pace;
		this.distance = distance;
		this.speed = speed;
		this.calories = calories;
		this.time = time;
	}

}
