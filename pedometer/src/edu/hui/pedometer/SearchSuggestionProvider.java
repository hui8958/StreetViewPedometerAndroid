package edu.hui.pedometer;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {

	public final static String AUTHORITY="com.android.fzmap.map.SearchSuggestionProvider";
	public final static int MODE=DATABASE_MODE_QUERIES;
	
	public SearchSuggestionProvider(){
		super();
		setupSuggestions(AUTHORITY, MODE);
	}
}
